/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.thrift.TException;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftAuthenticationException;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;

import java.net.MalformedURLException;

public class HTTPTestAgent {

    public static void main(String[] args)
            throws UndefinedEventTypeException, AgentException, MalformedURLException,
                   AuthenticationException, MalformedStreamDefinitionException, DataBridgeException,
                   StreamDefinitionException, TransportException, InterruptedException,
                   DifferentStreamDefinitionAlreadyDefinedException, TException,
                   ThriftAuthenticationException {
        HTTPTestAgent testAgent = new HTTPTestAgent();
        testAgent.testHttpProtocol();
    }

    public void testHttpProtocol()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, DataBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException, TException, ThriftAuthenticationException {

        KeyStoreUtil.setTrustStoreParams();
//        System.setProperty("javax.net.ssl.trustStore",  "/home/suho/projects/wso2/trunk/kernel/distribution/product/modules/distribution/target/wso2carbon-4.0.0-SNAPSHOT/repository/resources/security/client-truststore.jks");
//        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");


        //All of these are possible
//        DataPublisher dataPublisher = new DataPublisher("ssl://localhost:7711", "http://localhost:9763", "admin", "admin");
//        DataPublisher dataPublisher = new DataPublisher("https://localhost:9443", "tcp://localhost:7611", "admin", "admin");
        DataPublisher dataPublisher = new DataPublisher("https://localhost:9443", "http://localhost:9763", "admin", "admin");
        String id1 = dataPublisher.defineStream("{" +
                                                "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                "  'version':'2.3.0'," +
                                                "  'nickName': 'Stock Quote Information'," +
                                                "  'description': 'Some Desc'," +
                                                "  'tags':['foo', 'bar']," +
                                                "  'metaData':[" +
                                                "          {'name':'ipAdd','type':'STRING'}" +
                                                "  ]," +
                                                "  'correlationData':[" +
                                                "          {'name':'correlationId','type':'STRING'}" +
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
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, new Object[]{"HD34"}, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        Thread.sleep(3000);
        dataPublisher.stop();


    }

}
