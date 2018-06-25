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
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

import java.util.List;

/**
 * Generate's the code for a Siddhi aggregation element
 */
public class AggregationCodeGenerator {

    public String generateAggregation(AggregationConfig aggregation) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(aggregation);

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

    private String generateAggregateByTimePeriod(AggregateByTimePeriod aggregateByTimePeriod)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(aggregateByTimePeriod);

        StringBuilder aggregateByTimePeriodStringBuilder = new StringBuilder();
        switch (aggregateByTimePeriod.getType().toUpperCase()) {
            case CodeGeneratorConstants.RANGE:
                aggregateByTimePeriodStringBuilder.append(
                        generateAggregateByTimeRange((AggregateByTimeRange) aggregateByTimePeriod));
                break;
            case CodeGeneratorConstants.INTERVAL:
                aggregateByTimePeriodStringBuilder.append(
                        generateAggregateByTimeInterval((AggregateByTimeInterval) aggregateByTimePeriod));
                break;
            default:
                throw new CodeGenerationException("Unidentified aggregateByTimePeriod element type: "
                        + aggregateByTimePeriod.getType());
        }

        return aggregateByTimePeriodStringBuilder.toString();
    }

    private String generateAggregateByTimeInterval(AggregateByTimeInterval aggregateByTimeInterval) throws CodeGenerationException {
        if (aggregateByTimeInterval.getValue() == null || aggregateByTimeInterval.getValue().isEmpty()) {
            throw new CodeGenerationException("The 'value' attribute of a given" +
                    " attributeByTimeInterval element is empty");
        }
        StringBuilder aggregateByTimeIntervalStringBuilder = new StringBuilder();
        int timeIntervalsLeft = aggregateByTimeInterval.getValue().size();
        for (String timeInterval : aggregateByTimeInterval.getValue()) {
            aggregateByTimeIntervalStringBuilder.append(timeInterval.toLowerCase());
            if (timeIntervalsLeft != 1) {
                aggregateByTimeIntervalStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            }
            timeIntervalsLeft--;
        }
        return aggregateByTimeIntervalStringBuilder.toString();
    }

    private String generateAggregateByTimeRange(AggregateByTimeRange aggregateByTimeRange) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(aggregateByTimeRange);
        StringBuilder aggregateByTimeRangeStringBuilder = new StringBuilder();
        aggregateByTimeRangeStringBuilder.append(aggregateByTimeRange.getValue().getMin().toLowerCase())
                .append(SiddhiCodeBuilderConstants.THREE_DOTS)
                .append(aggregateByTimeRange.getValue().getMax().toLowerCase());
        return aggregateByTimeRangeStringBuilder.toString();
    }

}
