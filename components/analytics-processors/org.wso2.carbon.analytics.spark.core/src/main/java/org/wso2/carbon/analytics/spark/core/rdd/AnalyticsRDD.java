/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.reflect.ClassTag;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static scala.collection.JavaConversions.asScalaIterator;

/**
 * This class represents Spark analytics RDD implementation.
 */
public class AnalyticsRDD extends RDD<Row> implements Serializable {

    private static final Log log = LogFactory.getLog(AnalyticsRDD.class);
    private static final long serialVersionUID = 5948588299500227997L;

    protected List<String> columns;
    private int tenantId;
    private String tableName;
    private long timeFrom;
    private long timeTo;
    private boolean incEnable;
    private String incID;

    public AnalyticsRDD() {
        super(null, null, null);
    }

    public AnalyticsRDD(int tenantId, String tableName, List<String> columns, SparkContext sc,
                        Seq<Dependency<?>> deps, ClassTag<Row> evidence, long timeFrom, long timeTo,
                        boolean incEnable, String incID) {
        super(sc, deps, evidence);
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.columns = columns;
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
            Iterator<Record> recordsItr = ServiceHolder.getAnalyticsDataService().readRecords(
                    partition.getRecordStoreName(), partition.getRecordGroup());
            return new InterruptibleIterator(context, asScalaIterator(getRowRecordIteratorAdaptor(recordsItr, this.tenantId, this.incEnable, this.incID)));
        } catch (AnalyticsException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    protected Iterator<Row> getRowRecordIteratorAdaptor(Iterator<Record> recordItr, int tenantId, boolean incEnable, String incID){
        return new RowRecordIteratorAdaptor(recordItr, tenantId, incEnable, incID);
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
            resp = ServiceHolder.getAnalyticsDataService().get(this.tenantId, this.tableName,
                                                               computePartitions(), this.columns, timeFrom , timeTo, 0, -1);
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

        private int tenantId;

        private boolean incEnable;

        private String incID;

        private long incMaxTS = Long.MIN_VALUE;

        public RowRecordIteratorAdaptor(Iterator<Record> recordItr, int tenantId,
                                        boolean incEnable, String incID) {
            this.recordItr = recordItr;
            this.tenantId = tenantId;
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
                long existingIncTS = ServiceHolder.getIncrementalMetaStore()
                        .getLastProcessedTimestamp(this.tenantId, this.incID, false);
                if (existingIncTS < this.incMaxTS) {
                    ServiceHolder.getIncrementalMetaStore()
                            .setLastProcessedTimestamp(this.tenantId, this.incID, this.incMaxTS, false);
                }
            } catch (AnalyticsException e) {
                throw new RuntimeException(e);
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
                if (columns.get(i).equals(AnalyticsConstants.TIMESTAMP_FIELD)) {
                    rowVals[i] = record.getTimestamp();
                } else if (columns.get(i).equals(AnalyticsConstants.TENANT_ID_FIELD)) {
                    rowVals[i] = record.getTenantId();
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
