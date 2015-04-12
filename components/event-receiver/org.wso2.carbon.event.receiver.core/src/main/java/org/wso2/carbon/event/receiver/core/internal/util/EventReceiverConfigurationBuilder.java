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
package org.wso2.carbon.event.receiver.core.internal.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.InputMapperFactory;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverStreamValidationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverValidationException;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class EventReceiverConfigurationBuilder {

    private static final Log log = LogFactory.getLog(EventReceiverConfigurationBuilder.class);

    public static OMElement eventReceiverConfigurationToOM(
            EventReceiverConfiguration eventReceiverConfiguration) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement eventReceiverConfigElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_ROOT_ELEMENT));
        eventReceiverConfigElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);

        eventReceiverConfigElement.addAttribute(EventReceiverConstants.ER_ATTR_NAME, eventReceiverConfiguration.getEventReceiverName(), null);
        if (eventReceiverConfiguration.isTraceEnabled()) {
            eventReceiverConfigElement.addAttribute(EventReceiverConstants.ER_ATTR_TRACE_ENABLED, EventReceiverConstants.ENABLE_CONST, null);
        } else {
            eventReceiverConfigElement.addAttribute(EventReceiverConstants.ER_ATTR_TRACE_ENABLED, EventReceiverConstants.DISABLE_CONST, null);
        }
        if (eventReceiverConfiguration.isStatisticsEnabled()) {
            eventReceiverConfigElement.addAttribute(EventReceiverConstants.ER_ATTR_STATISTICS_ENABLED, EventReceiverConstants.ENABLE_CONST, null);
        } else {
            eventReceiverConfigElement.addAttribute(EventReceiverConstants.ER_ATTR_STATISTICS_ENABLED, EventReceiverConstants.DISABLE_CONST, null);
        }

        //From properties
        OMElement fromOMElement = factory.createOMElement(new QName(
                EventReceiverConstants.ER_ELEMENT_FROM));
        fromOMElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        InputEventAdapterConfiguration inputEventAdapterConfiguration = eventReceiverConfiguration.getFromAdapterConfiguration();
        fromOMElement.addAttribute(EventReceiverConstants.ER_ATTR_TA_TYPE, inputEventAdapterConfiguration.getType(), null);
        Map<String, String> inputPropertyMap = inputEventAdapterConfiguration.getProperties();
        if (inputPropertyMap != null) {
            for (Map.Entry<String, String> propertyEntry : inputPropertyMap.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_PROPERTY));
                propertyElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
                propertyElement.addAttribute(EventReceiverConstants.ER_ATTR_NAME, propertyEntry.getKey(), null);
                propertyElement.setText(propertyEntry.getValue());
                fromOMElement.addChild(propertyElement);
            }
        }
        eventReceiverConfigElement.addChild(fromOMElement);

        OMElement mappingOMElement = EventReceiverServiceValueHolder.getMappingFactoryMap().get(eventReceiverConfiguration.getInputMapping().getMappingType()).constructOMFromInputMapping(eventReceiverConfiguration.getInputMapping(), factory);
        mappingOMElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        eventReceiverConfigElement.addChild(mappingOMElement);

        OMElement toOMElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_TO));
        toOMElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        toOMElement.addAttribute(EventReceiverConstants.ER_ATTR_STREAM_NAME, eventReceiverConfiguration.getToStreamName(), null);
        toOMElement.addAttribute(EventReceiverConstants.ER_ATTR_VERSION, eventReceiverConfiguration.getToStreamVersion(), null);

        eventReceiverConfigElement.addChild(toOMElement);
        try {
            String formattedXml = XmlFormatter.format(eventReceiverConfigElement.toString());
            eventReceiverConfigElement = AXIOMUtil.stringToOM(formattedXml);
        } catch (XMLStreamException e) {
            log.warn("Could not format OMElement properly." + eventReceiverConfigElement.toString());
        }

        return eventReceiverConfigElement;
    }

    public static EventReceiverConfiguration getEventReceiverConfiguration(
            OMElement eventReceiverConfigOMElement, String mappingType, boolean isEditable, int tenantId)
            throws EventReceiverConfigurationException {

        if (!eventReceiverConfigOMElement.getLocalName().equals(EventReceiverConstants.ER_ELEMENT_ROOT_ELEMENT)) {
            throw new EventReceiverConfigurationException("Root element is not an event receiver.");
        }
        String eventReceiverName = eventReceiverConfigOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));

        boolean traceEnabled = false;
        boolean statisticsEnabled = false;
        String traceEnabledAttribute = eventReceiverConfigOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TRACE_ENABLED));
        if (traceEnabledAttribute != null && traceEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            traceEnabled = true;
        }
        String statisticsEnabledAttribute = eventReceiverConfigOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_STATISTICS_ENABLED));
        if (statisticsEnabledAttribute != null && statisticsEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            statisticsEnabled = true;
        }

        EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

        OMElement fromElement = eventReceiverConfigOMElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM));
        OMElement mappingElement = eventReceiverConfigOMElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_MAPPING));
        OMElement toElement = eventReceiverConfigOMElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_TO));

        String fromEventAdapterType = fromElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TA_TYPE));

        if (!validateEventAdapter(fromEventAdapterType)) {
            throw new EventReceiverValidationException("Event Adapter with type: " + fromEventAdapterType + " does not exist", fromEventAdapterType);
        }

        InputEventAdapterConfiguration inputEventAdapterConfiguration = getInputEventAdapterConfiguration(fromEventAdapterType,eventReceiverName,mappingType);

        Iterator fromElementPropertyIterator = fromElement.getChildrenWithName(
                new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_PROPERTY)
        );
        

        while (fromElementPropertyIterator.hasNext()) {
            OMElement toElementProperty = (OMElement) fromElementPropertyIterator.next();
            String propertyName = toElementProperty.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
            String propertyValue = toElementProperty.getText();

            OMAttribute encryptedAttribute = toElementProperty.getAttribute(new QName(EventReceiverConstants.ER_ATTR_ENCRYPTED));
            if (encryptedAttribute != null) {
                if ("true".equals(encryptedAttribute.getAttributeValue())) {
                    try {
                        propertyValue = new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(propertyValue));
                    } catch (CryptoException e) {
                        log.error("Unable to decrypt the encrypted field: " + propertyName + " in adaptor: " + inputEventAdapterConfiguration.getName());
                        propertyValue = "";   // resetting the password if decryption is not possible.
                    }
                }
            }

            if (inputEventAdapterConfiguration.getProperties().containsKey(propertyName)) {
                inputEventAdapterConfiguration.getProperties().put(propertyName, propertyValue);
            } else {
                log.warn("To property " + propertyName + " with value " + propertyValue + " is dropped as its irrelevant of input adapter type:" + fromEventAdapterType);
            }
        }

        if (mappingType.equalsIgnoreCase(EventReceiverConstants.ER_WSO2EVENT_MAPPING_TYPE)) {
            if (!validateSupportedMapping(fromEventAdapterType, MessageType.WSO2EVENT)) {
                throw new EventReceiverConfigurationException("Wso2 Event Mapping is not supported by event adapter type " + fromEventAdapterType);
            }
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.ER_TEXT_MAPPING_TYPE)) {
            if (!validateSupportedMapping(fromEventAdapterType, MessageType.TEXT)) {
                throw new EventReceiverConfigurationException("Text Mapping is not supported by event adapter type " + fromEventAdapterType);
            }
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.ER_MAP_MAPPING_TYPE)) {
            if (!validateSupportedMapping(fromEventAdapterType, MessageType.MAP)) {
                throw new EventReceiverConfigurationException("Mapping for Map input is not supported by event adapter type " + fromEventAdapterType);
            }
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.ER_XML_MAPPING_TYPE)) {
            if (!validateSupportedMapping(fromEventAdapterType, MessageType.XML)) {
                throw new EventReceiverConfigurationException("XML Mapping is not supported by event adapter type " + fromEventAdapterType);
            }
            eventReceiverConfiguration = new EventReceiverConfiguration();
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.ER_JSON_MAPPING_TYPE)) {
            if (!validateSupportedMapping(fromEventAdapterType, MessageType.JSON)) {
                throw new EventReceiverConfigurationException("JSON Mapping is not supported by event adapter type " + fromEventAdapterType);
            }
        } else {
            String factoryClassName = getMappingTypeFactoryClass(mappingElement);
            if (factoryClassName == null) {
                throw new EventReceiverConfigurationException("Corresponding mappingType " + mappingType + " is not valid");
            }

            Class factoryClass;
            try {
                factoryClass = Class.forName(factoryClassName);
                InputMapperFactory inputMapperFactory = (InputMapperFactory) factoryClass.newInstance();
                EventReceiverServiceValueHolder.getMappingFactoryMap().putIfAbsent(mappingType, inputMapperFactory);
                eventReceiverConfiguration = new EventReceiverConfiguration();
            } catch (ClassNotFoundException e) {
                throw new EventReceiverConfigurationException("Class not found exception occurred ", e);
            } catch (InstantiationException e) {
                throw new EventReceiverConfigurationException("Instantiation exception occurred ", e);
            } catch (IllegalAccessException e) {
                throw new EventReceiverConfigurationException("Illegal exception occurred ", e);
            }
        }

        String toStreamName = toElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_STREAM_NAME));
        String toStreamVersion = toElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_VERSION));

        if (!validateStreamDetails(toStreamName, toStreamVersion, tenantId)) {
            throw new EventReceiverStreamValidationException("Stream " + toStreamName + ":" + toStreamVersion + " does not exist", toStreamName + ":" + toStreamVersion);
        }

        eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
        eventReceiverConfiguration.setTraceEnabled(traceEnabled);
        eventReceiverConfiguration.setStatisticsEnabled(statisticsEnabled);
        eventReceiverConfiguration.setToStreamName(toStreamName);
        eventReceiverConfiguration.setToStreamVersion(toStreamVersion);
        InputMapperFactory mapperFactory = EventReceiverServiceValueHolder.getMappingFactoryMap().get(mappingType);
        eventReceiverConfiguration.setInputMapping(mapperFactory.constructInputMappingFromOM(mappingElement));
        eventReceiverConfiguration.setFromAdapterConfiguration(inputEventAdapterConfiguration);
        eventReceiverConfiguration.setEditable(isEditable);
        return eventReceiverConfiguration;
    }


    public static InputEventAdapterConfiguration getInputEventAdapterConfiguration(
            String eventAdapterType, String receiverName, String messageFormat) {
        InputEventAdapterSchema schema = EventReceiverServiceValueHolder.getInputEventAdapterService().getInputEventAdapterSchema(eventAdapterType);
        InputEventAdapterConfiguration inputEventAdapterConfiguration = new InputEventAdapterConfiguration();
        inputEventAdapterConfiguration.setName(receiverName);
        inputEventAdapterConfiguration.setMessageFormat(messageFormat);
        inputEventAdapterConfiguration.setType(eventAdapterType);
        Map<String, String> staticProperties = new HashMap<String, String>();
        if (schema != null && schema.getPropertyList() != null) {
            for (Property property : schema.getPropertyList()) {
                staticProperties.put(property.getPropertyName(), property.getDefaultValue());
            }
        }
        inputEventAdapterConfiguration.setProperties(staticProperties);
        return inputEventAdapterConfiguration;
    }

    public static String getMappingTypeFactoryClass(OMElement omElement) {
        return omElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_FACTORY_CLASS));
    }

    private static boolean validateEventAdapter(String eventAdapterType) {

        InputEventAdapterService eventAdapterService = EventReceiverServiceValueHolder.getInputEventAdapterService();
        List<String> eventAdapterTypes = eventAdapterService.getInputEventAdapterTypes();

        if (eventAdapterTypes == null || eventAdapterTypes.size() == 0) {
            throw new EventReceiverValidationException("Event adapter with type: " + eventAdapterType + " does not exist", eventAdapterType);
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

    public static String getAttributeType(AttributeType attributeType) {
        Map<String, AttributeType> attributeMap = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP;
        for (Map.Entry<String, AttributeType> entry : attributeMap.entrySet()) {
            if (entry.getValue().equals(attributeType)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static boolean validateSupportedMapping(String eventAdapterType,
                                                   String messageType) {

        InputEventAdapterService eventAdapterService = EventReceiverServiceValueHolder.getInputEventAdapterService();
        InputEventAdapterSchema eventAdapterSchema = eventAdapterService.getInputEventAdapterSchema(eventAdapterType);

        if (eventAdapterSchema == null) {
            throw new EventReceiverValidationException("Event Adapter with type: " + eventAdapterType + " does not exist", eventAdapterType);
        }
        List<String> supportedInputMessageFormats = eventAdapterSchema.getSupportedMessageFormats();
        return supportedInputMessageFormats.contains(messageType);
    }


    private static boolean validateStreamDetails(String streamName, String streamVersion,
                                                 int tenantId)
            throws EventReceiverConfigurationException {

        EventStreamService eventStreamService = EventReceiverServiceValueHolder.getEventStreamService();
        try {
            StreamDefinition streamDefinition = eventStreamService.getStreamDefinition(streamName, streamVersion);
            if (streamDefinition != null) {
                return true;
            }
        } catch (EventStreamConfigurationException e) {
            throw new EventReceiverConfigurationException("Error while validating stream definition with store : " + e.getMessage(), e);
        }
        return false;

    }

}
