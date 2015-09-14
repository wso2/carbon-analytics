/*
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
package org.wso2.carbon.analytics.dataservice.core.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * This represents the configuration section for analytics record store.
 */
public class AnalyticsRecordStoreConfiguration {

    private String name;
    
    private String implementation;
    
    private AnalyticsDataServiceConfigProperty[] properties;
    
    @XmlAttribute (required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @XmlElement (nillable = false)
    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    @XmlElementWrapper (name = "properties")
    @XmlElement (name = "property")
    public AnalyticsDataServiceConfigProperty[] getProperties() {
        return properties;
    }

    public void setProperties(AnalyticsDataServiceConfigProperty[] properties) {
        this.properties = properties;
    }
    
}
