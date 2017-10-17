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

package org.wso2.carbon.stream.processor.core.coordination;

import org.wso2.carbon.stream.processor.core.coordination.dao.ActiveNodeLastPublishedEventTimeStamp;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerCallback;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementation of {@link SinkHandler} used for 2 node minimum HA
 */
public class HACoordinationSinkHandler extends SinkHandler {

    private boolean isActiveNode;
    private long lastPublishedEventTimestamp = 0L;
    private int queueCapacity;
    private Queue<Event> passiveNodeProcessedEvents;
    private String sinkElementId;

    /**
     * Constructor.
     *
     * @param queueCapacity is the size of the queue that would hold events in the Passive Node
     */
    public HACoordinationSinkHandler(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        passiveNodeProcessedEvents = new LinkedBlockingQueue<>(queueCapacity);
    }

    @Override
    public void init(String sinkElementId, StreamDefinition streamDefinition) {
        this.sinkElementId = sinkElementId;
    }

    /**
     * Method that would publish events if this is the Active Node.
     * Will buffer all events if this is the Passive Node.
     *
     * @param event the event to be published.
     * @param sinkHandlerCallback callback that would publish events.
     */
    @Override
    public void handle(Event event, SinkHandlerCallback sinkHandlerCallback) {
        if (isActiveNode) {
            lastPublishedEventTimestamp = event.getTimestamp();
            sinkHandlerCallback.mapAndSend(event);
        } else {
            synchronized (this) {
                boolean eventBuffered = passiveNodeProcessedEvents.offer(event);
                if (!eventBuffered) { //Handles if the queue is full
                    passiveNodeProcessedEvents.remove();
                    passiveNodeProcessedEvents.add(event);
                }
            }
        }
    }

    /**
     * Method that would publish events if this is the Active Node.
     * Will buffer all events if this is the Passive Node.
     *
     * @param events the event array to be published.
     * @param sinkHandlerCallback callback that would publish events.
     */
    @Override
    public void handle(Event[] events, SinkHandlerCallback sinkHandlerCallback) {
        if (isActiveNode) { // TODO: 10/16/17 Out of order issue.
            lastPublishedEventTimestamp = events[events.length - 1].getTimestamp();
            sinkHandlerCallback.mapAndSend(events);
        } else {
            synchronized (this) {
                int sizeAfterUpdate = passiveNodeProcessedEvents.size() + events.length;
                if (sizeAfterUpdate >= queueCapacity) {
                    for (int i = queueCapacity; i < sizeAfterUpdate; i++) {
                        passiveNodeProcessedEvents.remove();
                    }
                }
                for (Event event : events) {
                    passiveNodeProcessedEvents.add(event);
                }
            }
        }
    }

    /**
     * Method to change the sink handler to Active state so that publishing of events is commenced.
     * The currently buffered events will be published since it holds the events that the active node may not
     * have published yet. This might lead to event duplication but guarantees no events are dropped.
     * Will only be called when this node is the Passive Node.
     */
    public void setAsActive() {
        //When passive node becomes active, queued events should be published before any other events are processed
        this.isActiveNode = true;
        for (Event event : passiveNodeProcessedEvents) {
            handle(event);
        }
        passiveNodeProcessedEvents.clear();
    }

    /**
     * Get the timestamp of the last event published from the given sink.
     * Will only be called when this node is the Active node and publishing events.
     *
     * @return Object that holds the Sink Element Id and timestamp of last published event.
     */
    public ActiveNodeLastPublishedEventTimeStamp getActiveNodeLastPublishedTimestamp() {
        //Since both nodes deploy same siddhi apps, every sink will get the same element Id in both nodes
        return new ActiveNodeLastPublishedEventTimeStamp(sinkElementId, lastPublishedEventTimestamp);
    }

    /**
     * Method that removes the events from the queue that the active node has already published
     *
     * @param activeLastPublishedTimestamp timestamp of the last event the active node published from the given sink
     */
    public void trimPassiveNodeEventQueue(long activeLastPublishedTimestamp) {
        while (passiveNodeProcessedEvents.peek() != null &&
                passiveNodeProcessedEvents.peek().getTimestamp() <= activeLastPublishedTimestamp) {
            passiveNodeProcessedEvents.remove();
        }
    }

    public Queue<Event> getPassiveNodeProcessedEvents() {
        return passiveNodeProcessedEvents;
    }
}
