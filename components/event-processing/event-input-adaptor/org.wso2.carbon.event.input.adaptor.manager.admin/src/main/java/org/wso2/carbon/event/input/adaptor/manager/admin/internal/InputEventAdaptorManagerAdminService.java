package org.wso2.carbon.event.input.adaptor.manager.admin.internal;


/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may notgetAllActiveInputEventAdaptorProperties use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorDto;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.config.InternalInputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.manager.admin.internal.util.InputEventAdaptorHolder;
import org.wso2.carbon.event.input.adaptor.manager.admin.internal.util.InputEventAdaptorManagerHolder;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorFile;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InputEventAdaptorManagerAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(InputEventAdaptorManagerAdminService.class);

    /**
     * Get the Event Adaptor names
     *
     * @return Array of Event Adaptor type names
     * @throws AxisFault if Event names are empty
     */
    public String[] getAllInputEventAdaptorTypeNames() throws AxisFault {
        InputEventAdaptorHolder inputEventAdaptorHolder = InputEventAdaptorHolder.getInstance();
        List<InputEventAdaptorDto> eventAdaptorDtoList = inputEventAdaptorHolder.getEventAdaptorService().getEventAdaptors();
        if (eventAdaptorDtoList != null) {
            String[] eventAdaptorNames = new String[eventAdaptorDtoList.size()];
            for (int index = 0; index < eventAdaptorNames.length; index++) {
                eventAdaptorNames[index] = eventAdaptorDtoList.get(index).getEventAdaptorTypeName();
            }

            Arrays.sort(eventAdaptorNames);
            return eventAdaptorNames;

        }
        throw new AxisFault("No Input Event Adaptor type names are received");
    }

    /**
     * To get the all event adaptor property object
     *
     * @param eventAdaptorName event adaptor name
     * @return event adaptor properties
     * @throws AxisFault
     */
    public InputEventAdaptorPropertiesDto getInputEventAdaptorProperties(
            String eventAdaptorName) throws AxisFault {

        InputEventAdaptorHolder inputEventAdaptorHolder = InputEventAdaptorHolder.getInstance();
        InputEventAdaptorDto eventAdaptorDto = inputEventAdaptorHolder.getEventAdaptorService().getEventAdaptorDto(eventAdaptorName);

        if (eventAdaptorDto != null) {

            InputEventAdaptorPropertiesDto inputEventAdaptorPropertiesDto = new InputEventAdaptorPropertiesDto();
            inputEventAdaptorPropertiesDto.setInputEventAdaptorPropertyDtos(getInputEventAdaptorProperties(eventAdaptorDto));

            return inputEventAdaptorPropertiesDto;
        } else {
            throw new AxisFault("Input Event Adaptor Dto not found for " + eventAdaptorName);
        }
    }


    /**
     * Add Event Adaptor Configuration
     *
     * @param eventAdaptorName          -name of the event adaptor to be added
     * @param eventAdaptorType          -event adaptor type; jms,ws-event
     * @param inputAdaptorPropertyDtoInputs - input adaptor properties with values
     */
    public void deployInputEventAdaptorConfiguration(String eventAdaptorName,
                                                         String eventAdaptorType,
                                                         InputEventAdaptorPropertyDto[] inputAdaptorPropertyDtoInputs)
            throws AxisFault {

        if (checkInputEventAdaptorValidity(eventAdaptorName)) {
            try {
                InputEventAdaptorManagerHolder inputEventAdaptorManagerHolder = InputEventAdaptorManagerHolder.getInstance();

                InputEventAdaptorConfiguration eventAdaptorConfiguration = new InputEventAdaptorConfiguration();
                eventAdaptorConfiguration.setName(eventAdaptorName);
                eventAdaptorConfiguration.setType(eventAdaptorType);
                AxisConfiguration axisConfiguration = getAxisConfig();


                InternalInputEventAdaptorConfiguration inputEventAdaptorPropertyConfiguration = new InternalInputEventAdaptorConfiguration();
                if (inputAdaptorPropertyDtoInputs.length != 0) {
                    for (InputEventAdaptorPropertyDto inputEventAdaptorPropertyDto : inputAdaptorPropertyDtoInputs) {
                        if (!inputEventAdaptorPropertyDto.getValue().trim().equals("")) {
                            inputEventAdaptorPropertyConfiguration.addEventAdaptorProperty(inputEventAdaptorPropertyDto.getKey(), inputEventAdaptorPropertyDto.getValue());
                        }
                    }
                }
                eventAdaptorConfiguration.setInputConfiguration(inputEventAdaptorPropertyConfiguration);

                inputEventAdaptorManagerHolder.getInputEventAdaptorManagerService().deployInputEventAdaptorConfiguration(eventAdaptorConfiguration, axisConfiguration);

            } catch (InputEventAdaptorManagerConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventAdaptorName + " is already registered for this tenant");
        }
    }


    /**
     * To remove a event adaptor configuration by its name
     *
     * @param eventAdaptorName event Adaptor's name
     * @throws AxisFault
     */
    public void undeployActiveInputEventAdaptorConfiguration(String eventAdaptorName)
            throws AxisFault {
        InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            inputEventAdaptorManager.getInputEventAdaptorManagerService().undeployActiveInputEventAdaptorConfiguration(eventAdaptorName, axisConfiguration);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * to edit a event adaptor configuration
     *
     * @param eventAdaptorConfiguration event adaptor configuration of the edited adaptor
     * @param eventAdaptorName          event adaptor name
     * @throws AxisFault
     */
    public void editActiveInputEventAdaptorConfiguration(String eventAdaptorConfiguration,
                                                             String eventAdaptorName)
            throws AxisFault {
        InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            inputEventAdaptorManager.getInputEventAdaptorManagerService().editActiveInputEventAdaptorConfiguration(eventAdaptorConfiguration, eventAdaptorName, axisConfiguration);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * to edit not deployed event adaptor configuration
     *
     * @param eventAdaptorConfiguration event adaptor configuration of the edited adaptor
     * @param fileName                      file path of the configuration file
     * @throws AxisFault
     */
    public void editInactiveInputEventAdaptorConfiguration(
            String eventAdaptorConfiguration,
            String fileName)
            throws AxisFault {

        InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            inputEventAdaptorManager.getInputEventAdaptorManagerService().editInactiveInputEventAdaptorConfiguration(eventAdaptorConfiguration, fileName, axisConfiguration);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when editing Input Event Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * To get the event adaptor configuration file from the file system
     *
     * @param eventAdaptorName event adaptor name
     * @return Event adaptor configuration file
     * @throws AxisFault
     */
    public String getActiveInputEventAdaptorConfigurationContent(String eventAdaptorName)
            throws AxisFault {
        InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            return inputEventAdaptorManager.getInputEventAdaptorManagerService().getActiveInputEventAdaptorConfigurationContent(eventAdaptorName, axisConfiguration);

        } catch (InputEventAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when retrieving Input Event Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * To get the not deployed event adaptor configuration file from the file system
     *
     * @param fileName file path of the configuration file
     * @return Event adaptor configuration file
     * @throws AxisFault
     */

    public String getInactiveInputEventAdaptorConfigurationContent(String fileName)
            throws AxisFault {
        InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();
        try {
            String eventAdaptorConfigurationFile = inputEventAdaptorManager.getInputEventAdaptorManagerService().getInactiveInputEventAdaptorConfigurationContent(fileName, getAxisConfig());
            return eventAdaptorConfigurationFile.trim();
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * to remove a event adaptor configuration file from the file system
     *
     * @param fileName fileName of the Event Adaptor file
     * @throws AxisFault
     */
    public void undeployInactiveInputEventAdaptorConfiguration(String fileName)
            throws AxisFault {
        InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            inputEventAdaptorManager.getInputEventAdaptorManagerService().undeployInactiveInputEventAdaptorConfiguration(fileName, axisConfiguration);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * This method is used to get the event adaptor name and type
     *
     * @throws AxisFault
     */
    public InputEventAdaptorConfigurationInfoDto[] getAllActiveInputEventAdaptorConfiguration()
            throws AxisFault {
        try {
            InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();
            AxisConfiguration axisConfiguration = getAxisConfig();

            // get event adaptor configurations
            List<InputEventAdaptorConfiguration> eventAdaptorConfigurationList;
            eventAdaptorConfigurationList = inputEventAdaptorManager.getInputEventAdaptorManagerService().
                    getAllActiveInputEventAdaptorConfiguration(axisConfiguration);

            if (eventAdaptorConfigurationList != null) {
                // create event adaptor configuration details array
                InputEventAdaptorConfigurationInfoDto[] inputEventAdaptorConfigurationInfoDtoArray = new
                        InputEventAdaptorConfigurationInfoDto[eventAdaptorConfigurationList.size()];
                for (int index = 0; index < inputEventAdaptorConfigurationInfoDtoArray.length; index++) {
                    InputEventAdaptorConfiguration eventAdaptorConfiguration = eventAdaptorConfigurationList.get(index);
                    String eventAdaptorName = eventAdaptorConfiguration.getName();
                    String eventAdaptorType = eventAdaptorConfiguration.getType();

                    // create event adaptor configuration details with event adaptor name and type
                    inputEventAdaptorConfigurationInfoDtoArray[index] = new InputEventAdaptorConfigurationInfoDto();
                    inputEventAdaptorConfigurationInfoDtoArray[index].setEventAdaptorName(eventAdaptorName);
                    inputEventAdaptorConfigurationInfoDtoArray[index].setEventAdaptorType(eventAdaptorType);
                    inputEventAdaptorConfigurationInfoDtoArray[index].setEnableTracing(eventAdaptorConfiguration.isEnableTracing());
                    inputEventAdaptorConfigurationInfoDtoArray[index].setEnableStats(eventAdaptorConfiguration.isEnableStatistics());
                }
                return inputEventAdaptorConfigurationInfoDtoArray;
            } else {
                return new InputEventAdaptorConfigurationInfoDto[0];
            }
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    /**
     * Get the Event Adaptor files which are undeployed
     */
    public InputEventAdaptorFileDto[] getAllInactiveInputEventAdaptorConfigurationFile()
            throws AxisFault {

        InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        List<InputEventAdaptorFile> inputEventAdaptorFileList = inputEventAdaptorManager.getInputEventAdaptorManagerService().
                getAllInactiveInputEventAdaptorConfiguration(axisConfiguration);
        if (inputEventAdaptorFileList != null) {

            // create event adaptor file details array
            InputEventAdaptorFileDto[] inputEventAdaptorFileDtoArray = new
                    InputEventAdaptorFileDto[inputEventAdaptorFileList.size()];

            for (int index = 0; index < inputEventAdaptorFileDtoArray.length; index++) {
                InputEventAdaptorFile inputEventAdaptorFile = inputEventAdaptorFileList.get(index);
                String fileName = inputEventAdaptorFile.getFileName();
                String eventAdaptorName = inputEventAdaptorFile.getEventAdaptorName();

                // create event adaptor file with file path and adaptor name
                inputEventAdaptorFileDtoArray[index] = new InputEventAdaptorFileDto(fileName, eventAdaptorName);
            }
            return inputEventAdaptorFileDtoArray;
        } else {
            return new InputEventAdaptorFileDto[0];
        }
    }

    /**
     * To get the event adaptor configuration details with values and necessary properties
     *
     * @param eventAdaptorName event adaptor name
     * @throws AxisFault
     */
    public InputEventAdaptorPropertiesDto getActiveInputEventAdaptorConfiguration(
            String eventAdaptorName) throws AxisFault {

        try {
            InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            InputEventAdaptorConfiguration eventAdaptorConfiguration;

            eventAdaptorConfiguration = inputEventAdaptorManager.getInputEventAdaptorManagerService().
                    getActiveInputEventAdaptorConfiguration(eventAdaptorName, tenantId);

            if (eventAdaptorConfiguration != null) {
                InputEventAdaptorHolder inputEventAdaptorHolder = InputEventAdaptorHolder.getInstance();
                InputEventAdaptorDto eventAdaptorDto = inputEventAdaptorHolder.getEventAdaptorService().getEventAdaptorDto(eventAdaptorConfiguration.getType());

                if (eventAdaptorDto != null) {
                    InputEventAdaptorPropertiesDto inputEventAdaptorPropertiesDto = new InputEventAdaptorPropertiesDto();
                    inputEventAdaptorPropertiesDto.setInputEventAdaptorPropertyDtos(getInputEventAdaptorConfiguration(eventAdaptorConfiguration, eventAdaptorDto));

                    return inputEventAdaptorPropertiesDto;
                } else {
                    return null;
                }
            } else {
                throw new AxisFault("Cannot retrieve Input Event Adaptor details for " + eventAdaptorName);
            }
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * Method used to enable or disable statistics for input event adaptor
     */
    public void setStatisticsEnabled(String eventAdaptorName, boolean flag) throws AxisFault {

        InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            inputEventAdaptorManager.getInputEventAdaptorManagerService().setStatisticsEnabled(eventAdaptorName, axisConfiguration, flag);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * Method used to enable or disable tracing for input event adaptor
     */
    public void setTracingEnabled(String eventAdaptorName, boolean flag) throws AxisFault {
        InputEventAdaptorManagerHolder inputEventAdaptorManager = InputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            inputEventAdaptorManager.getInputEventAdaptorManagerService().setTracingEnabled(eventAdaptorName, axisConfiguration, flag);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    //Private methods are below

    private InputEventAdaptorPropertyDto[] getInputEventAdaptorProperties(
            InputEventAdaptorDto eventAdaptorDto)
            throws AxisFault {

        List<Property> inputPropertyList = eventAdaptorDto.getAdaptorPropertyList();
        if (inputPropertyList != null) {
            InputEventAdaptorPropertyDto[] inputEventAdaptorPropertyDtoArray = new InputEventAdaptorPropertyDto[inputPropertyList.size()];
            for (int index = 0; index < inputEventAdaptorPropertyDtoArray.length; index++) {
                Property property = inputPropertyList.get(index);
                // set event property parameters
                inputEventAdaptorPropertyDtoArray[index] = new InputEventAdaptorPropertyDto(property.getPropertyName(), "");
                inputEventAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                inputEventAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                inputEventAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                inputEventAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                inputEventAdaptorPropertyDtoArray[index].setHint(property.getHint());
                inputEventAdaptorPropertyDtoArray[index].setOptions(property.getOptions());
            }
            return inputEventAdaptorPropertyDtoArray;
        }
        return new InputEventAdaptorPropertyDto[0];
    }


    private InputEventAdaptorPropertyDto[] getInputEventAdaptorConfiguration(
            InputEventAdaptorConfiguration eventAdaptorConfiguration,
            InputEventAdaptorDto eventAdaptorDto)
            throws AxisFault {

        if (eventAdaptorConfiguration != null) {

            // get input event adaptor properties
            List<Property> inputPropertyList = eventAdaptorDto.getAdaptorPropertyList();
            if (eventAdaptorConfiguration.getInputConfiguration() != null) {
                Map<String, String> inputEventProperties = eventAdaptorConfiguration.getInputConfiguration().getProperties();
                if (inputPropertyList != null) {
                    InputEventAdaptorPropertyDto[] inputEventAdaptorPropertyDtoArray = new InputEventAdaptorPropertyDto[inputPropertyList.size()];
                    int index = 0;
                    for (Property property : inputPropertyList) {
                        // create input event property
                        inputEventAdaptorPropertyDtoArray[index] = new InputEventAdaptorPropertyDto(property.getPropertyName(),
                                                                                                            inputEventProperties.get(property.
                                                                                                                    getPropertyName()));
                        // set input event property parameters
                        inputEventAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                        inputEventAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                        inputEventAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                        inputEventAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                        inputEventAdaptorPropertyDtoArray[index].setHint(property.getHint());
                        inputEventAdaptorPropertyDtoArray[index].setOptions(property.getOptions());

                        index++;
                    }
                    return inputEventAdaptorPropertyDtoArray;
                }
                return new InputEventAdaptorPropertyDto[0];
            }
        }
        return new InputEventAdaptorPropertyDto[0];

    }


    private boolean checkInputEventAdaptorValidity(String eventAdaptorName)
            throws AxisFault {
        try {
            InputEventAdaptorManagerHolder inputEventAdaptorManagerHolder = InputEventAdaptorManagerHolder.getInstance();
            AxisConfiguration axisConfiguration = getAxisConfig();

            List<InputEventAdaptorConfiguration> eventAdaptorConfigurationList;
            eventAdaptorConfigurationList = inputEventAdaptorManagerHolder.getInputEventAdaptorManagerService().getAllActiveInputEventAdaptorConfiguration(axisConfiguration);
            for (InputEventAdaptorConfiguration eventAdaptorConfiguration : eventAdaptorConfigurationList) {
                if (eventAdaptorConfiguration.getName().equalsIgnoreCase(eventAdaptorName)) {
                    return false;
                }
            }

        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

}