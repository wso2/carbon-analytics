package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiStringBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorHelper;

public class QueryCodeGenerator {

    /**
     * Generates a query definition string from a QueryConfig object
     *
     * @param query The QueryConfig object to be converted
     * @return The converted query definition string
     * @throws CodeGenerationException Error while generating code
     */
    public String generateQueryCode(QueryConfig query) throws CodeGenerationException {
        if (query == null) {
            throw new CodeGenerationException("A given query element is empty");
        }

        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append(CodeGeneratorHelper.getAnnotations(query.getAnnotationList()))
                .append(CodeGeneratorHelper.getQueryInput(query.getQueryInput()))
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(CodeGeneratorHelper.getQuerySelect(query.getSelect()));

        if (query.getGroupBy() != null && !query.getGroupBy().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getQueryGroupBy(query.getGroupBy()));
        }
        if (query.getHaving() != null && !query.getHaving().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getQueryHaving(query.getHaving()));
        }
        if (query.getOrderBy() != null && !query.getOrderBy().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getQueryOrderBy(query.getOrderBy()));
        }
        if (query.getLimit() != 0) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getQueryLimit(query.getLimit()));
        }
        if (query.getOutputRateLimit() != null && !query.getOutputRateLimit().isEmpty()) {
            queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                    .append(CodeGeneratorHelper.getQueryOutputRateLimit(query.getOutputRateLimit()));
        }

        queryStringBuilder.append(SiddhiStringBuilderConstants.SPACE)
                .append(CodeGeneratorHelper.getQueryOutput(query.getQueryOutput()));

        return queryStringBuilder.toString();
    }

}
