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
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;

import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.sources.AnalyticsPartition;
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

    private List<String> columns;
    
    private int tenantId;
    
    private String tableName;
    
    public AnalyticsRDD() { 
        super(null, null, null);
    }
    
    public AnalyticsRDD(int tenantId, String tableName, List<String> columns, 
            SparkContext sc, Seq<Dependency<?>> deps, ClassTag<Row> evidence) {
        super(sc, deps, evidence);
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.columns = columns;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public scala.collection.Iterator<Row> compute(Partition split, TaskContext context) {
        AnalyticsPartition partition = (AnalyticsPartition) split;
        try {
            Iterator<Record> recordsItr = ServiceHolder.getAnalyticsDataService().readRecords(
                    partition.getRecordStoreName(), partition.getRecordGroup());
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
                    computePartitions(), this.columns, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
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

    private int computePartitions(){
        if (ServiceHolder.getAnalyticskExecutor() != null){
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
        
        public RowRecordIteratorAdaptor(Iterator<Record> recordItr) {
            this.recordItr = recordItr;
        }
        
        @Override
        public boolean hasNext() {
            return this.recordItr.hasNext();
        }

        @Override
        public Row next() {
            return this.recordToRow(this.recordItr.next());           
        }
        
        private Row recordToRow(Record record) {
            if (record == null) {
                return null;
            }
            Map<String, Object> recordVals = record.getValues();
            Object[] rowVals = new Object[columns.size()+1];

            //adding the timestamp for the record
            rowVals[0] = record.getTimestamp();

            for (int i = 1; i < columns.size(); i++) {
                rowVals[i] = recordVals.get(columns.get(i));
            }
//            return new GenericRow(rowVals);
            return RowFactory.create(rowVals);
        }

        @Override
        public void remove() {
            this.recordItr.remove();
        }
        
    }

}
