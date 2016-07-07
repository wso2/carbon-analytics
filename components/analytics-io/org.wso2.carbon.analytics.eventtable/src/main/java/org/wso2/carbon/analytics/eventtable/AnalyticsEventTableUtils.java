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
package org.wso2.carbon.analytics.eventtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.eventtable.internal.ServiceHolder;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility operations for analytics event table.
 */
public class AnalyticsEventTableUtils {

    private static Log log = LogFactory.getLog(AnalyticsEventTableUtils.class);

    public static int putEvents(int tenantId, String tableName, List<Attribute> attrs,
                                 ComplexEventChunk<ComplexEvent> addingEventChunk) {
        List<Record> records = new ArrayList<Record>();
        ComplexEvent event;
        while (addingEventChunk.hasNext()) {
            event = addingEventChunk.next();
            records.add(streamEventToRecord(tenantId, tableName, attrs, event));
        }
        try {
            ServiceHolder.getAnalyticsDataService().put(records);
            return records.size();
        } catch (AnalyticsException e) {
            throw new IllegalStateException("Error in adding records to analytics event table: " +
                    e.getMessage(), e);
        }
    }

    private static Record streamEventToRecord(int tenantId, String tableName, List<Attribute> attrs,
                                              ComplexEvent event) {
        Map<String, Object> values = streamEventToRecordValues(attrs, event, null);
        Object timestampObj = values.remove(AnalyticsEventTableConstants.INTERNAL_TIMESTAMP_ATTRIBUTE);
        if (timestampObj != null) {
            if (timestampObj instanceof Long) {
                return new Record(tenantId, tableName, values, (Long) timestampObj);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("_timestamp in the event table's schema, is not a Long value. Using the event's timestamp..");
                }
                return new Record(tenantId, tableName, values, event.getTimestamp());
            }
        } else {
            return new Record(tenantId, tableName, values, event.getTimestamp());
        }
    }

    public static Map<String, Object> streamEventToRecordValues(List<Attribute> attrs, ComplexEvent event, UpdateAttributeMapper[] updateAttributeMappers) {
        if (updateAttributeMappers != null) {
            Map<String, Object> values = new HashMap<String, Object>();
            for (int i = 0; i < attrs.size(); i++) {
                values.put(attrs.get(i).getName(), updateAttributeMappers[i].getOutputData((StateEvent) event));
            }
            return values;
        } else {
            Object[] data = event.getOutputData();
            if (data == null) {
                return new HashMap<>(0);
            }
            Map<String, Object> values = new HashMap<String, Object>();
            for (int i = 0; i < attrs.size(); i++) {
                if (data.length > i) {
                    values.put(attrs.get(i).getName(), data[i]);
                } else {
                    break;
                }
            }
            return values;
        }
    }

    public static List<Record> getAllRecords(int tenantId, String tableName) {
        try {
            AnalyticsDataResponse resp = ServiceHolder.getAnalyticsDataService().get(
                    tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
            return AnalyticsDataServiceUtils.listRecords(ServiceHolder.getAnalyticsDataService(), resp);
        } catch (AnalyticsException e) {
            throw new IllegalStateException("Error in getting event records: " + e.getMessage(), e);
        }
    }

    public static void deleteRecords(int tenantId, String tableName, List<Record> records) {
        try {
            List<String> ids = new ArrayList<String>(records.size());
            for (Record record : records) {
                ids.add(record.getId());
            }
            ServiceHolder.getAnalyticsDataService().delete(tenantId, tableName, ids);
        } catch (AnalyticsException e) {
            throw new IllegalStateException("Error in getting deleting records: " + e.getMessage(), e);
        }
    }

    public static StreamEvent recordsToStreamEvent(List<Attribute> attrs, List<Record> records) {
        ComplexEventChunk<StreamEvent> eventChunk = new ComplexEventChunk<StreamEvent>(true);
        for (Record record : records) {
            eventChunk.add(recordToStreamEvent(attrs, record));
        }
        return eventChunk.getFirst();
    }

    public static StreamEvent recordToStreamEvent(List<Attribute> attrs, Record record) {
        StreamEvent event = new StreamEvent(0, 0, attrs.size());
        Object[] data = new Object[attrs.size()];
        Attribute attr;
        for (int i = 0; i < attrs.size(); i++) {
            attr = attrs.get(i);
            if (attr.getName().equals(AnalyticsEventTableConstants.INTERNAL_TIMESTAMP_ATTRIBUTE)) {
                data[i] = record.getTimestamp();
            } else {
                data[i] = record.getValue(attr.getName());
            }
        }
        event.setOutputData(data);
        return event;
    }

}
