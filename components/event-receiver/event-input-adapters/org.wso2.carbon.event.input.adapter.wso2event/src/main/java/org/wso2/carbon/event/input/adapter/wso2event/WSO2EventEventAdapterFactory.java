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
package org.wso2.carbon.event.input.adapter.wso2event;


import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.wso2event.internal.util.WSO2EventAdapterConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.*;

/**
 * The WSO2Event adapter factory class to create a WSO2Event input adapter
 */
public class WSO2EventEventAdapterFactory extends InputEventAdapterFactory {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adapter.wso2event.i18n.Resources", Locale.getDefault());
    private int thriftTCPPort;
    private int thriftSSLPort;
    private int binaryTCPPort;
    private int binarySSLPort;
    private int portOffset;

    public WSO2EventEventAdapterFactory() {
        portOffset = getPortOffset();
        thriftTCPPort = WSO2EventAdapterConstants.DEFAULT_THRIFT_TCP_PORT + portOffset;
        thriftSSLPort = WSO2EventAdapterConstants.DEFAULT_THRIFT_SSL_PORT + portOffset;
        binaryTCPPort = WSO2EventAdapterConstants.DEFAULT_BINARY_TCP_PORT + portOffset;
        binarySSLPort = WSO2EventAdapterConstants.DEFAULT_BINARY_SSL_PORT + portOffset;
    }

    @Override
    public String getType() {
        return WSO2EventAdapterConstants.ADAPTER_TYPE_WSO2EVENT;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.WSO2EVENT);
        return supportInputMessageTypes;
    }

    @Override
    public List<Property> getPropertyList() {
        List<Property> propertyList = new ArrayList<Property>();

        Property isDuplicatedInCluster = new Property(EventAdapterConstants.EVENTS_DUPLICATED_IN_CLUSTER);
        isDuplicatedInCluster.setDisplayName(
                resourceBundle.getString(EventAdapterConstants.EVENTS_DUPLICATED_IN_CLUSTER));
        isDuplicatedInCluster.setRequired(false);
        isDuplicatedInCluster.setHint(resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_IS_EVENTS_DUPLICATED_IN_CLUSTER_HINT));
        isDuplicatedInCluster.setOptions(new String[]{"true", "false"});
        isDuplicatedInCluster.setDefaultValue("false");
        propertyList.add(isDuplicatedInCluster);
        return propertyList;
    }

    @Override
    public String getUsageTips() {
        return resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_USAGE_TIPS_PREFIX) + thriftTCPPort + resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_USAGE_TIPS_IN_BETWEEN) + thriftSSLPort + resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_USAGE_TIPS_POSTFIX) + binaryTCPPort + resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_USAGE_TIPS_IN_BETWEEN) + binarySSLPort;
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        return new WSO2EventAdapter(eventAdapterConfiguration, globalProperties);
    }

    private int getPortOffset() {
        return CarbonUtils.getPortFromServerConfig(WSO2EventAdapterConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
    }
}
