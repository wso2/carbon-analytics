/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.spark.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.api.java.JavaSQLContext;
import org.apache.spark.sql.api.java.JavaSchemaRDD;
import org.apache.spark.sql.api.java.Row;
import org.apache.spark.sql.api.java.StructField;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the analytics query execution context.
 */
public class AnalyticsExecutionContext {

    private static final String TABLE = "table";
    
    private static final String DEFINE = "define";
    
    private static final String INSERT = "insert";
    
    private static final String INTO = "into";
        
    private static JavaSQLContext sqlCtx;
    
    public static void init() {
        SparkConf sparkConf = new SparkConf()
                .setMaster("local")
                .setAppName("CarbonAnalytics");
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        sqlCtx = new JavaSQLContext(ctx);
    }

    public static void stop(){
        sqlCtx.sqlContext().sparkContext().stop();
    }
    
    public static AnalyticsQueryResult executeQuery(int tenantId, String query) throws AnalyticsExecutionException {
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }
        String[] tokens = query.split(" ");
        if (tokens.length >= 3) {
            if (tokens[0].trim().equalsIgnoreCase(DEFINE) &&
                    tokens[1].trim().equalsIgnoreCase(TABLE)) {
                String tableName = tokens[2].trim();
                String schemaString = query.substring(tokens[0].length() + tokens[1].length() + tokens[2].length() + 3);
                try {
                    registerTable(tenantId, tableName, schemaString);
                } catch (AnalyticsException e) {
                    throw new AnalyticsExecutionException("Error in registering analytics table: " + e.getMessage(), e);
                }
                return null;
            } else if (tokens[0].trim().equalsIgnoreCase(INSERT) &&
                    tokens[1].trim().equalsIgnoreCase(INTO)) {
                String tableName = tokens[2].trim();
                String selectQuery = query.substring(tokens[0].length() + tokens[1].length() + tokens[2].length() + 3);
                try {
                    insertIntoTable(tenantId, tableName, toResult(sqlCtx.sql(selectQuery)));
                } catch (AnalyticsException e) {
                    throw new AnalyticsExecutionException("Error in executing insert into query: " + e.getMessage(), e);
                }
                return null;
            }
        }
        return toResult(sqlCtx.sql(query));
    }
    
    private static void insertIntoTable(int tenantId, String tableName, 
            AnalyticsQueryResult data) throws AnalyticsTableNotAvailableException, AnalyticsException {
        AnalyticsDataService ads = AnalyticsSparkServiceHolder.getAnalyticsDataService();
        List<Record> records = generateRecordsForTable(tenantId, tableName, data);
        ads.insert(records);
    }
    
    private static List<Record> generateRecordsForTable(int tenantId, String tableName, AnalyticsQueryResult data) {
        List<List<Object>> rows = data.getRows();
        StructField[] columns = data.getColumns();
        List<Record> result = new ArrayList<Record>(rows.size());
        for (List<Object> row : rows) {
            result.add(new Record(tenantId, tableName, extractValuesFromRow(row, columns), System.currentTimeMillis()));
        }
        return result;
    }
    
    private static Map<String, Object> extractValuesFromRow(List<Object> row, StructField[] columns) {
        Map<String, Object> result = new HashMap<String, Object>(row.size());
        for (int i = 0; i < row.size(); i++) {
            result.put(columns[i].getName(), row.get(i));
        }
        return result;
    }
    
    private static AnalyticsQueryResult toResult(JavaSchemaRDD schemaRDD) throws AnalyticsExecutionException {
        return new AnalyticsQueryResult(schemaRDD.schema().getFields(), convertRowsToObjects(schemaRDD.collect()));
    }
    
    private static List<List<Object>> convertRowsToObjects(List<Row> rows) {
        List<List<Object>> result = new ArrayList<List<Object>>();
        List<Object> objects;
        for (Row row : rows) {
            objects = new ArrayList<Object>();
            for (int i = 0; i < row.length(); i++) {
                objects.add(row.get(i));
            }
            result.add(objects);
        }
        return result;
    }
    
    private static void registerTable(int tenantId, String tableName, String schemaString) throws AnalyticsException {
        AnalyticsDataService ads = AnalyticsSparkServiceHolder.getAnalyticsDataService();
        if (!ads.tableExists(tenantId, tableName)) {
            ads.createTable(tenantId, tableName);
        }
        AnalyticsRelation table = new AnalyticsRelation(tenantId, tableName, sqlCtx, schemaString);
        JavaSchemaRDD schemaRDD = sqlCtx.baseRelationToSchemaRDD(table);
        schemaRDD.registerTempTable(tableName);
    }
    
}
