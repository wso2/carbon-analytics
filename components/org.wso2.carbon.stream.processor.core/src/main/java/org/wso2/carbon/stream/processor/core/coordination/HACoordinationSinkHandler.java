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

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class HACoordinationSinkHandler extends SinkHandler {

    private boolean isActiveNode;
    private long lastPublishedEventTs = 24L;
    private Queue<Event> passiveNodeProcessedEvents = new LinkedBlockingQueue<>(1000);

    public Event handle(Event event) {
        if (isActiveNode) {
            lastPublishedEventTs = event.getTimestamp();
            return event;
        } else {
            passiveNodeProcessedEvents.add(event);
            return null;
        }
    }

    public Event[] handle(Event[] events) {
        if (isActiveNode) {
            lastPublishedEventTs = events[events.length-1].getTimestamp();
            return events;
        } else {
            for (Event event: events) {
                passiveNodeProcessedEvents.add(event);
            }
            return null;
        }
    }

    void setAsActive() {
        //When passive node becomes active, queued events should be published before any other events are processed
        for (Event event: passiveNodeProcessedEvents) {
            this.getSinkMapper().mapAndSend(event);
        }
        this.isActiveNode = true;
    }

    public Map<String, Long> getLastPublishedTs() {
        HashMap<String, Long> sinkIdAndTs = new HashMap<>(1);
        //Since both nodes deploy same siddhi apps, every sink will get the same element Id in both nodes
        sinkIdAndTs.put(this.getSinkElementId(), lastPublishedEventTs);
        return sinkIdAndTs;
    }

    public void updatePassiveNodeEventQueue(long activeLastPublishedTs) {
        if (!passiveNodeProcessedEvents.isEmpty()) {
            while (passiveNodeProcessedEvents.peek().getTimestamp() < activeLastPublishedTs) {
                passiveNodeProcessedEvents.remove();
            }
        }
    }
}
