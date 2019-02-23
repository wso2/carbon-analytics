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

package org.wso2.carbon.sp.jobmanager.core.deployment;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.sp.jobmanager.core.DeploymentManager;
import org.wso2.carbon.sp.jobmanager.core.allocation.Knapsack;
import org.wso2.carbon.sp.jobmanager.core.allocation.MetricsBasedAllocationAlgorithm;
import org.wso2.carbon.sp.jobmanager.core.allocation.ResourceAllocationAlgorithm;
import org.wso2.carbon.sp.jobmanager.core.ResourcePoolChangeListener;
import org.wso2.carbon.sp.jobmanager.core.SiddhiAppDeployer;
import org.wso2.carbon.sp.jobmanager.core.allocation.RoundRobinAllocationAlgorithm;
import org.wso2.carbon.sp.jobmanager.core.appcreator.DistributedSiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.appcreator.SiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.sp.jobmanager.core.model.SiddhiAppHolder;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation regarding deploying siddhi applications in the resource cluster
 */
public class DeploymentManagerImpl extends  Thread implements DeploymentManager, ResourcePoolChangeListener  {
    private static final Logger log = Logger.getLogger(DeploymentManagerImpl.class);
    private final Lock lock = new ReentrantLock();
    private MetricsBasedAllocationAlgorithm metricsBasedAllocationAlgorithm = new MetricsBasedAllocationAlgorithm();
    private ResourceAllocationAlgorithm resourceAllocationAlgorithm = ServiceDataHolder.getAllocationAlgorithm();
    private ResourceAllocationAlgorithm receiverAllocationAlgorithm = new RoundRobinAllocationAlgorithm();
    public static Long  starttime = 0L;
    boolean metricScheduling = false;
    Connection conn = null;
    Statement statement;

    @Override
    public DeploymentStatus deploy(DistributedSiddhiQuery distributedSiddhiQuery , boolean metricScheduling) {
        Map<String, List<SiddhiAppHolder>> deployedSiddhiAppHoldersMap = ServiceDataHolder
                .getResourcePool().getSiddhiAppHoldersMap();
        List<SiddhiAppHolder> appsToDeploy = getSiddhiAppHolders(distributedSiddhiQuery);
        List<SiddhiAppHolder> deployedApps = new ArrayList<>();
        boolean shouldDeploy = true;
        starttime = System.currentTimeMillis();
        lock.lock();
        try {
            if (deployedSiddhiAppHoldersMap.containsKey(distributedSiddhiQuery.getAppName())) {
                List<SiddhiAppHolder> existingApps = deployedSiddhiAppHoldersMap
                        .get(distributedSiddhiQuery.getAppName());
                if (CollectionUtils.isEqualCollection(existingApps, appsToDeploy)) {
                    boolean waitingToDeploy = false;
                    for (SiddhiAppHolder app : existingApps) {
                        if (app.getDeployedNode() == null) {
                            waitingToDeploy = true;
                            break;
                        }
                    }
                    if (waitingToDeploy) {
                        log.info(String.format("Exact Siddhi app with name: %s is already exists in waiting mode. " +
                                "Hence, trying to re-deploy.", distributedSiddhiQuery
                                .getAppName()));
                        rollback(existingApps);
                    } else {
                        log.info(String.format("Exact Siddhi app with name: %s is already deployed.",
                                distributedSiddhiQuery.getAppName()));
                        shouldDeploy = false;
                    }
                } else {
                    log.info("Different Siddhi app with name:" + distributedSiddhiQuery.getAppName() + " is already " +
                            "deployed. Hence, un-deploying existing Siddhi app.");
                    rollback(deployedSiddhiAppHoldersMap.get(distributedSiddhiQuery.getAppName()));
                }
            }
            boolean isDeployed = true;
            if (shouldDeploy) {

                if (metricScheduling) {
                    processAlgorithm(distributedSiddhiQuery);
                    log.info("metricsAllocationAlgorithm formulated");

                }
                for (SiddhiAppHolder appHolder : appsToDeploy) {
                    log.info(appHolder.getAppName() + " is starting to deploy");
                    ResourceNode deployedNode;
                    deployedNode = deploy(new SiddhiQuery(appHolder.getAppName(), appHolder.getSiddhiApp(),
                            appHolder.isReceiverQueryGroup()), 0, appHolder.getParallelism() , metricScheduling);
                    if (deployedNode != null) {
                        appHolder.setDeployedNode(deployedNode);
                        deployedApps.add(appHolder);
                        log.info(String.format("Siddhi app %s of %s successfully deployed in %s.",
                                appHolder.getAppName(), appHolder.getParentAppName(), deployedNode));
                    } else {
                        log.warn(String.format("Insufficient resources to deploy Siddhi app %s of %s. Hence, rolling " +
                                "back.", appHolder.getAppName(), appHolder.getParentAppName()));
                        isDeployed = false;
                        break;
                    }
                }
                if (isDeployed) {
                    deployedSiddhiAppHoldersMap.put(distributedSiddhiQuery.getAppName(), deployedApps);
                    log.info("Siddhi app " + distributedSiddhiQuery.getAppName() + " successfully deployed.");
                    boolean isMetricScheduling = ServiceDataHolder.getDeploymentConfig().getMetricScheduling();
                    log.info("metricschduling  "+ isMetricScheduling);
                    int waitingTime = ServiceDataHolder.getDeploymentConfig().getWaitingTime();
                    log.info("waitingtime " + waitingTime);
                    log.info("isMetricscheduling " + isMetricScheduling);
                    if (isMetricScheduling){
                        while (true) {
                            if ((DeploymentManagerImpl.starttime + (waitingTime *60*1000) < System.currentTimeMillis())) {
                                metricScheduling = true;
                                rollback(deployedApps);
                                deployedApps = Collections.emptyList();
                                deployedSiddhiAppHoldersMap.remove(distributedSiddhiQuery.getAppName());
                                ServiceDataHolder.getResourcePool().getAppsWaitingForDeploy()
                                        .put(distributedSiddhiQuery.getAppName(), appsToDeploy);
                                log.info("Starting MetricBasedResourceAllocation...");
                                deploy(distributedSiddhiQuery , metricScheduling);
                                break;
                            }
                        }
                    }

                } else {
                    rollback(deployedApps);
                    deployedApps = Collections.emptyList();
                    deployedSiddhiAppHoldersMap.remove(distributedSiddhiQuery.getAppName());
                    ServiceDataHolder.getResourcePool().getAppsWaitingForDeploy()
                            .put(distributedSiddhiQuery.getAppName(), appsToDeploy);
                    log.info("Siddhi app " + distributedSiddhiQuery.getAppName() + " held back in waiting mode.");
                }
            } else {
                deployedApps = deployedSiddhiAppHoldersMap.get(distributedSiddhiQuery.getAppName());
            }
            ServiceDataHolder.getResourcePool().persist();
        } finally {
            lock.unlock();
        }
        // Returning true as the deployment state, since we might put some apps on wait.
        return getDeploymentStatus(true, deployedApps);
    }

    public Statement dbConnector(){
        try {
            String datasourceName = ServiceDataHolder.getDeploymentConfig().getDatasource();
            DataSourceService dataSourceService = ServiceDataHolder.getDataSourceService();
            DataSource datasource = (HikariDataSource) dataSourceService.getDataSource(datasourceName);conn = datasource.getConnection();
            conn = datasource.getConnection();

            Statement st = conn.createStatement();
            return st;
        } catch (SQLException e) {
           log.error("SQL error" + e.getMessage());
        } catch (DataSourceException e) {
            log.error("Datasource error" + e.getMessage()); }
        return null;
    }

    private DeploymentStatus getDeploymentStatus(boolean isDeployed, List<SiddhiAppHolder> siddhiAppHolders) {
        Map<String, List<String>> deploymentDataMap = new HashMap<>();
        for (SiddhiAppHolder appHolder : siddhiAppHolders) {
            if (appHolder.getDeployedNode() != null && appHolder.getDeployedNode().getHttpsInterface() != null) {
                if (deploymentDataMap.containsKey(appHolder.getGroupName())) {
                    deploymentDataMap.get(appHolder.getGroupName())
                            .add(appHolder.getDeployedNode().getHttpsInterface().getHost());
                } else {
                    List<String> hosts = new ArrayList<>();
                    hosts.add(appHolder.getDeployedNode().getHttpsInterface().getHost());
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
                        queryGroup.getGroupName(), query.getAppName(), query.getApp(),
                        null, queryGroup.isReceiverQueryGroup(), queryGroup.getParallelism()));
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

        lock.lock();
        try {
            if (siddhiAppHoldersMap.containsKey(siddhiAppName) || waitingAppList.containsKey(siddhiAppName)) {
                // remove from the deployed apps
                rollback(siddhiAppHoldersMap.get(siddhiAppName));
                siddhiAppHoldersMap.remove(siddhiAppName);

                // remove from the waiting list
                rollback(siddhiAppHoldersMap.get(siddhiAppName));
                waitingAppList.remove(siddhiAppName);
                unDeployed = true;

                log.info("Siddhi app " + siddhiAppName + "un-deployed successfully");
            } else {
                log.warn("Siddhi app " + siddhiAppName + " is not deployed. Therefore, cannot un-deploy.");
            }
            ServiceDataHolder.getResourcePool().persist();
        } finally {
            lock.unlock();
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


    @Override
    public void resourceAdded(ResourceNode resourceNode) {
        Map<String, List<SiddhiAppHolder>> waitingList = ServiceDataHolder.getResourcePool().getAppsWaitingForDeploy();
        Set<String> waitingParentAppNames = new HashSet<>(waitingList.keySet());
        List<SiddhiAppHolder> partialAppHoldersOfSiddhiApp;
        List<SiddhiAppHolder> currentDeployedPartialApps;
        boolean deployedCompletely;

        lock.lock();
        try {
            for (String parentSiddhiAppName : waitingParentAppNames) {
                partialAppHoldersOfSiddhiApp = waitingList.getOrDefault(parentSiddhiAppName, Collections.emptyList());
                deployedCompletely = true;
                currentDeployedPartialApps = new ArrayList<>();

                for (SiddhiAppHolder partialAppHolder : partialAppHoldersOfSiddhiApp) {
                    ResourceNode deployedNode = deploy(new SiddhiQuery(partialAppHolder.getAppName(),
                                    partialAppHolder.getSiddhiApp(), partialAppHolder.isReceiverQueryGroup()), 0,
                            partialAppHolder.getParallelism() , metricScheduling);

                    if (deployedNode != null) {
                        partialAppHolder.setDeployedNode(deployedNode);
                        currentDeployedPartialApps.add(partialAppHolder);
                        log.info(String.format("Siddhi app %s of %s successfully deployed in %s.",
                                partialAppHolder.getAppName(), partialAppHolder.getParentAppName(),
                                deployedNode));
                    } else {
                        deployedCompletely = false;
                        break;
                    }
                }
                if (deployedCompletely) {
                    ServiceDataHolder.getResourcePool().getSiddhiAppHoldersMap()
                            .put(parentSiddhiAppName, partialAppHoldersOfSiddhiApp);
                    waitingList.remove(parentSiddhiAppName);
                    log.info("Siddhi app " + parentSiddhiAppName + " successfully deployed.");
                } else {
                    log.warn(String.format("Still insufficient resources to deploy %s. Hence, rolling back the " +
                                    "deployment and waiting for additional resources.",
                            parentSiddhiAppName));
                    rollback(currentDeployedPartialApps);
                }
            }
            ServiceDataHolder.getResourcePool().persist();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void resourceRemoved(ResourceNode resourceNode) {
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        List<SiddhiAppHolder> affectedPartialApps = resourcePool.getNodeAppMapping().get(resourceNode);

        lock.lock();
        try {
            if (affectedPartialApps != null) {
                log.info(String.format("Siddhi apps %s were affected by the removal of node %s. Hence, re-deploying "
                        + "them in other resource nodes.", affectedPartialApps, resourceNode));
                rollback(affectedPartialApps);

                affectedPartialApps.forEach(affectedPartialApp -> {
                    ResourceNode deployedNode = deploy(new SiddhiQuery(affectedPartialApp.getAppName(),
                                    affectedPartialApp.getSiddhiApp(), affectedPartialApp.isReceiverQueryGroup()), 0,
                            affectedPartialApp.getParallelism() , metricScheduling);
                    if (deployedNode != null) {
                        affectedPartialApp.setDeployedNode(deployedNode);
                        log.info(String.format("Siddhi app %s of %s successfully deployed in %s.",
                                affectedPartialApp.getAppName(), affectedPartialApp.getParentAppName(),
                                deployedNode));
                    } else {
                        log.warn(String.format("Insufficient resources to deploy %s. Therefore, cannot re-balance "
                                        + "Siddhi app %s. Hence, rolling back the deployment and waiting"
                                        + " for additional resources.", affectedPartialApp.getAppName(),
                                affectedPartialApp.getParentAppName()));
                        List<SiddhiAppHolder> appHolders = resourcePool.getSiddhiAppHoldersMap()
                                .remove(affectedPartialApp.getParentAppName());

                        if (appHolders != null) {
                            appHolders.forEach(e -> e.setDeployedNode(null));
                            rollback(appHolders);
                            resourcePool.getAppsWaitingForDeploy().put(affectedPartialApp.getParentAppName(),
                                    appHolders);
                        }
                    }
                });
            }
            resourcePool.persist();
        } finally {
            lock.unlock();
        }
    }

    public void reDeployAppsInResourceNode(ResourceNode resourceNode) {
        lock.lock();
        try {
            ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
            List<SiddhiAppHolder> deployedAppHolders = resourcePool.getNodeAppMapping().get(resourceNode);
            if (resourceNode != null && deployedAppHolders != null) {

                deployedAppHolders.forEach(appHolder -> {
                    String appName = SiddhiAppDeployer.deploy(resourceNode, new SiddhiQuery(appHolder.getAppName(),
                            appHolder.getSiddhiApp(), appHolder.isReceiverQueryGroup()));
                    if (appName == null || appName.isEmpty()) {
                        log.warn(String.format("Couldn't re-deploy partial Siddhi app %s of %s in %s. Therefore, " +
                                        "assuming the %s has left the resource pool.", appHolder
                                        .getAppName(),
                                appHolder.getParentAppName(), resourceNode, resourceNode));
                        if (resourceNode.isReceiverNode()) {
                            resourcePool.removeReceiverNode(resourceNode.getId());
                        } else {
                            resourcePool.removeResourceNode(resourceNode.getId());
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Partial Siddhi app %s of %s successfully re-deployed in %s.",
                                    appName, appHolder.getParentAppName(), resourceNode));
                        }
                    }
                });
            }
            resourcePool.persist();
        } finally {
            lock.unlock();
        }
    }

    public void processAlgorithm(DistributedSiddhiQuery distributedSiddhiQuery){
         metricsBasedAllocationAlgorithm.executeKnapsack(ServiceDataHolder.getResourcePool().getResourceNodeMap(),
                ServiceDataHolder.getDeploymentConfig().getMinResourceCount() , distributedSiddhiQuery);
    }

    private ResourceNode deploy(SiddhiQuery siddhiQuery, int retry, int parallelism , boolean metricScheduling) {
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        Map<String, ResourceNode> nodeMap;
        ResourceNode resourceNode;

        if (siddhiQuery.isReceiverQuery()) {
            nodeMap = resourcePool.getReceiverNodeMap();
            resourceNode = receiverAllocationAlgorithm.getNextResourceNode(nodeMap, parallelism , siddhiQuery);
        } else {
            nodeMap = resourcePool.getResourceNodeMap();
            statement = dbConnector();
            if (metricScheduling) {
                log.info("Executing MetricBasedAllocationAlgorithm");
                resourceNode = metricsBasedAllocationAlgorithm.getNextResourceNode(nodeMap,
                        ServiceDataHolder.getDeploymentConfig().getMinResourceCount() , siddhiQuery);

                try {
                    String query = "INSERT into schedulingdetails (timestamp,partialSiddhiApp," +
                            "deployedNode,algorithm)  values (" +
                            System.currentTimeMillis() + "," +
                            "\'"+ siddhiQuery.getAppName() + "\'" +  "," +
                            "\'" + resourceNode.getId() + "\'" + "," +
                            "\'" + "MetricsBasedAllocationAlgorithm" + "\'" + ")";
                    statement.executeUpdate(query);
                    log.info("data pushed to table");

                } catch(SQLException e) {
                    log.error("Error in inserting query . " + e.getMessage());
                }
            } else {
                log.info("Executing Standard ResourceAllocationAlgorithm");
                resourceNode = resourceAllocationAlgorithm.getNextResourceNode(nodeMap,
                        ServiceDataHolder.getDeploymentConfig().getMinResourceCount() , siddhiQuery);
                try {
                    String query = "INSERT into schedulingdetails (timestamp,partialSiddhiApp," +
                            "deployedNode,algorithm)  values (" +
                            System.currentTimeMillis() + "," +
                            "\'"+ siddhiQuery.getAppName() + "\'" +  "," +
                            "\'" + resourceNode.getId() + "\'" + "," +
                            "\'" + "RoundRobinAlgorithm" + "\'" + ")";
                    statement.executeUpdate(query);
                    log.info("data pushed to table");

                } catch(SQLException e) {
                    log.error("Error in inserting query. " + e.getMessage());
                }
            }
        }
        ResourceNode deployedNode = null;

        if (resourceNode != null) {
            String appName = SiddhiAppDeployer.deploy(resourceNode, siddhiQuery);
            if (appName == null || appName.isEmpty()) {
                log.warn(String.format("Couldn't deploy partial Siddhi app %s in %s", siddhiQuery.getAppName(),
                        resourceNode));
                if (retry < nodeMap.size()) {
                    deployedNode = deploy(siddhiQuery, retry + 1, parallelism , metricScheduling);
                } else {
                    if (log.isDebugEnabled()) {
                        log.warn(String.format("Couldn't deploy partial Siddhi app %s even after %s attempts.",
                                siddhiQuery.getAppName(), retry));
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Partial Siddhi app %s successfully deployed in %s.",
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
                        log.warn(String.format("Could not un-deploy Siddhi app %s from %s.",
                                appHolder.getAppName(), appHolder.getDeployedNode()));
                    } else {
                        appHolder.setDeployedNode(null);
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Siddhi app %s un-deployed from %s.",
                                    appHolder.getAppName(), appHolder.getDeployedNode()));
                        }
                    }
                }
            });
        }
    }
}
