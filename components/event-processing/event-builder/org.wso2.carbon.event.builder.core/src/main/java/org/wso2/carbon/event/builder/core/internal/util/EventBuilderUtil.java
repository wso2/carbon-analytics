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

package org.wso2.carbon.event.builder.core.internal.util;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.config.InputStreamConfiguration;
import org.wso2.carbon.event.builder.core.internal.type.AbstractInputMapping;
import org.wso2.carbon.event.builder.core.internal.type.wso2event.Wso2EventInputMapping;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EventBuilderUtil {

    public static String deriveEventBuilderNameFrom(String filename) {
        int beginIndex = 0;
        int endIndex = filename.lastIndexOf(EventBuilderConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT);
        if (filename.contains(File.separator)) {
            beginIndex = filename.lastIndexOf(File.separator) + 1;
        }
        return filename.substring(beginIndex, endIndex);
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

    public static String getExportedStreamIdFrom(EventBuilderConfiguration eventBuilderConfiguration) {
        String streamId = null;
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getToStreamName() != null && !eventBuilderConfiguration.getToStreamName().isEmpty()) {
            streamId = eventBuilderConfiguration.getToStreamName() + EventBuilderConstants.STREAM_NAME_VER_DELIMITER +
                    ((eventBuilderConfiguration.getToStreamVersion() != null && !eventBuilderConfiguration.getToStreamVersion().isEmpty()) ?
                            eventBuilderConfiguration.getToStreamVersion() : EventBuilderConstants.DEFAULT_STREAM_VERSION);
        }

        return streamId;
    }

    public static boolean isMetaAttribute(String attributeName) {
        return attributeName != null && attributeName.startsWith(EventBuilderConstants.META_DATA_PREFIX);
    }

    public static boolean isCorrelationAttribute(String attributeName) {
        return attributeName != null && attributeName.startsWith(EventBuilderConstants.CORRELATION_DATA_PREFIX);
    }

    public static void addAttributesToStreamDefinition(StreamDefinition streamDefinition, Attribute[] orderedAttributeArray) {
        if (orderedAttributeArray != null) {
            for (Attribute attribute : orderedAttributeArray) {
                if (attribute.getName().startsWith(EventBuilderConstants.META_DATA_PREFIX)) {
                    String attributeName = attribute.getName().substring(EventBuilderConstants.META_DATA_PREFIX.length());
                    streamDefinition.addMetaData(attributeName, attribute.getType());
                } else if (attribute.getName().startsWith(EventBuilderConstants.CORRELATION_DATA_PREFIX)) {
                    String attributeName = attribute.getName().substring(EventBuilderConstants.CORRELATION_DATA_PREFIX.length());
                    streamDefinition.addCorrelationData(attributeName, attribute.getType());
                } else {
                    streamDefinition.addPayloadData(attribute.getName(), attribute.getType());
                }
            }
        }
    }

    public static Attribute[] getOrderedAttributeArray(AbstractInputMapping inputMapping) {
        List<InputMappingAttribute> orderedInputMappingAttributes = EventBuilderUtil.sortInputMappingAttributes(inputMapping.getInputMappingAttributes());
        int currentCount = 0;
        int totalAttributeCount = orderedInputMappingAttributes.size();
        Attribute[] attributeArray = new Attribute[totalAttributeCount];
        for (InputMappingAttribute inputMappingAttribute : orderedInputMappingAttributes) {
            attributeArray[currentCount++] = new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
        }
        return attributeArray;
    }

    public static List<InputMappingAttribute> sortInputMappingAttributes(List<InputMappingAttribute> inputMappingAttributes) {
        List<InputMappingAttribute> metaAttributes = new ArrayList<InputMappingAttribute>();
        List<InputMappingAttribute> correlationAttributes = new ArrayList<InputMappingAttribute>();
        List<InputMappingAttribute> payloadAttributes = new ArrayList<InputMappingAttribute>();
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
            if (inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.META_DATA_PREFIX)) {
                metaAttributes.add(inputMappingAttribute);
            } else if (inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.CORRELATION_DATA_PREFIX)) {
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

    public static String generateFilePath(EventBuilderConfiguration eventBuilderConfiguration) throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String repositoryPath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
        String eventBuilderName = eventBuilderConfiguration.getEventBuilderName();
        File repoDir = new File(repositoryPath);
        if (!repoDir.exists()) {
            if (repoDir.mkdir()) {
                throw new EventBuilderConfigurationException("Cannot create directory to add tenant specific event builder :" + eventBuilderName);
            }
        }
        File subDir = new File(repoDir.getAbsolutePath() + File.separator + EventBuilderConstants.EB_CONFIG_DIRECTORY);
        if (!subDir.exists()) {
            if (!subDir.mkdir()) {
                throw new EventBuilderConfigurationException("Cannot create directory " + EventBuilderConstants.EB_CONFIG_DIRECTORY + " to add tenant specific event builder :" + eventBuilderName);
            }
        }
        return subDir.getAbsolutePath() + File.separator + eventBuilderName + EventBuilderConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;
    }

    public static String generateFilePath(EventBuilderConfiguration eventBuilderConfiguration,
                                          AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        String eventBuilderName = eventBuilderConfiguration.getEventBuilderName();
        File repoDir = new File(axisConfiguration.getRepository().getPath());
        if (!repoDir.exists()) {
            if (repoDir.mkdir()) {
                throw new EventBuilderConfigurationException("Cannot create directory to add tenant specific event builder :" + eventBuilderName);
            }
        }
        File subDir = new File(repoDir.getAbsolutePath() + File.separator + EventBuilderConstants.EB_CONFIG_DIRECTORY);
        if (!subDir.exists()) {
            if (!subDir.mkdir()) {
                throw new EventBuilderConfigurationException("Cannot create directory " + EventBuilderConstants.EB_CONFIG_DIRECTORY + " to add tenant specific event builder :" + eventBuilderName);
            }
        }
        return subDir.getAbsolutePath() + File.separator + eventBuilderName + EventBuilderConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;
    }

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
                attributes[index] = new Attribute(EventBuilderConstants.META_DATA_PREFIX + attribute.getName(), attribute.getType());
                index++;
            }
        }
        if (streamDefinition.getCorrelationData() != null) {
            for (Attribute attribute : streamDefinition.getCorrelationData()) {
                attributes[index] = new Attribute(EventBuilderConstants.CORRELATION_DATA_PREFIX + attribute.getName(), attribute.getType());
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

    public static EventBuilderConfiguration createDefaultEventBuilder(String streamId, String transportAdaptorName) {
        String toStreamName = DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId);
        String toStreamVersion = DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId);

        EventBuilderConfiguration eventBuilderConfiguration =
                new EventBuilderConfiguration();

        eventBuilderConfiguration.setEventBuilderName(streamId.replaceAll(":", "_") + EventBuilderConstants.DEFAULT_EVENT_BUILDER_POSTFIX);

        Wso2EventInputMapping wso2EventInputMapping = new Wso2EventInputMapping();
        wso2EventInputMapping.setCustomMappingEnabled(false);
        eventBuilderConfiguration.setInputMapping(wso2EventInputMapping);

        InputStreamConfiguration inputStreamConfiguration = new InputStreamConfiguration();
        InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration = new InputEventAdaptorMessageConfiguration();
        inputEventAdaptorMessageConfiguration.addInputMessageProperty(EventBuilderConstants.ADAPTOR_MESSAGE_STREAM_NAME, toStreamName);
        inputEventAdaptorMessageConfiguration.addInputMessageProperty(EventBuilderConstants.ADAPTOR_MESSAGE_STREAM_VERSION, toStreamVersion);
        inputStreamConfiguration.setInputEventAdaptorMessageConfiguration(inputEventAdaptorMessageConfiguration);
        inputStreamConfiguration.setInputEventAdaptorName(transportAdaptorName);
        inputStreamConfiguration.setInputEventAdaptorType(EventBuilderConstants.ADAPTOR_TYPE_WSO2EVENT);
        eventBuilderConfiguration.setInputStreamConfiguration(inputStreamConfiguration);

        eventBuilderConfiguration.setToStreamName(toStreamName);
        eventBuilderConfiguration.setToStreamVersion(toStreamVersion);

        return eventBuilderConfiguration;
    }
}
