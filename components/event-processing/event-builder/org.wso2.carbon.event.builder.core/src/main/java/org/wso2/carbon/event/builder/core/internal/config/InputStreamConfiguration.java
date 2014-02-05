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

package org.wso2.carbon.event.builder.core.internal.config;


import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

public class InputStreamConfiguration {

    private String inputEventAdaptorName;
    private String inputEventAdaptorType;
    private InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration;

    public InputEventAdaptorMessageConfiguration getInputEventAdaptorMessageConfiguration() {
        return inputEventAdaptorMessageConfiguration;
    }

    public void setInputEventAdaptorMessageConfiguration(
            InputEventAdaptorMessageConfiguration EventInputAdaptorMessageConfiguration) {
        this.inputEventAdaptorMessageConfiguration = EventInputAdaptorMessageConfiguration;
    }

    public String getInputEventAdaptorType() {
        return inputEventAdaptorType;
    }

    public void setInputEventAdaptorType(String inputEventAdaptorType) {
        this.inputEventAdaptorType = inputEventAdaptorType;
    }

    public String getInputEventAdaptorName() {
        return inputEventAdaptorName;
    }

    public void setInputEventAdaptorName(String inputEventAdaptorName) {
        this.inputEventAdaptorName = inputEventAdaptorName;
    }


}
