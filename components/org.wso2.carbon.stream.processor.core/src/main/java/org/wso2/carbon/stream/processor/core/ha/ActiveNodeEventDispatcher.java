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
import org.wso2.carbon.stream.processor.core.event.queue.QueuedEvent;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPConnection;
import org.wso2.carbon.stream.processor.core.ha.util.HAConstants;
import org.wso2.carbon.stream.processor.core.util.BinaryEventConverter;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class ActiveNodeEventDispatcher {
    private static final Logger log = Logger.getLogger(ActiveNodeEventDispatcher.class);
    private TCPConnection tcpConnection;
    private String host;
    private int port;
    private ByteBuffer messageBuffer = ByteBuffer.wrap(new byte[1024 * 1024 * 10]);
    private byte[] data;
    private QueuedEvent queuedEvent;
    private QueuedEvent[] queuedEvents;

    public void setQueuedEvent(QueuedEvent queuedEvent) {
        this.queuedEvent = queuedEvent;
    }

    public void setQueuedEvents(QueuedEvent[] queuedEvents) {
        this.queuedEvents = queuedEvents;
    }

    public void sendEventToPassiveNode(QueuedEvent queuedEvent) {
        int numOfEvents = 0;
        messageBuffer.clear();
        messageBuffer.putInt(1);
        try {
            BinaryEventConverter.convertToBinaryMessage(queuedEvent, messageBuffer);
            numOfEvents++;
        } catch (IOException e) {
            log.error("Error in converting events to binary message.Will retry in the next iteration");
        }
        messageBuffer.putInt(0, numOfEvents);
        if (numOfEvents > 0) {
            try {
                data = Arrays.copyOfRange(messageBuffer.array(), 0, messageBuffer.position());
                tcpConnection.send(HAConstants.CHANNEL_ID_MESSAGE, data);
            } catch (ConnectionUnavailableException e) {
                log.error("Error in sending events to " + host + ":" + port + ".Will retry in the next iteration");
            }
        }
    }

    public void sendEventsToPassiveNode(QueuedEvent[] queuedEvents) {
        int numOfEvents = 0;
        messageBuffer.clear();
        messageBuffer.putInt(0);
        for (QueuedEvent queuedEvent : queuedEvents) {
            try {
                BinaryEventConverter.convertToBinaryMessage(queuedEvent, messageBuffer);
                numOfEvents++;
            } catch (IOException e) {
                log.error("Error in converting events to binary message.Will retry in the next iteration");
            }
        }
        messageBuffer.putInt(0, numOfEvents);
        if (numOfEvents > 0) {
            try {
                data = Arrays.copyOfRange(messageBuffer.array(), 0, messageBuffer.position());
                tcpConnection.send(HAConstants.CHANNEL_ID_MESSAGE, data);
            } catch (ConnectionUnavailableException e) {
                log.error("Error in sending events to " + host + ":" + port + ".Will retry in the next iteration");
            }
        }
    }

    public void setTcpConnection(TCPConnection tcpConnection) {
        this.tcpConnection = tcpConnection;
    }

}
