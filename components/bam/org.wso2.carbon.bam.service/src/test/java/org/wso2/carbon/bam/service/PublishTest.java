/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.service;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class PublishTest {

    private void publish() {
        TTransport transport;
        try {
            transport = new TSocket("localhost", 7911);
            TProtocol protocol = new TBinaryProtocol(transport);
            // this client depends on the client class in the gen-java/tserver/gen/TimeServer.java file
            // public static class Client implements TServiceClient, Iface
            ReceiverService.Client client = new ReceiverService.Client(protocol);
            transport.open();

            Map<String, ByteBuffer> correlation = new HashMap<String, ByteBuffer>();
            correlation.put("workFlowId", ByteBuffer.wrap("ORDER".getBytes()));

            Map<String, ByteBuffer> meta = new HashMap<String, ByteBuffer>();
            correlation.put("MetaKey", ByteBuffer.wrap("MetaValue".getBytes()));

            Map<String, ByteBuffer> eventData = new HashMap<String, ByteBuffer>();
            correlation.put("EventData", ByteBuffer.wrap("EventValue".getBytes()));

            Event event = new Event();
            event.setCorrelation(correlation);
            event.setMeta(meta);
            event.setEvent(eventData);

            client.publish(event, "");

            transport.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (SessionTimeOutException e) {
            e.printStackTrace();
        }
    }

    private void publish1() {
        String trustStore = "/opt/installations/bam-stratos-setup/wso2carbon-core-3.2.2/repository/resources/security";
        System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        try {
            TTransport client = new THttpClient("https://localhost:9447/thriftAuthenticator");
            TProtocol protocol = new TBinaryProtocol(client);
            AuthenticatorService.Client authClient = new AuthenticatorService.Client(protocol);
            client.open();

            String sessionId = authClient.authenticate("albion@albion.com", "123abc");

            System.out.println("Session Id : " + sessionId);

            client.close();

            client = new THttpClient("https://localhost:9447/thriftReceiver");
            protocol = new TBinaryProtocol(client);
            ReceiverService.Client rclient = new ReceiverService.Client(protocol);
            client.open();

            Map<String, ByteBuffer> correlation = new HashMap<String, ByteBuffer>();
            correlation.put("workFlowId", ByteBuffer.wrap("ORDER".getBytes()));
            correlation.put("activityId", ByteBuffer.wrap("randomNumber".getBytes()));

            Map<String, ByteBuffer> meta = new HashMap<String, ByteBuffer>();
            meta.put("MetaKey", ByteBuffer.wrap("MetaValue".getBytes()));

            Map<String, ByteBuffer> eventData = new HashMap<String, ByteBuffer>();
            eventData.put("EventData", ByteBuffer.wrap("EventValue".getBytes()));
            Event event = new Event();
            event.setCorrelation(correlation);
            event.setMeta(meta);
            event.setEvent(eventData);

            //client.write("Test data".getBytes());
            rclient.publish(event, sessionId);

            client.close();
        } catch (TTransportException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TException e) {
            e.printStackTrace();
        } catch (SessionTimeOutException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    private void publish2() {
        String trustStore = "/opt/installations/bam-stratos-setup/wso2carbon-core-3.2.2/repository/resources/security";
        System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        
        try {
            TTransport client = new THttpClient("https://localhost:9443/thriftAuthenticator");
            TProtocol protocol = new TBinaryProtocol(client);
            AuthenticatorService.Client authClient = new AuthenticatorService.Client(protocol);
            client.open();

            String sessionId = authClient.authenticate("admin", "admin");

            System.out.println("Session Id : " + sessionId);

            client.close();

            TTransport receiverTransport = new TFramedTransport(new TSocket("localhost", 7611));
            protocol = new TBinaryProtocol(receiverTransport);

            ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);
            receiverTransport.open();

            Map<String, ByteBuffer> correlation = new HashMap<String, ByteBuffer>();
            correlation.put("workFlowId", ByteBuffer.wrap("ORDER".getBytes()));
            correlation.put("activityId", ByteBuffer.wrap("randomNumber".getBytes()));

            Map<String, ByteBuffer> meta = new HashMap<String, ByteBuffer>();
            meta.put("MetaKey", ByteBuffer.wrap("MetaValue".getBytes()));

            Map<String, ByteBuffer> eventData = new HashMap<String, ByteBuffer>();
            eventData.put("EventData", ByteBuffer.wrap("EventValue".getBytes()));
            Event event = new Event();
            event.setCorrelation(correlation);
            event.setMeta(meta);
            event.setEvent(eventData);

            receiverClient.publish(event, sessionId);


        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {

        } catch (SessionTimeOutException e) {

        }

    }

    public static void main(String[] args) {
        PublishTest publisher = new PublishTest();
        publisher.publish2();
    }

}
