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
package org.wso2.carbon.event.input.adapter.soap;


import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.soap.internal.util.SOAPEventAdapterConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.*;

/**
 * The soap event adapter factory class to create a soap input adapter
 */
public class SOAPEventAdapterFactory extends InputEventAdapterFactory {
    private ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adapter.soap.i18n.Resources", Locale.getDefault());
    private int httpPort;
    private int httpsPort;
    private int portOffset;

    public SOAPEventAdapterFactory() {
        portOffset = getPortOffset();
        httpPort = SOAPEventAdapterConstants.DEFAULT_HTTP_PORT + portOffset;
        httpsPort = SOAPEventAdapterConstants.DEFAULT_HTTPS_PORT + portOffset;
    }

    @Override
    public String getType() {
        return SOAPEventAdapterConstants.ADAPTER_TYPE_SOAP;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);
        return supportInputMessageTypes;
    }

    @Override
    public List<Property> getPropertyList() {

        List<Property> propertyList = new ArrayList<Property>();

        // Transport Exposed
        Property exposedTransportsProperty = new Property(SOAPEventAdapterConstants.EXPOSED_TRANSPORTS);
        exposedTransportsProperty.setRequired(true);
        exposedTransportsProperty.setDisplayName(
                resourceBundle.getString(SOAPEventAdapterConstants.EXPOSED_TRANSPORTS));
        exposedTransportsProperty.setOptions(new String[]{SOAPEventAdapterConstants.HTTPS, SOAPEventAdapterConstants.HTTP, SOAPEventAdapterConstants.LOCAL, SOAPEventAdapterConstants.ALL});
        exposedTransportsProperty.setDefaultValue(SOAPEventAdapterConstants.ALL);
        propertyList.add(exposedTransportsProperty);

        return propertyList;
    }

    @Override
    public String getUsageTips() {
        return resourceBundle.getString(SOAPEventAdapterConstants.ADAPTER_USAGE_TIPS_PREFIX) + httpPort + resourceBundle.getString(SOAPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID1) + httpsPort + resourceBundle.getString(SOAPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID2) + httpPort + resourceBundle.getString(SOAPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID3) + httpsPort + resourceBundle.getString(SOAPEventAdapterConstants.ADAPTER_USAGE_TIPS_POSTFIX);
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        return new SOAPEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    private int getPortOffset() {
        return CarbonUtils.getPortFromServerConfig(SOAPEventAdapterConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
    }
}
