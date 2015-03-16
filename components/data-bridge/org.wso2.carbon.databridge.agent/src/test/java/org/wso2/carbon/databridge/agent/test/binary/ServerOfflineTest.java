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

import junit.framework.Assert;
import junit.framework.TestCase;
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

public class ServerOfflineTest extends TestCase {
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

    private BinaryTestServer binaryTestServer;

    private synchronized void startServer(int port, int securePort) throws DataBridgeException,
            StreamDefinitionStoreException, MalformedStreamDefinitionException, IOException {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
        binaryTestServer = new BinaryTestServer();
        binaryTestServer.start(port, securePort);
        binaryTestServer.addStreamDefinition(STREAM_DEFN, -1234);
    }

    public void testSendingEventsWhileServerOffline()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
            DataEndpointException, DataEndpointConfigurationException, SocketException {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();

        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath());
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ": 9611",
                "ssl://" + hostName + ":9711", "admin", "admin");
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
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

    public void testBlockingEventSendingAndServerStartup()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath());
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9611",
                "ssl://" + hostName + ":9711", "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        startServer(9611, 9711);

        int queueSize = AgentHolder.getInstance().getDataEndpointAgent("Binary").
                getAgentConfiguration().getQueueSize();
        int numberOfEventsSent = queueSize + 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(event);
        }
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(numberOfEventsSent, binaryTestServer.getNumberOfEventsReceived());
        binaryTestServer.stop();
    }


    public void testNonBlockingEventSendingAndServerStartup()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException,
            TransportException, DataEndpointException, DataEndpointConfigurationException,
            MalformedStreamDefinitionException, DataBridgeException,
            StreamDefinitionStoreException, IOException {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath());
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9651",
                "ssl://" + hostName + ":9751", "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        binaryTestServer = new BinaryTestServer();
        binaryTestServer.addStreamDefinition(STREAM_DEFN, -1234);
        binaryTestServer.stopAndStartDuration(9651, 9751, 10000, 1000);

        int queueSize = AgentHolder.getInstance().getDataEndpointAgent("Binary").
                getAgentConfiguration().getQueueSize();
        int numberOfEventsSent = queueSize + 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.tryPublish(event);
        }
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
        }

        Assert.assertEquals(queueSize, binaryTestServer.getNumberOfEventsReceived());
        binaryTestServer.stop();
    }
}
