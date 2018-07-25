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

import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.event.queue.EventQueue;
import org.wso2.carbon.stream.processor.core.event.queue.QueuedEvent;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPNettyClient;
import org.wso2.carbon.stream.processor.core.util.BinaryEventConverter;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ActiveNodeEventDispatcher implements Runnable {
    private static final Logger log = Logger.getLogger(ActiveNodeEventDispatcher.class);
    private TCPNettyClient tcpNettyClient = new TCPNettyClient();
    private String host;
    private int port;
    private EventQueue<QueuedEvent> eventQueue;
    private ByteBuffer messageBuffer = ByteBuffer.wrap(new byte[1024 * 1024 * 100]);
    private byte[] data;

    public ActiveNodeEventDispatcher(EventQueue<QueuedEvent> eventQueue, String host, int port) {
        this.eventQueue = eventQueue;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            if (!tcpNettyClient.isActive()) {
                tcpNettyClient.connect(host, port);
            }
        } catch (ConnectionUnavailableException e) {
            log.error("Error in connecting to " + host + ":" + port + ". Will retry in the next iteration");
            return;
        }
        log.info("Active Node Remaining Capacity :   " + eventQueue.getRemainingCapacity());
        QueuedEvent queuedEvent = eventQueue.dequeue();
        int numOfEvents = 0;
        messageBuffer.clear();
        messageBuffer.putInt(0);
        while (queuedEvent != null && messageBuffer.position() < 104857600) {
            try {
                BinaryEventConverter.convertToBinaryMessage(queuedEvent, messageBuffer);
                numOfEvents++;
            } catch (IOException e) {
                log.error("Error in converting events to binary message.Will retry in the next iteration");
            }
            log.info("Sent - " + queuedEvent.getSourceHandlerElementId() + " |   " + queuedEvent.getEvent().toString());
            queuedEvent = eventQueue.dequeue();
        }
        messageBuffer.putInt(0, numOfEvents);
        if (numOfEvents > 0) {
            try {
                data = Arrays.copyOfRange(messageBuffer.array(), 0, messageBuffer.position());
                tcpNettyClient.send("eventMessage", data);
            } catch (ConnectionUnavailableException e) {
                log.error("Error in sending events to " + host + ":" + port + ".Will retry in the next iteration");
            }
        }
    }
}
