package org.wso2.carbon.status.dashboard.core.bean;

import org.wso2.carbon.status.dashboard.core.model.DistributedServerDetails;

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
