/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.stream.processor.statistics.bean;

/**
 * This is the bean class which keep the statistical details other than metrics.
 */
public class WorkerStatistics {

    private WorkerMetrics workerMetrics;
    private String haStatus;
    private String runningStatus;
    private boolean isStatsEnabled;
    private String clusterID;
    private String lastSyncTime;
    private String lastSnapshotTime;
    private String osName;
    private boolean isInSync;

    public WorkerMetrics getWorkerMetrics() {
        return workerMetrics;
    }

    public void setWorkerMetrics(WorkerMetrics workerMetrics) {
        this.workerMetrics = workerMetrics;
    }

    public String getHaStatus() {
        return haStatus;
    }

    public void setHaStatus(String haStatus) {
        this.haStatus = haStatus;
    }

    public boolean isStatsEnabled() {
        return isStatsEnabled;
    }

    public void setStatsEnabled(boolean statsEnabled) {
        isStatsEnabled = statsEnabled;
    }

    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(String clusterID) {
        this.clusterID = clusterID;
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

    public void setRunningStatus(String runningStatus) {
        this.runningStatus = runningStatus;
    }

    public String getOsName() {return osName;}

    public void setOsName(String osName) {this.osName = osName;}

    public String getLastSnapshotTime() {
        return lastSnapshotTime;
    }

    public void setLastSnapshotTime(String lastSnapshotTime) {
        this.lastSnapshotTime = lastSnapshotTime;
    }

    public boolean isInSync() {
        return isInSync;
    }

    public void setInSync(boolean inSync) {
        isInSync = inSync;
    }
}
