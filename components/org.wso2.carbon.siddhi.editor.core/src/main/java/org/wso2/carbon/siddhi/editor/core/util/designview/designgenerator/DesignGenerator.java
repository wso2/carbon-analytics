/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.Edge;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.EventFlow;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.SiddhiAppConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregateByTimePeriod;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.AnnotationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.AnnotationValue;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.ListAnnotationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sink.SinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.source.SourceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.source.SourceMap;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.attributesselection.AttributesSelectionConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.QueryConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.regexpatterns.CodeToDesignRegexPatterns;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGeneratorHelperException;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.factories.AnnotationConfigFactory;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiAnnotationTypes;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.DesignGeneratorHelper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.aggregation.TimePeriod.Duration;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.*;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.selection.BasicSelector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.TimeConstant;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Generates Siddhi app elements for the design view
 */
public class DesignGenerator {
    private String siddhiAppString;
    private SiddhiApp siddhiApp;
    private SiddhiAppRuntime siddhiAppRuntime;
    private SiddhiAppConfig siddhiAppConfig;
    private Map<String, Edge> edges;

    /**
     * Gets EventFlow configuration for a given Siddhi app code string
     * @param siddhiAppString   Code representation of the Siddhi app
     * @return                  Event flow representation of the Siddhi app
     */
    public EventFlow getEventFlow(String siddhiAppString) {
        try {
            this.siddhiAppString = siddhiAppString;
            siddhiApp = SiddhiCompiler.parse(siddhiAppString);
            siddhiAppRuntime = new SiddhiManager().createSiddhiAppRuntime(siddhiApp);
        } catch (Exception e) {
            // Runtime exception occurred. Eg: Missing a library for a particular source
            throw new SiddhiAppCreationException(e.getMessage());
        }
        siddhiAppConfig = new SiddhiAppConfig();
        edges = new HashMap<>();
        loadAppNameAndDescription();
        loadTriggers();
        loadStreams();

        // loadSources();

        loadTables();
        loadWindows();
        loadAggregations();
        loadFunctions();
        loadExecutionElements();



        // TODO: 3/27/18 implement
//        return null;
        return new EventFlow(siddhiAppConfig, Collections.emptyList());
    }

    /**
     * Loads name & description from the SiddhiApp
     */
    private void loadAppNameAndDescription() {
        for (Annotation annotation : siddhiApp.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase(SiddhiAnnotationTypes.NAME)) {
                siddhiAppConfig.setAppName(annotation.getElements().get(0).getValue());
            } else if (annotation.getName().equalsIgnoreCase(SiddhiAnnotationTypes.DESCRIPTION)) {
                siddhiAppConfig.setAppDescription(annotation.getElements().get(0).getValue());
            }
        }
    }

    /**
     * Loads Triggers from the SiddhiApp
     */
    private void loadTriggers() {
        final String EVERY_SPLIT_KEYWORD = "every ";
        for (TriggerDefinition triggerDefinition : siddhiApp.getTriggerDefinitionMap().values()) {
            // Get 'at'
            String at = "";
            if (triggerDefinition.getAtEvery() != null) {
                at = EVERY_SPLIT_KEYWORD +
                        ConfigBuildingUtilities.getDefinition(triggerDefinition, siddhiAppString)
                                .split(EVERY_SPLIT_KEYWORD)[1];
            } else if (triggerDefinition.getAt() != null) {
                at = triggerDefinition.getAt();
            }

            siddhiAppConfig.add(new TriggerConfig(
                    triggerDefinition.getId(),
                    triggerDefinition.getId(),
                    at,
                    null)); // TODO: 3/29/18 annotationList
        }
    }

    /**
     * Returns the availability of a Trigger with the given name, in the given Siddhi app
     *
     * @param triggerName   Name of the Trigger
     * @param siddhiApp     Siddhi app in which, availability of Trigger is searched
     * @return              Availability of Trigger with the given name, in given Siddhi app
     */
    private boolean isTriggerDefined(String triggerName, SiddhiApp siddhiApp) {
        return (siddhiApp.getTriggerDefinitionMap().size() != 0 &&
                siddhiApp.getTriggerDefinitionMap().containsKey(triggerName));
    }

    /**
     * Loads Streams from the SiddhiAppRuntime
     */
    private void loadStreams() {
        for (StreamDefinition streamDefinition : siddhiAppRuntime.getStreamDefinitionMap().values()) {
            if (!isTriggerDefined(streamDefinition.getId(), siddhiApp)) {
                siddhiAppConfig.add(generateStreamConfig(streamDefinition, false));
            }
        }
        // Inner Streams
        for (Map<String, AbstractDefinition> abstractDefinitionMap :
                siddhiAppRuntime.getPartitionedInnerStreamDefinitionMap().values()) {
            for (AbstractDefinition abstractDefinition : abstractDefinitionMap.values()) {
                if (abstractDefinition instanceof StreamDefinition) {
                    siddhiAppConfig.add(generateStreamConfig((StreamDefinition) abstractDefinition, true));
                } else {
                    throw new IllegalArgumentException("The partitioned inner stream definition map does not have an " +
                            "instance of class 'StreamDefinition'");
                }
            }
        }
    }

    /**
     * Loads Sources from the SiddhiAppRuntime
     */
    private void loadSources() {
        for (List<Source> source : siddhiAppRuntime.getSources()) {
            generateSourceOptions(source.get(0).getStreamDefinition().toString());
            // source.get(0); // this is a source config
            siddhiAppConfig.add(new SourceConfig(
                    null,
                    null,
                    null,
                    null)); // TODO: 3/28/18 new SourceConfig()
        }
    }

    /**
     * Loads Sinks from the SiddhiAppRuntime
     */
    private void loadSinks() {
        for (List<Sink> sink : siddhiAppRuntime.getSinks()) {
            // sink.get(0); // this is a sink config
            siddhiAppConfig.add(new SinkConfig(null, null, null, null)); // TODO: 3/28/18 new SinkConfig()
        }
    }

    /**
     * Loads Tables from the Siddhi App
     */
    private void loadTables() {
        for (TableDefinition tableDefinition : siddhiApp.getTableDefinitionMap().values()) {
            List<AnnotationConfig> annotationConfigs = new ArrayList<>();
            for (Annotation annotation : tableDefinition.getAnnotations()) {
                annotationConfigs.add(generateStreamOrTableAnnotationConfig(annotation));
            }
            siddhiAppConfig.add(new TableConfig(
                    tableDefinition.getId(),
                    tableDefinition.getId(),
                    generateAttributeConfigs(tableDefinition.getAttributeList()),
                    null, // TODO: 3/29/18 store
                    annotationConfigs));
        }
    }

    /**
     * Loads Windows from the SiddhiAppRuntime
     */
    private void loadWindows() {
        // Defined Windows
        for (WindowDefinition windowDefinition : siddhiApp.getWindowDefinitionMap().values()) {
            List<String> parameters = new ArrayList<>();
            for (Expression expression : windowDefinition.getWindow().getParameters()) {
                parameters.add(ConfigBuildingUtilities.getDefinition(expression, siddhiAppString));
            }
            siddhiAppConfig.add(new WindowConfig(
                    windowDefinition.getId(),
                    windowDefinition.getId(),
                    generateAttributeConfigs(windowDefinition.getAttributeList()),
                    windowDefinition.getWindow().getName(),
                    parameters,
                    windowDefinition.getOutputEventType().name(),
                    null)); // TODO: 3/29/18 annotations might not be possible
        }
    }

    /**
     * Loads Aggregations from the SiddhiApp
     */
    private void loadAggregations() {
        for (AggregationDefinition aggregationDefinition : siddhiApp.getAggregationDefinitionMap().values()) {
            AttributesSelectionConfig selectedAttributesConfig;
            List<String> groupBy = new ArrayList<>();

            if (aggregationDefinition.getSelector() instanceof BasicSelector) {
                BasicSelector selector = (BasicSelector) aggregationDefinition.getSelector();
                AttributesSelectionConfigGenerator attributesSelectionConfigGenerator =
                        new AttributesSelectionConfigGenerator();
                selectedAttributesConfig =
                        attributesSelectionConfigGenerator.generateAttributesSelectionConfig(
                                selector.getSelectionList());
                // Populate 'groupBy' list
                for (Variable variable : selector.getGroupByList()) {
                    groupBy.add(variable.getAttributeName());
                }
            } else {
                throw new IllegalArgumentException("Selector of AggregationDefinition is not of class BasicSelector");
            }

            List<AnnotationConfig> annotationList = new ArrayList<>();
            for (Annotation annotation : aggregationDefinition.getAnnotations()) {
                annotationList.add(generateStreamOrTableAnnotationConfig(annotation));
            }

            // For creating 'aggregate by time' object
            List<Duration> aggregationTimePeriodDurations = aggregationDefinition.getTimePeriod().getDurations();

            siddhiAppConfig.add(new AggregationConfig(
                    aggregationDefinition.getId(),
                    aggregationDefinition.getId(),
                    aggregationDefinition.getBasicSingleInputStream().getStreamId(),
                    selectedAttributesConfig,
                    groupBy,
                    aggregationDefinition.getAggregateAttribute().getAttributeName(),
                    new AggregateByTimePeriod(
                            (aggregationTimePeriodDurations.get(0)).name(),
                            (aggregationTimePeriodDurations.get(aggregationTimePeriodDurations.size() - 1)).name()),
                    null, // TODO: 4/6/18 store
                    annotationList));
        }
    }

    private void loadFunctions() {
        // TODO: 3/28/18 implement 
    }

    /**
     * Loads Execution Elements from the Siddhi App
     */
    private void loadExecutionElements() {
        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            if (executionElement instanceof Query) {
                addQueryConfig((Query) executionElement);
            } else if (executionElement instanceof Partition) {
                addPartitionConfig((Partition) executionElement);
            } else {
                throw new IllegalArgumentException("Unable create configuration for execution element");
            }
        }

        // TODO: 3/28/18 implement
    }

    /* EXECUTIONAL ELEMENTS [START] */

    /**
     * Adds config for the given query, in SiddhiAppConfig
     * @param query     Siddhi query
     */
    private void addQueryConfig(Query query) {
        QueryConfigGenerator queryConfigGenerator = new QueryConfigGenerator();
        QueryConfig queryConfig = queryConfigGenerator.generateQueryConfig(query, siddhiAppString, siddhiApp);
        siddhiAppConfig.add(queryConfig);
    }

    /**
     * Adds config for the given partition, in SiddhiAppConfig
     * @param partition     Siddhi partition
     */
    private void addPartitionConfig(Partition partition) {
        // TODO: 3/28/18 implement
    }
    /* EXECUTIONAL ELEMENTS [END] */

    /* HELPER METHODS [START] */

    /**
     * Generates StreamConfig object, with given Siddhi StreamDefinition
     * @param streamDefinition  Siddhi StreamDefinition object
     * @param isInnerStream     Whether the stream is an inner stream
     * @return                  StreamConfig object
     */
    private StreamConfig generateStreamConfig(StreamDefinition streamDefinition, boolean isInnerStream) {
        List<AttributeConfig> attributeConfigs = new ArrayList<>();
        for (Attribute attribute : streamDefinition.getAttributeList()) {
            attributeConfigs.add(new AttributeConfig(attribute.getName(), attribute.getType().name()));
        }
        List<AnnotationConfig> annotationConfigs = new ArrayList<>();
        for (Annotation annotation : streamDefinition.getAnnotations()) {
            annotationConfigs.add(generateStreamOrTableAnnotationConfig(annotation));
        }
        return new StreamConfig(streamDefinition.getId(),
                streamDefinition.getId(),
                isInnerStream,
                attributeConfigs,
                annotationConfigs);
    }

    /**
     * Gets the AnnotationConfig of the given Annotation, that belongs to a stream
     * @param annotation    Siddhi Annotation, that belongs to a stream
     */ // TODO: 4/2/18 Find a good name for the method
    private AnnotationConfig generateStreamOrTableAnnotationConfig(Annotation annotation) {
        Map<String, AnnotationValue> annotationElements = new HashMap<>();
        List<AnnotationValue> annotationValues = new ArrayList<>();
        for (Element element : annotation.getElements()) {
            if (null == element.getKey()) {
                annotationValues.add(
                        new AnnotationValue(element.getValue(), DesignGeneratorHelper.isStringValue(element)));
            } else {
                annotationElements.put(
                        element.getKey(),
                        new AnnotationValue(element.getValue(), DesignGeneratorHelper.isStringValue(element)));
            }
        }
        if (annotationElements.isEmpty() && annotationValues.isEmpty()) {
            // No elements inside the annotation. Consider as an empty ListAnnotationConfig
            return new ListAnnotationConfig(annotation.getName(), new ArrayList<>(0));
        }
        AnnotationConfigFactory annotationConfigFactory = new AnnotationConfigFactory();
        if (annotationElements.isEmpty()) {
            return annotationConfigFactory.getAnnotationConfig(annotation.getName(), annotationValues);
        }
        return annotationConfigFactory.getAnnotationConfig(annotation.getName(), annotationElements);
    }

    /**
     * Generates list of AttributeConfigs, with given List of Siddhi Attributes
     * @param attributes    List of Siddhi Attribute objects
     * @return              List of AttributeConfig objects
     */
    private List<AttributeConfig> generateAttributeConfigs(List<Attribute> attributes) {
        List<AttributeConfig> attributeConfigs = new ArrayList<>();
        for (Attribute attribute : attributes) {
            attributeConfigs.add(new AttributeConfig(attribute.getName(), attribute.getType().name()));
        }
        return attributeConfigs;
    }

    // TODO: 4/6/18 comment
    private Map<String, String> generateSourceOptions(String streamDefinition) {
        Pattern sourceAnnotationContentPattern = Pattern.compile(CodeToDesignRegexPatterns.SOURCE_ANNOTATION_CONTENT);
        String sourceAnnotationContent;
        try {
            sourceAnnotationContent =
                    DesignGeneratorHelper.getRegexMatches(streamDefinition, sourceAnnotationContentPattern).get(0);
        } catch (DesignGeneratorHelperException e) {
            throw new IllegalArgumentException("No content found in the Source annotation", e);
        }

        // Extract content in 'map' annotation
        Pattern mapAnnotationContentPattern =
                Pattern.compile(CodeToDesignRegexPatterns.SOURCE_SINK_MAP_ANNOTATION_CONTENT);
        String mapAnnotationContent;
        try {
            mapAnnotationContent =
                    DesignGeneratorHelper.getRegexMatches(sourceAnnotationContent, mapAnnotationContentPattern).get(0);
        } catch (DesignGeneratorHelperException e) {
            throw new IllegalArgumentException(
                    "No content found in the Map annotation in:" + sourceAnnotationContent, e);
        }
        SourceMap sourceMap = generateSourceMap(mapAnnotationContent);

        // TODO: 4/6/18 implement
        return null;
    }

    // TODO: 4/6/18 comment
    private SourceMap generateSourceMap(String mapAnnotationContent) {
        // TODO: 4/6/18 implement
        return null;
    }

    /* HELPER METHODS [END] */

    /* EDGES METHODS [START] */

    private void generateEdges() {
        // TODO: 3/29/18 implement
    }

    /**
     * Returns whether the edge has been already generated or not.
     * An existing edge would have had the given childID as its parentID, and vice versa
     * @param parentID  Parent ID of the current edge. Will be the Child ID of the duplicate edge, if it exists
     * @param childID   Child ID of the current edge. Will be the Parent ID of the duplicate edge, if it exists
     * @return          Whether the edge is duplicate or not
     */
    private boolean isEdgeDuplicate(String parentID, String childID) {
        return (edges.containsKey(DesignGeneratorHelper.generateEdgeID(childID, parentID)));
    }

    /* EDGES METHODS [END] */
}
