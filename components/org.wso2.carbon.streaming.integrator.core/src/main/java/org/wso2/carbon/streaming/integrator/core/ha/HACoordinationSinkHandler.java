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

import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;

import io.siddhi.core.event.Event;
import io.siddhi.core.stream.output.sink.SinkHandler;
import io.siddhi.core.stream.output.sink.SinkHandlerCallback;
import io.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

/**
 * Implementation of {@link SinkHandler} used for 2 node minimum HA
 */
public class HACoordinationSinkHandler extends SinkHandler<HACoordinationSinkHandler.SinkState> {

    private boolean isActiveNode;
    private long lastPublishedEventTimestamp = 0L;
//    private String sinkHandlerElementId;

    @Override
    public StateFactory<SinkState> init(StreamDefinition streamDefinition, SinkHandlerCallback sinkHandlerCallback) {
//        this.sinkHandlerElementId = sinkHandlerElementId;
        return SinkState::new;
    }

    /**
     * Method that would publish events if this is the Active Node.
     *
     * @param event the event to be published.
     * @param sinkHandlerCallback callback that would publish events.
     * @param state state object
     */
    @Override
    public void handle(Event event, SinkHandlerCallback sinkHandlerCallback, SinkState state) {
        if (isActiveNode) {
            lastPublishedEventTimestamp = event.getTimestamp();
            sinkHandlerCallback.mapAndSend(event);
        }
    }

    /**
     * Method that would publish events if this is the Active Node.
     *
     * @param events the event array to be published.
     * @param sinkHandlerCallback callback that would publish events.
     * @param state state object
     */
    @Override
    public void handle(Event[] events, SinkHandlerCallback sinkHandlerCallback, SinkState state) {
        if (isActiveNode) {
            lastPublishedEventTimestamp = events[events.length - 1].getTimestamp();
            sinkHandlerCallback.mapAndSend(events);
        }
    }

    /**
     * Method to change the sink handler to Active state so that publishing of events is commenced.
     */
    public void setAsActive() {
        this.isActiveNode = true;
    }

    /**
     * Method to change the sink handler to Passive so that events will not be published.
     */
    public void setAsPassive() {
        this.isActiveNode = false;
    }

    /**
     * Get the timestamp of the last event published from the given sink.
     * Will only be called when this node is the Active node and publishing events.
     *
     * @return Object that holds the Sink Handler Element Id and timestamp of last published event.
     */
    public long getActiveNodeLastPublishedTimestamp() {
        //Since both nodes deploy same siddhi apps, every sink handler will get the same element Id in both nodes
        return lastPublishedEventTimestamp;
    }

//    @Override
//    public String getElementId() {
//        return sinkHandlerElementId;
//    }

    class SinkState extends State {
        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public Map<String, Object> snapshot() {
            return null;
        }

        @Override
        public void restore(Map<String, Object> state) {

        }
    }
}