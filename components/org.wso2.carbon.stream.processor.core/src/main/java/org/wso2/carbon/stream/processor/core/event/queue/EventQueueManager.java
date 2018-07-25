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

package org.wso2.carbon.stream.processor.core.event.queue;

import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.stream.input.source.Source;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventQueueManager {
    private static EventQueue<QueuedEvent> eventQueue;

    public EventQueueManager() {
    }

    public static EventQueue<QueuedEvent> initializeEventQueue(int queueSize) {
        eventQueue = new EventQueue<>(queueSize);
        return eventQueue;
    }

    public static EventQueue getEventQueue() {
        return eventQueue;
    }

    public void trimAndSendToInputHandler() throws InterruptedException {

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().
                getSiddhiAppMap();

        for (Map.Entry<String, SiddhiAppData> entry : siddhiAppMap.entrySet()) {
            String revision = StreamProcessorDataHolder.getSiddhiManager().
                    getLastRevision(entry.getValue().getSiddhiApp());
            if(revision != null){
                long persistedTimestamp =  Long.parseLong(revision.split("_")[0]);
                Collection<List<Source>> sourceCollection = entry.getValue().getSiddhiAppRuntime().getSources();
                for (List<Source> sources : sourceCollection) {
                    for (Source source : sources) {
                        Iterator<QueuedEvent> itr = eventQueue.getQueue().iterator();
                        while (itr.hasNext()) {
                            QueuedEvent queuedEvent = itr.next();
                            if(queuedEvent.getSourceHandlerElementId().equals(source.getMapper().
                                    getHandler().getElementId()) && persistedTimestamp < queuedEvent.getEvent().getTimestamp()){
                                source.getMapper().getHandler().sendEvent(queuedEvent.getEvent());
                                eventQueue.getQueue().remove(queuedEvent);
                            }
                        }
                    }
                }
            }
        }
        eventQueue.getQueue().clear();
    }

    public void trimQueue(long timestamp){
        eventQueue.trim((QueuedEvent queuedEvent) -> queuedEvent.getEvent().getTimestamp() > timestamp);
    }

    public void addToQueue(QueuedEvent queuedEvent){
        eventQueue.enqueue(queuedEvent);
    }
}
