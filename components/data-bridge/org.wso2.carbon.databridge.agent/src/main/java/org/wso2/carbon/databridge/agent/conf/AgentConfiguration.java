/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Data agent configuration
 */
@Configuration(description = "Data agent configuration")
public class AgentConfiguration {

    @Element(description = "Data agent name", required = true)
    private String name = "";

    @Element(description = "Data endpoint class", required = true)
    private String dataEndpointClass = "";

    @Element(description = "Data publisher strategy", required = false)
    private String publishingStrategy ="async";

    @Element(description = "Trust store path", required = false)
    private String trustStorePath = "${carbon.home}/resources/security/client-truststore.jks";

    @Element(description = "Trust store password", required = false)
    private String trustStorePassword = "wso2carbon";

    @Element(description = "Queue Size", required = false)
    private int queueSize = 32768;

    @Element(description = "Batch Size", required = false)
    private int batchSize = 200;

    @Element(description = "Core pool size", required = false)
    private int corePoolSize = 1;

    @Element(description = "Socket timeout in milliseconds", required = false)
    private int socketTimeoutMS = 30000;

    @Element(description = "Maximum pool size", required = false)
    private int maxPoolSize = 1;

    @Element(description = "Keep alive time in pool", required = false)
    private int keepAliveTimeInPool = 20;

    @Element(description = "Reconnection interval", required = false)
    private int reconnectionInterval = 30;

    @Element(description = "Max transport pool size", required = false)
    private int maxTransportPoolSize = 250;

    @Element(description = "Max idle connections", required = false)
    private int maxIdleConnections = 250;

    @Element(description = "Eviction time interval", required = false)
    private int evictionTimePeriod = 5500;

    @Element(description = "Min idle time in pool", required = false)
    private int minIdleTimeInPool = 5000;

    @Element(description = "Secure max transport pool size", required = false)
    private int secureMaxTransportPoolSize = 250;

    @Element(description = "Secure max idle connections", required = false)
    private int secureMaxIdleConnections = 250;

    @Element(description = "secure eviction time period", required = false)
    private int secureEvictionTimePeriod = 5500;

    @Element(description = "Secure min idle time in pool", required = false)
    private int secureMinIdleTimeInPool = 5000;

    @Element(description = "SSL enabled protocols", required = false)
    private String sslEnabledProtocols = "TLSv1,TLSv1.1,TLSv1.2";

    @Element(description = "Ciphers", required = false)
    private String ciphers = "SSL_RSA_WITH_RC4_128_MD5,SSL_RSA_WITH_RC4_128_SHA,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,SSL_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA";

    public String getName() {
        return name;
    }

    public String getDataEndpointClass() {
        return dataEndpointClass;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getSocketTimeoutMS() {
        return socketTimeoutMS;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getKeepAliveTimeInPool() {
        return keepAliveTimeInPool;
    }

    public int getReconnectionInterval() {
        return reconnectionInterval;
    }

    public int getMaxTransportPoolSize() {
        return maxTransportPoolSize;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public int getEvictionTimePeriod() {
        return evictionTimePeriod;
    }

    public int getMinIdleTimeInPool() {
        return minIdleTimeInPool;
    }

    public int getSecureMaxTransportPoolSize() {
        return secureMaxTransportPoolSize;
    }

    public int getSecureMaxIdleConnections() {
        return secureMaxIdleConnections;
    }

    public int getSecureEvictionTimePeriod() {
        return secureEvictionTimePeriod;
    }

    public int getSecureMinIdleTimeInPool() {
        return secureMinIdleTimeInPool;
    }

    public String getSslEnabledProtocols() {
        return sslEnabledProtocols;
    }

    public String getCiphers() {
        return ciphers;
    }

    public String getPublishingStrategy() {
        return publishingStrategy;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDataEndpointClass(String dataEndpointClass) {
        this.dataEndpointClass = dataEndpointClass;
    }

    public void setPublishingStrategy(String publishingStrategy) {
        this.publishingStrategy = publishingStrategy;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public void setSocketTimeoutMS(int socketTimeoutMS) {
        this.socketTimeoutMS = socketTimeoutMS;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void setKeepAliveTimeInPool(int keepAliveTimeInPool) {
        this.keepAliveTimeInPool = keepAliveTimeInPool;
    }

    public void setReconnectionInterval(int reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
    }

    public void setMaxTransportPoolSize(int maxTransportPoolSize) {
        this.maxTransportPoolSize = maxTransportPoolSize;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public void setEvictionTimePeriod(int evictionTimePeriod) {
        this.evictionTimePeriod = evictionTimePeriod;
    }

    public void setMinIdleTimeInPool(int minIdleTimeInPool) {
        this.minIdleTimeInPool = minIdleTimeInPool;
    }

    public void setSecureMaxTransportPoolSize(int secureMaxTransportPoolSize) {
        this.secureMaxTransportPoolSize = secureMaxTransportPoolSize;
    }

    public void setSecureMaxIdleConnections(int secureMaxIdleConnections) {
        this.secureMaxIdleConnections = secureMaxIdleConnections;
    }

    public void setSecureEvictionTimePeriod(int secureEvictionTimePeriod) {
        this.secureEvictionTimePeriod = secureEvictionTimePeriod;
    }

    public void setSecureMinIdleTimeInPool(int secureMinIdleTimeInPool) {
        this.secureMinIdleTimeInPool = secureMinIdleTimeInPool;
    }

    public void setSslEnabledProtocols(String sslEnabledProtocols) {
        this.sslEnabledProtocols = sslEnabledProtocols;
    }

    public void setCiphers(String ciphers) {
        this.ciphers = ciphers;
    }

    @Override
    public String toString() {
        return ", Name : " + name +
               "DataEndpointClass : " + dataEndpointClass +
               "PublishingStrategy : "+ publishingStrategy+
               "TrustSorePath" + trustStorePath +
               "TrustSorePassword" + trustStorePassword +
               "QueueSize" + queueSize +
               "BatchSize" + batchSize +
               "CorePoolSize" + corePoolSize +
               "SocketTimeoutMS" + socketTimeoutMS +
               "MaxPoolSize" + maxPoolSize +
               "KeepAliveTimeInPool" + keepAliveTimeInPool +
               "ReconnectionInterval" + reconnectionInterval +
               "MaxTransportPoolSize" + maxTransportPoolSize +
               "MaxIdleConnections" + maxIdleConnections +
               "EvictionTimePeriod" + evictionTimePeriod +
               "MinIdleTimeInPool" + minIdleTimeInPool +
               "SecureMaxTransportPoolSize" + secureMaxTransportPoolSize +
               "SecureMaxIdleConnections" + secureMaxIdleConnections +
               "SecureEvictionTimePeriod" + secureEvictionTimePeriod +
               "SecureMinIdleTimeInPool" + secureMinIdleTimeInPool +
               "SSLEnabledProtocols" + sslEnabledProtocols +
               "Ciphers" + ciphers;
    }

    public AgentConfiguration(String name, String dataEndpointClass) {
        this.name = name;
        this.dataEndpointClass = dataEndpointClass;
    }

    public AgentConfiguration() {
    }
}
