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

/**
 * Bean class for the deployment configurations.
 */
@Configuration(namespace = "deployment.config", description = "Cluster Coordination Mode Configuration")
public class DeploymentConfig {

    private String type;
    private TCPServerConfig tcpServer;
    private int recordTableQueueCapacity = 20000;
    private int eventByteBufferQueueCapacity = 20000;
    private String passiveNodeHost;
    private int passiveNodePort;
    private TCPClientPoolConfig tcpClientPool;

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

    public TCPClientPoolConfig getTcpClientPoolConfig() {
        return tcpClientPool;
    }

    public void setTcpClientPoolConfig(TCPClientPoolConfig tcpClientPool) {
        this.tcpClientPool = tcpClientPool;
    }

    public int getRecordTableQueueCapacity() {

        return recordTableQueueCapacity;
    }

    public void setRecordTableQueueCapacity(int recordTableQueueCapacity) {

        this.recordTableQueueCapacity = recordTableQueueCapacity;
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
