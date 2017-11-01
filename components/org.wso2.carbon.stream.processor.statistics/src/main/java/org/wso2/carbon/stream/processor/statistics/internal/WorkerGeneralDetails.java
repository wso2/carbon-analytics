/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.stream.processor.statistics.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This is a bean class for keeping the worker general details.
 */
public class WorkerGeneralDetails {
    private static final Log logger = LogFactory.getLog(WorkerGeneralDetails.class);
    private static WorkerGeneralDetails instance = new WorkerGeneralDetails();
    private String carbonId;
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
    private long serverStartTime;
    private long lastSnapshotTime;

    private WorkerGeneralDetails() {
        init();
    }

    public static WorkerGeneralDetails getInstance(){
        return instance;
    }

    private void init() {
        repoLocation = System.getProperty("carbon.home") + "/deployment";
        serverStartTime = Long.parseLong(System.getProperty("carbon.start.time"));
        javaRuntimeName = System.getProperty("java.runtime.name");
        javaVMVersion = System.getProperty("java.vm.version");
        javaVMVendor = System.getProperty("java.vm.vendor");
        javaVersion = System.getProperty("java.version");
        userCountry = System.getProperty("user.country");
        userHome = System.getProperty("user.home");
        userTimezone = System.getProperty("user.timezone");
        userName = System.getProperty("user.name");
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
        javaHome = System.getProperty("java.home");
        carbonId = getCarbonID();
        lastSnapshotTime = System.currentTimeMillis();
    }

    private String getCarbonID() {
        try {
            ConfigProvider configProvider = StreamProcessorStatisticDataHolder.getInstance().getConfigProvider();
            CarbonConfiguration carbonConfiguration = configProvider.getConfigurationObject(CarbonConfiguration.class);
            return carbonConfiguration.getId();
        } catch (ConfigurationException e) {
            logger.error("Error in fetching data from carbon config provider.",e);
        }

        return getDefaultSource();
    }

    /**
     * A utility method to provide a default source value
     *
     * @return The default source, if it is available, otherwise return the hostname. If the hostname is also not
     * available, it will return "Carbon".
     */
    public static String getDefaultSource() {
            // Use host name if available
            String hostname = null;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                // Ignore exception
            }
            if (hostname == null || hostname.trim().length() == 0) {
               return  "Carbon";
            } else {
                return hostname;
            }
    }

    public String getJavaRuntimeName() {
        return javaRuntimeName;
    }

    public String getJavaVMVersion() {
        return javaVMVersion;
    }

    public String getJavaVMVendor() {
        return javaVMVendor;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getUserHome() {
        return userHome;
    }

    public String getUserTimezone() {
        return userTimezone;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public String getRepoLocation() {
        return repoLocation;
    }

    public long getServerStartTime() {
        return serverStartTime;
    }

    public long getLastSnapshotTime() {
        return lastSnapshotTime;
    }

    public String getCarbonId() {
        return carbonId;
    }

    public void setCarbonId(String carbonId) {
        this.carbonId = carbonId;
    }

    public String toString() {
        return "JVM Name='" + javaRuntimeName + '\'' +
                ", JVM Version='" + javaVMVersion + '\'' +
                ", JVM Vendor='" + javaVMVendor + '\'' +
                ", Java Home='" + javaHome + '\'' +
                ", OS Name='" + osName + '\'' +
                ", OS Version='" + osVersion + '\'' +
                ", User Home='" + userHome + '\'' +
                ", User Timezone='" + userTimezone + '\'' +
                ", Username='" + userName + '\'' +
                ", User Country='" + userCountry + '\'' +
                ", Repository Location='" + repoLocation + '\'';
    }

}