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

import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.ha.tcp.SiddhiEventConverter;
import org.wso2.carbon.stream.processor.core.ha.util.HAConstants;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.util.BinaryMessageConverterUtil;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.source.Source;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class EventListMapManager {
    private static ConcurrentSkipListMap<Long,QueuedEvent> eventListMap;
    private static long lastControlMessageSequenceId;
    private static final Logger log = Logger.getLogger(EventListMapManager.class);

    public EventListMapManager() {
    }

    public static ConcurrentSkipListMap<Long,QueuedEvent> initializeEventListMap() {
        eventListMap = new ConcurrentSkipListMap<Long, QueuedEvent>();
        return eventListMap;
    }

    public synchronized void parseControlMessage(byte[] controlMessageContentByteArray) {
        String message = new String(controlMessageContentByteArray);
        if (message != null && !message.isEmpty()) {
            message = message.replace ("[", "");
            message = message.replace ("]", "");
            String[] persistedApps = message.split(",");
            String lastPersistedAppInfo = persistedApps[persistedApps.length - 1];
            lastControlMessageSequenceId = Long.valueOf(lastPersistedAppInfo.split(HAConstants.
                    PERSISTED_APP_SPLIT_DELIMITER)[0].trim());
            this.trimQueue(persistedApps);
        }
    }

    public synchronized void parseMessage(byte[] eventContentByteArray) {
        try {
            ByteBuffer eventContent = ByteBuffer.wrap(eventContentByteArray);
            int noOfEvents = eventContent.getInt();
            QueuedEvent queuedEvent;
            Event[] events = new Event[noOfEvents];
            for (int i = 0; i < noOfEvents; i++) {
                String sourceHandlerElementId;
                String siddhiAppName;
                long sequenceID = eventContent.getLong();
                if (sequenceID > lastControlMessageSequenceId) {
                    int stringSize = eventContent.getInt();
                    if (stringSize == 0) {
                        sourceHandlerElementId = null;
                    } else {
                        sourceHandlerElementId = BinaryMessageConverterUtil.getString(eventContent, stringSize);
                    }

                    int appSize = eventContent.getInt();
                    if (appSize == 0) {
                        siddhiAppName = null;
                    } else {
                        siddhiAppName = BinaryMessageConverterUtil.getString(eventContent, appSize);
                    }

                    String attributes;
                    stringSize = eventContent.getInt();
                    if (stringSize == 0) {
                        attributes = null;
                    } else {
                        attributes = BinaryMessageConverterUtil.getString(eventContent, stringSize);
                    }
                    String[] attributeTypes = attributes.substring(1, attributes.length() - 1).split(", ");
                    events[i] = SiddhiEventConverter.getEvent(eventContent, attributeTypes);
                    queuedEvent = new QueuedEvent(siddhiAppName, sourceHandlerElementId, sequenceID, events[i]);
                    this.addToEventListMap(sequenceID, queuedEvent);
                }

            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error when converting bytes " + e.getMessage(), e);
        }
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
                String appName = details[2].trim();
                long seqId = Long.valueOf(details[0].trim());
                for(Map.Entry<Long,QueuedEvent> listMapValue : eventListMap.entrySet()) {
                    long key = listMapValue.getKey();
                    QueuedEvent queuedEvent = listMapValue.getValue();
                    if(queuedEvent.getSiddhiAppName().equals(appName) &&
                            seqId > queuedEvent.getSequenceID()){
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
