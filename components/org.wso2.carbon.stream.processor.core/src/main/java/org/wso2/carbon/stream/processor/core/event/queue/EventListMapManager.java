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

import org.wso2.carbon.stream.processor.core.ha.util.HAConstants;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.stream.input.source.Source;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class EventListMapManager {
    private static ConcurrentSkipListMap<Long,QueuedEvent> eventListMap;

    public EventListMapManager() {
    }

    public static ConcurrentSkipListMap<Long,QueuedEvent> initializeEventListMap() {
        eventListMap = new ConcurrentSkipListMap<Long, QueuedEvent>();
        return eventListMap;
    }

    public void trimAndSendToInputHandler() throws InterruptedException {

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().
                getSiddhiAppMap();

        for (Map.Entry<String, SiddhiAppData> entry : siddhiAppMap.entrySet()) {
            String revision = StreamProcessorDataHolder.getSiddhiManager().
                    getLastRevision(entry.getKey());
            if(revision != null){
                long persistedTimestamp =  Long.parseLong(revision.split(
                        HAConstants.REVISION_SPLIT_DELIMITER)[0].trim());
                Collection<List<Source>> sourceCollection = entry.getValue().getSiddhiAppRuntime().getSources();
                for (List<Source> sources : sourceCollection) {
                    for (Source source : sources) {
                        for(Map.Entry<Long,QueuedEvent> listMapValue : eventListMap.entrySet()) {
                            long key = listMapValue.getKey();
                            QueuedEvent queuedEvent = listMapValue.getValue();
                            if(queuedEvent.getSourceHandlerElementId().equals(source.getMapper().
                                    getHandler().getElementId()) &&
                                    persistedTimestamp < queuedEvent.getEvent().getTimestamp()){
                                source.getMapper().getHandler().sendEvent(queuedEvent.getEvent());
                                eventListMap.remove(key);
                            }
                        }
                    }
                }
            }
        }
        eventListMap.clear();
    }

    public void trimQueue(String[] persistedAppDetails){
        if (eventListMap.size() != 0){
            for(String appDetail : persistedAppDetails){
                String[] details = appDetail.split(HAConstants.PERSISTED_APP_SPLIT_DELIMITER);
                String appName = details[1].trim();
                long timestamp = Long.valueOf(details[0].trim());
                for(Map.Entry<Long,QueuedEvent> listMapValue : eventListMap.entrySet()) {
                    long key = listMapValue.getKey();
                    QueuedEvent queuedEvent = listMapValue.getValue();
                    if(queuedEvent.getSiddhiAppName().equals(appName) &&
                            timestamp >= queuedEvent.getEvent().getTimestamp()){
                        eventListMap.remove(key);

                    }
                }
            }
        }
    }

    public void addToEventListMap(long sequenceNum, QueuedEvent queuedEvent){
        eventListMap.put(sequenceNum, queuedEvent);
    }
}
