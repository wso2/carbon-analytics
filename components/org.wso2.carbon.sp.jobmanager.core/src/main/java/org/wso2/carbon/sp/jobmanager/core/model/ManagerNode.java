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

package org.wso2.carbon.sp.jobmanager.core.model;

import org.wso2.carbon.sp.jobmanager.core.bean.InterfaceConfig;

import java.io.Serializable;

/**
 * This class represents a Manager node.
 */
public class ManagerNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id = "wso2-sp";
    private InterfaceConfig httpsInterface;
    private int heartbeatInterval = 1000;
    private int heartbeatMaxRetry = 2;

    public String getId() {
        return id;
    }

    public ManagerNode setId(String id) {
        this.id = id;
        return this;
    }

    public InterfaceConfig getHttpsInterface() {
        return httpsInterface;
    }

    public ManagerNode setHttpsInterface(InterfaceConfig httpsInterface) {
        this.httpsInterface = httpsInterface;
        return this;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public ManagerNode setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
        return this;
    }

    public int getHeartbeatMaxRetry() {
        return heartbeatMaxRetry;
    }

    public ManagerNode setHeartbeatMaxRetry(int heartbeatMaxRetry) {
        this.heartbeatMaxRetry = heartbeatMaxRetry;
        return this;
    }

    @Override
    public String toString() {
        return String.format("ManagerNode { id: %s, host: %s, port: %s }",
                getId(), getHttpsInterface().getHost(), getHttpsInterface().getPort());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!ManagerNode.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        ManagerNode that = (ManagerNode) obj;
        if ((this.id == null) ? (that.id != null) : !this.id.equals(that.id)) {
            return false;
        }
        if (!this.httpsInterface.equals(that.httpsInterface)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + getHeartbeatInterval();
        return result;
    }
}
