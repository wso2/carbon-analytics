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

import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.test.thrift.KeyStoreUtil;

import java.net.MalformedURLException;

/**
 * Client of single client multiple server test
 */
public class AgentClient implements Runnable {
    static int NO_OF_EVENTS = 1000000;
    static int STABLE = 1000000;
    static int NO_OF_SERVERS = 2;
    static int serversLeft;
    private static volatile boolean started = false;


    public static void main(String[] args)
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException {
        KeyStoreUtil.setTrustStoreParams();
        if (args.length != 0 && args[0] != null) {
            NO_OF_EVENTS = Integer.parseInt(args[0]);
        }
        if (args.length != 0 && args[1] != null) {
            NO_OF_SERVERS = Integer.parseInt(args[1]);
        }
        System.out.println("Event no=" + NO_OF_EVENTS);
        System.out.println("server no=" + NO_OF_SERVERS);
        serversLeft = NO_OF_SERVERS;
        Thread thread = new Thread(new AgentClient());
        thread.start();
    }

    public void run() {

        Agent agent = new Agent();
        for (int i = 0; i < NO_OF_SERVERS; i++) {
            DataPublisher dataPublisher = null;
            try {
                dataPublisher = new DataPublisher("tcp://localhost:" + (7611 + i), "admin", "admin", agent);

                Thread thread = new Thread(new EventSender(dataPublisher));
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class EventSender implements Runnable {

        DataPublisher dataPublisher;

        EventSender(DataPublisher dataPublisher) {
            this.dataPublisher = dataPublisher;
        }

        @Override
        public void run() {
            try {
                String streamId = dataPublisher.defineStream("{" +
                                                             "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                             "  'version':'1.3.0'," +
                                                             "  'nickName': 'Stock Quote Information'," +
                                                             "  'description': 'Some Desc'," +
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
                for (int i = 0; i < NO_OF_EVENTS + STABLE; i++) {
                    dataPublisher.publish(generateEvent(streamId));
                }
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dataPublisher.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private Event generateEvent(String streamId) {
        Event event = new Event();
        event.setStreamId(streamId);
        event.setTimeStamp(System.currentTimeMillis());

        event.setMetaData(createMetaData());
        event.setCorrelationData(createCorrelationData());
        event.setPayloadData(createPayloadData());
        return event;
    }

    private Object[] createMetaData() {
        Object[] objects = new Object[1];
        objects[0] = "127.0.0.1";
        return objects;
    }


    private Object[] createCorrelationData() {
        return null;
    }

    private Object[] createPayloadData() {
        Object[] objects = new Object[5];
        objects[0] = "IBM";
        objects[1] = 76.5;
        objects[2] = 234;
        objects[3] = 89.3;
        objects[4] = 70.5;
        return objects;
    }

}
