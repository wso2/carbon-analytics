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

package org.wso2.carbon.event.builder.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.type.json.JsonInputMappingConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.map.MapInputMappingConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.text.TextInputMappingConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.wso2event.Wso2EventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLInputMappingConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorDto;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConfigurationValidator {
    private static Log log = LogFactory.getLog(ConfigurationValidator.class);

    public static boolean isInputEventAdaptorActive(String inputEventAdaptorName,
                                                    String inputEventAdaptorType, int tenantId) {

        InputEventAdaptorManagerService inputEventAdaptorManagerService = EventBuilderServiceValueHolder.getInputEventAdaptorManagerService();
        try {
            InputEventAdaptorConfiguration InputEventAdaptorConfiguration = inputEventAdaptorManagerService.getActiveInputEventAdaptorConfiguration(inputEventAdaptorName, tenantId);
            if (InputEventAdaptorConfiguration != null && InputEventAdaptorConfiguration.getType().equals(inputEventAdaptorType)) {
                return true;
            }
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error("Exception when retrieving configuration for InputEventAdaptor '" + inputEventAdaptorName + "'.", e);
        }

        return false;
    }

    public static void validateEventBuilderConfiguration(OMElement ebConfigOmElement) throws EventBuilderConfigurationException {
        if (!ebConfigOmElement.getLocalName().equals(EventBuilderConstants.EB_ELEMENT_ROOT_ELEMENT)) {
            throw new EventBuilderConfigurationException("Invalid event builder configuration.");
        }

        String eventBuilderName = ebConfigOmElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
        OMElement fromElement = ebConfigOmElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_FROM));
        OMElement mappingElement = ebConfigOmElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_MAPPING));
        OMElement toElement = ebConfigOmElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_TO));

        if (eventBuilderName == null || eventBuilderName.isEmpty() || fromElement == null || mappingElement == null || toElement == null) {
            throw new EventBuilderConfigurationException("Invalid event builder configuration for event builder: " + eventBuilderName);
        }

        String fromInputEventAdaptorName = fromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TA_NAME));
        String fromInputEventAdaptorType = fromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TA_TYPE));

        if (fromInputEventAdaptorName == null || fromInputEventAdaptorName.isEmpty() ||
                fromInputEventAdaptorType == null || fromInputEventAdaptorType.isEmpty()) {
            throw new EventBuilderConfigurationException("Invalid event builder configuration for event builder: " + eventBuilderName);
        }

        InputEventAdaptorMessageConfiguration inputEventMessageConfiguration = EventBuilderConfigHelper.getInputEventMessageConfiguration(fromInputEventAdaptorType);

        Iterator fromElementPropertyIterator = fromElement.getChildrenWithName(
                new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_PROPERTY));
        Map<String, String> fromPropertyMap = new HashMap<String, String>();
        while (fromElementPropertyIterator.hasNext()) {
            OMElement fromElementProperty = (OMElement) fromElementPropertyIterator.next();
            String propertyName = fromElementProperty.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
            String propertyValue = fromElementProperty.getText();
            fromPropertyMap.put(propertyName, propertyValue);
        }
        for (String propertyKey : inputEventMessageConfiguration.getInputMessageProperties().keySet()) {
            if (fromPropertyMap.get(propertyKey) == null) {
                throw new EventBuilderConfigurationException("Invalid event builder configuration for event builder: " + eventBuilderName);
            }
        }

        String mappingType = mappingElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TYPE));
        if (mappingType != null && !mappingType.isEmpty()) {
            validateMappingProperties(mappingElement, mappingType);
        } else {
            throw new EventBuilderConfigurationException("Mapping type not specified for : " + eventBuilderName);
        }

        String toStreamName = toElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_STREAM_NAME));
        String toStreamVersion = toElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_VERSION));

        if (toStreamName == null || toStreamName.isEmpty() || toStreamVersion == null || toStreamVersion.isEmpty()) {
            throw new EventBuilderConfigurationException("Invalid event builder configuration for event builder: " + eventBuilderName);
        }
    }

    public static boolean checkActivationPreconditions(
            EventBuilderConfiguration eventBuilderConfiguration, int tenantId) {
        String fromInputEventAdaptorName = eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorName();
        String fromInputEventAdaptorType = eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorType();

        if (!ConfigurationValidator.isInputEventAdaptorActive(fromInputEventAdaptorName, fromInputEventAdaptorType, tenantId)) {
            return false;
        }
        return true;
    }

    public static boolean validateSupportedMapping(String inputEventAdaptorType,
                                                   String messageType) {

        InputEventAdaptorService inputEventAdaptorService = EventBuilderServiceValueHolder.getInputEventAdaptorService();
        InputEventAdaptorDto inputEventAdaptorDto = inputEventAdaptorService.getEventAdaptorDto(inputEventAdaptorType);

        if (inputEventAdaptorDto == null) {
            return false;
        }

        List<String> supportedInputMessageTypes = inputEventAdaptorDto.getSupportedMessageTypes();
        return supportedInputMessageTypes.contains(messageType);
    }

    @SuppressWarnings("unchecked")
    public static void validateMappingProperties(OMElement mappingElement, String mappingType) throws EventBuilderConfigurationException {
        if (mappingType.equalsIgnoreCase(EventBuilderConstants.EB_WSO2EVENT_MAPPING_TYPE)) {
            Wso2EventBuilderConfigBuilder.validateWso2EventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventBuilderConstants.EB_TEXT_MAPPING_TYPE)) {
            TextInputMappingConfigBuilder.validateTextMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventBuilderConstants.EB_MAP_MAPPING_TYPE)) {
            MapInputMappingConfigBuilder.validateMapEventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventBuilderConstants.EB_XML_MAPPING_TYPE)) {
            XMLInputMappingConfigBuilder.validateXMLEventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventBuilderConstants.EB_JSON_MAPPING_TYPE)) {
            JsonInputMappingConfigBuilder.validateJsonEventMapping(mappingElement);
        } else {
            log.info("No validations available for input mapping type :" + mappingType);
        }
    }
}
