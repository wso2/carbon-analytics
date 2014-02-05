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

package org.wso2.carbon.event.output.adaptor.wsevent.local;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.wsevent.local.internal.ds.WSEventLocalAdaptorServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.wsevent.local.internal.util.WSEventLocalAdaptorConstants;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;


public final class WSEventLocalAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(WSEventLocalAdaptorType.class);
    private static WSEventLocalAdaptorType wsEventLocalAdaptor = new WSEventLocalAdaptorType();
    private ResourceBundle resourceBundle;

    private WSEventLocalAdaptorType() {

    }


    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.XML);

        return supportOutputMessageTypes;
    }

    /**
     * @return WS Local event adaptor instance
     */
    public static WSEventLocalAdaptorType getInstance() {

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
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.wsevent.local.i18n.Resources", Locale.getDefault());
    }


    @Override
    public List<Property> getOutputAdaptorProperties() {
        return null;
    }

    @Override
    public List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set receiver url event adaptor
        Property topicProperty = new Property(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME);
        topicProperty.setDisplayName(
                resourceBundle.getString(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_HINT_TOPIC_NAME));

        propertyList.add(topicProperty);

        return propertyList;
    }


    @Override
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        EventBroker eventAdaptor = WSEventLocalAdaptorServiceValueHolder.getEventBroker();
        Message eventMessage = new Message();
        eventMessage.setMessage(((OMElement) message));
        try {
            eventAdaptor.publishRobust(eventMessage, outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME));
        } catch (EventBrokerException e) {
            throw new OutputEventAdaptorEventProcessingException("Can not publish the to local broker ", e);
        }

    }

    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        String testMessage = " <eventAdaptorConfigurationTest>\n" +
                             "   <message>This is a test message.</message>\n" +
                             "   </eventAdaptorConfigurationTest>";
        try {
            XMLStreamReader reader1 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(testMessage.getBytes()));
            StAXOMBuilder builder1 = new StAXOMBuilder(reader1);
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration = new OutputEventAdaptorMessageConfiguration();
            Map<String, String> propertyList = new ConcurrentHashMap<String, String>();
            outputEventAdaptorMessageConfiguration.setOutputMessageProperties(propertyList);

            propertyList.put(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME, "test");

            publish(outputEventAdaptorMessageConfiguration, builder1.getDocumentElement(), outputEventAdaptorConfiguration, tenantId);

        } catch (XMLStreamException e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }
    }
}
