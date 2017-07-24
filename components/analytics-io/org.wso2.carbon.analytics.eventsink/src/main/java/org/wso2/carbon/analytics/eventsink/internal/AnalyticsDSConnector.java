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
package org.wso2.carbon.analytics.eventsink.internal;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.eventsink.internal.jmx.EventCounter;
import org.wso2.carbon.analytics.eventsink.internal.util.AnalyticsEventSinkConstants;
import org.wso2.carbon.analytics.eventsink.internal.util.AnalyticsEventSinkUtil;
import org.wso2.carbon.analytics.eventsink.internal.util.ServiceHolder;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Analytics data service connector which actually makes the interacts with DS.
 */
public class AnalyticsDSConnector {
    private static final Log log = LogFactory.getLog(AnalyticsDSConnector.class);

    private Gson gson;

    private AtomicInteger recordsPersisted;
    private AtomicInteger totalRecordCounter;
    private long startTime;
    private boolean isProfilePersistence;
    private int cutoff = 100000;

    public AnalyticsDSConnector() {
        gson = new Gson();
        String profileReceiver = System.getProperty("profilePersistence");
        if (profileReceiver != null && profileReceiver.equalsIgnoreCase("true")) {
            isProfilePersistence = true;
            recordsPersisted = new AtomicInteger();
            totalRecordCounter = new AtomicInteger();
            startTime = 0;
            String cutoffInput = System.getProperty("persistenceStatsCutoff");
            if (cutoffInput != null && StringUtils.isNumeric(cutoffInput)) {
                this.cutoff = Integer.parseInt(cutoffInput);
            }
        }
    }

    public void insertEvents(int tenantId, List<Event> events) throws StreamDefinitionStoreException,
            AnalyticsException {
        if (!ServiceHolder.getEventPublisherManagementService().isDrop()) {
            //In CEP HA setup the same event will be sent along the cluster, and hence only the leader
            // will need to store the event to avoid the duplicate events stored..
            List<Record> records = this.convertEventsToRecord(tenantId, events);
            EventCounter.incrementAndGet(events.size());
            startTimeMeasurement();
            ServiceHolder.getAnalyticsDataAPI().put(records);
            endTimeMeasurement(records.size());
        }
    }

    private void endTimeMeasurement(int recordCount) {
        if (isProfilePersistence) {
            recordsPersisted.addAndGet(recordCount);
            if (recordsPersisted.get() > cutoff) {
                synchronized (this) {
                    if (recordsPersisted.get() > cutoff) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();

                        long endTime = System.currentTimeMillis();
                        int currentBatchSize = recordsPersisted.getAndSet(0);
                        totalRecordCounter.addAndGet(currentBatchSize);

                        String line = "[" + dateFormat.format(date) + "] # of records : " + currentBatchSize +
                                " start timestamp : " + startTime +
                                " end time stamp : " + endTime + " Throughput is (records / sec) : " +
                                (currentBatchSize * 1000) / (endTime - startTime) + " Total Record Count : " +
                                totalRecordCounter + " \n";
                        File file = new File(CarbonUtils.getCarbonHome() + File.separator + "persistence-perf.txt");
                        if (!file.exists()) {
                            log.info("Creating the data persistence performance measurement log at : " + file.getAbsolutePath());
                        }
                        try {
                            appendToFile(IOUtils.toInputStream(line), file);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                        startTime = 0;
                    }
                }
            }
        }

    }

    public void appendToFile(final InputStream in, final File f) throws IOException {
        OutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(f, true));
            IOUtils.copy(in, stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void startTimeMeasurement() {
        if (isProfilePersistence) {
            if (startTime == 0) {
                synchronized (this) {
                    if (startTime == 0) {
                        startTime = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    private List<Record> convertEventsToRecord(int tenantId, List<Event> events)
            throws StreamDefinitionStoreException, AnalyticsException {
        List<Record> records = new ArrayList<>(events.size());
        String tableName;
        StreamDefinition streamDefinition;
        AnalyticsSchema analyticsSchema;
        AbstractStreamDefinitionStore streamDefinitionStore = ServiceHolder.getStreamDefinitionStoreService();
        if (streamDefinitionStore == null) {
            throw new AnalyticsException("Stream Definition store is not available. dropping Event");
        }
        for (Event event : events) {
            long timestamp = System.currentTimeMillis();
            streamDefinition = streamDefinitionStore.getStreamDefinition(event.getStreamId(), tenantId);
            tableName = AnalyticsEventSinkUtil.generateAnalyticsTableName(streamDefinition.getName());
            analyticsSchema = ServiceHolder.getAnalyticsDataAPI().getTableSchema(tenantId, tableName);
            Map<String, Object> eventAttributes = new HashMap<>();
            populateCommonAttributes(streamDefinition, analyticsSchema, eventAttributes);
            populateTypedAttributes(analyticsSchema, AnalyticsEventSinkConstants.EVENT_META_DATA_TYPE,
                                    streamDefinition.getMetaData(),
                                    event.getMetaData(), eventAttributes);
            populateTypedAttributes(analyticsSchema, AnalyticsEventSinkConstants.EVENT_CORRELATION_DATA_TYPE,
                                    streamDefinition.getCorrelationData(),
                                    event.getCorrelationData(), eventAttributes);
            Long payloadTimestamp = populateTypedPayloadAttributes(analyticsSchema, null,
                                                                   streamDefinition.getPayloadData(),
                                                                   event.getPayloadData(), eventAttributes);
            if (payloadTimestamp != null) {
                timestamp = payloadTimestamp;
            } else if (event.getTimeStamp() != 0L) {
                timestamp = event.getTimeStamp();
            }
            if (event.getArbitraryDataMap() != null && !event.getArbitraryDataMap().isEmpty()) {
                for (String attributeName : event.getArbitraryDataMap().keySet()) {
                    String attributeKey = "_" + attributeName;
                    eventAttributes.put(attributeKey, getRecordValue(analyticsSchema, attributeKey,
                            event.getArbitraryDataMap().get(attributeName), true));
                }
            }
            Record record = new Record(tenantId, tableName, eventAttributes, timestamp);
            if (log.isDebugEnabled()) {
                log.debug("Record being added: " + record);
            }
            records.add(record);
        }
        return records;
    }

    private void populateTypedAttributes(AnalyticsSchema schema, String type, List<Attribute> attributes, Object[] values,
                                         Map<String, Object> eventAttribute) throws AnalyticsException {
        if (attributes == null) {
            return;
        }
        int iteration = 0;
        for (Attribute attribute : attributes) {
            String attributeKey = getAttributeKey(type, attribute.getName());
            Object recordValue = getRecordValue(schema, attributeKey, values[iteration], false);
            if (recordValue != null) {
                eventAttribute.put(attributeKey, recordValue);
            }
            iteration++;
        }
    }

    private Long populateTypedPayloadAttributes(AnalyticsSchema schema, String type, List<Attribute> attributes,
                                                Object[] values, Map<String, Object> eventAttribute)
            throws AnalyticsException {
        Long timestamp = null;
        if (attributes == null) {
            return null;
        }
        int iteration = 0;
        for (Attribute attribute : attributes) {
            if (AnalyticsEventSinkConstants.PAYLOAD_TIMESTAMP.equals(attribute.getName())) {
                if (values[iteration] != null) {
                    timestamp = (Long) values[iteration];
                    continue;
                } else {
                    log.error("Timestamp value is null.");
                }
            }
            String attributeKey = getAttributeKey(type, attribute.getName());
            Object recordValue = getRecordValue(schema, attributeKey, values[iteration], false);
            if (recordValue != null) {
                eventAttribute.put(attributeKey, recordValue);
            }
            iteration++;
        }
        return timestamp;
    }

    private String getAttributeKey(String type, String attributeName) {
        if (type == null) {
            return attributeName;
        } else {
            return type + "_" + attributeName;
        }
    }

    private void populateCommonAttributes(StreamDefinition streamDefinition, AnalyticsSchema schema,
                                          Map<String, Object> eventAttributes) throws AnalyticsException {
        eventAttributes.put(AnalyticsEventSinkConstants.STREAM_VERSION_KEY, getRecordValue(schema,
                AnalyticsEventSinkConstants.STREAM_VERSION_KEY, streamDefinition.getVersion(), true));
    }

    private Object getRecordValue(AnalyticsSchema schema, String fieldName, Object fieldValue, boolean mandatoryValue)
            throws AnalyticsException {
        ColumnDefinition columnDefinition = schema.getColumns().get(fieldName);
        if (columnDefinition != null) {
            if (fieldValue instanceof String) {
                String fieldStrValue = (String) fieldValue;
                switch (columnDefinition.getType()) {
                    case STRING:
                        if (columnDefinition.isFacet()) {
                            //converting the json array to comma separated String
                            try {
                                return StringUtils.join(gson.fromJson(fieldStrValue, List.class), ',');
                            } catch (Exception e) {
                                return fieldStrValue;
                            }
                        }
                        return fieldStrValue;
                    case BINARY:
                        return GenericUtils.serializeObject(fieldStrValue);
                    case BOOLEAN:
                        return Boolean.parseBoolean(fieldStrValue);
                    case DOUBLE:
                        return Double.parseDouble(fieldStrValue);
                    case FLOAT:
                        return Float.parseFloat(fieldStrValue);
                    case INTEGER:
                        return Integer.parseInt(fieldStrValue);
                    case LONG:
                        return Long.parseLong(fieldStrValue);
                    case FACET:
                        return fieldStrValue;
                }
                return fieldValue;
            } else {
                return fieldValue;
            }
        } else if (mandatoryValue) {
            return fieldValue;
        } else {
            return null;
        }
    }
}
