/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package test.server;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.manager.commons.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServerConfig;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import test.util.AnalyticStatDataProvider;
import test.util.DataProvider;
import test.util.SimpleDataProvider;

import java.io.IOException;
import java.net.BindException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TCPEventSendingTestCase {
    public static final int EVENTS_PER_CLIENT = 10;
    public static final int TOTAL_CLIENTS = 5;
    private static final Log log = LogFactory.getLog(TCPEventSendingTestCase.class);
    private ExecutorService threadPool;

    @Before
    public void initialize() {
        threadPool = Executors.newFixedThreadPool(20);
    }

    @Test
    public void testEventSendingToServer() throws InterruptedException, IOException {
        String hostname = "0.0.0.0";
        int port = 7612;

        StreamDefinition streamDefinition = new StreamDefinition().id("TestStream")
                .attribute("att1", Attribute.Type.INT)
                .attribute("att2", Attribute.Type.FLOAT)
                .attribute("att3", Attribute.Type.STRING)
                .attribute("att4", Attribute.Type.INT);

        TestStreamCallback streamCallback = new TestStreamCallback();
        TCPEventServer tcpEventServer = new TCPEventServer(new TCPEventServerConfig(hostname, port), streamCallback, null);
        tcpEventServer.addStreamDefinition(streamDefinition);
        tcpEventServer.start();
        Thread.sleep(1000);
        threadPool.submit(new ClientThread(hostname, port, streamDefinition, new SimpleDataProvider(), 100, false, 0));
        Thread.sleep(5000);
        Assert.assertEquals(100, streamCallback.getEventCount());
        log.info("Shutting down server...");
        tcpEventServer.shutdown();
    }

    @Test
    public void testHighLoadEventSendingToServer() throws IOException, InterruptedException {
        String hostname = "0.0.0.0";
        int port = 7613;

        StreamDefinition streamDefinition = new StreamDefinition().id("analyticsStats")
                .attribute("meta_ipAdd", Attribute.Type.STRING)
                .attribute("meta_index", Attribute.Type.LONG)
                .attribute("meta_timestamp", Attribute.Type.LONG)
                .attribute("meta_nanoTime", Attribute.Type.LONG)
                .attribute("userID", Attribute.Type.STRING)
                .attribute("searchTerms", Attribute.Type.STRING);

        TestStreamCallback streamCallback = new TestStreamCallback();
        TCPEventServer tcpEventServer = new TCPEventServer(new TCPEventServerConfig(hostname, port), streamCallback, null);
        tcpEventServer.addStreamDefinition(streamDefinition);
        tcpEventServer.start();
        Thread.sleep(1000);
        for (int i = 0; i < TOTAL_CLIENTS; i++) {
            threadPool.submit(new ClientThread(hostname, port, streamDefinition, new AnalyticStatDataProvider(), EVENTS_PER_CLIENT, false, 0));
        }
        while (streamCallback.getEventCount() < TOTAL_CLIENTS * EVENTS_PER_CLIENT) {
            Thread.sleep(5000);
        }
        Assert.assertEquals(TOTAL_CLIENTS * EVENTS_PER_CLIENT, streamCallback.getEventCount());
        log.info("Shutting down server...");
        tcpEventServer.shutdown();
    }


    @Test
    public void testEventSendingOnServerFailure() throws IOException, InterruptedException {
        String hostname = "0.0.0.0";
        int port = 7614;

        StreamDefinition streamDefinition = new StreamDefinition().id("TestStream")
                .attribute("att1", Attribute.Type.INT)
                .attribute("att2", Attribute.Type.FLOAT)
                .attribute("att3", Attribute.Type.STRING)
                .attribute("att4", Attribute.Type.INT);

        TestStreamCallback streamCallback = new TestStreamCallback();
        TCPEventServer tcpEventServer = new TCPEventServer(new TCPEventServerConfig(hostname, port), streamCallback, null);
        threadPool.submit(new ClientThread(hostname, port, streamDefinition, new SimpleDataProvider(), 100, false, 1000));
        Thread.sleep(10000);
        tcpEventServer.addStreamDefinition(streamDefinition);
        tcpEventServer.start();
        Thread.sleep(5000);
        Assert.assertTrue(streamCallback.getEventCount() > 0);
        log.info("Shutting down server...");
        tcpEventServer.shutdown();
    }


    @Test
    public void testAddressAlreadyExisted() throws Exception {
        String hostname = "0.0.0.0";
        int port = 7615;

        StreamDefinition streamDefinition = new StreamDefinition().id("TestStream")
                .attribute("att1", Attribute.Type.INT)
                .attribute("att2", Attribute.Type.FLOAT)
                .attribute("att3", Attribute.Type.STRING)
                .attribute("att4", Attribute.Type.INT);

        TestStreamCallback streamCallback = new TestStreamCallback();

        TCPEventServer tcpEventServer = new TCPEventServer(new TCPEventServerConfig(hostname, port), streamCallback, null);
        TCPEventServer tcpEventServer1 = new TCPEventServer(new TCPEventServerConfig(hostname, port), streamCallback, null);
        boolean errorOccurred = false;
        try {
            tcpEventServer.addStreamDefinition(streamDefinition);
            tcpEventServer.start();
            Thread.sleep(1000);

            try {
                tcpEventServer1.addStreamDefinition(streamDefinition);
                tcpEventServer1.start();
                Thread.sleep(1000);
            } catch (BindException e) {
                log.error("Address already exist", e);
                errorOccurred = true;
            } catch (IOException e) {
                throw new Exception(e);
            }
        } finally {
            log.info("Shutting down server 1...");
            tcpEventServer.shutdown();
            log.info("Shutting down server 2...");
            tcpEventServer1.shutdown();
            Assert.assertEquals(true, errorOccurred);
        }
    }


    private static class TestStreamCallback implements StreamCallback {
        AtomicInteger eventCount = new AtomicInteger(0);

        @Override
        public void receive(String streamId, long timestamp, Object[] event) {
            log.info("Event count:" + eventCount.incrementAndGet() + ", Stream ID: " + streamId + ", Event: " + Arrays.deepToString(event));
        }

        public int getEventCount() {
            return eventCount.get();
        }
    }

    private static class ClientThread implements Runnable {
        private final String SEPARATOR = ":";
        int eventsToSend = 0;
        StreamDefinition streamDefinition;
        DataProvider dataProvider;
        private boolean isSynchronous;
        private int delay;
        private String hostURL;

        public ClientThread(String hostname, int port, StreamDefinition streamDefinition, DataProvider dataProvider,
                            int eventsToSend, boolean isSynchronous, int delay) {
            this.hostURL = hostname + SEPARATOR + port;
            this.eventsToSend = eventsToSend;
            this.streamDefinition = streamDefinition;
            this.dataProvider = dataProvider;
            this.isSynchronous = isSynchronous;
            this.delay = delay;
        }

        @Override
        public void run() {
            TCPEventPublisher tcpEventPublisher = null;
            try {
                tcpEventPublisher = new TCPEventPublisher(hostURL, isSynchronous, null);
                tcpEventPublisher.addStreamDefinition(streamDefinition);
                Thread.sleep(1000);
                log.info("Starting event client to send events to " + hostURL);

                for (int i = 0; i < eventsToSend; i++) {
                    tcpEventPublisher.sendEvent(streamDefinition.getId(), System.currentTimeMillis(), dataProvider.getEvent(), true);
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (tcpEventPublisher != null) {
                    tcpEventPublisher.shutdown();
                }
            }
        }
    }
}