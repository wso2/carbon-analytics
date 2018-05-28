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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.NodeType;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.DesignGeneratorHelper;

import java.util.ArrayList;
import java.util.List;

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
     * @return      List of all the Edges
     */
    public List<Edge> generateEdges() {
        List<Edge> edges = new ArrayList<>();
        edges.addAll(generateWindowFilterProjectionQueryEdges(siddhiAppConfig.getWindowFilterProjectionQueryList()));
        edges.addAll(generateJoinQueryEdges(siddhiAppConfig.getJoinQueryList()));
        // TODO implement other edges
        return edges;
    }

    /**
     * Generates a list of edges, whose members denote an Edge related to a JoinQuery element
     * @param windowFilterProjectionQueryList       List of WindowFilterProjection QueryConfigs
     * @return                                      List of Edges
     */
    private List<Edge> generateWindowFilterProjectionQueryEdges(List<QueryConfig> windowFilterProjectionQueryList) {
        List<Edge> edges = new ArrayList<>();
        for (QueryConfig query : windowFilterProjectionQueryList) {
            // Edge towards Query element
            edges.add(
                    generateEdge(
                            getElementWithStreamName(
                                    ((WindowFilterProjectionConfig) (query.getQueryInput())).getFrom()),
                            query));
            // Edge from Query element
            edges.add(
                    generateEdge(
                            query,
                            getElementWithStreamName(query.getQueryOutput().getTarget()).getId()));
        }
        return edges;
    }

    /**
     * Generates a list of edges, whose members denote an Edge related to a JoinQuery element
     * @param joinQueryList     List of Join QueryConfigs
     * @return                  List of Edges
     */
    private List<Edge> generateJoinQueryEdges(List<QueryConfig> joinQueryList) {
        List<Edge> edges = new ArrayList<>();
        for (QueryConfig query : joinQueryList) {
            // Edge towards Query element (From Left element)
            edges.add(
                    generateEdge(
                            getElementWithStreamName(((JoinConfig) (query.getQueryInput())).getLeft().getFrom()),
                            query));
            // Edge towards Query element (From Right element)
            edges.add(
                    generateEdge(
                            getElementWithStreamName(((JoinConfig) (query.getQueryInput())).getRight().getFrom()),
                            query));
            // Edge from Query element
            edges.add(
                    generateEdge(
                            query,
                            getElementWithStreamName(query.getQueryOutput().getTarget())));
        }
        return edges;
    }

    /**
     * Generates an Edge between corresponding parent and child SiddhiElements.
     * When one of the given parent/child is a SiddhiElement object, the Element is used directly.
     * When one of those is an Id, the respective element for the id is got and used
     * @param parentElementOrId     Parent Element object or Id
     * @param childElementOrId      Child Element object or Id
     * @return                      Edge object
     */
    private Edge generateEdge(Object parentElementOrId, Object childElementOrId) {
        SiddhiElementConfig parentElement = getOrAcceptSiddhiElement(parentElementOrId);
        SiddhiElementConfig childElement = getOrAcceptSiddhiElement(childElementOrId);
        return generateEdgesForElements(parentElement, childElement);
    }

    /**
     * Accepts and returns the given object when it is a SiddhiElementConfig.
     * Gets the respective SiddhiElement and returns, when the Id is given
     * @param elementOrId       SiddhiElementConfig object or Id
     * @return                  SiddhiElementConfig object
     */
    private SiddhiElementConfig getOrAcceptSiddhiElement(Object elementOrId) {
        if (elementOrId instanceof SiddhiElementConfig) {
            return (SiddhiElementConfig) elementOrId;
        } else if (elementOrId instanceof String) {
            return getElementWithId((String) elementOrId);
        }
        throw new IllegalArgumentException(
                "SiddhiElement ID or SiddhiElement object is expected, to find the element or accept the given one");
    }

    /**
     * Generates an edge between the given parent and child SiddhiElements
     * @param parentElement     SiddhiElement object, where the Edge starts from
     * @param childElement      SiddhiElement object, where the Edge ends at
     * @return                  Edge object
     */
    private Edge generateEdgesForElements(SiddhiElementConfig parentElement, SiddhiElementConfig childElement) {
        NodeType parentType = getSiddhiElementType(parentElement);
        NodeType childType = getSiddhiElementType(childElement);
        String edgeId = DesignGeneratorHelper.generateEdgeID(parentElement.getId(), childElement.getId());
        return new Edge(edgeId, parentElement.getId(), parentType, childElement.getId(), childType);
    }

    /**
     * Gets SiddhiElementConfig object from the SiddhiAppConfig, which has a related stream with the given name
     * @param streamName        Name of the SiddhiElementConfig's related stream
     * @return                  SiddhiElementConfig object
     */
    private SiddhiElementConfig getElementWithStreamName(String streamName) {
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
        throw new IllegalArgumentException("Unable to find an element with related stream name '" + streamName + "'");
    }

    /**
     * Gets SiddhiElementConfig object from the SiddhiAppConfig, which has the given Id
     * @param id        Id of the SiddhiElementConfig
     * @return          SiddhiElementConfig object
     */
    private SiddhiElementConfig getElementWithId(String id) {
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getWindowFilterProjectionQueryList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getJoinQueryList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getPatternQueryList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getSequenceQueryList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getSinkList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getSourceList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getStreamList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getTableList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getTriggerList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getWindowList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        for (SiddhiElementConfig siddhiElementConfig : siddhiAppConfig.getAggregationList()) {
            if (siddhiElementConfig.getId().equals(id)) {
                return siddhiElementConfig;
            }
        }
        throw new IllegalArgumentException("Unable to find element with id '" + id + "'");
    }

    /**
     * Gets Node Type of the given SiddhiElementConfig object
     * @param siddhiElementConfig       SiddhiElementConfig object, which is represented as a Node
     * @return                          Node type
     */
    private NodeType getSiddhiElementType(SiddhiElementConfig siddhiElementConfig) {
        if (siddhiElementConfig instanceof StreamConfig) {
            return NodeType.STREAM;
        }
        if (siddhiElementConfig instanceof TableConfig) {
            return NodeType.TABLE;
        }
        if (siddhiElementConfig instanceof WindowConfig) {
            return NodeType.WINDOW;
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
            throw new IllegalArgumentException("Type is unknown for Query Input");
        }
        // TODO implement
        throw new IllegalArgumentException(
                "Type is unknown for Siddhi Element with id '" + siddhiElementConfig.getId() + "'");
    }
}
