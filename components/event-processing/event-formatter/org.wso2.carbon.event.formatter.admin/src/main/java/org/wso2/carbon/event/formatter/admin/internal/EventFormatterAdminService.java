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

package org.wso2.carbon.event.formatter.admin.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.admin.internal.util.EventFormatterAdminServiceValueHolder;
import org.wso2.carbon.event.formatter.admin.internal.util.PropertyAttributeTypeConstants;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.config.EventOutputProperty;
import org.wso2.carbon.event.formatter.core.internal.config.ToPropertyConfiguration;
import org.wso2.carbon.event.formatter.core.internal.type.json.JSONOutputMapping;
import org.wso2.carbon.event.formatter.core.internal.type.map.MapOutputMapping;
import org.wso2.carbon.event.formatter.core.internal.type.text.TextOutputMapping;
import org.wso2.carbon.event.formatter.core.internal.type.wso2event.WSO2EventOutputMapping;
import org.wso2.carbon.event.formatter.core.internal.type.xml.XMLOutputMapping;
import org.wso2.carbon.event.formatter.core.internal.util.EventFormatterConfigurationFile;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.message.MessageDto;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorInfo;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventFormatterAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventFormatterAdminService.class);


    public EventFormatterConfigurationInfoDto[] getAllActiveEventFormatterConfiguration()
            throws AxisFault {

        try {
            EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

            AxisConfiguration axisConfiguration = getAxisConfig();

            // get event formatter configurations
            List<EventFormatterConfiguration> eventFormatterConfigurationList;
            eventFormatterConfigurationList = eventFormatterService.getAllActiveEventFormatterConfiguration(axisConfiguration);

            if (eventFormatterConfigurationList != null) {
                // create event formatter configuration details array
                EventFormatterConfigurationInfoDto[] eventFormatterConfigurationInfoDtoArray = new
                        EventFormatterConfigurationInfoDto[eventFormatterConfigurationList.size()];
                for (int index = 0; index < eventFormatterConfigurationInfoDtoArray.length; index++) {
                    EventFormatterConfiguration eventFormatterConfiguration = eventFormatterConfigurationList.get(index);
                    String eventFormatterName = eventFormatterConfiguration.getEventFormatterName();
                    String mappingType = eventFormatterConfiguration.getOutputMapping().getMappingType();
                    String outputEventAdaptorName = eventFormatterConfiguration.getToPropertyConfiguration().getEventAdaptorName();
                    String streamNameWithVersion = eventFormatterConfiguration.getFromStreamName() + ":" + eventFormatterConfiguration.getFromStreamVersion();


                    eventFormatterConfigurationInfoDtoArray[index] = new EventFormatterConfigurationInfoDto();
                    eventFormatterConfigurationInfoDtoArray[index].setEventFormatterName(eventFormatterName);
                    eventFormatterConfigurationInfoDtoArray[index].setMappingType(mappingType);
                    eventFormatterConfigurationInfoDtoArray[index].setOutEventAdaptorName(outputEventAdaptorName);
                    eventFormatterConfigurationInfoDtoArray[index].setInputStreamId(streamNameWithVersion);
                    eventFormatterConfigurationInfoDtoArray[index].setEnableStats(eventFormatterConfiguration.isEnableStatistics());
                    eventFormatterConfigurationInfoDtoArray[index].setEnableTracing(eventFormatterConfiguration.isEnableTracing());
                }
                return eventFormatterConfigurationInfoDtoArray;
            } else {
                return new EventFormatterConfigurationInfoDto[0];
            }
        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }


    public EventFormatterConfigurationInfoDto[] getAllStreamSpecificActiveEventFormatterConfiguration (String streamId)
            throws AxisFault {

        try {
            EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

            AxisConfiguration axisConfiguration = getAxisConfig();

            // get event formatter configurations
            List<EventFormatterConfiguration> eventFormatterConfigurationList;
            eventFormatterConfigurationList = eventFormatterService.getAllActiveEventFormatterConfiguration(axisConfiguration,streamId);

            if (eventFormatterConfigurationList != null) {
                // create event formatter configuration details array
                EventFormatterConfigurationInfoDto[] eventFormatterConfigurationInfoDtoArray = new
                        EventFormatterConfigurationInfoDto[eventFormatterConfigurationList.size()];
                for (int index = 0; index < eventFormatterConfigurationInfoDtoArray.length; index++) {
                    EventFormatterConfiguration eventFormatterConfiguration = eventFormatterConfigurationList.get(index);
                    String eventFormatterName = eventFormatterConfiguration.getEventFormatterName();
                    String mappingType = eventFormatterConfiguration.getOutputMapping().getMappingType();
                    String outputEventAdaptorName = eventFormatterConfiguration.getToPropertyConfiguration().getEventAdaptorName();

                    eventFormatterConfigurationInfoDtoArray[index] = new EventFormatterConfigurationInfoDto();
                    eventFormatterConfigurationInfoDtoArray[index].setEventFormatterName(eventFormatterName);
                    eventFormatterConfigurationInfoDtoArray[index].setMappingType(mappingType);
                    eventFormatterConfigurationInfoDtoArray[index].setOutEventAdaptorName(outputEventAdaptorName);
                    eventFormatterConfigurationInfoDtoArray[index].setEnableStats(eventFormatterConfiguration.isEnableStatistics());
                    eventFormatterConfigurationInfoDtoArray[index].setEnableTracing(eventFormatterConfiguration.isEnableTracing());
                }
                return eventFormatterConfigurationInfoDtoArray;
            } else {
                return new EventFormatterConfigurationInfoDto[0];
            }
        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }


    public void undeployActiveEventFormatterConfiguration(String eventFormatterName)
            throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.undeployActiveEventFormatterConfiguration(eventFormatterName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventFormatterConfigurationFileDto[] getAllInactiveEventFormatterConfiguration()
            throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterService.getAllInactiveEventFormatterConfiguration(axisConfiguration);
        if (eventFormatterConfigurationFileList != null) {

            // create event formatter file details array
            EventFormatterConfigurationFileDto[] eventFormatterFileDtoArray = new
                    EventFormatterConfigurationFileDto[eventFormatterConfigurationFileList.size()];

            for (int index = 0; index < eventFormatterFileDtoArray.length; index++) {
                EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterConfigurationFileList.get(index);
                String fileName = eventFormatterConfigurationFile.getFileName();
                String eventFormatterName = eventFormatterConfigurationFile.getEventFormatterName();

                eventFormatterFileDtoArray[index] = new EventFormatterConfigurationFileDto(fileName, eventFormatterName);
            }
            return eventFormatterFileDtoArray;
        } else {
            return new EventFormatterConfigurationFileDto[0];
        }
    }

    public void undeployInactiveEventFormatterConfiguration(String fileName)
            throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            eventFormatterService.undeployInactiveEventFormatterConfiguration(fileName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getInactiveEventFormatterConfigurationContent(String fileName)
            throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        try {
            String eventFormatterConfigurationFile = eventFormatterService.getInactiveEventFormatterConfigurationContent(fileName, getAxisConfig());
            return eventFormatterConfigurationFile.trim();
        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void editInactiveEventFormatterConfiguration(
            String eventFormatterConfiguration,
            String fileName)
            throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.editInactiveEventFormatterConfiguration(eventFormatterConfiguration, fileName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getActiveEventFormatterConfigurationContent(String eventFormatterName)
            throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            return eventFormatterService.getActiveEventFormatterConfigurationContent(eventFormatterName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void editActiveEventFormatterConfiguration(String eventFormatterConfiguration,
                                                      String eventFormatterName)
            throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.editActiveEventFormatterConfiguration(eventFormatterConfiguration, eventFormatterName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    //TODO have to get the streams directly from stream store
    public String[] getAllEventStreamNames() throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            List<String> streamNames = eventFormatterService.getAllEventStreams(axisConfiguration);
            return streamNames.toArray(new String[streamNames.size()]);
        } catch (EventFormatterConfigurationException ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }
    }

    public String getStreamDefinition(String streamNameWithVersion) throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            StreamDefinition streamDefinition = eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration);
            return getStreamAttributes(streamDefinition);
        } catch (EventFormatterConfigurationException ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }

    }

    public OutputEventAdaptorInfoDto[] getOutputEventAdaptorInfo() throws AxisFault {

        OutputEventAdaptorManagerService eventAdaptorManagerService = EventFormatterAdminServiceValueHolder.getOutputEventAdaptorManagerService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            List<OutputEventAdaptorInfo> eventAdaptorInfoList = eventAdaptorManagerService.getOutputEventAdaptorInfo(tenantId);
            if (eventAdaptorInfoList != null) {
                OutputEventAdaptorInfoDto[] eventAdaptorNames = new OutputEventAdaptorInfoDto[eventAdaptorInfoList.size()];
                for (int index = 0; index < eventAdaptorNames.length; index++) {

                    OutputEventAdaptorInfo eventAdaptorInfo = eventAdaptorInfoList.get(index);
                    eventAdaptorNames[index] = new OutputEventAdaptorInfoDto(eventAdaptorInfo.getEventAdaptorName(),
                                                                                     eventAdaptorInfo.getEventAdaptorType());
                }
                return eventAdaptorNames;
            }
            return new OutputEventAdaptorInfoDto[0];
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }

    }

    public String[] getSupportedMappingTypes(String eventAdaptorName) throws AxisFault {

        OutputEventAdaptorManagerService eventAdaptorManagerService = EventFormatterAdminServiceValueHolder.getOutputEventAdaptorManagerService();
        OutputEventAdaptorService eventAdaptorService = EventFormatterAdminServiceValueHolder.getOutputEventAdaptorService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            List<OutputEventAdaptorInfo> eventAdaptorInfoList = eventAdaptorManagerService.getOutputEventAdaptorInfo(tenantId);
            for (OutputEventAdaptorInfo eventAdaptorInfo : eventAdaptorInfoList) {
                if (eventAdaptorInfo.getEventAdaptorName().equals(eventAdaptorName)) {
                    String eventAdaptorType = eventAdaptorInfo.getEventAdaptorType();
                    OutputEventAdaptorDto eventAdaptorDto = eventAdaptorService.getEventAdaptorDto(eventAdaptorType);
                    List<String> supportedOutputMessageTypes = eventAdaptorDto.getSupportedMessageTypes();
                    String[] supportedMappingTypes = new String[supportedOutputMessageTypes.size()];
                    for (int index = 0; index < supportedMappingTypes.length; index++) {
                        String mappingInfo = supportedOutputMessageTypes.get(index);
                        supportedMappingTypes[index] = mappingInfo.toLowerCase();
                    }
                    return supportedMappingTypes;
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }

        return new String[0];

    }

    public EventFormatterPropertyDto[] getEventFormatterProperties(String eventAdaptorName)
            throws AxisFault {

        OutputEventAdaptorService eventAdaptorService = EventFormatterAdminServiceValueHolder.getOutputEventAdaptorService();

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String eventAdaptorType = "";

        try {
            eventAdaptorType = getEventAdaptorType(eventAdaptorName, tenantId);
            MessageDto messageDto = eventAdaptorService.getEventAdaptorMessageDto(eventAdaptorType);

            List<Property> propertyList = messageDto.getMessageOutPropertyList();
            if (propertyList != null) {
                EventFormatterPropertyDto[] eventFormatterPropertyDtoArray = new EventFormatterPropertyDto[propertyList.size()];
                for (int index = 0; index < eventFormatterPropertyDtoArray.length; index++) {
                    Property property = propertyList.get(index);
                    // set event formatter property parameters
                    eventFormatterPropertyDtoArray[index] = new EventFormatterPropertyDto(property.getPropertyName(), "");
                    eventFormatterPropertyDtoArray[index].setRequired(property.isRequired());
                    eventFormatterPropertyDtoArray[index].setSecured(property.isSecured());
                    eventFormatterPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                    eventFormatterPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                    eventFormatterPropertyDtoArray[index].setHint(property.getHint());
                    eventFormatterPropertyDtoArray[index].setOptions(property.getOptions());
                }
                return eventFormatterPropertyDtoArray;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }
        return new EventFormatterPropertyDto[0];
    }

    private String getEventAdaptorType(String eventAdaptorName, int tenantId) {

        OutputEventAdaptorManagerService eventAdaptorManagerService = EventFormatterAdminServiceValueHolder.getOutputEventAdaptorManagerService();

        String eventAdaptorType = "";

        List<OutputEventAdaptorInfo> eventAdaptorInfoList = eventAdaptorManagerService.getOutputEventAdaptorInfo(tenantId);
        for (OutputEventAdaptorInfo eventAdaptorInfo : eventAdaptorInfoList) {
            if (eventAdaptorInfo.getEventAdaptorName().equals(eventAdaptorName)) {
                eventAdaptorType = eventAdaptorInfo.getEventAdaptorType();
                break;
            }
        }
        return eventAdaptorType;
    }

    public void deployWSO2EventFormatterConfiguration(String eventFormatterName,
                                                      String streamNameWithVersion,
                                                      String eventAdaptorName,
                                                      String eventAdaptorType,
                                                      EventOutputPropertyConfigurationDto[] metaData,
                                                      EventOutputPropertyConfigurationDto[] correlationData,
                                                      EventOutputPropertyConfigurationDto[] payloadData,
                                                      EventFormatterPropertyDto[] outputPropertyConfiguration, boolean mappingEnabled)
            throws AxisFault {

        if (checkEventFormatterValidity(eventFormatterName)) {
            try {
                EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

                EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration();

                eventFormatterConfiguration.setEventFormatterName(eventFormatterName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventFormatterConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventFormatterConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                StreamDefinition streamDefinition = eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration);

                ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
                toPropertyConfiguration.setEventAdaptorName(eventAdaptorName);
                toPropertyConfiguration.setEventAdaptorType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration = new OutputEventAdaptorMessageConfiguration();

                    for (EventFormatterPropertyDto eventFormatterProperty : outputPropertyConfiguration) {
                        if (!eventFormatterProperty.getValue().trim().equals("")) {
                            outputEventMessageConfiguration.addOutputMessageProperty(eventFormatterProperty.getKey().trim(), eventFormatterProperty.getValue().trim());
                        }
                    }
                    toPropertyConfiguration.setOutputEventAdaptorMessageConfiguration(outputEventMessageConfiguration);
                }

                eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

                WSO2EventOutputMapping wso2EventOutputMapping = new WSO2EventOutputMapping();
                wso2EventOutputMapping.setCustomMappingEnabled(mappingEnabled);
                List<String> outputEventAttributes = new ArrayList<String>();

                if (metaData != null && metaData.length != 0) {
                    for (EventOutputPropertyConfigurationDto wso2EventOutputPropertyConfiguration : metaData) {
                        EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(getPropertyAttributeDataType(wso2EventOutputPropertyConfiguration.getValueOf(),streamDefinition)));
                        wso2EventOutputMapping.addMetaWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                        outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                    }

                }

                if (correlationData != null && correlationData.length != 0) {
                    for (EventOutputPropertyConfigurationDto wso2EventOutputPropertyConfiguration : correlationData) {
                        EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(getPropertyAttributeDataType(wso2EventOutputPropertyConfiguration.getValueOf(),streamDefinition)));
                        wso2EventOutputMapping.addCorrelationWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                        outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                    }
                }

                if (payloadData != null && payloadData.length != 0) {
                    for (EventOutputPropertyConfigurationDto wso2EventOutputPropertyConfiguration : payloadData) {
                        EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(getPropertyAttributeDataType(wso2EventOutputPropertyConfiguration.getValueOf(),streamDefinition)));
                        wso2EventOutputMapping.addPayloadWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                        outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                    }
                }

                eventFormatterConfiguration.setOutputMapping(wso2EventOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes,streamDefinition)) {
                    eventFormatterService.deployEventFormatterConfiguration(eventFormatterConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventFormatterConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventFormatterName + " is already registered for this tenant");
        }

    }

    public void deployTextEventFormatterConfiguration(String eventFormatterName,
                                                      String streamNameWithVersion,
                                                      String eventAdaptorName,
                                                      String eventAdaptorType,
                                                      String textData,
                                                      EventFormatterPropertyDto[] outputPropertyConfiguration,
                                                      String dataFrom)
            throws AxisFault {

        if (checkEventFormatterValidity(eventFormatterName)) {
            try {
                EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

                EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration();

                eventFormatterConfiguration.setEventFormatterName(eventFormatterName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventFormatterConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventFormatterConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
                toPropertyConfiguration.setEventAdaptorName(eventAdaptorName);
                toPropertyConfiguration.setEventAdaptorType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration = new OutputEventAdaptorMessageConfiguration();

                    for (EventFormatterPropertyDto eventFormatterProperty : outputPropertyConfiguration) {
                        if (!eventFormatterProperty.getValue().trim().equals("")) {
                            outputEventMessageConfiguration.addOutputMessageProperty(eventFormatterProperty.getKey().trim(), eventFormatterProperty.getValue().trim());
                        }
                    }
                    toPropertyConfiguration.setOutputEventAdaptorMessageConfiguration(outputEventMessageConfiguration);
                }

                eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

                TextOutputMapping textOutputMapping = new TextOutputMapping();
                textOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                textOutputMapping.setMappingText(textData);
                textOutputMapping.setCustomMappingEnabled(true);

                if (dataFrom.equalsIgnoreCase("registry")) {
                    textData = eventFormatterService.getRegistryResourceContent(textData, tenantId);
                }
                List<String> outputEventAttributes = getOutputMappingPropertyList(textData);

                eventFormatterConfiguration.setOutputMapping(textOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventFormatterService.deployEventFormatterConfiguration(eventFormatterConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventFormatterConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventFormatterName + " is already registered for this tenant");
        }

    }

    public void deployXmlEventFormatterConfiguration(String eventFormatterName,
                                                     String streamNameWithVersion,
                                                     String eventAdaptorName,
                                                     String eventAdaptorType,
                                                     String textData,
                                                     EventFormatterPropertyDto[] outputPropertyConfiguration,
                                                     String dataFrom)
            throws AxisFault {

        if (checkEventFormatterValidity(eventFormatterName)) {
            try {
                EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

                EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration();

                eventFormatterConfiguration.setEventFormatterName(eventFormatterName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventFormatterConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventFormatterConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
                toPropertyConfiguration.setEventAdaptorName(eventAdaptorName);
                toPropertyConfiguration.setEventAdaptorType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration = new OutputEventAdaptorMessageConfiguration();

                    for (EventFormatterPropertyDto eventFormatterProperty : outputPropertyConfiguration) {
                        if (!eventFormatterProperty.getValue().trim().equals("")) {
                            outputEventMessageConfiguration.addOutputMessageProperty(eventFormatterProperty.getKey().trim(), eventFormatterProperty.getValue().trim());
                        }
                    }
                    toPropertyConfiguration.setOutputEventAdaptorMessageConfiguration(outputEventMessageConfiguration);
                }

                eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

                XMLOutputMapping xmlOutputMapping = new XMLOutputMapping();
                xmlOutputMapping.setMappingXMLText(textData);
                xmlOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                xmlOutputMapping.setCustomMappingEnabled(true);
                List<String> outputEventAttributes = getOutputMappingPropertyList(textData);

                eventFormatterConfiguration.setOutputMapping(xmlOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventFormatterService.deployEventFormatterConfiguration(eventFormatterConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventFormatterConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventFormatterName + " is already registered for this tenant");
        }

    }

    public void deployMapEventFormatterConfiguration(String eventFormatterName,
                                                     String streamNameWithVersion,
                                                     String eventAdaptorName,
                                                     String eventAdaptorType,
                                                     EventOutputPropertyConfigurationDto[] mapData,
                                                     EventFormatterPropertyDto[] outputPropertyConfiguration)
            throws AxisFault {

        if (checkEventFormatterValidity(eventFormatterName)) {
            try {
                EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

                EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration();

                eventFormatterConfiguration.setEventFormatterName(eventFormatterName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventFormatterConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventFormatterConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
                toPropertyConfiguration.setEventAdaptorName(eventAdaptorName);
                toPropertyConfiguration.setEventAdaptorType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration = new OutputEventAdaptorMessageConfiguration();

                    for (EventFormatterPropertyDto eventFormatterProperty : outputPropertyConfiguration) {
                        if (!eventFormatterProperty.getValue().trim().equals("")) {
                            outputEventMessageConfiguration.addOutputMessageProperty(eventFormatterProperty.getKey().trim(), eventFormatterProperty.getValue().trim());
                        }
                    }
                    toPropertyConfiguration.setOutputEventAdaptorMessageConfiguration(outputEventMessageConfiguration);
                }

                eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

                MapOutputMapping mapOutputMapping = new MapOutputMapping();
                mapOutputMapping.setCustomMappingEnabled(true);
                List<String> outputEventAttributes = new ArrayList<String>();

                if (mapData != null && mapData.length != 0) {
                    for (EventOutputPropertyConfigurationDto eventOutputPropertyConfiguration : mapData) {
                        EventOutputProperty eventOutputProperty = new EventOutputProperty(eventOutputPropertyConfiguration.getName(), eventOutputPropertyConfiguration.getValueOf());
                        mapOutputMapping.addOutputPropertyConfiguration(eventOutputProperty);
                        outputEventAttributes.add(eventOutputPropertyConfiguration.getValueOf());
                    }

                }

                eventFormatterConfiguration.setOutputMapping(mapOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventFormatterService.deployEventFormatterConfiguration(eventFormatterConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventFormatterConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventFormatterName + " is already registered for this tenant");
        }

    }

    public void deployJsonEventFormatterConfiguration(String eventFormatterName,
                                                      String streamNameWithVersion,
                                                      String eventAdaptorName,
                                                      String eventAdaptorType,
                                                      String jsonData,
                                                      EventFormatterPropertyDto[] outputPropertyConfiguration,
                                                      String dataFrom)
            throws AxisFault {

        if (checkEventFormatterValidity(eventFormatterName)) {
            try {
                EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

                EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration();

                eventFormatterConfiguration.setEventFormatterName(eventFormatterName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventFormatterConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventFormatterConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
                toPropertyConfiguration.setEventAdaptorName(eventAdaptorName);
                toPropertyConfiguration.setEventAdaptorType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration = new OutputEventAdaptorMessageConfiguration();

                    for (EventFormatterPropertyDto eventFormatterProperty : outputPropertyConfiguration) {
                        if (!eventFormatterProperty.getValue().trim().equals("")) {
                            outputEventMessageConfiguration.addOutputMessageProperty(eventFormatterProperty.getKey().trim(), eventFormatterProperty.getValue().trim());
                        }
                    }
                    toPropertyConfiguration.setOutputEventAdaptorMessageConfiguration(outputEventMessageConfiguration);
                }

                eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

                JSONOutputMapping jsonOutputMapping = new JSONOutputMapping();
                jsonOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                jsonOutputMapping.setMappingText(jsonData);
                jsonOutputMapping.setCustomMappingEnabled(true);
                List<String> outputEventAttributes = getOutputMappingPropertyList(jsonData);

                eventFormatterConfiguration.setOutputMapping(jsonOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventFormatterService.deployEventFormatterConfiguration(eventFormatterConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventFormatterConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventFormatterName + " is already registered for this tenant");
        }

    }

    public EventFormatterConfigurationDto getActiveEventFormatterConfiguration(
            String eventFormatterName) throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        AxisConfiguration axisConfiguration = getAxisConfig();

        try {
            EventFormatterConfiguration eventFormatterConfiguration = eventFormatterService.getActiveEventFormatterConfiguration(eventFormatterName, tenantId);
            if (eventFormatterConfiguration != null) {
                EventFormatterConfigurationDto eventFormatterConfigurationDto = new EventFormatterConfigurationDto();
                eventFormatterConfigurationDto.setEventFormatterName(eventFormatterConfiguration.getEventFormatterName());
                String streamNameWithVersion = eventFormatterConfiguration.getFromStreamName() + ":" + eventFormatterConfiguration.getFromStreamVersion();
                eventFormatterConfigurationDto.setFromStreamNameWithVersion(streamNameWithVersion);
                eventFormatterConfigurationDto.setStreamDefinition(getStreamAttributes(eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration)));

                ToPropertyConfiguration toPropertyConfiguration = eventFormatterConfiguration.getToPropertyConfiguration();
                if (toPropertyConfiguration != null) {
                    ToPropertyConfigurationDto toPropertyConfigurationDto = new ToPropertyConfigurationDto();
                    toPropertyConfigurationDto.setEventAdaptorName(toPropertyConfiguration.getEventAdaptorName());
                    toPropertyConfigurationDto.setEventAdaptorType(toPropertyConfiguration.getEventAdaptorType());
                    OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration = toPropertyConfiguration.getOutputEventAdaptorMessageConfiguration();
                    if (outputEventAdaptorMessageConfiguration != null && outputEventAdaptorMessageConfiguration.getOutputMessageProperties().size() > 0) {
                        EventFormatterPropertyDto[] eventFormatterPropertyDtos = getOutputEventFormatterMessageConfiguration(outputEventAdaptorMessageConfiguration.getOutputMessageProperties(), toPropertyConfiguration.getEventAdaptorType());
                        toPropertyConfigurationDto.setOutputEventAdaptorMessageConfiguration(eventFormatterPropertyDtos);
                    }

                    eventFormatterConfigurationDto.setToPropertyConfigurationDto(toPropertyConfigurationDto);
                }

                if (eventFormatterConfiguration.getOutputMapping().getMappingType().equals(EventFormatterConstants.EF_JSON_MAPPING_TYPE)) {
                    JSONOutputMapping jsonOutputMapping = (JSONOutputMapping) eventFormatterConfiguration.getOutputMapping();
                    JSONOutputMappingDto jsonOutputMappingDto = new JSONOutputMappingDto();
                    jsonOutputMappingDto.setMappingText(jsonOutputMapping.getMappingText());
                    jsonOutputMappingDto.setRegistryResource(jsonOutputMapping.isRegistryResource());
                    eventFormatterConfigurationDto.setJsonOutputMappingDto(jsonOutputMappingDto);
                    eventFormatterConfigurationDto.setMappingType("json");
                } else if (eventFormatterConfiguration.getOutputMapping().getMappingType().equals(EventFormatterConstants.EF_XML_MAPPING_TYPE)) {
                    XMLOutputMapping xmlOutputMapping = (XMLOutputMapping) eventFormatterConfiguration.getOutputMapping();
                    XMLOutputMappingDto xmlOutputMappingDto = new XMLOutputMappingDto();
                    xmlOutputMappingDto.setMappingXMLText(xmlOutputMapping.getMappingXMLText());
                    xmlOutputMappingDto.setRegistryResource(xmlOutputMapping.isRegistryResource());
                    eventFormatterConfigurationDto.setXmlOutputMappingDto(xmlOutputMappingDto);
                    eventFormatterConfigurationDto.setMappingType("xml");
                } else if (eventFormatterConfiguration.getOutputMapping().getMappingType().equals(EventFormatterConstants.EF_TEXT_MAPPING_TYPE)) {
                    TextOutputMapping textOutputMapping = (TextOutputMapping) eventFormatterConfiguration.getOutputMapping();
                    TextOutputMappingDto textOutputMappingDto = new TextOutputMappingDto();
                    textOutputMappingDto.setMappingText(textOutputMapping.getMappingText());
                    textOutputMappingDto.setRegistryResource(textOutputMapping.isRegistryResource());
                    eventFormatterConfigurationDto.setTextOutputMappingDto(textOutputMappingDto);
                    eventFormatterConfigurationDto.setMappingType("text");
                } else if (eventFormatterConfiguration.getOutputMapping().getMappingType().equals(EventFormatterConstants.EF_MAP_MAPPING_TYPE)) {
                    MapOutputMapping mapOutputMapping = (MapOutputMapping) eventFormatterConfiguration.getOutputMapping();
                    MapOutputMappingDto mapOutputMappingDto = new MapOutputMappingDto();
                    List<EventOutputProperty> outputPropertyList = mapOutputMapping.getOutputPropertyConfiguration();
                    if (outputPropertyList != null && outputPropertyList.size() > 0) {
                        EventOutputPropertyDto[] eventOutputPropertyDtos = new EventOutputPropertyDto[outputPropertyList.size()];
                        int index = 0;
                        for (EventOutputProperty eventOutputProperty : outputPropertyList) {
                            eventOutputPropertyDtos[index] = new EventOutputPropertyDto();
                            eventOutputPropertyDtos[index].setName(eventOutputProperty.getName());
                            eventOutputPropertyDtos[index].setValueOf(eventOutputProperty.getValueOf());
                            index++;
                        }
                        mapOutputMappingDto.setOutputPropertyConfiguration(eventOutputPropertyDtos);
                    }

                    eventFormatterConfigurationDto.setMapOutputMappingDto(mapOutputMappingDto);
                    eventFormatterConfigurationDto.setMappingType("map");
                } else if (eventFormatterConfiguration.getOutputMapping().getMappingType().equals(EventFormatterConstants.EF_WSO2EVENT_MAPPING_TYPE)) {
                    WSO2EventOutputMapping wso2EventOutputMapping = (WSO2EventOutputMapping) eventFormatterConfiguration.getOutputMapping();
                    WSO2EventOutputMappingDto wso2EventOutputMappingDto = new WSO2EventOutputMappingDto();
                    List<EventOutputProperty> metaOutputPropertyList = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
                    List<EventOutputProperty> correlationOutputPropertyList = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
                    List<EventOutputProperty> payloadOutputPropertyList = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

                    wso2EventOutputMappingDto.setMetaWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(metaOutputPropertyList));
                    wso2EventOutputMappingDto.setCorrelationWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(correlationOutputPropertyList));
                    wso2EventOutputMappingDto.setPayloadWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(payloadOutputPropertyList));

                    eventFormatterConfigurationDto.setWso2EventOutputMappingDto(wso2EventOutputMappingDto);
                    eventFormatterConfigurationDto.setMappingType("wso2event");
                }

                return eventFormatterConfigurationDto;
            }

        } catch (EventFormatterConfigurationException ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }
        return null;
    }

    public void setStatisticsEnabled(String eventFormatterName, boolean flag) throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.setStatisticsEnabled(eventFormatterName, axisConfiguration, flag);
        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void setTracingEnabled(String eventFormatterName, boolean flag) throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.setTraceEnabled(eventFormatterName, axisConfiguration, flag);
        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    public String getEventFormatterStatusAsString(String filename) {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        return eventFormatterService.getEventFormatterStatusAsString(filename);
    }

    private EventOutputPropertyDto[] getEventPropertyDtoArray(
            List<EventOutputProperty> eventOutputPropertyList) {

        if (eventOutputPropertyList != null && eventOutputPropertyList.size() > 0) {
            EventOutputPropertyDto[] eventOutputPropertyDtos = new EventOutputPropertyDto[eventOutputPropertyList.size()];
            int index = 0;
            Iterator<EventOutputProperty> outputPropertyIterator = eventOutputPropertyList.iterator();
            while (outputPropertyIterator.hasNext()) {
                EventOutputProperty eventOutputProperty = outputPropertyIterator.next();
                eventOutputPropertyDtos[index] = new EventOutputPropertyDto(eventOutputProperty.getName(), eventOutputProperty.getValueOf(), eventOutputProperty.getType().toString().toLowerCase());
                index++;
            }

            return eventOutputPropertyDtos;
        }
        return null;
    }

    private EventFormatterPropertyDto[] getOutputEventFormatterMessageConfiguration(
            Map<String, String> messageProperties, String eventAdaptorType) {

        OutputEventAdaptorService outputEventAdaptorService = EventFormatterAdminServiceValueHolder.getOutputEventAdaptorService();
        List<Property> outputMessagePropertyList = outputEventAdaptorService.getEventAdaptorMessageDto(eventAdaptorType).getMessageOutPropertyList();
        if (outputMessagePropertyList != null) {
            EventFormatterPropertyDto[] eventFormatterPropertyDtoArray = new EventFormatterPropertyDto[outputMessagePropertyList.size()];
            int index = 0;
            for (Property property : outputMessagePropertyList) {
                // create output event property
                eventFormatterPropertyDtoArray[index] = new EventFormatterPropertyDto(property.getPropertyName(),
                                                                                      messageProperties.get(property.getPropertyName()));
                // set output event property parameters
                eventFormatterPropertyDtoArray[index].setSecured(property.isSecured());
                eventFormatterPropertyDtoArray[index].setRequired(property.isRequired());
                eventFormatterPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                eventFormatterPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                eventFormatterPropertyDtoArray[index].setHint(property.getHint());
                eventFormatterPropertyDtoArray[index].setOptions(property.getOptions());

                index++;
            }
            return eventFormatterPropertyDtoArray;
        }
        return new EventFormatterPropertyDto[0];
    }

    private boolean checkStreamAttributeValidity(List<String> outputEventAttributes,
                                                 StreamDefinition streamDefinition) {

        if (streamDefinition != null) {
            List<String> inComingStreamAttributes = new ArrayList<String>();
            final String PROPERTY_META_PREFIX = "meta_";
            final String PROPERTY_CORRELATION_PREFIX = "correlation_";

            List<Attribute> metaAttributeList = streamDefinition.getMetaData();
            List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
            List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();


            if (metaAttributeList != null) {
                for (Attribute attribute : metaAttributeList) {
                    inComingStreamAttributes.add(PROPERTY_META_PREFIX + attribute.getName());
                }
            }
            if (correlationAttributeList != null) {
                for (Attribute attribute : correlationAttributeList) {
                    inComingStreamAttributes.add(PROPERTY_CORRELATION_PREFIX + attribute.getName());
                }
            }
            if (payloadAttributeList != null) {
                for (Attribute attribute : payloadAttributeList) {
                    inComingStreamAttributes.add(attribute.getName());
                }
            }


            if (outputEventAttributes.size() > 0) {
                if (inComingStreamAttributes.containsAll(outputEventAttributes)) {
                    return true;
                } else {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }

    }

    private String getStreamAttributes(StreamDefinition streamDefinition) {
        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();

        String attributes = "";

        if (metaAttributeList != null) {
            for (Attribute attribute : metaAttributeList) {
                attributes += PropertyAttributeTypeConstants.PROPERTY_META_PREFIX + attribute.getName() + " " + attribute.getType().toString().toLowerCase() + ", \n";
            }
        }
        if (correlationAttributeList != null) {
            for (Attribute attribute : correlationAttributeList) {
                attributes += PropertyAttributeTypeConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName() + " " + attribute.getType().toString().toLowerCase() + ", \n";
            }
        }
        if (payloadAttributeList != null) {
            for (Attribute attribute : payloadAttributeList) {
                attributes += attribute.getName() + " " + attribute.getType().toString().toLowerCase() + ", \n";
            }
        }

        if (!attributes.equals("")) {
            return attributes.substring(0, attributes.lastIndexOf(","));
        } else {
            return attributes;
        }
    }

    private List<String> getOutputMappingPropertyList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        mappingTextList.clear();
        while (text.contains("{{") && text.indexOf("}}") > 0) {
            mappingTextList.add(text.substring(text.indexOf("{{") + 2, text.indexOf("}}")));
            text = text.substring(text.indexOf("}}") + 2);
        }
        return mappingTextList;
    }

    private boolean checkEventFormatterValidity(String eventFormatterName) throws AxisFault {
        try {
            EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
            AxisConfiguration axisConfiguration = getAxisConfig();

            List<EventFormatterConfiguration> eventFormatterConfigurationList = null;
            eventFormatterConfigurationList = eventFormatterService.getAllActiveEventFormatterConfiguration(axisConfiguration);
            Iterator eventFormatterConfigurationIterator = eventFormatterConfigurationList.iterator();
            while (eventFormatterConfigurationIterator.hasNext()) {

                EventFormatterConfiguration eventFormatterConfiguration = (EventFormatterConfiguration) eventFormatterConfigurationIterator.next();
                if (eventFormatterConfiguration.getEventFormatterName().equalsIgnoreCase(eventFormatterName)) {
                    return false;
                }
            }

        } catch (EventFormatterConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    private boolean validateRegistrySource(String fromData) {

        return !fromData.equalsIgnoreCase("inline");
    }

    private String getPropertyAttributeDataType(String propertyName, StreamDefinition streamDefinition) throws AxisFault{

        if(propertyName != null){
            List<Attribute> metaDataList = streamDefinition.getMetaData();
            if(metaDataList != null){
                for (Attribute attribute : metaDataList){
                    if(propertyName.equalsIgnoreCase(PropertyAttributeTypeConstants.PROPERTY_META_PREFIX+attribute.getName())){
                        return attribute.getType().toString().toLowerCase();
                    }
                }
            }

            List<Attribute> correlationDataList = streamDefinition.getCorrelationData();
            if(correlationDataList != null){
                for (Attribute attribute : correlationDataList){
                    if(propertyName.equalsIgnoreCase(PropertyAttributeTypeConstants.PROPERTY_CORRELATION_PREFIX+attribute.getName())){
                        return attribute.getType().toString().toLowerCase();
                    }
                }
            }

            List<Attribute> payloadDataList = streamDefinition.getPayloadData();
            if(payloadDataList != null){
                for (Attribute attribute : payloadDataList){
                    if(propertyName.equalsIgnoreCase(attribute.getName())){
                        return attribute.getType().toString().toLowerCase();
                    }
                }
            }
        }

        throw new AxisFault("Output Stream attributes are not matching with input stream definition");

    }


}