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

package org.wso2.carbon.das.jobmanager.core.deployment;

import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.DeploymentManager;
import org.wso2.carbon.das.jobmanager.core.ResourcePoolChangeListener;
import org.wso2.carbon.das.jobmanager.core.SiddhiAppDeployer;
import org.wso2.carbon.das.jobmanager.core.appCreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.das.jobmanager.core.appCreator.DistributedSiddhiQuery;
import org.wso2.carbon.das.jobmanager.core.appCreator.SiddhiQuery;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.das.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.das.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.das.jobmanager.core.model.SiddhiAppHolder;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DeploymentManagerImpl implements DeploymentManager, ResourcePoolChangeListener {
    private static final Logger LOG = Logger.getLogger(DeploymentManagerImpl.class);
    private Iterator resourceIterator;

    @Override
    public DeploymentStatus deploy(DistributedSiddhiQuery distributedSiddhiQuery) {
        Map<String, List<SiddhiAppHolder>> siddhiAppHoldersMap = ServiceDataHolder
                .getResourcePool().getSiddhiAppHoldersMap();
        List<SiddhiAppHolder> siddhiAppHolders = new ArrayList<>();
        boolean isDeployed = true;
        if (siddhiAppHoldersMap.containsKey(distributedSiddhiQuery.getAppName())) {
            LOG.warn("Siddhi app " + distributedSiddhiQuery.getAppName() + " already deployed. " +
                    "Hence, un-deploying existing Siddhi app.");
            rollback(siddhiAppHoldersMap.get(distributedSiddhiQuery.getAppName()));
        }
        for (DeployableSiddhiQueryGroup queryGroup : distributedSiddhiQuery.getQueryGroups()) {
            for (SiddhiQuery query : queryGroup.getSiddhiQueries()) {
                ResourceNode node = getNextResourceNode();
                if (node != null) {
                    siddhiAppHolders.add(new SiddhiAppHolder(distributedSiddhiQuery.getAppName(),
                            queryGroup.getGroupName(), query.getAppName(), query.getApp(), node));
                    String appName = SiddhiAppDeployer.deploy(node, query);
                    if (appName == null || appName.isEmpty()) {
                        LOG.error("Couldn't deploy " + distributedSiddhiQuery.getAppName() + ". Hence, rolling back.");
                        isDeployed = false;
                        break;
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Siddhi app %s of %s deployed successfully.", appName,
                                    distributedSiddhiQuery.getAppName()));
                        }
                    }
                } else {
                    LOG.error("Insufficient resources to deploy Siddhi app: " + distributedSiddhiQuery.getAppName());
                    isDeployed = false;
                    break;
                }
            }
        }
        if (isDeployed) {
            siddhiAppHoldersMap.put(distributedSiddhiQuery.getAppName(), siddhiAppHolders);
        } else {
            rollback(siddhiAppHolders);
        }
        return getDeploymentStatus(isDeployed, siddhiAppHolders);
    }

    private DeploymentStatus getDeploymentStatus(boolean isDeployed, List<SiddhiAppHolder> siddhiAppHolders) {
        return new DeploymentStatus(isDeployed, siddhiAppHolders.stream()
                .collect(Collectors.groupingBy(SiddhiAppHolder::getGroupName,
                        Collectors.mapping(o -> o.getDeployedNode().getHttpInterface().getHost(),
                                Collectors.toList()))));
    }

    @Override
    public boolean unDeploy(String siddhiAppName) {
        boolean unDeployed = false;
        Map<String, List<SiddhiAppHolder>> siddhiAppHoldersMap = ServiceDataHolder
                .getResourcePool().getSiddhiAppHoldersMap();
        if (siddhiAppHoldersMap.containsKey(siddhiAppName)) {
            LOG.info("Un deploying Siddhi app " + siddhiAppName);
            rollback(siddhiAppHoldersMap.get(siddhiAppName));
            unDeployed = true;
        } else {
            LOG.warn("Siddhi app " + siddhiAppName + " is not deployed. Therefore, cannot un deploy the Siddhi App.");
        }
        return unDeployed;
    }

    @Override
    public boolean isDeployed(String parentSiddhiAppName) {
        Map<String, List<SiddhiAppHolder>> siddhiAppHoldersMap = ServiceDataHolder
                .getResourcePool().getSiddhiAppHoldersMap();
        Map<String, List<SiddhiAppHolder>> waitingAppList = ServiceDataHolder
                .getResourcePool().getAppsWaitingForDeploy();
        return siddhiAppHoldersMap.containsKey(parentSiddhiAppName)
                || waitingAppList.containsKey(parentSiddhiAppName);
    }

    /**
     * Get an available {@link ResourceNode} in round robin manner.
     *
     * @return a {@link ResourceNode}
     */
    private ResourceNode getNextResourceNode() {
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        if (resourceIterator == null) {
            resourceIterator = resourcePool.getResourceNodeMap().values().iterator();
        }
        if (resourceIterator.hasNext()) {
            return (ResourceNode) resourceIterator.next();
        } else {
            resourceIterator = resourcePool.getResourceNodeMap().values().iterator();
            if (resourceIterator.hasNext()) {
                return (ResourceNode) resourceIterator.next();
            } else {
                return null;
            }
        }
    }

    @Override
    public void resourceAdded(ResourceNode resourceNode) {
        // Refresh iterator after the pool change (since new node added).
        resourceIterator = ServiceDataHolder.getResourcePool().getResourceNodeMap().values().iterator();
        Map<String, List<SiddhiAppHolder>> waitingList = ServiceDataHolder.getResourcePool().getAppsWaitingForDeploy();
        Set<String> waitingParentAppNames = new HashSet<>(waitingList.keySet());
        List<SiddhiAppHolder> partialAppHoldersOfSiddhiApp;
        List<SiddhiAppHolder> currentDeployedPartialApps;
        boolean deployedCompletely;
        for (String parentSiddhiAppName : waitingParentAppNames) {
            partialAppHoldersOfSiddhiApp = waitingList.get(parentSiddhiAppName);
            deployedCompletely = true;
            currentDeployedPartialApps = new ArrayList<>();
            for (SiddhiAppHolder partialAppHolder : partialAppHoldersOfSiddhiApp) {
                ResourceNode deployedNode = deploy(
                        new SiddhiQuery(partialAppHolder.getAppName(), partialAppHolder.getSiddhiApp()), 0);
                if (deployedNode != null) {
                    partialAppHolder.setDeployedNode(deployedNode);
                    currentDeployedPartialApps.add(partialAppHolder);
                } else {
                    deployedCompletely = false;
                    break;
                }
            }
            if (deployedCompletely) {
                ServiceDataHolder.getResourcePool().getSiddhiAppHoldersMap()
                        .put(parentSiddhiAppName, partialAppHoldersOfSiddhiApp);
                waitingList.remove(parentSiddhiAppName);
                currentDeployedPartialApps = null;
            } else {
                LOG.warn(String.format("Still insufficient resources to deploy %s. Hence, rolling back the " +
                        "deployment and waiting for additional resources.", parentSiddhiAppName));
                rollback(currentDeployedPartialApps);
            }
        }
    }

    @Override
    public void resourceRemoved(ResourceNode resourceNode) {
        // Refresh iterator after the pool change (since node get removed).
        resourceIterator = ServiceDataHolder.getResourcePool().getResourceNodeMap().values().iterator();
        List<SiddhiAppHolder> affectedPartialApps = getNodeAppMapping().get(resourceNode);
        if (affectedPartialApps != null) {
            LOG.info(String.format("Siddhi apps %s were affected by the removal of node %s. Hence, re-deploying them " +
                    "in other resource nodes.", affectedPartialApps, resourceNode));
            rollback(affectedPartialApps);
            affectedPartialApps.forEach(affectedPartialApp -> {
                ResourceNode deployedNode = deploy(
                        new SiddhiQuery(affectedPartialApp.getAppName(), affectedPartialApp.getSiddhiApp()), 0);
                if (deployedNode != null) {
                    affectedPartialApp.setDeployedNode(deployedNode);
                } else {
                    LOG.warn(String.format("Insufficient resources to deploy %s. Therefore, cannot re-balance Siddhi " +
                                    "app %s. Hence, rolling back the deployment and waiting for additional resources.",
                            affectedPartialApp.getAppName(), affectedPartialApp.getParentAppName()));
                    List<SiddhiAppHolder> appHolders = ServiceDataHolder.getResourcePool()
                            .getSiddhiAppHoldersMap().remove(affectedPartialApp.getParentAppName());
                    if (appHolders != null) {
                        appHolders.forEach(e -> e.setDeployedNode(null));
                        rollback(appHolders);
                        ServiceDataHolder.getResourcePool().getAppsWaitingForDeploy()
                                .put(affectedPartialApp.getParentAppName(), appHolders);
                    }
                }
            });
        }
    }

    private ResourceNode deploy(SiddhiQuery siddhiQuery, int retry) {
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        ResourceNode resourceNode = getNextResourceNode();
        ResourceNode deployedNode = null;
        if (resourceNode != null) {
            String appName = SiddhiAppDeployer.deploy(resourceNode, siddhiQuery);
            if (appName == null || appName.isEmpty()) {
                LOG.warn(String.format("Couldn't deploy partial Siddhi app %s in %s", siddhiQuery.getAppName(),
                        resourceNode));
                if (retry < resourcePool.getResourceNodeMap().size()) {
                    deployedNode = deploy(siddhiQuery, retry + 1);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.warn(String.format("Couldn't deploy partial Siddhi app %s even after %s attempts.",
                                siddhiQuery.getAppName(), retry));
                    }
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Partial Siddhi app %s successfully deployed in %s.",
                            appName, resourceNode));
                }
                deployedNode = resourceNode;
            }
        }
        return deployedNode;
    }

    /**
     * Rollback (un-deploy) already deployed Siddhi apps.
     *
     * @param siddhiAppHolders list of Siddhi app holders to be un deployed.
     */
    private void rollback(List<SiddhiAppHolder> siddhiAppHolders) {
        if (siddhiAppHolders != null) {
            siddhiAppHolders.forEach(appHolder -> {
                if (!SiddhiAppDeployer.unDeploy(appHolder.getDeployedNode(), appHolder.getAppName())) {
                    LOG.warn(String.format("Could not un-deploy Siddhi app %s from %s.",
                            appHolder.getAppName(), appHolder.getDeployedNode()));
                } else {
                    appHolder.setDeployedNode(null);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Siddhi app %s un-deployed from %s.",
                                appHolder.getAppName(), appHolder.getDeployedNode()));
                    }
                }
            });
        }
    }

    /**
     * This will return ResourceNode -> List of SiddhiAppHolders mapping
     * mapping using the siddhiAppHoldersMap.
     *
     * @return ResourceNode to List of SiddhiAppHolders mapping.
     */
    private Map<ResourceNode, List<SiddhiAppHolder>> getNodeAppMapping() {
        Map<String, List<SiddhiAppHolder>> siddhiAppHoldersMap = ServiceDataHolder
                .getResourcePool().getSiddhiAppHoldersMap();
        return siddhiAppHoldersMap.values().stream().flatMap(List::stream)
                .collect(Collectors.groupingBy(SiddhiAppHolder::getDeployedNode));
    }
}
