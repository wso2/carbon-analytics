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
package org.wso2.carbon.event.receiver.core.internal.type.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.config.InputMapping;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.config.mapping.XMLInputMapping;
import org.wso2.carbon.event.receiver.core.config.mapping.XPathDefinition;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConfigurationBuilder;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class is used to read the values of the event receiver configuration defined in XML configuration files
 */
public class XMLInputMapperConfigrationBuilder {

    private static XMLInputMapperConfigrationBuilder instance = new XMLInputMapperConfigrationBuilder();

    private XMLInputMapperConfigrationBuilder() {

    }

    public static XMLInputMapperConfigrationBuilder getInstance() {
        return XMLInputMapperConfigrationBuilder.instance;
    }

    public OMElement inputMappingToOM(
            InputMapping inputMapping, OMFactory factory) {

        XMLInputMapping xmlInputMapping = (XMLInputMapping) inputMapping;
        List<XPathDefinition> xPathDefinitions = xmlInputMapping.getXPathDefinitions();
        OMElement mappingOMElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        if (xmlInputMapping.getParentSelectorXpath() != null && !xmlInputMapping.getParentSelectorXpath().isEmpty()) {
            mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_PARENT_XPATH, xmlInputMapping.getParentSelectorXpath(), null);
        }
        mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_TYPE, EventReceiverConstants.ER_XML_MAPPING_TYPE, null);

        if (xmlInputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED, EventReceiverConstants.ENABLE_CONST, null);
        } else {
            mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED, EventReceiverConstants.DISABLE_CONST, null);
        }
        if (xPathDefinitions != null) {
            for (XPathDefinition xPathDefinition : xPathDefinitions) {
                OMElement xpathDefOMElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_XPATH_DEFINITION));
                xpathDefOMElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
                xpathDefOMElement.addAttribute(EventReceiverConstants.ER_ATTR_PREFIX, xPathDefinition.getPrefix(), null);
                xpathDefOMElement.addAttribute(EventReceiverConstants.ER_ATTR_NAMESPACE, xPathDefinition.getNamespaceUri(), null);
                mappingOMElement.addChild(xpathDefOMElement);
            }
        }

        for (InputMappingAttribute inputMappingAttribute : xmlInputMapping.getInputMappingAttributes()) {
            OMElement propertyOMElement = getPropertyOmElement(factory, inputMappingAttribute);
            propertyOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
            mappingOMElement.addChild(propertyOMElement);
        }

        return mappingOMElement;
    }

    private InputMappingAttribute getInputMappingAttributeFromOM(OMElement omElement) {
        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM));
        OMElement propertyToElement = omElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_TO));

        String xpath = propertyFromElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_XPATH));
        String outputPropertyName = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
        String attributeType = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TYPE));
        AttributeType outputPropertyType = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attributeType.toLowerCase());
        String defaultValue = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_DEFAULT_VALUE));

        InputMappingAttribute inputMappingAttribute = new InputMappingAttribute(xpath, outputPropertyName, outputPropertyType);
        inputMappingAttribute.setDefaultValue(defaultValue);

        return inputMappingAttribute;
    }

    private OMElement getPropertyOmElement(OMFactory factory,
                                           InputMappingAttribute inputMappingAttribute) {
        OMElement propertyOmElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_PROPERTY));

        OMElement fromElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_FROM));
        fromElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        fromElement.addAttribute(EventReceiverConstants.ER_ATTR_XPATH, inputMappingAttribute.getFromElementKey(), null);

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

    public InputMapping fromOM(
            OMElement mappingElement)
            throws EventReceiverConfigurationException {

        XMLInputMapperConfigrationBuilder.validateXMLEventMapping(mappingElement);
        XMLInputMapping xmlInputMapping = new XMLInputMapping();
        String customMappingEnabledAttribute = mappingElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED));
        if (customMappingEnabledAttribute == null || customMappingEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            xmlInputMapping.setCustomMappingEnabled(true);
            String parentSelectorXpath = mappingElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_PARENT_XPATH));
            if (parentSelectorXpath != null && !parentSelectorXpath.isEmpty()) {
                xmlInputMapping.setParentSelectorXpath(parentSelectorXpath);
            }

            Iterator xpathDefIterator = mappingElement.getChildrenWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_XPATH_DEFINITION));
            List<XPathDefinition> xPathDefinitions = new ArrayList<XPathDefinition>();
            while (xpathDefIterator.hasNext()) {
                OMElement xpathDefElement = (OMElement) xpathDefIterator.next();
                if (xpathDefElement != null) {
                    String prefix = xpathDefElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_PREFIX));
                    String namespace = xpathDefElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAMESPACE));
                    xPathDefinitions.add(new XPathDefinition(prefix, namespace));
                }
            }
            xmlInputMapping.setXPathDefinitions(xPathDefinitions);

            Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_PROPERTY));
            while (propertyIterator.hasNext()) {
                OMElement propertyOMElement = (OMElement) propertyIterator.next();
                InputMappingAttribute inputMappingAttribute = getInputMappingAttributeFromOM(propertyOMElement);
                xmlInputMapping.addInputMappingAttribute(inputMappingAttribute);
            }
        } else {
            xmlInputMapping.setCustomMappingEnabled(false);
        }

        return xmlInputMapping;
    }

    @SuppressWarnings("unchecked")
    public static void validateXMLEventMapping(OMElement omElement)
            throws EventReceiverConfigurationException {

        String customMappingEnabledAttribute = omElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED));
        if (customMappingEnabledAttribute == null || customMappingEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            List<String> supportedChildTags = new ArrayList<String>();
            supportedChildTags.add(EventReceiverConstants.ER_ELEMENT_PROPERTY);
            supportedChildTags.add(EventReceiverConstants.ER_ELEMENT_XPATH_DEFINITION);

            int propertyCount = 0;
            Iterator<OMElement> mappingIterator = omElement.getChildElements();
            while (mappingIterator.hasNext()) {
                OMElement childElement = mappingIterator.next();
                String childTag = childElement.getLocalName();
                if (!supportedChildTags.contains(childTag)) {
                    throw new EventReceiverConfigurationException("Unsupported XML configuration element for XML Input Mapping : " + childTag);
                }
                if (childTag.equals(EventReceiverConstants.ER_ELEMENT_PROPERTY)) {
                    propertyCount++;
                    OMElement propertyFromElement = childElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM));
                    OMElement propertyToElement = childElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_TO));
                    if (propertyFromElement == null) {
                        throw new EventReceiverConfigurationException("An attribute mapping must provide a valid 'from' element");
                    }
                    if (propertyFromElement.getAttribute(new QName(EventReceiverConstants.ER_ATTR_XPATH)) == null) {
                        throw new EventReceiverConfigurationException("XML Mapping must contain an XPath expression to map from");
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

            if (propertyCount == 0) {
                throw new EventReceiverConfigurationException("No Mapping properties in configuration");
            }
        }
    }


}




