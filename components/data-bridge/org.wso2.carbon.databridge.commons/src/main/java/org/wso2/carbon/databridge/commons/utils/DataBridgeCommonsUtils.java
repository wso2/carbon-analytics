/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.databridge.commons.utils;

import org.wso2.carbon.databridge.commons.Event;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

/**
 * Data Bridge Commons Utils
 */
public class DataBridgeCommonsUtils {

    public static final String STREAM_NAME_VERSION_SPLITTER = ":";
    private static final String JVM_BIT_ARCH_SYSTEM_PROPERTY = "sun.arch.data.model";
    private static int referenceSize;

    static {
        String arch = System.getProperty(JVM_BIT_ARCH_SYSTEM_PROPERTY);
        if (arch.equals("32")) {
            // 32-bit architecture
            referenceSize = 4;
        } else {
            referenceSize = 8;
        }
    }

    public static String generateStreamId(String streamName, String streamVersion) {
        return streamName + STREAM_NAME_VERSION_SPLITTER + streamVersion;
    }

    public static String getStreamNameFromStreamId(String streamId) {
        if (streamId == null) {
            return null;
        }
        return streamId.split(STREAM_NAME_VERSION_SPLITTER)[0];
    }

    public static String getStreamVersionFromStreamId(String streamId) {
        if (streamId == null) {
            return null;
        }
        return streamId.split(STREAM_NAME_VERSION_SPLITTER)[1];
    }

    public static int getSize(Event event) {
        int size = event.getStreamId().getBytes().length;
        size += 8; // for timestamp.
        if (event.getPayloadData() != null) {
            size += getSize(event.getPayloadData());
        }
        if (event.getMetaData() != null) {
            size += getSize(event.getMetaData());
        }
        if (event.getCorrelationData() != null) {
            size += getSize(event.getCorrelationData());
        }
        if (event.getArbitraryDataMap() != null) {
            size += getSize(event.getArbitraryDataMap());
        }
        return size;
    }

    public static int getSize(Map<String, String> arbitraryData) {
        int totalSize = 0;
        Set<Map.Entry<String, String>> entrySet = arbitraryData.entrySet();
        for (Map.Entry<String, String> anEntry : entrySet) {
            totalSize += getSize(anEntry.getKey());
            totalSize += getSize(anEntry.getValue());
        }
        totalSize += referenceSize * arbitraryData.size() * 2; // for the object references.
        return totalSize;
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

    public static int getReferenceSize() {
        return referenceSize;
    }

    public static String replaceSystemProperty(String text) {
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        while (indexOfStartingChars < text.indexOf("${") && (indexOfStartingChars = text.indexOf("${")) != -1
                && (indexOfClosingBrace = text.indexOf('}')) != -1) {
            String sysProp = text.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue + text.substring(indexOfClosingBrace + 1);
            }
            if (sysProp.equals("carbon.home") && propValue != null && propValue.equals(".")) {

                text = new File(".").getAbsolutePath() + File.separator + text;
            }
        }
        return text;
    }

}
