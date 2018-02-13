package org.wso2.carbon.siddhi.editor.core.util.eventflow;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventFlow {

    // TODO: 2/2/18 Add license headers to all the classes
    private SiddhiAppInfo siddhiAppInfo;

    private JSONObject eventFlow = new JSONObject();

    private JSONArray nodes = new JSONArray();
    private JSONArray edges = new JSONArray();
    private JSONArray groups = new JSONArray();

    public EventFlow(SiddhiAppInfo siddhiAppInfo) {
        this.siddhiAppInfo = siddhiAppInfo;
        setEventFlowJSON();
    }

    private void setEventFlowJSON() {
        // Set App Name & Description
        eventFlow.put("appName", siddhiAppInfo.getAppName());
        eventFlow.put("appDescription", siddhiAppInfo.getAppDescription());

        // Set Nodes
        setNodes();

        // Set Edges
        setEdges();

        // Set Groups
        setGroups();
    }

    private void setNodes() {
        // Set Trigger Nodes
        for (TriggerInfo trigger : siddhiAppInfo.getTriggers()) {
            createNode("trigger", trigger.getId(), trigger.getName(), trigger.getDefinition());
        }

        // Set Stream Nodes
        for (StreamInfo stream : siddhiAppInfo.getStreams()) {
            createNode("stream", stream.getId(), stream.getName(), stream.getDefinition());
        }

        // Set Table Nodes
        for (TableInfo table : siddhiAppInfo.getTables()) {
            createNode("table", table.getId(), table.getName(), table.getDefinition());
        }

        // Set Window Nodes
        for (WindowInfo window : siddhiAppInfo.getWindows()) {
            createNode("window", window.getId(), window.getName(), window.getDefinition());
        }

        // Set Aggregation Nodes
        for (AggregationInfo aggregation : siddhiAppInfo.getAggregations()) {
            createNode("aggregation", aggregation.getId(), aggregation.getName(), aggregation.getDefinition());
        }

        // Set Function Nodes
        for (FunctionInfo function : siddhiAppInfo.getFunctions()) {
            createNode("function", function.getId(), function.getName(), function.getDefinition());
        }

        // Set Query Nodes
        for (QueryInfo query : siddhiAppInfo.getQueries()) {
            createQueryNode(query);
        }

        // Set Partition, It's Query & PartitionType Nodes
        for (PartitionInfo partition : siddhiAppInfo.getPartitions()) {
            createNode("partition", partition.getId(), partition.getName(), partition.getDefinition());

            // Create Nodes For The Queries Inside The Partition
            for (QueryInfo query : partition.getQueries()) {
                createQueryNode(query);
            }

            // Create Nodes For The Range & Value Partitions Inside The Partition
            for (PartitionTypeInfo partitionType : partition.getPartitionTypes()) {
                createNode("partitionType", partitionType.getId(), partitionType.getName(), partitionType.getDefinition());
            }
        }

        // Add All The Created Nodes To The Event Flow
        eventFlow.put("nodes", nodes);
    }

    private void createNode(String type, String id, String name, String description) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        jsonObject.put("description", description);

        // Add The JSONObject To The Current Set Of Nodes
        nodes.put(jsonObject);
    }

    private void createQueryNode(QueryInfo queryInfo) {
        // If The OutputStream Of This Query Has Not Been Defined,
        // Create Another Node For The Undefined OutputStream
        boolean isOutputQueryDefined = false;
        for (int i = 0; i < nodes.length(); i++) {
            if (nodes.getJSONObject(i).get("id").equals(queryInfo.getOutputStreamId())) {
                isOutputQueryDefined = true;
                break;
            }
        }
        // If The Query's OutputStream Does Not Exist
        if (!isOutputQueryDefined) {
            // Create The New OutputStream Node
            createNode("stream", queryInfo.getOutputStreamId(), queryInfo.getOutputStreamId(),
                    "undefined");
        }

        // Create The Actual Query Node
        createNode("query", queryInfo.getId(), queryInfo.getName(), queryInfo.getDefinition());
    }

    private void setEdges() {
        // Set Edges With Aggregations
        for (AggregationInfo aggregation : siddhiAppInfo.getAggregations()) {
            createEdge("arrow", aggregation.getInputStreamId(), aggregation.getId());
        }

        // Set Edges With Queries
        for (QueryInfo query : siddhiAppInfo.getQueries()) {

            for (String inputStreamId : query.getInputStreamIds()) {
                createEdge("arrow", inputStreamId, query.getId());
            }

            createEdge("arrow", query.getId(), query.getOutputStreamId());

            for (String functionId : query.getFunctionIds()) {
                createEdge("arrow", functionId, query.getId());
            }

            // Search for the 'in' keyword
            Pattern pattern = Pattern.compile("\\b[i|I][n|N]\\b\\s+(\\w+)");
            Matcher matcher = pattern.matcher(query.getDefinition());
            while (matcher.find()) {
                String tableId = matcher.group(1);
                createEdge("dotted-line", tableId, query.getId());
            }
        }

        // Set Edges With Partition Queries
        for (PartitionInfo partition : siddhiAppInfo.getPartitions()) {

            // For Each Query In This Partition
            for (QueryInfo query : partition.getQueries()) {

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
                        createEdge("arrow", inputStreamId, partitionedStreamId);
                        createEdge("arrow", partitionedStreamId, query.getId());
                    } else {
                        // If The Input Stream Is Not Partitioned
                        createEdge("arrow", inputStreamId, query.getId());
                    }

                }

                // Connect The Query With Its Output Stream
                createEdge("arrow", query.getId(), query.getOutputStreamId());

                // Connect All The Functions That Are Used By This Query
                for (String functionId : query.getFunctionIds()) {
                    createEdge("arrow", functionId, query.getId());
                }

                // Connects Any Tables That Are Used By The Queries Using The In Keyword
                Pattern pattern = Pattern.compile("\\b[i|I][n|N]\\b\\s+(\\w+)");
                Matcher matcher = pattern.matcher(query.getDefinition());
                while (matcher.find()) {
                    String tableId = matcher.group(1);
                    createEdge("dotted-line", tableId, query.getId());
                }

            }

        }

        // Set All The Edges To The Event Flow
        eventFlow.put("edges", edges);
    }

    private void createEdge(String type, String parent, String child) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("parent", parent);
        jsonObject.put("child", child);

        // Add The Created Edges To The JSONArray
        edges.put(jsonObject);
    }

    private void setGroups() {
        // Groups Are Only For Partitions
        for (PartitionInfo partition : siddhiAppInfo.getPartitions()) {
            createGroup(partition.getId(), partition.getName(), partition.getQueries(), partition.getPartitionTypes());
        }

        eventFlow.put("groups", groups);
    }

    private void createGroup(String id, String name, List<QueryInfo> queries, List<PartitionTypeInfo> partitionTypes) {
        // Create A Group To Define A Partition
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);

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

        jsonObject.put("children", children.toArray());

        // Add The Created JSONObject To The Groups JSONArray
        groups.put(jsonObject);
    }

    // Getters
    public JSONObject getEventFlowJSON() {
        return eventFlow;
    }

}
