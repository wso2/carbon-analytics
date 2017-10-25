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

public class Heartbeat {
    private String nodeId;
    private long lastUpdatedTimestamp;
    private int failedAttempts;
    private boolean expired;

    public Heartbeat(String nodeId) {
        this.nodeId = nodeId;
        this.lastUpdatedTimestamp = System.currentTimeMillis();
        this.failedAttempts = 0;
        this.expired = false;
    }

    public String getNodeId() {
        return nodeId;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void updateTimestamp() {
        this.lastUpdatedTimestamp = System.currentTimeMillis();
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void resetFailedAttempts() {
        failedAttempts = 0;
    }

    public void incrementFailedAttempts() {
        failedAttempts += 1;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    @Override
    public String toString() {
        return String.format("HeartBeat{ nodeId: %s, lastUpdatedTimestamp: %s, failedAttempts: %s, isExpired: %s}",
                nodeId, lastUpdatedTimestamp, failedAttempts, expired);
    }
}
