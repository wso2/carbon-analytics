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

package org.wso2.carbon.siddhi.editor.core.util.designview;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.Edge;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.EventFlow;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.factories.AnnotationConfigFactory;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiAnnotationType;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.DesignGenerationHelper;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.*;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates Siddhi app elements for the design view
 */
public class DesignGenerator {
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
        loadTables();
        loadWindows();
        loadAggregations();
        loadFunctions();
        loadQueriesAndPartitions();

        // TODO: 3/27/18 implement
        return null;
    }

    /**
     * Loads name & description from the SiddhiApp
     */
    private void loadAppNameAndDescription() {
        for (Annotation annotation : siddhiApp.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase(SiddhiAnnotationType.NAME)) {
                siddhiAppConfig.setAppName(annotation.getElements().get(0).getValue());
            } else if (annotation.getName().equalsIgnoreCase(SiddhiAnnotationType.DESCRIPTION)) {
                siddhiAppConfig.setAppDescription(annotation.getElements().get(0).getValue());
            }
        }
    }

    /**
     * Loads Triggers from the SiddhiApp
     */
    private void loadTriggers() {
        for (TriggerDefinition triggerDefinition : siddhiApp.getTriggerDefinitionMap().values()) {
            siddhiAppConfig.add(new TriggerConfig(
                    triggerDefinition.getId(),
                    triggerDefinition.getId(),
                    triggerDefinition.getAt(),
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
            // source.get(0); // this is a source config
            siddhiAppConfig.add(new SourceConfig(null, null, null, null)); // TODO: 3/28/18 new SourceConfig()
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
            siddhiAppConfig.add(new TableConfig(
                    tableDefinition.getId(),
                    tableDefinition.getId(),
                    generateAttributeConfigs(tableDefinition.getAttributeList()),
                    null,
                    null)); // TODO: 3/29/18 store & annotationList
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
                parameters.add(null);
            } // TODO: 3/29/18 Add parameters
            siddhiAppConfig.add(new WindowConfig(
                    windowDefinition.getId(),
                    windowDefinition.getId(),
                    generateAttributeConfigs(windowDefinition.getAttributeList()),
                    windowDefinition.getWindow().getName(),
                    null,
                    windowDefinition.getOutputEventType().name(),
                    null)); // TODO: 3/29/18 parameters & annotationList
        }
    }

    private void loadAggregations() {
        for (AggregationDefinition aggregationDefinition : siddhiApp.getAggregationDefinitionMap().values()) {
            // TODO: 3/28/18 implement
        }
    }

    private void loadFunctions() {
        // TODO: 3/28/18 implement 
    }

    private void loadQueriesAndPartitions() {
        // TODO: 3/28/18 implement
    }

    /* EXECUTIONAL ELEMENTS [START] */
    private void loadQueries() {
        // TODO: 3/28/18 implement
    }

    private void loadPartitions() {
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
            annotationConfigs.add(generateStreamAnnotationConfig(annotation));
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
     */
    private AnnotationConfig generateStreamAnnotationConfig(Annotation annotation) {
        Map<String, String> annotationElements = new HashMap<>();
        List<String> annotationValues = new ArrayList<>();
        for (Element element : annotation.getElements()) {
            if (null == element.getKey()) {
                annotationValues.add(element.getValue());
            } else {
                annotationElements.put(element.getKey(), element.getValue());
            }
        }
        if (annotationElements.size() == 0 && annotationValues.size() == 0) {
            // No elements inside the annotation. Consider as an empty ListAnnotation
            return new ListAnnotationConfig(annotation.getName(), new ArrayList<>(0));
        }
        AnnotationConfigFactory annotationConfigFactory = new AnnotationConfigFactory();
        if (annotationElements.size() == 0) {
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
        return (edges.containsKey(DesignGenerationHelper.generateEdgeID(childID, parentID)));
    }

    /* EDGES METHODS [END] */
}
