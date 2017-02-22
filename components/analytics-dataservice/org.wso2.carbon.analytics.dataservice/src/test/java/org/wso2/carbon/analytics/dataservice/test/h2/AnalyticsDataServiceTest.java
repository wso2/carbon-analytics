/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.dataservice.test.h2;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.data.commons.AnalyticsDataService;
import org.wso2.carbon.analytics.data.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.data.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.data.commons.service.AnalyticsSchema;
import org.wso2.carbon.analytics.data.commons.service.ColumnDefinition;
import org.wso2.carbon.analytics.data.commons.sources.Record;
import org.wso2.carbon.analytics.data.commons.test.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.data.commons.utils.AnalyticsCommonUtils;

import java.util.*;

import static org.wso2.carbon.analytics.data.commons.service.AnalyticsSchema.ColumnType;

/**
 * This class represents the analytics data service tests.
 */
public class AnalyticsDataServiceTest {

    private AnalyticsDataService service;

    @BeforeClass
    public void init() throws AnalyticsException {
        System.setProperty(AnalyticsCommonUtils.WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP, "src/test/resources/conf");
        ServiceLoader<AnalyticsDataService> analyticsDataServiceServiceLoader = ServiceLoader.load(AnalyticsDataService.class);
        if (this.service == null) {
            this.service = analyticsDataServiceServiceLoader.iterator().next();
            if (this.service == null) {
                throw new AnalyticsException("Analytics Data Service cannot be loaded!");
            }
        }
    }

    @Test
    public void testMultipleRecordStores() throws AnalyticsException {
        this.service.deleteTable("T1");
        List<String> recordStoreNames = this.service.listRecordStoreNames();
        Assert.assertTrue(recordStoreNames.size() > 0);
        this.service.createTable("T1");
        Assert.assertTrue(recordStoreNames.contains(this.service.getRecordStoreNameByTable("T1")));
        if (recordStoreNames.size() > 1) {
            System.out.println("** Multiple Record Stores Found **");
            this.service.deleteTable("T1");
            this.service.deleteTable("T2");
            this.service.createTable(recordStoreNames.get(0), "T1");
            this.service.createTable(recordStoreNames.get(1), "T2");
            Assert.assertTrue(this.service.tableExists("T1"));
            Assert.assertTrue(this.service.tableExists("T2"));
            Assert.assertEquals(this.service.getRecordStoreNameByTable("T1"), recordStoreNames.get(0));
            Assert.assertEquals(this.service.getRecordStoreNameByTable("T2"), recordStoreNames.get(1));
            this.service.deleteTable("T2");
        }
        this.service.deleteTable("T1");
    }

    @Test
    public void testTableCreateDeleteList() throws AnalyticsException {
        this.service.deleteTable("TABLE1");
        this.service.deleteTable("TABLE2");
        this.service.deleteTable("TABLEX");
        this.service.createTable("TABLE1");
        List<String> tables = this.service.listTables();
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(new HashSet<>(tables).contains("TABLE1"));
        Assert.assertTrue(this.service.tableExists("table1"));
        Assert.assertTrue(this.service.tableExists("TABLE1"));
        /* this should not throw an exception */
        this.service.createTable("Table1");
        this.service.deleteTable("TABLE2");
        this.service.deleteTable("TABLE1");
        Assert.assertEquals(this.service.listTables().size(), 0);
    }

    @Test(dependsOnMethods = "testTableCreateDeleteList")
    public void testTableCreateTableIfNotExists() throws AnalyticsException {
        this.service.deleteTable("TABLE1");
        this.service.createTableIfNotExists("EVENT_STORE", "TABLE1");
        List<String> tables = this.service.listTables();
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(new HashSet<>(tables).contains("TABLE1"));
        Assert.assertTrue(this.service.tableExists("table1"));
        Assert.assertTrue(this.service.tableExists("TABLE1"));
        this.service.deleteTable("TABLE1");
        this.service.createTable("TABLE1");
        this.service.createTableIfNotExists("EVENT_STORE", "TABLE1");
        tables = this.service.listTables();
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(this.service.tableExists("TABLE1"));
        this.service.deleteTable("TABLE1");
        Assert.assertEquals(this.service.listTables().size(), 0);
    }

    @Test(dependsOnMethods = "testTableCreateTableIfNotExists")
    public void testTableSetGetSchema() throws AnalyticsException {
        String tableName = "T1";
        this.service.deleteTable(tableName);
        this.service.createTable(tableName);
        AnalyticsSchema schema = this.service.getTableSchema(tableName);
        /* for an empty schema, still the schema object must be returned */
        Assert.assertNotNull(schema);
        List<ColumnDefinition> columns = new ArrayList<>();
        ColumnDefinition cd1 = new ColumnDefinition("name", AnalyticsSchema.ColumnType.STRING);
        cd1.setType(AnalyticsSchema.ColumnType.STRING);
        columns.add(cd1);
        ColumnDefinition cd2 = new ColumnDefinition("age", AnalyticsSchema.ColumnType.INTEGER);
        columns.add(cd2);
        ColumnDefinition cd3 = new ColumnDefinition("weight", AnalyticsSchema.ColumnType.DOUBLE);
        columns.add(cd3);
        ColumnDefinition cd4 = new ColumnDefinition("something1", AnalyticsSchema.ColumnType.FLOAT);
        columns.add(cd4);
        ColumnDefinition cd5 = new ColumnDefinition("something2", AnalyticsSchema.ColumnType.BOOLEAN);
        columns.add(cd5);
        ColumnDefinition cd6 = new ColumnDefinition("something3", AnalyticsSchema.ColumnType.LONG);
        columns.add(cd6);
        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("name");
        primaryKeys.add("age");
        schema = new AnalyticsSchema(columns, primaryKeys);
        this.service.setTableSchema(tableName, schema);
        AnalyticsSchema schemaIn = this.service.getTableSchema(tableName);
        Assert.assertEquals(schema, schemaIn);
        this.service.deleteTable(tableName);
    }

    @Test(expectedExceptions = AnalyticsTableNotAvailableException.class, dependsOnMethods = "testTableSetGetSchema")
    public void testTableGetNoSchema() throws AnalyticsException {
        this.service.deleteTable("T1");
        this.service.getTableSchema("T1");
    }

    @Test(dependsOnMethods = "testTableCreateTableIfNotExists")
    public void testMultipleDataRecordAddRetrieveWithTimestampRange() throws AnalyticsException {
        this.service.deleteTable("T1");
        this.service.createTable("T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = AnalyticsRecordStoreTest.generateRecords("T1", 1, 100, time, timeOffset);
        this.service.put(records);
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get("T1", 2, null, time - 10, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get("T1", 1, null, time, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get("T1", 1, null, time, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get("T1", 2, null, time + 1, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get("T1", 5, null, time + 1, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        records.remove(99);
        records.remove(0);
        Assert.assertEquals(new HashSet<>(records), new HashSet<>(recordsIn));
        this.service.deleteTable("T1");
    }

    @Test(dependsOnMethods = "testTableCreateTableIfNotExists")
    public void testMultipleDataRecordAddRetrieveWithKeys() throws AnalyticsException {
        String tableName = "MyT1";
        this.service.deleteTable(tableName);
        this.service.createTable(tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("tenant", ColumnType.INTEGER));
        columns.add(new ColumnDefinition("log", ColumnType.STRING));
        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("tenant");
        primaryKeys.add("log");
        AnalyticsSchema schema = new AnalyticsSchema(columns, primaryKeys);
        this.service.setTableSchema(tableName, schema);
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(tableName, 1, 75, -1, -1, false);
        this.service.put(records);
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get(tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        records = AnalyticsRecordStoreTest.generateRecords(tableName, 1, 74, -1, -1, false);
        this.service.put(records);
        recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get(tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 75);
        records = AnalyticsRecordStoreTest.generateRecords(tableName, 1, 77, -1, -1, false);
        this.service.put(records);
        recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get(tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 77);
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        primaryKeys.clear();
        schema = new AnalyticsSchema(columns, primaryKeys);
        this.service.setTableSchema(tableName, schema);
        records = AnalyticsRecordStoreTest.generateRecords(tableName, 1, 10, -1, -1, false);
        this.service.put(records);
        recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get(tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 87);
        schema = new AnalyticsSchema(columns, null);
        this.service.setTableSchema(tableName, schema);
        records = AnalyticsRecordStoreTest.generateRecords(tableName, 1, 10, -1, -1, false);
        this.service.put(records);
        recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get(tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 97);
        this.service.deleteTable(tableName);
    }

    @Test(dependsOnMethods = "testMultipleDataRecordAddRetrieveWithKeys")
    public void testRecordAddRetrieveWithKeyValues() throws AnalyticsException {
        String tableName = "MyT1";
        this.service.deleteTable(tableName);
        this.service.createTable(tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("tenant", ColumnType.INTEGER));
        columns.add(new ColumnDefinition("log", ColumnType.STRING));
        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("tenant");
        primaryKeys.add("log");
        AnalyticsSchema schema = new AnalyticsSchema(columns, primaryKeys);
        this.service.setTableSchema(tableName, schema);
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("tenant", "1");
        values.put("log", "log statement 1");
        Record record1 = new Record(tableName, values);
        values = new HashMap<>();
        values.put("tenant", "1");
        values.put("log", "log statement 2");
        Record record2 = new Record(tableName, values);
        values = new HashMap<>();
        values.put("tenant", "2");
        values.put("log", "log statement 1");
        Record record3 = new Record(tableName, values);
        values = new HashMap<>();
        values.put("tenant", "2");
        values.put("log", "log statement 2");
        Record record4 = new Record(tableName, values);
        records.add(record1);
        records.add(record2);
        records.add(record3);
        records.add(record4);
        this.service.put(records);
        List<Map<String, Object>> valuesBatch = new ArrayList<>();
        values = new HashMap<>();
        values.put("tenant", "1");
        values.put("log", "log statement 1");
        valuesBatch.add(values);
        values = new HashMap<>();
        values.put("tenant", "2");
        values.put("log", "log statement 2");
        values.put("some_other_field", "xxxxxxxx zzzzzz");
        valuesBatch.add(values);
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.getWithKeyValues(tableName, 1, null, valuesBatch));
        Set<Record> matchRecords = new HashSet<>();
        matchRecords.add(record1);
        matchRecords.add(record4);
        Assert.assertEquals(recordsIn.size(), 2);
        Assert.assertEquals(new HashSet<>(recordsIn), matchRecords);
        this.service.deleteTable(tableName);
    }

    @Test(dependsOnMethods = "testRecordAddRetrieveWithKeyValues")
    public void testDataRecordAddReadPerformance() throws AnalyticsException {
        String tableName = "TableX";
        this.service.deleteTable(tableName);
        System.out.println("\n************** START ANALYTICS DATASERVICE PERF TEST **************");
        int n = 100, batch = 200;
        List<Record> records;
        
        /* warm-up */
        this.service.createTable(tableName);
        for (int i = 0; i < 10; i++) {
            records = AnalyticsRecordStoreTest.generateRecords(tableName, i, batch, -1, -1);
            this.service.put(records);
        }
        this.service.deleteTable(tableName);

        this.service.createTable(tableName);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            records = AnalyticsRecordStoreTest.generateRecords(tableName, i, batch, -1, -1);
            this.service.put(records);
        }
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.service,
                this.service.get(tableName, 7, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        this.service.deleteTable(tableName);
        System.out.println("\n************** END ANALYTICS DATASERVICE PERF TEST **************");
    }

    @AfterClass
    public void done() throws AnalyticsException {
        this.service.destroy();
    }

}
