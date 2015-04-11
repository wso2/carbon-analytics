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
package org.wso2.carbon.event.input.adapter.file;

import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.file.internal.util.FileEventAdapterConstants;

import java.util.*;

public class FileEventAdapterFactory extends InputEventAdapterFactory {

    private ResourceBundle resourceBundle = ResourceBundle.getBundle
            ("org.wso2.carbon.event.input.adapter.file.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return FileEventAdapterConstants.EVENT_ADAPTER_TYPE_FILE;
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

        Property filePath = new Property(FileEventAdapterConstants.EVENT_ADAPTER_CONF_FILEPATH);
        filePath.setDisplayName(
                resourceBundle.getString(FileEventAdapterConstants.EVENT_ADAPTER_CONF_FILEPATH));
        filePath.setRequired(true);
        filePath.setHint(resourceBundle.getString(FileEventAdapterConstants.EVENT_ADAPTER_CONF_FILEPATH_HINT));
        propertyList.add(filePath);

        return propertyList;
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                                                Map<String, String> globalProperties) {
        return new FileEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
