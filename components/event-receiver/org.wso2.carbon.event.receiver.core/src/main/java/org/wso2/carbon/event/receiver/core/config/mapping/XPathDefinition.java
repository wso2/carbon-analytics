/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.receiver.core.config.mapping;

public class XPathDefinition {
    private String prefix;
    private String namespaceUri;

    public XPathDefinition(String prefix, String namespaceUri) {
        this.prefix = prefix;
        this.namespaceUri = namespaceUri;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public boolean isEmpty() {
        return !((prefix != null && !prefix.isEmpty()) || (namespaceUri != null && !namespaceUri.isEmpty()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XPathDefinition that = (XPathDefinition) o;

        if (!namespaceUri.equals(that.namespaceUri)) {
            return false;
        }
        if (!prefix.equals(that.prefix)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = prefix.hashCode();
        result = 31 * result + namespaceUri.hashCode();
        return result;
    }
}
