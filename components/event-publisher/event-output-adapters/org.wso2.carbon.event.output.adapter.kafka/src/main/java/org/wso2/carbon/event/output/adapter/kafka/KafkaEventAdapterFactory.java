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
package org.wso2.carbon.event.output.adapter.kafka;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.*;
import org.wso2.carbon.event.output.adapter.kafka.internal.util.KafkaEventAdapterConstants;

import java.util.*;

/**
 * The kafka event adapter factory class to create a kafka output adapter
 */
public class KafkaEventAdapterFactory extends OutputEventAdapterFactory {
    private static final Log log = LogFactory.getLog(KafkaEventAdapterFactory.class);

    private ResourceBundle resourceBundle =
            ResourceBundle.getBundle("org.wso2.carbon.event.output.adapter.kafka.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return KafkaEventAdapterConstants.ADAPTOR_TYPE_KAFKA;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.TEXT);
        supportedMessageFormats.add(MessageType.XML);
        supportedMessageFormats.add(MessageType.JSON);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {
        List<Property> propertyList = new ArrayList<Property>();

        //set Kafka Connect of broker
        Property webKafkaConnect = new Property(KafkaEventAdapterConstants.ADAPTOR_META_BROKER_LIST);
        webKafkaConnect.setDisplayName(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_META_BROKER_LIST));
        webKafkaConnect.setHint(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_META_BROKER_LIST_HINT));
        webKafkaConnect.setRequired(true);
        propertyList.add(webKafkaConnect);

        Property optionConfigProperties = new Property(KafkaEventAdapterConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES);
        optionConfigProperties.setDisplayName(
                resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES));
        optionConfigProperties.setHint(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES_HINT));
        propertyList.add(optionConfigProperties);

        return propertyList;

    }

    @Override
    public List<Property> getDynamicPropertyList() {
        List<Property> propertyList = new ArrayList<Property>();

        //set Topic of broker
        Property webTopic = new Property(KafkaEventAdapterConstants.ADAPTOR_PUBLISH_TOPIC);
        webTopic.setDisplayName(resourceBundle.getString(KafkaEventAdapterConstants.ADAPTOR_PUBLISH_TOPIC));
        webTopic.setRequired(true);
        propertyList.add(webTopic);

        return propertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {
        return new KafkaEventAdapter(eventAdapterConfiguration, globalProperties);
    }

}
