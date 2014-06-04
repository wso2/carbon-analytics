package org.wso2.carbon.bam.cassandra.data.archive.util;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Date;


public class ArchiveConfiguration {

    private String streamName;
    private String version;
    private Date startDate;
    private Date endDate;
    private int noOfDays;
    private String cronExpression;
    private String userName;
    private String password;
    private boolean isSchedulingOn;
    private String connectionURL;


    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public boolean isSchedulingOn() {
        return isSchedulingOn;
    }

    public void setSchedulingOn(boolean schedulingOn) {
        isSchedulingOn = schedulingOn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getNoOfDays() {
        return noOfDays;
    }

    public void setNoOfDays(int noOfDays) {
        this.noOfDays = noOfDays;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date dateTime) {
        this.startDate = dateTime;
    }
}
