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

package org.wso2.carbon.streaming.integrator.core.internal.beans;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Bean class for the deployment configurations.
 */
@Configuration(namespace = "deployment.config", description = "Cluster Coordination Mode Configuration")
public class DeploymentConfig {

    private String type;
    @Element(description = "Wait time out for passive node details", required = false)
    private long passiveNodeDetailsWaitTimeOutMillis = 300000;
    @Element(description = "Sleep time before retrying for passive node details", required = false)
    private long passiveNodeDetailsRetrySleepTimeMillis = 500;
    private EventSyncServerConfig eventSyncServer;
    @Element(description = "Byte buffer queue capacity", required = false)
    private int eventByteBufferQueueCapacity = 20000;
    @Element(description = "Event sync client pool configurations", required = false)
    private EventSyncClientPoolConfig eventSyncClientPool = new EventSyncClientPoolConfig();
    @Element(description = "Pool of threads to retrieve bytes from byte buffer queue", required = false)
    private int byteBufferExtractorThreadPoolSize = 5;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EventSyncServerConfig eventSyncServerConfigs() {
        return eventSyncServer;
    }

    public void setEventSyncServer(EventSyncServerConfig eventSyncServer) {
        this.eventSyncServer = eventSyncServer;
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

    public int getByteBufferExtractorThreadPoolSize() {
        return byteBufferExtractorThreadPoolSize;
    }

    public void setByteBufferExtractorThreadPoolSize(int byteBufferExtractorThreadPoolSize) {
        this.byteBufferExtractorThreadPoolSize = byteBufferExtractorThreadPoolSize;
    }

    public long getPassiveNodeDetailsWaitTimeOutMillis() {

        return passiveNodeDetailsWaitTimeOutMillis;
    }

    public void setPassiveNodeDetailsWaitTimeOutMillis(long passiveNodeDetailsWaitTimeOutMillis) {

        this.passiveNodeDetailsWaitTimeOutMillis = passiveNodeDetailsWaitTimeOutMillis;
    }

    public long getPassiveNodeDetailsRetrySleepTimeMillis() {

        return passiveNodeDetailsRetrySleepTimeMillis;
    }

    public void setPassiveNodeDetailsRetrySleepTimeMillis(long passiveNodeDetailsRetrySleepTimeMillis) {

        this.passiveNodeDetailsRetrySleepTimeMillis = passiveNodeDetailsRetrySleepTimeMillis;
    }
}
