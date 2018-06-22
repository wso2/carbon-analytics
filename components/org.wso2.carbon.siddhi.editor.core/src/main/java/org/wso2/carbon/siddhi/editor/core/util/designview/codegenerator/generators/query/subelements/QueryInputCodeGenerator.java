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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryInputCodeGenerator {

    /**
     * Generates a string representation of a query input using the given QueryInputConfig object
     *
     * @param queryInput The QueryInputConfig object to be converted
     * @return The string representation of the given QueryInputConfig object
     * @throws CodeGenerationException Error while generating code
     */
    public static String generateQueryInput(QueryInputConfig queryInput) throws CodeGenerationException {
        if (queryInput == null) {
            throw new CodeGenerationException("A given query input element is empty");
        } else if (queryInput.getType() == null || queryInput.getType().isEmpty()) {
            throw new CodeGenerationException("The 'type' value of a given query input element is empty");
        }

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
     * Generates a Siddhi string representation of the given WindowFilterProjectionConfig object
     *
     * @param windowFilterProjection The WindowFilterProjectCofig to be converted
     * @return The string representation of the given WindowFilterProjectionConfig object
     * @throws CodeGenerationException Error while generating code
     */
    private static String generateWindowFilterProjectionQueryInput(WindowFilterProjectionConfig windowFilterProjection)
            throws CodeGenerationException {
        if (windowFilterProjection == null) {
            throw new CodeGenerationException("A given window/filter/project element is empty");
        } else if (windowFilterProjection.getFrom() == null || windowFilterProjection.getFrom().isEmpty()) {
            throw new CodeGenerationException("The 'from' value of a given window/filter/project element is empty");
        }

        return SiddhiCodeBuilderConstants.FROM +
                SiddhiCodeBuilderConstants.SPACE +
                windowFilterProjection.getFrom() +
                SubElementCodeGenerator.generateStreamHandlerList(windowFilterProjection.getStreamHandlerList());
    }

    /**
     * Generates a Siddhi string representation of a JoinConfig object
     *
     * @param join The JoinConfig object to be converted
     * @return The string representation of the given JoinConfig object
     * @throws CodeGenerationException Error while generating code
     */
    private static String generateJoinQueryInput(JoinConfig join) throws CodeGenerationException {
        if (join == null) {
            throw new CodeGenerationException("A given join query is empty");
        } else if (join.getJoinWith() == null || join.getJoinType().isEmpty()) {
            throw new CodeGenerationException("The 'joinWith' value of a given join query is empty");
        } else if (join.getJoinType() == null || join.getJoinType().isEmpty()) {
            throw new CodeGenerationException("The 'joinType' value of a given join query is empty");
        } else if (join.getLeft() == null || join.getRight() == null) {
            throw new CodeGenerationException("The left/right join element for a given join query is empty");
        } else if (join.getLeft().getType() == null || join.getLeft().getType().isEmpty()) {
            throw new CodeGenerationException("The 'type' value of the left join element" +
                    " of a given join query is empty");
        } else if (join.getRight().getType() == null || join.getRight().getType().isEmpty()) {
            throw new CodeGenerationException("The 'type' value of the right join element" +
                    " of a given join query is empty");
        }

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
     * Generates a string representation of the given JoinElementConfig object
     *
     * @param joinElement The JoinElementConfig object to be converted
     * @return The string representation of the given JoinElementConfig object
     * @throws CodeGenerationException Error while generating code
     */
    private static String generateJoinElement(JoinElementConfig joinElement) throws CodeGenerationException {
        if (joinElement == null) {
            throw new CodeGenerationException("A given join element is empty");
        } else if (joinElement.getFrom() == null || joinElement.getFrom().isEmpty()) {
            throw new CodeGenerationException("The 'from' value of a given join element is empty");
        }

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
     * Generates a 'Siddhi' string representation of the given joinType string
     *
     * @param joinType The join type as a string
     * @return The Siddhi string representation of the given join type
     * @throws CodeGenerationException Error while generating code
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
     * Generates a Siddhi string representation of a given PatternSequenceConfig object
     *
     * @param patternSequence The PatterSequenceConfig object to be converted
     * @return The string representation of the given PatternSequenceConfig object
     * @throws CodeGenerationException Error while generating code
     */
    private static String generatePatternSequenceInput(PatternSequenceConfig patternSequence)
            throws CodeGenerationException {
        if (patternSequence == null) {
            throw new CodeGenerationException("A given pattern/sequence query is empty");
        } else if (patternSequence.getLogic() == null || patternSequence.getLogic().isEmpty()) {
            throw new CodeGenerationException("The 'logic' value for a given pattern/sequence query is empty");
        } else if (patternSequence.getConditionList() == null || patternSequence.getConditionList().isEmpty()) {
            throw new CodeGenerationException("The condition list for a given pattern/sequence query is empty");
        }

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
     * Generates a Siddhi string representation of a given PatternSequenceConditionConfig object
     *
     * @param condition The PatternSequenceConditionConfig object to be converted
     * @return The Siddhi string representation of the given PatternSequenceConfig object
     * @throws CodeGenerationException Error while generating code
     */
    private static String generatePatternSequenceConditionLogic(PatternSequenceConditionConfig condition, boolean hasNot)
            throws CodeGenerationException {
        if (condition == null) {
            throw new CodeGenerationException("A given pattern/sequence query condition is empty");
        } else if (condition.getStreamName() == null || condition.getStreamName().isEmpty()) {
            throw new CodeGenerationException("The stream name of a given pattern/sequence query condition is empty");
        }

        StringBuilder patternSequenceConditionStringBuilder = new StringBuilder();

        if (!hasNot) {
            patternSequenceConditionStringBuilder.append(condition.getConditionId())
                    .append(SiddhiCodeBuilderConstants.EQUAL);
        }
        patternSequenceConditionStringBuilder.append(condition.getStreamName())
                .append(SubElementCodeGenerator.generateStreamHandlerList(condition.getStreamHandlerList()));

        return patternSequenceConditionStringBuilder.toString();
    }

}
