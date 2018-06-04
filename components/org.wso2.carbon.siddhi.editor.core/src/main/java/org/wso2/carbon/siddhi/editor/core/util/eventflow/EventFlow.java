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

package org.wso2.carbon.siddhi.editor.core.util.eventflow;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.constants.EdgeType;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.constants.NodeType;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.AggregationInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.FunctionInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.PartitionInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.PartitionTypeInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.QueryInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.SinkInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.SourceInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.StreamInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.TableInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.TriggerInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.WindowInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Obtains a SiddhiAppMap instance and generate's a JSON with a predefined format for the graph view in SP Editor UI.
 */
public class EventFlow {

    private static final String APP_NAME = "appName";
    private static final String APP_DESCRIPTION = "appDescription";
    private static final String TYPE = "type";
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String DESCRIPTION = "description";
    private static final String PARENT = "parent";
    private static final String CHILD = "child";
    private static final String NODE = "nodes";
    private static final String EDGE = "edges";
    private static final String GROUP = "groups";
    private static final String CHILDREN = "children";
    private static final Pattern PATTERN = Pattern.compile("\\b[i|I][n|N]\\b\\s+(\\w+)");

    private SiddhiAppMap siddhiAppMap;

    private JSONObject eventFlowJSON = new JSONObject();

    private JSONArray nodes = new JSONArray();
    private JSONArray edges = new JSONArray();
    private JSONArray groups = new JSONArray();

    public EventFlow(SiddhiAppMap siddhiAppMap) {
        this.siddhiAppMap = siddhiAppMap;
        setEventFlowJSON();
    }

    /**
     * Main method that is called in the constructor to generate a JSON
     * object from the provided SiddhiAppMap object.
     */
    private void setEventFlowJSON() {
        eventFlowJSON.put(APP_NAME, siddhiAppMap.getAppName());
        eventFlowJSON.put(APP_DESCRIPTION, siddhiAppMap.getAppDescription());
        setNodes();
        setEdges();
        setGroups();
    }

    /**
     * Creates all the node objects and adds it to the nodes JSONArray
     * which is then added to the returned JSONObject.
     */
    private void setNodes() {
        // Create Trigger Nodes
        for (TriggerInfo trigger : siddhiAppMap.getTriggers()) {
            createNode(NodeType.TRIGGER, trigger.getId(), trigger.getName(), trigger.getDefinition());
        }

        // Create Stream Nodes
        for (StreamInfo stream : siddhiAppMap.getStreams()) {
            createNode(NodeType.STREAM, stream.getId(), stream.getName(), stream.getDefinition());
        }

        // Create Source Nodes
        for (SourceInfo source : siddhiAppMap.getSources()) {
            createNode(NodeType.SOURCE, source.getId(), source.getName(), source.getDefinition());
        }

        // Create Sink Nodes
        for (SinkInfo sink : siddhiAppMap.getSinks()) {
            createNode(NodeType.SINK, sink.getId(), sink.getName(), sink.getDefinition());
        }

        // Create Table Nodes
        for (TableInfo table : siddhiAppMap.getTables()) {
            createNode(NodeType.TABLE, table.getId(), table.getName(), table.getDefinition());
        }

        // Create Window Nodes
        for (WindowInfo window : siddhiAppMap.getWindows()) {
            createNode(NodeType.WINDOW, window.getId(), window.getName(), window.getDefinition());
        }

        // Create Aggregation Nodes
        for (AggregationInfo aggregation : siddhiAppMap.getAggregations()) {
            createNode(NodeType.AGGREGATION, aggregation.getId(), aggregation.getName(), aggregation.getDefinition());
        }

        // Create Function Nodes
        for (FunctionInfo function : siddhiAppMap.getFunctions()) {
            createNode(NodeType.FUNCTION, function.getId(), function.getName(), function.getDefinition());
        }

        // Create Query Nodes
        for (QueryInfo query : siddhiAppMap.getQueries()) {
            createNode(NodeType.QUERY, query.getId(), query.getName(), query.getDefinition());
        }

        // Create Partition Nodes
        for (PartitionInfo partition : siddhiAppMap.getPartitions()) {
            createNode(NodeType.PARTITION, partition.getId(), partition.getName(), partition.getDefinition());

            // Create Nodes For The Queries Inside The Partition
            for (QueryInfo query : partition.getQueries()) {
                createNode(NodeType.QUERY, query.getId(), query.getName(), query.getDefinition());
            }

            // Create Nodes For The Range & Value Partition Types Inside The Partition
            for (PartitionTypeInfo partitionType : partition.getPartitionTypes()) {
                createNode(NodeType.PARTITION_TYPE, partitionType.getId(), partitionType.getName(),
                        partitionType.getDefinition());
            }
        }

        // Add All The Created Nodes To The Event Flow
        eventFlowJSON.put(NODE, nodes);
    }

    /**
     * Creates a JSONObject that defines a node and adds it to the nodes JSONArray.
     *
     * @param type        Defines the type of node (Ex: stream, table, trigger etc.)
     * @param id          A unique value given to identify a particular node
     * @param name        The name of the node (not always unique - this is the name that will be displayed in the UI)
     * @param description A piece of the Siddhi code where the node (stream, table etc.) is defined
     */
    private void createNode(NodeType type, String id, String name, String description) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TYPE, type.getTypeAsString());
        jsonObject.put(ID, id);
        jsonObject.put(NAME, name);
        jsonObject.put(DESCRIPTION, description);
        nodes.put(jsonObject);
    }

    /**
     * Calls all the relevant functions to create the edge JSONObjects
     * and then add these JSONObjects to the edges JSONArray.
     */
    private void setEdges() {
        setSourceEdges();
        setSinkEdges();
        setAggregationEdges();
        setQueryEdges();
        setPartitionEdges();
        eventFlowJSON.put(EDGE, edges);
    }

    /**
     * Creates all the edge JSONObjects for all the Sources in the SiddhhiAppMap object.
     */
    private void setSourceEdges() {
        for (SourceInfo source : siddhiAppMap.getSources()) {
            createEdge(EdgeType.DEFAULT, source.getId(), source.getStreamId());
        }
    }

    /**
     * Creates all the edge JSONObjects for all the Sinks in the SiddhiAppMap object.
     */
    private void setSinkEdges() {
        for (SinkInfo sink : siddhiAppMap.getSinks()) {
            createEdge(EdgeType.DEFAULT, sink.getStreamId(), sink.getId());
        }
    }

    /**
     * Creates all the edge JSONObjects for all the aggregations in the SiddhiAppMap object.
     */
    private void setAggregationEdges() {
        for (AggregationInfo aggregation : siddhiAppMap.getAggregations()) {
            createEdge(EdgeType.DEFAULT, aggregation.getInputStreamId(), aggregation.getId());
        }
    }

    /**
     * Creates all the edge JSONObjects for all the queries in the SiddhiAppMap object.
     */
    private void setQueryEdges() {
        // Set Edges With Queries
        for (QueryInfo query : siddhiAppMap.getQueries()) {
            // For Each Input Stream Id In The Query
            for (String inputStreamId : query.getInputStreamIds()) {
                createEdge(EdgeType.DEFAULT, inputStreamId, query.getId());
            }

            // Create An Edge Between The Query And It's OutputStream
            createEdge(EdgeType.DEFAULT, query.getId(), query.getOutputStreamId());

            // For Every User Defined Function Used In The Query
            for (String functionId : query.getFunctionIds()) {
                createEdge(EdgeType.DEFAULT, functionId, query.getId());
            }

            // Search For The 'in' Keyword Inside The Query To `Create Dotted Lined Edge`
            Matcher matcher = PATTERN.matcher(query.getDefinition());
            while (matcher.find()) {
                String tableId = matcher.group(1);
                createEdge(EdgeType.DOTTED_LINE, tableId, query.getId());
            }
        }
    }

    /**
     * Creates all the edge JSONObjects for all the partitions in the SiddhiAppMap object.
     */
    private void setPartitionEdges() {
        // Set Edges With Partition Queries
        for (PartitionInfo partition : siddhiAppMap.getPartitions()) {
            // For Each Query In This Partition
            for (QueryInfo query : partition.getQueries()) {
                createEdgesForQueryInPartition(query, partition);
            }
        }
    }

    /**
     * Creates all the edges for a particular query inside a partition.
     *
     * @param query     The query in which the edges should be made to and from
     * @param partition The partition in which the given query belongs to
     */
    private void createEdgesForQueryInPartition(QueryInfo query, PartitionInfo partition) {
        // For Each Input Stream In This Query
        for (String inputStreamId : query.getInputStreamIds()) {
            // Check Whether This Input Stream Is Partitioned Or Not
            String partitionedStreamId = null;
            for (PartitionTypeInfo partitionType : partition.getPartitionTypes()) {
                if (inputStreamId.equals(partitionType.getStreamId())) {
                    partitionedStreamId = partitionType.getId();
                    break;
                }
            }

            if (partitionedStreamId != null) {
                // If The Input Stream Is Partitioned
                createEdge(EdgeType.DEFAULT, inputStreamId, partitionedStreamId);
                createEdge(EdgeType.DEFAULT, partitionedStreamId, query.getId());
            } else {
                // If The Input Stream Is Not Partitioned
                createEdge(EdgeType.DEFAULT, inputStreamId, query.getId());
            }
        }

        // Connect The Query With Its Output Stream
        createEdge(EdgeType.DEFAULT, query.getId(), query.getOutputStreamId());

        // Connect All The Functions That Are Used By This Query
        for (String functionId : query.getFunctionIds()) {
            createEdge(EdgeType.DEFAULT, functionId, query.getId());
        }

        // Connects Any Tables That Are Used By The Queries Using The 'in' Keyword
        Matcher matcher = PATTERN.matcher(query.getDefinition());
        while (matcher.find()) {
            String tableId = matcher.group(1);
            createEdge(EdgeType.DOTTED_LINE, tableId, query.getId());
        }
    }

    /**
     * Creates a JSONObject that defines an edge and adds it to the edges JSONArray.
     *
     * @param edgeType The type of edge (can be either a 'arrow' or 'dotted-line')
     * @param parent   The Id of the parent node where the edge starts from
     * @param child    The Id of the child node where the edge should point towards
     */
    private void createEdge(EdgeType edgeType, String parent, String child) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TYPE, edgeType.getTypeAsString());
        jsonObject.put(PARENT, parent);
        jsonObject.put(CHILD, child);
        edges.put(jsonObject);
    }

    /**
     * Creates all the group objects and adds it to the groups JSONArray
     * which is then added to the returned JSONObject.
     */
    private void setGroups() {
        // Create A Group For Every Partition
        for (PartitionInfo partition : siddhiAppMap.getPartitions()) {
            createGroup(partition.getId(), partition.getName(), partition.getQueries(), partition.getPartitionTypes());
        }
        eventFlowJSON.put(GROUP, groups);
    }

    /**
     * Creates a JSONObject that defines a group and adds it to the groups JSONArray.
     * Groups are only meant for partitions as they define what streams/queries and partition types that
     * belong to a particular partition.
     *
     * @param id             The unique Id of the partition which the group belongs to
     * @param name           The name of the partition (i.e. the name to be displayed in the UI)
     * @param queries        The list of queries defined inside a particular partition
     * @param partitionTypes The list of nodes that define's how a particular stream is partitioned by
     */
    private void createGroup(String id, String name, List<QueryInfo> queries, List<PartitionTypeInfo> partitionTypes) {
        // Create A Group To Define A Partition
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ID, id);
        jsonObject.put(NAME, name);

        List<String> children = new ArrayList<>();
        // Add All The Queries To The Partition As Children
        for (QueryInfo query : queries) {
            children.add(query.getId());
            if (query.getOutputStreamId().substring(0, 1).equals("#")) {
                children.add(query.getOutputStreamId());
            }
        }
        // Add All The Partitioned Values As Children
        for (PartitionTypeInfo partitionType : partitionTypes) {
            children.add(partitionType.getId());
        }
        jsonObject.put(CHILDREN, children.toArray());

        // Add The Created JSONObject To The Groups JSONArray
        groups.put(jsonObject);
    }

    // Getters
    public JSONObject getEventFlowJSON() {
        return eventFlowJSON;
    }

}
