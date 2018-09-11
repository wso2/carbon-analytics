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
 * Bean class that holds the summary details of siddhi applications
 */
package org.wso2.carbon.status.dashboard.core.bean;

public class SiddhiAppSummaryInfo {
    private String appName;
    private String status;
    private String lastUpdate;
    private boolean isStatEnabled;
    private String deployedNodeType;
    private String deployedNodeHost;
    private String deployedNodePort;
    private String deploymentMode;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeployedNodeType() {
        return deployedNodeType;
    }

    public void setDeployedNodeType(String deployedNodeType) {
        this.deployedNodeType = deployedNodeType;
    }

    public String getDeployedNodeHost() {
        return deployedNodeHost;
    }

    public void setDeployedNodeHost(String deployedNodeHost) {
        this.deployedNodeHost = deployedNodeHost;
    }

    public String getDeployedNodePort() {
        return deployedNodePort;
    }

    public void setDeployedNodePort(String deployedNodePort) {
        this.deployedNodePort = deployedNodePort;
    }

    public String getDeploymentMode() {
        return deploymentMode;
    }

    public void setDeploymentMode(String deploymentMode) {
        this.deploymentMode = deploymentMode;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isStatEnabled() {
        return isStatEnabled;
    }

    public void setStatEnabled(boolean statEnabled) {
        isStatEnabled = statEnabled;
    }
}
