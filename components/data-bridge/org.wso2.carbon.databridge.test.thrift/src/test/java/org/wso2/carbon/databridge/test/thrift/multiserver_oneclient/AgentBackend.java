/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.test.thrift.multiserver_oneclient;

import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.thrift.internal.ThriftDataReceiver;
import org.wso2.carbon.databridge.test.thrift.KeyStoreUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;

/**
 * Server of single client multiple server test
 */
public class AgentBackend {
    ThriftDataReceiver thriftDataReceiver;
    static int NO_OF_EVENTS = 100000;
    static int NO_OF_SERVERS = 2;
    static int STABLE = 1000000;
    int offset = 0;

    public static void main(String[] args)
            throws DataBridgeException, InterruptedException {

        if (0 != args.length && args[0] != null) {
            NO_OF_EVENTS = Integer.parseInt(args[0]);
        }

        if (args.length != 0 && args[1] != null) {
            NO_OF_SERVERS = Integer.parseInt(args[1]);
        }

        System.out.println("Event no=" + NO_OF_EVENTS);
        System.out.println("Server no=" + NO_OF_SERVERS);

        KeyStoreUtil.setKeyStoreParams();

        for (int i = 0; i < NO_OF_SERVERS; i++) {
            AgentBackend server = new AgentBackend(i);
            server.start();
        }
    }

    public AgentBackend(int offset) {
        this.offset = offset;
    }

    public void start() throws DataBridgeException, InterruptedException {

        ThriftDataReceiverConfiguration thriftDataReceiverConfiguration = generateServerConf(offset);
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
        thriftDataReceiver = new ThriftDataReceiver(thriftDataReceiverConfiguration, databridge);
        databridge.subscribe(assignAgentCallback());
        thriftDataReceiver.start("localhost");
    }

    private AgentCallback assignAgentCallback() {

        return new AgentCallback() {
            long startTime = -1;
            int size = 0;
            private StreamDefinition streamDefinition;

            private synchronized void addCount(List<Event> eventList) {
                size += eventList.size();
            }

            @Override
            public void definedStream(StreamDefinition streamDefinition,
                                      int tenantId) {
                this.streamDefinition = streamDefinition;
            }

            @Override
            public void removeStream(StreamDefinition streamDefinition, int tenantId) {
                this.streamDefinition = null;
            }


            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                addCount(eventList);
                if (size <= STABLE && size > STABLE - 500) {
                    startTime = System.currentTimeMillis();
                }
                if (NO_OF_EVENTS <= (size - STABLE)) {
                    if (startTime != -1) {
                        System.out.println("Total time in ms=" + (System.currentTimeMillis() - startTime));
                    } else {
                        System.out.println("Start time not set ");
                    }
                }
            }
        };
    }

    private ThriftDataReceiverConfiguration generateServerConf(int offset) {
        return new ThriftDataReceiverConfiguration(7711 + offset, 7611 + offset);
    }

    public void stop() {
        thriftDataReceiver.stop();
    }
}
