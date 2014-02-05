/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.input.adaptor.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorDto;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.internal.ds.InputEventAdaptorServiceValueHolder;
import org.wso2.carbon.event.input.adaptor.core.message.MessageDto;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventAdaptor service implementation.
 */
public class CarbonInputEventAdaptorService implements InputEventAdaptorService {

    private static final String INPUT_EVENT_ADAPTOR = "Input Event Adaptor";
    private static Log log = LogFactory.getLog(CarbonInputEventAdaptorService.class);
    private Map<String, AbstractInputEventAdaptor> eventAdaptorMap;

    public CarbonInputEventAdaptorService() {
        this.eventAdaptorMap = new ConcurrentHashMap<String, AbstractInputEventAdaptor>();
    }

    public void registerEventAdaptor(AbstractInputEventAdaptor abstractInputEventAdaptor) {
        InputEventAdaptorDto inputEventAdaptorDto = abstractInputEventAdaptor.getInputEventAdaptorDto();
        this.eventAdaptorMap.put(inputEventAdaptorDto.getEventAdaptorTypeName(), abstractInputEventAdaptor);
        InputEventAdaptorServiceValueHolder.getComponentContext().getBundleContext().registerService(AbstractInputEventAdaptor.class.getName(), abstractInputEventAdaptor, null);
    }

    public void unRegisterEventAdaptor(AbstractInputEventAdaptor abstractInputEventAdaptor) {
        InputEventAdaptorDto inputEventAdaptorDto = abstractInputEventAdaptor.getInputEventAdaptorDto();
        this.eventAdaptorMap.remove(inputEventAdaptorDto.getEventAdaptorTypeName());
    }

    @Override
    public List<InputEventAdaptorDto> getEventAdaptors() {
        List<InputEventAdaptorDto> inputEventAdaptorDtos = new ArrayList<InputEventAdaptorDto>();
        for (AbstractInputEventAdaptor abstractEventAdaptor : this.eventAdaptorMap.values()) {
            inputEventAdaptorDtos.add(abstractEventAdaptor.getInputEventAdaptorDto());
        }
        return inputEventAdaptorDtos;
    }

    @Override
    public MessageDto getEventMessageDto(String eventAdaptorTypeName) {

        for (AbstractInputEventAdaptor abstractInputEventAdaptor : this.eventAdaptorMap.values()) {
            if (abstractInputEventAdaptor.getInputEventAdaptorDto().getEventAdaptorTypeName().equals(eventAdaptorTypeName)) {
                return abstractInputEventAdaptor.getMessageDto();
            }
        }
        return null;
    }

    @Override
    public String subscribe(InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
                            InputEventAdaptorListener inputEventAdaptorListener,
                            AxisConfiguration axisConfiguration) {
        AbstractInputEventAdaptor inputEventAdaptor = this.eventAdaptorMap.get(inputEventAdaptorConfiguration.getType());

        inputEventAdaptorListener.setStatisticsEnabled(inputEventAdaptorConfiguration.isEnableStatistics());
        inputEventAdaptorListener.setTraceEnabled(inputEventAdaptorConfiguration.isEnableTracing());
        inputEventAdaptorListener.setEventAdaptorName(inputEventAdaptorConfiguration.getName());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (inputEventAdaptorConfiguration.isEnableStatistics()) {
            EventStatisticsMonitor statisticsMonitor = InputEventAdaptorServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, INPUT_EVENT_ADAPTOR, inputEventAdaptorConfiguration.getName(), null);
            inputEventAdaptorListener.setStatisticsMonitor(statisticsMonitor);
        }
        if (inputEventAdaptorConfiguration.isEnableTracing()) {
            String tracerPrefix = "TenantId=" + tenantId + " : " + INPUT_EVENT_ADAPTOR + " : " + inputEventAdaptorConfiguration.getName() + ", received " + System.getProperty("line.separator");
            inputEventAdaptorListener.setTracerPrefix(tracerPrefix);
        }

        try {
            return inputEventAdaptor.subscribe(inputEventAdaptorMessageConfiguration, inputEventAdaptorListener, inputEventAdaptorConfiguration, axisConfiguration);
        } catch (InputEventAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new InputEventAdaptorEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void unsubscribe(InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
                            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {
        AbstractInputEventAdaptor abstractInputEventAdaptor = this.eventAdaptorMap.get(inputEventAdaptorConfiguration.getType());
        // We do not throw an error when trying to unsubscribing from an input event adaptor that is not there.
        if (abstractInputEventAdaptor != null) {
            try {
                abstractInputEventAdaptor.unsubscribe(inputEventAdaptorMessageConfiguration, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId);
            } catch (InputEventAdaptorEventProcessingException e) {
                log.error(e.getMessage(), e);
                throw new InputEventAdaptorEventProcessingException(e.getMessage(), e);
            }
        }
    }

    @Override
    public InputEventAdaptorDto getEventAdaptorDto(String eventAdaptorType) {

        AbstractInputEventAdaptor abstractInputEventAdaptor = eventAdaptorMap.get(eventAdaptorType);
        if (abstractInputEventAdaptor != null) {
            return abstractInputEventAdaptor.getInputEventAdaptorDto();
        }
        return null;
    }


}
