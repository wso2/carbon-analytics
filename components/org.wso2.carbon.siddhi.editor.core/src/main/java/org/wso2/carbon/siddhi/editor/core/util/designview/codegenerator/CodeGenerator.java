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

package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.EventFlow;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.SiddhiAppConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.FunctionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TableConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.WindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.elements.ExecutionElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.AggregationCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.FunctionCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.PartitionCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.SourceSinkCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.StreamCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.SubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.TableCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.TriggerCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.WindowCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.QueryCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryListType;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Generate's the code for a Siddhi application
 */
public class CodeGenerator {

    /**
     * Generate's the Siddhi app code as a string of a given EventFlow object
     *
     * @param eventFlow The EventFlow object
     * @return The Siddhi application code as a string of the given EventFlow object
     * @throws CodeGenerationException Error while generating the code
     */
    public String generateSiddhiAppCode(EventFlow eventFlow) throws CodeGenerationException {
        SiddhiAppConfig siddhiApp = eventFlow.getSiddhiAppConfig();

        List<QueryConfig> queries = new ArrayList<>();
        for (List<QueryConfig> queryList : siddhiApp.getQueryLists().values()) {
            queries.addAll(queryList);
        }
        for (PartitionConfig partition : siddhiApp.getPartitionList()) {
            for (List<QueryConfig> queryList : partition.getQueryLists().values()) {
                queries.addAll(queryList);
            }
        }
        List<StreamConfig> streamsToBeGenerated = CodeGeneratorUtils.getStreamsToBeGenerated(siddhiApp.getStreamList(),
                siddhiApp.getSourceList(), siddhiApp.getSinkList(), queries);
        List<String> definitionsToBeGenerated = CodeGeneratorUtils.getDefinitionNames(streamsToBeGenerated,
                siddhiApp.getTableList(), siddhiApp.getWindowList(),
                siddhiApp.getTriggerList(), siddhiApp.getAggregationList(), siddhiApp.getPartitionList());
        List<String> allDefinitions = CodeGeneratorUtils.getDefinitionNames(siddhiApp.getStreamList(),
                siddhiApp.getTableList(), siddhiApp.getWindowList(),
                siddhiApp.getTriggerList(), siddhiApp.getAggregationList(), siddhiApp.getPartitionList());

        return generateAppName(siddhiApp.getSiddhiAppName()) +
                SubElementCodeGenerator.generateAnnotations(siddhiApp.getAppAnnotationList()) +
                generateStreams(streamsToBeGenerated, siddhiApp.getSourceList(), siddhiApp.getSinkList()) +
                generateTables(siddhiApp.getTableList()) +
                generateWindows(siddhiApp.getWindowList()) +
                generateTriggers(siddhiApp.getTriggerList()) +
                generateAggregations(siddhiApp.getAggregationList()) +
                generateFunctions(siddhiApp.getFunctionList()) +
                generateExecutionElements(siddhiApp.getQueryLists(), siddhiApp.getPartitionList(),
                        definitionsToBeGenerated, allDefinitions);
    }

    /**
     * Generate's the Siddhi code representation of a Siddhi app's app name
     *
     * @param appName The Siddhi app's app name
     * @return The Siddhi code representation of a Siddhi app name annotation
     */
    private String generateAppName(String appName) {
        StringBuilder appNameStringBuilder = new StringBuilder();

        if (appName != null && !appName.isEmpty()) {
            appNameStringBuilder.append(SiddhiCodeBuilderConstants.APP_NAME_ANNOTATION)
                    .append(appName)
                    .append(SiddhiCodeBuilderConstants.SINGLE_QUOTE)
                    .append(SiddhiCodeBuilderConstants.CLOSE_BRACKET);
        } else {
            appNameStringBuilder.append(SiddhiCodeBuilderConstants.DEFAULT_APP_NAME_ANNOTATION);
        }

        return appNameStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a Siddhi app's stream definitions
     *
     * @param streamList The list of streams to be defined in a Siddhi app
     * @param sourceList The list of source annotations in a Siddhi app
     * @param sinkList   The list of sink annotations in a Siddhi app
     * @return The Siddhi code representation of all the streams in a Siddhi app
     * @throws CodeGenerationException Error while generating the code
     */
    private String generateStreams(List<StreamConfig> streamList, List<SourceSinkConfig> sourceList,
                                   List<SourceSinkConfig> sinkList) throws CodeGenerationException {
        if (streamList == null || streamList.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        List<SourceSinkConfig> sourcesAndSinks = new LinkedList<>();
        sourcesAndSinks.addAll(sourceList);
        sourcesAndSinks.addAll(sinkList);

        StringBuilder streamListStringBuilder = new StringBuilder();
        streamListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        SourceSinkCodeGenerator sourceSinkCodeGenerator = new SourceSinkCodeGenerator();
        StreamCodeGenerator streamCodeGenerator = new StreamCodeGenerator();
        for (StreamConfig stream : streamList) {
            CodeGeneratorUtils.NullValidator.validateConfigObject(stream);
            if (stream.getPartitionId() != null && !stream.getPartitionId().isEmpty()) {
                continue;
            }

            for (SourceSinkConfig sourceSink : sourcesAndSinks) {
                if (stream.getName().equals(sourceSink.getConnectedElementName())) {
                    streamListStringBuilder.append(sourceSinkCodeGenerator.generateSourceSink(sourceSink));
                }
            }

            streamListStringBuilder.append(streamCodeGenerator.generateStream(stream));
        }

        streamListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        return streamListStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a Siddhi app's table definitions
     *
     * @param tableList The list of tables to be defined in a Siddhi app
     * @return The Siddhi code representation of all the tables in a Siddhi app
     * @throws CodeGenerationException Error while generating the code
     */
    private String generateTables(List<TableConfig> tableList) throws CodeGenerationException {
        if (tableList == null || tableList.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder tableListStringBuilder = new StringBuilder();
        tableListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        TableCodeGenerator tableCodeGenerator = new TableCodeGenerator();
        for (TableConfig table : tableList) {
            tableListStringBuilder.append(tableCodeGenerator.generateTable(table));
        }

        tableListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        return tableListStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a Siddhi app's window definitions
     *
     * @param windowList The list of windows to be defined in a Siddhi app
     * @return The Siddhi code representation of all the windows in a Siddhi app
     * @throws CodeGenerationException Error while generating the code
     */
    private String generateWindows(List<WindowConfig> windowList) throws CodeGenerationException {
        if (windowList == null || windowList.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder windowListStringBuilder = new StringBuilder();
        windowListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        WindowCodeGenerator windowCodeGenerator = new WindowCodeGenerator();
        for (WindowConfig window : windowList) {
            windowListStringBuilder.append(windowCodeGenerator.generateWindow(window));
        }

        windowListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        return windowListStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a Siddhi app's trigger definitions
     *
     * @param triggerList The list of triggers to be defined in a Siddhi app
     * @return The Siddhi code representation of all the triggers in a Siddhi app
     * @throws CodeGenerationException Error while generating the code
     */
    private String generateTriggers(List<TriggerConfig> triggerList) throws CodeGenerationException {
        if (triggerList == null || triggerList.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder triggerListStringBuilder = new StringBuilder();
        triggerListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        TriggerCodeGenerator triggerCodeGenerator = new TriggerCodeGenerator();
        for (TriggerConfig trigger : triggerList) {
            triggerListStringBuilder.append(triggerCodeGenerator.generateTrigger(trigger));
        }

        triggerListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        return triggerListStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a Siddhi app's aggregation definitions
     *
     * @param aggregationList The list of aggregations to be defined in a Siddhi app
     * @return The Siddhi code representation of all the aggregations in a Siddhi app
     * @throws CodeGenerationException Error while generating the code
     */
    private String generateAggregations(List<AggregationConfig> aggregationList) throws CodeGenerationException {
        if (aggregationList == null || aggregationList.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder aggregationListStringBuilder = new StringBuilder();
        aggregationListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        AggregationCodeGenerator aggregationCodeGenerator = new AggregationCodeGenerator();
        for (AggregationConfig aggregation : aggregationList) {
            aggregationListStringBuilder.append(aggregationCodeGenerator.generateAggregation(aggregation));
        }

        aggregationListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        return aggregationListStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a Siddhi app's function definitions
     *
     * @param functionList The list of functions to be defined in a Siddhi app
     * @return The Siddhi code representation of all the functions in a Siddhi app
     * @throws CodeGenerationException Error while generating the code
     */
    private String generateFunctions(List<FunctionConfig> functionList) throws CodeGenerationException {
        if (functionList == null || functionList.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder functionListStringBuilder = new StringBuilder();
        functionListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        FunctionCodeGenerator functionCodeGenerator = new FunctionCodeGenerator();
        for (FunctionConfig function : functionList) {
            functionListStringBuilder.append(functionCodeGenerator.generateFunction(function));
        }

        functionListStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);

        return functionListStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a Siddhi app's execution elements (queries and partitions)
     *
     * @param queryLists               The list of queries in a Siddhi app
     * @param partitions               The list of partitions in a Siddhi app
     * @param definitionsToBeGenerated The names of all the definition elements that are to be defined in the app
     * @param allDefinitions           The names of all the definition elements in the Siddhi app
     * @return The Siddhi code representation of all the queries and partitions of a Siddhi app
     * @throws CodeGenerationException Error while generating the code
     */
    private String generateExecutionElements(Map<QueryListType, List<QueryConfig>> queryLists,
                                             List<PartitionConfig> partitions, List<String> definitionsToBeGenerated,
                                             List<String> allDefinitions)
            throws CodeGenerationException {
        StringBuilder executionElementStringBuilder = new StringBuilder();
        executionElementStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);
        List<QueryConfig> queries = new LinkedList<>();
        for (List<QueryConfig> queryList : queryLists.values()) {
            queries.addAll(queryList);
        }

        List<ExecutionElementConfig> executionElements =
                CodeGeneratorUtils.convertToExecutionElements(queries, partitions);
        QueryCodeGenerator queryCodeGenerator = new QueryCodeGenerator();
        PartitionCodeGenerator partitionCodeGenerator = new PartitionCodeGenerator();
        for (ExecutionElementConfig executionElement :
                CodeGeneratorUtils.reorderExecutionElements(executionElements, definitionsToBeGenerated)) {
            if (executionElement.getType().equalsIgnoreCase(CodeGeneratorConstants.QUERY)) {
                QueryConfig query = (QueryConfig) executionElement.getValue();
                executionElementStringBuilder.append(queryCodeGenerator.generateQuery(query));
            } else if (executionElement.getType().equalsIgnoreCase(CodeGeneratorConstants.PARTITION)) {
                PartitionConfig partition = (PartitionConfig) executionElement.getValue();
                executionElementStringBuilder.append(partitionCodeGenerator.generatePartition(partition, allDefinitions));
            } else {
                throw new CodeGenerationException("Unidentified ExecutionElement type: " + executionElement.getType());
            }
            executionElementStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);
        }

        return executionElementStringBuilder.toString();
    }

}
