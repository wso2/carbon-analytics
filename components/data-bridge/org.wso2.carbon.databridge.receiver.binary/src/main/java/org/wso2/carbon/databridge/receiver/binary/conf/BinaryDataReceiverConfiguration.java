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
package org.wso2.carbon.databridge.receiver.binary.conf;

import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.conf.DataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.binary.BinaryDataReceiverConstants;
import org.wso2.carbon.databridge.receiver.binary.internal.BinaryDataReceiverServiceComponent;

/**
 * The receiver configuration for Binary Transport Receiver
 */
public class BinaryDataReceiverConfiguration {
    private int sslPort;
    private int tcpPort;
    private int sizeOfSSLThreadPool;
    private int sizeOfTCPThreadPool;
    private String sslProtocols;
    private String ciphers;

    public BinaryDataReceiverConfiguration(int sslPort, int tcpPort) {
        this.sslPort = sslPort;
        this.tcpPort = tcpPort;
        this.sizeOfSSLThreadPool = BinaryDataReceiverConstants.DEFAULT_SSL_RECEIVER_THREAD_POOL_SIZE;
        this.sizeOfTCPThreadPool = BinaryDataReceiverConstants.DEFAULT_TCP_RECEIVER_THREAD_POOL_SIZE;
    }

    public BinaryDataReceiverConfiguration(DataBridgeConfiguration dataBridgeConfiguration) {

        DataReceiverConfiguration dataReceiverConfiguration = dataBridgeConfiguration.getDataReceiver(BinaryDataReceiverConstants.DATA_BRIDGE_RECEIVER_CONFIG_NAME);
        String sslPortConfiguration = dataReceiverConfiguration.getProperties().get(
                BinaryDataReceiverConstants.SSL_RECEIVER_PORT_CONFIG_NAME);
        String tcpPortConfiguration = dataReceiverConfiguration.getProperties().get(
                BinaryDataReceiverConstants.TCP_RECEIVER_PORT_CONFIG_NAME);
        String sslThreadPoolSize = dataReceiverConfiguration.getProperties().get(
                BinaryDataReceiverConstants.SSL_RECEIVER_THREAD_POOL_SIZE);
        String tcpThreadPoolSize = dataReceiverConfiguration.getProperties().get(
                BinaryDataReceiverConstants.TCP_RECEIVER_THREAD_POOL_SIZE);
        String sslProtocols = dataReceiverConfiguration.getProperties().get(
                BinaryDataReceiverConstants.SSL_RECEIVER_PROTOCOLS_CONFIG_NAME);
        String ciphers = dataReceiverConfiguration.getProperties().get(
                BinaryDataReceiverConstants.SSL_RECEIVER_CIPHERS_CONFIG_NAME);


        if (sslPortConfiguration != null && !sslPortConfiguration.trim().isEmpty()) {
            this.sslPort = Integer.parseInt(sslPortConfiguration.trim()) + getPortOffset();
        } else {
            this.sslPort = BinaryDataReceiverConstants.DEFAULT_SSL_RECEIVER_PORT + getPortOffset();
        }

        if (tcpPortConfiguration != null && !tcpPortConfiguration.trim().isEmpty()) {
            this.tcpPort = Integer.parseInt(tcpPortConfiguration.trim()) + getPortOffset();
        } else {
            this.tcpPort = BinaryDataReceiverConstants.DEFAULT_TCP_RECEIVER_PORT + getPortOffset();
        }

        if (sslThreadPoolSize != null && !sslThreadPoolSize.trim().isEmpty()) {
            this.sizeOfSSLThreadPool = Integer.parseInt(sslThreadPoolSize.trim());
        } else {
            this.sizeOfSSLThreadPool = BinaryDataReceiverConstants.DEFAULT_SSL_RECEIVER_THREAD_POOL_SIZE;
        }

        if (tcpThreadPoolSize != null && !tcpThreadPoolSize.trim().isEmpty()) {
            this.sizeOfTCPThreadPool = Integer.parseInt(tcpThreadPoolSize.trim());
        } else {
            this.sizeOfTCPThreadPool = BinaryDataReceiverConstants.DEFAULT_TCP_RECEIVER_THREAD_POOL_SIZE;
        }

        if (sslProtocols != null && !sslProtocols.trim().isEmpty()) {
            this.sslProtocols = sslProtocols;
        }

        if (ciphers != null && !ciphers.trim().isEmpty()) {
            this.ciphers = ciphers;
        }

    }

    private static int getPortOffset() {
        return BinaryDataReceiverServiceComponent.getCarbonRuntime().getConfiguration().getPortsConfig().
                getOffset() + 1;
    }

    public int getSSLPort() {
        return sslPort;
    }

    public int getTCPPort() {
        return tcpPort;
    }

    public int getSizeOfTCPThreadPool() {
        return sizeOfTCPThreadPool;
    }

    public int getSizeOfSSLThreadPool() {
        return sizeOfSSLThreadPool;
    }

    public String getSslProtocols() {
        return sslProtocols;
    }

    public String getCiphers() {
        return ciphers;
    }
}
