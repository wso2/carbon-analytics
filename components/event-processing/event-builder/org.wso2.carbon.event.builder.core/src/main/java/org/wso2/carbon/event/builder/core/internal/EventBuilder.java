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

package org.wso2.carbon.event.builder.core.internal;


import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
import org.wso2.carbon.event.builder.core.internal.config.StreamEventJunction;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderRuntimeValidator;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;

import java.util.Arrays;

public class EventBuilder {

    private static final Log log = LogFactory.getLog(EventBuilder.class);
    private boolean traceEnabled = false;
    private boolean statisticsEnabled = false;
    private boolean customMappingEnabled = false;
    private Logger trace = Logger.getLogger(EventBuilderConstants.EVENT_TRACE_LOGGER);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private AxisConfiguration axisConfiguration;
    private StreamDefinition exportedStreamDefinition;
    private InputMapper inputMapper = null;
    private String subscriptionId;
    private StreamEventJunction streamEventJunction = null;
    private EventStatisticsMonitor statisticsMonitor;
    private String beforeTracerPrefix;
    private String afterTracerPrefix;

    public EventBuilder(EventBuilderConfiguration eventBuilderConfiguration, StreamDefinition exportedStreamDefinition,
                        AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        this.axisConfiguration = axisConfiguration;
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (this.eventBuilderConfiguration != null) {
            this.traceEnabled = eventBuilderConfiguration.isTraceEnabled();
            this.statisticsEnabled = eventBuilderConfiguration.isStatisticsEnabled();
            this.customMappingEnabled = eventBuilderConfiguration.getInputMapping().isCustomMappingEnabled();
            String mappingType = this.eventBuilderConfiguration.getInputMapping().getMappingType();
            this.inputMapper = EventBuilderServiceValueHolder.getMappingFactoryMap().get(mappingType).constructInputMapper(this.eventBuilderConfiguration, null);

            // The input mapper should not be null. For configurations where custom mapping is disabled,
            // an input mapper would be created without the mapping details
            if (this.inputMapper != null) {
                if (customMappingEnabled) {
                    EventBuilderRuntimeValidator.validateExportedStream(eventBuilderConfiguration, exportedStreamDefinition, this.inputMapper);
                }
                this.exportedStreamDefinition = exportedStreamDefinition;
            } else {
                throw new EventBuilderConfigurationException("Could not create input mapper for mapping type "
                        + mappingType + " for event builder :" + eventBuilderConfiguration.getEventBuilderName());
            }

            // Initialize tracer and statistics.
            if (statisticsEnabled) {
                this.statisticsMonitor = EventBuilderServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(
                        tenantId, EventBuilderConstants.EVENT_BUILDER, eventBuilderConfiguration.getEventBuilderName(), null);
            }
            if (traceEnabled) {
                this.beforeTracerPrefix = "TenantId=" + tenantId + " : " + EventBuilderConstants.EVENT_BUILDER + " : "
                        + eventBuilderConfiguration.getEventBuilderName() + ", before processing " + System.getProperty("line.separator");
                this.afterTracerPrefix = "TenantId=" + tenantId + " : " + EventBuilderConstants.EVENT_BUILDER + " : "
                        + eventBuilderConfiguration.getEventBuilderName() + " : " + EventBuilderConstants.EVENT_STREAM + " : "
                        + EventBuilderUtil.getExportedStreamIdFrom(eventBuilderConfiguration) + " , after processing " + System.getProperty("line.separator");
            }
        }
    }

    public int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    public void setStreamEventJunction(StreamEventJunction streamEventJunction) {
        this.streamEventJunction = streamEventJunction;
    }

    /**
     * Returns the stream definition that is exported by this event builder.
     * This stream definition will available to any object that consumes the event builder service
     * (e.g. EventProcessors)
     *
     * @return the {@link StreamDefinition} of the stream that will be
     *         sending out events from this event builder
     */
    public StreamDefinition getExportedStreamDefinition() {
        return exportedStreamDefinition;
    }

    /**
     * Returns the event builder configuration associated with this event builder
     *
     * @return the {@link EventBuilderConfiguration} instance
     */
    public EventBuilderConfiguration getEventBuilderConfiguration() {
        return this.eventBuilderConfiguration;
    }

    /**
     * Subscribes to a event adaptor according to the current event builder configuration
     */
    public void subscribeToEventAdaptor() throws EventBuilderConfigurationException {
        if (this.eventBuilderConfiguration == null || this.inputMapper == null) {
            throw new EventBuilderConfigurationException("Cannot subscribe to input event adaptor. Event builder has not been initialized properly.");
        }
        if (this.subscriptionId == null) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String inputEventAdaptorName = eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorName();
            try {
                InputEventAdaptorConfiguration inputEventAdaptorConfiguration =
                        EventBuilderServiceValueHolder.getInputEventAdaptorManagerService().getActiveInputEventAdaptorConfiguration(
                                inputEventAdaptorName, tenantId);
                if (this.customMappingEnabled) {
                    this.subscriptionId = EventBuilderServiceValueHolder.getInputEventAdaptorService().subscribe(
                            inputEventAdaptorConfiguration,
                            eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorMessageConfiguration(),
                            new MappedEventListenerImpl(), axisConfiguration);
                } else {
                    this.subscriptionId = EventBuilderServiceValueHolder.getInputEventAdaptorService().subscribe(
                            inputEventAdaptorConfiguration,
                            eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorMessageConfiguration(),
                            new TypedEventListenerImpl(), axisConfiguration);
                }
            } catch (InputEventAdaptorManagerConfigurationException e) {
                log.error("Cannot subscribe to input event adaptor :" + inputEventAdaptorName + ", error in configuration.");
                throw new EventBuilderConfigurationException(e);
            } catch (InputEventAdaptorEventProcessingException e) {
                throw new EventBuilderValidationException("Cannot subscribe to input event adaptor :" + inputEventAdaptorName + ", error processing connection by adaptor.", inputEventAdaptorName, e);
            }
        }
    }

    /**
     * Unsubscribes from the input event adaptor that corresponds to the passed in configuration
     *
     * @param inputEventAdaptorConfiguration the configuration of the input event adaptor from which unsubscribing happens
     */
    public void unsubscribeFromEventAdaptor(
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration) throws EventBuilderConfigurationException {
        if (inputEventAdaptorConfiguration == null) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String inputEventAdaptorName = this.eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorName();
            try {
                inputEventAdaptorConfiguration =
                        EventBuilderServiceValueHolder.getInputEventAdaptorManagerService().getActiveInputEventAdaptorConfiguration(
                                inputEventAdaptorName, tenantId);
            } catch (InputEventAdaptorManagerConfigurationException e) {
                log.error("Cannot unsubscribe from input event adaptor : " + inputEventAdaptorName + ", " + e.getMessage(), e);
                throw new EventBuilderConfigurationException(e);
            } catch (InputEventAdaptorEventProcessingException e) {
                throw new EventBuilderValidationException("Cannot unsubscribe from input event adaptor :" + inputEventAdaptorName + ", error processing unsubscribe.", inputEventAdaptorName, e);
            }
        }
        if (inputEventAdaptorConfiguration != null && this.subscriptionId != null) {
            EventBuilderServiceValueHolder.getInputEventAdaptorService().unsubscribe(
                    eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorMessageConfiguration(),
                    inputEventAdaptorConfiguration, axisConfiguration, this.subscriptionId);
        }
        this.subscriptionId = null;
    }

    protected void processMappedEvent(Object obj) {
        if (traceEnabled) {
            trace.info(beforeTracerPrefix + obj.toString());
        }
        Object convertedEvent = null;
        try {
            convertedEvent = this.inputMapper.convertToMappedInputEvent(obj);
            if (convertedEvent instanceof Object[][]) {
                Object[][] arrayOfEvents = (Object[][]) convertedEvent;
                for (Object[] outObjArray : arrayOfEvents) {
                    sendEvent(outObjArray);
                }
            } else {
                sendEvent((Object[]) convertedEvent);
            }
        } catch (EventBuilderConfigurationException e) {
            log.error("Error processing event : " + e.getMessage(), e);
        }
    }

    protected void processTypedEvent(Object obj) {
        if (traceEnabled) {
            trace.info(beforeTracerPrefix + obj.toString());
        }
        Object convertedEvent = null;
        try {
            convertedEvent = this.inputMapper.convertToTypedInputEvent(obj);
        } catch (EventBuilderConfigurationException e) {
            log.error("Error processing event : " + e.getMessage(), e);
        }
        sendEvent((Object[]) convertedEvent);
    }

    protected void sendEvent(Object[] outObjArray) {
        if (traceEnabled) {
            trace.info(afterTracerPrefix + Arrays.toString(outObjArray));
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementRequest();
        }
        this.streamEventJunction.dispatchEvent(outObjArray);
    }

    protected void defineEventStream(Object definition) throws EventBuilderConfigurationException {
        if (log.isDebugEnabled()) {
            log.debug("EventBuilder: " + eventBuilderConfiguration.getEventBuilderName() + ", notifying event definition addition :" + definition.toString());
        }
        if (definition instanceof StreamDefinition) {
            StreamDefinition inputStreamDefinition = (StreamDefinition) definition;
            String mappingType = eventBuilderConfiguration.getInputMapping().getMappingType();
            this.inputMapper = EventBuilderServiceValueHolder.getMappingFactoryMap().get(mappingType).constructInputMapper(eventBuilderConfiguration, inputStreamDefinition);
            if (!customMappingEnabled) {
                if (EventBuilderUtil.getExportedStreamIdFrom(this.eventBuilderConfiguration).equals(inputStreamDefinition.getStreamId())) {
                    this.exportedStreamDefinition = inputStreamDefinition;
                    this.streamEventJunction.setExportedStreamDefinition(inputStreamDefinition, getTenantId());
                } else {
                    throw new EventBuilderConfigurationException("Input Stream Definition does not match outgoing stream definition" +
                            " while no custom mapping is available");
                }
            }
        }
    }

    protected void removeEventStream(Object definition) {
        if (log.isDebugEnabled()) {
            log.debug("EventBuilder: " + eventBuilderConfiguration.getEventBuilderName() + ", notifying event definition addition :" + definition.toString());
        }
        this.inputMapper = null;
    }

    private class MappedEventListenerImpl extends InputEventAdaptorListener {

        @Override
        public void addEventDefinition(Object o) {
            try {
                defineEventStream(o);
            } catch (EventBuilderConfigurationException e) {
                log.error("Error in adding event definition : " + e.getMessage(), e);
            }
        }

        @Override
        public void removeEventDefinition(Object o) {
            removeEventStream(o);
        }

        @Override
        public void onEvent(Object o) {
            processMappedEvent(o);
        }
    }

    private class TypedEventListenerImpl extends InputEventAdaptorListener {

        @Override
        public void addEventDefinition(Object o) {
            try {
                defineEventStream(o);
            } catch (EventBuilderConfigurationException e) {
                log.error("Error in adding event definition : " + e.getMessage(), e);
            }
        }

        @Override
        public void removeEventDefinition(Object o) {
            removeEventStream(o);
        }

        @Override
        public void onEvent(Object o) {
            processTypedEvent(o);
        }
    }
}
