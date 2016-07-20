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
import java.util.List;
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
     * @param payloadsList   Payloads Map
     * @param eventIndex    Index of the current event
     * @param timestamp     Tmestamp of the current event
     * @param host          Host of the current event
     * @return              Array of values of the fields in the event
     */
    public static Object[] getFieldValues(List<String> columns, List<Object> event, List<PublishingPayload> payloadsList,
            int eventIndex, long timestamp, int _tenantId, int metaTenantId, String host) {
        Object [] fieldsVals = new Object[columns.size()];
        int eventFieldIndex = 0;
        // Adding component attributes
        if (event != null) {
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).equals(AnalyticsConstants.TIMESTAMP_FIELD)) {
                    fieldsVals[i] = timestamp;
                } else if (columns.get(i).equals(AnalyticsConstants.TENANT_ID_FIELD)) {
                    fieldsVals[i] = _tenantId;
                } else if (columns.get(i).equals(AnalyticsConstants.META_FIELD_TENANT_ID)) {
                    fieldsVals[i] = metaTenantId;
                } else if (columns.get(i).equals(AnalyticsConstants.HOST_ATTRIBUTE)) {
                    fieldsVals[i] = host;
                } else {
                    fieldsVals[i] = event.get(eventFieldIndex);
                    eventFieldIndex++;
                }
            }
        }
        
        // Adding payloads
        if (payloadsList != null) {
            for (int j = 0 ; j < payloadsList.size() ; j++) {
                PublishingPayload publishingPalyload = payloadsList.get(j);
                String payload = publishingPalyload.getPayload();
                List<Integer> mappingAttributes = publishingPalyload.getEvents().get(eventIndex);
                if (mappingAttributes != null) {
                    for (int k = 0 ; k < mappingAttributes.size() ; k++) {
                        fieldsVals[mappingAttributes.get(k)] = payload;
                    }
                }
            }
        }
        return fieldsVals;
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
