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
package org.wso2.carbon.event.publisher.admin;


public class OutputAdapterConfigurationDto {

    private String eventAdapterType;

    private String[] supportedMessageFormats;

    private DetailOutputAdapterPropertyDto[] outputEventAdapterStaticProperties;

    private DetailOutputAdapterPropertyDto[] outputEventAdapterDynamicProperties;

    public String getEventAdapterType() {
        return eventAdapterType;
    }

    public void setEventAdapterType(String eventAdapterType) {
        this.eventAdapterType = eventAdapterType;
    }

    public DetailOutputAdapterPropertyDto[] getOutputEventAdapterStaticProperties() {
        return outputEventAdapterStaticProperties;
    }

    public void setOutputEventAdapterStaticProperties(
            DetailOutputAdapterPropertyDto[] outputEventAdapterMessageConfiguration) {
        this.outputEventAdapterStaticProperties = outputEventAdapterMessageConfiguration;
    }

    public String[] getSupportedMessageFormats() {
        return supportedMessageFormats;
    }

    public void setSupportedMessageFormats(String[] supportedMessageFormats) {
        this.supportedMessageFormats = supportedMessageFormats;
    }

    public DetailOutputAdapterPropertyDto[] getOutputEventAdapterDynamicProperties() {
        return outputEventAdapterDynamicProperties;
    }

    public void setOutputEventAdapterDynamicProperties(DetailOutputAdapterPropertyDto[] outputEventAdapterDynamicProperties) {
        this.outputEventAdapterDynamicProperties = outputEventAdapterDynamicProperties;
    }
}
