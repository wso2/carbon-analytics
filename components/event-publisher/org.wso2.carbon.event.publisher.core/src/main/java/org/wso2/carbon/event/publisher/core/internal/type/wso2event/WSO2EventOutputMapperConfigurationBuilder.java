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
package org.wso2.carbon.event.publisher.core.internal.type.wso2event;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.publisher.core.config.EventOutputProperty;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.OutputMapping;
import org.wso2.carbon.event.publisher.core.config.mapping.WSO2EventOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherValidationException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class is used to read the values of the event builder configuration defined in XML configuration files
 */
public class WSO2EventOutputMapperConfigurationBuilder {

    private WSO2EventOutputMapperConfigurationBuilder() {

    }

    public static OutputMapping fromOM(
            OMElement mappingElement)
            throws EventPublisherValidationException, EventPublisherConfigurationException {

        WSO2EventOutputMapping wso2EventOutputMapping = new WSO2EventOutputMapping();

        String customMappingEnabled = mappingElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING));
        if (customMappingEnabled == null || (customMappingEnabled.equals(EventPublisherConstants.ENABLE_CONST))) {
            wso2EventOutputMapping.setCustomMappingEnabled(true);
            if (!validateWso2EventMapping(mappingElement)) {
                throw new EventPublisherConfigurationException("WS02Event Mapping is not valid, check the output mapping");
            }
            wso2EventOutputMapping.setCustomMappingEnabled(true);

            OMElement metaMappingElement = mappingElement.getFirstChildWithName(
                    new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_TO_METADATA_PROPERTY));

            if (metaMappingElement != null) {
                Iterator metaPropertyIterator = metaMappingElement.getChildrenWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_PROPERTY));
                while (metaPropertyIterator.hasNext()) {
                    OMElement propertyOMElement = (OMElement) metaPropertyIterator.next();

                    EventOutputProperty eventOutputProperty = getWSO2EventOutputPropertyFromOM(propertyOMElement);
                    wso2EventOutputMapping.addMetaWSO2EventOutputPropertyConfiguration(eventOutputProperty);

                }
            }


            OMElement correlationMappingElement = mappingElement.getFirstChildWithName(
                    new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_TO_CORRELATION_PROPERTY));

            if (correlationMappingElement != null) {
                Iterator correlationPropertyIterator = correlationMappingElement.getChildrenWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_PROPERTY));
                while (correlationPropertyIterator.hasNext()) {
                    OMElement propertyOMElement = (OMElement) correlationPropertyIterator.next();

                    EventOutputProperty eventOutputProperty = getWSO2EventOutputPropertyFromOM(propertyOMElement);
                    wso2EventOutputMapping.addCorrelationWSO2EventOutputPropertyConfiguration(eventOutputProperty);

                }
            }

            OMElement payloadMappingElement = mappingElement.getFirstChildWithName(
                    new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_TO_PAYLOAD_PROPERTY));
            if (payloadMappingElement != null) {
                Iterator payloadPropertyIterator = payloadMappingElement.getChildrenWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_PROPERTY));
                while (payloadPropertyIterator.hasNext()) {
                    OMElement propertyOMElement = (OMElement) payloadPropertyIterator.next();

                    EventOutputProperty eventOutputProperty = getWSO2EventOutputPropertyFromOM(propertyOMElement);
                    wso2EventOutputMapping.addPayloadWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                }
            }
        } else {
            wso2EventOutputMapping.setCustomMappingEnabled(false);
        }

        return wso2EventOutputMapping;
    }

    private static EventOutputProperty getWSO2EventOutputPropertyFromOM(OMElement omElement) {

        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_FROM));
        OMElement propertyToElement = omElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_TO));

        String name = propertyToElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));
        String valueOf = propertyFromElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));
        AttributeType type = EventPublisherConstants.STRING_ATTRIBUTE_TYPE_MAP.get(propertyToElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TYPE)));

        return new EventOutputProperty(name, valueOf, type);

    }


    public static boolean validateWso2EventMapping(OMElement omElement) {

        List<String> supportedChildTags = new ArrayList<String>();
        supportedChildTags.add(EventPublisherConstants.EF_ELE_TO_METADATA_PROPERTY);
        supportedChildTags.add(EventPublisherConstants.EF_ELE_TO_CORRELATION_PROPERTY);
        supportedChildTags.add(EventPublisherConstants.EF_ELE_TO_PAYLOAD_PROPERTY);

        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            OMElement childElement = mappingIterator.next();
            String childTag = childElement.getLocalName();
            if (!supportedChildTags.contains(childTag)) {
                return false;
            }
            count++;
        }

        return count != 0;

    }


    public static OMElement outputMappingToOM(
            OutputMapping outputMapping, OMFactory factory) {

        WSO2EventOutputMapping wso2EventOutputMapping = (WSO2EventOutputMapping) outputMapping;

        List<EventOutputProperty> metaWSO2EventPropertyConfiguration = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
        List<EventOutputProperty> correlationWSO2EventPropertyConfiguration = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
        List<EventOutputProperty> payloadWSO2EventPropertyConfiguration = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

        OMElement mappingOMElement = factory.createOMElement(new QName(
                EventPublisherConstants.EF_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

        mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_TYPE, EventPublisherConstants.EF_WSO2EVENT_MAPPING_TYPE, null);

        if (wso2EventOutputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING, EventPublisherConstants.ENABLE_CONST, null);


            if (metaWSO2EventPropertyConfiguration.size() > 0) {
                OMElement metaOMElement = factory.createOMElement(new QName(EventPublisherConstants.EF_ELE_TO_METADATA_PROPERTY));
                metaOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

                for (EventOutputProperty eventOutputProperty : metaWSO2EventPropertyConfiguration) {
                    metaOMElement.addChild(getPropertyOmElement(factory, eventOutputProperty));
                }
                mappingOMElement.addChild(metaOMElement);
            }

            if (correlationWSO2EventPropertyConfiguration.size() > 0) {
                OMElement correlationOMElement = factory.createOMElement(new QName(EventPublisherConstants.EF_ELE_TO_CORRELATION_PROPERTY));
                correlationOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

                for (EventOutputProperty eventOutputProperty : correlationWSO2EventPropertyConfiguration) {
                    correlationOMElement.addChild(getPropertyOmElement(factory, eventOutputProperty));
                }
                mappingOMElement.addChild(correlationOMElement);
            }

            if (payloadWSO2EventPropertyConfiguration.size() > 0) {
                OMElement payloadOMElement = factory.createOMElement(new QName(EventPublisherConstants.EF_ELE_TO_PAYLOAD_PROPERTY));
                payloadOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

                for (EventOutputProperty eventOutputProperty : payloadWSO2EventPropertyConfiguration) {
                    payloadOMElement.addChild(getPropertyOmElement(factory, eventOutputProperty));
                }
                mappingOMElement.addChild(payloadOMElement);
            }
        } else {
            mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING, EventPublisherConstants.TM_VALUE_DISABLE, null);
        }

        return mappingOMElement;
    }

    private static OMElement getPropertyOmElement(OMFactory factory,
                                                  EventOutputProperty eventOutputProperty) {

        OMElement propertyOMElement = factory.createOMElement(new QName(EventPublisherConstants.EF_ELE_PROPERTY));
        propertyOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

        OMElement fromElement = factory.createOMElement(new QName(EventPublisherConstants.EF_ELEMENT_FROM));
        fromElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);
        fromElement.addAttribute(EventPublisherConstants.EF_ATTR_NAME, eventOutputProperty.getValueOf(), null);

        OMElement toElement = factory.createOMElement(new QName(EventPublisherConstants.EF_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);
        toElement.addAttribute(EventPublisherConstants.EF_ATTR_NAME, eventOutputProperty.getName(), null);
        toElement.addAttribute(EventPublisherConstants.EF_ATTR_TYPE, getAttributeType(eventOutputProperty.getType()), null);

        propertyOMElement.addChild(fromElement);
        propertyOMElement.addChild(toElement);

        return propertyOMElement;

    }

    private static String getAttributeType(AttributeType attributeType) {
        Map<String, AttributeType> attributeMap = EventPublisherConstants.STRING_ATTRIBUTE_TYPE_MAP;
        for (Map.Entry<String, AttributeType> entry : attributeMap.entrySet()) {
            if (entry.getValue().equals(attributeType)) {
                return entry.getKey();
            }
        }
        return null;
    }

}




