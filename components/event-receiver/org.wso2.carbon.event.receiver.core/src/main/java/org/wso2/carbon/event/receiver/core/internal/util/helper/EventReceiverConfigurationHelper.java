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
package org.wso2.carbon.event.receiver.core.internal.util.helper;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterSchema;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.input.adapter.core.Property;
import org.wso2.carbon.event.receiver.core.EventReceiverDeployer;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverStreamValidationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverValidationException;
import org.wso2.carbon.event.receiver.core.InputMapper;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.type.json.JSONInputMapperConfigurationBuilder;
import org.wso2.carbon.event.receiver.core.internal.type.map.MapInputMappingConfigBuilder;
import org.wso2.carbon.event.receiver.core.internal.type.text.TextInputMapperConfigurationBuilder;
import org.wso2.carbon.event.receiver.core.internal.type.wso2event.WSO2EventInputMapperConfigurationBuilder;
import org.wso2.carbon.event.receiver.core.internal.type.xml.XMLInputMapperConfigrationBuilder;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EventReceiverConfigurationHelper {

    private static final Log log = LogFactory.getLog(EventReceiverConfigurationHelper.class);

    public static String getInputMappingType(OMElement eventReceiverOMElement) {
        OMElement mappingElement = eventReceiverOMElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_MAPPING));
        return mappingElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TYPE));
    }

    public static String getEventReceiverName(OMElement eventReceiverOMElement) {
        return eventReceiverOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
    }

    public static EventReceiverDeployer getEventReceiverDeployer(
            AxisConfiguration axisConfiguration) {
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfiguration.getConfigurator();
        return (EventReceiverDeployer) deploymentEngine.getDeployer(EventReceiverConstants.ER_CONFIG_DIRECTORY, EventReceiverConstants.ER_CONFIG_FILE_EXTENSION);
    }

    public static Attribute[] getAttributes(List<InputMappingAttribute> inputMappingAttributes) {
        List<Attribute> metaAttributes = new ArrayList<Attribute>();
        List<Attribute> correlationAttributes = new ArrayList<Attribute>();
        List<Attribute> payloadAttributes = new ArrayList<Attribute>();
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
            if (inputMappingAttribute.getToElementKey().startsWith(EventReceiverConstants.META_DATA_PREFIX)) {
                metaAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            } else if (inputMappingAttribute.getToElementKey().startsWith(EventReceiverConstants.CORRELATION_DATA_PREFIX)) {
                correlationAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            } else {
                payloadAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            }
        }
        Attribute[] outputAttributes = new Attribute[metaAttributes.size() + correlationAttributes.size() + payloadAttributes.size()];
        int attributeCount = 0;
        for (Attribute attribute : metaAttributes) {
            outputAttributes[attributeCount++] = attribute;
        }
        for (Attribute attribute : correlationAttributes) {
            outputAttributes[attributeCount++] = attribute;
        }
        for (Attribute attribute : payloadAttributes) {
            outputAttributes[attributeCount++] = attribute;
        }
        return outputAttributes;
    }

    public static void validateEventReceiverConfiguration(OMElement eventReceiverOMElement)
            throws EventReceiverConfigurationException {
        if (!eventReceiverOMElement.getLocalName().equals(EventReceiverConstants.ER_ELEMENT_ROOT_ELEMENT)) {
            throw new EventReceiverConfigurationException("Invalid event receiver configuration.");
        }

        if (eventReceiverOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME)) == null || eventReceiverOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME)).trim().isEmpty()) {
            throw new EventReceiverConfigurationException("Need to have an eventReceiver name");
        }

        String eventReceiverName = eventReceiverOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));

        Iterator childElements = eventReceiverOMElement.getChildElements();
        int count = 0;

        while (childElements.hasNext()) {
            count++;
            childElements.next();
        }

        if (count != 3) {
            throw new EventReceiverConfigurationException("Not a valid configuration, Event Receiver Configuration can only contains 3 child tags (From,Mapping & To), for " + eventReceiverName);
        }

        OMElement fromElement = eventReceiverOMElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM));
        OMElement mappingElement = eventReceiverOMElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_MAPPING));
        OMElement toElement = eventReceiverOMElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_TO));

        if (fromElement == null || mappingElement == null || toElement == null) {
            throw new EventReceiverConfigurationException("Invalid event receiver configuration for event receiver: " + eventReceiverName);
        }

        //From property of the event receiver configuration file
        Iterator fromPropertyIter = eventReceiverOMElement.getChildrenWithName(
                new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM));
        OMElement fromPropertyOMElement = null;
        count = 0;
        while (fromPropertyIter.hasNext()) {
            fromPropertyOMElement = (OMElement) fromPropertyIter.next();
            count++;
        }
        if (count != 1) {
            throw new EventReceiverConfigurationException("There can be only one 'From' element in Event Receiver configuration file.");
        }

        String fromEventAdapterType = fromPropertyOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TA_TYPE));

        if (fromEventAdapterType == null || fromEventAdapterType.isEmpty()) {
            throw new EventReceiverConfigurationException("There should be a event adapter type in Receiver configuration file.");
        }

        if (!validateFromPropertyConfiguration(fromElement, fromEventAdapterType)) {
            throw new EventReceiverConfigurationException("From property does not contains all the required values for event adapter type " + fromEventAdapterType);
        }

        //Mapping property of the event receiver configuration file
        Iterator mappingPropertyIter = eventReceiverOMElement.getChildrenWithName(
                new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_MAPPING));
        OMElement mappingPropertyOMElement = null;
        count = 0;
        while (mappingPropertyIter.hasNext()) {
            mappingPropertyOMElement = (OMElement) mappingPropertyIter.next();
            count++;
        }
        if (count != 1) {
            throw new EventReceiverConfigurationException("There can be only one 'Mapping' element in Event Receiver configuration " + eventReceiverName);
        }

        String mappingType = mappingPropertyOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TYPE));

        if (mappingType == null || mappingType.isEmpty()) {
            throw new EventReceiverConfigurationException("There should be proper mapping type in Event Receiver configuration " + eventReceiverName);
        }

        validateMappingProperties(mappingElement, mappingType);

        //To property of the event publisher configuration file
        Iterator toPropertyIter = eventReceiverOMElement.getChildrenWithName(
                new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_TO));
        OMElement toPropertyOMElement = null;
        count = 0;
        while (toPropertyIter.hasNext()) {
            toPropertyOMElement = (OMElement) toPropertyIter.next();
            count++;
        }
        if (count != 1) {
            throw new EventReceiverConfigurationException("There can be only one 'To' element in Event Receiver configuration " + eventReceiverName);
        }
        String toStreamName = toPropertyOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_STREAM_NAME));
        String toStreamVersion = toPropertyOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_VERSION));

        if (toStreamName == null || toStreamName.isEmpty() || toStreamVersion == null || toStreamVersion.isEmpty()) {
            throw new EventReceiverConfigurationException("There should be stream name and version in the 'To' element, of " + eventReceiverName);
        }
    }

    private static boolean validateFromPropertyConfiguration(OMElement fromElement, String fromEventAdapterType) {
        List<String> requiredProperties = new ArrayList<String>();
        List<String> propertiesInConfig = new ArrayList<String>();

        Iterator toElementPropertyIterator = fromElement.getChildrenWithName(
                new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_PROPERTY)
        );

        InputEventAdapterService eventAdapterService = EventReceiverServiceValueHolder.getInputEventAdapterService();
        InputEventAdapterSchema adapterSchema = eventAdapterService.getInputEventAdapterSchema(fromEventAdapterType);

        if (adapterSchema == null) {
            throw new EventReceiverValidationException("Event Adapter with type: " + fromEventAdapterType + " does not exist", fromEventAdapterType);
        }

        List<Property> propertyList = adapterSchema.getPropertyList();

        if (propertyList != null) {

            for (Property property : propertyList) {
                if (property.isRequired()) {
                    requiredProperties.add(property.getPropertyName());
                }
            }

            while (toElementPropertyIterator.hasNext()) {
                OMElement toElementProperty = (OMElement) toElementPropertyIterator.next();
                String propertyName = toElementProperty.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
                propertiesInConfig.add(propertyName);
            }

            if (!propertiesInConfig.containsAll(requiredProperties)) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public static void validateMappingProperties(OMElement mappingElement, String mappingType)
            throws EventReceiverConfigurationException {
        if (mappingType.equalsIgnoreCase(EventReceiverConstants.ER_WSO2EVENT_MAPPING_TYPE)) {
            WSO2EventInputMapperConfigurationBuilder.validateWso2EventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.ER_TEXT_MAPPING_TYPE)) {
            TextInputMapperConfigurationBuilder.validateTextMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.ER_MAP_MAPPING_TYPE)) {
            MapInputMappingConfigBuilder.validateMapEventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.ER_XML_MAPPING_TYPE)) {
            XMLInputMapperConfigrationBuilder.validateXMLEventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.ER_JSON_MAPPING_TYPE)) {
            JSONInputMapperConfigurationBuilder.validateJsonEventMapping(mappingElement);
        } else {
            log.info("No validations available for input mapping type :" + mappingType);
        }
    }

    public static void validateExportedStream(EventReceiverConfiguration eventReceiverConfiguration,
                                              StreamDefinition exportedStreamDefinition,
                                              InputMapper inputMapper) {
        if (eventReceiverConfiguration != null && exportedStreamDefinition != null) {
            if (eventReceiverConfiguration.getInputMapping().isCustomMappingEnabled()) {
                String streamId = exportedStreamDefinition.getStreamId();
                if (inputMapper.getOutputAttributes() == null || inputMapper.getOutputAttributes().length == 0) {
                    throw new EventReceiverStreamValidationException("The input mapper is not exporting any output attributes for stream " + streamId);
                }
                List<Attribute> outputAttributes = new ArrayList<Attribute>(Arrays.asList(inputMapper.getOutputAttributes()));
                List<Attribute> metaAttributeList = exportedStreamDefinition.getMetaData();
                if (metaAttributeList != null) {
                    for (Attribute attribute : metaAttributeList) {
                        Attribute prependedAttribute = new Attribute(EventReceiverConstants.META_DATA_PREFIX + attribute.getName(), attribute.getType());
                        if (!outputAttributes.contains(prependedAttribute)) {
                            throw new EventReceiverStreamValidationException("The meta data attribute '" + attribute.getName()
                                    + "' in stream '" + streamId + "' cannot be found under attributes exported by this event receiver mapping", streamId);
                        } else {
                            outputAttributes.remove(prependedAttribute);
                        }
                    }
                }
                List<Attribute> correlationAttributeList = exportedStreamDefinition.getCorrelationData();
                if (correlationAttributeList != null) {
                    for (Attribute attribute : correlationAttributeList) {
                        Attribute prependedAttribute = new Attribute(EventReceiverConstants.CORRELATION_DATA_PREFIX + attribute.getName(), attribute.getType());
                        if (!outputAttributes.contains(prependedAttribute)) {
                            throw new EventReceiverStreamValidationException("The correlation data attribute '" + attribute.getName()
                                    + "' in stream '" + streamId + "' cannot be found under attributes exported by this event receiver mapping", streamId);
                        } else {
                            outputAttributes.remove(prependedAttribute);
                        }
                    }
                }
                List<Attribute> payloadAttributeList = exportedStreamDefinition.getPayloadData();
                if (payloadAttributeList != null) {
                    for (Attribute attribute : payloadAttributeList) {
                        if (!outputAttributes.contains(attribute)) {
                            throw new EventReceiverStreamValidationException("The payload data attribute '" + attribute.getName()
                                    + "' in stream '" + streamId + "' cannot be found under attributes exported by this event receiver mapping", streamId);
                        } else {
                            outputAttributes.remove(attribute);
                        }
                    }
                }
                if (outputAttributes.size() > 0) {
                    throw new EventReceiverStreamValidationException("The attribute '" + outputAttributes.get(0).getName()
                            + "' exported by this event receiver mapping cannot be found not in '" + streamId + "'", streamId);

                }
            }
        }
    }

    /*
        Checks whether all the secure fields are encrypted.
         */
    public static boolean validateEncryptedProperties(OMElement eventAdapterConfigOMElement) {

        String adaptorType = eventAdapterConfigOMElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM)).getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TA_TYPE));

        //get Static and Dynamic PropertyLists
        List<String> encryptedProperties = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getEncryptedProperties(adaptorType);
        Iterator propertyIter = eventAdapterConfigOMElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM)).getChildrenWithName(new QName(EventReceiverConstants.ER_ELEMENT_PROPERTY));

        while (propertyIter.hasNext()) {
            OMElement propertyOMElement = (OMElement) propertyIter.next();
            String name = propertyOMElement.getAttributeValue(
                    new QName(EventReceiverConstants.ER_ATTR_NAME));

            String value = propertyOMElement.getText();
            if (encryptedProperties.contains(name.trim())) {
                OMAttribute encryptedAttribute = propertyOMElement.getAttribute(new QName(EventReceiverConstants.ER_ATTR_ENCRYPTED));
                if ((value != null && value.length() > 0) && (encryptedAttribute == null || (!"true".equals(encryptedAttribute.getAttributeValue())))) {
                    return false;
                }
            }
        }
        return true;
    }
}
