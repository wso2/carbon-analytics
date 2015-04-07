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

import org.apache.commons.collections.IteratorUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import javax.naming.Context;
import java.util.*;

/**
 * This class contains tests related to {@link AnalyticsRecordStore}.
 */
public class AnalyticsRecordStoreTest {

    private AnalyticsRecordStore analyticsRS;

    private String implementationName;

    public AnalyticsRecordStoreTest() {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InMemoryICFactory.class.getName());
    }

    protected void init(String implementationName, AnalyticsRecordStore analyticsRS) throws AnalyticsException {
        this.implementationName = implementationName;
        this.analyticsRS = analyticsRS;
        this.cleanup();
    }

    protected void cleanup() throws AnalyticsException {
        this.analyticsRS.deleteTable(7, "MYTABLE1");
        this.analyticsRS.deleteTable(7, "T1");
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
        return new Record(GenericUtils.generateRecordID(), tenantId, tableName, values, System.currentTimeMillis());
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
            result.add(new Record(GenericUtils.generateRecordID(), tenantId, tableName, values, timeTmp));
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

    private void cleanupT1() throws AnalyticsException {
        this.analyticsRS.deleteTable(7, "T1");
        this.analyticsRS.deleteTable(15, "T1");
        Assert.assertFalse(this.analyticsRS.tableExists(7, "T1"));
        Assert.assertFalse(this.analyticsRS.tableExists(15, "T1"));
    }

    @Test
    public void testTableCreateDeleteList() throws AnalyticsException {
        this.analyticsRS.deleteTable(250035, "TABLE1");
        this.analyticsRS.deleteTable(250035, "TABLE2");
        this.analyticsRS.deleteTable(8830, "TABLEX");
        this.analyticsRS.createTable(250035, "TABLE1");
        List<String> tables = this.analyticsRS.listTables(250035);
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(new HashSet<String>(tables).contains("TABLE1"));
        Assert.assertTrue(this.analyticsRS.tableExists(250035, "table1"));
        Assert.assertTrue(this.analyticsRS.tableExists(250035, "TABLE1"));
        /* this should not throw an exception */
        this.analyticsRS.createTable(250035, "Table1");
        Record record = this.createRecord(250035, "TABLE2", "S1", "10.0.0.1", 1, "LOG");
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        this.analyticsRS.deleteTable(250035, "TABLE2");
        this.analyticsRS.deleteTable(250035, "TABLE1");
        this.analyticsRS.deleteTable(8830, "TABLEX");
        Assert.assertEquals(this.analyticsRS.listTables(250035).size(), 0);
        Assert.assertEquals(this.analyticsRS.listTables(8830).size(), 0);
    }

    @Test
    public void testTableSetGetSchema() throws AnalyticsException {
        int tenantId = 105;
        String tableName = "T1";
        this.analyticsRS.deleteTable(tenantId, tableName);
        this.analyticsRS.createTable(tenantId, tableName);
        AnalyticsSchema schema = this.analyticsRS.getTableSchema(tenantId, tableName);
        /* for an empty schema, still the schema object must be returned */
        Assert.assertNotNull(schema);
        Map<String, AnalyticsSchema.ColumnType> columns = new HashMap<String, AnalyticsSchema.ColumnType>();
        columns.put("name", AnalyticsSchema.ColumnType.STRING);
        columns.put("age", AnalyticsSchema.ColumnType.INTEGER);
        columns.put("weight", AnalyticsSchema.ColumnType.LONG);
        columns.put("something1", AnalyticsSchema.ColumnType.FLOAT);
        columns.put("something2", AnalyticsSchema.ColumnType.DOUBLE);
        columns.put("something3", AnalyticsSchema.ColumnType.BOOLEAN);
        List<String> primaryKeys = new ArrayList<String>();
        primaryKeys.add("name");
        primaryKeys.add("age");
        schema = new AnalyticsSchema(columns, primaryKeys);
        this.analyticsRS.setTableSchema(tenantId, tableName, schema);
        AnalyticsSchema schemaIn = this.analyticsRS.getTableSchema(tenantId, tableName);
        Assert.assertEquals(schema, schemaIn);
        this.analyticsRS.deleteTable(tenantId, tableName);
    }

    @Test (expectedExceptions = AnalyticsTableNotAvailableException.class)
    public void testTableGetNoSchema() throws AnalyticsTableNotAvailableException, AnalyticsException {
        this.analyticsRS.deleteTable(105, "T1");
        this.analyticsRS.getTableSchema(105, "T1");
    }

    @Test
    public void testTableCreateDeleteListNegativeTenantIds() throws AnalyticsException {
        this.analyticsRS.deleteTable(-1234, "TABLE1");
        this.analyticsRS.deleteTable(-1234, "TABLE2");
        this.analyticsRS.createTable(-1234, "TABLE1");
        List<String> tables = this.analyticsRS.listTables(-1234);
        Assert.assertEquals(tables.size(), 1);
        Assert.assertTrue(new HashSet<String>(tables).contains("TABLE1"));
        Assert.assertTrue(this.analyticsRS.tableExists(-1234, "table1"));
        Assert.assertTrue(this.analyticsRS.tableExists(-1234, "TABLE1"));
        /* this should not throw an exception */
        this.analyticsRS.createTable(-1234, "Table1");
        Record record = this.createRecord(-1234, "TABLE2", "S1", "10.0.0.1", 1, "LOG");
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        this.analyticsRS.deleteTable(-1234, "TABLE2");
        this.analyticsRS.deleteTable(-1234, "TABLE1");
        Assert.assertEquals(this.analyticsRS.listTables(-1234).size(), 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDataRecordAddRetrieve() throws AnalyticsException {
        this.cleanupT1();
        String serverName = "ESB1";
        String ip = "10.0.0.1";
        int tenant = 44;
        String log = "Boom!";
        this.analyticsRS.createTable(7, "T1");
        Record record = this.createRecord(7, "T1", serverName, ip, tenant, log);
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        this.analyticsRS.put(records);
        String id = record.getId();
        List<String> ids = new ArrayList<String>();
        ids.add(id);
        RecordGroup[] rgs = this.analyticsRS.get(7, "T1", 3, null, ids);
        Assert.assertEquals(rgs.length, 1);
        List<Record> recordsIn = IteratorUtils.toList(this.analyticsRS.readRecords(rgs[0]));
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
    public void testTableMT() throws AnalyticsException {
        int t1 = 10;
        int t2 = 11;
        String tableName = "Table";
        this.analyticsRS.deleteTable(t1, tableName);
        this.analyticsRS.deleteTable(t2, tableName);
        this.analyticsRS.createTable(t1, tableName);
        this.analyticsRS.createTable(t2, tableName);
        List<Record> records1 = generateRecords(t1, tableName, 1, 30, -1, -1);
        List<Record> records2 = generateRecords(t2, tableName, 1, 40, -1, -1);
        this.analyticsRS.put(records1);
        this.analyticsRS.put(records2);
        List<Record> recordsIn1 = GenericUtils.listRecords(
                this.analyticsRS, this.analyticsRS.get(t1, tableName, 1, null,
                Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        List<Record> recordsIn2 = GenericUtils.listRecords(
                this.analyticsRS, this.analyticsRS.get(t2, tableName, 1, null,
                Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(records1.size(), recordsIn1.size());
        Assert.assertEquals(records2.size(), recordsIn2.size());
        Assert.assertEquals(new HashSet<Record>(records1), new HashSet<Record>(recordsIn1));
        Assert.assertEquals(new HashSet<Record>(records2), new HashSet<Record>(recordsIn2));
        this.analyticsRS.deleteTable(t1, tableName);
        this.analyticsRS.deleteTable(t2, tableName);
    }

    @Test
    public void testMultipleDataRecordAddRetieve() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        List<Record> records = generateRecords(7, "T1", 1, 100, -1, -1);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<Record>(recordsIn), new HashSet<Record>(records));
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
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, columns, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<Record>(recordsIn), new HashSet<Record>(records));
        columns.remove("ip");
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 4, columns, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertNotEquals(new HashSet<Record>(recordsIn), new HashSet<Record>(records));
        this.cleanupT1();
    }

    @Test
    public void testMultipleDataRecordAddRetieveUpdate() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(15, "T1");
        List<Record> records = generateRecords(15, "T1", 1, 50, -1, -1);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(15, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<Record>(recordsIn), new HashSet<Record>(records));
        records = this.generateRecordsForUpdate(records);
        this.analyticsRS.put(records);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(15, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<Record>(recordsIn), new HashSet<Record>(records));
        this.cleanupT1();
    }

    @Test
    public void testDataRecordCount() throws AnalyticsException {
        if (!this.analyticsRS.isPaginationSupported()) {
            return;
        }
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        int count = (int) (2000 * Math.random()) + 1;
        List<Record> records = generateRecords(7, "T1", 1, count, -15000, 333);
        this.analyticsRS.put(records);
        Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", Long.MIN_VALUE, Long.MAX_VALUE), count);
        Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", -15000, -14999), 1);
        this.analyticsRS.delete(7, "T1", Long.MIN_VALUE, Long.MAX_VALUE);
        Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", Long.MIN_VALUE, Long.MAX_VALUE), 0);
        this.cleanupT1();
    }

    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange1() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time - 10, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(new HashSet<Record>(recordsIn), new HashSet<Record>(records));
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, time, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(new HashSet<Record>(recordsIn), new HashSet<Record>(records));
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, time, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        if(this.analyticsRS.isPaginationSupported()){
            Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", time, time + timeOffset * 99), 99);
        }
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time + 1, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        if(this.analyticsRS.isPaginationSupported()) {
            Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", time + 1, time + timeOffset * 99 + 1), 99);
        }
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 5, null, time + 1, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        if(this.analyticsRS.isPaginationSupported()) {
            Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", time + 1, time + timeOffset * 99), 98);
        }
        records.remove(99);
        records.remove(0);
        Assert.assertEquals(new HashSet<Record>(records), new HashSet<Record>(recordsIn));
        this.cleanupT1();
    }

    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange2() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time + 22, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(recordsIn.size(), 97);
        if(this.analyticsRS.isPaginationSupported()) {
            Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", time + 22, time + timeOffset * 100), 97);
        }
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, time, time + timeOffset * 96 - 2, 0, -1));
        Assert.assertEquals(recordsIn.size(), 96);
        if(this.analyticsRS.isPaginationSupported()) {
            Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", time, time + timeOffset * 96 - 2), 96);
        }
        this.cleanupT1();
    }

    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange3() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, time - 100, time - 10, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        if(this.analyticsRS.isPaginationSupported()) {
            Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", time - 100, time - 10), 0);
        }
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time + timeOffset * 103, time + timeOffset * 110, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        if(this.analyticsRS.isPaginationSupported()) {
            Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", time + timeOffset * 103, time + timeOffset * 110), 0);
        }
        this.cleanupT1();
    }

    @Test
    public void testMultipleDataRecordAddRetieveWithPagination1() throws AnalyticsException {
        if (!this.analyticsRS.isPaginationSupported()) {
            return;
        }
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 2, 200, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn1 = GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 1, -1));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 1, 200));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn1.size(), 200);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 1, 199));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 1, 100));
        Assert.assertEquals(recordsIn1.size(), 100);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 4, null, Long.MIN_VALUE, Long.MAX_VALUE, 100, 101));
        Assert.assertEquals(recordsIn1.size(), 100);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 55, 73));
        Assert.assertEquals(recordsIn1.size(), 73);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        List<Record> recordsIn2 = new ArrayList<Record>();
        for (int i = 0; i < 200; i += 20) {
            recordsIn2.addAll(GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, i, 20)));
        }
        Assert.assertEquals(recordsIn2.size(), 200);
        Assert.assertEquals(new HashSet<Record>(recordsIn1), new HashSet<Record>(recordsIn2));
        this.cleanupT1();
    }

    @Test
    public void testMultipleDataRecordAddRetieveWithPagination2() throws AnalyticsException {
        if (!this.analyticsRS.isPaginationSupported()) {
            return;
        }
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 2, 200, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, time, time + timeOffset * 200, 1, 200));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time, time + timeOffset * 200, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 200);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time + 55, time + timeOffset * 200, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 194);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time + 55, time + timeOffset * 199, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 193);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, time + 55, time + timeOffset * 198 - 5, 0, 200));
        Assert.assertEquals(recordsIn1.size(), 192);
        List<Record> recordsIn2 = new ArrayList<Record>();
        for (int i = 0; i < 200; i += 10) {
            recordsIn2.addAll(GenericUtils.listRecords(this.analyticsRS,
                    this.analyticsRS.get(7, "T1", 2, null, time + 55, time + timeOffset * 198 - 5, i, 10)));
        }
        Assert.assertEquals(recordsIn2.size(), 192);
        Assert.assertEquals(new HashSet<Record>(recordsIn1), new HashSet<Record>(recordsIn2));
        List<String> columns = new ArrayList<String>();
        columns.add("tenant");
        columns.add("ip");
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, columns, time + 55, time + timeOffset * 198 - 5, 0, 200));
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
        this.analyticsRS.createTable(7, "T1");
        List<Record> records = generateRecords(7, "T1", 2, 10, -1, -1);
        this.analyticsRS.put(records);
        Assert.assertEquals(GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 10);
        List<String> ids = new ArrayList<String>();
        ids.add(records.get(2).getId());
        ids.add(records.get(5).getId());
        this.analyticsRS.delete(7, "T1", ids);
        Assert.assertEquals(GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 8);
        ids.clear();
        ids.add(records.get(0).getId());
        this.analyticsRS.delete(7, "T1", ids);
        Assert.assertEquals(GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 7);
        this.analyticsRS.delete(7, "T1", ids);
        Assert.assertEquals(GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 7);
        this.analyticsRS.delete(7, "T1", new ArrayList<String>());
        Assert.assertEquals(GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 7);
        this.cleanupT1();
    }

    @Test
    public void testDataRecordDeleteWithTimestamps() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        Assert.assertEquals(GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 100);
        this.analyticsRS.delete(7, "T1", time - 100, time + 12);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        records.remove(0);
        records.remove(0);
        Assert.assertEquals(new HashSet<Record>(records), new HashSet<Record>(recordsIn));
        this.analyticsRS.delete(7, "T1", time + timeOffset * 97, time + timeOffset * 101);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        records.remove(97);
        records.remove(96);
        records.remove(95);
        Assert.assertEquals(recordsIn.size(), records.size());
        Assert.assertEquals(new HashSet<Record>(records), new HashSet<Record>(recordsIn));
        this.analyticsRS.delete(7, "T1", time + timeOffset * 5 - 2, time + timeOffset * 7 + 4);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        records.remove(5);
        records.remove(4);
        records.remove(3);
        Assert.assertEquals(new HashSet<Record>(records), new HashSet<Record>(recordsIn));
        this.cleanupT1();
    }

    @Test
    public void testDataRecordAddReadPerformance() throws AnalyticsException {
        System.out.println("\n************** START RECORD PERF TEST [" + this.getImplementationName() + "] **************");
        this.cleanupT1();
        
        /* warm-up */
        this.analyticsRS.createTable(7, "T1");
        List<Record> records;
        for (int i = 0; i < 10; i++) {
            records = generateRecords(7, "T1", i, 100, -1, -1);
            this.analyticsRS.put(records);
        }
        this.cleanupT1();

        this.analyticsRS.createTable(7, "T1");
        long hash1 = 0;
        int n = 50, batch = 200;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            records = generateRecords(7, "T1", i, batch, -1, -1);
            this.analyticsRS.put(records);
            for (Record record : records) {
                hash1 += record.hashCode();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("* Records: " + (n * batch));
        System.out.println("* Write Time: " + (end - start) + " ms.");
        System.out.println("* Write Throughput (TPS): " + (n * batch) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
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

}
