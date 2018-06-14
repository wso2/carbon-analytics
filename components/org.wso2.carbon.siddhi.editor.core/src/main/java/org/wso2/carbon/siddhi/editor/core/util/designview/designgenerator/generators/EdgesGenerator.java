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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.Edge;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.SiddhiAppConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConditionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.NodeType;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryInputType;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryListType;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generator to create Edges, that connect Siddhi Elements - considered as Nodes
 */
public class EdgesGenerator {
    private SiddhiAppConfig siddhiAppConfig;

    public EdgesGenerator(SiddhiAppConfig siddhiAppConfig) {
        this.siddhiAppConfig = siddhiAppConfig;
    }

    /**
     * Generates Edges for the elements in the SiddhiAppConfig
     * @return                                  Set of all the Edges
     * @throws DesignGenerationException        Error while generating config
     */
    public Set<Edge> generateEdges() throws DesignGenerationException {
        Set<Edge> edges = new HashSet<>();
        edges.addAll(generateSourceEdges(siddhiAppConfig.getSourceList()));
        edges.addAll(generateSinkEdges(siddhiAppConfig.getSinkList()));
        edges.addAll(
                generateWindowFilterProjectionQueryEdges(
                        getCompleteQueryTypeList(QueryListType.WINDOW_FILTER_PROJECTION)));
        edges.addAll(
                generateJoinQueryEdges(
                        getCompleteQueryTypeList(QueryListType.JOIN)));
        edges.addAll(
                generatePatternSequenceQueryEdges(
                        getCompleteQueryTypeList(QueryListType.PATTERN)));
        edges.addAll(
                generatePatternSequenceQueryEdges(
                        getCompleteQueryTypeList(QueryListType.SEQUENCE)));
        edges.addAll(generateAggregationEdges(siddhiAppConfig.getAggregationList()));
        return edges;
    }

    /**
     * Gets complete query list of the given queryListType,
     * from SiddhiAppConfig's outer level and Partitions
     * @param queryListType     Type of the QueryList
     * @return                  List of QueryConfigs
     */
    private List<QueryConfig> getCompleteQueryTypeList(QueryListType queryListType) {
        List<QueryConfig> queryList = new ArrayList<>(siddhiAppConfig.getQueryLists().get(queryListType));
        for (PartitionConfig partitionConfig : siddhiAppConfig.getPartitionList()) {
            queryList.addAll(partitionConfig.getQueryLists().get(queryListType));
        }
        return queryList;
    }

    /**
     * Generates Edges related to Sources
     * @param sourceList                        List of Source configs
     * @return                                  Set of Edges
     * @throws DesignGenerationException        Error while generating edges
     */
    private Set<Edge> generateSourceEdges(List<SourceSinkConfig> sourceList) throws DesignGenerationException {
        Set<Edge> edges = new HashSet<>();
        for (SourceSinkConfig source : sourceList) {
            edges.add(generateEdge(source, getElementWithStreamName(source.getConnectedElementName(), null)));
        }
        return edges;
    }

    /**
     * Generates Edges related to Sinks
     * @param sinkList                          List of Sink configs
     * @return                                  Set of Edges
     * @throws DesignGenerationException        Error while generating edges
     */
    private Set<Edge> generateSinkEdges(List<SourceSinkConfig> sinkList) throws DesignGenerationException {
        Set<Edge> edges = new HashSet<>();
        for (SourceSinkConfig sink : sinkList) {
            edges.add(generateEdge(getElementWithStreamName(sink.getConnectedElementName(), null), sink));
        }
        return edges;
    }

    /**
     * Generates Edges related to WindowFilterProjection Queries
     * @param windowFilterProjectionQueryList       List of WindowFilterProjection QueryConfigs
     * @return                                      Set of Edges
     * @throws DesignGenerationException            Error while generating edges
     */
    private Set<Edge> generateWindowFilterProjectionQueryEdges(List<QueryConfig> windowFilterProjectionQueryList)
            throws DesignGenerationException {
        Set<Edge> edges = new HashSet<>();
        for (QueryConfig query : windowFilterProjectionQueryList) {
            // Edge towards Query
            String inputStreamName = ((WindowFilterProjectionConfig) (query.getQueryInput())).getFrom();
            if (query.getPartitionId() != null && query.getConnectorsAndStreams() != null &&
                    query.getConnectorsAndStreams().values().contains(
                        ((WindowFilterProjectionConfig) (query.getQueryInput())).getFrom())) {
                    // Query connects through a PartitionConnector
                    // Edge from Outer Element to PartitionConnector
                    edges.add(
                            generateEdgeToPartitionConnector(
                                    getElementWithStreamName(inputStreamName, query.getPartitionId()),
                                    query.getConnectorIdByStreamName(inputStreamName)));
                    // Edge from PartitionConnector to Query
                    edges.add(
                            generateEdgeFromPartitionConnector(
                                    query.getConnectorIdByStreamName(inputStreamName),
                                    query));
            } else {
                // Query doesn't connect through a PartitionConnector
                edges.add(
                        generateEdge(
                                getElementWithStreamName(inputStreamName, query.getPartitionId()),
                                query));
            }
            // Edge from Query
            edges.add(
                    generateEdge(
                            query,
                            getElementWithStreamName(query.getQueryOutput().getTarget(), query.getPartitionId())));
        }
        return edges;
    }

    /**
     * Generates Edges related to Join Queries
     * @param joinQueryList                         List of Join QueryConfigs
     * @return                                      Set of Edges
     * @throws DesignGenerationException            Error while generating edges
     */
    private Set<Edge> generateJoinQueryEdges(List<QueryConfig> joinQueryList) throws DesignGenerationException {
        Set<Edge> edges = new HashSet<>();
        for (QueryConfig query : joinQueryList) {
            // Edge towards Query (From Left)
            String leftInputStreamName = (((JoinConfig) (query.getQueryInput())).getLeft().getFrom());
            if (query.getPartitionId() != null && query.getConnectorsAndStreams() != null &&
                    query.getConnectorsAndStreams().values().contains(leftInputStreamName)) {
                // Query connects through a PartitionConnector
                // Edge from Outer Element to PartitionConnector
                edges.add(
                        generateEdgeToPartitionConnector(
                                getElementWithStreamName(leftInputStreamName, query.getPartitionId()),
                                query.getConnectorIdByStreamName(leftInputStreamName)));
                // Edge from PartitionConnector to Query
                edges.add(
                        generateEdgeFromPartitionConnector(
                                query.getConnectorIdByStreamName(leftInputStreamName),
                                query));
            } else {
                // Query doesn't connect through a PartitionConnector
                edges.add(
                        generateEdge(
                                getElementWithStreamName(leftInputStreamName, query.getPartitionId()),
                                query));
            }
            // Edge towards Query (From Right)
            String rightInputStream = (((JoinConfig) (query.getQueryInput())).getRight().getFrom());
            if (query.getPartitionId() != null && query.getConnectorsAndStreams() != null &&
                    query.getConnectorsAndStreams().values().contains(rightInputStream)) {
                // Query connects through a PartitionConnector
                // Edge from Outer Element to PartitionConnector
                edges.add(
                        generateEdgeToPartitionConnector(
                                getElementWithStreamName(rightInputStream, query.getPartitionId()),
                                query.getConnectorIdByStreamName(rightInputStream)));
                // Edge from PartitionConnector to Query
                edges.add(
                        generateEdgeFromPartitionConnector(
                                query.getConnectorIdByStreamName(rightInputStream),
                                query));
            } else {
                // Query doesn't connect through a PartitionConnector
                edges.add(
                        generateEdge(
                                getElementWithStreamName(rightInputStream, query.getPartitionId()),
                                query));
            }
            // Edge from Query
            edges.add(
                    generateEdge(
                            query,
                            getElementWithStreamName(query.getQueryOutput().getTarget(), query.getPartitionId())));
        }
        return edges;
    }

    /**
     * Generates Edges related to Pattern/Sequence Queries
     * @param patternSequenceQueryList          List of Pattern/Sequence QueryConfigs
     * @return                                  Set of Edges
     * @throws DesignGenerationException        Error while generating edges
     */
    private Set<Edge> generatePatternSequenceQueryEdges(List<QueryConfig> patternSequenceQueryList)
            throws DesignGenerationException {
        Set<Edge> edges = new HashSet<>();
        for (QueryConfig query : patternSequenceQueryList) {
            // Edges towards Query
            List<String> inputStreamNames = new ArrayList<>();
            for (PatternSequenceConditionConfig condition :
                    ((PatternSequenceConfig) (query.getQueryInput())).getConditionList()) {
                inputStreamNames.add(condition.getStreamName());
            }
            for (String inputStreamName : inputStreamNames) {
                if (query.getPartitionId() != null && query.getConnectorsAndStreams() != null &&
                        query.getConnectorsAndStreams().values().contains(inputStreamName)) {
                    // Query connects through a PartitionConnector
                    // Edge from Outer Element to PartitionConnector
                    edges.add(
                            generateEdgeToPartitionConnector(
                                    getElementWithStreamName(inputStreamName, query.getPartitionId()),
                                    query.getConnectorIdByStreamName(inputStreamName)));
                    // Edge from PartitionConnector to Query
                    edges.add(
                            generateEdgeFromPartitionConnector(
                                    query.getConnectorIdByStreamName(inputStreamName),
                                    query));
                } else {
                    // Query doesn't connect through a PartitionConnector
                    edges.add(
                            generateEdge(
                                    getElementWithStreamName(inputStreamName, query.getPartitionId()),
                                    query));
                }
            }
            // Edge from Query
            edges.add(
                    generateEdge(
                            query,
                            getElementWithStreamName(query.getQueryOutput().getTarget(), query.getPartitionId())));
        }
        return edges;
    }

    /**
     * Generates Edges related to Aggregations
     * @param aggregationConfigList             List of AggregationConfigs
     * @return                                  Set of Edges
     * @throws DesignGenerationException        Error while generating Edges
     */
    private Set<Edge> generateAggregationEdges(List<AggregationConfig> aggregationConfigList)
        throws DesignGenerationException {
        Set<Edge> edges = new HashSet<>();
        for (AggregationConfig aggregation : aggregationConfigList) {
            edges.add(
                    generateEdge(
                            getElementWithStreamName(aggregation.getFrom(), null),
                            aggregation));
        }
        return edges;
    }

    /**
     * Generates an Edge between a SiddhiElementConfig parent and PartitionConnector child
     * @param parentElement                     Parent SiddhiElementConfig
     * @param partitionConnectorId              Id of the PartitionConnector
     * @return                                  Edge object
     * @throws DesignGenerationException        Error while getting type of the parent
     */
    private Edge generateEdgeToPartitionConnector(SiddhiElementConfig parentElement,
                                                  String partitionConnectorId) throws DesignGenerationException {
        return new Edge(
                generateEdgeId(parentElement.getId(), partitionConnectorId),
                parentElement.getId(),
                getSiddhiElementType(parentElement),
                partitionConnectorId,
                NodeType.PARTITION);
    }

    /**
     * Generates an Edge between a PartitionConnector parent and SiddhiElementConfig child
     * @param partitionConnectorId              Id of the PartitionConnector
     * @param childElement                      Child SiddhiElementConfig
     * @return                                  Edge object
     * @throws DesignGenerationException        Error while getting type of the child
     */
    private Edge generateEdgeFromPartitionConnector(String partitionConnectorId,
                                                    SiddhiElementConfig childElement) throws DesignGenerationException {
        return new Edge(
                generateEdgeId(partitionConnectorId, childElement.getId()),
                partitionConnectorId,
                NodeType.PARTITION,
                childElement.getId(),
                getSiddhiElementType(childElement));
    }

    /**
     * Generates an Edge between corresponding parent and child SiddhiElements.
     * When one of the given parent/child is a SiddhiElement object, the Element is used directly.
     * When one of those is an Id, the respective element for the id is got and used
     * @param parentElementOrId                 Parent Element object or Id
     * @param childElementOrId                  Child Element object or Id
     * @return                                  Edge object
     * @throws DesignGenerationException        Error while generating config
     */
    private Edge generateEdge(Object parentElementOrId, Object childElementOrId) throws DesignGenerationException {
        SiddhiElementConfig parentElement = getOrAcceptSiddhiElement(parentElementOrId);
        SiddhiElementConfig childElement = getOrAcceptSiddhiElement(childElementOrId);
        return generateEdgeForElements(parentElement, childElement);
    }

    /**
     * Accepts and returns the given object when it is a SiddhiElementConfig.
     * Gets the respective SiddhiElement and returns, when the Id is given
     * @param elementOrId                       SiddhiElementConfig object or Id
     * @return                                  SiddhiElementConfig object
     * @throws DesignGenerationException        Error while generating config
     */
    private SiddhiElementConfig getOrAcceptSiddhiElement(Object elementOrId) throws DesignGenerationException {
        if (elementOrId instanceof SiddhiElementConfig) {
            return (SiddhiElementConfig) elementOrId;
        } else if (elementOrId instanceof String) {
            return getElementWithId((String) elementOrId);
        }
        throw new DesignGenerationException(
                "SiddhiElement ID or SiddhiElement object is expected, to find the element or accept the given one");
    }

    /**
     * Generates an edge between the given parent and child SiddhiElements
     * @param parentElement                     SiddhiElement object, where the Edge starts from
     * @param childElement                      SiddhiElement object, where the Edge ends at
     * @return                                  Edge object
     * @throws DesignGenerationException        Error while generating config
     */
    private Edge generateEdgeForElements(SiddhiElementConfig parentElement, SiddhiElementConfig childElement)
            throws DesignGenerationException {
        NodeType parentType = getSiddhiElementType(parentElement);
        NodeType childType = getSiddhiElementType(childElement);
        String edgeId = generateEdgeId(parentElement.getId(), childElement.getId());
        return new Edge(edgeId, parentElement.getId(), parentType, childElement.getId(), childType);
    }

    /**
     * Gets SiddhiElementConfig object from the SiddhiAppConfig, which has a related stream with the given name
     * @param streamName                        Name of the SiddhiElementConfig's related stream
     * @param scopedPartitionId                 Id of the Partition, which is the scope for finding streams
     * @return                                  SiddhiElementConfig object
     * @throws DesignGenerationException        Error while generating config
     */
    private SiddhiElementConfig getElementWithStreamName(String streamName, String scopedPartitionId)
            throws DesignGenerationException {
        for (StreamConfig streamConfig : siddhiAppConfig.getStreamList()) {
            if (streamConfig.getName().equals(streamName)) {
                return streamConfig;
            }
        }
        for (TableConfig tableConfig : siddhiAppConfig.getTableList()) {
            if (tableConfig.getName().equals(streamName)) {
                return tableConfig;
            }
        }
        for (TriggerConfig triggerConfig : siddhiAppConfig.getTriggerList()) {
            if (triggerConfig.getName().equals(streamName)) {
                return triggerConfig;
            }
        }
        for (WindowConfig windowConfig : siddhiAppConfig.getWindowList()) {
            if (windowConfig.getName().equals(streamName)) {
                return windowConfig;
            }
        }
        for (AggregationConfig aggregationConfig : siddhiAppConfig.getAggregationList()) {
            if (aggregationConfig.getName().equals(streamName)) {
                return aggregationConfig;
            }
        }
        if (scopedPartitionId != null) {
            // Search only in the scoped Partition
            for (PartitionConfig partitionConfig : siddhiAppConfig.getPartitionList()) {
                if (partitionConfig.getId().equals(scopedPartitionId)) {
                    for (StreamConfig streamConfig : partitionConfig.getStreamList()) {
                        if (streamConfig.getName().equals(streamName)) {
                            return streamConfig;
                        }
                    }
                }
            }
        }
        throw new DesignGenerationException("Unable to find an element with related stream name '" + streamName + "'");
    }

    /**
     * Gets SiddhiElementConfig object from the SiddhiAppConfig, which has the given Id
     * @param id                                Id of the SiddhiElementConfig
     * @return                                  SiddhiElementConfig object
     * @throws DesignGenerationException        No element found with the given Id
     */
    private SiddhiElementConfig getElementWithId(String id) throws DesignGenerationException {
        for (QueryListType queryListType : siddhiAppConfig.getQueryLists().keySet()) {
            for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getQueryLists().get(queryListType)) {
                if (siddhiElementConfig.getId().equals(id)) {
                    return siddhiElementConfig;
                }
            }
        }
        List<SiddhiElementConfig> siddhiElementLists = new ArrayList<>();
        siddhiElementLists.addAll(siddhiAppConfig.getSinkList());
        siddhiElementLists.addAll(siddhiAppConfig.getSourceList());
        siddhiElementLists.addAll(siddhiAppConfig.getStreamList());
        siddhiElementLists.addAll(siddhiAppConfig.getTableList());
        siddhiElementLists.addAll(siddhiAppConfig.getTriggerList());
        siddhiElementLists.addAll(siddhiAppConfig.getWindowList());
        siddhiElementLists.addAll(siddhiAppConfig.getAggregationList());
        for (Map.Entry<QueryListType, List<QueryConfig>> queryList : siddhiAppConfig.getQueryLists().entrySet()) {
            siddhiElementLists.addAll(queryList.getValue());
        }
        siddhiElementLists.addAll(siddhiAppConfig.getPartitionList());
        for (PartitionConfig partitionConfig : siddhiAppConfig.getPartitionList()) {
            siddhiElementLists.addAll(partitionConfig.getStreamList());
            for (Map.Entry<QueryListType, List<QueryConfig>> queryList : partitionConfig.getQueryLists().entrySet()) {
                siddhiElementLists.addAll(queryList.getValue());
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiElementLists) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        throw new DesignGenerationException("Unable to find element with id '" + id + "'");
    }

    /**
     * Gets Node Type of the given SiddhiElementConfig object
     * @param siddhiElementConfig               SiddhiElementConfig object, which is represented as a Node
     * @return                                  Node type
     * @throws DesignGenerationException        Error while generating config
     */
    private NodeType getSiddhiElementType(SiddhiElementConfig siddhiElementConfig) throws DesignGenerationException {
        if (siddhiElementConfig instanceof StreamConfig) {
            return NodeType.STREAM;
        }
        if (siddhiElementConfig instanceof TableConfig) {
            return NodeType.TABLE;
        }
        if (siddhiElementConfig instanceof WindowConfig) {
            return NodeType.WINDOW;
        }
        if (siddhiElementConfig instanceof SourceSinkConfig) {
            String annotationType = ((SourceSinkConfig) siddhiElementConfig).getAnnotationType().toUpperCase();
            return NodeType.valueOf(annotationType);
        }
        if (siddhiElementConfig instanceof AggregationConfig) {
            return NodeType.AGGREGATION;
        }
        if (siddhiElementConfig instanceof TriggerConfig) {
            return NodeType.TRIGGER;
        }
        if (siddhiElementConfig instanceof QueryConfig) {
            QueryInputConfig queryInputConfig = ((QueryConfig) siddhiElementConfig).getQueryInput();
            if (queryInputConfig instanceof WindowFilterProjectionConfig) {
                return NodeType.WINDOW_FILTER_PROJECTION_QUERY;
            }
            if (queryInputConfig instanceof JoinConfig) {
                return NodeType.JOIN_QUERY;
            }
            if (queryInputConfig instanceof PatternSequenceConfig) {
                if (queryInputConfig.getType().equalsIgnoreCase(QueryInputType.PATTERN.toString())) {
                    return NodeType.PATTERN_QUERY;
                }
                return NodeType.SEQUENCE_QUERY;
            }
        }
        throw new DesignGenerationException(
                "Type is unknown for Siddhi Element with id '" + siddhiElementConfig.getId() + "'");
    }

    /**
     * Generates Edge ID using the parent ID and the child ID, that are connected to this edge
     * @param parentID  ID of the parent node
     * @param childID   ID of the child node
     * @return          ID of the edge
     */
    private static String generateEdgeId(String parentID, String childID) {
        return String.format("%s_%s", parentID, childID);
    }
}
