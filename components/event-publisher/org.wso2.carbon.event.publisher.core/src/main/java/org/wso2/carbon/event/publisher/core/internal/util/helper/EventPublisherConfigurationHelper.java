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
package org.wso2.carbon.event.publisher.core.internal.util.helper;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterSchema;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherValidationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.event.publisher.core.internal.type.json.JSONOutputMapperConfigurationBuilder;
import org.wso2.carbon.event.publisher.core.internal.type.map.MapOutputMapperConfigurationBuilder;
import org.wso2.carbon.event.publisher.core.internal.type.text.TextOutputMapperConfigurationBuilder;
import org.wso2.carbon.event.publisher.core.internal.type.wso2event.WSO2EventOutputMapperConfigurationBuilder;
import org.wso2.carbon.event.publisher.core.internal.type.xml.XMLOutputMapperConfigurationBuilder;

import javax.xml.namespace.QName;
import java.util.*;

public class EventPublisherConfigurationHelper {

    private static final Log log = LogFactory.getLog(EventPublisherConfigurationHelper.class);

    public static void validateEventPublisherConfiguration(OMElement eventPublisherOMElement) throws
            EventPublisherConfigurationException,
            EventPublisherValidationException {

        if (!eventPublisherOMElement.getLocalName().equals(EventPublisherConstants.EF_ELEMENT_ROOT_ELEMENT)) {
            throw new EventPublisherConfigurationException("Invalid event publisher configuration.");
        }

        if (eventPublisherOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME)) == null || eventPublisherOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME)).trim().isEmpty()) {
            throw new EventPublisherConfigurationException("Need to have an eventPublisher name");
        }

        String eventPublisherName = eventPublisherOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));

        Iterator childElements = eventPublisherOMElement.getChildElements();
        int count = 0;

        while (childElements.hasNext()) {
            count++;
            childElements.next();
        }

        if (count != 3) {
            throw new EventPublisherConfigurationException("Not a valid configuration, Event Publisher Configuration can only contains 3 child tags (From,Mapping & To), for " + eventPublisherName);
        }

        OMElement fromElement = eventPublisherOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_FROM));
        OMElement mappingElement = eventPublisherOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_MAPPING));
        OMElement toElement = eventPublisherOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_TO));

        if (fromElement == null || mappingElement == null || toElement == null) {
            throw new EventPublisherConfigurationException("Invalid event publisher configuration for event publisher: " + eventPublisherName);
        }

        //From property of the event publisher configuration file
        Iterator fromPropertyIter = eventPublisherOMElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_FROM));
        OMElement fromPropertyOMElement = null;
        count = 0;
        while (fromPropertyIter.hasNext()) {
            fromPropertyOMElement = (OMElement) fromPropertyIter.next();
            count++;
        }
        if (count != 1) {
            throw new EventPublisherConfigurationException("There can be only one 'From' element in Event Publisher configuration " + eventPublisherName);
        }
        String fromStreamName = fromPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_STREAM_NAME));
        String fromStreamVersion = fromPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_VERSION));

        if (fromStreamName == null || fromStreamName.isEmpty() || fromStreamVersion == null || fromStreamName.isEmpty()) {
            throw new EventPublisherConfigurationException("There should be stream name and version in the 'From' element, of " + eventPublisherName);
        }

        //Mapping property of the event publisher configuration file
        Iterator mappingPropertyIter = eventPublisherOMElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_MAPPING));
        OMElement mappingPropertyOMElement = null;
        count = 0;
        while (mappingPropertyIter.hasNext()) {
            mappingPropertyOMElement = (OMElement) mappingPropertyIter.next();
            count++;
        }
        if (count != 1) {
            throw new EventPublisherConfigurationException("There can be only one 'Mapping' element in Event Publisher configuration " + eventPublisherName);
        }

        String mappingType = mappingPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TYPE));

        if (mappingType == null || mappingType.isEmpty()) {
            throw new EventPublisherConfigurationException("There should be proper mapping type in Event Publisher configuration " + eventPublisherName);
        }

        validateMappingProperties(mappingElement, mappingType);

        //To property of the event publisher configuration file
        Iterator toPropertyIter = eventPublisherOMElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_TO));
        OMElement toPropertyOMElement = null;
        count = 0;
        while (toPropertyIter.hasNext()) {
            toPropertyOMElement = (OMElement) toPropertyIter.next();
            count++;
        }
        if (count != 1) {
            throw new EventPublisherConfigurationException("There can be only one 'To' element in Event Publisher configuration file.");
        }

        String toEventAdapterType = toPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TA_TYPE));

        if (toEventAdapterType == null || toEventAdapterType.isEmpty()) {
            throw new EventPublisherConfigurationException("There should be a event adapter type in Publisher configuration file.");
        }

        if (!validateToPropertyConfiguration(toPropertyOMElement, toEventAdapterType)) {
            throw new EventPublisherConfigurationException("To property does not contains all the required values for event adapter type " + toEventAdapterType);
        }
    }

    public static void validateMappingProperties(OMElement mappingElement, String mappingType)
            throws EventPublisherConfigurationException {
        if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_WSO2EVENT_MAPPING_TYPE)) {
            WSO2EventOutputMapperConfigurationBuilder.validateWso2EventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_TEXT_MAPPING_TYPE)) {
            TextOutputMapperConfigurationBuilder.validateTextMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_MAP_MAPPING_TYPE)) {
            MapOutputMapperConfigurationBuilder.validateMapEventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_XML_MAPPING_TYPE)) {
            XMLOutputMapperConfigurationBuilder.validateXMLEventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventPublisherConstants.EF_JSON_MAPPING_TYPE)) {
            JSONOutputMapperConfigurationBuilder.validateJsonEventMapping(mappingElement);
        } else {
            log.info("No validations available for output mapping type :" + mappingType);
        }
    }

    private static boolean validateToPropertyConfiguration(OMElement toElement,
                                                           String eventAdapterType)
            throws EventPublisherConfigurationException {

        List<String> requiredProperties = new ArrayList<String>();
        List<String> propertiesInConfig = new ArrayList<String>();

        Iterator toElementPropertyIterator = toElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_PROPERTY)
        );

        OutputEventAdapterService eventAdapterService = EventPublisherServiceValueHolder.getOutputEventAdapterService();
        OutputEventAdapterSchema adapterSchema = eventAdapterService.getOutputEventAdapterSchema(eventAdapterType);

        if (adapterSchema == null) {
            throw new EventPublisherValidationException("Event Adapter with type: " + eventAdapterType + " does not exist", eventAdapterType);
        }

        List<Property> propertyList = new ArrayList<Property>();
        if (adapterSchema.getDynamicPropertyList() != null) {
            propertyList.addAll(adapterSchema.getDynamicPropertyList());
        }
        if (adapterSchema.getDynamicPropertyList() != null) {
            propertyList.addAll(adapterSchema.getDynamicPropertyList());
        }

        if (propertyList.size() > 0) {

            for (Property property : propertyList) {
                if (property.isRequired()) {
                    requiredProperties.add(property.getPropertyName());
                }
            }

            while (toElementPropertyIterator.hasNext()) {
                OMElement toElementProperty = (OMElement) toElementPropertyIterator.next();
                String propertyName = toElementProperty.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));
                propertiesInConfig.add(propertyName);
            }

            if (!propertiesInConfig.containsAll(requiredProperties)) {
                return false;
            }
        }

        return true;
    }


    public static OutputEventAdapterConfiguration getOutputEventAdapterConfiguration(
            String eventAdapterType, String publisherName, String messageFormat) {
        OutputEventAdapterSchema schema = EventPublisherServiceValueHolder.getOutputEventAdapterService().getOutputEventAdapterSchema(eventAdapterType);
        OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
        outputEventAdapterConfiguration.setName(publisherName);
        outputEventAdapterConfiguration.setMessageFormat(messageFormat);
        outputEventAdapterConfiguration.setType(eventAdapterType);
        Map<String, String> staticProperties = new HashMap<String, String>();
        if (schema != null && schema.getStaticPropertyList() != null) {
            for (Property property : schema.getStaticPropertyList()) {
                staticProperties.put(property.getPropertyName(), property.getDefaultValue());
            }
        }
        outputEventAdapterConfiguration.setStaticProperties(staticProperties);
        return outputEventAdapterConfiguration;
    }

    public static Map<String, String> getDynamicProperties(String eventAdapterType) {
        Map<String, String> dynamicProperties = new HashMap<String, String>();
        OutputEventAdapterSchema schema = EventPublisherServiceValueHolder.getOutputEventAdapterService().getOutputEventAdapterSchema(eventAdapterType);
        if (schema != null && schema.getDynamicPropertyList() != null) {
            for (Property property : schema.getDynamicPropertyList()) {
                dynamicProperties.put(property.getPropertyName(), property.getDefaultValue());
            }
        }
        return dynamicProperties;
    }

    public static String getOutputMappingType(OMElement eventPublisherOMElement) {
        OMElement mappingPropertyOMElement = eventPublisherOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_MAPPING));
        return mappingPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TYPE));
    }

    public static String getEventPublisherName(OMElement eventPublisherOMElement) {
        return eventPublisherOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));
    }

    /*
        Checks whether all the secure fields are encrypted.
         */
    public static boolean validateEncryptedProperties(OMElement eventAdapterConfigOMElement) {

        String adaptorType = eventAdapterConfigOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_TO)).getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TA_TYPE));

        //get Static and Dynamic PropertyLists
        List<String> encryptedProperties = EventPublisherServiceValueHolder.getCarbonEventPublisherService().getEncryptedProperties(adaptorType);
        Iterator propertyIter = eventAdapterConfigOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_TO)).getChildrenWithName(new QName(EventPublisherConstants.EF_ELE_PROPERTY));

        while (propertyIter.hasNext()) {
            OMElement propertyOMElement = (OMElement) propertyIter.next();
            String name = propertyOMElement.getAttributeValue(
                    new QName(EventPublisherConstants.EF_ATTR_NAME));

            String value = propertyOMElement.getText();
            if (encryptedProperties.contains(name.trim())) {
                OMAttribute encryptedAttribute = propertyOMElement.getAttribute(new QName(EventPublisherConstants.EF_ATTR_ENCRYPTED));
                if ((value != null && value.length() > 0) && (encryptedAttribute == null || (!"true".equals(encryptedAttribute.getAttributeValue())))) {
                    return false;
                }
            }
        }
        return true;
    }
}
