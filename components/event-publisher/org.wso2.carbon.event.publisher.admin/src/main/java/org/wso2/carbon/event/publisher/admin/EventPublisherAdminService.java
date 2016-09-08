/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.publisher.admin;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterSchema;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.publisher.admin.internal.PropertyAttributeTypeConstants;
import org.wso2.carbon.event.publisher.admin.internal.ds.EventPublisherAdminServiceValueHolder;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.core.config.EventOutputProperty;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfigurationFile;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.mapping.*;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;

import java.util.*;

public class EventPublisherAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventPublisherAdminService.class);

    public EventPublisherConfigurationInfoDto[] getAllActiveEventPublisherConfigurations()
            throws AxisFault {

        try {
            EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

            // get event publisher configurations
            List<EventPublisherConfiguration> eventPublisherConfigurationList;
            eventPublisherConfigurationList = eventPublisherService.getAllActiveEventPublisherConfigurations();

            if (eventPublisherConfigurationList != null) {
                // create event publisher configuration details array
                EventPublisherConfigurationInfoDto[] eventPublisherConfigurationInfoDtoArray = new
                        EventPublisherConfigurationInfoDto[eventPublisherConfigurationList.size()];
                for (int index = 0; index < eventPublisherConfigurationInfoDtoArray.length; index++) {
                    EventPublisherConfiguration eventPublisherConfiguration = eventPublisherConfigurationList.get(index);
                    String eventPublisherName = eventPublisherConfiguration.getEventPublisherName();
                    String mappingType = eventPublisherConfiguration.getOutputMapping().getMappingType();
                    String outputEventAdapterType = eventPublisherConfiguration.getToAdapterConfiguration().getType();
                    String streamNameWithVersion = eventPublisherConfiguration.getFromStreamName() + ":" + eventPublisherConfiguration.getFromStreamVersion();


                    eventPublisherConfigurationInfoDtoArray[index] = new EventPublisherConfigurationInfoDto();
                    eventPublisherConfigurationInfoDtoArray[index].setEventPublisherName(eventPublisherName);
                    eventPublisherConfigurationInfoDtoArray[index].setMessageFormat(mappingType);
                    eventPublisherConfigurationInfoDtoArray[index].setOutputAdapterType(outputEventAdapterType);
                    eventPublisherConfigurationInfoDtoArray[index].setInputStreamId(streamNameWithVersion);
                    eventPublisherConfigurationInfoDtoArray[index].setEnableStats(eventPublisherConfiguration.isStatisticsEnabled());
                    eventPublisherConfigurationInfoDtoArray[index].setEnableTracing(eventPublisherConfiguration.isTracingEnabled());
                    eventPublisherConfigurationInfoDtoArray[index].setEditable(eventPublisherConfiguration.isEditable());
                    eventPublisherConfigurationInfoDtoArray[index].setEnableProcessing(eventPublisherConfiguration.isProcessingEnabled());

                }
                Arrays.sort(eventPublisherConfigurationInfoDtoArray, new Comparator() {

                    @Override
                    public int compare(Object o1, Object o2) {
                        return ((EventPublisherConfigurationInfoDto) o1).getEventPublisherName().compareTo(((EventPublisherConfigurationInfoDto) o2).getEventPublisherName());
                    }
                });
                return eventPublisherConfigurationInfoDtoArray;

            } else {
                return new EventPublisherConfigurationInfoDto[0];
            }
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventPublisherConfigurationInfoDto[] getAllStreamSpecificActiveEventPublisherConfigurations(
            String streamId)
            throws AxisFault {

        try {
            EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

            // get event publisher configurations
            List<EventPublisherConfiguration> eventPublisherConfigurationList;
            eventPublisherConfigurationList = eventPublisherService.getAllActiveEventPublisherConfigurations(streamId);

            if (eventPublisherConfigurationList != null) {
                // create event publisher configuration details array
                EventPublisherConfigurationInfoDto[] eventPublisherConfigurationInfoDtoArray = new
                        EventPublisherConfigurationInfoDto[eventPublisherConfigurationList.size()];
                for (int index = 0; index < eventPublisherConfigurationInfoDtoArray.length; index++) {
                    EventPublisherConfiguration eventPublisherConfiguration = eventPublisherConfigurationList.get(index);
                    String eventPublisherName = eventPublisherConfiguration.getEventPublisherName();
                    String mappingType = eventPublisherConfiguration.getOutputMapping().getMappingType();
                    String outputEventAdapterType = eventPublisherConfiguration.getToAdapterConfiguration().getType();

                    eventPublisherConfigurationInfoDtoArray[index] = new EventPublisherConfigurationInfoDto();
                    eventPublisherConfigurationInfoDtoArray[index].setEventPublisherName(eventPublisherName);
                    eventPublisherConfigurationInfoDtoArray[index].setMessageFormat(mappingType);
                    eventPublisherConfigurationInfoDtoArray[index].setOutputAdapterType(outputEventAdapterType);
                    eventPublisherConfigurationInfoDtoArray[index].setEnableStats(eventPublisherConfiguration.isStatisticsEnabled());
                    eventPublisherConfigurationInfoDtoArray[index].setEnableTracing(eventPublisherConfiguration.isTracingEnabled());
                    eventPublisherConfigurationInfoDtoArray[index].setEditable(eventPublisherConfiguration.isEditable());
                    eventPublisherConfigurationInfoDtoArray[index].setEnableProcessing(eventPublisherConfiguration.isProcessingEnabled());

                }
                Arrays.sort(eventPublisherConfigurationInfoDtoArray, new Comparator() {

                    @Override
                    public int compare(Object o1, Object o2) {
                        return ((EventPublisherConfigurationInfoDto) o1).getEventPublisherName().compareTo(((EventPublisherConfigurationInfoDto) o2).getEventPublisherName());
                    }
                });
                return eventPublisherConfigurationInfoDtoArray;
            } else {
                return new EventPublisherConfigurationInfoDto[0];
            }
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventPublisherConfigurationFileDto[] getAllInactiveEventPublisherConfigurations()
            throws AxisFault {

        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        List<EventPublisherConfigurationFile> eventPublisherConfigurationFileList = eventPublisherService.getAllInactiveEventPublisherConfigurations();
        if (eventPublisherConfigurationFileList != null) {

            // create event publisher file details array
            EventPublisherConfigurationFileDto[] eventPublisherFileDtoArray = new
                    EventPublisherConfigurationFileDto[eventPublisherConfigurationFileList.size()];

            for (int index = 0; index < eventPublisherFileDtoArray.length; index++) {
                EventPublisherConfigurationFile eventPublisherConfigurationFile = eventPublisherConfigurationFileList.get(index);
                String fileName = eventPublisherConfigurationFile.getFileName();
                String eventPublisherName = eventPublisherConfigurationFile.getEventPublisherName();
                String statusMsg = eventPublisherConfigurationFile.getDeploymentStatusMessage();
                if (eventPublisherConfigurationFile.getDependency() != null) {
                    statusMsg = statusMsg + " [Dependency: " + eventPublisherConfigurationFile.getDependency() + "]";
                }

                eventPublisherFileDtoArray[index] = new EventPublisherConfigurationFileDto(fileName, eventPublisherName, statusMsg);
            }
            Arrays.sort(eventPublisherFileDtoArray, new Comparator() {

                @Override
                public int compare(Object o1, Object o2) {
                    return ((EventPublisherConfigurationFileDto) o1).getFileName().compareTo(((EventPublisherConfigurationFileDto) o2).getFileName());
                }
            });
            return eventPublisherFileDtoArray;
        } else {
            return new EventPublisherConfigurationFileDto[0];
        }
    }

    public EventPublisherConfigurationDto getActiveEventPublisherConfiguration(
            String eventPublisherName) throws AxisFault {

        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

        try {
            EventPublisherConfiguration eventPublisherConfiguration = eventPublisherService.getActiveEventPublisherConfiguration(eventPublisherName);
            if (eventPublisherConfiguration != null) {
                EventPublisherConfigurationDto eventPublisherConfigurationDto = new EventPublisherConfigurationDto();
                eventPublisherConfigurationDto.setEventPublisherName(eventPublisherConfiguration.getEventPublisherName());
                String streamNameWithVersion = eventPublisherConfiguration.getFromStreamName() + ":" + eventPublisherConfiguration.getFromStreamVersion();
                eventPublisherConfigurationDto.setFromStreamNameWithVersion(streamNameWithVersion);
                eventPublisherConfigurationDto.setStreamDefinition(getStreamAttributes(eventPublisherService.getStreamDefinition(streamNameWithVersion)));

                OutputEventAdapterConfiguration toAdapterConfiguration = eventPublisherConfiguration.getToAdapterConfiguration();

                if (toAdapterConfiguration != null) {
                    OutputEventAdapterService outputEventAdapterService = EventPublisherAdminServiceValueHolder.getOutputEventAdapterService();
                    OutputEventAdapterSchema outputEventAdapterSchema = outputEventAdapterService.getOutputEventAdapterSchema(toAdapterConfiguration.getType());

                    OutputAdapterConfigurationDto toAdapterConfigurationDto = new OutputAdapterConfigurationDto();
                    toAdapterConfigurationDto.setEventAdapterType(toAdapterConfiguration.getType());
                    toAdapterConfigurationDto.setSupportedMessageFormats(
                            outputEventAdapterSchema.getSupportedMessageFormats().
                                    toArray(new String[outputEventAdapterSchema.getSupportedMessageFormats().size()]));
                    toAdapterConfigurationDto.setUsageTips(outputEventAdapterSchema.getUsageTips());

                    Map<String, String> outputAdapterProperties = new HashMap<String, String>();
                    outputAdapterProperties.putAll(toAdapterConfiguration.getStaticProperties());
                    outputAdapterProperties.putAll(eventPublisherConfiguration.getToAdapterDynamicProperties());

                    DetailOutputAdapterPropertyDto[] detailOutputAdapterStaticPropertyDtos = getPropertyConfigurations(outputAdapterProperties, outputEventAdapterSchema.getStaticPropertyList());
                    DetailOutputAdapterPropertyDto[] detailOutputAdapterDynamicPropertyDtos = getPropertyConfigurations(outputAdapterProperties, outputEventAdapterSchema.getDynamicPropertyList());
                    toAdapterConfigurationDto.setOutputEventAdapterStaticProperties(detailOutputAdapterStaticPropertyDtos);
                    toAdapterConfigurationDto.setOutputEventAdapterDynamicProperties(detailOutputAdapterDynamicPropertyDtos);

                    eventPublisherConfigurationDto.setToAdapterConfigurationDto(toAdapterConfigurationDto);
                }

                if (eventPublisherConfiguration.getOutputMapping().getMappingType().equals(EventPublisherConstants.EF_JSON_MAPPING_TYPE)) {
                    JSONOutputMapping jsonOutputMapping = (JSONOutputMapping) eventPublisherConfiguration.getOutputMapping();
                    JSONOutputMappingDto jsonOutputMappingDto = new JSONOutputMappingDto();
                    jsonOutputMappingDto.setMappingText(jsonOutputMapping.getMappingText());
                    jsonOutputMappingDto.setRegistryResource(jsonOutputMapping.isRegistryResource());
                    jsonOutputMappingDto.setCacheTimeoutDuration(jsonOutputMapping.getCacheTimeoutDuration());
                    eventPublisherConfigurationDto.setJsonOutputMappingDto(jsonOutputMappingDto);
                    eventPublisherConfigurationDto.setCustomMappingEnabled(jsonOutputMapping.isCustomMappingEnabled());
                    eventPublisherConfigurationDto.setMessageFormat(EventPublisherConstants.EF_JSON_MAPPING_TYPE);
                } else if (eventPublisherConfiguration.getOutputMapping().getMappingType().equals(EventPublisherConstants.EF_XML_MAPPING_TYPE)) {
                    XMLOutputMapping xmlOutputMapping = (XMLOutputMapping) eventPublisherConfiguration.getOutputMapping();
                    XMLOutputMappingDto xmlOutputMappingDto = new XMLOutputMappingDto();
                    xmlOutputMappingDto.setMappingXMLText(xmlOutputMapping.getMappingXMLText());
                    xmlOutputMappingDto.setRegistryResource(xmlOutputMapping.isRegistryResource());
                    xmlOutputMappingDto.setCacheTimeoutDuration(xmlOutputMapping.getCacheTimeoutDuration());
                    eventPublisherConfigurationDto.setCustomMappingEnabled(xmlOutputMapping.isCustomMappingEnabled());
                    eventPublisherConfigurationDto.setXmlOutputMappingDto(xmlOutputMappingDto);
                    eventPublisherConfigurationDto.setMessageFormat(EventPublisherConstants.EF_XML_MAPPING_TYPE);
                } else if (eventPublisherConfiguration.getOutputMapping().getMappingType().equals(EventPublisherConstants.EF_TEXT_MAPPING_TYPE)) {
                    TextOutputMapping textOutputMapping = (TextOutputMapping) eventPublisherConfiguration.getOutputMapping();
                    TextOutputMappingDto textOutputMappingDto = new TextOutputMappingDto();
                    textOutputMappingDto.setMappingText(textOutputMapping.getMappingText());
                    textOutputMappingDto.setRegistryResource(textOutputMapping.isRegistryResource());
                    textOutputMappingDto.setCacheTimeoutDuration(textOutputMapping.getCacheTimeoutDuration());
                    eventPublisherConfigurationDto.setTextOutputMappingDto(textOutputMappingDto);
                    eventPublisherConfigurationDto.setCustomMappingEnabled(textOutputMapping.isCustomMappingEnabled());
                    eventPublisherConfigurationDto.setMessageFormat(EventPublisherConstants.EF_TEXT_MAPPING_TYPE);
                } else if (eventPublisherConfiguration.getOutputMapping().getMappingType().equals(EventPublisherConstants.EF_MAP_MAPPING_TYPE)) {
                    MapOutputMapping mapOutputMapping = (MapOutputMapping) eventPublisherConfiguration.getOutputMapping();
                    MapOutputMappingDto mapOutputMappingDto = new MapOutputMappingDto();
                    List<EventOutputProperty> outputPropertyList = mapOutputMapping.getOutputPropertyConfiguration();
                    if (outputPropertyList != null && outputPropertyList.size() > 0) {
                        EventMappingPropertyDto[] eventMappingPropertyDtos = new EventMappingPropertyDto[outputPropertyList.size()];
                        int index = 0;
                        for (EventOutputProperty eventOutputProperty : outputPropertyList) {
                            eventMappingPropertyDtos[index] = new EventMappingPropertyDto();
                            eventMappingPropertyDtos[index].setName(eventOutputProperty.getName());
                            eventMappingPropertyDtos[index].setValueOf(eventOutputProperty.getValueOf());
                            index++;
                        }
                        mapOutputMappingDto.setEventMappingProperties(eventMappingPropertyDtos);
                    }

                    eventPublisherConfigurationDto.setMapOutputMappingDto(mapOutputMappingDto);
                    eventPublisherConfigurationDto.setCustomMappingEnabled(mapOutputMapping.isCustomMappingEnabled());
                    eventPublisherConfigurationDto.setMessageFormat(EventPublisherConstants.EF_MAP_MAPPING_TYPE);
                } else if (eventPublisherConfiguration.getOutputMapping().getMappingType().equals(EventPublisherConstants.EF_WSO2EVENT_MAPPING_TYPE)) {
                    WSO2EventOutputMapping wso2EventOutputMapping = (WSO2EventOutputMapping) eventPublisherConfiguration.getOutputMapping();
                    WSO2EventOutputMappingDto wso2EventOutputMappingDto = new WSO2EventOutputMappingDto();
                    List<EventOutputProperty> metaOutputPropertyList = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
                    List<EventOutputProperty> correlationOutputPropertyList = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
                    List<EventOutputProperty> payloadOutputPropertyList = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

                    wso2EventOutputMappingDto.setMetaWSO2EventMappingProperties(getEventPropertyDtoArray(metaOutputPropertyList));
                    wso2EventOutputMappingDto.setCorrelationWSO2EventMappingProperties(getEventPropertyDtoArray(correlationOutputPropertyList));
                    wso2EventOutputMappingDto.setPayloadWSO2EventMappingProperties(getEventPropertyDtoArray(payloadOutputPropertyList));
                    wso2EventOutputMappingDto.setOutputStreamName(wso2EventOutputMapping.getToEventName());
                    wso2EventOutputMappingDto.setOutputStreamVersion(wso2EventOutputMapping.getToEventVersion());

                    eventPublisherConfigurationDto.setCustomMappingEnabled(wso2EventOutputMapping.isCustomMappingEnabled());
                    eventPublisherConfigurationDto.setWso2EventOutputMappingDto(wso2EventOutputMappingDto);
                    eventPublisherConfigurationDto.setMessageFormat(EventPublisherConstants.EF_WSO2EVENT_MAPPING_TYPE);
                }

                return eventPublisherConfigurationDto;
            }

        } catch (EventPublisherConfigurationException ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }
        return null;
    }

    public String getActiveEventPublisherConfigurationContent(String eventPublisherName)
            throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            return eventPublisherService.getActiveEventPublisherConfigurationContent(eventPublisherName);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getInactiveEventPublisherConfigurationContent(String fileName)
            throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            String eventPublisherConfigurationFile = eventPublisherService.getInactiveEventPublisherConfigurationContent(fileName);
            return eventPublisherConfigurationFile.trim();
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public boolean undeployActiveEventPublisherConfiguration(String eventPublisherName)
            throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            eventPublisherService.undeployActiveEventPublisherConfiguration(eventPublisherName);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean undeployInactiveEventPublisherConfiguration(String fileName)
            throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            eventPublisherService.undeployInactiveEventPublisherConfiguration(fileName);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean editActiveEventPublisherConfiguration(String eventPublisherConfiguration,
                                                         String eventPublisherName)
            throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            eventPublisherService.editActiveEventPublisherConfiguration(eventPublisherConfiguration, eventPublisherName);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean editInactiveEventPublisherConfiguration(
            String eventPublisherConfiguration,
            String fileName)
            throws AxisFault {

        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            eventPublisherService.editInactiveEventPublisherConfiguration(eventPublisherConfiguration, fileName);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean deployEventPublisherConfiguration(String eventPublisherConfigXml)
            throws AxisFault {
        try {
            EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
            eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfigXml);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean deployWSO2EventPublisherConfiguration(String eventPublisherName,
                                                         String streamNameWithVersion,
                                                         String eventAdapterType,
                                                         EventMappingPropertyDto[] metaData,
                                                         EventMappingPropertyDto[] correlationData,
                                                         EventMappingPropertyDto[] payloadData,
                                                         BasicOutputAdapterPropertyDto[] outputPropertyConfiguration,
                                                         boolean mappingEnabled,
                                                         String toStreamNameWithVersion)
            throws AxisFault {

        if (checkEventPublisherValidity(eventPublisherName)) {
            try {
                EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

                EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();
                eventPublisherConfiguration.setProcessEnabled(true);
                eventPublisherConfiguration.setEventPublisherName(eventPublisherName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventPublisherConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventPublisherConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                StreamDefinition streamDefinition = eventPublisherService.getStreamDefinition(streamNameWithVersion);

                constructOutputAdapterRelatedConfigs(eventPublisherName, eventAdapterType, outputPropertyConfiguration,
                        eventPublisherConfiguration, EventPublisherConstants.EF_WSO2EVENT_MAPPING_TYPE);

                WSO2EventOutputMapping wso2EventOutputMapping = new WSO2EventOutputMapping();
                wso2EventOutputMapping.setCustomMappingEnabled(mappingEnabled);

                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    if (metaData != null && metaData.length != 0) {
                        for (EventMappingPropertyDto wso2EventOutputPropertyConfiguration : metaData) {
                            EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(getPropertyAttributeDataType(wso2EventOutputPropertyConfiguration.getValueOf(), streamDefinition)));
                            wso2EventOutputMapping.addMetaWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                            outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                        }

                    }

                    if (correlationData != null && correlationData.length != 0) {
                        for (EventMappingPropertyDto wso2EventOutputPropertyConfiguration : correlationData) {
                            EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(getPropertyAttributeDataType(wso2EventOutputPropertyConfiguration.getValueOf(), streamDefinition)));
                            wso2EventOutputMapping.addCorrelationWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                            outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                        }
                    }

                    if (payloadData != null && payloadData.length != 0) {
                        for (EventMappingPropertyDto wso2EventOutputPropertyConfiguration : payloadData) {
                            EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(getPropertyAttributeDataType(wso2EventOutputPropertyConfiguration.getValueOf(), streamDefinition)));
                            wso2EventOutputMapping.addPayloadWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                            outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                        }
                    }
                    String[] toStreamProperties = toStreamNameWithVersion.split(":");
                    wso2EventOutputMapping.setToEventName(toStreamProperties[0]);
                    wso2EventOutputMapping.setToEventVersion(toStreamProperties[1]);

                }

                eventPublisherConfiguration.setOutputMapping(wso2EventOutputMapping);
                eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfiguration);

            } catch (EventPublisherConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventPublisherName + " is already registered for this tenant");
        }
        return true;
    }

    // Deprecated method
    public boolean deployTextEventPublisherConfiguration(String eventPublisherName,
                                                         String streamNameWithVersion,
                                                         String eventAdapterType,
                                                         String textData,
                                                         BasicOutputAdapterPropertyDto[] outputPropertyConfiguration,
                                                         String dataFrom, boolean mappingEnabled)
            throws AxisFault {
        return deployCacheableTextEventPublisherConfiguration(eventPublisherName, streamNameWithVersion, eventAdapterType,
                textData, outputPropertyConfiguration, dataFrom, PropertyAttributeTypeConstants.DEFAULT_REGISTRY_RESOURCE_CACHE_TIMEOUT, mappingEnabled);
    }

    public boolean deployCacheableTextEventPublisherConfiguration(String eventPublisherName,
                                                                  String streamNameWithVersion,
                                                                  String eventAdapterType,
                                                                  String textData,
                                                                  BasicOutputAdapterPropertyDto[] outputPropertyConfiguration,
                                                                  String dataFrom, long cacheTimeoutDuration, boolean mappingEnabled)
            throws AxisFault {

        if (checkEventPublisherValidity(eventPublisherName)) {
            try {
                EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

                EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();
                eventPublisherConfiguration.setProcessEnabled(true);
                eventPublisherConfiguration.setEventPublisherName(eventPublisherName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventPublisherConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventPublisherConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                constructOutputAdapterRelatedConfigs(eventPublisherName, eventAdapterType, outputPropertyConfiguration,
                        eventPublisherConfiguration, EventPublisherConstants.EF_TEXT_MAPPING_TYPE);


                TextOutputMapping textOutputMapping = new TextOutputMapping();
                textOutputMapping.setCustomMappingEnabled(mappingEnabled);

                textOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                textOutputMapping.setMappingText(textData);
                textOutputMapping.setCacheTimeoutDuration(cacheTimeoutDuration);

                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    if (dataFrom.equalsIgnoreCase("registry") && !textData.contains(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) || textData.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) < 0) {
                        textData = eventPublisherService.getRegistryResourceContent(textData);
                    }
                    outputEventAttributes = getOutputMappingPropertyList(textData);
                }
                eventPublisherConfiguration.setOutputMapping(textOutputMapping);
                eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfiguration);

            } catch (EventPublisherConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventPublisherName + " is already registered for this tenant");
        }
        return true;
    }

    // Deprecated method
    public boolean deployXmlEventPublisherConfiguration(String eventPublisherName,
                                                        String streamNameWithVersion,
                                                        String eventAdapterType,
                                                        String textData,
                                                        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration,
                                                        String dataFrom, boolean mappingEnabled)
            throws AxisFault {
        return deployCacheableXmlEventPublisherConfiguration(eventPublisherName, streamNameWithVersion, eventAdapterType,
                textData, outputPropertyConfiguration, dataFrom, PropertyAttributeTypeConstants.DEFAULT_REGISTRY_RESOURCE_CACHE_TIMEOUT, mappingEnabled);
    }

    public boolean deployCacheableXmlEventPublisherConfiguration(String eventPublisherName,
                                                                 String streamNameWithVersion,
                                                                 String eventAdapterType,
                                                                 String textData,
                                                                 BasicOutputAdapterPropertyDto[] outputPropertyConfiguration,
                                                                 String dataFrom, long cacheTimeoutDuration, boolean mappingEnabled)
            throws AxisFault {

        if (checkEventPublisherValidity(eventPublisherName)) {
            try {
                EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

                EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();
                eventPublisherConfiguration.setProcessEnabled(true);

                eventPublisherConfiguration.setEventPublisherName(eventPublisherName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventPublisherConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventPublisherConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                constructOutputAdapterRelatedConfigs(eventPublisherName, eventAdapterType, outputPropertyConfiguration,
                        eventPublisherConfiguration, EventPublisherConstants.EF_XML_MAPPING_TYPE);

                XMLOutputMapping xmlOutputMapping = new XMLOutputMapping();
                xmlOutputMapping.setCustomMappingEnabled(mappingEnabled);
                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    xmlOutputMapping.setMappingXMLText(textData);
                    xmlOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                    xmlOutputMapping.setCacheTimeoutDuration(cacheTimeoutDuration);
                    outputEventAttributes = getOutputMappingPropertyList(textData);
                }

                eventPublisherConfiguration.setOutputMapping(xmlOutputMapping);
                eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfiguration);

            } catch (EventPublisherConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventPublisherName + " is already registered for this tenant");
        }
        return true;
    }

    public boolean deployMapEventPublisherConfiguration(String eventPublisherName,
                                                        String streamNameWithVersion,
                                                        String eventAdapterType,
                                                        EventMappingPropertyDto[] mapData,
                                                        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration,
                                                        boolean mappingEnabled)
            throws AxisFault {

        if (checkEventPublisherValidity(eventPublisherName)) {
            try {
                EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

                EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();
                eventPublisherConfiguration.setProcessEnabled(true);
                eventPublisherConfiguration.setEventPublisherName(eventPublisherName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventPublisherConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventPublisherConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                constructOutputAdapterRelatedConfigs(eventPublisherName, eventAdapterType, outputPropertyConfiguration,
                        eventPublisherConfiguration, EventPublisherConstants.EF_MAP_MAPPING_TYPE);


                MapOutputMapping mapOutputMapping = new MapOutputMapping();
                mapOutputMapping.setCustomMappingEnabled(mappingEnabled);
                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    if (mapData != null && mapData.length != 0) {
                        for (EventMappingPropertyDto eventOutputPropertyConfiguration : mapData) {
                            EventOutputProperty eventOutputProperty = new EventOutputProperty(eventOutputPropertyConfiguration.getName(), eventOutputPropertyConfiguration.getValueOf());
                            mapOutputMapping.addOutputPropertyConfiguration(eventOutputProperty);
                            outputEventAttributes.add(eventOutputPropertyConfiguration.getValueOf());
                        }

                    }
                }

                eventPublisherConfiguration.setOutputMapping(mapOutputMapping);
                eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfiguration);

            } catch (EventPublisherConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventPublisherName + " is already registered for this tenant");
        }
        return true;
    }

    // Deprecated method
    public boolean deployJsonEventPublisherConfiguration(String eventPublisherName,
                                                         String streamNameWithVersion,
                                                         String eventAdapterType,
                                                         String textData,
                                                         BasicOutputAdapterPropertyDto[] outputPropertyConfiguration,
                                                         String dataFrom, boolean mappingEnabled)
            throws AxisFault {
        return deployCacheableJsonEventPublisherConfiguration(eventPublisherName, streamNameWithVersion, eventAdapterType,
                textData, outputPropertyConfiguration, dataFrom, PropertyAttributeTypeConstants.DEFAULT_REGISTRY_RESOURCE_CACHE_TIMEOUT, mappingEnabled);
    }

    public boolean deployCacheableJsonEventPublisherConfiguration(String eventPublisherName,
                                                                  String streamNameWithVersion,
                                                                  String eventAdapterType,
                                                                  String jsonData,
                                                                  BasicOutputAdapterPropertyDto[] outputPropertyConfiguration,
                                                                  String dataFrom, long cacheTimeoutDuration, boolean mappingEnabled)
            throws AxisFault {

        if (checkEventPublisherValidity(eventPublisherName)) {
            try {
                EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

                EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();
                eventPublisherConfiguration.setProcessEnabled(true);
                eventPublisherConfiguration.setEventPublisherName(eventPublisherName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventPublisherConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventPublisherConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                constructOutputAdapterRelatedConfigs(eventPublisherName, eventAdapterType, outputPropertyConfiguration,
                        eventPublisherConfiguration, EventPublisherConstants.EF_JSON_MAPPING_TYPE);

                JSONOutputMapping jsonOutputMapping = new JSONOutputMapping();

                jsonOutputMapping.setCustomMappingEnabled(mappingEnabled);
                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    jsonOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                    jsonOutputMapping.setMappingText(jsonData);
                    jsonOutputMapping.setCacheTimeoutDuration(cacheTimeoutDuration);
                    outputEventAttributes = getOutputMappingPropertyList(jsonData);
                }

                eventPublisherConfiguration.setOutputMapping(jsonOutputMapping);
                eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfiguration);

            } catch (EventPublisherConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventPublisherName + " is already registered for this tenant");
        }
        return true;
    }

    public boolean setStatisticsEnabled(String eventPublisherName, boolean flag) throws AxisFault {

        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            eventPublisherService.setStatisticsEnabled(eventPublisherName, flag);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean setTracingEnabled(String eventPublisherName, boolean flag) throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            eventPublisherService.setTraceEnabled(eventPublisherName, flag);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean setProcessingEnabled(String eventPublisherName, boolean flag) throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            eventPublisherService.setProcessEnabled(eventPublisherName, flag);

        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public OutputAdapterConfigurationDto getOutputAdapterConfigurationSchema(String adopterType) {
        OutputEventAdapterService outputEventAdapterService = EventPublisherAdminServiceValueHolder.getOutputEventAdapterService();
        OutputEventAdapterSchema outputEventAdapterSchema = outputEventAdapterService.getOutputEventAdapterSchema(adopterType);

        OutputAdapterConfigurationDto outputAdapterConfigurationDto = new OutputAdapterConfigurationDto();
        outputAdapterConfigurationDto.setOutputEventAdapterStaticProperties(getPropertyConfigurations(null, outputEventAdapterSchema.getStaticPropertyList()));
        outputAdapterConfigurationDto.setOutputEventAdapterDynamicProperties(getPropertyConfigurations(null, outputEventAdapterSchema.getDynamicPropertyList()));
        outputAdapterConfigurationDto.setEventAdapterType(adopterType);
        outputAdapterConfigurationDto.setSupportedMessageFormats(
                outputEventAdapterSchema.getSupportedMessageFormats().
                        toArray(new String[outputEventAdapterSchema.getSupportedMessageFormats().size()]));
        outputAdapterConfigurationDto.setUsageTips(outputEventAdapterSchema.getUsageTips());
        return outputAdapterConfigurationDto;
    }

    public String[] getAllOutputAdapterTypes() {
        OutputEventAdapterService outputEventAdapterService = EventPublisherAdminServiceValueHolder.getOutputEventAdapterService();
        List<String> outputEventAdapters = outputEventAdapterService.getOutputEventAdapterTypes();
        if (outputEventAdapters == null) {
            return new String[0];
        } else {
            Collections.sort(outputEventAdapters);
            String[] types = new String[outputEventAdapters.size()];
            return outputEventAdapters.toArray(types);
        }
    }

    // Private Methods
    private void constructOutputAdapterRelatedConfigs(String eventPublisherName, String eventAdapterType,
                                                      BasicOutputAdapterPropertyDto[] outputPropertyConfiguration,
                                                      EventPublisherConfiguration eventPublisherConfiguration,
                                                      String messageFormat) {
        OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
        outputEventAdapterConfiguration.setName(eventPublisherName);
        outputEventAdapterConfiguration.setType(eventAdapterType);
        outputEventAdapterConfiguration.setMessageFormat(messageFormat);
        outputEventAdapterConfiguration.setStaticProperties(new HashMap<String, String>());
        eventPublisherConfiguration.setToAdapterDynamicProperties(new HashMap<String, String>());

        // add output message property configuration to the map
        if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {

            for (BasicOutputAdapterPropertyDto eventPublisherProperty : outputPropertyConfiguration) {
                if (!eventPublisherProperty.getValue().trim().equals("")) {
                    if (eventPublisherProperty.isStatic()) {
                        outputEventAdapterConfiguration.getStaticProperties().put(eventPublisherProperty.getKey().trim(), eventPublisherProperty.getValue().trim());
                    } else {
                        eventPublisherConfiguration.getToAdapterDynamicProperties().put(eventPublisherProperty.getKey().trim(), eventPublisherProperty.getValue().trim());
                    }
                }
            }

        }

        eventPublisherConfiguration.setToAdapterConfiguration(outputEventAdapterConfiguration);
    }

    private EventMappingPropertyDto[] getEventPropertyDtoArray(
            List<EventOutputProperty> eventOutputPropertyList) {

        if (eventOutputPropertyList != null && eventOutputPropertyList.size() > 0) {
            EventMappingPropertyDto[] eventMappingPropertyDtos = new EventMappingPropertyDto[eventOutputPropertyList.size()];
            int index = 0;
            Iterator<EventOutputProperty> outputPropertyIterator = eventOutputPropertyList.iterator();
            while (outputPropertyIterator.hasNext()) {
                EventOutputProperty eventOutputProperty = outputPropertyIterator.next();
                eventMappingPropertyDtos[index] = new EventMappingPropertyDto(eventOutputProperty.getName(), eventOutputProperty.getValueOf(), eventOutputProperty.getType().toString().toLowerCase());
                index++;
            }

            return eventMappingPropertyDtos;
        }
        return null;
    }

    private DetailOutputAdapterPropertyDto[] getPropertyConfigurations(Map<String, String> messageProperties, List<Property> propertyList) {
        if (propertyList != null && propertyList.size() > 0) {
            DetailOutputAdapterPropertyDto[] detailOutputAdapterPropertyDtoArray = new DetailOutputAdapterPropertyDto[propertyList.size()];
            int index = 0;
            for (Property property : propertyList) {
                // create output event property
                String value = null;
                if (messageProperties != null) {
                    value = messageProperties.get(property.getPropertyName());
                }
                detailOutputAdapterPropertyDtoArray[index] = new DetailOutputAdapterPropertyDto(property.getPropertyName(),
                        value);
                // set output event property parameters
                detailOutputAdapterPropertyDtoArray[index].setSecured(property.isSecured());
                detailOutputAdapterPropertyDtoArray[index].setRequired(property.isRequired());
                detailOutputAdapterPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                detailOutputAdapterPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                detailOutputAdapterPropertyDtoArray[index].setHint(property.getHint());
                detailOutputAdapterPropertyDtoArray[index].setOptions(property.getOptions());
                index++;
            }
            return detailOutputAdapterPropertyDtoArray;
        }
        return new DetailOutputAdapterPropertyDto[0];
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
            String property = text.substring(text.indexOf("{{") + 2, text.indexOf("}}"));
            if (property != null && !property.startsWith(PropertyAttributeTypeConstants.ARBITRARY_MAP_PREFIX)) {
                // Do not consider arbitrary map properties for validation
                mappingTextList.add(property);
            }
            text = text.substring(text.indexOf("}}") + 2);
        }
        return mappingTextList;
    }

    private boolean checkEventPublisherValidity(String eventPublisherName) throws AxisFault {
        try {
            EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            List<EventPublisherConfiguration> eventPublisherConfigurationList = null;

            eventPublisherConfigurationList = eventPublisherService.getAllActiveEventPublisherConfigurations();
            Iterator eventPublisherConfigurationIterator = eventPublisherConfigurationList.iterator();
            while (eventPublisherConfigurationIterator.hasNext()) {

                EventPublisherConfiguration eventPublisherConfiguration = (EventPublisherConfiguration) eventPublisherConfigurationIterator.next();
                if (eventPublisherConfiguration.getEventPublisherName().equalsIgnoreCase(eventPublisherName)) {
                    return false;
                }
            }

        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    private boolean validateRegistrySource(String fromData) {

        return !fromData.equalsIgnoreCase("inline");
    }

    private String getPropertyAttributeDataType(String propertyName,
                                                StreamDefinition streamDefinition)
            throws AxisFault {

        if (propertyName != null) {
            List<Attribute> metaDataList = streamDefinition.getMetaData();
            if (metaDataList != null) {
                for (Attribute attribute : metaDataList) {
                    if (propertyName.equalsIgnoreCase(PropertyAttributeTypeConstants.PROPERTY_META_PREFIX + attribute.getName())) {
                        return attribute.getType().toString().toLowerCase();
                    }
                }
            }

            List<Attribute> correlationDataList = streamDefinition.getCorrelationData();
            if (correlationDataList != null) {
                for (Attribute attribute : correlationDataList) {
                    if (propertyName.equalsIgnoreCase(PropertyAttributeTypeConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName())) {
                        return attribute.getType().toString().toLowerCase();
                    }
                }
            }

            List<Attribute> payloadDataList = streamDefinition.getPayloadData();
            if (payloadDataList != null) {
                for (Attribute attribute : payloadDataList) {
                    if (propertyName.equalsIgnoreCase(attribute.getName())) {
                        return attribute.getType().toString().toLowerCase();
                    }
                }
            }
        }

        throw new AxisFault("Output Stream attributes are not matching with input stream definition");

    }

    public void testPublisherConnection(String eventPublisherName, String eventAdapterType,
                                        BasicOutputAdapterPropertyDto[] outputPropertyConfiguration,
                                        String messageFormat) throws AxisFault {

        try {

            EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();

            constructOutputAdapterRelatedConfigs(eventPublisherName, eventAdapterType, outputPropertyConfiguration,
                    eventPublisherConfiguration, messageFormat);

            EventPublisherAdminServiceValueHolder.getOutputEventAdapterService()
                    .testConnection(eventPublisherConfiguration.getToAdapterConfiguration());

        } catch (TestConnectionNotSupportedException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        } catch (OutputEventAdapterException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public boolean isPublisherEditable(String eventPublisherName) {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        List<EventPublisherConfiguration> eventPublisherConfigurationList = null;
        boolean isEditable = false;
        try {
            eventPublisherConfigurationList = eventPublisherService.getAllActiveEventPublisherConfigurations();
            Iterator eventReceiverConfigurationIterator = eventPublisherConfigurationList.iterator();
            while (eventReceiverConfigurationIterator.hasNext()) {
                EventPublisherConfiguration eventPublisherConfiguration =
                        (EventPublisherConfiguration) eventReceiverConfigurationIterator.next();
                if (eventPublisherConfiguration.getEventPublisherName().equalsIgnoreCase(eventPublisherName)) {
                    isEditable = eventPublisherConfiguration.isEditable();
                }
            }
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
        }
        return isEditable;
    }

    public boolean isPublisherStatisticsEnabled(String eventPublisherName) {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        List<EventPublisherConfiguration> eventPublisherConfigurationList = null;
        boolean isStatisticsEnabled = false;
        try {
            eventPublisherConfigurationList = eventPublisherService.getAllActiveEventPublisherConfigurations();
            Iterator eventReceiverConfigurationIterator = eventPublisherConfigurationList.iterator();
            while (eventReceiverConfigurationIterator.hasNext()) {
                EventPublisherConfiguration eventPublisherConfiguration =
                        (EventPublisherConfiguration) eventReceiverConfigurationIterator.next();
                if (eventPublisherConfiguration.getEventPublisherName().equalsIgnoreCase(eventPublisherName)) {
                    isStatisticsEnabled = eventPublisherConfiguration.isStatisticsEnabled();
                }
            }
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
        }
        return isStatisticsEnabled;
    }


    public boolean isPublisherProcessingEnabled(String eventPublisherName) {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        List<EventPublisherConfiguration> eventPublisherConfigurationList = null;
        boolean isProcessEnabled = true;
        try {
            eventPublisherConfigurationList = eventPublisherService.getAllActiveEventPublisherConfigurations();
            Iterator eventReceiverConfigurationIterator = eventPublisherConfigurationList.iterator();
            while (eventReceiverConfigurationIterator.hasNext()) {
                EventPublisherConfiguration eventPublisherConfiguration =
                        (EventPublisherConfiguration) eventReceiverConfigurationIterator.next();
                if (eventPublisherConfiguration.getEventPublisherName().equalsIgnoreCase(eventPublisherName)) {
                    isProcessEnabled = eventPublisherConfiguration.isProcessingEnabled();
                }
            }
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
        }
        return isProcessEnabled;
    }

    public boolean isPublisherTraceEnabled(String eventPublisherName) {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        List<EventPublisherConfiguration> eventPublisherConfigurationList = null;
        boolean isTraceEnabled = false;
        try {
            eventPublisherConfigurationList = eventPublisherService.getAllActiveEventPublisherConfigurations();
            Iterator eventReceiverConfigurationIterator = eventPublisherConfigurationList.iterator();
            while (eventReceiverConfigurationIterator.hasNext()) {
                EventPublisherConfiguration eventPublisherConfiguration =
                        (EventPublisherConfiguration) eventReceiverConfigurationIterator.next();
                if (eventPublisherConfiguration.getEventPublisherName().equalsIgnoreCase(eventPublisherName)) {
                    isTraceEnabled = eventPublisherConfiguration.isTracingEnabled();
                }
            }
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
        }
        return isTraceEnabled;
    }


}