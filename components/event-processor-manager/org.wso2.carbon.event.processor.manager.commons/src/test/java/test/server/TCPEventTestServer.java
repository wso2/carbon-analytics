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

import org.wso2.carbon.event.processor.manager.commons.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServerConfig;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

public class TCPEventTestServer {


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


        TCPEventServer eventServer = new TCPEventServer(new TCPEventServerConfig("0.0.0.0", 7612), new StreamCallback() {

            public int count;
            public long start = System.currentTimeMillis();

            /**
             * @param streamId the stream id for the incoming event
             * @param timestamp
             * @param event    the event as an object array of attributes
             */
            @Override
            public void receive(String streamId, long timestamp, Object[] event, Map<String, String> arbitraryMapData) {

                count++;
                if (count % 2000000 == 0) {
                    long end = System.currentTimeMillis();
                    double tp = (2000000 * 1000.0 / (end - start));
                    System.out.println("Throughput = " + tp + " Event/sec");
                    start = end;
                }
            }
        }, null);

        eventServer.addStreamDefinition(streamDefinition);
        eventServer.addStreamDefinition(streamDefinition1);

        eventServer.start();

        Thread.sleep(1000000);

        System.out.println("shutdown");
        eventServer.shutdown();
        Thread.sleep(2000);
    }
}
