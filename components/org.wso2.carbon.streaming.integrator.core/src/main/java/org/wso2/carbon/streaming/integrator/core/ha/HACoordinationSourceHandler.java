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

package org.wso2.carbon.streaming.integrator.core.ha;

import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.event.Event;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.input.source.SourceHandler;
import io.siddhi.core.stream.input.source.SourceSyncCallback;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.statistics.ThroughputTracker;
import io.siddhi.query.api.definition.StreamDefinition;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.log4j.Logger;
import org.wso2.carbon.streaming.integrator.core.event.queue.QueuedEvent;
import org.wso2.carbon.streaming.integrator.core.ha.transport.EventSyncConnection;
import org.wso2.carbon.streaming.integrator.core.ha.transport.EventSyncConnectionPoolManager;
import org.wso2.carbon.streaming.integrator.core.ha.util.CoordinationConstants;
import org.wso2.carbon.streaming.integrator.core.ha.util.HAConstants;
import org.wso2.carbon.streaming.integrator.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.streaming.integrator.core.util.BinaryEventConverter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of {@link SourceHandler} used for 2 node minimum HA
 */
public class HACoordinationSourceHandler extends SourceHandler<HACoordinationSourceHandler.SourceState> {

    private boolean isActiveNode;
    private String siddhiAppName;
    private AtomicLong sequenceIDGenerator;
    private volatile boolean passiveNodeAdded;
    private SourceSyncCallback sourceSyncCallback;
    private ThroughputTracker throughputTracker;
    private static final String IGNORING_SOURCE_TYPE = "inMemory";
    private String sourceType;
    private AtomicBoolean isWaiting = new AtomicBoolean(false);
    private AtomicLong lastConnRefusedTimestamp = new AtomicLong(-1);;

    private static final Logger log = Logger.getLogger(HACoordinationSourceHandler.class);

    public HACoordinationSourceHandler(ThroughputTracker throughputTracker, String sourceType) {
        this.sequenceIDGenerator = EventSyncConnectionPoolManager.getSequenceID();
        this.throughputTracker = throughputTracker;
        this.sourceType = sourceType;
    }

    @Override
    public StateFactory<SourceState> init(String siddhiAppName, SourceSyncCallback sourceSyncCallback,
                                          StreamDefinition streamDefinition, SiddhiAppContext siddhiAppContext) {
        this.siddhiAppName = siddhiAppName;
        this.sourceSyncCallback = sourceSyncCallback;
        return SourceState::new;
    }

    /**
     * Method that would process events if this is the Active Node.
     *
     * @param event                   the event being sent to processing.
     * @param transportSyncProperties transport sync properties which used to sync passive source state
     * @param state                   state object
     * @param inputHandler            callback that would send events for processing.
     */
    @Override
    public void sendEvent(Event event, String[] transportSyncProperties, SourceState state, InputHandler inputHandler)
            throws InterruptedException {
        if (isActiveNode) {
            state.lastProcessedEventTimestamp = event.getTimestamp();
            if (passiveNodeAdded && !IGNORING_SOURCE_TYPE.equalsIgnoreCase(sourceType)) {
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
     * @param state                   state object
     * @param inputHandler            callback that would send events for processing.
     */
    @Override
    public void sendEvent(Event[] events, String[] transportSyncProperties, SourceState state,
                          InputHandler inputHandler) throws InterruptedException {
        if (isActiveNode) {
            state.lastProcessedEventTimestamp = events[events.length - 1].getTimestamp();
            if (passiveNodeAdded && !IGNORING_SOURCE_TYPE.equalsIgnoreCase(sourceType)) {
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

    private void sendEventsToPassiveNode(Event event, String[] transportSyncProperties) {
        if (!isWaiting.get() || lastConnRefusedTimestamp.get() + 5000 < System.currentTimeMillis()) {
            isWaiting.set(false);
            GenericKeyedObjectPool objectPool = EventSyncConnectionPoolManager.getConnectionPool();
            if (objectPool != null) {
                EventSyncConnection.Connection connection = null;
                try {
                    connection = (EventSyncConnection.Connection)
                            objectPool.borrowObject(HAConstants.ACTIVE_NODE_CONNECTION_POOL_ID);
                    if (connection != null) {
                        QueuedEvent queuedEvent = new QueuedEvent(siddhiAppName, getId(),
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
                        if (!isWaiting.get() && e.getMessage().contains("Connection refused")) {
                            lastConnRefusedTimestamp.set(System.currentTimeMillis());
                            isWaiting.set(true);
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
        if (!isWaiting.get() || lastConnRefusedTimestamp.get() + 5000 < System.currentTimeMillis()) {
            isWaiting.set(false);
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
                                queuedEvent = new QueuedEvent(siddhiAppName, getId(), sequenceIDGenerator
                                        .incrementAndGet(), event, transportSyncProperties);
                            } else {
                                queuedEvent = new QueuedEvent(siddhiAppName, getId(), sequenceIDGenerator
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
                        if (!isWaiting.get() && e.getMessage().contains("Connection refused")) {
                            lastConnRefusedTimestamp.set(System.currentTimeMillis());
                            isWaiting.set(true);
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

    class SourceState extends State {
        private long lastProcessedEventTimestamp = 0L;

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public Map<String, Object> snapshot() {
            Map<String, Object> state = new HashMap<>();
            state.put(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP, lastProcessedEventTimestamp);
            if (log.isDebugEnabled()) {
                log.debug("Active Node: Saving state of Source Handler with Id " + getId() + " with timestamp "
                        + lastProcessedEventTimestamp);
            }
            return state;
        }

        @Override
        public void restore(Map<String, Object> state) {
            // Do nothing
        }
    }
}