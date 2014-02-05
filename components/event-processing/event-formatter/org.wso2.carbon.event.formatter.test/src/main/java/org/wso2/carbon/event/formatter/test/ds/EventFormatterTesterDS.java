/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.formatter.test.ds;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.test.EventSourceImpl;
import org.wso2.carbon.event.formatter.test.util.TestEventFormatterServiceHolder;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * this class is used to get the Transport Adaptor service.
 *
 * @scr.component name="eventFormatter.test.component" immediate="true"
 * @scr.reference name="eventFormatterService.service"
 * interface="org.wso2.carbon.event.formatter.core.EventFormatterService" cardinality="1..1"
 * policy="dynamic" bind="setEventFormatterService" unbind="unsetEventFormatterService"
 * @scr.reference name="configuration.contextService.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class EventFormatterTesterDS {
    private static final Log log = LogFactory.getLog(EventFormatterTesterDS.class);

    protected void activate(ComponentContext context) {

        log.info("*************************Activated*******************************");

        //TransportAdaptorService transportAdaptorService = TestEventFormatterServiceHolder.getInstance().getTransportAdaptorService();

//        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = new TransportAdaptorConfiguration();
//        InputTransportMessageConfiguration inputTransportMessageConfiguration = new InputTransportMessageConfiguration();

//        String streamName = "org.wso2.phone.retail.store";
//        String streamVersion = "1.2.0";
//
//        configureInputTransportAdaptor(transportAdaptorService, axisConfiguration, streamName, streamVersion,
//                inputTransportAdaptorConfiguration, inputTransportMessageConfiguration);
//        configureEventBuilder(eventFormatterService, axisConfiguration, inputTransportMessageConfiguration);
//
//        StreamDefinition streamDefinition = null;

//
//        try {
//            streamDefinition = new StreamDefinition(streamName, streamVersion);
//        } catch (MalformedStreamDefinitionException e) {
//            log.error("Error creating stream definition with id " + streamName + ":" + streamVersion);
//        }

//        TestBasicEventListener testBasicEventListener = new TestBasicEventListener();

        EventFormatterService eventFormatterService = TestEventFormatterServiceHolder.getInstance().getEventFormatterService();
        EventSourceImpl eventSource = new EventSourceImpl();
        ConfigurationContextService configurationContextService = TestEventFormatterServiceHolder.getInstance().getConfigurationContextService();
        AxisConfiguration axisConfiguration = configurationContextService.getClientConfigContext().getAxisConfiguration();


        try {
            // eventFormatterService.registerEventSource(axisConfiguration, eventSource);
            log.info("Successfully subscribed to Event Formatter");
        } catch (EventFormatterConfigurationException e) {
            log.error("Error when subscribing to event formatter:\n" + e);
        } catch (Throwable t) {
            log.error(t);
        }
    }

    private void configureEventFormatter(EventFormatterService eventFormatterService,
                                         AxisConfiguration axisConfiguration) {

    }

    private void configureEventBuilder(EventFormatterService eventFormatterService,
                                       AxisConfiguration axisConfiguration,
                                       InputTransportMessageConfiguration inputTransportMessageConfiguration) {
//        EventBuilderConfiguration<TupleInputMapping> eventBuilderConfiguration = new EventBuilderConfiguration<TupleInputMapping>();
//        eventBuilderConfiguration.setInputTransportMessageConfiguration(inputTransportMessageConfiguration);
//        try {
//            eventFormatterService.addEventBuilder(new TupleInputEventBuilder(eventBuilderConfiguration), axisConfiguration);
//            log.info("Successfully deployed event builder service tester");
//        } catch (EventFormatterConfigurationException e) {
//            log.error(e);
//        }
    }

//    private void configureInputTransportAdaptor(TransportAdaptorService transportAdaptorService,
//                                                AxisConfiguration axisConfiguration, String streamName, String streamVersion,
//                                                InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration, InputTransportMessageConfiguration inputTransportMessageConfiguration) {
//        if (inputTransportAdaptorConfiguration != null && inputTransportMessageConfiguration != null) {
//            inputTransportAdaptorConfiguration.setName("localAgentBroker");
//            inputTransportAdaptorConfiguration.setType("agent");
//            Map<String, String> transportAdaptorCommonProperties = new HashMap<String, String>();
//            transportAdaptorCommonProperties.put("receiverURL", "tcp://localhost:76111");
//            transportAdaptorCommonProperties.put("authenticatorURL", "ssl://localhost:77111");
//            transportAdaptorCommonProperties.put("username", "admin");
//            transportAdaptorCommonProperties.put("password", "admin");
//            inputTransportAdaptorConfiguration.setTransportAdaptorCommonProperties(transportAdaptorCommonProperties);
//
//            inputTransportMessageConfiguration = new InputTransportMessageConfiguration();
//            inputTransportMessageConfiguration.addInputMessageProperty("streamName", streamName);
//            inputTransportMessageConfiguration.addInputMessageProperty("version", streamVersion);
//
//            try {
//                transportAdaptorService.subscribe(inputTransportAdaptorConfiguration, inputTransportMessageConfiguration, null, axisConfiguration);
//            } catch (TransportAdaptorEventProcessingException e) {
//                log.error("Error subscribing to transport adaptor:\n" + e);
//            }
//        } else {
//            log.error("Cannot create input transport adaptor. Some parameters are null");
//        }
//    }

    protected void setEventFormatterService(EventFormatterService eventFormatterService) {
        TestEventFormatterServiceHolder.getInstance().registerEventFormatterService(eventFormatterService);
    }

    protected void unsetEventFormatterService(EventFormatterService eventFormatterService) {
        TestEventFormatterServiceHolder.getInstance().unregisterEventFormatterService(eventFormatterService);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        TestEventFormatterServiceHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {

        TestEventFormatterServiceHolder.getInstance().unregisterConfigurationContextService(configurationContextService);

    }

//    protected void setTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
//        TestEventFormatterServiceHolder.getInstance().registerTransportAdaptorService(transportAdaptorService);
//    }
//
//    protected void unsetTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
//        TestEventFormatterServiceHolder.getInstance().unregisterTransportAdaptorService(transportAdaptorService);
//    }
//
//    protected void setConfigurationContextService(
//            ConfigurationContextService configurationContextService) {
//        TestEventFormatterServiceHolder.getInstance().registerConfigurationContextService(configurationContextService);
//    }
//
//    protected void unsetConfigurationContextService(
//            ConfigurationContextService configurationContextService) {
//        TestEventFormatterServiceHolder.getInstance().unregisterConfigurationContextService(configurationContextService);
//    }
}
