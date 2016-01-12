/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.output.adapter.wso2event;


import org.wso2.carbon.event.output.adapter.core.*;
import org.wso2.carbon.event.output.adapter.wso2event.internal.util.WSO2EventAdapterConstants;

import java.util.*;

/**
 * The WSO2Event adapter factory class to create a WSO2Event output adapter
 */
public class WSO2EventAdapterFactory extends OutputEventAdapterFactory {

    ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adapter.wso2event.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return WSO2EventAdapterConstants.ADAPTER_TYPE_WSO2EVENT;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.WSO2EVENT);
        return supportOutputMessageTypes;
    }

    @Override
    public List<Property> getStaticPropertyList() {
        List<Property> propertyList = new ArrayList<Property>();

        // set receiver url event adapter
        Property ipProperty = new Property(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_RECEIVER_URL);
        ipProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_RECEIVER_URL));
        ipProperty.setRequired(true);
        ipProperty.setHint(resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_HINT_RECEIVER_URL));

        // set authenticator url of event adapter
        Property authenticatorIpProperty = new Property(WSO2EventAdapterConstants.
                ADAPTER_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL);
        authenticatorIpProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL));
        authenticatorIpProperty.setRequired(false);
        authenticatorIpProperty.setHint(resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_HINT_AUTHENTICATOR_URL));


        // set connection user name as property
        Property userNameProperty = new Property(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_USER_NAME);
        userNameProperty.setRequired(true);
        userNameProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_USER_NAME));
        userNameProperty.setHint(resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_HINT_USER_NAME));

        // set connection password as property
        Property passwordProperty = new Property(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PASSWORD);
        passwordProperty.setRequired(true);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PASSWORD));
        passwordProperty.setHint(resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_HINT_PASSWORD));

        // set connection protocol as property
        Property protocolProperty = new Property(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PROTOCOL);
        protocolProperty.setRequired(true);
        protocolProperty.setDefaultValue(WSO2EventAdapterConstants.ADAPTER_PROTOCOL_THRIFT);
        protocolProperty.setOptions(new String[]{WSO2EventAdapterConstants.ADAPTER_PROTOCOL_THRIFT, WSO2EventAdapterConstants.ADAPTER_PROTOCOL_BINARY});
        protocolProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PROTOCOL));
        protocolProperty.setHint(resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_HINT_PROTOCOL));

        // set publishingMode as property
        Property publishingModeProperty = new Property(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PUBLISHING_MODE);
        publishingModeProperty.setDefaultValue(WSO2EventAdapterConstants.ADAPTER_PUBLISHING_MODE_NON_BLOCKING);
        publishingModeProperty.setOptions(new String[]{WSO2EventAdapterConstants.ADAPTER_PUBLISHING_MODE_BLOCKING, WSO2EventAdapterConstants.ADAPTER_PUBLISHING_MODE_NON_BLOCKING});
        publishingModeProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PUBLISHING_MODE));
        publishingModeProperty.setHint(resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_HINT_PUBLISHING_MODE));

        // set publishTimeout as property
        Property publishTimeoutProperty = new Property(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PUBLISH_TIMEOUT_MS);
        publishTimeoutProperty.setDefaultValue("0");
        publishTimeoutProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_PROP_PUBLISH_TIMEOUT_MS));
        publishTimeoutProperty.setHint(resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_CONF_WSO2EVENT_HINT_PUBLISH_TIMEOUT_MS));

        propertyList.add(ipProperty);
        propertyList.add(authenticatorIpProperty);
        propertyList.add(userNameProperty);
        propertyList.add(passwordProperty);
        propertyList.add(protocolProperty);
        propertyList.add(publishingModeProperty);
        propertyList.add(publishTimeoutProperty);

        return propertyList;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        return null;
    }

    @Override
    public String getUsageTips() {
        return resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_USAGE_TIPS_WSO2EVENT);
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        return new WSO2EventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
