/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.jobmanager.core.dbhandler;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.sp.jobmanager.core.bean.ManagerAccessCredentials;
import org.wso2.carbon.sp.jobmanager.core.bean.ManagerConnectionConfigurations;

import java.util.ArrayList;
import java.util.List;

/**
 * Configurations details specified in the deployment.yaml.
 */
@Configuration(namespace = "wso2.sp.status", description = "WSO2 status dashboard manager query "
        + "provider")
public class ManagerDeploymentConfig {
    @Element(description = "Admin Username across cluster")
    private ManagerAccessCredentials managerAccessCredentials = new ManagerAccessCredentials();

    @Element(description = "Feign client configurations.")
    private ManagerConnectionConfigurations managerConnectionConfigurations = new ManagerConnectionConfigurations();

    @Element(description = "polling interval to get real-time statistics of worker in seconds")
    private Integer pollingInterval;

    @Element(description = "Dasboard ManagerDataSource.")
    private String dashboardManagerDatasourceName;

    @Element(description = "Database query map")
    private ArrayList<Queries> queries;

    @Element(description = "Map of sysAdminRoles list")
    private List<String> sysAdminRoles;

    @Element(description = "Map of developerRoles list")
    private List<String> developerRoles;

    @Element(description = "Map of viewerRoles list")
    private List<String> viewerRoles;

    public ManagerDeploymentConfig() {
    }

    public ArrayList<Queries> getQueries() {
        return queries;
    }

    public void setQueries(ArrayList<Queries> queries) {
        this.queries = queries;
    }

    public String getDashboardManagerDatasourceName() {
        return dashboardManagerDatasourceName;
    }

    public void setDashboardManagerDatasourceName(String dashboardManagerDatasourceName) {
        this.dashboardManagerDatasourceName = dashboardManagerDatasourceName;
    }

    public ManagerAccessCredentials getManagerAccessCredentials() {
        return managerAccessCredentials;
    }

    public void setManagerAccessCredentials(
            ManagerAccessCredentials managerAccessCredentials) {
        this.managerAccessCredentials = managerAccessCredentials;
    }

    public ManagerConnectionConfigurations getManagerConnectionConfigurations() {
        return managerConnectionConfigurations;
    }

    public void setManagerConnectionConfigurations(
            int connectionTimeOut, int readTimeOut) {
        this.managerConnectionConfigurations = new ManagerConnectionConfigurations(connectionTimeOut, readTimeOut);
    }

    public Integer getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(Integer pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public String getUsername() {
        return managerAccessCredentials.getUsername();
    }

    public void setUsername(String username) {
        managerAccessCredentials.setUsername(username);
    }

    public String getPassword() {
        return managerAccessCredentials.getPassword();
    }

    public void setPassword(String password) {
        managerAccessCredentials.setPassword(password);
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
}
