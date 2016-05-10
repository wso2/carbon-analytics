/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.spark.core.rdd;

import static scala.collection.JavaConversions.asScalaIterator;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.Dependency;
import org.apache.spark.InterruptibleIterator;
import org.apache.spark.Partition;
import org.apache.spark.SparkContext;
import org.apache.spark.TaskContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse.Entry;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.sources.AnalyticsPartition;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.CompressedEventAnalyticsUtils;
import org.wso2.carbon.analytics.spark.core.util.PublishingPayload;
import org.wso2.carbon.analytics.spark.core.util.PublishingPayloadEvent;

import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.reflect.ClassTag;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

/**
 * This class represents Spark analytics RDD implementation.
 */
public class CompressedEventAnalyticsRDD extends RDD<Row> implements Serializable {

    private static final Log log = LogFactory.getLog(CompressedEventAnalyticsRDD.class);
    private static final long serialVersionUID = 5948588299500227997L;
    private List<String> allColumns;
    private List<String> outputColumns;
    private int tenantId;
    private String tableName;
    private long timeFrom;
    private long timeTo;
    private boolean incEnable;
    private String incID;

    public CompressedEventAnalyticsRDD() {
        super(null, null, null);
    }

    /**
     * Create a Compressed Event Analytics RDD.
     * 
     * @param tenantId      Tenant ID
     * @param tableName     Name of the associated table
     * @param mergeSchema   Flag to merge the existing schema and the defined schema
     * @param sc            Spark Context
     * @param deps          Scala Sequence
     * @param evidence      Class Tag
     */
    public CompressedEventAnalyticsRDD(int tenantId, String tableName, List<String> columns, 
            boolean mergeSchema, SparkContext sc, Seq<Dependency<?>> deps, ClassTag<Row> evidence, long timeFrom,
            long timeTo, boolean incEnable, String incID) {
        super(sc, deps, evidence);
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.outputColumns = new ArrayList<String>(columns);
        this.allColumns = getAllColumns(columns);
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.incEnable = incEnable;
        this.incID = incID;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public scala.collection.Iterator<Row> compute(Partition split, TaskContext context) {
        AnalyticsPartition partition = (AnalyticsPartition) split;
        try {
            Iterator<Record> recordsItr = ServiceHolder.getAnalyticsDataService().readRecords(partition
                .getRecordStoreName(), partition.getRecordGroup());
            return new InterruptibleIterator(context, asScalaIterator(new RowRecordIteratorAdaptor(recordsItr, 
                this.tenantId, this.incEnable, this.incID)));
        } catch (AnalyticsException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Seq<String> getPreferredLocations(Partition split) {
        if (split instanceof AnalyticsPartition) {
            AnalyticsPartition ap = (AnalyticsPartition) split;
            try {
                return JavaConversions.asScalaBuffer(Arrays.asList(ap.getRecordGroup().getLocations())).toList();
            } catch (AnalyticsException e) {
                log.error("Error in getting preffered location: " + e.getMessage() + " falling back to default impl."
                    , e);
                return super.getPreferredLocations(split);
            }
        } else {
            return super.getPreferredLocations(split);
        }
    }

    /**
     * Get a list of all columns.
     * 
     * @param columns defined columns
     * @return
     */
    private List<String> getAllColumns(List<String> columns) {
        if (!columns.contains(AnalyticsConstants.DATA_COLUMN)) {
            columns.add(AnalyticsConstants.DATA_COLUMN);
        }
        columns.add(AnalyticsConstants.META_FIELD_COMPRESSED);
        return columns;
    }
    
    @Override
    public Partition[] getPartitions() {
        AnalyticsDataResponse resp;
        try {
            resp = ServiceHolder.getAnalyticsDataService().get(this.tenantId, this.tableName, computePartitions(), 
                this.allColumns, timeFrom , timeTo, 0, -1);
        } catch (AnalyticsException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        List<Entry> entries = resp.getEntries();
        Partition[] result = new Partition[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            result[i] = new AnalyticsPartition(entries.get(i).getRecordStoreName(), entries.get(i).getRecordGroup(), i);
        }
        return result;
    }

    private int computePartitions() throws AnalyticsException {
        if (ServiceHolder.getAnalyticskExecutor() != null) {
            return ServiceHolder.getAnalyticskExecutor().getNumPartitionsHint();
        }
        return AnalyticsConstants.SPARK_DEFAULT_PARTITION_COUNT;
    }

    /**
     * Row iterator implementation to act as an adaptor for a record iterator.
     */
    private class RowRecordIteratorAdaptor implements Iterator<Row>, Serializable {
        private static final long serialVersionUID = -8866801517386445810L;
        private Iterator<Record> recordItr;
        private Iterator<Row> rows;
        private int tenantId;
        private boolean incEnable;
        private String incID;
        private long incMaxTS = Long.MIN_VALUE;
        private int timestampIndex;
        private Kryo kryo = new Kryo();

        public RowRecordIteratorAdaptor(Iterator<Record> recordItr, int tenantId, boolean incEnable, String incID) {
            this.recordItr = recordItr;
            this.tenantId = tenantId;
            this.incEnable = incEnable;
            this.incID = incID;
            
            /* Class registering precedence matters. Hence intentionally giving a registration ID */
            kryo.register(HashMap.class, 111);
            kryo.register(ArrayList.class, 222);
            kryo.register(PublishingPayload.class, 333);
            kryo.register(PublishingPayloadEvent.class, 444);
        }

        @Override
        public boolean hasNext() {
            boolean hasNext;
            if (this.rows == null && this.recordItr.hasNext()) {
                this.rows = this.recordToRows(this.recordItr.next());
            }
            if (this.rows == null) {
                hasNext = false;
            } else if (this.rows.hasNext()) {
                hasNext = true;
            } else {
                this.rows = null;
                hasNext = this.hasNext();
            }
            if (!hasNext && this.incEnable) {
                this.updateIncProcessingTS();
            }
            return hasNext;
        }
        
        private void updateIncProcessingTS() {
            try {
                long existingIncTS = ServiceHolder.getIncrementalMetaStore().getLastProcessedTimestamp(this.tenantId,
                    this.incID, false);
                if (existingIncTS < this.incMaxTS) {
                    ServiceHolder.getIncrementalMetaStore().setLastProcessedTimestamp(this.tenantId, this.incID,
                        this.incMaxTS, false);
                }
            } catch (AnalyticsException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @Override
        public Row next() {
            Row row;
            if (this.hasNext()) {
                row = this.rows.next();
            } else {
                row = null;
            }
            if (this.incEnable) {
                if (row.getLong(this.timestampIndex) > this.incMaxTS) {
                    this.incMaxTS = row.getLong(this.timestampIndex);
                }
            }
            return row;
        }

        /**
         * Converts a DB record to Spark Row(s). Create one ore more rows from a single record.
         * 
         * @param record    Record to be converted to row(s)
         */
        @SuppressWarnings("unchecked")
        private Iterator<Row> recordToRows(Record record) {
            List<Row> tempRows = new ArrayList<Row>();
            Map<String, Object> recordVals = record.getValues();
            if (recordVals.get(AnalyticsConstants.DATA_COLUMN) != null) {
                String eventsString = recordVals.get(AnalyticsConstants.DATA_COLUMN).toString();
                ByteArrayInputStream unzippedByteArray;
                if ((Boolean) recordVals.get(AnalyticsConstants.META_FIELD_COMPRESSED)) {
                    unzippedByteArray = CompressedEventAnalyticsUtils.decompress(eventsString);
                } else {
                    unzippedByteArray = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(eventsString));
                }
                Input input = new Input(unzippedByteArray);
                
                Map<String, Object> aggregatedEvent = this.kryo.readObject(input, HashMap.class);
                ArrayList<Map<String, Object>> eventsList = (ArrayList<Map<String, Object>>) aggregatedEvent.get(
                    AnalyticsConstants.EVENTS_ATTRIBUTE);
                ArrayList<PublishingPayload> payloadsList = (ArrayList<PublishingPayload>) aggregatedEvent.get(
                    AnalyticsConstants.PAYLOADS_ATTRIBUTE);
                Map<Integer, Map<String, String>> payloadsMap = null;
                if (payloadsList != null) {
                    payloadsMap =  CompressedEventAnalyticsUtils.getPayloadsAsMap(payloadsList);
                }
                String host = (String)aggregatedEvent.get(AnalyticsConstants.HOST_ATTRIBUTE);
                String messageFlowId = (String)aggregatedEvent.get(AnalyticsConstants.MESSAGE_FLOW_ID_ATTRIBUTE);
                // Iterate over the array of events
                for (int i = 0; i < eventsList.size(); i++) {
                    // Create a row with extended fields
                    tempRows.add(RowFactory.create(CompressedEventAnalyticsUtils.getFieldValues(eventsList.get(i), 
                        outputColumns.toArray(new String[0]), payloadsMap, i, record.getTimestamp(), host, 
                        messageFlowId)));
                }
            } else {
                tempRows.add(RowFactory.create(Collections.emptyList().toArray()));
            }
            return tempRows.iterator();
        }
        
        
        @Override
        public void remove() {
            this.recordItr.remove();
        }
    }
}