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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem.DataInput;

/**
 * This class contains tests related to analytics data sources.
 */
public class AnalyticsDataSourceTest {

    private AnalyticsDataSource analyticsDS;
    
    private AnalyticsFileSystem analyticsFileSystem;
    
    private String implementationName;
    
    public AnalyticsDataSourceTest() {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InMemoryICFactory.class.getName());
    }
    
    public void init(String implementationName, AnalyticsDataSource analyticsDS) throws AnalyticsException {
        this.implementationName = implementationName;
        this.analyticsDS = analyticsDS;
        this.analyticsDS.deleteTable(7, "mytable1");
        this.analyticsDS.deleteTable(7, "T1");
        try {
            this.analyticsFileSystem = this.analyticsDS.getFileSystem();
        } catch (IOException e) {
            throw new AnalyticsException(e.getMessage(), e);
        }
    }
    
    public String getImplementationName() {
        return implementationName;
    }

    private Record createRecord(int tenantId, String tableName, String serverName, String ip, int tenant, String log) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("server_name", serverName);
        values.put("ip", ip);
        values.put("tenant", tenant);
        values.put("log", log);
        values.put("sequence", null);
        values.put("summary2", null);
        return new Record(tenantId, tableName, values, System.currentTimeMillis());
    }
    
    public static List<Record> generateRecords(int tenantId, String tableName, int i, int c, long time, int timeOffset) {
        List<Record> result = new ArrayList<Record>();
        Map<String, Object> values;
        long timeTmp;
        for (int j = 0; j < c; j++) {
            values = new HashMap<String, Object>();
            values.put("server_name", "ESB-" + i);
            values.put("ip", "192.168.0." + (i % 256));
            values.put("tenant", i);
            values.put("spam_index", i + 0.3454452);
            values.put("important", i % 2 == 0 ? true : false);
            values.put("sequence", i + 104050000L);
            values.put("summary", "Joey asks, how you doing?");
            values.put("log", "Exception in Sequence[" + i + "," + j + "]");
            if (time != -1) {
                timeTmp = time;
                time += timeOffset;
            } else {
                timeTmp = System.currentTimeMillis();
            }
            result.add(new Record(tenantId, tableName, values, timeTmp));
        }
        return result;
    }
    
    private List<Record> generateRecordsForUpdate(List<Record> recordsIn) {
        List<Record> result = new ArrayList<Record>();
        Map<String, Object> values;
        for (Record recordIn : recordsIn) {
            values = new HashMap<String, Object>();
            values.put("server_name", "ESB-" + recordIn.getId());
            values.put("ip", "192.168.0." + (recordIn.getTimestamp() % 256));
            values.put("tenant", recordIn.getTenantId());
            values.put("change_index", recordIn.getTenantId() + 0.3454452);
            result.add(new Record(recordIn.getId(), recordIn.getTenantId(), recordIn.getTableName(), 
                    values, System.currentTimeMillis()));
        }
        return result;
    }
    
    public static Set<Record> recordGroupsToSet(RecordGroup[] rgs) throws AnalyticsException {
        Set<Record> result = new HashSet<Record>();
        for (RecordGroup rg : rgs) {
            result.addAll(rg.getRecords());
        }
        return result;
    }
    
    @AfterTest
    @Test
    public void cleanup() throws AnalyticsException {
        this.analyticsDS.deleteTable(7, "MYTABLE1");
        this.analyticsDS.deleteTable(7, "T1");
    }
    
    private void cleanupT1() throws AnalyticsException {
        this.analyticsDS.deleteTable(7, "T1");
        Assert.assertFalse(this.analyticsDS.tableExists(7, "T1"));
    }
    
    @Test
    public void testTableCreateDeleteList() throws AnalyticsException {
        this.analyticsDS.deleteTable(250035, "TABLE1");
        this.analyticsDS.deleteTable(250035, "TABLE2");
        this.analyticsDS.deleteTable(8830, "TABLEX");        
        this.analyticsDS.createTable(250035, "TABLE1");
        List<String> tables = this.analyticsDS.listTables(250035);
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(new HashSet<String>(tables).contains("TABLE1"));
        Assert.assertTrue(this.analyticsDS.tableExists(250035, "table1"));
        Assert.assertTrue(this.analyticsDS.tableExists(250035, "TABLE1"));
        /* this should not throw an exception */
        this.analyticsDS.createTable(250035, "Table1");
        Record record = this.createRecord(250035, "TABLE2", "S1", "10.0.0.1", 1, "LOG");
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        this.analyticsDS.deleteTable(250035, "TABLE2");
        this.analyticsDS.deleteTable(250035, "TABLE1");
        this.analyticsDS.deleteTable(8830, "TABLEX");
        Assert.assertEquals(this.analyticsDS.listTables(250035).size(), 0);
        Assert.assertEquals(this.analyticsDS.listTables(8830).size(), 0);
    }
    
    @Test
    public void testDataRecordAddRetrieve() throws AnalyticsException {
        this.cleanupT1();
        String serverName = "ESB1";
        String ip = "10.0.0.1";
        int tenant = 44;
        String log = "Boom!";
        this.analyticsDS.createTable(7, "T1");
        Record record = this.createRecord(7, "T1", serverName, ip, tenant, log);
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        this.analyticsDS.insert(records);
        String id = record.getId();
        List<String> ids = new ArrayList<String>();
        ids.add(id);
        RecordGroup[] rgs = this.analyticsDS.get(7, "T1", null, ids);
        Assert.assertEquals(rgs.length, 1);
        List<Record> recordsIn = rgs[0].getRecords();
        Assert.assertEquals(recordsIn.size(), 1);
        Record recordIn = recordsIn.get(0);
        Assert.assertEquals(record.getId(), recordIn.getId());
        Assert.assertEquals(record.getTableName(), recordIn.getTableName());
        Assert.assertEquals(record.getTimestamp(), recordIn.getTimestamp());
        Assert.assertEquals(record.getNotNullValues(), recordIn.getNotNullValues());
        Assert.assertEquals(record, recordIn);
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieve() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsDS.createTable(7, "T1");
        List<Record> records = generateRecords(7, "T1", 1, 100, -1, -1);
        this.analyticsDS.insert(records);
        Set<Record> recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        List<String> columns = new ArrayList<String>();
        columns.add("server_name");
        columns.add("ip");
        columns.add("tenant");
        columns.add("log");
        columns.add("summary");
        columns.add("summary2");
        columns.add("sequence");
        columns.add("spam_index");
        columns.add("important");
        recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", columns, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        columns.remove("ip");
        recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", columns, -1, -1, 0, -1));
        Assert.assertNotEquals(recordsIn, new HashSet<Record>(records));
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveUpdate() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsDS.createTable(15, "T1");
        List<Record> records = generateRecords(15, "T1", 1, 50, -1, -1);
        this.analyticsDS.insert(records);
        Set<Record> recordsIn = recordGroupsToSet(this.analyticsDS.get(15, "T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        records = this.generateRecordsForUpdate(records);
        this.analyticsDS.update(records);
        recordsIn = recordGroupsToSet(this.analyticsDS.get(15, "T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        this.cleanupT1();
    }
    
    @Test
    public void testDataRecordCount() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsDS.createTable(7, "T1");
        int count = (int) (200 * Math.random()) + 1;
        List<Record> records = generateRecords(7, "T1", 1, count, -1, -1);
        this.analyticsDS.insert(records);
        Assert.assertEquals(this.analyticsDS.getRecordCount(7, "T1"), count);
        this.analyticsDS.delete(7, "T1", -1, -1);
        Assert.assertEquals(this.analyticsDS.getRecordCount(7, "T1"), 0);
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange1() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsDS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsDS.insert(records);
        Set<Record> recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time - 10, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time + 1, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time + 1, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        records.remove(99);
        records.remove(0);
        Assert.assertEquals(new HashSet<Record>(records), new HashSet<Record>(recordsIn));
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange2() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsDS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsDS.insert(records);
        Set<Record> recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time + 22, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(recordsIn.size(), 97);
        recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time, time + timeOffset * 96 - 2, 0, -1));
        Assert.assertEquals(recordsIn.size(), 96);
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange3() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsDS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsDS.insert(records);
        Set<Record> recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time - 100, time - 10, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time + timeOffset * 103, time + timeOffset * 110, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithPagination1() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsDS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 2, 200, time, timeOffset);
        this.analyticsDS.insert(records);
        Set<Record> recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 1, -1));
        Assert.assertEquals(recordsIn1.size(), 199);        
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 1, 200));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn1.size(), 200);
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 1, 199));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 1, 100));
        Assert.assertEquals(recordsIn1.size(), 100);
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 100, 101));
        Assert.assertEquals(recordsIn1.size(), 100);
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 55, 73));
        Assert.assertEquals(recordsIn1.size(), 73);
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1));
        List<Record> recordsIn2 = new ArrayList<Record>();
        for (int i = 0; i < 200; i += 20) {
            recordsIn2.addAll(recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, i, 20)));
        }
        Assert.assertEquals(recordsIn2.size(), 200);
        Assert.assertEquals(recordsIn1, new HashSet<Record>(recordsIn2));
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithPagination2() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsDS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 2, 200, time, timeOffset);
        this.analyticsDS.insert(records);
        Set<Record> recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time, time + timeOffset * 200, 1, 200));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time, time + timeOffset * 200, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 200);
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time + 55, time + timeOffset * 200, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 194);
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time + 55, time + timeOffset * 199, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 193);
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time + 55, time + timeOffset * 198 - 5, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 192);
        List<Record> recordsIn2 = new ArrayList<Record>();
        for (int i = 0; i < 200; i += 10) {
            recordsIn2.addAll(recordGroupsToSet(this.analyticsDS.get(7, "T1", null, time + 55, time + timeOffset * 198 - 5, i, 10)));
        }
        Assert.assertEquals(recordsIn2.size(), 192);
        Assert.assertEquals(recordsIn1, new HashSet<Record>(recordsIn2));
        List<String> columns = new ArrayList<String>();
        columns.add("tenant");
        columns.add("ip");
        recordsIn1 = recordGroupsToSet(this.analyticsDS.get(7, "T1", columns, time + 55, time + timeOffset * 198 - 5, 0, 200));
        Record r1 = recordsIn1.iterator().next();
        Record r2 = recordsIn1.iterator().next();
        Assert.assertEquals(r1.getValues().size(), 2);
        Assert.assertEquals(r2.getValues().size(), 2);
        StringBuilder columnNames = new StringBuilder();
        for (String col : r1.getValues().keySet()) {
            columnNames.append(col);
        }
        StringBuilder values = new StringBuilder();
        for (Object val : r2.getValues().values()) {
            values.append(val.toString());
        }
        Assert.assertTrue(columnNames.toString().contains("tenant"));
        Assert.assertTrue(columnNames.toString().contains("ip"));
        Assert.assertTrue(values.toString().equals("2192.168.0.2") || values.toString().equals("192.168.0.22"));
        this.cleanupT1();
    }
    
    @Test
    public void testDataRecordDeleteWithIds() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsDS.createTable(7, "T1");
        List<Record> records = generateRecords(7, "T1", 2, 10, -1, -1);
        this.analyticsDS.insert(records);
        Assert.assertEquals(recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1)).size(), 10);
        List<String> ids = new ArrayList<String>();
        ids.add(records.get(2).getId());
        ids.add(records.get(5).getId());
        this.analyticsDS.delete(7, "T1", ids);
        Assert.assertEquals(recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1)).size(), 8);
        ids.clear();
        ids.add(records.get(0).getId());
        this.analyticsDS.delete(7, "T1", ids);
        Assert.assertEquals(recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1)).size(), 7);
        this.analyticsDS.delete(7, "T1", ids);
        Assert.assertEquals(recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1)).size(), 7);
        this.analyticsDS.delete(7, "T1", new ArrayList<String>());
        Assert.assertEquals(recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1)).size(), 7);
        this.cleanupT1();
    }
    
    @Test
    public void testDataRecordDeleteWithTimestamps() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsDS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsDS.insert(records);
        Assert.assertEquals(recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1)).size(), 100);
        this.analyticsDS.delete(7, "T1", time - 100, time + 12);
        Set<Record> recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        records.remove(0);
        records.remove(0);
        Assert.assertEquals(new HashSet<Record>(records), recordsIn);
        this.analyticsDS.delete(7, "T1", time + timeOffset * 97, time + timeOffset * 101);
        recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1));
        records.remove(97);
        records.remove(96);
        records.remove(95);
        Assert.assertEquals(new HashSet<Record>(records), recordsIn);
        this.analyticsDS.delete(7, "T1", time + timeOffset * 5 - 2, time + timeOffset * 7 + 4);
        recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1));
        records.remove(5);
        records.remove(4);
        records.remove(3);
        Assert.assertEquals(new HashSet<Record>(records), recordsIn);
        this.cleanupT1();
    }
    
    @Test
    public void testDataRecordAddReadPerformance() throws AnalyticsException {
        System.out.println("\n************** START RECORD PERF TEST [" + this.getImplementationName() + "] **************");
        this.cleanupT1();
        
        /* warm-up */
        this.analyticsDS.createTable(7, "T1");
        List<Record> records;
        for (int i = 0; i < 10; i++) {
            records = generateRecords(7, "T1", i, 100, -1, -1);
            this.analyticsDS.insert(records);
        }
        this.cleanupT1();
        
        this.analyticsDS.createTable(7, "T1");
        long hash1 = 0;
        int n = 100, batch = 200;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            records = generateRecords(7, "T1", i, batch, -1, -1);
            this.analyticsDS.insert(records);
            for (Record record : records) {
                hash1 += record.hashCode();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        Set<Record> recordsIn = recordGroupsToSet(this.analyticsDS.get(7, "T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch));
        end = System.currentTimeMillis();
        long hash2 = 0;
        for (Record record : recordsIn) {
            hash2 += record.hashCode();
        }
        Assert.assertEquals(hash1, hash2);
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        System.out.println("************** END RECORD PERF TEST [" + this.getImplementationName() + "] **************\n");
        this.cleanupT1();
    }
    
    private void addFilesToDir(String dir, String...files) throws IOException {
        OutputStream out;
        for (String file : files) {
            out = this.analyticsFileSystem.createOutput(dir + "/" + file);
            out.write(generateData(100));
            out.close();
        }
    }
    
    @Test
    public void testFSDirectoryOperations() throws IOException {
        this.analyticsFileSystem.delete("/d1");
        this.analyticsFileSystem.mkdir("/d1/d2/d3");
        this.analyticsFileSystem.mkdir("/d1/d2/d5");
        Assert.assertTrue(this.analyticsFileSystem.exists("/d1/d2/d3"));
        Assert.assertTrue(this.analyticsFileSystem.exists("/d1/d2/d5"));
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d4"));
        List<String> files = this.analyticsFileSystem.list("/d1/d2");
        Assert.assertEquals(files.size(), 2);
        /* the path must be normalized, can end with "/" or not */
        files = this.analyticsFileSystem.list("/d1/d2/");
        Assert.assertEquals(files.size(), 2);
        Assert.assertEquals(new HashSet<String>(Arrays.asList(new String[] { "d3", "d5" })), 
                new HashSet<String>(files));
        this.addFilesToDir("/d1/d2", "f1", "f2", "f3");
        files = this.analyticsFileSystem.list("/d1/d2");
        Assert.assertEquals(files.size(), 5);
        Assert.assertEquals(new HashSet<String>(Arrays.asList(new String[] { "d3", "d5", "f1", "f2", "f3" })), 
                new HashSet<String>(files));
        this.analyticsFileSystem.delete("/d1");
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d3"));
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d5"));
    }
    
    public static byte[] generateData(int size) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < size; i++) {
            out.write((int) (Math.random() * 127));
        }
        try {
            out.close();
        } catch (IOException ignore) {
            /* never happens */
        }
        return out.toByteArray();
    }
    
    @Test
    public void testFSFileIOOperations() throws IOException {
        this.analyticsFileSystem.delete("/d1");
        OutputStream out = this.analyticsFileSystem.createOutput("/d1/d2/d3/f1");
        byte[] data = generateData(1024 * 1024 + 7);
        long start = System.currentTimeMillis();
        out.write(data, 0, data.length);
        out.flush();
        out.close();
        long end = System.currentTimeMillis();
        System.out.println("File data (1 MB) written in: " + (end - start) + " ms.");
        Assert.assertEquals(this.analyticsFileSystem.length("/d1/d2/d3/f1"), data.length);
        DataInput in = this.analyticsFileSystem.createInput("/d1/d2/d3/f1");
        byte[] dataIn = new byte[data.length];
        start = System.currentTimeMillis();
        int len = in.read(dataIn, 0, dataIn.length);
        in.close();
        end = System.currentTimeMillis();
        System.out.println("File data (1 MB) read in: " + (end - start) + " ms.");
        Assert.assertEquals(len, data.length);
        Assert.assertEquals(data, dataIn);
        this.analyticsFileSystem.delete("/d1/d2/d3/f1");
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d3/f1"));
        this.analyticsFileSystem.delete("/d1/d2/d3");
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d3"));
        this.analyticsFileSystem.delete("/d1");
    }
    
    private void fileRandomWriteRead(String path, int writerBufferSize, int readBufferSize, int n) 
            throws IOException {
        OutputStream out = this.analyticsFileSystem.createOutput(path);
        ByteArrayOutputStream byteOut1 = new ByteArrayOutputStream();
        byte[] data = generateData(writerBufferSize);
        for (int i = 0; i < n; i++) {
            out.write(data, 0, data.length);
            if (i % 10 == 0) {
                out.flush();
            }
            byteOut1.write(data, 0, data.length);
        }
        out.close();
        byteOut1.close();
        Assert.assertEquals(this.analyticsFileSystem.length(path), n * writerBufferSize);
        DataInput in = this.analyticsFileSystem.createInput(path);
        byte[] buff = new byte[readBufferSize];
        int j;
        ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
        while ((j = in.read(buff, 0, buff.length)) > 0) {
            byteOut2.write(buff, 0, j);
        }
        byteOut2.close();
        in.close();
        Assert.assertEquals(byteOut1.toByteArray(), byteOut2.toByteArray());
    }
    
    @Test
    public void testFSFileIOOperations2() throws AnalyticsException, IOException {
        this.analyticsFileSystem.delete("/d1");
        this.fileRandomWriteRead("/d1/d2/f1", 1350, 1350, 100);
        this.fileRandomWriteRead("/d1/d2/f2", 1350, 200, 150);
        this.fileRandomWriteRead("/d1/d2/f3", 240, 2000, 50);
        this.fileRandomWriteRead("/d1/d2/f2", 10, 500, 300);
        this.fileRandomWriteRead("/d1/d2/f1", 1, 1, 1);
        this.analyticsFileSystem.delete("/d1");
        Assert.assertFalse(this.analyticsFileSystem.exists("/d1/d2/d3"));
    }
    
    private void fileReadSeekPosition(String path, int n, int chunk, int... locs) throws IOException {
        byte[] data = generateData(n);
        OutputStream out = this.analyticsFileSystem.createOutput(path);
        out.write(data, 0, data.length);
        out.close();
        byte[] din = new byte[chunk];
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        byte[] din2 = new byte[chunk];
        DataInput in = this.analyticsFileSystem.createInput(path);
        int count, count2;
        for (int i : locs) {
            in.seek(i);
            Assert.assertEquals(in.getPosition(), i);
            count = in.read(din, 0, din.length);
            Assert.assertEquals(in.getPosition(), i + (count < 0 ? 0 : count));
            bin.reset();
            bin.skip(i);
            count2 = bin.read(din2, 0, din2.length);
            Assert.assertEquals(count, count2);
            Assert.assertEquals(din, din2);
        }
    }
    
    @Test
    public void testFSReadSeekPosition() throws IOException {
        this.analyticsFileSystem.delete("/d1");
        this.fileReadSeekPosition("/d1/f1", 2000, 5, 0, 10, 5, 50, 100, 1570, 1998, 0);
        this.fileReadSeekPosition("/d1/f2", 100, 5, 99);
        this.fileReadSeekPosition("/d1/f3", 100, 5, 0, 10, 5, 50, 99, 0, 1, 20);
        this.fileReadSeekPosition("/d1/f4", 10, 1, 0, 10);
        this.fileReadSeekPosition("/d1/f4", 10, 1, 0, 10, 5, 7, 9, 0, 1, 0);
        this.analyticsFileSystem.delete("/d1");
    }
    
    @Test
    public void testFSPerfTest() throws IOException {
        System.out.println("\n************** START FS PERF TEST [" + this.getImplementationName() + "] **************");
        this.analyticsFileSystem.delete("/mydir");
        byte[] data = generateData(2048);
        OutputStream out;
        
        /* warm-up */
        for (int i = 0; i < 100; i++) {
            out = this.analyticsFileSystem.createOutput("/mydir/perf_warmup/file" + i);
            out.write(data, 0, data.length);
            out.close();
        }
        
        long start = System.currentTimeMillis();
        int count = 1000;
        for (int i = 0; i < count; i++) {
            out = this.analyticsFileSystem.createOutput("/mydir/perf/file" + i);
            out.write(data, 0, data.length);
            out.close();
        }
        System.out.println();
        long end = System.currentTimeMillis();
        System.out.println("* " + count + " 2K files written in: " + (end - start) + " ms. " + (count / (double) (end - start) * 1000.0) + " FPS.");
        DataInput in;
        byte[] dataIn = new byte[data.length];
        int len;
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            in = this.analyticsFileSystem.createInput("/mydir/perf/file" + i);
            len = in.read(dataIn, 0, dataIn.length);
            in.close();
            Assert.assertEquals(len, dataIn.length);
        }
        end = System.currentTimeMillis();
        System.out.println("* " + count + " 2K files read in: " + (end - start) + " ms. " + (count / (double) (end - start) * 1000.0) + " FPS.");
        this.analyticsFileSystem.delete("/mydir");
        System.out.println("\n************** END FS PERF TEST [" + this.getImplementationName() + "] **************");
    }
    
}
