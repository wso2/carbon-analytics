/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models;

import java.util.Set;

/**
 * Denotes a special identifier for an extension.
 * This is applicable when an extension's name cannot be directly matched with a
 * {@link org.wso2.carbon.siddhi.extensions.installer.core.models.SiddhiAppExtensionUsage}.
 */
public class ExtensionIdentifierConfig {
    private String type;
    private String uniqueAttribute;
    private String uniqueAttributeValue;
    private String uniqueAttributeValueRegex;
    private Set<String> alternativeTypes;

    public boolean isUniqueAttributeValueValid() {
        return type != null && uniqueAttribute != null && uniqueAttributeValue != null;
    }

    public boolean isUniqueAttributeValueRegexValid() {
        return type != null && uniqueAttribute != null && uniqueAttributeValueRegex != null;
    }

    public boolean isAlternativeTypeValid() {
        return alternativeTypes != null && !alternativeTypes.isEmpty();
    }

    public Set<String> getAlternativeTypes() {
        return alternativeTypes;
    }

    public String getType() {
        return type;
    }

    public String getUniqueAttribute() {
        return uniqueAttribute;
    }

    public String getUniqueAttributeValue() {
        return uniqueAttributeValue;
    }

    public String getUniqueAttributeValueRegex() {
        return uniqueAttributeValueRegex;
    }
}
