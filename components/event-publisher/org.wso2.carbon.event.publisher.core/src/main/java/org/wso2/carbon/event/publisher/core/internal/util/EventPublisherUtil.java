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
package org.wso2.carbon.event.publisher.core.internal.util;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherStreamValidationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.siddhi.core.event.Event;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventPublisherUtil {

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

    public static String generateFilePath(String eventPublisherName, String repositoryPath) throws EventPublisherConfigurationException {
        File repoDir = new File(repositoryPath);
        if (!repoDir.exists()) {
            synchronized (repositoryPath.intern()) {
                if (!repoDir.exists()) {
                    if (!repoDir.mkdir()) {
                        throw new EventPublisherConfigurationException("Cannot create directory to add tenant specific event publisher :" + eventPublisherName);
                    }
                }
            }
        }
        String path = repoDir.getAbsolutePath() + File.separator + EventPublisherConstants.EF_CONFIG_DIRECTORY;
        File subDir = new File(path);
        if (!subDir.exists()) {
            synchronized (path.intern()) {
                if (!subDir.exists()) {
                    if (!subDir.mkdir()) {
                        throw new EventPublisherConfigurationException("Cannot create directory " + EventPublisherConstants.EF_CONFIG_DIRECTORY + " to add tenant specific event publisher :" + eventPublisherName);
                    }
                }
            }
        }
        return subDir.getAbsolutePath() + File.separator + eventPublisherName + EventPublisherConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
    }

    public static String getImportedStreamIdFrom(
            EventPublisherConfiguration eventPublisherConfiguration) {
        String streamId = null;
        if (eventPublisherConfiguration != null && eventPublisherConfiguration.getFromStreamName() != null && !eventPublisherConfiguration.getFromStreamName().isEmpty()) {
            streamId = eventPublisherConfiguration.getFromStreamName() + EventPublisherConstants.STREAM_ID_SEPERATOR +
                    ((eventPublisherConfiguration.getFromStreamVersion() != null && !eventPublisherConfiguration.getFromStreamVersion().isEmpty()) ?
                            eventPublisherConfiguration.getFromStreamVersion() : EventPublisherConstants.DEFAULT_STREAM_VERSION);
        }

        return streamId;
    }

    public static AxisConfiguration getAxisConfiguration() {
        AxisConfiguration axisConfiguration = null;
        if (CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfiguration = EventPublisherServiceValueHolder.getConfigurationContextService().
                    getServerConfigContext().getAxisConfiguration();
        } else {
            axisConfiguration = TenantAxisUtils.getTenantAxisConfiguration(CarbonContext.
                            getThreadLocalCarbonContext().getTenantDomain(),
                    EventPublisherServiceValueHolder.getConfigurationContextService().
                            getServerConfigContext());
        }
        return axisConfiguration;
    }

    public static int getSize(Event event) {
        int size = 8; // For long timestamp field
        size += getSize(event.getData());
        size += 1; // for expired field
        return size;
    }

    public static int getSize(org.wso2.carbon.databridge.commons.Event event) {
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

    public static Event convertToSiddhiEvent(org.wso2.carbon.databridge.commons.Event inputEvent, int inputStreamSize) {
        Object[] data = new Object[inputStreamSize];
        int dataArrayCount = 0;
        if (inputEvent.getMetaData() != null) {
            for (Object attribute : inputEvent.getMetaData()) {
                data[dataArrayCount++] = attribute;
            }
        }
        if (inputEvent.getCorrelationData() != null) {
            for (Object attribute : inputEvent.getCorrelationData()) {
                data[dataArrayCount++] = attribute;
            }
        }
        if (inputEvent.getPayloadData() != null) {
            for (Object attribute : inputEvent.getPayloadData()) {
                data[dataArrayCount++] = attribute;
            }
        }
        Event event = new Event(inputEvent.getTimeStamp(), data);
        // Unchecked assignment is required to convert Map<String, String> to Map<String, Object>
        Map map = inputEvent.getArbitraryDataMap();
        event.setArbitraryDataMap(map);
        return event;
    }

    public static void validateStreamDefinitionWithOutputProperties(String actualMappingText, Map<String, Integer> propertyPositionMap, Map<String, Object> arbitraryDataMap)
            throws EventPublisherConfigurationException {
        List<String> mappingProperties = EventPublisherUtil.getOutputMappingPropertyList(actualMappingText);
        for (String property : mappingProperties) {
            if (!propertyPositionMap.containsKey(property) && (arbitraryDataMap == null || !arbitraryDataMap.containsKey(property))) {
                throw new EventPublisherStreamValidationException("Property " + property + " is neither in the input stream attributes nor in runtime arbitrary data map.");
            }
        }
    }

    public static List<String> getOutputMappingPropertyList(String mappingText) throws EventPublisherConfigurationException {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        int prefixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
        int postFixIndex;
        while (prefixIndex > 0) {
            postFixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX);
            if (postFixIndex > prefixIndex) {
                mappingTextList.add(text.substring(prefixIndex + 2, postFixIndex));
                text = text.substring(postFixIndex + 2);
            } else {
                throw new EventPublisherConfigurationException("Found template attribute prefix " + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX
                        + " without corresponding postfix " + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX + ". Please verify your template.");
            }
            prefixIndex = text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX);
        }
        return mappingTextList;
    }

    public static void validateFilePath(String fileName) throws EventPublisherConfigurationException {
        if (fileName.contains("../") || fileName.contains("..\\")) {
            throw new EventPublisherConfigurationException("File name contains restricted path elements. " + fileName);
        }
    }
}
