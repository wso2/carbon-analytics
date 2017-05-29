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

import java.io.IOException;
import java.net.SocketException;

public class ServerOfflineSyncBinaryTest {
    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";
    private String agentConfigFileName = "sync.data.agent.config.yaml";

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

    private BinaryTestServer binaryTestServer;

    @BeforeClass
    public static void init() {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
    }

    @AfterClass
    public static void stop() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException {
        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://localhost:9687",
                "ssl://localhost:9787", "admin", "admin");
        dataPublisher.shutdownWithAgent();
    }

    @Test
    public void testTrySendingEventsWhileServerOffline()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
            DataEndpointException, DataEndpointConfigurationException, SocketException {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();

        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ": 9612",
                "ssl://" + hostName + ":9712", "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.tryPublish(event);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
    }

    @Test
    public void testBlockingEventSendingAndServerStartup()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9620",
                "ssl://" + hostName + ":9720", "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        binaryTestServer = new BinaryTestServer();
        binaryTestServer.addStreamDefinition(STREAM_DEFN);
        binaryTestServer.stopAndStartDuration(9620, 9720, 1000, 1000);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        int queueSize = AgentHolder.getInstance().getDataEndpointAgent("Binary").
                getAgentConfiguration().getQueueSize();
        int numberOfEventsSent = queueSize + 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(event);
        }

        try {
            Thread.sleep(40000);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(binaryTestServer.getNumberOfEventsReceived(), queueSize + 1000);
        dataPublisher.shutdown();
        binaryTestServer.stop();
    }


    @Test
    public void testNonBlockingEventSendingAndServerStartup()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException,
            TransportException, DataEndpointException, DataEndpointConfigurationException,
            MalformedStreamDefinitionException, DataBridgeException,
            StreamDefinitionStoreException, IOException {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9652",
                "ssl://" + hostName + ":9752", "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        binaryTestServer = new BinaryTestServer();
        binaryTestServer.addStreamDefinition(STREAM_DEFN);
        binaryTestServer.stopAndStartDuration(9652, 9752, 7000, 1000);

        int queueSize = AgentHolder.getInstance().getDataEndpointAgent("Binary").
                getAgentConfiguration().getQueueSize();
        int numberOfEventsSent = queueSize + 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.tryPublish(event);
        }
        try {
            Thread.sleep(16000);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(binaryTestServer.getNumberOfEventsReceived(), 0);
        dataPublisher.shutdown();
        binaryTestServer.stop();
    }
}
