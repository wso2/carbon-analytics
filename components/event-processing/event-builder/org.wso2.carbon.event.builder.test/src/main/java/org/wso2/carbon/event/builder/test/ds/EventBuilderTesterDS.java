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

package org.wso2.carbon.event.builder.test.ds;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputStreamConfiguration;
import org.wso2.carbon.event.builder.test.TestBasicEventListener;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.config.InternalInputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.processor.api.receive.EventReceiver;
import org.wso2.carbon.event.processor.api.receive.exception.EventReceiverException;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * this class is used to get the Transport Adaptor service.
 *
 * @scr.component name="eventBuilder.test.component" immediate="true"
 * @scr.reference name="transportservice.service"
 * interface="org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setInputEventAdaptorService" unbind="unsetInputEventAdaptorService"
 * @scr.reference name="eventReceiver.service"
 * interface="org.wso2.carbon.event.processor.api.receive.EventReceiver" cardinality="1..1"
 * policy="dynamic" bind="notifyEventReceiver" unbind="notifyRemovalEventReceiver"
 * @scr.reference name="eventBuilder.service"
 * interface="org.wso2.carbon.event.builder.core.EventBuilderService" cardinality="1..1"
 * policy="dynamic" bind="setEventBuilderService" unbind="unsetEventBuilderService"
 * @scr.reference name="configuration.contextService.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class EventBuilderTesterDS {
    private static final Log log = LogFactory.getLog(EventBuilderTesterDS.class);

    protected void activate(ComponentContext context) {
        EventReceiver eventReceiver = TestEventBuilderServiceHolder.getInstance().getEventReceiverList().get(0);
        InputEventAdaptorService inputEventAdaptorService = TestEventBuilderServiceHolder.getInstance().getInputEventAdaptorService();
        ConfigurationContextService configurationContextService = TestEventBuilderServiceHolder.getInstance().getConfigurationContextService();
        AxisConfiguration axisConfiguration = configurationContextService.getClientConfigContext().getAxisConfiguration();
        InputEventAdaptorConfiguration InputEventAdaptorConfiguration = new InputEventAdaptorConfiguration();
        InputEventAdaptorMessageConfiguration inputTransportMessageConfiguration = new InputEventAdaptorMessageConfiguration();

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fromStreamName = "analytics_Statistics";
        String fromStreamVersion = "1.2.0";
        String toStreamName = "kpiStream";
        String toStreamVersion = "1.0.0";

        inputTransportMessageConfiguration.addInputMessageProperty("streamName", fromStreamName);
        inputTransportMessageConfiguration.addInputMessageProperty("version", fromStreamVersion);
        configureInputEventAdaptor(inputEventAdaptorService, axisConfiguration, fromStreamName, fromStreamVersion,
                InputEventAdaptorConfiguration, inputTransportMessageConfiguration);
        configureEventBuilder(TestEventBuilderServiceHolder.getInstance().getEventBuilderService(), axisConfiguration, inputTransportMessageConfiguration, InputEventAdaptorConfiguration, toStreamName, toStreamVersion);

        StreamDefinition streamDefinition;
        try {
            streamDefinition = new StreamDefinition(fromStreamName, fromStreamVersion);
            log.info("[TEST-Module] Successfully deployed event builder service tester");

            TestBasicEventListener testBasicEventListener = new TestBasicEventListener();
            eventReceiver.subscribe(streamDefinition.getStreamId(), testBasicEventListener, tenantId);
            log.info("[TEST-Module] Successfully subscribed to event builder.");

        } catch (MalformedStreamDefinitionException e) {
            log.error("[TEST-Module] Error creating stream definition with id " + fromStreamName + ":" + fromStreamVersion);
        } catch (EventReceiverException e) {
            log.error("[TEST-Module] Error when subscribing to event receiver:\n" + e);
        }
    }

    private void configureEventBuilder(EventBuilderService eventBuilderService,
                                       AxisConfiguration axisConfiguration,
                                       InputEventAdaptorMessageConfiguration inputTransportMessageConfiguration,
                                       InputEventAdaptorConfiguration InputEventAdaptorConfiguration,
                                       String toStreamName, String toStreamVersion) {

        EventBuilderConfiguration eventBuilderConfiguration =
                new EventBuilderConfiguration();
        InputStreamConfiguration inputStreamConfiguration = new InputStreamConfiguration();
        inputStreamConfiguration.setInputEventAdaptorMessageConfiguration(inputTransportMessageConfiguration);
        inputStreamConfiguration.setInputEventAdaptorName(InputEventAdaptorConfiguration.getName());
        inputStreamConfiguration.setInputEventAdaptorType(InputEventAdaptorConfiguration.getType());
        eventBuilderConfiguration.setInputStreamConfiguration(inputStreamConfiguration);
        eventBuilderConfiguration.setToStreamName(toStreamName);
        eventBuilderConfiguration.setToStreamVersion(toStreamVersion);

        try {
            eventBuilderService.deployEventBuilderConfiguration(eventBuilderConfiguration, axisConfiguration);
        } catch (EventBuilderConfigurationException e) {
            log.error(e);
        }
    }

    private void configureInputEventAdaptor(
            InputEventAdaptorService inputEventAdaptorService,
            AxisConfiguration axisConfiguration,
            String streamName, String streamVersion,
            InputEventAdaptorConfiguration InputEventAdaptorConfiguration,
            InputEventAdaptorMessageConfiguration inputTransportMessageConfiguration) {
        if (InputEventAdaptorConfiguration != null && inputTransportMessageConfiguration != null) {
            InputEventAdaptorConfiguration.setName("localEventReceiver");
            InputEventAdaptorConfiguration.setType("wso2event-receiver");

            InternalInputEventAdaptorConfiguration internalInputEventAdaptorConfiguration = new InternalInputEventAdaptorConfiguration();
            internalInputEventAdaptorConfiguration.addEventAdaptorProperty("receiverURL", "tcp://localhost:76111");
            internalInputEventAdaptorConfiguration.addEventAdaptorProperty("authenticatorURL", "ssl://localhost:77111");
            internalInputEventAdaptorConfiguration.addEventAdaptorProperty("username", "admin");
            internalInputEventAdaptorConfiguration.addEventAdaptorProperty("password", "admin");
            InputEventAdaptorConfiguration.setInputConfiguration(internalInputEventAdaptorConfiguration);

            inputTransportMessageConfiguration.addInputMessageProperty("streamName", streamName);
            inputTransportMessageConfiguration.addInputMessageProperty("version", streamVersion);

            try {
                inputEventAdaptorService.subscribe(InputEventAdaptorConfiguration, inputTransportMessageConfiguration, null, axisConfiguration);
            } catch (InputEventAdaptorEventProcessingException e) {
                log.error("Error subscribing to transport adaptor:\n" + e);
            }
        } else {
            log.error("Cannot create input transport adaptor. Some parameters are null");
        }
    }

    public void notifyEventReceiver(EventReceiver eventReceiver) {
        TestEventBuilderServiceHolder.addEventReceiver(eventReceiver);
//        eventReceiver.subscribeNotificationListener(new EventReceiverStreamNotificationListenerImpl(eventReceiver));
    }

    protected void setEventBuilderService(EventBuilderService eventBuilderService) {
        TestEventBuilderServiceHolder.getInstance().registerEventBuilderService(eventBuilderService);
    }

    public void notifyRemovalEventReceiver(EventReceiver eventReceiver) {
        TestEventBuilderServiceHolder.removeEventReceiver(eventReceiver);
    }

    protected void unsetEventBuilderService(EventBuilderService eventBuilderService) {
        TestEventBuilderServiceHolder.getInstance().unregisterEventBuilderService(eventBuilderService);
    }

    protected void setInputEventAdaptorService(
            InputEventAdaptorService inputEventAdaptorService) {
        TestEventBuilderServiceHolder.getInstance().registerInputEventAdaptorService(inputEventAdaptorService);
    }

    protected void unsetInputEventAdaptorService(
            InputEventAdaptorService inputEventAdaptorService) {
        TestEventBuilderServiceHolder.getInstance().unregisterInputEventAdaptorService(inputEventAdaptorService);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        TestEventBuilderServiceHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        TestEventBuilderServiceHolder.getInstance().unregisterConfigurationContextService(configurationContextService);
    }
}
