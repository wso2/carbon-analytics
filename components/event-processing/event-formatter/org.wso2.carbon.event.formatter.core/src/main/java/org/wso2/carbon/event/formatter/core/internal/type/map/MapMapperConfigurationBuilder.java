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

package org.wso2.carbon.event.formatter.core.internal.type.map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.config.OutputMapping;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterValidationException;
import org.wso2.carbon.event.formatter.core.internal.config.EventOutputProperty;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;


/**
 * This class is used to read the values of the event builder configuration defined in XML configuration files
 */
public class MapMapperConfigurationBuilder {

    private MapMapperConfigurationBuilder() {

    }

    public static OutputMapping fromOM(
            OMElement mappingElement)
            throws EventFormatterValidationException, EventFormatterConfigurationException {

        MapOutputMapping mapOutputMapping = new MapOutputMapping();

        String customMappingEnabled = mappingElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_CUSTOM_MAPPING));
        if (customMappingEnabled != null && (customMappingEnabled.equals(EventFormatterConstants.TM_VALUE_DISABLE))) {
            mapOutputMapping.setCustomMappingEnabled(false);
        } else {
            mapOutputMapping.setCustomMappingEnabled(true);
            if (!validateMapEventMapping(mappingElement)) {
                throw new EventFormatterConfigurationException("Map Mapping is not valid, check the output mapping");
            }

            if (mappingElement != null) {
                Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_PROPERTY));
                while (propertyIterator.hasNext()) {
                    OMElement propertyOMElement = (OMElement) propertyIterator.next();
                    EventOutputProperty eventOutputProperty = getOutputPropertyFromOM(propertyOMElement);
                    mapOutputMapping.addOutputPropertyConfiguration(eventOutputProperty);
                }
            }
        }

        return mapOutputMapping;
    }

    private static EventOutputProperty getOutputPropertyFromOM(OMElement omElement) {

        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_FROM_PROPERTY));
        OMElement propertyToElement = omElement.getFirstChildWithName(new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_TO_PROPERTY));

        String name = propertyToElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_NAME));
        String valueOf = propertyFromElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_NAME));

        return new EventOutputProperty(name, valueOf);

    }


    private static boolean validateMapEventMapping(OMElement omElement) {


        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            OMElement childElement = mappingIterator.next();
            String childTag = childElement.getLocalName();
            if (!childTag.equals(EventFormatterConstants.EF_ELE_PROPERTY)) {
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
                EventFormatterConstants.EF_ELE_MAPPING_PROPERTY));
        mappingOMElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);

        mappingOMElement.addAttribute(EventFormatterConstants.EF_ATTR_TYPE, EventFormatterConstants.EF_MAP_MAPPING_TYPE, null);

        if (mapOutputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventFormatterConstants.EF_ATTR_CUSTOM_MAPPING, EventFormatterConstants.TM_VALUE_ENABLE, null);
        } else {
            mappingOMElement.addAttribute(EventFormatterConstants.EF_ATTR_CUSTOM_MAPPING, EventFormatterConstants.TM_VALUE_DISABLE, null);
        }

        if (outputPropertyConfiguration.size() > 0) {
            for (EventOutputProperty eventOutputProperty : outputPropertyConfiguration) {
                mappingOMElement.addChild(getPropertyOmElement(factory, eventOutputProperty));
            }
        }
        return mappingOMElement;
    }

    private static OMElement getPropertyOmElement(OMFactory factory,
                                                  EventOutputProperty eventOutputProperty) {

        OMElement propertyOMElement = factory.createOMElement(new QName(EventFormatterConstants.EF_ELE_PROPERTY));
        propertyOMElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);

        OMElement fromElement = factory.createOMElement(new QName(EventFormatterConstants.EF_ELE_FROM_PROPERTY));
        fromElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);
        fromElement.addAttribute(EventFormatterConstants.EF_ATTR_NAME, eventOutputProperty.getValueOf(), null);

        OMElement toElement = factory.createOMElement(new QName(EventFormatterConstants.EF_ELE_TO_PROPERTY));
        toElement.declareDefaultNamespace(EventFormatterConstants.EF_CONF_NS);
        toElement.addAttribute(EventFormatterConstants.EF_ATTR_NAME, eventOutputProperty.getName(), null);

        propertyOMElement.addChild(fromElement);
        propertyOMElement.addChild(toElement);

        return propertyOMElement;

    }


}




