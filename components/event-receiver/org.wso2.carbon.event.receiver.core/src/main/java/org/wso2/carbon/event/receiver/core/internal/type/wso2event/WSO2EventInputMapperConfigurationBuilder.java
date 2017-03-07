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
package org.wso2.carbon.event.receiver.core.internal.type.wso2event;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.config.InputMapping;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.config.mapping.WSO2EventInputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConfigurationBuilder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class is used to read the values of the event receiver configuration defined in XML configuration files.
 * This class extends has methods to read syntax specific to Wso2EventInputMapping
 */
public class WSO2EventInputMapperConfigurationBuilder {

    private static WSO2EventInputMapperConfigurationBuilder instance = new WSO2EventInputMapperConfigurationBuilder();

    private WSO2EventInputMapperConfigurationBuilder() {

    }

    public static WSO2EventInputMapperConfigurationBuilder getInstance() {
        return WSO2EventInputMapperConfigurationBuilder.instance;
    }

    public InputMapping fromOM(
            OMElement mappingElement)
            throws EventReceiverConfigurationException {

        WSO2EventInputMapperConfigurationBuilder.validateWso2EventMapping(mappingElement);
        WSO2EventInputMapping wso2EventInputMapping = new WSO2EventInputMapping();
        String customMappingEnabledAttribute = mappingElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED));
        if (customMappingEnabledAttribute == null || customMappingEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            wso2EventInputMapping.setCustomMappingEnabled(true);
            Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_PROPERTY));
            while (propertyIterator.hasNext()) {
                OMElement propertyOMElement = (OMElement) propertyIterator.next();
                InputMappingAttribute inputMappingAttribute = getInputMappingAttributeFromOM(propertyOMElement);
                wso2EventInputMapping.addInputMappingAttribute(inputMappingAttribute);
            }
            OMElement fromOMElement = mappingElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM));
            wso2EventInputMapping.setFromEventName(fromOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_STREAM_NAME)));
            wso2EventInputMapping.setFromEventVersion(fromOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_VERSION)));

            List<InputMappingAttribute> sortedInputMappingAttributes = EventReceiverUtil.sortInputMappingAttributes(wso2EventInputMapping.getInputMappingAttributes());
            int streamPosition = 0;
            for (InputMappingAttribute inputMappingAttribute : sortedInputMappingAttributes) {
                inputMappingAttribute.setToStreamPosition(streamPosition++);
            }
            wso2EventInputMapping.setInputMappingAttributes(sortedInputMappingAttributes);
        } else {
            wso2EventInputMapping.setCustomMappingEnabled(false);
        }
        String arbitraryMappingEnabledAttribute = mappingElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_ARBITRARY_MAPS_ENABLED));
        if (arbitraryMappingEnabledAttribute != null && arbitraryMappingEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            wso2EventInputMapping.setArbitraryMapsEnabled(true);
        }

        return wso2EventInputMapping;
    }

    private InputMappingAttribute getInputMappingAttributeFromOM(OMElement omElement) {

        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM));
        OMElement propertyToElement = omElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_TO));

        String name = propertyFromElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
        String dataType = propertyFromElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_DATA_TYPE));
        String valueOf = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
        String attributeType = propertyToElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TYPE));
        AttributeType type = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attributeType.toLowerCase());

        if (valueOf == null) {
            valueOf = name;
        }

        return new InputMappingAttribute(name, valueOf, type, dataType);
    }

    @SuppressWarnings("unchecked")
    public static void validateWso2EventMapping(OMElement omElement)
            throws EventReceiverConfigurationException {
        List<String> supportedChildTags = new ArrayList<String>();
        supportedChildTags.add(EventReceiverConstants.ER_ELEMENT_PROPERTY);
        supportedChildTags.add(EventReceiverConstants.ER_ELEMENT_FROM);

        String customMappingEnabledAttribute = omElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED));
        if (customMappingEnabledAttribute == null || customMappingEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {

            int count = 0;
            Iterator<OMElement> mappingIterator = omElement.getChildElements();
            while (mappingIterator.hasNext()) {
                count++;
                OMElement childElement = mappingIterator.next();
                String childTag = childElement.getLocalName();
                if (!supportedChildTags.contains(childTag)) {
                    throw new EventReceiverConfigurationException("Unsupported XML configuration element for WSO2Event Input Mapping : " + childTag);
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

    public OMElement inputMappingToOM(
            InputMapping inputMapping, OMFactory factory) {

        WSO2EventInputMapping wso2EventInputMapping = (WSO2EventInputMapping) inputMapping;

        List<InputMappingAttribute> inputMappingAttributes = wso2EventInputMapping.getInputMappingAttributes();

        OMElement mappingOMElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_TYPE, EventReceiverConstants.ER_WSO2EVENT_MAPPING_TYPE, null);

        if (wso2EventInputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED, EventReceiverConstants.ENABLE_CONST, null);
        } else {
            mappingOMElement.addAttribute(EventReceiverConstants.ER_ATTR_CUSTOM_MAPPING_ENABLED, EventReceiverConstants.DISABLE_CONST, null);
        }
        if (inputMappingAttributes.size() > 0) {
            OMElement fromOMElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_FROM));
            fromOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
            fromOMElement.addAttribute(EventReceiverConstants.ER_ATTR_STREAM_NAME, wso2EventInputMapping.getFromEventName(), null);
            fromOMElement.addAttribute(EventReceiverConstants.ER_ATTR_VERSION, wso2EventInputMapping.getFromEventVersion(), null);
            mappingOMElement.addChild(fromOMElement);

            for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
                OMElement propertyOMElement = getPropertyOmElement(factory, inputMappingAttribute);
                propertyOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
                mappingOMElement.addChild(propertyOMElement);
            }
        }

        return mappingOMElement;
    }

    protected OMElement getPropertyOmElement(OMFactory factory,
                                             InputMappingAttribute inputMappingAttribute) {
        OMElement propertyOMElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_PROPERTY));

        OMElement fromElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_FROM));
        fromElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        fromElement.addAttribute(EventReceiverConstants.ER_ATTR_NAME, inputMappingAttribute.getFromElementKey(), null);
        fromElement.addAttribute(EventReceiverConstants.ER_ATTR_DATA_TYPE, inputMappingAttribute.getFromElementType(), null);

        OMElement toElement = factory.createOMElement(new QName(EventReceiverConstants.ER_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventReceiverConstants.ER_CONF_NS);
        toElement.addAttribute(EventReceiverConstants.ER_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventReceiverConstants.ER_ATTR_TYPE, EventReceiverConfigurationBuilder.getAttributeType(inputMappingAttribute.getToElementType()), null);

        propertyOMElement.addChild(fromElement);
        propertyOMElement.addChild(toElement);

        return propertyOMElement;
    }

}




