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

package org.wso2.carbon.event.output.adaptor.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.jms.internal.util.JMSConnectionFactory;
import org.wso2.carbon.event.output.adaptor.jms.internal.util.JMSConstants;
import org.wso2.carbon.event.output.adaptor.jms.internal.util.JMSEventAdaptorConstants;
import org.wso2.carbon.event.output.adaptor.jms.internal.util.JMSMessageSender;
import org.wso2.carbon.event.output.adaptor.jms.internal.util.JMSUtils;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class JMSEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(JMSEventAdaptorType.class);
    private static JMSEventAdaptorType jmsEventAdaptorAdaptor = new JMSEventAdaptorType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, PublisherDetails>> publisherMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, PublisherDetails>>();

    private JMSEventAdaptorType() {

    }

    /**
     * @return jms event adaptor instance
     */
    public static JMSEventAdaptorType getInstance() {

        return jmsEventAdaptorAdaptor;
    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.XML);
        supportOutputMessageTypes.add(MessageType.JSON);
        supportOutputMessageTypes.add(MessageType.MAP);
        supportOutputMessageTypes.add(MessageType.TEXT);
        return supportOutputMessageTypes;
    }

    /**
     * @return name of the jms event adaptor
     */
    @Override
    protected String getName() {
        return JMSEventAdaptorConstants.ADAPTOR_TYPE_JMS;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.jms.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // JNDI initial context factory class
        Property initialContextProperty = new Property(JMSEventAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS);
        initialContextProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS));
        initialContextProperty.setRequired(true);
        initialContextProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS_HINT));
        propertyList.add(initialContextProperty);


        // JNDI Provider URL
        Property javaNamingProviderUrlProperty = new Property(JMSEventAdaptorConstants.JAVA_NAMING_PROVIDER_URL);
        javaNamingProviderUrlProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.JAVA_NAMING_PROVIDER_URL));
        javaNamingProviderUrlProperty.setRequired(true);
        javaNamingProviderUrlProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.JAVA_NAMING_PROVIDER_URL_HINT));
        propertyList.add(javaNamingProviderUrlProperty);


        // JNDI Username
        Property userNameProperty = new Property(JMSEventAdaptorConstants.JAVA_NAMING_SECURITY_PRINCIPAL);
        userNameProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.JAVA_NAMING_SECURITY_PRINCIPAL));
        propertyList.add(userNameProperty);


        // JNDI Password
        Property passwordProperty = new Property(JMSEventAdaptorConstants.JAVA_NAMING_SECURITY_CREDENTIALS);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.JAVA_NAMING_SECURITY_CREDENTIALS));
        propertyList.add(passwordProperty);

        // Connection Factory JNDI Name
        Property connectionFactoryNameProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_CONNECTION_FACTORY_JNDINAME);
        connectionFactoryNameProperty.setRequired(true);
        connectionFactoryNameProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_CONNECTION_FACTORY_JNDINAME));
        connectionFactoryNameProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_CONNECTION_FACTORY_JNDINAME_HINT));
        propertyList.add(connectionFactoryNameProperty);


        // Destination Type
        Property destinationTypeProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION_TYPE);
        destinationTypeProperty.setRequired(true);
        destinationTypeProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION_TYPE));
        destinationTypeProperty.setOptions(new String[]{"queue", "topic"});
        destinationTypeProperty.setDefaultValue("topic");
        destinationTypeProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION_TYPE_HINT));
        propertyList.add(destinationTypeProperty);


        return propertyList;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // Topic
        Property topicProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION);
        topicProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION));
        topicProperty.setRequired(true);
        propertyList.add(topicProperty);

        // Header
        Property headerProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_HEADER);
        headerProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_HEADER));
        propertyList.add(headerProperty);

        return propertyList;

    }

    /**
     * @param outputEventAdaptorMessageConfiguration
     *                 - topic name to publish messages
     * @param message  - is and Object[]{Event, EventDefinition}
     * @param outputEventAdaptorConfiguration the {@link OutputEventAdaptorConfiguration} object that will be used to
     *                                        get configuration information
     * @param tenantId tenant id of the calling thread.
     */
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        ConcurrentHashMap<String, PublisherDetails> topicEventSender = publisherMap.get(outputEventAdaptorConfiguration.getName());
        if (null == topicEventSender) {
            topicEventSender = new ConcurrentHashMap<String, PublisherDetails>();
            if (null != publisherMap.putIfAbsent(outputEventAdaptorConfiguration.getName(), topicEventSender)) {
                topicEventSender = publisherMap.get(outputEventAdaptorConfiguration.getName());
            }
        }

        String topicName = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION);
        PublisherDetails publisherDetails = topicEventSender.get(topicName);
        Map<String, String> messageConfig = new HashMap<String, String>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, topicName);
        try {
            if (null == publisherDetails) {
                publisherDetails = initPublisher(outputEventAdaptorConfiguration, topicEventSender, topicName, messageConfig);
            }
            Message jmsMessage =publisherDetails.getJmsMessageSender().convertToJMSMessage(message,messageConfig);
            setJMSTransportHeaders(jmsMessage,outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(JMSEventAdaptorConstants.ADAPTOR_JMS_HEADER));
            publisherDetails.getJmsMessageSender().send(jmsMessage, messageConfig);
        } catch (RuntimeException e) {
            log.warn("Caught exception: " + e.getMessage() + ". Reinitializing connection and sending...");
            publisherDetails = topicEventSender.remove(topicName);
            if (publisherDetails != null) {
                publisherDetails.getJmsMessageSender().close();
                publisherDetails.getJmsConnectionFactory().stop();
            }
            //TODO If this send also fails, the exception will be thrown up. Will that break the flow?
            // Retry sending after reinitializing connection
            publisherDetails = initPublisher(outputEventAdaptorConfiguration, topicEventSender, topicName, messageConfig);
            Message jmsMessage =publisherDetails.getJmsMessageSender().convertToJMSMessage(message,messageConfig);
            setJMSTransportHeaders(jmsMessage,outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(JMSEventAdaptorConstants.ADAPTOR_JMS_HEADER));
            publisherDetails.getJmsMessageSender().send(jmsMessage, messageConfig);
        }
    }

    private PublisherDetails initPublisher(OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, ConcurrentHashMap<String, PublisherDetails> topicEventSender, String topicName, Map<String, String> messageConfig) {
        PublisherDetails publisherDetails;
        Hashtable<String, String> adaptorProperties = new Hashtable<String, String>();
        adaptorProperties.putAll(outputEventAdaptorConfiguration.getOutputProperties());

        JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties, outputEventAdaptorConfiguration.getName());
        JMSMessageSender jmsMessageSender = new JMSMessageSender(jmsConnectionFactory, messageConfig);
        publisherDetails = new PublisherDetails(jmsConnectionFactory, jmsMessageSender);
        topicEventSender.put(topicName, publisherDetails);
        return publisherDetails;
    }

    private Message setJMSTransportHeaders(Message message, String headerProperty){

        Map<String,String> messageConfiguration = new HashMap<String, String>();

        if(headerProperty != null && message != null){
            String[] headers = headerProperty.split(",");

            if(headers != null && headers.length > 0){
                for (String  header: headers){
                    String[] headerPropertyWithValue = header.split(":");
                    messageConfiguration.put(headerPropertyWithValue[0],headerPropertyWithValue[1]);
                }
            }

            try {
                return JMSUtils.setTransportHeaders(messageConfiguration,message);
            } catch (JMSException e) {
                throw new OutputEventAdaptorEventProcessingException(e);
            }
        }

        return message;
    }

    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        try {
            Hashtable<String, String> adaptorProperties = new Hashtable<String, String>();
            adaptorProperties.putAll(outputEventAdaptorConfiguration.getOutputProperties());

            JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties, outputEventAdaptorConfiguration.getName());
            Connection connection = jmsConnectionFactory.getConnection();
            connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.close();
            jmsConnectionFactory.stop();
        } catch (Exception e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }
    }

    class PublisherDetails {
        private final JMSConnectionFactory jmsConnectionFactory;
        private final JMSMessageSender jmsMessageSender;

        public PublisherDetails(JMSConnectionFactory jmsConnectionFactory,
                                JMSMessageSender jmsMessageSender) {
            this.jmsConnectionFactory = jmsConnectionFactory;
            this.jmsMessageSender = jmsMessageSender;
        }

        public JMSConnectionFactory getJmsConnectionFactory() {
            return jmsConnectionFactory;
        }

        public JMSMessageSender getJmsMessageSender() {
            return jmsMessageSender;
        }
    }


}
