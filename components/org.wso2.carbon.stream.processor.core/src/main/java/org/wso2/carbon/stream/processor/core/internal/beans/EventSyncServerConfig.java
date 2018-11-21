/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.stream.processor.core.internal.beans;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.stream.processor.core.internal.util.TCPServerConstants;

/**
 * Bean class for the Event Sync Server Configurations.
 */
@Configuration(description = "Event Sync Server Configurations")
public class EventSyncServerConfig {

    private String host = TCPServerConstants.DEFAULT_HOST;
    private int port = TCPServerConstants.DEFAULT_PORT;
    @Element(description = "host to which the events by the active node must be directed,", required = false)
    private String advertisedHost;
    @Element(description = "port to which the events by the active node must be directed,", required = false)
    private int advertisedPort;
    @Element(description = "Boss threads to handle connection", required = false)
    private int bossThreads = TCPServerConstants.DEFAULT_BOSS_THREADS;
    @Element(description = "worker threads", required = false)
    private int workerThreads = TCPServerConstants.DEFAULT_WORKER_THREADS;

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

    public int getBossThreads() {

        return bossThreads;
    }

    public void setBossThreads(int bossThreads) {

        this.bossThreads = bossThreads;
    }

    public int getWorkerThreads() {

        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {

        this.workerThreads = workerThreads;
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
}
