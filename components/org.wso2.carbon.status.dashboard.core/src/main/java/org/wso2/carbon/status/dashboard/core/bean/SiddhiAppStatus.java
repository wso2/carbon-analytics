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
package org.wso2.carbon.status.dashboard.core.bean;

/**
 * Bean class use for store siddhi app statistics.
 */
public class SiddhiAppStatus {
    private String appName;
    private String status;
    private int age;
    private String agetime;
    private boolean isStatEnabled;
    private SiddhiAppMetricsHistory appMetricsHistory;

    public SiddhiAppStatus() {
        //to make seconds
        this.agetime = getTimeAgo(age);
        this.age = this.age/1000;
    }

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        //to make seconds
        this.agetime = getTimeAgo(age);
        this.age = age/1000;
    }

    public boolean isStatEnabled() {
        return isStatEnabled;
    }

    public void setStatEnabled(boolean statEnabled) {
        isStatEnabled = statEnabled;
    }

    public SiddhiAppMetricsHistory getAppMetricsHistory() {
        return appMetricsHistory;
    }

    public void setAppMetricsHistory(SiddhiAppMetricsHistory appMetricsHistory) {
        this.appMetricsHistory = appMetricsHistory;
    }

    /**
     * Get human redable time format
     * @param diff milliseconds
     * @return
     */
    public static String getTimeAgo(long diff) {
        int SECOND = 1;
        int MINUTE_SECOND = 60 * SECOND;
        int HOUR_SECOND = 60 * MINUTE_SECOND;
        int DAY_MILLIS = 24 * HOUR_SECOND;
        if (diff < SECOND) {
            return "just now";
        } else if (diff < 2 * MINUTE_SECOND) {
            return "a min ago";
        } else if (diff < 50 * MINUTE_SECOND) {
            return diff / MINUTE_SECOND + " min ago";
        } else if (diff < 90 * MINUTE_SECOND) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_SECOND) {
            return diff / HOUR_SECOND + " hrs ago";
        } else if (diff < 48 * HOUR_SECOND) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
}
