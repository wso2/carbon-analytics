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

package org.wso2.carbon.siddhi.editor.core.util.designview.utilities;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.AttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StoreConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.SelectedAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.UserDefinedSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryOrderByConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConditionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.DeleteOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.InsertOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.UpdateInsertIntoOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.setattribute.SetAttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FilterConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FunctionWindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.MapperConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.AttributeSelection;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

import java.util.List;
import java.util.Map;

// TODO: 4/20/18 Check Everywhere for null values
// TODO: 5/2/18 Look for constants for all the cases in switch case
// TODO: 5/24/18 Improve The Information Given In The Error Messages

public class CodeGeneratorHelper {

    public static String getAttributes(List<AttributeConfig> attributes) {
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
                    .append(CodeGeneratorConstants.SPACE)
                    .append(attribute.getType());
            if (attributesLeft != 1) {
                stringBuilder.append(CodeGeneratorConstants.COMMA)
                        .append(CodeGeneratorConstants.SPACE);
            }
            attributesLeft--;
        }
        return stringBuilder.toString();
    }

    public static String getParameterList(List<String> parameters) {
        if (parameters.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder parametersStringBuilder = new StringBuilder();
        int parametersLeft = parameters.size();
        for (String parameter : parameters) {
            parametersStringBuilder.append(parameter);
            if (parametersLeft != 1) {
                parametersStringBuilder.append(CodeGeneratorConstants.COMMA)
                        .append(CodeGeneratorConstants.SPACE);
            }
            parametersLeft--;
        }

        return parametersStringBuilder.toString();
    }

    public static String getStore(StoreConfig store) {
        if (store == null) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder storeStringBuilder = new StringBuilder();

        storeStringBuilder.append(CodeGeneratorConstants.STORE)
                .append(store.getType())
                .append(CodeGeneratorConstants.SINGLE_QUOTE)
                .append(CodeGeneratorConstants.COMMA)
                .append(CodeGeneratorConstants.SPACE);
        Map<String, String> options = store.getOptions();
        int optionsLeft = options.size();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            storeStringBuilder.append(entry.getKey())
                    .append(CodeGeneratorConstants.EQUALS)
                    .append(CodeGeneratorConstants.SINGLE_QUOTE)
                    .append(entry.getValue())
                    .append(CodeGeneratorConstants.SINGLE_QUOTE);
            if (optionsLeft != 1) {
                storeStringBuilder.append(CodeGeneratorConstants.COMMA)
                        .append(CodeGeneratorConstants.SPACE);
            }
            optionsLeft--;
        }
        storeStringBuilder.append(CodeGeneratorConstants.CLOSE_BRACKET)
                .append(CodeGeneratorConstants.NEW_LINE);

        return storeStringBuilder.toString();
    }

    public static String getAnnotations(List<String> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder annotationsStringBuilder = new StringBuilder();
        for (String annotation : annotations) {
            annotationsStringBuilder.append(annotation)
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        return annotationsStringBuilder.toString();
    }

    public static String getAggregationAnnotations(List<String> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder annotationsStringBuilder = new StringBuilder();
        for (String annotation : annotations) {
            if (annotation.toLowerCase().contains("@primarykey")) {
                break;
            }
            annotationsStringBuilder.append(annotation)
                    .append(CodeGeneratorConstants.NEW_LINE);
        }

        return annotationsStringBuilder.toString();
    }

    public static String getMapper(MapperConfig mapper, String annotationType) {
        if (mapper.getType() == null || mapper.getType().isEmpty()) {
            // TODO not sure whether to throw an error here or not
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder mapperStringBuilder = new StringBuilder();
        mapperStringBuilder.append(CodeGeneratorConstants.MAP)
                .append(mapper.getType())
                .append(CodeGeneratorConstants.SINGLE_QUOTE);

        if (mapper.getOptions() != null && !mapper.getOptions().isEmpty()) {
            mapperStringBuilder.append(CodeGeneratorConstants.COMMA)
                    .append(CodeGeneratorConstants.SPACE)
                    .append(getParameterList(mapper.getOptions()));
        }

        if (mapper.getAttributes() != null && !mapper.getAttributes().isEmpty()) {
            mapperStringBuilder.append(CodeGeneratorConstants.COMMA)
                    .append(CodeGeneratorConstants.SPACE);
            if (annotationType.equalsIgnoreCase("SINK")) {
                mapperStringBuilder.append(CodeGeneratorConstants.ATTRIBUTES);
            } else if (annotationType.equalsIgnoreCase("SOURCE")) {
                mapperStringBuilder.append(CodeGeneratorConstants.PAYLOAD);
            }

            mapperStringBuilder.append(getParameterList(mapper.getAttributes()))
                    .append(CodeGeneratorConstants.CLOSE_BRACKET);
        }

        mapperStringBuilder.append(CodeGeneratorConstants.CLOSE_BRACKET);

        return mapperStringBuilder.toString();
    }

    /**
     * Converts a QueryInputConfig object to the input part of a Siddhi query
     *
     * @param queryInput The QueryInputConfig instance to be converted
     * @return The query input string representation of the given QueryInputConfig object
     */
    public static String getQueryInput(QueryInputConfig queryInput) {
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
    private static String getWindowFilterProjectionQueryInput(WindowFilterProjectionConfig windowFilterProjection) {
        // TODO: 5/28/18 Complete this 
        if (windowFilterProjection == null) {
            throw new CodeGenerationException("The WindowFilterProjection Instance Is Null");
        } else if (windowFilterProjection.getFrom() == null || windowFilterProjection.getFrom().isEmpty()) {
            throw new CodeGenerationException("From Value Is Null");
        }

        StringBuilder windowFilterProjectionStringBuilder = new StringBuilder();
        windowFilterProjectionStringBuilder.append(CodeGeneratorConstants.FROM)
                .append(CodeGeneratorConstants.SPACE)
                .append(windowFilterProjection.getFrom())
                .append(getStreamHandlerList(windowFilterProjection.getStreamHandlerList()));

        return windowFilterProjectionStringBuilder.toString();
    }

    /**
     * Converts a JoinConfig object to a query input of type join as a string
     *
     * @param join The JoinConfig object to be converted
     * @return The join input string representation of the given JoinConfig object
     */
    private static String getJoinQueryInput(JoinConfig join) {
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
        }

        StringBuilder joinStringBuilder = new StringBuilder();
        joinStringBuilder.append(CodeGeneratorConstants.FROM)
                .append(CodeGeneratorConstants.SPACE)
                .append(getJoinElement(join.getLeft()))
                .append(CodeGeneratorConstants.SPACE)
                .append(getJoinType(join.getJoinType()))
                .append(CodeGeneratorConstants.SPACE)
                .append(getJoinElement(join.getRight()))
                .append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorConstants.TAB_SPACE)
                .append(CodeGeneratorConstants.ON)
                .append(CodeGeneratorConstants.SPACE)
                .append(join.getOn());

        if (join.getJoinWith().equalsIgnoreCase("AGGREGATION")) {
            if (join.getWithin() == null || join.getWithin().isEmpty()) {
                throw new CodeGenerationException("The 'within' attribute for the given join aggregation query is null");
            } else if (join.getPer() == null || join.getPer().isEmpty()) {
                throw new CodeGenerationException("The 'per' attribute for the given join aggregation query is null");
            }

            joinStringBuilder.append(CodeGeneratorConstants.NEW_LINE)
                    .append(CodeGeneratorConstants.TAB_SPACE)
                    .append(CodeGeneratorConstants.WITHIN)
                    .append(CodeGeneratorConstants.SPACE)
                    .append(join.getWithin())
                    .append(CodeGeneratorConstants.NEW_LINE)
                    .append(CodeGeneratorConstants.TAB_SPACE)
                    .append(CodeGeneratorConstants.PER)
                    .append(CodeGeneratorConstants.SPACE)
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
    private static String getJoinElement(JoinElementConfig joinElement) {
        // TODO: 5/28/18 complete and test this
        if (joinElement == null) {
            throw new CodeGenerationException("The given JoinElementConfig instance given is null");
        } else if (joinElement.getFrom() == null || joinElement.getFrom().isEmpty()) {
            throw new CodeGenerationException("The 'from' value in the given JoinElementConfig instance is null");
        }

        StringBuilder joinElementStringBuilder = new StringBuilder();

        joinElementStringBuilder.append(joinElement.getFrom())
                .append(getStreamHandlerList(joinElement.getStreamHandlerList()));

        if (joinElement.getAs() != null && !joinElement.getAs().isEmpty()) {
            joinElementStringBuilder.append(CodeGeneratorConstants.SPACE)
                    .append(CodeGeneratorConstants.AS)
                    .append(CodeGeneratorConstants.SPACE)
                    .append(joinElement.getAs());
        }

        if (joinElement.isUnidirectional()) {
            joinElementStringBuilder.append(CodeGeneratorConstants.SPACE)
                    .append(CodeGeneratorConstants.UNIDIRECTIONAL);
        }

        return joinElementStringBuilder.toString();
    }

    private static String getStreamHandlerList(List<StreamHandlerConfig> streamHandlerList) {
        if (streamHandlerList == null || streamHandlerList.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder streamhandlerListStringBuilder = new StringBuilder();

        for (StreamHandlerConfig streamHandler : streamHandlerList) {
            streamhandlerListStringBuilder.append(getStreamHandler(streamHandler));
        }

        return streamhandlerListStringBuilder.toString();
    }

    private static String getStreamHandler(StreamHandlerConfig streamHandler) {
        if (streamHandler == null) {
            throw new CodeGenerationException("Empty StreamHandler Config");
        } else if (streamHandler.getType() == null || streamHandler.getType().isEmpty()) {
            throw new CodeGenerationException("The type variable of the StreamHandlerConfig instance given is null");
        }

        StringBuilder streamHandlerStringBuilder = new StringBuilder();

        switch (streamHandler.getType().toUpperCase()) {
            case "FILTER":
                FilterConfig filter = (FilterConfig) streamHandler;
                streamHandlerStringBuilder.append(CodeGeneratorConstants.OPEN_SQUARE_BRACKET)
                        .append(filter.getValue())
                        .append(CodeGeneratorConstants.CLOSE_SQUARE_BRACKET);
                break;
            case "FUNCTION":
                FunctionWindowConfig function = (FunctionWindowConfig) streamHandler;
                streamHandlerStringBuilder.append(CodeGeneratorConstants.HASH)
                        .append(function.getValue().getFunction())
                        .append(CodeGeneratorConstants.OPEN_BRACKET)
                        .append(getParameterList(function.getValue().getParameters()))
                        .append(CodeGeneratorConstants.CLOSE_BRACKET);
                break;
            case "WINDOW":
                FunctionWindowConfig window = (FunctionWindowConfig) streamHandler;
                streamHandlerStringBuilder.append(CodeGeneratorConstants.HASH)
                        .append(CodeGeneratorConstants.WINDOW)
                        .append(CodeGeneratorConstants.FULL_STOP)
                        .append(window.getValue().getFunction())
                        .append(CodeGeneratorConstants.OPEN_BRACKET)
                        .append(getParameterList(window.getValue().getParameters()))
                        .append(CodeGeneratorConstants.CLOSE_BRACKET);
                break;
            default:
                throw new CodeGenerationException("Unidentified StreamHandlerConfig type: "
                        + streamHandler.getType());
        }

        return streamHandlerStringBuilder.toString();
    }

    /**
     * Identifies the join type string given and returns the respective Siddhi code snippet for the
     * given join type
     *
     * @param joinType The identifier for which join type the query has
     * @return The Siddhi representation of the given join type
     */
    private static String getJoinType(String joinType) {
        if (joinType == null || joinType.isEmpty()) {
            throw new CodeGenerationException("The join type value given is null");
        }

        switch (joinType.toUpperCase()) {
            case "JOIN":
                return CodeGeneratorConstants.JOIN;
            case "LEFT_OUTER":
                return CodeGeneratorConstants.LEFT_OUTER_JOIN;
            case "RIGHT_OUTER":
                return CodeGeneratorConstants.RIGHT_OUTER_JOIN;
            case "FULL_OUTER":
                return CodeGeneratorConstants.FULL_OUTER_JOIN;
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
    private static String getPatternSequenceInput(PatternSequenceConfig patternSequence) {
        // TODO: 5/28/18 Complete and test
        if (patternSequence == null) {
            throw new CodeGenerationException("The given PatternSequenceConfig instance is null");
        } else if (patternSequence.getLogic() == null || patternSequence.getLogic().isEmpty()) {
            throw new CodeGenerationException("The 'logic' attribute in the given " +
                    "PatternSequenceConfig instance in null");
        } else if (patternSequence.getConditionList() == null || patternSequence.getConditionList().isEmpty()) {
            throw new CodeGenerationException("The condition list of the given PatternSequenceConfig is null/empty");
        }

        String logic = patternSequence.getLogic();

        for (PatternSequenceConditionConfig condition : patternSequence.getConditionList()) {
            logic = logic.replace(condition.getConditionId(), getPatternSequenceConditionLogic(condition));
        }

        return logic;
    }

    private static String getPatternSequenceConditionLogic(PatternSequenceConditionConfig condition) {
        if (condition == null) {
            throw new CodeGenerationException("The given PatternSequenceConditionConfig instance is null");
        } else if (condition.getStreamName() == null || condition.getStreamName().isEmpty()) {
            throw new CodeGenerationException("The stream name of the given PatternSequenceConditionConfig" +
                    " instance is null");
        }

        StringBuilder patternSequenceConditionStringBuilder = new StringBuilder();

        patternSequenceConditionStringBuilder.append(condition.getStreamName())
                .append(getStreamHandlerList(condition.getStreamHandlerList()));

        return patternSequenceConditionStringBuilder.toString();
    }

    /**
     * Converts a AttributesSelectionConfig object to the select section of a Siddhi query as a string
     *
     * @param attributesSelection The AttributeSelectionConfig object to be converted
     * @return The string representation of the select part of a Siddhi query
     */
    public static String getQuerySelect(AttributesSelectionConfig attributesSelection) {
        if (attributesSelection == null) {
            throw new CodeGenerationException(" Attribute Selection Instance Cannot Be Null");
        }

        StringBuilder attributesSelectionStringBuilder = new StringBuilder();

        attributesSelectionStringBuilder.append(CodeGeneratorConstants.SELECT)
                .append(CodeGeneratorConstants.SPACE);

        if (attributesSelection.getType() == null || attributesSelection.getType().isEmpty()) {
            throw new CodeGenerationException("The Type Of Attribute Selection Cannot Be Null");
        }

        switch (attributesSelection.getType().toUpperCase()) {
            case AttributeSelection.TYPE_USER_DEFINED:
                UserDefinedSelectionConfig userDefinedSelection = (UserDefinedSelectionConfig) attributesSelection;
                attributesSelectionStringBuilder.append(getUserDefinedSelection(userDefinedSelection));
                break;
            case AttributeSelection.TYPE_ALL:
                attributesSelectionStringBuilder.append(CodeGeneratorConstants.ALL);
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
    private static String getUserDefinedSelection(UserDefinedSelectionConfig userDefinedSelection) {
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
                    userDefinedSelectionStringBuilder.append(CodeGeneratorConstants.SPACE)
                            .append(CodeGeneratorConstants.AS)
                            .append(CodeGeneratorConstants.SPACE)
                            .append(attribute.getAs());
                }
            }
            if (attributesLeft != 1) {
                userDefinedSelectionStringBuilder.append(CodeGeneratorConstants.COMMA)
                        .append(CodeGeneratorConstants.SPACE);
            }
            attributesLeft--;
        }

        return userDefinedSelectionStringBuilder.toString();
    }

    public static String getQueryGroupBy(List<String> groupByList) {
        if (groupByList == null || groupByList.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder groupByListStringBuilder = new StringBuilder();
        groupByListStringBuilder.append(CodeGeneratorConstants.GROUP_BY)
                .append(CodeGeneratorConstants.SPACE);
        groupByListStringBuilder.append(getParameterList(groupByList));
        return groupByListStringBuilder.toString();
    }

    public static String getQueryOrderBy(List<QueryOrderByConfig> orderByList) {
        if (orderByList == null || orderByList.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder orderByListStringBuilder = new StringBuilder();
        orderByListStringBuilder.append(CodeGeneratorConstants.ORDER_BY)
                .append(CodeGeneratorConstants.SPACE);

        int orderByAttributesLeft = orderByList.size();
        for (QueryOrderByConfig orderByAttribute : orderByList) {
            // TODO: 5/28/18 Can add this to another method to decrease complexity
            if (orderByAttribute == null) {
                throw new CodeGenerationException("Query Order By Attribute Cannot Be Null");
            } else if (orderByAttribute.getValue() == null || orderByAttribute.getValue().isEmpty()) {
                throw new CodeGenerationException("Query Order By Attribute Value Cannot Be Null");
            }

            orderByListStringBuilder.append(orderByAttribute.getValue());
            if (orderByAttribute.getOrder() != null && !orderByAttribute.getOrder().isEmpty()) {
                orderByListStringBuilder.append(CodeGeneratorConstants.SPACE)
                        .append(orderByAttribute.getOrder());
            }

            if (orderByAttributesLeft != 1) {
                orderByListStringBuilder.append(CodeGeneratorConstants.COMMA)
                        .append(CodeGeneratorConstants.SPACE);
            }
            orderByAttributesLeft--;
        }

        return orderByListStringBuilder.toString();
    }

    public static String getQueryLimit(long limit) {
        if (limit != 0) {
            StringBuilder limitStringBuilder = new StringBuilder();
            limitStringBuilder.append(CodeGeneratorConstants.LIMIT)
                    .append(CodeGeneratorConstants.SPACE)
                    .append(limit);
            return limitStringBuilder.toString();
        }
        return CodeGeneratorConstants.EMPTY_STRING;
    }

    public static String getQueryHaving(String having) {
        if (having == null || having.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }

        StringBuilder havingStringBuilder = new StringBuilder();
        havingStringBuilder.append(CodeGeneratorConstants.HAVING).append(CodeGeneratorConstants.SPACE).append(having);

        return havingStringBuilder.toString();
    }

    public static String getQueryOutputRateLimit(String outputRateLimit) {
        if (outputRateLimit == null || outputRateLimit.isEmpty()) {
            return CodeGeneratorConstants.EMPTY_STRING;
        }
        StringBuilder outputRateLimitStringBuilder = new StringBuilder();
        outputRateLimitStringBuilder.append(CodeGeneratorConstants.OUTPUT)
                .append(CodeGeneratorConstants.SPACE)
                .append(outputRateLimit);

        return outputRateLimitStringBuilder.toString();
    }

    public static String getQueryOutput(QueryOutputConfig queryOutput) {
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

    private static String getInsertOutput(InsertOutputConfig insertOutput, String target) {
        if (insertOutput == null) {
            throw new CodeGenerationException("The InsertOutputConfig instance given is null");
        } else if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The target for the given query output is null");
        }

        StringBuilder insertOutputStringBuilder = new StringBuilder();

        insertOutputStringBuilder.append(CodeGeneratorConstants.INSERT)
                .append(CodeGeneratorConstants.SPACE);

        if (insertOutput.getEventType() != null && !insertOutput.getEventType().isEmpty()) {
            switch (insertOutput.getEventType().toUpperCase()) {
                case "CURRENT_EVENTS":
                    insertOutputStringBuilder.append(CodeGeneratorConstants.CURRENT_EVENTS)
                            .append(CodeGeneratorConstants.SPACE);
                    break;
                case "EXPIRED_EVENTS":
                    insertOutputStringBuilder.append(CodeGeneratorConstants.EXPIRED_EVENTS)
                            .append(CodeGeneratorConstants.SPACE);
                    break;
                case "ALL_EVENTS":
                    insertOutputStringBuilder.append(CodeGeneratorConstants.ALL_EVENTS)
                            .append(CodeGeneratorConstants.SPACE);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified event type: " + insertOutput.getEventType());
            }
        }

        insertOutputStringBuilder.append(CodeGeneratorConstants.INTO)
                .append(CodeGeneratorConstants.SPACE)
                .append(target)
                .append(CodeGeneratorConstants.SEMI_COLON);

        return insertOutputStringBuilder.toString();
    }

    private static String getDeleteOutput(DeleteOutputConfig deleteOutput, String target) {
        if (deleteOutput == null) {
            throw new CodeGenerationException("The given DeleteOutputConfig instance is null");
        } else if (deleteOutput.getOn() == null || deleteOutput.getOn().isEmpty()) {
            throw new CodeGenerationException("The 'on' statement of the given DeleteOutputConfig instance is null");
        } else if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The given query output target is null");
        }

        StringBuilder deleteOutputStringBuilder = new StringBuilder();

        deleteOutputStringBuilder.append(CodeGeneratorConstants.DELETE)
                .append(CodeGeneratorConstants.SPACE)
                .append(target);

        if (deleteOutput.getEventType() != null && !deleteOutput.getEventType().isEmpty()) {
            deleteOutputStringBuilder
                    .append(CodeGeneratorConstants.NEW_LINE)
                    .append(CodeGeneratorConstants.TAB_SPACE)
                    .append(CodeGeneratorConstants.FOR);
            switch (deleteOutput.getEventType().toUpperCase()) {
                case "CURRENT_EVENTS":
                    deleteOutputStringBuilder.append(CodeGeneratorConstants.CURRENT_EVENTS);
                    break;
                case "EXPIRED_EVENTS":
                    deleteOutputStringBuilder.append(CodeGeneratorConstants.EXPIRED_EVENTS);
                    break;
                case "ALL_EVENTS":
                    deleteOutputStringBuilder.append(CodeGeneratorConstants.ALL_EVENTS);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified event type: " + deleteOutput.getEventType());
            }
        }

        deleteOutputStringBuilder.append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorConstants.TAB_SPACE)
                .append(CodeGeneratorConstants.ON)
                .append(CodeGeneratorConstants.SPACE)
                .append(deleteOutput.getOn())
                .append(CodeGeneratorConstants.SEMI_COLON);

        return deleteOutputStringBuilder.toString();
    }

    private static String getUpdateOutput(String type, UpdateInsertIntoOutputConfig updateInsertIntoOutput, String target) {
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
            updateInsertIntoOutputStringBuilder.append(CodeGeneratorConstants.UPDATE);
        } else if (type.equalsIgnoreCase("UPDATE_OR_INSERT_INTO")) {
            updateInsertIntoOutputStringBuilder.append(CodeGeneratorConstants.UPDATE_OR_INSERT_INTO);
        }

        updateInsertIntoOutputStringBuilder.append(CodeGeneratorConstants.SPACE)
                .append(target)
                .append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorConstants.TAB_SPACE)
                .append(CodeGeneratorConstants.SET)
                .append(CodeGeneratorConstants.SPACE);

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
                    .append(CodeGeneratorConstants.SPACE)
                    .append(CodeGeneratorConstants.EQUALS)
                    .append(CodeGeneratorConstants.SPACE)
                    .append(setAttribute.getValue());
            if (setAttributesLeft != 1) {
                updateInsertIntoOutputStringBuilder.append(CodeGeneratorConstants.COMMA)
                        .append(CodeGeneratorConstants.SPACE);
            }
            setAttributesLeft--;
        }

        updateInsertIntoOutputStringBuilder.append(CodeGeneratorConstants.NEW_LINE)
                .append(CodeGeneratorConstants.TAB_SPACE)
                .append(CodeGeneratorConstants.ON)
                .append(CodeGeneratorConstants.SPACE)
                .append(updateInsertIntoOutput.getOn())
                .append(CodeGeneratorConstants.SEMI_COLON);

        return updateInsertIntoOutputStringBuilder.toString();
    }

}
