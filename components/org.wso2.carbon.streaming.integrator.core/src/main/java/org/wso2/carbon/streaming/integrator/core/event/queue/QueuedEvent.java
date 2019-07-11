/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.streaming.integrator.core.event.queue;

import io.siddhi.core.event.Event;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class QueuedEvent {
    private String sourceHandlerElementId;
    private long sequenceID;
    private Event event;
    private long timestamp;
    private String siddhiAppName;
    private String[] transportSyncProperties;

    @SuppressWarnings("EI_EXPOSE_REP2")
    public QueuedEvent(String siddhiAppName, String sourceHandlerElementId, long sequenceID, Event event,
                       String[] transportSyncProperties) {
        this.sourceHandlerElementId = sourceHandlerElementId;
        this.siddhiAppName = siddhiAppName;
        this.sequenceID = sequenceID;
        this.event = event;
        this.transportSyncProperties = transportSyncProperties;
    }

    public long getSequenceID() {
        return sequenceID;
    }

    public void setSequenceID(long sequenceID) {
        this.sequenceID = sequenceID;
    }

    public String getSourceHandlerElementId() {
        return sourceHandlerElementId;
    }

    public void setSourceHandlerElementId(String sourceHandlerElementId) {
        this.sourceHandlerElementId = sourceHandlerElementId;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    public void setSiddhiAppName(String siddhiAppName) {
        this.siddhiAppName = siddhiAppName;
    }

    @SuppressWarnings("EI_EXPOSE_REP")
    public String[] getTransportSyncProperties() {
        return transportSyncProperties;
    }
}
