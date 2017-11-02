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


import java.io.Serializable;

/**
 * Representation of a Manager Node configuration. This extends {@link NodeConfig}.
 */
public class ManagerNodeConfig extends NodeConfig implements Serializable {
    private static final long serialVersionUID = -2345076276326287573L;
    /**
     * Heartbeat interval of the manager node.
     */
    private long heartbeatInterval = 1000;
    /**
     * Maximum retries for heartbeat before marking the resource node as expired.
     */
    private long heartbeatMaxRetry = 2;

    /**
     * Getter for the heartbeatInterval.
     *
     * @return heartbeatInterval.
     */
    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    /**
     * Setter for the heartbeatInterval.
     *
     * @param heartbeatInterval heartbeatInterval.
     */
    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    /**
     * Getter for the heartbeatMaxRetry.
     *
     * @return heartbeatMaxRetry.
     */
    public long getHeartbeatMaxRetry() {
        return heartbeatMaxRetry;
    }

    /**
     * Setter for the heartbeatMaxRetry.
     *
     * @param heartbeatMaxRetry heartbeatMaxRetry.
     */
    public void setHeartbeatMaxRetry(long heartbeatMaxRetry) {
        this.heartbeatMaxRetry = heartbeatMaxRetry;
    }

    @Override
    public String toString() {
        return String.format("ManagerNode { id: %s, host: %s, port: %s }",
                getId(), getHttpInterface().getHost(), getHttpInterface().getPort());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ManagerNodeConfig that = (ManagerNodeConfig) o;
        if (getHeartbeatInterval() != that.getHeartbeatInterval()) {
            return false;
        }
        return getHeartbeatMaxRetry() == that.getHeartbeatMaxRetry();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (getHeartbeatInterval() ^ (getHeartbeatInterval() >>> 32));
        result = 31 * result + (int) (getHeartbeatMaxRetry() ^ (getHeartbeatMaxRetry() >>> 32));
        return result;
    }
}
