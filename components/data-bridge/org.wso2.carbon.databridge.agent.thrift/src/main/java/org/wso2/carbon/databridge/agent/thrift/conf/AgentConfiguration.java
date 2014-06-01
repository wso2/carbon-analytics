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


package org.wso2.carbon.databridge.agent.thrift.conf;

import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;

/**
 * Configuration details of Agent
 */
public class AgentConfiguration {

    private int bufferedEventsSize = AgentConstants.DEFAULT_BUFFERED_EVENTS_SIZE;
    private int poolSize = AgentConstants.DEFAULT_POOL_SIZE;
    private int maxPoolSize = AgentConstants.MAX_DEFAULT_POOL_SIZE;
    private int asyncDataPublisherBufferedEventSize = AgentConstants.DEFAULT_ASYNC_CLIENT_BUFFERED_EVENTS_SIZE;
    private int loadBalancingDataPublisherBufferedEventSize = AgentConstants.DEFAULT_LB_CLIENT_BUFFERED_EVENTS_SIZE;

    private int maxTransportPoolSize = AgentConstants.DEFAULT_MAX_TRANSPORT_POOL_SIZE;
    private int maxIdleConnections = AgentConstants.DEFAULT_MAX_IDLE_CONNECTIONS;
    private long evictionTimePeriod = AgentConstants.DEFAULT_EVICTION_IDLE_TIME_IN_POOL;
    private long minIdleTimeInPool = AgentConstants.DEFAULT_MIN_IDLE_TIME_IN_POOL;

    private int maxMessageBundleSize = AgentConstants.DEFAULT_MAX_MESSAGE_BUNDLE_SIZE;

    private int secureMaxTransportPoolSize = AgentConstants.DEFAULT_SECURE_MAX_TRANSPORT_POOL_SIZE;
    private int secureMaxIdleConnections = AgentConstants.DEFAULT_SECURE_MAX_IDLE_CONNECTIONS;
    private long secureEvictionTimePeriod = AgentConstants.DEFAULT_SECURE_EVICTION_IDLE_TIME_IN_POOL;
    private long secureMinIdleTimeInPool = AgentConstants.DEFAULT_SECURE_MIN_IDLE_TIME_IN_POOL;
    private long reconnectionInterval = AgentConstants.DEFAULT_RECONNECTION_INTERVAL;

    private String trustStore = null;
    private String trustStorePassword = null;

    public void setSecureMaxTransportPoolSize(int secureMaxTransportPoolSize) {
        this.secureMaxTransportPoolSize = secureMaxTransportPoolSize;
    }

    public void setSecureMaxIdleConnections(int secureMaxIdleConnections) {
        this.secureMaxIdleConnections = secureMaxIdleConnections;
    }

    public void setSecureEvictionTimePeriod(long secureEvictionTimePeriod) {
        this.secureEvictionTimePeriod = secureEvictionTimePeriod;
    }

    public void setSecureMinIdleTimeInPool(long secureMinIdleTimeInPool) {
        this.secureMinIdleTimeInPool = secureMinIdleTimeInPool;
    }

    public int getMaxMessageBundleSize() {
        return maxMessageBundleSize;
    }

    public void setMaxMessageBundleSize(int maxMessageBundleSize) {
        this.maxMessageBundleSize = maxMessageBundleSize;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public long getEvictionTimePeriod() {
        return evictionTimePeriod;
    }

    public void setEvictionTimePeriod(long evictionTimePeriod) {
        this.evictionTimePeriod = evictionTimePeriod;
    }

    public long getMinIdleTimeInPool() {
        return minIdleTimeInPool;
    }

    public void setMinIdleTimeInPool(long minIdleTimeInPool) {
        this.minIdleTimeInPool = minIdleTimeInPool;
    }

    public int getBufferedEventsSize() {
        return bufferedEventsSize;
    }

    public void setBufferedEventsSize(int bufferedEventsSize) {
        this.bufferedEventsSize = bufferedEventsSize;
    }

    public int getMaxTransportPoolSize() {
        return maxTransportPoolSize;
    }

    public void setMaxTransportPoolSize(int maxTransportPoolSize) {
        this.maxTransportPoolSize = maxTransportPoolSize;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public int getSecureMaxTransportPoolSize() {
        return secureMaxTransportPoolSize;
    }

    public int getSecureMaxIdleConnections() {
        return secureMaxIdleConnections;
    }

    public long getSecureEvictionTimePeriod() {
        return secureEvictionTimePeriod;
    }

    public long getSecureMinIdleTimeInPool() {
        return secureMinIdleTimeInPool;
    }

    public int getAsyncDataPublisherBufferedEventSize() {
        return asyncDataPublisherBufferedEventSize;
    }

    public void setAsyncDataPublisherBufferedEventSize(int asyncDataPublisherBufferedEventSize) {
        this.asyncDataPublisherBufferedEventSize = asyncDataPublisherBufferedEventSize;
    }

    public long getReconnectionInterval() {
        return reconnectionInterval;
    }

    public void setReconnectionInterval(long reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
    }

    public int getLoadBalancingDataPublisherBufferedEventSize() {
        return loadBalancingDataPublisherBufferedEventSize;
    }

    public void setLoadBalancingDataPublisherBufferedEventSize(int loadBalancingDataPublisherBufferedEventSize) {
        this.loadBalancingDataPublisherBufferedEventSize = loadBalancingDataPublisherBufferedEventSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
}
