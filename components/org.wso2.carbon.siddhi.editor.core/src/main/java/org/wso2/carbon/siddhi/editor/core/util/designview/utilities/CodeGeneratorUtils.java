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

package org.wso2.carbon.siddhi.editor.core.util.designview.utilities;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TableConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.WindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConditionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.elements.ExecutionElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Helper that contains generic reusable utility methods
 * for the CodeGenerator class to build a Siddhi app string,
 * mainly to generate strings for sub-elements of a SiddhiAppConfig object
 */
public class CodeGeneratorUtils {

    /**
     * Obtains a list of queries are reordered in a way that is valid in siddhi.
     * This is done because the query list may come in an invalid order that cannot be compiled by the Siddhi
     * runtime.
     *
     * @param queries The queries to be ordered
     * @return The list of given queries in a valid order
     * @throws CodeGenerationException Error While Reordering Queries
     */
    public static List<QueryConfig> reorderQueries(List<QueryConfig> queries, List<String> definitionNames)
            throws CodeGenerationException {
        if (queries == null) {
            throw new CodeGenerationException("A given list of queries for a partition is empty");
        }
        Set<String> existingInputs = new HashSet<>(definitionNames);
        List<QueryConfig> reorderedQueries = new LinkedList<>();
        while (!queries.isEmpty()) {
            Iterator<QueryConfig> queryIterator = queries.iterator();
            while (queryIterator.hasNext()) {
                QueryConfig query = queryIterator.next();
                List<String> queryInputStreams = getInputStreams(query);
                queryInputStreams.removeAll(existingInputs);
                if (queryInputStreams.isEmpty()) {
                    reorderedQueries.add(query);
                    queryIterator.remove();
                    existingInputs.add(query.getQueryOutput().getTarget());
                }
            }
        }
        return reorderedQueries;
    }

    /**
     * Obtains a list of input streams of a given QueryConfig object
     *
     * @param query The given QueryConfig object
     * @return The input streams in the given QueryConfig object
     * @throws CodeGenerationException Error while trying to get the input streams
     */
    private static List<String> getInputStreams(QueryConfig query)
            throws CodeGenerationException {
        if (query == null) {
            throw new CodeGenerationException("A given query element is empty");
        } else if (query.getQueryInput() == null) {
            throw new CodeGenerationException("The query input of a given query element is empty");
        } else if (query.getQueryInput().getType() == null || query.getQueryInput().getType().isEmpty()) {
            throw new CodeGenerationException("The 'type' value of a given query input element is empty");
        }

        List<String> inputStreamList = new LinkedList<>();
        switch (query.getQueryInput().getType().toUpperCase()) {
            case CodeGeneratorConstants.WINDOW:
            case CodeGeneratorConstants.FILTER:
            case CodeGeneratorConstants.PROJECTION:
            case CodeGeneratorConstants.FUNCTION:
                WindowFilterProjectionConfig windowFilterProjection =
                        (WindowFilterProjectionConfig) query.getQueryInput();
                if (windowFilterProjection.getFrom() == null || windowFilterProjection.getFrom().isEmpty()) {
                    throw new CodeGenerationException("The 'from' value of a given" +
                            " window/filter/projection query input is empty");
                }
                inputStreamList.add(windowFilterProjection.getFrom());
                break;
            case CodeGeneratorConstants.JOIN:
                JoinConfig join = (JoinConfig) query.getQueryInput();
                if (join.getLeft() == null) {
                    throw new CodeGenerationException("The join left element of a given join query is empty");
                } else if (join.getRight() == null) {
                    throw new CodeGenerationException("The join right element of a given join query is empty");
                } else if (join.getLeft().getFrom() == null || join.getLeft().getFrom().isEmpty()) {
                    throw new CodeGenerationException("The left element 'from' value of a given" +
                            " join query is empty");
                } else if (join.getRight().getFrom() == null || join.getRight().getFrom().isEmpty()) {
                    throw new CodeGenerationException("The right element 'from' value of a" +
                            " given join query is empty");
                }

                inputStreamList.add(join.getLeft().getFrom());
                inputStreamList.add(join.getRight().getFrom());
                break;
            case CodeGeneratorConstants.PATTERN:
            case CodeGeneratorConstants.SEQUENCE:
                PatternSequenceConfig patternSequence = (PatternSequenceConfig) query.getQueryInput();
                if (patternSequence.getConditionList() == null || patternSequence.getConditionList().isEmpty()) {
                    throw new CodeGenerationException("The condition list of a given " +
                            "pattern/sequence query is empty");
                }
                for (PatternSequenceConditionConfig condition : patternSequence.getConditionList()) {
                    if (condition == null) {
                        throw new CodeGenerationException("The condition value of a given" +
                                " pattern/sequence condition list is empty");
                    } else if (condition.getStreamName() == null || condition.getStreamName().isEmpty()) {
                        throw new CodeGenerationException("The condition stream name of the given" +
                                " pattern/sequence query condition is empty");
                    }

                    inputStreamList.add(condition.getStreamName());
                }
                break;
            default:
                throw new CodeGenerationException("Unidentified query type: " + query.getQueryInput().getType());
        }

        return inputStreamList;
    }

    /**
     * Gets a list of StreamConfig objects and identifies the streams that does not need to be generated,
     * and outputs the ones that do need to be generated.
     *
     * @param streamList The list of streams in the Siddhi app
     * @param sourceList The list of sources in the Siddhi app
     * @param sinkList   The list of sinks in the Siddhi app
     * @param queryList  The list of queries in the Siddhi app
     * @return The list of streams that are to generated
     */
    public static List<StreamConfig> getStreamsToBeGenerated(List<StreamConfig> streamList,
                                                             List<SourceSinkConfig> sourceList,
                                                             List<SourceSinkConfig> sinkList,
                                                             List<QueryConfig> queryList) {
        List<StreamConfig> definedStreams = new ArrayList<>();
        for (StreamConfig stream : streamList) {
            // Check For Annotations
            if (stream.getAnnotationList() != null && !stream.getAnnotationList().isEmpty()) {
                definedStreams.add(stream);
                continue;
            }
            // Check For Sources/Sinks
            boolean hasSourceSink = false;
            List<SourceSinkConfig> sourceSinkList = new ArrayList<>();
            sourceSinkList.addAll(sourceList);
            sourceSinkList.addAll(sinkList);
            for (SourceSinkConfig source : sourceSinkList) {
                if (stream.getName().equals(source.getConnectedElementName())) {
                    hasSourceSink = true;
                    break;
                }
            }
            if (hasSourceSink) {
                definedStreams.add(stream);
                continue;
            }
            // Check For Query Output
            boolean isQueryOutput = false;
            for (QueryConfig query : queryList) {
                if (query.getQueryOutput().getTarget().equals(stream.getName())) {
                    isQueryOutput = true;
                    break;
                }
            }
            if (!isQueryOutput) {
                definedStreams.add(stream);
            }
        }

        return definedStreams;
    }

    /**
     * Gets the names of all the definition elements of a Siddhi app.
     *
     * @param streams      The list of streams in a Siddhi app
     * @param tables       The list of tables in a Siddhi app
     * @param windows      The list of windows in a Siddhi app
     * @param triggers     The list of triggers in a Siddhi app
     * @param aggregations The list of aggregations in a Siddhi app
     * @return The names of all the definitions in a Siddhi app
     */
    public static List<String> getDefinitionNames(List<StreamConfig> streams, List<TableConfig> tables,
                                                  List<WindowConfig> windows, List<TriggerConfig> triggers,
                                                  List<AggregationConfig> aggregations, List<PartitionConfig> partitions) {
        List<String> definitionNames = new LinkedList<>();
        for (StreamConfig stream : streams) {
            definitionNames.add(stream.getName());
        }
        for (TableConfig table : tables) {
            definitionNames.add(table.getName());
        }
        for (WindowConfig window : windows) {
            definitionNames.add(window.getName());
        }
        for (TriggerConfig trigger : triggers) {
            definitionNames.add(trigger.getName());
        }
        for (AggregationConfig aggregation : aggregations) {
            definitionNames.add(aggregation.getName());
        }
        for (PartitionConfig partition : partitions) {
            for (StreamConfig stream : partition.getStreamList()) {
                definitionNames.add(stream.getName());
            }
        }
        return definitionNames;
    }

    public static List<ExecutionElementConfig> convertToExecutionElements(List<QueryConfig> queries,
                                                                          List<PartitionConfig> partitions)
            throws CodeGenerationException {
        List<ExecutionElementConfig> executionElements = new LinkedList<>();
        for (QueryConfig query : queries) {
            executionElements.add(new ExecutionElementConfig(query));
        }
        for (PartitionConfig partition : partitions) {
            executionElements.add(new ExecutionElementConfig(partition));
        }
        return executionElements;
    }

    public static List<ExecutionElementConfig> reorderExecutionElements(List<ExecutionElementConfig> executionElements, List<String> definitionNames)
            throws CodeGenerationException {
        if (executionElements == null) {
            throw new CodeGenerationException("The given list of execution elements is empty");
        }
        Set<String> existingInputs = new HashSet<>(definitionNames);
        List<ExecutionElementConfig> reorderedExecutionElements = new LinkedList<>();
        while (!executionElements.isEmpty()) {
            Iterator<ExecutionElementConfig> executionElementIterator = executionElements.iterator();
            while (executionElementIterator.hasNext()) {
                ExecutionElementConfig executionElement = executionElementIterator.next();
                List<String> executionElementInputStreams = executionElement.getInputStreams();
                executionElementInputStreams.removeAll(existingInputs);
                if (executionElementInputStreams.isEmpty()) {
                    reorderedExecutionElements.add(executionElement);
                    executionElementIterator.remove();
                    existingInputs.addAll(executionElement.getOutputStreams());
                }
            }
        }
        return reorderedExecutionElements;
    }

    private CodeGeneratorUtils() {
    }

}
