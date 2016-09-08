/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.publisher.core.internal.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterSchema;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.OutputMapperFactory;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherStreamValidationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherValidationException;
import org.wso2.carbon.event.publisher.core.internal.EventPublisher;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.event.publisher.core.internal.util.helper.EventPublisherConfigurationHelper;
import org.wso2.carbon.event.publisher.core.internal.util.helper.XmlFormatter;
import org.wso2.carbon.event.stream.core.EventStreamService;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventPublisherConfigurationBuilder {
    private static final Log log = LogFactory.getLog(EventPublisherConfigurationBuilder.class);

    public static OMElement eventPublisherConfigurationToOM(
            EventPublisherConfiguration eventPublisherConfiguration)
            throws EventPublisherConfigurationException {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement eventPublisherConfigElement = factory.createOMElement(new QName(
                EventPublisherConstants.EF_ELEMENT_ROOT_ELEMENT));
        eventPublisherConfigElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

        eventPublisherConfigElement.addAttribute(EventPublisherConstants.EF_ATTR_NAME, eventPublisherConfiguration.getEventPublisherName(), null);

        if (eventPublisherConfiguration.isStatisticsEnabled()) {
            eventPublisherConfigElement.addAttribute(EventPublisherConstants.EF_ATTR_STATISTICS_ENABLED, EventPublisherConstants.ENABLE_CONST, null);
        } else {
            eventPublisherConfigElement.addAttribute(EventPublisherConstants.EF_ATTR_STATISTICS_ENABLED, EventPublisherConstants.TM_VALUE_DISABLE, null);
        }

        if (eventPublisherConfiguration.isTracingEnabled()) {
            eventPublisherConfigElement.addAttribute(EventPublisherConstants.EF_ATTR_TRACE_ENABLED, EventPublisherConstants.ENABLE_CONST, null);
        } else {
            eventPublisherConfigElement.addAttribute(EventPublisherConstants.EF_ATTR_TRACE_ENABLED, EventPublisherConstants.TM_VALUE_DISABLE, null);
        }


        if (eventPublisherConfiguration.isProcessingEnabled()) {
            eventPublisherConfigElement.addAttribute(EventPublisherConstants.EF_ATTR_PROCESSING, EventPublisherConstants.ENABLE_CONST, null);
        } else {
            eventPublisherConfigElement.addAttribute(EventPublisherConstants.EF_ATTR_PROCESSING, EventPublisherConstants.TM_VALUE_DISABLE, null);
        }

        //From properties - Stream Name and version
        OMElement fromOMElement = factory.createOMElement(new QName(
                EventPublisherConstants.EF_ELEMENT_FROM));
        fromOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);
        fromOMElement.addAttribute(EventPublisherConstants.EF_ATTR_STREAM_NAME, eventPublisherConfiguration.getFromStreamName(), null);
        fromOMElement.addAttribute(EventPublisherConstants.EF_ATTR_VERSION, eventPublisherConfiguration.getFromStreamVersion(), null);
        eventPublisherConfigElement.addChild(fromOMElement);

        OMElement mappingOMElement = EventPublisherServiceValueHolder.getMappingFactoryMap().get(eventPublisherConfiguration.getOutputMapping().getMappingType()).constructOMFromOutputMapping(eventPublisherConfiguration.getOutputMapping(), factory);
        eventPublisherConfigElement.addChild(mappingOMElement);

        OMElement toOMElement = factory.createOMElement(new QName(
                EventPublisherConstants.EF_ELEMENT_TO));
        toOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);
        OutputEventAdapterConfiguration outputEventAdapterConfiguration = eventPublisherConfiguration.getToAdapterConfiguration();
        toOMElement.addAttribute(EventPublisherConstants.EF_ATTR_TA_TYPE, outputEventAdapterConfiguration.getType(), null);
        Map<String, String> properties = new HashMap<String, String>();
        if (outputEventAdapterConfiguration.getStaticProperties() != null) {
            properties.putAll(outputEventAdapterConfiguration.getStaticProperties());
        }
        if (eventPublisherConfiguration.getToAdapterDynamicProperties() != null) {
            properties.putAll(eventPublisherConfiguration.getToAdapterDynamicProperties());
        }
        for (Map.Entry<String, String> propertyEntry : properties.entrySet()) {
            if (propertyEntry.getValue() != null) {
                OMElement propertyElement = factory.createOMElement(new QName(
                        EventPublisherConstants.EF_ELE_PROPERTY));
                propertyElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);
                propertyElement.addAttribute(EventPublisherConstants.EF_ATTR_NAME, propertyEntry.getKey(), null);
                propertyElement.setText(propertyEntry.getValue());
                toOMElement.addChild(propertyElement);
            }
        }
        eventPublisherConfigElement.addChild(toOMElement);
        try {
            String formattedXml = XmlFormatter.format(eventPublisherConfigElement.toString());
            eventPublisherConfigElement = AXIOMUtil.stringToOM(formattedXml);
        } catch (XMLStreamException e) {
            log.warn("Could not format OMElement properly." + eventPublisherConfigElement.toString());
        }

        return eventPublisherConfigElement;
    }

    public static EventPublisherConfiguration getEventPublisherConfiguration(
            OMElement eventPublisherConfigOMElement, String mappingType, boolean isEditable, int tenantId)
            throws EventPublisherConfigurationException, EventPublisherValidationException {

        if (!eventPublisherConfigOMElement.getLocalName().equals(EventPublisherConstants.EF_ELEMENT_ROOT_ELEMENT)) {
            throw new EventPublisherConfigurationException("Root element is not an event publisher.");
        }

        String publisherName = eventPublisherConfigOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));

        boolean traceEnabled = false;
        boolean statisticsEnabled = false;
        boolean processingEnabled = true;

        String traceEnabledAttribute = eventPublisherConfigOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TRACE_ENABLED));
        if (traceEnabledAttribute != null && traceEnabledAttribute.equalsIgnoreCase(EventPublisherConstants.ENABLE_CONST)) {
            traceEnabled = true;
        }
        String statisticsEnabledAttribute = eventPublisherConfigOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_STATISTICS_ENABLED));
        if (statisticsEnabledAttribute != null && statisticsEnabledAttribute.equalsIgnoreCase(EventPublisherConstants.ENABLE_CONST)) {
            statisticsEnabled = true;
        }

        String processingEnabledAttribute = eventPublisherConfigOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_PROCESSING));
        if (processingEnabledAttribute != null && processingEnabledAttribute.equalsIgnoreCase(EventPublisherConstants.TM_VALUE_DISABLE)) {
            processingEnabled = false;
        }

        EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();

        OMElement fromElement = eventPublisherConfigOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_FROM));
        OMElement mappingElement = eventPublisherConfigOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_MAPPING));
        OMElement toElement = eventPublisherConfigOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_TO));

        String fromStreamName = fromElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_STREAM_NAME));
        String fromStreamVersion = fromElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_VERSION));

        if (!validateStreamDetails(fromStreamName, fromStreamVersion, tenantId)) {
            throw new EventPublisherStreamValidationException("Stream " + fromStreamName + ":" + fromStreamVersion + " does not exist", fromStreamName + ":" + fromStreamVersion);
        }

        String toEventAdapterType = toElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TA_TYPE));

        if (!validateEventAdapter(toEventAdapterType)) {
            throw new EventPublisherValidationException("Event Adapter with type: " + toEventAdapterType + " does not exist", toEventAdapterType);
        }

        Iterator toElementPropertyIterator = toElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_PROPERTY)
        );

        OutputEventAdapterConfiguration outputEventAdapterConfiguration = EventPublisherConfigurationHelper.getOutputEventAdapterConfiguration(toEventAdapterType, publisherName, mappingType);
        Map<String, String> dynamicProperties = EventPublisherConfigurationHelper.getDynamicProperties(toEventAdapterType);

        while (toElementPropertyIterator.hasNext()) {
            OMElement toElementProperty = (OMElement) toElementPropertyIterator.next();
            String propertyName = toElementProperty.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));
            String propertyValue = toElementProperty.getText();

            OMAttribute encryptedAttribute = toElementProperty.getAttribute(new QName(EventPublisherConstants.EF_ATTR_ENCRYPTED));
            if (encryptedAttribute != null) {
                if ("true".equals(encryptedAttribute.getAttributeValue())) {
                    try {
                        propertyValue = new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(propertyValue));
                    } catch (CryptoException e) {
                        log.error("Unable to decrypt the encrypted field: " + propertyName + " in adaptor: " + outputEventAdapterConfiguration.getName());
                        propertyValue = "";   // resetting the password if decryption is not possible.
                    }
                }
            }

            if (outputEventAdapterConfiguration.getStaticProperties().containsKey(propertyName)) {
                outputEventAdapterConfiguration.getStaticProperties().put(propertyName, propertyValue);
            } else if (dynamicProperties.containsKey(propertyName)) {
                dynamicProperties.put(propertyName, propertyValue);
            } else {
                log.warn("To property " + propertyName + " with value " + propertyValue + " is dropped as its irrelevant of output adapter type:" + toEventAdapterType);
            }
        }

        String toStreamName = "";
        String toStreamVersion = "";

        String customMappingEnabledAttribute = mappingElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING));
        if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_WSO2EVENT_MAPPING_TYPE) && customMappingEnabledAttribute != null && customMappingEnabledAttribute.equalsIgnoreCase(EventPublisherConstants.ENABLE_CONST)) {
            OMElement toOMElement = mappingElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_TO));
            toStreamName = toOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_STREAM_NAME));
            toStreamVersion = toOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_VERSION));
        }

        if (toStreamName == null || toStreamName.isEmpty() || toStreamVersion == null || toStreamVersion.isEmpty()) {
            outputEventAdapterConfiguration.setOutputStreamIdOfWso2eventMessageFormat(fromStreamName + EventPublisherConstants.STREAM_ID_SEPERATOR + fromStreamVersion);
        } else {
            outputEventAdapterConfiguration.setOutputStreamIdOfWso2eventMessageFormat(toStreamName + EventPublisherConstants.STREAM_ID_SEPERATOR + toStreamVersion);
        }

        if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_WSO2EVENT_MAPPING_TYPE)) {
            if (!validateSupportedMapping(toEventAdapterType, MessageType.WSO2EVENT)) {
                throw new EventPublisherConfigurationException("WSO2Event Mapping is not supported by event adapter type " + toEventAdapterType);
            }
        } else if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_TEXT_MAPPING_TYPE)) {
            if (!validateSupportedMapping(toEventAdapterType, MessageType.TEXT)) {
                throw new EventPublisherConfigurationException("Text Mapping is not supported by event adapter type " + toEventAdapterType);
            }
        } else if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_MAP_MAPPING_TYPE)) {
            if (!validateSupportedMapping(toEventAdapterType, MessageType.MAP)) {
                throw new EventPublisherConfigurationException("Map Mapping is not supported by event adapter type " + toEventAdapterType);
            }
        } else if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_XML_MAPPING_TYPE)) {
            if (!validateSupportedMapping(toEventAdapterType, MessageType.XML)) {
                throw new EventPublisherConfigurationException("XML Mapping is not supported by event adapter type " + toEventAdapterType);
            }
        } else if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_JSON_MAPPING_TYPE)) {
            if (!validateSupportedMapping(toEventAdapterType, MessageType.JSON)) {
                throw new EventPublisherConfigurationException("JSON Mapping is not supported by event adapter type " + toEventAdapterType);
            }
        } else {
            String factoryClassName = getMappingTypeFactoryClass(mappingElement);
            if (factoryClassName == null) {
                throw new EventPublisherConfigurationException("Corresponding mappingType " + mappingType + " is not valid");
            }

            Class factoryClass;
            try {
                factoryClass = Class.forName(factoryClassName);
                OutputMapperFactory outputMapperFactory = (OutputMapperFactory) factoryClass.newInstance();
                EventPublisherServiceValueHolder.getMappingFactoryMap().putIfAbsent(mappingType, outputMapperFactory);
            } catch (ClassNotFoundException e) {
                throw new EventPublisherConfigurationException("Class not found exception occurred ", e);
            } catch (InstantiationException e) {
                throw new EventPublisherConfigurationException("Instantiation exception occurred ", e);
            } catch (IllegalAccessException e) {
                throw new EventPublisherConfigurationException("Illegal exception occurred ", e);
            }
        }

        eventPublisherConfiguration.setEventPublisherName(publisherName);
        eventPublisherConfiguration.setStatisticsEnabled(statisticsEnabled);
        eventPublisherConfiguration.setTraceEnabled(traceEnabled);
        eventPublisherConfiguration.setFromStreamName(fromStreamName);
        eventPublisherConfiguration.setFromStreamVersion(fromStreamVersion);
        eventPublisherConfiguration.setOutputMapping(EventPublisherServiceValueHolder.getMappingFactoryMap().get(mappingType).constructOutputMapping(mappingElement));
        eventPublisherConfiguration.setToAdapterConfiguration(outputEventAdapterConfiguration);
        eventPublisherConfiguration.setToAdapterDynamicProperties(dynamicProperties);
        eventPublisherConfiguration.setEditable(isEditable);
        eventPublisherConfiguration.setProcessEnabled(processingEnabled);
        return eventPublisherConfiguration;

    }

    public static String getMappingTypeFactoryClass(OMElement omElement) {
        return omElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_FACTORY_CLASS));
    }

    private static boolean validateStreamDetails(String streamName, String streamVersion,
                                                 int tenantId)
            throws EventPublisherConfigurationException {

        EventStreamService eventStreamService = EventPublisherServiceValueHolder.getEventStreamService();
        try {
            StreamDefinition streamDefinition = eventStreamService.getStreamDefinition(streamName, streamVersion);
            if (streamDefinition != null) {
                return true;
            }
        } catch (Throwable e) {
            log.error("Error while retrieving stream definition from Event Stream Service : " + e.getMessage(), e);
        }
        return false;

    }

    private static boolean validateEventAdapter(String eventAdapterType) {

        OutputEventAdapterService eventAdapterService = EventPublisherServiceValueHolder.getOutputEventAdapterService();
        List<String> eventAdapterTypes = eventAdapterService.getOutputEventAdapterTypes();

        if (eventAdapterTypes == null || eventAdapterTypes.size() == 0) {
            throw new EventPublisherValidationException("Event adapter with type: " + eventAdapterType + " does not exist", eventAdapterType);
        }

        Iterator<String> eventAdaIteratorTypeIterator = eventAdapterTypes.iterator();
        for (; eventAdaIteratorTypeIterator.hasNext(); ) {
            String adapterType = eventAdaIteratorTypeIterator.next();
            if (adapterType.equals(eventAdapterType)) {
                return true;
            }
        }

        return false;
    }

    private static boolean validateSupportedMapping(String eventAdapterType,
                                                    String messageType) {

        OutputEventAdapterService eventAdapterService = EventPublisherServiceValueHolder.getOutputEventAdapterService();
        OutputEventAdapterSchema eventAdapterSchema = eventAdapterService.getOutputEventAdapterSchema(eventAdapterType);

        if (eventAdapterSchema == null) {
            throw new EventPublisherValidationException("Event Adapter with type: " + eventAdapterType + " does not exist", eventAdapterType);
        }
        List<String> supportedOutputMessageFormats = eventAdapterSchema.getSupportedMessageFormats();
        return supportedOutputMessageFormats.contains(messageType);

    }


}
