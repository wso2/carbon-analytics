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
package org.wso2.carbon.event.output.adapter.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.*;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.core.internal.ds.OutputEventAdapterServiceValueHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventAdapter service implementation.
 */
public class CarbonOutputEventAdapterService implements OutputEventAdapterService {

    private static Log log = LogFactory.getLog(CarbonOutputEventAdapterService.class);
    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);

    private final Map<String, OutputEventAdapterFactory> eventAdapterFactoryMap;
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, OutputAdapterRuntime>> tenantSpecificEventAdapters;


    public CarbonOutputEventAdapterService() {
        this.eventAdapterFactoryMap = new ConcurrentHashMap<String, OutputEventAdapterFactory>();
        this.tenantSpecificEventAdapters = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, OutputAdapterRuntime>>();
    }

    public void registerEventAdapterFactory(OutputEventAdapterFactory outputEventAdapterFactory) {
        OutputEventAdapterSchema outputEventAdapterSchema = outputEventAdapterFactory.getOutputEventAdapterSchema();
        this.eventAdapterFactoryMap.put(outputEventAdapterSchema.getType(), outputEventAdapterFactory);
    }

    public void unRegisterEventAdapter(OutputEventAdapterFactory outputEventAdapterFactory) {
        OutputEventAdapterSchema outputEventAdapterSchema = outputEventAdapterFactory.getOutputEventAdapterSchema();
        this.eventAdapterFactoryMap.remove(outputEventAdapterSchema.getType());
    }


    @Override
    public List<String> getOutputEventAdapterTypes() {
        return new ArrayList<String>(eventAdapterFactoryMap.keySet());
    }

    /**
     * This method returns the event adapter dto for a specific event adapter type
     *
     * @param eventAdapterType
     * @return
     */
    @Override
    public OutputEventAdapterSchema getOutputEventAdapterSchema(String eventAdapterType) {
        OutputEventAdapterFactory outputEventAdapterFactory = eventAdapterFactoryMap.get(eventAdapterType);
        if (outputEventAdapterFactory != null) {
            return outputEventAdapterFactory.getOutputEventAdapterSchema();
        }
        return null;
    }

    @Override
    public void create(OutputEventAdapterConfiguration outputEventAdapterConfiguration) throws OutputEventAdapterException {
        int tenantId= PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, OutputAdapterRuntime> eventAdapters = tenantSpecificEventAdapters.get(tenantId);
        if (eventAdapters == null) {
            tenantSpecificEventAdapters.putIfAbsent(tenantId, new ConcurrentHashMap<String, OutputAdapterRuntime>());
            eventAdapters = tenantSpecificEventAdapters.get(tenantId);
        }
        OutputEventAdapterFactory adapterFactory = eventAdapterFactoryMap.get(outputEventAdapterConfiguration.getType());
        if (adapterFactory == null) {
            throw new OutputEventAdapterException("Output Event Adapter not created as no adapter factory is registered " +
                    "with type " + outputEventAdapterConfiguration.getType());
        }
        if (outputEventAdapterConfiguration.getName() == null) {
            throw new OutputEventAdapterException("Output Event Adapter name cannot by null, for the adapter type " +
                    outputEventAdapterConfiguration.getType());
        }
        if (eventAdapters.get(outputEventAdapterConfiguration.getName()) != null) {
            throw new OutputEventAdapterException("Output Event Adapter not created as another adapter with same name '"
                    + outputEventAdapterConfiguration.getName() + "' already exist for tenant " + tenantId);
        }
        //check if all the required properties are given here
        List<Property> staticPropertyList = adapterFactory.getStaticPropertyList();
        if(staticPropertyList != null){
            Map<String,String> staticPropertyMap = outputEventAdapterConfiguration.getStaticProperties();
            for (Property property: staticPropertyList){
                if(property.isRequired()){
                    if(staticPropertyMap == null){
                        throw new OutputEventAdapterException("Output Event Adapter not created as the 'staticProperties' are null, " +
                                "which means, the required property "+property.getPropertyName()+" is not  being set, for the adapter type " +
                                outputEventAdapterConfiguration.getType());
                    }
                    if(staticPropertyMap.get(property.getPropertyName()) == null){
                        throw new OutputEventAdapterException("Output Event Adapter not created as the required property: "+property.getPropertyName()+
                                " is not set, for the adapter type " +outputEventAdapterConfiguration.getType());
                    }
                }
            }
        }
        Map<String, String> globalProperties = OutputEventAdapterServiceValueHolder.getGlobalAdapterConfigs().
                getAdapterConfig(outputEventAdapterConfiguration.getType()).getGlobalPropertiesAsMap();
        eventAdapters.put(outputEventAdapterConfiguration.getName(), new OutputAdapterRuntime(adapterFactory.
                createEventAdapter(outputEventAdapterConfiguration, globalProperties), outputEventAdapterConfiguration.getName()));
    }

    /**
     * publishes the message using the given event adapter to the given topic.
     *  @param name              - name of the event adapter
     * @param dynamicProperties
     * @param message
     */
    @Override
    public void publish(String name, Map<String, String> dynamicProperties, Object message) {
        int tenantId= PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, OutputAdapterRuntime> eventAdapters = tenantSpecificEventAdapters.get(tenantId);
        if (eventAdapters == null) {
            throw new OutputEventAdapterRuntimeException("Event not published as no Output Event Adapter found with for" +
                    " tenant id " + tenantId);
        }
        OutputAdapterRuntime outputAdapterRuntime = eventAdapters.get(name);
        if (outputAdapterRuntime == null) {
            throw new OutputEventAdapterRuntimeException("Event not published as no Output Event Adapter found with name" +
                    " '" + name + "' for tenant id " + tenantId);
        }
        outputAdapterRuntime.publish(message, dynamicProperties);
    }

    /**
     * publish testConnect message using the given event adapter.
     *
     * @param outputEventAdapterConfiguration - Configuration Details of the event adapter
     */
    @Override
    public void testConnection(OutputEventAdapterConfiguration outputEventAdapterConfiguration)
            throws OutputEventAdapterException, TestConnectionNotSupportedException {
        OutputEventAdapter outputEventAdapter = null;
        try {
            OutputEventAdapterFactory outputEventAdapterFactory = this.eventAdapterFactoryMap.get(outputEventAdapterConfiguration.getType());
            OutputEventAdapterFactory adapterFactory = eventAdapterFactoryMap.get(outputEventAdapterConfiguration.getType());
            if (adapterFactory == null) {
                throw new OutputEventAdapterException("Output Event Adapter not created as no adapter factory is " +
                        "registered with type " + outputEventAdapterConfiguration.getType());
            }
            if (outputEventAdapterConfiguration.getName() == null) {
                throw new OutputEventAdapterException("Output Event Adapter name cannot by null, for the adapter type "
                        + outputEventAdapterConfiguration.getType());
            }
            Map<String, String> globalProperties = OutputEventAdapterServiceValueHolder.getGlobalAdapterConfigs().
                    getAdapterConfig(outputEventAdapterConfiguration.getType()).getGlobalPropertiesAsMap();
            outputEventAdapter = outputEventAdapterFactory.createEventAdapter(outputEventAdapterConfiguration, globalProperties);
            outputEventAdapter.init();
            outputEventAdapter.testConnect();
            outputEventAdapter.disconnect();
            outputEventAdapter.destroy();
        } finally {
            if (outputEventAdapter != null) {
                outputEventAdapter.destroy();
            }
        }
    }

    @Override
    public void destroy(String name) {
        int tenantId= PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, OutputAdapterRuntime> eventAdapters = tenantSpecificEventAdapters.get(tenantId);
        if (eventAdapters == null) {
            return;
        }
        OutputAdapterRuntime outputAdapterRuntime = eventAdapters.remove(name);
        if (outputAdapterRuntime != null) {
            outputAdapterRuntime.destroy();
        }
    }

}
