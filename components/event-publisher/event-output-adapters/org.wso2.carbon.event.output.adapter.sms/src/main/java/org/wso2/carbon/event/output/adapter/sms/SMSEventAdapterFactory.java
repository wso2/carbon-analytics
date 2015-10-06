/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.output.adapter.sms;

import org.wso2.carbon.event.output.adapter.core.*;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.sms.internal.util.SMSEventAdapterConstants;

import java.util.*;

/**
 * The sms event adapter factory class to create a sms output adapter
 */
public class SMSEventAdapterFactory extends OutputEventAdapterFactory {
    private ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adapter.sms.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return SMSEventAdapterConstants.ADAPTER_TYPE_SMS;
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
        return null;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        List<Property> dynamicPropertyList = new ArrayList<Property>();

        Property phoneNo = new Property(SMSEventAdapterConstants.ADAPTER_MESSAGE_SMS_NO);
        phoneNo.setDisplayName(
                resourceBundle.getString(SMSEventAdapterConstants.ADAPTER_MESSAGE_SMS_NO));
        phoneNo.setHint(resourceBundle.getString(SMSEventAdapterConstants.ADAPTER_CONF_SMS_HINT_NO));
        phoneNo.setRequired(true);
        dynamicPropertyList.add(phoneNo);

        return dynamicPropertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        return new SMSEventAdapter(eventAdapterConfiguration, globalProperties);
    }

}
