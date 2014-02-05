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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.builder.admin.exception.EventBuilderAdminServiceException;
import org.wso2.carbon.event.builder.admin.internal.ds.EventBuilderAdminServiceValueHolder;
import org.wso2.carbon.event.builder.admin.internal.util.DtoConverter;
import org.wso2.carbon.event.builder.admin.internal.util.DtoConverterFactory;
import org.wso2.carbon.event.builder.admin.internal.util.EventBuilderAdminUtil;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.EventBuilderConfigurationFile;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.MessageDto;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorInfo;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;

import java.util.List;

public class EventBuilderAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(EventBuilderAdminService.class);
    private DtoConverterFactory dtoConverterFactory;

    public EventBuilderAdminService() {
        dtoConverterFactory = new DtoConverterFactory();
    }

    public InputEventAdaptorInfoDto[] getInputEventAdaptorInfo() throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        InputEventAdaptorManagerService inputEventAdaptorManagerService = EventBuilderAdminServiceValueHolder.getInputEventAdaptorManagerService();
        List<InputEventAdaptorInfo> inputEventAdaptorInfoList = inputEventAdaptorManagerService.getInputEventAdaptorInfo(tenantId);
        if (inputEventAdaptorInfoList != null && !inputEventAdaptorInfoList.isEmpty()) {
            InputEventAdaptorInfoDto[] InputEventAdaptorInfoDtos = new InputEventAdaptorInfoDto[inputEventAdaptorInfoList.size()];
            for (int i = 0; i < inputEventAdaptorInfoList.size(); i++) {
                InputEventAdaptorInfo InputEventAdaptorInfo = inputEventAdaptorInfoList.get(i);
                InputEventAdaptorInfoDtos[i] = new InputEventAdaptorInfoDto(InputEventAdaptorInfo.getEventAdaptorName(), InputEventAdaptorInfo.getEventAdaptorType());
            }
            return InputEventAdaptorInfoDtos;
        }

        return new InputEventAdaptorInfoDto[0];
    }

    public String[] getSupportedInputMappingTypes(String inputEventAdaptorName) throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<String> supportedInputMappingTypes = eventBuilderService.getSupportedInputMappingTypes(inputEventAdaptorName, tenantId);
        return supportedInputMappingTypes.toArray(new String[supportedInputMappingTypes.size()]);
    }

    public void deployEventBuilderConfiguration(
            EventBuilderConfigurationDto eventBuilderConfigurationDto)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfigurationDto.getInputMappingType());
        EventBuilderConfiguration eventBuilderConfiguration;
        try {
            eventBuilderConfiguration = dtoConverter.toEventBuilderConfiguration(eventBuilderConfigurationDto, tenantId);
        } catch (EventBuilderAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventBuilderConfiguration != null) {
            try {
                eventBuilderService.deployEventBuilderConfiguration(eventBuilderConfiguration, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }
    }

    public void undeployActiveConfiguration(String eventBuilderName) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        try {
            eventBuilderService.undeployActiveEventBuilderConfiguration(eventBuilderName, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void undeployInactiveEventBuilderConfiguration(String filename) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        try {
            eventBuilderService.undeployInactiveEventBuilderConfiguration(filename, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventBuilderConfigurationDto getActiveEventBuilderConfiguration(String eventBuilderName)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        EventBuilderConfiguration eventBuilderConfiguration = eventBuilderService.getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfiguration.getInputMapping().getMappingType());

        try {
            return dtoConverter.fromEventBuilderConfiguration(eventBuilderConfiguration);
        } catch (EventBuilderAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getActiveEventBuilderConfigurationContent(String eventBuilderName) throws AxisFault {
        try {
            EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
            return eventBuilderService.getActiveEventBuilderConfigurationContent(eventBuilderName, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getInactiveEventBuilderConfigurationContent(String filename) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        String eventBuilderConfigurationContent;
        try {
            eventBuilderConfigurationContent = eventBuilderService.getInactiveEventBuilderConfigurationContent(filename, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return eventBuilderConfigurationContent;
    }

    public void editActiveEventBuilderConfiguration(String originalEventBuilderName,
                                                    String eventBuilderConfigXml) throws AxisFault {
        if (eventBuilderConfigXml != null && !eventBuilderConfigXml.isEmpty() && originalEventBuilderName != null && !originalEventBuilderName.isEmpty()) {
            EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
            try {
                eventBuilderService.editActiveEventBuilderConfiguration(eventBuilderConfigXml, originalEventBuilderName, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            String errMsg = "Some required parameters were null or empty. Cannot proceed with updating.";
            log.error(errMsg);
            throw new AxisFault(errMsg);
        }
    }

    public void editInactiveEventBuilderConfiguration(String filename, String eventBuilderConfigXml)
            throws AxisFault {
        if (eventBuilderConfigXml != null && !eventBuilderConfigXml.isEmpty() && filename != null && !filename.isEmpty()) {
            EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
            try {
                eventBuilderService.editInactiveEventBuilderConfiguration(eventBuilderConfigXml, filename, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            String errMsg = "Some required parameters were null or empty. Cannot proceed with updating.";
            log.error(errMsg);
            throw new AxisFault(errMsg);
        }
    }

    public String[] getAllInactiveEventBuilderConfigurations() {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = eventBuilderService.getAllInactiveEventBuilderConfigurations(getAxisConfig());
        if (eventBuilderConfigurationFileList != null) {
            String[] fileNameList = new String[eventBuilderConfigurationFileList.size()];
            int i = 0;
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileList) {
                fileNameList[i++] = EventBuilderAdminUtil.deriveConfigurationFilenameFrom(eventBuilderConfigurationFile.getFileName());
            }
            return fileNameList;
        }
        return new String[0];
    }

    public EventBuilderConfigurationDto[] getAllActiveEventBuilderConfigurations()
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<EventBuilderConfiguration> eventBuilderConfigurationList = eventBuilderService.getAllActiveEventBuilderConfigurations(tenantId);
        if (eventBuilderConfigurationList != null && !eventBuilderConfigurationList.isEmpty()) {
            EventBuilderConfigurationDto[] eventBuilderConfigurationDtos = new EventBuilderConfigurationDto[eventBuilderConfigurationList.size()];
            for (int i = 0; i < eventBuilderConfigurationList.size(); i++) {
                EventBuilderConfiguration eventBuilderConfiguration = eventBuilderConfigurationList.get(i);
                DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfiguration.getInputMapping().getMappingType());
                EventBuilderConfigurationDto eventBuilderConfigurationDto;
                try {
                    eventBuilderConfigurationDto = dtoConverter.fromEventBuilderConfiguration(eventBuilderConfiguration);
                } catch (EventBuilderAdminServiceException e) {
                    throw new AxisFault(e.getMessage());
                }
                eventBuilderConfigurationDtos[i] = eventBuilderConfigurationDto;
            }

            return eventBuilderConfigurationDtos;
        }

        return new EventBuilderConfigurationDto[0];
    }

    public EventBuilderConfigurationDto[] getAllStreamSpecificActiveEventBuilderConfiguration(String streamId)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<EventBuilderConfiguration> eventBuilderConfigurationList = eventBuilderService.getAllStreamSpecificActiveEventBuilderConfigurations(tenantId,streamId);
        if (eventBuilderConfigurationList != null && !eventBuilderConfigurationList.isEmpty()) {
            EventBuilderConfigurationDto[] eventBuilderConfigurationDtos = new EventBuilderConfigurationDto[eventBuilderConfigurationList.size()];
            for (int i = 0; i < eventBuilderConfigurationList.size(); i++) {
                EventBuilderConfiguration eventBuilderConfiguration = eventBuilderConfigurationList.get(i);
                DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfiguration.getInputMapping().getMappingType());
                EventBuilderConfigurationDto eventBuilderConfigurationDto;
                try {
                    eventBuilderConfigurationDto = dtoConverter.fromEventBuilderConfiguration(eventBuilderConfiguration);
                } catch (EventBuilderAdminServiceException e) {
                    throw new AxisFault(e.getMessage());
                }
                eventBuilderConfigurationDtos[i] = eventBuilderConfigurationDto;
            }

            return eventBuilderConfigurationDtos;
        }

        return new EventBuilderConfigurationDto[0];
    }

    public void setTraceEnabled(String eventBuilderName, boolean traceEnabled) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        try {
            eventBuilderService.setTraceEnabled(eventBuilderName, traceEnabled, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void setStatisticsEnabled(String eventBuilderName, boolean statisticsEnabled) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        try {
            eventBuilderService.setStatisticsEnabled(eventBuilderName, statisticsEnabled, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventBuilderPropertyDto[] getMessageConfigurationProperties(
            String inputEventAdaptorName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        InputEventAdaptorService InputEventAdaptorService = EventBuilderAdminServiceValueHolder.getInputEventAdaptorService();
        InputEventAdaptorManagerService InputEventAdaptorManagerService = EventBuilderAdminServiceValueHolder.getInputEventAdaptorManagerService();
        InputEventAdaptorConfiguration InputEventAdaptorConfiguration = null;
        try {
            InputEventAdaptorConfiguration = InputEventAdaptorManagerService.getActiveInputEventAdaptorConfiguration(inputEventAdaptorName, tenantId);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            String errorMsg = "Error retrieving input event adaptor configuration with name '" + inputEventAdaptorName + "' " + e.getMessage();
            log.error(errorMsg, e);
        }
        if (InputEventAdaptorConfiguration != null) {
            MessageDto messageDto = InputEventAdaptorService.getEventMessageDto(InputEventAdaptorConfiguration.getType());
            DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(null);
            return dtoConverter.getEventBuilderPropertiesFrom(messageDto, null);
        }

        return new EventBuilderPropertyDto[0];
    }

    public EventBuilderPropertyDto[] getMessageConfigurationPropertiesWithValue(
            String eventBuilderName)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        EventBuilderConfiguration eventBuilderConfiguration = eventBuilderService.getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        String inputEventAdaptorName = eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorName();
        InputEventAdaptorService InputEventAdaptorService = EventBuilderAdminServiceValueHolder.getInputEventAdaptorService();
        InputEventAdaptorManagerService InputEventAdaptorManagerService = EventBuilderAdminServiceValueHolder.getInputEventAdaptorManagerService();
        InputEventAdaptorConfiguration InputEventAdaptorConfiguration = null;
        try {
            InputEventAdaptorConfiguration = InputEventAdaptorManagerService.getActiveInputEventAdaptorConfiguration(inputEventAdaptorName, tenantId);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error("Error retrieving input event adaptor configuration with name '" + inputEventAdaptorName + "' " + e.getMessage(), e);
        }
        if (InputEventAdaptorConfiguration != null) {
            MessageDto messageDto = InputEventAdaptorService.getEventMessageDto(InputEventAdaptorConfiguration.getType());
            DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(null);
            return dtoConverter.getEventBuilderPropertiesFrom(messageDto, eventBuilderConfiguration);
        }

        return new EventBuilderPropertyDto[0];
    }

    public String getEventBuilderStatusAsString(String filename) {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        return eventBuilderService.getEventBuilderStatusAsString(filename);
    }
}
