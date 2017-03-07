/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.receiver.admin;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterSchema;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.input.adapter.core.Property;
import org.wso2.carbon.event.receiver.admin.internal.EventReceiverAdminConstants;
import org.wso2.carbon.event.receiver.admin.internal.ds.EventReceiverAdminServiceValueHolder;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.config.*;
import org.wso2.carbon.event.receiver.core.config.mapping.*;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;

import java.util.*;

public class EventReceiverAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventReceiverAdminService.class);

    public EventReceiverConfigurationInfoDto[] getAllActiveEventReceiverConfigurations()
            throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

        // get event receiver configurations
        List<EventReceiverConfiguration> eventReceiverConfigurationList;
        eventReceiverConfigurationList = eventReceiverService.getAllActiveEventReceiverConfigurations();

        if (eventReceiverConfigurationList != null) {
            // create event receiver configuration details array
            EventReceiverConfigurationInfoDto[] eventReceiverConfigurationInfoDtoArray = new
                    EventReceiverConfigurationInfoDto[eventReceiverConfigurationList.size()];
            for (int index = 0; index < eventReceiverConfigurationInfoDtoArray.length; index++) {
                EventReceiverConfiguration eventReceiverConfiguration = eventReceiverConfigurationList.get(index);
                String eventReceiverName = eventReceiverConfiguration.getEventReceiverName();
                String mappingType = eventReceiverConfiguration.getInputMapping().getMappingType();
                String inputEventAdapterType = eventReceiverConfiguration.getFromAdapterConfiguration().getType();
                String streamNameWithVersion = eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion();


                eventReceiverConfigurationInfoDtoArray[index] = new EventReceiverConfigurationInfoDto();
                eventReceiverConfigurationInfoDtoArray[index].setEventReceiverName(eventReceiverName);
                eventReceiverConfigurationInfoDtoArray[index].setMessageFormat(mappingType);
                eventReceiverConfigurationInfoDtoArray[index].setInputAdapterType(inputEventAdapterType);
                eventReceiverConfigurationInfoDtoArray[index].setInputStreamId(streamNameWithVersion);
                eventReceiverConfigurationInfoDtoArray[index].setEnableStats(eventReceiverConfiguration.isStatisticsEnabled());
                eventReceiverConfigurationInfoDtoArray[index].setEnableTracing(eventReceiverConfiguration.isTraceEnabled());
                eventReceiverConfigurationInfoDtoArray[index].setEditable(eventReceiverConfiguration.isEditable());
            }
            Arrays.sort(eventReceiverConfigurationInfoDtoArray, new Comparator() {

                @Override
                public int compare(Object o1, Object o2) {
                    return ((EventReceiverConfigurationInfoDto) o1).getEventReceiverName().compareTo(((EventReceiverConfigurationInfoDto) o2).getEventReceiverName());
                }
            });
            return eventReceiverConfigurationInfoDtoArray;
        } else {
            return new EventReceiverConfigurationInfoDto[0];
        }
    }

    public EventReceiverConfigurationInfoDto[] getAllStreamSpecificActiveEventReceiverConfigurations(
            String streamId)
            throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

        // get event receiver configurations
        List<EventReceiverConfiguration> eventReceiverConfigurationList;
        eventReceiverConfigurationList = eventReceiverService.getAllActiveEventReceiverConfigurations(streamId);

        if (eventReceiverConfigurationList != null) {
            // create event receiver configuration details array
            EventReceiverConfigurationInfoDto[] eventReceiverConfigurationInfoDtoArray = new
                    EventReceiverConfigurationInfoDto[eventReceiverConfigurationList.size()];
            for (int index = 0; index < eventReceiverConfigurationInfoDtoArray.length; index++) {
                EventReceiverConfiguration eventReceiverConfiguration = eventReceiverConfigurationList.get(index);
                String eventReceiverName = eventReceiverConfiguration.getEventReceiverName();
                String mappingType = eventReceiverConfiguration.getInputMapping().getMappingType();
                String inputEventAdapterType = eventReceiverConfiguration.getFromAdapterConfiguration().getType();

                eventReceiverConfigurationInfoDtoArray[index] = new EventReceiverConfigurationInfoDto();
                eventReceiverConfigurationInfoDtoArray[index].setEventReceiverName(eventReceiverName);
                eventReceiverConfigurationInfoDtoArray[index].setMessageFormat(mappingType);
                eventReceiverConfigurationInfoDtoArray[index].setInputAdapterType(inputEventAdapterType);
                eventReceiverConfigurationInfoDtoArray[index].setEnableStats(eventReceiverConfiguration.isStatisticsEnabled());
                eventReceiverConfigurationInfoDtoArray[index].setEnableTracing(eventReceiverConfiguration.isTraceEnabled());
                eventReceiverConfigurationInfoDtoArray[index].setEditable(eventReceiverConfiguration.isEditable());
            }
            Arrays.sort(eventReceiverConfigurationInfoDtoArray, new Comparator() {

                @Override
                public int compare(Object o1, Object o2) {
                    return ((EventReceiverConfigurationInfoDto) o1).getEventReceiverName().compareTo(((EventReceiverConfigurationInfoDto) o2).getEventReceiverName());
                }
            });
            return eventReceiverConfigurationInfoDtoArray;
        } else {
            return new EventReceiverConfigurationInfoDto[0];
        }
    }

    public EventReceiverConfigurationFileDto[] getAllInactiveEventReceiverConfigurations()
            throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFileList = eventReceiverService.getAllInactiveEventReceiverConfigurations();
        if (eventReceiverConfigurationFileList != null) {

            // create event receiver file details array
            EventReceiverConfigurationFileDto[] eventReceiverFileDtoArray = new
                    EventReceiverConfigurationFileDto[eventReceiverConfigurationFileList.size()];

            for (int index = 0; index < eventReceiverFileDtoArray.length; index++) {
                EventReceiverConfigurationFile eventReceiverConfigurationFile = eventReceiverConfigurationFileList.get(
                        index);
                String fileName = eventReceiverConfigurationFile.getFileName();
                String eventReceiverName = eventReceiverConfigurationFile.getEventReceiverName();
                String statusMsg = eventReceiverConfigurationFile.getDeploymentStatusMessage();
                if (eventReceiverConfigurationFile.getDependency() != null) {
                    statusMsg = statusMsg + " [Dependency: " + eventReceiverConfigurationFile.getDependency() + "]";
                }

                eventReceiverFileDtoArray[index] = new EventReceiverConfigurationFileDto(fileName, eventReceiverName, statusMsg);
            }
            Arrays.sort(eventReceiverFileDtoArray, new Comparator() {

                @Override
                public int compare(Object o1, Object o2) {
                    return ((EventReceiverConfigurationFileDto) o1).getFileName().compareTo(((EventReceiverConfigurationFileDto) o2).getFileName());
                }
            });
            return eventReceiverFileDtoArray;
        } else {
            return new EventReceiverConfigurationFileDto[0];
        }
    }

    public EventReceiverConfigurationDto getActiveEventReceiverConfiguration(
            String eventReceiverName) throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

        EventReceiverConfiguration eventReceiverConfiguration = eventReceiverService.getActiveEventReceiverConfiguration(
                eventReceiverName);
        if (eventReceiverConfiguration != null) {
            EventReceiverConfigurationDto eventReceiverConfigurationDto = new EventReceiverConfigurationDto();
            eventReceiverConfigurationDto.setEventReceiverName(eventReceiverConfiguration.getEventReceiverName());
            String streamNameWithVersion = eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion();
            eventReceiverConfigurationDto.setToStreamNameWithVersion(streamNameWithVersion);

            InputEventAdapterConfiguration fromAdapterConfiguration = eventReceiverConfiguration.getFromAdapterConfiguration();

            if (fromAdapterConfiguration != null) {
                InputEventAdapterService inputEventAdapterService = EventReceiverAdminServiceValueHolder.getInputEventAdapterService();
                InputEventAdapterSchema inputEventAdapterSchema = inputEventAdapterService.getInputEventAdapterSchema(fromAdapterConfiguration.getType());

                InputAdapterConfigurationDto fromAdapterConfigurationDto = new InputAdapterConfigurationDto();
                fromAdapterConfigurationDto.setEventAdapterType(fromAdapterConfiguration.getType());
                fromAdapterConfigurationDto.setSupportedMessageFormats(
                        inputEventAdapterSchema.getSupportedMessageFormats().
                                toArray(new String[inputEventAdapterSchema.getSupportedMessageFormats().size()]));
                fromAdapterConfigurationDto.setUsageTips(inputEventAdapterSchema.getUsageTips());

                Map<String, String> inputAdapterProperties = new HashMap<String, String>();
                inputAdapterProperties.putAll(fromAdapterConfiguration.getProperties());

                DetailInputAdapterPropertyDto[] detailInputAdapterPropertyDtos = getPropertyConfigurations(inputAdapterProperties, inputEventAdapterSchema.getPropertyList());
                fromAdapterConfigurationDto.setInputEventAdapterProperties(detailInputAdapterPropertyDtos);

                eventReceiverConfigurationDto.setFromAdapterConfigurationDto(fromAdapterConfigurationDto);
            }

            InputMapping inputMapping = eventReceiverConfiguration.getInputMapping();

            eventReceiverConfigurationDto.setCustomMappingEnabled(eventReceiverConfiguration.getInputMapping().isCustomMappingEnabled());
            eventReceiverConfigurationDto.setMessageFormat(inputMapping.getMappingType());

            if (inputMapping.isCustomMappingEnabled()) {
                if (inputMapping.getMappingType().equalsIgnoreCase(EventReceiverConstants.ER_WSO2EVENT_MAPPING_TYPE)) {
                    List<EventMappingPropertyDto> metaMappingPropertyDtos = new ArrayList<EventMappingPropertyDto>();
                    List<EventMappingPropertyDto> correlationMappingPropertyDtos = new ArrayList<EventMappingPropertyDto>();
                    List<EventMappingPropertyDto> payloadMappingPropertyDtos = new ArrayList<EventMappingPropertyDto>();

                    for (InputMappingAttribute inputMappingAttribute : inputMapping.getInputMappingAttributes()) {
                        EventMappingPropertyDto mappingPropertyDto = new EventMappingPropertyDto();
                        mappingPropertyDto.setName(inputMappingAttribute.getFromElementKey());
                        mappingPropertyDto.setValueOf(inputMappingAttribute.getToElementKey());
                        mappingPropertyDto.setType(EventReceiverAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
                        mappingPropertyDto.setDefaultValue(inputMappingAttribute.getDefaultValue());
                        if (EventReceiverConstants.META_DATA_VAL.equalsIgnoreCase(inputMappingAttribute.getFromElementType())) {
                            metaMappingPropertyDtos.add(mappingPropertyDto);
                        } else if (EventReceiverConstants.CORRELATION_DATA_VAL.equalsIgnoreCase(inputMappingAttribute.getFromElementType())) {
                            correlationMappingPropertyDtos.add(mappingPropertyDto);
                        } else if (EventReceiverConstants.PAYLOAD_DATA_VAL.equalsIgnoreCase(inputMappingAttribute.getFromElementType())) {
                            payloadMappingPropertyDtos.add(mappingPropertyDto);
                        }
                    }
                    eventReceiverConfigurationDto.setMetaMappingPropertyDtos(metaMappingPropertyDtos.toArray(new EventMappingPropertyDto[metaMappingPropertyDtos.size()]));
                    eventReceiverConfigurationDto.setCorrelationMappingPropertyDtos(correlationMappingPropertyDtos.toArray(new EventMappingPropertyDto[correlationMappingPropertyDtos.size()]));
                    eventReceiverConfigurationDto.setMappingPropertyDtos(payloadMappingPropertyDtos.toArray(new EventMappingPropertyDto[payloadMappingPropertyDtos.size()]));

                    //Get fromStreamNameWithVersion for WSO2Event type custom mapping
                    WSO2EventInputMapping wso2EventInputMapping = (WSO2EventInputMapping) inputMapping;
                    String fromStreamNameWithVersion = wso2EventInputMapping.getFromEventName() + ":" + wso2EventInputMapping.getFromEventVersion();
                    eventReceiverConfigurationDto.setFromStreamNameWithVersion(fromStreamNameWithVersion);

                } else if (inputMapping.getMappingType().equalsIgnoreCase(EventReceiverConstants.ER_XML_MAPPING_TYPE)) {

                    List<EventMappingPropertyDto> xPathDefinitions = new ArrayList<EventMappingPropertyDto>();
                    for (XPathDefinition xPathDefinition : ((XMLInputMapping) inputMapping).getXPathDefinitions()) {
                        EventMappingPropertyDto mappingPropertyDto = new EventMappingPropertyDto();
                        mappingPropertyDto.setName(xPathDefinition.getPrefix());
                        mappingPropertyDto.setValueOf(xPathDefinition.getNamespaceUri());
                        xPathDefinitions.add(mappingPropertyDto);
                    }
                    eventReceiverConfigurationDto.setXpathDefinitionMappingPropertyDtos(xPathDefinitions.toArray(new EventMappingPropertyDto[xPathDefinitions.size()]));

                    List<EventMappingPropertyDto> mappingPropertyDtos = new ArrayList<EventMappingPropertyDto>();
                    for (InputMappingAttribute inputMappingAttribute : inputMapping.getInputMappingAttributes()) {
                        EventMappingPropertyDto mappingPropertyDto = new EventMappingPropertyDto();
                        mappingPropertyDto.setName(inputMappingAttribute.getFromElementKey());
                        mappingPropertyDto.setValueOf(inputMappingAttribute.getToElementKey());
                        mappingPropertyDto.setType(EventReceiverAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
                        mappingPropertyDto.setDefaultValue(inputMappingAttribute.getDefaultValue());
                        mappingPropertyDtos.add(mappingPropertyDto);
                    }
                    eventReceiverConfigurationDto.setMappingPropertyDtos(mappingPropertyDtos.toArray(new EventMappingPropertyDto[mappingPropertyDtos.size()]));
                    eventReceiverConfigurationDto.setParentSelectorXpath(((XMLInputMapping) inputMapping).getParentSelectorXpath());

                } else {

                    // for map, text and json
                    List<EventMappingPropertyDto> mappingPropertyDtos = new ArrayList<EventMappingPropertyDto>();
                    for (InputMappingAttribute inputMappingAttribute : inputMapping.getInputMappingAttributes()) {
                        EventMappingPropertyDto mappingPropertyDto = new EventMappingPropertyDto();
                        mappingPropertyDto.setName(inputMappingAttribute.getFromElementKey());
                        mappingPropertyDto.setValueOf(inputMappingAttribute.getToElementKey());
                        mappingPropertyDto.setType(EventReceiverAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
                        mappingPropertyDto.setDefaultValue(inputMappingAttribute.getDefaultValue());
                        mappingPropertyDtos.add(mappingPropertyDto);
                    }
                    eventReceiverConfigurationDto.setMappingPropertyDtos(mappingPropertyDtos.toArray(new EventMappingPropertyDto[mappingPropertyDtos.size()]));

                }
            }

            return eventReceiverConfigurationDto;
        }

        return null;
    }

    public String getActiveEventReceiverConfigurationContent(String eventReceiverName)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            return eventReceiverService.getActiveEventReceiverConfigurationContent(eventReceiverName);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getInactiveEventReceiverConfigurationContent(String fileName)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            String eventReceiverConfigurationFile = eventReceiverService.getInactiveEventReceiverConfigurationContent(fileName);
            return eventReceiverConfigurationFile.trim();
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public boolean undeployActiveEventReceiverConfiguration(String eventReceiverName)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            eventReceiverService.undeployActiveEventReceiverConfiguration(eventReceiverName);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean undeployInactiveEventReceiverConfiguration(String fileName)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            eventReceiverService.undeployInactiveEventReceiverConfiguration(fileName);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean editActiveEventReceiverConfiguration(String eventReceiverConfiguration,
                                                        String eventReceiverName)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            eventReceiverService.editActiveEventReceiverConfiguration(eventReceiverConfiguration, eventReceiverName);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean editInactiveEventReceiverConfiguration(
            String eventReceiverConfiguration,
            String fileName)
            throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            eventReceiverService.editInactiveEventReceiverConfiguration(eventReceiverConfiguration, fileName);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean deployEventReceiverConfiguration(String eventReceiverConfigXml)
            throws AxisFault {
        try {
            EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
            eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfigXml);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean deployWso2EventReceiverConfiguration(String eventReceiverName,
                                                        String streamNameWithVersion,
                                                        String eventAdapterType,
                                                        EventMappingPropertyDto[] metaData,
                                                        EventMappingPropertyDto[] correlationData,
                                                        EventMappingPropertyDto[] payloadData,
                                                        BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                        boolean mappingEnabled,
                                                        String fromStreamNameWithVersion)
            throws AxisFault {

        if (checkEventReceiverValidity(eventReceiverName)) {
            try {
                EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

                EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

                eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
                String[] toStreamProperties = streamNameWithVersion.split(":");
                eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
                eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);

                constructInputAdapterRelatedConfigs(eventReceiverName, eventAdapterType, inputPropertyConfiguration,
                        eventReceiverConfiguration, EventReceiverConstants.ER_WSO2EVENT_MAPPING_TYPE);

                WSO2EventInputMapping wso2EventInputMapping = new WSO2EventInputMapping();
                wso2EventInputMapping.setCustomMappingEnabled(mappingEnabled);

                if (mappingEnabled) {
                    if (metaData != null && metaData.length != 0) {
                        for (EventMappingPropertyDto mappingPropertyDto : metaData) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingPropertyDto.getName(), mappingPropertyDto.getValueOf(), EventReceiverAdminConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingPropertyDto.getType()), EventReceiverConstants.META_DATA_VAL);
                            inputProperty.setDefaultValue(mappingPropertyDto.getDefaultValue());
                            wso2EventInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                    if (correlationData != null && correlationData.length != 0) {
                        for (EventMappingPropertyDto mappingPropertyDto : correlationData) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingPropertyDto.getName(), mappingPropertyDto.getValueOf(), EventReceiverAdminConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingPropertyDto.getType()), EventReceiverConstants.CORRELATION_DATA_VAL);
                            inputProperty.setDefaultValue(mappingPropertyDto.getDefaultValue());
                            wso2EventInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                    if (payloadData != null && payloadData.length != 0) {
                        for (EventMappingPropertyDto mappingPropertyDto : payloadData) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingPropertyDto.getName(), mappingPropertyDto.getValueOf(), EventReceiverAdminConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingPropertyDto.getType()), EventReceiverConstants.PAYLOAD_DATA_VAL);
                            inputProperty.setDefaultValue(mappingPropertyDto.getDefaultValue());
                            wso2EventInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                    String[] fromStreamProperties = fromStreamNameWithVersion.split(":");
                    wso2EventInputMapping.setFromEventName(fromStreamProperties[0]);
                    wso2EventInputMapping.setFromEventVersion(fromStreamProperties[1]);

                }
                eventReceiverConfiguration.setInputMapping(wso2EventInputMapping);
                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration);
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventReceiverName + " is already registered for this tenant");
        }
        return true;
    }

    public boolean deployTextEventReceiverConfiguration(String eventReceiverName,
                                                        String streamNameWithVersion,
                                                        String eventAdapterType,
                                                        EventMappingPropertyDto[] inputMappings,
                                                        BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                        boolean mappingEnabled)
            throws AxisFault {

        if (checkEventReceiverValidity(eventReceiverName)) {
            try {
                EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

                EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

                eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
                String[] toStreamProperties = streamNameWithVersion.split(":");
                eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
                eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);

                constructInputAdapterRelatedConfigs(eventReceiverName, eventAdapterType, inputPropertyConfiguration,
                        eventReceiverConfiguration, EventReceiverConstants.ER_TEXT_MAPPING_TYPE);

                TextInputMapping textInputMapping = new TextInputMapping();
                textInputMapping.setCustomMappingEnabled(mappingEnabled);
                if (mappingEnabled) {
                    if (inputMappings != null && inputMappings.length != 0) {
                        for (EventMappingPropertyDto mappingProperty : inputMappings) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingProperty.getName(), mappingProperty.getValueOf(), EventReceiverAdminConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingProperty.getType()));
                            inputProperty.setDefaultValue(mappingProperty.getDefaultValue());
                            textInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                }
                eventReceiverConfiguration.setInputMapping(textInputMapping);

                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration);

            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventReceiverName + " is already registered for this tenant");
        }
        return true;
    }

    public boolean deployXmlEventReceiverConfiguration(String eventReceiverName,
                                                       String streamNameWithVersion,
                                                       String eventAdapterType,
                                                       String parentXpath,
                                                       EventMappingPropertyDto[] namespaces,
                                                       EventMappingPropertyDto[] inputMappings,
                                                       BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                       boolean mappingEnabled)
            throws AxisFault {

        if (checkEventReceiverValidity(eventReceiverName)) {
            try {
                EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

                EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

                eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
                String[] toStreamProperties = streamNameWithVersion.split(":");
                eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
                eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);

                constructInputAdapterRelatedConfigs(eventReceiverName, eventAdapterType, inputPropertyConfiguration,
                        eventReceiverConfiguration, EventReceiverConstants.ER_XML_MAPPING_TYPE);

                XMLInputMapping xmlInputMapping = new XMLInputMapping();
                xmlInputMapping.setCustomMappingEnabled(mappingEnabled);
                xmlInputMapping.setParentSelectorXpath(parentXpath);
                if (namespaces != null && namespaces.length != 0) {
                    List<XPathDefinition> xPathDefinitions = new ArrayList<XPathDefinition>();
                    for (EventMappingPropertyDto namespace : namespaces) {
                        XPathDefinition xPathDefinition = new XPathDefinition(namespace.getName(), namespace.getValueOf());
                        xPathDefinitions.add(xPathDefinition);
                    }
                    xmlInputMapping.setXPathDefinitions(xPathDefinitions);
                }

                if (mappingEnabled) {
                    if (inputMappings != null && inputMappings.length != 0) {
                        for (EventMappingPropertyDto mappingProperty : inputMappings) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingProperty.getName(), mappingProperty.getValueOf(), EventReceiverAdminConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingProperty.getType()));
                            inputProperty.setDefaultValue(mappingProperty.getDefaultValue());
                            xmlInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                }
                eventReceiverConfiguration.setInputMapping(xmlInputMapping);

                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration);
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventReceiverName + " is already registered for this tenant");
        }
        return true;
    }

    public boolean deployMapEventReceiverConfiguration(String eventReceiverName,
                                                       String streamNameWithVersion,
                                                       String eventAdapterType,
                                                       EventMappingPropertyDto[] inputMappings,
                                                       BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                       boolean mappingEnabled)
            throws AxisFault {

        if (checkEventReceiverValidity(eventReceiverName)) {
            try {
                EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

                EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

                eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
                String[] toStreamProperties = streamNameWithVersion.split(":");
                eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
                eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);

                constructInputAdapterRelatedConfigs(eventReceiverName, eventAdapterType, inputPropertyConfiguration,
                        eventReceiverConfiguration, EventReceiverConstants.ER_MAP_MAPPING_TYPE);


                MapInputMapping mapInputMapping = new MapInputMapping();
                mapInputMapping.setCustomMappingEnabled(mappingEnabled);

                if (mappingEnabled) {
                    if (inputMappings != null && inputMappings.length != 0) {
                        for (EventMappingPropertyDto mappingProperty : inputMappings) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingProperty.getName(), mappingProperty.getValueOf(), EventReceiverAdminConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingProperty.getType()));
                            inputProperty.setDefaultValue(mappingProperty.getDefaultValue());
                            mapInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                }
                eventReceiverConfiguration.setInputMapping(mapInputMapping);

                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration);

            } catch (EventReceiverConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventReceiverName + " is already registered for this tenant");
        }
        return true;
    }

    public boolean deployJsonEventReceiverConfiguration(String eventReceiverName,
                                                        String streamNameWithVersion,
                                                        String eventAdapterType,
                                                        EventMappingPropertyDto[] inputMappings,
                                                        BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                        boolean mappingEnabled)
            throws AxisFault {

        if (checkEventReceiverValidity(eventReceiverName)) {
            try {
                EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

                EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

                eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
                String[] toStreamProperties = streamNameWithVersion.split(":");
                eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
                eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);

                constructInputAdapterRelatedConfigs(eventReceiverName, eventAdapterType, inputPropertyConfiguration,
                        eventReceiverConfiguration, EventReceiverConstants.ER_JSON_MAPPING_TYPE);

                JSONInputMapping jsonInputMapping = new JSONInputMapping();
                jsonInputMapping.setCustomMappingEnabled(mappingEnabled);
                if (mappingEnabled) {
                    if (inputMappings != null && inputMappings.length != 0) {
                        for (EventMappingPropertyDto mappingProperty : inputMappings) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingProperty.getName(), mappingProperty.getValueOf(), EventReceiverAdminConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingProperty.getType()));
                            inputProperty.setDefaultValue(mappingProperty.getDefaultValue());
                            jsonInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                }
                eventReceiverConfiguration.setInputMapping(jsonInputMapping);

                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration);

            } catch (EventReceiverConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventReceiverName + " is already registered for this tenant");
        }
        return true;
    }

    public boolean setStatisticsEnabled(String eventReceiverName, boolean flag) throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            eventReceiverService.setStatisticsEnabled(eventReceiverName, flag);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public boolean setTracingEnabled(String eventReceiverName, boolean flag) throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            eventReceiverService.setTraceEnabled(eventReceiverName, flag);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    public InputAdapterConfigurationDto getInputAdapterConfigurationSchema(String adopterType) {
        InputEventAdapterService inputEventAdapterService = EventReceiverAdminServiceValueHolder.getInputEventAdapterService();
        InputEventAdapterSchema inputEventAdapterSchema = inputEventAdapterService.getInputEventAdapterSchema(adopterType);

        InputAdapterConfigurationDto inputAdapterConfigurationDto = new InputAdapterConfigurationDto();
        inputAdapterConfigurationDto.setInputEventAdapterProperties(getPropertyConfigurations(null, inputEventAdapterSchema.getPropertyList()));
        inputAdapterConfigurationDto.setEventAdapterType(adopterType);
        inputAdapterConfigurationDto.setSupportedMessageFormats(
                inputEventAdapterSchema.getSupportedMessageFormats().
                        toArray(new String[inputEventAdapterSchema.getSupportedMessageFormats().size()]));
        inputAdapterConfigurationDto.setUsageTips(inputEventAdapterSchema.getUsageTips());
        return inputAdapterConfigurationDto;
    }

    public String[] getAllInputAdapterTypes() {
        InputEventAdapterService inputEventAdapterService = EventReceiverAdminServiceValueHolder.getInputEventAdapterService();
        List<String> inputEventAdapters = inputEventAdapterService.getInputEventAdapterTypes();
        if (inputEventAdapters == null) {
            return new String[0];
        } else {
            Collections.sort(inputEventAdapters);
            String[] types = new String[inputEventAdapters.size()];
            return inputEventAdapters.toArray(types);
        }
    }


    private DetailInputAdapterPropertyDto[] getPropertyConfigurations(Map<String, String> messageProperties, List<Property> propertyList) {
        if (propertyList != null && propertyList.size() > 0) {
            DetailInputAdapterPropertyDto[] detailInputAdapterPropertyDtoArray = new DetailInputAdapterPropertyDto[propertyList.size()];
            int index = 0;
            for (Property property : propertyList) {
                // create input event property
                String value = null;
                if (messageProperties != null) {
                    value = messageProperties.get(property.getPropertyName());
                }
                detailInputAdapterPropertyDtoArray[index] = new DetailInputAdapterPropertyDto(property.getPropertyName(),
                        value);
                // set input event property parameters
                detailInputAdapterPropertyDtoArray[index].setSecured(property.isSecured());
                detailInputAdapterPropertyDtoArray[index].setRequired(property.isRequired());
                detailInputAdapterPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                detailInputAdapterPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                detailInputAdapterPropertyDtoArray[index].setHint(property.getHint());
                detailInputAdapterPropertyDtoArray[index].setOptions(property.getOptions());
                index++;
            }
            return detailInputAdapterPropertyDtoArray;
        }
        return new DetailInputAdapterPropertyDto[0];
    }

    private boolean checkEventReceiverValidity(String eventReceiverName) throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        List<EventReceiverConfiguration> eventReceiverConfigurationList = null;

        eventReceiverConfigurationList = eventReceiverService.getAllActiveEventReceiverConfigurations();
        Iterator eventReceiverConfigurationIterator = eventReceiverConfigurationList.iterator();
        while (eventReceiverConfigurationIterator.hasNext()) {
            EventReceiverConfiguration eventReceiverConfiguration = (EventReceiverConfiguration) eventReceiverConfigurationIterator.next();
            if (eventReceiverConfiguration.getEventReceiverName().equalsIgnoreCase(eventReceiverName)) {
                return false;
            }
        }

        return true;
    }

    private void constructInputAdapterRelatedConfigs(String eventReceiverName, String eventAdapterType,
                                                     BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                     EventReceiverConfiguration eventReceiverConfiguration,
                                                     String messageFormat) {
        InputEventAdapterConfiguration inputEventAdapterConfiguration = new InputEventAdapterConfiguration();
        inputEventAdapterConfiguration.setName(eventReceiverName);
        inputEventAdapterConfiguration.setType(eventAdapterType);
        inputEventAdapterConfiguration.setMessageFormat(messageFormat);
        inputEventAdapterConfiguration.setProperties(new HashMap<String, String>());

        // add input message property configuration to the map
        if (inputPropertyConfiguration != null && inputPropertyConfiguration.length != 0) {

            for (BasicInputAdapterPropertyDto eventReceiverProperty : inputPropertyConfiguration) {
                if (!eventReceiverProperty.getValue().trim().equals("")) {
                    inputEventAdapterConfiguration.getProperties().put(eventReceiverProperty.getKey().trim(), eventReceiverProperty.getValue().trim());
                }
            }
        }

        eventReceiverConfiguration.setFromAdapterConfiguration(inputEventAdapterConfiguration);
    }

    public boolean isReceiverEditable(String eventReceiverName) {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        List<EventReceiverConfiguration> eventReceiverConfigurationList = null;
        boolean isEditable = false;
        eventReceiverConfigurationList = eventReceiverService.getAllActiveEventReceiverConfigurations();
        Iterator eventReceiverConfigurationIterator = eventReceiverConfigurationList.iterator();
        while (eventReceiverConfigurationIterator.hasNext()) {
            EventReceiverConfiguration eventReceiverConfiguration =
                    (EventReceiverConfiguration) eventReceiverConfigurationIterator.next();
            if (eventReceiverConfiguration.getEventReceiverName().equalsIgnoreCase(eventReceiverName)) {
                isEditable = eventReceiverConfiguration.isEditable();
            }
        }
        return isEditable;
    }

    public boolean isReceiverStatisticsEnabled(String eventReceiverName) {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        List<EventReceiverConfiguration> eventReceiverConfigurationList = null;
        boolean isStatisticsEnabled = false;
        eventReceiverConfigurationList = eventReceiverService.getAllActiveEventReceiverConfigurations();
        Iterator eventReceiverConfigurationIterator = eventReceiverConfigurationList.iterator();
        while (eventReceiverConfigurationIterator.hasNext()) {
            EventReceiverConfiguration eventReceiverConfiguration =
                    (EventReceiverConfiguration) eventReceiverConfigurationIterator.next();
            if (eventReceiverConfiguration.getEventReceiverName().equalsIgnoreCase(eventReceiverName)) {
                isStatisticsEnabled = eventReceiverConfiguration.isStatisticsEnabled();
            }
        }
        return isStatisticsEnabled;
    }

    public boolean isReceiverTraceEnabled(String eventReceiverName) {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        List<EventReceiverConfiguration> eventReceiverConfigurationList = null;
        boolean isTraceEnabled = false;
        eventReceiverConfigurationList = eventReceiverService.getAllActiveEventReceiverConfigurations();
        Iterator eventReceiverConfigurationIterator = eventReceiverConfigurationList.iterator();
        while (eventReceiverConfigurationIterator.hasNext()) {
            EventReceiverConfiguration eventReceiverConfiguration =
                    (EventReceiverConfiguration) eventReceiverConfigurationIterator.next();
            if (eventReceiverConfiguration.getEventReceiverName().equalsIgnoreCase(eventReceiverName)) {
                isTraceEnabled = eventReceiverConfiguration.isTraceEnabled();
            }
        }
        return isTraceEnabled;
    }

}