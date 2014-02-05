/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.event.formatter.core.internal.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.config.OutputMapperFactory;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterValidationException;
import org.wso2.carbon.event.formatter.core.internal.config.ToPropertyConfiguration;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.formatter.core.internal.util.helper.EventFormatterConfigurationHelper;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorInfo;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;
import org.wso2.carbon.event.processor.api.send.EventProducer;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FormatterConfigurationBuilder {

    private static boolean validateStreamDetails(String streamName, String streamVersion,
                                                 int tenantId)
            throws EventFormatterConfigurationException {
        List<EventProducer> eventProducerList = EventFormatterServiceValueHolder.getEventProducerList();

        if (eventProducerList == null || eventProducerList.size() == 0) {
            throw new EventFormatterValidationException("Event producers are not loaded", streamName + ":" + streamVersion);
        }

        EventStreamService eventStreamService = EventFormatterServiceValueHolder.getEventStreamService();
        try {
            StreamDefinition streamDefinition = eventStreamService.getStreamDefinitionFromStore(streamName, streamVersion, tenantId);
            if(streamDefinition !=null){
                return true;
            }
        } catch (EventStreamConfigurationException e) {
            throw new EventFormatterConfigurationException("Error while validating stream definition with store : "+e.getMessage(),e);
        }
        return false;

    }

    private static boolean validateEventAdaptor(String eventAdaptorName,
                                                String eventAdaptorType, int tenantId) {

        OutputEventAdaptorManagerService eventAdaptorManagerService = EventFormatterServiceValueHolder.getOutputEventAdaptorManagerService();
        List<OutputEventAdaptorInfo> eventAdaptorInfoList = eventAdaptorManagerService.getOutputEventAdaptorInfo(tenantId);

        if (eventAdaptorInfoList == null || eventAdaptorInfoList.size() == 0) {
            throw new EventFormatterValidationException("Corresponding Event adaptor " + eventAdaptorType + " module not loaded", eventAdaptorName);
        }

        Iterator<OutputEventAdaptorInfo> eventAdaIteratorInfoIterator = eventAdaptorInfoList.iterator();
        for (; eventAdaIteratorInfoIterator.hasNext(); ) {
            OutputEventAdaptorInfo eventAdaptorInfo = eventAdaIteratorInfoIterator.next();
            if (eventAdaptorInfo.getEventAdaptorName().equals(eventAdaptorName) && eventAdaptorInfo.getEventAdaptorType().equals(eventAdaptorType)) {
                return true;
            }
        }

        return false;
    }

    private static boolean validateSupportedMapping(String eventAdaptorName,
                                                    String eventAdaptorType,
                                                    String messageType) {

        OutputEventAdaptorService eventAdaptorService = EventFormatterServiceValueHolder.getOutputEventAdaptorService();
        OutputEventAdaptorDto eventAdaptorDto = eventAdaptorService.getEventAdaptorDto(eventAdaptorType);

        if (eventAdaptorDto == null) {
            throw new EventFormatterValidationException("Event Adaptor " + eventAdaptorType + " is not loaded", eventAdaptorName);
        }
        List<String> supportedOutputMessageTypes = eventAdaptorDto.getSupportedMessageTypes();
        return supportedOutputMessageTypes.contains(messageType);

    }


    public static EventFormatterConfiguration getEventFormatterConfiguration(
            OMElement eventFormatterConfigOMElement, int tenantId, String mappingType)
            throws EventFormatterConfigurationException, EventFormatterValidationException {

        EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration();

        OMElement fromElement = eventFormatterConfigOMElement.getFirstChildWithName(new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_FROM_PROPERTY));
        OMElement mappingElement = eventFormatterConfigOMElement.getFirstChildWithName(new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_MAPPING_PROPERTY));
        OMElement toElement = eventFormatterConfigOMElement.getFirstChildWithName(new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_TO_PROPERTY));

        String fromStreamName = fromElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_STREAM_NAME));
        String fromStreamVersion = fromElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_VERSION));

        String toEventAdaptorName = toElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_TA_NAME));
        String toEventAdaptorType = toElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_TA_TYPE));

        if (!validateEventAdaptor(toEventAdaptorName, toEventAdaptorType, tenantId)) {
            throw new EventFormatterValidationException("There is no any Event Adaptor with this name " + toEventAdaptorName + " which is a " + toEventAdaptorType, toEventAdaptorName);
        }

        if (!validateStreamDetails(fromStreamName, fromStreamVersion, tenantId)) {
            throw new EventFormatterValidationException("There is no any stream called " + fromStreamName + " with the version " + fromStreamVersion, fromStreamName + ":" + fromStreamVersion);
        }

        OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration = EventFormatterConfigurationHelper.getOutputEventAdaptorMessageConfiguration(toEventAdaptorType);
        ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
        toPropertyConfiguration.setEventAdaptorName(toEventAdaptorName);
        toPropertyConfiguration.setEventAdaptorType(toEventAdaptorType);

        Iterator toElementPropertyIterator = toElement.getChildrenWithName(
                new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_PROPERTY)
        );

        while (toElementPropertyIterator.hasNext()) {
            OMElement toElementProperty = (OMElement) toElementPropertyIterator.next();
            String propertyName = toElementProperty.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_NAME));
            String propertyValue = toElementProperty.getText();
            outputEventAdaptorMessageConfiguration.addOutputMessageProperty(propertyName, propertyValue);
        }

        toPropertyConfiguration.setOutputEventAdaptorMessageConfiguration(outputEventAdaptorMessageConfiguration);


        if (mappingType.equalsIgnoreCase(EventFormatterConstants.EF_WSO2EVENT_MAPPING_TYPE)) {
            if (!validateSupportedMapping(toEventAdaptorName, toEventAdaptorType, MessageType.WSO2EVENT)) {
                throw new EventFormatterConfigurationException("WSO2Event Mapping is not supported by event adaptor type " + toEventAdaptorType);
            }
        } else if (mappingType.equalsIgnoreCase(EventFormatterConstants.EF_TEXT_MAPPING_TYPE)) {
            if (!validateSupportedMapping(toEventAdaptorName, toEventAdaptorType, MessageType.TEXT)) {
                throw new EventFormatterConfigurationException("Text Mapping is not supported by event adaptor type " + toEventAdaptorType);
            }
        } else if (mappingType.equalsIgnoreCase(EventFormatterConstants.EF_MAP_MAPPING_TYPE)) {
            if (!validateSupportedMapping(toEventAdaptorName, toEventAdaptorType, MessageType.MAP)) {
                throw new EventFormatterConfigurationException("Map Mapping is not supported by event adaptor type " + toEventAdaptorType);
            }
        } else if (mappingType.equalsIgnoreCase(EventFormatterConstants.EF_XML_MAPPING_TYPE)) {
            if (!validateSupportedMapping(toEventAdaptorName, toEventAdaptorType, MessageType.XML)) {
                throw new EventFormatterConfigurationException("XML Mapping is not supported by event adaptor type " + toEventAdaptorType);
            }
        } else if (mappingType.equalsIgnoreCase(EventFormatterConstants.EF_JSON_MAPPING_TYPE)) {
            if (!validateSupportedMapping(toEventAdaptorName, toEventAdaptorType, MessageType.JSON)) {
                throw new EventFormatterConfigurationException("JSON Mapping is not supported by event adaptor type " + toEventAdaptorType);
            }
        } else {
            String factoryClassName = getMappingTypeFactoryClass(mappingElement);
            if (factoryClassName == null) {
                throw new EventFormatterConfigurationException("Corresponding mappingType " + mappingType + " is not valid");
            }

            Class factoryClass;
            try {
                factoryClass = Class.forName(factoryClassName);
                OutputMapperFactory outputMapperFactory = (OutputMapperFactory) factoryClass.newInstance();
                EventFormatterServiceValueHolder.getMappingFactoryMap().putIfAbsent(mappingType, outputMapperFactory);
            } catch (ClassNotFoundException e) {
                throw new EventFormatterConfigurationException("Class not found exception occurred ", e);
            } catch (InstantiationException e) {
                throw new EventFormatterConfigurationException("Instantiation exception occurred ", e);
            } catch (IllegalAccessException e) {
                throw new EventFormatterConfigurationException("Illegal exception occurred ", e);
            }
        }


        eventFormatterConfiguration.setEventFormatterName(eventFormatterConfigOMElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_NAME)));

        if (eventFormatterConfigOMElement.getAttributeValue(new QName(EventFormatterConstants.TM_ATTR_STATISTICS)) != null && eventFormatterConfigOMElement.getAttributeValue(new QName(EventFormatterConstants.TM_ATTR_STATISTICS)).equals(EventFormatterConstants.TM_VALUE_ENABLE)) {
            eventFormatterConfiguration.setEnableStatistics(true);
        } else if (eventFormatterConfigOMElement.getAttributeValue(new QName(EventFormatterConstants.TM_ATTR_STATISTICS)) != null && eventFormatterConfigOMElement.getAttributeValue(new QName(EventFormatterConstants.TM_ATTR_STATISTICS)).equals(EventFormatterConstants.TM_VALUE_DISABLE)) {
            eventFormatterConfiguration.setEnableStatistics(false);
        }

        if (eventFormatterConfigOMElement.getAttributeValue(new QName(EventFormatterConstants.TM_ATTR_TRACING)) != null && eventFormatterConfigOMElement.getAttributeValue(new QName(EventFormatterConstants.TM_ATTR_TRACING)).equals(EventFormatterConstants.TM_VALUE_ENABLE)) {
            eventFormatterConfiguration.setEnableTracing(true);
        } else if (eventFormatterConfigOMElement.getAttributeValue(new QName(EventFormatterConstants.TM_ATTR_TRACING)) != null && eventFormatterConfigOMElement.getAttributeValue(new QName(EventFormatterConstants.TM_ATTR_TRACING)).equals(EventFormatterConstants.TM_VALUE_DISABLE)) {
            eventFormatterConfiguration.setEnableTracing(false);
        }

        eventFormatterConfiguration.setFromStreamName(fromStreamName);
        eventFormatterConfiguration.setFromStreamVersion(fromStreamVersion);
        eventFormatterConfiguration.setOutputMapping(EventFormatterServiceValueHolder.getMappingFactoryMap().get(mappingType).constructOutputMapping(mappingElement));
        eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);
        return eventFormatterConfiguration;

    }

    public static OMElement eventFormatterConfigurationToOM(
            EventFormatterConfiguration eventFormatterConfiguration) throws EventFormatterConfigurationException {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement eventFormatterConfigElement = factory.createOMElement(new QName(
                EventFormatterConstants.EF_ELE_ROOT_ELEMENT));
        eventFormatterConfigElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);

        eventFormatterConfigElement.addAttribute(EventFormatterConstants.EF_ATTR_NAME, eventFormatterConfiguration.getEventFormatterName(), null);

        if (eventFormatterConfiguration.isEnableStatistics()) {
            eventFormatterConfigElement.addAttribute(EventFormatterConstants.TM_ATTR_STATISTICS, EventFormatterConstants.TM_VALUE_ENABLE,
                                                     null);
        } else if (!eventFormatterConfiguration.isEnableStatistics()) {
            eventFormatterConfigElement.addAttribute(EventFormatterConstants.TM_ATTR_STATISTICS, EventFormatterConstants.TM_VALUE_DISABLE,
                                                     null);
        }

        if (eventFormatterConfiguration.isEnableTracing()) {
            eventFormatterConfigElement.addAttribute(EventFormatterConstants.TM_ATTR_TRACING, EventFormatterConstants.TM_VALUE_ENABLE,
                                                     null);
        } else if (!eventFormatterConfiguration.isEnableTracing()) {
            eventFormatterConfigElement.addAttribute(EventFormatterConstants.TM_ATTR_TRACING, EventFormatterConstants.TM_VALUE_DISABLE,
                                                     null);
        }

        //From properties - Stream Name and version
        OMElement fromPropertyElement = factory.createOMElement(new QName(
                EventFormatterConstants.EF_ELE_FROM_PROPERTY));
        fromPropertyElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);
        fromPropertyElement.addAttribute(EventFormatterConstants.EF_ATTR_STREAM_NAME, eventFormatterConfiguration.getFromStreamName(), null);
        fromPropertyElement.addAttribute(EventFormatterConstants.EF_ATTR_VERSION, eventFormatterConfiguration.getFromStreamVersion(), null);
        eventFormatterConfigElement.addChild(fromPropertyElement);

        OMElement mappingOMElement = EventFormatterServiceValueHolder.getMappingFactoryMap().get(eventFormatterConfiguration.getOutputMapping().getMappingType()).constructOutputMappingOM(eventFormatterConfiguration.getOutputMapping(), factory);

        eventFormatterConfigElement.addChild(mappingOMElement);


        OMElement toOMElement = factory.createOMElement(new QName(
                EventFormatterConstants.EF_ELE_TO_PROPERTY));
        toOMElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);

        ToPropertyConfiguration toPropertyConfiguration = eventFormatterConfiguration.getToPropertyConfiguration();
        toOMElement.addAttribute(EventFormatterConstants.EF_ATTR_TA_NAME, toPropertyConfiguration.getEventAdaptorName(), null);
        toOMElement.addAttribute(EventFormatterConstants.EF_ATTR_TA_TYPE, toPropertyConfiguration.getEventAdaptorType(), null);

        OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration = toPropertyConfiguration.getOutputEventAdaptorMessageConfiguration();

        if (outputEventAdaptorMessageConfiguration != null) {
            Map<String, String> wso2EventOutputPropertyMap = outputEventAdaptorMessageConfiguration.getOutputMessageProperties();
            for (Map.Entry<String, String> propertyEntry : wso2EventOutputPropertyMap.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(
                        EventFormatterConstants.EF_ELE_PROPERTY));
                propertyElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);
                propertyElement.addAttribute(EventFormatterConstants.EF_ATTR_NAME, propertyEntry.getKey(), null);
                propertyElement.setText(propertyEntry.getValue());
                toOMElement.addChild(propertyElement);
            }
        }
        eventFormatterConfigElement.addChild(toOMElement);
        return eventFormatterConfigElement;
    }

    public static String getMappingTypeFactoryClass(OMElement omElement) {
        return omElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_FACTORY_CLASS));
    }


}
