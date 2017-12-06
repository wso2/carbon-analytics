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

import java.util.List;
import java.util.Map;

/**
 * This bean class for dashboard configuration.
 */

@Configuration(namespace = "wso2.status.dashboard", description = "SP Status Dashboard Configuration Parameters")
public class StatusDashboardConfiguration {
    @Element(description = "Admin Username across cluster", required = true)
    private String adminUsername;

    @Element(description = "Admin password across cluster")
    private String adminPassword;

    @Element(description = "polling interval to get real-time statistics of worker in seconds")
    private Integer pollingInterval;

    @Element(description = "Metrics Datasource")
    private String metricsDatasourceName;

    @Element(description = "Dashboard Datasource")
    private String dashboardDatasourceName;

    @Element(description = "Database query map")
    private Map<String, Map<String, String>> queries;

    @Element(description = "Database query map")
    private Map<String, String> typeMapping;

    @Element(description = "Map of sysAdminRoles list")
    private List<String> sysAdminRoles;

    @Element(description = "Map of developerRoles list")
    private List<String> developerRoles;

    public StatusDashboardConfiguration() {

    }

    public List<String> getSysAdminRoles() {
        return sysAdminRoles;
    }

    public List<String> getDeveloperRoles() {
        return developerRoles;
    }

    public Integer getPollingInterval() {
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

    public void setPollingInterval(Integer pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public void setMetricsDatasourceName(String metricsDatasourceName) {
        this.metricsDatasourceName = metricsDatasourceName;
    }

    public void setDashboardDatasourceName(String dashboardDatasourceName) {
        this.dashboardDatasourceName = dashboardDatasourceName;
    }

    public void setSysAdminRoles(List<String> sysAdminRoles) {
        this.sysAdminRoles = sysAdminRoles;
    }

    public void setDeveloperRoles(List<String> developerRoles) {
        this.developerRoles = developerRoles;
    }

}
