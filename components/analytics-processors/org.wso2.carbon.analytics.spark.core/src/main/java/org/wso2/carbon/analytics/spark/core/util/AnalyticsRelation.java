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

package org.wso2.carbon.analytics.spark.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.InsertableRelation;
import org.apache.spark.sql.sources.TableScan;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import scala.reflect.ClassTag$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    public AnalyticsRelation(int tenantId, String tableName,
                             SQLContext sqlContext) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.sqlContext = sqlContext;

        try {
            AnalyticsSchema analyticsSchema = ServiceHolder.getAnalyticsDataService().getTableSchema(
                    tenantId, tableName);
            this.schema = new StructType(extractFieldsFromColumns(analyticsSchema.getColumns()));
        } catch (AnalyticsException e) {
            log.error("Failed to load the schema for table " + tableName, e);
        }
    }

    public AnalyticsRelation(int tenantId, String tableName,
                             SQLContext sqlContext, StructType schema) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.sqlContext = sqlContext;
        this.schema = schema;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RDD<Row> buildScan() {
        return new AnalyticsRDD(this.tenantId, this.tableName,
                                new ArrayList<>(Arrays.asList(this.schema.fieldNames())),
                                this.sqlContext.sparkContext(), scala.collection.Seq$.MODULE$.empty(),
                                ClassTag$.MODULE$.<Row>apply(Row.class));
    }

    @Override
    public SQLContext sqlContext() {
        return this.sqlContext;
    }

    @Override
    public StructType schema() {
        return schema;
    }

    @Override
    public void insert(final DataFrame data, boolean overwrite) {
            final AnalyticsDataService dataService = ServiceHolder.getAnalyticsDataService();
        try {
            if (overwrite && dataService.tableExists(this.tenantId, this.tableName)) {
                AnalyticsSchema tempSchema = dataService.getTableSchema(this.tenantId, this.tableName);
                dataService.deleteTable(this.tenantId, this.tableName);
                dataService.createTable(this.tenantId, this.tableName);
                dataService.setTableSchema(this.tenantId, this.tableName, tempSchema);
            }
            data.foreachPartition(new AnalyticsFunction1(tenantId, tableName, data.schema()));
        } catch (AnalyticsException e) {
            log.error("Error while inserting data into table " + tableName, e);
            
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
        StructField[] resFields = new StructField[(columns.size())];
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

