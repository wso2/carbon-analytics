/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sp.jobmanager.core.bean;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.io.Serializable;

/**
 * This class represents a HTTP Interface configuration which consists of host and port of the node.
 */
@Configuration(description = "HTTP Interface Configuration")
public class InterfaceConfig implements Serializable {
    private static final long serialVersionUID = 8392492664142835402L;
    @Element(description = "host name of the node", required = true)
    private String host;

    @Element(description = "port of the node", required = true)
    private int port;

    @Element(description = "username for authentication of the node", required = true)
    private String username = "admin";

    @Element(description = "password for authentication of the node", required = true)
    private String password = "admin";

    public InterfaceConfig() {
    }

    public InterfaceConfig(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("Interface { host: %s, port: %s }", host, port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InterfaceConfig that = (InterfaceConfig) o;
        if (getPort() != that.getPort()) {
            return false;
        }
        return getHost() != null ? getHost().equals(that.getHost()) : that.getHost() == null;
    }

    @Override
    public int hashCode() {
        int result = getHost() != null ? getHost().hashCode() : 0;
        result = 31 * result + getPort();
        return result;
    }
}
