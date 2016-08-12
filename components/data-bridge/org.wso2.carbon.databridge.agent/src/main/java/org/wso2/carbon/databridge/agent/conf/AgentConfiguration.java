/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.databridge.agent.conf;

import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;

import javax.xml.bind.annotation.XmlElement;

/**
 * This class has the Agent's POJO representation of the XML data-agent-config.xml.
 */
public class AgentConfiguration {

    private String dataEndpointName;

    private String className;

    private String trustStore;

    private int corePoolSize;

    private int maxPoolSize;

    private int keepAliveTimeInPool;

    private String trustStorePassword;

    private int reconnectionInterval;

    private int queueSize;

    private int batchSize;

    private int maxTransportPoolSize;

    private int maxIdleConnections;

    private int minIdleTimeInPool;

    private int evictionTimePeriod;

    private int secureMaxTransportPoolSize;

    private int secureMaxIdleConnections;

    private int secureMinIdleTimeInPool;

    private int secureEvictionTimePeriod;

    private int socketTimeoutMS;

    private String publishingStrategy = DataEndpointConstants.ASYNC_STRATEGY;

    private String sslEnabledProtocols;

    private String ciphers;

    @XmlElement(name = "Name")
    public String getDataEndpointName() {
        return dataEndpointName;
    }

    @XmlElement(name = "DataEndpointClass")
    public String getClassName() {
        return className;
    }

    @XmlElement(name = "TrustSore")
    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        if (trustStore != null) {
            this.trustStore = trustStore.trim();
        } else {
            this.trustStore = null;
        }
    }

    @XmlElement(name = "TrustSorePassword")
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        if (trustStorePassword != null) {
            this.trustStorePassword = trustStorePassword.trim();
        } else {
            this.trustStorePassword = null;
        }
    }

    @XmlElement(name = "QueueSize")
    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    @XmlElement(name = "BatchSize")
    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @XmlElement(name = "ReconnectionInterval")
    public int getReconnectionInterval() {
        return reconnectionInterval;
    }

    public void setReconnectionInterval(int reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
    }

    @XmlElement(name = "MaxTransportPoolSize")
    public int getMaxTransportPoolSize() {
        return maxTransportPoolSize;
    }

    public void setMaxTransportPoolSize(int maxTransportPoolSize) {
        this.maxTransportPoolSize = maxTransportPoolSize;
    }

    @XmlElement(name = "MaxIdleConnections")
    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    @XmlElement(name = "MinIdleTimeInPool")
    public int getMinIdleTimeInPool() {
        return minIdleTimeInPool;
    }

    public void setMinIdleTimeInPool(int minIdleTimeInPool) {
        this.minIdleTimeInPool = minIdleTimeInPool;
    }

    @XmlElement(name = "EvictionTimePeriod")
    public int getEvictionTimePeriod() {
        return evictionTimePeriod;
    }

    public void setEvictionTimePeriod(int evictionTimePeriod) {
        this.evictionTimePeriod = evictionTimePeriod;
    }

    @XmlElement(name = "SecureMaxTransportPoolSize")
    public int getSecureMaxTransportPoolSize() {
        return secureMaxTransportPoolSize;
    }

    public void setSecureMaxTransportPoolSize(int secureMaxTransportPoolSize) {
        this.secureMaxTransportPoolSize = secureMaxTransportPoolSize;
    }

    @XmlElement(name = "SecureMaxIdleConnections")
    public int getSecureMinIdleTimeInPool() {
        return secureMinIdleTimeInPool;
    }

    public void setSecureMinIdleTimeInPool(int secureMinIdleTimeInPool) {
        this.secureMinIdleTimeInPool = secureMinIdleTimeInPool;
    }

    @XmlElement(name = "SecureEvictionTimePeriod")
    public int getSecureMaxIdleConnections() {
        return secureMaxIdleConnections;
    }

    public void setSecureMaxIdleConnections(int secureMaxIdleConnections) {
        this.secureMaxIdleConnections = secureMaxIdleConnections;
    }

    @XmlElement(name = "SecureMinIdleTimeInPool")
    public int getSecureEvictionTimePeriod() {
        return secureEvictionTimePeriod;
    }

    public void setSecureEvictionTimePeriod(int secureEvictionTimePeriod) {
        this.secureEvictionTimePeriod = secureEvictionTimePeriod;
    }

    @XmlElement(name = "CorePoolSize")
    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    @XmlElement(name = "MaxPoolSize")
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    @XmlElement(name = "KeepAliveTimeInPool")
    public int getKeepAliveTimeInPool() {
        return keepAliveTimeInPool;
    }

    public void setKeepAliveTimeInPool(int keepAliveTimeInPool) {
        this.keepAliveTimeInPool = keepAliveTimeInPool;
    }

    public void setDataEndpointName(String dataEndpointName) {
        if (dataEndpointName != null) {
            this.dataEndpointName = dataEndpointName.trim();
        } else {
            this.dataEndpointName = null;
        }
    }

    @XmlElement(name = "SocketTimeoutMS")
    public int getSocketTimeoutMS() {
        return socketTimeoutMS;
    }

    public void setSocketTimeoutMS(int socketTimeoutMS) {
        this.socketTimeoutMS = socketTimeoutMS;
    }

    public void setClassName(String className) {
        if (className != null) {

            this.className = className.trim();
        } else {
            this.className = null;
        }
    }

    @XmlElement(name = "PublishingStrategy")
    public String getPublishingStrategy() {
        return publishingStrategy;
    }

    public void setPublishingStrategy(String publishingStrategy) {
        this.publishingStrategy = publishingStrategy;
    }

    @XmlElement(name = "sslEnabledProtocols")
    public String getSslEnabledProtocols() {
        return sslEnabledProtocols;
    }

    public void setSslEnabledProtocols(String sslEnabledProtocols) {
        this.sslEnabledProtocols = sslEnabledProtocols;
    }

    @XmlElement(name = "ciphers")
    public String getCiphers() {
        return ciphers;
    }

    public void setCiphers(String ciphers) {
        this.ciphers = ciphers;
    }

    /**
     * Validates the configurations that valid.
     *
     * @throws DataEndpointAgentConfigurationException
     */
    public void validate() throws DataEndpointAgentConfigurationException {
        if (this.dataEndpointName == null || this.dataEndpointName.isEmpty()) {
            throw new DataEndpointAgentConfigurationException("Endpoint name is not set in "
                    + DataEndpointConstants.DATA_AGENT_CONF_FILE_NAME);
        }
        if (this.className == null || this.className.isEmpty()) {
            throw new DataEndpointAgentConfigurationException("Endpoint class name is not set in "
                    + DataEndpointConstants.DATA_AGENT_CONF_FILE_NAME + " for name: " + this.dataEndpointName);
        }
    }
}

