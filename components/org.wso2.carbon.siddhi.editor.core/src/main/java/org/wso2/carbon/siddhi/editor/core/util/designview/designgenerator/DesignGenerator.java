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
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.AnnotationConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.QueryConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiAnnotationTypes;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.aggregation.TimePeriod.Duration;
import org.wso2.siddhi.query.api.annotation.Annotation;
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
     * @param siddhiAppString                   Code representation of the Siddhi app
     * @return                                  Event flow representation of the Siddhi app
     * @throws DesignGenerationException        Error while generating config
     */
    public EventFlow getEventFlow(String siddhiAppString) throws DesignGenerationException {
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
     * @throws DesignGenerationException        Error while loading elements
     */
    private void loadElements() throws DesignGenerationException {
        loadAppNameAndDescription();
        loadTriggers();
        loadStreams();
        loadSources();
        loadSinks();
        loadTables();
        loadWindows();
        loadAggregations();
        loadExecutionElements();
        loadFunctions();
    }

    /**
     * Loads generated Edges that represent connections between SiddhiElementConfigs, into SiddhiAppConfig object
     * @throws DesignGenerationException        Error while loading edges
     */
    private void loadEdges() throws DesignGenerationException {
        EdgesGenerator edgesGenerator = new EdgesGenerator(siddhiAppConfig);
        edges = edgesGenerator.generateEdges();
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
     * @throws DesignGenerationException        Error while loading elements
     */
    private void loadTriggers() throws DesignGenerationException {
        TriggerConfigGenerator triggerConfigGenerator =
                new TriggerConfigGenerator(siddhiAppString, siddhiAppRuntime.getStreamDefinitionMap());
        for (TriggerDefinition triggerDefinition : siddhiApp.getTriggerDefinitionMap().values()) {
            siddhiAppConfig.add(triggerConfigGenerator.generateTriggerConfig(triggerDefinition));
        }
    }

    /**
     * Returns the availability of a Trigger with the given name, in the given Siddhi app
     *
     * @param streamName    Name of the Stream
     * @param siddhiApp     Siddhi app in which, availability of Trigger is searched
     * @return              Availability of Trigger with the given name, in given Siddhi app
     */
    private boolean isTriggerDefined(String streamName, SiddhiApp siddhiApp) {
        return (siddhiApp.getTriggerDefinitionMap().size() != 0 &&
                siddhiApp.getTriggerDefinitionMap().containsKey(streamName));
    }

    /**
     * Loads Streams from the SiddhiAppRuntime
     * @throws DesignGenerationException        Error while loading elements
     */
    private void loadStreams() throws DesignGenerationException {
        StreamDefinitionConfigGenerator streamDefinitionConfigGenerator = new StreamDefinitionConfigGenerator();
        Map<String, StreamDefinition> streamDefinitionMap = siddhiAppRuntime.getStreamDefinitionMap();
        for (Map.Entry<String, StreamDefinition> streamDefinitionEntry : streamDefinitionMap.entrySet()) {
            if (!isTriggerDefined(streamDefinitionEntry.getKey(), siddhiApp)) {
                siddhiAppConfig.add(
                        streamDefinitionConfigGenerator
                                .generateStreamConfig(streamDefinitionEntry.getValue(), false));
            }
        }

        // Inner Streams
        for (Map<String, AbstractDefinition> abstractDefinitionMap :
                siddhiAppRuntime.getPartitionedInnerStreamDefinitionMap().values()) {
            for (AbstractDefinition abstractDefinition : abstractDefinitionMap.values()) {
                if (abstractDefinition instanceof StreamDefinition) {
                    siddhiAppConfig.add(
                            streamDefinitionConfigGenerator
                                    .generateStreamConfig((StreamDefinition) abstractDefinition, true));
                } else {
                    throw new DesignGenerationException(
                            "The partitioned inner stream definition map does not have an instance of class " +
                                    "'StreamDefinition'");
                }
            }
        }
    }

    /**
     * Loads Sources from the SiddhiAppRuntime
     */
    private void loadSources() {
        SourceSinkConfigGenerator sourceConfigGenerator = new SourceSinkConfigGenerator();
        for (List<Source> sourceList : siddhiAppRuntime.getSources()) {
            for (Source source : sourceList) {
                siddhiAppConfig.addSource(sourceConfigGenerator.generateSourceConfig(source));
            }
        }
    }

    /**
     * Loads Sinks from the SiddhiAppRuntime
     */
    private void loadSinks() {
        SourceSinkConfigGenerator sinkConfigGenerator = new SourceSinkConfigGenerator();
        for (List<Sink> sinkList : siddhiAppRuntime.getSinks()) {
            for (Sink sink : sinkList) {
                siddhiAppConfig.addSink(sinkConfigGenerator.generateSinkConfig(sink));
            }
        }
    }

    /**
     * Loads Tables from the Siddhi App
     */
    private void loadTables() {
        TableConfigGenerator tableConfigGenerator = new TableConfigGenerator();
        for (TableDefinition tableDefinition : siddhiApp.getTableDefinitionMap().values()) {
            siddhiAppConfig.add(tableConfigGenerator.generateTableConfig(tableDefinition));
        }
    }

    /**
     * Loads Defined Windows from the SiddhiAppRuntime
     * @throws DesignGenerationException        Error while loading elements
     */
    private void loadWindows() throws DesignGenerationException {
        WindowConfigGenerator windowConfigGenerator = new WindowConfigGenerator(siddhiAppString);
        for (WindowDefinition windowDefinition : siddhiApp.getWindowDefinitionMap().values()) {
            siddhiAppConfig.add(windowConfigGenerator.generateWindowConfig(windowDefinition));
        }
    }

    /**
     * Loads Aggregations from the SiddhiApp
     * @throws DesignGenerationException        Error while loading elements
     */
    private void loadAggregations() throws DesignGenerationException {
        AggregationConfigGenerator aggregationConfigGenerator = new AggregationConfigGenerator(siddhiAppString);
        for (AggregationDefinition aggregationDefinition : siddhiApp.getAggregationDefinitionMap().values()) {
            siddhiAppConfig.add(aggregationConfigGenerator.generateAggregationConfig(aggregationDefinition));
        }
    }

    /**
     * Loads Functions from the siddhi app
     */
    private void loadFunctions() {
        FunctionConfigGenerator functionConfigGenerator = new FunctionConfigGenerator();
        for (FunctionDefinition functionDefinition : siddhiApp.getFunctionDefinitionMap().values()) {
            siddhiAppConfig.add(functionConfigGenerator.generateFunctionConfig(functionDefinition));
        }
    }

    /**
     * Loads Execution Elements from the Siddhi App
     * @throws DesignGenerationException        Error while loading elements
     */
    private void loadExecutionElements() throws DesignGenerationException {
        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            if (executionElement instanceof Query) {
                siddhiAppConfig.add(
                        new QueryConfigGenerator()
                                .generateQueryConfig((Query) executionElement, siddhiAppString, siddhiApp));
            } else if (executionElement instanceof Partition) {
                // TODO: 3/28/18 implement
                throw new DesignGenerationException("Unable to generate partition configs");
            } else {
                throw new DesignGenerationException("Unable create config for execution element of type unknown");
            }
        }
    }
}
