/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.api.internal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AnalyticsDataConfiguration")
public class AnalyticsDataConfiguration {
    private String mode;
    private String endpoint;
    private String username;
    private String password;
    private Mode operationMode;
    private int maxActive;
    private int maxIdle;
    private boolean testOnBorrow;
    private long timeBetweenEvictionRunsMillis;
    private long minEvictableIdleTimeMillis;

    @XmlElement(name = "Mode")
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @XmlElement(name = "URL")
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @XmlElement(name = "Username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlElement(name = "Password")
    public String getPassword() {
        //TODO: secure vault
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Mode getOperationMode() {
        if (operationMode == null) {
            synchronized (this) {
                if (operationMode == null) {
                    if (mode == null) {
                        operationMode = Mode.LOCAL;
                    } else if (mode.equalsIgnoreCase(Mode.REMOTE.toString())) {
                        operationMode = Mode.REMOTE;
                    } else {
                        operationMode = Mode.LOCAL;
                    }
                }
            }
        }
        return operationMode;
    }

    @XmlElement(name = "MaxActiveConnections")
    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    @XmlElement(name = "MinIdleTimeMS")
    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    @XmlElement(name = "TestOnBorrow")
    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    @XmlElement(name = "TimeBetweenEvictionRunMS")
    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    @XmlElement(name = "MinEvictableIdleTimeMS")
    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public enum Mode {
        LOCAL, REMOTE
    }
}
