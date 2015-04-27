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
package org.wso2.carbon.event.receiver.core.internal.type.xml.config;

import org.apache.axiom.om.xpath.AXIOMXPath;

public class XPathData {
    private String type;
    private AXIOMXPath xpath;
    private String defaultValue;

    public XPathData(AXIOMXPath xpath, String type) {
        this(xpath, type, null);
    }

    public XPathData(AXIOMXPath xpath, String type, String defaultValue) {
        this.type = type;
        this.xpath = xpath;
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public AXIOMXPath getXpath() {
        return xpath;
    }

    public String getType() {
        return type;
    }
}
