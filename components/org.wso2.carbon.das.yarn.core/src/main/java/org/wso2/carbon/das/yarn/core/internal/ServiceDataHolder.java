package org.wso2.carbon.das.yarn.core.internal;

import org.wso2.carbon.das.yarn.core.bean.DeploymentConfig;
import org.wso2.carbon.das.yarn.core.deployment.YarnDeploymentManagerImpl;
import org.wso2.carbon.stream.processor.core.util.DeploymentMode;

public class ServiceDataHolder {
    private static ServiceDataHolder instance = new ServiceDataHolder();
    private static DeploymentMode deploymentMode;
    private static DeploymentConfig yarnDeploymentConfig;
    private static YarnDeploymentManagerImpl deploymentManager;

    public static DeploymentMode getDeploymentMode() {
        return deploymentMode;
    }

    public static void setDeploymentMode(DeploymentMode deploymentMode) {
        ServiceDataHolder.deploymentMode = deploymentMode;
    }
    public static YarnDeploymentManagerImpl getDeploymentManager() {
        return deploymentManager;
    }

    public static void setDeploymentManager(YarnDeploymentManagerImpl deploymentManager) {
        ServiceDataHolder.deploymentManager = deploymentManager;
    }

    public static DeploymentConfig getYarnDeploymentConfig() {
        return yarnDeploymentConfig;
    }

    public static ServiceDataHolder getInstance() {
        return instance;
    }

    public static void setYarnDeploymentConfig(DeploymentConfig yarnDeploymentConfig) {
        ServiceDataHolder.yarnDeploymentConfig = yarnDeploymentConfig;
    }
}
