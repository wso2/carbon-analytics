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

package org.wso2.carbon.event.builder.admin.internal.util;

import org.wso2.carbon.event.builder.admin.exception.EventBuilderAdminServiceException;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderPropertyDto;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.message.MessageDto;

import java.util.List;
import java.util.Map;

public abstract class DtoConverter {
    /**
     * Returns {@link MessageDto} properties of the input event adaptor
     * of the passed in event builder as an array of {@link EventBuilderPropertyDto}
     *
     * @param messageDto                the message dto to be used for extracting the properties needed
     * @param eventBuilderConfiguration the event builder configuration which will be used to extract information about
     *                                  the InputEventAdaptorConfiguration
     * @return an array of {@link EventBuilderPropertyDto}
     */
    public EventBuilderPropertyDto[] getEventBuilderPropertiesFrom(MessageDto messageDto, EventBuilderConfiguration eventBuilderConfiguration) {
        List<Property> messageDtoPropertyList = messageDto.getMessageInPropertyList();
        EventBuilderPropertyDto[] eventBuilderPropertyDtos = new EventBuilderPropertyDto[messageDtoPropertyList.size()];
        int i = 0;
        if (eventBuilderConfiguration != null) {
            Map<String, String> propertyValueMap = eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorMessageConfiguration().getInputMessageProperties();
            for (Property property : messageDtoPropertyList) {
                String value = propertyValueMap.get(property.getPropertyName());
                EventBuilderPropertyDto eventBuilderPropertyDto = getEventBuilderPropertyFrom(property, value);
                eventBuilderPropertyDtos[i++] = eventBuilderPropertyDto;
            }
        } else {
            for (Property property : messageDtoPropertyList) {
                EventBuilderPropertyDto eventBuilderPropertyDto = getEventBuilderPropertyFrom(property, null);
                eventBuilderPropertyDtos[i++] = eventBuilderPropertyDto;
            }
        }

        return eventBuilderPropertyDtos;

    }

    /**
     * Returns an {@link EventBuilderConfiguration} corresponding to the passed in {@link EventBuilderConfigurationDto}
     *
     * @param eventBuilderConfigurationDto the {@link EventBuilderConfigurationDto} object to be converted
     * @param tenantId                     the tenant id of the calling tenant
     * @return {@link EventBuilderConfiguration} instance populated from the passed in parameters.
     * @throws EventBuilderAdminServiceException
     *
     */
    public abstract EventBuilderConfiguration toEventBuilderConfiguration(
            EventBuilderConfigurationDto eventBuilderConfigurationDto, int tenantId) throws
            EventBuilderAdminServiceException;

    /**
     * Returns an {@link EventBuilderConfigurationDto} for the passed in {@link EventBuilderConfiguration}
     *
     * @param eventBuilderConfiguration the event builder configuration that needs to be converted to a DTO
     * @return an {@link EventBuilderConfigurationDto} instance that matches the passed in EventBuilderConfiguration
     * @throws EventBuilderAdminServiceException
     *
     */
    public abstract EventBuilderConfigurationDto fromEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration)
            throws EventBuilderAdminServiceException;

    private EventBuilderPropertyDto getEventBuilderPropertyFrom(Property msgDtoProperty,
                                                                String value) {
        String key = msgDtoProperty.getPropertyName() + EventBuilderAdminConstants.FROM_SUFFIX;
        EventBuilderPropertyDto eventBuilderPropertyDto = new EventBuilderPropertyDto();
        eventBuilderPropertyDto.setKey(key);
        eventBuilderPropertyDto.setDefaultValue(msgDtoProperty.getDefaultValue());
        eventBuilderPropertyDto.setDisplayName(msgDtoProperty.getDisplayName());
        eventBuilderPropertyDto.setHint(msgDtoProperty.getHint());
        eventBuilderPropertyDto.setRequired(msgDtoProperty.isRequired());
        eventBuilderPropertyDto.setSecured(msgDtoProperty.isSecured());
        if (value != null) {
            eventBuilderPropertyDto.setValue(value);
        } else {
            eventBuilderPropertyDto.setValue(msgDtoProperty.getDefaultValue());
        }
        eventBuilderPropertyDto.setOptions(msgDtoProperty.getOptions());

        return eventBuilderPropertyDto;
    }

}
