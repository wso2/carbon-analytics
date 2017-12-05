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
package org.wso2.carbon.status.dashboard.core.internal;

import com.zaxxer.hikari.HikariDataSource;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.status.dashboard.core.internal.roles.provider.RolesProvider;

/**
 * This is data holder for config provider implementations.
 */
public class DashboardDataHolder {
    private static DashboardDataHolder instance = new DashboardDataHolder();
    private ConfigProvider configProvider;
    private static HikariDataSource metricsDataSource;
    private static HikariDataSource dashboardDataSource;
    private static String metricsDataSourceName;
    private static String dashboardDataSourceName;
    private static RolesProvider rolesProvider;
    private PermissionProvider permissionProvider;

    private DashboardDataHolder() {
    }

    /**
     * Provide instance of DashboardDataHolder class.
     *
     * @return Instance of DashboardDataHolder
     */
    public static DashboardDataHolder getInstance() {
        return instance;
    }

    /**
     * Returns config provider.
     *
     * @return Instance of config provider
     */
    public ConfigProvider getConfigProvider() {
        return this.configProvider;
    }

    /**
     * Sets instance of config provider.
     *
     * @param configProvider Instance of servicers provider
     */
    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }


    public HikariDataSource getMetricsDataSource() {
        return metricsDataSource;
    }

    public void setMetricsDataSource(HikariDataSource metricsDataSource) {
        DashboardDataHolder.metricsDataSource = metricsDataSource;
    }

    public HikariDataSource getDashboardDataSource() {
        return dashboardDataSource;
    }

    public void setDashboardDataSource(HikariDataSource dashboardDataSource) {
        DashboardDataHolder.dashboardDataSource = dashboardDataSource;
    }

    public static String getMetricsDataSourceName() {
        return metricsDataSourceName;
    }

    public static void setMetricsDataSourceName(String metricsDataSourceName) {
        DashboardDataHolder.metricsDataSourceName = metricsDataSourceName;
    }

    public static String getDashboardDataSourceName() {
        return dashboardDataSourceName;
    }

    public static void setDashboardDataSourceName(String dashboardDataSourceName) {
        DashboardDataHolder.dashboardDataSourceName = dashboardDataSourceName;
    }

    public static RolesProvider getRolesProvider() {
        return rolesProvider;
    }

    public static void setRolesProvider(RolesProvider rolesProvider) {
        DashboardDataHolder.rolesProvider = rolesProvider;
    }

    public PermissionProvider getPermissionProvider() {
        return permissionProvider;
    }

    public void setPermissionProvider(PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
    }
}
