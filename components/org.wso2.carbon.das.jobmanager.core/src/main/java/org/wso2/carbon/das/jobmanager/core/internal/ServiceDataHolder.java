package org.wso2.carbon.das.jobmanager.core.internal;

import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.das.jobmanager.core.beans.ClusterConfig;
import org.wso2.carbon.das.jobmanager.core.util.DeploymentMode;
import org.wso2.carbon.das.jobmanager.core.util.RuntimeMode;
import org.wso2.carbon.datasource.core.api.DataSourceService;

public class ServiceDataHolder {
    private static ConfigProvider configProvider;
    private static DataSourceService dataSourceService;
    private static ClusterCoordinator clusterCoordinator;
    private static DeploymentMode deploymentMode;
    private static RuntimeMode runtimeMode;
    private static ClusterConfig clusterConfig;
    private static NodeDetail leaderNodeDetail;
    private static boolean isLeader;

    public static ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public static void setConfigProvider(ConfigProvider configProvider) {
        ServiceDataHolder.configProvider = configProvider;
    }

    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public static void setDataSourceService(DataSourceService dataSourceService) {
        ServiceDataHolder.dataSourceService = dataSourceService;
    }

    public static ClusterCoordinator getClusterCoordinator() {
        return clusterCoordinator;
    }

    public static void setClusterCoordinator(ClusterCoordinator clusterCoordinator) {
        ServiceDataHolder.clusterCoordinator = clusterCoordinator;
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

    public static RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    public static void setRuntimeMode(RuntimeMode runtimeMode) {
        ServiceDataHolder.runtimeMode = runtimeMode;
    }

    public static boolean isLeader() {
        return isLeader;
    }

    public static void setIsLeader(boolean isLeader) {
        ServiceDataHolder.isLeader = isLeader;
    }

    public static NodeDetail getLeaderNodeDetail() {
        return leaderNodeDetail;
    }

    public static void setLeaderNodeDetail(NodeDetail leaderNodeDetail) {
        ServiceDataHolder.leaderNodeDetail = leaderNodeDetail;
    }
}
