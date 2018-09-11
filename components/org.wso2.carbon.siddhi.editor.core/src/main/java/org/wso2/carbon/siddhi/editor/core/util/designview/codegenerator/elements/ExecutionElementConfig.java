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

package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.elements;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConditionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Store's information of an execution element bean class,
 * which is either of type QueryConfig or PartitionConfig.
 */
public class ExecutionElementConfig {

    private String type;
    private Object value;
    private List<String> inputStreams;
    private List<String> outputStreams;

    public ExecutionElementConfig(QueryConfig query) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(query);
        this.type = CodeGeneratorConstants.QUERY;
        this.value = query;
        this.inputStreams = extractInputStreams(query);
        this.outputStreams = extractOutputStreams(query);
    }

    public ExecutionElementConfig(PartitionConfig partition) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(partition);
        this.type = CodeGeneratorConstants.PARTITION;
        this.value = partition;
        this.inputStreams = extractInputStreams(partition);
        this.outputStreams = extractOutputStreams(partition);
    }

    /**
     * Identifies the names of the input streams of a query
     *
     * @param query The query object to be used
     * @return The list of names of the given query's input streams
     * @throws CodeGenerationException Error when identifying input streams
     */
    private List<String> extractInputStreams(QueryConfig query) throws CodeGenerationException {
        List<String> inputStreamList = new LinkedList<>();
        switch (query.getQueryInput().getType().toUpperCase()) {
            case CodeGeneratorConstants.WINDOW:
            case CodeGeneratorConstants.FILTER:
            case CodeGeneratorConstants.PROJECTION:
            case CodeGeneratorConstants.FUNCTION:
                WindowFilterProjectionConfig windowFilterProjection =
                        (WindowFilterProjectionConfig) query.getQueryInput();
                CodeGeneratorUtils.NullValidator.validateConfigObject(windowFilterProjection);
                inputStreamList.add(windowFilterProjection.getFrom());
                break;
            case CodeGeneratorConstants.JOIN:
                JoinConfig join = (JoinConfig) query.getQueryInput();
                CodeGeneratorUtils.NullValidator.validateConfigObject(join);
                inputStreamList.add(join.getLeft().getFrom());
                inputStreamList.add(join.getRight().getFrom());
                break;
            case CodeGeneratorConstants.PATTERN:
            case CodeGeneratorConstants.SEQUENCE:
                PatternSequenceConfig patternSequence = (PatternSequenceConfig) query.getQueryInput();
                CodeGeneratorUtils.NullValidator.validateConfigObject(patternSequence);
                for (PatternSequenceConditionConfig condition : patternSequence.getConditionList()) {
                    if (!inputStreamList.contains(condition.getStreamName())) {
                        inputStreamList.add(condition.getStreamName());
                    }
                }
                break;
            default:
                throw new CodeGenerationException("Unidentified Query Input Type: "
                        + query.getQueryInput().getType());
        }
        return inputStreamList;
    }

    /**
     * Identifies the names of the input streams of a partition
     *
     * @param partition The partition object to be used
     * @return The list of names of the given partition's input streams
     * @throws CodeGenerationException Error when identifying input streams
     */
    private List<String> extractInputStreams(PartitionConfig partition) throws CodeGenerationException {
        List<String> inputStreamList = new LinkedList<>();
        for (List<QueryConfig> queryList : partition.getQueryLists().values()) {
            for (QueryConfig query : queryList) {
                List<String> queryInputStreams = extractInputStreams(query);
                for (String inputStreamName : queryInputStreams) {
                    if (!inputStreamList.contains(inputStreamName) &&
                            !inputStreamName.substring(0, 1).equals("#")) {
                        inputStreamList.add(inputStreamName);
                    }
                }
            }
        }
        return inputStreamList;
    }

    /**
     * Identifies the name of the output stream of a query
     *
     * @param query The query object to be used
     * @return The name of the given query's output stream
     * @throws CodeGenerationException Error when identifying the output stream
     */
    private List<String> extractOutputStreams(QueryConfig query) throws CodeGenerationException {
        CodeGeneratorUtils.NullValidator.validateConfigObject(query.getQueryOutput());
        List<String> outputStreamList = new LinkedList<>();
        outputStreamList.add(query.getQueryOutput().getTarget());
        return outputStreamList;
    }

    /**
     * Identifies the names of the output streams of a partition
     *
     * @param partition The partition object to be used
     * @return The list of names of the given partition's output streams
     * @throws CodeGenerationException Error when identifying the output streams
     */
    private List<String> extractOutputStreams(PartitionConfig partition) throws CodeGenerationException {
        List<String> outputStreamList = new LinkedList<>();
        for (List<QueryConfig> queryList : partition.getQueryLists().values()) {
            for (QueryConfig query : queryList) {
                String outputStream = extractOutputStreams(query).get(0);
                if (!outputStreamList.contains(outputStream) && !outputStream.substring(0, 1).equals("#")) {
                    outputStreamList.add(extractOutputStreams(query).get(0));
                }
            }
        }
        return outputStreamList;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public List<String> getInputStreams() {
        return inputStreams;
    }

    public List<String> getOutputStreams() {
        return outputStreams;
    }

}
