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

package org.wso2.carbon.event.receiver.core.internal.util;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.input.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.config.InputMapping;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventReceiverUtil {

    private static final String JVM_BIT_ARCH_SYSTEM_PROPERTY = "sun.arch.data.model";
    private static int referenceSize;

    static {
        String arch = System.getProperty(JVM_BIT_ARCH_SYSTEM_PROPERTY);
        if (arch.equals("32")) {
            //32-bit architecture
            referenceSize = 4;
        } else {
            referenceSize = 8;
        }
    }

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

    public static int getSize(Event event) {
        int size = 8; // For long timestamp field
        size += getSize(event.getStreamId());
        if (event.getMetaData() != null) {
            size += getSize(event.getMetaData());
        }
        if (event.getCorrelationData() != null) {
            size += getSize(event.getCorrelationData());
        }
        if (event.getPayloadData() != null) {
            size += getSize(event.getPayloadData());
        }
        size += referenceSize; // for the arbitrary map reference
        if (event.getArbitraryDataMap() != null) {
            size += getSize(event.getArbitraryDataMap());
        }
        return size;
    }

    private static int getSize(Object[] objects) {
        int size = 0;
        for (Object object : objects) {
            if (object != null) {
                if (object instanceof Integer) {
                    size += 4;
                } else if (object instanceof Long) {
                    size += 8;
                } else if (object instanceof Boolean) {
                    size += 1;
                } else if (object instanceof Double) {
                    size += 8;
                } else if (object instanceof Float) {
                    size += 4;
                } else if (object instanceof String) {
                    size += getSize(object.toString());
                }
            }
        }
        size += referenceSize * objects.length; // for the object reference holders
        return size;
    }

    public static int getSize(String value) {
        int size = 0;
        if (value != null) {
            try {
                size = value.getBytes("UTF8").length;
            } catch (UnsupportedEncodingException e) {
                size = value.getBytes().length;
            }
        }
        return size;
    }

    private static int getSize(Map<String, String> arbitraryDataMap) {
        int size = 0;
        if (arbitraryDataMap != null) {
            for (Map.Entry<String, String> entry : arbitraryDataMap.entrySet()) {
                size += getSize(entry.getKey());
                size += getSize(entry.getValue());
                size += referenceSize * 2; // Two string references for key and value
            }
        }
        return size;
    }

    public static String getMappedInputStreamAttributeName(String toStreamAttributeName, InputMapping inputMapping) {
        for (InputMappingAttribute inputMappingAttribute : inputMapping.getInputMappingAttributes()) {
            if (inputMappingAttribute.getToElementKey().equals(toStreamAttributeName)) {
                return inputMappingAttribute.getFromElementKey();
            }
        }
        return null;
    }

    public static Event getEventFromArray(Object[] outObjArray, StreamDefinition outStreamDefinition, Object[] metaDataArray, Object[] correlationDataArray, Object[] payloadDataArray) {
        int attributeCount = 0;
        int metaDataCount = metaDataArray.length;
        int correlationDataCount = correlationDataArray.length;
        for (Object attributeObject : outObjArray) {
            if (attributeCount < metaDataCount) {
                metaDataArray[attributeCount++] = attributeObject;
            } else if (attributeCount < (metaDataCount + correlationDataCount)) {
                correlationDataArray[attributeCount++ - metaDataCount] = attributeObject;
            } else {
                payloadDataArray[attributeCount++ - (metaDataCount + correlationDataCount)] = attributeObject;
            }
        }
        return new Event(outStreamDefinition.getStreamId(), System.currentTimeMillis(), metaDataArray, correlationDataArray, payloadDataArray);
    }


    public static void validateFilePath(String file) throws EventReceiverConfigurationException {

        Path baseDirPath = Paths.get(EventAdapterUtil.getAxisConfiguration().getRepository() + File.separator + EventReceiverConstants.ER_CONFIG_DIRECTORY);
        Path path = Paths.get(file);
        Path resolvedPath = baseDirPath.resolve(path).normalize();

        if (! resolvedPath.startsWith(baseDirPath)) {
            throw new EventReceiverConfigurationException("File name contains restricted path elements. " + file);
        }
    }

}
