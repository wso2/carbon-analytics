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


import org.wso2.carbon.status.dashboard.core.dbhandler.exceptions.StatusDashboardValidationException;

import java.util.List;

/**
 * Bean class for worker general details.
 */
public class WorkerGeneralDetails {
    private String carbonId;
    private String workerId;
    private String javaRuntimeName;
    private String javaVMVersion;
    private String javaVMVendor;
    private String javaHome;
    private String javaVersion;
    private String osName;
    private String osVersion;
    private String userHome;
    private String userTimezone;
    private String userName;
    private String userCountry;
    private String repoLocation;
    private Long serverStartTime;
    private Long lastSnapshotTime;

    public WorkerGeneralDetails() {
    }

    public String getJavaRuntimeName() {
        return javaRuntimeName;
    }

    public void setJavaRuntimeName(String javaRuntimeName) {
        this.javaRuntimeName = javaRuntimeName;
    }

    public String getJavaVMVersion() {
        return javaVMVersion;
    }

    public void setJavaVMVersion(String javaVMVersion) {
        this.javaVMVersion = javaVMVersion;
    }

    public String getJavaVMVendor() {
        return javaVMVendor;
    }

    public void setJavaVMVendor(String javaVMVendor) {
        this.javaVMVendor = javaVMVendor;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getUserHome() {
        return userHome;
    }

    public void setUserHome(String userHome) {
        this.userHome = userHome;
    }

    public String getUserTimezone() {
        return userTimezone;
    }

    public void setUserTimezone(String userTimezone) {
        this.userTimezone = userTimezone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public void setUserCountry(String userCountry) {
        this.userCountry = userCountry;
    }

    public String getRepoLocation() {
        return repoLocation;
    }

    public void setRepoLocation(String repoLocation) {
        this.repoLocation = repoLocation;
    }

    public Long getServerStartTime() {
        return serverStartTime;
    }

    public void setServerStartTime(Long serverStartTime) {
        this.serverStartTime = serverStartTime;
    }

    public Long getLastSnapshotTime() {
        return lastSnapshotTime;
    }

    public void setLastSnapshotTime(Long lastSnapshotTime) {
        this.lastSnapshotTime = lastSnapshotTime;
    }

    public String getCarbonId() {
        return carbonId;
    }

    public void setCarbonId(String carbonId) {
        this.carbonId = carbonId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public Object[] toArray() {
        return new Object[]{carbonId, workerId, javaRuntimeName, javaVMVersion, javaVMVendor, javaHome, javaVersion,
                osName,
                osVersion, userHome, userTimezone, userName, userCountry, repoLocation, serverStartTime
                , System.currentTimeMillis()};

    }

    public void setArrayList(List values) throws StatusDashboardValidationException {

        Object[] objects = new Object[]{carbonId, workerId, javaRuntimeName, javaVMVersion, javaVMVendor,
                javaHome, javaVersion, osName, osVersion, userHome, userTimezone, userName, userCountry,
                repoLocation, serverStartTime, System.currentTimeMillis()};
        if (values.size() != objects.length) {
            throw new StatusDashboardValidationException("Invalid length of object");
        }
        for (int i = 0; i < objects.length; i++) {
            switch (i) {
                case 0:
                    carbonId = (String) values.get(i);
                    break;
                case 1:
                    workerId = (String) values.get(i);
                    break;
                case 2:
                    javaRuntimeName = (String) values.get(i);
                    break;  //optional
                case 3:
                    javaVMVersion = (String) values.get(i);
                    break;  //optional
                case 4:
                    javaVMVendor = (String) values.get(i);
                    break;
                case 5:
                    javaHome = (String) values.get(i);
                    break;  //optional
                case 6:
                    javaVersion = (String) values.get(i);
                    break;
                case 7:
                    osName = (String) values.get(i);
                    break;  //optional
                case 8:
                    osVersion = (String) values.get(i);
                    break;
                case 9:
                    userHome = (String) values.get(i);
                    break;  //optional
                case 10:
                    userTimezone = (String) values.get(i);
                    break;  //optional
                case 11:
                    userName = (String) values.get(i);
                    break;  //optional
                case 12:
                    userCountry = (String) values.get(i);
                    break;
                case 13:
                    repoLocation = (String) values.get(i);
                    break;  //optional
                case 14:
                    serverStartTime = (Long) values.get(i);
                    break;
                case 15:
                    lastSnapshotTime = (Long) values.get(i);
                    break;
                default:
                    throw new StatusDashboardValidationException("Invalid length of object");
            }
        }


    }

    public static String getColumnLabeles() {
        return "CARBONID,WORKERID,JAVARUNTIMENAME,JAVAVMVERSION,JAVAVMVENDOR,JAVAHOME,JAVAVERSION,OSNAME," +
                "OSVERSION,USERHOME,USERTIMEZONE,USERNAME,USERCOUNTRY,REPOLOCATION,SERVERSTARTTIME,LASTSNAPSHOTTIME";
    }

}
