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
import org.wso2.carbon.stream.processor.core.ha.transport.EventSyncConnection;
import org.wso2.carbon.stream.processor.core.ha.transport.EventSyncConnectionPoolManager;
import org.wso2.carbon.stream.processor.core.ha.util.CoordinationConstants;
import org.wso2.carbon.stream.processor.core.ha.util.HAConstants;
import org.wso2.carbon.stream.processor.core.util.BinaryEventConverter;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;
import org.wso2.siddhi.core.stream.input.source.SourceSyncCallback;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of {@link SourceHandler} used for 2 node minimum HA
 */
public class HACoordinationSourceHandler extends SourceHandler {

    private boolean isActiveNode;
    private long lastProcessedEventTimestamp = 0L;
    private String sourceHandlerElementId;
    private String siddhiAppName;
    private GenericKeyedObjectPool eventSyncConnectionPoolFactory;
    private AtomicLong sequenceIDGenerator;
    private volatile boolean passiveNodeAdded;
    private SourceSyncCallback sourceSyncCallback;

    private static final Logger log = Logger.getLogger(HACoordinationSourceHandler.class);

    public HACoordinationSourceHandler() {
        this.sequenceIDGenerator = EventSyncConnectionPoolManager.getSequenceID();
    }

    @Override
    public void init(String siddhiAppName, SourceSyncCallback sourceSyncCallback, String sourceElementId,
                     StreamDefinition streamDefinition) {
        this.sourceHandlerElementId = sourceElementId;
        this.siddhiAppName = siddhiAppName;
        this.sourceSyncCallback = sourceSyncCallback;
    }

    /**
     * Method that would process events if this is the Active Node.
     *
     * @param event        the event being sent to processing.
     * @param transportSyncProperties transport sync properties which used to sync passive source state
     * @param inputHandler callback that would send events for processing.
     */
    @Override
    public void sendEvent(Event event, String[] transportSyncProperties, InputHandler inputHandler)
            throws InterruptedException {
        if (isActiveNode) {
            lastProcessedEventTimestamp = event.getTimestamp();
            if (passiveNodeAdded) {
                sendEventsToPassiveNode(event, transportSyncProperties);
            }
            inputHandler.send(event);
        }
    }

    /**
     * Method that would process events if this is the Active Node.
     * If Passive Node, events will be buffered during the state syncing state.
     *
     * @param events       the event array being sent to processing.
     * @param transportSyncProperties transport sync properties which used to sync passive source state
     * @param inputHandler callback that would send events for processing.
     */
    @Override
    public void sendEvent(Event[] events, String[] transportSyncProperties, InputHandler inputHandler)
            throws InterruptedException {
        if (isActiveNode) {
            lastProcessedEventTimestamp = events[events.length - 1].getTimestamp();
            if (passiveNodeAdded) {
                sendEventsToPassiveNode(events, transportSyncProperties);
            }
            inputHandler.send(events);
        }
    }

    public void setPassiveNodeAdded(boolean passiveNodeAdded) {
        this.passiveNodeAdded = passiveNodeAdded;
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
    public void restoreState(Map<String, Object> map) {
        //do nothing
    }

    @Override
    public String getElementId() {
        return sourceHandlerElementId;
    }

    private void sendEventsToPassiveNode(Event event, String[] transportSyncProperties) {
        EventSyncConnection eventSyncConnection = getTCPNettyClient();
        ByteBuffer messageBuffer = null;
        if (eventSyncConnection != null) {
            QueuedEvent queuedEvent = new QueuedEvent(siddhiAppName, sourceHandlerElementId, sequenceIDGenerator
                    .incrementAndGet(), event, transportSyncProperties);
            try {
                messageBuffer = BinaryEventConverter.convertToBinaryMessage(new QueuedEvent[]{queuedEvent});
            } catch (IOException e) {
                log.error("Error in converting events to binary message.Hence not sending message to the passive node");
                return;
            }
            if (messageBuffer != null) {
                try {
                    eventSyncConnection.send(HAConstants.CHANNEL_ID_MESSAGE, messageBuffer.array());
                } catch (ConnectionUnavailableException e) {
                    log.error("Error in sending events to the passive node. " + e.getMessage());
                }
            }
            try {
                eventSyncConnectionPoolFactory.returnObject(HAConstants.ACTIVE_NODE_CONNECTION_POOL_ID, eventSyncConnection);
            } catch (Exception e) {
                log.error("Error in returning the tcpClient connection object to the pool. ", e);
            }
        }
    }

    private void sendEventsToPassiveNode(Event[] events, String[] transportSyncProperties) {
        EventSyncConnection eventSyncConnection = getTCPNettyClient();
        ByteBuffer messageBuffer = null;
        if (eventSyncConnection != null) {
            QueuedEvent[] queuedEvents = new QueuedEvent[events.length];
            int i = 0;
            for (Event event : events) {
                QueuedEvent queuedEvent;
                if (i == 0) {
                    queuedEvent = new QueuedEvent(siddhiAppName, sourceHandlerElementId, sequenceIDGenerator
                            .incrementAndGet(), event, transportSyncProperties);
                } else {
                    queuedEvent = new QueuedEvent(siddhiAppName, sourceHandlerElementId, sequenceIDGenerator
                            .incrementAndGet(), event, null);
                }
                queuedEvents[i] = queuedEvent;
                i++;
            }
            try {
                messageBuffer = BinaryEventConverter.convertToBinaryMessage(queuedEvents);
            } catch (IOException e) {
                log.error("Error in converting events to binary message.Hence not sending message to the passive node");
            }
            if (messageBuffer != null) {
                try {
                    eventSyncConnection.send(HAConstants.CHANNEL_ID_MESSAGE, messageBuffer.array());
                } catch (ConnectionUnavailableException e) {
                    log.error("Error in sending events to the passive node. " + e.getMessage());
                }
            }
            try {
                eventSyncConnectionPoolFactory.returnObject(HAConstants.ACTIVE_NODE_CONNECTION_POOL_ID, eventSyncConnection);
            } catch (Exception e) {
                log.error("Error in returning the tcpClient connection object to the pool. ", e);
            }
        }
    }

    private EventSyncConnection getTCPNettyClient() {
        eventSyncConnectionPoolFactory = EventSyncConnectionPoolManager.getConnectionPool();
        EventSyncConnection eventSyncConnection = null;
        try {
            eventSyncConnection = (EventSyncConnection) eventSyncConnectionPoolFactory.borrowObject(HAConstants.ACTIVE_NODE_CONNECTION_POOL_ID);
        } catch (Exception e) {
            log.warn("Error in obtaining a tcp connection to the passive node. Hence not sending events to the " +
                    "passive node. " + e.getMessage());
        }
        return eventSyncConnection;
    }

    public void updateTransportSyncProperties(String[] transportSyncProperties) {
        if (null != sourceSyncCallback) {
            sourceSyncCallback.update(transportSyncProperties);
        }
    }

}
