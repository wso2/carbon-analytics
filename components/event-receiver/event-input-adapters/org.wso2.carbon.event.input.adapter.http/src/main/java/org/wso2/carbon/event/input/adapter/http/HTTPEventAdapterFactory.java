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
package org.wso2.carbon.event.input.adapter.http;


import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.http.internal.util.HTTPEventAdapterConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.*;

/**
 * The http event adapter factory class to create a http input adapter
 */
public class HTTPEventAdapterFactory extends InputEventAdapterFactory {

    private ResourceBundle resourceBundle =
            ResourceBundle.getBundle("org.wso2.carbon.event.input.adapter.http.i18n.Resources", Locale.getDefault());
    private int httpPort;
    private int httpsPort;
    private int portOffset;

    public HTTPEventAdapterFactory() {
        portOffset = getPortOffset();
        httpPort = HTTPEventAdapterConstants.DEFAULT_HTTP_PORT + portOffset;
        httpsPort = HTTPEventAdapterConstants.DEFAULT_HTTPS_PORT + portOffset;
    }

    @Override
    public String getType() {
        return HTTPEventAdapterConstants.ADAPTER_TYPE_HTTP;
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

        // Transport Exposed
        Property exposedTransportsProperty = new Property(HTTPEventAdapterConstants.EXPOSED_TRANSPORTS);
        exposedTransportsProperty.setRequired(true);
        exposedTransportsProperty.setDisplayName(
                resourceBundle.getString(HTTPEventAdapterConstants.EXPOSED_TRANSPORTS));
        exposedTransportsProperty.setOptions(new String[]{HTTPEventAdapterConstants.HTTPS, HTTPEventAdapterConstants.HTTP, HTTPEventAdapterConstants.LOCAL, HTTPEventAdapterConstants.ALL});
        exposedTransportsProperty.setDefaultValue(HTTPEventAdapterConstants.ALL);
        propertyList.add(exposedTransportsProperty);

        return propertyList;
    }

    @Override
    public String getUsageTips() {
        return resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_PREFIX) + httpPort + resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID1) + httpsPort + resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID2) + httpPort + resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID3) + httpsPort + resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_POSTFIX);
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                                                Map<String, String> globalProperties) {
        return new HTTPEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    private int getPortOffset() {
        return CarbonUtils.getPortFromServerConfig(HTTPEventAdapterConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
    }
}
