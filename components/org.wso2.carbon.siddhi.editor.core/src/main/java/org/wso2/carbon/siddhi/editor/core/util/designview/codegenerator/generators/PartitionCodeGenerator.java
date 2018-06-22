package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionWithElement;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.QueryCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

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
    public String generatePartition(PartitionConfig partition, List<String> definitionNames)
            throws CodeGenerationException {
        if (partition == null) {
            throw new CodeGenerationException("A given partition object is empty");
        } else if (partition.getPartitionWith() == null || partition.getPartitionWith().isEmpty()) {
            throw new CodeGenerationException("The 'partitionWith' value of a given partition element is empty");
        } else if (partition.getQueryLists() == null || partition.getQueryLists().isEmpty()) {
            throw new CodeGenerationException("The query lists of a given partition element is empty");
        }

        StringBuilder partitionStringBuilder = new StringBuilder();

        partitionStringBuilder.append(SubElementCodeGenerator.generateAnnotations(partition.getAnnotationList()))
                .append(SiddhiCodeBuilderConstants.PARTITION_WITH)
                .append(SiddhiCodeBuilderConstants.OPEN_BRACKET)
                .append(generatePartitionWith(partition.getPartitionWith()))
                .append(SiddhiCodeBuilderConstants.CLOSE_BRACKET)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(SiddhiCodeBuilderConstants.BEGIN)
                .append(SiddhiCodeBuilderConstants.SPACE);

        List<QueryConfig> queries = new LinkedList<>();
        for (List<QueryConfig> queryList : partition.getQueryLists().values()) {
            queries.addAll(queryList);
        }

        if (!queries.isEmpty()) {
            QueryCodeGenerator queryCodeGenerator = new QueryCodeGenerator();
            int queriesLeft = queries.size();
            for (QueryConfig query : CodeGeneratorUtils.reorderQueries(queries, definitionNames)) {
                partitionStringBuilder.append(queryCodeGenerator.generateQuery(query));
                if (queriesLeft != 1) {
                    partitionStringBuilder.append(SiddhiCodeBuilderConstants.NEW_LINE);
                }
                queriesLeft--;
            }
        }

        partitionStringBuilder.append(SiddhiCodeBuilderConstants.END)
                .append(SiddhiCodeBuilderConstants.SEMI_COLON)
                .append(SiddhiCodeBuilderConstants.NEW_LINE);

        return partitionStringBuilder.toString();
    }

    /**
     * Generates a string representation of a list of PartitionWithElement objects for a particular partition
     *
     * @param partitionWith The List of partitionWithElement objects to be converted
     * @return The Siddhi string representation of the PartitionWithElement list given
     * @throws CodeGenerationException Error while generating code
     */
    private String generatePartitionWith(List<PartitionWithElement> partitionWith) throws CodeGenerationException {
        if (partitionWith == null || partitionWith.isEmpty()) {
            throw new CodeGenerationException("A given 'partitionWith' list is empty");
        }

        StringBuilder partitionWithStringBuilder = new StringBuilder();
        int partitionWithElementsLeft = partitionWith.size();
        for (PartitionWithElement partitionWithElement : partitionWith) {
            partitionWithStringBuilder.append(generatePartitionWithElement(partitionWithElement));
            if (partitionWithElementsLeft != 1) {
                partitionWithStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            }
            partitionWithElementsLeft--;
        }

        return partitionWithStringBuilder.toString();
    }

    /**
     * Generates a string representation of a single PartitionWithElement object
     *
     * @param partitionWithElement The PartitionWithElement object to be converted
     * @return The Siddhi string representation of the given PartitionWithElement object
     * @throws CodeGenerationException Error while generating code
     */
    private String generatePartitionWithElement(PartitionWithElement partitionWithElement)
            throws CodeGenerationException {
        if (partitionWithElement == null) {
            throw new CodeGenerationException("A given 'partition with' element is empty");
        } else if (partitionWithElement.getExpression() == null || partitionWithElement.getExpression().isEmpty()) {
            throw new CodeGenerationException("The 'expression' value of a given 'partition with' element is empty");
        } else if (partitionWithElement.getStreamName() == null || partitionWithElement.getStreamName().isEmpty()) {
            throw new CodeGenerationException("The stream name of a given 'partition with' element is empty");
        }

        return partitionWithElement.getExpression() +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.OF +
                SiddhiCodeBuilderConstants.SPACE +
                partitionWithElement.getStreamName();
    }

}
