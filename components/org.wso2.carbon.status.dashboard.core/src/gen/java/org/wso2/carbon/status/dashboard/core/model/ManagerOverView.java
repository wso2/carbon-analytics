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
package org.wso2.carbon.status.dashboard.core.model;

import org.wso2.carbon.status.dashboard.core.bean.ManagerClusterInfo;

/**
 * Class that gives an overview for manager node
 */
public class ManagerOverView {
    private String workerId;
    private Long lastUpdate;
    private String statusMessage;
    private DistributedServerDetails serverDetails;
    private ManagerClusterInfo clusterInfo;


    public ManagerOverView() {
        serverDetails = new DistributedServerDetails();
        clusterInfo = new ManagerClusterInfo();
    }

    public ManagerClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public void setClusterInfo(ManagerClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public DistributedServerDetails getServerDetails() {
        return serverDetails;
    }

    public void setServerDetails(DistributedServerDetails serverDetails) {
        this.serverDetails = serverDetails;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
