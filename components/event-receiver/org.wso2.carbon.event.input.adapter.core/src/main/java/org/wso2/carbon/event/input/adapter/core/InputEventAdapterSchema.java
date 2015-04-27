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
package org.wso2.carbon.event.input.adapter.core;

import java.util.List;

/**
 * this class is used to transfer the event adapter type details to the UI. UI renders the
 * properties according to the properties specified here.
 */
public class InputEventAdapterSchema {

    private String type;
    private List<String> supportedMessageFormats;
    private List<Property> propertyList;

    public InputEventAdapterSchema(String type, List<String> supportedMessageFormats, List<Property> propertyList) {
        this.type = type;
        this.supportedMessageFormats = supportedMessageFormats;
        this.propertyList = propertyList;
    }

    public String getType() {
        return type;
    }

    public List<String> getSupportedMessageFormats() {
        return supportedMessageFormats;
    }

    public List<Property> getPropertyList() {
        return propertyList;
    }

}
