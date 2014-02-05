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

package org.wso2.carbon.event.input.adaptor.wso2event;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.wso2event.internal.ds.WSO2EventAdaptorServiceValueHolder;
import org.wso2.carbon.event.input.adaptor.wso2event.internal.util.WSO2EventAdaptorConstants;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WSO2EventEventAdaptorType extends AbstractInputEventAdaptor {

    private static final Log log = LogFactory.getLog(WSO2EventEventAdaptorType.class);
    private static WSO2EventEventAdaptorType wso2EventAdaptor = new WSO2EventEventAdaptorType();
    private ResourceBundle resourceBundle;
    private Map<InputEventAdaptorMessageConfiguration, Map<String, EventAdaptorConf>> inputEventAdaptorListenerMap =
            new ConcurrentHashMap<InputEventAdaptorMessageConfiguration, Map<String, EventAdaptorConf>>();
    private Map<InputEventAdaptorMessageConfiguration, StreamDefinition> inputStreamDefinitionMap =
            new ConcurrentHashMap<InputEventAdaptorMessageConfiguration, StreamDefinition>();
    private Map<String, Map<String, EventAdaptorConf>> streamIdEventAdaptorListenerMap =
            new ConcurrentHashMap<String, Map<String, EventAdaptorConf>>();

    private WSO2EventEventAdaptorType() {

        WSO2EventAdaptorServiceValueHolder.getDataBridgeSubscriberService().subscribe(new AgentTransportCallback());

    }


    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.WSO2EVENT);

        return supportInputMessageTypes;
    }

    /**
     * @return WSO2EventReceiver event adaptor instance
     */
    public static WSO2EventEventAdaptorType getInstance() {

        return wso2EventAdaptor;
    }

    /**
     * @return name of the WSO2EventReceiver event adaptor
     */
    @Override
    protected String getName() {
        return WSO2EventAdaptorConstants.ADAPTOR_TYPE_WSO2EVENT;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.wso2event.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return input adaptor configuration property list
     */
    @Override
    public List<Property> getInputAdaptorProperties() {
        return null;
    }

    /**
     * @return input message configuration property list
     */
    @Override
    public List<Property> getInputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set stream definition
        Property streamDefinitionProperty = new Property(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_NAME);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_NAME));
        streamDefinitionProperty.setRequired(true);


        // set stream version
        Property streamVersionProperty = new Property(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_VERSION);
        streamVersionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_VERSION));
        streamVersionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);
        propertyList.add(streamVersionProperty);

        return propertyList;

    }

    public String subscribe(InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
                            InputEventAdaptorListener inputEventAdaptorListener,
                            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {
        String subscriptionId = UUID.randomUUID().toString();

        EventAdaptorConf eventAdaptorConf = new EventAdaptorConf(inputEventAdaptorListener, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        if (!inputEventAdaptorListenerMap.keySet().contains(inputEventAdaptorMessageConfiguration)) {
            Map<String, EventAdaptorConf> map = new HashMap<String, EventAdaptorConf>();
            map.put(subscriptionId, eventAdaptorConf);
            inputEventAdaptorListenerMap.put(inputEventAdaptorMessageConfiguration, map);
        } else {
            inputEventAdaptorListenerMap.get(inputEventAdaptorMessageConfiguration).put(subscriptionId, eventAdaptorConf);
            StreamDefinition streamDefinition = inputStreamDefinitionMap.get(inputEventAdaptorMessageConfiguration);
            if (streamDefinition != null) {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(eventAdaptorConf.tenantId);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(eventAdaptorConf.tenantDomain);
                    inputEventAdaptorListener.addEventDefinitionCall(streamDefinition);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }


            }

        }

        return subscriptionId;
    }

    public void unsubscribe(InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
                            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {
        Map<String, EventAdaptorConf> map = inputEventAdaptorListenerMap.get(inputEventAdaptorMessageConfiguration);
        if (map != null) {
            map.remove(subscriptionId);
        }

    }


    private class AgentTransportCallback implements AgentCallback {


        @Override
        public void removeStream(StreamDefinition streamDefinition, Credentials credentials) {
            inputStreamDefinitionMap.remove(createTopic(streamDefinition));
            Map<String, EventAdaptorConf> inputEventAdaptorListenerMap = WSO2EventEventAdaptorType.this.inputEventAdaptorListenerMap.get(createTopic(streamDefinition));
            if (inputEventAdaptorListenerMap != null) {
                for (EventAdaptorConf eventAdaptorConf : inputEventAdaptorListenerMap.values()) {
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(eventAdaptorConf.tenantId);
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(eventAdaptorConf.tenantDomain);
                        eventAdaptorConf.inputEventAdaptorListener.removeEventDefinitionCall(streamDefinition);
                    } catch (InputEventAdaptorEventProcessingException e) {
                        log.error("Cannot remove Stream Definition from a eventAdaptorListener subscribed to " +
                                  streamDefinition.getStreamId(), e);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                }
            }
            streamIdEventAdaptorListenerMap.remove(streamDefinition.getStreamId());
        }

        @Override
        public void definedStream(StreamDefinition streamDefinition, Credentials credentials) {
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration = createTopic(streamDefinition);

            inputStreamDefinitionMap.put(inputEventAdaptorMessageConfiguration, streamDefinition);
            Map<String, EventAdaptorConf> eventAdaptorListeners = inputEventAdaptorListenerMap.get(inputEventAdaptorMessageConfiguration);
            if (eventAdaptorListeners == null) {
                eventAdaptorListeners = new HashMap<String, EventAdaptorConf>();
                inputEventAdaptorListenerMap.put(inputEventAdaptorMessageConfiguration, eventAdaptorListeners);

            }

            for (EventAdaptorConf eventAdaptorConf : eventAdaptorListeners.values()) {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(eventAdaptorConf.tenantId);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(eventAdaptorConf.tenantDomain);
                    eventAdaptorConf.inputEventAdaptorListener.addEventDefinitionCall(streamDefinition);
                } catch (InputEventAdaptorEventProcessingException e) {
                    log.error("Cannot send Stream Definition to a eventAdaptorListener subscribed to " +
                              streamDefinition.getStreamId(), e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }

            }
            streamIdEventAdaptorListenerMap.put(streamDefinition.getStreamId(), inputEventAdaptorListenerMap.get(inputEventAdaptorMessageConfiguration));
        }

        private InputEventAdaptorMessageConfiguration createTopic(StreamDefinition streamDefinition) {

            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration = new InputEventAdaptorMessageConfiguration();
            Map<String, String> inputMessageProperties = new HashMap<String, String>();
            inputMessageProperties.put(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_NAME, streamDefinition.getName());
            inputMessageProperties.put(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_VERSION, streamDefinition.getVersion());
            inputEventAdaptorMessageConfiguration.setInputMessageProperties(inputMessageProperties);

            return inputEventAdaptorMessageConfiguration;
        }

        @Override
        public void receive(List<Event> events, Credentials credentials) {
            for (Event event : events) {
                Map<String, EventAdaptorConf> eventAdaptorListeners = streamIdEventAdaptorListenerMap.get(event.getStreamId());
                if (eventAdaptorListeners == null) {
                    try {
                        definedStream(WSO2EventAdaptorServiceValueHolder.getDataBridgeSubscriberService().getStreamDefinition(credentials, event.getStreamId()), credentials);
                    } catch (StreamDefinitionNotFoundException e) {
                        log.error("No Stream definition store found for the event " +
                                  event.getStreamId(), e);
                        return;
                    } catch (StreamDefinitionStoreException e) {
                        log.error("No Stream definition store found when checking stream definition for " +
                                  event.getStreamId(), e);
                        return;
                    }
                    eventAdaptorListeners = streamIdEventAdaptorListenerMap.get(event.getStreamId());
                    if (eventAdaptorListeners == null) {
                        log.error("No event adaptor listeners for  " + event.getStreamId());
                        return;
                    }
                }
                for (EventAdaptorConf eventAdaptorConf : eventAdaptorListeners.values()) {
                    try {
                        //todo check for performance
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(eventAdaptorConf.tenantId);
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(eventAdaptorConf.tenantDomain);
                        eventAdaptorConf.inputEventAdaptorListener.onEventCall(event);
                    } catch (InputEventAdaptorEventProcessingException e) {
                        log.error("Cannot send event to a eventAdaptorListener subscribed to " +
                                  event.getStreamId(), e);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                }

            }
        }

    }

    private class EventAdaptorConf {

        private final InputEventAdaptorListener inputEventAdaptorListener;
        private final int tenantId;
        private final String tenantDomain;

        public EventAdaptorConf(InputEventAdaptorListener inputEventAdaptorListener, int tenantId,
                                String tenantDomain) {
            this.inputEventAdaptorListener = inputEventAdaptorListener;
            this.tenantId = tenantId;
            this.tenantDomain = tenantDomain;
        }
    }

}
