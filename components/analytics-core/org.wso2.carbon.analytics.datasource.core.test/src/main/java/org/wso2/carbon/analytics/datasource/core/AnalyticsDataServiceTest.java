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
package org.wso2.carbon.analytics.datasource.core;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterException;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.core.clustering.GroupEventListener;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema.ColumnType;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinitionExt;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.base.MultitenantConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the analytics data service tests.
 */
public class AnalyticsDataServiceTest implements GroupEventListener {

    private static final int DEFAULT_WAIT_TIME = 60000;

    protected AnalyticsDataService service;
    
    private boolean becameLeader, leaderUpdated;

    private Random randomGenerator;
               
    public void init(AnalyticsDataService service) {
        this.service = service;
        randomGenerator = new Random();
    }
    
    @Test
    public void testMultipleRecordStores() throws AnalyticsException {
        this.cleanupTable(1, "T1");        
        List<String> recordStoreNames = this.service.listRecordStoreNames();
        Assert.assertTrue(recordStoreNames.size() > 0);
        this.service.createTable(1, "T1");
        Assert.assertTrue(recordStoreNames.contains(this.service.getRecordStoreNameByTable(1, "T1")));
        if (recordStoreNames.size() > 1) {
            System.out.println("** Multiple Record Stores Found **");
            this.cleanupTable(2, "T1");
            this.cleanupTable(2, "T2");            
            this.service.createTable(2, recordStoreNames.get(0), "T1");
            this.service.createTable(2, recordStoreNames.get(1), "T2");
            Assert.assertTrue(this.service.tableExists(2, "T1"));
            Assert.assertTrue(this.service.tableExists(2, "T2"));
            Assert.assertEquals(this.service.getRecordStoreNameByTable(2, "T1"), recordStoreNames.get(0));
            Assert.assertEquals(this.service.getRecordStoreNameByTable(2, "T2"), recordStoreNames.get(1));
            this.cleanupTable(2, "T1");
            this.cleanupTable(2, "T2");
        }
        this.cleanupTable(1, "T1");        
    }
    
    @Test (dependsOnMethods = "testMultipleRecordStores")
    public void testIndexAddRetrieve() throws AnalyticsException {
        this.indexAddRetrieve(MultitenantConstants.SUPER_TENANT_ID);
        this.indexAddRetrieve(1);
        this.indexAddRetrieve(15001);
    }
    
    private void indexAddRetrieve(int tenantId) throws AnalyticsException {
        this.cleanupTable(tenantId, "T1");
        this.service.createTable(tenantId, "T1");
        this.service.setTableSchema(tenantId, "T1", new AnalyticsSchema());
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("C1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("C2", ColumnType.BOOLEAN, true, false));
        columns.add(new ColumnDefinitionExt("C3", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("C4", ColumnType.LONG, true, false));
        columns.add(new ColumnDefinitionExt("C5", ColumnType.FLOAT, true, false));
        columns.add(new ColumnDefinitionExt("C6", ColumnType.DOUBLE, true, false));
        columns.add(new ColumnDefinitionExt("C7", ColumnType.BINARY, true, false));
        columns.add(new ColumnDefinitionExt("C8", ColumnType.STRING, true, false, true));
        AnalyticsSchema schema = new AnalyticsSchema(columns, null);
        this.service.setTableSchema(tenantId, "T1", schema);
        AnalyticsSchema schemaIn = this.service.getTableSchema(tenantId, "T1");
        Assert.assertEquals(schemaIn, schemaIn);
        this.cleanupTable(tenantId, "T1");
    }
    
    @Test (dependsOnMethods = "testIndexAddRetrieve")
    public void testTableCreateDeleteList() throws AnalyticsException {
        this.service.deleteTable(250035, "TABLE1");
        this.service.deleteTable(250035, "TABLE2");
        this.service.deleteTable(8830, "TABLEX");
        this.service.createTable(250035, "TABLE1");
        List<String> tables = this.service.listTables(250035);
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(new HashSet<>(tables).contains("TABLE1"));
        Assert.assertTrue(this.service.tableExists(250035, "table1"));
        Assert.assertTrue(this.service.tableExists(250035, "TABLE1"));
        /* this should not throw an exception */
        this.service.createTable(250035, "Table1");
        this.service.deleteTable(250035, "TABLE2");
        this.service.deleteTable(250035, "TABLE1");
        this.service.deleteTable(8830, "TABLEX");
        Assert.assertEquals(this.service.listTables(250035).size(), 0);
        Assert.assertEquals(this.service.listTables(8830).size(), 0);
    }
    
    @Test (dependsOnMethods = "testTableCreateDeleteList")
    public void testTableCreateTableIfNotExists() throws AnalyticsException {
        this.service.deleteTable(10, "TABLE1");
        this.service.createTableIfNotExists(10, "EVENT_STORE", "TABLE1");
        List<String> tables = this.service.listTables(10);
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(new HashSet<>(tables).contains("TABLE1"));
        Assert.assertTrue(this.service.tableExists(10, "table1"));
        Assert.assertTrue(this.service.tableExists(10, "TABLE1"));
        this.service.deleteTable(10, "TABLE1");
        this.service.createTable(10, "TABLE1");
        this.service.createTableIfNotExists(10, "EVENT_STORE", "TABLE1");
        tables = this.service.listTables(10);
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(this.service.tableExists(10, "TABLE1"));
        this.service.deleteTable(10, "TABLE1");
        Assert.assertEquals(this.service.listTables(10).size(), 0);
    }
    
    @Test (dependsOnMethods = "testTableCreateTableIfNotExists")
    public void testTableSetGetSchema() throws AnalyticsException {
        int tenantId = 105;
        String tableName = "T1";
        this.service.deleteTable(tenantId, tableName);
        this.service.createTable(tenantId, tableName);
        AnalyticsSchema schema = this.service.getTableSchema(tenantId, tableName);
        /* for an empty schema, still the schema object must be returned */
        Assert.assertNotNull(schema);
        List<ColumnDefinition> columns = new ArrayList<>();
        ColumnDefinition cd1 = new ColumnDefinitionExt("name", AnalyticsSchema.ColumnType.STRING);
        cd1.setType(AnalyticsSchema.ColumnType.STRING);
        columns.add(cd1);
        ColumnDefinition cd2 = new ColumnDefinitionExt("age", AnalyticsSchema.ColumnType.INTEGER);
        columns.add(cd2);
        ColumnDefinition cd3 = new ColumnDefinitionExt("weight", AnalyticsSchema.ColumnType.DOUBLE);
        columns.add(cd3);
        ColumnDefinition cd4 = new ColumnDefinitionExt("something1", AnalyticsSchema.ColumnType.FLOAT);
        columns.add(cd4);
        ColumnDefinition cd5 = new ColumnDefinitionExt("something2", AnalyticsSchema.ColumnType.BOOLEAN);
        columns.add(cd5);
        ColumnDefinition cd6 = new ColumnDefinitionExt("something3", AnalyticsSchema.ColumnType.LONG);
        columns.add(cd6);
        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("name");
        primaryKeys.add("age");
        schema = new AnalyticsSchema(columns, primaryKeys);
        this.service.setTableSchema(tenantId, tableName, schema);
        AnalyticsSchema schemaIn = this.service.getTableSchema(tenantId, tableName);
        Assert.assertEquals(schema, schemaIn);
        this.service.deleteTable(tenantId, tableName);
    }

    @Test (expectedExceptions = AnalyticsTableNotAvailableException.class, dependsOnMethods = "testTableSetGetSchema")
    public void testTableGetNoSchema() throws AnalyticsException {
        this.service.deleteTable(105, "T1");
        this.service.getTableSchema(105, "T1");
    }

    @Test (dependsOnMethods = "testTableGetNoSchema")
    public void testTableCreateDeleteListNegativeTenantIds() throws AnalyticsException {
        this.service.deleteTable(-1234, "TABLE1");
        this.service.deleteTable(-1234, "TABLE2");
        this.service.createTable(-1234, "TABLE1");
        List<String> tables = this.service.listTables(-1234);
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(new HashSet<>(tables).contains("TABLE1"));
        Assert.assertTrue(this.service.tableExists(-1234, "table1"));
        Assert.assertTrue(this.service.tableExists(-1234, "TABLE1"));
        /* this should not throw an exception */
        this.service.createTable(-1234, "Table1");
        this.service.deleteTable(-1234, "TABLE2");
        this.service.deleteTable(-1234, "TABLE1");
        Assert.assertEquals(this.service.listTables(-1234).size(), 0);
    }
    
    private void cleanupTable(int tenantId, String tableName) throws AnalyticsException {
        if (this.service.tableExists(tenantId, tableName)) {
            this.service.clearIndexData(tenantId, tableName);
            this.service.deleteTable(tenantId, tableName);
        }
    }
    
    @Test (enabled = true, dependsOnMethods = "testTableCreateDeleteListNegativeTenantIds")
    public void testMultipleDataRecordAddRetieveWithTimestampRange() throws AnalyticsException {
        this.service.deleteTable(7, "T1");
        this.service.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.service.put(records);
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(7, "T1", 2, null, time - 10, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(7, "T1", 1, null, time, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(7, "T1", 1, null, time, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(7, "T1", 2, null, time + 1, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(7, "T1", 5, null, time + 1, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        if (this.service.isRecordCountSupported(this.service.getRecordStoreNameByTable(7, "T1"))) {
            long count = this.service.getRecordCount(7, "T1", time, time + timeOffset * 99);
            Assert.assertEquals(count, 99);
            Assert.assertEquals(this.service.getRecordCount(7, "T1", time + 1, time + timeOffset * 99 + 1), 99);
            Assert.assertEquals(this.service.getRecordCount(7, "T1", time + 1, time + timeOffset * 99), 98);
        }
        records.remove(99);
        records.remove(0);
        Assert.assertEquals(new HashSet<>(records), new HashSet<>(recordsIn));
        this.service.deleteTable(7, "T1");
    }
    
    private List<Record> generateIndexRecords(int tenantId, String tableName, int n, long startTimestamp) {
        Map<String, Object> values;
        Record record;
        List<Record> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            values = new HashMap<>();
            values.put("INT1", i);
            values.put("STR1", "STRING" + i);
            values.put("str2", "string" + i);
            values.put("TXT1", "My name is bill" + i);
            values.put("LN1", 1435000L + i);
            values.put("DB1", 54.535 + i);
            values.put("FL1", 3.14 + i);
            values.put("BL1", i % 2 == 0);
            record = new Record(tenantId, tableName, values, startTimestamp + i * 10);
            result.add(record);
        }        
        return result;
    }
    
    private void indexDataAddRetrieve(int tenantId, String tableName, int n) throws AnalyticsException {
        this.cleanupTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("INT1", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("STR1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("str2", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("TXT1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("LN1", ColumnType.LONG, true, false));
        columns.add(new ColumnDefinitionExt("DB1", ColumnType.DOUBLE, true, false));
        columns.add(new ColumnDefinitionExt("FL1", ColumnType.FLOAT, true, false));
        columns.add(new ColumnDefinitionExt("BL1", ColumnType.BOOLEAN, true, false));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        List<Record> records = this.generateIndexRecords(tenantId, tableName, n, 0);
        this.service.put(records);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        List<SearchResultEntry> result = this.service.search(tenantId, tableName, "STR1:STRING0", 0, 10);
        Assert.assertEquals(result.size(), 1);
        result = this.service.search(tenantId, tableName, "str2:string0", 0, 10);
        Assert.assertEquals(result.size(), 1);
        result = this.service.search(tenantId, tableName, "str2:String0", 0, 10);
        Assert.assertEquals(result.size(), 1);
        result = this.service.search(tenantId, tableName, "TXT1:name", 0, n + 10);
        Assert.assertEquals(result.size(), n);
        result = this.service.search(tenantId, tableName, "INT1:" + (n - 1), 0, 10);
        Assert.assertEquals(result.size(), 1);
        result = this.service.search(tenantId, tableName, "LN1:1435000", 0, 10);
        Assert.assertEquals(result.size(), 1);
        result = this.service.search(tenantId, tableName, "DB1:54.535", 0, 10);
        Assert.assertEquals(result.size(), 1);
        result = this.service.search(tenantId, tableName, "FL1:3.14", 0, 10);
        Assert.assertEquals(result.size(), 1);
        result = this.service.search(tenantId, tableName, "BL1:true", 0, 10);
        Assert.assertTrue(result.size() > 0);
        if (n > 4) {
            result = this.service.search(tenantId, tableName, "INT1:[1 TO 3]", 0, 10);
            Assert.assertEquals(result.size(), 3);
            result = this.service.search(tenantId, tableName, "LN1:[1435000 TO 1435001]", 0, 10);
            Assert.assertEquals(result.size(), 2);
            result = this.service.search(tenantId, tableName, "LN1:[1435000 TO 1435001}", 0, 10);
            Assert.assertEquals(result.size(), 1);
            result = this.service.search(tenantId, tableName, "DB1:[54.01 TO 55.86]", 0, 10);
            Assert.assertEquals(result.size(), 2);
            result = this.service.search(tenantId, tableName, "FL1:[3.01 TO 4.50]", 0, 10);
            Assert.assertEquals(result.size(), 2);
            result = this.service.search(tenantId, tableName, "BL1:[false TO true]", 0, 10);
            Assert.assertTrue(result.size() > 2);
        }
        this.cleanupTable(tenantId, tableName);
    }
    
    @Test (enabled = true, dependsOnMethods = "testMultipleDataRecordAddRetieveWithTimestampRange")
    public void testMultitenantDataAddGlobalDataRetrieve() throws AnalyticsException {
        Set<Record> outRecords = new HashSet<>();
        this.service.deleteTable(1, "T1");
        this.service.deleteTable(2, "T1");
        this.service.deleteTable(3, "T1");
        this.service.deleteTable(1, "T2");
        this.service.deleteTable(MultitenantConstants.SUPER_TENANT_ID, "T1");
        this.service.createTable(1, "T1");
        this.service.createTable(2, "T1");
        this.service.createTable(3, "T1");
        this.service.createTable(1, "T2");
        this.service.createTable(MultitenantConstants.SUPER_TENANT_ID, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "T1", 1, 100, time, timeOffset);
        this.service.put(records);
        outRecords.addAll(records);
        records = AnalyticsRecordStoreTest.generateRecords(2, "T1", 1, 50, time, timeOffset);
        this.service.put(records);
        outRecords.addAll(records);
        records = AnalyticsRecordStoreTest.generateRecords(3, "T1", 1, 20, time, timeOffset);
        this.service.put(records);
        outRecords.addAll(records);
        records = AnalyticsRecordStoreTest.generateRecords(MultitenantConstants.SUPER_TENANT_ID, "T1", 1, 10, time, timeOffset);
        this.service.put(records);
        outRecords.addAll(records);
        records = AnalyticsRecordStoreTest.generateRecords(1, "T2", 1, 5, time, timeOffset);
        this.service.put(records);        
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(Constants.GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 180);
        Assert.assertEquals(outRecords, new HashSet<>(recordsIn));
        this.service.deleteTable(1, "T1");
        this.service.deleteTable(2, "T1");
        this.service.deleteTable(3, "T1");
        this.service.deleteTable(1, "T2");
        this.service.deleteTable(MultitenantConstants.SUPER_TENANT_ID, "T1");
    }
    
    @Test (enabled = true, dependsOnMethods = "testMultitenantDataAddGlobalDataRetrieve")
    public void testIndexedDataAddRetrieve() throws AnalyticsException {
        this.indexDataAddRetrieve(5, "TX", 1);
        this.indexDataAddRetrieve(5, "TX", 10);
        this.indexDataAddRetrieve(6, "TX", 150);
        this.indexDataAddRetrieve(7, "TX", 2500);
    }
    
    @Test (dependsOnMethods = "testIndexedDataAddRetrieve")
    public void testSearchCount() throws AnalyticsException {
        int tenantId = 4;
        String tableName = "Books";
        this.cleanupTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("INT1", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("STR1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("str2", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("TXT1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("LN1", ColumnType.LONG, true, false));
        columns.add(new ColumnDefinitionExt("DB1", ColumnType.DOUBLE, true, false));
        columns.add(new ColumnDefinitionExt("FL1", ColumnType.FLOAT, true, false));
        columns.add(new ColumnDefinitionExt("BL1", ColumnType.BOOLEAN, true, false));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        List<Record> records = this.generateIndexRecords(tenantId, tableName, 100, 0);
        this.service.put(records);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        int count = this.service.searchCount(tenantId, tableName, "STR1:STRING55");
        Assert.assertEquals(count, 1);
        count = this.service.searchCount(tenantId, tableName, "TXT1:name");
        Assert.assertEquals(count, 100);
        this.cleanupTable(tenantId, tableName);
    }
    
    @Test (dependsOnMethods = "testSearchCount")
    public void testIndexedDataUpdate() throws Exception {
        int tenantId = 1;
        String tableName = "T1";
        this.cleanupTable(tenantId, tableName);
        this.service.createTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("STR1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("STR2", ColumnType.STRING, true, false));
        Map<String, Object> values = new HashMap<>();
        values.put("STR1", "Sri Lanka is known for tea");
        values.put("STR2", "Cricket is most famous");
        Map<String, Object> values2 = new HashMap<>();
        values2.put("STR1", "Canada is known for Ice Hockey");
        values2.put("STR2", "It is very cold");
        Record record = new Record(tenantId, tableName, values);
        Record record2 = new Record(tenantId, tableName, values2);
        List<Record> records = new ArrayList<>();
        records.add(record);
        records.add(record2);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        this.service.put(records);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        List<SearchResultEntry> result = this.service.search(tenantId, tableName, "STR1:tea", 0, 10);
        Assert.assertEquals(result.size(), 1);
        String id = record.getId();
        Assert.assertEquals(result.get(0).getId(), id);
        result = this.service.search(tenantId, tableName, "STR1:diamonds", 0, 10);
        Assert.assertEquals(result.size(), 0);
        result = this.service.search(tenantId, tableName, "STR2:cricket", 0, 10);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getId(), id);
        values = new HashMap<>();
        values.put("STR1", "South Africa is know for diamonds");
        values.put("STR2", "NBA has the best basketball action");
        record = new Record(id, tenantId, tableName, values);
        records = new ArrayList<>();
        records.add(record);
        /* update */
        this.service.put(records);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        result = this.service.search(tenantId, tableName, "STR1:tea", 0, 10);
        Assert.assertEquals(result.size(), 0);
        result = this.service.search(tenantId, tableName, "STR2:cricket", 0, 10);
        Assert.assertEquals(result.size(), 0);
        result = this.service.search(tenantId, tableName, "STR1:diamonds", 0, 10);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getId(), id);
        result = this.service.search(tenantId, tableName, "STR2:basketball", 0, 10);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getId(), id);
        result = this.service.search(tenantId, tableName, "STR1:hockey", 0, 10);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getId(), record2.getId());
        this.cleanupTable(tenantId, tableName);
    }
    
    @Test (dependsOnMethods = "testIndexedDataUpdate")
    public void testIndexDataDeleteWithIds() throws AnalyticsException {
        int tenantId = 5100;
        String tableName = "X1";
        this.cleanupTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("INT1", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("STR1", ColumnType.STRING, true, false));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        Assert.assertEquals(this.service.search(tenantId, tableName, "STR1:S*", 0, 150).size(), 0);
        List<Record> records = this.generateIndexRecords(tenantId, tableName, 98, 0);
        this.service.put(records);
        List<String> ids = new ArrayList<>();
        ids.add(records.get(0).getId());
        ids.add(records.get(5).getId());
        ids.add(records.get(50).getId());
        ids.add(records.get(97).getId());
        Assert.assertEquals(AnalyticsDataServiceUtils.listRecords(this.service, this.service.get(tenantId, tableName, 2, null, ids)).size(), 4);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        List<SearchResultEntry> result = this.service.search(tenantId, tableName, "STR1:S*", 0, 150);
        Assert.assertEquals(result.size(), 98);
        this.service.delete(tenantId, tableName, ids);
        Assert.assertEquals(AnalyticsDataServiceUtils.listRecords(this.service, this.service.get(
                tenantId, tableName, 1, null, ids)).size(), 0);
        Assert.assertEquals(AnalyticsDataServiceUtils.listRecords(this.service, this.service.get(
                tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 94);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        result = this.service.search(tenantId, tableName, "STR1:S*", 0, 150);
        Assert.assertEquals(result.size(), 94);
        Assert.assertEquals(AnalyticsDataServiceUtils.listRecords(this.service, this.service.get(tenantId, tableName, 3, null, ids)).size(), 0);
        this.cleanupTable(tenantId, tableName);
    }
    
    @Test (dependsOnMethods = "testIndexDataDeleteWithIds")
    public void testIndexDataDeleteRange() throws AnalyticsException {
        int tenantId = 230;
        String tableName = "Scores";
        int n = 115;
        this.cleanupTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("INT1", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("STR1", ColumnType.STRING, true, false));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        List<Record> records = this.generateIndexRecords(tenantId, tableName, n, 1000);
        this.service.put(records);
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), n);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        this.service.delete(tenantId, tableName, 1030, 1060);
        recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 5, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), n - 3);
        /* lets test table name case-insensitiveness too */
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        List<SearchResultEntry> results = this.service.search(tenantId, 
                tableName.toUpperCase(), "STR1:s*", 0, n);
        Assert.assertEquals(results.size(), n - 3);
        this.cleanupTable(tenantId, tableName);
    }
        
    private void writeIndexRecords(int tenantId, String tableName, int n, int batch) throws AnalyticsException {
        List<Record> records;
        for (int i = 0; i < n; i++) {
            records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, i, batch, -1, -1);
            this.service.put(records);
        }
    }

    private void writeIndexRecordsWithFacets(int tenantId, String[] tableName, int n, int batch) throws AnalyticsException {
        List<Record> records;
        if (tableName.length > 1) {
            for (int i = 0; i < n; i++) {
                int randomNumber = randomGenerator.nextInt(tableName.length);
                records = AnalyticsRecordStoreTest.generateRecordsWithFacets(tenantId, tableName[randomNumber], i, batch, -1, -1);
                this.service.put(records);
            }
        } else {
            for (int i = 0; i < n; i++) {
                records = AnalyticsRecordStoreTest.generateRecordsWithFacets(tenantId, tableName[0], i, batch, -1, -1);
                this.service.put(records);
            }
        }
    }

    @Test (dependsOnMethods = "testIndexDataDeleteRange")
    public void testMultipleDataRecordAddRetrieveWithKeys() throws AnalyticsException {
        int tenantId = 1;
        String tableName = "MyT1";
        this.cleanupTable(tenantId, tableName);
        this.service.createTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("tenant", ColumnType.INTEGER, false, false));
        columns.add(new ColumnDefinitionExt("log", ColumnType.STRING, false, false));
        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("tenant");
        primaryKeys.add("log");
        AnalyticsSchema schema = new AnalyticsSchema(columns, primaryKeys);
        this.service.setTableSchema(tenantId, tableName, schema);
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, 1, 75, -1, -1, false);
        this.service.put(records);
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, 1, 74, -1, -1, false);
        this.service.put(records);
        recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 75);
        records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, 1, 77, -1, -1, false);
        this.service.put(records);
        recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 77);
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        primaryKeys.clear();
        schema = new AnalyticsSchema(columns, primaryKeys);
        this.service.setTableSchema(tenantId, tableName, schema);
        records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, 1, 10, -1, -1, false);
        this.service.put(records);
        recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 87);
        schema = new AnalyticsSchema(columns, null);
        this.service.setTableSchema(tenantId, tableName, schema);
        records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, 1, 10, -1, -1, false);
        this.service.put(records);
        recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 97);
        this.cleanupTable(tenantId, tableName);
    }
    
    @Test (dependsOnMethods = "testMultipleDataRecordAddRetrieveWithKeys")
    public void testRecordAddRetrieveWithKeyValues() throws AnalyticsException {
        int tenantId = 1;
        String tableName = "MyT1";
        this.cleanupTable(tenantId, tableName);
        this.service.createTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("tenant", ColumnType.INTEGER, false, false));
        columns.add(new ColumnDefinitionExt("log", ColumnType.STRING, false, false));
        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("tenant");
        primaryKeys.add("log");
        AnalyticsSchema schema = new AnalyticsSchema(columns, primaryKeys);
        this.service.setTableSchema(tenantId, tableName, schema);
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("tenant", "1");
        values.put("log", "log statement 1");
        Record record1 = new Record(tenantId, tableName, values);
        values = new HashMap<>();
        values.put("tenant", "1");
        values.put("log", "log statement 2");
        Record record2 = new Record(tenantId, tableName, values);
        values = new HashMap<>();
        values.put("tenant", "2");
        values.put("log", "log statement 1");
        Record record3 = new Record(tenantId, tableName, values);
        values = new HashMap<>();
        values.put("tenant", "2");
        values.put("log", "log statement 2");
        Record record4 = new Record(tenantId, tableName, values);
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
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.getWithKeyValues(tenantId, tableName, 1, null, valuesBatch));
        Set<Record> matchRecords = new HashSet<>();
        matchRecords.add(record1);
        matchRecords.add(record4);
        Assert.assertEquals(recordsIn.size(), 2);
        Assert.assertEquals(new HashSet<>(recordsIn), matchRecords);
        this.cleanupTable(tenantId, tableName);
    }
    
    @Test (dependsOnMethods = "testRecordAddRetrieveWithKeyValues")
    public void testDataRecordAddReadPerformanceNonIndex() throws AnalyticsException {
        int tenantId = 51;
        String tableName = "TableX";
        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** START ANALYTICS DS (WITHOUT INDEXING) PERF TEST **************");
        int n = 100, batch = 200;
        List<Record> records;
        
        /* warm-up */
        this.service.createTable(tenantId, tableName);      
        for (int i = 0; i < 10; i++) {
            records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, i, batch, -1, -1);
            this.service.put(records);
        }
        this.cleanupTable(tenantId, tableName);
        
        this.service.createTable(tenantId, tableName);
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, i, batch, -1, -1);
            this.service.put(records);
        }
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 7, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** END ANALYTICS DS (WITHOUT INDEXING) PERF TEST **************");
    }
    
    @Test (dependsOnMethods = "testDataRecordAddReadPerformanceNonIndex")
    public void testDataRecordAddReadPerformanceIndex1C() throws AnalyticsException {
        System.out.println("\n************** START ANALYTICS DS (WITH INDEXING - SINGLE THREAD) PERF TEST **************");

        int tenantId = 50;
        String tableName = "TableX";
        this.cleanupTable(tenantId, tableName);
        int n = 250, batch = 200;
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("ip", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("log", ColumnType.STRING, true, false));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        
        long start = System.currentTimeMillis();
        this.writeIndexRecords(tenantId, tableName, n, batch);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                                                          this.service.get(tenantId, tableName, 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);        
        start = System.currentTimeMillis();
        List<SearchResultEntry> results = this.service.search(tenantId, tableName, "log: exception", 0, 75);
        end = System.currentTimeMillis();
        Assert.assertEquals(results.size(), 75);
        System.out.println("* Search Result Count: " + results.size() + " Time: " + (end - start) + " ms.");
        start = System.currentTimeMillis();
        int count = this.service.searchCount(tenantId, tableName, "*:*");
        end = System.currentTimeMillis();
        System.out.println("* Search Index Full Count: " + count + " Time: " + (end - start) + " ms.");
        Assert.assertEquals(count, n * batch);
        
        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** END ANALYTICS DS (WITH INDEXING) PERF TEST **************");
    }

    @Test (dependsOnMethods = "testDataRecordAddReadPerformanceIndex1C")
    public void testFacetDataRecordAddReadPerformanceIndex1C() throws AnalyticsException {
        System.out.println("\n************** START ANALYTICS DS (WITH FACET INDEXING - SINGLE THREAD) PERF TEST **************");

        int tenantId = 50;
        String tableName = "TableY";
        this.cleanupTable(tenantId, tableName);
        int n = 250, batch = 200;
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("ip", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("log", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("location", ColumnType.STRING, true, false, true));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));

        long start = System.currentTimeMillis();
        this.writeIndexRecordsWithFacets(tenantId, new String[]{tableName}, n, batch);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                                                          this.service.get(tenantId, tableName, 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
        drillDownRequest.setTableName(tableName);
        drillDownRequest.setRecordStartIndex(0);
        drillDownRequest.setRecordCount(75);
        drillDownRequest.setQuery("log: exception");
        List<String> path = Arrays.asList("SomeLocation", "SomeInnerLocation");
        drillDownRequest.addCategoryPath("location", path);
        List<SearchResultEntry> results = this.service.drillDownSearch(tenantId, drillDownRequest);
        end = System.currentTimeMillis();
        Assert.assertEquals(results.size(), 75);
        System.out.println("* Search Result Count: " + results.size() + " Time: " + (end - start) + " ms.");

        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** END ANALYTICS DS (WITH FACET INDEXING - SINGLE THREAD) PERF TEST **************");
    }

    @Test (dependsOnMethods = "testFacetDataRecordAddReadPerformanceIndex1C")
    public void testFacetDataRecordAddReadPerformanceIndexMultipleTables1C() throws AnalyticsException {
        System.out.println("\n************** START ANALYTICS DS (WITH FACET INDEXING - SINGLE THREAD, MULTIPLE TABLES) PERF TEST **************");

        int tenantId = 50;
        String[] tableNames = new String[]{"TableY", "TableYY"};
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("ip", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("log", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("location", ColumnType.STRING, true, false, true));

        for (String tableName : tableNames) {
            this.cleanupTable(tenantId, tableName);
            this.service.createTable(tenantId, tableName);
            this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        }
        int n = 250, batch = 200;
        long start = System.currentTimeMillis();
        this.writeIndexRecordsWithFacets(tenantId, tableNames, n, batch);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        List<Record> recordsIn = new ArrayList<>();
        for (String tableName : tableNames) {
            recordsIn.addAll(AnalyticsDataServiceUtils.listRecords(this.service,
                                                              this.service.get(tenantId, tableName, 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)));
        }
        Assert.assertEquals(recordsIn.size(), (n * batch));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
        List<SearchResultEntry> results = new ArrayList<>();
        for (String tableName : tableNames) {
            drillDownRequest.setTableName(tableName);
            drillDownRequest.setRecordStartIndex(0);
            drillDownRequest.setRecordCount(75);
            drillDownRequest.setQuery("log: exception");
            List<String> path = Arrays.asList("SomeLocation", "SomeInnerLocation");
            drillDownRequest.addCategoryPath("location", path);
            results.addAll(this.service.drillDownSearch(tenantId, drillDownRequest));
        }
        end = System.currentTimeMillis();
        Assert.assertTrue(results.size() <= 75 * tableNames.length);

        System.out.println("* Search Result Count: " + results.size() + " Time: " + (end - start) + " ms.");
        start = System.currentTimeMillis();
        double count = 0;
        for (String tableName : tableNames) {
            drillDownRequest.setTableName(tableName);
            drillDownRequest.setRecordStartIndex(0);
            drillDownRequest.setRecordCount(75);
            drillDownRequest.setQuery("log:exception");
            List<String> path = Arrays.asList("SomeLocation", "SomeInnerLocation");
            drillDownRequest.addCategoryPath("location", path);
            count += this.service.drillDownSearchCount(tenantId, drillDownRequest);
        }
        end = System.currentTimeMillis();
        Assert.assertTrue(count == n * batch);
        System.out.println("* DrilldownSearchCount Result: " + count + " Time: " + (end - start) + " ms.");
        for (String tableName : tableNames) {
            this.cleanupTable(tenantId, tableName);
        }
        System.out.println("\n************** END ANALYTICS DS (WITH FACET INDEXING - SINGLE THREAD, MULTIPLE TABLES) PERF TEST **************");
    }
    
    private void writeIndexRecords(final int tenantId, final String tableName, final int n, 
            final int batch, final int nThreads) throws AnalyticsException {
        ExecutorService es = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            es.execute(new Runnable() {            
                @Override
                public void run() {
                    try {
                        writeIndexRecords(tenantId, tableName, n, batch);
                    } catch (AnalyticsException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        try {
            es.shutdown();
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new AnalyticsException(e.getMessage(), e);
        }
    }

    private void writeIndexRecordsWithFacets(final int tenantId, final String[] tableName, final int n,
                                   final int batch, final int nThreads) throws AnalyticsException {
        ExecutorService es = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        writeIndexRecordsWithFacets(tenantId, tableName, n, batch);
                    } catch (AnalyticsException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        try {
            es.shutdown();
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new AnalyticsException(e.getMessage(), e);
        }
    }
    
    @Test (dependsOnMethods = "testFacetDataRecordAddReadPerformanceIndexMultipleTables1C")
    public void testDataRecordAddReadPerformanceIndexNC() throws AnalyticsException {
        System.out.println("\n************** START ANALYTICS DS (WITH INDEXING - MULTIPLE THREADS) PERF TEST **************");

        int tenantId = 50;
        String tableName = "TableX";
        this.cleanupTable(tenantId, tableName);        
        int n = 50, batch = 200, nThreads = 5;
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("ip", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("log", ColumnType.STRING, true, false));        
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        
        long start = System.currentTimeMillis();
        this.writeIndexRecords(tenantId, tableName, n, batch, nThreads);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch * nThreads));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch * nThreads) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch * nThreads));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch * nThreads) / (double) (end - start) * 1000.0);        
        start = System.currentTimeMillis();
        List<SearchResultEntry> results = this.service.search(tenantId, tableName, "log: exception", 0, 75);
        end = System.currentTimeMillis();
        Assert.assertEquals(results.size(), 75);
        System.out.println("* Search Result Count: " + results.size() + " Time: " + (end - start) + " ms.");
        
        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** END ANALYTICS DS (WITH INDEXING - MULTIPLE THREADS) PERF TEST **************");
    }

    @Test (dependsOnMethods = "testDataRecordAddReadPerformanceIndexNC")
    public void testFacetDataRecordAddReadPerformanceIndexNC() throws AnalyticsException {
        System.out.println("\n************** START ANALYTICS DS (WITH FACET INDEXING - MULTIPLE THREADS) PERF TEST **************");

        int tenantId = 50;
        String tableName = "TableZ";
        this.cleanupTable(tenantId, tableName);
        int n = 50, batch = 200, nThreads = 5;
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("ip", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("log", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("location", ColumnType.STRING, true, false, true));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));

        long start = System.currentTimeMillis();
        this.writeIndexRecordsWithFacets(tenantId, new String[]{tableName}, n, batch, nThreads);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch * nThreads));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch * nThreads) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        List<Record> recordsIn = AnalyticsDataServiceUtils.listRecords(this.service,
                                                          this.service.get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch * nThreads));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch * nThreads) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
        drillDownRequest.setTableName(tableName);
        drillDownRequest.setRecordStartIndex(0);
        drillDownRequest.setRecordCount(75);
        drillDownRequest.setQuery("log:exception");
        List<String> path = Arrays.asList("SomeLocation", "SomeInnerLocation");
        drillDownRequest.addCategoryPath("location", path);
        List<SearchResultEntry> results = this.service.drillDownSearch(tenantId, drillDownRequest);
        end = System.currentTimeMillis();
        Assert.assertEquals(results.size(), 75);
        System.out.println("* Drilldown Result Count: " + results.size() + " Time: " + (end - start) + " ms.");
        start = System.currentTimeMillis();
        double count = this.service.drillDownSearchCount(tenantId, drillDownRequest);
        end = System.currentTimeMillis();
        Assert.assertEquals(count, 50000.0);
        System.out.println("* DrilldownSearch Result Count: " + count + " Time: " + (end - start) + " ms.");
        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** END ANALYTICS DS (WITH FACET INDEXING - MULTIPLE THREADS) PERF TEST **************");
    }

    @Test (dependsOnMethods = "testFacetDataRecordAddReadPerformanceIndexNC")
    public void testFacetDataRecordAddReadPerformanceIndexMultipleTablesNC() throws AnalyticsException {
        System.out.println("\n************** START ANALYTICS DS (WITH FACET INDEXING - MULTIPLE THREADS, MULTIPLE TABLES) PERF TEST **************");

        int tenantId = 50;
        String[] tableNames = new String[]{"TableZ", "TableZZ"};
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinitionExt("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinitionExt("ip", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("log", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinitionExt("location", ColumnType.STRING, true, false, true));
        for (String tableName : tableNames) {
            this.cleanupTable(tenantId, tableName);
            this.service.createTable(tenantId, tableName);
            this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        }

        int n = 50, batch = 200, nThreads = 5;
        long start = System.currentTimeMillis();
        this.writeIndexRecordsWithFacets(tenantId, tableNames, n, batch, nThreads);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch * nThreads));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch * nThreads) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        List<Record> recordsIn = new ArrayList<>();
        for (String tableName : tableNames) {
            recordsIn.addAll(AnalyticsDataServiceUtils.listRecords(this.service,
                                                      this.service.get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)));
        }

        Assert.assertEquals(recordsIn.size(), (n * batch * nThreads));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch * nThreads) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        List<SearchResultEntry> results = new ArrayList<>();
        for (String tableName : tableNames) {
            AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
            drillDownRequest.setTableName(tableName);
            drillDownRequest.setRecordStartIndex(0);
            drillDownRequest.setRecordCount(75);
            drillDownRequest.setQuery("log:exception");
            drillDownRequest.setScoreFunction("_weight");
            List<String> path = Arrays.asList("SomeLocation", "SomeInnerLocation");
            drillDownRequest.addCategoryPath("location", path);
            results.addAll(this.service.drillDownSearch(tenantId, drillDownRequest));
        }
        end = System.currentTimeMillis();
        Assert.assertTrue(results.size() <= 75 * tableNames.length);
        System.out.println("* Drilldown Result Count: " + results.size() + " Time: " + (end - start) + " ms.");
        start = System.currentTimeMillis();
        double count = 0;
        for (String tableName : tableNames) {
            AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
            drillDownRequest.setTableName(tableName);
            drillDownRequest.setRecordStartIndex(0);
            drillDownRequest.setRecordCount(75);
            drillDownRequest.setQuery("log:exception");
            List<String> path = Arrays.asList("SomeLocation", "SomeInnerLocation");
            drillDownRequest.addCategoryPath("location", path);
            count += this.service.drillDownSearchCount(tenantId, drillDownRequest);
        }
        end = System.currentTimeMillis();
        Assert.assertEquals((int) count, n * batch * nThreads);
        System.out.println("* DrilldownSearchCount Result: " + count + " Time: " + (end - start) + " ms.");
        for (String tableName: tableNames) {
            this.cleanupTable(tenantId, tableName);
        }
        System.out.println("\n************** END ANALYTICS DS (WITH FACET INDEXING - MULTIPLE THREADS, MULTIPLE TABLES) PERF TEST **************");
    }

    private void resetClusterTestResults() {
        this.becameLeader = false;
        this.leaderUpdated = false;
    }
    
    @Test (dependsOnMethods = "testFacetDataRecordAddReadPerformanceIndexMultipleTablesNC")
    public void testAnalyticsClusterManager() throws AnalyticsClusterException {
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (!acm.isClusteringEnabled()) {
            return;
        }
        this.resetClusterTestResults();        
        acm.joinGroup("G1", this);
        Assert.assertTrue(this.leaderUpdated);
        int value = 5;
        List<Integer> result = acm.executeAll("G1", new ClusterGroupTestMessage(value));
        Assert.assertTrue(result.size() > 0);
        Assert.assertEquals((int) result.get(0), value + 1);
        List<Object> members = acm.getMembers("G1");
        Assert.assertTrue(members.size() > 0);
        int result2 = acm.executeOne("G1", members.get(0), new ClusterGroupTestMessage(value + 1));
        Assert.assertEquals(result2, value + 2);
        this.resetClusterTestResults();
    }

    @Test (enabled = false)
    @Override
    public void onBecomingLeader() {
        this.becameLeader = true;
        AnalyticsServiceHolder.getAnalyticsClusterManager().setProperty("G1", "location", "10.0.0.2");
    }

    @Test (enabled = false)
    @Override
    public void onLeaderUpdate() {
        this.leaderUpdated = true;
        Assert.assertTrue(this.becameLeader);
        String location = (String) AnalyticsServiceHolder.getAnalyticsClusterManager().getProperty("G1", "location");
        Assert.assertEquals(location, "10.0.0.2");
        location = (String) AnalyticsServiceHolder.getAnalyticsClusterManager().getProperty("GX", "location");
        Assert.assertNull(location);
    }

    @Override
    public void onMembersChangeForLeader(boolean removed) {
        /* nothing to do */
    }
    
    @Override
    public void onMemberRemoved() {
        /* nothing to do */
    }
    
    /**
     * Test cluster message implementation.
     */
    public static class ClusterGroupTestMessage implements Callable<Integer>, Serializable {

        private static final long serialVersionUID = 3918252455368655212L;
        
        private int data;
                
        public ClusterGroupTestMessage(int data) {
            this.data = data;
        }
        
        @Override
        public Integer call() throws Exception {
            return data + 1;
        }
        
    }

}
