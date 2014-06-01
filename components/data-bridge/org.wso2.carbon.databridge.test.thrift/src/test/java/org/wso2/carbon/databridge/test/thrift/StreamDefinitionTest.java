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
package org.wso2.carbon.databridge.test.thrift;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;

import java.net.MalformedURLException;

public class StreamDefinitionTest extends TestCase {

    public void testSendingSameStreamDefinitions()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, DataBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7614);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7614", "admin", "admin");
        String id1 = dataPublisher.defineStream("{" +
                                                "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                "  'version':'2.3.0'," +
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
                                                "}");
        String id2 = dataPublisher.defineStream("{" +
                                                "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                "  'version':'2.3.0'," +
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
                                                "}");

        Assert.assertEquals(id1, id2);
        //In this case correlation data is null
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.stop();
        testServer.stop();
    }

    public void testSendingSameStreamDefinitionWithAndWithoutVersion()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, DataBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7615);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7615", "admin", "admin");
        String streamDef = dataPublisher.defineStream("{" +
                                                      "  'name':'org.wso2.esb.MediatorStatistics1'," +
//                                                  "  'version':'1.0.0'," +
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
                                                      "}");
        dataPublisher.defineStream("{" +
                                   "  'name':'org.wso2.esb.MediatorStatistics1'," +
                                   "  'version':'1.0.0'," +
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
                                   "}");

        //In this case correlation data is null
        dataPublisher.publish(streamDef, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.stop();
        testServer.stop();
    }

    public void testSendingSameStreamDefinitionWithAndWithoutVersion2()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, DataBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7616);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7616", "admin", "admin");
        String streamDef = dataPublisher.defineStream("{" +
                                                      "  'name':'org.wso2.esb.MediatorStatistics2'," +
                                                      "  'version':'1.0.0'," +
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
                                                      "}");
        dataPublisher.defineStream("{" +
                                   "  'name':'org.wso2.esb.MediatorStatistics2'," +
//                                                  "  'version':'1.0.0'," +
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
                                   "}");

        //In this case correlation data is null
        dataPublisher.publish(streamDef, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.stop();
        testServer.stop();
    }


    public void testSendingTwoDifferentStreamDefinitionsWithSameStreamId()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,

                   InterruptedException, DataBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException {

        TestServer testServer = new TestServer();
        testServer.start(7617);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7617", "admin", "admin");
        dataPublisher.defineStream("{" +
                                   "  'name':'org.wso2.esb.MediatorStatistics3'," +
                                   "  'version':'2.3.0'," +
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
                                   "}");
        Boolean exceptionOccurred = false;
        try {
            dataPublisher.defineStream("{" +
                                       "  'name':'org.wso2.esb.MediatorStatistics3'," +
                                       "  'version':'2.3.0'," +
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
                                       "          {'name':'min','type':'Double'}" +
                                       "  ]" +
                                       "}");

        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            exceptionOccurred = true;
        }
        Assert.assertTrue(exceptionOccurred);

        dataPublisher.stop();
        testServer.stop();
    }

}
