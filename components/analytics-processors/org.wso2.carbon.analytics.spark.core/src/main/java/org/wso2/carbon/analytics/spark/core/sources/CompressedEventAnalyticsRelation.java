/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.Serializable;
import java.util.List;

import org.apache.spark.Dependency;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.InsertableRelation;
import org.apache.spark.sql.sources.TableScan;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.spark.core.rdd.AnalyticsRDD;
import org.wso2.carbon.analytics.spark.core.rdd.CompressedEventAnalyticsRDD;

import scala.collection.Seq;
import scala.reflect.ClassTag;

/**
 * This class represents a Spark SQL relation with respect to the Analytics Data Service.
 */
public class CompressedEventAnalyticsRelation extends AnalyticsRelation implements TableScan,
                                                               InsertableRelation, Serializable {
    private static final long serialVersionUID = -6621212018440626281L;
    
    public CompressedEventAnalyticsRelation() {
    }

    /**
     * Creates a relation between the spark table and physical DB table.
     * 
     * @param tenantId      Tenant ID
     * @param recordStore   Record Store name
     * @param tableName     Table name
     * @param sqlContext    Spark SQL Context
     * @param schema        Schema of the Table
     */
    public CompressedEventAnalyticsRelation(int tenantId, String recordStore, String tableName, SQLContext sqlContext,
            StructType schema, String incParams, boolean globalTenantAccess, String schemaString, String primaryKeys,
            boolean mergeFlag, boolean preserveOrder) {
        super(tenantId, recordStore, tableName, sqlContext, schema, incParams, globalTenantAccess, schemaString, 
            primaryKeys, mergeFlag, preserveOrder);
    }

    
    @Override
    protected AnalyticsRDD getAnalyticsRDD(int tenantId, String tableName, List<String> columns, 
            SparkContext sparkContext, Seq<Dependency<?>> deps, ClassTag<Row> evidence, long startTime, long endTime,
            boolean incEnable, String incID) {
        return new CompressedEventAnalyticsRDD(tenantId, tableName, columns, sparkContext, deps, evidence, startTime, 
            endTime, incEnable, incID);
    }
}