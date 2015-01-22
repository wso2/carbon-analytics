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

import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;

/**
 * This class represents the analytics query execution context.
 */
public class AnalyticsExecutionContext {

    private static final String TABLE = "table";
    private static final String DEFINE = "define";
    
    private static AnalyticsDataService analyticsDS;
    
    private static JavaSQLContext sqlCtx;
    
    public static void init(AnalyticsDataService analyticsDS) {
        AnalyticsExecutionContext.analyticsDS = analyticsDS;
        SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("CarbonAnalytics");
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        sqlCtx = new JavaSQLContext(ctx);
    }
    
    public static AnalyticsQueryResult executeQuery(int tenantId, String query) throws AnalyticsExecutionException {
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }
        query = query.replace("  ", " ");
        String[] tokens = query.split(" ");
        if (tokens.length >= 3) {
            if (tokens[0].trim().equalsIgnoreCase(DEFINE) &&
                    tokens[1].trim().equalsIgnoreCase(TABLE)) {
                String tableName = tokens[2].trim();
                String schemaString = query.substring(tokens[0].length() + tokens[1].length() + tokens[2].length() + 3);
                registerTable(tenantId, tableName, schemaString);
                return null;
            }
        }
        return toResult(sqlCtx.sql(query));
    }
    
    private static AnalyticsQueryResult toResult(JavaSchemaRDD schemaRDD) throws AnalyticsExecutionException {
        return new AnalyticsQueryResult(schemaRDD.schema().getFields(), schemaRDD.collect());
    }
    
    private static void registerTable(int tenantId, String tableName, String schemaString) {
        AnalyticsRelation table = new AnalyticsRelation(analyticsDS, tenantId, tableName, sqlCtx, schemaString);
        JavaSchemaRDD schemaRDD = sqlCtx.baseRelationToSchemaRDD(table);
        schemaRDD.registerTempTable(tableName);
    }
    
}
