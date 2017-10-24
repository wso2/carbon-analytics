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

package org.wso2.carbon.das.jobmanager.core.model;

import org.wso2.carbon.das.jobmanager.core.ResourceManager;
import org.wso2.carbon.das.jobmanager.core.internal.HeartbeatMonitor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcePool implements Serializable {
    private static final long serialVersionUID = 2606866798031783615L;
    private String groupId;
    private ManagerNode leaderNode;
    private ResourceManager resourceManager;
    private Map<String, ManagerNode> managerNodeMap;
    private Map<String, ResourceNode> resourceNodeMap;
    private HeartbeatMonitor heartbeatMonitor;

    public ResourcePool(String groupId) {
        this.groupId = groupId;
        this.managerNodeMap = new ConcurrentHashMap<>();
        this.resourceNodeMap = new ConcurrentHashMap<>();
        this.heartbeatMonitor = new HeartbeatMonitor();
        this.resourceManager = new ResourceManager();
        heartbeatMonitor.registerHeartbeatChangeListener(resourceManager);
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public ManagerNode getLeaderNode() {
        return leaderNode;
    }

    public void setLeaderNode(ManagerNode leaderNode) {
        this.leaderNode = leaderNode;
    }

    public Map<String, ManagerNode> getManagerNodeMap() {
        return managerNodeMap;
    }

    public void setManagerNodeMap(Map<String, ManagerNode> managerNodeMap) {
        this.managerNodeMap = managerNodeMap;
    }

    public void addManagerNode(ManagerNode managerNode) {
        this.managerNodeMap.put(managerNode.getId(), managerNode);
    }

    public void removeManagerNode(ManagerNode managerNode) {
        this.managerNodeMap.remove(managerNode.getId());
    }

    public Map<String, ResourceNode> getResourceNodeMap() {
        return resourceNodeMap;
    }

    public void setResourceNodeMap(Map<String, ResourceNode> resourceNodeMap) {
        this.resourceNodeMap = resourceNodeMap;
    }


    public void addResourceNode(ResourceNode resourceNode) {
        this.resourceNodeMap.put(resourceNode.getId(), resourceNode);
    }

    public void removeResourceNode(ResourceNode resourceNode) {
        this.resourceNodeMap.remove(resourceNode.getId());
    }

    public HeartbeatMonitor getHeartbeatMonitor() {
        return heartbeatMonitor;
    }

    public void setHeartbeatMonitor(HeartbeatMonitor heartbeatMonitor) {
        this.heartbeatMonitor = heartbeatMonitor;
    }


}
