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
package org.wso2.carbon.event.receiver.core.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterSubscription;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.InputMapper;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverProcessingException;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigurationHelper;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.stream.core.EventProducer;
import org.wso2.carbon.event.stream.core.EventProducerCallback;

import java.util.Arrays;
import java.util.List;

public class EventReceiver implements EventProducer {

    private static final Log log = LogFactory.getLog(EventReceiver.class);
    private boolean traceEnabled = false;
    private boolean statisticsEnabled = false;
    private boolean customMappingEnabled = false;
    private Logger trace = Logger.getLogger(EventReceiverConstants.EVENT_TRACE_LOGGER);
    private EventReceiverConfiguration eventReceiverConfiguration = null;
    private StreamDefinition exportedStreamDefinition;
    private InputMapper inputMapper = null;
    private EventStatisticsMonitor statisticsMonitor;
    private String beforeTracerPrefix;
    private String afterTracerPrefix;
    private EventProducerCallback callBack;

    public EventReceiver(EventReceiverConfiguration eventReceiverConfiguration,
                         StreamDefinition exportedStreamDefinition)
            throws EventReceiverConfigurationException {
        this.eventReceiverConfiguration = eventReceiverConfiguration;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (this.eventReceiverConfiguration != null) {
            this.traceEnabled = eventReceiverConfiguration.isTraceEnabled();
            this.statisticsEnabled = eventReceiverConfiguration.isStatisticsEnabled();
            this.customMappingEnabled = eventReceiverConfiguration.getInputMapping().isCustomMappingEnabled();
            String mappingType = this.eventReceiverConfiguration.getInputMapping().getMappingType();
            this.inputMapper = EventReceiverServiceValueHolder.getMappingFactoryMap().get(mappingType).constructInputMapper(this.eventReceiverConfiguration, exportedStreamDefinition);

            // The input mapper should not be null. For configurations where custom mapping is disabled,
            // an input mapper would be created without the mapping details
            if (this.inputMapper != null) {
                if (customMappingEnabled) {
                    EventReceiverConfigurationHelper.validateExportedStream(eventReceiverConfiguration, exportedStreamDefinition, this.inputMapper);
                }
                this.exportedStreamDefinition = exportedStreamDefinition;
            } else {
                throw new EventReceiverConfigurationException("Could not create input mapper for mapping type "
                        + mappingType + " for event receiver :" + eventReceiverConfiguration.getEventReceiverName());
            }

            // Initialize tracer and statistics.
            if (statisticsEnabled) {
                this.statisticsMonitor = EventReceiverServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(
                        tenantId, EventReceiverConstants.EVENT_RECEIVER, eventReceiverConfiguration.getEventReceiverName(), null);
            }
            if (traceEnabled) {
                this.beforeTracerPrefix = "TenantId=" + tenantId + " : " + EventReceiverConstants.EVENT_RECEIVER + " : "
                        + eventReceiverConfiguration.getEventReceiverName() + ", before processing " + System.getProperty("line.separator");
                this.afterTracerPrefix = "TenantId=" + tenantId + " : " + EventReceiverConstants.EVENT_RECEIVER + " : "
                        + eventReceiverConfiguration.getEventReceiverName() + " : " + EventReceiverConstants.EVENT_STREAM + " : "
                        + EventReceiverUtil.getExportedStreamIdFrom(eventReceiverConfiguration) + " , after processing " + System.getProperty("line.separator");
            }

            String inputEventAdapterName = eventReceiverConfiguration.getFromAdapterConfiguration().getName();
            try {
                if (this.customMappingEnabled) {
                    EventReceiverServiceValueHolder.getInputEventAdapterService().create(
                            eventReceiverConfiguration.getFromAdapterConfiguration(), new MappedEventSubscription());
                } else {
                    EventReceiverServiceValueHolder.getInputEventAdapterService().create(
                            eventReceiverConfiguration.getFromAdapterConfiguration(), new TypedEventSubscription());
                }
            } catch (InputEventAdapterException e) {
                throw new EventReceiverConfigurationException("Cannot subscribe to input event adapter :" + inputEventAdapterName + ", error in configuration.", e);
            } catch (InputEventAdapterRuntimeException e) {
                throw new EventReceiverProcessingException("Cannot subscribe to input event adapter :" + inputEventAdapterName + ", error while connecting by adapter.", e);
            }
        }
    }

    public int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * Returns the stream definition that is exported by this event receiver.
     * This stream definition will available to any object that consumes the event receiver service
     * (e.g. EventProcessors)
     *
     * @return the {@link StreamDefinition} of the stream that will be
     * sending out events from this event receiver
     */
    public StreamDefinition getExportedStreamDefinition() {
        return exportedStreamDefinition;
    }

    /**
     * Returns the event receiver configuration associated with this event receiver
     *
     * @return the {@link EventReceiverConfiguration} instance
     */
    public EventReceiverConfiguration getEventReceiverConfiguration() {
        return this.eventReceiverConfiguration;
    }

    protected void processMappedEvent(Object object) {
        if (traceEnabled) {
            trace.info(beforeTracerPrefix + object.toString());
        }

        try {
            if (object instanceof List) {
                sendEventList((List<Event>) object);
            } else {
                Object convertedEvent = this.inputMapper.convertToMappedInputEvent(object);
                if (convertedEvent != null) {
                    if (convertedEvent instanceof Object[][]) {
                        Object[][] arrayOfEvents = (Object[][]) convertedEvent;
                        for (Object[] outObjArray : arrayOfEvents) {
                            sendEvent(outObjArray);
                        }
                    } else {
                        sendEvent((Object[]) convertedEvent);
                    }
                } else {
                    log.warn("Dropping the empty/null event, Event does not matched with mapping");
                }
            }
        } catch (EventReceiverProcessingException e) {
            log.error("Dropping event, Error processing event : " + e.getMessage(), e);
        }

    }

    protected void processTypedEvent(Object obj) {
        if (traceEnabled) {
            trace.info(beforeTracerPrefix + obj.toString());
        }
        Object convertedEvent = null;
        try {
            if (obj instanceof List) {
                sendEventList((List<Event>) obj);
            } else {
                convertedEvent = this.inputMapper.convertToTypedInputEvent(obj);
                if (convertedEvent instanceof Object[][]) {
                    Object[][] arrayOfEvents = (Object[][]) convertedEvent;
                    for (Object[] outObjArray : arrayOfEvents) {
                        sendEvent(outObjArray);
                    }
                } else {
                    sendEvent((Object[]) convertedEvent);
                }
            }
        } catch (EventReceiverProcessingException e) {
            log.error("Dropping event, Error processing event: " + e.getMessage(), e);
        }
    }

    protected void sendEvent(Object[] outObjArray) {
        if (traceEnabled) {
            trace.info(afterTracerPrefix + Arrays.toString(outObjArray));
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementRequest();
        }
        this.callBack.sendEventData(outObjArray);
    }

    protected void sendEventList(List<Event> events) {
        if (traceEnabled) {
            trace.info(afterTracerPrefix + events);
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementRequest();
        }
        this.callBack.sendEvents(events);
    }

    protected void defineEventStream(Object definition) throws EventReceiverConfigurationException {
        if (log.isDebugEnabled()) {
            log.debug("EventReceiver: " + eventReceiverConfiguration.getEventReceiverName() + ", notifying event definition addition :" + definition.toString());
        }
        if (definition instanceof StreamDefinition) {
            StreamDefinition inputStreamDefinition = (StreamDefinition) definition;
            String mappingType = eventReceiverConfiguration.getInputMapping().getMappingType();
            this.inputMapper = EventReceiverServiceValueHolder.getMappingFactoryMap().get(mappingType).constructInputMapper(eventReceiverConfiguration, exportedStreamDefinition);
        }
    }

    protected void removeEventStream(Object definition) {
        if (log.isDebugEnabled()) {
            log.debug("EventReceiver: " + eventReceiverConfiguration.getEventReceiverName() + ", notifying event definition addition :" + definition.toString());
        }
        this.inputMapper = null;
    }

    @Override
    public String getStreamId() {
        return exportedStreamDefinition.getStreamId();
    }

    @Override
    public void setCallBack(EventProducerCallback callBack) {
        this.callBack = callBack;
    }

    public void destroy() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventReceiverServiceValueHolder.getInputEventAdapterService().destroy(eventReceiverConfiguration.getFromAdapterConfiguration().getName());

    }

    private class MappedEventSubscription implements InputEventAdapterSubscription {
        @Override
        public void onEvent(Object o) {
            processMappedEvent(o);
        }
    }

    private class TypedEventSubscription implements InputEventAdapterSubscription {
        @Override
        public void onEvent(Object o) {
            processTypedEvent(o);
        }
    }
}
