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
     * @return      List of Edges
     */
    public List<Edge> generateEdges() {
        List<Edge> edges = new ArrayList<>();
        for (QueryConfig windowFilterProjectionQuery : siddhiAppConfig.getWindowFilterProjectionQueryList()) {
            // Edge for Input
            edges.add(generateEdge(
                    ((WindowFilterProjectionConfig) (windowFilterProjectionQuery.getQueryInput())).getFrom(),
                    windowFilterProjectionQuery.getId()));
            // Edge for Output
            edges.add(generateEdge(
                    windowFilterProjectionQuery.getId(),
                    windowFilterProjectionQuery.getQueryOutput().getTarget()));
        }
        for (QueryConfig joinQuery : siddhiAppConfig.getJoinQueryList()) {
            // Edge for Input (Left)
            edges.add(generateEdge(((JoinConfig)(joinQuery.getQueryInput())).getLeft().getFrom(), joinQuery.getId()));
            // Edge for Input (Right)
            edges.add(generateEdge(((JoinConfig)(joinQuery.getQueryInput())).getRight().getFrom(), joinQuery.getId()));
            // Edge for Output
            edges.add(generateEdge(joinQuery.getId(), joinQuery.getQueryOutput().getTarget()));
        }
        // TODO: 3/29/18 implement other edges
        return edges;
    }

    /**
     * Returns an Edge, that represents the connection between given parent and child, denoted by their Ids
     * @param parentId      Id of the Parent Node
     * @param childId       Id of the Child Node
     * @return              Edge which connects the given Parent and Child
     */
    private Edge generateEdge(String parentId, String childId) {
        NodeType parentType = getSiddhiElementType(getElementById(parentId));
        NodeType childType = getSiddhiElementType(getElementById(childId));
        String edgeId = DesignGeneratorHelper.generateEdgeID(parentId, childId);
        return new Edge(edgeId, parentId, parentType, childId, childType);
    }

    /**
     * Gets SiddhiElementConfig object from the SiddhiAppConfig, that has the given Id
     * @param id        Id of the Siddhi Element
     * @return          SiddhiElementConfig object
     */
    private SiddhiElementConfig getElementById(String id) {
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
        throw new IllegalArgumentException("Unable to find an element with the id '" + id + "'");
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
        throw new IllegalArgumentException("Type is unknown for Siddhi Element '" + siddhiElementConfig.getId() + "'");
    }
}
