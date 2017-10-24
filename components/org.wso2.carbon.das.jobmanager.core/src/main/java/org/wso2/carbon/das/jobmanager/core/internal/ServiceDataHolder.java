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

package org.wso2.carbon.das.jobmanager.core.internal;

import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.das.jobmanager.core.bean.ClusterConfig;
import org.wso2.carbon.das.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.das.jobmanager.core.impl.RDBMSServiceImpl;
import org.wso2.carbon.das.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.das.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.das.jobmanager.core.util.DeploymentMode;
import org.wso2.carbon.datasource.core.api.DataSourceService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServiceDataHolder {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(2);
    private static RDBMSServiceImpl rdbmsService;
    private static DataSourceService dataSourceService;
    private static DeploymentMode deploymentMode;
    private static ClusterConfig clusterConfig;
    private static DeploymentConfig deploymentConfig;
    private static ManagerNode currentNode;
    private static ManagerNode leaderNode;
    private static ClusterCoordinator coordinator;
    private static ResourcePool resourcePool;
    private static boolean isLeader;

    public static ScheduledExecutorService getExecutorService() {
        return EXECUTOR_SERVICE;
    }

    public static boolean isIsLeader() {
        return isLeader;
    }

    public static void setIsLeader(boolean isLeader) {
        ServiceDataHolder.isLeader = isLeader;
    }

    public static RDBMSServiceImpl getRdbmsService() {
        return rdbmsService;
    }

    public static void setRdbmsService(RDBMSServiceImpl rdbmsService) {
        ServiceDataHolder.rdbmsService = rdbmsService;
    }

    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public static void setDataSourceService(DataSourceService dataSourceService) {
        ServiceDataHolder.dataSourceService = dataSourceService;
    }

    public static DeploymentMode getDeploymentMode() {
        return deploymentMode;
    }

    public static void setDeploymentMode(DeploymentMode deploymentMode) {
        ServiceDataHolder.deploymentMode = deploymentMode;
    }

    public static ClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    public static void setClusterConfig(ClusterConfig clusterConfig) {
        ServiceDataHolder.clusterConfig = clusterConfig;
    }

    public static DeploymentConfig getDeploymentConfig() {
        return deploymentConfig;
    }

    public static void setDeploymentConfig(DeploymentConfig deploymentConfig) {
        ServiceDataHolder.deploymentConfig = deploymentConfig;
    }

    public static ManagerNode getCurrentNode() {
        return currentNode;
    }

    public static void setCurrentNode(ManagerNode currentNode) {
        ServiceDataHolder.currentNode = currentNode;
    }

    public static ManagerNode getLeaderNode() {
        return leaderNode;
    }

    public static void setLeaderNode(ManagerNode leaderNode) {
        ServiceDataHolder.leaderNode = leaderNode;
    }

    public static boolean isLeader() {
        return isLeader;
    }

    public static ClusterCoordinator getCoordinator() {
        return coordinator;
    }

    public static void setCoordinator(ClusterCoordinator coordinator) {
        ServiceDataHolder.coordinator = coordinator;
    }

    public static ResourcePool getResourcePool() {
        return resourcePool;
    }

    public static void setResourcePool(ResourcePool resourcePool) {
        ServiceDataHolder.resourcePool = resourcePool;
    }
}
