/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Date: 7/25/13
 * Time: 12:16 PM
 */

package org.wso2.carbon.bam.jmx.agent.profiles;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MBeanAttribute {


    private String attributeName;
    /* Alias name is optional if MBean type is composite. Mandatory if MBean is a simple type */
    private String aliasName;
    private MBeanAttributeProperty[] properties;

    public String getAttributeName() {
        return attributeName;
    }

    @XmlElement
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public MBeanAttributeProperty[] getProperties() {
        return properties;
    }

    @XmlElement
    public void setProperties(MBeanAttributeProperty[] properties) {
        this.properties = properties;
    }

    public String getAliasName() {
        return aliasName;
    }

    @XmlElement
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }
}
