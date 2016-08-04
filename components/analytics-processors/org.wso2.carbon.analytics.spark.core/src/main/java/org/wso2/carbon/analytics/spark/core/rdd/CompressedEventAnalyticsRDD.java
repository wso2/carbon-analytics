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

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.spark.Dependency;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.CompressedEventAnalyticsUtils;
import org.wso2.carbon.analytics.spark.core.util.PublishingPayload;

import scala.collection.Seq;
import scala.reflect.ClassTag;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

/**
 * This class represents Spark analytics RDD implementation.
 */
public class CompressedEventAnalyticsRDD extends AnalyticsRDD implements Serializable {

    private static final long serialVersionUID = 5948588299500227997L;
    private List<String> outputColumns;

    public CompressedEventAnalyticsRDD() {
        super();
    }

    /**
     * Create a Compressed Event Analytics RDD.
     * 
     * @param tenantId      Tenant ID
     * @param tableName     Name of the associated table
     * @param sc            Spark Context
     * @param deps          Scala Sequence
     * @param evidence      Class Tag
     */
    public CompressedEventAnalyticsRDD(int tenantId, String tableName, List<String> columns, SparkContext sc, 
            Seq<Dependency<?>> deps, ClassTag<Row> evidence, long timeFrom, long timeTo, boolean incEnable, 
            String incID) {
        super(tenantId, tableName, columns, sc, deps, evidence, timeFrom, timeTo, incEnable, incID);
        this.outputColumns = columns;
        this.columns = getAllColumns(columns);
    }
    
    @Override
    protected Iterator<Row> getRowRecordIteratorAdaptor(Iterator<Record> recordItr, int tenantId, boolean incEnable, String incID){
        return new CompressedEventRowRecordIteratorAdaptor(recordItr, tenantId, incEnable, incID, this.outputColumns);
    }

    /**
     * Get a list of all columns.
     * 
     * @param columns defined columns
     * @return
     */
    private List<String> getAllColumns(List<String> columns) {
        List<String> allColumns = new ArrayList<String>(columns);
        if (!allColumns.contains(AnalyticsConstants.DATA_COLUMN)) {
            allColumns.add(AnalyticsConstants.DATA_COLUMN);
        }
        allColumns.add(AnalyticsConstants.META_FIELD_COMPRESSED);
        return allColumns;
    }


    /**
     * Row iterator implementation to act as an adaptor for a record iterator.
     */
    private static class CompressedEventRowRecordIteratorAdaptor implements Iterator<Row>, Serializable {
        private static final long serialVersionUID = -8866801517386445810L;
        private Iterator<Record> recordItr;
        private Iterator<Row> rows;
        private int tenantId;
        private boolean incEnable;
        private String incID;
        private long incMaxTS = Long.MIN_VALUE;
        private int timestampIndex;
        private Kryo kryo = new Kryo();
        private List<String> columns;

        public CompressedEventRowRecordIteratorAdaptor(Iterator<Record> recordItr, int tenantId, boolean incEnable, String incID, List<String> columns) {
            this.columns = columns;
            this.recordItr = recordItr;
            this.tenantId = tenantId;
            this.incEnable = incEnable;
            this.incID = incID;
            this.timestampIndex = columns.indexOf(AnalyticsConstants.TIMESTAMP_FIELD);

            /* Class registering precedence matters. Hence intentionally giving a registration ID */
            kryo.register(HashMap.class, 111);
            kryo.register(ArrayList.class, 222);
            kryo.register(PublishingPayload.class, 333);
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
                List<List<Object>> eventsList = (List<List<Object>>) aggregatedEvent.get(
                    AnalyticsConstants.EVENTS_ATTRIBUTE);
                List<PublishingPayload> payloadsList = (List<PublishingPayload>) aggregatedEvent.get(
                    AnalyticsConstants.PAYLOADS_ATTRIBUTE);
                
                int metaTenantId = 0;
                if (recordVals.containsKey(AnalyticsConstants.META_FIELD_TENANT_ID)) {
                    metaTenantId = (int) recordVals.get(AnalyticsConstants.META_FIELD_TENANT_ID);
                }
                String host = null;
                if (aggregatedEvent.containsKey(AnalyticsConstants.HOST_ATTRIBUTE)) {
                    host = aggregatedEvent.get(AnalyticsConstants.HOST_ATTRIBUTE).toString();
                }
                // Iterate over the array of events
                for (int i = 0; i < eventsList.size(); i++) {
                    // Create a row with extended fields
                    tempRows.add(RowFactory.create(CompressedEventAnalyticsUtils.getFieldValues(this.columns, eventsList.get(i),
                        payloadsList, i, record.getTimestamp(), record.getTenantId(), metaTenantId, host)));
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