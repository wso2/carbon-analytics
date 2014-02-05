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

package org.wso2.carbon.event.builder.core.internal.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapperFactory;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
import org.wso2.carbon.event.builder.core.internal.config.InputStreamConfiguration;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.helper.ConfigurationValidator;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Iterator;
import java.util.Map;


public class EventBuilderConfigBuilder {

    private static final Log log = LogFactory.getLog(EventBuilderConfigBuilder.class);

    public static OMElement eventBuilderConfigurationToOM(
            EventBuilderConfiguration eventBuilderConfiguration) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement eventBuilderConfigElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_ROOT_ELEMENT));
        eventBuilderConfigElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);

        eventBuilderConfigElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, eventBuilderConfiguration.getEventBuilderName(), null);
        if (eventBuilderConfiguration.isTraceEnabled()) {
            eventBuilderConfigElement.addAttribute(EventBuilderConstants.EB_ATTR_TRACE_ENABLED, EventBuilderConstants.ENABLE_CONST, null);
        } else {
            eventBuilderConfigElement.addAttribute(EventBuilderConstants.EB_ATTR_TRACE_ENABLED, EventBuilderConstants.DISABLE_CONST, null);
        }
        if (eventBuilderConfiguration.isStatisticsEnabled()) {
            eventBuilderConfigElement.addAttribute(EventBuilderConstants.EB_ATTR_STATISTICS_ENABLED, EventBuilderConstants.ENABLE_CONST, null);
        } else {
            eventBuilderConfigElement.addAttribute(EventBuilderConstants.EB_ATTR_STATISTICS_ENABLED, EventBuilderConstants.DISABLE_CONST, null);
        }

        //From properties - Stream Name and version
        InputStreamConfiguration inputStreamConfiguration = eventBuilderConfiguration.getInputStreamConfiguration();
        OMElement fromOMElement = factory.createOMElement(EventBuilderConstants.EB_ELEMENT_FROM, eventBuilderConfigElement.getDefaultNamespace());
        fromOMElement.addAttribute(EventBuilderConstants.EB_ATTR_TA_NAME, inputStreamConfiguration.getInputEventAdaptorName(), null);
        fromOMElement.addAttribute(EventBuilderConstants.EB_ATTR_TA_TYPE, inputStreamConfiguration.getInputEventAdaptorType(), null);

        InputEventAdaptorMessageConfiguration InputEventAdaptorMessageConfiguration = inputStreamConfiguration.getInputEventAdaptorMessageConfiguration();
        if (InputEventAdaptorMessageConfiguration != null) {
            Map<String, String> wso2EventInputPropertyMap = InputEventAdaptorMessageConfiguration.getInputMessageProperties();
            for (Map.Entry<String, String> propertyEntry : wso2EventInputPropertyMap.entrySet()) {
                OMElement propertyElement = factory.createOMElement(EventBuilderConstants.EB_ELEMENT_PROPERTY, fromOMElement.getDefaultNamespace());
                propertyElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, propertyEntry.getKey(), null);
                propertyElement.setText(propertyEntry.getValue());
                fromOMElement.addChild(propertyElement);
            }
        }
        eventBuilderConfigElement.addChild(fromOMElement);

        String mappingType = eventBuilderConfiguration.getInputMapping().getMappingType();
        InputMapperFactory mapperFactory = EventBuilderServiceValueHolder.getMappingFactoryMap().get(mappingType);
        OMElement mappingOMElement = mapperFactory.constructOMFromInputMapping(eventBuilderConfiguration.getInputMapping(), factory);
        mappingOMElement.setNamespace(eventBuilderConfigElement.getDefaultNamespace());
        eventBuilderConfigElement.addChild(mappingOMElement);

        OMElement toOMElement = factory.createOMElement(EventBuilderConstants.EB_ELEMENT_TO, eventBuilderConfigElement.getDefaultNamespace());
        toOMElement.addAttribute(EventBuilderConstants.EB_ATTR_STREAM_NAME, eventBuilderConfiguration.getToStreamName(), null);
        toOMElement.addAttribute(EventBuilderConstants.EB_ATTR_VERSION, eventBuilderConfiguration.getToStreamVersion(), null);

        eventBuilderConfigElement.addChild(toOMElement);
        try {
            String formattedXml = XmlFormatter.format(eventBuilderConfigElement.toString());
            eventBuilderConfigElement = AXIOMUtil.stringToOM(formattedXml);
        } catch (XMLStreamException e) {
            log.warn("Could not format OMElement properly." + eventBuilderConfigElement.toString());
        }

        return eventBuilderConfigElement;
    }

    public static EventBuilderConfiguration getEventBuilderConfiguration(
            OMElement eventBuilderConfigOMElement, String mappingType, int tenantId)
            throws EventBuilderConfigurationException {

        if (!eventBuilderConfigOMElement.getLocalName().equals(EventBuilderConstants.EB_ELEMENT_ROOT_ELEMENT)) {
            throw new EventBuilderConfigurationException("Root element is not an event builder.");
        }

        String eventBuilderName = eventBuilderConfigOMElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
        boolean traceEnabled = false;
        boolean statisticsEnabled = false;
        String traceEnabledAttribute = eventBuilderConfigOMElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TRACE_ENABLED));
        if (traceEnabledAttribute != null && traceEnabledAttribute.equalsIgnoreCase(EventBuilderConstants.ENABLE_CONST)) {
            traceEnabled = true;
        }
        String statisticsEnabledAttribute = eventBuilderConfigOMElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_STATISTICS_ENABLED));
        if (statisticsEnabledAttribute != null && statisticsEnabledAttribute.equalsIgnoreCase(EventBuilderConstants.ENABLE_CONST)) {
            statisticsEnabled = true;
        }

        OMElement fromElement = eventBuilderConfigOMElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_FROM));
        OMElement mappingElement = eventBuilderConfigOMElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_MAPPING));
        OMElement toElement = eventBuilderConfigOMElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_TO));

        String fromEventAdaptorName = fromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TA_NAME));
        String fromEventAdaptorType = fromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TA_TYPE));

        if (!ConfigurationValidator.isInputEventAdaptorActive(fromEventAdaptorName, fromEventAdaptorType, tenantId)) {
            throw new EventBuilderValidationException("Could not validate the input event adaptor configuration " + fromEventAdaptorName + " which is a " + fromEventAdaptorType, fromEventAdaptorName);
        }

        InputEventAdaptorMessageConfiguration inputEventMessageConfiguration = EventBuilderConfigHelper.getInputEventMessageConfiguration(fromEventAdaptorType);
        InputStreamConfiguration inputStreamConfiguration = new InputStreamConfiguration();
        inputStreamConfiguration.setInputEventAdaptorName(fromEventAdaptorName);
        inputStreamConfiguration.setInputEventAdaptorType(fromEventAdaptorType);

        Iterator fromElementPropertyIterator = fromElement.getChildrenWithName(
                new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_PROPERTY));
        while (fromElementPropertyIterator.hasNext()) {
            OMElement fromElementProperty = (OMElement) fromElementPropertyIterator.next();
            String propertyName = fromElementProperty.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
            String propertyValue = fromElementProperty.getText();
            if (inputEventMessageConfiguration.getInputMessageProperties().containsKey(propertyName)) {
                inputEventMessageConfiguration.addInputMessageProperty(propertyName, propertyValue);
            }
        }

        inputStreamConfiguration.setInputEventAdaptorMessageConfiguration(inputEventMessageConfiguration);

        String toStreamName = toElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_STREAM_NAME));
        String toStreamVersion = toElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_VERSION));

        EventBuilderConfiguration eventBuilderConfiguration;

        if (mappingType.equalsIgnoreCase(EventBuilderConstants.EB_WSO2EVENT_MAPPING_TYPE)) {
            if (!ConfigurationValidator.validateSupportedMapping(fromEventAdaptorType, MessageType.WSO2EVENT)) {
                throw new EventBuilderConfigurationException("Wso2 Event Mapping is not supported by event adaptor type " + fromEventAdaptorType);
            }
            eventBuilderConfiguration = new EventBuilderConfiguration();
        } else if (mappingType.equalsIgnoreCase(EventBuilderConstants.EB_TEXT_MAPPING_TYPE)) {
            if (!ConfigurationValidator.validateSupportedMapping(fromEventAdaptorType, MessageType.TEXT)) {
                throw new EventBuilderConfigurationException("Text Mapping is not supported by event adaptor type " + fromEventAdaptorType);
            }
            eventBuilderConfiguration = new EventBuilderConfiguration();
        } else if (mappingType.equalsIgnoreCase(EventBuilderConstants.EB_MAP_MAPPING_TYPE)) {
            if (!ConfigurationValidator.validateSupportedMapping(fromEventAdaptorType, MessageType.MAP)) {
                throw new EventBuilderConfigurationException("Mapping for Map input is not supported by event adaptor type " + fromEventAdaptorType);
            }
            eventBuilderConfiguration = new EventBuilderConfiguration();
        } else if (mappingType.equalsIgnoreCase(EventBuilderConstants.EB_XML_MAPPING_TYPE)) {
            if (!ConfigurationValidator.validateSupportedMapping(fromEventAdaptorType, MessageType.XML)) {
                throw new EventBuilderConfigurationException("XML Mapping is not supported by event adaptor type " + fromEventAdaptorType);
            }
            eventBuilderConfiguration = new EventBuilderConfiguration();
        } else if (mappingType.equalsIgnoreCase(EventBuilderConstants.EB_JSON_MAPPING_TYPE)) {
            if (!ConfigurationValidator.validateSupportedMapping(fromEventAdaptorType, MessageType.JSON)) {
                throw new EventBuilderConfigurationException("JSON Mapping is not supported by event adaptor type " + fromEventAdaptorType);
            }
            eventBuilderConfiguration = new EventBuilderConfiguration();
        } else {
            String factoryClassName = getMappingTypeFactoryClass(mappingElement);
            if (factoryClassName == null) {
                throw new EventBuilderConfigurationException("Corresponding mappingType " + mappingType + " is not valid");
            }

            Class factoryClass;
            try {
                factoryClass = Class.forName(factoryClassName);
                InputMapperFactory inputMapperFactory = (InputMapperFactory) factoryClass.newInstance();
                EventBuilderServiceValueHolder.getMappingFactoryMap().putIfAbsent(mappingType, inputMapperFactory);
                eventBuilderConfiguration = new EventBuilderConfiguration();
            } catch (ClassNotFoundException e) {
                throw new EventBuilderConfigurationException("Class not found exception occurred ", e);
            } catch (InstantiationException e) {
                throw new EventBuilderConfigurationException("Instantiation exception occurred ", e);
            } catch (IllegalAccessException e) {
                throw new EventBuilderConfigurationException("Illegal exception occurred ", e);
            }
        }

        eventBuilderConfiguration.setEventBuilderName(eventBuilderName);
        eventBuilderConfiguration.setTraceEnabled(traceEnabled);
        eventBuilderConfiguration.setStatisticsEnabled(statisticsEnabled);
        eventBuilderConfiguration.setToStreamName(toStreamName);
        eventBuilderConfiguration.setToStreamVersion(toStreamVersion);
        InputMapperFactory mapperFactory = EventBuilderServiceValueHolder.getMappingFactoryMap().get(mappingType);
        eventBuilderConfiguration.setInputMapping(mapperFactory.constructInputMappingFromOM(mappingElement));
        eventBuilderConfiguration.setInputStreamConfiguration(inputStreamConfiguration);
        return eventBuilderConfiguration;
    }

    public static String getMappingTypeFactoryClass(OMElement omElement) {
        return omElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_FACTORY_CLASS));
    }

    public static String getAttributeType(AttributeType attributeType) {
        Map<String, AttributeType> attributeMap = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP;
        for (Map.Entry<String, AttributeType> entry : attributeMap.entrySet()) {
            if (entry.getValue().equals(attributeType)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
