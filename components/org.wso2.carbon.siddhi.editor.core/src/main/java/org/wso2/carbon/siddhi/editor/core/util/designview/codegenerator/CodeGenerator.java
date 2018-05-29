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
        // TODO: 4/20/18 complete Main Public Method
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
                .append(generateQueries(siddhiApp.getQueryLists()));

        // TODO: 4/23/18 Add the partitions loop

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
            appNameAndDescriptionStringBuilder.append(CodeGeneratorConstants.APP_NAME)
                    .append(appName)
                    .append(CodeGeneratorConstants.SINGLE_QUOTE)
                    .append(CodeGeneratorConstants.CLOSE_BRACKET)
                    .append(CodeGeneratorConstants.NEW_LINE);
        } else {
            appNameAndDescriptionStringBuilder.append(CodeGeneratorConstants.DEFAULT_APP_NAME)
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        if (appDescription != null && !appDescription.isEmpty()) {
            appNameAndDescriptionStringBuilder.append(CodeGeneratorConstants.NEW_LINE)
                    .append(CodeGeneratorConstants.APP_DESCRIPTION)
                    .append(appDescription)
                    .append(CodeGeneratorConstants.SINGLE_QUOTE)
                    .append(CodeGeneratorConstants.CLOSE_BRACKET)
                    .append(CodeGeneratorConstants.NEW_LINE);
        } else {
            appNameAndDescriptionStringBuilder.append(CodeGeneratorConstants.DEFAULT_APP_DESCRIPTION)
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        appNameAndDescriptionStringBuilder.append(CodeGeneratorConstants.NEW_LINE);

        return appNameAndDescriptionStringBuilder.toString();
    }

    private String generateStreams(List<StreamConfig> streamList, List<SourceSinkConfig> sourceList,
                                   List<SourceSinkConfig> sinkList) {
        // TODO source and sink should be somehow connected to a stream over here
        if (streamList == null || streamList.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder streamListStringBuilder = new StringBuilder();
        streamListStringBuilder.append("-- Streams")
                .append(CodeGeneratorConstants.NEW_LINE);

        for (StreamConfig stream : streamList) {
            // TODO: 5/29/18 Check for inner streams
            if (stream.isInnerStream()) {
                break;
            }

            for (SourceSinkConfig source : sourceList) {
                // TODO: 5/29/18 This might have to be .equalsIgnoreCase() Check whether it is possible
                if (stream.getName().equals(source.getConnectedElementName())) {
                    streamListStringBuilder.append(generateSourceSinkString(source))
                            .append(CodeGeneratorConstants.NEW_LINE);
                }
            }

            for (SourceSinkConfig sink : sinkList) {
                if (stream.getName().equals(sink.getConnectedElementName())) {
                    streamListStringBuilder.append(generateSourceSinkString(sink))
                            .append(CodeGeneratorConstants.NEW_LINE);
                }
            }

            streamListStringBuilder.append(generateStreamString(stream))
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        streamListStringBuilder.append(CodeGeneratorConstants.NEW_LINE);

        return streamListStringBuilder.toString();
    }

    private String generateTables(List<TableConfig> tableList) {
        if (tableList == null || tableList.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder tableListStringBuilder = new StringBuilder();
        tableListStringBuilder.append("-- Tables")
                .append(CodeGeneratorConstants.NEW_LINE);

        for (TableConfig table : tableList) {
            tableListStringBuilder.append(generateTableString(table))
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        tableListStringBuilder.append(CodeGeneratorConstants.NEW_LINE);

        return tableListStringBuilder.toString();
    }

    private String generateWindows(List<WindowConfig> windowList) {
        if (windowList == null || windowList.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder windowListStringBuilder = new StringBuilder();
        windowListStringBuilder.append("-- Windows")
                .append(CodeGeneratorConstants.NEW_LINE);

        for (WindowConfig window : windowList) {
            windowListStringBuilder.append(generateWindowString(window))
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        windowListStringBuilder.append(CodeGeneratorConstants.NEW_LINE);

        return windowListStringBuilder.toString();
    }

    private String generateTriggers(List<TriggerConfig> triggerList, List<SourceSinkConfig> sourceList,
                                    List<SourceSinkConfig> sinkList) {
        // TODO: 5/28/18 NOTE - Triggers can have sources and sinks as well
        if (triggerList == null || triggerList.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder triggerListStringBuilder = new StringBuilder();
        triggerListStringBuilder.append("-- Triggers")
                .append(CodeGeneratorConstants.NEW_LINE);

        for (TriggerConfig trigger : triggerList) {

            for (SourceSinkConfig source : sourceList) {
                if (trigger.getName().equals(source.getConnectedElementName())) {
                    triggerListStringBuilder.append(generateSourceSinkString(source))
                            .append(CodeGeneratorConstants.NEW_LINE);
                }
            }

            for (SourceSinkConfig sink: sinkList) {
                if (trigger.getName().equals(sink.getConnectedElementName())) {
                    triggerListStringBuilder.append(generateSourceSinkString(sink))
                            .append(CodeGeneratorConstants.NEW_LINE);
                }
            }

            triggerListStringBuilder.append(generateTriggerString(trigger))
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        triggerListStringBuilder.append(CodeGeneratorConstants.NEW_LINE);

        return triggerListStringBuilder.toString();
    }

    private String generateAggregations(List<AggregationConfig> aggregationList) {
        if (aggregationList == null || aggregationList.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder aggregationListStringBuilder = new StringBuilder();
        aggregationListStringBuilder.append("-- Aggregations")
                .append(CodeGeneratorConstants.NEW_LINE);

        for (AggregationConfig aggregation : aggregationList) {
            aggregationListStringBuilder.append(generateAggregationString(aggregation))
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        aggregationListStringBuilder.append(CodeGeneratorConstants.NEW_LINE);

        return aggregationListStringBuilder.toString();
    }

    private String generateFunctions(List<FunctionConfig> functionList) {
        if (functionList == null || functionList.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder functionListStringBuilder = new StringBuilder();
        functionListStringBuilder.append("-- Functions")
                .append(CodeGeneratorConstants.NEW_LINE);

        for (FunctionConfig function : functionList) {
            functionListStringBuilder.append(generateFunctionString(function))
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        functionListStringBuilder.append(CodeGeneratorConstants.NEW_LINE);

        return functionListStringBuilder.toString();
    }

    private String generateQueries(Map<String, List<QueryConfig>> queryLists) {
        if (queryLists == null || queryLists.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder queryListStringBuilder = new StringBuilder();
        queryListStringBuilder.append("-- Queries")
                .append(CodeGeneratorConstants.NEW_LINE);

        for (List<QueryConfig> queryList : queryLists.values()) {
            for (QueryConfig query : queryList) {
                queryListStringBuilder.append(generateQueryString(query))
                        .append(CodeGeneratorConstants.NEW_LINE);
            }
        }

        queryListStringBuilder.append(CodeGeneratorConstants.NEW_LINE);

        return queryListStringBuilder.toString();
    }

    private String generatePartitions(List<PartitionConfig> partitionList) {
        if (partitionList == null || partitionList.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder partitionListStringBuilder = new StringBuilder();
        partitionListStringBuilder.append("-- Partitions");
        for (PartitionConfig partition : partitionList) {
            partitionListStringBuilder.append(generatePartitionString(partition))
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        partitionListStringBuilder.append(CodeGeneratorConstants.NEW_LINE);

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
                .append(CodeGeneratorConstants.DEFINE_STREAM)
                .append(CodeGeneratorConstants.SPACE)
                .append(stream.getName())
                .append(CodeGeneratorConstants.SPACE)
                .append(CodeGeneratorConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getAttributes(stream.getAttributeList()))
                .append(CodeGeneratorConstants.CLOSE_BRACKET)
                .append(CodeGeneratorConstants.SEMI_COLON);

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
                .append(CodeGeneratorConstants.DEFINE_TABLE)
                .append(CodeGeneratorConstants.SPACE)
                .append(table.getName())
                .append(CodeGeneratorConstants.SPACE)
                .append(CodeGeneratorConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getAttributes(table.getAttributeList()))
                .append(CodeGeneratorConstants.CLOSE_BRACKET)
                .append(CodeGeneratorConstants.SEMI_COLON);

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
                .append(CodeGeneratorConstants.DEFINE_WINDOW)
                .append(CodeGeneratorConstants.SPACE)
                .append(window.getName())
                .append(CodeGeneratorConstants.SPACE)
                .append(CodeGeneratorConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getAttributes(window.getAttributeList()))
                .append(CodeGeneratorConstants.CLOSE_BRACKET)
                .append(CodeGeneratorConstants.SPACE)
                .append(window.getFunction())
                .append(CodeGeneratorConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getParameterList(window.getParameters()))
                .append(CodeGeneratorConstants.CLOSE_BRACKET);

        if (window.getOutputEventType() != null && !window.getOutputEventType().isEmpty()) {
            windowStringBuilder.append(CodeGeneratorConstants.SPACE);
            switch (window.getOutputEventType().toUpperCase()) {
                // TODO: 4/26/18 The cases must be constants and not free strings
                case "CURRENT_EVENTS":
                    windowStringBuilder.append(CodeGeneratorConstants.OUTPUT_CURRENT_EVENTS);
                    break;
                case "EXPIRED_EVENTS":
                    windowStringBuilder.append(CodeGeneratorConstants.OUTPUT_EXPIRED_EVENTS);
                    break;
                case "ALL_EVENTS":
                    windowStringBuilder.append(CodeGeneratorConstants.OUTPUT_ALL_EVENTS);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified output event type: " + window.getOutputEventType());
            }
        }
        windowStringBuilder.append(CodeGeneratorConstants.SEMI_COLON);

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
                .append(CodeGeneratorConstants.DEFINE_TRIGGER)
                .append(CodeGeneratorConstants.SPACE)
                .append(trigger.getName())
                .append(CodeGeneratorConstants.SPACE)
                .append(CodeGeneratorConstants.AT)
                .append(CodeGeneratorConstants.SPACE)
                .append(trigger.getAt())
                .append(CodeGeneratorConstants.SEMI_COLON);

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
                .append(CodeGeneratorConstants.DEFINE_AGGREGATION)
                .append(CodeGeneratorConstants.SPACE)
                .append(aggregation.getName())
                .append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorConstants.TAB_SPACE)
                .append(CodeGeneratorConstants.FROM)
                .append(CodeGeneratorConstants.SPACE)
                .append(aggregation.getFrom())
                .append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorConstants.TAB_SPACE)
                .append(CodeGeneratorHelper.getQuerySelect(aggregation.getSelect()))
                .append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorConstants.TAB_SPACE)
                .append(CodeGeneratorHelper.getQueryGroupBy(aggregation.getGroupBy()))
                .append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorConstants.AGGREGATE);

        // TODO: 5/28/18 Can break this down into smaller methods in the Helper class
        if (aggregation.getAggregateByAttribute() != null && !aggregation.getAggregateByAttribute().isEmpty()) {
            aggregationStringBuilder.append(CodeGeneratorConstants.SPACE)
                    .append(CodeGeneratorConstants.BY)
                    .append(CodeGeneratorConstants.SPACE)
                    .append(aggregation.getAggregateByAttribute());
        }

        aggregationStringBuilder.append(CodeGeneratorConstants.SPACE)
                .append(CodeGeneratorConstants.EVERY)
                .append(CodeGeneratorConstants.SPACE)
                .append(aggregation.getAggregateByTimePeriod().getMinValue());

        if (aggregation.getAggregateByTimePeriod().getMaxValue() != null && !aggregation.getAggregateByTimePeriod().getMaxValue().isEmpty()) {
            aggregationStringBuilder.append(CodeGeneratorConstants.THRIPPLE_DOTS)
                    .append(aggregation.getAggregateByTimePeriod().getMaxValue());
        }

        aggregationStringBuilder.append(CodeGeneratorConstants.SEMI_COLON);

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
        functionStringBuilder.append(CodeGeneratorConstants.DEFINE_FUNCTION)
                .append(CodeGeneratorConstants.SPACE)
                .append(function.getName())
                .append(CodeGeneratorConstants.OPEN_SQUARE_BRACKET)
                .append(function.getScriptType())
                .append(CodeGeneratorConstants.CLOSE_SQUARE_BRACKET)
                .append(CodeGeneratorConstants.SPACE)
                .append(CodeGeneratorConstants.RETURN)
                .append(CodeGeneratorConstants.SPACE)
                .append(function.getReturnType())
                .append(CodeGeneratorConstants.SPACE)
                .append(CodeGeneratorConstants.OPEN_CURLY_BRACKET)
                .append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorConstants.TAB_SPACE)
                .append(function.getBody().trim())
                .append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorConstants.CLOSE_CURLY_BRACKET)
                .append(CodeGeneratorConstants.SEMI_COLON);

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
                .append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorHelper.getQuerySelect(query.getSelect()));

        // TODO: 5/28/18 Can add this to submethods in the Helper as well 
        if (query.getGroupBy() != null && !query.getGroupBy().isEmpty()) {
            queryStringBuilder.append(CodeGeneratorConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryGroupBy(query.getGroupBy()));
        }
        if (query.getOrderBy() != null && !query.getOrderBy().isEmpty()) {
            queryStringBuilder.append(CodeGeneratorConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryOrderBy(query.getOrderBy()));
        }
        if (query.getLimit() != 0) {
            queryStringBuilder.append(CodeGeneratorConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryLimit(query.getLimit()));
        }
        if (query.getHaving() != null && !query.getHaving().isEmpty()) {
            queryStringBuilder.append(CodeGeneratorConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryHaving(query.getHaving()));
        }
        if (query.getOutputRateLimit() != null && !query.getOutputRateLimit().isEmpty()) {
            queryStringBuilder.append(CodeGeneratorConstants.NEW_LINE)
                    .append(CodeGeneratorHelper.getQueryOutputRateLimit(query.getOutputRateLimit()));
        }

        queryStringBuilder.append(CodeGeneratorConstants.NEW_LINE)
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
        // TODO: 4/19/18 Write the logic here
        return CodeGeneratorConstants.EMPTY_STRING;
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
            sourceSinkStringBuilder.append(CodeGeneratorConstants.SOURCE);
        } else if (sourceSink.getAnnotationType().equalsIgnoreCase("SINK")) {
            sourceSinkStringBuilder.append(CodeGeneratorConstants.SINK);
        } else {
            throw new CodeGenerationException("Unknown type: " + sourceSink.getType() +
                    ". The SinkSourceConfig can only have type 'SINK' or type 'SOURCE'");
        }

        sourceSinkStringBuilder.append(sourceSink.getType())
                .append(CodeGeneratorConstants.SINGLE_QUOTE);
        if (sourceSink.getOptions() != null && !sourceSink.getOptions().isEmpty()) {
            sourceSinkStringBuilder.append(CodeGeneratorConstants.COMMA)
                    .append(CodeGeneratorConstants.SPACE)
                    .append(CodeGeneratorHelper.getParameterList(sourceSink.getOptions()));
        }

        if (sourceSink.getMap() != null) {
            sourceSinkStringBuilder.append(CodeGeneratorConstants.COMMA)
                    .append(CodeGeneratorConstants.SPACE)
                    .append(CodeGeneratorHelper.getMapper(sourceSink.getMap(), sourceSink.getAnnotationType()));
        }

        sourceSinkStringBuilder.append(CodeGeneratorConstants.CLOSE_BRACKET);

        return sourceSinkStringBuilder.toString();
    }

}
