/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.stream.processor.core.ha;

import io.netty.handler.codec.base64.Base64;
import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.event.queue.QueuedEvent;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPNettyClient;
import org.wso2.carbon.stream.processor.core.util.BinaryEventConverter;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.Deflater;

public class ActiveNodeEventDispatcher {
    private static final Logger log = Logger.getLogger(ActiveNodeEventDispatcher.class);
    private TCPNettyClient tcpNettyClient;
    private String host;
    private int port;
    //private EventQueue<QueuedEvent> eventQueue;
    private ByteBuffer messageBuffer = ByteBuffer.wrap(new byte[1024 * 1024 * 10]);
    private byte[] data;

    public ActiveNodeEventDispatcher() {
//        this.eventQueue = eventQueue;
//        this.host = host;
//        this.port = port;
        tcpNettyClient = TCPNettyClient.getInstance();
    }

    public void sendEventToPassiveNode(QueuedEvent queuedEvent) {
        try {
            if (!tcpNettyClient.isActive()) {
                tcpNettyClient.connect("localhost", 9893);
            }
        } catch (ConnectionUnavailableException e) {
            log.error("Error in connecting to " + host + ":" + port + ". Will retry in the next iteration");
            return;
        }
        // log.info("Active Node Remaining Capacity :   " + eventQueue .getRemainingCapacity());
        //QueuedEvent queuedEvent = eventQueue.dequeue();
        int numOfEvents = 0;
        messageBuffer.clear();
        messageBuffer.putInt(1);
        //messageBuffer.putInt(0);
        //while (queuedEvent != null && messageBuffer.position() < 10485760) {
        try {
            BinaryEventConverter.convertToBinaryMessage(queuedEvent, messageBuffer);
            numOfEvents++;
        } catch (IOException e) {
            log.error("Error in converting events to binary message.Will retry in the next iteration");
        }
        log.info("Sent - " + queuedEvent.getSourceHandlerElementId() + " |   " + queuedEvent.getEvent().toString());
        // queuedEvent = eventQueue.dequeue();
        //}
        messageBuffer.putInt(0, numOfEvents);
        if (numOfEvents > 0) {
            try {
                data = Arrays.copyOfRange(messageBuffer.array(), 0, messageBuffer.position());
                tcpNettyClient.send("eventMessage", compress(data));
            } catch (ConnectionUnavailableException e) {
                log.error("Error in sending events to " + host + ":" + port + ".Will retry in the next iteration");
            }
        }
    }


    public void sendEventsToPassiveNode(QueuedEvent[] queuedEvents) {
        try {
            if (!tcpNettyClient.isActive()) {
                tcpNettyClient.connect("localhost", 9893);
            }
        } catch (ConnectionUnavailableException e) {
            log.error("Error in connecting to " + host + ":" + port + ". Will retry in the next iteration");
            return;
        }
        // log.info("Active Node Remaining Capacity :   " + eventQueue .getRemainingCapacity());
        //QueuedEvent queuedEvent = eventQueue.dequeue();
        int numOfEvents = 0;
        messageBuffer.clear();
        messageBuffer.putInt(1);
        messageBuffer.putInt(0);
        for (QueuedEvent queuedEvent : queuedEvents) {
            try {
                BinaryEventConverter.convertToBinaryMessage(queuedEvent, messageBuffer);
                numOfEvents++;
            } catch (IOException e) {
                log.error("Error in converting events to binary message.Will retry in the next iteration");
            }
        }
        //while (queuedEvent != null && messageBuffer.position() < 10485760) {
//        try {
//            BinaryEventConverter.convertToBinaryMessage(queuedEvent, messageBuffer);
//            numOfEvents++;
//        } catch (IOException e) {
//            log.error("Error in converting events to binary message.Will retry in the next iteration");
//        }
//        log.info("Sent - " + queuedEvent.getSourceHandlerElementId() + " |   " + queuedEvent.getEvent().toString());
        // queuedEvent = eventQueue.dequeue();
        //}
        messageBuffer.putInt(0, numOfEvents);
        if (numOfEvents > 0) {
            try {
                data = Arrays.copyOfRange(messageBuffer.array(), 0, messageBuffer.position());
                tcpNettyClient.send("eventMessage", compress(data));
            } catch (ConnectionUnavailableException e) {
                log.error("Error in sending events to " + host + ":" + port + ".Will retry in the next iteration");
            }
        }
    }

    public static byte[] compress(byte[] dataBytes) {
        //count++;
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        //byte[] dataBytes = data.getBytes("UTF-8");
        deflater.setInput(dataBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataBytes.length);
        deflater.finish();
        byte[] buffer = new byte[dataBytes.length];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            log.error("Error in closing the output stream");
        }


        byte[] output = outputStream.toByteArray();
        //String result =  Base64.encode(output);

        int diff = (dataBytes.length - output.length);
        //totalBytesSaved += diff;
//        if (count % 10000 == 0){
        //System.out.println("Total Bytes Saved :" + diff + "       " + output.length);
//        }
        return output;
        //return new String(output, 0, count, "UTF-8");
    }
}
