package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.AggregateByTimePeriod;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.aggregationbytimerange.AggregateByTimeInterval;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.aggregationbytimerange.AggregateByTimeRange;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements.QuerySelectCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements.QuerySubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

import java.util.List;

public class AggregationCodeGenerator {

    public String generateAggregation(AggregationConfig aggregation) throws CodeGenerationException {
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
        aggregationStringBuilder.append(SubElementCodeGenerator.generateStore(aggregation.getStore()))
                .append(generateAggregationAnnotations(aggregation.getAnnotationList()))
                .append(SiddhiCodeBuilderConstants.DEFINE_AGGREGATION)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(aggregation.getName())
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(SiddhiCodeBuilderConstants.FROM)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(aggregation.getFrom())
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(QuerySelectCodeGenerator.generateQuerySelect(aggregation.getSelect()))
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(QuerySubElementCodeGenerator.generateQueryGroupBy(aggregation.getGroupBy()))
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(SiddhiCodeBuilderConstants.AGGREGATE);

        if (aggregation.getAggregateByAttribute() != null && !aggregation.getAggregateByAttribute().isEmpty()) {
            aggregationStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(SiddhiCodeBuilderConstants.BY)
                    .append(SiddhiCodeBuilderConstants.SPACE)
                    .append(aggregation.getAggregateByAttribute());
        }

        aggregationStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                .append(SiddhiCodeBuilderConstants.EVERY)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(generateAggregateByTimePeriod(aggregation.getAggregateByTimePeriod()))
                .append(SiddhiCodeBuilderConstants.SEMI_COLON);

        return aggregationStringBuilder.toString();
    }

    /**
     * Generates a string representation of a list of annotations for an aggregation definition
     *
     * @param annotations The list of annotations to be converted
     * @return The string representation of the annotations for an aggregation defintion
     */
    private String generateAggregationAnnotations(List<String> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
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

    /**
     * Generate's a Siddhi string representation of a aggregation's AggregateByTimePeriod object
     *
     * @param aggregateByTimePeriod The AggregateByTimePeriod object to be converted
     * @return The Siddhi string representation of the given AggregateByTimePeriod object
     * @throws CodeGenerationException Error while generating the code
     */
    private String generateAggregateByTimePeriod(AggregateByTimePeriod aggregateByTimePeriod)
            throws CodeGenerationException {
        if (aggregateByTimePeriod == null) {
            throw new CodeGenerationException("A given aggregateByTimePeriod element is empty");
        } else if (aggregateByTimePeriod.getType() == null || aggregateByTimePeriod.getType().isEmpty()) {
            throw new CodeGenerationException("The 'type' value of a given aggregateByTimePeriod element is empty");
        }

        StringBuilder aggregateByTimePeriodStringBuilder = new StringBuilder();
        switch (aggregateByTimePeriod.getType().toUpperCase()) {
            case CodeGeneratorConstants.RANGE:
                AggregateByTimeRange aggregateByTimeRange = (AggregateByTimeRange) aggregateByTimePeriod;
                if (aggregateByTimeRange.getValue() == null) {
                    throw new CodeGenerationException("The 'value' attribute of a given aggregateByTimeRange" +
                            " element is empty");
                } else if (aggregateByTimeRange.getValue().getMin() == null ||
                        aggregateByTimeRange.getValue().getMin().isEmpty()) {
                    throw new CodeGenerationException("The 'min' value of a given" +
                            " aggregateByTimeRange element is empty");
                } else if (aggregateByTimeRange.getValue().getMax() == null ||
                        aggregateByTimeRange.getValue().getMax().isEmpty()) {
                    throw new CodeGenerationException("The 'max' value of a given" +
                            " aggregateByTimeRange element is empty");
                }
                aggregateByTimePeriodStringBuilder.append(aggregateByTimeRange.getValue().getMin().toLowerCase())
                        .append(SiddhiCodeBuilderConstants.THREE_DOTS)
                        .append(aggregateByTimeRange.getValue().getMax().toLowerCase());
                break;
            case CodeGeneratorConstants.INTERVAL:
                AggregateByTimeInterval aggregateByTimeInterval = (AggregateByTimeInterval) aggregateByTimePeriod;
                if (aggregateByTimeInterval.getValue() == null || aggregateByTimeInterval.getValue().isEmpty()) {
                    throw new CodeGenerationException("The 'value' attribute of a given" +
                            " attributeByTimeInterval element is empty");
                }
                int timeIntervalsLeft = aggregateByTimeInterval.getValue().size();
                for (String timeInterval : aggregateByTimeInterval.getValue()) {
                    aggregateByTimePeriodStringBuilder.append(timeInterval.toLowerCase());
                    if (timeIntervalsLeft != 1) {
                        aggregateByTimePeriodStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
                    }
                    timeIntervalsLeft--;
                }
                break;
            default:
                throw new CodeGenerationException("Unidentified aggregateByTimePeriod element type: "
                        + aggregateByTimePeriod.getType());
        }

        return aggregateByTimePeriodStringBuilder.toString();
    }

}
