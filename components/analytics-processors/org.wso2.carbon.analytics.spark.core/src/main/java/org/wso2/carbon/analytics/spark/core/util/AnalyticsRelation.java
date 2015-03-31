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

import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.sources.TableScan;
import scala.reflect.ClassTag$;

import java.io.Serializable;
import java.util.ArrayList;

import static scala.collection.JavaConversions.asJavaCollection;

/**
 * This class represents a Spark SQL relation.
 */
public class AnalyticsRelation extends TableScan implements Serializable {

    private static final long serialVersionUID = -7773419083178608517L;

    private SQLContext sqlContext;
    
    private StructType schema;
        
    private int tenantId;
    
    private String tableName;
    
    public AnalyticsRelation() { }
    
    public AnalyticsRelation(int tenantId, String tableName, 
            SQLContext sqlContext, String schemaString) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.sqlContext = sqlContext;
        this.schema = new AnalyticsSchema(schemaString);
    }

    @SuppressWarnings("unchecked")
    @Override
    public RDD<Row> buildScan() {
        return new AnalyticsRDD(this.tenantId, this.tableName,
                new ArrayList<String>(asJavaCollection(this.schema.fieldNames().toList())),
                sqlContext.sparkContext(), scala.collection.Seq$.MODULE$.empty(),
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
    
}
