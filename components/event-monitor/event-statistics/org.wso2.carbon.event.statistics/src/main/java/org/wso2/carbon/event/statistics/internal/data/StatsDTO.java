/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.statistics.internal.data;

public class StatsDTO {

    private String name;
    private String type;

    private String requestLastUpdatedTime;
    private long requestTotalCount;
    private long requestMaxCountPerSec;
    private double requestAvgCountPerSec;
    private long requestLastSecCount;
    private long requestLastMinCount;
    private long requestLast15MinCount;
    private long requestLastHourCount;
    private long requestLast6HourCount;
    private long requestLastDayCount;


    private String responseLastUpdatedTime;
    private long responseTotalCount;
    private long responseMaxCountPerSec;
    private double responseAvgCountPerSec;
    private long responseLastSecCount;
    private long responseLastMinCount;
    private long responseLast15MinCount;
    private long responseLastHourCount;
    private long responseLast6HourCount;
    private long responseLastDayCount;

    private String[] childStats;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestLastUpdatedTime() {
        return requestLastUpdatedTime;
    }

    public void setRequestLastUpdatedTime(String requestLastUpdatedTime) {
        this.requestLastUpdatedTime = requestLastUpdatedTime;
    }

    public String getResponseLastUpdatedTime() {
        return responseLastUpdatedTime;
    }

    public void setResponseLastUpdatedTime(String responseLastUpdatedTime) {
        this.responseLastUpdatedTime = responseLastUpdatedTime;
    }

    public long getRequestTotalCount() {
        return requestTotalCount;
    }

    public void setRequestTotalCount(long requestTotalCount) {
        this.requestTotalCount = requestTotalCount;
    }

    public long getResponseTotalCount() {
        return responseTotalCount;
    }

    public void setResponseTotalCount(long responseTotalCount) {
        this.responseTotalCount = responseTotalCount;
    }

    public long getRequestMaxCountPerSec() {
        return requestMaxCountPerSec;
    }

    public void setRequestMaxCountPerSec(long requestMaxCountPerSec) {
        this.requestMaxCountPerSec = requestMaxCountPerSec;
    }

    public long getResponseMaxCountPerSec() {
        return responseMaxCountPerSec;
    }

    public void setResponseMaxCountPerSec(long responseMaxCountPerSec) {
        this.responseMaxCountPerSec = responseMaxCountPerSec;
    }

    public double getRequestAvgCountPerSec() {
        return requestAvgCountPerSec;
    }

    public void setRequestAvgCountPerSec(double requestAvgCountPerSec) {
        this.requestAvgCountPerSec = requestAvgCountPerSec;
    }

    public double getResponseAvgCountPerSec() {
        return responseAvgCountPerSec;
    }

    public void setResponseAvgCountPerSec(double responseAvgCountPerSec) {
        this.responseAvgCountPerSec = responseAvgCountPerSec;
    }

    public long getRequestLastSecCount() {
        return requestLastSecCount;
    }

    public void setRequestLastSecCount(long requestLastSecCount) {
        this.requestLastSecCount = requestLastSecCount;
    }

    public long getRequestLastMinCount() {
        return requestLastMinCount;
    }

    public void setRequestLastMinCount(long requestLastMinCount) {
        this.requestLastMinCount = requestLastMinCount;
    }

    public long getRequestLast15MinCount() {
        return requestLast15MinCount;
    }

    public void setRequestLast15MinCount(long requestLast15MinCount) {
        this.requestLast15MinCount = requestLast15MinCount;
    }

    public long getRequestLastHourCount() {
        return requestLastHourCount;
    }

    public void setRequestLastHourCount(long requestLastHourCount) {
        this.requestLastHourCount = requestLastHourCount;
    }

    public long getRequestLast6HourCount() {
        return requestLast6HourCount;
    }

    public void setRequestLast6HourCount(long requestLast6HourCount) {
        this.requestLast6HourCount = requestLast6HourCount;
    }

    public long getRequestLastDayCount() {
        return requestLastDayCount;
    }

    public void setRequestLastDayCount(long requestLastDayCount) {
        this.requestLastDayCount = requestLastDayCount;
    }

    public long getResponseLastSecCount() {
        return responseLastSecCount;
    }

    public void setResponseLastSecCount(long responseLastSecCount) {
        this.responseLastSecCount = responseLastSecCount;
    }

    public long getResponseLastMinCount() {
        return responseLastMinCount;
    }

    public void setResponseLastMinCount(long responseLastMinCount) {
        this.responseLastMinCount = responseLastMinCount;
    }

    public long getResponseLast15MinCount() {
        return responseLast15MinCount;
    }

    public void setResponseLast15MinCount(long responseLast15MinCount) {
        this.responseLast15MinCount = responseLast15MinCount;
    }

    public long getResponseLastHourCount() {
        return responseLastHourCount;
    }

    public void setResponseLastHourCount(long responseLastHourCount) {
        this.responseLastHourCount = responseLastHourCount;
    }

    public long getResponseLast6HourCount() {
        return responseLast6HourCount;
    }

    public void setResponseLast6HourCount(long responseLast6HourCount) {
        this.responseLast6HourCount = responseLast6HourCount;
    }

    public long getResponseLastDayCount() {
        return responseLastDayCount;
    }

    public void setResponseLastDayCount(long responseLastDayCount) {
        this.responseLastDayCount = responseLastDayCount;
    }

    public String[] getChildStats() {
        return childStats;
    }

    public void setChildStats(String[] childStats) {
        this.childStats = childStats;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
