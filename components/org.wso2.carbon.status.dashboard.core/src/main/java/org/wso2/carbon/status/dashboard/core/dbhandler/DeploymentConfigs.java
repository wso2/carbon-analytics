package org.wso2.carbon.status.dashboard.core.dbhandler;
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

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.status.dashboard.core.bean.WorkerAccessCredentials;
import org.wso2.carbon.status.dashboard.core.bean.WorkerConnectionConfigurations;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration bean class for status dashboard manager query configurations.
 */
@Configuration(namespace = "wso2.status.dashboard", description = "WSO2 Status Dashboard Manager Query Provider")
public class DeploymentConfigs {
    @Element(description = "Admin Username across cluster")
    private WorkerAccessCredentials workerAccessCredentials = new WorkerAccessCredentials();

    @Element(description = "Feign client configurations.")
    private WorkerConnectionConfigurations workerConnectionConfigurations = new WorkerConnectionConfigurations();

    @Element(description = "polling interval to get real-time statistics of worker in seconds")
    private Integer pollingInterval;

    @Element(description = "Metrics Datasource")
    private String metricsDatasourceName;

    @Element(description = "Dashboard Datasource")
    private String dashboardDatasourceName;

    @Element(description = "Database query map")
    private ArrayList<Queries> queries;

    @Element(description = "Map of sysAdminRoles list")
    private List<String> sysAdminRoles;

    @Element(description = "Map of developerRoles list")
    private List<String> developerRoles;

    @Element(description = "Map of viewerRoles list")
    private List<String> viewerRoles;

    public DeploymentConfigs() {
    }

    public ArrayList<Queries> getQueries() {
        return queries;
    }

    public void setQueries(ArrayList<Queries> queries) {
        this.queries = queries;
    }

    public void setWorkerAccessCredentials(WorkerAccessCredentials workerAccessCredentials) {
        this.workerAccessCredentials = workerAccessCredentials;
    }

    public Integer getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(Integer pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public String getMetricsDatasourceName() {
        return metricsDatasourceName;
    }

    public void setMetricsDatasourceName(String metricsDatasourceName) {
        this.metricsDatasourceName = metricsDatasourceName;
    }

    public String getDashboardDatasourceName() {
        return dashboardDatasourceName;
    }

    public void setDashboardDatasourceName(String dashboardDatasourceName) {
        this.dashboardDatasourceName = dashboardDatasourceName;
    }

    public List<String> getSysAdminRoles() {
        return sysAdminRoles;
    }

    public void setSysAdminRoles(List<String> sysAdminRoles) {
        this.sysAdminRoles = sysAdminRoles;
    }

    public List<String> getDeveloperRoles() {
        return developerRoles;
    }

    public void setDeveloperRoles(List<String> developerRoles) {
        this.developerRoles = developerRoles;
    }

    public List<String> getViewerRoles() {
        return viewerRoles;
    }

    public void setViewerRoles(List<String> viewerRoles) {
        this.viewerRoles = viewerRoles;
    }

    public WorkerConnectionConfigurations getWorkerConnectionConfigurations() {
        return workerConnectionConfigurations;
    }

    public void setWorkerConnectionConfigurations(int connectionTimeOut, int readTimeOut) {
        this.workerConnectionConfigurations = new WorkerConnectionConfigurations(connectionTimeOut, readTimeOut);
    }

    public String getUsername() {
        return workerAccessCredentials.getUsername();
    }

    public String getPassword() {
        return workerAccessCredentials.getPassword();
    }

    public void setUsername(String username) {
        workerAccessCredentials.setUsername(username);
    }

    public void setPassword(String password) {
        workerAccessCredentials.setPassword(password);
    }

}
