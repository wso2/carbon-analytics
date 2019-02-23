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

package org.wso2.carbon.sp.jobmanager.core.model;

import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.ResourcePoolChangeListener;
import org.wso2.carbon.sp.jobmanager.core.appcreator.DistributedSiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.exception.ResourceManagerException;
import org.wso2.carbon.sp.jobmanager.core.internal.ResourceNodeMonitor;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Details about Resource Pool.
 */
public class ResourcePool implements Serializable {
    private static final Logger LOG = Logger.getLogger(ResourcePool.class);
    private static final long serialVersionUID = 2606866798031783615L;
    private String groupId;
    private ManagerNode leaderNode;
    private Map<String, ResourceNode> resourceNodeMap;
    private Map<String, ResourceNode> receiverNodeMap;
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
        this.receiverNodeMap = new ConcurrentHashMap<>();
        this.siddhiAppHoldersMap = new ConcurrentHashMap<>();
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

    public Map<String, ResourceNode> getReceiverNodeMap() {
        return receiverNodeMap;
    }

    public void setResourceNodeMap(Map<String, ResourceNode> resourceNodeMap) {
        this.resourceNodeMap = resourceNodeMap;
    }

    public void setReceiverNodeMap(Map<String, ResourceNode> receiverNodeMap) {
        this.receiverNodeMap = receiverNodeMap;
    }

    public void addResourceNode(ResourceNode resourceNode) {
        this.resourceNodeMap.put(resourceNode.getId(), resourceNode);
        LOG.info(String.format("%s added to the resource pool.", resourceNode));
        persist();
        poolChangeListeners.forEach(listener -> listener.resourceAdded(resourceNode));
    }

    public void addReceiverNode(ResourceNode resourceNode) {
        this.receiverNodeMap.put(resourceNode.getId(), resourceNode);
        LOG.info(String.format("%s added to the resource pool as a receiver node.", resourceNode));
        persist();
        poolChangeListeners.forEach(listener -> listener.resourceAdded(resourceNode));

    }

    public void removeResourceNode(String nodeId) {
        ResourceNode resourceNode = this.resourceNodeMap.remove(nodeId);
        LOG.info(String.format("%s removed from the resource pool.", resourceNode));
        persist();
        poolChangeListeners.forEach(listener -> listener.resourceRemoved(resourceNode));
    }

    public void removeReceiverNode(String nodeId) {
        ResourceNode resourceNode = this.receiverNodeMap.remove(nodeId);
        LOG.info(String.format("Receiver node: %s removed from the resource pool.", resourceNode));
        persist();
        poolChangeListeners.forEach(listener -> listener.resourceRemoved(resourceNode));
    }

    public void notifyResourceNode(String nodeId, boolean redeploy, boolean receiverNode) {
        ResourceNode resourceNode;
        if (receiverNode) {
            resourceNode = receiverNodeMap.get(nodeId);
        } else {
            resourceNode = resourceNodeMap.get(nodeId);
        }
        if (resourceNode != null) {
            List<SiddhiAppHolder> deployedApps = getNodeAppMapping().get(resourceNode);
            if (deployedApps != null && !deployedApps.isEmpty()) {
                if (redeploy) {
                    ServiceDataHolder.getDeploymentManager().reDeployAppsInResourceNode(resourceNode);
                }
            } else {
                poolChangeListeners.forEach(listener -> listener.resourceAdded(resourceNode));
            }
        }
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

    /**
     * This will return ResourceNode -> List of SiddhiAppHolders mapping
     * mapping using the siddhiAppHoldersMap.
     *
     * @return ResourceNode to List of SiddhiAppHolders mapping.
     */
    public Map<ResourceNode, List<SiddhiAppHolder>> getNodeAppMapping() {
        return siddhiAppHoldersMap.values().stream().flatMap(List::stream).filter(siddhiAppHolder
                -> siddhiAppHolder
                .getDeployedNode() != null)
                .collect(Collectors.groupingBy(SiddhiAppHolder::getDeployedNode));
    }
}
