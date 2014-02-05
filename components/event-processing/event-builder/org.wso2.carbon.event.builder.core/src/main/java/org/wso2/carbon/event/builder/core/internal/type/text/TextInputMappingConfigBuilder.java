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

package org.wso2.carbon.event.builder.core.internal.type.text;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.core.config.InputMapping;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class is used to read the values of the event builder configuration defined in XML configuration files
 */
public class TextInputMappingConfigBuilder {

    private static TextInputMappingConfigBuilder instance = new TextInputMappingConfigBuilder();

    private TextInputMappingConfigBuilder() {

    }

    public static TextInputMappingConfigBuilder getInstance() {
        return TextInputMappingConfigBuilder.instance;
    }

    @SuppressWarnings("unchecked")
    public static void validateTextMapping(OMElement omElement) throws EventBuilderConfigurationException {
        List<String> supportedChildTags = new ArrayList<String>();
        supportedChildTags.add(EventBuilderConstants.EB_ELEMENT_PROPERTY);

        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            count++;
            OMElement childElement = mappingIterator.next();
            String childTag = childElement.getLocalName();
            if (!supportedChildTags.contains(childTag)) {
                throw new EventBuilderConfigurationException("Unsupported XML configuration element for Text Input Mapping : " + childTag);
            }
            if (childTag.equals(EventBuilderConstants.EB_ELEMENT_PROPERTY)) {
                OMElement propertyFromElement = childElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_FROM));
                OMElement propertyToElement = childElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_TO));
                if (propertyFromElement == null) {
                    throw new EventBuilderConfigurationException("An attribute mapping must provide a valid 'from' element");
                }
                if (propertyToElement == null) {
                    throw new EventBuilderConfigurationException("An attribute mapping must provide a valid 'to' element");
                }
                if (propertyToElement.getAttribute(new QName(EventBuilderConstants.EB_ATTR_NAME)) == null ||
                        propertyToElement.getAttribute(new QName(EventBuilderConstants.EB_ATTR_TYPE)) == null) {
                    throw new EventBuilderConfigurationException("An attribute mapping must provide name and type for its 'to' element.");
                }
            }
        }

        if (count == 0) {
            throw new EventBuilderConfigurationException("There must be at least 1 attribute mapping with Custom Mapping enabled.");
        }
    }

    public InputMapping fromOM(
            OMElement mappingElement)
            throws EventBuilderValidationException, EventBuilderConfigurationException {
        TextInputMappingConfigBuilder.validateTextMapping(mappingElement);
        TextInputMapping textInputMapping = new TextInputMapping();
//        String customMappingEnabledAttribute = mappingElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_CUSTOM_MAPPING_ENABLED));
//        if (customMappingEnabledAttribute != null && customMappingEnabledAttribute.equalsIgnoreCase(EventBuilderConstants.ENABLE_CONST)) {
        textInputMapping.setCustomMappingEnabled(true);
        Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_PROPERTY));
        List<InputMappingAttribute> inputMappingAttributeList = new ArrayList<InputMappingAttribute>();
        while (propertyIterator.hasNext()) {
            OMElement propertyOMElement = (OMElement) propertyIterator.next();
            inputMappingAttributeList.addAll(getInputMappingAttributesFromOM(propertyOMElement));
        }
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributeList) {
            textInputMapping.addInputMappingAttribute(inputMappingAttribute);
        }
//        }

        return textInputMapping;
    }

    public OMElement inputMappingToOM(
            InputMapping inputMapping, OMFactory factory) {

        TextInputMapping textInputMapping = (TextInputMapping) inputMapping;

        OMElement mappingOMElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        mappingOMElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, EventBuilderConstants.EB_TEXT_MAPPING_TYPE, null);

        if (textInputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventBuilderConstants.EB_ATTR_CUSTOM_MAPPING_ENABLED, EventBuilderConstants.ENABLE_CONST, null);
        } else {
            mappingOMElement.addAttribute(EventBuilderConstants.EB_ATTR_CUSTOM_MAPPING_ENABLED, EventBuilderConstants.DISABLE_CONST, null);
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
        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_FROM));
        Iterator toElementIterator = omElement.getChildrenWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_TO));

        List<InputMappingAttribute> inputMappingAttributeList = new ArrayList<InputMappingAttribute>();
        while (toElementIterator.hasNext()) {
            OMElement propertyToElement = (OMElement) toElementIterator.next();
            String regex = propertyFromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_REGEX));
            String outputPropertyName = propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
            String attributeType = propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TYPE));
            AttributeType outputPropertyType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attributeType.toLowerCase());
            String defaultValue = propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_DEFAULT_VALUE));

            InputMappingAttribute inputMappingAttribute = new InputMappingAttribute(regex, outputPropertyName, outputPropertyType);
            inputMappingAttribute.setDefaultValue(defaultValue);
            inputMappingAttributeList.add(inputMappingAttribute);
        }
        return inputMappingAttributeList;
    }

    private OMElement getPropertyOmElement(OMFactory factory,
                                           InputMappingAttribute inputMappingAttribute) {
        OMElement propertyOmElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_PROPERTY));

        OMElement fromElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_FROM));
        fromElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        fromElement.addAttribute(EventBuilderConstants.EB_ATTR_REGEX, inputMappingAttribute.getFromElementKey(), null);

        OMElement toElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, EventBuilderConfigBuilder.getAttributeType(inputMappingAttribute.getToElementType()), null);
        if (inputMappingAttribute.getDefaultValue() != null && !inputMappingAttribute.getDefaultValue().isEmpty()) {
            toElement.addAttribute(EventBuilderConstants.EB_ATTR_DEFAULT_VALUE, inputMappingAttribute.getDefaultValue(), null);
        }

        propertyOmElement.addChild(fromElement);
        propertyOmElement.addChild(toElement);

        return propertyOmElement;
    }

    private void addAnotherToProperty(OMFactory factory, OMElement propertyOmElement,
                                      InputMappingAttribute inputMappingAttribute) {
        OMElement toElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, EventBuilderConfigBuilder.getAttributeType(inputMappingAttribute.getToElementType()), null);
        if (inputMappingAttribute.getDefaultValue() != null && !inputMappingAttribute.getDefaultValue().isEmpty()) {
            toElement.addAttribute(EventBuilderConstants.EB_ATTR_DEFAULT_VALUE, inputMappingAttribute.getDefaultValue(), null);
        }

        propertyOmElement.addChild(toElement);
    }

}




