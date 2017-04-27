/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cluster.coordinator.commons.node;

import java.util.Map;

/**
 * This class contains node heartbeat data of a specific node.
 */
public class NodeDetail {

    /**
     * Node ID of the belonging node.
     */
    private final String nodeId;
    /**
     * Group ID of the belonging node.
     */
    private final String groupId;
    /**
     * The last updated heartbeat value.
     */
    private final long lastHeartbeat;
    /**
     * Indicate if the node addition is already identified by the coordinator.
     */
    private final boolean isNewNode;
    /**
     * The property map of the current node.
     */
    private Map<String, Object> propertiesMap;
    /**
     * Indicate if the node is the coordinator node.
     */
    private boolean isCoordinator;

    /**
     * NodeDetail constructor.
     *
     * @param nodeId        node ID
     * @param groupId       cluster agent's socket address
     * @param lastHeartbeat last heartbeat received from the node
     * @param isNewNode     true if new node
     */
    public NodeDetail(String nodeId, String groupId, boolean isCoordinator, long lastHeartbeat,
            boolean isNewNode, Map<String, Object> propertiesMap) {
        this.nodeId = nodeId;
        this.lastHeartbeat = lastHeartbeat;
        this.isNewNode = isNewNode;
        this.groupId = groupId;
        this.propertiesMap = propertiesMap;
        this.isCoordinator = isCoordinator;
    }

    /**
     * Getter method for Node ID.
     *
     * @return node ID
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Getter method for last heartbeat.
     *
     * @return last heartbeat received form the node
     */
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    /**
     * Getter method for isNewNode.
     *
     * @return true if this is a new node
     */
    public boolean isNewNode() {
        return isNewNode;
    }

    /**
     * Getter method for groupID.
     *
     * @return groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Getter method for isCoordinator.
     *
     * @return true if this is the coordinator
     */
    public boolean isCoordinator() {
        return isCoordinator;
    }

    /**
     * The getter method for property map
     *
     * @return property map
     */
    public Map<String, Object> getpropertiesMap() {
        return propertiesMap;
    }
}
