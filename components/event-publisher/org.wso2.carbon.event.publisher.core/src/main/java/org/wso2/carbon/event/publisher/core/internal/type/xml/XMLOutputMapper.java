/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.publisher.core.internal.type.xml;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.mapping.XMLOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherProcessingException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherStreamValidationException;
import org.wso2.carbon.event.publisher.core.internal.OutputMapper;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.siddhi.core.event.Event;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XMLOutputMapper implements OutputMapper {

    private static final Log log = LogFactory.getLog(XMLOutputMapper.class);
    private EventPublisherConfiguration eventPublisherConfiguration = null;
    private Map<String, Integer> propertyPositionMap = null;
    private String outputXMLText = "";

    public XMLOutputMapper(EventPublisherConfiguration eventPublisherConfiguration,
                           Map<String, Integer> propertyPositionMap,
                           int tenantId, StreamDefinition streamDefinition) throws
            EventPublisherConfigurationException {
        this.eventPublisherConfiguration = eventPublisherConfiguration;
        this.propertyPositionMap = propertyPositionMap;

        if (eventPublisherConfiguration.getOutputMapping().isCustomMappingEnabled()) {
            validateStreamDefinitionWithOutputProperties(tenantId);
        } else {
            generateTemplateXMLEvent(streamDefinition);
        }
    }

    private List<String> getOutputMappingPropertyList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        mappingTextList.clear();
        while (text.contains(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) && text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) > 0) {
            mappingTextList.add(text.substring(text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) + 2, text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX)));
            text = text.substring(text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) + 2);
        }
        return mappingTextList;
    }

    private void validateStreamDefinitionWithOutputProperties(int tenantId)
            throws EventPublisherConfigurationException {

        XMLOutputMapping textOutputMapping = ((XMLOutputMapping) eventPublisherConfiguration.getOutputMapping());
        String actualMappingText = textOutputMapping.getMappingXMLText();
        if (textOutputMapping.isRegistryResource()) {
            actualMappingText = EventPublisherServiceValueHolder.getCarbonEventPublisherService().getRegistryResourceContent(textOutputMapping.getMappingXMLText());
        }
        this.outputXMLText = actualMappingText;
        List<String> mappingProperties = getOutputMappingPropertyList(actualMappingText);

        Iterator<String> mappingTextListIterator = mappingProperties.iterator();
        for (; mappingTextListIterator.hasNext(); ) {
            String property = mappingTextListIterator.next();
            if (!propertyPositionMap.containsKey(property)) {
                throw new EventPublisherStreamValidationException("Property " + property + " is not in the input stream definition.", eventPublisherConfiguration.getFromStreamName() + ":" + eventPublisherConfiguration.getFromStreamVersion());
            }
        }
    }

    private String getPropertyValue(Object[] inputObjArray, String mappingProperty) {
        if (inputObjArray.length != 0) {
            int position = propertyPositionMap.get(mappingProperty);
            Object data = inputObjArray[position];
            if (data != null) {
                return data.toString();
            }
        }
        return "";
    }

    private OMElement buildOuputOMElement(Object[] eventData, OMElement omElement)
            throws EventPublisherConfigurationException {
        Iterator<OMElement> iterator = omElement.getChildElements();
        int prefixIndex, postfixIndex;
        if (iterator.hasNext()) {
            while (iterator.hasNext()) {
                OMElement childElement = iterator.next();
                Iterator<OMAttribute> iteratorAttr = childElement.getAllAttributes();
                while (iteratorAttr.hasNext()) {
                    OMAttribute omAttribute = iteratorAttr.next();
                    String attributeText = omAttribute.getAttributeValue();
                    if (attributeText != null && !attributeText.isEmpty()) {
                        prefixIndex = attributeText.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
                        if (prefixIndex >= 0) {
                            postfixIndex = attributeText.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX);
                            if (postfixIndex > 0) {
                                String propertyToReplace = attributeText.substring(prefixIndex + 2, postfixIndex);
                                String value = getPropertyValue(eventData, propertyToReplace);
                                omAttribute.setAttributeValue(value);
                            }
                        }
                    }
                }

                // Since the same OM element is being modified, the modifications will be preserved even if
                // the returned OM element is explicitly assigned.
                buildOuputOMElement(eventData, childElement);
            }
        } else {
            String text = omElement.getText();
            if (text != null && !text.isEmpty()) {
                prefixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
                if (prefixIndex >= 0) {
                    postfixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX);
                    if (postfixIndex > 0) {
                        String propertyToReplace = text.substring(prefixIndex + 2, postfixIndex);
                        String value = getPropertyValue(eventData, propertyToReplace);
                        omElement.setText(value);
                    }
                }
            }
        }
        return omElement;
    }

    private String buildOuputStringMessage(Object[] eventData)
            throws EventPublisherConfigurationException {
        StringBuilder eventText = new StringBuilder(outputXMLText);
        int postfixIndex, fromIndex = 0;
        int prefixIndex = eventText.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
        while (prefixIndex >= 0) {
            postfixIndex = eventText.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX, fromIndex);
            if (postfixIndex > 0) {
                String propertyToReplace = eventText.substring(prefixIndex + 2, postfixIndex);
                String value = getPropertyValue(eventData, propertyToReplace);
                eventText.replace(prefixIndex, postfixIndex + 2, value);
                fromIndex = postfixIndex + 2;
            } else {
                throw new EventPublisherProcessingException("Could not find closing tag " + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX
                        + " after the opening tag for the attribute variable!");
            }
            prefixIndex = eventText.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX, fromIndex);
        }
        return eventText.toString();
    }

    @Override
    public Object convertToMappedInputEvent(Event event)
            throws EventPublisherConfigurationException {
        if (event.getData().length > 0) {
            try {
                return buildOuputOMElement(event.getData(), AXIOMUtil.stringToOM(outputXMLText));
            } catch (XMLStreamException e) {
                throw new EventPublisherConfigurationException("XML mapping is not in XML format :" + outputXMLText, e);
            }
        } else {
            throw new EventPublisherProcessingException("Input Object array is empty!");
        }
    }

    @Override
    public Object convertToTypedInputEvent(Event event)
            throws EventPublisherConfigurationException {
        if (event.getData().length > 0) {
            return buildOuputStringMessage(event.getData());
        } else {
            throw new EventPublisherProcessingException("Input Object array is empty!");
        }
    }


    private void generateTemplateXMLEvent(StreamDefinition streamDefinition) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement compositeEventElement = factory.createOMElement(new QName(
                EventPublisherConstants.MULTIPLE_EVENTS_PARENT_TAG));

        OMElement templateEventElement = factory.createOMElement(new QName(EventPublisherConstants.EVENT_PARENT_TAG));
        compositeEventElement.addChild(templateEventElement);

        List<Attribute> metaDatAttributes = streamDefinition.getMetaData();
        if (metaDatAttributes != null && metaDatAttributes.size() > 0) {
            templateEventElement.addChild(createPropertyElement(factory, EventPublisherConstants.PROPERTY_META_PREFIX, metaDatAttributes, EventPublisherConstants.EVENT_META_TAG));
        }

        List<Attribute> correlationAttributes = streamDefinition.getCorrelationData();
        if (correlationAttributes != null && correlationAttributes.size() > 0) {
            templateEventElement.addChild(createPropertyElement(factory, EventPublisherConstants.PROPERTY_CORRELATION_PREFIX, correlationAttributes, EventPublisherConstants.EVENT_CORRELATION_TAG));
        }

        List<Attribute> payloadAttributes = streamDefinition.getPayloadData();
        if (payloadAttributes != null && payloadAttributes.size() > 0) {
            templateEventElement.addChild(createPropertyElement(factory, "", payloadAttributes, EventPublisherConstants.EVENT_PAYLOAD_TAG));
        }

        outputXMLText = compositeEventElement.toString();

    }

    private static OMElement createPropertyElement(OMFactory factory, String dataPrefix,
                                                   List<Attribute> attributeList,
                                                   String propertyTag) {
        OMElement parentPropertyElement = factory.createOMElement(new QName(
                propertyTag));

        for (Attribute attribute : attributeList) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    attribute.getName()));
            propertyElement.setText(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX + dataPrefix + attribute.getName() + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX);
            parentPropertyElement.addChild(propertyElement);
        }
        return parentPropertyElement;
    }

}
