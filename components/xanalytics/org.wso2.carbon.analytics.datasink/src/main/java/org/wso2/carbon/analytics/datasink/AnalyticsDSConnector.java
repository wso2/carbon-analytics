/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.datasink;

import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasink.internal.util.AnalyticsDatasinkConstants;
import org.wso2.carbon.analytics.datasink.internal.util.ServiceHolder;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.*;

/**
 * Analytics data service connector which actually makes the interacts with DS.
 */
public class AnalyticsDSConnector {

    public void addStream(int tenantId, StreamDefinition streamDefinition) throws AnalyticsException {
        ServiceHolder.getAnalyticsDataService().createTable(tenantId, generateTableName(streamDefinition));
    }

    public void insertEvents(int tenantId, List<Event> events) throws StreamDefinitionStoreException,
            AnalyticsException {
        ServiceHolder.getAnalyticsDataService().insert(convertEventsToRecord(tenantId, events));
    }

    private String generateTableName(StreamDefinition streamDefinition) {
        return streamDefinition.getName();
    }

    private List<Record> convertEventsToRecord(int tenantId, List<Event> events)
            throws StreamDefinitionStoreException {
        List<Record> records = new ArrayList<Record>();
        for (Event event : events) {
            long timestamp;
            StreamDefinition streamDefinition = ServiceHolder.getStreamDefinitionStoreService().
                    getStreamDefinition(event.getStreamId(), tenantId);
            Map<String, Object> eventAttributes = new HashMap<String, Object>();
            populateCommonAttributes(streamDefinition, eventAttributes);
            populateTypedAttributes(AnalyticsDatasinkConstants.EVENT_META_DATA_TYPE,
                    streamDefinition.getMetaData(),
                    event.getMetaData(), eventAttributes);
            populateTypedAttributes(AnalyticsDatasinkConstants.EVENT_CORRELATION_DATA_TYPE,
                    streamDefinition.getCorrelationData(),
                    event.getCorrelationData(), eventAttributes);
            populateTypedAttributes(AnalyticsDatasinkConstants.EVENT_PAYLOAD_DATA_TYPE,
                    streamDefinition.getPayloadData(),
                    event.getPayloadData(), eventAttributes);
            eventAttributes.putAll(event.getArbitraryDataMap());
            if (event.getTimeStamp() != 0L) timestamp = event.getTimeStamp();
            else timestamp = System.currentTimeMillis();

            Record record = new Record(tenantId, generateTableName(streamDefinition), eventAttributes, timestamp);
            records.add(record);
        }
        return records;
    }

    private void populateTypedAttributes(String type, List<Attribute> attributes, Object[] values,
                                         Map<String, Object> eventAttribute) {
        int iteration = 0;
        for (Attribute attribute : attributes) {
            String attributeKey = type + "_" + attribute.getName();
            eventAttribute.put(attributeKey, values[iteration]);
            iteration++;
        }
    }

    private void populateCommonAttributes(StreamDefinition streamDefinition, Map<String, Object> eventAttributes) {
        eventAttributes.put(AnalyticsDatasinkConstants.STREAM_VERSION_KEY, streamDefinition.getVersion());
    }

    public void deleteStream(int tenantId, StreamDefinition streamDefinition) throws AnalyticsException {
        ServiceHolder.getAnalyticsDataService().deleteTable(tenantId, generateTableName(streamDefinition));
    }
}
