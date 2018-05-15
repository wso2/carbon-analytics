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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.AttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StoreConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TableConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.WindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation.AnnotationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.SelectedAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.UserDefinedSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryOrderByConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryWindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.DeleteOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.InsertOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.UpdateInsertIntoOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.setattribute.SetAttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sink.SinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.source.SourceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.AttributeSelection;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

import java.util.List;
import java.util.Map;

// TODO: 4/20/18 Check Everywhere for null values

/**
 * Used to convert an EventFlow object to a Siddhi app string
 */
public class CodeGenerator {

    // TODO: 5/2/18 Look for constants for all the cases in switch case

    private static final String VOID_RETURN = "";

    /**
     * Converts a EventFlow object to a Siddhi app string
     *
     * @param eventFlow The EventFlow object to be converted
     * @return The Siddhi app string representation of the given EventFlow object
     */
    public String getSiddhiAppCode(EventFlow eventFlow) {
        // TODO: 4/20/18 complete Main Public Method
        SiddhiAppConfig siddhiApp = eventFlow.getSiddhiAppConfig();
        StringBuilder siddhiAppStringBuilder = new StringBuilder();

        siddhiAppStringBuilder.append(generateAppNameAndDescription(siddhiApp.getAppName(), siddhiApp.getAppDescription()))
                .append(Constants.NEW_LINE)
                .append(Constants.NEW_LINE)
                .append("-- Streams")
                .append(Constants.NEW_LINE);
        for (StreamConfig stream : siddhiApp.getStreamList()) {
            siddhiAppStringBuilder.append(generateStreamString(stream))
                    .append(Constants.NEW_LINE);
        }
        siddhiAppStringBuilder.append(Constants.NEW_LINE);

        siddhiAppStringBuilder.append("-- Tables").append(Constants.NEW_LINE);
        for (TableConfig table : siddhiApp.getTableList()) {
            siddhiAppStringBuilder.append(generateTableString(table))
                    .append(Constants.NEW_LINE);
        }
        siddhiAppStringBuilder.append(Constants.NEW_LINE);

        siddhiAppStringBuilder.append("-- Windows").append(Constants.NEW_LINE);
        for (WindowConfig window : siddhiApp.getWindowList()) {
            siddhiAppStringBuilder.append(generateWindowString(window))
                    .append(Constants.NEW_LINE);
        }
        siddhiAppStringBuilder.append(Constants.NEW_LINE);

        siddhiAppStringBuilder.append("-- Triggers").append(Constants.NEW_LINE);
        for (TriggerConfig trigger : siddhiApp.getTriggerList()) {
            siddhiAppStringBuilder.append(generateTriggerString(trigger))
                    .append(Constants.NEW_LINE);
        }
        siddhiAppStringBuilder.append(Constants.NEW_LINE);

        siddhiAppStringBuilder.append("-- Aggregations").append(Constants.NEW_LINE);
        for (AggregationConfig aggregation : siddhiApp.getAggregationList()) {
            siddhiAppStringBuilder.append(generateAggregationString(aggregation))
                    .append(Constants.NEW_LINE);
        }
        siddhiAppStringBuilder.append(Constants.NEW_LINE);

        // TODO source and sink should be somehow connected to a stream over here
//        for (SourceConfig source : siddhiApp.getSourceList()) {
//            siddhiAppStringBuilder.append(generateSourceString(source));
//        }
//
//        for (SinkConfig sink : siddhiApp.getSinkList()) {
//            siddhiAppStringBuilder.append(generateSinkString(sink));
//        }

        siddhiAppStringBuilder.append("-- Queries").append(Constants.NEW_LINE);
        for (QueryConfig query : siddhiApp.getWindowFilterProjectionQueryList()) {
            siddhiAppStringBuilder.append(generateQueryString(query));
        }
        for (QueryConfig query : siddhiApp.getJoinQueryList()) {
            siddhiAppStringBuilder.append(generateQueryString(query));
        }
        siddhiAppStringBuilder.append(Constants.NEW_LINE);
        siddhiAppStringBuilder.append(Constants.NEW_LINE);

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
            appNameAndDescriptionStringBuilder.append(Constants.APP_NAME)
                    .append(appName)
                    .append(Constants.SINGLE_QUOTE)
                    .append(Constants.CLOSE_BRACKET);
        }
        if (appDescription != null && !appDescription.isEmpty()) {
            appNameAndDescriptionStringBuilder.append(Constants.NEW_LINE)
                    .append(Constants.APP_DESCRIPTION)
                    .append(appDescription)
                    .append(Constants.SINGLE_QUOTE)
                    .append(Constants.CLOSE_BRACKET);
        }

        return appNameAndDescriptionStringBuilder.toString();
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
        streamStringBuilder.append(getAnnotations(stream.getAnnotationList()))
                .append(Constants.DEFINE_STREAM)
                .append(Constants.SPACE)
                .append(stream.getName())
                .append(Constants.SPACE)
                .append(Constants.OPEN_BRACKET)
                .append(getAttributes(stream.getAttributeList()))
                .append(Constants.CLOSE_BRACKET)
                .append(Constants.SEMI_COLON);

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
        tableStringBuilder.append(getStore(table.getStore()))
                .append(getAnnotations(table.getAnnotationList()))
                .append(Constants.DEFINE_TABLE)
                .append(Constants.SPACE)
                .append(table.getName())
                .append(Constants.SPACE)
                .append(Constants.OPEN_BRACKET)
                .append(getAttributes(table.getAttributeList()))
                .append(Constants.CLOSE_BRACKET)
                .append(Constants.SEMI_COLON);

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
        windowStringBuilder.append(getAnnotations(window.getAnnotationList()))
                .append(Constants.DEFINE_WINDOW)
                .append(Constants.SPACE)
                .append(window.getName())
                .append(Constants.SPACE)
                .append(Constants.OPEN_BRACKET)
                .append(getAttributes(window.getAttributeList()))
                .append(Constants.CLOSE_BRACKET)
                .append(Constants.SPACE)
                .append(window.getFunction())
                .append(Constants.OPEN_BRACKET)
                .append(getParameterList(window.getParameters()))
                .append(Constants.CLOSE_BRACKET);

        if (window.getOutputEventType() != null && !window.getOutputEventType().isEmpty()) {
            windowStringBuilder.append(Constants.SPACE);
            switch (window.getOutputEventType().toUpperCase()) {
                // TODO: 4/26/18 The cases must be constants and not free strings
                case "CURRENT_EVENTS":
                    windowStringBuilder.append(Constants.OUTPUT_CURRENT_EVENTS);
                    break;
                case "EXPIRED_EVENTS":
                    windowStringBuilder.append(Constants.OUTPUT_EXPIRED_EVENTS);
                    break;
                case "ALL_EVENTS":
                    windowStringBuilder.append(Constants.OUTPUT_ALL_EVENTS);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified output event type: " + window.getOutputEventType());
            }
        }
        windowStringBuilder.append(Constants.SEMI_COLON);

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
        triggerStringBuilder.append(getAnnotations(trigger.getAnnotationList()))
                .append(Constants.DEFINE_TRIGGER)
                .append(Constants.SPACE)
                .append(trigger.getName())
                .append(Constants.SPACE)
                .append(Constants.AT)
                .append(Constants.SPACE)
                .append(trigger.getAt())
                .append(Constants.SEMI_COLON);

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
        aggregationStringBuilder.append(getStore(aggregation.getStore()))
                .append(getAnnotations(aggregation.getAnnotationList()))
                .append(Constants.DEFINE_AGGREGATION)
                .append(Constants.SPACE)
                .append(aggregation.getName())
                .append(Constants.NEW_LINE)
                .append(Constants.TAB_SPACE)
                .append(Constants.FROM)
                .append(Constants.SPACE)
                .append(aggregation.getFrom())
                .append(Constants.NEW_LINE)
                .append(Constants.TAB_SPACE)
                .append(getQuerySelect(aggregation.getSelect()))
                .append(Constants.NEW_LINE)
                .append(Constants.TAB_SPACE)
                .append(getQueryGroupBy(aggregation.getGroupBy()))
                .append(Constants.NEW_LINE)
                .append(Constants.AGGREGATE);

        if (aggregation.getAggregateByAttribute() != null && !aggregation.getAggregateByAttribute().isEmpty()) {
            aggregationStringBuilder.append(Constants.SPACE)
                    .append(Constants.BY)
                    .append(Constants.SPACE)
                    .append(aggregation.getAggregateByAttribute());
        }

        aggregationStringBuilder.append(Constants.SPACE)
                .append(Constants.EVERY)
                .append(Constants.SPACE)
                .append(aggregation.getAggregateByTimePeriod().getMinValue());

        if (aggregation.getAggregateByTimePeriod().getMaxValue() != null && !aggregation.getAggregateByTimePeriod().getMaxValue().isEmpty()) {
            aggregationStringBuilder.append(Constants.THRIPPLE_DOTS)
                    .append(aggregation.getAggregateByTimePeriod().getMaxValue());
        }

        aggregationStringBuilder.append(Constants.SEMI_COLON);

        return aggregationStringBuilder.toString();
    }

    /**
     * Converts a SourceConfig object to a Siddhi source annotation string
     *
     * @param source The SourceConfig object to be converted
     * @return The source annotation string representation of the given SourceConfig object
     */
    private String generateSourceString(SourceConfig source) {
        // TODO: 4/19/18 Write the logic here
        StringBuilder sourceStringBuilder = new StringBuilder();
        return sourceStringBuilder.toString();
    }

    /**
     * Converts a SinkConfig object to a Siddhi source annotation string
     *
     * @param sink The SinkConfig object to be converted
     * @return The sink annotation string representation of the given SinkConfig object
     */
    private String generateSinkString(SinkConfig sink) {
        // TODO: 4/19/18 Write the logic here
        StringBuilder sinkStringBuilder = new StringBuilder();
        return sinkStringBuilder.toString();
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
        queryStringBuilder.append(getAnnotations(query.getAnnotationList()))
                .append(getQueryInput(query.getQueryInput()))
                .append(Constants.NEW_LINE)
                .append(getQuerySelect(query.getSelect()));

        if (query.getGroupBy() != null && !query.getGroupBy().isEmpty()) {
            queryStringBuilder.append(Constants.NEW_LINE)
                    .append(getQueryGroupBy(query.getGroupBy()));
        }
        if (query.getOrderBy() != null && !query.getOrderBy().isEmpty()) {
            queryStringBuilder.append(Constants.NEW_LINE)
                    .append(getQueryOrderBy(query.getOrderBy()));
        }
        if (query.getLimit() != 0) {
            queryStringBuilder.append(Constants.NEW_LINE)
                    .append(getQueryLimit(query.getLimit()));
        }
        if (query.getHaving() != null && !query.getHaving().isEmpty()) {
            queryStringBuilder.append(Constants.NEW_LINE)
                    .append(getQueryHaving(query.getHaving()));
        }
        if (query.getOutputRateLimit() != null && !query.getOutputRateLimit().isEmpty()) {
            queryStringBuilder.append(Constants.NEW_LINE)
                    .append(getQueryOutputRateLimit(query.getOutputRateLimit()));
        }

        queryStringBuilder.append(Constants.NEW_LINE)
                .append(getQueryOutput(query.getQueryOutput()));

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
        StringBuilder partitionStringBuilder = new StringBuilder();
        return partitionStringBuilder.toString();
    }

    /**
     * Converts a QueryInputConfig object to the input part of a Siddhi query
     *
     * @param queryInput The QueryInputConfig instance to be converted
     * @return The query input string representation of the given QueryInputConfig object
     */
    private String getQueryInput(QueryInputConfig queryInput) {
        if (queryInput == null) {
            throw new CodeGenerationException("Query Input Cannot Be Null");
        } else if (queryInput.getType() == null || queryInput.getType().isEmpty()) {
            throw new CodeGenerationException("The query input type is null");
        }

        StringBuilder queryInputStringBuilder = new StringBuilder();

        switch (queryInput.getType().toUpperCase()) {
            case "WINDOW":
            case "FILTER":
            case "PROJECTION":
                WindowFilterProjectionConfig windowFilterProjectionQuery = (WindowFilterProjectionConfig) queryInput;
                queryInputStringBuilder.append(getWindowFilterProjectionQueryInput(windowFilterProjectionQuery));
                break;
            case "JOIN":
                JoinConfig joinQuery = (JoinConfig) queryInput;
                queryInputStringBuilder.append(getJoinQueryInput(joinQuery));
                break;
            case "PATTERN":
            case "SEQUENCE":
                PatternSequenceConfig patternSequence = (PatternSequenceConfig) queryInput;
                queryInputStringBuilder.append(getPatternSequenceInput(patternSequence));
                break;
            default:
                throw new CodeGenerationException("Unidentified Query Input Type Has Been Given: " + queryInput.getType());
        }

        return queryInputStringBuilder.toString();
    }

    /**
     * Converts a WindowFilterProjectionConfig object to a query input of type window, filter or projection string
     *
     * @param windowFilterProjection The WindowFilterProjection object to be converted
     * @return The window,filter or projection string representation of the given WindowFilterProjection object
     */
    private String getWindowFilterProjectionQueryInput(WindowFilterProjectionConfig windowFilterProjection) {
        if (windowFilterProjection == null) {
            throw new CodeGenerationException("The WindowFilterProjection Instance Is Null");
        } else if (windowFilterProjection.getFrom() == null || windowFilterProjection.getFrom().isEmpty()) {
            throw new CodeGenerationException("From Value Is Null");
        }

        StringBuilder windowFilterProjectionStringBuilder = new StringBuilder();
        windowFilterProjectionStringBuilder.append(Constants.FROM)
                .append(Constants.SPACE)
                .append(windowFilterProjection.getFrom());

        if (windowFilterProjection.getFilter() != null && !windowFilterProjection.getFilter().isEmpty()) {
            windowFilterProjectionStringBuilder.append(Constants.OPEN_SQUARE_BRACKET)
                    .append(windowFilterProjection.getFilter())
                    .append(Constants.CLOSE_SQUARE_BRACKET);
        }

        if (windowFilterProjection.getWindow() != null) {
            QueryWindowConfig queryWindow = windowFilterProjection.getWindow();
            windowFilterProjectionStringBuilder.append(Constants.HASH)
                    .append(Constants.WINDOW)
                    .append(Constants.FULL_STOP)
                    .append(queryWindow.getFunction())
                    .append(Constants.OPEN_BRACKET)
                    .append(getParameterList(queryWindow.getParameters()))
                    .append(Constants.CLOSE_BRACKET);
        }

        if (windowFilterProjection.getPostWindowFilter() != null && !windowFilterProjection.getPostWindowFilter().isEmpty()) {
            windowFilterProjectionStringBuilder.append(Constants.OPEN_SQUARE_BRACKET)
                    .append(windowFilterProjection.getPostWindowFilter())
                    .append(Constants.CLOSE_SQUARE_BRACKET);
        }

        return windowFilterProjectionStringBuilder.toString();
    }

    /**
     * Converts a JoinConfig object to a query input of type join as a string
     *
     * @param join The JoinConfig object to be converted
     * @return The join input string representation of the given JoinConfig object
     */
    private String getJoinQueryInput(JoinConfig join) {
        if (join == null) {
            throw new CodeGenerationException("The given JoinConfig instance is null");
        } else if (join.getJoinWith() == null || join.getJoinType().isEmpty()) {
            throw new CodeGenerationException("The join with attribute given is null");
        } else if (join.getJoinType() == null || join.getJoinType().isEmpty()) {
            throw new CodeGenerationException("The join type given is null");
        } else if (join.getOn() == null || join.getOn().isEmpty()) {
            throw new CodeGenerationException("The 'on' attribute in the join query is null");
        } else if (join.getLeft() == null || join.getRight() == null) {
            throw new CodeGenerationException("The join element(s) given is null");
        } else if (join.getLeft().getType() == null || join.getLeft().getType().isEmpty()) {
            throw new CodeGenerationException("The left join element does not have a type");
        } else if (join.getRight().getType() == null || join.getRight().getType().isEmpty()) {
            throw new CodeGenerationException("The right join element does not have a type");
        } else if (!(join.getLeft().getType().equalsIgnoreCase("STREAM") ||
                join.getRight().getType().equalsIgnoreCase("STREAM") ||
                join.getLeft().getType().equalsIgnoreCase("TRIGGER") ||
                join.getRight().getType().equalsIgnoreCase("TRIGGER"))) {
            throw new CodeGenerationException("Atleast one of the join elements must be of type 'stream' or 'trigger'");
        }

        StringBuilder joinStringBuilder = new StringBuilder();
        joinStringBuilder.append(Constants.FROM)
                .append(Constants.SPACE)
                .append(getJoinElement(join.getLeft()))
                .append(Constants.SPACE)
                .append(getJoinType(join.getJoinType()))
                .append(Constants.SPACE)
                .append(getJoinElement(join.getRight()))
                .append(Constants.NEW_LINE)
                .append(Constants.TAB_SPACE)
                .append(Constants.ON)
                .append(Constants.SPACE)
                .append(join.getOn());

        if (join.getJoinWith().equalsIgnoreCase("AGGREGATION")) {
            if (join.getWithin() == null || join.getWithin().isEmpty()) {
                throw new CodeGenerationException("The 'within' attribute for the given join aggregation query is null");
            } else if (join.getPer() == null || join.getPer().isEmpty()) {
                throw new CodeGenerationException("The 'per' attribute for the given join aggregation query is null");
            }

            joinStringBuilder.append(Constants.NEW_LINE)
                    .append(Constants.TAB_SPACE)
                    .append(Constants.WITHIN)
                    .append(Constants.SPACE)
                    .append(join.getWithin())
                    .append(Constants.NEW_LINE)
                    .append(Constants.TAB_SPACE)
                    .append(Constants.PER)
                    .append(Constants.SPACE)
                    .append(join.getPer());
        }

        return joinStringBuilder.toString();
    }

    /**
     * Converts a JoinElementConfig object to a Siddhi string representation of that object
     *
     * @param joinElement The JoinElementConfig to be converted
     * @return The Siddhi string representation of the given JoinElementConfig object
     */
    private String getJoinElement(JoinElementConfig joinElement) {
        if (joinElement == null) {
            throw new CodeGenerationException("The given JoinElementConfig instance given is null");
        } else if (joinElement.getFrom() == null || joinElement.getFrom().isEmpty()) {
            throw new CodeGenerationException("The 'from' value in the given JoinElementConfig instance is null");
        }

        StringBuilder joinElementStringBuilder = new StringBuilder();

        joinElementStringBuilder.append(joinElement.getFrom());

        if (joinElement.getFilter() != null && !joinElement.getFilter().isEmpty()) {
            if (joinElement.getType().equalsIgnoreCase("WINDOW")) {
                joinElementStringBuilder.append(Constants.OPEN_SQUARE_BRACKET)
                        .append(joinElement.getFilter())
                        .append(Constants.CLOSE_SQUARE_BRACKET);
            } else if (!joinElement.getWindow().isEmpty()) {
                if (joinElement.getWindow().getFunction() == null || joinElement.getWindow().getFunction().isEmpty()) {
                    throw new CodeGenerationException("The 'function' value of the given window in the join query is null");
                } else if (joinElement.getWindow().getParameters() == null) {
                    throw new CodeGenerationException("The parameter list for the given window is null");
                }

                joinElementStringBuilder.append(Constants.OPEN_SQUARE_BRACKET)
                        .append(joinElement.getFilter())
                        .append(Constants.CLOSE_SQUARE_BRACKET)
                        .append(Constants.HASH)
                        .append(Constants.WINDOW)
                        .append(Constants.FULL_STOP)
                        .append(joinElement.getWindow().getFunction())
                        .append(Constants.OPEN_BRACKET)
                        .append(getParameterList(joinElement.getWindow().getParameters()))
                        .append(Constants.CLOSE_BRACKET);
            } else {
                throw new CodeGenerationException("The given " + joinElement.getType() + " cannot have a filter without a window");
            }
        } else if (!joinElement.getWindow().isEmpty()) {
            if (joinElement.getWindow().getFunction() == null || joinElement.getWindow().getFunction().isEmpty()) {
                throw new CodeGenerationException("The 'function' of the given window in the join query is null");
            } else if (joinElement.getWindow().getParameters() == null) {
                throw new CodeGenerationException("The parameter list for the given window is null");
            }

            joinElementStringBuilder.append(Constants.HASH)
                    .append(Constants.WINDOW)
                    .append(Constants.FULL_STOP)
                    .append(joinElement.getWindow().getFunction())
                    .append(Constants.OPEN_BRACKET)
                    .append(getParameterList(joinElement.getWindow().getParameters()))
                    .append(Constants.CLOSE_BRACKET);
        }

        if (joinElement.getAs() != null && !joinElement.getAs().isEmpty()) {
            joinElementStringBuilder.append(Constants.SPACE)
                    .append(Constants.AS)
                    .append(Constants.SPACE)
                    .append(joinElement.getAs());
        }

        if (joinElement.isUnidirectional()) {
            joinElementStringBuilder.append(Constants.SPACE)
                    .append(Constants.UNIDIRECTIONAL);
        }

        return joinElementStringBuilder.toString();
    }

    /**
     * Identifies the join type string given and returns the respective Siddhi code snippet for the
     * given join type
     *
     * @param joinType The identifier for which join type the query has
     * @return The Siddhi representation of the given join type
     */
    private String getJoinType(String joinType) {
        if (joinType == null || joinType.isEmpty()) {
            throw new CodeGenerationException("The join type value given is null");
        }

        switch (joinType.toUpperCase()) {
            case "JOIN":
                return Constants.JOIN;
            case "LEFT_OUTER":
                return Constants.LEFT_OUTER_JOIN;
            case "RIGHT_OUTER":
                return Constants.RIGHT_OUTER_JOIN;
            case "FULL_OUTER":
                return Constants.FULL_OUTER_JOIN;
            default:
                throw new CodeGenerationException("Invalid Join Type: " + joinType);
        }
    }

    /**
     * Converts a PatternSequenceConfig object to a Siddhi string representation of a pattern/sequence query input
     *
     * @param patternSequence The PatternSequenceConfig object to be converted
     * @return The Siddhi string representation of the given PatternSequenceConfig instance
     */
    private String getPatternSequenceInput(PatternSequenceConfig patternSequence) {
        return VOID_RETURN;
    }

    /**
     * Converts a AttributesSelectionConfig object to the select section of a Siddhi query as a string
     *
     * @param attributesSelection The AttributeSelectionConfig object to be converted
     * @return The string representation of the select part of a Siddhi query
     */
    private String getQuerySelect(AttributesSelectionConfig attributesSelection) {
        if (attributesSelection == null) {
            throw new CodeGenerationException(" Attribute Selection Instance Cannot Be Null");
        }

        StringBuilder attributesSelectionStringBuilder = new StringBuilder();

        attributesSelectionStringBuilder.append(Constants.SELECT)
                .append(Constants.SPACE);

        if (attributesSelection.getType() == null || attributesSelection.getType().isEmpty()) {
            throw new CodeGenerationException("The Type Of Attribute Selection Cannot Be Null");
        }

        switch (attributesSelection.getType().toUpperCase()) {
            case AttributeSelection.TYPE_USER_DEFINED:
                UserDefinedSelectionConfig userDefinedSelection = (UserDefinedSelectionConfig) attributesSelection;
                attributesSelectionStringBuilder.append(getUserDefinedSelection(userDefinedSelection));
                break;
            case AttributeSelection.TYPE_ALL:
                attributesSelectionStringBuilder.append(Constants.ALL);
                break;
            default:
                throw new CodeGenerationException("Undefined Attribute Selection Type");
        }

        return attributesSelectionStringBuilder.toString();
    }

    /**
     * Converts a UserDefinedSelectionConfig object to the select part of a Siddhi query
     *
     * @param userDefinedSelection The UserDefinedSelectionConfig instance to be converted
     * @return The string representation of the select part of a Siddhi query
     */
    private String getUserDefinedSelection(UserDefinedSelectionConfig userDefinedSelection) {
        // TODO: 4/23/18 Complete to get rid of duplicate code
        StringBuilder userDefinedSelectionStringBuilder = new StringBuilder();

        if (userDefinedSelection == null || userDefinedSelection.getValue() == null || userDefinedSelection.getValue().isEmpty()) {
            throw new CodeGenerationException("The given UserDefinedSelection instance is null");
        }

        int attributesLeft = userDefinedSelection.getValue().size();
        for (SelectedAttribute attribute : userDefinedSelection.getValue()) {
            if (attribute.getExpression() == null || attribute.getExpression().isEmpty()) {
                throw new CodeGenerationException("The given expression of the attribute is null");
            }
            userDefinedSelectionStringBuilder.append(attribute.getExpression());
            if (attribute.getAs() != null && !attribute.getAs().isEmpty()) {
                if (!attribute.getAs().equals(attribute.getExpression())) {
                    userDefinedSelectionStringBuilder.append(Constants.SPACE)
                            .append(Constants.AS)
                            .append(Constants.SPACE)
                            .append(attribute.getAs());
                }
            }
            if (attributesLeft != 1) {
                userDefinedSelectionStringBuilder.append(Constants.COMMA)
                        .append(Constants.SPACE);
            }
            attributesLeft--;
        }

        return userDefinedSelectionStringBuilder.toString();
    }

    /**
     * Converts a list of groupBy parameters
     *
     * @param groupByList
     * @return
     */
    private String getQueryGroupBy(List<String> groupByList) {
        if (groupByList == null || groupByList.isEmpty()) {
            return VOID_RETURN;
        }

        StringBuilder groupByListStringBuilder = new StringBuilder();
        groupByListStringBuilder.append(Constants.GROUP_BY)
                .append(Constants.SPACE);
        groupByListStringBuilder.append(getParameterList(groupByList));
        return groupByListStringBuilder.toString();
    }

    private String getQueryOrderBy(List<QueryOrderByConfig> orderByList) {
        if (orderByList == null || orderByList.isEmpty()) {
            return VOID_RETURN;
        }

        StringBuilder orderByListStringBuilder = new StringBuilder();
        orderByListStringBuilder.append(Constants.ORDER_BY)
                .append(Constants.SPACE);

        int orderByAttributesLeft = orderByList.size();
        for (QueryOrderByConfig orderByAttribute : orderByList) {
            if (orderByAttribute == null) {
                throw new CodeGenerationException("Query Order By Attribute Cannot Be Null");
            } else if (orderByAttribute.getValue() == null || orderByAttribute.getValue().isEmpty()) {
                throw new CodeGenerationException("Query Order By Attribute Value Cannot Be Null");
            }

            orderByListStringBuilder.append(orderByAttribute.getValue());
            if (orderByAttribute.getOrder() != null && !orderByAttribute.getOrder().isEmpty()) {
                orderByListStringBuilder.append(Constants.SPACE)
                        .append(orderByAttribute.getOrder());
            }

            if (orderByAttributesLeft != 1) {
                orderByListStringBuilder.append(Constants.COMMA)
                        .append(Constants.SPACE);
            }
            orderByAttributesLeft--;
        }

        return orderByListStringBuilder.toString();
    }

    private String getQueryLimit(long limit) {
        if (limit != 0) {
            StringBuilder limitStringBuilder = new StringBuilder();
            limitStringBuilder.append(Constants.LIMIT)
                    .append(Constants.SPACE)
                    .append(limit);
            return limitStringBuilder.toString();
        }
        return VOID_RETURN;
    }

    private String getQueryHaving(String having) {
        if (having == null || having.isEmpty()) {
            return VOID_RETURN;
        }

        StringBuilder havingStringBuilder = new StringBuilder();
        havingStringBuilder.append(Constants.HAVING).append(Constants.SPACE).append(having);

        return havingStringBuilder.toString();
    }

    private String getQueryOutputRateLimit(String outputRateLimit) {
        if (outputRateLimit == null || outputRateLimit.isEmpty()) {
            return VOID_RETURN;
        }
        StringBuilder outputRateLimitStringBuilder = new StringBuilder();
        outputRateLimitStringBuilder.append(Constants.OUTPUT)
                .append(Constants.SPACE)
                .append(outputRateLimit);

        return outputRateLimitStringBuilder.toString();
    }

    private String getQueryOutput(QueryOutputConfig queryOutput) {
        if (queryOutput == null) {
            throw new CodeGenerationException("The QueryOutputInstance Given Is Null");
        } else if (queryOutput.getType() == null || queryOutput.getType().isEmpty()) {
            throw new CodeGenerationException("The query output type given is null");
        }

        StringBuilder queryOutputStringBuilder = new StringBuilder();

        switch (queryOutput.getType().toUpperCase()) {
            case "INSERT":
                InsertOutputConfig insertOutputConfig = (InsertOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(getInsertOutput(insertOutputConfig, queryOutput.getTarget()));
                break;
            case "DELETE":
                DeleteOutputConfig deleteOutputConfig = (DeleteOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(getDeleteOutput(deleteOutputConfig, queryOutput.getTarget()));
                break;
            case "UPDATE":
                UpdateInsertIntoOutputConfig updateIntoOutput = (UpdateInsertIntoOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(getUpdateOutput(queryOutput.getType(), updateIntoOutput, queryOutput.getTarget()));
                break;
            case "UPDATE_OR_INSERT_INTO":
                UpdateInsertIntoOutputConfig updateInsertIntoOutput = (UpdateInsertIntoOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(getUpdateOutput(queryOutput.getType(), updateInsertIntoOutput, queryOutput.getTarget()));
                break;
            default:
                throw new CodeGenerationException("Unidentified query output type: " + queryOutput.getType());
        }

        return queryOutputStringBuilder.toString();
    }

    private String getInsertOutput(InsertOutputConfig insertOutput, String target) {
        if (insertOutput == null) {
            throw new CodeGenerationException("The InsertOutputConfig instance given is null");
        } else if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The target for the given query output is null");
        }

        StringBuilder insertOutputStringBuilder = new StringBuilder();

        insertOutputStringBuilder.append(Constants.INSERT)
                .append(Constants.SPACE);

        if (insertOutput.getEventType() != null && !insertOutput.getEventType().isEmpty()) {
            switch (insertOutput.getEventType().toUpperCase()) {
                case "CURRENT_EVENTS":
                    insertOutputStringBuilder.append(Constants.CURRENT_EVENTS)
                            .append(Constants.SPACE);
                    break;
                case "EXPIRED_EVENTS":
                    insertOutputStringBuilder.append(Constants.EXPIRED_EVENTS)
                            .append(Constants.SPACE);
                    break;
                case "ALL_EVENTS":
                    insertOutputStringBuilder.append(Constants.ALL_EVENTS)
                            .append(Constants.SPACE);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified event type: " + insertOutput.getEventType());
            }
        }

        insertOutputStringBuilder.append(Constants.INTO)
                .append(Constants.SPACE)
                .append(target)
                .append(Constants.SEMI_COLON);

        return insertOutputStringBuilder.toString();
    }

    private String getDeleteOutput(DeleteOutputConfig deleteOutput, String target) {
        if (deleteOutput == null) {
            throw new CodeGenerationException("The given DeleteOutputConfig instance is null");
        } else if (deleteOutput.getOn() == null || deleteOutput.getOn().isEmpty()) {
            throw new CodeGenerationException("The 'on' statement of the given DeleteOutputConfig instance is null");
        } else if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The given query output target is null");
        }

        StringBuilder deleteOutputStringBuilder = new StringBuilder();

        deleteOutputStringBuilder.append(Constants.DELETE)
                .append(Constants.SPACE)
                .append(target);

        if (deleteOutput.getEventType() != null && !deleteOutput.getEventType().isEmpty()) {
            deleteOutputStringBuilder
                    .append(Constants.NEW_LINE)
                    .append(Constants.TAB_SPACE)
                    .append(Constants.FOR);
            switch (deleteOutput.getEventType().toUpperCase()) {
                case "CURRENT_EVENTS":
                    deleteOutputStringBuilder.append(Constants.CURRENT_EVENTS);
                    break;
                case "EXPIRED_EVENTS":
                    deleteOutputStringBuilder.append(Constants.EXPIRED_EVENTS);
                    break;
                case "ALL_EVENTS":
                    deleteOutputStringBuilder.append(Constants.ALL_EVENTS);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified event type: " + deleteOutput.getEventType());
            }
        }

        deleteOutputStringBuilder.append(Constants.NEW_LINE)
                .append(Constants.TAB_SPACE)
                .append(Constants.ON)
                .append(Constants.SPACE)
                .append(deleteOutput.getOn())
                .append(Constants.SEMI_COLON);

        return deleteOutputStringBuilder.toString();
    }

    private String getUpdateOutput(String type, UpdateInsertIntoOutputConfig updateInsertIntoOutput, String target) {
        if (updateInsertIntoOutput == null) {
            throw new CodeGenerationException("The given UpdateInsertIntoOutputConfig is null");
        } else if (updateInsertIntoOutput.getSet() == null || updateInsertIntoOutput.getSet().isEmpty()) {
            throw new CodeGenerationException("The 'set' values in the update/insert query is empty");
        } else if (updateInsertIntoOutput.getOn() == null || updateInsertIntoOutput.getOn().isEmpty()) {
            throw new CodeGenerationException("The 'on' value of the update/insert query is empty");
        } else if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The given target for the update/insert into query is null");
        }

        StringBuilder updateInsertIntoOutputStringBuilder = new StringBuilder();
        if (type.equalsIgnoreCase("UPDATE")) {
            updateInsertIntoOutputStringBuilder.append(Constants.UPDATE);
        } else if (type.equalsIgnoreCase("UPDATE_OR_INSERT_INTO")) {
            updateInsertIntoOutputStringBuilder.append(Constants.UPDATE_OR_INSERT_INTO);
        }

        updateInsertIntoOutputStringBuilder.append(Constants.SPACE)
                .append(target)
                .append(Constants.NEW_LINE)
                .append(Constants.TAB_SPACE)
                .append(Constants.SET)
                .append(Constants.SPACE);

        int setAttributesLeft = updateInsertIntoOutput.getSet().size();
        for (SetAttributeConfig setAttribute : updateInsertIntoOutput.getSet()) {
            if (setAttribute == null) {
                throw new CodeGenerationException("The given SetAttributeConfig instance is null in the update output query type");
            } else if (setAttribute.getAttribute() == null || setAttribute.getAttribute().isEmpty()) {
                throw new CodeGenerationException("The given attribute value to be set is null");
            } else if (setAttribute.getValue() == null || setAttribute.getValue().isEmpty()) {
                throw new CodeGenerationException("The given value to the value for update query output is null");
            }

            updateInsertIntoOutputStringBuilder.append(setAttribute.getAttribute())
                    .append(Constants.SPACE)
                    .append(Constants.EQUALS)
                    .append(Constants.SPACE)
                    .append(setAttribute.getValue());
            if (setAttributesLeft != 1) {
                updateInsertIntoOutputStringBuilder.append(Constants.COMMA)
                        .append(Constants.SPACE);
            }
            setAttributesLeft--;
        }

        updateInsertIntoOutputStringBuilder.append(Constants.NEW_LINE)
                .append(Constants.TAB_SPACE)
                .append(Constants.ON)
                .append(Constants.SPACE)
                .append(updateInsertIntoOutput.getOn())
                .append(Constants.SEMI_COLON);

        return updateInsertIntoOutputStringBuilder.toString();
    }

    private String getAttributes(List<AttributeConfig> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new CodeGenerationException("The attribute list given cannot be null/empty");
        }

        StringBuilder stringBuilder = new StringBuilder();
        int attributesLeft = attributes.size();
        for (AttributeConfig attribute : attributes) {
            if (attribute == null) {
                throw new CodeGenerationException("The attribute value given is null");
            } else if (attribute.getName() == null || attribute.getName().isEmpty()) {
                throw new CodeGenerationException("The attrubte name given is null");
            } else if (attribute.getType() == null || attribute.getType().isEmpty()) {
                throw new CodeGenerationException("The attrubte type given is null");
            }
            stringBuilder.append(attribute.getName())
                    .append(Constants.SPACE)
                    .append(attribute.getType());
            if (attributesLeft != 1) {
                stringBuilder.append(Constants.COMMA)
                        .append(Constants.SPACE);
            }
            attributesLeft--;
        }
        return stringBuilder.toString();
    }

    private String getAnnotations(List<AnnotationConfig> annotations) {
        // TODO: 4/18/18 Fill Once The Annotation Beans Are Complete
        StringBuilder annotationsStringBuilder = new StringBuilder();
        return annotationsStringBuilder.toString();
    }

    private String getStore(StoreConfig store) {
        StringBuilder storeStringBuilder = new StringBuilder();

        if (store != null) {
            storeStringBuilder.append(Constants.STORE)
                    .append(store.getType())
                    .append(Constants.SINGLE_QUOTE)
                    .append(Constants.COMMA)
                    .append(Constants.SPACE);
            Map<String, String> options = store.getOptions();
            int optionsLeft = options.size();
            for (Map.Entry<String, String> entry : options.entrySet()) {
                storeStringBuilder.append(entry.getKey())
                        .append(Constants.EQUALS)
                        .append(Constants.SINGLE_QUOTE)
                        .append(entry.getValue())
                        .append(Constants.SINGLE_QUOTE);
                if (optionsLeft != 1) {
                    storeStringBuilder.append(Constants.COMMA)
                            .append(Constants.SPACE);
                }
                optionsLeft--;
            }
            storeStringBuilder.append(Constants.CLOSE_BRACKET)
                    .append(Constants.NEW_LINE);
        }

        return storeStringBuilder.toString();
    }

    private String getParameterList(List<String> parameters) {
        StringBuilder parametersStringBuilder = new StringBuilder();

        int parametersLeft = parameters.size();
        for (String parameter : parameters) {
            parametersStringBuilder.append(parameter);
            if (parametersLeft != 1) {
                parametersStringBuilder.append(Constants.COMMA)
                        .append(Constants.SPACE);
            }
            parametersLeft--;
        }

        return parametersStringBuilder.toString();
    }

    /**
     * Contains all the generic string/char values that are needed by
     * the CodeGenerator class to build the entire Siddhi app string
     */
    private static class Constants {
        // TODO: 5/3/18 Change the name of the constants class to something else
        private static final char ALL = '*';
        private static final char CLOSE_BRACKET = ')';
        private static final char CLOSE_SQUARE_BRACKET = ']';
        private static final char COMMA = ',';
        private static final char DOUBLE_QUOTE = '\"';
        private static final char EQUALS = '=';
        private static final char FULL_STOP = '.';
        private static final char HASH = '#';
        private static final char NEW_LINE = '\n';
        private static final char OPEN_BRACKET = '(';
        private static final char OPEN_SQUARE_BRACKET = '[';
        private static final char SEMI_COLON = ';';
        private static final char SINGLE_QUOTE = '\'';
        private static final char SPACE = ' ';

        private static final String TAB_SPACE = "    ";
        private static final String THRIPPLE_DOTS = "...";

        private static final String DEFINE_STREAM = "define stream";
        private static final String DEFINE_TABLE = "define table";
        private static final String DEFINE_WINDOW = "define window";
        private static final String DEFINE_TRIGGER = "define trigger";
        private static final String DEFINE_AGGREGATION = "define aggregation";

        // TODO: 5/2/18 combine 'OUTPUT_<VALUE>_EVENTS' with 'OUTPUT + SPACE + <VALUE>_EVENTS'
        private static final String OUTPUT_CURRENT_EVENTS = "output current events";
        private static final String OUTPUT_EXPIRED_EVENTS = "output expired events";
        private static final String OUTPUT_ALL_EVENTS = "output all events";

        private static final String CURRENT_EVENTS = "current events";
        private static final String EXPIRED_EVENTS = "expired events";
        private static final String ALL_EVENTS = "all events";

        private static final String FROM = "from";
        private static final String SELECT = "select";
        private static final String AT = "at";
        private static final String AS = "as";
        private static final String GROUP_BY = "group by";
        private static final String ORDER_BY = "order by";
        private static final String AGGREGATE_BY = "aggregate by";
        private static final String EVERY = "every";
        private static final String LIMIT = "limit";
        private static final String WINDOW = "window";
        private static final String STORE = "@store(type='";
        private static final String HAVING = "having";
        private static final String OUTPUT = "output";
        private static final String ON = "on";
        private static final String INSERT = "insert";
        private static final String INTO = "into";
        private static final String DELETE = "delete";
        private static final String FOR = "for";
        private static final String UPDATE = "update";
        private static final String UPDATE_OR_INSERT_INTO = "update or insert into";
        private static final String SET = "set";
        private static final String UNIDIRECTIONAL = "unidirectional";
        private static final String JOIN = "join";
        private static final String LEFT_OUTER_JOIN = "left outer join";
        private static final String RIGHT_OUTER_JOIN = "right outer join";
        private static final String FULL_OUTER_JOIN = "full outer join";
        private static final String WITHIN = "within";
        private static final String PER = "per";
        private static final String APP_NAME = "@App:name('";
        private static final String APP_DESCRIPTION = "@App:description('";
        private static final String AGGREGATE = "aggregate";
        private static final String BY = "by";

        private Constants() {
        }

        // TODO: 5/4/18 Add another constants class for values that are not used to create the siddhi app
        // use these values for things like switch cases

    }

}
