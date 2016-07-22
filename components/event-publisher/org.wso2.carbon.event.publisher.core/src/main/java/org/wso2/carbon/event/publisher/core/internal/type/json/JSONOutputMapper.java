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

package org.wso2.carbon.event.publisher.core.internal.type.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.mapping.JSONOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.OutputMapper;
import org.wso2.carbon.event.publisher.core.internal.util.EventPublisherUtil;
import org.wso2.carbon.event.publisher.core.internal.util.RuntimeResourceLoader;
import org.wso2.siddhi.core.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSONOutputMapper implements OutputMapper {

    private List<String> mappingTextList;
    private EventPublisherConfiguration eventPublisherConfiguration = null;
    private Map<String, Integer> propertyPositionMap = null;
    private final StreamDefinition streamDefinition;
    private boolean isCustomRegistryPath;
    private final RuntimeResourceLoader runtimeResourceLoader;
    private final boolean isCustomMappingEnabled;
    private final String mappingText;

    public JSONOutputMapper(EventPublisherConfiguration eventPublisherConfiguration,
                            Map<String, Integer> propertyPositionMap, int tenantId,
                            StreamDefinition streamDefinition)
            throws EventPublisherConfigurationException {
        this.eventPublisherConfiguration = eventPublisherConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        this.streamDefinition = streamDefinition;

        JSONOutputMapping outputMapping = (JSONOutputMapping) eventPublisherConfiguration.getOutputMapping();
        this.runtimeResourceLoader = new RuntimeResourceLoader(outputMapping.getCacheTimeoutDuration(), propertyPositionMap);
        this.isCustomMappingEnabled = outputMapping.isCustomMappingEnabled();

        if (this.isCustomMappingEnabled) {
            this.mappingText = getCustomMappingText();
        } else {
            this.mappingText = generateJsonEventTemplate(streamDefinition);
        }

        if (!outputMapping.isRegistryResource()) {     // Store only if it is not from registry
            this.mappingTextList = generateMappingTextList(this.mappingText);
        }
    }

    private List<String> generateMappingTextList(String mappingText) throws EventPublisherConfigurationException {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        int prefixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
        int postFixIndex;
        while (prefixIndex > 0) {
            postFixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX);
            if (postFixIndex > prefixIndex) {
                mappingTextList.add(text.substring(0, prefixIndex));
                mappingTextList.add(text.substring(prefixIndex + 2, postFixIndex));
                text = text.substring(postFixIndex + 2);
            } else {
                throw new EventPublisherConfigurationException("Found template attribute prefix " + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX
                        + " without corresponding postfix " + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX + ". Please verify your JSON template.");
            }
            prefixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
        }
        mappingTextList.add(text);
        return mappingTextList;
    }

    @Override
    public Object convertToMappedInputEvent(Event event)
            throws EventPublisherConfigurationException {

        if (this.isCustomMappingEnabled) {
            EventPublisherUtil.validateStreamDefinitionWithOutputProperties(mappingText, propertyPositionMap, event.getArbitraryDataMap());
        }
        // Retrieve resource at runtime if it is from registry
        JSONOutputMapping outputMapping = (JSONOutputMapping) eventPublisherConfiguration.getOutputMapping();
        if (outputMapping.isRegistryResource()) {
            String path = outputMapping.getMappingText();
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
            this.mappingTextList = generateMappingTextList(actualMappingText);
        }

        StringBuilder eventText = new StringBuilder(mappingTextList.get(0));
        for (int i = 1, size = mappingTextList.size(); i < size; i++) {
            if (i % 2 == 0) {
                eventText.append(mappingTextList.get(i));
            } else {
                Object propertyValue = getPropertyValue(event, mappingTextList.get(i));
                if (propertyValue != null && propertyValue instanceof String) {
                    eventText.append(EventPublisherConstants.DOUBLE_QUOTE)
                            .append(propertyValue)
                            .append(EventPublisherConstants.DOUBLE_QUOTE);
                } else {
                    eventText.append(propertyValue);
                }
            }
        }

        String text = eventText.toString();
        if (!this.isCustomMappingEnabled) {
            Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
            if (arbitraryDataMap != null && !arbitraryDataMap.isEmpty()) {
                Gson gson = new Gson();
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = parser.parse(text).getAsJsonObject();
                jsonObject.getAsJsonObject(EventPublisherConstants.EVENT_PARENT_TAG).add(EventPublisherConstants.EVENT_ARBITRARY_DATA_MAP_TAG, gson.toJsonTree(arbitraryDataMap));
                text = gson.toJson(jsonObject);
            }
        }

        return text;
    }

    @Override
    public Object convertToTypedInputEvent(Event event)
            throws EventPublisherConfigurationException {
        return convertToMappedInputEvent(event);
    }


    private String getCustomMappingText() throws EventPublisherConfigurationException {
        JSONOutputMapping jsonOutputMapping = ((JSONOutputMapping) eventPublisherConfiguration.getOutputMapping());
        String actualMappingText = jsonOutputMapping.getMappingText();
        if (actualMappingText == null) {
            throw new EventPublisherConfigurationException("Json mapping text is empty!");
        }
        if (jsonOutputMapping.isRegistryResource()) {
            this.isCustomRegistryPath = actualMappingText.contains(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) && actualMappingText.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) > 0;
            if (!this.isCustomRegistryPath) {
                actualMappingText = this.runtimeResourceLoader.getResourceContent(jsonOutputMapping.getMappingText());
            }
        }
        return actualMappingText;
    }

    private Object getPropertyValue(Event event, String mappingProperty) {
        Object[] eventData = event.getData();
        Map<String, Object> arbitraryMap = event.getArbitraryDataMap();
        Integer position = propertyPositionMap.get(mappingProperty);
        Object data = null;

        if (position != null && eventData.length != 0) {
            data = eventData[position];
        } else if (mappingProperty != null && arbitraryMap != null && arbitraryMap.containsKey(mappingProperty)) {
            data = arbitraryMap.get(mappingProperty);
        }
        return data;
    }

    private String generateJsonEventTemplate(StreamDefinition streamDefinition) {

        JsonObject jsonEventObject = new JsonObject();
        JsonObject innerParentObject = new JsonObject();

        List<Attribute> metaDatAttributes = streamDefinition.getMetaData();
        if (metaDatAttributes != null && metaDatAttributes.size() > 0) {
            innerParentObject.add(EventPublisherConstants.EVENT_META_TAG, createPropertyElement(EventPublisherConstants.PROPERTY_META_PREFIX, metaDatAttributes));
        }

        List<Attribute> correlationAttributes = streamDefinition.getCorrelationData();
        if (correlationAttributes != null && correlationAttributes.size() > 0) {
            innerParentObject.add(EventPublisherConstants.EVENT_CORRELATION_TAG, createPropertyElement(EventPublisherConstants.PROPERTY_CORRELATION_PREFIX, correlationAttributes));
        }

        List<Attribute> payloadAttributes = streamDefinition.getPayloadData();
        if (payloadAttributes != null && payloadAttributes.size() > 0) {
            innerParentObject.add(EventPublisherConstants.EVENT_PAYLOAD_TAG, createPropertyElement("", payloadAttributes));
        }

        jsonEventObject.add(EventPublisherConstants.EVENT_PARENT_TAG, innerParentObject);

        String defaultMapping = jsonEventObject.toString();
        defaultMapping = defaultMapping.replaceAll("\"\\{\\{", "{{");
        defaultMapping = defaultMapping.replaceAll("\\}\\}\"", "}}");

        return defaultMapping;
    }

    private static JsonObject createPropertyElement(String dataPrefix,
                                                    List<Attribute> attributeList) {

        JsonObject innerObject = new JsonObject();
        for (Attribute attribute : attributeList) {
            innerObject.addProperty(attribute.getName(), EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX + dataPrefix + attribute.getName() + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX);
        }
        return innerObject;
    }


}
