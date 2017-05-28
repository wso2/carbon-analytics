/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.core.conf;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Configuration class for data-bridge-config.yaml file.
 */
@Configuration(namespace = "databridge.config", description = "Configuration used for the databridge communication")
public class DataBridgeConfiguration {

    @Element(description = "No of worker threads to consume events", required = true)
    private int workerThreads = 10;

    @Element(description = "Maximum amount of messages that can be queued internally in MB", required = true)
    private int maxEventBufferCapacity = 10;

    @Element(description = "Queue size; the maximum number of events that can be stored in the queue", required = true)
    private int eventBufferSize = 2000;

    @Element(description = "Session timeout value in mins", required = true)
    private int clientTimeoutMin = 30;

    @Element(description = "Keystore file path", required = true)
    private String keyStoreLocation = null;

    @Element(description = "Keystore password", required = true)
    private String keyStorePassword = null;


    @Element(description = "Data receiver configurations", required = true)
    public List<DataReceiver> dataReceivers = new ArrayList<>();

    public DataReceiverConfiguration getDataReceiver(String name) {
        DataReceiverConfiguration dataReceiverConfiguration = null;
        for (DataReceiver dataReceiver : dataReceivers) {
            if (dataReceiver.getDataReceiver().getType().equalsIgnoreCase(name)) {
                dataReceiverConfiguration = dataReceiver.getDataReceiver();
                break;
            }
        }

        return dataReceiverConfiguration;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public int getMaxEventBufferCapacity() {
        return maxEventBufferCapacity;
    }

    public int getEventBufferSize() {
        return eventBufferSize;
    }

    public int getClientTimeoutMin() {
        return clientTimeoutMin;
    }

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public List<DataReceiver> getDataReceivers() {
        return dataReceivers;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public void setMaxEventBufferCapacity(int maxEventBufferCapacity) {
        this.maxEventBufferCapacity = maxEventBufferCapacity;
    }

    public void setEventBufferSize(int eventBufferSize) {
        this.eventBufferSize = eventBufferSize;
    }

    public void setClientTimeoutMin(int clientTimeoutMin) {
        this.clientTimeoutMin = clientTimeoutMin;
    }

    public void setKeyStoreLocation(String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public void setDataReceivers(List<DataReceiver> dataReceivers) {
        this.dataReceivers = dataReceivers;
    }

    @Override
    public String toString() {
        return "workerThreads : " + workerThreads + ", maxEventBufferCapacity : " + maxEventBufferCapacity + ", " +
               "childConfiguration - " + dataReceivers.toString();
    }

    public DataBridgeConfiguration() {
        LinkedHashMap<String, String> thriftPropertiesMap = new LinkedHashMap<>();
        thriftPropertiesMap.put("tcpPort", "7611");
        thriftPropertiesMap.put("sslPort", "7711");
        thriftPropertiesMap.put("sslEnabledProtocols", "TLSv1,TLSv1.1,TLSv1.2");
        thriftPropertiesMap.put("ciphers", "SSL_RSA_WITH_RC4_128_MD5,SSL_RSA_WITH_RC4_128_SHA,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,SSL_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA");

        LinkedHashMap<String, String> binaryPropertiesMap = new LinkedHashMap<>();
        thriftPropertiesMap.put("tcpPort", "9611");
        thriftPropertiesMap.put("sslPort", "9711");
        thriftPropertiesMap.put("tcpReceiverThreadPoolSize", "100");
        thriftPropertiesMap.put("sslReceiverThreadPoolSize", "100");
        thriftPropertiesMap.put("sslEnabledProtocols", "TLSv1,TLSv1.1,TLSv1.2");
        thriftPropertiesMap.put("ciphers", "SSL_RSA_WITH_RC4_128_MD5,SSL_RSA_WITH_RC4_128_SHA,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,SSL_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA");


        dataReceivers.add(new DataReceiver("Thrift", thriftPropertiesMap));
        dataReceivers.add(new DataReceiver("Binary", binaryPropertiesMap));
    }
}
