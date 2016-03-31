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

package org.wso2.carbon.analytics.spark.core.sources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.InsertableRelation;
import org.apache.spark.sql.sources.TableScan;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.rdd.AnalyticsRDD;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.CarbonScalaUtils;

import scala.reflect.ClassTag$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import static org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils.extractFieldsFromColumns;
import static org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils.extractFieldsFromString;
import static org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils.isEmptyAnalyticsSchema;
import static org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils.isEmptySchema;

/**
 * This class represents a Spark SQL relation with respect to the Analytics Data Service.
 */
public class AnalyticsRelation extends BaseRelation implements TableScan,
                                                               InsertableRelation, Serializable {

    private static final long serialVersionUID = -7773419083178608517L;
    private static final Log log = LogFactory.getLog(AnalyticsRelation.class);

    private SQLContext sqlContext;
    private StructType schema;
    private int tenantId;
    private String tableName;
    private String recordStore;
    private boolean incEnable;
    private String incID;
    private long incWindowSizeMS;
    private int incBuffer;

    public AnalyticsRelation() {
    }

    @Deprecated
    public AnalyticsRelation(int tenantId, String tableName,
                             SQLContext sqlContext, String schemaString) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.sqlContext = sqlContext;
        this.schema = new StructType(extractFieldsFromString(schemaString));
    }

    public AnalyticsRelation(int tenantId, String recordStore, String tableName,
                             SQLContext sqlContext, String incParams) {
        this.tenantId = tenantId;
        this.recordStore = recordStore;
        this.tableName = tableName;
        this.sqlContext = sqlContext;

        try {
            AnalyticsSchema analyticsSchema = ServiceHolder.getAnalyticsDataService().getTableSchema(
                    tenantId, tableName);
            if (isEmptyAnalyticsSchema(analyticsSchema)) {
                log.warn(this.tableName + " table created with an empty schema. Aborting creating the relation");
                throw new RuntimeException("Analytics Relation created with an empty schema for " +
                                           "table" + this.tableName);
            } else {
                this.schema = new StructType(extractFieldsFromColumns(analyticsSchema.getColumns()));
            }
        } catch (AnalyticsException e) {
            String msg = "Failed to load the schema for table " + tableName + " : " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        setIncParams(incParams);
    }

    public AnalyticsRelation(int tenantId, String recordStore, String tableName,
                             SQLContext sqlContext, StructType schema, String incParams) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.recordStore = recordStore;
        this.sqlContext = sqlContext;
        this.schema = schema;
        setIncParams(incParams);
    }

    private void setIncParams(String incParamStr) {
        if(!incParamStr.isEmpty()) {
            this.incEnable = true;
            logDebug("Incremental processing enabled. Setting incremental parameters " + incParamStr);
            String[] splits = incParamStr.split("\\s*,\\s*");
            if (splits.length == 2) {
                this.incID = splits[0];
                this.incWindowSizeMS = Long.parseLong(splits[1]) * 1000;
                this.incBuffer = 1;
            } else if (splits.length == 3) {
                this.incID = splits[0];
                this.incWindowSizeMS = Long.parseLong(splits[1]) * 1000;
                this.incBuffer = Integer.parseInt(splits[2]);
            } else {
                String msg = "Error while setting incremental processing parameters : " + incParamStr;
                log.error(msg);
                throw new RuntimeException(msg);
            }
        } else {
            logDebug("Incremental processing disabled");
            this.incEnable = false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public RDD<Row> buildScan() {
        if (isEmptySchema(this.schema)) {
            String msg = "Unable to scan through the table as the schema " +
                         "is unavailable for " + this.tableName;
            log.error(msg);
            throw new RuntimeException(msg);
        }
        long startTime, endTime;
        if (this.incEnable) {
            try {
                startTime = ServiceHolder.getIncrementalMetaStore().getLastProcessedTimestamp(
                        this.tenantId, this.incID, true);
                if (startTime > 0) {
                    startTime -= startTime % this.incWindowSizeMS;
                    startTime -= this.incBuffer * this.incWindowSizeMS;
                }

                endTime = System.currentTimeMillis() + AnalyticsConstants.INC_END_TIME_BUFFER_MS;
            } catch (AnalyticsException e) {
                throw new RuntimeException(e);
            }
        } else {
            startTime = Long.MIN_VALUE;
            endTime = Long.MAX_VALUE;
        }
        return new AnalyticsRDD(this.tenantId, this.tableName,
                                new ArrayList<>(Arrays.asList(this.schema.fieldNames())),
                                this.sqlContext.sparkContext(), scala.collection.Seq$.MODULE$.empty(),
                                ClassTag$.MODULE$.<Row>apply(Row.class), startTime, endTime, this.incEnable,
                                this.incID);
    }

    private void logDebug(String s) {
        if (log.isDebugEnabled()) {
            log.debug(s);
        }
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
        return schema;
    }

    @Override
    public void insert(final DataFrame data, boolean overwrite) {
        AnalyticsDataService dataService = ServiceHolder.getAnalyticsDataService();
        try {
            AnalyticsSchema tempSchema = dataService.getTableSchema(this.tenantId, this.tableName);
            if (isEmptyAnalyticsSchema(tempSchema)) {
                throw new RuntimeException("Unable to insert data to the table as the AnalyticsSchema " +
                                           "is unavailable for " + this.tableName);
            }
            if (overwrite && dataService.tableExists(this.tenantId, this.tableName)) {
                dataService.deleteTable(this.tenantId, this.tableName);
                if (!dataService.listRecordStoreNames().contains(this.recordStore)) {
                    throw new RuntimeException("Unknown data store name " + this.recordStore);
                }
                dataService.createTable(this.tenantId, this.recordStore, this.tableName);
                dataService.setTableSchema(this.tenantId, this.tableName, tempSchema);
            }

            writeDataFrameToDAL(data);
        } catch (AnalyticsException e) {
            String msg = "Error while inserting data into table " + this.tableName + " : " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    private void writeDataFrameToDAL(DataFrame data) {
        for (int i = 0; i < data.rdd().partitions().length; i++) {
            data.sqlContext().sparkContext().runJob(data.rdd(),
                                                    new AnalyticsWritingFunction(tenantId, tableName, data.schema()),
                                                    CarbonScalaUtils.getNumberSeq(i, i + 1), false,
                                                    ClassTag$.MODULE$.Unit());
        }
    }




}

