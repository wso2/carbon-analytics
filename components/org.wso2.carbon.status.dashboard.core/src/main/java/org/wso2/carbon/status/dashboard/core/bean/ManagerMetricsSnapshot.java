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
package org.wso2.carbon.status.dashboard.core.bean;

import org.wso2.carbon.status.dashboard.core.model.DistributedServerDetails;

/**
 * Bean class for keeping the snapshot of manager status .
 */

public class ManagerMetricsSnapshot {
    private DistributedServerDetails serverDetails = new DistributedServerDetails();
    private ManagerClusterInfo clusterInfo = new ManagerClusterInfo();
    private Long timeStamp;

    public ManagerMetricsSnapshot(DistributedServerDetails workerMetrics, Long timeStamp) {
        this.serverDetails = workerMetrics;
        this.timeStamp = timeStamp;
    }

    public void updateRunningStatus(String status) {
        serverDetails.setRunningStatus(status);

    }

    public DistributedServerDetails getServerDetails() {
        return serverDetails;
    }

    public ManagerClusterInfo getClusterInfo(){return clusterInfo;}

    public void setServerDetails(DistributedServerDetails serverDetails) {
        this.serverDetails = serverDetails;
    }

    public void setClusterInfo(ManagerClusterInfo clusterInfo){this.clusterInfo=clusterInfo;}

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
