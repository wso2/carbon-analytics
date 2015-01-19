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


//        System.out.println("=== Data source: RDD ===");
//        // Load a text file and convert each line to a Java Bean.
//        JavaRDD<Person> people = ctx.textFile("org.wso2.carbon.analytics.spark.test/src/main/resources/people.txt").map(
//                new Function<String, Person>() {
//                    @Override
//                    public Person call(String line) {
//                        String[] parts = line.split(",");
//
//                        Person person = new Person();
//                        person.setName(parts[0]);
//                        person.setAge(Integer.parseInt(parts[1].trim()));
//
//                        return person;
//                    }
//                });
//
//        // Apply a schema to an RDD of Java Beans and register it as a table.
//        JavaSchemaRDD schemaPeople = sqlCtx.applySchema(people, Person.class);
//        schemaPeople.registerTempTable("people");
//
//        // SQL can be run over RDDs that have been registered as tables.
//        JavaSchemaRDD teenagers = sqlCtx.sql("SELECT name FROM people WHERE age >= 13 AND age <= 19");
//
//        // The results of SQL queries are SchemaRDDs and support all the normal RDD operations.
//        // The columns of a row in the result can be accessed by ordinal.
//        List<String> teenagerNames = teenagers.map(new Function<Row, String>() {
//            @Override
//            public String call(Row row) {
//                return "Name: " + row.getString(0);
//            }
//        }).collect();
//        for (String name : teenagerNames) {
//            System.out.println(name);
//        }

    }
}
