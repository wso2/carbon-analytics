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

import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.ha.util.CoordinationConstants;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementation of {@link SourceHandler} used for 2 node minimum HA
 */
public class HACoordinationSourceHandler extends SourceHandler {

    private boolean isActiveNode;
    private boolean collectEvents;
    private long lastProcessedEventTimestamp = 0L;
    private Queue<Event> passiveNodeBufferedEvents;
    private String sourceHandlerElementId;

    private final int queueCapacity;
    private static final Logger log = Logger.getLogger(HACoordinationSourceHandler.class);

    public HACoordinationSourceHandler(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        passiveNodeBufferedEvents = new LinkedBlockingQueue<>(queueCapacity);
    }


    @Override
    public void init(String sourceElementId, StreamDefinition streamDefinition) {
        this.sourceHandlerElementId = sourceElementId;
    }

    /**
     * Method that would process events if this is the Active Node.
     * If Passive Node, events will be buffered during the state syncing state.
     *
     * @param event        the event being sent to processing.
     * @param inputHandler callback that would send events for processing.
     */
    @Override
    public void sendEvent(Event event, InputHandler inputHandler) throws InterruptedException {
        if (isActiveNode) {
            lastProcessedEventTimestamp = event.getTimestamp();
            inputHandler.send(event);
        } else {
            synchronized (this) {
                if (collectEvents) {
                    boolean eventBuffered = passiveNodeBufferedEvents.offer(event);
                    if (!eventBuffered) {
                        passiveNodeBufferedEvents.remove();
                        passiveNodeBufferedEvents.add(event);
                    }
                } else {
                    inputHandler.send(event);
                }
            }
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
            inputHandler.send(events);
        } else {
            if (collectEvents) {
                synchronized (this) {
                    int sizeAfterUpdate = passiveNodeBufferedEvents.size() + events.length;
                    if (sizeAfterUpdate >= queueCapacity) {
                        for (int i = queueCapacity; i < sizeAfterUpdate; i++) {
                            passiveNodeBufferedEvents.remove();
                        }
                    }
                    for (Event event : events) {
                        passiveNodeBufferedEvents.add(event);
                    }
                }
            } else {
                inputHandler.send(events);
            }
        }
    }

    /**
     * This method will trim the passive nodes buffer and and send the remaining events for processing
     *
     * @param activeLastProcessedEventTimestamp the point to which the passive nodes buffer should be trimmed
     */
    public void processBufferedEvents(long activeLastProcessedEventTimestamp) {

        while (passiveNodeBufferedEvents.peek() != null &&
                passiveNodeBufferedEvents.peek().getTimestamp() <= activeLastProcessedEventTimestamp) {
            passiveNodeBufferedEvents.remove();
        }
        while (passiveNodeBufferedEvents.peek() != null) {
            try {
                getInputHandler().send(passiveNodeBufferedEvents.poll());
            } catch (InterruptedException e) {
                log.error("Error esending Passive Node Events after State Sync. ", e);
            }
        }
        collectEvents(false);
        if (log.isDebugEnabled()) {
            log.debug("Setting Source Handler with ID " + sourceHandlerElementId + " to stop collecting events" +
                    " in buffer");
        }

        //Recheck if queue is not empty due to other thread updating the queue and send events
        while (passiveNodeBufferedEvents.peek() != null) {
            try {
                getInputHandler().send(passiveNodeBufferedEvents.poll());
            } catch (InterruptedException e) {
                log.error("Error Resending Passive Node Events after State Sync. ", e);
            }
        }
    }

    /**
     * Method to change the source handler to Active state so that timestamp of event being processed is saved.
     */
    public void setAsActive() {
        isActiveNode = true;
    }

    /**
     * Will indicate the passive node to start collecting events
     *
     * @param collectEvents should be true only when passive node requests the state of active node.
     * Since state syncing takes time events should be collected to ensure that no events are lost during
     * the state sync
     */
    public void collectEvents(boolean collectEvents) {
        this.collectEvents = collectEvents;
    }

    public Queue<Event> getPassiveNodeBufferedEvents() {
        return passiveNodeBufferedEvents;
    }

    @Override
    public Map<String, Object> currentState() {
        HashMap<String, Object> currentState = new HashMap<>();
        currentState.put(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP, lastProcessedEventTimestamp);
        if (log.isDebugEnabled()) {
            log.debug("Active Node: Saving state of Source Handler with Id " + getElementId() + " with timestamp "
                    + lastProcessedEventTimestamp);
        }
        return currentState;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
        if (map != null) {
            if (map.get(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP) != null) {
                processBufferedEvents((Long) map.get(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMP));
            }
        }
    }

    @Override
    public String getElementId() {
        return sourceHandlerElementId;
    }
}
