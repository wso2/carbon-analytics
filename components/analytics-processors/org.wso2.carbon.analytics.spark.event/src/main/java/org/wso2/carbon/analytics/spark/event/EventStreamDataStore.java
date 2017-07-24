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
package org.wso2.carbon.analytics.spark.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.databridge.commons.Event;

/**
 * This class represents the data store used to store the events destined to specific streams.
 */
public class EventStreamDataStore {
    
    private static final Log log = LogFactory.getLog(EventStreamDataStore.class);
    
    private static final String EVENT_STREAM_DATA_STORE_TABLE = "__EVENT_STREAM_DATA_STORE__";

    private static final int META_TABLE_TID = Constants.META_INFO_TENANT_ID;
    
    private EventStreamDataStore() { }
    
    public static void initStore() throws AnalyticsException {
        AnalyticsServiceHolder.getAnalyticsDataService().createTable(META_TABLE_TID, EVENT_STREAM_DATA_STORE_TABLE);
    }
    
    public static void addToStore(List<EventRecord> eventRecordList) throws AnalyticsException {
        AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
        List<Record> records = new ArrayList<>(eventRecordList.size());
        for (EventRecord eventRecord : eventRecordList) {
            records.add(new Record(META_TABLE_TID, EVENT_STREAM_DATA_STORE_TABLE, createValues(eventRecord)));
        }
        if (records.size() > 0) {
            ads.put(records);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Map<Integer, List<Event>> extractNextEventBatch() {
        AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
        try {
            AnalyticsDataResponse resp = ads.get(META_TABLE_TID, EVENT_STREAM_DATA_STORE_TABLE, 1, null, Long.MIN_VALUE, 
                    Long.MAX_VALUE, 0, Constants.RECORDS_BATCH_SIZE);
            List<Record> records = AnalyticsDataServiceUtils.listRecords(ads, resp);
            Map<Integer, List<Event>> result = new HashMap<>();
            List<String> ids = new ArrayList<>(records.size());
            for (Record record : records) {
                Integer tenantId = (Integer) record.getValue(EventingConstants.TENANT_ID);
                String streamId = (String) record.getValue(EventingConstants.STREAM_ID);
                List<Object> payload = (List<Object>) record.getValue(EventingConstants.PAYLOAD);
                List<Object> metaData = (List<Object>) record.getValue(EventingConstants.META_DATA);
                List<Object> correlationData = (List<Object>) record.getValue(EventingConstants.CORRELATION_DATA);
                if (tenantId == null || streamId == null || payload == null) {
                    log.warn("Corrupted Spark eventing store record (ignoring): " + record);
                } else {
                    List<Event> tlist = result.get(tenantId);
                    if (tlist == null) {
                        tlist = new ArrayList<>();
                        result.put(tenantId, tlist);
                    }
                    tlist.add(buildEvent(streamId, payload, metaData, correlationData, record.getTimestamp()));
                }
                ids.add(record.getId());
            }
            /* delete the extracted records */
            ads.delete(META_TABLE_TID, EVENT_STREAM_DATA_STORE_TABLE, ids);
            return result;
        } catch (AnalyticsException e) {
            throw new RuntimeException("Error in extracting next event batch in Spark event store: " + e.getMessage(), e);
        }
    }

    private static Map<String, Object> createValues(EventRecord eventRecord) {
        Map<String, Object> result = new HashMap<>();
        result.put(EventingConstants.TENANT_ID, eventRecord.getTenantId());
        result.put(EventingConstants.STREAM_ID, eventRecord.getStreamId());
        result.put(EventingConstants.PAYLOAD, eventRecord.getPayloadEntries());
        result.put(EventingConstants.META_DATA, eventRecord.getMetaEntries());
        result.put(EventingConstants.CORRELATION_DATA, eventRecord.getCorrelationEntries());
        return result;
    }
    
    private static Event buildEvent(String streamId, List<Object> payload,
                                    List<Object> metaData, List<Object> correlationData, long ts) {
        Event event = new Event();
        event.setTimeStamp(ts);
        event.setStreamId(streamId);
        event.setPayloadData(payload.toArray());
        event.setMetaData(metaData.toArray());
        event.setCorrelationData(correlationData.toArray());
        return event;
    }
    
}
