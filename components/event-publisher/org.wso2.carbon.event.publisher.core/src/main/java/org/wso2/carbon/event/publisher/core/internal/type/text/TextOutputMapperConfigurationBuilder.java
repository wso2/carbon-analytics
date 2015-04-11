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
package org.wso2.carbon.event.publisher.core.internal.type.text;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.OutputMapping;
import org.wso2.carbon.event.publisher.core.config.mapping.TextOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherValidationException;

import javax.xml.namespace.QName;
import java.util.Iterator;


/**
 * This class is used to read the values of the event builder configuration defined in XML configuration files
 */
public class TextOutputMapperConfigurationBuilder {


    private TextOutputMapperConfigurationBuilder() {

    }

    public static OutputMapping fromOM(
            OMElement mappingElement)
            throws EventPublisherValidationException, EventPublisherConfigurationException {


        TextOutputMapping textOutputMapping = new TextOutputMapping();

        String customMappingEnabled = mappingElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING));
        if (customMappingEnabled == null || (customMappingEnabled.equals(EventPublisherConstants.ENABLE_CONST))) {
            textOutputMapping.setCustomMappingEnabled(true);
            if (!validateTextMapping(mappingElement)) {
                throw new EventPublisherConfigurationException("Text Mapping is not valid, check the output mapping");
            }

            OMElement innerMappingElement = mappingElement.getFirstChildWithName(
                    new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_MAPPING_INLINE));
            if (innerMappingElement != null) {
                textOutputMapping.setRegistryResource(false);
            } else {
                innerMappingElement = mappingElement.getFirstChildWithName(
                        new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_MAPPING_REGISTRY));
                if (innerMappingElement != null) {
                    textOutputMapping.setRegistryResource(true);
                } else {
                    throw new EventPublisherConfigurationException("Text Mapping is not valid, Mapping should be inline or from registry");
                }
            }

            if (innerMappingElement.getText() == null || innerMappingElement.getText().trim().isEmpty()) {
                throw new EventPublisherConfigurationException("There is no any mapping content available");

            } else {
                textOutputMapping.setMappingText(innerMappingElement.getText());
            }

        } else {
            textOutputMapping.setCustomMappingEnabled(false);
        }

        return textOutputMapping;
    }


    public static boolean validateTextMapping(OMElement omElement) {


        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            count++;
            mappingIterator.next();
        }

        return count != 0;

    }


    public static OMElement outputMappingToOM(
            OutputMapping outputMapping, OMFactory factory) {

        TextOutputMapping textOutputMapping = (TextOutputMapping) outputMapping;

        OMElement mappingOMElement = factory.createOMElement(new QName(
                EventPublisherConstants.EF_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

        mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_TYPE, EventPublisherConstants.EF_TEXT_MAPPING_TYPE, null);

        if (textOutputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING, EventPublisherConstants.ENABLE_CONST, null);


            OMElement innerMappingElement;
            if (textOutputMapping.isRegistryResource()) {
                innerMappingElement = factory.createOMElement(new QName(
                        EventPublisherConstants.EF_ELE_MAPPING_REGISTRY));
                innerMappingElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);
            } else {
                innerMappingElement = factory.createOMElement(new QName(
                        EventPublisherConstants.EF_ELE_MAPPING_INLINE));
                innerMappingElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);
            }
            mappingOMElement.addChild(innerMappingElement);
            innerMappingElement.setText(textOutputMapping.getMappingText());
        } else {
            mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING, EventPublisherConstants.TM_VALUE_DISABLE, null);
        }
        return mappingOMElement;
    }

}




