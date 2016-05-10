/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class contains utility methods for compressed event analytics.
 */
public class CompressedEventAnalyticsUtils {

    private static final Log log = LogFactory.getLog(CompressedEventAnalyticsUtils.class);

    /**
     * Get the values of the attributes of an event, as an array.
     * 
     * @param event         Current Event
     * @param payloadsMap   Payloads Map
     * @param eventIndex    Index of the current event
     * @param timestamp     Tmestamp of the current event
     * @param host          Host of the current event
     * @param messageFlowId Message ID of the current event
     * @return              Array of values of the fields in the event
     */
    public static Object[] getFieldValues(Map<String, Object> event, String[] outputColumns,
        Map<Integer, Map<String, String>> payloadsMap, int eventIndex, long timestamp, String host, String messageFlowId) {
        Map<String, Object> extendedRowVals = new LinkedHashMap<String, Object>();
        if (event != null) {
            for (int j = 0; j < outputColumns.length; j++) {
                String fieldName = outputColumns[j];
                Object fieldValue = event.get(fieldName);
                if (fieldName.equals(AnalyticsConstants.MESSAGE_FLOW_ID_ATTRIBUTE)) {
                    extendedRowVals.put(fieldName, messageFlowId);
                } else if (fieldName.equals(AnalyticsConstants.HOST_ATTRIBUTE)) {
                    extendedRowVals.put(fieldName, host);
                } else if (fieldName.equals(AnalyticsConstants.TIMESTAMP_FIELD)) {
                    extendedRowVals.put(fieldName, timestamp);
                } else if (fieldName.equalsIgnoreCase(AnalyticsConstants.COMPONENT_INDEX_ATTRIBUTE)) {
                    extendedRowVals.put(fieldName, eventIndex);
                } else if (fieldValue == null && payloadsMap != null && payloadsMap.containsKey(eventIndex)) {
                    extendedRowVals.put(fieldName, payloadsMap.get(eventIndex).get(fieldName));
                } else {
                    extendedRowVals.put(fieldName, fieldValue);
                }
            }
        }
        return extendedRowVals.values().toArray();
    }

    /**
     * Convert payload to map.
     * 
     * @param payloadsList  List containing payload details
     * @return              Map of payloads
     */
    public static Map<Integer, Map<String, String>> getPayloadsAsMap(ArrayList<PublishingPayload> payloadsList) {
        Map<Integer, Map<String, String>> payloadsMap = new HashMap<Integer, Map<String, String>>();
        for (int i = 0; i < payloadsList.size(); i++) {
            PublishingPayload publishingPayload = payloadsList.get(i);
            String payload = publishingPayload.getPayload();
            ArrayList<PublishingPayloadEvent> eventRefs = publishingPayload.getEvents();

            for (int j = 0; j < eventRefs.size(); j++) {
                PublishingPayloadEvent eventRef = eventRefs.get(j);
                int eventIndex = eventRef.getEventIndex();
                Map<String, String> existingPayloadMap = payloadsMap.get(eventIndex);
                if (existingPayloadMap == null) {
                    Map<String, String> attributesMap = new HashMap<String, String>();
                    attributesMap.put(eventRef.getAttribute(), payload);
                    payloadsMap.put(eventIndex, attributesMap);
                } else {
                    existingPayloadMap.put(eventRef.getAttribute(), payload);
                }
            }
        }
        return payloadsMap;
    }

    /**
     * Decompress a compressed event string.
     * 
     * @param str   Compressed string
     * @return      Decompressed string
     */
    public static ByteArrayInputStream decompress(String str) {
        ByteArrayInputStream byteInputStream = null;
        GZIPInputStream gzipInputStream = null;
        try {
            byteInputStream = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(str));
            gzipInputStream = new GZIPInputStream(byteInputStream);
            byte[] unzippedBytes = IOUtils.toByteArray(gzipInputStream);
            return new ByteArrayInputStream(unzippedBytes);
        } catch (IOException e) {
            throw new RuntimeException("Error occured while decompressing events string: " + e.getMessage(), e);
        } finally {
            try {
                if (byteInputStream != null) {
                    byteInputStream.close();
                }
                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }
            } catch (IOException e) {
                log.error("Error occured while closing streams: " + e.getMessage(), e);
            }
        }
    }
}
