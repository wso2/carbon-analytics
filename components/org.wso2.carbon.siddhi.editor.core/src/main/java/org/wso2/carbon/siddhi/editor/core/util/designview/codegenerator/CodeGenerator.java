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
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorHelper;

import java.util.List;
import java.util.Map;

/**
 * Used to convert an EventFlow object to a Siddhi app string
 */
public class CodeGenerator {

    // TODO: 5/2/18 Look for constants for all the cases in switch case
    // TODO: 5/24/18 Improve The Information Given In The Error Messages

    /**
     * Converts a EventFlow object to a Siddhi app string
     *
     * @param eventFlow The EventFlow object to be converted
     * @return The Siddhi app string representation of the given EventFlow object
     */
    public String generateSiddhiAppCode(EventFlow eventFlow) {
        SiddhiAppConfig siddhiApp = eventFlow.getSiddhiAppConfig();
        StringBuilder siddhiAppStringBuilder = new StringBuilder();
        siddhiAppStringBuilder
                .append(generateAppNameAndDescription(siddhiApp.getAppName(), siddhiApp.getAppDescription()))
                .append(generateStreams(siddhiApp.getStreamList(), siddhiApp.getSourceList(), siddhiApp.getSinkList()))
                .append(generateTables(siddhiApp.getTableList()))
                .append(generateWindows(siddhiApp.getWindowList()))
                .append(generateTriggers(siddhiApp.getTriggerList(), siddhiApp.getSourceList(), siddhiApp.getSinkList()))
                .append(generateAggregations(siddhiApp.getAggregationList()))
                .append(generateFunctions(siddhiApp.getFunctionList()))
                .append(generateQueries(siddhiApp.getQueryLists()))
                .append(generatePartitions(null));

        return siddhiAppStringBuilder.toString();
    }

    /**
     * Generates a string representation of the Siddhi app name and description annotations
     * based on the given parameters
     *
     * @param appName        The name of the Siddhi app
     * @param appDescription The description of the siddhi app
     * @return The Siddhi annotation representation of the name and the description
     */
    private String generateAppNameAndDescription(String appName, String appDescription) {
        StringBuilder appNameAndDescriptionStringBuilder = new StringBuilder();

        if (appName != null && !appName.isEmpty()) {
            appNameAndDescriptionStringBuilder.append(SiddhiCodeConstants.APP_NAME)
                    .append(appName)
                    .append(SiddhiCodeConstants.SINGLE_QUOTE)
                    .append(SiddhiCodeConstants.CLOSE_BRACKET)
                    .append(SiddhiCodeConstants.NEW_LINE);
        } else {
            appNameAndDescriptionStringBuilder.append(SiddhiCodeConstants.DEFAULT_APP_NAME)
                    .append(SiddhiCodeConstants.NEW_LINE);
        }

        if (appDescription != null && !appDescription.isEmpty()) {
            appNameAndDescriptionStringBuilder.append(SiddhiCodeConstants.APP_DESCRIPTION)
                    .append(appDescription)
                    .append(SiddhiCodeConstants.SINGLE_QUOTE)
                    .append(SiddhiCodeConstants.CLOSE_BRACKET)
                    .append(SiddhiCodeConstants.NEW_LINE);
        } else {
            appNameAndDescriptionStringBuilder.append(SiddhiCodeConstants.DEFAULT_APP_DESCRIPTION)
                    .append(SiddhiCodeConstants.NEW_LINE);
        }

        appNameAndDescriptionStringBuilder.append(SiddhiCodeConstants.NEW_LINE);

        return appNameAndDescriptionStringBuilder.toString();
    }

    private String generateStreams(List<StreamConfig> streamList, List<SourceSinkConfig> sourceList,
                                   List<SourceSinkConfig> sinkList) {
        // TODO source and sink should be somehow connected to a stream over here
        if (streamList == null || streamList.isEmpty()) {
            return SiddhiCodeConstants.EMPTY_STRING;
        }

        StringBuilder streamListStringBuilder = new StringBuilder();
        streamListStringBuilder.append("-- Streams")
                .append(SiddhiCodeConstants.NEW_LINE);

        for (StreamConfig stream : streamList) {
            // TODO: 5/29/18 Check for inner streams
            if (stream.isInnerStream()) {
                break;
            }

            for (SourceSinkConfig source : sourceList) {
                // TODO: 5/29/18 This might have to be .equalsIgnoreCase() Check whether it is possible
                if (stream.getName().equals(source.getConnectedElementName())) {
                    streamListStringBuilder.append(generateSourceSinkString(source))
                            .append(SiddhiCodeConstants.NEW_LINE);
                }
            }

            for (SourceSinkConfig sink : sinkList) {
                if (stream.getName().equals(sink.getConnectedElementName())) {
                    streamListStringBuilder.append(generateSourceSinkString(sink))
                            .append(SiddhiCodeConstants.NEW_LINE);
                }
            }

            streamListStringBuilder.append(generateStreamString(stream))
                    .append(SiddhiCodeConstants.NEW_LINE);
        }

        streamListStringBuilder.append(SiddhiCodeConstants.NEW_LINE);

        return streamListStringBuilder.toString();
    }

    private String generateTables(List<TableConfig> tableList) {
        if (tableList == null || tableList.isEmpty()) {
            return SiddhiCodeConstants.EMPTY_STRING;
        }

        StringBuilder tableListStringBuilder = new StringBuilder();
        tableListStringBuilder.append("-- Tables")
                .append(SiddhiCodeConstants.NEW_LINE);

        for (TableConfig table : tableList) {
            tableListStringBuilder.append(generateTableString(table))
                    .append(SiddhiCodeConstants.NEW_LINE);
        }

        tableListStringBuilder.append(SiddhiCodeConstants.NEW_LINE);

        return tableListStringBuilder.toString();
    }

    private String generateWindows(List<WindowConfig> windowList) {
        if (windowList == null || windowList.isEmpty()) {
            return SiddhiCodeConstants.EMPTY_STRING;
        }

        StringBuilder windowListStringBuilder = new StringBuilder();
        windowListStringBuilder.append("-- Windows")
                .append(SiddhiCodeConstants.NEW_LINE);

        for (WindowConfig window : windowList) {
            windowListStringBuilder.append(generateWindowString(window))
                    .append(SiddhiCodeConstants.NEW_LINE);
        }

        windowListStringBuilder.append(SiddhiCodeConstants.NEW_LINE);

        return windowListStringBuilder.toString();
    }

    private String generateTriggers(List<TriggerConfig> triggerList, List<SourceSinkConfig> sourceList,
                                    List<SourceSinkConfig> sinkList) {
        // TODO: 5/28/18 NOTE - Triggers can have sources and sinks as well
        if (triggerList == null || triggerList.isEmpty()) {
            return SiddhiCodeConstants.EMPTY_STRING;
        }

        StringBuilder triggerListStringBuilder = new StringBuilder();
        triggerListStringBuilder.append("-- Triggers")
                .append(SiddhiCodeConstants.NEW_LINE);

        for (TriggerConfig trigger : triggerList) {

            for (SourceSinkConfig source : sourceList) {
                if (trigger.getName().equals(source.getConnectedElementName())) {
                    triggerListStringBuilder.append(generateSourceSinkString(source))
                            .append(SiddhiCodeConstants.NEW_LINE);
                }
            }

            for (SourceSinkConfig sink : sinkList) {
                if (trigger.getName().equals(sink.getConnectedElementName())) {
                    triggerListStringBuilder.append(generateSourceSinkString(sink))
                            .append(SiddhiCodeConstants.NEW_LINE);
                }
            }

            triggerListStringBuilder.append(generateTriggerString(trigger))
                    .append(SiddhiCodeConstants.NEW_LINE);
        }

        triggerListStringBuilder.append(SiddhiCodeConstants.NEW_LINE);

        return triggerListStringBuilder.toString();
    }

    private String generateAggregations(List<AggregationConfig> aggregationList) {
        if (aggregationList == null || aggregationList.isEmpty()) {
            return SiddhiCodeConstants.EMPTY_STRING;
        }

        StringBuilder aggregationListStringBuilder = new StringBuilder();
        aggregationListStringBuilder.append("-- Aggregations")
                .append(SiddhiCodeConstants.NEW_LINE);

        for (AggregationConfig aggregation : aggregationList) {
            aggregationListStringBuilder.append(generateAggregationString(aggregation))
                    .append(SiddhiCodeConstants.NEW_LINE);
        }

        aggregationListStringBuilder.append(SiddhiCodeConstants.NEW_LINE);

        return aggregationListStringBuilder.toString();
    }

    private String generateFunctions(List<FunctionConfig> functionList) {
        if (functionList == null || functionList.isEmpty()) {
            return SiddhiCodeConstants.EMPTY_STRING;
        }

        StringBuilder functionListStringBuilder = new StringBuilder();
        functionListStringBuilder.append("-- Functions")
                .append(SiddhiCodeConstants.NEW_LINE);

        for (FunctionConfig function : functionList) {
            functionListStringBuilder.append(generateFunctionString(function))
                    .append(SiddhiCodeConstants.NEW_LINE);
        }

        functionListStringBuilder.append(SiddhiCodeConstants.NEW_LINE);

        return functionListStringBuilder.toString();
    }

    private String generateQueries(Map<String, List<QueryConfig>> queryLists) {
        if (queryLists == null || queryLists.isEmpty()) {
            return SiddhiCodeConstants.EMPTY_STRING;
        }

        StringBuilder queryListStringBuilder = new StringBuilder();
        queryListStringBuilder.append("-- Queries")
                .append(SiddhiCodeConstants.NEW_LINE);

        for (List<QueryConfig> queryList : queryLists.values()) {
            for (QueryConfig query : queryList) {
                queryListStringBuilder.append(generateQueryString(query))
                        .append(SiddhiCodeConstants.NEW_LINE)
                        .append(SiddhiCodeConstants.NEW_LINE);
            }
        }

        queryListStringBuilder.append(SiddhiCodeConstants.NEW_LINE);

        return queryListStringBuilder.toString();
    }

    private String generatePartitions(List<PartitionConfig> partitionList) {
        if (partitionList == null || partitionList.isEmpty()) {
            return SiddhiCodeConstants.EMPTY_STRING;
        }

        StringBuilder partitionListStringBuilder = new StringBuilder();
        partitionListStringBuilder.append("-- Partitions");
        for (PartitionConfig partition : partitionList) {
            partitionListStringBuilder.append(generatePartitionString(partition))
                    .append(SiddhiCodeConstants.NEW_LINE);
        }

        partitionListStringBuilder.append(SiddhiCodeConstants.NEW_LINE);

        return partitionListStringBuilder.toString();
    }

    /**
     * Converts a StreamConfig object to a Siddhi stream definition string
     *
     * @param stream The StreamConfig object to be converted
     * @return The stream definition string representation of the given StreamConfig object
     */
    private String generateStreamString(StreamConfig stream) {
        if (stream == null) {
            throw new CodeGenerationException("The StreamConfig instance is null");
        } else if (stream.getName() == null || stream.getName().isEmpty()) {
            throw new CodeGenerationException("The stream name is null");
        }

        StringBuilder streamStringBuilder = new StringBuilder();
        streamStringBuilder.append(CodeGeneratorHelper.getAnnotations(stream.getAnnotationList()))
                .append(SiddhiCodeConstants.DEFINE_STREAM)
                .append(SiddhiCodeConstants.SPACE)
                .append(stream.getName())
                .append(SiddhiCodeConstants.SPACE)
                .append(SiddhiCodeConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getAttributes(stream.getAttributeList()))
                .append(SiddhiCodeConstants.CLOSE_BRACKET)
                .append(SiddhiCodeConstants.SEMI_COLON);

        return streamStringBuilder.toString();
    }

    /**
     * Converts a TableConfig object to a Siddhi table definition String
     *
     * @param table The TableConfig object to be converted
     * @return The table definition string representation of the given TableConfig object
     */
    private String generateTableString(TableConfig table) {
        if (table == null) {
            throw new CodeGenerationException("The given TableConfig instance is null");
        } else if (table.getName() == null || table.getName().isEmpty()) {
            throw new CodeGenerationException("The table name is null");
        }

        StringBuilder tableStringBuilder = new StringBuilder();
        tableStringBuilder.append(CodeGeneratorHelper.getStore(table.getStore()))
                .append(CodeGeneratorHelper.getAnnotations(table.getAnnotationList()))
                .append(SiddhiCodeConstants.DEFINE_TABLE)
                .append(SiddhiCodeConstants.SPACE)
                .append(table.getName())
                .append(SiddhiCodeConstants.SPACE)
                .append(SiddhiCodeConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getAttributes(table.getAttributeList()))
                .append(SiddhiCodeConstants.CLOSE_BRACKET)
                .append(SiddhiCodeConstants.SEMI_COLON);

        return tableStringBuilder.toString();
    }

    /**
     * Converts a WindowConfig object to a Siddhi window definition String
     *
     * @param window The WindowConfig object to be converted
     * @return The window definition string representation of the given WindowConfig object
     */
    private String generateWindowString(WindowConfig window) {
        if (window == null) {
            throw new CodeGenerationException("The given WindowConfig instance is null");
        } else if (window.getName() == null || window.getName().isEmpty()) {
            throw new CodeGenerationException("Window Name Cannot Be Null");
        } else if (window.getFunction() == null || window.getFunction().isEmpty()) {
            throw new CodeGenerationException("Window Function Name Cannot Be Null");
        }

        StringBuilder windowStringBuilder = new StringBuilder();
        windowStringBuilder.append(CodeGeneratorHelper.getAnnotations(window.getAnnotationList()))
                .append(SiddhiCodeConstants.DEFINE_WINDOW)
                .append(SiddhiCodeConstants.SPACE)
                .append(window.getName())
                .append(SiddhiCodeConstants.SPACE)
                .append(SiddhiCodeConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getAttributes(window.getAttributeList()))
                .append(SiddhiCodeConstants.CLOSE_BRACKET)
                .append(SiddhiCodeConstants.SPACE)
                .append(window.getFunction())
                .append(SiddhiCodeConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getParameterList(window.getParameters()))
                .append(SiddhiCodeConstants.CLOSE_BRACKET);

        if (window.getOutputEventType() != null && !window.getOutputEventType().isEmpty()) {
            windowStringBuilder.append(SiddhiCodeConstants.SPACE);
            switch (window.getOutputEventType().toUpperCase()) {
                // TODO: 4/26/18 The cases must be constants and not free strings
                case "CURRENT_EVENTS":
                    windowStringBuilder.append(SiddhiCodeConstants.OUTPUT_CURRENT_EVENTS);
                    break;
                case "EXPIRED_EVENTS":
                    windowStringBuilder.append(SiddhiCodeConstants.OUTPUT_EXPIRED_EVENTS);
                    break;
                case "ALL_EVENTS":
                    windowStringBuilder.append(SiddhiCodeConstants.OUTPUT_ALL_EVENTS);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified output event type: " + window.getOutputEventType());
            }
        }
        windowStringBuilder.append(SiddhiCodeConstants.SEMI_COLON);

        return windowStringBuilder.toString();
    }

    /**
     * Converts a TriggerConfig object to a Siddhi trigger definition String
     *
     * @param trigger The TriggerConfig object to be converted
     * @return The trigger definition string representation of the given TriggerConfig object
     */
    private String generateTriggerString(TriggerConfig trigger) {
        if (trigger == null) {
            throw new CodeGenerationException("The TriggerConfig instance is null");
        } else if (trigger.getName() == null || trigger.getName().isEmpty()) {
            throw new CodeGenerationException("The name of trigger is null");
        } else if (trigger.getAt() == null || trigger.getAt().isEmpty()) {
            throw new CodeGenerationException("The 'at' value of trigger is null");
        }

        StringBuilder triggerStringBuilder = new StringBuilder();
        triggerStringBuilder.append(CodeGeneratorHelper.getAnnotations(trigger.getAnnotationList()))
                .append(SiddhiCodeConstants.DEFINE_TRIGGER)
                .append(SiddhiCodeConstants.SPACE)
                .append(trigger.getName())
                .append(SiddhiCodeConstants.SPACE)
                .append(SiddhiCodeConstants.AT)
                .append(SiddhiCodeConstants.SPACE)
                .append(trigger.getAt())
                .append(SiddhiCodeConstants.SEMI_COLON);

        return triggerStringBuilder.toString();
    }

    /**
     * Converts a AggregationConfig object to a Siddhi aggregation definition String
     *
     * @param aggregation The AggregationConfig object to be converted
     * @return The aggregation definition string representation of the given AggregationConfig object
     */
    private String generateAggregationString(AggregationConfig aggregation) {
        if (aggregation == null) {
            throw new CodeGenerationException("The AggregationConfig instance is null");
        } else if (aggregation.getName() == null || aggregation.getName().isEmpty()) {
            throw new CodeGenerationException("The name of aggregation  is null");
        } else if (aggregation.getFrom() == null || aggregation.getFrom().isEmpty()) {
            throw new CodeGenerationException("The input stream for aggregation  is null");
        } else if (aggregation.getAggregateByTimePeriod() == null) {
            throw new CodeGenerationException("The AggregateByTimePeriod instance is null");
        } else if (aggregation.getAggregateByTimePeriod().getMinValue() == null || aggregation.getAggregateByTimePeriod().getMinValue().isEmpty()) {
            throw new CodeGenerationException("The aggregate by time period must have atleast one value for aggregation");
        }

        StringBuilder aggregationStringBuilder = new StringBuilder();
        aggregationStringBuilder.append(CodeGeneratorHelper.getStore(aggregation.getStore()))
                .append(CodeGeneratorHelper.getAggregationAnnotations(aggregation.getAnnotationList()))
                .append(SiddhiCodeConstants.DEFINE_AGGREGATION)
                .append(SiddhiCodeConstants.SPACE)
                .append(aggregation.getName())
                .append(SiddhiCodeConstants.NEW_LINE)
                .append(SiddhiCodeConstants.TAB_SPACE)
                .append(SiddhiCodeConstants.FROM)
                .append(SiddhiCodeConstants.SPACE)
                .append(aggregation.getFrom())
                .append(SiddhiCodeConstants.NEW_LINE)
                .append(SiddhiCodeConstants.TAB_SPACE)
                .append(CodeGeneratorHelper.getQuerySelect(aggregation.getSelect()))
                .append(SiddhiCodeConstants.NEW_LINE)
                .append(SiddhiCodeConstants.TAB_SPACE)
                .append(CodeGeneratorHelper.getQueryGroupBy(aggregation.getGroupBy()))
                .append(SiddhiCodeConstants.NEW_LINE)
                .append(SiddhiCodeConstants.AGGREGATE);

        // TODO: 5/28/18 Can break this down into smaller methods in the Helper class
        if (aggregation.getAggregateByAttribute() != null && !aggregation.getAggregateByAttribute().isEmpty()) {
            aggregationStringBuilder.append(SiddhiCodeConstants.SPACE)
                    .append(SiddhiCodeConstants.BY)
                    .append(SiddhiCodeConstants.SPACE)
                    .append(aggregation.getAggregateByAttribute());
        }

        aggregationStringBuilder.append(SiddhiCodeConstants.SPACE)
                .append(SiddhiCodeConstants.EVERY)
                .append(SiddhiCodeConstants.SPACE)
                .append(aggregation.getAggregateByTimePeriod().getMinValue());

        if (aggregation.getAggregateByTimePeriod().getMaxValue() != null && !aggregation.getAggregateByTimePeriod().getMaxValue().isEmpty()) {
            aggregationStringBuilder.append(SiddhiCodeConstants.THRIPPLE_DOTS)
                    .append(aggregation.getAggregateByTimePeriod().getMaxValue());
        }

        aggregationStringBuilder.append(SiddhiCodeConstants.SEMI_COLON);

        return aggregationStringBuilder.toString();
    }

    private String generateFunctionString(FunctionConfig function) {
        if (function == null) {
            throw new CodeGenerationException("The given FunctionConfig instance is null");
        } else if (function.getName() == null || function.getName().isEmpty()) {
            throw new CodeGenerationException("The given function name is empty");
        } else if (function.getScriptType() == null || function.getScriptType().isEmpty()) {
            throw new CodeGenerationException("The given function script type is empty");
        } else if (function.getReturnType() == null || function.getReturnType().isEmpty()) {
            throw new CodeGenerationException("The given function return type is empty");
        } else if (function.getBody() == null || function.getBody().isEmpty()) {
            throw new CodeGenerationException("The given function body is empty");
        }

        StringBuilder functionStringBuilder = new StringBuilder();
        functionStringBuilder.append(SiddhiCodeConstants.DEFINE_FUNCTION)
                .append(SiddhiCodeConstants.SPACE)
                .append(function.getName())
                .append(SiddhiCodeConstants.OPEN_SQUARE_BRACKET)
                .append(function.getScriptType())
                .append(SiddhiCodeConstants.CLOSE_SQUARE_BRACKET)
                .append(SiddhiCodeConstants.SPACE)
                .append(SiddhiCodeConstants.RETURN)
                .append(SiddhiCodeConstants.SPACE)
                .append(function.getReturnType())
                .append(SiddhiCodeConstants.SPACE)
                .append(SiddhiCodeConstants.OPEN_CURLY_BRACKET)
                .append(SiddhiCodeConstants.NEW_LINE)
                .append(SiddhiCodeConstants.TAB_SPACE)
                .append(function.getBody().trim())
                .append(SiddhiCodeConstants.NEW_LINE)
                .append(SiddhiCodeConstants.CLOSE_CURLY_BRACKET)
                .append(SiddhiCodeConstants.SEMI_COLON);

        return functionStringBuilder.toString();
    }

    /**
     * Converts a QueryConfig object to a Siddhi query definition string
     *
     * @param query The QueryConfig object to be converted
     * @return The query definition string representation of the given QueryConfig object
     */
    private String generateQueryString(QueryConfig query) {
        if (query == null) {
            throw new CodeGenerationException("The Given QueryConfig Object Is Null");
        }

        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append(CodeGeneratorHelper.getAnnotations(query.getAnnotationList()))
                .append(CodeGeneratorHelper.getQueryInput(query.getQueryInput()))
                .append(SiddhiCodeConstants.NEW_LINE)
                .append(CodeGeneratorHelper.getQuerySelect(query.getSelect()));

        // TODO: 5/28/18 Can add this to submethods in the Helper as well 
        if (query.getGroupBy() != null && !query.getGroupBy().isEmpty()) {
            queryStringBuilder.append(SiddhiCodeConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryGroupBy(query.getGroupBy()));
        }
        if (query.getOrderBy() != null && !query.getOrderBy().isEmpty()) {
            queryStringBuilder.append(SiddhiCodeConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryOrderBy(query.getOrderBy()));
        }
        if (query.getLimit() != 0) {
            queryStringBuilder.append(SiddhiCodeConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryLimit(query.getLimit()));
        }
        if (query.getHaving() != null && !query.getHaving().isEmpty()) {
            queryStringBuilder.append(SiddhiCodeConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryHaving(query.getHaving()));
        }
        if (query.getOutputRateLimit() != null && !query.getOutputRateLimit().isEmpty()) {
            queryStringBuilder.append(SiddhiCodeConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryOutputRateLimit(query.getOutputRateLimit()));
        }

        queryStringBuilder.append(SiddhiCodeConstants.NEW_LINE)
                .append(CodeGeneratorHelper.getQueryOutput(query.getQueryOutput()));

        return queryStringBuilder.toString();
    }

    /**
     * Converts a PartitionConfig object to a Siddhi partition definition string
     *
     * @param partition The PartitionConfig object to be converted
     * @return The partition definition string representation of the given PartitionConfig object
     */
    private String generatePartitionString(PartitionConfig partition) {
        if (partition == null) {
            throw new CodeGenerationException("The given PartitionConfig instance is null");
        } else if (partition.getPartitionWith() == null || partition.getPartitionWith().isEmpty()) {
            throw new CodeGenerationException("The 'partitionWith' value for the given PartitionConfig");
        }

        StringBuilder partitionStringBuilder = new StringBuilder();

        partitionStringBuilder.append(SiddhiCodeConstants.PARTITION_WITH)
                .append(SiddhiCodeConstants.OPEN_BRACKET)
                .append(partition.getPartitionWith())
                .append(SiddhiCodeConstants.CLOSE_BRACKET)
                .append(SiddhiCodeConstants.NEW_LINE)
                .append(SiddhiCodeConstants.BEGIN)
                .append(SiddhiCodeConstants.NEW_LINE)
                .append(generateQueries(partition.getQueryLists()))
                .append(SiddhiCodeConstants.END)
                .append(SiddhiCodeConstants.SEMI_COLON)
                .append(SiddhiCodeConstants.NEW_LINE);

        return partitionStringBuilder.toString();
    }

    private String generateSourceSinkString(SourceSinkConfig sourceSink) {
        if (sourceSink == null) {
            throw new CodeGenerationException("The given SourceSinkConfig instance is null");
        } else if (sourceSink.getAnnotationType() == null || sourceSink.getAnnotationType().isEmpty()) {
            throw new CodeGenerationException("The annotation type for the given SourceSinkConfig is empty");
        } else if (sourceSink.getType() == null || sourceSink.getType().isEmpty()) {
            throw new CodeGenerationException("The type of source/sink for the given SourceSinkConfig is empty");
        }

        StringBuilder sourceSinkStringBuilder = new StringBuilder();
        if (sourceSink.getAnnotationType().equalsIgnoreCase("SOURCE")) {
            sourceSinkStringBuilder.append(SiddhiCodeConstants.SOURCE);
        } else if (sourceSink.getAnnotationType().equalsIgnoreCase("SINK")) {
            sourceSinkStringBuilder.append(SiddhiCodeConstants.SINK);
        } else {
            throw new CodeGenerationException("Unknown type: " + sourceSink.getType() +
                    ". The SinkSourceConfig can only have type 'SINK' or type 'SOURCE'");
        }

        sourceSinkStringBuilder.append(sourceSink.getType())
                .append(SiddhiCodeConstants.SINGLE_QUOTE);
        if (sourceSink.getOptions() != null && !sourceSink.getOptions().isEmpty()) {
            sourceSinkStringBuilder.append(SiddhiCodeConstants.COMMA)
                    .append(SiddhiCodeConstants.SPACE)
                    .append(CodeGeneratorHelper.getParameterList(sourceSink.getOptions()));
        }

        if (sourceSink.getMap() != null) {
            sourceSinkStringBuilder.append(SiddhiCodeConstants.COMMA)
                    .append(SiddhiCodeConstants.SPACE)
                    .append(CodeGeneratorHelper.getMapper(sourceSink.getMap(), sourceSink.getAnnotationType()));
        }

        sourceSinkStringBuilder.append(SiddhiCodeConstants.CLOSE_BRACKET);

        return sourceSinkStringBuilder.toString();
    }

}
