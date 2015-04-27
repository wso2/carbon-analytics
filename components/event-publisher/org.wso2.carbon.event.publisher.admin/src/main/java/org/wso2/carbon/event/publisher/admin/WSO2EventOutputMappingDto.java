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


public class WSO2EventOutputMappingDto {

    EventMappingPropertyDto[] metaWSO2EventMappingProperties;

    EventMappingPropertyDto[] correlationWSO2EventMappingProperties;

    EventMappingPropertyDto[] payloadWSO2EventMappingProperties;


    public WSO2EventOutputMappingDto() {
    }


    public EventMappingPropertyDto[] getMetaWSO2EventMappingProperties() {
        return metaWSO2EventMappingProperties;
    }

    public void setMetaWSO2EventMappingProperties(
            EventMappingPropertyDto[] metaWSO2EventMappingProperties) {
        this.metaWSO2EventMappingProperties = metaWSO2EventMappingProperties;
    }

    public EventMappingPropertyDto[] getCorrelationWSO2EventMappingProperties() {
        return correlationWSO2EventMappingProperties;
    }

    public void setCorrelationWSO2EventMappingProperties(
            EventMappingPropertyDto[] correlationWSO2EventMappingProperties) {
        this.correlationWSO2EventMappingProperties = correlationWSO2EventMappingProperties;
    }

    public EventMappingPropertyDto[] getPayloadWSO2EventMappingProperties() {
        return payloadWSO2EventMappingProperties;
    }

    public void setPayloadWSO2EventMappingProperties(
            EventMappingPropertyDto[] payloadWSO2EventMappingProperties) {
        this.payloadWSO2EventMappingProperties = payloadWSO2EventMappingProperties;
    }

}
