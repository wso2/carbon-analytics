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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryOrderByConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.SubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

import java.util.List;

/**
 * Generate's the code for sub-elements of a Siddhi query
 */
public class QuerySubElementCodeGenerator {

    /**
     * Generate's the Siddhi code representation of a query's group by list
     *
     * @param groupByList The group by list given
     * @return The Siddhi code representation of the given query's group by list
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
     * Generate's the Siddhi code representation of a query's order by list
     *
     * @param orderByList The order by list given
     * @return The Siddhi code representation of the given query's order by list
     * @throws CodeGenerationException Error when generating the code
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
            CodeGeneratorUtils.NullValidator.validateConfigObject(orderByAttribute);

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
     * Generate's the Siddhi code representation of a query's limit
     *
     * @param limit The limit value given
     * @return The Siddhi code representation of the given query's limit value
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
     * Generate's the Siddhi code representation of a query's having
     *
     * @param having The having value given
     * @return The Siddhi code representation of the given query's having value
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
     * Generate's the Siddhi code representation of a query's output rate limit
     *
     * @param outputRateLimit The output rate limit value given
     * @return The Siddhi code representation of the given query's output rate limit
     */
    public static String generateQueryOutputRateLimit(String outputRateLimit) {
        if (outputRateLimit == null || outputRateLimit.isEmpty()) {
            return SiddhiCodeBuilderConstants.EMPTY_STRING;
        }

        return SiddhiCodeBuilderConstants.OUTPUT +
                SiddhiCodeBuilderConstants.SPACE +
                outputRateLimit;
    }

    private QuerySubElementCodeGenerator() {
    }

}
