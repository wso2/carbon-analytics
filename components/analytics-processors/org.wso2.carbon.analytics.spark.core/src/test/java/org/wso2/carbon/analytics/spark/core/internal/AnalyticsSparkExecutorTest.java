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
package org.wso2.carbon.analytics.spark.core.internal;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema.ColumnType;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;
import org.wso2.carbon.base.MultitenantConstants;

import javax.naming.NamingException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AnalyticsSparkExecutorTest {

    private static final Log log = LogFactory.getLog(AnalyticsSparkExecutorTest.class);
    private AnalyticsDataService service;

    @BeforeClass
    public void setup() throws NamingException, AnalyticsException, IOException {
        GenericUtils.clearGlobalCustomDataSourceRepo();
        System.setProperty(GenericUtils.WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP, "src/test/resources/conf1");
        AnalyticsServiceHolder.setHazelcastInstance(null);
        AnalyticsServiceHolder.setAnalyticsClusterManager(new AnalyticsClusterManagerImpl());
        System.setProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
        this.service = ServiceHolder.getAnalyticsDataService();
        ServiceHolder.setAnalyticskExecutor(new SparkAnalyticsExecutor("localhost", 0));
        ServiceHolder.getAnalyticskExecutor().initializeSparkServer();
    }

    @AfterClass
    public void done() throws NamingException, AnalyticsException, IOException {
        ServiceHolder.getAnalyticskExecutor().stop();
        this.service.destroy();
        System.clearProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP);
    }

    @Test
    public void testCreateTableQuery() throws AnalyticsException {
        System.out.println(testString("start : create temp table test"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log1", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log1");
        this.service.createTable(1, "Log1");
        this.service.put(records);
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log1 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log1\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, summary STRING\"" +
                           ")");
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT ip FROM Log1");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        result = ex.executeQuery(1, "SELECT * FROM Log1");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        this.service.deleteTable(1, "Log1");
        System.out.println(testString("end : create temp table test"));
    }

    @Test
    public void testCreateTableUsingCompressedEventAnalytics() throws Exception {
        log.info(testString("start : create temp table using Compressed Event Analytics test"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        this.service.deleteTable(1, "CompressedEventsTable");
        this.service.createTable(1, "CompressedEventsTable");
        // Set schema
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("meta_compressed", ColumnType.BOOLEAN));
        columns.add(new ColumnDefinition("flowData", ColumnType.STRING));
        AnalyticsSchema compressedEventsTableSchema = new AnalyticsSchema(columns, null);
        this.service.setTableSchema(1, "CompressedEventsTable", compressedEventsTableSchema);
        // Put sample records to the table
        List<Record> records = generateRecordsForCompressedEventAnalytics(1, "CompressedEventsTable", false);
        this.service.put(records);
        ex.executeQuery(1, "CREATE TEMPORARY TABLE EventsTable USING CompressedEventAnalytics " +
                           "OPTIONS(tableName \"CompressedEventsTable\", schema \"messageFlowId STRING, compotentType STRING, " +
                           "componentName STRING, compotentIndex INT, componentId STRING, startTime LONG, endTime LONG, " +
                           "duration LONG, beforePayload STRING, afterPayload STRING, contextPropertyMap STRING, " +
                           "transportPropertyMap STRING, children STRING, entryPoint STRING, entryPointHashcode INT, faultCount INT," +
                           " hashCode INT, host STRING, _tenantId INT, _timestamp LONG\", " +
                           "incrementalParams \"EventsTable, SECOND\")");
        // Check the rows split
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM EventsTable");
        log.info(result);
        Assert.assertEquals(result.getRows().size(), 54, "Incorrect number of rows after spliting");
        // Check attribute count in a single event
        Assert.assertEquals(result.getRows().get(0).size(), 20, "Incorrect number of fileds in an event, after " +
                                                                "decompressing");
        result = ex.executeQuery(1, "SELECT * FROM EventsTable WHERE messageFlowId=\"urn_uuid_f403b0b6-4431-4a83-" +
                                    "935d-c7b72867a111\"");
        List<List<Object>> rowResults = result.getRows();
        Assert.assertEquals(rowResults.size(), 27, "Incorrect number of rows after spliting");
        // check the content of the decompressed events
        log.info("Checking row content after decompressing..");
        for (int i = 0; i < rowResults.size(); i++) {
            List<Object> fields = rowResults.get(i);
            Assert.assertEquals(fields.get(0), "urn_uuid_f403b0b6-4431-4a83-935d-c7b72867a111", "Incorrect message Id");
            String componentType;
            if (i == 0) {
                componentType = "Proxy Service";
            } else {
                componentType = "Mediator";
            }
            Assert.assertEquals(fields.get(1), componentType, "Incorrect component type.");
            Assert.assertEquals(fields.get(2), "compName" + i, "Incorrect component name.");
            Assert.assertEquals((int) fields.get(3), i, "Incorrect component index.");
            Assert.assertEquals(fields.get(4), "compId" + i, "Incorrect component Id.");
            //This is a stats-only event. Hence payload/properties should be null
            for (int j = 8; j < 12; j++) {
                Assert.assertEquals(fields.get(j), null, "Incorrect payloads/properties.");
            }
        }
        // Check whether the original schema has been changed
        AnalyticsSchema schema = this.service.getTableSchema(1, "CompressedEventsTable");
        Map<String, ColumnDefinition> schemaCols = schema.getColumns();
        Assert.assertEquals(schemaCols.size(), 2, "Compressed Events table's schema has changed after decompressing");

        // incremental processing test
        int beforeCount = ex.executeQuery(1, "SELECT * FROM EventsTable").getRows().size();
        Assert.assertEquals(beforeCount, 54, "Before count is wrong!");
        ex.executeQuery(1, "incremental_table_commit EventsTable");
        int afterCount = ex.executeQuery(1, "SELECT * FROM EventsTable").getRows().size();
        Assert.assertEquals(afterCount, 27, "After count is wrong!");
        ex.executeQuery(1, "incremental_table_reset EventsTable");

        this.service.deleteTable(1, "CompressedEventsTable");
        log.info(testString("end : create temp table using Compressed Event Analytics test"));
    }


    @Test
    public void testExecutionInsertQuery() throws AnalyticsException {
        System.out.println(testString("start : insert table test "));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log", 0, 10000, -1, -1);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.put(records);
        this.service.deleteTable(1, "Log2");
        this.service.deleteTable(1, "Log3");
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log STRING\"" +
                           ")");
        long start = System.currentTimeMillis();
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log2 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log2\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log STRING\"," +
                           "primaryKeys \"ip, log\"" +
                           ")");

        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log2 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log2\"," +
                           "schema \"server_name STRING -i, ip STRING, tenant INTEGER -sp, sequence LONG, log STRING\"," +
                           "primaryKeys \"ip, log\"" +
                           ")");

        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log3 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log3\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log STRING\"" +
                           ")");
        long end = System.currentTimeMillis();
        System.out.println("* Spark SQL define table time: " + (end - start) + " ms.");
        ex.executeQuery(1, "INSERT INTO TABLE Log2 SELECT * FROM Log");
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM Log2");
        Assert.assertEquals(result.getRows().size(), 10000);
        //with the given composite primary key, it should just update the next insert
        start = System.currentTimeMillis();
        ex.executeQuery(1, "INSERT INTO TABLE Log2 SELECT * FROM Log");
        end = System.currentTimeMillis();
        System.out.println("* Spark SQL insert/update table time: " + (end - start) + " ms.");
        result = ex.executeQuery(1, "SELECT * FROM Log2");
        Assert.assertEquals(result.getRows().size(), 10000);
        //insert to a table without a primary key
        ex.executeQuery(1, "INSERT INTO TABLE Log3 SELECT * FROM Log");
        result = ex.executeQuery(1, "SELECT * FROM Log3");
        Assert.assertEquals(result.getRows().size(), 10000);
        ex.executeQuery(1, "INSERT INTO TABLE Log3 SELECT * FROM Log");
        result = ex.executeQuery(1, "SELECT * FROM Log3");
        Assert.assertEquals(result.getRows().size(), 20000);

        this.service.deleteTable(1, "Log");
        this.service.deleteTable(1, "Log2");
        this.service.deleteTable(1, "Log3");
        System.out.println(testString("end : insert table test "));
    }

    @Test
    public void testMultiTenantQueryExecution() throws AnalyticsException {
        System.out.println(testString("start : multi tenancy test "));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        String commonTableName = "log4";
        List<Record> records1 = AnalyticsRecordStoreTest.generateRecords(-1234, commonTableName, 0, 10, -1, -1);
        List<Record> records2 = AnalyticsRecordStoreTest.generateRecords(2, commonTableName, 0, 2000, -1, -1);

        this.service.deleteTable(-1234, commonTableName);
        this.service.deleteTable(2, commonTableName);
        this.service.createTable(-1234, commonTableName);
        this.service.createTable(2, commonTableName);
        this.service.put(records1);
        this.service.put(records2);

        //test supertenant queries
        ex.executeQuery(-1234, "CREATE TEMPORARY TABLE log4 USING CarbonAnalytics " +
                               "OPTIONS" +
                               "(tableName \"log4\"," +
                               "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log4 STRING\"" +
                               ")");
        AnalyticsQueryResult result = ex.executeQuery(-1234, "SELECT * FROM log4");
        Assert.assertEquals(result.getRows().size(), 10);

        //test tenant queries
        ex.executeQuery(2, "CREATE TEMPORARY TABLE log4 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"log4\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log4 STRING\"" +
                           ")");
        result = ex.executeQuery(2, "SELECT * FROM log4");
        Assert.assertEquals(result.getRows().size(), 2000);

        //test <table>.<column name> queries
        ex.executeQuery(-1234, "CREATE TEMPORARY TABLE log44 USING CarbonAnalytics " +
                               "OPTIONS" +
                               "(tableName \"log4\"," +
                               "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log4 STRING\"" +
                               ")");
        result = ex.executeQuery(-1234, "SELECT log4.ip FROM log4 UNION SELECT log44.ip FROM log44");
        Assert.assertEquals(result.getRows().size(), 1);

        //test single letter table queries ex; table name = "t"
        ex.executeQuery(-1234, "CREATE TEMPORARY TABLE t USING CarbonAnalytics " +
                               "OPTIONS" +
                               "(tableName \"log4\"," +
                               "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log4 STRING\"" +
                               ")");
        result = ex.executeQuery(-1234, "SELECT t.ip FROM t");
        Assert.assertEquals(result.getRows().size(), 10);

        //test queries with table names and tables alias
        result = ex.executeQuery(-1234, "select * from ( select * from log4 ) t1 full outer join " +
                                        "( select * from log44 ) t2 on t1.ip = t2.ip");
        Assert.assertEquals(result.getRows().size(), 10 * 10);

        this.service.deleteTable(-1234, "log4");
        this.service.deleteTable(-1234, "log44");
        this.service.deleteTable(-1234, "t");
        this.service.deleteTable(-1234, "t1");
        this.service.deleteTable(-1234, "t2");
        this.service.deleteTable(2, "log4");
        System.out.println(testString("end : multi tenancy test "));
    }


    @Test
    public void testSparkUdfTest() throws AnalyticsException {
        System.out.println(testString("start : spark udf test"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT stringLengthTest('test') ");
        Assert.assertEquals(result.getRows().get(0).get(0), 4);
        System.out.println(result.getRows().get(0).get(0));
        System.out.println(testString("end : spark udf test"));
    }

    @Test
    public void testSparkUdafTest() throws AnalyticsException, InterruptedException {
        System.out.println(testString("start : spark udaf test"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        this.service.deleteTable(1, "Log5");
        String query = "CREATE TEMPORARY TABLE Log5 USING CarbonAnalytics " +
                       "OPTIONS" +
                       "(tableName \"Log5\"," +
                       "schema \"member DOUBLE\"" +
                       ")";
        ex.executeQuery(1, query);
        for (int i = 1; i < 6; i++) {
            ex.executeQuery(1, "INSERT INTO TABLE Log5 SELECT " + Math.pow(2, i));
        }
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT geometricMean(member) FROM Log5");
        Assert.assertEquals(Math.round(Double.parseDouble(result.getRows().get(0).get(0).toString())), 8L);
        AnalyticsQueryResult result2 = ex.executeQuery(1, "SELECT harmonicMean(member) FROM Log5");
        Assert.assertEquals(Math.round(Double.parseDouble(result2.getRows().get(0).get(0).toString()) * 100.0) / 100.0, 5.16);
        this.cleanupTable(1, "Log5");
        System.out.println(testString("end : spark udaf test"));
    }

    @Test
    public void testCreateTableWithColumnOptions() throws AnalyticsException {
        System.out.println(testString("start : create temp table with column options test"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log5", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log5");
        this.service.createTable(1, "Log5");
        this.service.put(records);
        String query = "CREATE TEMPORARY TABLE Log5 USING CarbonAnalytics " +
                       "OPTIONS" +
                       "(tableName \"Log5\"," +
                       "schema \"server_name STRING, ip STRING -i, tenant INTEGER -sp, sequence LONG -i, summary STRING\", " +
                       "primaryKeys \"ip, log\"" +
                       ")";
        ex.executeQuery(1, query);
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT ip FROM Log5");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);


        query = "CREATE TEMPORARY TABLE Log5 USING CarbonAnalytics " +
                "OPTIONS" +
                "(tableName \"Log5\"," +
                "schema \"server_name STRING -sp, ip STRING, tenant INTEGER, sequence LONG, summary STRING\", " +
                "primaryKeys \"ip, log\"" +
                ")";
        boolean success = false;
        try {
            ex.executeQuery(1, query);
        } catch (Exception e) {
            System.out.println("Query failed with : " + e.getMessage());
            success = true;
        }
        Assert.assertTrue(success, "Query did not fail!");

        query = "CREATE TEMPORARY TABLE Log5 USING CarbonAnalytics " +
                "OPTIONS" +
                "(tableName \"Log5\"," +
                "schema \"server_name STRING -XX, ip STRING, tenant INTEGER, sequence LONG, summary STRING\", " +
                "primaryKeys \"ip, log\"" +
                ")";
        success = false;
        try {
            ex.executeQuery(1, query);
        } catch (Exception e) {
            System.out.println("Query failed with : " + e.getMessage());
            success = true;
        }
        Assert.assertTrue(success, "Query did not fail!");

        query = "CREATE TEMPORARY TABLE Log5 USING CarbonAnalytics " +
                "OPTIONS" +
                "(tableName \"Log5\"," +
                "schema \"server_name STRING -xx xx, ip STRING, tenant INTEGER, sequence LONG, summary STRING\", " +
                "primaryKeys \"ip, log\"" +
                ")";
        success = false;
        try {
            ex.executeQuery(1, query);
        } catch (Exception e) {
            System.out.println("Query failed with : " + e.getMessage());
            success = true;
        }
        Assert.assertTrue(success, "Query did not fail!");

        this.service.deleteTable(1, "Log5");
        System.out.println(testString("end : create temp table with column options test"));
    }

    @Test
    public void testCreateTableWithMultipleRecordStores() throws AnalyticsException {
        System.out.println(testString("start : create temp table with multiple record stores test"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        this.cleanupTable(1, "Log6");

        List<String> recordStoreNames = this.service.listRecordStoreNames();
        Assert.assertTrue(recordStoreNames.size() > 1, "Only single data store is available");

        String query = "CREATE TEMPORARY TABLE Log6 USING CarbonAnalytics " +
                       "OPTIONS" +
                       "(tableName \"Log6\"," +
                       "schema \"server_name STRING, ip STRING -i, tenant INTEGER -sp, sequence LONG -i, summary STRING\", " +
                       "primaryKeys \"ip, log\"" +
                       ")";
        ex.executeQuery(1, query);

        Assert.assertEquals(this.service.getRecordStoreNameByTable(1, "Log6"), "PROCESSED_DATA_STORE",
                            "Table is not created in PROCESSED_DATA_STORE by default");
        this.cleanupTable(1, "Log6");


        query = "CREATE TEMPORARY TABLE Log6 USING CarbonAnalytics " +
                "OPTIONS" +
                "(tableName \"Log6\"," +
                "schema \"server_name STRING, ip STRING -i, tenant INTEGER -sp, sequence LONG -i, summary STRING\", " +
                "recordStore \"EVENT_STORE\", " +
                "primaryKeys \"ip, log\"" +
                ")";
        ex.executeQuery(1, query);

        Assert.assertEquals(this.service.getRecordStoreNameByTable(1, "Log6"), "EVENT_STORE",
                            "Table is not created in EVENT_STORE");
        this.cleanupTable(1, "Log6");

        query = "CREATE TEMPORARY TABLE Log6 USING CarbonAnalytics " +
                "OPTIONS" +
                "(tableName \"Log6\"," +
                "schema \"server_name STRING, ip STRING -i, tenant INTEGER -sp, sequence LONG -i, summary STRING\", " +
                "recordStore \"XXX\", " +
                "primaryKeys \"ip, log\"" +
                ")";
        boolean success = false;
        try {
            ex.executeQuery(1, query);
        } catch (Exception e) {
            System.out.println("Query failed with : " + e.getMessage());
            success = true;
        }
        Assert.assertTrue(success, "Query did not fail!");
        this.cleanupTable(1, "Log6");

        System.out.println(testString("end : create temp table with multiple record stores test"));
    }


    @Test
    public void testTimestampRetrivability() throws AnalyticsException, InterruptedException {
        System.out.println(testString("start : Test Time stamp retrievability"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log7", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log7");
        this.service.deleteTable(1, "Log77");
        this.service.deleteTable(1, "Log777");
        this.service.deleteTable(1, "Log7777");
        this.service.createTable(1, "Log7");
        this.service.put(records);
        List<ColumnDefinition> colDefs = new ArrayList<>();
        colDefs.add(new ColumnDefinition("server_name", AnalyticsSchema.ColumnType.STRING));
        colDefs.add(new ColumnDefinition("ip", AnalyticsSchema.ColumnType.STRING));
        colDefs.add(new ColumnDefinition("tenant", AnalyticsSchema.ColumnType.INTEGER));
        colDefs.add(new ColumnDefinition("sequence", AnalyticsSchema.ColumnType.LONG));
        colDefs.add(new ColumnDefinition("summary", AnalyticsSchema.ColumnType.STRING));
        AnalyticsSchema schema = new AnalyticsSchema(colDefs, Collections.<String>emptyList());
        this.service.setTableSchema(1, "Log7", schema);

        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log7 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log7\"" +
                           ")");
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM Log7");
        Assert.assertEquals(result.getRows().size(), 10);
        Assert.assertEquals(result.getColumns().length, 5);
        Assert.assertEquals(Arrays.asList(result.getColumns()).contains("_timestamp"), false);
        System.out.println(result);

        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log7 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log7\"," +
                           "schema \"_timestamp LONG, server_name STRING, ip STRING, tenant INTEGER, sequence LONG, summary STRING\"" +
                           ")");
        result = ex.executeQuery(1, "SELECT _timestamp FROM Log7");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        result = ex.executeQuery(1, "SELECT * FROM Log7");
        Assert.assertEquals(result.getRows().size(), 10);
        Assert.assertEquals(result.getColumns().length, 6);
        System.out.println(result);

        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log7 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log7\"," +
                           "schema \"server_name STRING, ip STRING, _timestamp LONG, tenant INTEGER, sequence LONG, summary STRING\"" +
                           ")");
        result = ex.executeQuery(1, "SELECT _timestamp, ip FROM Log7");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        result = ex.executeQuery(1, "SELECT * FROM Log7");
        Assert.assertEquals(result.getRows().size(), 10);
        Assert.assertEquals(Arrays.asList(result.getColumns()).contains("_timestamp"), true);
        System.out.println(result);


        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log77 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log77\"," +
                           "schema \"server_name STRING, ip STRING\"" +
                           ")");
        ex.executeQuery(1, "insert into table Log77 SELECT server_name, ip FROM Log7 limit 5");
        result = ex.executeQuery(1, "SELECT server_name, ip FROM Log77");
        Assert.assertEquals(result.getRows().size(), 5);
        System.out.println(result);

        Thread.sleep(2000);
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log777 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log777\"," +
                           "schema \"ip STRING, _timestamp LONG\"" +
                           ")");
        ex.executeQuery(1, "insert into table Log777 SELECT ip, _timestamp FROM Log7 limit 5");
        result = ex.executeQuery(1, "SELECT ip, _timestamp FROM Log777");
        Assert.assertEquals(result.getRows().size(), 5);
        System.out.println(result);
        Assert.assertEquals(this.service.getTableSchema(1, "Log777").getColumns().size(), 1,
                            "wrong number of columns");
        System.out.println(this.service.getTableSchema(1, "Log777").getColumns().toString());

        Thread.sleep(2000);
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log7777 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log7777\"," +
                           "schema \"server_name STRING, ip STRING\"" +
                           ")");
        ex.executeQuery(1, "insert into table Log7777 SELECT server_name, ip  FROM Log7 limit 5");
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log33 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log7777\"," +
                           "schema \"_timestamp LONG, server_name STRING, ip STRING\"" +
                           ")");
        result = ex.executeQuery(1, "SELECT _timestamp FROM Log33");
        Assert.assertEquals(result.getRows().size(), 5);
        System.out.println(result);

        // create a time difference between the records and the current time
        Thread.sleep(2000);
        String currentTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        result = ex.executeQuery(1, "SELECT * FROM Log7 where _timestamp < timestamp(\"" + currentTimeString + "\")");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);

        currentTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz").format(new Date());
        result = ex.executeQuery(1, "SELECT * FROM Log7 where _timestamp < timestamp('" + currentTimeString + "')");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);

        this.service.deleteTable(1, "Log7");
        this.service.deleteTable(1, "Log77");
        this.service.deleteTable(1, "Log777");
        this.service.deleteTable(1, "Log7777");
        System.out.println(testString("end : test Time stamp retrievability"));
    }

    @Test(expectedExceptions = {SparkException.class, AnalyticsException.class})
    public void testFaultyTimestampUDFException() throws AnalyticsException, InterruptedException {
        System.out.println(testString("start : Faulty Timestamp exception test"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log8", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log8");
        this.service.createTable(1, "Log8");
        this.service.put(records);
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log8 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log8\"," +
                           "schema \"_timestamp LONG, server_name STRING, ip STRING, tenant INTEGER, sequence LONG, summary STRING\"" +
                           ")");

        String faultyTimeStamp = "falseTimestamp";
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM Log8 where _timestamp < timestamp('" + faultyTimeStamp + "')");
        System.out.println(result);

        this.service.deleteTable(1, "Log8");
        System.out.println(testString("end : Faulty Timestamp exception test"));
    }

    @Test
    public void testCreateTableWithNoSchema() throws AnalyticsException {
        System.out.println(testString("start : create temp table with no schema test"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log9", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log9");
        this.service.createTable(1, "Log9");
        this.service.put(records);
        String query = "CREATE TEMPORARY TABLE Log USING CarbonAnalytics " +
                       "OPTIONS" +
                       "(tableName \"Log9\"" +
                       ")";
        boolean success = false;
//        try {
        ex.executeQuery(1, query);
//        } catch (Exception e) {
//            System.out.println("Query failed with : " + e.getMessage());
//            success = true;
//        }
//        Assert.assertTrue(success, "Query did not fail!");

//        success = false;
        try {
            ex.executeQuery(1, "SELECT ip FROM Log9");
        } catch (Exception e) {
            System.out.println("Query failed with : " + e.getMessage());
            success = true;
        }
        Assert.assertTrue(success, "Query did not fail!");

        success = false;
        try {
            ex.executeQuery(1, "insert into table Log9 select 1");
        } catch (Exception e) {
            System.out.println("Query failed with : " + e.getMessage());
            success = true;
        }
        Assert.assertTrue(success, "Query did not fail!");

        this.cleanupTable(1, "Log9");
        System.out.println(testString("end : create temp table with column options test"));
    }

    @Test
    public void testMergeTableSchema() throws AnalyticsException {
        System.out.println(testString("start : merge table schema test"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log10", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log10");
        this.service.createTable(1, "Log10");
        this.service.put(records);

        List<ColumnDefinition> cols = new ArrayList<>();
        cols.add(new ColumnDefinition("server_name", AnalyticsSchema.ColumnType.STRING));
        cols.add(new ColumnDefinition("ip", AnalyticsSchema.ColumnType.STRING));
        cols.add(new ColumnDefinition("tenant", AnalyticsSchema.ColumnType.INTEGER));
        cols.add(new ColumnDefinition("sequence", AnalyticsSchema.ColumnType.LONG));
        cols.add(new ColumnDefinition("summary", AnalyticsSchema.ColumnType.LONG));
        this.service.setTableSchema(1, "Log10", new AnalyticsSchema(cols, Collections.<String>emptyList()));

        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log10 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log10\"," +
                           "schema \"ip1 int\"" +
                           ")");

        AnalyticsSchema schema = this.service.getTableSchema(1, "Log10");
        Assert.assertEquals(schema.getColumns().size(), 6, "Merged schema columns do not match");

        AnalyticsQueryResult result;
        boolean success = false;
        try {
            ex.executeQuery(1, "SELECT ip FROM Log10");
        } catch (Exception e) {
            System.out.println("Query failed with : " + e.getMessage());
            success = true;
        }
        Assert.assertTrue(success, "Query did not fail!");


        result = ex.executeQuery(1, "SELECT * FROM Log10");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);

        result = ex.executeQuery(1, "SELECT ip1 FROM Log10");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);

        this.cleanupTable(1, "Log10");

        records = AnalyticsRecordStoreTest.generateRecords(1, "Log11", 0, 10, -1, -1);
        this.service.createTable(1, "Log11");
        this.service.put(records);

        cols = new ArrayList<>();
        cols.add(new ColumnDefinition("server_name", AnalyticsSchema.ColumnType.STRING));
        cols.add(new ColumnDefinition("ip", AnalyticsSchema.ColumnType.STRING));
        cols.add(new ColumnDefinition("tenant", AnalyticsSchema.ColumnType.INTEGER));
        cols.add(new ColumnDefinition("sequence", AnalyticsSchema.ColumnType.LONG));
        cols.add(new ColumnDefinition("summary", AnalyticsSchema.ColumnType.LONG));
        this.service.setTableSchema(1, "Log11", new AnalyticsSchema(cols, Collections.<String>emptyList()));

        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log11 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log11\"," +
                           "schema \"ip1 int\"," +
                           "mergeSchema \"false\"" +
                           ")");

        schema = this.service.getTableSchema(1, "Log11");
        Assert.assertEquals(schema.getColumns().size(), 1, "Merged schema columns do not match");


        success = false;
        try {
            ex.executeQuery(1, "SELECT ip FROM Log11");
        } catch (Exception e) {
            System.out.println("Query failed with : " + e.getMessage());
            success = true;
        }
        Assert.assertTrue(success, "Query did not fail!");

        result = ex.executeQuery(1, "SELECT ip1 FROM Log11");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);

        this.cleanupTable(1, "Log11");


        System.out.println(testString("end : merge table schema test"));
    }

    @Test
    public void testIncrementalProcessing() throws AnalyticsException, InterruptedException {
        String testName = "incremental processing test";
        System.out.println(testString("start : " + testName));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log10", 0, 10, 0, 5000);
        records.addAll(AnalyticsRecordStoreTest.generateRecords(1, "Log11", 0, 10, 0, 5000));
        this.service.deleteTable(1, "Log10");
        this.service.deleteTable(1, "Log11");
        this.service.createTable(1, "Log10");
        this.service.createTable(1, "Log11");
        service.put(records);

        List<ColumnDefinition> cols = new ArrayList<>();
        cols.add(new ColumnDefinition("server_name", ColumnType.STRING));
        cols.add(new ColumnDefinition("ip", ColumnType.STRING));
        cols.add(new ColumnDefinition("tenant", ColumnType.INTEGER));
        cols.add(new ColumnDefinition("sequence", ColumnType.LONG));
        cols.add(new ColumnDefinition("summary", ColumnType.STRING));
        this.service.setTableSchema(1, "Log10", new AnalyticsSchema(cols, Collections.<String>emptyList()));
        this.service.setTableSchema(1, "Log11", new AnalyticsSchema(cols, Collections.<String>emptyList()));

        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log10 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log10\"," +
                           "incrementalParams \"L10, SECOND\"" +
                           ")");

        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log11 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log11\"," +
                           "incrementalParams \"L11, SECOND\"" +
                           ")");

        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM Log10");
        Assert.assertEquals(result.getRows().size(), 10, "Wrong number of rows returned");

        result = ex.executeQuery(1, "SELECT * FROM Log11");
        Assert.assertEquals(result.getRows().size(), 10, "Wrong number of rows returned");

        result = ex.executeQuery(1, "SELECT * FROM Log10");
        Assert.assertEquals(result.getRows().size(), 10, "Wrong number of rows returned in the second time");

        result = ex.executeQuery(1, "SELECT * FROM Log11");
        Assert.assertEquals(result.getRows().size(), 10, "Wrong number of rows returned in the second time");

        result = ex.executeQuery(1, "INCREMENTAL_TABLE_SHOW L10, L11");
        Assert.assertEquals(result.getRows().size(), 2, "Wrong number of rows returned in incremental table show result");
        System.out.println(result);

        result = ex.executeQuery(1, "INCREMENTAL_TABLE_COMMIT L10, L11");
        Assert.assertEquals(result.getRows().size(), 2, "Wrong number of rows returned in incremental table commit result");
        System.out.println(result);

        result = ex.executeQuery(1, "SELECT * FROM Log10");
        Assert.assertEquals(result.getRows().size(), 1, "Wrong number of rows returned after incremental table commit");
        result = ex.executeQuery(1, "SELECT * FROM Log11");
        Assert.assertEquals(result.getRows().size(), 1, "Wrong number of rows returned after incremental table commit");

        List<Record> newRecords = AnalyticsRecordStoreTest.generateRecords(1, "Log10", 0, 2, 11 * 5000, 5000);
        newRecords.addAll(AnalyticsRecordStoreTest.generateRecords(1, "Log11", 0, 2, 11 * 5000, 5000));
        this.service.put(newRecords);

        result = ex.executeQuery(1, "SELECT * FROM Log10");
        Assert.assertEquals(result.getRows().size(), 3, "Wrong number of rows returned after pushing new data");
        result = ex.executeQuery(1, "SELECT * FROM Log11");
        Assert.assertEquals(result.getRows().size(), 3, "Wrong number of rows returned after pushing new data");

        this.cleanupTable(1, "Log10");
        this.cleanupTable(1, "Log11");
        this.cleanupIncrementalTable();
        System.out.println(testString("end : " + testName));
    }

    @Test
    public void testPreserveOrder() throws AnalyticsException, InterruptedException {
        String testName = "preserve order test";
        String table1 = "Log12";
        String table2 = "Log13";
        String table3 = "Log14";
        int recordsCount = 10000;

        System.out.println(testString("start : " + testName));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();

        List<Record> records = new ArrayList<>();
        for (int i = 0; i < recordsCount; i++) {
            Map<String, Object> values = new HashMap<>();
            values.put("value", i);
            Record record = new Record(1, table1, values);
            records.add(record);
        }

        this.service.deleteTable(1, table1);
        this.service.createTable(1, table1);
        this.service.deleteTable(1, table2);
        this.service.createTable(1, table2);
        this.service.deleteTable(1, table3);
        this.service.createTable(1, table3);
        service.put(records);

        List<ColumnDefinition> cols = new ArrayList<>();
        cols.add(new ColumnDefinition("value", ColumnType.INTEGER));
        this.service.setTableSchema(1, table1, new AnalyticsSchema(cols, Collections.<String>emptyList()));

        cols.add(new ColumnDefinition("cnt", ColumnType.INTEGER));
        this.service.setTableSchema(1, table2, new AnalyticsSchema(cols, Collections.<String>emptyList()));
        this.service.setTableSchema(1, table3, new AnalyticsSchema(cols, Collections.<String>emptyList()));

        ex.executeQuery(1, "CREATE TEMPORARY TABLE " + table1 + " USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"" + table1 + "\"" +
                           ")");

        ex.executeQuery(1, "CREATE TEMPORARY TABLE " + table2 + " USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"" + table2 + "\"," +
                           "preserveOrder \"true\"" +
                           ")");

        ex.executeQuery(1, "CREATE TEMPORARY TABLE " + table3 + " USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"" + table3 + "\"," +
                           "preserveOrder \"false\"" +
                           ")");

        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM " + table1);
        Assert.assertEquals(result.getRows().size(), recordsCount, "Wrong number of rows returned");

        ex.executeQuery(1, "INSERT OVERWRITE TABLE " + table2 + " SELECT value, count(*) as cnt from " + table1 + " " +
                           "group by value order by value");

        result = ex.executeQuery(1, "SELECT * FROM " + table2);
        Assert.assertEquals(result.getRows().size(), recordsCount, "Wrong number of rows returned");

        Assert.assertTrue(checkResultOrder(result.getRows()), "The results are out of order");

        ex.executeQuery(1, "INSERT OVERWRITE TABLE " + table3 + " SELECT value, count(*) as cnt from " + table1 + " " +
                           "group by value order by value");

        result = ex.executeQuery(1, "SELECT * FROM " + table3);
        Assert.assertEquals(result.getRows().size(), recordsCount, "Wrong number of rows returned");

        this.cleanupTable(1, table1);
        this.cleanupTable(1, table2);
        System.out.println(testString("end : " + testName));
    }

    @Test(expectedExceptions = Exception.class)
    public void testGlobalTenantCreateNonMTFail() throws Exception {
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Stats USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Stats\"," +
                           "schema \"name STRING, count INT, _tenantId INTEGER\", globalTenantAccess \"true\"" +
                           ")");
    }

    @Test(expectedExceptions = Exception.class)
    public void testGlobalTenantReadNonMTFail() throws Exception {
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Stats USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Stats\"," +
                           "schema \"name STRING, count INT, _tenantId INTEGER\", globalTenantAccess \"true\"" +
                           ")");
        ex.executeQuery(1, "SELECT * FROM Stats");
    }

    @Test(expectedExceptions = Exception.class)
    public void testGlobalTenantWriteNonMTFail() throws Exception {
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Stats USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Stats\"," +
                           "schema \"name STRING, count INT, _tenantId INTEGER\", globalTenantAccess \"true\"" +
                           ")");
        ex.executeQuery(1, "INSERT INTO TABLE Stats SELECT \"api1\", 5, 1;");
    }

    private List<Object> generateObjList(Object... objs) {
        List<Object> result = new ArrayList<>(objs.length);
        Collections.addAll(result, objs);
        return result;
    }

    @Test
    public void testGlobalTenantAccess() throws AnalyticsException {
        this.service.deleteTable(1, "Stats");
        this.service.deleteTable(2, "Stats");
        this.service.deleteTable(1, "StatsSummary");
        this.service.deleteTable(2, "StatsSummary");
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "CREATE TEMPORARY TABLE Stats USING CarbonAnalytics " +
                                                              "OPTIONS" +
                                                              "(tableName \"Stats\"," +
                                                              "schema \"name STRING, cnt INT, _tenantId INTEGER\", globalTenantAccess \"true\"" +
                                                              ")");
        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "SELECT * FROM Stats");
        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "INSERT INTO TABLE Stats SELECT \"api1\", 5, 1;");
        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "INSERT INTO TABLE Stats SELECT \"api1\", 7, 1;");
        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "INSERT INTO TABLE Stats SELECT \"api2\", 10, 1;");
        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "INSERT INTO TABLE Stats SELECT \"api2\", 14, 1;");
        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "INSERT INTO TABLE Stats SELECT \"api1\", 14, 2;");
        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "INSERT INTO TABLE Stats SELECT \"api1\", 2, 2;");
        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "INSERT INTO TABLE Stats SELECT \"api3\", 15, 2;");
        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "INSERT INTO TABLE Stats SELECT \"api3\", 5, 2;");

        List<Record> res1 = AnalyticsDataServiceUtils.listRecords(this.service, this.service.get(1, "Stats", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        List<Record> res2 = AnalyticsDataServiceUtils.listRecords(this.service, this.service.get(2, "Stats", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(res1.size(), 4);
        Assert.assertEquals(res2.size(), 4);

        AnalyticsQueryResult res = ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "SELECT * FROM Stats");
        Assert.assertTrue(Arrays.asList(res.getColumns()).contains("_tenantId"));
        List<List<Object>> expectedRes = new ArrayList<>();
        expectedRes.add(this.generateObjList("api1", 5, 1));
        expectedRes.add(this.generateObjList("api1", 7, 1));
        expectedRes.add(this.generateObjList("api2", 10, 1));
        expectedRes.add(this.generateObjList("api2", 14, 1));
        expectedRes.add(this.generateObjList("api1", 14, 2));
        expectedRes.add(this.generateObjList("api1", 2, 2));
        expectedRes.add(this.generateObjList("api3", 15, 2));
        expectedRes.add(this.generateObjList("api3", 5, 2));
        Assert.assertEquals(new HashSet<>(res.getRows()), new HashSet<>(expectedRes));

        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "CREATE TEMPORARY TABLE StatsSummary USING CarbonAnalytics " +
                                                              "OPTIONS" +
                                                              "(tableName \"StatsSummary\"," +
                                                              "schema \"name STRING, cnt INT, _tenantId INTEGER\", globalTenantAccess \"true\"" +
                                                              ")");

        ex.executeQuery(MultitenantConstants.SUPER_TENANT_ID, "INSERT INTO TABLE StatsSummary SELECT name, SUM(cnt), _tenantId FROM Stats GROUP BY name, _tenantId");

        res1 = AnalyticsDataServiceUtils.listRecords(this.service, this.service.get(1, "StatsSummary", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        res2 = AnalyticsDataServiceUtils.listRecords(this.service, this.service.get(2, "StatsSummary", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(res1.size(), 2);
        Assert.assertEquals(res2.size(), 2);

        Record res1rec1, res1rec2, res2rec1, res2rec2;
        if (res1.get(0).getValue("name").equals("api1")) {
            res1rec1 = res1.get(0);
            res1rec2 = res1.get(1);
        } else {
            res1rec1 = res1.get(1);
            res1rec2 = res1.get(0);
        }
        if (res2.get(0).getValue("name").equals("api1")) {
            res2rec1 = res2.get(0);
            res2rec2 = res2.get(1);
        } else {
            res2rec1 = res2.get(1);
            res2rec2 = res2.get(0);
        }

        Assert.assertEquals(res1rec1.getValue("cnt"), 12);
        Assert.assertEquals(res1rec2.getValue("cnt"), 24);
        Assert.assertEquals(res2rec1.getValue("cnt"), 16);
        Assert.assertEquals(res2rec2.getValue("cnt"), 20);

        this.service.deleteTable(1, "Stats");
        this.service.deleteTable(2, "Stats");
        this.service.deleteTable(1, "StatsSummary");
        this.service.deleteTable(2, "StatsSummary");
    }


    //*************** util methods ********************************


    private List<Record> generateRecordsForCompressedEventAnalytics(int tenantId, String tableName,
                                                                    boolean generateRecordIds) throws Exception {
        List<Record> result = new ArrayList<>();
        Map<String, Object> values;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String[] sampleData;
        try {
            sampleData = IOUtils.toString(classLoader.getResourceAsStream("sample-data/CompressedEventData"))
                    .split("\n");
        } catch (IOException e) {
            throw new AnalyticsException(e.getMessage());
        }
        long timeTmp = System.currentTimeMillis();
        for (String aSampleData : sampleData) {
            values = new HashMap<>();
            String[] fields = aSampleData.split(",", 2);
            values.put("meta_compressed", Boolean.parseBoolean(fields[0]));
            values.put("flowData", fields[1]);
            timeTmp = timeTmp + 5000;
            result.add(new Record(generateRecordIds ? GenericUtils.generateRecordID() : null, tenantId, tableName,
                                  values, timeTmp));
        }
        return result;
    }


    private String testString(String str) {
        return "\n************** " + str.toUpperCase() + " **************\n";
    }

    private void cleanupTable(int tenantId, String tableName) throws AnalyticsException {
        if (this.service.tableExists(tenantId, tableName)) {
            this.service.clearIndexData(tenantId, tableName);
            this.service.deleteTable(tenantId, tableName);
        }
    }

    private void cleanupIncrementalTable() throws AnalyticsException {
        this.cleanupTable(-5000, "__analytics_incremental_meta_table");
    }


    private boolean checkResultOrder(List<List<Object>> rows) {
        boolean result = true;
        for (int i = 0; i < rows.size() - 1; i++) {
            if (((int) rows.get(i).get(0)) > ((int) rows.get(i + 1).get(0))) {
                result = false;
                break;
            }
        }
         return result;
    }
}