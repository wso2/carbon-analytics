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
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.AttributeConfigListGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.EdgesGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.AttributesSelectionConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.StreamDefinitionConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.annotation.StreamTableAnnotationConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.QueryConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiAnnotationTypes;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
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
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.*;

/**
 * Generator to create Config objects from Siddhi Code
 */
public class DesignGenerator {
    private String siddhiAppString;
    private SiddhiApp siddhiApp;
    private SiddhiAppRuntime siddhiAppRuntime;
    private SiddhiAppConfig siddhiAppConfig;
    private List<Edge> edges;

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
        edges = new ArrayList<>();

        loadElements();
        loadEdges();

        return new EventFlow(siddhiAppConfig, edges);
    }

    /**
     * Loads generated SiddhiElementConfigs into SiddhiAppConfig object
     */
    private void loadElements() {
        loadAppNameAndDescription();
        loadTriggers();
        loadStreams();
        // TODO loadSources() loadSinks() loadFunctions()
        loadTables();
        loadWindows();
        loadAggregations();
        loadExecutionElements();
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
                siddhiAppConfig.add(
                        new StreamDefinitionConfigGenerator()
                                .generateStreamConfig(streamDefinition, false));
            }
        }
        // Inner Streams
        for (Map<String, AbstractDefinition> abstractDefinitionMap :
                siddhiAppRuntime.getPartitionedInnerStreamDefinitionMap().values()) {
            for (AbstractDefinition abstractDefinition : abstractDefinitionMap.values()) {
                if (abstractDefinition instanceof StreamDefinition) {
                    siddhiAppConfig.add(
                            new StreamDefinitionConfigGenerator()
                                    .generateStreamConfig((StreamDefinition) abstractDefinition, false));
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
//        for (List<Source> source : siddhiAppRuntime.getSources()) {
//            generateSourceOptions(source.get(0).getStreamDefinition().toString());
//            // source.get(0); // this is a source config
//            siddhiAppConfig.add(new SourceConfig(
//                    null,
//                    null,
//                    null,
//                    null));
//        }
        // TODO implement
        throw new IllegalArgumentException("Unable to generate source configs");
    }

    /**
     * Loads Sinks from the SiddhiAppRuntime
     */
    private void loadSinks() {
//        for (List<Sink> sink : siddhiAppRuntime.getSinks()) {
//            // sink.get(0); // this is a sink config
//            siddhiAppConfig.add(new SinkConfig(null, null, null, null));
//        }
        // TODO implement
        throw new IllegalArgumentException("Unable to generate sink configs");
    }

    /**
     * Loads Tables from the Siddhi App
     */
    private void loadTables() {
        for (TableDefinition tableDefinition : siddhiApp.getTableDefinitionMap().values()) {
            List<AnnotationConfig> annotationConfigs = new ArrayList<>();
            for (Annotation annotation : tableDefinition.getAnnotations()) {
                annotationConfigs.add(new StreamTableAnnotationConfigGenerator().generateAnnotationConfig(annotation));
            }
            siddhiAppConfig.add(new TableConfig(
                    tableDefinition.getId(),
                    tableDefinition.getId(),
                    new AttributeConfigListGenerator().generateAttributeConfigList(tableDefinition.getAttributeList()),
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
//                    generateAttributeConfigs(windowDefinition.getAttributeList()),
                    new AttributeConfigListGenerator().generateAttributeConfigList(windowDefinition.getAttributeList()),
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
                        new AttributesSelectionConfigGenerator(siddhiAppString);
                selectedAttributesConfig =
                        attributesSelectionConfigGenerator.generateAttributesSelectionConfig(selector);
                // Populate 'groupBy' list
                for (Variable variable : selector.getGroupByList()) {
                    groupBy.add(ConfigBuildingUtilities.getDefinition(variable, siddhiAppString));
                }
            } else {
                throw new IllegalArgumentException("Selector of AggregationDefinition is not of class BasicSelector");
            }

            List<AnnotationConfig> annotationList = new ArrayList<>();
            for (Annotation annotation : aggregationDefinition.getAnnotations()) {
                annotationList.add(new StreamTableAnnotationConfigGenerator().generateAnnotationConfig(annotation));
            }

            // For creating 'aggregate by time' object
            List<Duration> aggregationTimePeriodDurations = aggregationDefinition.getTimePeriod().getDurations();

            // 'aggregateByAttribute'
            String aggregateByAttribute = "";
            if (aggregationDefinition.getAggregateAttribute() != null) {
                aggregateByAttribute = aggregationDefinition.getAggregateAttribute().getAttributeName();
            }

            siddhiAppConfig.add(new AggregationConfig(
                    aggregationDefinition.getId(),
                    aggregationDefinition.getId(),
                    aggregationDefinition.getBasicSingleInputStream().getStreamId(),
                    selectedAttributesConfig,
                    groupBy,
                    aggregateByAttribute,
                    new AggregateByTimePeriod(
                            (aggregationTimePeriodDurations.get(0)).name(),
                            (aggregationTimePeriodDurations.get(aggregationTimePeriodDurations.size() - 1)).name()),
                    null, // TODO: 4/6/18 store
                    annotationList));
        }
    }

    private void loadFunctions() {
        // TODO: 3/28/18 implement
        throw new IllegalArgumentException("Unable to generate function configs");
    }

    /**
     * Loads Execution Elements from the Siddhi App
     */
    private void loadExecutionElements() {
        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            if (executionElement instanceof Query) {
                siddhiAppConfig.add(
                        new QueryConfigGenerator()
                                .generateQueryConfig((Query) executionElement, siddhiAppString, siddhiApp));
            } else if (executionElement instanceof Partition) {
                // TODO: 3/28/18 implement
                throw new IllegalArgumentException("Unable to generate partition configs");
            } else {
                throw new IllegalArgumentException("Unable create config for execution element of type unknown");
            }
        }
    }



    /* HELPER METHODS [END] */

    /**
     * Loads generated Edges that represent connections between SiddhiElementConfigs, into SiddhiAppConfig object
     */
    private void loadEdges() {
        EdgesGenerator edgesGenerator = new EdgesGenerator(siddhiAppConfig);
        edges = edgesGenerator.generateEdges();
    }
}
