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

package org.wso2.carbon.event.builder.admin.internal;

/**
 * Event Builder Configuration Details are stored in this class
 */
public class EventBuilderConfigurationDto {
    private String eventBuilderConfigName;
    private String inputMappingType;
    private String inputEventAdaptorName;
    private String inputEventAdaptorType;
    private String toStreamName;
    private String toStreamVersion;
    private boolean traceEnabled;
    private boolean statisticsEnabled;
    private EventBuilderPropertyDto[] eventBuilderPropertyDtos;

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    public String getToStreamName() {
        return toStreamName;
    }

    public void setToStreamName(String toStreamName) {
        this.toStreamName = toStreamName;
    }

    public String getToStreamVersion() {
        return toStreamVersion;
    }

    public void setToStreamVersion(String toStreamVersion) {
        this.toStreamVersion = toStreamVersion;
    }

    public String getInputEventAdaptorType() {
        return inputEventAdaptorType;
    }

    public void setInputEventAdaptorType(String InputEventAdaptorType) {
        this.inputEventAdaptorType = InputEventAdaptorType;
    }

    public String getInputEventAdaptorName() {
        return inputEventAdaptorName;
    }

    public void setInputEventAdaptorName(String InputEventAdaptorName) {
        this.inputEventAdaptorName = InputEventAdaptorName;
    }

    public String getInputMappingType() {
        return inputMappingType;
    }

    public void setInputMappingType(String inputMappingType) {
        this.inputMappingType = inputMappingType;
    }

    public String getEventBuilderConfigName() {
        return eventBuilderConfigName;
    }

    public void setEventBuilderConfigName(String eventBuilderConfigName) {
        this.eventBuilderConfigName = eventBuilderConfigName;
    }

    public EventBuilderPropertyDto[] getEventBuilderProperties() {
        return this.eventBuilderPropertyDtos;
    }

    public void setEventBuilderProperties(EventBuilderPropertyDto[] eventBuilderProperties) {
        this.eventBuilderPropertyDtos = eventBuilderProperties;
    }
}
