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
import org.wso2.carbon.das.jobmanager.core.ResourcePoolChangeListener;
import org.wso2.carbon.das.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.das.jobmanager.core.exception.ResourceManagerException;
import org.wso2.carbon.das.jobmanager.core.internal.ResourceNodeMonitor;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class ResourcePool implements Serializable {
    private static final Logger LOG = Logger.getLogger(ResourcePool.class);
    private static final long serialVersionUID = 2606866798031783615L;
    private String groupId;
    private ManagerNode leaderNode;
    private Map<String, ResourceNode> resourceNodeMap;
    /**
     * Map of parentSiddhiAppName -> List of SiddhiAppHolders.
     */
    private Map<String, List<SiddhiAppHolder>> siddhiAppHoldersMap;
    /**
     * List which hold list of apps which are waiting for new resource nodes.
     */
    private Map<String, List<SiddhiAppHolder>> appsWaitingForDeploy;
    private transient List<ResourcePoolChangeListener> poolChangeListeners;

    public ResourcePool(String groupId) {
        this.groupId = groupId;
        this.resourceNodeMap = new ConcurrentHashMap<>();
        this.siddhiAppHoldersMap = new ConcurrentHashMap<>();
        this.poolChangeListeners = new CopyOnWriteArrayList<>();
        this.appsWaitingForDeploy = new ConcurrentHashMap<>();
    }

    public void init() {
        this.poolChangeListeners = new CopyOnWriteArrayList<>();
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        setLeaderNode(ServiceDataHolder.getLeaderNode());
        registerResourcePoolChangeListener(ServiceDataHolder.getDeploymentManager());
        ServiceDataHolder.getExecutorService().scheduleAtFixedRate(
                new ResourceNodeMonitor(), deploymentConfig.getHeartbeatInterval(),
                deploymentConfig.getHeartbeatInterval(), TimeUnit.MILLISECONDS);

        // TODO: 11/1/17 Don't ues this for now
        //        List<String> deployedApps = new ArrayList<>();
        //        getResourceNodeMap().values().forEach(resourceNode
        //                -> deployedApps.addAll(SiddhiAppDeployer.getDeployedApps(resourceNode)));

    }

    public String getGroupId() {
        return groupId;
    }

    public ManagerNode getLeaderNode() {
        return leaderNode;
    }

    public void setLeaderNode(ManagerNode leaderNode) {
        this.leaderNode = leaderNode;
        persist();
    }

    public Map<String, ResourceNode> getResourceNodeMap() {
        return resourceNodeMap;
    }

    public void setResourceNodeMap(Map<String, ResourceNode> resourceNodeMap) {
        this.resourceNodeMap = resourceNodeMap;
    }

    public void addResourceNode(ResourceNode resourceNode) {
        this.resourceNodeMap.put(resourceNode.getId(), resourceNode);
        persist();
        poolChangeListeners.forEach(listener -> listener.resourceAdded(resourceNode));
        LOG.info(String.format("%s added to the resource pool.", resourceNode));
    }

    public void removeResourceNode(String nodeId) {
        ResourceNode resourceNode = this.resourceNodeMap.remove(nodeId);
        persist();
        poolChangeListeners.forEach(listener -> listener.resourceRemoved(resourceNode));
        LOG.info(String.format("%s removed from the resource pool.", resourceNode));
    }

    public Map<String, List<SiddhiAppHolder>> getSiddhiAppHoldersMap() {
        return siddhiAppHoldersMap;
    }

    public void setSiddhiAppHoldersMap(Map<String, List<SiddhiAppHolder>> siddhiAppHoldersMap) {
        this.siddhiAppHoldersMap = siddhiAppHoldersMap;
    }

    public void registerResourcePoolChangeListener(ResourcePoolChangeListener resourcePoolChangeListener) {
        this.poolChangeListeners.add(resourcePoolChangeListener);
    }

    public Map<String, List<SiddhiAppHolder>> getAppsWaitingForDeploy() {
        return appsWaitingForDeploy;
    }

    public void setAppsWaitingForDeploy(Map<String, List<SiddhiAppHolder>> appsWaitingForDeploy) {
        this.appsWaitingForDeploy = appsWaitingForDeploy;
    }

    public void persist() {
        try {
            ServiceDataHolder.getRdbmsService().persistResourcePool(ServiceDataHolder.getResourcePool());
        } catch (ResourceManagerException e) {
            LOG.error("Could not persist resource pool state to the database.", e);
        }
    }
}
