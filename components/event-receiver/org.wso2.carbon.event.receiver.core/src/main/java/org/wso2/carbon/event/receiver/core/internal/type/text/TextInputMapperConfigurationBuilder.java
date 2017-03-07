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
package org.wso2.carbon.event.receiver.core.internal.type.text;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.config.InputMapping;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.config.mapping.TextInputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverValidationException;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConfigurationBuilder;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class is used to read the values of the event receiver configuration defined in XML configuration files
 */
public class TextInputMapperConfigurationBuilder {

    private static TextInputMapperConfigurationBuilder instance = new TextInputMapperConfigurationBuilder();

    private TextInputMapperConfigurationBuilder() {

    }

    public static TextInputMapperConfigurationBuilder getInstance() {
        return TextInputMapperConfigurationBuilder.instance;
    }

    @SuppressWarnings("unchecked")
    public static void validateTextMapping(OMElement omElement)
            throws EventReceiverConfigurationException {

        List<String> supportedChildTags = new ArrayList<String>();
        supportedChildTags.add(EventReceiverConstants.ER_ELEMENT_PROPERTY);
        String customMappingEnabledAttribute = omElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED));
        if (customMappingEnabledAttribute == null || customMappingEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            int count = 0;
            Iterator<OMElement> mappingIterator = omElement.getChildElements();
            while (mappingIterator.hasNext()) {
                count++;
                OMElement childElement = mappingIterator.next();
                String childTag = childElement.getLocalName();
                if (!supportedChildTags.contains(childTag)) {
                    throw new EventReceiverConfigurationException("Unsupported XML configuration element for Text Input Mapping : " + childTag);
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

    public InputMapping fromOM(
            OMElement mappingElement)
            throws EventReceiverValidationException, EventReceiverConfigurationException {
        TextInputMapperConfigurationBuilder.validateTextMapping(mappingElement);
        TextInputMapping textInputMapping = new TextInputMapping();
        String customMappingEnabledAttribute = mappingElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED));
        if (customMappingEnabledAttribute == null || customMappingEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            textInputMapping.setCustomMappingEnabled(true);
            Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_PROPERTY));
            List<InputMappingAttribute> inputMappingAttributeList = new ArrayList<InputMappingAttribute>();
            while (propertyIterator.hasNext()) {
                OMElement propertyOMElement = (OMElement) propertyIterator.next();
                inputMappingAttributeList.addAll(getInputMappingAttributesFromOM(propertyOMElement));
            }
            for (InputMappingAttribute inputMappingAttribute : inputMappingAttributeList) {
                textInputMapping.addInputMappingAttribute(inputMappingAttribute);
            }
        }else{
            textInputMapping.setCustomMappingEnabled(false);
        }

        return textInputMapping;
    }

    public OMElement inputMappingToOM(
            InputMapping inputMapping, OMFactory factory) {

        TextInputMapping textInputMapping = (TextInputMapping) inputMapping;

        OMElement mappingOMElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_TYPE, EventReceiverConstants.ER_TEXT_MAPPING_TYPE, null);

        if (textInputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED, EventReceiverConstants.ENABLE_CONST, null);
        } else {
            mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED, EventReceiverConstants.DISABLE_CONST, null);
        }
        List<InputMappingAttribute> inputMappingAttributes = textInputMapping.getInputMappingAttributes();
        InputMappingAttribute prevInputMappingAttribute = null;
        OMElement propertyOMElement = null;
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
            if (prevInputMappingAttribute != null && prevInputMappingAttribute.getFromElementKey().equals(inputMappingAttribute.getFromElementKey())) {
                addAnotherToProperty(factory, propertyOMElement, inputMappingAttribute);
            } else {
                propertyOMElement = getPropertyOmElement(factory, inputMappingAttribute);
                propertyOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
                mappingOMElement.addChild(propertyOMElement);
            }
            prevInputMappingAttribute = inputMappingAttribute;
        }

        return mappingOMElement;
    }

    private List<InputMappingAttribute> getInputMappingAttributesFromOM(OMElement omElement) {
        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM));
        Iterator toElementIterator = omElement.getChildrenWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_TO));

        List<InputMappingAttribute> inputMappingAttributeList = new ArrayList<InputMappingAttribute>();
        while (toElementIterator.hasNext()) {
            OMElement propertyToElement = (OMElement) toElementIterator.next();
            String regex = propertyFromElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_REGEX));
            String outputPropertyName = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
            String attributeType = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TYPE));
            AttributeType outputPropertyType = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attributeType.toLowerCase());
            String defaultValue = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_DEFAULT_VALUE));

            InputMappingAttribute inputMappingAttribute = new InputMappingAttribute(regex, outputPropertyName, outputPropertyType);
            inputMappingAttribute.setDefaultValue(defaultValue);
            inputMappingAttributeList.add(inputMappingAttribute);
        }
        return inputMappingAttributeList;
    }

    private OMElement getPropertyOmElement(OMFactory factory,
                                           InputMappingAttribute inputMappingAttribute) {
        OMElement propertyOmElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_PROPERTY));

        OMElement fromElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_FROM));
        fromElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        fromElement.addAttribute(EventReceiverConstants.ER_ATTR_REGEX, inputMappingAttribute.getFromElementKey(), null);

        OMElement toElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        toElement.addAttribute(EventReceiverConstants.ER_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventReceiverConstants.ER_ATTR_TYPE, EventReceiverConfigurationBuilder.getAttributeType(inputMappingAttribute.getToElementType()), null);
        if (inputMappingAttribute.getDefaultValue() != null && !inputMappingAttribute.getDefaultValue().isEmpty()) {
            toElement.addAttribute(EventReceiverConstants.ER_ATTR_DEFAULT_VALUE, inputMappingAttribute.getDefaultValue(), null);
        }

        propertyOmElement.addChild(fromElement);
        propertyOmElement.addChild(toElement);

        return propertyOmElement;
    }

    private void addAnotherToProperty(OMFactory factory, OMElement propertyOmElement,
                                      InputMappingAttribute inputMappingAttribute) {
        OMElement toElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        toElement.addAttribute(EventReceiverConstants.ER_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventReceiverConstants.ER_ATTR_TYPE, EventReceiverConfigurationBuilder.getAttributeType(inputMappingAttribute.getToElementType()), null);
        if (inputMappingAttribute.getDefaultValue() != null && !inputMappingAttribute.getDefaultValue().isEmpty()) {
            toElement.addAttribute(EventReceiverConstants.ER_ATTR_DEFAULT_VALUE, inputMappingAttribute.getDefaultValue(), null);
        }

        propertyOmElement.addChild(toElement);
    }

}




