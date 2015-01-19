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

import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.api.java.JavaSQLContext;
import org.apache.spark.sql.catalyst.expressions.Row;
import org.apache.spark.sql.catalyst.types.StructType;
import org.apache.spark.sql.sources.TableScan;
import org.wso2.carbon.analytics.spark.core.rdd.CarbonRDD;
import org.wso2.carbon.analytics.spark.sources.DefineSchema;
import scala.reflect.ClassTag$;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by niranda on 1/6/15.
 */
public class CarbonDatasourceRelation extends TableScan {

    private JavaSQLContext sqlContext;
    private Map<String, String> schemaMap = new HashMap<String, String>();

    // todo: decide on a proper constructor
    public CarbonDatasourceRelation(JavaSQLContext sqlContext, String tableName) {
        this.sqlContext = sqlContext;
        schemaMap.put("first_name", "String");
        schemaMap.put("last_name", "String");
    }

    @Override
    public RDD<Row> buildScan() {
        return new CarbonRDD<Row>(sqlContext.sqlContext().sparkContext(), scala.collection.Seq$.MODULE$.empty(),
                                  ClassTag$.MODULE$.<Row>apply(Row.class));
    }

    @Override
    public SQLContext sqlContext() {
        return this.sqlContext.sqlContext();
    }

    @Override
    public StructType schema() {
        // todo: make the schema compatible with all types
        // update: schema string is compatible with string, int, boolean, float, double
        // format: "<field1> <type>; <field2> <type>; ..."
        String schemaString = "first_name String; last_name String; age Int";

        return new DefineSchema(schemaString).getSchema();
    }
}
