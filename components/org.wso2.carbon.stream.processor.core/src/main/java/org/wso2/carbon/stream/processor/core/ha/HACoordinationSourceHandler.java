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
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.stream.processor.core.event.queue.QueuedEvent;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPConnection;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPConnectionPoolManager;
import org.wso2.carbon.stream.processor.core.ha.util.CoordinationConstants;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

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
    private ActiveNodeEventDispatcher activeNodeEventDispatcher;
    private GenericKeyedObjectPool tcpConnectionPool;
    private QueuedEvent queuedEvent = new QueuedEvent();
    private AtomicLong sequenceID;
    private ClusterCoordinator clusterCoordinator;
    private HAManager haManager;
    private boolean isPassiveNodeAdded;

    private static final Logger log = Logger.getLogger(HACoordinationSourceHandler.class);

    public HACoordinationSourceHandler() {
        this.clusterCoordinator = StreamProcessorDataHolder.getClusterCoordinator();
        this.haManager = StreamProcessorDataHolder.getHAManager();
        this.tcpConnectionPool = TCPConnectionPoolManager.getConnectionPool();
        this.sequenceID = TCPConnectionPoolManager.getSequenceID();
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
            if (isPassiveNodeAdded) {
                sendEventsToPassiveNode(event);
            }
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
            if (isPassiveNodeAdded) {
                sendEventsToPassiveNode(events);
            }
            inputHandler.send(events);
        }
    }

    public void setPassiveNodeAdded(boolean isPassiveNodeAdded) {
        this.isPassiveNodeAdded = isPassiveNodeAdded;
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

    private void sendEventsToPassiveNode(Event event) {
        TCPConnection tcpConnection = getTCPNettyClient();
        if (tcpConnection != null) {
            queuedEvent.setSequenceID(sequenceID.incrementAndGet());
            queuedEvent.setEvent(event);
            queuedEvent.setSiddhiAppName(siddhiAppName);
            queuedEvent.setSourceHandlerElementId(sourceHandlerElementId);
            activeNodeEventDispatcher.setQueuedEvent(queuedEvent);
            activeNodeEventDispatcher.setQueuedEvents(null);
            activeNodeEventDispatcher.setTcpConnection(tcpConnection);
            activeNodeEventDispatcher.sendEventToPassiveNode(queuedEvent);
            try {
                tcpConnectionPool.returnObject("ActiveNode", tcpConnection);
            } catch (Exception e) {
                log.error("Error in returning the tcpClient connection object to the pool. ", e);
            }
        }
    }

    private void sendEventsToPassiveNode(Event[] events) {
        TCPConnection tcpConnection = getTCPNettyClient();
        if (tcpConnection != null) {
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
            activeNodeEventDispatcher.setTcpConnection(tcpConnection);
            activeNodeEventDispatcher.sendEventsToPassiveNode(queuedEvents);
            try {
                tcpConnectionPool.returnObject("ActiveNode", tcpConnection);
            } catch (Exception e) {
                log.error("Error in returning the tcpClient connection object to the pool. ", e);
            }
        }
    }

    private TCPConnection getTCPNettyClient() {
        TCPConnection tcpConnection = null;
        try {
            tcpConnection = (TCPConnection) tcpConnectionPool.borrowObject("ActiveNode");
        } catch (Exception e) {
            log.error("Error in obtaining a tcp connection to the passive node. Hence not sending events to the " +
                    "passive node. " + e.getMessage());
            try {
                tcpConnectionPool.returnObject("ActiveNode", tcpConnection);
                tcpConnectionPool.clear();
            } catch (Exception exception) {
                log.error("Error in returning the tcpClient connection object to the pool. ", exception);
            }
        }
        return tcpConnection;
    }
}
