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
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiStringBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryListType;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Used to convert an EventFlow object to a Siddhi app string
 */
public class CodeGenerator {

    /**
     * Converts a given EventFlow object to it's Siddhi app string representation
     *
     * @param eventFlow The EventFlow object to be converted
     * @return The Siddhi app as a string
     * @throws CodeGenerationException Error while generating code
     */
    public String generateSiddhiAppCode(EventFlow eventFlow) throws CodeGenerationException {
        SiddhiAppConfig siddhiApp = eventFlow.getSiddhiAppConfig();
        return generateAppName(siddhiApp.getSiddhiAppName()) +
                CodeGeneratorHelper.getAnnotations(siddhiApp.getAppAnnotationList()) +
                generateStreams(siddhiApp.getStreamList(), siddhiApp.getSourceList(), siddhiApp.getSinkList()) +
                generateTables(siddhiApp.getTableList()) +
                generateWindows(siddhiApp.getWindowList()) +
                generateTriggers(siddhiApp.getTriggerList()) +
                generateAggregations(siddhiApp.getAggregationList()) +
                generateFunctions(siddhiApp.getFunctionList()) +
                generateQueries(siddhiApp.getQueryLists()) +
                generatePartitions(siddhiApp.getPartitionList());
    }

    /**
     * Generates a string representation of the siddhi app name annotation
     *
     * @param appName The app name
     * @return The string representation of the siddhi app name annotation
     */
    private String generateAppName(String appName) {
        StringBuilder appNameStringBuilder = new StringBuilder();

        if (appName != null && !appName.isEmpty()) {
            appNameStringBuilder.append(SiddhiStringBuilderConstants.APP_NAME_ANNOTATION)
                    .append(appName)
                    .append(SiddhiStringBuilderConstants.SINGLE_QUOTE)
                    .append(SiddhiStringBuilderConstants.CLOSE_BRACKET);
        } else {
            appNameStringBuilder.append(SiddhiStringBuilderConstants.DEFAULT_APP_NAME_ANNOTATION);
        }

        return appNameStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the streams in a Siddhi app
     *
     * @param streamList A list of StreamConfig objects from the SiddhiAppConfig object
     * @param sourceList A list of StreamConfig objects from the SiddhiAppConfig object
     * @param sinkList   A list of StreamConfig objects from the SiddhiAppConfig object
     * @return The string representation of the stream definitions
     * @throws CodeGenerationException Error while generating code
     */
    private String generateStreams(List<StreamConfig> streamList, List<SourceSinkConfig> sourceList,
                                   List<SourceSinkConfig> sinkList) throws CodeGenerationException {
        if (streamList == null || streamList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder streamListStringBuilder = new StringBuilder();
        streamListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(SiddhiStringBuilderConstants.STREAMS_COMMENT)
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (StreamConfig stream : streamList) {
            if (stream == null) {
                throw new CodeGenerationException("A given stream element is empty");
            } else if (stream.getPartitionId() != null && !stream.getPartitionId().isEmpty()) {
                continue;
            } else if (stream.getName() == null || stream.getName().isEmpty()) {
                throw new CodeGenerationException("The name of a given stream element is empty");
            }

            for (SourceSinkConfig source : sourceList) {
                if (stream.getName().equals(source.getConnectedElementName())) {
                    streamListStringBuilder.append(generateSourceSinkString(source));
                }
            }

            for (SourceSinkConfig sink : sinkList) {
                if (stream.getName().equals(sink.getConnectedElementName())) {
                    streamListStringBuilder.append(generateSourceSinkString(sink));
                }
            }

            streamListStringBuilder.append(generateStreamString(stream));
        }

        streamListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return streamListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the tables in a Siddhi app
     *
     * @param tableList A list of TableConfig objects from the SiddhiAppConfig object
     * @return The Siddhi string representation of the table definitions
     * @throws CodeGenerationException Error while generating code
     */
    private String generateTables(List<TableConfig> tableList) throws CodeGenerationException {
        if (tableList == null || tableList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder tableListStringBuilder = new StringBuilder();
        tableListStringBuilder.append(SiddhiStringBuilderConstants.TABLES_COMMENT)
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (TableConfig table : tableList) {
            tableListStringBuilder.append(generateTableString(table));
        }

        tableListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return tableListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the window definitions in a Siddhi app
     *
     * @param windowList A list of WindowConfig objects to be converted
     * @return The Siddhi string representaiotn of all the window definitions
     * @throws CodeGenerationException Error while generating code
     */
    private String generateWindows(List<WindowConfig> windowList) throws CodeGenerationException {
        if (windowList == null || windowList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder windowListStringBuilder = new StringBuilder();
        windowListStringBuilder.append(SiddhiStringBuilderConstants.WINDOWS_COMMENT)
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (WindowConfig window : windowList) {
            windowListStringBuilder.append(generateWindowString(window));
        }

        windowListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return windowListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the trigger definitions in a Siddhi app
     *
     * @param triggerList A list of all the TriggerConfig objects to be converted
     * @return The Siddhi string representation of all the trigger definitions
     * @throws CodeGenerationException Error while generating code
     */
    private String generateTriggers(List<TriggerConfig> triggerList) throws CodeGenerationException {
        if (triggerList == null || triggerList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder triggerListStringBuilder = new StringBuilder();
        triggerListStringBuilder.append(SiddhiStringBuilderConstants.TRIGGERS_COMMENT)
                .append(SiddhiStringBuilderConstants.NEW_LINE);
        for (TriggerConfig trigger : triggerList) {
            triggerListStringBuilder.append(generateTriggerString(trigger));
        }

        triggerListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return triggerListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the aggregation definitions in a Siddhi app
     *
     * @param aggregationList A list of AggregationConfig objects to be converted
     * @return The Siddhi string representation of all the aggregation definitions
     * @throws CodeGenerationException Error while generating code
     */
    private String generateAggregations(List<AggregationConfig> aggregationList) throws CodeGenerationException {
        if (aggregationList == null || aggregationList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder aggregationListStringBuilder = new StringBuilder();
        aggregationListStringBuilder.append(SiddhiStringBuilderConstants.AGGREGATIONS_COMMENT)
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (AggregationConfig aggregation : aggregationList) {
            aggregationListStringBuilder.append(generateAggregationString(aggregation));
        }

        aggregationListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return aggregationListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the function definitions in a Siddhi app
     *
     * @param functionList A list of FunctionConfig objects to be converted
     * @return The Siddhi string representation of all the function definitions
     * @throws CodeGenerationException Error while generating code
     */
    private String generateFunctions(List<FunctionConfig> functionList) throws CodeGenerationException {
        if (functionList == null || functionList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder functionListStringBuilder = new StringBuilder();
        functionListStringBuilder.append(SiddhiStringBuilderConstants.FUNCTIONS_COMMENT)
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (FunctionConfig function : functionList) {
            functionListStringBuilder.append(generateFunctionString(function));
        }

        functionListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return functionListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the queries in a Siddhi app
     *
     * @param queryLists A list of QueryConfig objects to be converted
     * @return The Siddhi string representation of the given QueryConfig list
     * @throws CodeGenerationException Error while generating code
     */
    private String generateQueries(Map<QueryListType, List<QueryConfig>> queryLists) throws CodeGenerationException {
        if (queryLists == null || queryLists.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder queryListStringBuilder = new StringBuilder();


        boolean hasQueries = false;
        for (List<QueryConfig> queryList : queryLists.values()) {
            if (queryList != null && !queryList.isEmpty()) {
                if (!hasQueries) {
                    hasQueries = true;
                    queryListStringBuilder.append(SiddhiStringBuilderConstants.QUERIES_COMMENT)
                            .append(SiddhiStringBuilderConstants.NEW_LINE);
                }

                for (QueryConfig query : queryList) {
                    queryListStringBuilder.append(generateQueryString(query))
                            .append(SiddhiStringBuilderConstants.NEW_LINE);
                }
            }
        }

        queryListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return queryListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the partitions in a Siddhi app
     *
     * @param partitionList The list of PartitionConfig objects to be converted
     * @return The Siddhi string representation of the given PartitionConfig list
     * @throws CodeGenerationException Error while generating code
     */
    private String generatePartitions(List<PartitionConfig> partitionList) throws CodeGenerationException {
        if (partitionList == null || partitionList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder partitionListStringBuilder = new StringBuilder();
        partitionListStringBuilder.append(SiddhiStringBuilderConstants.PARTITIONS_COMMENT)
                .append(SiddhiStringBuilderConstants.NEW_LINE);
        for (PartitionConfig partition : partitionList) {
            partitionListStringBuilder.append(generatePartitionString(partition))
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        }

        partitionListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return partitionListStringBuilder.toString();
    }

    /**
     * Generates a stream definition string from a StreamConfig object
     *
     * @param stream The StreamConfig object to be converted
     * @return The converted stream definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateStreamString(StreamConfig stream) throws CodeGenerationException {
        if (stream == null) {
            throw new CodeGenerationException("A given stream element is empty");
        } else if (stream.getName() == null || stream.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given stream element is empty");
        }

        return CodeGeneratorHelper.getAnnotations(stream.getAnnotationList()) +
                SiddhiStringBuilderConstants.DEFINE_STREAM +
                SiddhiStringBuilderConstants.SPACE +
                stream.getName() +
                SiddhiStringBuilderConstants.SPACE +
                SiddhiStringBuilderConstants.OPEN_BRACKET +
                CodeGeneratorHelper.getAttributes(stream.getAttributeList()) +
                SiddhiStringBuilderConstants.CLOSE_BRACKET +
                SiddhiStringBuilderConstants.SEMI_COLON;
    }

    /**
     * Generates a table definition string from a TableConfig object
     *
     * @param table The TableConfig object to be converted
     * @return The converted table definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateTableString(TableConfig table) throws CodeGenerationException {
        if (table == null) {
            throw new CodeGenerationException("A given table element is empty");
        } else if (table.getName() == null || table.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given table element is empty");
        }

        return CodeGeneratorHelper.getStore(table.getStore()) +
                CodeGeneratorHelper.getAnnotations(table.getAnnotationList()) +
                SiddhiStringBuilderConstants.DEFINE_TABLE +
                SiddhiStringBuilderConstants.SPACE +
                table.getName() +
                SiddhiStringBuilderConstants.SPACE +
                SiddhiStringBuilderConstants.OPEN_BRACKET +
                CodeGeneratorHelper.getAttributes(table.getAttributeList()) +
                SiddhiStringBuilderConstants.CLOSE_BRACKET +
                SiddhiStringBuilderConstants.SEMI_COLON;
    }

    /**
     * Generates a window definition string from a WindowConfig object
     *
     * @param window The WindowConfig object to be converted
     * @return The converted window definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateWindowString(WindowConfig window) throws CodeGenerationException {
        if (window == null) {
            throw new CodeGenerationException("A given window element is empty");
        } else if (window.getName() == null || window.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given window element is empty");
        } else if (window.getFunction() == null || window.getFunction().isEmpty()) {
            throw new CodeGenerationException("The function name of the window " + window.getName() + " is empty");
        }

        StringBuilder windowStringBuilder = new StringBuilder();
        windowStringBuilder.append(CodeGeneratorHelper.getAnnotations(window.getAnnotationList()))
                .append(SiddhiStringBuilderConstants.DEFINE_WINDOW)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(window.getName())
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getAttributes(window.getAttributeList()))
                .append(SiddhiStringBuilderConstants.CLOSE_BRACKET)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(window.getFunction())
                .append(SiddhiStringBuilderConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getParameterList(window.getParameters()))
                .append(SiddhiStringBuilderConstants.CLOSE_BRACKET);

        if (window.getOutputEventType() != null && !window.getOutputEventType().isEmpty()) {
            windowStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(SiddhiStringBuilderConstants.OUTPUT)
                    .append(SiddhiStringBuilderConstants.SPACE);
            switch (window.getOutputEventType().toUpperCase()) {
                case CodeGeneratorConstants.CURRENT_EVENTS:
                    windowStringBuilder.append(SiddhiStringBuilderConstants.CURRENT_EVENTS);
                    break;
                case CodeGeneratorConstants.EXPIRED_EVENTS:
                    windowStringBuilder.append(SiddhiStringBuilderConstants.EXPIRED_EVENTS);
                    break;
                case CodeGeneratorConstants.ALL_EVENTS:
                    windowStringBuilder.append(SiddhiStringBuilderConstants.ALL_EVENTS);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified window output event type: "
                            + window.getOutputEventType());
            }
        }
        windowStringBuilder.append(SiddhiStringBuilderConstants.SEMI_COLON);

        return windowStringBuilder.toString();
    }

    /**
     * Generates a trigger definition string from a TriggerConfig object
     *
     * @param trigger The TriggerConfig object to be converted
     * @return The converted trigger definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateTriggerString(TriggerConfig trigger) throws CodeGenerationException {
        if (trigger == null) {
            throw new CodeGenerationException("A given trigger element is empty");
        } else if (trigger.getName() == null || trigger.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given trigger element is empty");
        } else if (trigger.getAt() == null || trigger.getAt().isEmpty()) {
            throw new CodeGenerationException("The 'at' value of " + trigger.getName() + " is empty");
        }

        return CodeGeneratorHelper.getAnnotations(trigger.getAnnotationList()) +
                SiddhiStringBuilderConstants.DEFINE_TRIGGER +
                SiddhiStringBuilderConstants.SPACE +
                trigger.getName() +
                SiddhiStringBuilderConstants.SPACE +
                SiddhiStringBuilderConstants.AT +
                SiddhiStringBuilderConstants.SPACE +
                trigger.getAt() +
                SiddhiStringBuilderConstants.SEMI_COLON;
    }

    /**
     * Generates a aggregation definition string from a AggregationConfig object
     *
     * @param aggregation The AggregationConfig object to be converted
     * @return The converted aggregation definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateAggregationString(AggregationConfig aggregation) throws CodeGenerationException {
        if (aggregation == null) {
            throw new CodeGenerationException("A given aggregation element is empty");
        } else if (aggregation.getName() == null || aggregation.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given aggregation element is empty");
        } else if (aggregation.getFrom() == null || aggregation.getFrom().isEmpty()) {
            throw new CodeGenerationException("The 'from' value of " + aggregation.getName() + " is empty");
        } else if (aggregation.getAggregateByTimePeriod() == null) {
            throw new CodeGenerationException("The 'aggregateByTimePeriod' value of " + aggregation.getName()
                    + " is empty");
        } else if (aggregation.getAggregateByTimePeriod().getType() == null
                || aggregation.getAggregateByTimePeriod().getType().isEmpty()) {
            throw new CodeGenerationException("The aggregateByTimePeriod 'type' value of "
                    + aggregation.getName() + " is empty");
        }

        StringBuilder aggregationStringBuilder = new StringBuilder();
        aggregationStringBuilder.append(CodeGeneratorHelper.getStore(aggregation.getStore()))
                .append(CodeGeneratorHelper.getAggregationAnnotations(aggregation.getAnnotationList()))
                .append(SiddhiStringBuilderConstants.DEFINE_AGGREGATION)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(aggregation.getName())
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.FROM)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(aggregation.getFrom())
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(CodeGeneratorHelper.getQuerySelect(aggregation.getSelect()))
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(CodeGeneratorHelper.getQueryGroupBy(aggregation.getGroupBy()))
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.AGGREGATE);

        if (aggregation.getAggregateByAttribute() != null && !aggregation.getAggregateByAttribute().isEmpty()) {
            aggregationStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(SiddhiStringBuilderConstants.BY)
                    .append(SiddhiStringBuilderConstants.SPACE)
                    .append(aggregation.getAggregateByAttribute());
        }

        aggregationStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.EVERY)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(CodeGeneratorHelper.getAggregateByTimePeriod(aggregation.getAggregateByTimePeriod()))
                .append(SiddhiStringBuilderConstants.SEMI_COLON);

        return aggregationStringBuilder.toString();
    }

    /**
     * Generates a function definition string from a FunctionConfig object
     *
     * @param function The FunctionConfig object to be converted
     * @return The converted function definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateFunctionString(FunctionConfig function) throws CodeGenerationException {
        if (function == null) {
            throw new CodeGenerationException("A given function element is empty");
        } else if (function.getName() == null || function.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given function element is empty");
        } else if (function.getScriptType() == null || function.getScriptType().isEmpty()) {
            throw new CodeGenerationException("The 'script type' of " + function.getName() + " is empty");
        } else if (function.getReturnType() == null || function.getReturnType().isEmpty()) {
            throw new CodeGenerationException("The return type of " + function.getName() + " is empty");
        } else if (function.getBody() == null || function.getBody().isEmpty()) {
            throw new CodeGenerationException("The 'body' value of " + function.getName() + " is empty");
        }

        return SiddhiStringBuilderConstants.DEFINE_FUNCTION +
                SiddhiStringBuilderConstants.SPACE +
                function.getName() +
                SiddhiStringBuilderConstants.OPEN_SQUARE_BRACKET +
                function.getScriptType() +
                SiddhiStringBuilderConstants.CLOSE_SQUARE_BRACKET +
                SiddhiStringBuilderConstants.SPACE +
                SiddhiStringBuilderConstants.RETURN +
                SiddhiStringBuilderConstants.SPACE +
                function.getReturnType().toLowerCase() +
                SiddhiStringBuilderConstants.SPACE +
                SiddhiStringBuilderConstants.OPEN_CURLY_BRACKET +
                function.getBody().trim() +
                SiddhiStringBuilderConstants.CLOSE_CURLY_BRACKET +
                SiddhiStringBuilderConstants.SEMI_COLON;
    }

    /**
     * Generates a query definition string from a QueryConfig object
     *
     * @param query The QueryConfig object to be converted
     * @return The converted query definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateQueryString(QueryConfig query) throws CodeGenerationException {
        if (query == null) {
            throw new CodeGenerationException("A given query element is empty");
        }

        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append(CodeGeneratorHelper.getAnnotations(query.getAnnotationList()))
                .append(CodeGeneratorHelper.getQueryInput(query.getQueryInput()))
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(CodeGeneratorHelper.getQuerySelect(query.getSelect()));

        if (query.getGroupBy() != null && !query.getGroupBy().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getQueryGroupBy(query.getGroupBy()));
        }
        if (query.getHaving() != null && !query.getHaving().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getQueryHaving(query.getHaving()));
        }
        if (query.getOrderBy() != null && !query.getOrderBy().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getQueryOrderBy(query.getOrderBy()));
        }
        if (query.getLimit() != 0) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getQueryLimit(query.getLimit()));
        }
        if (query.getOutputRateLimit() != null && !query.getOutputRateLimit().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getQueryOutputRateLimit(query.getOutputRateLimit()));
        }

        queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                .append(CodeGeneratorHelper.getQueryOutput(query.getQueryOutput()));

        return queryStringBuilder.toString();
    }

    /**
     * Generates a partition definition string from a PartitionConfig object
     *
     * @param partition The PartitionConfig object to be converted
     * @return The converted partition definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generatePartitionString(PartitionConfig partition) throws CodeGenerationException {
        if (partition == null) {
            throw new CodeGenerationException("A given partition object is empty");
        } else if (partition.getPartitionWith() == null || partition.getPartitionWith().isEmpty()) {
            throw new CodeGenerationException("The 'partitionWith' value of a given partition element is empty");
        } else if (partition.getQueryLists() == null || partition.getQueryLists().isEmpty()) {
            throw new CodeGenerationException("The query lists of a given partition element is empty");
        }

        StringBuilder partitionStringBuilder = new StringBuilder();

        partitionStringBuilder.append(CodeGeneratorHelper.getAnnotations(partition.getAnnotationList()))
                .append(SiddhiStringBuilderConstants.PARTITION_WITH)
                .append(SiddhiStringBuilderConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getPartitionWith(partition.getPartitionWith()))
                .append(SiddhiStringBuilderConstants.CLOSE_BRACKET)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.BEGIN)
                .append(SiddhiStringBuilderConstants.SPACE);

        List<QueryConfig> queries = new LinkedList<>();
        for (List<QueryConfig> queryList : partition.getQueryLists().values()) {
            queries.addAll(queryList);
        }

        if (!queries.isEmpty()) {
            for (QueryConfig query : CodeGeneratorHelper.orderPartitionQueries(queries)) {
                partitionStringBuilder.append(generateQueryString(query))
                        .append(SiddhiStringBuilderConstants.NEW_LINE);
            }
        }

        partitionStringBuilder.append(SiddhiStringBuilderConstants.END)
                .append(SiddhiStringBuilderConstants.SEMI_COLON)
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        return partitionStringBuilder.toString();
    }

    /**
     * Generates a source/sink definition string from a SourceSinkConfig object
     *
     * @param sourceSink The SourceSinkConfig object to be converted
     * @return The converted source/sink definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateSourceSinkString(SourceSinkConfig sourceSink) throws CodeGenerationException {
        if (sourceSink == null) {
            throw new CodeGenerationException("A given source/sink element is empty");
        } else if (sourceSink.getAnnotationType() == null || sourceSink.getAnnotationType().isEmpty()) {
            throw new CodeGenerationException("The annotation type for a given source/sink element is empty");
        } else if (sourceSink.getType() == null || sourceSink.getType().isEmpty()) {
            throw new CodeGenerationException("The type attribute for a given source/sink element is empty");
        }

        StringBuilder sourceSinkStringBuilder = new StringBuilder();
        if (sourceSink.getAnnotationType().equalsIgnoreCase(CodeGeneratorConstants.SOURCE)) {
            sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.SOURCE_ANNOTATION);
        } else if (sourceSink.getAnnotationType().equalsIgnoreCase(CodeGeneratorConstants.SINK)) {
            sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.SINK_ANNOTATION);
        } else {
            throw new CodeGenerationException("Unidentified source/sink type: " + sourceSink.getType());
        }

        sourceSinkStringBuilder.append(sourceSink.getType())
                .append(SiddhiStringBuilderConstants.SINGLE_QUOTE);
        if (sourceSink.getOptions() != null && !sourceSink.getOptions().isEmpty()) {
            sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.COMMA)
                    .append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getParameterList(sourceSink.getOptions()));
        }

        if (sourceSink.getMap() != null) {
            sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.COMMA)
                    .append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getMapper(sourceSink.getMap(), sourceSink.getAnnotationType()));
        }

        sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.CLOSE_BRACKET);

        return sourceSinkStringBuilder.toString();
    }

}
