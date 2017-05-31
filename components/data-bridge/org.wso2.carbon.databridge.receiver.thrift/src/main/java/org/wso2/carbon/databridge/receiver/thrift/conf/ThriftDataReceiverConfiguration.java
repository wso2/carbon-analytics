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
package org.wso2.carbon.databridge.receiver.thrift.conf;

import org.wso2.carbon.databridge.commons.thrift.utils.CommonThriftConstants;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.conf.DataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.thrift.internal.utils.ThriftDataReceiverConstants;

/**
 * configuration details related to DataReceiver
 */
public class ThriftDataReceiverConfiguration {
    private int secureDataReceiverPort;
    private int dataReceiverPort;
    private String sslProtocols;
    private String ciphers;
    private String receiverHostName;

    public ThriftDataReceiverConfiguration(int defaultSslPort, int defaultPort) {
        secureDataReceiverPort = defaultSslPort;
        dataReceiverPort = defaultPort;
    }

    public ThriftDataReceiverConfiguration(DataBridgeConfiguration dataBridgeConfiguration, int portOffset) {
        DataReceiverConfiguration dataReceiverConfiguration = dataBridgeConfiguration.getDataReceiver
                (ThriftDataReceiverConstants.DATA_BRIDGE_RECEIVER_NAME);

        String sslPortConfiguration = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.SECURE_PORT_ELEMENT);
        String tcpPortConfiguration = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.PORT_ELEMENT);
        String receiverHostName = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.RECEIVER_HOST_NAME);
        String sslProtocols = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.PROTOCOLS_ELEMENT);
        String ciphers = dataReceiverConfiguration.getProperties().get(ThriftDataReceiverConstants.CIPHERS_ELEMENT);

        if (sslPortConfiguration != null && !sslPortConfiguration.trim().isEmpty()) {
            this.secureDataReceiverPort = Integer.parseInt(sslPortConfiguration.trim()) + portOffset;
        } else {
            this.secureDataReceiverPort = CommonThriftConstants.DEFAULT_RECEIVER_PORT +
                                          CommonThriftConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET + portOffset;
        }

        if (tcpPortConfiguration != null && !tcpPortConfiguration.trim().isEmpty()) {
            this.dataReceiverPort = Integer.parseInt(tcpPortConfiguration) + portOffset;
        } else {
            this.dataReceiverPort = CommonThriftConstants.DEFAULT_RECEIVER_PORT + portOffset;
        }

        if (receiverHostName != null && !receiverHostName.trim().isEmpty()) {
            this.receiverHostName = receiverHostName;
        } else {
            this.receiverHostName = ThriftDataReceiverConstants.DEFAULT_HOSTNAME;
        }

        if (sslProtocols != null && !sslProtocols.trim().isEmpty()) {
            this.sslProtocols = sslProtocols;
        }

        if (ciphers != null && !ciphers.trim().isEmpty()) {
            this.ciphers = ciphers;
        }
    }

    public ThriftDataReceiverConfiguration(int defaultSslPort, int defaultPort,
                                           String confHostName) {
        secureDataReceiverPort = defaultSslPort;
        dataReceiverPort = defaultPort;
        receiverHostName = confHostName;
    }

    public int getDataReceiverPort() {
        return dataReceiverPort;
    }

    public void setDataReceiverPort(int dataReceiverPort) {
        this.dataReceiverPort = dataReceiverPort;
    }

    public int getSecureDataReceiverPort() {
        return secureDataReceiverPort;
    }

    public void setSecureDataReceiverPort(int secureDataReceiverPort) {
        this.secureDataReceiverPort = secureDataReceiverPort;
    }

    public String getReceiverHostName() {
        return receiverHostName;
    }

    public void setReceiverHostName(String receiverHostName) {
        this.receiverHostName = receiverHostName;
    }

    public String getSslProtocols() {
        return sslProtocols;
    }

    public void setSslProtocols(String sslProtocols) {
        this.sslProtocols = sslProtocols;
    }

    public String getCiphers() {
        return ciphers;
    }

    public void setCiphers(String ciphers) {
        this.ciphers = ciphers;
    }
}
