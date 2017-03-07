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
package org.wso2.carbon.event.publisher.core.internal.type.map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.publisher.core.config.EventOutputProperty;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.OutputMapping;
import org.wso2.carbon.event.publisher.core.config.mapping.MapOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherValidationException;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;


/**
 * This class is used to read the values of the event builder configuration defined in XML configuration files
 */
public class MapOutputMapperConfigurationBuilder {

    private MapOutputMapperConfigurationBuilder() {

    }

    public static OutputMapping fromOM(
            OMElement mappingElement)
            throws EventPublisherValidationException, EventPublisherConfigurationException {

        MapOutputMapping mapOutputMapping = new MapOutputMapping();

        String customMappingEnabled = mappingElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING));
        if (customMappingEnabled == null || (customMappingEnabled.equals(EventPublisherConstants.ENABLE_CONST))) {
            mapOutputMapping.setCustomMappingEnabled(true);
            if (!validateMapEventMapping(mappingElement)) {
                throw new EventPublisherConfigurationException("Map Mapping is not valid, check the output mapping");
            }

            if (mappingElement != null) {
                Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_PROPERTY));
                while (propertyIterator.hasNext()) {
                    OMElement propertyOMElement = (OMElement) propertyIterator.next();
                    EventOutputProperty eventOutputProperty = getOutputPropertyFromOM(propertyOMElement);
                    mapOutputMapping.addOutputPropertyConfiguration(eventOutputProperty);
                }
            }
        } else {
            mapOutputMapping.setCustomMappingEnabled(false);
        }

        return mapOutputMapping;
    }

    private static EventOutputProperty getOutputPropertyFromOM(OMElement omElement) {

        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_FROM));
        OMElement propertyToElement = omElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_TO));

        String name = propertyToElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));
        String valueOf = propertyFromElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));

        return new EventOutputProperty(name, valueOf);

    }


    public static boolean validateMapEventMapping(OMElement omElement) {


        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            OMElement childElement = mappingIterator.next();
            String childTag = childElement.getLocalName();
            if (!childTag.equals(EventPublisherConstants.EF_ELE_PROPERTY)) {
                return false;
            }
            count++;
        }

        return count != 0;

    }


    public static OMElement outputMappingToOM(
            OutputMapping outputMapping, OMFactory factory) {

        MapOutputMapping mapOutputMapping = (MapOutputMapping) outputMapping;

        List<EventOutputProperty> outputPropertyConfiguration = mapOutputMapping.getOutputPropertyConfiguration();

        OMElement mappingOMElement = factory.createOMElement(new QName(
                EventPublisherConstants.EF_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

        mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_TYPE, EventPublisherConstants.EF_MAP_MAPPING_TYPE, null);

        if (mapOutputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING, EventPublisherConstants.ENABLE_CONST, null);


            if (outputPropertyConfiguration.size() > 0) {
                for (EventOutputProperty eventOutputProperty : outputPropertyConfiguration) {
                    mappingOMElement.addChild(getPropertyOmElement(factory, eventOutputProperty));
                }
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

        propertyOMElement.addChild(fromElement);
        propertyOMElement.addChild(toElement);

        return propertyOMElement;

    }


}




