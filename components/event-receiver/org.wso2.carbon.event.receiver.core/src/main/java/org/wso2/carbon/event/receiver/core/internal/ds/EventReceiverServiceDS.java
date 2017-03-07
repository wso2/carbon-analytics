/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.receiver.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverManagementService;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverService;
import org.wso2.carbon.event.receiver.core.internal.EventStreamListenerImpl;
import org.wso2.carbon.event.receiver.core.internal.tenantmgt.TenantLazyLoaderValve;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.Set;

/**
 * @scr.component name="eventReceiverService.component" immediate="true"
 * @scr.reference name="inputEventAdapter.service"
 * interface="org.wso2.carbon.event.input.adapter.core.InputEventAdapterService" cardinality="1..1"
 * policy="dynamic" bind="setInputEventAdapterService" unbind="unsetInputEventAdapterService"
 * @scr.reference name="input.event.adapter.tracker.service"
 * interface="org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory" cardinality="0..n"
 * policy="dynamic" bind="setEventAdapterType" unbind="unSetEventAdapterType"
 * @scr.reference name="eventManagement.service"
 * interface="org.wso2.carbon.event.processor.manager.core.EventManagementService" cardinality="1..1"
 * policy="dynamic" bind="setEventManagementService" unbind="unsetEventManagementService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 */
public class EventReceiverServiceDS {
    private static final Log log = LogFactory.getLog(EventReceiverServiceDS.class);

    protected void activate(ComponentContext context) {
        try {

            checkIsStatsEnabled();
            CarbonEventReceiverService carbonEventReceiverService = new CarbonEventReceiverService();
            EventReceiverServiceValueHolder.registerEventReceiverService(carbonEventReceiverService);

            CarbonEventReceiverManagementService carbonEventReceiverManagementService = new CarbonEventReceiverManagementService();
            EventReceiverServiceValueHolder.getEventManagementService().subscribe(carbonEventReceiverManagementService);

            EventReceiverServiceValueHolder.registerReceiverManagementService(carbonEventReceiverManagementService);

            context.getBundleContext().registerService(EventReceiverService.class.getName(), carbonEventReceiverService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventReceiverService.");
            }

            activateInactiveEventReceiverConfigurations(carbonEventReceiverService);
            context.getBundleContext().registerService(EventStreamListener.class.getName(),
                    new EventStreamListenerImpl(), null);
            ArrayList<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
            valves.add(new TenantLazyLoaderValve());
            TomcatValveContainer.addValves(valves);
        } catch (Throwable e) {
            log.error("Could not create EventReceiverService or EventReceiver : " + e.getMessage(), e);
        }
    }

    private void checkIsStatsEnabled() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        String confStatisticsReporterDisabled = config.getFirstProperty("StatisticsReporterDisabled");
        if (!"".equals(confStatisticsReporterDisabled)) {
            boolean disabled = Boolean.valueOf(confStatisticsReporterDisabled);
            if (disabled) {
                return;
            }
        }
        EventReceiverServiceValueHolder.setGlobalStatisticsEnabled(true);
    }

    private void activateInactiveEventReceiverConfigurations(CarbonEventReceiverService carbonEventReceiverService) {
        Set<String> inputEventAdapterTypes = EventReceiverServiceValueHolder.getInputEventAdapterTypes();
        inputEventAdapterTypes.addAll(EventReceiverServiceValueHolder.getInputEventAdapterService().getInputEventAdapterTypes());
        for (String type : inputEventAdapterTypes) {
            try {
                carbonEventReceiverService.activateInactiveEventReceiverConfigurationsForAdapter(type);
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    protected void setInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
        EventReceiverServiceValueHolder.registerInputEventAdapterService(inputEventAdapterService);
    }

    protected void unsetInputEventAdapterService(
            InputEventAdapterService inputEventAdapterService) {
        EventReceiverServiceValueHolder.getInputEventAdapterTypes().clear();
        EventReceiverServiceValueHolder.registerInputEventAdapterService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventReceiverServiceValueHolder.registerRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventReceiverServiceValueHolder.registerRegistryService(null);
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        EventReceiverServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        EventReceiverServiceValueHolder.registerEventStreamService(null);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventReceiverServiceValueHolder.setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventReceiverServiceValueHolder.setConfigurationContextService(null);

    }

    protected void setEventAdapterType(InputEventAdapterFactory inputEventAdapterFactory) {
        EventReceiverServiceValueHolder.addInputEventAdapterType(inputEventAdapterFactory.getType());
        if (EventReceiverServiceValueHolder.getCarbonEventReceiverService() != null) {
            try {
                EventReceiverServiceValueHolder.getCarbonEventReceiverService().activateInactiveEventReceiverConfigurationsForAdapter(inputEventAdapterFactory.getType());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    protected void unSetEventAdapterType(InputEventAdapterFactory inputEventAdapterFactory) {
        EventReceiverServiceValueHolder.removeInputEventAdapterType(inputEventAdapterFactory.getType());
        if (EventReceiverServiceValueHolder.getCarbonEventReceiverService() != null) {
            try {
                EventReceiverServiceValueHolder.getCarbonEventReceiverService().deactivateActiveEventReceiverConfigurationsForAdapter(inputEventAdapterFactory.getType());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    protected void setEventManagementService(EventManagementService eventManagementService) {
        EventReceiverServiceValueHolder.registerEventManagementService(eventManagementService);

    }

    protected void unsetEventManagementService(EventManagementService eventManagementService) {
        EventReceiverServiceValueHolder.registerEventManagementService(null);
        eventManagementService.unsubscribe(EventReceiverServiceValueHolder.getCarbonEventReceiverManagementService());

    }

    protected void setRealmService(RealmService realmService) {
        EventReceiverServiceValueHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
    }
}
