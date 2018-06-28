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

package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConditionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.SubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate's the code for a input element of a Siddhi query
 */
public class QueryInputCodeGenerator {

    /**
     * Generate's the Siddhi code representation of a QueryInputConfig object
     *
     * @param queryInput The QueryInputConfig object
     * @return The Siddhi code representation of the given QueryInputConfig object
     * @throws CodeGenerationException Error when generating code
     */
    public static String generateQueryInput(QueryInputConfig queryInput) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(queryInput);

        StringBuilder queryInputStringBuilder = new StringBuilder();
        switch (queryInput.getType().toUpperCase()) {
            case CodeGeneratorConstants.WINDOW:
            case CodeGeneratorConstants.FILTER:
            case CodeGeneratorConstants.PROJECTION:
            case CodeGeneratorConstants.FUNCTION:
                WindowFilterProjectionConfig windowFilterProjectionQuery = (WindowFilterProjectionConfig) queryInput;
                queryInputStringBuilder.append(generateWindowFilterProjectionQueryInput(windowFilterProjectionQuery));
                break;
            case CodeGeneratorConstants.JOIN:
                JoinConfig joinQuery = (JoinConfig) queryInput;
                queryInputStringBuilder.append(generateJoinQueryInput(joinQuery));
                break;
            case CodeGeneratorConstants.PATTERN:
            case CodeGeneratorConstants.SEQUENCE:
                PatternSequenceConfig patternSequence = (PatternSequenceConfig) queryInput;
                queryInputStringBuilder.append(generatePatternSequenceInput(patternSequence));
                break;
            default:
                throw new CodeGenerationException("Unidentified query input type: " + queryInput.getType());
        }

        return queryInputStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a WindowFilterProjectionConfig object
     *
     * @param windowFilterProjection The WindowFilterProjectionConfig object
     * @return The Siddhi code representation of the given WindowFilterProjectionConfig object
     * @throws CodeGenerationException Error when generating code
     */
    private static String generateWindowFilterProjectionQueryInput(WindowFilterProjectionConfig windowFilterProjection)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(windowFilterProjection);

        return SiddhiCodeBuilderConstants.FROM +
                SiddhiCodeBuilderConstants.SPACE +
                windowFilterProjection.getFrom() +
                SubElementCodeGenerator.generateStreamHandlerList(windowFilterProjection.getStreamHandlerList());
    }

    /**
     * Generate's the Siddhi code representation of a JoinConfig object
     *
     * @param join The JoinConfig object
     * @return The Siddhi code representation of the given JoinConfig object
     * @throws CodeGenerationException Error when generating code
     */
    private static String generateJoinQueryInput(JoinConfig join) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(join);

        StringBuilder joinStringBuilder = new StringBuilder();
        joinStringBuilder.append(SiddhiCodeBuilderConstants.FROM)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(generateJoinElement(join.getLeft()))
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(generateJoinType(join.getJoinType()))
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(generateJoinElement(join.getRight()));

        if (join.getOn() != null && !join.getOn().isEmpty()) {
            joinStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(SiddhiCodeBuilderConstants.ON)
                    .append(SiddhiCodeBuilderConstants.SPACE)
                    .append(join.getOn());
        }

        if (join.getJoinWith().equalsIgnoreCase(CodeGeneratorConstants.AGGREGATION)) {
            if (join.getWithin() == null || join.getWithin().isEmpty()) {
                throw new CodeGenerationException("The 'within' value for a given join" +
                        " aggregation query is empty");
            } else if (join.getPer() == null || join.getPer().isEmpty()) {
                throw new CodeGenerationException("The 'per' attribute for a given join " +
                        "aggregation query is empty");
            }

            joinStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(SiddhiCodeBuilderConstants.WITHIN)
                    .append(SiddhiCodeBuilderConstants.SPACE)
                    .append(join.getWithin())
                    .append(SiddhiCodeBuilderConstants.SPACE)
                    .append(SiddhiCodeBuilderConstants.PER)
                    .append(SiddhiCodeBuilderConstants.SPACE)
                    .append(join.getPer());
        }

        return joinStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a JoinElementConfig object
     *
     * @param joinElement The JoinElementConfig object
     * @return The Siddhi code representation of the given JoinElementConfig object
     * @throws CodeGenerationException Error when generating code
     */
    private static String generateJoinElement(JoinElementConfig joinElement) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(joinElement);

        StringBuilder joinElementStringBuilder = new StringBuilder();

        joinElementStringBuilder.append(joinElement.getFrom())
                .append(SubElementCodeGenerator.generateStreamHandlerList(joinElement.getStreamHandlerList()));

        if (joinElement.getAs() != null && !joinElement.getAs().isEmpty()) {
            joinElementStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(SiddhiCodeBuilderConstants.AS)
                    .append(SiddhiCodeBuilderConstants.SPACE)
                    .append(joinElement.getAs());
        }

        if (joinElement.isUnidirectional()) {
            joinElementStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(SiddhiCodeBuilderConstants.UNIDIRECTIONAL);
        }

        return joinElementStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a join type
     *
     * @param joinType The join type
     * @return The Siddhi code representation of the given join type
     * @throws CodeGenerationException Error when generating code
     */
    private static String generateJoinType(String joinType) throws CodeGenerationException {
        if (joinType == null || joinType.isEmpty()) {
            throw new CodeGenerationException("The 'joinType' value of a given join query is empty");
        }

        switch (joinType.toUpperCase()) {
            case CodeGeneratorConstants.JOIN:
                return SiddhiCodeBuilderConstants.JOIN;
            case CodeGeneratorConstants.LEFT_OUTER:
                return SiddhiCodeBuilderConstants.LEFT_OUTER_JOIN;
            case CodeGeneratorConstants.RIGHT_OUTER:
                return SiddhiCodeBuilderConstants.RIGHT_OUTER_JOIN;
            case CodeGeneratorConstants.FULL_OUTER:
                return SiddhiCodeBuilderConstants.FULL_OUTER_JOIN;
            default:
                throw new CodeGenerationException("Invalid Join Type: " + joinType);
        }
    }

    /**
     * Generate's the Siddhi code representation of a PatternSequenceConfig object
     *
     * @param patternSequence The PatternSequenceConfig object
     * @return The Siddhi code representation of the given PatternSequenceConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    private static String generatePatternSequenceInput(PatternSequenceConfig patternSequence)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(patternSequence);

        StringBuilder patternSequenceInputStringBuilder = new StringBuilder();
        patternSequenceInputStringBuilder.append(SiddhiCodeBuilderConstants.FROM)
                .append(SiddhiCodeBuilderConstants.SPACE);

        String logic = patternSequence.getLogic();
        for (PatternSequenceConditionConfig condition : patternSequence.getConditionList()) {
            if (logic.contains(condition.getConditionId())) {
                Pattern pattern = Pattern.compile("not\\s+" + condition.getConditionId());
                Matcher matcher = pattern.matcher(logic);
                if (matcher.find()) {
                    logic = logic.replace(condition.getConditionId(),
                            generatePatternSequenceConditionLogic(condition, true));
                } else {
                    logic = logic.replace(condition.getConditionId(),
                            generatePatternSequenceConditionLogic(condition, false));
                }
            }
        }

        patternSequenceInputStringBuilder.append(logic);
        return patternSequenceInputStringBuilder.toString();
    }

    /**
     * Generate's the Siddhi code representation of a PatternSequenceConditionConfig object
     *
     * @param condition The PatternSequenceConditionConfig objecy
     * @param hasNot    Flag to show whether the given PatternSequenceConditionConfig has a 'not' keyword
     * @return The Siddhi code representation of the given PatternSequenceConditionConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    private static String generatePatternSequenceConditionLogic(PatternSequenceConditionConfig condition,
                                                                boolean hasNot)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(condition);

        StringBuilder patternSequenceConditionStringBuilder = new StringBuilder();

        if (!hasNot) {
            patternSequenceConditionStringBuilder.append(condition.getConditionId())
                    .append(SiddhiCodeBuilderConstants.EQUAL);
        }
        patternSequenceConditionStringBuilder.append(condition.getStreamName())
                .append(SubElementCodeGenerator.generateStreamHandlerList(condition.getStreamHandlerList()));

        return patternSequenceConditionStringBuilder.toString();
    }

    private QueryInputCodeGenerator() {
    }

}
