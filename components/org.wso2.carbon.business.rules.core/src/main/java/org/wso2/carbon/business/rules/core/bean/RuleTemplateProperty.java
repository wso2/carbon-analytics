/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.business.rules.core.bean;

import java.util.List;

/**
 * Represents a Property, that is a templated element of any template,
 * which belongs to a Rule Template
 * (Eg: 'fieldName' in a SiddhiApp template)
 */
public class RuleTemplateProperty {
    private String key; // Denoted key of the object
    private String fieldName;
    private String description; // Optional
    private String defaultValue;
    private List<String> options; // If not null, then option type. Otherwise, string

    public RuleTemplateProperty(String key, String fieldName, String description, String defaultValue,
                                List<String> options) {
        this.key = key;
        this.fieldName = fieldName;
        this.description = description;
        this.defaultValue = defaultValue;
        this.options = options;
    }

    public String getKey() {
        return key;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<String> getOptions() {
        return options;
    }
}
