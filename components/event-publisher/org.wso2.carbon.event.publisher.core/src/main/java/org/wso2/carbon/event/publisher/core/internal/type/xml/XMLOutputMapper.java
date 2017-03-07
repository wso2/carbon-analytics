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
import org.wso2.carbon.event.publisher.core.internal.OutputMapper;
import org.wso2.carbon.event.publisher.core.internal.util.EventPublisherUtil;
import org.wso2.carbon.event.publisher.core.internal.util.RuntimeResourceLoader;
import org.wso2.siddhi.core.event.Event;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XMLOutputMapper implements OutputMapper {

    private static final Log log = LogFactory.getLog(XMLOutputMapper.class);
    private EventPublisherConfiguration eventPublisherConfiguration = null;
    private Map<String, Integer> propertyPositionMap = null;
    private List<String> mappingTextList;
    private boolean isCustomRegistryPath;
    private final RuntimeResourceLoader runtimeResourceLoader;
    private final boolean isCustomMappingEnabled;
    private String mappingText;

    public XMLOutputMapper(EventPublisherConfiguration eventPublisherConfiguration,
                           Map<String, Integer> propertyPositionMap,
                           int tenantId, StreamDefinition streamDefinition) throws
            EventPublisherConfigurationException {
        this.eventPublisherConfiguration = eventPublisherConfiguration;
        this.propertyPositionMap = propertyPositionMap;

        XMLOutputMapping outputMapping = (XMLOutputMapping) eventPublisherConfiguration.getOutputMapping();
        this.runtimeResourceLoader = new RuntimeResourceLoader(outputMapping.getCacheTimeoutDuration(), propertyPositionMap);
        this.isCustomMappingEnabled = outputMapping.isCustomMappingEnabled();

        if (this.isCustomMappingEnabled) {
            this.mappingText = getCustomMappingText();
        } else {
            this.mappingText = generateTemplateXMLEvent(streamDefinition);
        }

        if (!outputMapping.isRegistryResource()) {     // Store only if it is not from registry
            this.mappingText = validateXML(mappingText);
            this.mappingTextList = generateMappingTextList(mappingText);
        }
    }

    private String validateXML(String text) throws EventPublisherConfigurationException {
        try {
            //Parsing and converting back to string to discover parse exceptions early.
            OMElement mappingOMElement = AXIOMUtil.stringToOM(text);
            return mappingOMElement.toString();
        } catch (XMLStreamException e) {
            throw new EventPublisherConfigurationException("Could not parse the mapping text:" + e.getMessage(), e);
        }
    }

    private List<String> getOutputMappingPropertyList(String mappingText) throws EventPublisherConfigurationException {
        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        int prefixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
        int postfixIndex;
        while (prefixIndex > 0) {
            postfixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX);
            if (postfixIndex > prefixIndex) {
                mappingTextList.add(text.substring(prefixIndex + 2, postfixIndex));
                text = text.substring(postfixIndex + 2);
            } else {
                throw new EventPublisherConfigurationException("Found template attribute prefix "
                        + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX + " without corresponding postfix "
                        + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX + ". Please verify your XML template.");
            }
            prefixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
        }
        return mappingTextList;
    }

    private List<String> generateMappingTextList(String mappingText) throws EventPublisherConfigurationException {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        int prefixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
        int postfixIndex;
        while (prefixIndex > 0) {
            postfixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX);
            if (postfixIndex > prefixIndex) {
                mappingTextList.add(text.substring(0, prefixIndex));
                mappingTextList.add(text.substring(prefixIndex + 2, postfixIndex));
                text = text.substring(postfixIndex + 2);
            } else {
                throw new EventPublisherConfigurationException("Found template attribute prefix "
                        + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX + " without corresponding postfix "
                        + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX + ". Please verify your XML template.");
            }
            prefixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
        }
        mappingTextList.add(text);
        return mappingTextList;
    }

    private String getCustomMappingText() throws EventPublisherConfigurationException {
        XMLOutputMapping textOutputMapping = ((XMLOutputMapping) eventPublisherConfiguration.getOutputMapping());
        String actualMappingText = textOutputMapping.getMappingXMLText();
        if (textOutputMapping.isRegistryResource()) {
            this.isCustomRegistryPath = actualMappingText.contains(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) && actualMappingText.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) > 0;
            if (!this.isCustomRegistryPath) {
                actualMappingText = this.runtimeResourceLoader.getResourceContent(textOutputMapping.getMappingXMLText());
            }
        }
        return actualMappingText;
    }

    private String getPropertyValue(Event event, String mappingProperty) {
        Object[] eventData = event.getData();
        Map<String, Object> arbitraryMap = event.getArbitraryDataMap();
        Integer position = propertyPositionMap.get(mappingProperty);
        Object data = null;

        if (position != null && eventData.length != 0) {
            data = eventData[position];
        } else if (mappingProperty != null && arbitraryMap != null && arbitraryMap.containsKey(mappingProperty)) {
            data = arbitraryMap.get(mappingProperty);
        }
        if (data != null) {
            return data.toString();
        }
        return "";
    }

    @Override
    public Object convertToMappedInputEvent(Event event)
            throws EventPublisherConfigurationException {
        if (this.isCustomMappingEnabled) {
            EventPublisherUtil.validateStreamDefinitionWithOutputProperties(mappingText, propertyPositionMap, event.getArbitraryDataMap());
        }
        // Retrieve resource at runtime if it is from registry
        XMLOutputMapping outputMapping = (XMLOutputMapping) eventPublisherConfiguration.getOutputMapping();
        if (outputMapping.isRegistryResource()) {
            String path = outputMapping.getMappingXMLText();
            if (isCustomRegistryPath) {

                // Retrieve the actual path
                List<String> pathMappingTextList = generateMappingTextList(path);
                StringBuilder pathBuilder = new StringBuilder(pathMappingTextList.get(0));
                for (int i = 1; i < pathMappingTextList.size(); i++) {
                    if (i % 2 == 0) {
                        pathBuilder.append(pathMappingTextList.get(i));
                    } else {
                        pathBuilder.append(getPropertyValue(event, pathMappingTextList.get(i)));
                    }
                }
                path = pathBuilder.toString();
            }
            // Retrieve actual content
            String actualMappingText = this.runtimeResourceLoader.getResourceContent(path);
            // Validate XML
            actualMappingText = validateXML(actualMappingText);
            this.mappingTextList = generateMappingTextList(actualMappingText);
        }

        StringBuilder eventText = new StringBuilder(mappingTextList.get(0));
        for (int i = 1, size = mappingTextList.size(); i < size; i++) {
            if (i % 2 == 0) {
                eventText.append(mappingTextList.get(i));
            } else {
                eventText.append(getPropertyValue(event, mappingTextList.get(i)));
            }
        }

        String text = eventText.toString();
        if (!this.isCustomMappingEnabled) {
            Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
            if (arbitraryDataMap != null && !arbitraryDataMap.isEmpty()) {
                // Add arbitrary data map to the default template
                try {
                    OMFactory factory = OMAbstractFactory.getOMFactory();
                    OMElement compositeEventElement = AXIOMUtil.stringToOM(text);
                    OMElement parentPropertyElement = factory.createOMElement(new QName(EventPublisherConstants.EVENT_ARBITRARY_DATA_MAP_TAG));

                    for (Map.Entry<String, Object> entry : arbitraryDataMap.entrySet()) {
                        OMElement propertyElement = factory.createOMElement(new QName(entry.getKey()));
                        propertyElement.setText(entry.getValue().toString());
                        parentPropertyElement.addChild(propertyElement);
                    }
                    compositeEventElement.getFirstElement().addChild(parentPropertyElement);
                    text = compositeEventElement.toString();
                } catch (XMLStreamException e) {
                    log.warn("Error in parsing event XML text", e);
                }
            }
        }

        return text;
    }

    @Override
    public Object convertToTypedInputEvent(Event event)
            throws EventPublisherConfigurationException {
        return convertToMappedInputEvent(event);
    }


    private String generateTemplateXMLEvent(StreamDefinition streamDefinition) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement compositeEventElement = factory.createOMElement(new QName(
                EventPublisherConstants.MULTIPLE_EVENTS_PARENT_TAG));

        OMElement templateEventElement = factory.createOMElement(new QName(EventPublisherConstants.EVENT_PARENT_TAG));
        compositeEventElement.addChild(templateEventElement);

        List<Attribute> metaDatAttributes = streamDefinition.getMetaData();
        if (metaDatAttributes != null && metaDatAttributes.size() > 0) {
            templateEventElement.addChild(createPropertyElement(factory, EventPublisherConstants.PROPERTY_META_PREFIX,
                    metaDatAttributes, EventPublisherConstants.EVENT_META_TAG));
        }

        List<Attribute> correlationAttributes = streamDefinition.getCorrelationData();
        if (correlationAttributes != null && correlationAttributes.size() > 0) {
            templateEventElement.addChild(createPropertyElement(factory, EventPublisherConstants.PROPERTY_CORRELATION_PREFIX,
                    correlationAttributes, EventPublisherConstants.EVENT_CORRELATION_TAG));
        }

        List<Attribute> payloadAttributes = streamDefinition.getPayloadData();
        if (payloadAttributes != null && payloadAttributes.size() > 0) {
            templateEventElement.addChild(createPropertyElement(factory, "", payloadAttributes, EventPublisherConstants.EVENT_PAYLOAD_TAG));
        }

        return compositeEventElement.toString();
    }

    private static OMElement createPropertyElement(OMFactory factory, String dataPrefix,
                                                   List<Attribute> attributeList,
                                                   String propertyTag) {
        OMElement parentPropertyElement = factory.createOMElement(new QName(
                propertyTag));

        for (Attribute attribute : attributeList) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    attribute.getName()));
            propertyElement.setText(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX + dataPrefix + attribute.getName()
                    + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX);
            parentPropertyElement.addChild(propertyElement);
        }
        return parentPropertyElement;
    }

}
