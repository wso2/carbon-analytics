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
package org.wso2.carbon.sp.distributed.resource.core.bean;


import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.io.Serializable;

/**
 * This class represents a HTTPS Interface configuration which consists of host and port of the node.
 */
@Configuration(description = "HTTPS Interface Configuration")
public class HTTPSInterfaceConfig implements Serializable {

    private static final long serialVersionUID = -2797775483534299019L;

    /**
     * Host name of the node.
     */
    @Element(description = "host name of the node", required = true)
    private String host;

    /**
     * Port of the node.
     */
    @Element(description = "port of the node", required = true)
    private int port;

    /**
     * Host name of the node.
     */
    @Element(description = "username for authentication of the node", required = true)
    private String username = "admin";
    /**
     * Port of the node.
     */
    @Element(description = "password for authentication of the node", required = true)
    private String password = "admin";

    /**
     * Getter for the host name.
     *
     * @return host name.
     */
    public String getHost() {
        return host;
    }

    /**
     * Setter for the host name.
     *
     * @param host host name.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Getter for the port.
     *
     * @return port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Setter for the port
     *
     * @param port port.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Getter of the username.
     *
     * @return username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter of the username
     *
     * @param username username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter of the password
     *
     * @return password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter of the password
     *
     * @param password password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("HTTPSInterface { host: %s, port: %s }", host, port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HTTPSInterfaceConfig that = (HTTPSInterfaceConfig) o;
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
