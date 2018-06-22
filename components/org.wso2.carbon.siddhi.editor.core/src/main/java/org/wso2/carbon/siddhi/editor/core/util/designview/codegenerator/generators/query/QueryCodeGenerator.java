package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.SubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements.QueryInputCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements.QueryOutputCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements.QuerySelectCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements.QuerySubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

public class QueryCodeGenerator {

    public String generateQuery(QueryConfig query) throws CodeGenerationException {
        if (query == null) {
            throw new CodeGenerationException("A given query element is empty");
        }

        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append(SubElementCodeGenerator.generateAnnotations(query.getAnnotationList()))
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
