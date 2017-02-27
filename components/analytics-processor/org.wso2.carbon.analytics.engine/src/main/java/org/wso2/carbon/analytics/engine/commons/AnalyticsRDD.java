/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.engine.commons;

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
import org.wso2.carbon.analytics.data.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.data.commons.service.AnalyticsDataHolder;
import org.wso2.carbon.analytics.data.commons.service.AnalyticsDataResponse;
import org.wso2.carbon.analytics.data.commons.sources.AnalyticsCommonConstants;
import org.wso2.carbon.analytics.data.commons.sources.Record;
import org.wso2.carbon.analytics.engine.services.AnalyticsServiceHolder;
import scala.collection.Iterator;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.reflect.ClassTag;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static scala.collection.JavaConversions.asScalaIterator;

/**
 * RDD extension for DAS.
 */
public class AnalyticsRDD extends RDD<Row> {

    private static final Log log = LogFactory.getLog(AnalyticsRDD.class);
    private static final long serialVersionUID = 5948588299500227997L;

    private String tableName;
    private String incrementalId;
    private List<String> columns;
    private long timeTo;
    private long timeFrom;
    private boolean incrementalEnabled;

    public AnalyticsRDD(String tableName, List<String> columns, long timeFrom, long timeTo, boolean incrementalEnabled,
                        String incrementalId, SparkContext sc, Seq<Dependency<?>> deps, ClassTag<Row> evidence) {
        super(sc, deps, evidence);
        this.tableName = tableName;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.columns = columns;
        this.incrementalEnabled = incrementalEnabled;
        this.incrementalId = incrementalId;
    }

    @Override
    public Iterator<Row> compute(Partition split, TaskContext taskContext) {
        AnalyticsDataHolder.getInstance().setAnalyticsConfigsDir(TaskContext.get()
                .getLocalProperty(AnalyzerEngineConstants.SPARK_ANALYTICS_CONFIGS));
        AnalyticsPartition partition = (AnalyticsPartition) split;
        try {
            java.util.Iterator<Record> recordsItr = AnalyticsServiceHolder.getAnalyticsDataService().readRecords(
                    partition.getRecordStoreName(), partition.getRecordGroup());
            return new InterruptibleIterator(taskContext, asScalaIterator(getRowRecordIteratorAdaptor(recordsItr,
                    this.incrementalEnabled, this.incrementalId)));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected java.util.Iterator<Row> getRowRecordIteratorAdaptor(java.util.Iterator<Record> recordItr,
                                                                  boolean incEnable, String incID) {
        return new RowRecordIteratorAdaptor(recordItr, incEnable, incID);
    }

    @Override
    public Seq<String> getPreferredLocations(Partition split) {
        if (split instanceof AnalyticsPartition) {
            AnalyticsPartition ap = (AnalyticsPartition) split;
            try {
                return JavaConversions.asScalaBuffer(Arrays.asList(ap.getRecordGroup().getLocations())).toList();
            } catch (AnalyticsException e) {
                log.error("Error in getting preffered location: " + e.getMessage() +
                        " falling back to default impl.", e);
                return super.getPreferredLocations(split);
            }
        } else {
            return super.getPreferredLocations(split);
        }
    }

    @Override
    public Partition[] getPartitions() {
        AnalyticsDataResponse resp;
        try {
            resp = AnalyticsServiceHolder.getAnalyticsDataService().get(this.tableName,
                    computePartitions(), this.columns, timeFrom, timeTo, 0, -1);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        List<AnalyticsDataResponse.Entry> entries = resp.getEntries();
        Partition[] result = new Partition[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            result[i] = new AnalyticsPartition(entries.get(i).getRecordStoreName(), entries.get(i).getRecordGroup(), i);
        }
        return result;
    }

    private int computePartitions() throws AnalyticsException {
        //fixme: fix when analytics executor is added
        /*if (ServiceHolder.getAnalyticskExecutor() != null) {
            return ServiceHolder.getAnalyticskExecutor().getNumPartitionsHint();
        }*/
        return AnalyzerEngineConstants.SPARK_DEFAULT_PARTITION_COUNT;
    }

    /**
     * Row iterator implementation to act as an adaptor for a record iterator.
     */
    private class RowRecordIteratorAdaptor implements java.util.Iterator<Row> {

        private java.util.Iterator<Record> recordItr;
        private boolean incEnable;
        private String incID;
        private long incMaxTS = Long.MIN_VALUE;

        public RowRecordIteratorAdaptor(java.util.Iterator<Record> recordItr, boolean incEnable, String incID) {
            this.recordItr = recordItr;
            this.incEnable = incEnable;
            this.incID = incID;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = this.recordItr.hasNext();
            if (!hasNext && this.incEnable) {
                this.updateIncProcessingTS();
            }
            return hasNext;
        }

        private void updateIncProcessingTS() {
            try {
                long existingIncTS = AnalyticsServiceHolder.getIncrementalMetaStore()
                        .getLastProcessedTimestamp(this.incID, false);
                if (existingIncTS < this.incMaxTS) {
                    AnalyticsServiceHolder.getIncrementalMetaStore()
                            .setLastProcessedTimestamp(this.incID, this.incMaxTS, false);
                }
            } catch (AnalyticsException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @Override
        public Row next() {
            Record record = this.recordItr.next();
            if (this.incEnable) {
                if (record.getTimestamp() > this.incMaxTS) {
                    this.incMaxTS = record.getTimestamp();
                }
            }
            return this.recordToRow(record);
        }

        private Row recordToRow(Record record) {
            if (record == null) {
                return null;
            }
            Map<String, Object> recordVals = record.getValues();
            Object[] rowVals = new Object[columns.size()];

            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).equals(AnalyticsCommonConstants.TIMESTAMP_FIELD)) {
                    rowVals[i] = record.getTimestamp();
                } else {
                    rowVals[i] = recordVals.get(columns.get(i));
                }
            }
            return RowFactory.create(rowVals);
        }

        @Override
        public void remove() {
            this.recordItr.remove();
        }

    }

}
