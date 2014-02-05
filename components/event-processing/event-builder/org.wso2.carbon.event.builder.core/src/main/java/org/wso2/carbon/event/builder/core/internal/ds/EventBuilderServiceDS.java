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

package org.wso2.carbon.event.builder.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.internal.CarbonEventBuilderService;
import org.wso2.carbon.event.builder.core.internal.EventStreamListenerImpl;
import org.wso2.carbon.event.builder.core.internal.InputEventAdaptorNotificationListenerImpl;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.processor.api.passthrough.PassthroughReceiverConfigurator;
import org.wso2.carbon.event.processor.api.receive.EventReceiver;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;


/**
 * @scr.component name="eventBuilderService.component" immediate="true"
 * @scr.reference name="inputEventAdaptor.service"
 * interface="org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setInputEventAdaptorService" unbind="unsetInputEventAdaptorService"
 * @scr.reference name="inputEventAdaptorManager.service"
 * interface="org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setInputEventAdaptorManagerService" unbind="unsetInputEventAdaptorManagerService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.manager.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 */
public class EventBuilderServiceDS {
    private static final Log log = LogFactory.getLog(EventBuilderServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            CarbonEventBuilderService carbonEventBuilderService = new CarbonEventBuilderService();
            EventBuilderServiceValueHolder.registerEventBuilderService(carbonEventBuilderService);
            context.getBundleContext().registerService(EventBuilderService.class.getName(), carbonEventBuilderService, null);
            log.info("Successfully deployed EventBuilderService.");

            context.getBundleContext().registerService(EventReceiver.class.getName(), carbonEventBuilderService, null);
            log.info("Successfully deployed EventBuilder EventReceiver.");

            context.getBundleContext().registerService(PassthroughReceiverConfigurator.class.getName(), carbonEventBuilderService, null);
            log.info("Successfully deployed EventBuilder PassthroughReceiverConfigurator.");

            EventBuilderServiceValueHolder.getInputEventAdaptorManagerService().registerDeploymentNotifier(new InputEventAdaptorNotificationListenerImpl());
            EventBuilderServiceValueHolder.getEventStreamService().registerEventStreamListener(new EventStreamListenerImpl());
        } catch (RuntimeException e) {
            log.error("Could not create EventBuilderService or EventReceiver : " + e.getMessage(), e);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error("Error when registering Event Adaptor deployment notifier : " + e.getMessage(), e);
        }
    }

    protected void setInputEventAdaptorService(InputEventAdaptorService inputEventAdaptorService) {
        EventBuilderServiceValueHolder.registerInputEventAdaptorService(inputEventAdaptorService);
    }

    protected void unsetInputEventAdaptorService(InputEventAdaptorService inputEventAdaptorService) {
        EventBuilderServiceValueHolder.registerInputEventAdaptorService(null);
    }

    protected void setInputEventAdaptorManagerService(InputEventAdaptorManagerService eventManagerService) {
        EventBuilderServiceValueHolder.registerInputEventAdaptorManagerService(eventManagerService);
    }

    protected void unsetInputEventAdaptorManagerService(InputEventAdaptorManagerService eventManagerService) {
        EventBuilderServiceValueHolder.registerInputEventAdaptorManagerService(null);
    }

    protected void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventBuilderServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    protected void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventBuilderServiceValueHolder.registerEventStatisticsService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventBuilderServiceValueHolder.registerRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventBuilderServiceValueHolder.registerRegistryService(null);
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        EventBuilderServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        EventBuilderServiceValueHolder.registerEventStreamService(null);
    }

}
