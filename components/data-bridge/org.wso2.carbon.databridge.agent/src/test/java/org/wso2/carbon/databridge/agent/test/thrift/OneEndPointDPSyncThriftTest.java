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


public class OneEndPointDPSyncThriftTest {
    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";
    private ThriftTestServer thriftTestServer;
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


    @BeforeClass
    public static void init() {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
    }

    @AfterClass
    public static void stop() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException {
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:8612",
                "admin", "admin");
        dataPublisher.shutdownWithAgent();
    }

    private synchronized void startServer(int port) throws DataBridgeException,
            StreamDefinitionStoreException, MalformedStreamDefinitionException {
        thriftTestServer = new ThriftTestServer();
        thriftTestServer.start(port);
        thriftTestServer.addStreamDefinition(STREAM_DEFN);

    }

    @Test
    public void testOneDataEndpoint() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, SocketException {
        startServer(7662);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("Thrift", "tcp://" + hostName + ":7662",
                "ssl://" + hostName + ":7762", "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(event);
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        Assert.assertEquals(thriftTestServer.getNumberOfEventsReceived(), numberOfEventsSent);
        thriftTestServer.resetReceivedEvents();
        thriftTestServer.stop();
    }


    @Test
    public void testTwoDataEndpoint() throws DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException, TransportException,
            DataEndpointException, DataEndpointConfigurationException,
            MalformedStreamDefinitionException, DataBridgeException,
            StreamDefinitionStoreException, SocketException {
        startServer(7672);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("Thrift", "tcp://" + hostName + ":7672, ssl://" + hostName + ":7662",
                "ssl://" + hostName + ":7772, ssl://" + hostName + ":7762", "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(event);
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        Assert.assertEquals(thriftTestServer.getNumberOfEventsReceived(), numberOfEventsSent);
        thriftTestServer.resetReceivedEvents();
        thriftTestServer.stop();
    }

    @Test
    public void testInvalidAuthenticationURLs() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, SocketException {
        boolean expected = false;
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        try {
            DataPublisher dataPublisher = new DataPublisher("thrift", "tcp://" + hostName + ":7611, ssl://" + hostName + ":7612",
                    "ssl://" + hostName + ":7711", "admin", "admin");

        } catch (DataEndpointConfigurationException ex) {
            expected = true;
        }
        Assert.assertTrue(expected, "Invalid urls passed for receiver and auth, and hence expected to fail");
    }

    @Test
    public void testInvalidReceiverURLs() throws DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException, TransportException,
            DataEndpointException, DataEndpointConfigurationException,
            MalformedStreamDefinitionException,
            DataBridgeException,
            StreamDefinitionStoreException, SocketException {
        boolean expected = false;
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        try {
            DataPublisher dataPublisher = new DataPublisher("Thrift", "tcp://" + hostName + ":7611",
                    "ssl://" + hostName + ":7711, ssl://" + hostName + ":7712", "admin", "admin");
        } catch (DataEndpointConfigurationException ex) {
            expected = true;
        }
        Assert.assertTrue(expected, "Invalid urls passed for receiver and auth, and hence expected to fail");
    }

    @Test
    public void testShutdownDataPublisher() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, SocketException {
        startServer(10162);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("Thrift", "tcp://" + hostName + ":10162",
                "ssl://" + hostName + ":10262", "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        int numberOfEventsSent = 100000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(event);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        dataPublisher.shutdown();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        dataPublisher.publish(event);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(thriftTestServer.getNumberOfEventsReceived(), numberOfEventsSent);
        thriftTestServer.resetReceivedEvents();
        thriftTestServer.stop();
    }

}
