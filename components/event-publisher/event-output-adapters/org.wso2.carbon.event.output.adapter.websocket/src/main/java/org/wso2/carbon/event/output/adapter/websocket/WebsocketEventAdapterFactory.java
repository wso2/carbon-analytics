/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.output.adapter.websocket;


import org.wso2.carbon.event.output.adapter.core.*;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.websocket.internal.util.WebsocketEventAdapterConstants;

import java.util.*;

/**
 * The websocket event adapter factory class to create a websocket output adapter
 */
public class WebsocketEventAdapterFactory extends OutputEventAdapterFactory {
    private ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adapter.websocket.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return WebsocketEventAdapterConstants.ADAPTER_TYPE_WEBSOCKET;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.TEXT);
        supportedMessageFormats.add(MessageType.XML);
        supportedMessageFormats.add(MessageType.JSON);
        return supportedMessageFormats;
    }

    //TODO, check websocket security feature
    @Override
    public List<Property> getStaticPropertyList() {
        List<Property> staticPropertyList = new ArrayList<Property>();

        Property adapterServerURL = new Property(WebsocketEventAdapterConstants.ADAPTER_SERVER_URL);
        adapterServerURL.setDisplayName(
                resourceBundle.getString(WebsocketEventAdapterConstants.ADAPTER_SERVER_URL));
        adapterServerURL.setHint(resourceBundle.getString(WebsocketEventAdapterConstants.ADAPTER_SERVER_URL_HINT));
        adapterServerURL.setRequired(true);
        staticPropertyList.add(adapterServerURL);

        return staticPropertyList;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        return null;
    }

    @Override
    public String getUsageTips() {
        return null;
    }


    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        return new WebsocketEventAdapter(eventAdapterConfiguration, globalProperties);
    }

}
