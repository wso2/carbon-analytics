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

package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.SubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements.QueryInputCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements.QueryOutputCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements.QuerySelectCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements.QuerySubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

/**
 * Generate's the code for a Siddhi query element
 */
public class QueryCodeGenerator {

    /**
     * Generate's the Siddhi code representation of a QueryConfig object
     *
     * @param query The QueryConfig objecy
     * @return The Siddhi code representation of the given QueryConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    public String generateQuery(QueryConfig query) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(query);

        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append(SubElementCodeGenerator.generateComment(query.getPreviousCommentSegment()))
                .append(SubElementCodeGenerator.generateAnnotations(query.getAnnotationList()))
                .append(QueryInputCodeGenerator.generateQueryInput(query.getQueryInput()))
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(QuerySelectCodeGenerator.generateQuerySelect(query.getSelect()));

        if (query.getGroupBy() != null && !query.getGroupBy().isEmpty()) {
            queryStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(QuerySubElementCodeGenerator.generateQueryGroupBy(query.getGroupBy()));
        }
        if (query.getHaving() != null && !query.getHaving().isEmpty()) {
            queryStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(QuerySubElementCodeGenerator.generateQueryHaving(query.getHaving()));
        }
        if (query.getOrderBy() != null && !query.getOrderBy().isEmpty()) {
            queryStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(QuerySubElementCodeGenerator.generateQueryOrderBy(query.getOrderBy()));
        }
        if (query.getLimit() != 0) {
            queryStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(QuerySubElementCodeGenerator.generateQueryLimit(query.getLimit()));
        }
        if (query.getOutputRateLimit() != null && !query.getOutputRateLimit().isEmpty()) {
            queryStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                    .append(QuerySubElementCodeGenerator.generateQueryOutputRateLimit(query.getOutputRateLimit()));
        }

        queryStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                .append(QueryOutputCodeGenerator.generateQueryOutput(query.getQueryOutput()));

        return queryStringBuilder.toString();
    }

}
