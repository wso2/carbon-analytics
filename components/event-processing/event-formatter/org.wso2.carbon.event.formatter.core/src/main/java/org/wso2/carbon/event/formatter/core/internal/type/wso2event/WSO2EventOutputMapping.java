/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.formatter.core.internal.type.wso2event;

import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.config.OutputMapping;
import org.wso2.carbon.event.formatter.core.internal.config.EventOutputProperty;

import java.util.ArrayList;
import java.util.List;

public class WSO2EventOutputMapping extends OutputMapping {

    List<EventOutputProperty> metaWSO2EventOutputPropertyConfiguration;

    List<EventOutputProperty> correlationWSO2EventOutputPropertyConfiguration;

    List<EventOutputProperty> payloadWSO2EventOutputPropertyConfiguration;


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
        return EventFormatterConstants.EF_WSO2EVENT_MAPPING_TYPE;
    }
}
