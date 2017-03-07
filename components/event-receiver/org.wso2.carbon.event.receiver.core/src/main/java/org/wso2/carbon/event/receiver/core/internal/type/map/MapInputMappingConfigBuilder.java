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
package org.wso2.carbon.event.receiver.core.internal.type.map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.config.InputMapping;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.config.mapping.MapInputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverValidationException;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConfigurationBuilder;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;


/**
 * This class is used to read the values of the event receiver configuration defined in XML configuration files
 */
public class MapInputMappingConfigBuilder {

    private static MapInputMappingConfigBuilder instance = new MapInputMappingConfigBuilder();

    private MapInputMappingConfigBuilder() {

    }

    public static MapInputMappingConfigBuilder getInstance() {
        return MapInputMappingConfigBuilder.instance;
    }

    @SuppressWarnings("unchecked")
    public static void validateMapEventMapping(OMElement omElement)
            throws EventReceiverConfigurationException {
        String customMappingEnabledAttribute = omElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED));
        if (customMappingEnabledAttribute == null || customMappingEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            int count = 0;
            Iterator<OMElement> mappingIterator = omElement.getChildElements();
            while (mappingIterator.hasNext()) {
                count++;
                OMElement childElement = mappingIterator.next();
                String childTag = childElement.getLocalName();
                if (!EventReceiverConstants.ER_ELEMENT_PROPERTY.equals(childTag)) {
                    throw new EventReceiverConfigurationException("Unsupported XML configuration element for Map Input Mapping : " + childTag);
                }
                if (childTag.equals(EventReceiverConstants.ER_ELEMENT_PROPERTY)) {
                    OMElement propertyFromElement = childElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM));
                    OMElement propertyToElement = childElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_TO));
                    if (propertyFromElement == null) {
                        throw new EventReceiverConfigurationException("An attribute mapping must provide a valid 'from' element");
                    }
                    if (propertyToElement == null) {
                        throw new EventReceiverConfigurationException("An attribute mapping must provide a valid 'to' element");
                    }
                    if (propertyToElement.getAttribute(new QName(EventReceiverConstants.ER_ATTR_NAME)) == null ||
                            propertyToElement.getAttribute(new QName(EventReceiverConstants.ER_ATTR_TYPE)) == null) {
                        throw new EventReceiverConfigurationException("An attribute mapping must provide name and type for its 'to' element.");
                    }
                }
            }

            if (count == 0) {
                throw new EventReceiverConfigurationException("There must be at least 1 attribute mapping with Custom Mapping enabled.");
            }
        }
    }

    private OMElement getPropertyOmElement(OMFactory factory,
                                           InputMappingAttribute inputMappingAttribute) {

        OMElement propertyOMElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_PROPERTY));

        OMElement fromElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_FROM));
        fromElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        fromElement.addAttribute(EventReceiverConstants.ER_ATTR_NAME, inputMappingAttribute.getFromElementKey(), null);

        OMElement toElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        toElement.addAttribute(EventReceiverConstants.ER_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventReceiverConstants.ER_ATTR_TYPE, EventReceiverConfigurationBuilder.getAttributeType(inputMappingAttribute.getToElementType()), null);

        if (inputMappingAttribute.getDefaultValue() != null && !inputMappingAttribute.getDefaultValue().isEmpty()) {
            toElement.addAttribute(EventReceiverConstants.ER_ATTR_DEFAULT_VALUE, inputMappingAttribute.getDefaultValue(), null);
        }
        propertyOMElement.addChild(fromElement);
        propertyOMElement.addChild(toElement);

        return propertyOMElement;

    }

    public InputMapping fromOM(
            OMElement mappingElement)
            throws EventReceiverValidationException, EventReceiverConfigurationException {
        MapInputMappingConfigBuilder.validateMapEventMapping(mappingElement);
        MapInputMapping mapInputMapping = new MapInputMapping();
        String customMappingEnabledAttribute = mappingElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED));
        if (customMappingEnabledAttribute == null || customMappingEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            mapInputMapping.setCustomMappingEnabled(true);
            Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_PROPERTY));
            int positionCount = 0;
            while (propertyIterator.hasNext()) {
                OMElement propertyOMElement = (OMElement) propertyIterator.next();
                InputMappingAttribute inputMappingAttribute = getInputMappingAttributeFromOM(propertyOMElement);
                inputMappingAttribute.setToStreamPosition(positionCount++);
                mapInputMapping.addInputMappingAttribute(inputMappingAttribute);
            }
        } else {
            mapInputMapping.setCustomMappingEnabled(false);
        }

        return mapInputMapping;
    }

    public OMElement inputMappingToOM(
            InputMapping outputMapping, OMFactory factory) {

        MapInputMapping mapInputMapping = (MapInputMapping) outputMapping;

        List<InputMappingAttribute> inputMappingAttributes = mapInputMapping.getInputMappingAttributes();

        OMElement mappingOMElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_TYPE, EventReceiverConstants.ER_MAP_MAPPING_TYPE, null);

        if (mapInputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED, EventReceiverConstants.ENABLE_CONST, null);
        } else {
            mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED, EventReceiverConstants.DISABLE_CONST, null);
        }
        if (inputMappingAttributes.size() > 0) {
            for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
                OMElement propertyOMElement = getPropertyOmElement(factory, inputMappingAttribute);
                propertyOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
                mappingOMElement.addChild(propertyOMElement);
            }
        }
        return mappingOMElement;
    }

    private InputMappingAttribute getInputMappingAttributeFromOM(OMElement omElement) {
        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM));
        OMElement propertyToElement = omElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_TO));

        String name = propertyFromElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
        String valueOf = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
        String attributeType = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TYPE));
        String defaultVal = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_DEFAULT_VALUE));
        AttributeType type = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attributeType.toLowerCase());

        if (valueOf == null) {
            valueOf = name;
        }

        InputMappingAttribute mappingAttribute = new InputMappingAttribute(name, valueOf, type);
        mappingAttribute.setDefaultValue(defaultVal);
        return mappingAttribute;
    }


}