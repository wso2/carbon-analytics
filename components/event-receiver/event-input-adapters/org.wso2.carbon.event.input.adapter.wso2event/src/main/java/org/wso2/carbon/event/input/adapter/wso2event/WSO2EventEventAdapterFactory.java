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

import java.util.*;


public class WSO2EventEventAdapterFactory extends InputEventAdapterFactory {
    ResourceBundle  resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adapter.wso2event.i18n.Resources", Locale.getDefault());


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

        // set stream definition
        Property streamDefinitionProperty = new Property(WSO2EventAdapterConstants.ADAPTER_STREAM_NAME);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_STREAM_NAME));
        streamDefinitionProperty.setRequired(true);


        // set stream version
        Property streamVersionProperty = new Property(WSO2EventAdapterConstants.ADAPTER_STREAM_VERSION);
        streamVersionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdapterConstants.ADAPTER_STREAM_VERSION));
        streamVersionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);
        propertyList.add(streamVersionProperty);

        return propertyList;
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        return new WSO2EventAdapter(eventAdapterConfiguration,globalProperties);
    }
}
