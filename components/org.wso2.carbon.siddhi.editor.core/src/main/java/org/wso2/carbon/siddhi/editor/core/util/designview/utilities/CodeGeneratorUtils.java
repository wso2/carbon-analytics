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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.AttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.FunctionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StoreConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TableConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.WindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.AggregateByTimePeriod;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.aggregationbytimerange.AggregateByTimeInterval;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.aggregationbytimerange.AggregateByTimeRange;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.SelectedAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionWithElement;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryOrderByConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConditionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.DeleteOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.InsertOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.UpdateInsertIntoOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.setattribute.SetAttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.MapperConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperListPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperMapPayloadOrAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.mapper.attribute.MapperPayloadOrAttribute;
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
 * Utility methods for the CodeGenerator class
 */
public class CodeGeneratorUtils {

    /**
     * Reorders a list of QueryConfig objects to the right order that it should be in displayed in
     * when converting to a Siddhi app partition
     *
     * @param queries         The list of queries to be ordered
     * @param definitionNames The names of all the definition elements (streams, tables, windows etc) of the Siddhi app
     * @return The queries reordered to the way it should be represented in a Siddhi app partition
     * @throws CodeGenerationException Error when trying to reorder the queries
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
     * Identifies the names of the input streams of a given QueryConfig object
     *
     * @param query The QueryConfig object
     * @return The list of names of the input streams of the given QueryConfig object
     * @throws CodeGenerationException Error while trying to identify the input streams
     */
    private static List<String> getInputStreams(QueryConfig query)
            throws CodeGenerationException {
        NullValidator.validateConfigObject(query);
        NullValidator.validateConfigObject(query.getQueryInput());

        List<String> inputStreamList = new LinkedList<>();
        switch (query.getQueryInput().getType().toUpperCase()) {
            case CodeGeneratorConstants.WINDOW:
            case CodeGeneratorConstants.FILTER:
            case CodeGeneratorConstants.PROJECTION:
            case CodeGeneratorConstants.FUNCTION:
                WindowFilterProjectionConfig windowFilterProjection =
                        (WindowFilterProjectionConfig) query.getQueryInput();
                NullValidator.validateConfigObject(windowFilterProjection);
                inputStreamList.add(windowFilterProjection.getFrom());
                break;
            case CodeGeneratorConstants.JOIN:
                JoinConfig join = (JoinConfig) query.getQueryInput();
                NullValidator.validateConfigObject(join);
                inputStreamList.add(join.getLeft().getFrom());
                inputStreamList.add(join.getRight().getFrom());
                break;
            case CodeGeneratorConstants.PATTERN:
            case CodeGeneratorConstants.SEQUENCE:
                PatternSequenceConfig patternSequence = (PatternSequenceConfig) query.getQueryInput();
                NullValidator.validateConfigObject(patternSequence);
                for (PatternSequenceConditionConfig condition : patternSequence.getConditionList()) {
                    NullValidator.validateConfigObject(condition);
                    inputStreamList.add(condition.getStreamName());
                }
                break;
            default:
                throw new CodeGenerationException("Unidentified query type: " + query.getQueryInput().getType());
        }

        return inputStreamList;
    }

    /**
     * Identifies that streams that need to be generated and displayed when generating the Siddhi app code
     *
     * @param streamList The list of streams in a Siddhi app
     * @param sourceList The list of source annotations in a Siddhi app
     * @param sinkList   The list of sink annotations in a Siddhi app
     * @param queryList  The list of queries in a Siddhi app (including partition queries)
     * @return The list of streams that need to be generated in a Siddhi app
     */
    public static List<StreamConfig> getStreamsToBeGenerated(List<StreamConfig> streamList,
                                                             List<SourceSinkConfig> sourceList,
                                                             List<SourceSinkConfig> sinkList,
                                                             List<QueryConfig> queryList) {
        List<StreamConfig> definedStreams = new ArrayList<>();
        for (StreamConfig stream : streamList) {
            // Check For Stream Comments
            if (stream.getPreviousCommentSegment() != null &&
                    stream.getPreviousCommentSegment().getContent() != null &&
                    !stream.getPreviousCommentSegment().getContent().isEmpty()) {
                definedStreams.add(stream);
                continue;
            }
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
     * Identifies the names of all the definition elements in a Siddhi application
     *
     * @param streams      The list of streams in a Siddhi app
     * @param tables       The list of tables in a Siddhi app
     * @param windows      The list of windows in a Siddhi app
     * @param triggers     The list of triggers in a Siddhi app
     * @param aggregations The list of aggregations in a Siddhi app
     * @param partitions   The list of partitions in a Siddhi app
     * @return The list of all the definition elements names in a Siddhi app
     */
    public static List<String> getDefinitionNames(List<StreamConfig> streams, List<TableConfig> tables,
                                                  List<WindowConfig> windows, List<TriggerConfig> triggers,
                                                  List<AggregationConfig> aggregations,
                                                  List<PartitionConfig> partitions) {
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

    /**
     * Converts a list of queries and partitions to ExecutionElementConfig objects
     *
     * @param queries    The list of queries in a Siddhi app
     * @param partitions The list of partitions in a Siddhi app
     * @return The list of ExecutionElementConfig objects of a Siddhi app
     * @throws CodeGenerationException Error when trying to convert a query/partition to an ExecutionElementConfig object
     */
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

    /**
     * Reorders a list of ExecutionElementConfig objects to the right order that it should
     * be in displayed in when converting to a Siddhi app partition
     *
     * @param executionElements The ExecutionElements (queries and partitions) of a Siddhi app
     * @param definitionNames   The names of all the definition elements in a Siddhi app (excluding inner streams and undefined streams)
     * @return The execution elements reordered to the way it should be represented in a Siddhi application
     * @throws CodeGenerationException Error when trying to reorder the ExecutionElementConfig list
     */
    public static List<ExecutionElementConfig> reorderExecutionElements(
            List<ExecutionElementConfig> executionElements, List<String> definitionNames)
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

    /**
     * Static inner class that is used to validate any incoming *Config objects for null pointers
     */
    public static class NullValidator {

        public static void validateConfigObject(StreamConfig stream) throws CodeGenerationException {
            if (stream == null) {
                throw new CodeGenerationException("A given stream element is empty");
            } else if (stream.getName() == null || stream.getName().isEmpty()) {
                throw new CodeGenerationException("The name of a given stream element is empty");
            }
        }

        public static void validateConfigObject(JoinConfig join) throws CodeGenerationException {
            if (join == null) {
                throw new CodeGenerationException("A given join query is empty");
            } else if (join.getJoinWith() == null || join.getJoinType().isEmpty()) {
                throw new CodeGenerationException("The 'joinWith' value of a given join query is empty");
            } else if (join.getJoinType() == null || join.getJoinType().isEmpty()) {
                throw new CodeGenerationException("The 'joinType' value of a given join query is empty");
            } else if (join.getLeft() == null || join.getRight() == null) {
                throw new CodeGenerationException("The left/right join element for a given join query is empty");
            } else if (join.getLeft().getType() == null || join.getLeft().getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of the left join element" +
                        " of a given join query is empty");
            } else if (join.getRight().getType() == null || join.getRight().getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of the right join element" +
                        " of a given join query is empty");
            }
        }

        public static void validateConfigObject(JoinElementConfig joinElement) throws CodeGenerationException {
            if (joinElement == null) {
                throw new CodeGenerationException("A given join element is empty");
            } else if (joinElement.getFrom() == null || joinElement.getFrom().isEmpty()) {
                throw new CodeGenerationException("The 'from' value of a given join element is empty");
            }
        }

        public static void validateConfigObject(PatternSequenceConditionConfig condition) throws CodeGenerationException {
            if (condition == null) {
                throw new CodeGenerationException("A given pattern/sequence query condition is empty");
            } else if (condition.getStreamName() == null || condition.getStreamName().isEmpty()) {
                throw new CodeGenerationException("The stream name of a given pattern/sequence query condition is empty");
            }
        }

        public static void validateConfigObject(PatternSequenceConfig patternSequence) throws CodeGenerationException {
            if (patternSequence == null) {
                throw new CodeGenerationException("A given pattern/sequence query is empty");
            } else if (patternSequence.getLogic() == null || patternSequence.getLogic().isEmpty()) {
                throw new CodeGenerationException("The 'logic' value for a given pattern/sequence query is empty");
            } else if (patternSequence.getConditionList() == null || patternSequence.getConditionList().isEmpty()) {
                throw new CodeGenerationException("The condition list for a given pattern/sequence query is empty");
            }
        }

        public static void validateConfigObject(QueryInputConfig queryInput) throws CodeGenerationException {
            if (queryInput == null) {
                throw new CodeGenerationException("A given query input element is empty");
            } else if (queryInput.getType() == null || queryInput.getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of a given query input element is empty");
            }
        }

        public static void validateConfigObject(WindowFilterProjectionConfig windowFilterProjection) throws CodeGenerationException {
            if (windowFilterProjection == null) {
                throw new CodeGenerationException("A given window/filter/project element is empty");
            } else if (windowFilterProjection.getFrom() == null || windowFilterProjection.getFrom().isEmpty()) {
                throw new CodeGenerationException("The 'from' value of a given window/filter/project element is empty");
            }
        }

        public static void validateConfigObject(QueryOutputConfig queryOutput) throws CodeGenerationException {
            if (queryOutput == null) {
                throw new CodeGenerationException("A given query output element is empty");
            } else if (queryOutput.getType() == null || queryOutput.getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of a given query output element is empty");
            }
        }

        public static void validateConfigObject(DeleteOutputConfig deleteOutput) throws CodeGenerationException {
            if (deleteOutput == null) {
                throw new CodeGenerationException("A given delete query output element is empty");
            } else if (deleteOutput.getOn() == null || deleteOutput.getOn().isEmpty()) {
                throw new CodeGenerationException("The 'on' statement of a given delete query" +
                        " output element is null/empty");
            }
        }

        public static void validateConfigObject(UpdateInsertIntoOutputConfig updateInsertIntoOutput) throws CodeGenerationException {
            if (updateInsertIntoOutput == null) {
                throw new CodeGenerationException("A given update/insert query output element is empty");
            } else if (updateInsertIntoOutput.getOn() == null || updateInsertIntoOutput.getOn().isEmpty()) {
                throw new CodeGenerationException("The 'on' value of a given update/insert query" +
                        " element is empty");
            }
        }

        public static void validateConfigObject(SetAttributeConfig setAttribute) throws CodeGenerationException {
            if (setAttribute == null) {
                throw new CodeGenerationException("A given set attribute element given is empty");
            } else if (setAttribute.getAttribute() == null || setAttribute.getAttribute().isEmpty()) {
                throw new CodeGenerationException("The 'attribute' value of a given set attribute element is empty");
            } else if (setAttribute.getValue() == null || setAttribute.getValue().isEmpty()) {
                throw new CodeGenerationException("The 'value' attribute of a given set attribute element is empty");
            }
        }

        public static void validateConfigObject(AggregationConfig aggregation) throws CodeGenerationException {
            if (aggregation == null) {
                throw new CodeGenerationException("A given aggregation element is empty");
            } else if (aggregation.getName() == null || aggregation.getName().isEmpty()) {
                throw new CodeGenerationException("The name of a given aggregation element is empty");
            } else if (aggregation.getFrom() == null || aggregation.getFrom().isEmpty()) {
                throw new CodeGenerationException("The 'from' value of " + aggregation.getName() + " is empty");
            } else if (aggregation.getAggregateByTimePeriod() == null) {
                throw new CodeGenerationException("The 'aggregateByTimePeriod' value of " + aggregation.getName()
                        + " is empty");
            } else if (aggregation.getAggregateByTimePeriod().getType() == null
                    || aggregation.getAggregateByTimePeriod().getType().isEmpty()) {
                throw new CodeGenerationException("The aggregateByTimePeriod 'type' value of "
                        + aggregation.getName() + " is empty");
            }
        }

        public static void validateConfigObject(AggregateByTimePeriod aggregateByTimePeriod) throws CodeGenerationException {
            if (aggregateByTimePeriod == null) {
                throw new CodeGenerationException("A given aggregateByTimePeriod element is empty");
            } else if (aggregateByTimePeriod.getType() == null || aggregateByTimePeriod.getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of a given aggregateByTimePeriod element is empty");
            }
        }

        public static void validateConfigObject(AggregateByTimeRange aggregateByTimeRange) throws CodeGenerationException {
            if (aggregateByTimeRange.getValue() == null) {
                throw new CodeGenerationException("The 'value' attribute of a given aggregateByTimeRange" +
                        " element is empty");
            } else if (aggregateByTimeRange.getValue().getMin() == null ||
                    aggregateByTimeRange.getValue().getMin().isEmpty()) {
                throw new CodeGenerationException("The 'min' value of a given" +
                        " aggregateByTimeRange element is empty");
            } else if (aggregateByTimeRange.getValue().getMax() == null ||
                    aggregateByTimeRange.getValue().getMax().isEmpty()) {
                throw new CodeGenerationException("The 'max' value of a given" +
                        " aggregateByTimeRange element is empty");
            }
        }

        public static void validateConfigObject(FunctionConfig function) throws CodeGenerationException {
            if (function == null) {
                throw new CodeGenerationException("A given function element is empty");
            } else if (function.getName() == null || function.getName().isEmpty()) {
                throw new CodeGenerationException("The name of a given function element is empty");
            } else if (function.getScriptType() == null || function.getScriptType().isEmpty()) {
                throw new CodeGenerationException("The 'script type' of " + function.getName() + " is empty");
            } else if (function.getReturnType() == null || function.getReturnType().isEmpty()) {
                throw new CodeGenerationException("The return type of " + function.getName() + " is empty");
            } else if (function.getBody() == null || function.getBody().isEmpty()) {
                throw new CodeGenerationException("The 'body' value of " + function.getName() + " is empty");
            }
        }

        public static void validateConfigObject(PartitionConfig partition) throws CodeGenerationException {
            if (partition == null) {
                throw new CodeGenerationException("A given partition object is empty");
            } else if (partition.getPartitionWith() == null || partition.getPartitionWith().isEmpty()) {
                throw new CodeGenerationException("The 'partitionWith' value of a given partition element is empty");
            } else if (partition.getQueryLists() == null || partition.getQueryLists().isEmpty()) {
                throw new CodeGenerationException("The query lists of a given partition element is empty");
            }
        }

        public static void validateConfigObject(PartitionWithElement partitionWithElement) throws CodeGenerationException {
            if (partitionWithElement == null) {
                throw new CodeGenerationException("A given 'partition with' element is empty");
            } else if (partitionWithElement.getExpression() == null || partitionWithElement.getExpression().isEmpty()) {
                throw new CodeGenerationException("The 'expression' value of a given 'partition with' element is empty");
            } else if (partitionWithElement.getStreamName() == null || partitionWithElement.getStreamName().isEmpty()) {
                throw new CodeGenerationException("The stream name of a given 'partition with' element is empty");
            }
        }

        public static void validateConfigObject(SourceSinkConfig sourceSink) throws CodeGenerationException {
            if (sourceSink == null) {
                throw new CodeGenerationException("A given source/sink element is empty");
            } else if (sourceSink.getAnnotationType() == null || sourceSink.getAnnotationType().isEmpty()) {
                throw new CodeGenerationException("The annotation type for a given source/sink element is empty");
            } else if (sourceSink.getType() == null || sourceSink.getType().isEmpty()) {
                throw new CodeGenerationException("The type attribute for a given source/sink element is empty");
            }
        }

        public static void validateConfigObject(StoreConfig store) throws CodeGenerationException {
            if (store.getType() == null || store.getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of a given store element is empty");
            } else if (store.getOptions() == null || store.getOptions().isEmpty()) {
                throw new CodeGenerationException("The options map of a given store element is empty");
            }
        }

        public static void validateConfigObject(StreamHandlerConfig streamHandler) throws CodeGenerationException {
            if (streamHandler == null) {
                throw new CodeGenerationException("A given stream handler element is empty");
            } else if (streamHandler.getType() == null || streamHandler.getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of a given stream handler element is empty");
            }
        }

        public static void validateConfigObject(TableConfig table) throws CodeGenerationException {
            if (table == null) {
                throw new CodeGenerationException("A given table element is empty");
            } else if (table.getName() == null || table.getName().isEmpty()) {
                throw new CodeGenerationException("The name of a given table element is empty");
            }
        }

        public static void validateConfigObject(TriggerConfig trigger) throws CodeGenerationException {
            if (trigger == null) {
                throw new CodeGenerationException("A given trigger element is empty");
            } else if (trigger.getName() == null || trigger.getName().isEmpty()) {
                throw new CodeGenerationException("The name of a given trigger element is empty");
            } else if (trigger.getAt() == null || trigger.getAt().isEmpty()) {
                throw new CodeGenerationException("The 'at' value of " + trigger.getName() + " is empty");
            }
        }

        public static void validateConfigObject(WindowConfig window) throws CodeGenerationException {
            if (window == null) {
                throw new CodeGenerationException("A given window element is empty");
            } else if (window.getName() == null || window.getName().isEmpty()) {
                throw new CodeGenerationException("The name of a given window element is empty");
            } else if (window.getFunction() == null || window.getFunction().isEmpty()) {
                throw new CodeGenerationException("The function name of the window " + window.getName() + " is empty");
            }
        }

        public static void validateConfigObject(InsertOutputConfig insertOutput) throws CodeGenerationException {
            if (insertOutput == null) {
                throw new CodeGenerationException("A given insert query output element is empty");
            }
        }

        public static void validateConfigObject(AttributesSelectionConfig attributesSelection) throws CodeGenerationException {
            if (attributesSelection == null) {
                throw new CodeGenerationException("A given attribute selection element is empty");
            } else if (attributesSelection.getType() == null || attributesSelection.getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of a given attribute selection element is empty");
            }
        }

        public static void validateConfigObject(SelectedAttribute attribute) throws CodeGenerationException {
            if (attribute.getExpression() == null || attribute.getExpression().isEmpty()) {
                throw new CodeGenerationException("The 'expression' value of a given select" +
                        " attribute element is empty");
            }
        }

        public static void validateConfigObject(QueryOrderByConfig orderByAttribute) throws CodeGenerationException {
            if (orderByAttribute == null) {
                throw new CodeGenerationException("A given query 'order by' value is empty");
            } else if (orderByAttribute.getValue() == null || orderByAttribute.getValue().isEmpty()) {
                throw new CodeGenerationException("The 'value' attribute for a given query order by element is empty");
            }
        }

        public static void validateConfigObject(QueryConfig query) throws CodeGenerationException {
            if (query == null) {
                throw new CodeGenerationException("A given query element is empty");
            }
        }

        public static void validateConfigObject(AggregateByTimeInterval aggregateByTimeInterval) throws CodeGenerationException {
            if (aggregateByTimeInterval.getValue() == null || aggregateByTimeInterval.getValue().isEmpty()) {
                throw new CodeGenerationException("The 'value' attribute of a given" +
                        " attributeByTimeInterval element is empty");
            }
        }

        public static void validateConfigObject(List<PartitionWithElement> partitionWith) throws CodeGenerationException {
            if (partitionWith == null || partitionWith.isEmpty()) {
                throw new CodeGenerationException("A given 'partitionWith' list is empty");
            }
        }

        public static void validateConfigObject(MapperConfig mapper) throws CodeGenerationException {
            if (mapper.getType() == null || mapper.getType().isEmpty()) {
                throw new CodeGenerationException("The map type of a given source/sink map element is empty");
            }
        }

        public static void validateConfigObject(MapperPayloadOrAttribute payloadOrAttribute) throws CodeGenerationException {
            if (payloadOrAttribute.getType() == null || payloadOrAttribute.getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of a given source/sink map attribute element is empty");
            }
        }

        public static void validateConfigObject(MapperListPayloadOrAttribute mapperListAttribute) throws CodeGenerationException {
            if (mapperListAttribute.getValue() == null || mapperListAttribute.getValue().isEmpty()) {
                throw new CodeGenerationException("The list values of a given sink/source" +
                        " map attribute element is empty");
            }
        }

        public static void validateConfigObject(MapperMapPayloadOrAttribute mapperMapAttribute) throws CodeGenerationException {
            if (mapperMapAttribute.getValue() == null || mapperMapAttribute.getValue().isEmpty()) {
                throw new CodeGenerationException("The key-value pair values of" +
                        " a given source/sink map attribute element is empty");
            }
        }

        public static void validateConfigObject(AttributeConfig attribute) throws CodeGenerationException {
            if (attribute == null) {
                throw new CodeGenerationException("A given attribute element is empty");
            } else if (attribute.getName() == null || attribute.getName().isEmpty()) {
                throw new CodeGenerationException("The 'name' of a given attribute element is empty");
            } else if (attribute.getType() == null || attribute.getType().isEmpty()) {
                throw new CodeGenerationException("The 'type' value of a given attribute element is empty");
            }
        }

        private NullValidator() {
        }

    }

    private CodeGeneratorUtils() {
    }

}
