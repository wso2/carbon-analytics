/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.input.adaptor.core.message.config;


import java.util.HashMap;
import java.util.Map;

/**
 * Input event message configuration details
 */
public class InputEventAdaptorMessageConfiguration {


    /**
     * Map contains the input message property configuration details
     */
    private Map<String, String> inputMessageProperties;


    public InputEventAdaptorMessageConfiguration() {

        this.inputMessageProperties = new HashMap<String, String>();
    }

    public void addInputMessageProperty(String name, String value) {
        this.inputMessageProperties.put(name, value);
    }


    public Map<String, String> getInputMessageProperties() {
        return inputMessageProperties;
    }

    public void setInputMessageProperties(Map<String, String> inputMessageProperties) {
        this.inputMessageProperties = inputMessageProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InputEventAdaptorMessageConfiguration)) {
            return false;
        }

        InputEventAdaptorMessageConfiguration that = (InputEventAdaptorMessageConfiguration) o;

        if (inputMessageProperties != null ? !inputMessageProperties.equals(that.inputMessageProperties) : that.inputMessageProperties != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = inputMessageProperties != null ? inputMessageProperties.hashCode() : 0;
        return result;
    }
}
