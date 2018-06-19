package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiStringBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorHelper;

import java.util.LinkedList;
import java.util.List;

public class PartitionCodeGenerator {

    /**
     * Generates a partition definition string from a PartitionConfig object
     *
     * @param partition The PartitionConfig object to be converted
     * @return The converted partition definition string
     * @throws CodeGenerationException Error while generating code
     */
    public String generatePartitionCode(PartitionConfig partition, List<String> definitionNames)
            throws CodeGenerationException {
        if (partition == null) {
            throw new CodeGenerationException("A given partition object is empty");
        } else if (partition.getPartitionWith() == null || partition.getPartitionWith().isEmpty()) {
            throw new CodeGenerationException("The 'partitionWith' value of a given partition element is empty");
        } else if (partition.getQueryLists() == null || partition.getQueryLists().isEmpty()) {
            throw new CodeGenerationException("The query lists of a given partition element is empty");
        }

        StringBuilder partitionStringBuilder = new StringBuilder();

        partitionStringBuilder.append(CodeGeneratorHelper.getAnnotations(partition.getAnnotationList()))
                .append(SiddhiStringBuilderConstants.PARTITION_WITH)
                .append(SiddhiStringBuilderConstants.OPEN_BRACKET)
                .append(CodeGeneratorHelper.getPartitionWith(partition.getPartitionWith()))
                .append(SiddhiStringBuilderConstants.CLOSE_BRACKET)
                .append(SiddhiStringBuilderConstants.SPACE)
                .append(SiddhiStringBuilderConstants.BEGIN)
                .append(SiddhiStringBuilderConstants.SPACE);

        List<QueryConfig> queries = new LinkedList<>();
        for (List<QueryConfig> queryList : partition.getQueryLists().values()) {
            queries.addAll(queryList);
        }

        if (!queries.isEmpty()) {
            QueryCodeGenerator queryCodeGenerator = new QueryCodeGenerator();
            for (QueryConfig query : CodeGeneratorHelper.reorderQueries(queries, definitionNames)) {
                partitionStringBuilder.append(queryCodeGenerator.generateQueryCode(query))
                        .append(SiddhiStringBuilderConstants.NEW_LINE);
            }
        }

        partitionStringBuilder.append(SiddhiStringBuilderConstants.END)
                .append(SiddhiStringBuilderConstants.SEMI_COLON)
                .append(SiddhiStringBuilderConstants.NEW_LINE);

        return partitionStringBuilder.toString();
    }

}
