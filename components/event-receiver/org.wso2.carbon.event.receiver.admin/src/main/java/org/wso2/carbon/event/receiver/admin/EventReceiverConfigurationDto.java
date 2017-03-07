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
package org.wso2.carbon.event.receiver.admin;


public class EventReceiverConfigurationDto {

    private String eventReceiverName;

    private String toStreamNameWithVersion;
    private String fromStreamNameWithVersion;

    private InputAdapterConfigurationDto fromAdapterConfigurationDto;

    private String messageFormat;

    private boolean customMappingEnabled;

    private String parentSelectorXpath;
    private EventMappingPropertyDto[] mappingPropertyDtos;
    private EventMappingPropertyDto[] metaMappingPropertyDtos;
    private EventMappingPropertyDto[] correlationMappingPropertyDtos;
    private EventMappingPropertyDto[] xpathDefinitionMappingPropertyDtos;

    public String getEventReceiverName() {
        return eventReceiverName;
    }

    public void setEventReceiverName(String eventReceiverName) {
        this.eventReceiverName = eventReceiverName;
    }

    public String getToStreamNameWithVersion() {
        return toStreamNameWithVersion;
    }

    public void setToStreamNameWithVersion(String toStreamNameWithVersion) {
        this.toStreamNameWithVersion = toStreamNameWithVersion;
    }

    public String getFromStreamNameWithVersion() {
        return fromStreamNameWithVersion;
    }

    public void setFromStreamNameWithVersion(String fromStreamNameWithVersion) {
        this.fromStreamNameWithVersion = fromStreamNameWithVersion;
    }

    public InputAdapterConfigurationDto getFromAdapterConfigurationDto() {
        return fromAdapterConfigurationDto;
    }

    public void setFromAdapterConfigurationDto(
            InputAdapterConfigurationDto fromAdapterConfigurationDto) {
        this.fromAdapterConfigurationDto = fromAdapterConfigurationDto;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public EventMappingPropertyDto[] getMappingPropertyDtos() {
        return mappingPropertyDtos;
    }

    public void setMappingPropertyDtos(EventMappingPropertyDto[] mappingPropertyDtos) {
        this.mappingPropertyDtos = mappingPropertyDtos;
    }

    public boolean isCustomMappingEnabled() {
        return customMappingEnabled;
    }

    public void setCustomMappingEnabled(boolean customMappingEnabled) {
        this.customMappingEnabled = customMappingEnabled;
    }

    public EventMappingPropertyDto[] getMetaMappingPropertyDtos() {
        return metaMappingPropertyDtos;
    }

    public void setMetaMappingPropertyDtos(EventMappingPropertyDto[] metaMappingPropertyDtos) {
        this.metaMappingPropertyDtos = metaMappingPropertyDtos;
    }

    public EventMappingPropertyDto[] getCorrelationMappingPropertyDtos() {
        return correlationMappingPropertyDtos;
    }

    public void setCorrelationMappingPropertyDtos(EventMappingPropertyDto[] correlationMappingPropertyDtos) {
        this.correlationMappingPropertyDtos = correlationMappingPropertyDtos;
    }

    public EventMappingPropertyDto[] getXpathDefinitionMappingPropertyDtos() {
        return xpathDefinitionMappingPropertyDtos;
    }

    public void setXpathDefinitionMappingPropertyDtos(EventMappingPropertyDto[] xpathDefinitionMappingPropertyDtos) {
        this.xpathDefinitionMappingPropertyDtos = xpathDefinitionMappingPropertyDtos;
    }

    public String getParentSelectorXpath() {
        return parentSelectorXpath;
    }

    public void setParentSelectorXpath(String parentSelectorXpath) {
        this.parentSelectorXpath = parentSelectorXpath;
    }
}
