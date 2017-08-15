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
package org.wso2.carbon.databridge.agent.test.binary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.databridge.agent.test.DataPublisherTestUtil;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.binary.internal.BinaryDataReceiver;
import org.wso2.carbon.databridge.receiver.binary.conf.BinaryDataReceiverConfiguration;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BinaryTestServer {
    Logger log = LoggerFactory.getLogger(BinaryTestServer.class);
    BinaryDataReceiver binaryDataReceiver;
    InMemoryStreamDefinitionStore streamDefinitionStore;
    AtomicInteger numberOfEventsReceived;
    RestarterThread restarterThread;

    public void startTestServer() throws DataBridgeException, InterruptedException, IOException {
        BinaryTestServer testServer = new BinaryTestServer();
        testServer.start(9611, 9711);
        Thread.sleep(100000000);
        testServer.stop();
    }

    public void addStreamDefinition(StreamDefinition streamDefinition)
            throws StreamDefinitionStoreException {
        streamDefinitionStore.saveStreamDefinitionToStore(streamDefinition);
    }

    public void addStreamDefinition(String streamDefinitionStr)
            throws StreamDefinitionStoreException, MalformedStreamDefinitionException {
        StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(streamDefinitionStr);
        getStreamDefinitionStore().saveStreamDefinitionToStore(streamDefinition);
    }

    private InMemoryStreamDefinitionStore getStreamDefinitionStore() {
        if (streamDefinitionStore == null) {
            streamDefinitionStore = new InMemoryStreamDefinitionStore();
        }
        return streamDefinitionStore;
    }

    public void start(int tcpPort, int securePort) throws DataBridgeException, IOException {
        DataPublisherTestUtil.setKeyStoreParams();
        streamDefinitionStore = getStreamDefinitionStore();
        numberOfEventsReceived = new AtomicInteger(0);
        DataBridge databridge = new DataBridge(new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName,
                                        String password) {
                return true;// allays authenticate to true
            }

            @Override
            public void initContext(AgentSession agentSession) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void destroyContext(AgentSession agentSession) {

            }
        }, streamDefinitionStore, DataPublisherTestUtil.getDataBridgeConfigPath());

        BinaryDataReceiverConfiguration dataReceiverConfiguration = new BinaryDataReceiverConfiguration(securePort, tcpPort);

        binaryDataReceiver = new BinaryDataReceiver(dataReceiverConfiguration, databridge);
        databridge.subscribe(new AgentCallback() {
            int totalSize = 0;

            public void definedStream(StreamDefinition streamDefinition) {
                log.info("StreamDefinition " + streamDefinition);
            }

            @Override
            public void removeStream(StreamDefinition streamDefinition) {
                log.info("StreamDefinition remove " + streamDefinition);
            }

            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                numberOfEventsReceived.addAndGet(eventList.size());
                log.info("Received events : " + numberOfEventsReceived);
//                log.info("eventListSize=" + eventList.size() + " eventList " + eventList + " for username " + credentials.getUsername());
            }

        });

        String address = "localhost";
        log.info("Test Server starting on " + address);
        binaryDataReceiver.start();
        log.info("Test Server Started");
    }

    public int getNumberOfEventsReceived() {
        if (numberOfEventsReceived != null) return numberOfEventsReceived.get();
        else return 0;
    }

    public void resetReceivedEvents() {
        numberOfEventsReceived.set(0);
    }

    public void stop() {
        binaryDataReceiver.stop();
        log.info("Test Server Stopped");
    }

    public void stopAndStartDuration(int port, int sslPort, long stopAfterTimeMilliSeconds, long startAfterTimeMS)
            throws SocketException, DataBridgeException {
        restarterThread = new RestarterThread(port, sslPort, stopAfterTimeMilliSeconds, startAfterTimeMS);
        Thread thread = new Thread(restarterThread);
        thread.start();
    }

    public int getEventsReceivedBeforeLastRestart() {
        return restarterThread.eventReceived;
    }


    class RestarterThread implements Runnable {
        int eventReceived;
        int port;
        int sslPort;

        long stopAfterTimeMilliSeconds;
        long startAfterTimeMS;

        RestarterThread(int port, int sslPort, long stopAfterTime, long startAfterTime) {
            this.port = port;
            this.sslPort = sslPort;
            stopAfterTimeMilliSeconds = stopAfterTime;
            startAfterTimeMS = startAfterTime;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(stopAfterTimeMilliSeconds);
            } catch (InterruptedException e) {
            }
            if (binaryDataReceiver != null) binaryDataReceiver.stop();

            eventReceived = getNumberOfEventsReceived();

            log.info("Number of events received in server shutdown :" + eventReceived);
            try {
                Thread.sleep(startAfterTimeMS);
            } catch (InterruptedException e) {
            }

            try {
                if (binaryDataReceiver != null) {
                    binaryDataReceiver.start();
                } else {
                    start(port, sslPort);
                }
            } catch (DataBridgeException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

        }
    }
}
