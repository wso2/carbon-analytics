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
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.util.*;

/**
 * This class contains tests related to {@link AnalyticsRecordStore}.
 */
public class AnalyticsRecordStoreTest {

    private AnalyticsRecordStore analyticsRS;

    private String implementationName;

    private Random randomNumberGenerator;

    protected void init(String implementationName, AnalyticsRecordStore analyticsRS) throws AnalyticsException {
        this.implementationName = implementationName;
        this.analyticsRS = analyticsRS;
        randomNumberGenerator = new Random();
    }

    protected void cleanup() throws AnalyticsException {
        this.analyticsRS.deleteTable(7, "MYTABLE1");
        this.analyticsRS.deleteTable(7, "T1");
        this.analyticsRS.destroy();
    }

    public String getImplementationName() {
        return implementationName;
    }

    private Record createRecord(int tenantId, String tableName, String serverName, String ip, int tenant, String log) {
        Map<String, Object> values = new HashMap<>();
        values.put("server_name", serverName);
        values.put("ip", ip);
        values.put("tenant", tenant);
        values.put("log", log);
        values.put("sequence", null);
        values.put("summary2", null);
        return new Record(GenericUtils.generateRecordID(), tenantId, tableName, values, System.currentTimeMillis());
    }

    public static List<Record> generateRecords(int tenantId, String tableName, int i, int c, long time,
            int timeOffset) {
        return generateRecords(tenantId, tableName, i, c, time, timeOffset, true);
    }

    public static List<Record> generateRecordsWithFacets(int tenantId, String tableName, int i, int c, long time,
            int timeOffset) {
        return generateRecordsWithFacets(tenantId, tableName, i, c, time, timeOffset, true);
    }

    public static List<Record> generateRecords(int tenantId, String tableName, int i, int c, long time, int timeOffset,
            boolean generateRecordIds) {
        List<Record> result = new ArrayList<>();
        Map<String, Object> values;
        long timeTmp;
        for (int j = 0; j < c; j++) {
            values = new HashMap<>();
            values.put("server_name", "ESB-" + i);
            values.put("ip", "192.168.0." + (i % 256));
            values.put("tenant", i);
            values.put("spam_index", i + 0.3454452);
            values.put("important", i % 2 == 0);
            values.put("sequence", i + 104050000L);
            values.put("summary", "Joey asks, how you doing?");
            values.put("log", "Exception in Sequence[" + i + "," + j + "]");
            if (time != -1) {
                timeTmp = time;
                time += timeOffset;
            } else {
                timeTmp = System.currentTimeMillis();
            }
            result.add(
                    new Record(generateRecordIds ? GenericUtils.generateRecordID() : null, tenantId, tableName, values,
                            timeTmp));
        }
        return result;
    }

    public static List<Record> generateRecordsWithFacets(int tenantId, String tableName, int i, int c, long time,
            int timeOffset, boolean generateRecordIds) {
        List<Record> result = new ArrayList<>();
        Map<String, Object> values;
        String aFacet;
        long timeTmp;
        for (int j = 0; j < c; j++) {
            values = new HashMap<>();
            aFacet = "SomeLocation,SomeInnerLocation,AVillage";
            values.put("server_name", "ESB-" + i);
            values.put("ip", "192.168.0." + (i % 256));
            values.put("tenant", i);
            values.put("spam_index", i + 0.3454452);
            values.put("important", i % 2 == 0);
            values.put("sequence", i + 104050000L);
            values.put("summary", "Joey asks, how you doing?");
            values.put("location", aFacet);
            values.put("log", "Exception in Sequence[" + i + "," + j + "]");
            if (time != -1) {
                timeTmp = time;
                time += timeOffset;
            } else {
                timeTmp = System.currentTimeMillis();
            }
            result.add(
                    new Record(generateRecordIds ? GenericUtils.generateRecordID() : null, tenantId, tableName, values,
                            timeTmp));
        }
        return result;
    }

    private List<Record> generateRecordsForUpdate(List<Record> recordsIn) {
        List<Record> result = new ArrayList<>();
        Map<String, Object> values;
        for (Record recordIn : recordsIn) {
            values = new HashMap<>();
            values.put("server_name", "ESB-" + recordIn.getId());
            values.put("ip", "192.168.0." + (recordIn.getTimestamp() % 256));
            values.put("tenant", recordIn.getTenantId());
            values.put("change_index", recordIn.getTenantId() + 0.3454452);
            result.add(new Record(recordIn.getId(), recordIn.getTenantId(), recordIn.getTableName(), values,
                    System.currentTimeMillis()));
        }
        return result;
    }

    private void cleanupT1() throws AnalyticsException {
        this.analyticsRS.deleteTable(7, "T1");
        this.analyticsRS.deleteTable(15, "T1");
    }

    @Test public void testTableCreateDelete() throws AnalyticsException {
        this.analyticsRS.deleteTable(250035, "TABLE1");
        boolean ok;
        try {
            this.analyticsRS.get(250035, "TABLE1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
            ok = false;
        } catch (AnalyticsTableNotAvailableException e) {
            ok = true;
        }
        Assert.assertTrue(ok);
        this.analyticsRS.deleteTable(250035, "TABLE2");
        this.analyticsRS.deleteTable(-8830, "TABLEX");
        this.analyticsRS.createTable(250035, "TABLE1");
        try {
            this.analyticsRS.get(250035, "TABLE1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
            ok = true;
        } catch (AnalyticsTableNotAvailableException e) {
            ok = false;
        }
        Assert.assertTrue(ok);
        /* this should not throw an exception */
        this.analyticsRS.createTable(250035, "Table1");
        this.analyticsRS.deleteTable(250035, "TABLE2");
        this.analyticsRS.deleteTable(250035, "TABLE1");
        this.analyticsRS.deleteTable(8830, "TABLEX");
        try {
            this.analyticsRS.get(250035, "TABLE1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
            ok = false;
        } catch (AnalyticsTableNotAvailableException e) {
            ok = true;
        }
        Assert.assertTrue(ok);
    }

    @Test public void testLongTableNameCreate() throws AnalyticsException {
        int tenantId = 25;
        String tableName = "OIJFOEIJFW4535363463643ooijowigjweg09XXXX52352_2532oijgwgowij_2352502965u2526515243623632632oijgwergwergw_15251233";
        this.analyticsRS.deleteTable(tenantId, tableName);
        this.analyticsRS.createTable(tenantId, tableName);
        Record record = this.createRecord(tenantId, tableName, "S1", "192.168.1.1", 55, "LOG1");
        List<Record> records = new ArrayList<>();
        records.add(record);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 1);
        this.analyticsRS.deleteTable(tenantId, tableName);
    }

    @Test @SuppressWarnings("unchecked") public void testDataRecordAddRetrieve() throws AnalyticsException {
        this.cleanupT1();
        String serverName = "ESB1";
        String ip = "10.0.0.1";
        int tenant = 44;
        String log = "Boom!";
        this.analyticsRS.createTable(7, "T1");
        Record record = this.createRecord(7, "T1", serverName, ip, tenant, log);
        List<Record> records = new ArrayList<>();
        records.add(record);
        this.analyticsRS.put(records);
        String id = record.getId();
        List<String> ids = new ArrayList<>();
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

    @Test public void testTableMT() throws AnalyticsException {
        int t1 = 10;
        int t2 = 11;
        String tableName = "Table";
        this.analyticsRS.deleteTable(t1, tableName);
        this.analyticsRS.deleteTable(t2, tableName);
        this.analyticsRS.createTable(t1, tableName);
        this.analyticsRS.createTable(t2, tableName);
        List<Record> records1 = generateRecords(t1, tableName, 1, 1, -1, -1);
        List<Record> records2 = generateRecords(t2, tableName, 1, 40, -1, -1);
        this.analyticsRS.put(records1);
        this.analyticsRS.put(records2);
        List<Record> recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(t1, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        List<Record> recordsIn2 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(t2, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(records1.size(), recordsIn1.size());
        Assert.assertEquals(records2.size(), recordsIn2.size());
        Assert.assertEquals(new HashSet<>(records1), new HashSet<>(recordsIn1));
        Assert.assertEquals(new HashSet<>(records2), new HashSet<>(recordsIn2));
        this.analyticsRS.deleteTable(t1, tableName);
        this.analyticsRS.deleteTable(t2, tableName);
    }

    @Test public void testMultipleDataRecordAddRetieve() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        List<Record> records = generateRecords(7, "T1", 1, 100, -1, -1);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        List<String> columns = new ArrayList<>();
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
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        columns.remove("ip");
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 4, columns, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertNotEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        this.cleanupT1();
    }

    @Test public void testMultipleDataRecordAddRetrieveUpdate() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(15, "T1");
        List<Record> records = generateRecords(15, "T1", 1, 50, -1, -1);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(15, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        records = this.generateRecordsForUpdate(records);
        this.analyticsRS.put(records);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(15, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        this.cleanupT1();
    }

    @Test public void testDataRecordCount() throws AnalyticsException {
        if (!this.analyticsRS.isRecordCountSupported()) {
            return;
        }
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");

        int count = (randomNumberGenerator.nextInt(2000)) + 1;
        List<Record> records = generateRecords(7, "T1", 1, count, 15000, 333);
        this.analyticsRS.put(records);
        long res = this.analyticsRS.getRecordCount(7, "T1", Long.MIN_VALUE, Long.MAX_VALUE);
        Assert.assertEquals(res, count);
        Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", 14999, 15001), 1);
        this.analyticsRS.delete(7, "T1", Long.MIN_VALUE, Long.MAX_VALUE);
        Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", Long.MIN_VALUE, Long.MAX_VALUE), 0);
        this.cleanupT1();
    }

    @Test public void testMultipleDataRecordAddRetieveWithTimestampRange1() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time - 10, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, time, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, time, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time + 1, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 5, null, time + 1, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        if (this.analyticsRS.isRecordCountSupported()) {
            long count = this.analyticsRS.getRecordCount(7, "T1", time, time + timeOffset * 99);
            Assert.assertEquals(count, 99);
            Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", time + 1, time + timeOffset * 99 + 1), 99);
            Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", time + 1, time + timeOffset * 99), 98);
        }
        records.remove(99);
        records.remove(0);
        Assert.assertEquals(new HashSet<>(records), new HashSet<>(recordsIn));
        this.cleanupT1();
    }

    @Test public void testMultipleDataRecordAddRetieveWithTimestampRange2() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time + 22, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(recordsIn.size(), 97);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, time, time + timeOffset * 96 - 2, 0, -1));
        Assert.assertEquals(recordsIn.size(), 96);
        if (this.analyticsRS.isRecordCountSupported()) {
            long count = this.analyticsRS.getRecordCount(7, "T1", time + 22, time + timeOffset * 100);
            Assert.assertEquals(count, 97);
            Assert.assertEquals(this.analyticsRS.getRecordCount(7, "T1", time, time + timeOffset * 96 - 2), 96);
        }
        this.cleanupT1();
    }

    @Test public void testMultipleDataRecordAddRetieveWithTimestampRange3() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn = GenericUtils
                .listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 1, null, time - 100, time - 10, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time + timeOffset * 103, time + timeOffset * 110, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        if (this.analyticsRS.isRecordCountSupported()) {
            long count = this.analyticsRS.getRecordCount(7, "T1", time - 100, time - 10);
            Assert.assertEquals(count, 0);
            Assert.assertEquals(
                    this.analyticsRS.getRecordCount(7, "T1", time + timeOffset * 103, time + timeOffset * 110), 0);
        }
        this.cleanupT1();
    }

    @Test public void testMultipleDataRecordAddRetieveWithPagination1() throws AnalyticsException {
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
                this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 1, -1));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 1, 200));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn1.size(), 200);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 1, 199));
        Assert.assertEquals(recordsIn1.size(), 199);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 1, 100));
        Assert.assertEquals(recordsIn1.size(), 100);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 4, null, Long.MIN_VALUE, Long.MAX_VALUE, 100, 101));
        Assert.assertEquals(recordsIn1.size(), 100);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 55, 73));
        Assert.assertEquals(recordsIn1.size(), 73);
        recordsIn1 = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        List<Record> recordsIn2 = new ArrayList<>();
        for (int i = 0; i < 200; i += 20) {
            recordsIn2.addAll(GenericUtils.listRecords(this.analyticsRS,
                    this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, i, 20)));
        }
        Assert.assertEquals(recordsIn2.size(), 200);
        Assert.assertEquals(new HashSet<>(recordsIn1), new HashSet<>(recordsIn2));
        this.cleanupT1();
    }

    @Test public void testMultipleDataRecordAddRetieveWithPagination2() throws AnalyticsException {
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
        List<Record> recordsIn2 = new ArrayList<>();
        for (int i = 0; i < 200; i += 10) {
            recordsIn2.addAll(GenericUtils.listRecords(this.analyticsRS,
                    this.analyticsRS.get(7, "T1", 2, null, time + 55, time + timeOffset * 198 - 5, i, 10)));
        }
        Assert.assertEquals(recordsIn2.size(), 192);
        Assert.assertEquals(new HashSet<>(recordsIn1), new HashSet<>(recordsIn2));
        List<String> columns = new ArrayList<>();
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

    @Test public void testRecordRetrievalWithFixedCount() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords(7, "T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn;
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, 500));
        Assert.assertEquals(recordsIn.size(), 100);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, 38));
        Assert.assertEquals(recordsIn.size(), 38);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, 500));
        Assert.assertEquals(recordsIn.size(), 100);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, 50));
        Assert.assertTrue(recordsIn.size() >= 50);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time + 22, time + timeOffset * 100, 0, 200));
        Assert.assertEquals(recordsIn.size(), 97);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 2, null, time + 22, time + timeOffset * 100, 0, 67));
        Assert.assertEquals(recordsIn.size(), 67);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, time, time + timeOffset * 96 - 2, 0, 1000));
        Assert.assertEquals(recordsIn.size(), 96);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, time, time + timeOffset * 96 - 2, 0, 41));
        Assert.assertEquals(recordsIn.size(), 41);
        this.cleanupT1();
    }

    @Test public void testDataRecordDeleteWithIds() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        List<Record> records = generateRecords(7, "T1", 2, 10, -1, -1);
        this.analyticsRS.put(records);
        Assert.assertEquals(GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 10);
        List<String> ids = new ArrayList<>();
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

    @Test public void testDataRecordDeleteWithTimestamps() throws AnalyticsException {
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
        Assert.assertEquals(new HashSet<>(records), new HashSet<>(recordsIn));
        this.analyticsRS.delete(7, "T1", time + timeOffset * 97, time + timeOffset * 101);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        records.remove(97);
        records.remove(96);
        records.remove(95);
        Assert.assertEquals(recordsIn.size(), records.size());
        Assert.assertEquals(new HashSet<>(records), new HashSet<>(recordsIn));
        this.analyticsRS.delete(7, "T1", time + timeOffset * 5 - 2, time + timeOffset * 7 + 4);
        recordsIn = GenericUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(7, "T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        records.remove(5);
        records.remove(4);
        records.remove(3);
        Assert.assertEquals(new HashSet<>(records), new HashSet<>(recordsIn));
        this.cleanupT1();
    }

    /**
     * Test if the GET operations work as intended when the same record is sent and queried at different time ranges.
     * This test is required in particular for connectors which depend on timestamp-based secondary indices
     * (i.e. HBase, Cassandra).
     * @throws AnalyticsException
     */
    @Test public void testDuplicateRecordSearch() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        List<Record> list = new ArrayList<>();
        Record testRecord = new Record("MyRecord", 7, "T1", new HashMap<String, Object>(), 10000);
        list.add(testRecord);
        this.analyticsRS.put(list);
        Assert.assertEquals(
                GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 1, null, 9000, 11000, 0, -1))
                        .size(), 1);
        list.clear();
        Record testRecord2 = new Record("MyRecord", 7, "T1", new HashMap<String, Object>(), 20000);
        list.add(testRecord2);
        this.analyticsRS.put(list);
        Assert.assertEquals(
                GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 1, null, 19000, 21000, 0, -1))
                        .size(), 1);
        Assert.assertEquals(
                GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 1, null, 9000, 11000, 0, -1))
                        .size(), 0);
        this.cleanupT1();
    }

    /**
     * This test checks whether only the records which strictly fall within the prescribed time ranges are deleted.
     * This test is required in particular for connectors which depend on timestamp-based secondary indices
     * (i.e. HBase, Cassandra).
     * @throws AnalyticsException
     */
    @Test public void testDuplicateRecordDelete() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable(7, "T1");
        List<Record> putList = new ArrayList<>();
        putList.add(new Record("MyRecord", 7, "T1", new HashMap<String, Object>(), 10000));
        putList.add(new Record("MyRecord", 7, "T1", new HashMap<String, Object>(), 20000));
        this.analyticsRS.put(putList);
        Assert.assertEquals(
                GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE,
                        Long.MAX_VALUE - 1L, 0, -1)).size(), 1);
        this.analyticsRS.delete(7,"T1",9000, 11000);
        Assert.assertEquals(
                GenericUtils.listRecords(this.analyticsRS, this.analyticsRS.get(7, "T1", 1, null, Long.MIN_VALUE,
                        Long.MAX_VALUE - 1L, 0, -1)).size(), 1);
        this.cleanupT1();
    }

    @Test public void testDataRecordAddReadPerformance() throws AnalyticsException {
        System.out.println(
                "\n************** START RECORD PERF TEST [" + this.getImplementationName() + "] **************");
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
        System.out
                .println("************** END RECORD PERF TEST [" + this.getImplementationName() + "] **************\n");
        this.cleanupT1();
    }

}
