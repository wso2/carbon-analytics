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
import org.wso2.carbon.stream.processor.core.internal.util.TCPServerConstants;

/**
 * Bean class for the TCP Server Configurations.
 */
@Configuration(description = "TCP Server Configurations")
public class TCPServerConfig {

    private String host = "0.0.0.0";
    private int port = TCPServerConstants.DEFAULT_PORT;
    private int bossThreads = TCPServerConstants.DEFAULT_BOSS_THREADS;
    private int workerThreads = TCPServerConstants.DEFAULT_WORKER_THREADS;
    private boolean tcpNoDelay = TCPServerConstants.DEFAULT_TCP_NO_DELAY;
    private boolean keepAlive = TCPServerConstants.DEFAULT_KEEP_ALIVE;

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

    public boolean isTcpNoDelay() {

        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {

        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isKeepAlive() {

        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {

        this.keepAlive = keepAlive;
    }
}
