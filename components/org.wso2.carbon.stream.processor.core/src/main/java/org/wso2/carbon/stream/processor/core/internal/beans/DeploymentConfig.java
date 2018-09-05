/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stream.processor.core.internal.beans;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Bean class for the deployment configurations.
 */
@Configuration(namespace = "deployment.config", description = "Cluster Coordination Mode Configuration")
public class DeploymentConfig {

    private String type;
    private TCPServerConfig tcpServer;
    private int eventByteBufferQueueCapacity = 20000;
    private String passiveNodeHost;
    private int passiveNodePort;
    @Element(description = "Event sync client pool configurations", required = false)
    private EventSyncClientPoolConfig eventSyncClientPool = new EventSyncClientPoolConfig();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TCPServerConfig getTcpServerConfigs() {

        return tcpServer;
    }

    public void setTcpServer(TCPServerConfig tcpServer) {

        this.tcpServer = tcpServer;
    }

    public int getEventByteBufferQueueCapacity() {

        return eventByteBufferQueueCapacity;
    }

    public void setEventByteBufferQueueCapacity(int eventByteBufferQueueCapacity) {
        this.eventByteBufferQueueCapacity = eventByteBufferQueueCapacity;
    }

    public EventSyncClientPoolConfig getTcpClientPoolConfig() {
        return eventSyncClientPool;
    }

    public void setTcpClientPoolConfig(EventSyncClientPoolConfig eventSyncClientPool) {
        this.eventSyncClientPool = eventSyncClientPool;
    }

    public String getPassiveNodeHost() {
        return passiveNodeHost;
    }

    public void setPassiveNodeHost(String passiveNodeHost) {
        this.passiveNodeHost = passiveNodeHost;
    }

    public int getPassiveNodePort() {
        return passiveNodePort;
    }

    public void setPassiveNodePort(int passiveNodePort) {
        this.passiveNodePort = passiveNodePort;
    }
}
