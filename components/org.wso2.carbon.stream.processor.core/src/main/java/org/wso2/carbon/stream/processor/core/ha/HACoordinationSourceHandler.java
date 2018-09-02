/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.stream.processor.core.ha;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.event.queue.QueuedEvent;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPNettyClient;
import org.wso2.carbon.stream.processor.core.ha.util.CoordinationConstants;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of {@link SourceHandler} used for 2 node minimum HA
 */
public class HACoordinationSourceHandler extends SourceHandler {

    private boolean isActiveNode;
    private long lastProcessedEventTimestamp = 0L;
    private String sourceHandlerElementId;
    private String siddhiAppName;
    private ActiveNodeEventDispatcher activeNodeEventDispatcher;
    private GenericKeyedObjectPool tcpConnectionPool;
    private QueuedEvent queuedEvent = new QueuedEvent();
    private AtomicLong sequenceID;

    private static final Logger log = Logger.getLogger(HACoordinationSourceHandler.class);

    public HACoordinationSourceHandler(GenericKeyedObjectPool tcpConnectionPool, AtomicLong sequenceID) {
        this.tcpConnectionPool = tcpConnectionPool;
        this.sequenceID = sequenceID;
        activeNodeEventDispatcher = new ActiveNodeEventDispatcher();
    }

    @Override
    public void init(String siddhiAppName, String sourceElementId, StreamDefinition streamDefinition) {
        this.sourceHandlerElementId = sourceElementId;
        this.siddhiAppName = siddhiAppName;
    }

    /**
     * Method that would process events if this is the Active Node.
     *
     * @param event        the event being sent to processing.
     * @param inputHandler callback that would send events for processing.
     */
    @Override
    public void sendEvent(Event event, InputHandler inputHandler) throws InterruptedException {
        if (isActiveNode) {
            lastProcessedEventTimestamp = event.getTimestamp();
            sendEventsToPassiveNode(event);
            inputHandler.send(event);
        }
    }

    /**
     * Method that would process events if this is the Active Node.
     * If Passive Node, events will be buffered during the state syncing state.
     *
     * @param events       the event array being sent to processing.
     * @param inputHandler callback that would send events for processing.
     */
    @Override
    public void sendEvent(Event[] events, InputHandler inputHandler) throws InterruptedException {
        if (isActiveNode) {
            lastProcessedEventTimestamp = events[events.length - 1].getTimestamp();
            sendEventsToPassiveNode(events);
            inputHandler.send(events);
        }
    }

    /**
     * Method to change the source handler to Active state so that timestamp of event being processed is saved.
     */
    public void setAsActive() {
        isActiveNode = true;
    }

    /**
     * Method to change the source handler to Passive state so that events will not be processed.
     */
    public void setAsPassive() {
        isActiveNode = false;
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> currentState = new HashMap<>();
        currentState.put(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP, lastProcessedEventTimestamp);
        if (log.isDebugEnabled()) {
            log.debug("Active Node: Saving state of Source Handler with Id " + getElementId() + " with timestamp "
                    + lastProcessedEventTimestamp);
        }
        return currentState;
    }

    @Override
    public void restoreState(Map<String, Object> map) {//todo
//        if (map != null) {
//            if (map.get(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP) != null) {
//                processBufferedEvents((Long) map.get(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP));
//            }
//        }
    }

    @Override
    public String getElementId() {
        return sourceHandlerElementId;
    }

    private void sendEventsToPassiveNode(Event event) {
        queuedEvent.setSequenceID(sequenceID.incrementAndGet());
        queuedEvent.setEvent(event);
        queuedEvent.setSiddhiAppName(siddhiAppName);
        queuedEvent.setSourceHandlerElementId(sourceHandlerElementId);
        activeNodeEventDispatcher.setQueuedEvent(queuedEvent);
        activeNodeEventDispatcher.setQueuedEvents(null);
        TCPNettyClient tcpNettyClient = null;
        try {
            tcpNettyClient = (TCPNettyClient) tcpConnectionPool.borrowObject("ActiveNode");
        } catch (Exception e) {
            e.printStackTrace();
        }
        activeNodeEventDispatcher.setTcpNettyClient(tcpNettyClient);
        activeNodeEventDispatcher.sendEventToPassiveNode(queuedEvent);
        try {
            tcpConnectionPool.returnObject("ActiveNode", tcpNettyClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEventsToPassiveNode(Event[] events) {
        QueuedEvent[] queuedEvents = new QueuedEvent[events.length];
        int i = 0;
        for (Event event : events) {
            queuedEvent.setSequenceID(sequenceID.incrementAndGet());
            queuedEvent.setEvent(event);
            queuedEvent.setSiddhiAppName(siddhiAppName);
            queuedEvent.setSourceHandlerElementId(sourceHandlerElementId);
            queuedEvents[i] = queuedEvent;
            i++;
        }
        activeNodeEventDispatcher.setQueuedEvent(null);
        activeNodeEventDispatcher.setQueuedEvents(queuedEvents);
        TCPNettyClient tcpNettyClient = null;
        try {
            tcpNettyClient = (TCPNettyClient) tcpConnectionPool.borrowObject("ActiveNode");
        } catch (Exception e) {
            e.printStackTrace();
        }
        activeNodeEventDispatcher.setTcpNettyClient(tcpNettyClient);
        activeNodeEventDispatcher.sendEventsToPassiveNode(queuedEvents);
        try {
            tcpConnectionPool.returnObject("ActiveNode", tcpNettyClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
