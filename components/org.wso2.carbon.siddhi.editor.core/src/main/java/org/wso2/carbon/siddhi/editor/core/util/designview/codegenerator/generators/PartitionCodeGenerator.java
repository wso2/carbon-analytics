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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionWithElement;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.QueryCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Generate's the code for a Siddhi partition element
 */
public class PartitionCodeGenerator {

    /**
     * Generate's the Siddhi code representation of a PartitionConfig object
     *
     * @param partition       The PartitionConfig object
     * @param definitionNames The names of all the Siddhi definition objects (including inner streams)
     * @return The Siddhi code representation of the given PartitionConfig object
     * @throws CodeGenerationException Error when generating the code
     */
    public String generatePartition(PartitionConfig partition, List<String> definitionNames)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(partition);

        StringBuilder partitionStringBuilder = new StringBuilder();

        partitionStringBuilder.append(SubElementCodeGenerator.generateComment(partition.getPreviousCommentSegment()))
                .append(SubElementCodeGenerator.generateAnnotations(partition.getAnnotationList()))
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
     * Generate's the Siddhi code representation of a partition's PartitionWithElement list
     *
     * @param partitionWith The PartitionWithElement list
     * @return The Siddhi code representation of the given PartitionWithElement list
     * @throws CodeGenerationException Error when generating the code
     */
    private String generatePartitionWith(List<PartitionWithElement> partitionWith) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(partitionWith);

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
     * Generate's the Siddhi code representation of a PartitionWithElement object
     *
     * @param partitionWithElement The PartitionWithElement object
     * @return The Siddhi code representation of the given PartitionWithElement object
     * @throws CodeGenerationException Error when generating the code
     */
    private String generatePartitionWithElement(PartitionWithElement partitionWithElement)
            throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(partitionWithElement);

        return partitionWithElement.getExpression() +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.OF +
                SiddhiCodeBuilderConstants.SPACE +
                partitionWithElement.getStreamName();
    }

}
