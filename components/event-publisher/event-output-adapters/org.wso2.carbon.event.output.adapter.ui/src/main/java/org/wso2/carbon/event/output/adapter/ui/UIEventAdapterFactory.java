/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.event.output.adapter.ui;

import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.ui.internal.util.UIEventAdapterConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class UIEventAdapterFactory extends OutputEventAdapterFactory{

    private ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adapter.ui.i18n" +
            ".Resources", Locale.getDefault());

    @Override
    public String getType() {
        return UIEventAdapterConstants.ADAPTER_TYPE_UI;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.WSO2EVENT);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {
        List<Property> staticPropertyList = new ArrayList<Property>();

        Property outputStreamName = new Property(UIEventAdapterConstants.ADAPTER_UI_OUTPUT_STREAM_NAME);
        outputStreamName.setDisplayName(
                resourceBundle.getString(UIEventAdapterConstants.ADAPTER_UI_OUTPUT_STREAM_NAME));
        outputStreamName.setRequired(true);
        staticPropertyList.add(outputStreamName);

        Property outputStreamVersion = new Property(UIEventAdapterConstants.ADAPTER_UI_OUTPUT_STREAM_VERSION);
        outputStreamVersion.setDisplayName(
                resourceBundle.getString(UIEventAdapterConstants.ADAPTER_UI_OUTPUT_STREAM_VERSION));
        outputStreamVersion.setRequired(false);
        staticPropertyList.add(outputStreamVersion);

        return staticPropertyList;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
            Map<String, String> globalProperties) {
        return new UIEventAdapter(eventAdapterConfiguration,globalProperties);
    }
}
