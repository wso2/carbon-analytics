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

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerCallback;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

/**
 * Implementation of {@link SinkHandler} used for 2 node minimum HA
 */
public class HACoordinationSinkHandler extends SinkHandler {
    private static final Logger log = Logger.getLogger(HACoordinationSinkHandler.class);

    private boolean isActiveNode;
    private long lastPublishedEventTimestamp = 0L;
    private String sinkHandlerElementId;


    /**
     * Constructor.
     *
     */
    public HACoordinationSinkHandler() {
    }

    @Override
    public void init(String sinkHandlerElementId, StreamDefinition streamDefinition,
                     SinkHandlerCallback sinkHandlerCallback) {
        this.sinkHandlerElementId = sinkHandlerElementId;
    }

    /**
     * Method that would publish events if this is the Active Node.
     *
     * @param event the event to be published.
     * @param sinkHandlerCallback callback that would publish events.
     */
    @Override
    public void handle(Event event, SinkHandlerCallback sinkHandlerCallback) {
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
     */
    @Override
    public void handle(Event[] events, SinkHandlerCallback sinkHandlerCallback) {
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

    @Override
    public Map<String, Object> currentState() {
        // Do Nothing
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
        // Do Nothing
    }

    @Override
    public String getElementId() {
        return sinkHandlerElementId;
    }

}
