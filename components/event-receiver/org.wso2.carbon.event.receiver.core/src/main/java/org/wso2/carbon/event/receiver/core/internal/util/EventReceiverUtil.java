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
package org.wso2.carbon.event.receiver.core.internal.util;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.config.InputMapping;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EventReceiverUtil {

    public static Object getConvertedAttributeObject(String value, AttributeType type) {
        switch (type) {
            case INT:
                return Integer.valueOf(value);
            case LONG:
                return Long.valueOf(value);
            case DOUBLE:
                return Double.valueOf(value);
            case FLOAT:
                return Float.valueOf(value);
            case BOOL:
                return Boolean.valueOf(value);
            case STRING:
            default:
                return value;
        }
    }

    public static String getExportedStreamIdFrom(
            EventReceiverConfiguration eventReceiverConfiguration) {
        String streamId = null;
        if (eventReceiverConfiguration != null && eventReceiverConfiguration.getToStreamName() != null && !eventReceiverConfiguration.getToStreamName().isEmpty()) {
            streamId = eventReceiverConfiguration.getToStreamName() + EventReceiverConstants.STREAM_NAME_VER_DELIMITER +
                    ((eventReceiverConfiguration.getToStreamVersion() != null && !eventReceiverConfiguration.getToStreamVersion().isEmpty()) ?
                            eventReceiverConfiguration.getToStreamVersion() : EventReceiverConstants.DEFAULT_STREAM_VERSION);
        }

        return streamId;
    }

    public static boolean isMetaAttribute(String attributeName) {
        return attributeName != null && attributeName.startsWith(EventReceiverConstants.META_DATA_PREFIX);
    }

    public static boolean isCorrelationAttribute(String attributeName) {
        return attributeName != null && attributeName.startsWith(EventReceiverConstants.CORRELATION_DATA_PREFIX);
    }

    public static Attribute[] getOrderedAttributeArray(InputMapping inputMapping) {
        List<InputMappingAttribute> orderedInputMappingAttributes = EventReceiverUtil.sortInputMappingAttributes(inputMapping.getInputMappingAttributes());
        int currentCount = 0;
        int totalAttributeCount = orderedInputMappingAttributes.size();
        Attribute[] attributeArray = new Attribute[totalAttributeCount];
        for (InputMappingAttribute inputMappingAttribute : orderedInputMappingAttributes) {
            attributeArray[currentCount++] = new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
        }
        return attributeArray;
    }

    public static List<InputMappingAttribute> sortInputMappingAttributes(
            List<InputMappingAttribute> inputMappingAttributes) {
        List<InputMappingAttribute> metaAttributes = new ArrayList<InputMappingAttribute>();
        List<InputMappingAttribute> correlationAttributes = new ArrayList<InputMappingAttribute>();
        List<InputMappingAttribute> payloadAttributes = new ArrayList<InputMappingAttribute>();
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
            if (inputMappingAttribute.getToElementKey().startsWith(EventReceiverConstants.META_DATA_PREFIX)) {
                metaAttributes.add(inputMappingAttribute);
            } else if (inputMappingAttribute.getToElementKey().startsWith(EventReceiverConstants.CORRELATION_DATA_PREFIX)) {
                correlationAttributes.add(inputMappingAttribute);
            } else {
                payloadAttributes.add(inputMappingAttribute);
            }
        }

        List<InputMappingAttribute> orderedInputMappingAttributes = new ArrayList<InputMappingAttribute>();
        orderedInputMappingAttributes.addAll(metaAttributes);
        orderedInputMappingAttributes.addAll(correlationAttributes);
        orderedInputMappingAttributes.addAll(payloadAttributes);

        return orderedInputMappingAttributes;
    }

    public static String generateFilePath(String eventReceiverName, String repositoryPath) throws EventReceiverConfigurationException {
        File repoDir = new File(repositoryPath);
        if (!repoDir.exists()) {
            synchronized (repositoryPath.intern()) {
                if (!repoDir.exists()) {
                    if (!repoDir.mkdir()) {
                        throw new EventReceiverConfigurationException("Cannot create directory to add tenant specific event receiver :" + eventReceiverName);
                    }
                }
            }
        }
        String path = repoDir.getAbsolutePath() + File.separator + EventReceiverConstants.ER_CONFIG_DIRECTORY;
        File subDir = new File(path);
        if (!subDir.exists()) {
            synchronized (path.intern()) {
                if (!subDir.exists()) {
                    if (!subDir.mkdir()) {
                        throw new EventReceiverConfigurationException("Cannot create directory " + EventReceiverConstants.ER_CONFIG_DIRECTORY + " to add tenant specific event receiver :" + eventReceiverName);
                    }
                }
            }
        }
        return subDir.getAbsolutePath() + File.separator + eventReceiverName + EventReceiverConstants.ER_CONFIG_FILE_EXTENSION_WITH_DOT;
    }

    /**
     * Returns an array of {@link Attribute} elements derived from the stream definition. The returned attributes
     * will be prefixed with its data type (e.g. meta_, correlation_)
     *
     * @param streamDefinition the stream definition to be used to extract attributes
     * @return the array of attributes in the passed in stream with attribute names that contain prefixes
     */
    public static Attribute[] streamDefinitionToAttributeArray(StreamDefinition streamDefinition) {

        int size = 0;
        if (streamDefinition.getMetaData() != null) {
            size += streamDefinition.getMetaData().size();
        }
        if (streamDefinition.getCorrelationData() != null) {
            size += streamDefinition.getCorrelationData().size();
        }
        if (streamDefinition.getPayloadData() != null) {
            size += streamDefinition.getPayloadData().size();
        }
        Attribute[] attributes = new Attribute[size];

        int index = 0;
        if (streamDefinition.getMetaData() != null) {
            for (Attribute attribute : streamDefinition.getMetaData()) {
                attributes[index] = new Attribute(EventReceiverConstants.META_DATA_PREFIX + attribute.getName(), attribute.getType());
                index++;
            }
        }
        if (streamDefinition.getCorrelationData() != null) {
            for (Attribute attribute : streamDefinition.getCorrelationData()) {
                attributes[index] = new Attribute(EventReceiverConstants.CORRELATION_DATA_PREFIX + attribute.getName(), attribute.getType());
                index++;
            }
        }
        if (streamDefinition.getPayloadData() != null) {
            for (Attribute attribute : streamDefinition.getPayloadData()) {
                attributes[index] = new Attribute(attribute.getName(), attribute.getType());
                index++;
            }
        }
        return attributes;
    }

    /**
     * Returns the position of a given attribute in the stream.
     * Complexity : O(#attributes of stream)
     *
     * @param attributeName    attribute name. Should be in the prefixed format
     * @param streamDefinition the stream definition to search in
     * @return the position of the attribute in stream if found, or -1 if no matching attribute is found.
     */
    public static int getAttributePosition(String attributeName, StreamDefinition streamDefinition) {
        if (streamDefinition != null) {
            int metaAttributeSize = 0;
            int correlationAttributeSize = 0;
            List<Attribute> metaData = streamDefinition.getMetaData();
            List<Attribute> correlationData = streamDefinition.getCorrelationData();
            List<Attribute> payloadData = streamDefinition.getPayloadData();

            if (metaData != null) {
                metaAttributeSize = metaData.size();
            }
            if (correlationData != null) {
                correlationAttributeSize = correlationData.size();
            }

            if (attributeName.startsWith(EventReceiverConstants.META_DATA_PREFIX)) {
                if (metaData != null) {
                    for (int i = 0; i < metaAttributeSize; i++) {
                        if (metaData.get(i).getName().equals(attributeName.substring(EventReceiverConstants.META_DATA_PREFIX.length()))) {
                            return i;
                        }
                    }
                }
            } else if (attributeName.startsWith(EventReceiverConstants.CORRELATION_DATA_PREFIX)) {
                if (correlationData != null) {
                    for (int i = 0; i < correlationAttributeSize; i++) {
                        if (correlationData.get(i).getName().equals(attributeName.substring(EventReceiverConstants.CORRELATION_DATA_PREFIX.length()))) {
                            return metaAttributeSize + i;
                        }
                    }
                }
            } else {
                if (payloadData != null) {
                    for (int i = 0; i < payloadData.size(); i++) {
                        if (payloadData.get(i).getName().equals(attributeName)) {
                            return metaAttributeSize + correlationAttributeSize + i;
                        }
                    }
                }
            }
        }

        return -1;
    }
}
