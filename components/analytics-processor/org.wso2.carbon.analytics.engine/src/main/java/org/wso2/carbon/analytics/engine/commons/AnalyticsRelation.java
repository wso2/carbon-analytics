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
import org.apache.spark.SparkContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.InsertableRelation;
import org.apache.spark.sql.sources.TableScan;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.data.commons.AnalyticsDataService;
import org.wso2.carbon.analytics.data.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.data.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.data.commons.service.AnalyticsSchema;
import org.wso2.carbon.analytics.engine.exceptions.AnalyticsDataServiceLoadException;
import org.wso2.carbon.analytics.engine.services.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.engine.utils.AnalyzerEngineUtils;

import scala.collection.Seq;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.wso2.carbon.analytics.engine.utils.AnalyzerEngineUtils.isEmptyAnalyticsSchema;
import static org.wso2.carbon.analytics.engine.utils.AnalyzerEngineUtils.isEmptySchema;

/**
 * Analytics Relation class mapping DAS relations.
 */
public class AnalyticsRelation extends BaseRelation implements TableScan, InsertableRelation, Serializable {

    private static final long serialVersionUID = -7773419083178608517L;
    private static final Log log = LogFactory.getLog(AnalyticsRelation.class);

    private SQLContext sqlContext;
    private StructType schema;
    private int recordBatchSize;
    private String tableName;
    private String recordStore;
    private boolean incEnabled;
    private String incID;
    private int incBuffer;
    private String schemaString;
    private String primaryKeys;
    private boolean mergeFlag;
    private AnalyzerEngineConstants.IncrementalWindowUnit windowUnit;

    public AnalyticsRelation(String recordStore, String tableName, SQLContext sqlContext, StructType schema,
                             String incrementalParams, String schemaString, String primaryKeys, boolean mergeFlag) {
        this.tableName = tableName;
        this.recordStore = recordStore;
        this.sqlContext = sqlContext;
        this.schema = schema;
        this.schemaString = schemaString;
        this.primaryKeys = primaryKeys;
        this.mergeFlag = mergeFlag;
        this.recordBatchSize = Integer.parseInt(sqlContext.sparkContext().getConf()
                .get(AnalyzerEngineConstants.CARBON_INSERT_BATCH_SIZE));
        setIncrementalParameters(incrementalParams);
    }

    @Override
    public SQLContext sqlContext() {
        return this.sqlContext;
    }

    @Override
    public StructType schema() {
        if (isEmptySchema(this.schema)) {
            log.warn("No schema is available for table " + this.tableName);
        }
        return this.schema;
    }

    @Override
    public RDD<Row> buildScan() {
        if (isEmptySchema(this.schema)) {
            String msg = "Unable to scan through the table as the schema is unavailable for " + this.tableName;
            throw new RuntimeException(msg);
        }
        long fromTimestamp;
        long toTimestamp = Long.MAX_VALUE;
        if (this.incEnabled) {
            try {
                fromTimestamp = AnalyticsServiceHolder.getIncrementalMetaStore()
                        .getLastProcessedTimestamp(this.incID, true);
            } catch (AnalyticsException e) {
                throw new RuntimeException("Cannot access the incremental meta store! ", e);
            }
            if (fromTimestamp > 0) {
                if (this.windowUnit != null) {
                    fromTimestamp = AnalyzerEngineUtils.getIncrementalStartTime(fromTimestamp, windowUnit, incBuffer);
                } else {
                    fromTimestamp += 1;
                }
            }
        } else {
            fromTimestamp = Long.MIN_VALUE;
        }
        return getAnalyticsRDD(this.tableName,
                new ArrayList<>(Arrays.asList(this.schema.fieldNames())),
                this.sqlContext.sparkContext(), (Seq<Dependency<?>>) scala.collection.Seq$.MODULE$.empty(),
                ClassTag$.MODULE$.apply(Row.class), fromTimestamp, toTimestamp, this.incEnabled,
                this.incID);
    }

    @Override
    public void insert(Dataset<Row> dataset, boolean overwrite) {
        try {
            AnalyticsDataService dataService = AnalyticsServiceHolder.getAnalyticsDataService();
            AnalyticsSchema tempSchema;
            try {
                tempSchema = dataService.getTableSchema(this.tableName);
            } catch (AnalyticsTableNotAvailableException e) {
                tempSchema = null;
            }
            if (overwrite && !isEmptyAnalyticsSchema(tempSchema)) {
                dataService.deleteTable(this.tableName);
                if (!dataService.listRecordStoreNames().contains(this.recordStore)) {
                    throw new RuntimeException("Unknown record store " + this.recordStore);
                }
                dataService.createTable(this.recordStore, this.tableName);
                dataService.setTableSchema(this.tableName, tempSchema);
            }
            this.writeDataFrameToDAL(dataset);
        } catch (AnalyticsException e) {
            String msg = "Error while inserting data into table " + this.tableName + " : " + e.getMessage();
            throw new RuntimeException(msg, e);
        } catch (AnalyticsDataServiceLoadException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void setIncrementalParameters(String incrementalParameterString) {
        if (!incrementalParameterString.isEmpty()) {
            this.incEnabled = true;
            if (log.isDebugEnabled()) {
                log.debug("Incremental processing enabled. Setting incremental params " + incrementalParameterString);
            }
            String[] splits = incrementalParameterString.split("\\s*,\\s*");
            if (splits.length == 1) {
                this.incID = splits[0];
            } else if (splits.length == 2) {
                this.incID = splits[0];
                this.windowUnit = AnalyzerEngineConstants.IncrementalWindowUnit.valueOf(splits[1].toUpperCase());
                this.incBuffer = 1;
            } else if (splits.length == 3) {
                this.incID = splits[0];
                this.windowUnit = AnalyzerEngineConstants.IncrementalWindowUnit.valueOf(splits[1].toUpperCase());
                this.incBuffer = Integer.parseInt(splits[2]);
            } else {
                String msg = "Error while setting incremental processing parameters : " + incrementalParameterString;
                throw new RuntimeException(msg);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Incremental processing disabled");
            }
            this.incEnabled = false;
        }
    }

    private void writeDataFrameToDAL(Dataset<Row> data) {
        for (int i = 0; i < data.rdd().partitions().length; i++) {
            //fixme: add the Carbon Scala utils from Java
            data.sqlContext().sparkContext().runJob(data.rdd(), new AnalyticsDALWriter(this.tableName, data.schema(),
                    this.recordBatchSize), ClassTag$.MODULE$.Unit());
        }
    }

    protected AnalyticsRDD getAnalyticsRDD(String tableName, List<String> columns, SparkContext sparkContext,
                                           Seq<Dependency<?>> deps, ClassTag<Row> evidence, long startTime,
                                           long endTime, boolean incEnable, String incID) {
        return new AnalyticsRDD(tableName, columns, startTime, endTime, incEnable, incID, sparkContext, deps, evidence);
    }
}
