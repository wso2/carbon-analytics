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

import java.util.ArrayList;

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
    private ArrayList<String> options; // If not null, then option type. Otherwise, string

    public RuleTemplateProperty(String key, String fieldName, String description, String defaultValue,
                                ArrayList<String> options) {
        this.key = key;
        this.fieldName = fieldName;
        this.description = description;
        this.defaultValue = defaultValue;
        this.options = options;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "RuleTemplateProperty{" +
                "\nkey='" + key + '\'' +
                ", \nfieldName='" + fieldName + '\'' +
                ", \ndescription='" + description + '\'' +
                ", \ndefaultValue='" + defaultValue + '\'' +
                ", \noptions=" + options +
                "\n}";
    }
}
