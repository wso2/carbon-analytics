/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.output.adapter.jms;

import org.wso2.carbon.event.output.adapter.core.*;
import org.wso2.carbon.event.output.adapter.jms.internal.util.JMSEventAdapterConstants;

import java.util.*;

/**
 * The jms event adapter factory class to create a jms output adapter
 */
public class JMSEventAdapterFactory extends OutputEventAdapterFactory {

    private ResourceBundle resourceBundle =
            ResourceBundle.getBundle("org.wso2.carbon.event.output.adapter.jms.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return JMSEventAdapterConstants.ADAPTER_TYPE_JMS;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.TEXT);
        supportedMessageFormats.add(MessageType.MAP);
        supportedMessageFormats.add(MessageType.XML);
        supportedMessageFormats.add(MessageType.JSON);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {
        List<Property> staticPropertyList = new ArrayList<Property>();

        // JNDI initial context factory class
        Property initialContextProperty = new Property(JMSEventAdapterConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS);
        initialContextProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS));
        initialContextProperty.setRequired(true);
        initialContextProperty.setHint(resourceBundle.getString(
                JMSEventAdapterConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS_HINT));

        // Connection Factory JNDI Name
        Property connectionFactoryNameProperty = new Property(
                JMSEventAdapterConstants.ADAPTER_JMS_CONNECTION_FACTORY_JNDINAME);
        connectionFactoryNameProperty.setRequired(true);
        connectionFactoryNameProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_CONNECTION_FACTORY_JNDINAME));
        connectionFactoryNameProperty.setHint(resourceBundle.getString(
                JMSEventAdapterConstants.ADAPTER_JMS_CONNECTION_FACTORY_JNDINAME_HINT));

        // JNDI Provider URL
        Property javaNamingProviderUrlProperty = new Property(JMSEventAdapterConstants.JAVA_NAMING_PROVIDER_URL);
        javaNamingProviderUrlProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.JAVA_NAMING_PROVIDER_URL));
        javaNamingProviderUrlProperty.setRequired(true);
        javaNamingProviderUrlProperty.setHint(resourceBundle.getString(
                JMSEventAdapterConstants.JAVA_NAMING_PROVIDER_URL_HINT));

        // JNDI Username
        Property userNameProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_USERNAME);
        userNameProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_USERNAME));

        // JNDI Password
        Property passwordProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_PASSWORD);
        passwordProperty.setSecured(true);
        passwordProperty.setEncrypted(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_PASSWORD));


        // Destination Type
        Property destinationTypeProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION_TYPE);
        destinationTypeProperty.setRequired(true);
        destinationTypeProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION_TYPE));
        destinationTypeProperty.setOptions(new String[]{"queue", "topic"});
        destinationTypeProperty.setDefaultValue("topic");
        destinationTypeProperty.setHint(resourceBundle.getString(
                JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION_TYPE_HINT));

        // Topic
        Property topicProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION);
        topicProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION));
        topicProperty.setRequired(true);

        Property concurrentPublishers = new Property(JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS);
        concurrentPublishers.setDisplayName(resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS));
        concurrentPublishers.setOptions(new String[]{JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS_ALLOWED, JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS_NOT_ALLOWED});
        concurrentPublishers.setHint(resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS_HINT));


        staticPropertyList.add(initialContextProperty);
        staticPropertyList.add(javaNamingProviderUrlProperty);
        staticPropertyList.add(userNameProperty);
        staticPropertyList.add(passwordProperty);
        staticPropertyList.add(connectionFactoryNameProperty);
        staticPropertyList.add(destinationTypeProperty);
        staticPropertyList.add(topicProperty);
        staticPropertyList.add(concurrentPublishers);

        return staticPropertyList;

    }

    @Override
    public List<Property> getDynamicPropertyList() {
        List<Property> dynamicPropertyList = new ArrayList<Property>();

        // Header
        Property headerProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_HEADER);
        headerProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_HEADER));
        headerProperty.setHint(resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_HEADER_HINT));
        dynamicPropertyList.add(headerProperty);

        return dynamicPropertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {
        return new JMSEventAdapter(eventAdapterConfiguration, globalProperties);
    }

}
