/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.sp.jobmanager.core.internal;

import com.zaxxer.hikari.HikariDataSource;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.sp.jobmanager.core.dbhandler.ManagerDeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.internal.roles.provider.RolesProvider;

public class ManagerDataHolder {
    private static ManagerDataHolder instance = new ManagerDataHolder();
    private ConfigProvider configProvider;
    private HikariDataSource dashboardManagerDataSource;
    private PermissionProvider permissionProvider;
    private ManagerDeploymentConfig managerDeploymentConfig = new ManagerDeploymentConfig();
    private RolesProvider rolesProvider;

    private ManagerDataHolder() {

    }

    /**
     * Provide instance of ManagerDataholder class.
     *
     * @return Instance of ManagerDataHolder
     */
    public static ManagerDataHolder getInstance() {
        return instance;
    }

    /**
     * Return Config provider
     *
     * @return Instance of the config reader
     */

    public ConfigProvider getConfigProvider() {
        return this.configProvider;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public HikariDataSource getDashboardManagerDataSource() {
        return dashboardManagerDataSource;
    }

    public void setDashboardManagerDataSource(HikariDataSource dashboardManagerDataSource) {
        this.dashboardManagerDataSource = dashboardManagerDataSource;
    }

    public PermissionProvider getPermissionProvider() {
        return permissionProvider;
    }

    public void setPermissionProvider(PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    public ManagerDeploymentConfig getManagerDeploymentConfig() {
        return managerDeploymentConfig;
    }

    public void setManagerDeploymentConfig(
            ManagerDeploymentConfig managerDeploymentConfig) {
        this.managerDeploymentConfig = managerDeploymentConfig;
    }

    public RolesProvider getRolesProvider() {
        return rolesProvider;
    }

    public void setRolesProvider(RolesProvider rolesProvider) {
        this.rolesProvider = rolesProvider;
    }
}
