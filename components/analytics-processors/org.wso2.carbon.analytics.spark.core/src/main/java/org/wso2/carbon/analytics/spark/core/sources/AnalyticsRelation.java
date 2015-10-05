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
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.rdd.AnalyticsRDD;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils;
import org.wso2.carbon.analytics.spark.core.util.CarbonScalaUtils;
import scala.collection.Iterator;
import scala.reflect.ClassTag$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a Spark SQL relation with respect to the Analytics Data Service.
 */
public class AnalyticsRelation extends BaseRelation implements TableScan,
                                                               InsertableRelation, Serializable {

    private static final long serialVersionUID = -7773419083178608517L;

    private static final Log log = LogFactory.getLog(AnalyticsRelation.class);

    private static final String DUMMY_COLUMN = "__dummy_data_type__";

    private SQLContext sqlContext;

    private StructType schema;

    private int tenantId;

    private String tableName;

    private String recordStore;

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
                             SQLContext sqlContext) {
        this.tenantId = tenantId;
        this.recordStore = recordStore;
        this.tableName = tableName;
        this.sqlContext = sqlContext;

        try {
            AnalyticsSchema analyticsSchema = ServiceHolder.getAnalyticsDataService().getTableSchema(
                    tenantId, tableName);
            if (isEmptyAnalyticsSchema(analyticsSchema)) {
                log.warn("Analytics Relation created with an empty schema for table " + this.tableName);
                this.schema = new StructType(new StructField[]{new StructField(
                        DUMMY_COLUMN, DataTypes.NullType, true, Metadata.empty())});
            } else {
                this.schema = new StructType(extractFieldsFromColumns(analyticsSchema.getColumns()));
            }
        } catch (AnalyticsException e) {
            String msg = "Failed to load the schema for table " + tableName + " : " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    private boolean isEmptyAnalyticsSchema(AnalyticsSchema analyticsSchema) {
        return analyticsSchema.getColumns() == null;
    }

    public AnalyticsRelation(int tenantId, String recordStore, String tableName,
                             SQLContext sqlContext, StructType schema) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.recordStore = recordStore;
        this.sqlContext = sqlContext;
        this.schema = schema;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RDD<Row> buildScan() {
        if (isEmptySchema(this.schema)) {
            throw new RuntimeException("Unable to scan through the table as the schema " +
                                       "is unavailable for " + this.tableName);
        }
        return new AnalyticsRDD(this.tenantId, this.tableName,
                                new ArrayList<>(Arrays.asList(this.schema.fieldNames())),
                                this.sqlContext.sparkContext(), scala.collection.Seq$.MODULE$.empty(),
                                ClassTag$.MODULE$.<Row>apply(Row.class));
    }

    private void logDebug(String s) {
        if (log.isDebugEnabled()) {
            log.debug(s);
        }
    }

    private boolean isEmptySchema(StructType schema) {
        return schema.fieldNames() == null || schema.fieldNames()[0].equals(DUMMY_COLUMN);
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

    private AnalyticsSchema analyticsSchemaFromStructType(StructType schema) {
        List<ColumnDefinition> colDefs = new ArrayList<>();
        Iterator<StructField> fieldIter = schema.iterator();
        while (fieldIter.hasNext()) {
            StructField field = fieldIter.next();
            String name = field.name();
            AnalyticsSchema.ColumnType type = AnalyticsCommonUtils.stringToColumnType(field.dataType().typeName());
            colDefs.add(new ColumnDefinition(name, type));
        }

        return new AnalyticsSchema(colDefs, Collections.<String>emptyList());
    }

    private void writeDataFrameToDAL(DataFrame data) {
        for (int i = 0; i < data.rdd().partitions().length; i++) {
            data.sqlContext().sparkContext().runJob(data.rdd(),
                                                    new AnalyticsWritingFunction(tenantId, tableName, data.schema()),
                                                    CarbonScalaUtils.getNumberSeq(i, i + 1), false,
                                                    ClassTag$.MODULE$.Unit());
        }
    }

    public Map<String, Object> convertRowAndSchemaToValuesMap(Row row, StructType schema) {
        String[] colNames = schema.fieldNames();
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < row.length(); i++) {
            result.put(colNames[i], row.get(i));
        }
        return result;
    }

    private StructField[] extractFieldsFromColumns(Map<String, ColumnDefinition> columns) {
        StructField[] resFields = new StructField[columns.size()];

        int i = 0;
        for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
            String type = entry.getValue().getType().name();
            resFields[i] = new StructField(entry.getKey(), AnalyticsCommonUtils.stringToDataType(type),
                                           true, Metadata.empty());
            i++;
        }
        return resFields;
    }

    private StructField[] extractFieldsFromString(String schemaString) {
        String[] strFields = schemaString.split(",");
        StructField[] resFields = new StructField[(strFields.length)];
        String name, type;
        String[] strFieldTokens;
        for (int i = 0; i < strFields.length; i++) {
            strFieldTokens = strFields[i].trim().split(" ");
            name = strFieldTokens[0].trim();
            type = strFieldTokens[1].trim().toLowerCase();
            StructField field = new StructField(name, AnalyticsCommonUtils.stringToDataType(type),
                                                true, Metadata.empty());
            resFields[i] = field;
        }
        return resFields;
    }
}

