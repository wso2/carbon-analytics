/**
 *
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.test.thrift;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.internal.ThriftDataReceiver;
import org.wso2.carbon.user.api.UserStoreException;

import java.net.SocketException;
import java.util.List;

public class TestServer extends TestCase {
    Logger log = Logger.getLogger(TestServer.class);
    ThriftDataReceiver thriftDataReceiver;

    public void testServerTest() throws DataBridgeException, InterruptedException {
        TestServer testServer = new TestServer();
        testServer.start(7661);
        Thread.sleep(2000);
        testServer.stop();
    }

    public void start(int receiverPort) throws DataBridgeException {
        KeyStoreUtil.setKeyStoreParams();
        DataBridge databridge = new DataBridge(new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName,
                                        String password) {
                return true;// allays authenticate to true

            }

            @Override
            public String getTenantDomain(String userName) {
                return "admin";
            }
            @Override
            public int getTenantId(String tenantDomain) throws UserStoreException {
                return -1234;
            }

            @Override
            public void initContext(AgentSession agentSession) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void destroyContext(AgentSession agentSession) {

            }
        }, new InMemoryStreamDefinitionStore());

        thriftDataReceiver = new ThriftDataReceiver(receiverPort, databridge);

        databridge.subscribe(new AgentCallback() {
            int totalSize = 0;

            public void definedStream(StreamDefinition streamDefinition,
                                      int tenantId) {
                log.info("StreamDefinition " + streamDefinition);
            }

            @Override
            public void removeStream(StreamDefinition streamDefinition, int tenantId) {
                log.info("StreamDefinition remove " + streamDefinition);
            }

            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                log.info("eventListSize=" + eventList.size() + " eventList " + eventList + " for username " + credentials.getUsername());
            }

        });

        try {
            String address = HostAddressFinder.findAddress("localhost");
            log.info("Test Server starting on " + address);
            thriftDataReceiver.start(address);
            log.info("Test Server Started");
        } catch (SocketException e) {
            log.error("Test Server not started !", e);
        }
    }

    public void stop() {
        thriftDataReceiver.stop();
        log.info("Test Server Stopped");
    }
}
