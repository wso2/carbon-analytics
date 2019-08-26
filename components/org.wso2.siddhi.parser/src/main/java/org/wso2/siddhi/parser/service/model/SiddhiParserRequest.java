/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.parser.service.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Siddhi Parser Request Model.
 */
public class SiddhiParserRequest {

    @SerializedName("siddhiApps")
    private List<String> siddhiApps = null;

    @SerializedName("propertyMap")
    private Map<String, String> propertyMap = null;

    @SerializedName("messagingSystem")
    private MessagingSystem messagingSystem = null;

    public Map<String, String> getPropertyMap() {
        return propertyMap;
    }

    public SiddhiParserRequest setPropertyMap(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
        return this;
    }

    public List<String> getSiddhiApps() {
        return siddhiApps;
    }

    public void setSiddhiApps(List<String> siddhiApps) {
        this.siddhiApps = siddhiApps;
    }

    public MessagingSystem getMessagingSystem() {
        return messagingSystem;
    }

    public void setMessagingSystem(MessagingSystem messagingSystem) {
        this.messagingSystem = messagingSystem;
    }
}
