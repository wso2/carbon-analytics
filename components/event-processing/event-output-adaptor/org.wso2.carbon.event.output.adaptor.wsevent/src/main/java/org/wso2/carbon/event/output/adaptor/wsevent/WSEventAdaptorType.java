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

package org.wso2.carbon.event.output.adaptor.wsevent;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.wsevent.internal.ds.WSEventAdaptorServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.wsevent.internal.util.WSEventAdaptorConstants;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class WSEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(WSEventAdaptorType.class);
    private static WSEventAdaptorType wsEventAdaptor = new WSEventAdaptorType();
    private ResourceBundle resourceBundle;

    private WSEventAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.XML);

        return supportOutputMessageTypes;
    }

    /**
     * @return WS Event adaptor instance
     */
    public static WSEventAdaptorType getInstance() {
        return wsEventAdaptor;
    }

    /**
     * @return name of the WS Event adaptor
     */
    @Override
    protected String getName() {
        return WSEventAdaptorConstants.ADAPTOR_TYPE_WSEVENT;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {

        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.wsevent.i18n.Resources", Locale.getDefault());
    }


    @Override
    public List<Property> getOutputAdaptorProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // URI
        Property uri = new Property(WSEventAdaptorConstants.ADAPTOR_CONF_WSEVENT_URI);
        uri.setDisplayName(
                resourceBundle.getString(WSEventAdaptorConstants.ADAPTOR_CONF_WSEVENT_URI));
        uri.setRequired(true);
        uri.setHint(resourceBundle.getString(WSEventAdaptorConstants.ADAPTOR_CONF_WSEVENT_URI_HINT));
        propertyList.add(uri);


        // Username
        Property userNameProperty = new Property(WSEventAdaptorConstants.ADAPTOR_CONF_WSEVENT_USERNAME);
        userNameProperty.setDisplayName(
                resourceBundle.getString(WSEventAdaptorConstants.ADAPTOR_CONF_WSEVENT_USERNAME));
        propertyList.add(userNameProperty);


        // Password
        Property passwordProperty = new Property(WSEventAdaptorConstants.ADAPTOR_CONF_WSEVENT_PASSWORD);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(WSEventAdaptorConstants.ADAPTOR_CONF_WSEVENT_PASSWORD));
        propertyList.add(passwordProperty);

        return propertyList;
    }

    @Override
    public List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // topic name
        Property topicProperty = new Property(WSEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME);
        topicProperty.setDisplayName(
                resourceBundle.getString(WSEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(WSEventAdaptorConstants.ADAPTOR_MESSAGE_HINT_TOPIC_NAME));

        propertyList.add(topicProperty);

        return propertyList;
    }

    @Override
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        Map<String, String> properties = outputEventAdaptorConfiguration.getOutputProperties();
        ConfigurationContextService configurationContextService =
                WSEventAdaptorServiceValueHolder.getConfigurationContextService();

        try {
            BrokerClient brokerClient = new BrokerClient(configurationContextService.getClientConfigContext(),
                                                         properties.get(WSEventAdaptorConstants.ADAPTOR_CONF_WSEVENT_URI),
                                                         properties.get(WSEventAdaptorConstants.ADAPTOR_CONF_WSEVENT_USERNAME),
                                                         properties.get(WSEventAdaptorConstants.ADAPTOR_CONF_WSEVENT_PASSWORD));

            String topicName = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(WSEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME);
            brokerClient.publish(topicName, ((OMElement) message));


        } catch (AxisFault axisFault) {
            throw new OutputEventAdaptorEventProcessingException("Can not publish to the ws event broker ", axisFault);
        } catch (AuthenticationExceptionException e) {
            throw new OutputEventAdaptorEventProcessingException("Can not authenticate the ws broker, hence cannot publish to the broker ", e);
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

            propertyList.put(WSEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME, "test");

            publish(outputEventAdaptorMessageConfiguration, builder1.getDocumentElement(), outputEventAdaptorConfiguration, tenantId);

        } catch (XMLStreamException e) {
            throw new OutputEventAdaptorEventProcessingException(e.getMessage());
        } catch (OutputEventAdaptorEventProcessingException e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }

    }
}
