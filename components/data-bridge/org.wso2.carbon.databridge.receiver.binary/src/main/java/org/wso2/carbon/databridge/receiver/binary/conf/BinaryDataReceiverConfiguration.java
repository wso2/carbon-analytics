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
import org.wso2.carbon.databridge.core.conf.DataReceiver;
import org.wso2.carbon.databridge.receiver.binary.BinaryDataReceiverConstants;
import org.wso2.carbon.utils.CarbonUtils;

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
        DataReceiver dataReceiver = dataBridgeConfiguration.
                getDataReceiver(BinaryDataReceiverConstants.DATA_BRIDGE_RECEIVER_CONFIG_NAME);
        this.sslPort = Integer.parseInt(dataReceiver.getConfiguration(BinaryDataReceiverConstants.SSL_RECEIVER_PORT_CONFIG_NAME,
                BinaryDataReceiverConstants.DEFAULT_SSL_RECEIVER_PORT).toString())+getPortOffset();
        this.tcpPort = Integer.parseInt(dataReceiver.getConfiguration(BinaryDataReceiverConstants.TCP_RECEIVER_PORT_CONFIG_NAME,
                BinaryDataReceiverConstants.DEFAULT_TCP_RECEIVER_PORT).toString())+getPortOffset();
        this.sizeOfSSLThreadPool = Integer.parseInt(dataReceiver.getConfiguration(
                BinaryDataReceiverConstants.SSL_RECEIVER_THREAD_POOL_SIZE,
                BinaryDataReceiverConstants.DEFAULT_SSL_RECEIVER_THREAD_POOL_SIZE).toString());
        this.sizeOfTCPThreadPool = Integer.parseInt(dataReceiver.getConfiguration(
                BinaryDataReceiverConstants.TCP_RECEIVER_THREAD_POOL_SIZE,
                BinaryDataReceiverConstants.DEFAULT_TCP_RECEIVER_THREAD_POOL_SIZE).toString());

        Object sslProtocolObj = dataReceiver.getConfiguration(BinaryDataReceiverConstants.SSL_RECEIVER_PROTOCOLS_CONFIG_NAME, null);
        sslProtocols =  sslProtocolObj != null ? sslProtocolObj.toString() : null;
        Object ciphersObj = dataReceiver.getConfiguration(BinaryDataReceiverConstants.SSL_RECEIVER_CIPHERS_CONFIG_NAME, null);
        ciphers =  sslProtocolObj != null ? ciphersObj.toString() : null;
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

    private static int getPortOffset() {
        return CarbonUtils.getPortFromServerConfig(BinaryDataReceiverConstants.CARBON_CONFIG_PORT_OFFSET_NODE)+1;
    }

    public String getSslProtocols() {
        return sslProtocols;
    }

    public String getCiphers() {
        return ciphers;
    }
}
