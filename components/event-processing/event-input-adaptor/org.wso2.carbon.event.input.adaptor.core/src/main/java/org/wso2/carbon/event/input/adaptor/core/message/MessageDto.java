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

package org.wso2.carbon.event.input.adaptor.core.message;


import org.wso2.carbon.event.input.adaptor.core.Property;

import java.util.List;

/**
 * this class used to event the event adaptor message related DTO
 */
public class MessageDto {


    /**
     * logical name of this type
     */
    private String adaptorName;

    private List<Property> messageInPropertyList;

    public void addMessageInProperty(Property messageInProperty) {
        this.messageInPropertyList.add(messageInProperty);
    }

    public List<Property> getMessageInPropertyList() {
        return messageInPropertyList;
    }

    public void setMessageInPropertyList(List<Property> messageInPropertyList) {
        this.messageInPropertyList = messageInPropertyList;
    }

    public String getAdaptorName() {
        return adaptorName;
    }

    public void setAdaptorName(String adaptorName) {
        this.adaptorName = adaptorName;
    }

}
