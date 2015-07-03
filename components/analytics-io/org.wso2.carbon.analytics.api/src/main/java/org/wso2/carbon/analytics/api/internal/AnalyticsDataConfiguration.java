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

/**
 * This class JAXB configuration object which represents the configuration from analytics-data-config.xml.
 */
@XmlRootElement(name = "AnalyticsDataConfiguration")
public class AnalyticsDataConfiguration {
    private String mode;
    private String endpoint;
    private String username;
    private String password;
    private Mode operationMode;
    private int maxConnections;
    private int maxConnectionsPerRoute;
    private int socketConnectionTimeoutMS;
    private int connectionTimeoutMS;
    private String trustStoreLocation;
    private String trustStorePassword;

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
                        operationMode = Mode.AUTO;
                    } else if (mode.equalsIgnoreCase(Mode.REMOTE.toString())) {
                        operationMode = Mode.REMOTE;
                    } else if (mode.equalsIgnoreCase(Mode.LOCAL.toString())){
                        operationMode = Mode.LOCAL;
                    }else {
                        operationMode = Mode.AUTO;
                    }
                }
            }
        }
        return operationMode;
    }

    @XmlElement(name = "MaxConnections")
    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    @XmlElement(name = "MaxConnectionsPerRoute")
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    @XmlElement(name = "SocketConnectionTimeout")
    public int getSocketConnectionTimeoutMS() {
        return socketConnectionTimeoutMS;
    }

    public void setSocketConnectionTimeoutMS(int socketConnectionTimeoutMS) {
        this.socketConnectionTimeoutMS = socketConnectionTimeoutMS;
    }

    @XmlElement(name = "ConnectionTimeout")
    public int getConnectionTimeoutMS() {
        return connectionTimeoutMS;
    }

    public void setConnectionTimeoutMS(int connectionTimeoutMS) {
        this.connectionTimeoutMS = connectionTimeoutMS;
    }

    public enum Mode {
        LOCAL, REMOTE, AUTO;
    }

    @XmlElement(name = "TrustStoreLocation")
    public String getTrustStoreLocation() {
        return trustStoreLocation;
    }

    public void setTrustStoreLocation(String trustStoreLocation) {
        this.trustStoreLocation = trustStoreLocation;
    }

    @XmlElement(name = "TrustStorePassword")
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }
}
