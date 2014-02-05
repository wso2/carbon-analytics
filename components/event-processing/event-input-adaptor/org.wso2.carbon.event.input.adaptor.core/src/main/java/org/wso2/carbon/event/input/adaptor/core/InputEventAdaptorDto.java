/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.input.adaptor.core;

import java.util.List;

/**
 * this class is used to transfer the event proxy type details to the UI. UI renders the
 * properties according to the properties specified here.
 */
public class InputEventAdaptorDto {


    /**
     * logical name of this type
     */
    private String eventAdaptorTypeName;

    /**
     * Adaptor type that supports
     */


    private List<String> supportedMessageTypes;

    /**
     * Property Lists
     */
    private List<Property> adaptorPropertyList;

    public String getEventAdaptorTypeName() {
        return eventAdaptorTypeName;
    }

    public void setEventAdaptorTypeName(String eventAdaptorTypeName) {
        this.eventAdaptorTypeName = eventAdaptorTypeName;
    }

    public List<String> getSupportedMessageTypes() {
        return supportedMessageTypes;
    }

    public void setSupportedMessageTypes(
            List<String> supportedMessageTypes) {
        this.supportedMessageTypes = supportedMessageTypes;
    }

    public List<Property> getAdaptorPropertyList() {
        return adaptorPropertyList;
    }

    public void setAdaptorPropertyList(List<Property> adaptorPropertyList) {
        this.adaptorPropertyList = adaptorPropertyList;
    }
}
