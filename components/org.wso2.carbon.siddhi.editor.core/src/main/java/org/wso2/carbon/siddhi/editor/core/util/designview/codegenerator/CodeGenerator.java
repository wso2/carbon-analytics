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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregateByTimePeriod;
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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.DeleteOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.InsertOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sink.SinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.source.SourceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.AttributeSelection;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.singletons.CodeGeneratorSingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: 4/20/18 Check Everywhere for null values

/**
 * Used to convert an EventFlow object to a Siddhi app string
 */
public class CodeGenerator {

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

        for (StreamConfig stream : siddhiApp.getStreamList()) {
            siddhiAppStringBuilder.append(generateStreamString(stream));
        }

        for (TableConfig table : siddhiApp.getTableList()) {
            siddhiAppStringBuilder.append(generateTableString(table));
        }

        for (WindowConfig window : siddhiApp.getWindowList()) {
            siddhiAppStringBuilder.append(generateWindowString(window));
        }

        for (TriggerConfig trigger : siddhiApp.getTriggerList()) {
            siddhiAppStringBuilder.append(generateTriggerString(trigger));
        }

        for (AggregationConfig aggregation : siddhiApp.getAggregationList()) {
            siddhiAppStringBuilder.append(generateAggregationString(aggregation));
        }

        // TODO source and sink should be somehow connected to a stream over here
//        for (SourceConfig source : siddhiApp.getSourceList()) {
//            siddhiAppStringBuilder.append(generateSourceString(source));
//        }
//
//        for (SinkConfig sink : siddhiApp.getSinkList()) {
//            siddhiAppStringBuilder.append(generateSinkString(sink));
//        }

        for (QueryConfig query : siddhiApp.getQueryList()) {
            siddhiAppStringBuilder.append(generateQueryString(query));
        }

        // TODO: 4/23/18 Add the partitions loop

        return siddhiAppStringBuilder.toString();
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
        streamStringBuilder.append(getAnnotationsAsString(stream.getAnnotationList()))
                .append(Constants.DEFINE_STREAM)
                .append(Constants.SPACE)
                .append(stream.getName())
                .append(Constants.SPACE)
                .append(Constants.OPEN_BRACKET)
                .append(getAttributesAsString(stream.getAttributeList()))
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
        tableStringBuilder.append(getStoreAsString(table.getStore()))
                .append(getAnnotationsAsString(table.getAnnotationList()))
                .append(Constants.DEFINE_TABLE)
                .append(Constants.SPACE)
                .append(table.getName())
                .append(Constants.SPACE)
                .append(Constants.OPEN_BRACKET)
                .append(getAttributesAsString(table.getAttributeList()))
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
        windowStringBuilder.append(getAnnotationsAsString(window.getAnnotationList()))
                .append(Constants.DEFINE_WINDOW)
                .append(Constants.SPACE)
                .append(window.getName())
                .append(Constants.SPACE)
                .append(Constants.OPEN_BRACKET)
                .append(getAttributesAsString(window.getAttributeList()))
                .append(Constants.CLOSE_BRACKET)
                .append(Constants.SPACE)
                .append(window.getFunction())
                .append(Constants.OPEN_BRACKET)
                .append(getParameterListAsString(window.getParameters()))
                .append(Constants.CLOSE_BRACKET);

        if (window.getOutputEventType() != null && !window.getOutputEventType().isEmpty()) {
            windowStringBuilder.append(Constants.SPACE);
            switch (window.getOutputEventType()) {
                // TODO: 4/26/18 The cases must be constants and not free strings
                case "current":
                    windowStringBuilder.append(Constants.OUTPUT_CURRENT_EVENTS);
                    break;
                case "expired":
                    windowStringBuilder.append(Constants.OUTPUT_EXPIRED_EVENTS);
                    break;
                case "all":
                    windowStringBuilder.append(Constants.OUTPUT_ALL_EVENTS);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified output event type:" + window.getOutputEventType());
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
        triggerStringBuilder.append(getAnnotationsAsString(trigger.getAnnotationList()))
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
        } else if (aggregation.getAggregateByAttribute() == null || aggregation.getAggregateByAttribute().isEmpty()) {
            throw new CodeGenerationException("The aggregate by attribute in aggregation  is null");
        } else if (aggregation.getAggregateByTimePeriod() == null) {
            throw new CodeGenerationException("The AggregateByTimePeriod instance is null");
        } else if (aggregation.getAggregateByTimePeriod().getMinValue() == null || aggregation.getAggregateByTimePeriod().getMinValue().isEmpty()) {
            throw new CodeGenerationException("The aggregate by time period must have atleast one value for aggregation");
        }

        StringBuilder aggregationStringBuilder = new StringBuilder();
        aggregationStringBuilder.append(getStoreAsString(aggregation.getStore()))
                .append(getAnnotationsAsString(aggregation.getAnnotationList()))
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
                .append(getQuerySelectAsString(aggregation.getSelect()))
                .append(Constants.NEW_LINE)
                .append(Constants.TAB_SPACE)
                .append(getQueryGroupByAsString(aggregation.getGroupBy()))
                .append(Constants.NEW_LINE)
                .append(Constants.AGGREGATE_BY)
                .append(Constants.SPACE)
                .append(aggregation.getAggregateByAttribute())
                .append(Constants.SPACE)
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
        queryStringBuilder.append(getAnnotationsAsString(query.getAnnotationList()))
                .append(getQueryInputAsString(query.getQueryInput()))
                .append(Constants.NEW_LINE)
                .append(getQuerySelectAsString(query.getSelect()))
                .append(Constants.NEW_LINE)
                .append(getQueryGroupByAsString(query.getGroupBy()))
                .append(Constants.NEW_LINE)
                .append(getQueryOrderByAsString(query.getOrderBy()))
                .append(Constants.NEW_LINE)
                .append(getQueryLimitAsString(query.getLimit()))
                .append(Constants.NEW_LINE)
                .append(getQueryHavingAsString(query.getHaving()))
                .append(Constants.NEW_LINE)
                .append(getQueryOutputRateLimitAsString(query.getOutputRateLimit()))
                .append(Constants.NEW_LINE)
                .append(getQueryOutputAsString(query.getQueryOutput()));

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

    private String getQueryOutputRateLimitAsString(String outputRateLimit) {
        if (outputRateLimit == null || outputRateLimit.isEmpty()) {
            return VOID_RETURN;
        }
        StringBuilder outputRateLimitStringBuilder = new StringBuilder();
        outputRateLimitStringBuilder.append(Constants.OUTPUT)
                .append(Constants.SPACE)
                .append(outputRateLimit);

        return outputRateLimitStringBuilder.toString();
    }

    private String getQueryHavingAsString(String having) {
        if (having == null || having.isEmpty()) {
            return VOID_RETURN;
        }

        StringBuilder havingStringBuilder = new StringBuilder();
        havingStringBuilder.append(Constants.HAVING).append(Constants.SPACE).append(having);

        return havingStringBuilder.toString();
    }

    private String getQueryLimitAsString(long limit) {
        if (limit != 0) {
            StringBuilder limitStringBuilder = new StringBuilder();
            limitStringBuilder.append(Constants.LIMIT)
                    .append(Constants.SPACE)
                    .append(limit);
            return limitStringBuilder.toString();
        }
        return VOID_RETURN;
    }

    private String getQueryInputAsString(QueryInputConfig queryInput) {
        if (queryInput == null) {
            throw new CodeGenerationException("Query Input Cannot Be Null");
        } else if (queryInput.getType() == null || queryInput.getType().isEmpty()) {
            throw new CodeGenerationException("The query input type is null");
        }

        StringBuilder queryInputStringBuilder = new StringBuilder();

        switch (queryInput.getType().toLowerCase()) {
            case "window_filter_projection":
                WindowFilterProjectionConfig windowFilterProjectionQuery = (WindowFilterProjectionConfig) queryInput;
                queryInputStringBuilder.append(getWindowFilterProjectionQueryInputAsString(windowFilterProjectionQuery));
                break;
            case "join":
                JoinConfig joinQuery = (JoinConfig) queryInput;
                queryInputStringBuilder.append(getJoinQueryInputAsString(joinQuery));
                break;
            case "pattern":
//                PatternSequenceConfig patternQuery = (PatternSequenceConfig) queryInput;
//                queryInputStringBuilder.append(getPatternQueryInputAsString(patternQuery));
                break;
            case "sequence":
//                PatternSequenceConfig sequenceQuery = (PatternSequenceConfig) queryInput;
//                queryInputStringBuilder.append(getSequenceQueryInputAsString(sequenceQuery));
                break;
            default:
                throw new CodeGenerationException("Unidentified Query Input Type Has Been Given");
        }

        return queryInputStringBuilder.toString();
    }

    private String getSequenceQueryInputAsString(PatternSequenceConfig sequenceQuery) {
        return VOID_RETURN;
    }

    private String getQueryOutputAsString(QueryOutputConfig queryOutput) {
        if (queryOutput == null) {
            throw new CodeGenerationException("The QueryOutputInstance Given Is Null");
        } else if (queryOutput.getType() == null || queryOutput.getType().isEmpty()) {
            throw new CodeGenerationException("The query output type given is null");
        }

        StringBuilder queryOutputStringBuilder = new StringBuilder();

        switch (queryOutput.getType().toLowerCase()) {
            case "insert":
                InsertOutputConfig insertOutputConfig = (InsertOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(getQueryInsetOutputAsString(insertOutputConfig));
                break;
            case "delete":
                DeleteOutputConfig deleteOutputConfig = (DeleteOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(getQueryDeleteOutputAsString(deleteOutputConfig));
                break;
            case "update":
                break;
            case "update_or_insert_into":
                break;
            default:
                throw new CodeGenerationException("Unidentified query output type: " + queryOutput.getType());
        }

        return queryOutputStringBuilder.toString();
    }

    private String getQueryDeleteOutputAsString(DeleteOutputConfig deleteOutput) {
        return VOID_RETURN;
    }

    private String getQueryInsetOutputAsString(InsertOutputConfig insertOutput) {
        return VOID_RETURN;
    }

    private String getQueryOrderByAsString(List<QueryOrderByConfig> orderByList) {
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

    private String getQueryGroupByAsString(List<String> groupByList) {
        if (groupByList == null) {
            return VOID_RETURN;
        }

        StringBuilder groupByListStringBuilder = new StringBuilder();
        groupByListStringBuilder.append(Constants.GROUP_BY)
                .append(Constants.SPACE);
        groupByListStringBuilder.append(getParameterListAsString(groupByList));
        return groupByListStringBuilder.toString();
    }

    private String getQuerySelectAsString(AttributesSelectionConfig attributesSelection) {
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
                attributesSelectionStringBuilder.append(getUserDefinedSelectionAsString(userDefinedSelection));
                break;
            case AttributeSelection.TYPE_ALL:
                attributesSelectionStringBuilder.append(Constants.ALL);
                break;
            default:
                throw new CodeGenerationException("Undefined Attribute Selection Type");
        }

        return attributesSelectionStringBuilder.toString();
    }

    private String getAttributesAsString(List<AttributeConfig> attributes) {
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

    private String getAnnotationsAsString(List<AnnotationConfig> annotations) {
        // TODO: 4/18/18 Fill Once The Annotation Beans Are Complete
        StringBuilder annotationsStringBuilder = new StringBuilder();
        return annotationsStringBuilder.toString();
    }

    private String getStoreAsString(StoreConfig store) {
        StringBuilder storeStringBuilder = new StringBuilder();

        if (store != null) {
            storeStringBuilder.append(Constants.STORE)
                    .append(store.getType())
                    .append(Constants.SINGLE_QUOTE)
                    .append(Constants.COMMA)
                    .append(Constants.SPACE);
            Map<String, String> options = store.getOptions();
            int optionsLeft = options.size();
            for (String key : options.keySet()) {
                storeStringBuilder.append(key)
                        .append(Constants.EQUALS)
                        .append(Constants.SINGLE_QUOTE)
                        .append(options.get(key))
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

    private String getPatternQueryInputAsString(PatternSequenceConfig patternQuery) {
        StringBuilder patternQueryStringBuilder = new StringBuilder();
        return patternQueryStringBuilder.toString();
    }

    private String getWindowFilterProjectionQueryInputAsString(WindowFilterProjectionConfig windowFilterProjection) {
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
                    .append(getParameterListAsString(queryWindow.getParameters()))
                    .append(Constants.CLOSE_BRACKET);

            if (queryWindow.getFilter() != null && !queryWindow.getFilter().isEmpty()) {
                windowFilterProjectionStringBuilder.append(Constants.OPEN_SQUARE_BRACKET)
                        .append(queryWindow.getFilter())
                        .append(Constants.CLOSE_SQUARE_BRACKET);
            }
        }

        return windowFilterProjectionStringBuilder.toString();
    }

    private String getJoinQueryInputAsString(JoinConfig joinQuery) {
        StringBuilder joinQueryStringBuilder = new StringBuilder();
        return joinQueryStringBuilder.toString();
    }

    private String getUserDefinedSelectionAsString(UserDefinedSelectionConfig userDefinedSelection) {
        // TODO: 4/23/18 Complete to get rid of duplicate code
        StringBuilder userDefinedSelectionStringBuilder = new StringBuilder();

        if (userDefinedSelection == null || userDefinedSelection.getValue() == null || userDefinedSelection.getValue().isEmpty()) {
            throw new CodeGenerationException("UserDefinedSelection Instance '" + userDefinedSelection.toString() + "' is null");
        }

        int attributesLeft = userDefinedSelection.getValue().size();
        for (SelectedAttribute attribute : userDefinedSelection.getValue()) {
            userDefinedSelectionStringBuilder.append(attribute.getExpression());
            if (attribute.getAs() != null && !attribute.getAs().isEmpty()) {
                userDefinedSelectionStringBuilder.append(Constants.SPACE)
                        .append(Constants.AS)
                        .append(Constants.SPACE)
                        .append(attribute.getAs());
            }
            if (attributesLeft != 1) {
                userDefinedSelectionStringBuilder.append(Constants.COMMA)
                        .append(Constants.SPACE);
            }
            attributesLeft--;
        }

        return userDefinedSelectionStringBuilder.toString();
    }

    private String getParameterListAsString(List<String> parameters) {
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
    private class Constants {

        // TODO: 4/18/18 add all the needed constants here
        private static final char NEW_LINE = '\n';
        private static final char COMMA = ',';
        private static final char SPACE = ' ';
        private static final char OPEN_BRACKET = '(';
        private static final char CLOSE_BRACKET = ')';
        private static final char SEMI_COLON = ';';
        private static final char OPEN_SQUARE_BRACKET = '[';
        private static final char CLOSE_SQUARE_BRACKET = ']';
        private static final char HASH = '#';
        private static final char SINGLE_QUOTE = '\'';
        private static final char DOUBLE_QUOTE = '\"';
        private static final char EQUALS = '=';
        private static final char ALL = '*';
        private static final char FULL_STOP = '.';

        private static final String TAB_SPACE = "    ";

        private static final String DEFINE_STREAM = "define stream";
        private static final String DEFINE_TABLE = "define table";
        private static final String DEFINE_WINDOW = "define window";
        private static final String DEFINE_TRIGGER = "define trigger";
        private static final String DEFINE_AGGREGATION = "define aggregation";

        private static final String OUTPUT_CURRENT_EVENTS = "output current events";
        private static final String OUTPUT_EXPIRED_EVENTS = "output expired events";
        private static final String OUTPUT_ALL_EVENTS = "output all events";

        private static final String AT = "at";
        private static final String FROM = "from";
        private static final String SELECT = "select";
        private static final String AS = "as";
        private static final String GROUP_BY = "group by";
        private static final String ORDER_BY = "order by";
        private static final String AGGREGATE_BY = "aggregate by";
        private static final String EVERY = "every";
        private static final String THRIPPLE_DOTS = "...";
        private static final String LIMIT = "limit";
        private static final String WINDOW = "window";

        private static final String STORE = "@store(type='";
        private static final String HAVING = "having";
        private static final String OUTPUT = "output";

        private Constants() {
        }

    }

    public static void main(String[] args) {
        CodeGenerator codeGenerator = CodeGeneratorSingleton.getInstance();


        Map<String, String> options = new HashMap<>();
        options.put("jdbc.url", "jdbc:mysql://localhost:3306/production");
        options.put("username", "wso2");
        options.put("passwords", "123");
        options.put("jdbc.driver.name", "com.mysql.jdbc.Driver");
        StoreConfig store = new StoreConfig("<UniqueID>", "rdbms", options);

        List<AnnotationConfig> annotations = new ArrayList<>();

        AttributeConfig attribute1 = new AttributeConfig("name", "string");
        AttributeConfig attribute2 = new AttributeConfig("age", "int");
        List<AttributeConfig> attributes = new ArrayList<>();
        attributes.add(attribute1);
        attributes.add(attribute2);

        StreamConfig stream = new StreamConfig("InStream", "InStream", false, attributes, annotations);
        String streamStr = codeGenerator.generateStreamString(stream);
        System.out.println("\n" + streamStr);

        TableConfig table = new TableConfig("InTable", "InTable", attributes, store, annotations);
        String tableStr = codeGenerator.generateTableString(table);
        System.out.println("\n" + tableStr);

        List<String> parameters = new ArrayList<>();
        parameters.add("1 second");
        WindowConfig window = new WindowConfig("InWindow", "InWindow", attributes, "timeBatch", parameters, "all", annotations);
        String windowStr = codeGenerator.generateWindowString(window);
        System.out.println("\n" + windowStr);

        TriggerConfig trigger = new TriggerConfig("InTrigger", "InTrigger", "every 5 min", annotations);
        String triggerStr = codeGenerator.generateTriggerString(trigger);
        System.out.println("\n" + triggerStr);


        List<SelectedAttribute> selectedAttributes = new ArrayList<>();
        selectedAttributes.add(new SelectedAttribute("name", null));
        selectedAttributes.add(new SelectedAttribute("avg(age)", "avgAge"));
        UserDefinedSelectionConfig userDefinedSelection = new UserDefinedSelectionConfig(selectedAttributes);

        List<String> groupBy = new ArrayList<>();
        groupBy.add("name");

        AggregateByTimePeriod aggregateByTimePeriod = new AggregateByTimePeriod("sec", "year");

        AggregationConfig aggregation = new AggregationConfig("InAggregation",
                "InAggregation",
                "InStream",
                userDefinedSelection,
                groupBy,
                "timestamp",
                aggregateByTimePeriod,
                store,
                annotations);
        String aggregationStr = codeGenerator.generateAggregationString(aggregation);
        System.out.println("\n" + aggregationStr);


//        List<String> params = new ArrayList<>();
//        params.add("1 second");
//        QueryWindowConfig queryWindowConfig = new QueryWindowConfig("time", params, "age < 30");
//        WindowFilterProjectionConfig windowFilterProjectionQueryConfig = new WindowFilterProjectionConfig("window",
//                "InStream",
//                "age >= 18",
//                queryWindowConfig);
//
//        InsertOutputConfig insertOutputConfig = new InsertOutputConfig("current");
//        QueryOutputConfig queryOutputConfig = new QueryOutputConfig("insert",
//                insertOutputConfig,
//                "OutStream");

//        List<QueryOrderByConfig> orderBy = new ArrayList<>();
//        orderBy.add(new QueryOrderByConfig("name", "desc"));

//        QueryConfig queryConfig = new QueryConfig("QueryID",
//                windowFilterProjectionQueryConfig,
//                userDefinedSelection,
//                groupBy,
//                orderBy,
//                100,
//                "Your Mom",
//                "No U",
//                queryOutputConfig,
//                annotations);
//        String queryStr = codeGenerator.generateQueryString(queryConfig);
//        System.out.println("\n" + queryStr);
    }

}
