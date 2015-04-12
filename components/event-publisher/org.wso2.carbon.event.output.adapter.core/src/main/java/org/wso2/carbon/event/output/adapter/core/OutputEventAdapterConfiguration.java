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


import java.util.Map;

/**
 * This class contain the configuration details of the event
 */

public class OutputEventAdapterConfiguration {

    private String name;
    private String type;
    private String messageFormat;
    private Map<String, String> staticProperties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public Map<String, String> getStaticProperties() {
        return staticProperties;
    }

    public void setStaticProperties(Map<String, String> staticProperties) {
        this.staticProperties = staticProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutputEventAdapterConfiguration)) return false;

        OutputEventAdapterConfiguration that = (OutputEventAdapterConfiguration) o;

        if (messageFormat != null ? !messageFormat.equals(that.messageFormat) : that.messageFormat != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (staticProperties != null ? !staticProperties.equals(that.staticProperties) : that.staticProperties != null)
            return false;
        return !(type != null ? !type.equals(that.type) : that.type != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (messageFormat != null ? messageFormat.hashCode() : 0);
        result = 31 * result + (staticProperties != null ? staticProperties.hashCode() : 0);
        return result;
    }
}