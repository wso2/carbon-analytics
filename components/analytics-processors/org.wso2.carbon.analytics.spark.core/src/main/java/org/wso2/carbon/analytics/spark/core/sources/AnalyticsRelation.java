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
import org.apache.spark.Dependency;
import org.apache.spark.SparkContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.InsertableRelation;
import org.apache.spark.sql.sources.TableScan;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.rdd.AnalyticsRDD;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants.IncrementalWindowUnit;
import org.wso2.carbon.analytics.spark.core.util.CarbonScalaUtils;
import org.wso2.carbon.analytics.spark.core.util.IncrementalUtils;
import org.wso2.carbon.base.MultitenantConstants;
import scala.collection.Seq;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private int recordBatchSize;
    private String tableName;
    private String recordStore;
    private boolean incEnable;
    private String incID;
    private int incBuffer;
    private boolean globalTenantAccess;
    private IncrementalWindowUnit windowUnit;
    private String schemaString;
    private String primaryKeys;
    private boolean mergeFlag;
    private boolean preserveOrder;

    public AnalyticsRelation() {
    }

    public AnalyticsRelation(int tenantId, String recordStore, String tableName,
                             SQLContext sqlContext, StructType schema, String incParams,
                             boolean globalTenantAccess, String schemaString, String primaryKeys, boolean mergeFlag,
                             boolean preserveOrder) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.recordStore = recordStore;
        this.sqlContext = sqlContext;
        this.schema = schema;
        setIncParams(incParams);
        this.globalTenantAccess = globalTenantAccess;
        this.schemaString = schemaString;
        this.primaryKeys = primaryKeys;
        this.mergeFlag = mergeFlag;
        this.recordBatchSize = Integer.parseInt(sqlContext.sparkContext().getConf()
                                                        .get(AnalyticsConstants.CARBON_INSERT_BATCH_SIZE));
        this.preserveOrder = preserveOrder;
    }

    private void setIncParams(String incParamStr) {
        if (!incParamStr.isEmpty()) {
            this.incEnable = true;
            logDebug("Incremental processing enabled. Setting incremental parameters " + incParamStr);
            String[] splits = incParamStr.split("\\s*,\\s*");
            if (splits.length == 1) {
                this.incID = splits[0];
            } else if (splits.length == 2) {
                this.incID = splits[0];
                this.windowUnit = IncrementalWindowUnit.valueOf(splits[1].toUpperCase());
                this.incBuffer = 1;
            } else if (splits.length == 3) {
                this.incID = splits[0];
                this.windowUnit = IncrementalWindowUnit.valueOf(splits[1].toUpperCase());
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
        long startTime;
        long endTime = Long.MAX_VALUE;
        if (this.incEnable) {
            try {
                startTime = ServiceHolder.getIncrementalMetaStore().getLastProcessedTimestamp(
                        this.tenantId, this.incID, true);
                if (startTime > 0) {
                    if (this.windowUnit != null) {
                        startTime = IncrementalUtils.getIncrementalStartTime(startTime, windowUnit, incBuffer);
                    } else {
                        startTime += 1;
                    }
                }
            } catch (AnalyticsException e) {
                throw new RuntimeException(e);
            }
        } else {
            startTime = Long.MIN_VALUE;
        }
        int targetTenantId;
        if (this.globalTenantAccess) {
            if (this.tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                targetTenantId = Constants.GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID;
            } else {
                throw new RuntimeException("Global tenant read can only be done by the super tenant");
            }
        } else {
            targetTenantId = this.tenantId;
        }
        return getAnalyticsRDD(targetTenantId, this.tableName,
                               new ArrayList<>(Arrays.asList(this.schema.fieldNames())),
                               this.sqlContext.sparkContext(), (Seq<Dependency<?>>) scala.collection.Seq$.MODULE$.empty(),
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
        int targetTenantId;
        if (this.globalTenantAccess) {
            if (this.tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                targetTenantId = Constants.GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID;
            } else {
                throw new RuntimeException("Global tenant write can only be done by the super tenant");
            }
        } else {
            targetTenantId = this.tenantId;
        }
        try {
            AnalyticsSchema tempSchema;
            try {
                tempSchema = dataService.getTableSchema(targetTenantId, this.tableName);
            } catch (AnalyticsTableNotAvailableException e) {
                tempSchema = null;
            }
            if (overwrite && !isEmptyAnalyticsSchema(tempSchema)) {
                dataService.deleteTable(targetTenantId, this.tableName);
                if (!dataService.listRecordStoreNames().contains(this.recordStore)) {
                    throw new RuntimeException("Unknown record store name " + this.recordStore);
                }
                dataService.createTable(targetTenantId, this.recordStore, this.tableName);
                dataService.setTableSchema(targetTenantId, this.tableName, tempSchema);
            }
            this.writeDataFrameToDAL(data);
        } catch (AnalyticsException e) {
            String msg = "Error while inserting data into table " + this.tableName + " : " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    private void writeDataFrameToDAL(DataFrame data) {
        if (this.preserveOrder) {
            logDebug("Inserting data with order preserved! Each partition will be written using separate jobs.");
            for (int i = 0; i < data.rdd().partitions().length; i++) {
                data.sqlContext().sparkContext().runJob(data.rdd(),
                                                        new AnalyticsWritingFunction(this.tenantId, this.tableName, data.schema(),
                                                                                     this.globalTenantAccess, this.schemaString, this.primaryKeys, this.mergeFlag,
                                                                                     this.recordStore, this.recordBatchSize), CarbonScalaUtils.getNumberSeq(i, i + 1),
                                                        false, ClassTag$.MODULE$.Unit());
            }
        } else {
            data.foreachPartition(new AnalyticsWritingFunction(this.tenantId, this.tableName, data.schema(),
                                                               this.globalTenantAccess, this.schemaString, this.primaryKeys, this.mergeFlag,
                                                               this.recordStore, this.recordBatchSize));
        }
    }

    protected AnalyticsRDD getAnalyticsRDD(int tenantId, String tableName, List<String> columns,
                                           SparkContext sparkContext, Seq<Dependency<?>> deps, ClassTag<Row> evidence,
                                           long startTime, long endTime,
                                           boolean incEnable, String incID) {
        return new AnalyticsRDD(tenantId, tableName, columns, sparkContext, deps, evidence, startTime, endTime, incEnable, incID);
    }
}

