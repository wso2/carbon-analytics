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

package org.wso2.carbon.event.input.adaptor.local;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.local.internal.util.LocalEventAdaptorConstants;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class LocalEventAdaptorType extends AbstractInputEventAdaptor {

    private static LocalEventAdaptorType localEventAdaptor = new LocalEventAdaptorType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, InputEventAdaptorListener>>> inputEventAdaptorListenerMap =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, InputEventAdaptorListener>>>();

    private LocalEventAdaptorType() {

    }


    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.MAP);

        return supportInputMessageTypes;
    }


    /**
     * @return Local event adaptor instance
     */
    public static LocalEventAdaptorType getInstance() {

        return localEventAdaptor;
    }

    /**
     * @return name of the Local event adaptor
     */
    @Override
    protected String getName() {
        return LocalEventAdaptorConstants.ADAPTOR_TYPE_LOCAL;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.local.i18n.Resources", Locale.getDefault());
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

        // set receiver url
        Property topicProperty = new Property(LocalEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME);
        topicProperty.setDisplayName(
                resourceBundle.getString(LocalEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(LocalEventAdaptorConstants.ADAPTORMESSAGE_HINT_TOPIC_NAME));

        propertyList.add(topicProperty);

        return propertyList;

    }


    @Override
    public String subscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration) {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, ConcurrentHashMap<String, InputEventAdaptorListener>> adaptorDestinationSubscriptionsMap = inputEventAdaptorListenerMap.get(tenantId);
        if (adaptorDestinationSubscriptionsMap == null) {
            adaptorDestinationSubscriptionsMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, InputEventAdaptorListener>>();
            if (null != inputEventAdaptorListenerMap.putIfAbsent(tenantId, adaptorDestinationSubscriptionsMap)) {
                adaptorDestinationSubscriptionsMap = inputEventAdaptorListenerMap.get(tenantId);
            }
        }

        String subscriptionId = UUID.randomUUID().toString();
        ConcurrentHashMap<String, InputEventAdaptorListener> innerMap = new ConcurrentHashMap<String, InputEventAdaptorListener>();
        innerMap.put(subscriptionId, inputEventAdaptorListener);
        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(LocalEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME);
        adaptorDestinationSubscriptionsMap.put(topic, innerMap);

        return subscriptionId;

    }

    @Override
    public void unsubscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(LocalEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME);
        ConcurrentHashMap<String, ConcurrentHashMap<String, InputEventAdaptorListener>> adaptorDestinationSubscriptionsMap = inputEventAdaptorListenerMap.get(tenantId);
        if (adaptorDestinationSubscriptionsMap == null) {
            throw new InputEventAdaptorEventProcessingException("There is no subscription for " + topic + " for tenant " + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true));
        } else {
            ConcurrentHashMap<String, InputEventAdaptorListener> topicSpecificListenerMap = adaptorDestinationSubscriptionsMap.get(topic);
            if (topicSpecificListenerMap != null) {
                topicSpecificListenerMap.remove(subscriptionId);
            }
        }

    }

    public void onEvent(Object object, String topic, int tenantId) {

        ConcurrentHashMap<String, ConcurrentHashMap<String, InputEventAdaptorListener>> adaptorDestinationSubscriptionsMap = inputEventAdaptorListenerMap.get(tenantId);
        if (adaptorDestinationSubscriptionsMap != null) {
            ConcurrentHashMap<String, InputEventAdaptorListener> topicSpecificListenerMap = adaptorDestinationSubscriptionsMap.get(topic);
            if (topicSpecificListenerMap != null) {
                for (Map.Entry<String, InputEventAdaptorListener> entry : topicSpecificListenerMap.entrySet()) {
                    entry.getValue().onEvent(object);
                }
            }
        }
    }


}
