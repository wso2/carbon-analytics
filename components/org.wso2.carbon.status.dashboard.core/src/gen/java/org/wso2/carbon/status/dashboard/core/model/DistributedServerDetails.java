package org.wso2.carbon.status.dashboard.core.model;

public class DistributedServerDetails {
    private SiddhiApps siddhiApps;
    private WorkerMetrics workerMetrics;
    private boolean isStatsEnabled;
    private String lastSyncTime;
    private String lastSnapshotTime;
    private String osName;
    private String runningStatus;
    private String message;

    public DistributedServerDetails() {
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
