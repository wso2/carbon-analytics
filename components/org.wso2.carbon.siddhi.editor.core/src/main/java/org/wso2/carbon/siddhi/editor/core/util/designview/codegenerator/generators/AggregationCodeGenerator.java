package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiStringBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorHelper;

import java.util.List;

public class AggregationCodeGenerator {

    /**
     * Generates a aggregation definition string from a AggregationConfig object
     *
     * @param aggregation The AggregationConfig object to be converted
     * @return The converted aggregation definition string
     * @throws CodeGenerationException Error while generating code
     */
    private String generateAggregationCode(AggregationConfig aggregation) throws CodeGenerationException {
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
                .append(getAggregationAnnotations(aggregation.getAnnotationList()))
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
     * Generates a string representation of a list of annotations for an aggregation definition
     *
     * @param annotations The list of annotations to be converted
     * @return The string representation of the annotations for an aggregation defintion
     */
    private String getAggregationAnnotations(List<String> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return SiddhiStringBuilderConstants.EMPTY_STRING;
        }

        StringBuilder annotationsStringBuilder = new StringBuilder();
        for (String annotation : annotations) {
            // The reason why generating annotations for aggregations is in a different
            // method is because the '@PrimaryKey' annotation is automatically generated
            // in Siddhi runtime for Aggregation Definitions. This is done to avoid that.
            if (annotation.toUpperCase().contains(CodeGeneratorConstants.PRIMARY_KEY_ANNOTATION)) {
                continue;
            }
            annotationsStringBuilder.append(annotation);
        }

        return annotationsStringBuilder.toString();
    }
}
