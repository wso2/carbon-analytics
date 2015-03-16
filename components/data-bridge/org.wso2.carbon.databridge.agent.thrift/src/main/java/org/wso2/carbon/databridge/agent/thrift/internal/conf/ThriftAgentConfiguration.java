/**
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.databridge.agent.thrift.internal.conf;

import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
public class ThriftAgentConfiguration {

    private int bufferedEventsSize;

    private int poolSize;

    private int maxPoolSize;

    private int maxTransportPoolSize;

    private int maxIdleConnections;

    private int evictionTimePeriod;

    private int minIdleTimeInPool;

    private int secureMaxTransportPoolSize;

    private int secureMaxIdleConnections;

    private int secureEvictionTimePeriod;

    private int secureMinIdleTimeInPool;

    private int maxMessageBundleSize;

    private int asyncDataPublisherBufferedEventSize;

    private int loadBalancingReconnectionInterval;

    public int getBufferedEventsSize() {
        return bufferedEventsSize;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setBufferedEventsSize(int bufferedEventsSize) {
        this.bufferedEventsSize = bufferedEventsSize;
    }

    public int getPoolSize() {
        return poolSize;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMaxTransportPoolSize() {
        return maxTransportPoolSize;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setMaxTransportPoolSize(int maxTransportPoolSize) {
        this.maxTransportPoolSize = maxTransportPoolSize;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public int getEvictionTimePeriod() {
        return evictionTimePeriod;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setEvictionTimePeriod(int evictionTimePeriod) {
        this.evictionTimePeriod = evictionTimePeriod;
    }

    public int getMinIdleTimeInPool() {
        return minIdleTimeInPool;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setMinIdleTimeInPool(int minIdleTimeInPool) {
        this.minIdleTimeInPool = minIdleTimeInPool;
    }

    public int getSecureMaxTransportPoolSize() {
        return secureMaxTransportPoolSize;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setSecureMaxTransportPoolSize(int secureMaxTransportPoolSize) {
        this.secureMaxTransportPoolSize = secureMaxTransportPoolSize;
    }

    public int getSecureMaxIdleConnections() {
        return secureMaxIdleConnections;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setSecureMaxIdleConnections(int secureMaxIdleConnections) {
        this.secureMaxIdleConnections = secureMaxIdleConnections;
    }

    public int getSecureEvictionTimePeriod() {
        return secureEvictionTimePeriod;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setSecureEvictionTimePeriod(int secureEvictionTimePeriod) {
        this.secureEvictionTimePeriod = secureEvictionTimePeriod;
    }

    public int getSecureMinIdleTimeInPool() {
        return secureMinIdleTimeInPool;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setSecureMinIdleTimeInPool(int secureMinIdleTimeInPool) {
        this.secureMinIdleTimeInPool = secureMinIdleTimeInPool;
    }

    public int getMaxMessageBundleSize() {
        return maxMessageBundleSize;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setMaxMessageBundleSize(int maxMessageBundleSize) {
        this.maxMessageBundleSize = maxMessageBundleSize;
    }

    public int getAsyncDataPublisherBufferedEventSize() {
        return asyncDataPublisherBufferedEventSize;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setAsyncDataPublisherBufferedEventSize(int asyncDataPublisherBufferedEventSize) {
        this.asyncDataPublisherBufferedEventSize = asyncDataPublisherBufferedEventSize;
    }

    public int getLoadBalancingReconnectionInterval() {
        return loadBalancingReconnectionInterval;
    }

    @XmlElement(namespace = AgentConstants.AGENT_CONF_NAMESPACE)
    public void setLoadBalancingReconnectionInterval(int loadBalancingReconnectionInterval) {
        this.loadBalancingReconnectionInterval = loadBalancingReconnectionInterval;
    }
}
