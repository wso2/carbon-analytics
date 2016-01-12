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
package org.wso2.carbon.event.output.adapter.soap;

import org.wso2.carbon.event.output.adapter.core.*;
import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.soap.internal.util.SoapEventAdapterConstants;


import java.util.*;

/**
 * The soap event adapter factory class to create a soap output adapter
 */
public class SoapEventAdapterFactory extends OutputEventAdapterFactory {
    private ResourceBundle resourceBundle =
            ResourceBundle.getBundle("org.wso2.carbon.event.output.adapter.soap.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return SoapEventAdapterConstants.ADAPTER_TYPE_SOAP;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.XML);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {
        return null;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        List<Property> dynamicPropertyList = new ArrayList<Property>();

        // Url
        Property host = new Property(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_URL);
        host.setDisplayName(
                resourceBundle.getString(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_URL));
        host.setRequired(true);
        host.setHint(resourceBundle.getString(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_URL_HINT));

        // Username
        Property userNameProperty = new Property(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_USERNAME);
        userNameProperty.setDisplayName(
                resourceBundle.getString(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_USERNAME));

        // Password
        Property passwordProperty = new Property(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_PASSWORD);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_PASSWORD));

        // header name
        Property soapHeaderProperty = new Property(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_HEADERS);
        soapHeaderProperty.setDisplayName(
                resourceBundle.getString(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_HEADERS));
        soapHeaderProperty.setHint(resourceBundle.getString(SoapEventAdapterConstants.ADAPTER_CONF_SOAP_HEADERS_HINT));
        soapHeaderProperty.setRequired(false);

        // header name
        Property httpHeaderProperty = new Property(SoapEventAdapterConstants.ADAPTER_CONF_HTTP_HEADERS);
        httpHeaderProperty.setDisplayName(
                resourceBundle.getString(SoapEventAdapterConstants.ADAPTER_CONF_HTTP_HEADERS));
        httpHeaderProperty.setHint(resourceBundle.getString(SoapEventAdapterConstants.ADAPTER_CONF_HTTP_HEADERS_HINT));
        httpHeaderProperty.setRequired(false);


        dynamicPropertyList.add(host);
        dynamicPropertyList.add(userNameProperty);
        dynamicPropertyList.add(passwordProperty);
        dynamicPropertyList.add(soapHeaderProperty);
        dynamicPropertyList.add(httpHeaderProperty);

        return dynamicPropertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }


    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {
        return new SoapEventAdapter(eventAdapterConfiguration, globalProperties);
    }

}
