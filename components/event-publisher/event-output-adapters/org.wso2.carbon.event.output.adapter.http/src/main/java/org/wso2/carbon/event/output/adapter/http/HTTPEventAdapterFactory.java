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
package org.wso2.carbon.event.output.adapter.http;

import org.wso2.carbon.event.output.adapter.core.*;
import org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants;

import java.util.*;

/**
 * The http event adapter factory class to create a http output adapter
 */
public class HTTPEventAdapterFactory extends OutputEventAdapterFactory {
    private ResourceBundle resourceBundle =
            ResourceBundle.getBundle("org.wso2.carbon.event.output.adapter.http.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return HTTPEventAdapterConstants.ADAPTER_TYPE_HTTP;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.TEXT);
        supportedMessageFormats.add(MessageType.XML);
        supportedMessageFormats.add(MessageType.JSON);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {

        List<Property> staticPropertyList = new ArrayList<Property>();

        Property proxyHostProp = new Property(HTTPEventAdapterConstants.ADAPTER_PROXY_HOST);
        proxyHostProp.setDisplayName(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_PROXY_HOST));
        proxyHostProp.setHint(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_PROXY_HOST_HINT));
        proxyHostProp.setRequired(false);

        Property proxyPortProp = new Property(HTTPEventAdapterConstants.ADAPTER_PROXY_PORT);
        proxyPortProp.setDisplayName(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_PROXY_PORT));
        proxyPortProp.setHint(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_PROXY_PORT_HINT));
        proxyPortProp.setRequired(false);

        Property clientMethod = new Property(HTTPEventAdapterConstants.ADAPTER_HTTP_CLIENT_METHOD);
        clientMethod.setDisplayName(
                resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_HTTP_CLIENT_METHOD));
        clientMethod.setRequired(true);
        clientMethod.setOptions(new String[]{HTTPEventAdapterConstants.CONSTANT_HTTP_POST, HTTPEventAdapterConstants.CONSTANT_HTTP_PUT});
        clientMethod.setDefaultValue(HTTPEventAdapterConstants.CONSTANT_HTTP_POST);

        staticPropertyList.add(proxyHostProp);
        staticPropertyList.add(proxyPortProp);
        staticPropertyList.add(clientMethod);

        return staticPropertyList;

    }

    @Override
    public List<Property> getDynamicPropertyList() {
        List<Property> dynamicPropertyList = new ArrayList<Property>();

        Property urlProp = new Property(HTTPEventAdapterConstants.ADAPTER_MESSAGE_URL);
        urlProp.setDisplayName(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_MESSAGE_URL));
        urlProp.setHint(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_MESSAGE_URL_HINT));
        urlProp.setRequired(true);

        Property usernameProp = new Property(HTTPEventAdapterConstants.ADAPTER_USERNAME);
        usernameProp.setDisplayName(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USERNAME));
        usernameProp.setHint(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USERNAME_HINT));
        usernameProp.setRequired(false);

        Property passwordProp = new Property(HTTPEventAdapterConstants.ADAPTER_PASSWORD);
        passwordProp.setDisplayName(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_PASSWORD));
        passwordProp.setHint(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_PASSWORD_HINT));
        passwordProp.setRequired(false);
        passwordProp.setSecured(true);
        passwordProp.setEncrypted(true);

        Property headersProp = new Property(HTTPEventAdapterConstants.ADAPTER_HEADERS);
        headersProp.setDisplayName(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_HEADERS));
        headersProp.setHint(resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_HEADERS_HINT));
        headersProp.setRequired(false);

        dynamicPropertyList.add(urlProp);
        dynamicPropertyList.add(usernameProp);
        dynamicPropertyList.add(passwordProp);
        dynamicPropertyList.add(headersProp);

        return dynamicPropertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                                                 Map<String, String> globalProperties) {
        return new HTTPEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
