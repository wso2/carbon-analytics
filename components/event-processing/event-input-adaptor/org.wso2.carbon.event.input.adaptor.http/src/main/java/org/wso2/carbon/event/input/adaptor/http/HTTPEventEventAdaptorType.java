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

package org.wso2.carbon.event.input.adaptor.http;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.http.internal.ds.HTTPEventAdaptorServiceDS;
import org.wso2.carbon.event.input.adaptor.http.internal.util.HTTPEventAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HTTPEventEventAdaptorType extends AbstractInputEventAdaptor {

    private static final Log log = LogFactory.getLog(HTTPEventEventAdaptorType.class);
    private static HTTPEventEventAdaptorType httpEventAdaptor = new HTTPEventEventAdaptorType();
    private ResourceBundle resourceBundle;
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, InputEventAdaptorListener>> inputEventAdaptorListenerMap =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, InputEventAdaptorListener>>();

    private HTTPEventEventAdaptorType() {

    }


    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.TEXT);

        return supportInputMessageTypes;
    }

    /**
     * @return WSO2EventReceiver event adaptor instance
     */
    public static HTTPEventEventAdaptorType getInstance() {

        return httpEventAdaptor;
    }

    /**
     * @return name of the WSO2EventReceiver event adaptor
     */
    @Override
    protected String getName() {
        return HTTPEventAdaptorConstants.ADAPTOR_TYPE_HTTP;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.http.i18n.Resources", Locale.getDefault());
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

        // set topic
        Property topicProperty = new Property(HTTPEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        topicProperty.setDisplayName(
                resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC));
        topicProperty.setRequired(true);
        propertyList.add(topicProperty);

        return propertyList;

    }

    public String subscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration) {
        String subscriptionId = UUID.randomUUID().toString();

        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(HTTPEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        ConcurrentHashMap<String, InputEventAdaptorListener> topicSpecificListeners = inputEventAdaptorListenerMap.get(topic);

        if (topicSpecificListeners == null) {
            topicSpecificListeners = new ConcurrentHashMap<String, InputEventAdaptorListener>();
            if (null != inputEventAdaptorListenerMap.putIfAbsent(topic, topicSpecificListeners)) {
                topicSpecificListeners = inputEventAdaptorListenerMap.get(topic);
            }
            HTTPEventAdaptorServiceDS.registerDynamicEndpoint(topic);
        }

        topicSpecificListeners.put(subscriptionId, inputEventAdaptorListener);

        return subscriptionId;
    }

    public void unsubscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) {

        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(HTTPEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        Map<String, InputEventAdaptorListener> topicSpecificListeners = inputEventAdaptorListenerMap.get(topic);

        if (topicSpecificListeners != null) {
            topicSpecificListeners.remove(subscriptionId);

            if (topicSpecificListeners.isEmpty()) {
                HTTPEventAdaptorServiceDS.unregisterDynamicEndpoint(topic);
            }
        }
    }
}
