/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.das.jobmanager.core.beans;


import org.wso2.carbon.config.annotation.Configuration;

/**
 * This class represents the cluster coordination mode configuration.
 */
@Configuration(description = "Cluster Coordination Mode Configuration")
public class CoordinationModeConfig {
    private String type;
    private String liveStateSync;
    private int publisherSyncInterval;
    private String advertisedHost;
    private int advertisedPort;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLiveStateSync() {
        return liveStateSync;
    }

    public void setLiveStateSync(String liveStateSync) {
        this.liveStateSync = liveStateSync;
    }

    public int getPublisherSyncInterval() {
        return publisherSyncInterval;
    }

    public void setPublisherSyncInterval(int publisherSyncInterval) {
        this.publisherSyncInterval = publisherSyncInterval;
    }

    public String getAdvertisedHost() {
        return advertisedHost;
    }

    public void setAdvertisedHost(String advertisedHost) {
        this.advertisedHost = advertisedHost;
    }

    public int getAdvertisedPort() {
        return advertisedPort;
    }

    public void setAdvertisedPort(int advertisedPort) {
        this.advertisedPort = advertisedPort;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode() not implemented";
        return -1;
    }
}
