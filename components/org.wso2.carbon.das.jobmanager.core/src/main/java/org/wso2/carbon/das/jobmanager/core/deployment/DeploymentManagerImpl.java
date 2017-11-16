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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.DeploymentManager;
import org.wso2.carbon.das.jobmanager.core.ResourcePoolChangeListener;
import org.wso2.carbon.das.jobmanager.core.SiddhiAppDeployer;
import org.wso2.carbon.das.jobmanager.core.appCreator.DistributedSiddhiQuery;
import org.wso2.carbon.das.jobmanager.core.appCreator.SiddhiQuery;
import org.wso2.carbon.das.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.das.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.das.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.das.jobmanager.core.model.SiddhiAppHolder;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
        Map<String, List<SiddhiAppHolder>> deployedSiddhiAppHoldersMap = ServiceDataHolder
                .getResourcePool().getSiddhiAppHoldersMap();
        List<SiddhiAppHolder> appsToDeploy = getSiddhiAppHolders(distributedSiddhiQuery);
        List<SiddhiAppHolder> deployedApps = new ArrayList<>();
        boolean shouldDeploy = true;

        if (deployedSiddhiAppHoldersMap.containsKey(distributedSiddhiQuery.getAppName())) {
            List<SiddhiAppHolder> existingApps = deployedSiddhiAppHoldersMap.get(distributedSiddhiQuery.getAppName());
            if (CollectionUtils.isEqualCollection(existingApps, appsToDeploy)) {
                boolean waitingToDeploy = false;
                for (SiddhiAppHolder app : existingApps) {
                    if (app.getDeployedNode() == null) {
                        waitingToDeploy = true;
                        break;
                    }
                }
                if (waitingToDeploy) {
                    LOG.info(String.format("Exact Siddhi app with name: %s is already exists in waiting mode. " +
                            "Hence, trying to re-deploy.", distributedSiddhiQuery.getAppName()));
                    rollback(existingApps);
                } else {
                    LOG.info(String.format("Exact Siddhi app with name: %s is already deployed.",
                            distributedSiddhiQuery.getAppName()));
                    shouldDeploy = false;
                }
            } else {
                LOG.info("Different Siddhi app with name:" + distributedSiddhiQuery.getAppName() + " is already " +
                        "deployed. Hence, un-deploying existing Siddhi app.");
                rollback(deployedSiddhiAppHoldersMap.get(distributedSiddhiQuery.getAppName()));
            }
        }
        boolean isDeployed = true;
        if (shouldDeploy) {
            for (SiddhiAppHolder appHolder : appsToDeploy) {
                ResourceNode deployedNode = deploy(new SiddhiQuery(appHolder.getAppName(),
                        appHolder.getSiddhiApp()), 0);
                if (deployedNode != null) {
                    appHolder.setDeployedNode(deployedNode);
                    deployedApps.add(appHolder);
                    LOG.info(String.format("Siddhi app %s of %s successfully deployed in %s.",
                            appHolder.getAppName(), appHolder.getParentAppName(), deployedNode));
                } else {
                    LOG.warn(String.format("Insufficient resources to deploy Siddhi app %s of %s. Hence, rolling back.",
                            appHolder.getAppName(), appHolder.getParentAppName()));
                    isDeployed = false;
                    break;
                }
            }
            if (isDeployed) {
                deployedSiddhiAppHoldersMap.put(distributedSiddhiQuery.getAppName(), deployedApps);
                LOG.info("Siddhi app " + distributedSiddhiQuery.getAppName() + " successfully deployed.");
            } else {
                rollback(deployedApps);
                deployedApps = Collections.emptyList();
                deployedSiddhiAppHoldersMap.remove(distributedSiddhiQuery.getAppName());
                ServiceDataHolder.getResourcePool().getAppsWaitingForDeploy()
                        .put(distributedSiddhiQuery.getAppName(), appsToDeploy);
                LOG.info("Siddhi app " + distributedSiddhiQuery.getAppName() + " held back in waiting mode.");
            }
        } else {
            deployedApps = deployedSiddhiAppHoldersMap.get(distributedSiddhiQuery.getAppName());
        }
        ServiceDataHolder.getResourcePool().persist();
        // Returning true as the deployment state, since we might put some apps on wait.
        return getDeploymentStatus(true, deployedApps);
    }

    private DeploymentStatus getDeploymentStatus(boolean isDeployed, List<SiddhiAppHolder> siddhiAppHolders) {
        Map<String, List<String>> deploymentDataMap = new HashMap<>();
        for (SiddhiAppHolder appHolder : siddhiAppHolders) {
            if (appHolder.getDeployedNode() != null && appHolder.getDeployedNode().getHttpInterface() != null) {
                if (deploymentDataMap.containsKey(appHolder.getGroupName())) {
                    deploymentDataMap.get(appHolder.getGroupName())
                            .add(appHolder.getDeployedNode().getHttpInterface().getHost());
                } else {
                    List<String> hosts = new ArrayList<>();
                    hosts.add(appHolder.getDeployedNode().getHttpInterface().getHost());
                    deploymentDataMap.put(appHolder.getGroupName(), hosts);
                }
            }
        }
        return new DeploymentStatus(isDeployed, deploymentDataMap);
    }

    private List<SiddhiAppHolder> getSiddhiAppHolders(DistributedSiddhiQuery distributedSiddhiQuery) {
        List<SiddhiAppHolder> siddhiAppHolders = new ArrayList<>();
        distributedSiddhiQuery.getQueryGroups().forEach(queryGroup -> {
            queryGroup.getSiddhiQueries().forEach(query -> {
                siddhiAppHolders.add(new SiddhiAppHolder(distributedSiddhiQuery.getAppName(),
                        queryGroup.getGroupName(), query.getAppName(), query.getApp(), null));
            });
        });
        return siddhiAppHolders;
    }

    @Override
    public boolean unDeploy(String siddhiAppName) {
        boolean unDeployed = false;
        Map<String, List<SiddhiAppHolder>> siddhiAppHoldersMap = ServiceDataHolder
                .getResourcePool().getSiddhiAppHoldersMap();
        Map<String, List<SiddhiAppHolder>> waitingAppList = ServiceDataHolder
                .getResourcePool().getAppsWaitingForDeploy();

        if (siddhiAppHoldersMap.containsKey(siddhiAppName) || waitingAppList.containsKey(siddhiAppName)) {
            // remove from the deployed apps
            rollback(siddhiAppHoldersMap.get(siddhiAppName));
            siddhiAppHoldersMap.remove(siddhiAppName);

            // remove from the waiting list
            rollback(siddhiAppHoldersMap.get(siddhiAppName));
            waitingAppList.remove(siddhiAppName);
            unDeployed = true;

            LOG.info("Siddhi app " + siddhiAppName + "un-deployed successfully");
        } else {
            LOG.warn("Siddhi app " + siddhiAppName + " is not deployed. Therefore, cannot un-deploy the Siddhi App.");
        }
        ServiceDataHolder.getResourcePool().persist();
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
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        if (deploymentConfig != null && resourcePool != null) {
            if (resourcePool.getResourceNodeMap().size() >= deploymentConfig.getMinResourceCount()) {
                if (resourceIterator == null) {
                    resourceIterator = resourcePool.getResourceNodeMap().values().iterator();
                }
                if (resourceIterator.hasNext()) {
                    return (ResourceNode) resourceIterator.next();
                } else {
                    resourceIterator = resourcePool.getResourceNodeMap().values().iterator();
                    if (resourceIterator.hasNext()) {
                        return (ResourceNode) resourceIterator.next();
                    }
                }
            }
        }
        return null;
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
                    LOG.info(String.format("Siddhi app %s of %s successfully deployed in %s.",
                            partialAppHolder.getAppName(), partialAppHolder.getParentAppName(), deployedNode));
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
                LOG.info("Siddhi app " + parentSiddhiAppName + " successfully deployed.");
            } else {
                LOG.warn(String.format("Still insufficient resources to deploy %s. Hence, rolling back the " +
                        "deployment and waiting for additional resources.", parentSiddhiAppName));
                rollback(currentDeployedPartialApps);
            }
        }
        ServiceDataHolder.getResourcePool().persist();
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
                    LOG.info(String.format("Siddhi app %s of %s successfully deployed in %s.",
                            affectedPartialApp.getAppName(), affectedPartialApp.getParentAppName(), deployedNode));
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
        ServiceDataHolder.getResourcePool().persist();
    }

    public void reDeployAppsInResourceNode(ResourceNode resourceNode) {
        List<SiddhiAppHolder> deployedAppHolders = getNodeAppMapping().get(resourceNode);
        if (resourceNode != null && deployedAppHolders != null) {

            deployedAppHolders.forEach(appHolder -> {
                String appName = SiddhiAppDeployer.deploy(resourceNode, new SiddhiQuery(appHolder.getAppName(),
                        appHolder.getSiddhiApp()));
                if (appName == null || appName.isEmpty()) {
                    LOG.warn(String.format("Couldn't re-deploy partial Siddhi app %s of %s in %s. Therefore, " +
                                    "assuming the %s has left the resource pool.", appHolder.getAppName(),
                            appHolder.getParentAppName(), resourceNode, resourceNode));
                    ServiceDataHolder.getResourcePool().removeResourceNode(resourceNode.getId());
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Partial Siddhi app %s of %s successfully re-deployed in %s.",
                                appName, appHolder.getParentAppName(), resourceNode));
                    }
                }
            });
        }
        ServiceDataHolder.getResourcePool().persist();
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
                if (appHolder.getDeployedNode() != null) {
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
        return siddhiAppHoldersMap.values().stream().flatMap(List::stream).filter(siddhiAppHolder
                -> siddhiAppHolder.getDeployedNode() != null)
                .collect(Collectors.groupingBy(SiddhiAppHolder::getDeployedNode));
    }
}
