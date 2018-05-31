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
     */
    public String generateSiddhiAppCode(EventFlow eventFlow) {
        SiddhiAppConfig siddhiApp = eventFlow.getSiddhiAppConfig();
        StringBuilder siddhiAppStringBuilder = new StringBuilder();
        siddhiAppStringBuilder
                .append(generateAppNameAndDescription(siddhiApp.getAppName(), siddhiApp.getAppDescription()))
                .append(generateStreams(siddhiApp.getStreamList(), siddhiApp.getSourceList(), siddhiApp.getSinkList()))
                .append(generateTables(siddhiApp.getTableList()))
                .append(generateWindows(siddhiApp.getWindowList()))
                .append(generateTriggers(siddhiApp.getTriggerList(), siddhiApp.getSourceList(),
                        siddhiApp.getSinkList()))
                .append(generateAggregations(siddhiApp.getAggregationList()))
                .append(generateFunctions(siddhiApp.getFunctionList()))
                .append(generateQueries(siddhiApp.getQueryLists()))
                .append(generatePartitions(null));

        return siddhiAppStringBuilder.toString();
    }

    /**
     * Generates a string representation of the siddhi app name and description annotations
     *
     * @param appName        The app name
     * @param appDescription The app description
     * @return The string representation of the siddhi app name and description annotations
     */
    private String generateAppNameAndDescription(String appName, String appDescription) {
        StringBuilder appNameAndDescriptionStringBuilder = new StringBuilder();

        if (appName != null && !appName.isEmpty()) {
            appNameAndDescriptionStringBuilder.append(SiddhiStringBuilderConstants.APP_NAME_ANNOTATION)
                    .append(appName)
                    .append(SiddhiStringBuilderConstants.SINGLE_QUOTE)
                    .append(SiddhiStringBuilderConstants.CLOSE_BRACKET)
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        } else {
            appNameAndDescriptionStringBuilder.append(SiddhiStringBuilderConstants.DEFAULT_APP_NAME_ANNOTATION)
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        }

        if (appDescription != null && !appDescription.isEmpty()) {
            appNameAndDescriptionStringBuilder.append(SiddhiStringBuilderConstants.APP_DESCRIPTION_ANNOTATION)
                    .append(appDescription)
                    .append(SiddhiStringBuilderConstants.SINGLE_QUOTE)
                    .append(SiddhiStringBuilderConstants.CLOSE_BRACKET)
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        } else {
            appNameAndDescriptionStringBuilder.append(SiddhiStringBuilderConstants.DEFAULT_APP_DESCRIPTION_ANNOTATION)
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        }

        appNameAndDescriptionStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return appNameAndDescriptionStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the streams in a Siddhi app
     *
     * @param streamList A list of StreamConfig objects from the SiddhiAppConfig object
     * @param sourceList A list of sources from the SiddhiAppConfig object
     * @param sinkList   A list of sinks from the SiddhiAppConfig object
     * @return The string representation of the stream definitions
     */
    private String generateStreams(List<StreamConfig> streamList, List<SourceSinkConfig> sourceList,
                                   List<SourceSinkConfig> sinkList) {
        if (streamList == null || streamList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder streamListStringBuilder = new StringBuilder();
        streamListStringBuilder.append("-- Streams")
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (StreamConfig stream : streamList) {
            if (stream.isInnerStream()) {
                break;
            }

            for (SourceSinkConfig source : sourceList) {
                if (stream.getName().equals(source.getConnectedElementName())) {
                    streamListStringBuilder.append(generateSourceSinkString(source))
                            .append(SiddhiStringBuilderConstants.NEW_LINE);
                }
            }

            for (SourceSinkConfig sink : sinkList) {
                if (stream.getName().equals(sink.getConnectedElementName())) {
                    streamListStringBuilder.append(generateSourceSinkString(sink))
                            .append(SiddhiStringBuilderConstants.NEW_LINE);
                }
            }

            streamListStringBuilder.append(generateStreamString(stream))
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        }

        streamListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return streamListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the tables in a Siddhi app
     *
     * @param tableList A list of TableConfig objects from the SiddhiAppConfig object
     * @return The Siddhi string representation of the table definitions
     */
    private String generateTables(List<TableConfig> tableList) {
        if (tableList == null || tableList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder tableListStringBuilder = new StringBuilder();
        tableListStringBuilder.append("-- Tables")
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (TableConfig table : tableList) {
            tableListStringBuilder.append(generateTableString(table))
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        }

        tableListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return tableListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the window definitions in a Siddhi app
     *
     * @param windowList A list of WindowConfig objects to be converted
     * @return The Siddhi string representaiotn of all the window definitions
     */
    private String generateWindows(List<WindowConfig> windowList) {
        if (windowList == null || windowList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder windowListStringBuilder = new StringBuilder();
        windowListStringBuilder.append("-- Windows")
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (WindowConfig window : windowList) {
            windowListStringBuilder.append(generateWindowString(window))
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        }

        windowListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return windowListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the trigger definitions in a Siddhi app
     *
     * @param triggerList A list of all the TriggerConfig objects to be converted
     * @param sourceList  A list of all sources in a SiddhiAppConfig object
     * @param sinkList    A list of all the sinks in a SiddhiAppConfig object
     * @return The Siddhi string representation of all the trigger definitions
     */
    private String generateTriggers(List<TriggerConfig> triggerList, List<SourceSinkConfig> sourceList,
                                    List<SourceSinkConfig> sinkList) {
        if (triggerList == null || triggerList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder triggerListStringBuilder = new StringBuilder();
        triggerListStringBuilder.append("-- Triggers")
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (TriggerConfig trigger : triggerList) {

            for (SourceSinkConfig source : sourceList) {
                if (trigger.getName().equals(source.getConnectedElementName())) {
                    triggerListStringBuilder.append(generateSourceSinkString(source))
                            .append(SiddhiStringBuilderConstants.NEW_LINE);
                }
            }

            for (SourceSinkConfig sink : sinkList) {
                if (trigger.getName().equals(sink.getConnectedElementName())) {
                    triggerListStringBuilder.append(generateSourceSinkString(sink))
                            .append(SiddhiStringBuilderConstants.NEW_LINE);
                }
            }

            triggerListStringBuilder.append(generateTriggerString(trigger))
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        }

        triggerListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return triggerListStringBuilder.toString();
    }

    /**
     * Genenerates a string representation of all the aggregation definitions in a Siddhi app
     *
     * @param aggregationList A list of AggregationConfig objects to be converted
     * @return The Siddhi string representation of all the aggregation definitions
     */
    private String generateAggregations(List<AggregationConfig> aggregationList) {
        if (aggregationList == null || aggregationList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder aggregationListStringBuilder = new StringBuilder();
        aggregationListStringBuilder.append("-- Aggregations")
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (AggregationConfig aggregation : aggregationList) {
            aggregationListStringBuilder.append(generateAggregationString(aggregation))
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        }

        aggregationListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return aggregationListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the function definitions in a Siddhi app
     *
     * @param functionList A list of FunctionConfig objects to be converted
     * @return The Siddhi string representation of all the function definitions
     */
    private String generateFunctions(List<FunctionConfig> functionList) {
        if (functionList == null || functionList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder functionListStringBuilder = new StringBuilder();
        functionListStringBuilder.append("-- Functions")
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        for (FunctionConfig function : functionList) {
            functionListStringBuilder.append(generateFunctionString(function))
                    .append(SiddhiStringBuilderConstants.NEW_LINE);
        }

        functionListStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE);

        return functionListStringBuilder.toString();
    }

    /**
     * Generates a string representation of all the queries in a Siddhi app
     *
     * @param queryLists A list of QueryConfig objects to be converted
     * @return The Siddhi string representation of the given QueryConfig list
     */
    private String generateQueries(Map<QueryListType, List<QueryConfig>> queryLists) {
        if (queryLists == null || queryLists.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder queryListStringBuilder = new StringBuilder();


        boolean hasQueries = false;
        for (List<QueryConfig> queryList : queryLists.values()) {
            if (queryList != null && !queryList.isEmpty()) {
                if (!hasQueries) {
                    hasQueries = true;
                    queryListStringBuilder.append("-- Queries")
                            .append(SiddhiStringBuilderConstants.NEW_LINE);
                }

                for (QueryConfig query : queryList) {
                    queryListStringBuilder.append(generateQueryString(query))
                            .append(SiddhiStringBuilderConstants.NEW_LINE)
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
     */
    private String generatePartitions(List<PartitionConfig> partitionList) {
        if (partitionList == null || partitionList.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder partitionListStringBuilder = new StringBuilder();
        partitionListStringBuilder.append("-- Partitions");
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
     */
    private String generateStreamString(StreamConfig stream) {
        if (stream == null) {
            throw new CodeGenerationException("The given StreamConfig object is null");
        } else if (stream.getName() == null || stream.getName().isEmpty()) {
            throw new CodeGenerationException("The stream name for the given StreamConfig object is null/empty");
        }

        StringBuilder streamStringBuilder = new StringBuilder();
        streamStringBuilder.append(CodeGeneratorHelper.getAnnotations(stream.getAnnotationList()))
                .append(SiddhiStringBuilderConstants.DEFINE_STREAM)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(stream.getName())
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getAttributes(stream.getAttributeList()))
                .append(SiddhiStringBuilderConstants.CLOSE_BRACKET)
                .append(SiddhiStringBuilderConstants.SEMI_COLON);

        return streamStringBuilder.toString();
    }

    /**
     * Generates a table definition string from a TableConfig object
     *
     * @param table The TableConfig object to be converted
     * @return The converted table definition string
     */
    private String generateTableString(TableConfig table) {
        if (table == null) {
            throw new CodeGenerationException("The given TableConfig object is null");
        } else if (table.getName() == null || table.getName().isEmpty()) {
            throw new CodeGenerationException("The table name for the given TableConfig object is null/empty");
        }

        StringBuilder tableStringBuilder = new StringBuilder();
        tableStringBuilder.append(CodeGeneratorHelper.getStore(table.getStore()))
                .append(CodeGeneratorHelper.getAnnotations(table.getAnnotationList()))
                .append(SiddhiStringBuilderConstants.DEFINE_TABLE)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(table.getName())
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getAttributes(table.getAttributeList()))
                .append(SiddhiStringBuilderConstants.CLOSE_BRACKET)
                .append(SiddhiStringBuilderConstants.SEMI_COLON);

        return tableStringBuilder.toString();
    }

    /**
     * Generates a window definition string from a WindowConfig object
     *
     * @param window The WindowConfig object to be converted
     * @return The converted window definition string
     */
    private String generateWindowString(WindowConfig window) {
        if (window == null) {
            throw new CodeGenerationException("The given WindowConfig object is null");
        } else if (window.getName() == null || window.getName().isEmpty()) {
            throw new CodeGenerationException("The window name for the given WindowConfig object is null/empty");
        } else if (window.getFunction() == null || window.getFunction().isEmpty()) {
            throw new CodeGenerationException("The function name for the given WindowConfig object is null/empty");
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
                    throw new CodeGenerationException("Unidentified output event type for WindowConfig: "
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
     */
    private String generateTriggerString(TriggerConfig trigger) {
        if (trigger == null) {
            throw new CodeGenerationException("The given TriggerConfig object is null");
        } else if (trigger.getName() == null || trigger.getName().isEmpty()) {
            throw new CodeGenerationException("The trigger name for the given TriggerConfig object is null/empty");
        } else if (trigger.getAt() == null || trigger.getAt().isEmpty()) {
            throw new CodeGenerationException("The 'at' value for the given TriggerConfig object is null/empty");
        }

        StringBuilder triggerStringBuilder = new StringBuilder();
        triggerStringBuilder.append(CodeGeneratorHelper.getAnnotations(trigger.getAnnotationList()))
                .append(SiddhiStringBuilderConstants.DEFINE_TRIGGER)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(trigger.getName())
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.AT)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(trigger.getAt())
                .append(SiddhiStringBuilderConstants.SEMI_COLON);

        return triggerStringBuilder.toString();
    }

    /**
     * Generates a aggregation definition string from a AggregationConfig object
     *
     * @param aggregation The AggregationConfig object to be converted
     * @return The converted aggregation definition string
     */
    private String generateAggregationString(AggregationConfig aggregation) {
        if (aggregation == null) {
            throw new CodeGenerationException("The AggregationConfig object is null");
        } else if (aggregation.getName() == null || aggregation.getName().isEmpty()) {
            throw new CodeGenerationException("The aggregation name for the given AggregationConfig" +
                    " object is null/empty");
        } else if (aggregation.getFrom() == null || aggregation.getFrom().isEmpty()) {
            throw new CodeGenerationException("The input stream for aggregation  is null");
        } else if (aggregation.getAggregateByTimePeriod() == null) {
            throw new CodeGenerationException("The AggregateByTimePeriod instance is null");
        } else if (aggregation.getAggregateByTimePeriod().getMinValue() == null ||
                aggregation.getAggregateByTimePeriod().getMinValue().isEmpty()) {
            throw new CodeGenerationException("The aggregate by time period must have atleast one" +
                    " value for aggregation");
        }

        StringBuilder aggregationStringBuilder = new StringBuilder();
        aggregationStringBuilder.append(CodeGeneratorHelper.getStore(aggregation.getStore()))
                .append(CodeGeneratorHelper.getAggregationAnnotations(aggregation.getAnnotationList()))
                .append(SiddhiStringBuilderConstants.DEFINE_AGGREGATION)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(aggregation.getName())
                .append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(SiddhiStringBuilderConstants.TAB_SPACE)
                .append(SiddhiStringBuilderConstants.FROM)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(aggregation.getFrom())
                .append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(SiddhiStringBuilderConstants.TAB_SPACE)
                .append(CodeGeneratorHelper.getQuerySelect(aggregation.getSelect()))
                .append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(SiddhiStringBuilderConstants.TAB_SPACE)
                .append(CodeGeneratorHelper.getQueryGroupBy(aggregation.getGroupBy()))
                .append(SiddhiStringBuilderConstants.NEW_LINE)
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
                .append(aggregation.getAggregateByTimePeriod().getMinValue().toLowerCase());

        if (aggregation.getAggregateByTimePeriod().getMaxValue() != null &&
                !aggregation.getAggregateByTimePeriod().getMaxValue().isEmpty() &&
                !aggregation.getAggregateByTimePeriod().getMaxValue()
                        .equalsIgnoreCase(aggregation.getAggregateByTimePeriod().getMinValue())) {
            aggregationStringBuilder.append(SiddhiStringBuilderConstants.THREE_DOTS)
                    .append(aggregation.getAggregateByTimePeriod().getMaxValue().toLowerCase());
        }

        aggregationStringBuilder.append(SiddhiStringBuilderConstants.SEMI_COLON);

        return aggregationStringBuilder.toString();
    }

    /**
     * Generates a function definition string from a FunctionConfig object
     *
     * @param function The FunctionConfig object to be converted
     * @return The converted function definition string
     */
    private String generateFunctionString(FunctionConfig function) {
        if (function == null) {
            throw new CodeGenerationException("The given FunctionConfig object is null");
        } else if (function.getName() == null || function.getName().isEmpty()) {
            throw new CodeGenerationException("The function name for the given FunctionConfig object is null/empty");
        } else if (function.getScriptType() == null || function.getScriptType().isEmpty()) {
            throw new CodeGenerationException("The script type for the given FunctionConfig object is null/empty");
        } else if (function.getReturnType() == null || function.getReturnType().isEmpty()) {
            throw new CodeGenerationException("The return type for the given FunctionConfig is null/empty");
        } else if (function.getBody() == null || function.getBody().isEmpty()) {
            throw new CodeGenerationException("The body for the given FunctionConfig is null/empty");
        }

        StringBuilder functionStringBuilder = new StringBuilder();
        functionStringBuilder.append(SiddhiStringBuilderConstants.DEFINE_FUNCTION)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(function.getName())
                .append(SiddhiStringBuilderConstants.OPEN_SQUARE_BRACKET)
                .append(function.getScriptType())
                .append(SiddhiStringBuilderConstants.CLOSE_SQUARE_BRACKET)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.RETURN)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(function.getReturnType().toLowerCase())
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.OPEN_CURLY_BRACKET)
                .append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(SiddhiStringBuilderConstants.TAB_SPACE)
                .append(function.getBody().trim())
                .append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(SiddhiStringBuilderConstants.CLOSE_CURLY_BRACKET)
                .append(SiddhiStringBuilderConstants.SEMI_COLON);

        return functionStringBuilder.toString();
    }

    /**
     * Generates a query definition string from a QueryConfig object
     *
     * @param query The QueryConfig object to be converted
     * @return The converted query definition string
     */
    private String generateQueryString(QueryConfig query) {
        if (query == null) {
            throw new CodeGenerationException("The given QueryConfig object is null");
        }

        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append(CodeGeneratorHelper.getAnnotations(query.getAnnotationList()))
                .append(CodeGeneratorHelper.getQueryInput(query.getQueryInput()))
                .append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(CodeGeneratorHelper.getQuerySelect(query.getSelect()));

        if (query.getGroupBy() != null && !query.getGroupBy().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryGroupBy(query.getGroupBy()));
        }
        if (query.getOrderBy() != null && !query.getOrderBy().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryOrderBy(query.getOrderBy()));
        }
        if (query.getLimit() != 0) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryLimit(query.getLimit()));
        }
        if (query.getHaving() != null && !query.getHaving().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryHaving(query.getHaving()));
        }
        if (query.getOutputRateLimit() != null && !query.getOutputRateLimit().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryOutputRateLimit(query.getOutputRateLimit()));
        }

        queryStringBuilder.append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(CodeGeneratorHelper.getQueryOutput(query.getQueryOutput()));

        return queryStringBuilder.toString();
    }

    /**
     * Generates a partition definition string from a PartitionConfig object
     *
     * @param partition The PartitionConfig object to be converted
     * @return The converted partition definition string
     */
    private String generatePartitionString(PartitionConfig partition) {
        if (partition == null) {
            throw new CodeGenerationException("The given PartitionConfig object is null");
        } else if (partition.getPartitionWith() == null || partition.getPartitionWith().isEmpty()) {
            throw new CodeGenerationException("The 'partitionWith' value for the given" +
                    " PartitionConfig object is null/empty");
        }

        StringBuilder partitionStringBuilder = new StringBuilder();

        partitionStringBuilder.append(SiddhiStringBuilderConstants.PARTITION_WITH)
                .append(SiddhiStringBuilderConstants.OPEN_BRACKET)
                .append(partition.getPartitionWith())
                .append(SiddhiStringBuilderConstants.CLOSE_BRACKET)
                .append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(SiddhiStringBuilderConstants.BEGIN)
                .append(SiddhiStringBuilderConstants.NEW_LINE)
                .append(generateQueries(partition.getQueryLists()))
                .append(SiddhiStringBuilderConstants.END)
                .append(SiddhiStringBuilderConstants.SEMI_COLON)
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        return partitionStringBuilder.toString();
    }

    /**
     * Generates a source/sink definition string from a SourceSinkConfig object
     *
     * @param sourceSink The SourceSinkConfig object to be converted
     * @return The converted source/sink definition string
     */
    private String generateSourceSinkString(SourceSinkConfig sourceSink) {
        if (sourceSink == null) {
            throw new CodeGenerationException("The given SourceSinkConfig object is null");
        } else if (sourceSink.getAnnotationType() == null || sourceSink.getAnnotationType().isEmpty()) {
            throw new CodeGenerationException("The annotation type for the given SourceSinkConfig object is null/empty");
        } else if (sourceSink.getType() == null || sourceSink.getType().isEmpty()) {
            throw new CodeGenerationException("The type of source/sink for the given SourceSinkConfig is null/empty");
        }

        StringBuilder sourceSinkStringBuilder = new StringBuilder();
        if (sourceSink.getAnnotationType().equalsIgnoreCase(CodeGeneratorConstants.SOURCE)) {
            sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.SOURCE_ANNOTATION);
        } else if (sourceSink.getAnnotationType().equalsIgnoreCase(CodeGeneratorConstants.SINK)) {
            sourceSinkStringBuilder.append(SiddhiStringBuilderConstants.SINK_ANNOTATION);
        } else {
            throw new CodeGenerationException("Unknown type: " + sourceSink.getType() +
                    ". The SinkSourceConfig can only have type 'SINK' or type 'SOURCE'");
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
