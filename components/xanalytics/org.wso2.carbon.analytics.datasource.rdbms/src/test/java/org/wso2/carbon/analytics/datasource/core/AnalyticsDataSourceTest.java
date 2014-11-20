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

/**
 * This class contains tests related to analytics data sources.
 */
public class AnalyticsDataSourceTest {

    protected AnalyticsDataSource analyticsDS;
    
    private String implementationName;
    
    public AnalyticsDataSourceTest() {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InMemoryICFactory.class.getName());
    }
    
    public void init(String implementationName, AnalyticsDataSource analyticsDS) throws AnalyticsDataSourceException {
        this.implementationName = implementationName;
        this.analyticsDS = analyticsDS;
        this.analyticsDS.purgeTable("MyTable1");
        this.analyticsDS.purgeTable("T1");
    }
    
    public String getImplementationName() {
        return implementationName;
    }

    private Record createRecord(String tableName, String serverName, String ip, int tenant, String log) {
        List<Column> values = new ArrayList<Record.Column>();
        values.add(new Column("server_name", serverName));
        values.add(new Column("ip", ip));
        values.add(new Column("tenant", tenant));
        values.add(new Column("log", log));
        values.add(new Column("sequence", null));
        values.add(new Column("summary2", null));
        return new Record(tableName, values, System.currentTimeMillis());
    }
    
    private List<Record> generateRecords(String tableName, int i, int c, long time, int timeOffset) {
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
            result.add(new Record(tableName, values, timeTmp));
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
        this.analyticsDS.purgeTable("MyTable1");
        this.analyticsDS.purgeTable("T1");
    }
    
    private void cleanupT1() throws AnalyticsDataSourceException {
        this.analyticsDS.delete("T1", -1, -1);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1)).size(), 0);
    }
    
    @Test
    public void testDataRecordAddRetrieve() throws AnalyticsDataSourceException {
        this.cleanupT1();
        String serverName = "ESB1";
        String ip = "10.0.0.1";
        int tenant = 44;
        String log = "Boom!";
        Record record = this.createRecord("T1", serverName, ip, tenant, log);
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        this.analyticsDS.put(records);
        String id = record.getId();
        List<String> ids = new ArrayList<String>();
        ids.add(id);
        RecordGroup[] rgs = this.analyticsDS.get("T1", null, ids);
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
        List<Record> records = this.generateRecords("T1", 1, 100, -1, -1);
        this.analyticsDS.put(records);
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1));
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
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", columns, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        columns.remove("ip");
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", columns, -1, -1, 0, -1));
        Assert.assertNotEquals(recordsIn, new HashSet<Record>(records));
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange1() throws AnalyticsDataSourceException {
        this.cleanupT1();
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = this.generateRecords("T1", 1, 100, time, timeOffset);
        this.analyticsDS.put(records);
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time - 10, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn, new HashSet<Record>(records));
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time + 1, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time + 1, time + timeOffset * 99, 0, -1));
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
        List<Record> records = this.generateRecords("T1", 1, 100, time, timeOffset);
        this.analyticsDS.put(records);
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time + 22, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(recordsIn.size(), 97);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time, time + timeOffset * 96 - 2, 0, -1));
        Assert.assertEquals(recordsIn.size(), 96);
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange3() throws AnalyticsDataSourceException {
        this.cleanupT1();
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = this.generateRecords("T1", 1, 100, time, timeOffset);
        this.analyticsDS.put(records);
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time - 100, time - 10, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time + timeOffset * 103, time + timeOffset * 110, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        this.cleanupT1();
    }
    
    @Test
    public void testMultipleDataRecordAddRetieveWithPagination1() throws AnalyticsDataSourceException {
        this.cleanupT1();
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = this.generateRecords("T1", 2, 200, time, timeOffset);
        this.analyticsDS.put(records);
        Set<Record> recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 1, -1));
        Assert.assertEquals(recordsIn1.size(), 199);        
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 1, 200));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn1.size(), 200);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 1, 199));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 1, 100));
        Assert.assertEquals(recordsIn1.size(), 100);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 100, 101));
        Assert.assertEquals(recordsIn1.size(), 100);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 55, 73));
        Assert.assertEquals(recordsIn1.size(), 73);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1));
        List<Record> recordsIn2 = new ArrayList<Record>();
        for (int i = 0; i < 200; i += 20) {
            recordsIn2.addAll(this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, i, 20)));
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
        List<Record> records = this.generateRecords("T1", 2, 200, time, timeOffset);
        this.analyticsDS.put(records);
        Set<Record> recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time, time + timeOffset * 200, 1, 200));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time, time + timeOffset * 200, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 200);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time + 55, time + timeOffset * 200, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 194);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time + 55, time + timeOffset * 199, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 193);
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", null, time + 55, time + timeOffset * 198 - 5, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 192);
        List<Record> recordsIn2 = new ArrayList<Record>();
        for (int i = 0; i < 200; i += 10) {
            recordsIn2.addAll(this.recordGroupsToSet(this.analyticsDS.get("T1", null, time + 55, time + timeOffset * 198 - 5, i, 10)));
        }
        Assert.assertEquals(recordsIn2.size(), 192);
        Assert.assertEquals(recordsIn1, new HashSet<Record>(recordsIn2));
        List<String> columns = new ArrayList<String>();
        columns.add("tenant");
        columns.add("ip");
        recordsIn1 = this.recordGroupsToSet(this.analyticsDS.get("T1", columns, time + 55, time + timeOffset * 198 - 5, 0, 200));
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
        List<Record> records = this.generateRecords("T1", 2, 10, -1, -1);
        this.analyticsDS.put(records);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1)).size(), 10);
        List<String> ids = new ArrayList<String>();
        ids.add(records.get(2).getId());
        ids.add(records.get(5).getId());
        this.analyticsDS.delete("T1", ids);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1)).size(), 8);
        ids.clear();
        ids.add(records.get(0).getId());
        this.analyticsDS.delete("T1", ids);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1)).size(), 7);
        this.analyticsDS.delete("T1", ids);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1)).size(), 7);
        this.analyticsDS.delete("T1", new ArrayList<String>());
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1)).size(), 7);
        this.cleanupT1();
    }
    
    @Test
    public void testDataRecordDeleteWithTimestamps() throws AnalyticsDataSourceException {
        this.cleanupT1();
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = this.generateRecords("T1", 1, 100, time, timeOffset);
        this.analyticsDS.put(records);
        Assert.assertEquals(this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1)).size(), 100);
        this.analyticsDS.delete("T1", time - 100, time + 12);
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        records.remove(0);
        records.remove(0);
        Assert.assertEquals(new HashSet<Record>(records), recordsIn);
        this.analyticsDS.delete("T1", time + timeOffset * 97, time + timeOffset * 101);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1));
        records.remove(97);
        records.remove(96);
        records.remove(95);
        Assert.assertEquals(new HashSet<Record>(records), recordsIn);
        this.analyticsDS.delete("T1", time + timeOffset * 5 - 2, time + timeOffset * 7 + 4);
        recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1));
        records.remove(5);
        records.remove(4);
        records.remove(3);
        Assert.assertEquals(new HashSet<Record>(records), recordsIn);
        this.cleanupT1();
    }
    
    @Test
    public void testDataRecordAddReadPerformance() throws AnalyticsDataSourceException {
        System.out.println("\n************** START PERF TEST [" + this.getImplementationName() + "] **************");
        this.cleanupT1();
        long hash1 = 0;
        List<Record> records;
        int n = 50, batch = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            records = this.generateRecords("T1", i, batch, -1, -1);
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
        Set<Record> recordsIn = this.recordGroupsToSet(this.analyticsDS.get("T1", null, -1, -1, 0, -1));
        Assert.assertEquals(recordsIn.size(), (n * batch));
        end = System.currentTimeMillis();
        long hash2 = 0;
        for (Record record : recordsIn) {
            hash2 += record.hashCode();
        }
        Assert.assertEquals(hash1, hash2);
        System.out.println("* Read Time: " + (end - start) + " ms.");
        System.out.println("* Read Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        System.out.println("************** END PERF TEST [" + this.getImplementationName() + "] **************\n");
        this.cleanupT1();
    }
    
}
