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

package org.wso2.carbon.analytics.test;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.api.java.JavaSQLContext;
import org.apache.spark.sql.api.java.JavaSchemaRDD;
import org.apache.spark.sql.api.java.Row;
import org.wso2.carbon.analytics.spark.util.TableOperations;

import java.util.List;

/**
 * Created by niranda on 1/15/15.
 */
public class SparkTest {

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("JavaSparkSQL");
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        JavaSQLContext sqlCtx = new JavaSQLContext(ctx);

        JavaSchemaRDD tableRDD = TableOperations.getTableRDD(sqlCtx, "table1");
        tableRDD.registerTempTable("table1");

        tableRDD.printSchema();
        System.out.println("count: " + tableRDD.count());
        List<Row> result = sqlCtx.sql("SELECT * from table1").collect();
        for (Row row : result) {
            System.out.println(row.toString());
        }


        JavaSchemaRDD resultRDD = sqlCtx.sql("SELECT * from table1 where age > 20"); //where last_name = 'bishop'
        resultRDD.printSchema();
        System.out.println("result count: " + resultRDD.count());
        List<Row> result1 = resultRDD.collect();
        for (Row row : result1) {
            System.out.println(row.toString());
        }

    }
}
