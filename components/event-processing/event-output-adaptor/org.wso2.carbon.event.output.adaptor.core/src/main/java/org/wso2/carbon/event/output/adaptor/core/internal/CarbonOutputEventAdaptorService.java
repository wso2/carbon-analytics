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

package org.wso2.carbon.event.output.adaptor.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.internal.ds.OutputEventAdaptorServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.core.message.MessageDto;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventAdaptor service implementation.
 */
public class CarbonOutputEventAdaptorService implements OutputEventAdaptorService {

    private static Log log = LogFactory.getLog(CarbonOutputEventAdaptorService.class);
    private Map<String, AbstractOutputEventAdaptor> eventAdaptorMap;

    public CarbonOutputEventAdaptorService() {
        this.eventAdaptorMap = new ConcurrentHashMap();
    }

    public void registerEventAdaptor(
            AbstractOutputEventAdaptor abstractOutputEventAdaptor) {
        OutputEventAdaptorDto outputEventAdaptorDto = abstractOutputEventAdaptor.getOutputEventAdaptorDto();
        this.eventAdaptorMap.put(outputEventAdaptorDto.getEventAdaptorTypeName(), abstractOutputEventAdaptor);
        OutputEventAdaptorServiceValueHolder.getComponentContext().getBundleContext().registerService(AbstractOutputEventAdaptor.class.getName(), abstractOutputEventAdaptor, null);
    }

    public void unRegisterEventAdaptor(
            AbstractOutputEventAdaptor abstractOutputEventAdaptor) {
        OutputEventAdaptorDto outputEventAdaptorDto = abstractOutputEventAdaptor.getOutputEventAdaptorDto();
        this.eventAdaptorMap.remove(outputEventAdaptorDto.getEventAdaptorTypeName());
        //OutputEventAdaptorServiceValueHolder.getComponentContext().getBundleContext().ungetService(AbstractOutputEventAdaptor.class.getName(),abstractOutputEventAdaptor,null);
    }


    @Override
    public List<OutputEventAdaptorDto> getEventAdaptors() {
        List<OutputEventAdaptorDto> outputEventAdaptorDtos = new ArrayList<OutputEventAdaptorDto>();
        for (AbstractOutputEventAdaptor abstractEventAdaptor : this.eventAdaptorMap.values()) {
            outputEventAdaptorDtos.add(abstractEventAdaptor.getOutputEventAdaptorDto());
        }
        return outputEventAdaptorDtos;
    }

    @Override
    public MessageDto getEventAdaptorMessageDto(String eventAdaptorTypeName) {

        for (AbstractOutputEventAdaptor abstractOutputEventAdaptor : this.eventAdaptorMap.values()) {
            if (abstractOutputEventAdaptor.getOutputEventAdaptorDto().getEventAdaptorTypeName().equals(eventAdaptorTypeName)) {
                return abstractOutputEventAdaptor.getMessageDto();
            }
        }
        return null;
    }

    @Override
    public void publish(OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
                        OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration,
                        Object object, int tenantId) {
        AbstractOutputEventAdaptor outputEventAdaptor = this.eventAdaptorMap.get(outputEventAdaptorConfiguration.getType());
        try {
            outputEventAdaptor.publishCall(outputEventMessageConfiguration, object, outputEventAdaptorConfiguration,tenantId);
        } catch (OutputEventAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new OutputEventAdaptorEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration) {
        AbstractOutputEventAdaptor outputEventAdaptor = this.eventAdaptorMap.get(outputEventAdaptorConfiguration.getType());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            outputEventAdaptor.testConnection(outputEventAdaptorConfiguration,tenantId);
        } catch (OutputEventAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new OutputEventAdaptorEventProcessingException(e.getMessage(), e);
        }
    }


    @Override
    public OutputEventAdaptorDto getEventAdaptorDto(String eventAdaptorType) {

        AbstractOutputEventAdaptor abstractOutputEventAdaptor = eventAdaptorMap.get(eventAdaptorType);
        if (abstractOutputEventAdaptor != null) {
            return abstractOutputEventAdaptor.getOutputEventAdaptorDto();
        }
        return null;
    }


}
