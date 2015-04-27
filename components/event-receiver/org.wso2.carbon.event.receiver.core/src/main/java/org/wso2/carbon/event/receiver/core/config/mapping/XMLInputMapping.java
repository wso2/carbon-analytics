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
package org.wso2.carbon.event.receiver.core.config.mapping;


import org.wso2.carbon.event.receiver.core.config.InputMapping;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;

import java.util.ArrayList;
import java.util.List;

public class XMLInputMapping extends InputMapping {

    private List<XPathDefinition> xpathDefinitions;
    private String parentSelectorXpath;

    public XMLInputMapping() {
        this.xpathDefinitions = new ArrayList<XPathDefinition>();
    }

    public String getParentSelectorXpath() {
        return parentSelectorXpath;
    }

    public void setParentSelectorXpath(String parentSelectorXpath) {
        this.parentSelectorXpath = parentSelectorXpath;
    }

    public List<XPathDefinition> getXPathDefinitions() {
        return xpathDefinitions;
    }

    public void setXPathDefinitions(List<XPathDefinition> XPathDefinitions) {
        this.xpathDefinitions = XPathDefinitions;
    }

    @Override
    public String getMappingType() {
        return EventReceiverConstants.ER_XML_MAPPING_TYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XMLInputMapping that = (XMLInputMapping) o;

        if (parentSelectorXpath != null ? !parentSelectorXpath.equals(that.parentSelectorXpath) : that.parentSelectorXpath != null) {
            return false;
        }
        if (!xpathDefinitions.equals(that.xpathDefinitions)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = xpathDefinitions.hashCode();
        result = 31 * result + (parentSelectorXpath != null ? parentSelectorXpath.hashCode() : 0);
        return result;
    }
}
