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

import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.ResourceManager;
import org.wso2.carbon.das.jobmanager.core.exception.ResourceManagerException;
import org.wso2.carbon.das.jobmanager.core.internal.HeartbeatMonitor;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcePool implements Serializable {
    private static final Logger LOG = Logger.getLogger(ResourcePool.class);
    private static final long serialVersionUID = 2606866798031783615L;
    private String groupId;
    private ManagerNode leaderNode;
    private Map<String, ResourceNode> resourceNodeMap;
    private transient ResourceManager resourceManager;
    private transient HeartbeatMonitor heartbeatMonitor;

    public ResourcePool(String groupId) {
        this.groupId = groupId;
        this.resourceNodeMap = new ConcurrentHashMap<>();
    }

    public void init() {
        this.heartbeatMonitor = new HeartbeatMonitor();
        this.resourceManager = new ResourceManager();
        for (String resourceNodeId : resourceNodeMap.keySet()) {
            heartbeatMonitor.updateHeartbeat(new Heartbeat(resourceNodeId));
        }
        // Register the listener after updating heartbeats, so that heartbeatAdded won't get triggered.
        heartbeatMonitor.registerHeartbeatChangeListener(resourceManager);
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public String getGroupId() {
        return groupId;
    }

    public ManagerNode getLeaderNode() {
        return leaderNode;
    }

    public void setLeaderNode(ManagerNode leaderNode) {
        this.leaderNode = leaderNode;
        persistResourcePool();
    }

    public Map<String, ResourceNode> getResourceNodeMap() {
        return resourceNodeMap;
    }

    public void setResourceNodeMap(Map<String, ResourceNode> resourceNodeMap) {
        this.resourceNodeMap = resourceNodeMap;
    }

    public void addResourceNode(ResourceNode resourceNode) {
        this.resourceNodeMap.put(resourceNode.getId(), resourceNode);
        persistResourcePool();
    }

    public void removeResourceNode(String nodeId) {
        this.resourceNodeMap.remove(nodeId);
        persistResourcePool();
    }

    public HeartbeatMonitor getHeartbeatMonitor() {
        return heartbeatMonitor;
    }

    public void setHeartbeatMonitor(HeartbeatMonitor heartbeatMonitor) {
        this.heartbeatMonitor = heartbeatMonitor;
    }

    private void persistResourcePool() {
        try {
            ServiceDataHolder.getRdbmsService().persistResourcePool(ServiceDataHolder.getResourcePool());
        } catch (ResourceManagerException e) {
            LOG.error("Could not persist resource pool state to the database.", e);
        }
    }
}
