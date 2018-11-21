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

/**
 * Bean class that holds manager's siddhi applications.
 */
package org.wso2.carbon.status.dashboard.core.bean;

public class ManagerSiddhiApps {
    private String parentAppName;
    private String usedWorkerNodes;
    private String numberOfGroups;
    private String totalWorkerNodes;
    private String numberOfChildApp;
    private String notDeployedChildApps;
    private String managerId;
    private String deployedChildApps;

    public String getParentAppName() {
        return parentAppName;
    }

    public void setParentAppName(String parentAppName) {
        this.parentAppName = parentAppName;
    }

    public String getUsedWorkerNodes() {
        return usedWorkerNodes;
    }

    public void setUsedWorkerNodes(String usedWorkerNodes) {
        this.usedWorkerNodes = usedWorkerNodes;
    }

    public String getNumberOfGroups() {
        return numberOfGroups;
    }

    public void setNumberOfGroups(String numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
    }

    public String getTotalWorkerNodes() {
        return totalWorkerNodes;
    }

    public void setTotalWorkerNodes(String totalWorkerNodes) {
        this.totalWorkerNodes = totalWorkerNodes;
    }

    public String getNumberOfChildApp() {
        return numberOfChildApp;
    }

    public void setNumberOfChildApp(String numberOfChildApp) {
        this.numberOfChildApp = numberOfChildApp;
    }

    public String getNotDeployedChildApps() {
        return notDeployedChildApps;
    }

    public void setNotDeployedChildApps(String notDeployedChildApps) {
        this.notDeployedChildApps = notDeployedChildApps;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getDeployedChildApps() {
        return deployedChildApps;
    }

    public void setDeployedChildApps(String deployedChildApps) {
        this.deployedChildApps = deployedChildApps;
    }
}
