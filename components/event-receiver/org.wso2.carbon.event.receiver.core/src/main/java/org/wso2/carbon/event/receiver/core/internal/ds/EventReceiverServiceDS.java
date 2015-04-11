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
package org.wso2.carbon.event.receiver.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverService;
import org.wso2.carbon.event.receiver.core.internal.EventStreamListenerImpl;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="eventReceiverService.component" immediate="true"
 * @scr.reference name="inputEventAdapter.service"
 * interface="org.wso2.carbon.event.input.adapter.core.InputEventAdapterService" cardinality="1..1"
 * policy="dynamic" bind="setInputEventAdapterService" unbind="unsetInputEventAdapterService"
 * @scr.reference name="input.event.adapter.tracker.service"
 * interface="org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory" cardinality="0..n"
 * policy="dynamic" bind="setEventAdapterType" unbind="unSetEventAdapterType"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class EventReceiverServiceDS {
    private static final Log log = LogFactory.getLog(EventReceiverServiceDS.class);

    public static List<String> inputEventAdapterTypes = new ArrayList<String>();

    protected void activate(ComponentContext context) {
        try {
            CarbonEventReceiverService carbonEventReceiverService = new CarbonEventReceiverService();
            EventReceiverServiceValueHolder.registerEventReceiverService(carbonEventReceiverService);
            context.getBundleContext().registerService(EventReceiverService.class.getName(), carbonEventReceiverService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventReceiverService.");
            }

            activateInactiveEventReceiverConfigurations(carbonEventReceiverService);
            context.getBundleContext().registerService(EventStreamListener.class.getName(), new EventStreamListenerImpl(), null);
        } catch (RuntimeException e) {
            log.error("Could not create EventReceiverService or EventReceiver : " + e.getMessage(), e);
        }
    }

    private void activateInactiveEventReceiverConfigurations(CarbonEventReceiverService carbonEventReceiverService) {
        inputEventAdapterTypes.addAll(EventReceiverServiceValueHolder.getInputEventAdapterService().getInputEventAdapterTypes());
        for (String type : inputEventAdapterTypes) {
            try {
                carbonEventReceiverService.activateInactiveEventReceiverConfigurationsForAdapter(type);
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }
        inputEventAdapterTypes.clear();
    }

    protected void setInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
        EventReceiverServiceValueHolder.registerInputEventAdapterService(inputEventAdapterService);
    }

    protected void unsetInputEventAdapterService(
            InputEventAdapterService inputEventAdapterService) {
        EventReceiverServiceValueHolder.registerInputEventAdapterService(null);
    }

    protected void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventReceiverServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    protected void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventReceiverServiceValueHolder.registerEventStatisticsService(null);
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

        if (EventReceiverServiceValueHolder.getCarbonEventReceiverService() != null) {
            try {
                EventReceiverServiceValueHolder.getCarbonEventReceiverService().activateInactiveEventReceiverConfigurationsForAdapter(inputEventAdapterFactory.getType());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            inputEventAdapterTypes.add(inputEventAdapterFactory.getType());
        }

    }

    protected void unSetEventAdapterType(InputEventAdapterFactory inputEventAdapterFactory) {

        if (EventReceiverServiceValueHolder.getCarbonEventReceiverService() != null) {
            try {
                EventReceiverServiceValueHolder.getCarbonEventReceiverService().deactivateActiveEventReceiverConfigurationsForAdapter(inputEventAdapterFactory.getType());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
