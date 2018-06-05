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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.builders;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.EventFlow;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.Edge;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.SiddhiAppConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiAnnotationTypes;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.QueryConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.*;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builder to create EventFlow
 */
public class EventFlowBuilder {
    private String siddhiAppString;
    private SiddhiApp siddhiApp;
    private SiddhiAppRuntime siddhiAppRuntime;

    private SiddhiAppConfig siddhiAppConfig;
    private List<Edge> edges;

    public EventFlowBuilder(String siddhiAppString, SiddhiApp siddhiApp, SiddhiAppRuntime siddhiAppRuntime) {
        this.siddhiAppString = siddhiAppString;
        this.siddhiApp = siddhiApp;
        this.siddhiAppRuntime = siddhiAppRuntime;
        siddhiAppConfig = new SiddhiAppConfig();
        edges = new ArrayList<>();
    }

    /**
     * Creates an EventFlow object with loaded elements
     * @return      EventFlow object
     */
    public EventFlow create() {
        return new EventFlow(siddhiAppConfig, edges);
    }

    /**
     * Loads name and description from the SiddhiApp
     */
    /**
     * Loads name and description from the SiddhiApp
     * @return      A reference to this object
     */
    public EventFlowBuilder loadAppNameAndDescription() {
        for (Annotation annotation : siddhiApp.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase(SiddhiAnnotationTypes.NAME)) {
                siddhiAppConfig.setAppName(annotation.getElements().get(0).getValue());
            } else if (annotation.getName().equalsIgnoreCase(SiddhiAnnotationTypes.DESCRIPTION)) {
                siddhiAppConfig.setAppDescription(annotation.getElements().get(0).getValue());
            }
        }
        return this;
    }

    /**
     * Loads Triggers from the SiddhiApp
     * @return                                  A reference to this object
     * @throws DesignGenerationException        Error while loading elements
     */
    public EventFlowBuilder loadTriggers() throws DesignGenerationException {
        TriggerConfigGenerator triggerConfigGenerator =
                new TriggerConfigGenerator(siddhiAppString, siddhiAppRuntime.getStreamDefinitionMap());
        for (TriggerDefinition triggerDefinition : siddhiApp.getTriggerDefinitionMap().values()) {
            siddhiAppConfig.add(triggerConfigGenerator.generateTriggerConfig(triggerDefinition));
        }
        return this;
    }

    /**
     * Returns the availability of a Trigger with the given name, in the given Siddhi app
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
     * @return                                  A reference to this object
     * @throws DesignGenerationException        Error while loading elements
     */
    public EventFlowBuilder loadStreams() throws DesignGenerationException {
        StreamDefinitionConfigGenerator streamDefinitionConfigGenerator = new StreamDefinitionConfigGenerator();
        Map<String, StreamDefinition> streamDefinitionMap = siddhiAppRuntime.getStreamDefinitionMap();
        for (Map.Entry<String, StreamDefinition> streamDefinitionEntry : streamDefinitionMap.entrySet()) {
            if (!isTriggerDefined(streamDefinitionEntry.getKey(), siddhiApp)) {
                siddhiAppConfig.add(
                        streamDefinitionConfigGenerator
                                .generateStreamConfig(streamDefinitionEntry.getValue()));
            }
        }

        // Inner Streams TODO LOOK INTO
        for (Map<String, AbstractDefinition> abstractDefinitionMap :
                siddhiAppRuntime.getPartitionedInnerStreamDefinitionMap().values()) {
            for (AbstractDefinition abstractDefinition : abstractDefinitionMap.values()) {
                if (abstractDefinition instanceof StreamDefinition) {
                    siddhiAppConfig.add(
                            streamDefinitionConfigGenerator
                                    .generateStreamConfig((StreamDefinition) abstractDefinition));
                } else {
                    throw new DesignGenerationException(
                            "The partitioned inner stream definition map does not have an instance of class " +
                                    "'StreamDefinition'");
                }
            }
        }
        return this;
    }

    /**
     * Loads Sources from the SiddhiAppRuntime
     * @return      A reference to this object
     */
    public EventFlowBuilder loadSources() {
        SourceSinkConfigGenerator sourceConfigGenerator = new SourceSinkConfigGenerator();
        for (List<Source> sourceList : siddhiAppRuntime.getSources()) {
            for (Source source : sourceList) {
                siddhiAppConfig.addSource(sourceConfigGenerator.generateSourceConfig(source));
            }
        }
        return this;
    }

    /**
     * Loads Sinks from the SiddhiAppRuntime
     * @return      A reference to this object
     */
    public EventFlowBuilder loadSinks() {
        SourceSinkConfigGenerator sinkConfigGenerator = new SourceSinkConfigGenerator();
        for (List<Sink> sinkList : siddhiAppRuntime.getSinks()) {
            for (Sink sink : sinkList) {
                siddhiAppConfig.addSink(sinkConfigGenerator.generateSinkConfig(sink));
            }
        }
        return this;
    }

    /**
     * Loads Tables from the Siddhi App
     * @return                                  A reference to this object
     * @throws DesignGenerationException        Error while loading elements
     */
    public EventFlowBuilder loadTables() throws DesignGenerationException {
        TableConfigGenerator tableConfigGenerator = new TableConfigGenerator();
        for (TableDefinition tableDefinition : siddhiApp.getTableDefinitionMap().values()) {
            siddhiAppConfig.add(tableConfigGenerator.generateTableConfig(tableDefinition));
        }
        return this;
    }

    /**
     * Loads Defined Windows from the SiddhiAppRuntime
     * @return                                  A reference to this object
     * @throws DesignGenerationException        Error while loading elements
     */
    public EventFlowBuilder loadWindows() throws DesignGenerationException {
        WindowConfigGenerator windowConfigGenerator = new WindowConfigGenerator(siddhiAppString);
        for (WindowDefinition windowDefinition : siddhiApp.getWindowDefinitionMap().values()) {
            siddhiAppConfig.add(windowConfigGenerator.generateWindowConfig(windowDefinition));
        }
        return this;
    }

    /**
     * Loads Aggregations from the SiddhiApp
     * @return                                  A reference to this object
     * @throws DesignGenerationException        Error while loading elements
     */
    public EventFlowBuilder loadAggregations() throws DesignGenerationException {
        AggregationConfigGenerator aggregationConfigGenerator = new AggregationConfigGenerator(siddhiAppString);
        for (AggregationDefinition aggregationDefinition : siddhiApp.getAggregationDefinitionMap().values()) {
            siddhiAppConfig.add(aggregationConfigGenerator.generateAggregationConfig(aggregationDefinition));
        }
        return this;
    }

    /**
     * Loads Functions from the siddhi app
     * @return      A reference to this object
     */
    public EventFlowBuilder loadFunctions() {
        FunctionConfigGenerator functionConfigGenerator = new FunctionConfigGenerator();
        for (FunctionDefinition functionDefinition : siddhiApp.getFunctionDefinitionMap().values()) {
            siddhiAppConfig.add(functionConfigGenerator.generateFunctionConfig(functionDefinition));
        }
        return this;
    }

    /**
     * Loads Execution Elements from the Siddhi App
     * @return                                  A reference to this object
     * @throws DesignGenerationException        Error while loading elements
     */
    public EventFlowBuilder loadExecutionElements() throws DesignGenerationException {
        QueryConfigGenerator queryConfigGenerator = new QueryConfigGenerator(siddhiAppString, siddhiApp);
        PartitionConfigGenerator partitionConfigGenerator = new PartitionConfigGenerator(siddhiAppString, siddhiApp);
        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            if (executionElement instanceof Query) {
                QueryConfig queryConfig = queryConfigGenerator.generateQueryConfig((Query) executionElement);
                siddhiAppConfig.addQuery(QueryConfigGenerator.getQueryListType(queryConfig), queryConfig);
            } else if (executionElement instanceof Partition) {
                siddhiAppConfig.add(partitionConfigGenerator.generatePartitionConfig((Partition) executionElement));
            } else {
                throw new DesignGenerationException("Unable create config for execution element of type unknown");
            }
        }
        return this;
    }

    /**
     * Loads generated Edges that represent connections between SiddhiElementConfigs, into SiddhiAppConfig object
     * @return                                  A reference to this object
     * @throws DesignGenerationException        Error while loading edges
     */
    public EventFlowBuilder loadEdges() throws DesignGenerationException {
        EdgesGenerator edgesGenerator = new EdgesGenerator(siddhiAppConfig);
        edges = edgesGenerator.generateEdges();
        return this;
    }
}
