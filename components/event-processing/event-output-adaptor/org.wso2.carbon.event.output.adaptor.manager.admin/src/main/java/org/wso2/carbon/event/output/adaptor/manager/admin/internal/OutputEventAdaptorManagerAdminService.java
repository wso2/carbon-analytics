package org.wso2.carbon.event.output.adaptor.manager.admin.internal;


/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.InternalOutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.manager.admin.internal.util.OutputEventAdaptorHolder;
import org.wso2.carbon.event.output.adaptor.manager.admin.internal.util.OutputEventAdaptorManagerHolder;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorFile;
import org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OutputEventAdaptorManagerAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(OutputEventAdaptorManagerAdminService.class);

    /**
     * Get the output Event Adaptor type names
     * @return Array of Event Adaptor type names
     * @throws AxisFault if Event names are empty
     */
    public String[] getOutputEventAdaptorTypeNames() throws AxisFault {
        OutputEventAdaptorHolder outputEventAdaptorHolder = OutputEventAdaptorHolder.getInstance();
        List<OutputEventAdaptorDto> eventAdaptorDtoList = outputEventAdaptorHolder.getEventAdaptorService().getEventAdaptors();
        if (eventAdaptorDtoList != null) {
            String[] eventAdaptorNames = new String[eventAdaptorDtoList.size()];
            for (int index = 0; index < eventAdaptorNames.length; index++) {
                eventAdaptorNames[index] = eventAdaptorDtoList.get(index).getEventAdaptorTypeName();
            }

            Arrays.sort(eventAdaptorNames);
            return eventAdaptorNames;

        }
        throw new AxisFault("No Output Event Adaptor type names are received.");
    }

    /**
     * To get the all event adaptor property object
     *
     * @param eventAdaptorName event adaptor name
     * @return event adaptor properties
     * @throws AxisFault
     */
    public OutputEventAdaptorPropertiesDto getOutputEventAdaptorProperties(
            String eventAdaptorName) throws AxisFault {

        OutputEventAdaptorHolder outputEventAdaptorHolder = OutputEventAdaptorHolder.getInstance();
        OutputEventAdaptorDto eventAdaptorDto = outputEventAdaptorHolder.getEventAdaptorService().getEventAdaptorDto(eventAdaptorName);

        if (eventAdaptorDto != null) {

            OutputEventAdaptorPropertiesDto outputEventAdaptorPropertiesDto = new OutputEventAdaptorPropertiesDto();
            outputEventAdaptorPropertiesDto.setOutputEventAdaptorPropertyDtos(getOutputEventAdaptorProperties(eventAdaptorDto));

            return outputEventAdaptorPropertiesDto;
        } else {
            throw new AxisFault("Output Event Adaptor Dto not found for " + eventAdaptorName);
        }
    }


    /**
     * Add Event Adaptor Configuration
     *
     * @param eventAdaptorName -name of the event adaptor to be added
     * @param eventAdaptorType -event adaptor type; jms,ws-event
     * @param outputAdaptorPropertyDtoOutputs
     *                             - output adaptor properties with values
     */
    public void deployOutputEventAdaptorConfiguration(String eventAdaptorName,
                                                      String eventAdaptorType,
                                                      OutputEventAdaptorPropertyDto[] outputAdaptorPropertyDtoOutputs)
            throws AxisFault {

        if (checkOutputEventAdaptorValidity(eventAdaptorName)) {
            try {
                OutputEventAdaptorManagerHolder outputEventAdaptorManagerHolder = OutputEventAdaptorManagerHolder.getInstance();

                OutputEventAdaptorConfiguration eventAdaptorConfiguration = new OutputEventAdaptorConfiguration();
                eventAdaptorConfiguration.setName(eventAdaptorName);
                eventAdaptorConfiguration.setType(eventAdaptorType);
                AxisConfiguration axisConfiguration = getAxisConfig();


                InternalOutputEventAdaptorConfiguration outputEventAdaptorPropertyConfiguration = new InternalOutputEventAdaptorConfiguration();
                if (outputAdaptorPropertyDtoOutputs.length != 0) {
                    for (OutputEventAdaptorPropertyDto outputEventAdaptorPropertyDto : outputAdaptorPropertyDtoOutputs) {
                        if (!outputEventAdaptorPropertyDto.getValue().trim().equals("")) {
                            outputEventAdaptorPropertyConfiguration.addEventAdaptorProperty(outputEventAdaptorPropertyDto.getKey(), outputEventAdaptorPropertyDto.getValue());
                        }
                    }
                }
                eventAdaptorConfiguration.setOutputConfiguration(outputEventAdaptorPropertyConfiguration);

                outputEventAdaptorManagerHolder.getOutputEventAdaptorManagerService().deployOutputEventAdaptorConfiguration(eventAdaptorConfiguration, axisConfiguration);

            } catch (OutputEventAdaptorManagerConfigurationException e) {
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
    public void undeployActiveOutputEventAdaptorConfiguration(String eventAdaptorName)
            throws AxisFault {
        OutputEventAdaptorManagerHolder outputEventAdaptorManager = OutputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            outputEventAdaptorManager.getOutputEventAdaptorManagerService().undeployActiveOutputEventAdaptorConfiguration(eventAdaptorName, axisConfiguration);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
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
    public void editActiveOutputEventAdaptorConfiguration(String eventAdaptorConfiguration,
                                                          String eventAdaptorName)
            throws AxisFault {
        OutputEventAdaptorManagerHolder outputEventAdaptorManager = OutputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            outputEventAdaptorManager.getOutputEventAdaptorManagerService().editActiveOutputEventAdaptorConfiguration(eventAdaptorConfiguration, eventAdaptorName, axisConfiguration);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
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
    public void editInactiveOutputEventAdaptorConfiguration(
            String eventAdaptorConfiguration,
            String fileName)
            throws AxisFault {

        OutputEventAdaptorManagerHolder outputEventAdaptorManager = OutputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            outputEventAdaptorManager.getOutputEventAdaptorManagerService().editInactiveOutputEventAdaptorConfiguration(eventAdaptorConfiguration, fileName, axisConfiguration);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * To get the event adaptor configuration file from the file system
     *
     * @param eventAdaptorName event adaptor name
     * @return Event adaptor configuration file
     * @throws AxisFault
     */
    public String getActiveOutputEventAdaptorConfigurationContent(String eventAdaptorName)
            throws AxisFault {
        OutputEventAdaptorManagerHolder outputEventAdaptorManager = OutputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            return outputEventAdaptorManager.getOutputEventAdaptorManagerService().getActiveOutputEventAdaptorConfigurationContent(eventAdaptorName, axisConfiguration);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * To get the not deployed event adaptor configuration file from the file system
     *
     * @param fileName file path of the configuration file
     * @return Event adaptor configuration file
     * @throws AxisFault
     */

    public String getInactiveOutputEventAdaptorConfigurationContent(String fileName)
            throws AxisFault {
        OutputEventAdaptorManagerHolder outputEventAdaptorManager = OutputEventAdaptorManagerHolder.getInstance();
        try {
            String eventAdaptorConfigurationFile = outputEventAdaptorManager.getOutputEventAdaptorManagerService()
                    .getInactiveOutputEventAdaptorConfigurationContent(fileName, getAxisConfig());
            return eventAdaptorConfigurationFile.trim();
        } catch (OutputEventAdaptorManagerConfigurationException e) {
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
    public void undeployInactiveOutputEventAdaptorConfiguration(String fileName)
            throws AxisFault {
        OutputEventAdaptorManagerHolder outputEventAdaptorManager = OutputEventAdaptorManagerHolder.getInstance();
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            outputEventAdaptorManager.getOutputEventAdaptorManagerService().undeployInactiveOutputEventAdaptorConfiguration(fileName, axisConfiguration);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * This method is used to get the event adaptor name and type
     * @throws AxisFault
     */
    public OutputEventAdaptorConfigurationInfoDto[] getAllActiveOutputEventAdaptorConfiguration()
            throws AxisFault {
        try {
            OutputEventAdaptorManagerHolder outputEventAdaptorManager = OutputEventAdaptorManagerHolder.getInstance();
            AxisConfiguration axisConfiguration = getAxisConfig();

            // get event adaptor configurations
            List<OutputEventAdaptorConfiguration> eventAdaptorConfigurationList;
            eventAdaptorConfigurationList = outputEventAdaptorManager.getOutputEventAdaptorManagerService().
                    getAllActiveOutputEventAdaptorConfiguration(axisConfiguration);

            if (eventAdaptorConfigurationList != null) {
                // create event adaptor configuration details array
                OutputEventAdaptorConfigurationInfoDto[] outputEventAdaptorConfigurationInfoDtoArray = new
                        OutputEventAdaptorConfigurationInfoDto[eventAdaptorConfigurationList.size()];
                for (int index = 0; index < outputEventAdaptorConfigurationInfoDtoArray.length; index++) {
                    OutputEventAdaptorConfiguration eventAdaptorConfiguration = eventAdaptorConfigurationList.get(index);
                    String eventAdaptorName = eventAdaptorConfiguration.getName();
                    String eventAdaptorType = eventAdaptorConfiguration.getType();

                    // create event adaptor configuration details with event adaptor name and type
                    outputEventAdaptorConfigurationInfoDtoArray[index] = new OutputEventAdaptorConfigurationInfoDto();
                    outputEventAdaptorConfigurationInfoDtoArray[index].setEventAdaptorName(eventAdaptorName);
                    outputEventAdaptorConfigurationInfoDtoArray[index].setEventAdaptorType(eventAdaptorType);
                    outputEventAdaptorConfigurationInfoDtoArray[index].setEnableTracing(eventAdaptorConfiguration.isEnableTracing());
                    outputEventAdaptorConfigurationInfoDtoArray[index].setEnableStats(eventAdaptorConfiguration.isEnableStatistics());
                }
                return outputEventAdaptorConfigurationInfoDtoArray;
            } else {
                return new OutputEventAdaptorConfigurationInfoDto[0];
            }
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    /**
     * Get the Event Adaptor files which are undeployed
     *
     *
     */
    public OutputEventAdaptorFileDto[] getAllInactiveOutputEventAdaptorConfiguration()
            throws AxisFault {

        OutputEventAdaptorManagerHolder outputEventAdaptorManager = OutputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        List<OutputEventAdaptorFile> outputEventAdaptorFileList = outputEventAdaptorManager.getOutputEventAdaptorManagerService().
                getAllInactiveOutputEventAdaptorConfiguration(axisConfiguration);
        if (outputEventAdaptorFileList != null) {

            // create event adaptor file details array
            OutputEventAdaptorFileDto[] outputEventAdaptorFileDtoArray = new
                    OutputEventAdaptorFileDto[outputEventAdaptorFileList.size()];

            for (int index = 0; index < outputEventAdaptorFileDtoArray.length; index++) {
                OutputEventAdaptorFile outputEventAdaptorFile = outputEventAdaptorFileList.get(index);
                String fileName = outputEventAdaptorFile.getFileName();
                String eventAdaptorName = outputEventAdaptorFile.getEventAdaptorName();

                // create event adaptor file with file path and adaptor name
                outputEventAdaptorFileDtoArray[index] = new OutputEventAdaptorFileDto(fileName, eventAdaptorName);
            }
            return outputEventAdaptorFileDtoArray;
        } else {
            return new OutputEventAdaptorFileDto[0];
        }
    }

    /**
     * To get the event adaptor configuration details with values and necessary properties
     *
     */
    public OutputEventAdaptorPropertiesDto getActiveOutputEventAdaptorConfiguration(
            String eventAdaptorName) throws AxisFault {

        try {
            OutputEventAdaptorManagerHolder outputEventAdaptorManager = OutputEventAdaptorManagerHolder.getInstance();

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            OutputEventAdaptorConfiguration eventAdaptorConfiguration;

            eventAdaptorConfiguration = outputEventAdaptorManager.getOutputEventAdaptorManagerService().
                    getActiveOutputEventAdaptorConfiguration(eventAdaptorName, tenantId);

            OutputEventAdaptorHolder outputEventAdaptorHolder = OutputEventAdaptorHolder.getInstance();
            OutputEventAdaptorDto eventAdaptorDto = outputEventAdaptorHolder.getEventAdaptorService().getEventAdaptorDto(eventAdaptorConfiguration.getType());

            if (eventAdaptorDto != null) {
                OutputEventAdaptorPropertiesDto outputEventAdaptorPropertiesDto = new OutputEventAdaptorPropertiesDto();
                outputEventAdaptorPropertiesDto.setOutputEventAdaptorPropertyDtos(getOutputEventAdaptorConfiguration(eventAdaptorConfiguration, eventAdaptorDto));

                return outputEventAdaptorPropertiesDto;
            } else {
                return null;
            }
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }


    public void testConnection(String eventAdaptorName,
                               String eventAdaptorType,
                               OutputEventAdaptorPropertyDto[] outputAdaptorPropertyDtoOutputs)
            throws AxisFault {

        try {
            OutputEventAdaptorHolder outputEventAdaptorHolder = OutputEventAdaptorHolder.getInstance();

            OutputEventAdaptorConfiguration eventAdaptorConfiguration = new OutputEventAdaptorConfiguration();
            eventAdaptorConfiguration.setName(eventAdaptorName);
            eventAdaptorConfiguration.setType(eventAdaptorType);

            InternalOutputEventAdaptorConfiguration outputEventAdaptorPropertyConfiguration = new InternalOutputEventAdaptorConfiguration();
            if (outputAdaptorPropertyDtoOutputs.length != 0) {
                for (OutputEventAdaptorPropertyDto outputEventAdaptorPropertyDto : outputAdaptorPropertyDtoOutputs) {
                    if (!outputEventAdaptorPropertyDto.getValue().trim().equals("")) {
                        outputEventAdaptorPropertyConfiguration.addEventAdaptorProperty(outputEventAdaptorPropertyDto.getKey(), outputEventAdaptorPropertyDto.getValue());
                    }
                }
            }
            eventAdaptorConfiguration.setOutputConfiguration(outputEventAdaptorPropertyConfiguration);

            outputEventAdaptorHolder.getEventAdaptorService().testConnection(eventAdaptorConfiguration);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    public void setStatisticsEnabled(String eventAdaptorName, boolean flag) throws AxisFault {

        OutputEventAdaptorManagerHolder outputEventAdaptorManagerHolder = OutputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            outputEventAdaptorManagerHolder.getOutputEventAdaptorManagerService().setStatisticsEnabled(eventAdaptorName, axisConfiguration, flag);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void setTracingEnabled(String eventAdaptorName, boolean flag) throws AxisFault {
        OutputEventAdaptorManagerHolder outputEventAdaptorManagerHolder = OutputEventAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            outputEventAdaptorManagerHolder.getOutputEventAdaptorManagerService().setTracingEnabled(eventAdaptorName, axisConfiguration, flag);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    //Private methods are below
    private OutputEventAdaptorPropertyDto[] getOutputEventAdaptorProperties(
            OutputEventAdaptorDto eventAdaptorDto)
            throws AxisFault {

        List<Property> outputPropertyList = eventAdaptorDto.getAdaptorPropertyList();
        if (outputPropertyList != null) {
            OutputEventAdaptorPropertyDto[] outputEventAdaptorPropertyDtoArray = new OutputEventAdaptorPropertyDto[outputPropertyList.size()];
            for (int index = 0; index < outputEventAdaptorPropertyDtoArray.length; index++) {
                Property property = outputPropertyList.get(index);
                // set event property parameters
                outputEventAdaptorPropertyDtoArray[index] = new OutputEventAdaptorPropertyDto(property.getPropertyName(), "");
                outputEventAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                outputEventAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                outputEventAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                outputEventAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                outputEventAdaptorPropertyDtoArray[index].setHint(property.getHint());
                outputEventAdaptorPropertyDtoArray[index].setOptions(property.getOptions());
            }
            return outputEventAdaptorPropertyDtoArray;
        }
        return new OutputEventAdaptorPropertyDto[0];
    }

    private OutputEventAdaptorPropertyDto[] getOutputEventAdaptorConfiguration(
            OutputEventAdaptorConfiguration eventAdaptorConfiguration,
            OutputEventAdaptorDto eventAdaptorDto)
            throws AxisFault {

        if (eventAdaptorConfiguration != null) {

            // get output event adaptor properties
            List<Property> outputPropertyList = eventAdaptorDto.getAdaptorPropertyList();
            if (eventAdaptorConfiguration.getOutputConfiguration() != null) {
                Map<String, String> outputEventProperties = eventAdaptorConfiguration.getOutputConfiguration().getProperties();
                if (outputPropertyList != null) {
                    OutputEventAdaptorPropertyDto[] outputEventAdaptorPropertyDtoArray = new OutputEventAdaptorPropertyDto[outputPropertyList.size()];
                    int index = 0;
                    for (Property property : outputPropertyList) {
                        // create output event property
                        outputEventAdaptorPropertyDtoArray[index] = new OutputEventAdaptorPropertyDto(property.getPropertyName(),
                                                                                                              outputEventProperties.get(property.
                                                                                                                      getPropertyName()));
                        // set output event property parameters
                        outputEventAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                        outputEventAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                        outputEventAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                        outputEventAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                        outputEventAdaptorPropertyDtoArray[index].setHint(property.getHint());
                        outputEventAdaptorPropertyDtoArray[index].setOptions(property.getOptions());

                        index++;
                    }
                    return outputEventAdaptorPropertyDtoArray;
                }
                return new OutputEventAdaptorPropertyDto[0];
            }
        }
        return new OutputEventAdaptorPropertyDto[0];

    }


    private boolean checkOutputEventAdaptorValidity(String eventAdaptorName)
            throws AxisFault {
        try {
            OutputEventAdaptorManagerHolder outputEventAdaptorManagerHolder = OutputEventAdaptorManagerHolder.getInstance();
            AxisConfiguration axisConfiguration = getAxisConfig();

            List<OutputEventAdaptorConfiguration> eventAdaptorConfigurationList;
            eventAdaptorConfigurationList = outputEventAdaptorManagerHolder.getOutputEventAdaptorManagerService().getAllActiveOutputEventAdaptorConfiguration(axisConfiguration);
            for (OutputEventAdaptorConfiguration eventAdaptorConfiguration : eventAdaptorConfigurationList) {
                if (eventAdaptorConfiguration.getName().equalsIgnoreCase(eventAdaptorName)) {
                    return false;
                }
            }

        } catch (OutputEventAdaptorManagerConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

}