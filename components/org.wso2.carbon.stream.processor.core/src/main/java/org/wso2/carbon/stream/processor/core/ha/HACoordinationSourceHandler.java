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
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.util.BinaryEventConverter;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;
import org.wso2.siddhi.core.stream.input.source.SourceSyncCallback;
import org.wso2.siddhi.core.util.statistics.ThroughputTracker;
import org.wso2.siddhi.core.util.statistics.metrics.Level;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of {@link SourceHandler} used for 2 node minimum HA
 */
public class HACoordinationSourceHandler extends SourceHandler {

    private boolean isActiveNode;
    private boolean playBack;
    private long lastProcessedEventTimestamp = 0L;
    private String sourceHandlerElementId;
    private String siddhiAppName;
    private AtomicLong sequenceIDGenerator;
    private volatile boolean passiveNodeAdded;
    private SourceSyncCallback sourceSyncCallback;
    private ThroughputTracker throughputTracker;
    private static final String IGNORING_SOURCE_TYPE = "inMemory";
    private String sourceType;
    private AtomicBoolean isWaitingForPassiveNode = new AtomicBoolean(false);
    private AtomicLong lastConnRefusedTimestamp = new AtomicLong(-1);

    private static final Logger log = Logger.getLogger(HACoordinationSourceHandler.class);

    public HACoordinationSourceHandler(ThroughputTracker throughputTracker, String sourceType) {
        this.sequenceIDGenerator = EventSyncConnectionPoolManager.getSequenceID();
        this.throughputTracker = throughputTracker;
        this.sourceType = sourceType;
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
     * @param event                   the event being sent to processing.
     * @param transportSyncProperties transport sync properties which used to sync passive source state
     * @param inputHandler            callback that would send events for processing.
     */
    @Override
    public void sendEvent(Event event, String[] transportSyncProperties, InputHandler inputHandler)
            throws InterruptedException {
        if (isActiveNode) {
            lastProcessedEventTimestamp = event.getTimestamp();
            if (!playBack && passiveNodeAdded && !IGNORING_SOURCE_TYPE.equalsIgnoreCase(sourceType)) {
                sendEventsToPassiveNode(event, transportSyncProperties);
            }
            inputHandler.send(event);
        }
    }

    /**
     * Method that would process events if this is the Active Node.
     * If Passive Node, events will be buffered during the state syncing state.
     *
     * @param events                  the event array being sent to processing.
     * @param transportSyncProperties transport sync properties which used to sync passive source state
     * @param inputHandler            callback that would send events for processing.
     */
    @Override
    public void sendEvent(Event[] events, String[] transportSyncProperties, InputHandler inputHandler)
            throws InterruptedException {
        if (isActiveNode) {
            lastProcessedEventTimestamp = events[events.length - 1].getTimestamp();
            if (passiveNodeAdded && !IGNORING_SOURCE_TYPE.equalsIgnoreCase(sourceType)) {
                sendEventsToPassiveNode(events, transportSyncProperties);
            }
            inputHandler.send(events);
        }
    }

    public void setPassiveNodeAdded(boolean passiveNodeAdded) {
        this.passiveNodeAdded = passiveNodeAdded;
        setIsWaitingForPassiveNode(passiveNodeAdded);
    }

    public void setPlayBack(boolean playBack) {
        this.playBack = playBack;
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
        if (!isWaitingForPassiveNode.get() || lastConnRefusedTimestamp.get() + 5000 < System.currentTimeMillis()) {
            GenericKeyedObjectPool objectPool = EventSyncConnectionPoolManager.getConnectionPool();
            if (objectPool != null) {
                EventSyncConnection.Connection connection = null;
                try {
                    connection = (EventSyncConnection.Connection)
                            objectPool.borrowObject(HAConstants.ACTIVE_NODE_CONNECTION_POOL_ID);
                    if (connection != null) {
                        QueuedEvent queuedEvent = new QueuedEvent(siddhiAppName, sourceHandlerElementId,
                                sequenceIDGenerator.incrementAndGet(), event, transportSyncProperties);
                        ByteBuffer messageBuffer = null;
                        try {
                            messageBuffer = BinaryEventConverter.convertToBinaryMessage(new QueuedEvent[]{queuedEvent});
                        } catch (IOException e) {
                            log.error("Error in converting events to binary message. " +
                                    "Hence not sending message to the passive node", e);
                            return;
                        }
                        if (messageBuffer != null) {
                            try {
                                connection.send(HAConstants.CHANNEL_ID_MESSAGE, messageBuffer.array());
                                if (throughputTracker != null && StreamProcessorDataHolder.isStatisticsEnabled()) {
                                    throughputTracker.eventIn();
                                }
                            } catch (ConnectionUnavailableException e) {
                                log.error("Connection unavailable to sending events to the passive node. " +
                                        e.getMessage(), e);
                            }
                        }
                    }
                } catch (Exception e) {
                    synchronized (this) {
                        log.warn("Error in sending events to the passive node." +
                                " Event syncing will start to retry again in 5 seconds. " + e.getMessage(), e);
                        if (e.getMessage().contains("Connection refused")) {
                            lastConnRefusedTimestamp.set(System.currentTimeMillis());
                            isWaitingForPassiveNode.set(true);
                        }
                    }
                } finally {
                    if (connection != null) {
                        try {
                            objectPool.returnObject(HAConstants.ACTIVE_NODE_CONNECTION_POOL_ID, connection);
                        } catch (Exception e) {
                            log.error("Error in returning the tcpClient connection object to the pool. " +
                                    e.getMessage(), e);
                        }
                    }
                }
            } else {
                log.error("Error in obtaining connection pool to send events to passive node, " +
                        "hence dropping the events.");
            }
        }
    }

    private void sendEventsToPassiveNode(Event[] events, String[] transportSyncProperties) {
        log.error("isWaitingForPassiveNode? " + isWaitingForPassiveNode.get());
        if (!isWaitingForPassiveNode.get() || lastConnRefusedTimestamp.get() + 5000 < System.currentTimeMillis()) {
            GenericKeyedObjectPool objectPool = EventSyncConnectionPoolManager.getConnectionPool();
            if (objectPool != null) {
                EventSyncConnection.Connection connection = null;
                try {
                    connection = (EventSyncConnection.Connection)
                            objectPool.borrowObject(HAConstants.ACTIVE_NODE_CONNECTION_POOL_ID);
                    if (connection != null) {
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
                        ByteBuffer messageBuffer = null;
                        try {
                            messageBuffer = BinaryEventConverter.convertToBinaryMessage(queuedEvents);
                        } catch (IOException e) {
                            log.error("Error in converting events to binary message. " +
                                    "Hence not sending message to the passive node", e);
                            return;
                        }
                        if (messageBuffer != null) {
                            try {
                                connection.send(HAConstants.CHANNEL_ID_MESSAGE, messageBuffer.array());
                                if (throughputTracker != null && StreamProcessorDataHolder.isStatisticsEnabled()) {
                                    throughputTracker.eventsIn(events.length);
                                }
                            } catch (ConnectionUnavailableException e) {
                                log.error("Connection unavailable to sending events to the passive node. " +
                                        e.getMessage(), e);
                            }
                        }
                    }
                } catch (Exception e) {
                    synchronized (this) {
                        log.warn("Error in sending events to the passive node." +
                                " Event syncing will start to retry again in 5 seconds. " + e.getMessage(), e);
                        if (e.getMessage().contains("Connection refused")) {
                            lastConnRefusedTimestamp.set(System.currentTimeMillis());
                            isWaitingForPassiveNode.set(true);
                        }
                    }
                } finally {
                    if (connection != null) {
                        try {
                            objectPool.returnObject(HAConstants.ACTIVE_NODE_CONNECTION_POOL_ID, connection);
                        } catch (Exception e) {
                            log.error("Error in returning the tcpClient connection object to the pool. " +
                                    e.getMessage(), e);
                        }
                    }
                }
            } else {
                log.error("Error in obtaining connection pool to send events to passive node, " +
                        "hence dropping the events.");
            }
        }
    }

    public void updateTransportSyncProperties(String[] transportSyncProperties) {
        if (null != sourceSyncCallback) {
            sourceSyncCallback.update(transportSyncProperties);
        }
    }

    public void setIsWaitingForPassiveNode(boolean passiveNodeAdded) {
        this.isWaitingForPassiveNode.set(!passiveNodeAdded);
    }
}
