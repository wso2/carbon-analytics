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
package org.wso2.carbon.event.publisher.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.core.EventStreamListenerImpl;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.CarbonEventPublisherManagementService;
import org.wso2.carbon.event.publisher.core.internal.CarbonEventPublisherService;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Set;


/**
 * @scr.component name="eventPublisherService.component" immediate="true"
 * @scr.reference name="eventAdapter.service"
 * interface="org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdapterService" unbind="unsetEventAdapterService"
 * @scr.reference name="output.event.adapter.tracker.service"
 * interface="org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory" cardinality="0..n"
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
 */
public class EventPublisherServiceDS {
    private static final Log log = LogFactory.getLog(EventPublisherServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            checkIsStatsEnabled();
            CarbonEventPublisherService carbonEventPublisherService = new CarbonEventPublisherService();
            EventPublisherServiceValueHolder.registerPublisherService(carbonEventPublisherService);

            CarbonEventPublisherManagementService carbonEventPublisherManagementService = new CarbonEventPublisherManagementService();
            EventPublisherServiceValueHolder.getEventManagementService().subscribe(carbonEventPublisherManagementService);
            EventPublisherServiceValueHolder.registerPublisherManagementService(carbonEventPublisherManagementService);

            context.getBundleContext().registerService(EventPublisherService.class.getName(), carbonEventPublisherService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventPublisherService");
            }

            activateInactiveEventPublisherConfigurations(carbonEventPublisherService);

            context.getBundleContext().registerService(EventStreamListener.class.getName(), new EventStreamListenerImpl(), null);

        } catch (RuntimeException e) {
            log.error("Could not create EventPublisherService : " + e.getMessage(), e);
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
        EventPublisherServiceValueHolder.setGlobalStatisticsEnabled(true);
    }

    private void activateInactiveEventPublisherConfigurations(CarbonEventPublisherService carbonEventPublisherService) {
        Set<String> outputEventAdapterTypes = EventPublisherServiceValueHolder.getOutputEventAdapterTypes();
        outputEventAdapterTypes.addAll(EventPublisherServiceValueHolder.getOutputEventAdapterService().getOutputEventAdapterTypes());
        for (String type : outputEventAdapterTypes) {
            try {
                carbonEventPublisherService.activateInactiveEventPublisherConfigurationsForAdapter(type);
            } catch (EventPublisherConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    protected void setEventAdapterService(
            OutputEventAdapterService outputEventAdapterService) {
        EventPublisherServiceValueHolder.registerEventAdapterService(outputEventAdapterService);
    }

    protected void unsetEventAdapterService(
            OutputEventAdapterService outputEventAdapterService) {
        EventPublisherServiceValueHolder.getOutputEventAdapterTypes().clear();
        EventPublisherServiceValueHolder.registerEventAdapterService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventPublisherServiceValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventPublisherServiceValueHolder.unSetRegistryService();
    }

    public void setEventStreamService(EventStreamService eventStreamService) {
        EventPublisherServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    public void unsetEventStreamService(EventStreamService eventStreamService) {
        EventPublisherServiceValueHolder.registerEventStreamService(null);
    }

    protected void setEventAdapterType(OutputEventAdapterFactory outputEventAdapterFactory) {
        EventPublisherServiceValueHolder.addOutputEventAdapterType(outputEventAdapterFactory.getType());
        if (EventPublisherServiceValueHolder.getCarbonEventPublisherService() != null) {
            try {
                EventPublisherServiceValueHolder.getCarbonEventPublisherService().activateInactiveEventPublisherConfigurationsForAdapter(outputEventAdapterFactory.getType());
            } catch (EventPublisherConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    protected void unSetEventAdapterType(OutputEventAdapterFactory outputEventAdapterFactory) {
        EventPublisherServiceValueHolder.removeOutputEventAdapterType(outputEventAdapterFactory.getType());
        if (EventPublisherServiceValueHolder.getCarbonEventPublisherService() != null) {
            try {
                EventPublisherServiceValueHolder.getCarbonEventPublisherService().deactivateActiveEventPublisherConfigurationsForAdapter(outputEventAdapterFactory.getType());
            } catch (EventPublisherConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventPublisherServiceValueHolder.setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventPublisherServiceValueHolder.setConfigurationContextService(null);

    }

    protected void setEventManagementService(EventManagementService eventManagementService) {
        EventPublisherServiceValueHolder.registerEventManagementService(eventManagementService);

    }

    protected void unsetEventManagementService(EventManagementService eventManagementService) {
        EventPublisherServiceValueHolder.registerEventManagementService(null);
        eventManagementService.unsubscribe(EventPublisherServiceValueHolder.getCarbonEventPublisherManagementService());

    }


}
