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
package org.wso2.carbon.das.jobmanager.core.model;


import org.wso2.carbon.das.jobmanager.core.bean.InterfaceConfig;

import java.io.Serializable;

/**
 * This class represents a resource node.
 */
public class ResourceNode implements Serializable {
    private static final long serialVersionUID = 7198320219118722368L;
    private String id;
    private String state;
    private InterfaceConfig httpInterface;
    private long lastPingTimestamp;
    private int failedPingAttempts;

    public ResourceNode(String id) {
        this.id = id;
        this.lastPingTimestamp = System.currentTimeMillis();
        this.failedPingAttempts = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public InterfaceConfig getHttpInterface() {
        return httpInterface;
    }

    public void setHttpInterface(InterfaceConfig httpInterface) {
        this.httpInterface = httpInterface;
    }

    public long getLastPingTimestamp() {
        return lastPingTimestamp;
    }

    public void updateLastPingTimestamp() {
        this.lastPingTimestamp = System.currentTimeMillis();
    }

    public int getFailedPingAttempts() {
        return failedPingAttempts;
    }

    public void resetFailedPingAttempts() {
        failedPingAttempts = 0;
    }

    public void incrementFailedPingAttempts() {
        failedPingAttempts += 1;
    }

    @Override
    public String toString() {
        return String.format("ResourceNode { id: %s, host: %s, port: %s }",
                getId(), getHttpInterface().getHost(), getHttpInterface().getPort());
    }

    @Override
    public boolean equals(Object o) {
        // Do not consider lastPingTimestamp and failedPingAttempts for the equals method.
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceNode that = (ResourceNode) o;
        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) {
            return false;
        }
        if (getState() != null ? !getState().equals(that.getState()) : that.getState() != null) {
            return false;
        }
        return getHttpInterface() != null
                ? getHttpInterface().equals(that.getHttpInterface()) : that.getHttpInterface() == null;
    }

    @Override
    public int hashCode() {
        // Do not consider lastPingTimestamp and failedPingAttempts for the hash method.
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getState() != null ? getState().hashCode() : 0);
        result = 31 * result + (getHttpInterface() != null ? getHttpInterface().hashCode() : 0);
        return result;
    }
}
