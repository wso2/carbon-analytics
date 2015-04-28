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
package org.wso2.carbon.analytics.datasource.rdbms;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterException;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.clustering.GroupEventListener;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema.ColumnType;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.base.MultitenantConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
               
    public void init(AnalyticsDataService service) {
        this.service = service;
    }
    
    @Test
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
        columns.add(new ColumnDefinition("C1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("C2", ColumnType.BOOLEAN, true, false));
        columns.add(new ColumnDefinition("C3", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("C4", ColumnType.LONG, true, false));
        columns.add(new ColumnDefinition("C5", ColumnType.FLOAT, true, false));
        columns.add(new ColumnDefinition("C6", ColumnType.DOUBLE, true, false));
        columns.add(new ColumnDefinition("C7", ColumnType.BINARY, true, false));
        columns.add(new ColumnDefinition("C8", ColumnType.FACET, true, false));
        AnalyticsSchema schema = new AnalyticsSchema(columns, null);
        this.service.setTableSchema(tenantId, "T1", schema);
        AnalyticsSchema schemaIn = this.service.getTableSchema(tenantId, "T1");
        Assert.assertEquals(schemaIn, schemaIn);
        this.cleanupTable(tenantId, "T1");
    }
    
    private void cleanupTable(int tenantId, String tableName) throws AnalyticsException {
        if (this.service.tableExists(tenantId, tableName)) {
            this.service.deleteTable(tenantId, tableName);
        }
        this.service.clearIndexData(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema());
    }
    
    private List<Record> generateIndexRecords(int tenantId, String tableName, int n, long startTimestamp) {
        Map<String, Object> values = new HashMap<String, Object>();
        Record record;
        List<Record> result = new ArrayList<Record>();
        for (int i = 0; i < n; i++) {
            values = new HashMap<String, Object>();
            values.put("INT1", i);
            values.put("STR1", "STRING" + i);
            values.put("str2", "string" + i);
            values.put("TXT1", "My name is bill" + i);
            values.put("LN1", 1435000L + i);
            values.put("DB1", 54.535 + i);
            values.put("FL1", 3.14 + i);
            values.put("BL1", i % 2 == 0 ? true : false);
            record = new Record(tenantId, tableName, values, startTimestamp + i * 10);
            result.add(record);
        }        
        return result;
    }
    
    private void indexDataAddRetrieve(int tenantId, String tableName, int n) throws AnalyticsException {
        this.cleanupTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("INT1", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("STR1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("str2", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("TXT1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("LN1", ColumnType.LONG, true, false));
        columns.add(new ColumnDefinition("DB1", ColumnType.DOUBLE, true, false));
        columns.add(new ColumnDefinition("FL1", ColumnType.FLOAT, true, false));
        columns.add(new ColumnDefinition("BL1", ColumnType.BOOLEAN, true, false));
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
    
    @Test
    public void testIndexedDataAddRetrieve() throws AnalyticsException {
        this.indexDataAddRetrieve(5, "TX", 1);
        this.indexDataAddRetrieve(5, "TX", 10);
        this.indexDataAddRetrieve(6, "TX", 150);
        this.indexDataAddRetrieve(7, "TX", 2500);
    }
    
    @Test
    public void testSearchCount() throws AnalyticsException {
        int tenantId = 4;
        String tableName = "Books";
        this.cleanupTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("INT1", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("STR1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("str2", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("TXT1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("LN1", ColumnType.LONG, true, false));
        columns.add(new ColumnDefinition("DB1", ColumnType.DOUBLE, true, false));
        columns.add(new ColumnDefinition("FL1", ColumnType.FLOAT, true, false));
        columns.add(new ColumnDefinition("BL1", ColumnType.BOOLEAN, true, false));
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
    
    @Test
    public void testIndexedDataUpdate() throws Exception {
        int tenantId = 1;
        String tableName = "T1";
        this.cleanupTable(tenantId, tableName);
        this.service.createTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("STR1", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("STR2", ColumnType.STRING, true, false));
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("STR1", "Sri Lanka is known for tea");
        values.put("STR2", "Cricket is most famous");
        Map<String, Object> values2 = new HashMap<String, Object>();
        values2.put("STR1", "Canada is known for Ice Hockey");
        values2.put("STR2", "It is very cold");
        Record record = new Record(tenantId, tableName, values);
        Record record2 = new Record(tenantId, tableName, values2);
        List<Record> records = new ArrayList<Record>();
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
        values = new HashMap<String, Object>();
        values.put("STR1", "South Africa is know for diamonds");
        values.put("STR2", "NBA has the best basketball action");
        record = new Record(id, tenantId, tableName, values);
        records = new ArrayList<Record>();
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
    
    @Test
    public void testIndexDataDeleteWithIds() throws AnalyticsException {
        int tenantId = 5100;
        String tableName = "X1";
        this.cleanupTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("INT1", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("STR1", ColumnType.STRING, true, false));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        List<Record> records = this.generateIndexRecords(tenantId, tableName, 98, 0);
        this.service.put(records);
        List<String> ids = new ArrayList<String>();
        ids.add(records.get(0).getId());
        ids.add(records.get(5).getId());
        ids.add(records.get(50).getId());
        ids.add(records.get(97).getId());
        Assert.assertEquals(GenericUtils.listRecords(this.service, this.service.get(tenantId, tableName, 2, null, ids)).size(), 4);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        List<SearchResultEntry> result = this.service.search(tenantId, tableName, "STR1:S*", 0, 150);
        Assert.assertEquals(result.size(), 98);
        this.service.delete(tenantId, tableName, ids);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        result = this.service.search(tenantId, tableName, "STR1:S*", 0, 150);
        Assert.assertEquals(result.size(), 94);
        Assert.assertEquals(GenericUtils.listRecords(this.service, this.service.get(tenantId, tableName, 3, null, ids)).size(), 0);
        this.cleanupTable(tenantId, tableName);
    }
    
    @Test
    public void testIndexDataDeleteRange() throws AnalyticsException {
        int tenantId = 230;
        String tableName = "Scores";
        int n = 115;
        this.cleanupTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("INT1", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("STR1", ColumnType.STRING, true, false));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));
        List<Record> records = this.generateIndexRecords(tenantId, tableName, n, 1000);
        this.service.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), n);
        this.service.delete(tenantId, tableName, 1030, 1060);
        recordsIn = GenericUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 5, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), n - 3);
        /* lets test table name case-insensitiveness too */
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        List<SearchResultEntry> results = this.service.search(tenantId, 
                tableName.toUpperCase(), "STR1:s*", 0, n);
        Assert.assertEquals(results.size(), n - 3);
        this.cleanupTable(tenantId, tableName);
    }
    
    @Test
    public void testDataRecordAddReadPerformanceNonIndex() throws AnalyticsException {
        int tenantId = 51;
        String tableName = "TableX";
        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** START ANALYTICS DS (WITHOUT INDEXING, H2-FILE) PERF TEST **************");
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
        List<Record> recordsIn = GenericUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 7, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch));
        end = System.currentTimeMillis();
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** END ANALYTICS DS (WITHOUT INDEXING, H2-FILE) PERF TEST **************");
    }
    
    private void writeIndexRecords(int tenantId, String tableName, int n, int batch) throws AnalyticsException {
        List<Record> records;
        for (int i = 0; i < n; i++) {
            records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, i, batch, -1, -1);
            this.service.put(records);
        }
    }

    private void writeIndexRecordsWithFacets(int tenantId, String tableName, int n, int batch) throws AnalyticsException {
        List<Record> records;
        for (int i = 0; i < n; i++) {
            records = AnalyticsRecordStoreTest.generateRecordsWithFacets(tenantId, tableName, i, batch, -1, -1);
            this.service.put(records);
        }
    }

    @Test
    public void testMultipleDataRecordAddRetrieveWithKeys() throws AnalyticsException {
        int tenantId = 1;
        String tableName = "MyT1";
        this.cleanupTable(tenantId, tableName);
        this.service.createTable(tenantId, tableName);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("log", ColumnType.STRING, true, false));
        List<String> primaryKeys = new ArrayList<String>();
        primaryKeys.add("tenant");
        primaryKeys.add("log");
        AnalyticsSchema schema = new AnalyticsSchema(columns, primaryKeys);
        this.service.setTableSchema(tenantId, tableName, schema);
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, 1, 75, -1, -1, false);
        this.service.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<Record>(recordsIn), new HashSet<Record>(records));
        records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, 1, 74, -1, -1, false);
        this.service.put(records);
        recordsIn = GenericUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 75);
        records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, 1, 77, -1, -1, false);
        this.service.put(records);
        recordsIn = GenericUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 77);
        Assert.assertEquals(new HashSet<Record>(recordsIn), new HashSet<Record>(records));
        primaryKeys.clear();
        schema = new AnalyticsSchema(columns, primaryKeys);
        this.service.setTableSchema(tenantId, tableName, schema);
        records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, 1, 10, -1, -1, false);
        this.service.put(records);
        recordsIn = GenericUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 87);
        schema = new AnalyticsSchema(columns, null);
        this.service.setTableSchema(tenantId, tableName, schema);
        records = AnalyticsRecordStoreTest.generateRecords(tenantId, tableName, 1, 10, -1, -1, false);
        this.service.put(records);
        recordsIn = GenericUtils.listRecords(this.service,
                this.service.get(tenantId, tableName, 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 97);
        this.cleanupTable(tenantId, tableName);
    }
    
    @Test
    public void testDataRecordAddReadPerformanceIndex1C() throws AnalyticsException {
        System.out.println("\n************** START ANALYTICS DS (WITH INDEXING - SINGLE THREAD, H2-FILE) PERF TEST **************");

        int tenantId = 50;
        String tableName = "TableX";
        this.cleanupTable(tenantId, tableName);
        int n = 250, batch = 200;
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("ip", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("log", ColumnType.STRING, true, false));
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
        List<Record> recordsIn = GenericUtils.listRecords(this.service,
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
        
        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** END ANALYTICS DS (WITH INDEXING, H2-FILE) PERF TEST **************");
    }

    @Test
    public void testFacetDataRecordAddReadPerformanceIndex1C() throws AnalyticsException {
        System.out.println("\n************** START ANALYTICS DS (WITH FACET INDEXING - SINGLE THREAD, H2-FILE) PERF TEST **************");

        int tenantId = 50;
        String tableName = "TableY";
        this.cleanupTable(tenantId, tableName);
        int n = 250, batch = 200;
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("ip", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("log", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("location", ColumnType.FACET, true, false));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));

        long start = System.currentTimeMillis();
        this.writeIndexRecordsWithFacets(tenantId, tableName, n, batch);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        List<Record> recordsIn = GenericUtils.listRecords(this.service,
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
        List<String> path = Arrays.asList(new String[]{"SomeLocation", "SomeInnerLocation"});
        drillDownRequest.addCategoryPath("location", path);
        List<SearchResultEntry> results = this.service.drillDownSearch(tenantId, drillDownRequest);
        end = System.currentTimeMillis();
        Assert.assertEquals(results.size(), 75);
        System.out.println("* Search Result Count: " + results.size() + " Time: " + (end - start) + " ms.");

        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** END ANALYTICS DS (WITH FACET INDEXING, H2-FILE) PERF TEST **************");
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

    private void writeIndexRecordsWithFacets(final int tenantId, final String tableName, final int n,
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
    
    @Test
    public void testDataRecordAddReadPerformanceIndexNC() throws AnalyticsException {
        System.out.println("\n************** START ANALYTICS DS (WITH INDEXING - MULTIPLE THREADS, H2-FILE) PERF TEST **************");

        int tenantId = 50;
        String tableName = "TableX";
        this.cleanupTable(tenantId, tableName);        
        int n = 50, batch = 200, nThreads = 5;
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("ip", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("log", ColumnType.STRING, true, false));        
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
        List<Record> recordsIn = GenericUtils.listRecords(this.service,
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
        System.out.println("\n************** END ANALYTICS DS (WITH INDEXING - MULTIPLE THREADS, H2-FILE) PERF TEST **************");
    }

    @Test
    public void testFacetDataRecordAddReadPerformanceIndexNC() throws AnalyticsException {
        System.out.println("\n************** START ANALYTICS DS (WITH FACET INDEXING - MULTIPLE THREADS, H2-FILE) PERF TEST **************");

        int tenantId = 50;
        String tableName = "TableZ";
        this.cleanupTable(tenantId, tableName);
        int n = 50, batch = 200, nThreads = 5;
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("tenant", ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("ip", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("log", ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("location", ColumnType.FACET, true, false));
        this.service.createTable(tenantId, tableName);
        this.service.setTableSchema(tenantId, tableName, new AnalyticsSchema(columns, null));

        long start = System.currentTimeMillis();
        this.writeIndexRecordsWithFacets(tenantId, tableName, n, batch, nThreads);
        this.service.waitForIndexing(DEFAULT_WAIT_TIME);
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch * nThreads));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch * nThreads) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        List<Record> recordsIn = GenericUtils.listRecords(this.service,
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
        drillDownRequest.setScoreFunction("_weight");
        List<String> path = Arrays.asList(new String[] {"SomeLocation", "SomeInnerLocation"});
        drillDownRequest.addCategoryPath("location", path);
        List<SearchResultEntry> results = this.service.drillDownSearch(tenantId, drillDownRequest);
        end = System.currentTimeMillis();
        Assert.assertEquals(results.size(), 75);
        System.out.println("* Drilldown Result Count: " + results.size() + " Time: " + (end - start) + " ms.");
        start = System.currentTimeMillis();
        int count = this.service.drillDownSearchCount(tenantId, drillDownRequest);
        end = System.currentTimeMillis();
        Assert.assertEquals(count, 50000);
        System.out.println("* DrilldownSearch Result Count: " + count + " Time: " + (end - start) + " ms.");
        this.cleanupTable(tenantId, tableName);
        System.out.println("\n************** END ANALYTICS DS (WITH INDEXING - MULTIPLE THREADS, H2-FILE) PERF TEST **************");
    }
    
    private void resetClusterTestResults() {
        this.becameLeader = false;
        this.leaderUpdated = false;
    }
    
    @Test
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
    public void onMembersChangeForLeader() {
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
