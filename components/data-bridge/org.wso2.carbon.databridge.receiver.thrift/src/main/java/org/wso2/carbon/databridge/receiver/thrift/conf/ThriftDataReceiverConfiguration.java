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

/**
 * configuration details related to DataReceiver
 */
public class ThriftDataReceiverConfiguration {
    private int secureDataReceiverPort = CommonThriftConstants.DEFAULT_RECEIVER_PORT + CommonThriftConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET;
    private int dataReceiverPort = CommonThriftConstants.DEFAULT_RECEIVER_PORT;
    private String receiverHostName;

    public ThriftDataReceiverConfiguration(int defaultSslPort, int defaultPort) {
        secureDataReceiverPort = defaultSslPort;
        dataReceiverPort = defaultPort;
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
}
