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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.sources.AnalyticsPartition;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;

import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.reflect.ClassTag;

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
    private String dataColumn;

    public CompressedEventAnalyticsRDD() {
        super(null, null, null);
    }

    public CompressedEventAnalyticsRDD(int tenantId, String tableName, List<String> columns, String dataColumn, 
            boolean mergeSchema, SparkContext sc, Seq<Dependency<?>> deps, ClassTag<Row> evidence) {
        super(sc, deps, evidence);
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.allColumns = columns;
        this.outputColumns = new ArrayList<String>(columns);
        this.dataColumn = dataColumn;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public scala.collection.Iterator<Row> compute(Partition split, TaskContext context) {
        AnalyticsPartition partition = (AnalyticsPartition) split;
        try {
            Iterator<Record> recordsItr = ServiceHolder.getAnalyticsDataService().readRecords(partition
                .getRecordStoreName(), partition.getRecordGroup());
            return new InterruptibleIterator(context, asScalaIterator(new RowRecordIteratorAdaptor(recordsItr)));
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

    @Override
    public Partition[] getPartitions() {
        AnalyticsDataResponse resp;
        if (!this.dataColumn.isEmpty() && !this.allColumns.contains(this.dataColumn)) {
            this.allColumns.add(this.dataColumn);
        }
        try {
            resp = ServiceHolder.getAnalyticsDataService().get(this.tenantId, this.tableName,
                computePartitions(), this.allColumns, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
        } catch (AnalyticsException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        RecordGroup[] rgs = resp.getRecordGroups();
        Partition[] result = new Partition[rgs.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new AnalyticsPartition(resp.getRecordStoreName(), rgs[i], i);
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

        public RowRecordIteratorAdaptor(Iterator<Record> recordItr) {
            this.recordItr = recordItr;
        }

        @Override
        public boolean hasNext() {
            if (rows == null) {
                return this.recordItr.hasNext();
            }
            if (!rows.hasNext() && this.recordItr.hasNext()) {
                recordToRows(this.recordItr.next());
            }
            return rows.hasNext();
        }

        @Override
        public Row next() {
            if (rows == null || !rows.hasNext()) {
                if (!this.recordItr.hasNext()) {
                    return null;
                } else {
                    recordToRows(this.recordItr.next());
                }
            }
            return rows.next();
        }

        /**
         * Converts a DB record to Spark Row. Create one ore more rows from a single record.
         * 
         * @param record    Record to be converted to row(s)
         */
        private void recordToRows(Record record) {
            List<Row> tempRows = new ArrayList<Row>();
            Map<String, Object> recordVals = record.getValues();
            try {
                if (recordVals.get(dataColumn) != null) {
                    JSONObject eventsAggregated = new JSONObject(recordVals.get(dataColumn).toString());
                    JSONArray eventsArray = eventsAggregated.getJSONArray(AnalyticsConstants.JSON_FIELD_EVENTS);
                    
                    Map <Integer, Map<String,String>> payloadsMap = null;
                    if (eventsAggregated.has(AnalyticsConstants.JSON_FIELD_PAYLOADS)) {
                        JSONArray payloadsArray = eventsAggregated.getJSONArray(AnalyticsConstants.JSON_FIELD_PAYLOADS);
                        payloadsMap = getPayloadsAsMap(payloadsArray);
                    }
                   
                    String [] extendedFieldNames = JSONObject.getNames(eventsArray.getJSONObject(0));
                    Map<String, Object> existingRowVals = new LinkedHashMap<String, Object>();
                   
                    // Iterate over existing fields
                    for (int i = 0; i < outputColumns.size(); i++) {
                        if (outputColumns.get(i).equals(AnalyticsConstants.TIMESTAMP_FIELD)) {
                            existingRowVals.put(outputColumns.get(i), record.getTimestamp());
                        } else {
                            existingRowVals.put(outputColumns.get(i), recordVals.get(outputColumns.get(i)));
                        }
                    }
                    // Iterate over the array of events
                    for (int j = 0; j < eventsArray.length(); j++) {
                        Map<String, Object> extendedRowVals = new LinkedHashMap<String, Object>(existingRowVals);
                        // Iterate over new (split) fields and add them
                        for (int k = 0 ; k < extendedFieldNames.length ; k++) {
                            String fieldValue = eventsArray.getJSONObject(j).getString(extendedFieldNames[k]);
                            if (fieldValue == null || "null".equalsIgnoreCase(fieldValue)) {
                                if (payloadsMap != null && payloadsMap.containsKey(j)) {
                                    extendedRowVals.put(extendedFieldNames[k], payloadsMap.get(j).get(extendedFieldNames[k]));
                                } else {
                                    extendedRowVals.put(extendedFieldNames[k], null);
                                }
                            } else {
                                extendedRowVals.put(extendedFieldNames[k], fieldValue);
                            }
                        }
                        //Create a row with existing fields and extended fields
                        tempRows.add(RowFactory.create(extendedRowVals.values().toArray()));
                    }
                } else {
                    Map<String, Object> rowVals = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < outputColumns.size(); i++) {
                        if (outputColumns.get(i).equals(AnalyticsConstants.TIMESTAMP_FIELD)) {
                            rowVals.put(outputColumns.get(i), record.getTimestamp());
                        } else {
                            rowVals.put(outputColumns.get(i), recordVals.get(outputColumns.get(i)));
                        }
                    }
                    tempRows.add(RowFactory.create(rowVals.values().toArray()));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Error occured while splitting the record to rows: " + e.getMessage(), e);
            }
            rows = tempRows.iterator();
        }

        
        /**
         * Convert json payload to map
         * 
         * @param payloadsArray     JSON Array containing payload details
         * @return                  map of payloads
         */
        private Map<Integer, Map<String, String>> getPayloadsAsMap(JSONArray payloadsArray) {
            Map<Integer, Map<String, String>> payloadsMap = new HashMap<Integer, Map<String, String>>();
            for (int i = 0; i < payloadsArray.length(); i++) {
                try {
                    JSONArray eventRefs = payloadsArray.getJSONObject(i).getJSONArray(AnalyticsConstants.JSON_FIELD_EVENTS);
                    for (int j = 0; j < eventRefs.length(); j++) {
                        int eventIndex = eventRefs.getJSONObject(j).getInt(AnalyticsConstants.JSON_FIELD_EVENT_INDEX);
                        if (payloadsMap.get(eventIndex) == null) {
                            Map<String, String> attributesMap = new HashMap<String, String>();
                            attributesMap.put(eventRefs.getJSONObject(j).getString(AnalyticsConstants.
                                JSON_FIELD_ATTRIBUTE), payloadsArray.getJSONObject(i).getString(AnalyticsConstants.
                                JSON_FIELD_PAYLOAD));
                            payloadsMap.put(eventIndex, attributesMap);
                        } else {
                            payloadsMap.get(eventIndex).put(eventRefs.getJSONObject(j).getString(AnalyticsConstants.
                                JSON_FIELD_ATTRIBUTE),payloadsArray.getJSONObject(i).getString(AnalyticsConstants.
                                JSON_FIELD_PAYLOAD));
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException("Error occured while generating payload map: " + e.getMessage(), e);
                }
            }
            return payloadsMap;
        }
        
        
        @Override
        public void remove() {
            this.recordItr.remove();
        }
    }
}
