/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
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
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherStreamValidationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.stream.core.RawEventConsumer;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import java.util.*;

public class EventPublisher implements RawEventConsumer {

    private static final Log log = LogFactory.getLog(EventPublisher.class);

    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;

    List<String> dynamicMessagePropertyList = new ArrayList<String>();
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);
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
            inputStreamDefinition = EventPublisherServiceValueHolder.getEventStreamService().getStreamDefinition(inputStreamName, inputStreamVersion);
        } catch (EventStreamConfigurationException e) {
            throw new EventPublisherConfigurationException("Cannot retrieve the stream definition from stream store : " + e.getMessage());
        }

        if (inputStreamDefinition == null) {
            throw new EventPublisherConfigurationException("There is no any event stream for the corresponding stream name and version : " + inputStreamName + "-" + inputStreamVersion);
        }

        this.streamId = inputStreamDefinition.getStreamId();
        createPropertyPositionMap(inputStreamDefinition);
        outputMapper = EventPublisherServiceValueHolder.getMappingFactoryMap().get(eventPublisherConfiguration.getOutputMapping().getMappingType()).constructOutputMapper(eventPublisherConfiguration, propertyPositionMap, tenantId, inputStreamDefinition);

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
            this.statisticsMonitor = EventPublisherServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, EventPublisherConstants.EVENT_PUBLISHER, eventPublisherConfiguration.getEventPublisherName(), null);
        }
        if (traceEnabled) {
            this.beforeTracerPrefix = "TenantId=" + tenantId + " : " + EventPublisherConstants.EVENT_PUBLISHER + " : " + eventPublisherConfiguration.getFromStreamName() + ", before processing " + System.getProperty("line.separator");
            this.afterTracerPrefix = "TenantId=" + tenantId + " : " + EventPublisherConstants.EVENT_PUBLISHER + " : " + eventPublisherConfiguration.getFromStreamName() + ", after processing " + System.getProperty("line.separator");
        }

        OutputEventAdapterService eventAdapterService = EventPublisherServiceValueHolder.getOutputEventAdapterService();
        try {
            eventAdapterService.create(eventPublisherConfiguration.getToAdapterConfiguration());
        } catch (OutputEventAdapterException e) {
            throw new EventPublisherConfigurationException("Error in creating the output Adapter for Event Publisher :" + eventPublisherConfiguration.getEventPublisherName() + ", " + e.getMessage(), e);
        }

    }

    public EventPublisherConfiguration getEventPublisherConfiguration() {
        return eventPublisherConfiguration;
    }

    public void sendEventData(Object[] eventData) {

        Map<String, String> dynamicProperties = new HashMap<String, String>(eventPublisherConfiguration.getToAdapterDynamicProperties());

        Object outObject;
        if (traceEnabled) {
            trace.info(beforeTracerPrefix + Arrays.deepToString(eventData));
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementResponse();
        }
        try {
            if (customMappingEnabled) {
                outObject = outputMapper.convertToMappedInputEvent(eventData);
            } else {
                outObject = outputMapper.convertToTypedInputEvent(eventData);
            }
        } catch (EventPublisherConfigurationException e) {
            log.error("Cannot send event:" + Arrays.deepToString(eventData) + " from " + eventPublisherConfiguration.getEventPublisherName());
            return;
        }

        if (traceEnabled) {
            if (outObject instanceof Object[]) {
                trace.info(afterTracerPrefix + Arrays.deepToString((Object[]) outObject));
            } else {
                trace.info(afterTracerPrefix + outObject);
            }
        }

        if (dynamicMessagePropertyEnabled) {
            changeDynamicEventAdapterMessageProperties(eventData, dynamicProperties);
        }

        OutputEventAdapterService eventAdapterService = EventPublisherServiceValueHolder.getOutputEventAdapterService();
        eventAdapterService.publish(eventPublisherConfiguration.getEventPublisherName(), eventPublisherConfiguration.getToAdapterDynamicProperties(), outObject);

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
    public void consumeEventData(Object[] eventData) {
        sendEventData(eventData);
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

    private void changePropertyValue(int position, String messageProperty, Object[] eventData, Map<String, String> dynamicProperties) {

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
        OutputEventAdapterService eventAdapterService = EventPublisherServiceValueHolder.getOutputEventAdapterService();
        eventAdapterService.destroy(eventPublisherConfiguration.getEventPublisherName());

    }
}
