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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.Record.Column;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem.DataInput;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem.DataOutput;

/**
 * This class contains tests related to analytics data sources.
 */
public class AnalyticsDataSourceTest {

    private AnalyticsDataSource analyticsDS;
    
    private FileSystem fileSystem;
    
    private String implementationName;
    
    public AnalyticsDataSourceTest() {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InMemoryICFactory.class.getName());
    }
    
    public void init(String implementationName, AnalyticsDataSource analyticsDS) throws AnalyticsDataSourceException {
        this.implementationName = implementationName;
        this.analyticsDS = analyticsDS;
        this.analyticsDS.deleteTable("acme.com", "MyTable1");
        this.analyticsDS.deleteTable("acme.com", "T1");
        this.fileSystem = this.analyticsDS.getFileSystem();
    }
    
    public String getImplementationName() {
        return implementationName;
    }

    private Record createRecord(String tableCategory, String tableName, String serverName, String ip, int tenant, String log) {
        List<Column> values = new ArrayList<Record.Column>();
        values.add(new Column("server_name", serverName));
        values.add(new Column("ip", ip));
        values.add(new Column("tenant", tenant));
        values.add(new Column("log", log));
        values.add(new Column("sequence", null));
        values.add(new Column("summary2", null));
        return new Record(tableCategory, tableName, values, System.currentTimeMillis());
    }
    
    private List<Record> generateRecords(String tableCategory, String tableName, int i, int c, long time, int timeOffset) {
        List<Record> result = new ArrayList<Record>();
        List<Column> values;
        long timeTmp;
        for (int j = 0; j < c; j++) {
            values = new ArrayList<Record.Column>();
            values.add(new Column("server_name", "ESB-" + i));
            values.add(new Column("ip", "192.168.0." + (i % 256)));
            values.add(new Column("tenant", i));
            values.add(new Column("spam_index", i + 0.3454452));
            values.add(new Column("important", i % 2 == 0 ? true : false));
            values.add(new Column("sequence", i + 104050000L));
            values.add(new Column("summary", "Joey asks, how you doing?"));
            values.add(new Column("log", "Exception in Sequence[" + i + "," + j + "]"));
            if (time != -1) {
                timeTmp = time;
                time += timeOffset;
            } else {
                timeTmp = System.currentTimeMillis();
            }
            result.add(new Record(tableCategory, tableName, values, timeTmp));
        }
        return result;
    }
    
    private Set<Record> recordGroupsToSet(RecordGroup[] rgs) throws AnalyticsDataSourceException {
        Set<Record> result = new HashSet<Record>();
        for (RecordGroup rg : rgs) {
            result.addAll(rg.getRecords());
        }
        return result;
    }
    
    @AfterTest
    @Test
    public void cleanup() throws AnalyticsDataSourceException {
        this.analyticsDS.deleteTable("acme.com", "MyTable1");
        this.analyticsDS.deleteTable("acme.com", "T1");
    }
    
    private void cleanupT1() throws AnalyticsDataSourceException {
        this.analyticsDS.delete("acme.com", "T1", -1, -1);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1)).size(), 0);
    }
    
    @Test
    public void testTableCreateDeleteList() throws AnalyticsDataSourceException {
        this.analyticsDS.deleteTable("cat1", "table1");
        this.analyticsDS.deleteTable("cat1", "table2");
        this.analyticsDS.deleteTable("cat2", "tablex");        
        this.analyticsDS.createTable("cat1", "table1");        
        List<String> tables = this.analyticsDS.listTables("cat1");
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(new HashSet<String>(tables).contains("table1"));
        Record record = this.createRecord("cat1", "table2", "S1", "10.0.0.1", 1, "LOG");
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        this.analyticsDS.put(records);
        tables = this.analyticsDS.listTables("cat1");
        Assert.assertEquals(tables.size(), 2);
        Assert.assertTrue(new HashSet<String>(tables).contains("table2"));
        Assert.assertTrue(new HashSet<String>(tables).contains("table1"));
        this.analyticsDS.delete("cat1", "table2", Long.MIN_VALUE, Long.MAX_VALUE);//TODO
        tables = this.analyticsDS.listTables("cat1");
        Assert.assertEquals(tables.size(), 2);
        Assert.assertTrue(new HashSet<String>(tables).contains("table2"));
        Assert.assertTrue(new HashSet<String>(tables).contains("table1"));
        record = this.createRecord("cat2", "tablex", "S1", "10.0.0.1", 1, "LOG");
        records.clear();
        records.add(record);
        this.analyticsDS.put(records);
        tables = this.analyticsDS.listTables("cat2");
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(new HashSet<String>(tables).contains("tablex"));        
        this.analyticsDS.deleteTable("cat1", "table2");
        this.analyticsDS.deleteTable("cat1", "table1");
        this.analyticsDS.deleteTable("cat2", "tablex");
        Assert.assertEquals(this.analyticsDS.listTables("cat1").size(), 0);
        Assert.assertEquals(this.analyticsDS.listTables("cat2").size(), 0);
    }
    
    @Test
    public void testDataRecordAddRetrieve() throws AnalyticsDataSourceException {
        this.cleanupT1();
        String serverName = "ESB1";
        String ip = "10.0.0.1";
        int tenant = 44;
        String log = "Boom!";
        Record record = this.createRecord("acme.com", "T1", serverName, ip, tenant, log);
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        this.analyticsDS.put(records);
        String id = record.getId();
        List<String> ids = new ArrayList<String>();
        ids.add(id);
        RecordGroup[] rgs = this.analyticsDS.get("acme.com", "T1", null, ids);
        Assert.assertEquals(rgs.length, 1);
        List<Record> recordsIn = rgs[0].getRecords();
        Assert.assertEquals(recordsIn.size(), 1);
        Record recordIn = recordsIn.get(0);
        Assert.assertEquals(record.getId(), recordIn.getId());
        Assert.assertEquals(record.getTableName(), recordIn.getTableName());
        Assert.assertEquals(record.getTimestamp(), recordIn.getTimestamp());
        Assert.assertEquals(new HashSet<Column>(record.getNotNullValues()), 
                new HashSet<Column>(recordIn.getNotNullValues()));
        Assert.assertEquals(record, recordIn);
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieve() throws AnalyticsDataSourceException {
        this.cleanupT1();
        List<Record> records = this.generateRecords("acme.com", "T1", 1, 100, -1, -1);
        this.analyticsDS.put(records);
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1));
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
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", columns, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        columns.remove("ip");
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", columns, -1, -1, 0, -1));
        Assert.assertNotEquals(recordsIn, new HashSet<Record>(records));
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange1() throws AnalyticsDataSourceException {
        this.cleanupT1();
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = this.generateRecords("acme.com", "T1", 1, 100, time, timeOffset);
        this.analyticsDS.put(records);
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time - 10, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time + 1, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time + 1, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        records.remove(99);
        records.remove(0);
        Assert.assertEquals(new HashSet<Record>(records), new HashSet<Record>(recordsIn));
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange2() throws AnalyticsDataSourceException {
        this.cleanupT1();
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = this.generateRecords("acme.com", "T1", 1, 100, time, timeOffset);
        this.analyticsDS.put(records);
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time + 22, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(recordsIn.size(), 97);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time, time + timeOffset * 96 - 2, 0, -1));
        Assert.assertEquals(recordsIn.size(), 96);
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange3() throws AnalyticsDataSourceException {
        this.cleanupT1();
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = this.generateRecords("acme.com", "T1", 1, 100, time, timeOffset);
        this.analyticsDS.put(records);
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time - 100, time - 10, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time + timeOffset * 103, time + timeOffset * 110, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithPagination1() throws AnalyticsDataSourceException {
        this.cleanupT1();
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = this.generateRecords("acme.com", "T1", 2, 200, time, timeOffset);
        this.analyticsDS.put(records);
        Set<Record> recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 1, -1));
        Assert.assertEquals(recordsIn1.size(), 199);        
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 1, 200));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn1.size(), 200);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 1, 199));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 1, 100));
        Assert.assertEquals(recordsIn1.size(), 100);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 100, 101));
        Assert.assertEquals(recordsIn1.size(), 100);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 55, 73));
        Assert.assertEquals(recordsIn1.size(), 73);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1));
        List<Record> recordsIn2 = new ArrayList<Record>();
        for (int i = 0; i < 200; i += 20) {
            recordsIn2.addAll(this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, i, 20)));
        }
        Assert.assertEquals(recordsIn2.size(), 200);
        Assert.assertEquals(recordsIn1, new HashSet<Record>(recordsIn2));
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithPagination2() throws AnalyticsDataSourceException {
        this.cleanupT1();
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = this.generateRecords("acme.com", "T1", 2, 200, time, timeOffset);
        this.analyticsDS.put(records);
        Set<Record> recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time, time + timeOffset * 200, 1, 200));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time, time + timeOffset * 200, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 200);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time + 55, time + timeOffset * 200, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 194);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time + 55, time + timeOffset * 199, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 193);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time + 55, time + timeOffset * 198 - 5, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 192);
        List<Record> recordsIn2 = new ArrayList<Record>();
        for (int i = 0; i < 200; i += 10) {
            recordsIn2.addAll(this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, time + 55, time + timeOffset * 198 - 5, i, 10)));
        }
        Assert.assertEquals(recordsIn2.size(), 192);
        Assert.assertEquals(recordsIn1, new HashSet<Record>(recordsIn2));
        List<String> columns = new ArrayList<String>();
        columns.add("tenant");
        columns.add("ip");
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", columns, time + 55, time + timeOffset * 198 - 5, 0, 200));
        Record r1 = recordsIn1.iterator().next();
        Record r2 = recordsIn1.iterator().next();
        Assert.assertEquals(r1.getValues().size(), 2);
        Assert.assertEquals(r2.getValues().size(), 2);
        StringBuilder columnNames = new StringBuilder();
        for (Column col : r1.getValues()) {
            columnNames.append(col.getName());
        }
        StringBuilder values = new StringBuilder();
        for (Column col : r2.getValues()) {
            values.append(col.getValue());
        }
        Assert.assertTrue(columnNames.toString().contains("tenant"));
        Assert.assertTrue(columnNames.toString().contains("ip"));
        Assert.assertTrue(values.toString().equals("2192.168.0.2") || values.toString().equals("192.168.0.22"));
        this.cleanupT1();
    }
    
    @Test
    public void testDataRecordDeleteWithIds() throws AnalyticsDataSourceException {
        this.cleanupT1();
        List<Record> records = this.generateRecords("acme.com", "T1", 2, 10, -1, -1);
        this.analyticsDS.put(records);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1)).size(), 10);
        List<String> ids = new ArrayList<String>();
        ids.add(records.get(2).getId());
        ids.add(records.get(5).getId());
        this.analyticsDS.delete("acme.com", "T1", ids);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1)).size(), 8);
        ids.clear();
        ids.add(records.get(0).getId());
        this.analyticsDS.delete("acme.com", "T1", ids);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1)).size(), 7);
        this.analyticsDS.delete("acme.com", "T1", ids);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1)).size(), 7);
        this.analyticsDS.delete("acme.com", "T1", new ArrayList<String>());
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1)).size(), 7);
        this.cleanupT1();
    }
    
    @Test
    public void testDataRecordDeleteWithTimestamps() throws AnalyticsDataSourceException {
        this.cleanupT1();
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = this.generateRecords("acme.com", "T1", 1, 100, time, timeOffset);
        this.analyticsDS.put(records);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1)).size(), 100);
        this.analyticsDS.delete("acme.com", "T1", time - 100, time + 12);
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        records.remove(0);
        records.remove(0);
        Assert.assertEquals(new HashSet<Record>(records), recordsIn);
        this.analyticsDS.delete("acme.com", "T1", time + timeOffset * 97, time + timeOffset * 101);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1));
        records.remove(97);
        records.remove(96);
        records.remove(95);
        Assert.assertEquals(new HashSet<Record>(records), recordsIn);
        this.analyticsDS.delete("acme.com", "T1", time + timeOffset * 5 - 2, time + timeOffset * 7 + 4);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1));
        records.remove(5);
        records.remove(4);
        records.remove(3);
        Assert.assertEquals(new HashSet<Record>(records), recordsIn);
        this.cleanupT1();
    }
    
    @Test
    public void testDataRecordAddReadPerformance() throws AnalyticsDataSourceException {
        System.out.println("\n************** START RECORD PERF TEST [" + this.getImplementationName() + "] **************");
        this.cleanupT1();
        long hash1 = 0;
        List<Record> records;
        int n = 50, batch = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            records = this.generateRecords("acme.com", "T1", i, batch, -1, -1);
            this.analyticsDS.put(records);
            for (Record record : records) {
                hash1 += record.hashCode();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("acme.com", "T1", null, -1, -1, 0, -1));
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
    
    @Test
    public void testFSDirectoryOperations() throws AnalyticsDataSourceException {
        this.fileSystem.delete("/d1/d2/d3");
        this.fileSystem.mkdir("/d1/d2/d3");
        this.fileSystem.mkdir("/d1/d2/d5");
        Assert.assertTrue(this.fileSystem.exists("/d1/d2/d3"));
        Assert.assertTrue(this.fileSystem.exists("/d1/d2/d5"));
        Assert.assertFalse(this.fileSystem.exists("/d1/d2/d4"));
        this.fileSystem.delete("/d1/d2/d3");
        this.fileSystem.delete("/d1/d2/d5");
        Assert.assertFalse(this.fileSystem.exists("/d1/d2/d3"));
        Assert.assertFalse(this.fileSystem.exists("/d1/d2/d5"));
    }
    
    private byte[] generateData(int size) {
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
    
    private boolean checkDataEquals(byte[] lhs, byte[] rhs) {
        for (int i = 0; i < lhs.length; i++) {
            if (lhs[i] != rhs[i]) {
                return false;
            }
        }
        return true;
    }
    
    @Test
    public void testFSFileIOOperations() throws AnalyticsDataSourceException {
        this.fileSystem.delete("/d1");
        DataOutput out = this.fileSystem.createOutput("/d1/d2/d3/f1");
        byte[] data = this.generateData(1024 * 1024 + 7);
        long start = System.currentTimeMillis();
        out.write(data, 0, data.length);
        out.flush();
        out.close();
        long end = System.currentTimeMillis();
        System.out.println("File data (1 MB) written in: " + (end - start) + " ms.");
        Assert.assertEquals(this.fileSystem.length("/d1/d2/d3/f1"), data.length);
        DataInput in = this.fileSystem.createInput("/d1/d2/d3/f1");
        byte[] dataIn = new byte[data.length];
        start = System.currentTimeMillis();
        int len = in.read(dataIn, 0, dataIn.length);
        in.close();
        end = System.currentTimeMillis();
        System.out.println("File data (1 MB) read in: " + (end - start) + " ms.");
        Assert.assertEquals(len, data.length);
        Assert.assertTrue(this.checkDataEquals(data, dataIn));
        this.fileSystem.delete("/d1/d2/d3/f1");
        Assert.assertFalse(this.fileSystem.exists("/d1/d2/d3/f1"));
        this.fileSystem.delete("/d1/d2/d3");
        Assert.assertFalse(this.fileSystem.exists("/d1/d2/d3"));
        this.fileSystem.delete("/d1");
    }
    
    @Test
    public void testFSFileIOOperations2() throws AnalyticsDataSourceException, IOException {
        this.fileSystem.delete("/d1");
        DataOutput out = this.fileSystem.createOutput("/d1/d2/d3");
        byte[] data = this.generateData(1350);
        for (int i = 0; i < 100; i++) {
            out.write(data, 0, data.length);
        }
        out.close();
        DataInput in = this.fileSystem.createInput("/d1/d2/d3");
        byte[] buff = new byte[500];
        int j;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        while ((j = in.read(buff, 0, buff.length)) > 0) {
            byteOut.write(buff, 0, j);
        }
        byteOut.close();
        in.close();
        Assert.assertTrue(this.checkDataEquals(data, byteOut.toByteArray()));
        /* overwriting earlier data again, with different buffer values */
        out = this.fileSystem.createOutput("/d1/d2/d3");
        data = this.generateData(135);
        for (int i = 0; i < 1000; i++) {
            out.write(data, 0, data.length);
        }
        out.close();
        in = this.fileSystem.createInput("/d1/d2/d3");
        buff = new byte[1000];
        byteOut = new ByteArrayOutputStream();
        while ((j = in.read(buff, 0, buff.length)) > 0) {
            byteOut.write(buff, 0, j);
        }
        byteOut.close();
        in.close();
        Assert.assertTrue(this.checkDataEquals(data, byteOut.toByteArray()));
        
        this.fileSystem.delete("/d1");
        Assert.assertFalse(this.fileSystem.exists("/d1/d2/d3"));
    }
    
    @Test
    public void testFSFileCopy() throws AnalyticsDataSourceException {
        this.fileSystem.delete("/d1");
        this.fileSystem.delete("/d2");
        byte[] data = this.generateData(1025);
        DataOutput out = this.fileSystem.createOutput("/d1/fx");
        out.write(data, 0, data.length);
        out.close();
        DataInput inx = this.fileSystem.createInput("/d1/fx");
        byte[] d = new byte[data.length];
        inx.read(d, 0, d.length);
        inx.close();
        Assert.assertTrue(this.checkDataEquals(data, d));
        this.fileSystem.copy("/d1/fx", "/d2/fx");
        Assert.assertTrue(this.fileSystem.exists("/d2/fx"));
        Assert.assertEquals(this.fileSystem.length("/d2/fx"), data.length);
        DataInput in = this.fileSystem.createInput("/d2/fx");
        byte[] dataIn = new byte[data.length];
        in.read(dataIn, 0, dataIn.length);
        in.close();
        Assert.assertTrue(this.checkDataEquals(data, dataIn));
        this.fileSystem.delete("/d1");
        this.fileSystem.delete("/d2");
    }
    
    @Test
    public void testFSReadSeekPosition() throws AnalyticsDataSourceException {
        this.fileSystem.delete("/d1");
        byte[] data = this.generateData(1024 * 45);
        byte[] dataSubset = new byte[5];
        dataSubset[0] = data[34021];
        dataSubset[1] = data[34022];
        dataSubset[2] = data[34023];
        dataSubset[3] = data[34024];
        dataSubset[4] = data[34025];
        DataOutput out = this.fileSystem.createOutput("/d1/fx");
        out.write(data, 0, data.length);
        out.close();
        DataInput in = this.fileSystem.createInput("/d1/fx");
        in.seek(34021);
        byte[] dataSubsetIn = new byte[5];
        in.read(dataSubsetIn, 0, dataSubsetIn.length);
        Assert.assertTrue(this.checkDataEquals(dataSubset, dataSubsetIn));
        Assert.assertEquals(in.getPosition(), 34026);
        this.fileSystem.delete("/d1");
    }
    
    @Test
    public void testFSSetLengthRead() throws AnalyticsDataSourceException {
        this.fileSystem.delete("/d_1");
        byte[] data = this.generateData(100);
        DataOutput out = this.fileSystem.createOutput("/d_1/d_2/file1");
        out.write(data, 0, data.length);
        out.flush();
        Assert.assertEquals(this.fileSystem.length("/d_1/d_2/file1"), data.length);
        out.setLength(2000);
        Assert.assertEquals(this.fileSystem.length("/d_1/d_2/file1"), 2000);
        out.close();
        byte[] dataIn = new byte[3000];
        DataInput in = this.fileSystem.createInput("/d_1/d_2/file1");
        int len = in.read(dataIn, 0, dataIn.length);
        in.close();
        Assert.assertEquals(len, 2000);
        out = this.fileSystem.createOutput("/d_1/d_2/file1");
        out.setLength(10);
        out.close();
        Assert.assertEquals(this.fileSystem.length("/d_1/d_2/file1"), 10);
        in = this.fileSystem.createInput("/d_1/d_2/file1");
        len = in.read(dataIn, 0, dataIn.length);
        in.close();
        Assert.assertEquals(len, 10);
        this.fileSystem.delete("/d_1");
    }
    
    @Test
    public void testFSWriteSeekPositionLength() throws AnalyticsDataSourceException {
        this.fileSystem.delete("/d1");
        byte[] data = this.generateData(1024 * 12 + 24);
        byte[] data2 = this.generateData(1024 * 3 + 10);
        DataOutput out = this.fileSystem.createOutput("/d1/d2/fy");
        out.write(data, 0, data.length);
        out.flush();
        out.seek(1024 * 5 + 20);
        out.write(data2, 0, data2.length);
        out.close();
        DataInput in = this.fileSystem.createInput("/d1/d2/fy");
        byte[] dataIn = new byte[data.length];
        in.read(dataIn, 0, dataIn.length);
        System.arraycopy(data2, 0, data, 1024 * 5 + 20, data2.length);
        Assert.assertTrue(this.checkDataEquals(data, dataIn));
        this.fileSystem.delete("/d1");
    }
    
    @Test
    public void testFSPerfTest() throws AnalyticsDataSourceException {
        System.out.println("\n************** START FS PERF TEST [" + this.getImplementationName() + "] **************");
        this.fileSystem.delete("/mydir");
        byte[] data = this.generateData(2048);
        DataOutput out;
        long start = System.currentTimeMillis();
        int count = 1000;
        for (int i = 0; i < count; i++) {
            out = this.fileSystem.createOutput("/mydir/perf/file" + i);
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
            in = this.fileSystem.createInput("/mydir/perf/file" + i);
            len = in.read(dataIn, 0, dataIn.length);
            in.close();
            Assert.assertEquals(len, dataIn.length);
        }
        end = System.currentTimeMillis();
        System.out.println("* " + count + " 2K files read in: " + (end - start) + " ms. " + (count / (double) (end - start) * 1000.0) + " FPS.");
        this.fileSystem.delete("/mydir");
        System.out.println("\n************** END FS PERF TEST [" + this.getImplementationName() + "] **************");
    }
    
}
