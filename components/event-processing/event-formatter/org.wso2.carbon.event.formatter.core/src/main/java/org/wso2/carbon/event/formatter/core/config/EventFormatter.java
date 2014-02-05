/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.formatter.core.config;


import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.EventFormatterSender;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterValidationException;
import org.wso2.carbon.event.formatter.core.internal.OutputMapper;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;
import org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.processor.api.send.EventProducer;
import org.wso2.carbon.event.processor.api.send.exception.EventProducerException;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import java.util.*;

public class EventFormatter {

    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;
    List<String> dynamicMessagePropertyList = new ArrayList<String>();
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);
    private EventFormatterConfiguration eventFormatterConfiguration = null;
    private int tenantId;
    private Map<String, Integer> propertyPositionMap = new TreeMap<String, Integer>();
    private OutputEventAdaptorConfiguration outputEventAdaptorConfiguration = null;
    private OutputMapper outputMapper = null;
    private Object[] eventObject = null;
    private boolean metaFlag = false;
    private boolean correlationFlag = false;
    private boolean payloadFlag = false;
    private List<EventProducer> eventProducerList = null;
    private EventFormatterSender eventFormatterSender = null;
    private String streamId = null;
    private EventStatisticsMonitor statisticsMonitor;
    private String beforeTracerPrefix;
    private String afterTracerPrefix;
    private boolean dynamicMessagePropertyEnabled = false;
    private boolean customMappingEnabled = false;

    public EventFormatter(EventFormatterConfiguration eventFormatterConfiguration) throws EventFormatterConfigurationException {

        this.eventFormatterConfiguration = eventFormatterConfiguration;
        this.customMappingEnabled = eventFormatterConfiguration.getOutputMapping().isCustomMappingEnabled();
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String inputStreamName = eventFormatterConfiguration.getFromStreamName();
        String inputStreamVersion = eventFormatterConfiguration.getFromStreamVersion();
        eventProducerList = getEventProducers();

        //Stream Definition must same for any event source, There are cannot be different stream definition for same stream id in multiple event sourced
        StreamDefinition inputStreamDefinition = null;

        if (eventProducerList == null) {
            throw new EventFormatterConfigurationException("There is no any event producers exist");
        } else {
            try {
                inputStreamDefinition = EventFormatterServiceValueHolder.getEventStreamService().getStreamDefinitionFromStore(inputStreamName, inputStreamVersion, tenantId);

            } catch (EventStreamConfigurationException e) {
                throw new EventFormatterConfigurationException("Cannot retrieve the stream definition from stream store : " + e.getMessage());
            }
        }

        if (inputStreamDefinition == null) {
            throw new EventFormatterConfigurationException("There is no any event senders for the corresponding stream name or version : " + inputStreamName + "-" + inputStreamVersion);
        }
        this.eventObject = createEventTemplate(inputStreamDefinition);
        this.streamId = inputStreamDefinition.getStreamId();
        createPropertyPositionMap(inputStreamDefinition);
        outputMapper = EventFormatterServiceValueHolder.getMappingFactoryMap().get(eventFormatterConfiguration.getOutputMapping().getMappingType()).constructOutputMapper(eventFormatterConfiguration, propertyPositionMap, tenantId);
        setOutputEventAdaptorConfiguration(tenantId);
        eventFormatterSender = new EventFormatterSender(this);

        Map<String, String> messageProperties = eventFormatterConfiguration.getToPropertyConfiguration().getOutputEventAdaptorMessageConfiguration().getOutputMessageProperties();
        for (Map.Entry<String, String> entry : messageProperties.entrySet()) {
            Map.Entry pairs = (Map.Entry) entry;
            getDynamicOutputMessageProperties(pairs.getValue() != null ? pairs.getValue().toString():"");
        }

        if (dynamicMessagePropertyList.size() > 0) {
            dynamicMessagePropertyEnabled = true;
        }

        for (EventProducer eventProducer : eventProducerList) {
            try {
                eventProducer.subscribe(streamId, eventFormatterSender);
            } catch (EventProducerException e) {
                throw new EventFormatterValidationException("Could not subscribe to event producer for stream id : " + streamId, streamId);
            }
        }

        this.traceEnabled = eventFormatterConfiguration.isEnableTracing();
        this.statisticsEnabled = eventFormatterConfiguration.isEnableStatistics();
        if (statisticsEnabled) {
            this.statisticsMonitor = EventFormatterServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, EventFormatterConstants.EVENT_FORMATTER, eventFormatterConfiguration.getEventFormatterName(), null);
        }
        if (traceEnabled) {
            this.beforeTracerPrefix = "TenantId=" + tenantId + " : " + EventFormatterConstants.EVENT_FORMATTER + " : " + eventFormatterConfiguration.getFromStreamName() + ", before processing " + System.getProperty("line.separator");
            this.afterTracerPrefix = "TenantId=" + tenantId + " : " + EventFormatterConstants.EVENT_FORMATTER + " : " + eventFormatterConfiguration.getFromStreamName() + ", after processing " + System.getProperty("line.separator");
        }
    }

    public EventFormatterConfiguration getEventFormatterConfiguration() {
        return eventFormatterConfiguration;
    }

    public void sendEventData(Object inObject) throws EventFormatterConfigurationException {

        Object outObject;
        if (traceEnabled) {
            if (inObject instanceof Object[]) {
                trace.info(beforeTracerPrefix + Arrays.deepToString((Object[]) inObject));
            } else {
                trace.info(beforeTracerPrefix + inObject);
            }
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementResponse();
        }

        if (customMappingEnabled) {
            outObject = outputMapper.convertToMappedInputEvent(inObject);
        } else {
            outObject = outputMapper.convertToTypedInputEvent(inObject);
        }

        if (traceEnabled) {
            if (outObject instanceof Object[]) {
                trace.info(afterTracerPrefix + Arrays.deepToString((Object[]) outObject));
            } else {
                trace.info(afterTracerPrefix + outObject);
            }
        }

        if (dynamicMessagePropertyEnabled) {
            changeDynamicEventAdaptorMessageProperties(inObject);
        }

        OutputEventAdaptorService eventAdaptorService = EventFormatterServiceValueHolder.getOutputEventAdaptorService();
        eventAdaptorService.publish(outputEventAdaptorConfiguration, eventFormatterConfiguration.getToPropertyConfiguration().getOutputEventAdaptorMessageConfiguration(), outObject, tenantId);

    }

    public void sendEvent(Event event) throws EventFormatterConfigurationException {

        int count = 0;
        Object[] metaData = event.getMetaData();
        Object[] correlationData = event.getCorrelationData();
        Object[] payloadData = event.getPayloadData();

        if (metaFlag) {
            System.arraycopy(metaData, 0, eventObject, 0, metaData.length);
            count += metaData.length;
        }

        if (correlationFlag) {
            System.arraycopy(correlationData, 0, eventObject, count, correlationData.length);
            count += correlationData.length;
        }

        if (payloadFlag) {
            System.arraycopy(payloadData, 0, eventObject, count, payloadData.length);
            count += payloadData.length;
        }

        sendEventData(eventObject);
    }

    private void setOutputEventAdaptorConfiguration(int tenantId)
            throws EventFormatterConfigurationException {
        OutputEventAdaptorManagerService eventAdaptorManagerService = EventFormatterServiceValueHolder.getOutputEventAdaptorManagerService();

        try {
            this.outputEventAdaptorConfiguration = eventAdaptorManagerService.getActiveOutputEventAdaptorConfiguration(eventFormatterConfiguration.getToPropertyConfiguration().getEventAdaptorName(), tenantId);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            throw new EventFormatterConfigurationException("Error while retrieving the output event adaptor configuration of : " + eventFormatterConfiguration.getToPropertyConfiguration().getEventAdaptorName(), e);
        }

    }

    private void createPropertyPositionMap(StreamDefinition streamDefinition) {
        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();

        int propertyCount = 0;
        if (metaAttributeList != null) {
            for (Attribute attribute : metaAttributeList) {
                propertyPositionMap.put(EventFormatterConstants.PROPERTY_META_PREFIX + attribute.getName(), propertyCount);
                propertyCount++;
            }
        }

        if (correlationAttributeList != null) {
            for (Attribute attribute : correlationAttributeList) {
                propertyPositionMap.put(EventFormatterConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName(), propertyCount);
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

    private List<EventProducer> getEventProducers() {
        return EventFormatterServiceValueHolder.getEventProducerList();
    }

    private Object[] createEventTemplate(StreamDefinition inputStreamDefinition) {
        int attributesCount = 0;
        if (inputStreamDefinition.getMetaData() != null) {
            attributesCount += inputStreamDefinition.getMetaData().size();
            metaFlag = true;
        }
        if (inputStreamDefinition.getCorrelationData() != null) {
            attributesCount += inputStreamDefinition.getCorrelationData().size();
            correlationFlag = true;
        }

        if (inputStreamDefinition.getPayloadData() != null) {
            attributesCount += inputStreamDefinition.getPayloadData().size();
            payloadFlag = true;
        }

        return new Object[attributesCount];
    }

    public String getStreamId() {
        return streamId;
    }

    public List<EventProducer> getEventProducerList() {
        return eventProducerList;
    }

    public EventFormatterSender getEventFormatterSender() {
        return eventFormatterSender;
    }

    private List<String> getDynamicOutputMessageProperties(String messagePropertyValue) {

        String text = messagePropertyValue;

        while (text.contains("{{") && text.indexOf("}}") > 0) {
            dynamicMessagePropertyList.add(text.substring(text.indexOf("{{") + 2, text.indexOf("}}")));
            text = text.substring(text.indexOf("}}") + 2);
        }
        return dynamicMessagePropertyList;
    }

    private void changeDynamicEventAdaptorMessageProperties(Object obj) {
        Object[] inputObjArray = (Object[]) obj;

        for (String dynamicMessageProperty : dynamicMessagePropertyList) {
            if (inputObjArray.length != 0) {
                int position = propertyPositionMap.get(dynamicMessageProperty);
                changePropertyValue(position, dynamicMessageProperty, obj);
            }
        }
    }

    private void changePropertyValue(int position, String messageProperty, Object obj) {
        Object[] inputObjArray = (Object[]) obj;
        Map<String, String> outputMessageProperties = eventFormatterConfiguration.getToPropertyConfiguration().getOutputEventAdaptorMessageConfiguration().getOutputMessageProperties();

        List<String> keys = new ArrayList<String>();

        for (Map.Entry<String, String> entry : outputMessageProperties.entrySet()) {
            String mapValue = "{{" + messageProperty + "}}";
            if (mapValue.equals(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }

        for (String key : keys) {
            outputMessageProperties.put(key, inputObjArray[position].toString());
        }

    }
}
