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
package org.wso2.carbon.event.input.adapter.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.core.internal.ds.InputEventAdapterServiceValueHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventAdapter service implementation.
 */
public class CarbonInputEventAdapterService implements InputEventAdapterService {

    private static Log log = LogFactory.getLog(CarbonInputEventAdapterService.class);

    private Map<String, InputEventAdapterFactory> eventAdapterFactoryMap;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, InputAdapterRuntime>> tenantSpecificEventAdapters;
    private boolean startPollingTriggered = false;
    private boolean startTriggered = false;


    public CarbonInputEventAdapterService() {
        this.eventAdapterFactoryMap = new ConcurrentHashMap<String, InputEventAdapterFactory>();
        this.tenantSpecificEventAdapters = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, InputAdapterRuntime>>();
    }

    public void registerEventAdapterFactory(InputEventAdapterFactory inputEventAdapterFactory) {
        InputEventAdapterSchema inputEventAdapterSchema = inputEventAdapterFactory.getInputEventAdapterSchema();
        this.eventAdapterFactoryMap.put(inputEventAdapterSchema.getType(), inputEventAdapterFactory);
    }

    public void unRegisterEventAdapter(InputEventAdapterFactory inputEventAdapterFactory) {
        InputEventAdapterSchema inputEventAdapterSchema = inputEventAdapterFactory.getInputEventAdapterSchema();
        this.eventAdapterFactoryMap.remove(inputEventAdapterSchema.getType());
    }


    @Override
    public List<String> getInputEventAdapterTypes() {
        return new ArrayList<String>(eventAdapterFactoryMap.keySet());
    }

    /**
     * This method returns the event adapter dto for a specific event adapter type
     *
     * @param eventAdapterType
     * @return
     */
    @Override
    public InputEventAdapterSchema getInputEventAdapterSchema(String eventAdapterType) {
        InputEventAdapterFactory inputEventAdapterFactory = eventAdapterFactoryMap.get(eventAdapterType);
        if (inputEventAdapterFactory != null) {
            return inputEventAdapterFactory.getInputEventAdapterSchema();
        }
        return null;
    }

    @Override
    public void create(InputEventAdapterConfiguration inputEventAdapterConfiguration, InputEventAdapterSubscription inputEventAdapterSubscription) throws InputEventAdapterException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, InputAdapterRuntime> eventAdapters = tenantSpecificEventAdapters.get(tenantId);
        if (eventAdapters == null) {
            tenantSpecificEventAdapters.putIfAbsent(tenantId, new ConcurrentHashMap<String, InputAdapterRuntime>());
            eventAdapters = tenantSpecificEventAdapters.get(tenantId);
        }
        InputEventAdapterFactory adapterFactory = eventAdapterFactoryMap.get(inputEventAdapterConfiguration.getType());
        if (adapterFactory == null) {
            throw new InputEventAdapterException("Input Event Adapter not created as no adapter factory is registered " +
                    "with type " + inputEventAdapterConfiguration.getType());
        }
        if (inputEventAdapterConfiguration.getName() == null) {
            throw new InputEventAdapterException("Input Event Adapter name cannot by null, for the adapter type " +
                    inputEventAdapterConfiguration.getType());
        }
        if (eventAdapters.get(inputEventAdapterConfiguration.getName()) != null) {
            throw new InputEventAdapterException("Input Event Adapter not created as another adapter with same name '"
                    + inputEventAdapterConfiguration.getName() + "' already exist for tenant " + tenantId);
        }
        Map<String, String> globalProperties = InputEventAdapterServiceValueHolder.getGlobalAdapterConfigs().
                getAdapterConfig(inputEventAdapterConfiguration.getType()).getGlobalPropertiesAsMap();

        InputEventAdapter inputEventAdapter = adapterFactory.
                createEventAdapter(inputEventAdapterConfiguration, globalProperties);
        InputAdapterRuntime inputAdapterRuntime = new InputAdapterRuntime(inputEventAdapter, inputEventAdapterConfiguration.getName(),
                inputEventAdapterSubscription);

        eventAdapters.put(inputEventAdapterConfiguration.getName(), inputAdapterRuntime);
        if (startTriggered) {
            inputAdapterRuntime.start();
        }
        if (startPollingTriggered) {
            inputAdapterRuntime.startPolling();
        }
    }

    /**
     * publish testConnect message using the given event adapter.
     *
     * @param inputEventAdapterConfiguration - Configuration Details of the event adapter
     */
    @Override
    public void testConnection(InputEventAdapterConfiguration inputEventAdapterConfiguration)
            throws InputEventAdapterException, TestConnectionNotSupportedException {
        InputEventAdapter inputEventAdapter = null;
        TestConnectionAdapterListener adaptorSubscription = null;
        try {
            InputEventAdapterFactory inputEventAdapterFactory = this.eventAdapterFactoryMap.get(inputEventAdapterConfiguration.getType());
            InputEventAdapterFactory adapterFactory = eventAdapterFactoryMap.get(inputEventAdapterConfiguration.getType());
            if (adapterFactory == null) {
                throw new InputEventAdapterException("Input Event Adapter not created as no adapter factory is " +
                        "registered with type " + inputEventAdapterConfiguration.getType());
            }
            if (inputEventAdapterConfiguration.getName() == null) {
                throw new InputEventAdapterException("Input Event Adapter name cannot by null, for the adapter type "
                        + inputEventAdapterConfiguration.getType());
            }
            Map<String, String> globalProperties = InputEventAdapterServiceValueHolder.getGlobalAdapterConfigs().
                    getAdapterConfig(inputEventAdapterConfiguration.getType()).getGlobalPropertiesAsMap();
            inputEventAdapter = inputEventAdapterFactory.createEventAdapter(inputEventAdapterConfiguration, globalProperties);
            adaptorSubscription = new TestConnectionAdapterListener();
            inputEventAdapter.init(adaptorSubscription);
            inputEventAdapter.testConnect();
            inputEventAdapter.disconnect();
            inputEventAdapter.destroy();
        } finally {
            if (inputEventAdapter != null) {
                inputEventAdapter.destroy();
            }
        }
        RuntimeException exception = adaptorSubscription.getConnectionUnavailableException();
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public void destroy(String name) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, InputAdapterRuntime> eventAdapters = tenantSpecificEventAdapters.get(tenantId);
        if (eventAdapters == null) {
            return;
        }
        InputAdapterRuntime inputAdapterRuntime = eventAdapters.remove(name);
        if (inputAdapterRuntime != null) {
            inputAdapterRuntime.destroy();
        }
    }

    public ConcurrentHashMap<Integer, ConcurrentHashMap<String, InputAdapterRuntime>> getTenantSpecificEventAdapters() {
        return tenantSpecificEventAdapters;
    }

    @Override
    public void startPolling() {
        startPollingTriggered = true;

        for (Map.Entry<Integer, ConcurrentHashMap<String, InputAdapterRuntime>> pair : tenantSpecificEventAdapters.entrySet()) {
            Map<String, InputAdapterRuntime> map = pair.getValue();
            int tenantId = pair.getKey();

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
                for (InputAdapterRuntime inputAdapterRuntime : map.values()) {
                    inputAdapterRuntime.startPolling();
                }
            } catch (Exception e) {
                log.error("Unable to startTriggered event adapters for tenant :" + tenantId, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public void start() {
        this.startTriggered = true;

        for (Map.Entry<Integer, ConcurrentHashMap<String, InputAdapterRuntime>> pair : tenantSpecificEventAdapters.entrySet()) {
            Map<String, InputAdapterRuntime> map = pair.getValue();
            int tenantId = pair.getKey();

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
                for (InputAdapterRuntime inputAdapterRuntime : map.values()) {
                    inputAdapterRuntime.start();
                }
            } catch (Exception e) {
                log.error("Unable to start event adapters for tenant :" + tenantId, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public boolean isEventDuplicatedInCluster(String inputEventAdapterName) throws InputEventAdapterException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, InputAdapterRuntime> inputRuntimeMap = tenantSpecificEventAdapters.get(tenantId);
        if (inputRuntimeMap != null) {
            InputAdapterRuntime inputAdapterRuntime = inputRuntimeMap.get(inputEventAdapterName);
            if (inputAdapterRuntime != null) {
                return inputAdapterRuntime.isEventDuplicatedInCluster();
            }
        }
        throw new InputEventAdapterException("Adapter with name '" + inputEventAdapterName + "' not found");
    }

    @Override
    public void start(String inputEventAdapterName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, InputAdapterRuntime> inputRuntimeMap = tenantSpecificEventAdapters.get(tenantId);
        if (inputRuntimeMap != null) {
            InputAdapterRuntime inputAdapterRuntime = inputRuntimeMap.get(inputEventAdapterName);
            if (inputAdapterRuntime != null) {
                inputAdapterRuntime.start();
            }
        }
    }

}
