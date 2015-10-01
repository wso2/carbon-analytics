/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.publisher.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.processor.manager.core.EventManagementUtil;
import org.wso2.carbon.event.processor.manager.core.EventSync;
import org.wso2.carbon.event.processor.manager.core.Manager;
import org.wso2.carbon.event.processor.manager.core.config.HAConfiguration;
import org.wso2.carbon.event.processor.manager.core.config.ManagementModeInfo;
import org.wso2.carbon.event.processor.manager.core.config.Mode;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherStreamValidationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.event.publisher.core.internal.util.EventPublisherUtil;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.stream.core.SiddhiEventConsumer;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.siddhi.core.event.Event;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventPublisher implements SiddhiEventConsumer, EventSync {

    private static final Log log = LogFactory.getLog(EventPublisher.class);

    private final boolean traceEnabled;
    private final boolean statisticsEnabled;

    List<String> dynamicMessagePropertyList = new ArrayList<String>();
    private Logger trace = Logger.getLogger(EventPublisherConstants.EVENT_TRACE_LOGGER);
    private EventPublisherConfiguration eventPublisherConfiguration = null;
    private int tenantId;
    private Map<String, Integer> propertyPositionMap = new TreeMap<String, Integer>();
    private OutputMapper outputMapper = null;
    private String streamId = null;
    private EventStatisticsMonitor statisticsMonitor;
    private String beforeTracerPrefix;
    private String afterTracerPrefix;
    private boolean dynamicMessagePropertyEnabled = false;
    private boolean customMappingEnabled = false;
    private boolean isPolled = false;

    private Mode mode = Mode.SingleNode;
    private String syncId;
    private boolean sendToOther = false;
    private org.wso2.siddhi.query.api.definition.StreamDefinition streamDefinition;
    private BlockingEventQueue eventQueue;


    public EventPublisher(EventPublisherConfiguration eventPublisherConfiguration)
            throws EventPublisherConfigurationException {

        this.eventPublisherConfiguration = eventPublisherConfiguration;
        this.customMappingEnabled = eventPublisherConfiguration.getOutputMapping().isCustomMappingEnabled();
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String inputStreamName = eventPublisherConfiguration.getFromStreamName();
        String inputStreamVersion = eventPublisherConfiguration.getFromStreamVersion();

        //Stream Definition must same for any event source, There are cannot be different stream definition for same stream id in multiple event sourced
        StreamDefinition inputStreamDefinition = null;

        try {
            inputStreamDefinition = EventPublisherServiceValueHolder.getEventStreamService().getStreamDefinition
                    (inputStreamName, inputStreamVersion);
        } catch (EventStreamConfigurationException e) {
            throw new EventPublisherConfigurationException("Cannot retrieve the stream definition from stream store :" +
                    " " + e.getMessage());
        }

        if (inputStreamDefinition == null) {
            throw new EventPublisherConfigurationException("No event stream exists for the corresponding stream name and " +
                    "version : " + inputStreamName + "-" + inputStreamVersion);
        }

        this.streamId = inputStreamDefinition.getStreamId();
        createPropertyPositionMap(inputStreamDefinition);
        outputMapper = EventPublisherServiceValueHolder.getMappingFactoryMap().get(eventPublisherConfiguration.
                getOutputMapping().getMappingType()).constructOutputMapper(eventPublisherConfiguration, propertyPositionMap,
                tenantId, inputStreamDefinition);

        Map<String, String> dynamicOutputAdapterProperties = eventPublisherConfiguration.getToAdapterDynamicProperties();
        for (Map.Entry<String, String> entry : dynamicOutputAdapterProperties.entrySet()) {
            Map.Entry pairs = (Map.Entry) entry;
            getDynamicOutputMessageProperties(pairs.getValue() != null ? pairs.getValue().toString() : "");
        }

        if (dynamicMessagePropertyList.size() > 0) {
            dynamicMessagePropertyEnabled = true;
        }

        try {
            EventPublisherServiceValueHolder.getEventStreamService().subscribe(this);
        } catch (EventStreamConfigurationException e) {
            throw new EventPublisherStreamValidationException("Stream " + streamId + " does not exist", streamId);
        }

        this.traceEnabled = eventPublisherConfiguration.isTracingEnabled();
        this.statisticsEnabled = eventPublisherConfiguration.isStatisticsEnabled();
        if (statisticsEnabled) {
            this.statisticsMonitor = EventPublisherServiceValueHolder.getEventStatisticsService().
                    getEventStatisticMonitor(tenantId, EventPublisherConstants.EVENT_PUBLISHER,
                            eventPublisherConfiguration.getEventPublisherName(), null);
        }
        if (traceEnabled) {
            this.beforeTracerPrefix = "TenantId : " + tenantId + ", " + EventPublisherConstants.EVENT_PUBLISHER +
                    " : " + eventPublisherConfiguration.getFromStreamName() + ", " +
                    EventPublisherConstants.EVENT_STREAM + " : " +
                    EventPublisherUtil.getImportedStreamIdFrom(eventPublisherConfiguration) +
                    ", before processing " + System.getProperty("line.separator");

            this.afterTracerPrefix = "TenantId : " + tenantId + ", " + EventPublisherConstants.EVENT_PUBLISHER + " : " +
                    eventPublisherConfiguration.getFromStreamName() + ", after processing " +
                    System.getProperty("line.separator");
        }

        OutputEventAdapterService eventAdapterService = EventPublisherServiceValueHolder.getOutputEventAdapterService();
        try {
            eventAdapterService.create(eventPublisherConfiguration.getToAdapterConfiguration());
        } catch (OutputEventAdapterException e) {
            throw new EventPublisherConfigurationException("Error in creating the output Adapter for Event Publisher :" +
                    eventPublisherConfiguration.getEventPublisherName() + ", " +
                    e.getMessage(), e);
        }
        try {
            isPolled = eventAdapterService.isPolled(eventPublisherConfiguration.getToAdapterConfiguration().getName());
        } catch (OutputEventAdapterException e) {
            throw new EventPublisherConfigurationException("Error in creating Event Publisher :" +
                    eventPublisherConfiguration.getEventPublisherName() + ", " +
                    e.getMessage(), e);
        }

        ManagementModeInfo managementModeInfo = EventPublisherServiceValueHolder.getEventManagementService().getManagementModeInfo();
        mode = managementModeInfo.getMode();
        if (mode == Mode.Distributed || mode == Mode.HA) {
            syncId = EventManagementUtil.constructEventSyncId(tenantId,
                    eventPublisherConfiguration.getToAdapterConfiguration().getName(),
                    Manager.ManagerType.Publisher);

            streamDefinition = EventManagementUtil.constructStreamDefinition(syncId, inputStreamDefinition);

            if (mode == Mode.Distributed && managementModeInfo.getDistributedConfiguration().isWorkerNode()) {
                sendToOther = true;
            } else if (mode == Mode.HA && managementModeInfo.getHaConfiguration().isWorkerNode()) {
                sendToOther = true;
                HAConfiguration haConfiguration = managementModeInfo.getHaConfiguration();
                eventQueue = new BlockingEventQueue(haConfiguration.getEventSyncPublisherMaxQueueSizeInMb(), haConfiguration.getEventSyncPublisherQueueSize());
            }
            EventPublisherServiceValueHolder.getEventManagementService().registerEventSync(this, Manager.ManagerType.Publisher);
        }
    }

    public EventPublisherConfiguration getEventPublisherConfiguration() {
        return eventPublisherConfiguration;
    }

    public void sendEvent(Event event) {

        if (isPolled) {
            if (sendToOther) {
                EventPublisherServiceValueHolder.getEventManagementService().syncEvent(syncId, Manager.ManagerType.Publisher, event);
            }
            process(event);
        } else {
            if (!EventPublisherServiceValueHolder.getCarbonEventPublisherManagementService().isDrop()) {
                if (mode == Mode.HA) {
                    //is queue not empty send events from last time
                    long currentTime = EventPublisherServiceValueHolder.getEventManagementService().getClusterTimeInMillis();
                    if (!eventQueue.isEmpty()) {
                        long lastProcessedTime = EventPublisherServiceValueHolder.getEventManagementService().getLatestEventSentTime(
                                eventPublisherConfiguration.getEventPublisherName(), tenantId);
                        while (!eventQueue.isEmpty()) {
                            EventWrapper eventWrapper = eventQueue.poll();
                            if (eventWrapper.getTimestampInMillis() > lastProcessedTime) {
                                process(eventWrapper.getEvent());
                            }
                        }
                    }
                    EventPublisherServiceValueHolder.getEventManagementService().updateLatestEventSentTime(
                            eventPublisherConfiguration.getEventPublisherName(), tenantId, currentTime);
                }
                process(event);
            } else {
                if (mode == Mode.HA) {
                    //add to Queue
                    long currentTime = EventPublisherServiceValueHolder.getEventManagementService().getClusterTimeInMillis();
                    EventWrapper eventWrapper = new EventWrapper(event, currentTime);
                    while (!eventQueue.offer(eventWrapper)) {
                        EventWrapper wrapper = eventQueue.poll();
                        if (log.isDebugEnabled()) {
                            log.debug("Dropping event arrived at " + wrapper.getTimestampInMillis() + " due to insufficient capacity at Event Publisher Queue, dropped event: " + wrapper.getEvent());
                        }
                    }

                    // get last processed time and remove old events from the queue
                    long lastProcessedTime = EventPublisherServiceValueHolder.getEventManagementService().getLatestEventSentTime(
                            eventPublisherConfiguration.getEventPublisherName(), tenantId);

                    while (!eventQueue.isEmpty() && eventQueue.peek().getTimestampInMillis() <= lastProcessedTime) {
                        eventQueue.remove();
                    }
                }
            }
        }
    }

    private void createPropertyPositionMap(StreamDefinition streamDefinition) {
        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();

        int propertyCount = 0;
        if (metaAttributeList != null) {
            for (Attribute attribute : metaAttributeList) {
                propertyPositionMap.put(EventPublisherConstants.PROPERTY_META_PREFIX + attribute.getName(), propertyCount);
                propertyCount++;
            }
        }

        if (correlationAttributeList != null) {
            for (Attribute attribute : correlationAttributeList) {
                propertyPositionMap.put(EventPublisherConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName(), propertyCount);
                propertyCount++;
            }
        }

        if (payloadAttributeList != null) {
            for (Attribute attribute : payloadAttributeList) {
                propertyPositionMap.put(attribute.getName(), propertyCount);
                propertyCount++;
            }
        }
    }

    public String getStreamId() {
        return streamId;
    }

    @Override
    public void consumeEvents(Event[] events) {
        for (Event event : events) {
            sendEvent(event);
        }
    }

    @Override
    public void consumeEvent(Event event) {
        sendEvent(event);
    }

    @Override
    public void shutdown() {

    }

    private List<String> getDynamicOutputMessageProperties(String messagePropertyValue) {

        String text = messagePropertyValue;

        while (text.contains("{{") && text.indexOf("}}") > 0) {
            dynamicMessagePropertyList.add(text.substring(text.indexOf("{{") + 2, text.indexOf("}}")));
            text = text.substring(text.indexOf("}}") + 2);
        }
        return dynamicMessagePropertyList;
    }

    private void changeDynamicEventAdapterMessageProperties(Object[] eventData, Map<String, String> dynamicProperties) {

        for (String dynamicMessageProperty : dynamicMessagePropertyList) {
            if (eventData.length != 0 && dynamicMessageProperty != null) {
                int position = propertyPositionMap.get(dynamicMessageProperty);
                changePropertyValue(position, dynamicMessageProperty, eventData, dynamicProperties);
            }
        }
    }

    private void changePropertyValue(int position, String messageProperty, Object[] eventData,
                                     Map<String, String> dynamicProperties) {

        for (Map.Entry<String, String> entry : dynamicProperties.entrySet()) {
            String mapValue = "{{" + messageProperty + "}}";
            String regexValue = "\\{\\{" + messageProperty + "\\}\\}";
            String entryValue = entry.getValue();
            if (entryValue != null && entryValue.contains(mapValue)) {
                if (eventData[position] != null) {
                    entry.setValue(entryValue.replaceAll(regexValue, eventData[position].toString()));
                } else {
                    entry.setValue(entryValue.replaceAll(regexValue, ""));
                }
            }
        }

    }

    public void destroy() {
        if (mode == Mode.Distributed || mode == Mode.HA) {
            EventPublisherServiceValueHolder.getEventManagementService().unregisterEventSync(syncId, Manager.ManagerType.Publisher);
        }
        EventPublisherServiceValueHolder.getOutputEventAdapterService().destroy(eventPublisherConfiguration.getEventPublisherName());
    }

    @Override
    public void process(Event event) {

        Map<String, String> dynamicProperties = new HashMap<String, String>(eventPublisherConfiguration.getToAdapterDynamicProperties());

        Object outObject;
        if (traceEnabled) {
            trace.info(beforeTracerPrefix + event);
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementResponse();
        }
        try {
            if (customMappingEnabled) {
                outObject = outputMapper.convertToMappedInputEvent(event);
            } else {
                outObject = outputMapper.convertToTypedInputEvent(event);
            }
        } catch (EventPublisherConfigurationException e) {
            log.error("Cannot send " + event + " from " + eventPublisherConfiguration.getEventPublisherName(), e);
            return;
        }

        if (traceEnabled) {
            trace.info(afterTracerPrefix + outObject);
        }

        if (dynamicMessagePropertyEnabled) {
            changeDynamicEventAdapterMessageProperties(event.getData(), dynamicProperties);
        }

        OutputEventAdapterService eventAdapterService = EventPublisherServiceValueHolder.getOutputEventAdapterService();
        eventAdapterService.publish(eventPublisherConfiguration.getEventPublisherName(), dynamicProperties, outObject);

    }

    @Override
    public org.wso2.siddhi.query.api.definition.StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    public void prepareDestroy() {
        if (EventPublisherServiceValueHolder.getEventManagementService().getManagementModeInfo().getMode() == Mode.HA &&
                EventPublisherServiceValueHolder.getEventManagementService().getManagementModeInfo().getHaConfiguration().isWorkerNode()) {
            EventPublisherServiceValueHolder.getEventManagementService().updateLatestEventSentTime(
                    eventPublisherConfiguration.getEventPublisherName(), tenantId,
                    EventPublisherServiceValueHolder.getEventManagementService().getClusterTimeInMillis());
        }
    }

    public class EventWrapper {

        private Event event;
        private long timestampInMillis;
        private int size;

        public EventWrapper(Event event, long timestamp) {
            this.event = event;
            this.timestampInMillis = timestamp;
        }

        public Event getEvent() {
            return event;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getTimestampInMillis() {
            return timestampInMillis;
        }
    }
}
