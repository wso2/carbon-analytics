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
package org.wso2.carbon.event.output.adapter.core;

/**
 * Event Adapter property details are stored
 */
public class Property {

    private final String propertyName;
    // property is a required field or not
    private boolean isRequired = false;
    // property is a password field or not
    private boolean isSecured = false;
    // property needs encryption
    private boolean isEncrypted = false;
    // display name in ui
    private String displayName;
    // default value of the property
    private String defaultValue;
    // in out type of the property
    private String[] options;
    // hint for the property
    private String hint;

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Property(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public void setSecured(boolean secured) {
        isSecured = secured;
        if(secured){
            //By default if property is secured then it also set as encrypted
            setEncrypted(true);
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public boolean isSecured() {
        return isSecured;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.isEncrypted = encrypted;
    }
}
