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
package org.wso2.carbon.status.dashboard.core.model;

/**
 * Server details bean class.
 */
public class ServerDetails {

    private SiddhiApps siddhiApps;
    private WorkerMetrics workerMetrics;
    private String haStatus;
    private Boolean isStatsEnabled;
    private String clusterID;
    private String lastSyncTime;
    private String lastSnapshotTime;
    private String osName;
    private String runningStatus;
    private String message;

    public ServerDetails() {
    }

    public WorkerMetrics getWorkerMetrics() {
        return workerMetrics;
    }

    public void setWorkerMetrics(WorkerMetrics workerMetrics) {
        this.workerMetrics = workerMetrics;
    }

    public SiddhiApps getSiddhiApps() {
        return siddhiApps;
    }

    public void setSiddhiApps(int active, int inactive) {
        this.siddhiApps = new SiddhiApps(active, inactive);
    }

    public boolean isStatEnabled() {
        return isStatsEnabled;
    }

    public void setStatEnabled(boolean statEnabled) {
        isStatsEnabled = statEnabled;
    }

    public String getHAStatus() {
        return haStatus;
    }

    public void setHAStatus(String HAStatus) {
        this.haStatus = HAStatus;
    }

    public String getClusterId() {
        return clusterID;
    }

    public void setClusterId(String clusterId) {
        this.clusterID = clusterId;
    }

    public String getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(String lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public String getRunningStatus() {
        return runningStatus;
    }

    public String getLastSnapshotTime() {
        return lastSnapshotTime;
    }

    public void setLastSnapshotTime(String lastSnapshotTime) {
        this.lastSnapshotTime = lastSnapshotTime;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public void setRunningStatus(String runningStatus) {
        this.runningStatus = runningStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
