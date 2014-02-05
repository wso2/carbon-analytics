/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.event.formatter.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterValidationException;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.message.MessageDto;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventFormatterConfigurationHelper {

    public static void validateEventFormatterConfiguration(OMElement eventFormatterOMElement) throws
                                                                                              EventFormatterConfigurationException,
                                                                                              EventFormatterValidationException {

        if (eventFormatterOMElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_NAME)) == null || eventFormatterOMElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_NAME)).trim().isEmpty()) {
            throw new EventFormatterConfigurationException("Need to have an eventFormatter name");
        }

        Iterator childElements = eventFormatterOMElement.getChildElements();
        int count = 0;

        while (childElements.hasNext()) {
            count++;
            childElements.next();
        }

        if (count != 3) {
            throw new EventFormatterConfigurationException("Not a valid configuration, Event Formatter Configuration can only contains 3 child tags (From,Mapping & To)");
        }

        //From property of the event formatter configuration file
        Iterator fromPropertyIter = eventFormatterOMElement.getChildrenWithName(
                new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_FROM_PROPERTY));
        OMElement fromPropertyOMElement = null;
        count = 0;
        while (fromPropertyIter.hasNext()) {
            fromPropertyOMElement = (OMElement) fromPropertyIter.next();
            count++;
        }
        if (count != 1) {
            throw new EventFormatterConfigurationException("There can be only one 'From' element in Event Formatter configuration file.");
        }
        String fromStreamName = fromPropertyOMElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_STREAM_NAME));
        String fromStreamVersion = fromPropertyOMElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_VERSION));

        if (fromStreamName == null || fromStreamVersion == null) {
            throw new EventFormatterConfigurationException("There should be stream name and version in the 'From' element");
        }

        //Mapping property of the event formatter configuration file
        Iterator mappingPropertyIter = eventFormatterOMElement.getChildrenWithName(
                new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_MAPPING_PROPERTY));
        OMElement mappingPropertyOMElement = null;
        count = 0;
        while (mappingPropertyIter.hasNext()) {
            mappingPropertyOMElement = (OMElement) mappingPropertyIter.next();
            count++;
        }
        if (count != 1) {
            throw new EventFormatterConfigurationException("There can be only one 'Mapping' element in Event Formatter configuration file.");
        }

        String mappingType = mappingPropertyOMElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_TYPE));

        if (mappingType == null) {
            throw new EventFormatterConfigurationException("There should be proper mapping type in Event Formatter configuration file.");

        }

        //To property of the event formatter configuration file
        Iterator toPropertyIter = eventFormatterOMElement.getChildrenWithName(
                new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_TO_PROPERTY));
        OMElement toPropertyOMElement = null;
        count = 0;
        while (toPropertyIter.hasNext()) {
            toPropertyOMElement = (OMElement) toPropertyIter.next();
            count++;
        }
        if (count != 1) {
            throw new EventFormatterConfigurationException("There can be only one 'To' element in Event Formatter configuration file.");
        }
        String toEventAdaptorType = toPropertyOMElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_TA_TYPE));
        String toEventAdaptorName = toPropertyOMElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_TA_NAME));

        if (toEventAdaptorType == null || toEventAdaptorName == null) {
            throw new EventFormatterConfigurationException("There should be a event adaptor name and event adaptor type in Formatter configuration file.");
        }

        if (!validateToPropertyConfiguration(toPropertyOMElement, toEventAdaptorType, toEventAdaptorName)) {
            throw new EventFormatterConfigurationException("To property does not contains all the required values for event adaptor type " + toEventAdaptorType);
        }
    }


    private static boolean validateToPropertyConfiguration(OMElement toElement,
                                                           String eventAdaptorType,
                                                           String eventAdaptorName)
            throws EventFormatterConfigurationException {

        List<String> requiredProperties = new ArrayList<String>();
        List<String> toMessageProperties = new ArrayList<String>();

        Iterator toElementPropertyIterator = toElement.getChildrenWithName(
                new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_PROPERTY)
        );

        OutputEventAdaptorService eventAdaptorService = EventFormatterServiceValueHolder.getOutputEventAdaptorService();
        MessageDto messageDto = eventAdaptorService.getEventAdaptorMessageDto(eventAdaptorType);

        if (messageDto == null) {
            throw new EventFormatterValidationException("Event Adaptor type : " + eventAdaptorType + " not loaded yet", eventAdaptorName);
        }

        List<Property> messagePropertyList = messageDto.getMessageOutPropertyList();
        if (messagePropertyList != null) {

            for (Property property : messagePropertyList) {
                if (property.isRequired()) {
                    requiredProperties.add(property.getPropertyName());
                }
            }

            while (toElementPropertyIterator.hasNext()) {
                OMElement toElementProperty = (OMElement) toElementPropertyIterator.next();
                String propertyName = toElementProperty.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_NAME));
                toMessageProperties.add(propertyName);
            }

            if (!toMessageProperties.containsAll(requiredProperties)) {
                return false;
            }
        }

        return true;
    }


    public static OutputEventAdaptorMessageConfiguration getOutputEventAdaptorMessageConfiguration(
            String eventAdaptorTypeName) {
        MessageDto messageDto = EventFormatterServiceValueHolder.getOutputEventAdaptorService().getEventAdaptorMessageDto(eventAdaptorTypeName);
        OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration = null;
        if (messageDto != null && messageDto.getMessageOutPropertyList() != null) {
            outputEventAdaptorMessageConfiguration = new OutputEventAdaptorMessageConfiguration();
            for (Property property : messageDto.getMessageOutPropertyList()) {
                outputEventAdaptorMessageConfiguration.addOutputMessageProperty(property.getPropertyName(), property.getDefaultValue());
            }
        }

        return outputEventAdaptorMessageConfiguration;
    }

    public static String getOutputMappingType(OMElement eventFormatterOMElement) {
        OMElement mappingPropertyOMElement = eventFormatterOMElement.getFirstChildWithName(new QName(EventFormatterConstants.EF_CONF_NS, EventFormatterConstants.EF_ELE_MAPPING_PROPERTY));

        return mappingPropertyOMElement.getAttributeValue(new QName(EventFormatterConstants.EF_ATTR_TYPE));
    }


}
