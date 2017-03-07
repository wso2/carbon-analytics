/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.input.adapter.websocket;

import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.websocket.internal.util.WebsocketEventAdapterConstants;

import java.util.*;

/**
 * The websocket event adapter factory class to create a websocket input adapter
 */
public class WebsocketEventAdapterFactory extends InputEventAdapterFactory {
    private ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adapter.websocket.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return WebsocketEventAdapterConstants.ADAPTER_TYPE_WEBSOCKET;
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
        Property urlProperty = new Property(WebsocketEventAdapterConstants.ADAPTER_SERVER_URL);
        urlProperty.setDisplayName(resourceBundle.getString(WebsocketEventAdapterConstants.ADAPTER_SERVER_URL));
        urlProperty.setHint(resourceBundle.getString(WebsocketEventAdapterConstants.ADAPTER_SERVER_URL_HINT));
        urlProperty.setRequired(true);
        propertyList.add(urlProperty);

        Property isDuplicatedInCluster = new Property(EventAdapterConstants.EVENTS_DUPLICATED_IN_CLUSTER);
        isDuplicatedInCluster.setDisplayName(
                resourceBundle.getString(EventAdapterConstants.EVENTS_DUPLICATED_IN_CLUSTER));
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
        return new WebsocketEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
