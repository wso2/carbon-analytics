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
import org.wso2.carbon.sp.metrics.core.SPThroughputMetric;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSourceHandler;
import org.wso2.carbon.stream.processor.core.ha.exception.InvalidByteMessageException;
import org.wso2.carbon.stream.processor.core.ha.tcp.SiddhiEventConverter;
import org.wso2.carbon.stream.processor.core.ha.util.HAConstants;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.carbon.stream.processor.core.util.BinaryMessageConverterUtil;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.statistics.metrics.Level;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class EventListMapManager {
    private static ConcurrentSkipListMap<Long,QueuedEvent> eventListMap;
    private static Map<String,Long> perAppLastControlMessageSequenceNumberList = new HashMap<>();
    private static final Logger log = Logger.getLogger(EventListMapManager.class);
    private static long startTime = new Date().getTime();;
    private static long endTime;
    private static int count = 0;
    private static final int TPS_EVENT_THRESHOLD = 100000;
    private SPThroughputMetric throughputTracker = null;

    public EventListMapManager() {
        if (throughputTracker == null) {
            throughputTracker =
                    (SPThroughputMetric) StreamProcessorDataHolder.getStatisticsConfiguration().getFactory().
                            createThroughputTracker(SiddhiAppProcessorConstants.HA_METRICS_PREFIX +
                                            SiddhiConstants.METRIC_DELIMITER +
                                            SiddhiAppProcessorConstants.HA_METRICS_RECEIVING_THROUGHPUT,
                                    StreamProcessorDataHolder.getStatisticsManager());
        }
    }

    public static void initializeEventListMap() {
        eventListMap = new ConcurrentSkipListMap<Long, QueuedEvent>();
    }

    public void parseControlMessage(byte[] controlMessageContentByteArray) throws UnsupportedEncodingException {
        if (log.isDebugEnabled()) {
            log.debug("Received a control message");
        }
        String message = null;
        message = new String(controlMessageContentByteArray, HAConstants.DEFAULT_CHARSET);
        if (!message.isEmpty()) {
            message = message.replace ("[", "");
            message = message.replace ("]", "");
            String[] persistedApps = message.split(",");
            this.trimQueue(persistedApps);
        }
    }

    public void parseMessage(byte[] eventContentByteArray) {
        try {
            ByteBuffer eventContent = ByteBuffer.wrap(eventContentByteArray);
            int noOfEvents = eventContent.getInt();
            if (throughputTracker != null && StreamProcessorDataHolder.isStatisticsEnabled()) {
                throughputTracker.eventsIn(noOfEvents);
            }
            QueuedEvent queuedEvent;
            Event[] events = new Event[noOfEvents];
            for (int i = 0; i < noOfEvents; i++) {
                String sourceHandlerElementId;
                String siddhiAppName;
                String[] transportSyncProperties = null;
                long sequenceID = eventContent.getLong();
                int sourceHandlerLength = eventContent.getInt();
                if (sourceHandlerLength == 0) {
                    throw new InvalidByteMessageException("Invalid sourceHandlerLength size = 0");
                } else {
                    sourceHandlerElementId = BinaryMessageConverterUtil.getString(eventContent, sourceHandlerLength);
                }

                int appNameLength = eventContent.getInt();
                if (appNameLength == 0) {
                    throw new InvalidByteMessageException("Invalid appNameLength size = 0");
                } else {
                    siddhiAppName = BinaryMessageConverterUtil.getString(eventContent, appNameLength);
                }
                int transportSyncPropertiesBytes = eventContent.getInt();
                if (transportSyncPropertiesBytes != 0) {
                    int transportSyncPropertiesSize = eventContent.getInt();
                    if (transportSyncPropertiesSize != 0) {
                        transportSyncProperties = new String[transportSyncPropertiesSize];
                        int byteReadLength = 0;
                        int propertiesLength = 0;
                        while (transportSyncPropertiesBytes != byteReadLength) {
                            int readLength = eventContent.getInt();
                            byteReadLength += readLength;
                            transportSyncProperties[propertiesLength] = BinaryMessageConverterUtil.
                                    getString(eventContent, readLength);
                            propertiesLength ++;
                        }
                    }
                }
                long lastSequenceIdForApp = -1;

                if (perAppLastControlMessageSequenceNumberList.size() != 0) {
                    if (perAppLastControlMessageSequenceNumberList.get(siddhiAppName) != null) {
                        lastSequenceIdForApp = perAppLastControlMessageSequenceNumberList.get(siddhiAppName);
                    }
                }
                synchronized (this) {
                    //we need this block synchronized to ensure if last remembered siddhi app id is smaller than the
                    // event id then add it to the tree map synchronously, else older event may add to the tree map
                    if (sequenceID > lastSequenceIdForApp) {
                        String attributes;
                        int attributeLength = eventContent.getInt();
                        if (attributeLength == 0) {
                            throw new InvalidByteMessageException("Invalid attributeLength size = 0");
                        } else {
                            attributes = BinaryMessageConverterUtil.getString(eventContent, attributeLength);
                        }
                        String[] attributeTypes = attributes.substring(1, attributes.length() - 1).split(", ");
                        events[i] = SiddhiEventConverter.getEvent(eventContent, attributeTypes);
                        queuedEvent = new QueuedEvent(siddhiAppName, sourceHandlerElementId, sequenceID, events[i],
                                transportSyncProperties);
                        this.addToEventListMap(sequenceID, queuedEvent);
                    }
                    if (log.isDebugEnabled()) {
                        count++;
                        if (count % TPS_EVENT_THRESHOLD == 0) {
                            endTime = new Date().getTime();
                            log.debug("# of events batch : " + TPS_EVENT_THRESHOLD + " start timestamp : " + startTime +
                                    " end time stamp : " + endTime + " Throughput is (events / sec) : " +
                                    (((TPS_EVENT_THRESHOLD * 1000) / (endTime - startTime))) +
                                    " Total Event Count : " + count +
                                    ". current eventListMap size: " + eventListMap.size());
                            startTime = new Date().getTime();
                        }
                    }
                }


            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error when converting bytes " + e.getMessage(), e);
        }
    }

    public void trimAndSendToInputHandler() throws InterruptedException {
        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().
                getSiddhiAppMap();

        for(Map.Entry<Long,QueuedEvent> listMapValue : eventListMap.entrySet()) {
            long key = listMapValue.getKey();
            QueuedEvent queuedEvent = listMapValue.getValue();
            SiddhiAppData siddhiAppData = siddhiAppMap.get(queuedEvent.getSiddhiAppName());
            if (siddhiAppData != null) {
                Collection<List<Source>> sourceCollection = siddhiAppData.getSiddhiAppRuntime().getSources();
                for (List<Source> sources : sourceCollection) {
                    boolean isFound = false;
                    for (Source source : sources) {
                        if(queuedEvent.getSourceHandlerElementId().equals(source.getMapper().
                                getHandler().getElementId())){
                            source.getMapper().getHandler().sendEvent(queuedEvent.getEvent(),
                                    queuedEvent.getTransportSyncProperties());
                            if (null != queuedEvent.getTransportSyncProperties() &&
                                    queuedEvent.getTransportSyncProperties().length != 0) {
                                if (source.getMapper().getHandler() instanceof HACoordinationSourceHandler) {
                                    ((HACoordinationSourceHandler)source.getMapper().getHandler()).
                                            updateTransportSyncProperties(queuedEvent.getTransportSyncProperties());
                                }
                            }
                            eventListMap.remove(key);
                            isFound = true;
                            break;
                        }
                    }
                    if (isFound) {
                        break;
                    }
                }
            }
        }
        eventListMap.clear();
        perAppLastControlMessageSequenceNumberList.clear();
    }

    public void trimQueue(String[] persistedAppDetails){
        synchronized (this) {
            //need to synchronize to make sure to finish remembering last control message seq id for siddhi app and
            // trim accordingly
            if (eventListMap.size() != 0 && persistedAppDetails.length != 0){
                long eventListMapSize = eventListMap.size();
                if (log.isDebugEnabled()) {
                    log.debug("eventListMapSize before trimming:  " + eventListMapSize);
                }
                for(String appDetail : persistedAppDetails) {
                    String[] details = appDetail.split(HAConstants.PERSISTED_APP_SPLIT_DELIMITER);
                    long seqId = Long.parseLong(details[0].trim());
                    String appName = details[2].trim();
                    if (perAppLastControlMessageSequenceNumberList.get(appName) != null) {
                        long existingId = perAppLastControlMessageSequenceNumberList.get(appName);
                        if (existingId < seqId) {
                            perAppLastControlMessageSequenceNumberList.put(appName, seqId);
                        }
                    } else {
                        perAppLastControlMessageSequenceNumberList.put(appName, seqId);
                    }
                    Iterator<Map.Entry<Long, QueuedEvent>> iterator = eventListMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Long, QueuedEvent> listMapValue = iterator.next();
                        long key = listMapValue.getKey();
                        if (appName.equals(listMapValue.getValue().getSiddhiAppName()) && seqId > key) {
                            iterator.remove();
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Trimmed " + (eventListMapSize - eventListMap.size()) +
                            " messages from eventListMap. Current eventListMap:" + eventListMap.size());
                }
            }
        }
    }

    public void addToEventListMap(long sequenceNum, QueuedEvent queuedEvent){
        eventListMap.put(sequenceNum, queuedEvent);
    }
}
