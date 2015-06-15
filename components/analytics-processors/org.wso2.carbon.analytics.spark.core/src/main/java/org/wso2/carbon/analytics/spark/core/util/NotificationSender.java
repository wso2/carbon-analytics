/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.spark.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.net.MalformedURLException;

public class NotificationSender {
    private static final Log log = LogFactory.getLog(NotificationSender.class);

    private String serverUrl;
    private String username;
    private String password;
    private String streamId;

    private DataPublisher dataPublisher;

    public NotificationSender(String serverUrl, String username, String password, String streamId) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.streamId = streamId;
    }

    public void send(Event event) {

    }

    private void initPublisher() throws MalformedURLException, AgentException,
            AuthenticationException, TransportException {
        System.setProperty("javax.net.ssl.trustStore", "./src/main/resources/truststore/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        AgentConfiguration agentConfiguration = new AgentConfiguration();
        agentConfiguration.setBufferedEventsSize(50000);
        Agent agent = new Agent(agentConfiguration);
        dataPublisher = new DataPublisher("tcp://" + serverUrl, username, password, agent);

    }



}
