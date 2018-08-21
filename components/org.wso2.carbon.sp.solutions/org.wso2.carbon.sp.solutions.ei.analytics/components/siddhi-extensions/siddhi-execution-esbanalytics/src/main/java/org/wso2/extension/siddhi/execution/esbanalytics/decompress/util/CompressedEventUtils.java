/*
 *  Copyright (c)  2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.esbanalytics.decompress.util;

import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.PublishingPayload;

import java.util.List;

/**
 * Utility methods to get required values from compressed events
 */
public class CompressedEventUtils {

    /**
     * Get attributes to be populated in the uncompressed message as an object array
     *
     * @param columns      List of output column names
     * @param event        event
     * @param payloadsList List of payloads
     * @param eventIndex   Index of the event
     * @param timestamp    Timestamp
     * @param metaTenantId Teanant ID field
     * @param host         Host
     * @return An array of objects of attributes to be populated in to the decompressed stream
     */
    public static Object[] getFieldValues(List<String> columns, List<Object> event,
                                          List<PublishingPayload> payloadsList, int eventIndex, long timestamp,
                                          int metaTenantId, String host) {

        Object[] fieldsVals = new Object[columns.size()];
        int eventFieldIndex = 0;
        // Adding component attributes
        if (event != null) {
            for (int i = 0; i < columns.size(); i++) {
                switch (columns.get(i)) {
                    case AnalyticsConstants.TIMESTAMP_FIELD:
                        fieldsVals[i] = timestamp;
                        break;
                    case ESBAnalyticsConstants.META_TENANT_ID_ATTRIBUTE:
                        fieldsVals[i] = metaTenantId;
                        break;
                    case AnalyticsConstants.HOST_ATTRIBUTE:
                        fieldsVals[i] = host;
                        break;
                    default:
                        fieldsVals[i] = event.get(eventFieldIndex);
                        eventFieldIndex++;
                        break;
                }
            }
        }

        // Adding payloads
        if (payloadsList != null) {
            for (PublishingPayload publishingPalyload : payloadsList) {
                String payload = publishingPalyload.getPayload();
                List<Integer> mappingAttributes = publishingPalyload.getEvents().get(eventIndex);
                if (mappingAttributes != null) {
                    for (int mappingAttribute : mappingAttributes) {
                        fieldsVals[mappingAttribute] = payload;
                    }
                }
            }
        }
        return fieldsVals;
    }
}
