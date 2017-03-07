/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.input.adapter.filetail;

import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.filetail.internal.util.FileTailEventAdapterConstants;

import java.util.*;

/**
 * The filetail event adapter factory class to create a filetail input adapter
 */
public class FileTailEventAdapterFactory extends InputEventAdapterFactory {

    private ResourceBundle resourceBundle = ResourceBundle.getBundle
            ("org.wso2.carbon.event.input.adapter.filetail.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return FileTailEventAdapterConstants.EVENT_ADAPTER_TYPE_FILE;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.TEXT);
        return supportInputMessageTypes;
    }

    @Override
    public List<Property> getPropertyList() {
        List<Property> propertyList = new ArrayList<Property>();

        Property filePath = new Property(FileTailEventAdapterConstants.EVENT_ADAPTER_CONF_FILEPATH);
        filePath.setDisplayName(
                resourceBundle.getString(FileTailEventAdapterConstants.EVENT_ADAPTER_CONF_FILEPATH));
        filePath.setRequired(true);
        filePath.setHint(resourceBundle.getString(FileTailEventAdapterConstants.EVENT_ADAPTER_CONF_FILEPATH_HINT));
        propertyList.add(filePath);


        Property delayInMillis = new Property(FileTailEventAdapterConstants.EVENT_ADAPTER_DELAY_MILLIS);
        delayInMillis.setDisplayName(
                resourceBundle.getString(FileTailEventAdapterConstants.EVENT_ADAPTER_DELAY_MILLIS));
        delayInMillis.setHint(resourceBundle.getString(FileTailEventAdapterConstants.EVENT_ADAPTER_DELAY_MILLIS_HINT));
        propertyList.add(delayInMillis);

        Property startFromEndProperty = new Property(FileTailEventAdapterConstants.EVENT_ADAPTER_START_FROM_END);
        startFromEndProperty.setRequired(true);
        startFromEndProperty.setDisplayName(
                resourceBundle.getString(FileTailEventAdapterConstants.EVENT_ADAPTER_START_FROM_END));
        startFromEndProperty.setOptions(new String[]{"true", "false"});
        startFromEndProperty.setDefaultValue("true");
        startFromEndProperty.setHint(resourceBundle.getString(
                FileTailEventAdapterConstants.EVENT_ADAPTER_START_FROM_END_HINT));
        propertyList.add(startFromEndProperty);

        return propertyList;
    }

    @Override
    public String getUsageTips() {
        return resourceBundle.getString(FileTailEventAdapterConstants.EVENT_ADAPTER_USAGE_TIPS_FILE);
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                                                Map<String, String> globalProperties) {
        return new FileTailEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
