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

package org.wso2.carbon.event.input.adapter.core.internal.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 3/2/15.
 */
public class AdapterConfig {

    private String type;
    private List<Property> globalProperties = new ArrayList<Property>();

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    public List<Property> getGlobalProperties() {
        return globalProperties;
    }

    @XmlElement(name = "property")
    public void setGlobalProperties(List<Property> globalProperties) {
        this.globalProperties = globalProperties;
    }

    public Property getProperty(String key) {
        Property matchedProperty = null;
        for (Property property : globalProperties) {
            if (property.getKey().equals(key)) {
                matchedProperty = property;
                break;
            }
        }
        return matchedProperty;
    }

    public Map<String, String> getGlobalPropertiesAsMap() {
        Map<String, String> properties = new HashMap<String, String>();
        for (Property property : globalProperties) {
            properties.put(property.getKey(), property.getValue());
        }
        return properties;
    }
}
