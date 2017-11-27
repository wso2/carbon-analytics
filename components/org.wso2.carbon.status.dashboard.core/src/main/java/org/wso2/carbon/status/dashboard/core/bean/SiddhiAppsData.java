package org.wso2.carbon.status.dashboard.core.bean;

import java.util.List;

/**
 * Siddhi app metrics and total app count.
 */
public class SiddhiAppsData {
    private List<SiddhiAppStatus> siddhiAppMetricsHistoryList;
    private int totalAppsCount;
    private int currentPageNum;
    private int maxPageCount;

    public SiddhiAppsData(int currentPageNum) {
        this.currentPageNum = currentPageNum;
    }

    public List<SiddhiAppStatus> getSiddhiAppMetricsHistoryList() {
        return siddhiAppMetricsHistoryList;
    }

    public void setSiddhiAppMetricsHistoryList(List<SiddhiAppStatus> siddhiAppMetricsHistoryList) {
        this.siddhiAppMetricsHistoryList = siddhiAppMetricsHistoryList;
    }

    public int getTotalAppsCount() {
        return totalAppsCount;
    }

    public void setTotalAppsCount(int totalAppsCount) {
        this.totalAppsCount = totalAppsCount;
    }

    public int getCurrentPageNum() {
        return currentPageNum;
    }

    public void setCurrentPageNum(int currentPageNum) {
        this.currentPageNum = currentPageNum;
    }

    public int getMaxPageCount() {
        return maxPageCount;
    }

    public void setMaxPageCount(int maxPageCount) {
        this.maxPageCount = maxPageCount;
    }
}
