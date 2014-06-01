/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.agent.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.internal.pool.client.general.ClientPool;
import org.wso2.carbon.databridge.agent.thrift.internal.pool.client.general.ClientPoolFactory;
import org.wso2.carbon.databridge.agent.thrift.internal.pool.client.secure.SecureClientPool;
import org.wso2.carbon.databridge.agent.thrift.internal.pool.client.secure.SecureClientPoolFactory;
import org.wso2.carbon.databridge.agent.thrift.internal.publisher.authenticator.AgentAuthenticator;
import org.wso2.carbon.databridge.agent.thrift.internal.publisher.authenticator.AgentAuthenticatorFactory;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;
import org.wso2.carbon.databridge.commons.utils.DataBridgeThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Agent who connects to the AgentServer and Sends Events
 * There has to be one Agent server in a JVM since else it will use all resources of the OS
 */
public class Agent {

    private static Log log = LogFactory.getLog(Agent.class);

    private AgentConfiguration agentConfiguration;
    private GenericKeyedObjectPool transportPool;
    private Semaphore queueSemaphore;
    private AgentAuthenticator agentAuthenticator;
    private List<DataPublisher> dataPublisherList;
    private ThreadPoolExecutor threadPool;
    private GenericKeyedObjectPool secureTransportPool;

    public Agent() {
        this(new AgentConfiguration());
    }

    public Agent(AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
        this.transportPool = new ClientPool().getClientPool(
                new ClientPoolFactory(), agentConfiguration.getMaxTransportPoolSize(),
                agentConfiguration.getMaxIdleConnections(), true, agentConfiguration.getEvictionTimePeriod(),
                agentConfiguration.getMinIdleTimeInPool());
        this.secureTransportPool = new SecureClientPool().getClientPool(
                new SecureClientPoolFactory(agentConfiguration.getTrustStore(), agentConfiguration.getTrustStorePassword()), agentConfiguration.getSecureMaxTransportPoolSize(),
                agentConfiguration.getSecureMaxIdleConnections(), true, agentConfiguration.getSecureEvictionTimePeriod(),
                agentConfiguration.getSecureMinIdleTimeInPool());
        this.agentAuthenticator = AgentAuthenticatorFactory.getAgentAuthenticator(secureTransportPool);
        this.dataPublisherList = new LinkedList<DataPublisher>();
        this.queueSemaphore = new Semaphore(agentConfiguration.getBufferedEventsSize());
        //for the unbounded queue implementation the maximum pool size irrelevant and
        // only the CorePoolSize number of threads will be created
        this.threadPool = new ThreadPoolExecutor(agentConfiguration.getPoolSize(),
                                                 agentConfiguration.getMaxPoolSize(),
                                                 AgentConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                                                 new LinkedBlockingQueue<Runnable>(),
                                                 new DataBridgeThreadFactory("Agent")
        );
    }

    void addDataPublisher(DataPublisher dataPublisher) {
        dataPublisherList.add(dataPublisher);
    }

    void removeDataPublisher(DataPublisher dataPublisher) {
        dataPublisherList.remove(dataPublisher);
    }

    /**
     * To shutdown Agent and DataPublishers
     */
    synchronized void shutdown(DataPublisher dataPublisher) {
        removeDataPublisher(dataPublisher);
        if (dataPublisherList.size() == 0) {
            shutdown();
        }
    }

    /**
     * To shutdown Agent
     */
    public void shutdown() {
        try {
            while (threadPool.getActiveCount() > 0) {
                Thread.sleep(500);
            }
            threadPool.shutdown();
            transportPool.close();
        } catch (Exception e) {
            log.warn("Agent shutdown failed");
        }
        AgentHolder.setAgent(null);
    }

    /**
     * To shutdown Agent and DataPublishers immediately
     */
    synchronized void shutdownNow(DataPublisher dataPublisher) {
        removeDataPublisher(dataPublisher);
        if (dataPublisherList.size() == 0) {
            shutdownNow();
        }
    }

    /**
     * To shutdown Agent immediately
     */
    public void shutdownNow() {
        try {
            threadPool.shutdown();
            transportPool.close();
        } catch (Exception e) {
            log.warn("Agent forceful shutdown failed",e);
        }
        AgentHolder.setAgent(null);
    }


    public AgentConfiguration getAgentConfiguration() {
        return agentConfiguration;
    }

    public GenericKeyedObjectPool getTransportPool() {
        return transportPool;
    }

    public Semaphore getQueueSemaphore() {
        return queueSemaphore;
    }

    public AgentAuthenticator getAgentAuthenticator() {
        return agentAuthenticator;
    }

    List<DataPublisher> getDataPublisherList() {
        return dataPublisherList;
    }

    public ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    public GenericKeyedObjectPool getSecureTransportPool() {
        return secureTransportPool;
    }
}
