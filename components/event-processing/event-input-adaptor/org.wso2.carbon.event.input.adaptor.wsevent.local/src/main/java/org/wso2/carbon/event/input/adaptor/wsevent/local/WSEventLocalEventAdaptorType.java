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

package org.wso2.carbon.event.input.adaptor.wsevent.local;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.wsevent.local.internal.util.Axis2Util;
import org.wso2.carbon.event.input.adaptor.wsevent.local.internal.util.WSEventLocalAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;


public final class WSEventLocalEventAdaptorType extends AbstractInputEventAdaptor {

    private static final Log log = LogFactory.getLog(WSEventLocalEventAdaptorType.class);
    private static WSEventLocalEventAdaptorType wsEventLocalAdaptor = new WSEventLocalEventAdaptorType();
    private ResourceBundle resourceBundle;


    private WSEventLocalEventAdaptorType() {

    }


    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);

        return supportInputMessageTypes;
    }


    /**
     * @return WS Local event adaptor instance
     */
    public static WSEventLocalEventAdaptorType getInstance() {

        return wsEventLocalAdaptor;
    }

    /**
     * @return name of the WS Local event adaptor
     */
    @Override
    protected String getName() {
        return WSEventLocalAdaptorConstants.ADAPTOR_TYPE_WSEVENT_LOCAL;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.wsevent.local.i18n.Resources", Locale.getDefault());
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
        Property topicProperty = new Property(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME);
        topicProperty.setDisplayName(
                resourceBundle.getString(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_HINT_TOPIC_NAME));

        propertyList.add(topicProperty);

        return propertyList;

    }


    @Override
    public String subscribe(InputEventAdaptorMessageConfiguration inputEventMessageConfiguration,
                            InputEventAdaptorListener inputEventAdaptorListener,
                            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {

        String subscriptionId = UUID.randomUUID().toString();

        try {
            Axis2Util.registerAxis2Service(inputEventMessageConfiguration, inputEventAdaptorListener,
                                           inputEventAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new InputEventAdaptorEventProcessingException("Can not create " +
                                                               "the axis2 service to receive events", axisFault);
        }
        return subscriptionId;

    }

    @Override
    public void unsubscribe(InputEventAdaptorMessageConfiguration inputEventMessageConfiguration,
                            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {
        try {
            Axis2Util.removeOperation(inputEventMessageConfiguration, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new InputEventAdaptorEventProcessingException("Can not remove operation ", axisFault);
        }

    }



}
