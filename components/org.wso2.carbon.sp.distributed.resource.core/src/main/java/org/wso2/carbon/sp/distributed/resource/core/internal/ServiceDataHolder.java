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

package org.wso2.carbon.sp.distributed.resource.core.internal;

import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.sp.distributed.resource.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.distributed.resource.core.bean.HTTPInterfaceConfig;
import org.wso2.carbon.sp.distributed.resource.core.bean.ManagerNodeConfig;
import org.wso2.carbon.sp.distributed.resource.core.bean.NodeConfig;
import org.wso2.carbon.stream.processor.core.util.DeploymentMode;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ServiceDataHolder {
    private static ConfigProvider configProvider;
    private static DeploymentConfig deploymentConfig;
    private static ManagerNodeConfig leaderNodeConfig;
    private static NodeConfig currentNodeConfig;
    private static DeploymentMode deploymentMode;
    private static Set<HTTPInterfaceConfig> resourceManagers = Collections.synchronizedSet(new LinkedHashSet<>());

    public static ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public static void setConfigProvider(ConfigProvider configProvider) {
        ServiceDataHolder.configProvider = configProvider;
    }

    public static DeploymentConfig getDeploymentConfig() {
        return deploymentConfig;
    }

    public static void setDeploymentConfig(DeploymentConfig deploymentConfig) {
        ServiceDataHolder.deploymentConfig = deploymentConfig;
    }

    public static ManagerNodeConfig getLeaderNodeConfig() {
        return leaderNodeConfig;
    }

    public static void setLeaderNodeConfig(ManagerNodeConfig leaderNodeConfig) {
        ServiceDataHolder.leaderNodeConfig = leaderNodeConfig;
    }

    public static NodeConfig getCurrentNodeConfig() {
        return currentNodeConfig;
    }

    public static void setCurrentNodeConfig(NodeConfig currentNodeConfig) {
        ServiceDataHolder.currentNodeConfig = currentNodeConfig;
    }

    public static DeploymentMode getDeploymentMode() {
        return deploymentMode;
    }

    public static void setDeploymentMode(DeploymentMode deploymentMode) {
        ServiceDataHolder.deploymentMode = deploymentMode;
    }

    public static Set<HTTPInterfaceConfig> getResourceManagers() {
        return resourceManagers;
    }

    public static void setResourceManagers(Set<HTTPInterfaceConfig> resourceManagers) {
        ServiceDataHolder.resourceManagers = resourceManagers;
    }
}
