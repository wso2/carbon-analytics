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
package org.wso2.carbon.status.dashboard.core.bean;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.status.dashboard.core.bean.roles.provider.Roles;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This bean class for dashboard configuration.
 */

@Configuration(namespace = "wso2.status.dashboard", description = "SP Status Dashboard Configuration Parameters")
public class StatusDashboardConfiguration {
    private static final String DEFAULT_METRICS_DATASOURCE_NAME = "WSO2_METRICS_DB";
    private static final String DEFAULT_DASHBOARD_DATASOURCE_NAME = "WSO2_STATUS_DASHBOARD_DB";
    private static final int DEFAULT_POLLING_INTERVAL = 5;

    @Element(description = "Admin Username across cluster", required= true)
    private String adminUsername = "admin";

    @Element(description = "Admin password across cluster")
    private String adminPassword = "admin";

    @Element(description = "polling interval to get real-time statistics of worker in seconds")
    private int pollingInterval;

    @Element(description = "Metrics Datasource")
    private String metricsDatasourceName;

    @Element(description = "Dashboard Datasource")
    private String dashboardDatasourceName;

    @Element(description = "Database query map")
    private Map<String, Map<String, String>> queries = new HashMap<>();

    @Element(description = "Database query map")
    private Map<String, String> typeMapping = new HashMap<>();

    @Element(description = "Map of sysAdminRoles list")
    private List<String> sysAdminRoles= Collections.emptyList();;

    @Element(description = "Map of developerRoles list")
    private List<String> developerRoles= Collections.emptyList();;

    public List<String>  getSysAdminRoles() {
        return sysAdminRoles;
    }

    public List<String>  getDeveloperRoles() {
        return developerRoles;
    }

    public StatusDashboardConfiguration() {
        this.pollingInterval = DEFAULT_POLLING_INTERVAL;
        this.metricsDatasourceName = DEFAULT_METRICS_DATASOURCE_NAME;
        this.dashboardDatasourceName = DEFAULT_DASHBOARD_DATASOURCE_NAME;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public Map<String, Map<String, String>> getQueries() {
        return queries;
    }

    public void setQueries(Map<String, Map<String, String>> queries) {
        this.queries = queries;
    }

    public String getMetricsDatasourceName() {
        return metricsDatasourceName;
    }

    public String getDashboardDatasourceName() {
        return dashboardDatasourceName;
    }

    public Map<String, String> getTypeMapping() {
        return typeMapping;
    }

    public void setTypeMapping(Map<String, String> typeMapping) {
        this.typeMapping = typeMapping;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
