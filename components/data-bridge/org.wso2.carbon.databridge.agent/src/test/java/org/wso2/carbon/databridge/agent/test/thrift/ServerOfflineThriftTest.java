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
package org.wso2.carbon.databridge.agent.test.thrift;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.test.DataPublisherTestUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.net.SocketException;

public class ServerOfflineThriftTest {
    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";
    private static final String STREAM_DEFN = "{" +
                                              "  'name':'" + STREAM_NAME + "'," +
                                              "  'version':'" + VERSION + "'," +
                                              "  'nickName': 'Stock Quote Information'," +
                                              "  'description': 'Some Desc'," +
                                              "  'tags':['foo', 'bar']," +
                                              "  'metaData':[" +
                                              "          {'name':'ipAdd','type':'STRING'}" +
                                              "  ]," +
                                              "  'payloadData':[" +
                                              "          {'name':'symbol','type':'STRING'}," +
                                              "          {'name':'price','type':'DOUBLE'}," +
                                              "          {'name':'volume','type':'INT'}," +
                                              "          {'name':'max','type':'DOUBLE'}," +
                                              "          {'name':'min','type':'Double'}" +
                                              "  ]" +
                                              "}";
    private String agentConfigFileName = "data.agent.config.yaml";
    private ThriftTestServer thriftTestServer;

    @BeforeClass
    public static void init() {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
    }

    @AfterClass
    public static void stop()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
                   DataEndpointException, DataEndpointConfigurationException {
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:8612",
                                                        "admin", "admin");
        dataPublisher.shutdownWithAgent();
    }

    @Test
    public void testSendingEventsWhileServerOffline()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
                   DataEndpointException, DataEndpointConfigurationException, SocketException {
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("tcp://" + hostName + ":8613",
                                                        "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        int numberOfEventsSent = 10;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(event);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

//    @Test
//    public void testAgentBehaviourWhileServerOffline() throws StreamDefinitionStoreException,
//            MalformedStreamDefinitionException, DataBridgeException, DataEndpointAuthenticationException,
//            DataEndpointAgentConfigurationException, TransportException, DataEndpointException,
//            DataEndpointConfigurationException, InterruptedException {
//        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
//        final TestAppender appender = new TestAppender();
////        final Logger logger = LoggerFactory.getRootLogger();
//        //logger.addAppender(appender);
//        String hostName = DataPublisherTestUtil.LOCAL_HOST;
//
//        thriftTestServer = new ThriftTestServer();
//        thriftTestServer.addStreamDefinition(STREAM_DEFN);
//        thriftTestServer.start(7611);
//
//        DataPublisher dataPublisher = new DataPublisher("tcp://" + hostName + ":7611", "knownUserName", "knownPass");
//
//        Event event = new Event();
//        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
//        event.setMetaData(new Object[]{"127.0.0.1"});
//        event.setCorrelationData(null);
//        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});
//
//        dataPublisher.publish(event);
//        Thread.sleep(5000);
//        thriftTestServer.stop();
//        Thread.sleep(5000);
//        dataPublisher.publish(event);
//        Thread.sleep(5000);
//
//        try {
//            final List<LoggingEvent> log = appender.getLog();
//            for (LoggingEvent loggingEvent : log) {
//                if (loggingEvent.getLevel() == Level.ERROR) {
//                    Throwable exception = loggingEvent.getThrowableInformation().getThrowable();
//                    if (exception instanceof DataEndpointException && exception.getMessage().contains("knownUserName")) {
//                        String errorMessage = exception.getMessage();
//                        Assert.assertTrue("Format of log entry does not match", errorMessage.contains(DataEndpointConstants.SEPARATOR));
//                        Assert.assertFalse("Log output not sanitized", errorMessage.contains("knownPass"));
//                        break;
//                    }
//                }
//            }
//        } finally {
//            //logger.removeAppender(appender);
//            try {
//                dataPublisher.shutdown();
//            } catch (Exception e) {
//                // do nothing
//            }
//        }
//    }

    @Test
    public void testBlockingEventSendingAndServerStartup()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
                   DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException,
                   DataBridgeException, StreamDefinitionStoreException, SocketException {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("tcp://" + hostName + ":7641",
                                                        "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        thriftTestServer = new ThriftTestServer();
        thriftTestServer.addStreamDefinition(STREAM_DEFN);
        thriftTestServer.stopAndStartDuration(7641, 10000, 1000);

        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();

        int queueSize = AgentHolder.getInstance().getDataEndpointAgent("Thrift").
                getAgentConfiguration().getQueueSize();
        int numberOfEventsSent = queueSize + 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(event);
        }
        try {
            Thread.sleep(35000);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(thriftTestServer.getNumberOfEventsReceived(), queueSize);
        dataPublisher.shutdown();
        thriftTestServer.stop();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void testNonBlockingEventSendingAndServerStartup()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
                   DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException,
                   DataBridgeException, StreamDefinitionStoreException, SocketException {
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("tcp://" + hostName + ":7651",
                                                        "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        thriftTestServer = new ThriftTestServer();
        thriftTestServer.addStreamDefinition(STREAM_DEFN);
        thriftTestServer.stopAndStartDuration(7651, 10000, 1000);

        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();

        int queueSize = AgentHolder.getInstance().getDataEndpointAgent("Thrift").
                getAgentConfiguration().getQueueSize();
        int numberOfEventsSent = queueSize + 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.tryPublish(event);
        }
        try {
            Thread.sleep(35000);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(thriftTestServer.getNumberOfEventsReceived(), queueSize);
        dataPublisher.shutdown();
        thriftTestServer.stop();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

//    public void testServerShutDownWhileBlockingPublishing() throws SocketException,
//            DataEndpointAuthenticationException, DataEndpointAgentConfigurationException,
//            TransportException, DataEndpointException, DataEndpointConfigurationException,
//            MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException {
//        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath());
//        String hostName = DataPublisherTestUtil.LOCAL_HOST;
//
//        startServer(7641);
//
//        DataPublisher dataPublisher = new DataPublisher("tcp://" + hostName + ":7641",
//                "ssl://" + hostName + ":7741", "admin", "admin");
//        Event event = new Event();
//        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
//        event.setMetaData(new Object[]{"127.0.0.1"});
//        event.setCorrelationData(null);
//        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});
//
//        int publishTime = 20000;
//
//        thriftTestServer.stopAndStartDuration(7641, 5000, 60000);
//        long startTime = System.currentTimeMillis();
//
//        int eventsPublished = 0;
//        while (true) {
//            eventsPublished++;
//            dataPublisher.publish(event);
//            if (System.currentTimeMillis() >= startTime + publishTime) {
//                break;
//            }
//        }
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//        }
//        Assert.assertEquals(thriftTestServer.getNumberOfEventsReceived(), eventsPublished);
//        thriftTestServer.stop();
//    }

//    private class TestAppender extends AppenderSkeleton {
//        private final List<LoggingEvent> log = new ArrayList<>();
//
//        @Override
//        public boolean requiresLayout() {
//            return false;
//        }
//
//        @Override
//        protected void append(final LoggingEvent loggingEvent) {
//            log.add(loggingEvent);
//        }
//
//        @Override
//        public void close() {
//        }
//
//        public List<LoggingEvent> getLog() {
//            return new ArrayList<>(log);
//        }
//    }

}
