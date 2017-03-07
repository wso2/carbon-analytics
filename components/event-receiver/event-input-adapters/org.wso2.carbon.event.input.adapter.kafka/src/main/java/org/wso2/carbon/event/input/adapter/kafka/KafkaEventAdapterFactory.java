/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.input.adapter.kafka;


import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.kafka.internal.util.KafkaEventAdapterConstants;

import java.util.*;

/**
 * The kafka event adapter factory class to create a kafka input adapter
 */
public class KafkaEventAdapterFactory extends InputEventAdapterFactory {
    private ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adapter.kafka.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return KafkaEventAdapterConstants.ADAPTOR_TYPE_KAFKA;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.TEXT);
        return supportInputMessageTypes;
    }

    @Override
    public List<Property> getPropertyList() {

        List<Property> propertyList = new ArrayList<Property>();

        //set Zk Connect of broker
        Property webZKConnect = new Property(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT);
        webZKConnect.setDisplayName(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT));
        webZKConnect.setHint(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT_HINT));
        webZKConnect.setRequired(true);
        propertyList.add(webZKConnect);

        //set GroupID of broker
        Property webGroupID = new Property(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_GROUP_ID);
        webGroupID.setDisplayName(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_GROUP_ID));
        webGroupID.setHint(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_GROUP_ID_hint));
        webGroupID.setRequired(true);
        propertyList.add(webGroupID);

        //set Subscriber threads
        Property webThreads = new Property(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_THREADS);
        webThreads.setDisplayName(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_THREADS));
        webThreads.setHint(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_THREADS_HINT));
        webThreads.setRequired(true);
        propertyList.add(webThreads);

        Property optionConfigProperties = new Property(KafkaEventAdapterConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES);
        optionConfigProperties.setDisplayName(
                resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES));
        optionConfigProperties.setHint(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES_HINT));
        propertyList.add(optionConfigProperties);

        //set Topic of broker
        Property webTopic = new Property(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_TOPIC);
        webTopic.setDisplayName(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_TOPIC));
        webTopic.setRequired(true);
        propertyList.add(webTopic);

        Property isDuplicatedInCluster = new Property(EventAdapterConstants.EVENTS_DUPLICATED_IN_CLUSTER);
        isDuplicatedInCluster.setDisplayName(resourceBundle.getString(EventAdapterConstants.EVENTS_DUPLICATED_IN_CLUSTER));
        isDuplicatedInCluster.setRequired(false);
        isDuplicatedInCluster.setOptions(new String[]{"true", "false"});
        isDuplicatedInCluster.setDefaultValue("false");
        propertyList.add(isDuplicatedInCluster);

        return propertyList;

 }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        return new KafkaEventAdapter(eventAdapterConfiguration,globalProperties);
    }
}
