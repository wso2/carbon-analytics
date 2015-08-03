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

import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisher;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.IOException;
import java.util.Random;

public class TCPEventTestClient {


    public static void main(String[] args) throws Exception {

        StreamDefinition streamDefinition = new StreamDefinition();
        streamDefinition.setId("TestStream");
        streamDefinition.attribute("att1", Attribute.Type.INT);
        streamDefinition.attribute("att2", Attribute.Type.FLOAT);
        streamDefinition.attribute("att3", Attribute.Type.STRING);
        streamDefinition.attribute("att4", Attribute.Type.INT);

        StreamDefinition streamDefinition1 = new StreamDefinition();
        streamDefinition1.setId("TestStream1");
        streamDefinition1.attribute("att1", Attribute.Type.LONG);
        streamDefinition1.attribute("att2", Attribute.Type.FLOAT);
        streamDefinition1.attribute("att3", Attribute.Type.STRING);
        streamDefinition1.attribute("att4", Attribute.Type.DOUBLE);
        streamDefinition1.attribute("att5", Attribute.Type.BOOL);
        TCPEventPublisher TCPEventPublisher = new TCPEventPublisher("localhost:7612", false, null);
        TCPEventPublisher.addStreamDefinition(streamDefinition);
        TCPEventPublisher.addStreamDefinition(streamDefinition1);

        Thread.sleep(1000);
        System.out.println("Start testing");
        Random random = new Random();

//        for (int i = 0; i < 1000000000; i++) {
//            eventClient.sendEvent("TestStream", new Object[]{75, 45f, "Abcdefghijklmnop", 89});
//            eventClient.sendEvent("TestStream1", new Object[]{90l, 77f, "Abcdefghijklmnop", 4.5,true});
//
//        }
        Thread thread = new Thread(new Runner(TCPEventPublisher));
        thread.start();
//        Thread thread1 = new Thread(new Runner(TCPEventPublisher));
//        thread1.start();
//        Thread thread2 = new Thread(new Runner(TCPEventPublisher));
//        thread2.start();
    }

    static class Runner implements Runnable {


        private TCPEventPublisher tcpEventPublisher;

        Runner(TCPEventPublisher tcpEventPublisher) {
            this.tcpEventPublisher = tcpEventPublisher;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
//            for (int i = 0; i < 1000000000; i++) {
            try {
                tcpEventPublisher.sendEvent("TestStream", System.currentTimeMillis(), new Object[]{75, 45f, "Abcdefghijklmnop", 89,}, true);
                Thread.sleep(10000);
                System.out.println("next");
                tcpEventPublisher.sendEvent("TestStream1", System.currentTimeMillis(), new Object[]{90l, 77f, "Abcdefghijklmnop", 4.5, true}, true);
                Thread.sleep(1000);
                System.out.println("next");
                tcpEventPublisher.sendEvent("TestStream1", System.currentTimeMillis(), new Object[]{90l, 77f, "Abcdefghijklmnop", 4.5, true}, true);
                Thread.sleep(1000);
                System.out.println("next");
                tcpEventPublisher.sendEvent("TestStream1", System.currentTimeMillis(), new Object[]{90l, 77f, "Abcdefghijklmnop", 4.5, true}, true);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//
//            }
        }
    }
}
