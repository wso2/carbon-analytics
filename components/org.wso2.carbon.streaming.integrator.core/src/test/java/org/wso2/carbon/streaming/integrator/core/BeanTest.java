/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.streaming.integrator.core;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.streaming.integrator.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.streaming.integrator.core.internal.beans.EventSyncClientPoolConfig;
import org.wso2.carbon.streaming.integrator.core.internal.beans.EventSyncServerConfig;

public class BeanTest {

    @Test
    public void testBeanFunctionality() {
        EventSyncServerConfig eventSyncServerConfig = new EventSyncServerConfig();
        eventSyncServerConfig.setHost("localhost");
        eventSyncServerConfig.setPort(9893);
        eventSyncServerConfig.setAdvertisedHost("localhost");
        eventSyncServerConfig.setAdvertisedPort(9893);
        eventSyncServerConfig.setBossThreads(15);
        eventSyncServerConfig.setWorkerThreads(15);
        EventSyncClientPoolConfig eventSyncClientPoolConfig = new EventSyncClientPoolConfig();
        eventSyncClientPoolConfig.setMaxActive(12);
        eventSyncClientPoolConfig.setMaxTotal(12);
        eventSyncClientPoolConfig.setMaxIdle(13);
        eventSyncClientPoolConfig.setMaxWait(15);
        eventSyncClientPoolConfig.setMinEvictableIdleTimeMillis(60000);
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setType("ha");
        deploymentConfig.setEventSyncServer(eventSyncServerConfig);
        deploymentConfig.setTcpClientPoolConfig(eventSyncClientPoolConfig);
        deploymentConfig.setEventByteBufferQueueCapacity(50000);
        deploymentConfig.setByteBufferExtractorThreadPoolSize(15);
        deploymentConfig.setPassiveNodeDetailsWaitTimeOutMillis(450000);
        deploymentConfig.setPassiveNodeDetailsRetrySleepTimeMillis(1000);

        Assert.assertEquals(deploymentConfig.getType(), "ha");
        Assert.assertEquals(deploymentConfig.getEventByteBufferQueueCapacity(), 50000);
        Assert.assertEquals(deploymentConfig.getByteBufferExtractorThreadPoolSize(), 15);
        Assert.assertEquals(deploymentConfig.eventSyncServerConfigs().getBossThreads(), 15);
        Assert.assertEquals(deploymentConfig.eventSyncServerConfigs().getWorkerThreads(), 15);
        Assert.assertEquals(deploymentConfig.eventSyncServerConfigs().getHost(), "localhost");
        Assert.assertEquals(deploymentConfig.eventSyncServerConfigs().getPort(), 9893);
        Assert.assertEquals(deploymentConfig.eventSyncServerConfigs().getAdvertisedPort(), 9893);
        Assert.assertEquals(deploymentConfig.eventSyncServerConfigs().getAdvertisedHost(), "localhost");
        Assert.assertEquals(deploymentConfig.getTcpClientPoolConfig().getMaxActive(), 12);
        Assert.assertEquals(deploymentConfig.getTcpClientPoolConfig().getMaxTotal(), 12);
        Assert.assertEquals(deploymentConfig.getTcpClientPoolConfig().getMaxIdle(), 13);
        Assert.assertEquals(deploymentConfig.getTcpClientPoolConfig().getMaxWait(), 15);
        Assert.assertEquals(deploymentConfig.getTcpClientPoolConfig().getMinEvictableIdleTimeMillis(), 60000);
        Assert.assertEquals(deploymentConfig.getPassiveNodeDetailsWaitTimeOutMillis(), 450000);
        Assert.assertEquals(deploymentConfig.getPassiveNodeDetailsRetrySleepTimeMillis(), 1000);
    }

    @Test
    public void testBeanDefaultValueFunctionality() {
        EventSyncServerConfig eventSyncServerConfig = new EventSyncServerConfig();
        eventSyncServerConfig.setHost("localhost");
        eventSyncServerConfig.setPort(9893);
        EventSyncClientPoolConfig eventSyncClientPoolConfig = new EventSyncClientPoolConfig();
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setType("ha");
        deploymentConfig.setEventSyncServer(eventSyncServerConfig);
        deploymentConfig.setTcpClientPoolConfig(eventSyncClientPoolConfig);

        Assert.assertEquals(deploymentConfig.getType(), "ha");
        Assert.assertEquals(deploymentConfig.getEventByteBufferQueueCapacity(), 20000);
        Assert.assertEquals(deploymentConfig.getByteBufferExtractorThreadPoolSize(), 5);
        Assert.assertEquals(deploymentConfig.eventSyncServerConfigs().getBossThreads(), 10);
        Assert.assertEquals(deploymentConfig.eventSyncServerConfigs().getWorkerThreads(), 10);
        Assert.assertEquals(deploymentConfig.eventSyncServerConfigs().getHost(), "localhost");
        Assert.assertEquals(deploymentConfig.eventSyncServerConfigs().getPort(), 9893);
        Assert.assertEquals(deploymentConfig.getTcpClientPoolConfig().getMaxActive(), 10);
        Assert.assertEquals(deploymentConfig.getTcpClientPoolConfig().getMaxTotal(), 10);
        Assert.assertEquals(deploymentConfig.getTcpClientPoolConfig().getMaxIdle(), 10);
        Assert.assertEquals(deploymentConfig.getTcpClientPoolConfig().getMaxWait(), 60000);
        Assert.assertEquals(deploymentConfig.getTcpClientPoolConfig().getMinEvictableIdleTimeMillis(), 120000);
        Assert.assertEquals(deploymentConfig.getPassiveNodeDetailsWaitTimeOutMillis(), 300000);
        Assert.assertEquals(deploymentConfig.getPassiveNodeDetailsRetrySleepTimeMillis(), 500);
    }
}
