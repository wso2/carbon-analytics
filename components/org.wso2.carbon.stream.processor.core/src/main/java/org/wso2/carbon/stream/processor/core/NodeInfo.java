/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stream.processor.core;

/**
 * Class that holds information about node's statistics for Status Dashboard.
 */
public class NodeInfo {

    private String nodeId;
    private String groupId;
    private DeploymentMode mode;
    private boolean isActiveNode;
    private long lastPersistedTimestamp;
    private long lastSyncedTimestamp;
    private boolean isInSync;

    public NodeInfo(DeploymentMode mode, String nodeId) {
        this.mode = mode;
        this.nodeId = nodeId;
    }

    public DeploymentMode getMode() {
        return mode;
    }

    public void setMode(DeploymentMode mode) {
        this.mode = mode;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isActiveNode() {
        return isActiveNode;
    }

    public void setActiveNode(boolean activeNode) {
        isActiveNode = activeNode;
    }

    public long getLastPersistedTimestamp() {
        return lastPersistedTimestamp;
    }

    public void setLastPersistedTimestamp(long lastPersistedTimestamp) {
        this.lastPersistedTimestamp = lastPersistedTimestamp;
    }

    public long getLastSyncedTimestamp() {
        return lastSyncedTimestamp;
    }

    public void setLastSyncedTimestamp(long lastSyncedTimestamp) {
        this.lastSyncedTimestamp = lastSyncedTimestamp;
    }

    public boolean isInSync() {
        return isInSync;
    }

    public void setInSync(boolean inSync) {
        isInSync = inSync;
    }
}
