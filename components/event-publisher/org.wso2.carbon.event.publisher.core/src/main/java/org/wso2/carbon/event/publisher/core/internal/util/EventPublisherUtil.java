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

import com.google.common.collect.ObjectArrays;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.siddhi.core.event.Event;

import java.io.File;
import java.io.UnsupportedEncodingException;
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

    public static Event convertToSiddhiEvent(org.wso2.carbon.databridge.commons.Event inputEvent) {
        Object[] data = new Object[0];
        if (inputEvent.getMetaData() != null) {
            data = ObjectArrays.concat(data, inputEvent.getMetaData(), Object.class);
        }
        if (inputEvent.getCorrelationData() != null) {
            data = ObjectArrays.concat(data, inputEvent.getCorrelationData(), Object.class);
        }
        if (inputEvent.getPayloadData() != null) {
            data = ObjectArrays.concat(data, inputEvent.getPayloadData(), Object.class);
        }

        return new Event(inputEvent.getTimeStamp(), data);
    }
}
