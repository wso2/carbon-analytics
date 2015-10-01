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

package org.wso2.carbon.event.input.adapter.websocket.local;

import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.websocket.local.internal.util.WebsocketLocalEventAdapterConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.*;

/**
 * The websocket local event adapter factory class to create a websocket local input adapter
 */
public class WebsocketLocalEventAdapterFactory extends InputEventAdapterFactory {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adapter.websocket.local.i18n.Resources", Locale.getDefault());
    private int httpPort;
    private int httpsPort;
    private int portOffset;

    public WebsocketLocalEventAdapterFactory() {
        portOffset = getPortOffset();
        httpPort = WebsocketLocalEventAdapterConstants.DEFAULT_HTTP_PORT + portOffset;
        httpsPort = WebsocketLocalEventAdapterConstants.DEFAULT_HTTPS_PORT + portOffset;
    }

    @Override
    public String getType() {
        return WebsocketLocalEventAdapterConstants.ADAPTOR_TYPE_WEBSOCKET_LOCAL;
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
        return null;
    }

    @Override
    public String getUsageTips() {
        return resourceBundle.getString(WebsocketLocalEventAdapterConstants.ADAPTOR_USAGE_TIPS_PREFIX) + httpPort +
                resourceBundle.getString(WebsocketLocalEventAdapterConstants.ADAPTER_USAGE_TIPS_MID) + httpsPort +
                resourceBundle.getString(WebsocketLocalEventAdapterConstants.ADAPTER_USAGE_TIPS_POSTFIX);
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        return new WebsocketLocalEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    private int getPortOffset() {
        return CarbonUtils.getPortFromServerConfig(WebsocketLocalEventAdapterConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
    }
}
