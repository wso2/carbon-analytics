package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryOrderByConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.SubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

import java.util.List;

public class QuerySubElementCodeGenerator {

    /**
     * Generates a Siddhi string representation of a group by list of a query
     *
     * @param groupByList The list of group by attributes to be converted
     * @return The Siddhi string representation of the given group by list
     */
    public static String generateQueryGroupBy(List<String> groupByList) {
        if (groupByList == null || groupByList.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        return SiddhiCodeBuilderConstants.GROUP +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.BY +
                SiddhiCodeBuilderConstants.SPACE +
                SubElementCodeGenerator.generateParameterList(groupByList);
    }

    /**
     * Generates a Siddhi string representation of a order by list of a query
     *
     * @param orderByList The order by list to be converted
     * @return The Siddhi string representation of the given order by config list
     * @throws CodeGenerationException Error while generating code
     */
    public static String generateQueryOrderBy(List<QueryOrderByConfig> orderByList) throws CodeGenerationException {
        if (orderByList == null || orderByList.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        StringBuilder orderByListStringBuilder = new StringBuilder();
        orderByListStringBuilder.append(SiddhiCodeBuilderConstants.ORDER)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(SiddhiCodeBuilderConstants.BY)
                .append(SiddhiCodeBuilderConstants.SPACE);

        int orderByAttributesLeft = orderByList.size();
        for (QueryOrderByConfig orderByAttribute : orderByList) {
            if (orderByAttribute == null) {
                throw new CodeGenerationException("A given query 'order by' value is empty");
            } else if (orderByAttribute.getValue() == null || orderByAttribute.getValue().isEmpty()) {
                throw new CodeGenerationException("The 'value' attribute for a given query order by element is empty");
            }

            orderByListStringBuilder.append(orderByAttribute.getValue());
            if (orderByAttribute.getOrder() != null && !orderByAttribute.getOrder().isEmpty()) {
                orderByListStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                        .append(orderByAttribute.getOrder());
            }

            if (orderByAttributesLeft != 1) {
                orderByListStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            }
            orderByAttributesLeft--;
        }

        return orderByListStringBuilder.toString();
    }

    /**
     * Generates a Siddhi string representation of the given limit value
     *
     * @param limit The limit value to be converted
     * @return The Siddhi representation of the given query limit
     */
    public static String generateQueryLimit(long limit) {
        if (limit != 0) {
            return SiddhiCodeBuilderConstants.LIMIT +
                    SiddhiCodeBuilderConstants.SPACE +
                    limit;
        }
        return SiddhiCodeBuilderConstants.EMPTY_STRING;
    }

    /**
     * Generates a Siddhi string representation of the given having value
     *
     * @param having The having value given
     * @return The Siddhi string representation of the query 'having' value
     */
    public static String generateQueryHaving(String having) {
        if (having == null || having.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        return SiddhiCodeBuilderConstants.HAVING +
                SiddhiCodeBuilderConstants.SPACE +
                having;
    }

    /**
     * Generates a Siddhi string representation of a given query output rate limit
     *
     * @param outputRateLimit The output rate limit value given
     * @return The Siddhi representation of the given output rate limit value
     */
    public static String generateQueryOutputRateLimit(String outputRateLimit) {
        if (outputRateLimit == null || outputRateLimit.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        return SiddhiCodeBuilderConstants.OUTPUT +
                SiddhiCodeBuilderConstants.SPACE +
                outputRateLimit;
    }

}
