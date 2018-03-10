package org.wso2.carbon.status.dashboard.core.model;

import org.wso2.carbon.status.dashboard.core.bean.ManagerClusterInfo;

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
