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
package org.wso2.carbon.event.publisher.core.config.mapping;

import org.wso2.carbon.event.publisher.core.config.EventOutputProperty;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.OutputMapping;

import java.util.ArrayList;
import java.util.List;

public class WSO2EventOutputMapping extends OutputMapping {

    private List<EventOutputProperty> metaWSO2EventOutputPropertyConfiguration;
    private List<EventOutputProperty> correlationWSO2EventOutputPropertyConfiguration;
    private List<EventOutputProperty> payloadWSO2EventOutputPropertyConfiguration;


    public WSO2EventOutputMapping() {
        this.metaWSO2EventOutputPropertyConfiguration = new ArrayList<EventOutputProperty>();
        this.correlationWSO2EventOutputPropertyConfiguration = new ArrayList<EventOutputProperty>();
        this.payloadWSO2EventOutputPropertyConfiguration = new ArrayList<EventOutputProperty>();
    }

    public void addMetaWSO2EventOutputPropertyConfiguration(
            EventOutputProperty eventOutputProperty) {
        this.metaWSO2EventOutputPropertyConfiguration.add(eventOutputProperty);
    }

    public void addCorrelationWSO2EventOutputPropertyConfiguration(
            EventOutputProperty eventOutputProperty) {
        this.correlationWSO2EventOutputPropertyConfiguration.add(eventOutputProperty);
    }

    public void addPayloadWSO2EventOutputPropertyConfiguration(
            EventOutputProperty eventOutputProperty) {
        this.payloadWSO2EventOutputPropertyConfiguration.add(eventOutputProperty);
    }

    public List<EventOutputProperty> getMetaWSO2EventOutputPropertyConfiguration() {
        return metaWSO2EventOutputPropertyConfiguration;
    }

    public void setMetaWSO2EventOutputPropertyConfiguration(
            List<EventOutputProperty> metaWSO2EventOutputPropertyConfiguration) {
        this.metaWSO2EventOutputPropertyConfiguration = metaWSO2EventOutputPropertyConfiguration;
    }

    public List<EventOutputProperty> getCorrelationWSO2EventOutputPropertyConfiguration() {
        return correlationWSO2EventOutputPropertyConfiguration;
    }

    public void setCorrelationWSO2EventOutputPropertyConfiguration(
            List<EventOutputProperty> correlationWSO2EventOutputPropertyConfiguration) {
        this.correlationWSO2EventOutputPropertyConfiguration = correlationWSO2EventOutputPropertyConfiguration;
    }

    public List<EventOutputProperty> getPayloadWSO2EventOutputPropertyConfiguration() {
        return payloadWSO2EventOutputPropertyConfiguration;
    }

    public void setPayloadWSO2EventOutputPropertyConfiguration(
            List<EventOutputProperty> payloadWSO2EventOutputPropertyConfiguration) {
        this.payloadWSO2EventOutputPropertyConfiguration = payloadWSO2EventOutputPropertyConfiguration;
    }

    @Override
    public String getMappingType() {
        return EventPublisherConstants.EF_WSO2EVENT_MAPPING_TYPE;
    }
}
