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

import java.util.concurrent.TimeUnit;

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
        this.agetime = getTimeAgo(age);
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
        this.age = age;
    }

    public String getAgetime() {
        return agetime;
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
     * Get human readable time format
     * @param millis milliseconds
     * @return return human readable format
     */
    public static String getTimeAgo(long millis) {
        String age = "";
        long days= TimeUnit.MILLISECONDS.toDays(millis);
        long hrs= TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
        long min= TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours
                (millis));
        long sec=  TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                .toMinutes(millis));

        if(days>0){
            age=age+" "+days+"d";
        }
        if(hrs>0){
            age=age+" "+hrs+"h";
        }
        if(min>0){
            age=age+" "+min+"m";
        }
        if(sec>0){
            age=age+" "+sec+"s";
        } else {
            age=age+" "+millis+" ms";
        }
        return age+" ago";
    }
}
