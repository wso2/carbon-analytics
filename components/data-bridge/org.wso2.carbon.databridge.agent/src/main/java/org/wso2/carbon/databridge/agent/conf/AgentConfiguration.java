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
 * This class has the Agent's POJO representation of the XML data-agent-conf.xml.
 */
public class AgentConfiguration {

    private String dataEndpointName;

    private String className;

    private String trustStore;

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
        this.trustStore = trustStore.trim();
    }

    @XmlElement(name = "TrustSorePassword")
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword.trim();
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

    public void setDataEndpointName(String dataEndpointName) {
        this.dataEndpointName = dataEndpointName.trim();
    }

    public void setClassName(String className) {
        this.className = className.trim();
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

