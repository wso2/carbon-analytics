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
 * The configuration of DataPublisher
 */
public class DataPublisherConfiguration {

    private String sessionId;
    private String publisherKey;
    private ReceiverConfiguration receiverConfiguration;

    public DataPublisherConfiguration(ReceiverConfiguration receiverConfiguration) {
        this.receiverConfiguration = receiverConfiguration;
        publisherKey = receiverConfiguration.getDataReceiverProtocol().toString()+
                       AgentConstants.SEPARATOR +
                       receiverConfiguration.getDataReceiverIp() +
                       AgentConstants.HOSTNAME_AND_PORT_SEPARATOR +
                       receiverConfiguration.getDataReceiverPort() +
                       AgentConstants.SEPARATOR +
                       receiverConfiguration.getSecureDataReceiverProtocol().toString()+
                       AgentConstants.SEPARATOR +
                       receiverConfiguration.getSecureDataReceiverIp() +
                       AgentConstants.HOSTNAME_AND_PORT_SEPARATOR +
                       receiverConfiguration.getSecureDataReceiverPort();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPublisherKey() {
        return publisherKey;
    }

    public ReceiverConfiguration getReceiverConfiguration() {
        return receiverConfiguration;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
