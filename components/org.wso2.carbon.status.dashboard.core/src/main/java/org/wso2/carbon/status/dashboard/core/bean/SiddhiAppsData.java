/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
