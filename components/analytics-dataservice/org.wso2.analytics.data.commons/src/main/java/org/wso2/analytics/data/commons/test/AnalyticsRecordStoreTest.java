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
package org.wso2.analytics.data.commons.test;

import org.apache.commons.collections.IteratorUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.analytics.data.commons.AnalyticsRecordStore;
import org.wso2.analytics.data.commons.exception.AnalyticsException;
import org.wso2.analytics.data.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.analytics.data.commons.sources.Record;
import org.wso2.analytics.data.commons.sources.RecordGroup;
import org.wso2.analytics.data.commons.utils.AnalyticsCommonUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class contains tests related to {@link AnalyticsRecordStore}.
 */
public abstract class AnalyticsRecordStoreTest {

    private AnalyticsRecordStore analyticsRS;

    private String implementationName;

    protected void init(String implementationName, AnalyticsRecordStore analyticsRS) throws AnalyticsException {
        this.implementationName = implementationName;
        this.analyticsRS = analyticsRS;
    }

    protected void cleanup() throws AnalyticsException {
        this.analyticsRS.deleteTable("MYTABLE1");
        this.analyticsRS.deleteTable("T1");
        this.analyticsRS.destroy();
    }

    private String getImplementationName() {
        return implementationName;
    }

    private Record createRecord(String tableName, String serverName, String ip, int tenant, String log) {
        Map<String, Object> values = new HashMap<>();
        values.put("server_name", serverName);
        values.put("ip", ip);
        values.put("tenant", tenant);
        values.put("log", log);
        values.put("sequence", null);
        values.put("summary2", null);
        return new Record(AnalyticsCommonUtils.generateRecordID(), tableName, values, System.currentTimeMillis());
    }

    public static List<Record> generateRecords(String tableName, int i, int c, long time,
                                               int timeOffset) {
        return generateRecords(tableName, i, c, time, timeOffset, true);
    }

    public static List<Record> generateRecords(String tableName, int i, int c, long time, int timeOffset,
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
                    new Record(generateRecordIds ? AnalyticsCommonUtils.generateRecordID() : null, tableName, values,
                            timeTmp));
        }
        return result;
    }

    private List<Record> generateRecordsForUpdate(List<Record> recordsIn) {
        List<Record> result = new ArrayList<>();
        int randInt = ThreadLocalRandom.current().nextInt(0, 65536);
        Map<String, Object> values;
        for (Record recordIn : recordsIn) {
            values = new HashMap<>();
            values.put("server_name", "ESB-" + recordIn.getId());
            values.put("ip", "192.168.0." + (recordIn.getTimestamp() % 256));
            values.put("tenant", randInt);
            values.put("change_index", randInt + 0.3454452);
            result.add(new Record(recordIn.getId(), recordIn.getTableName(), values,
                    System.currentTimeMillis()));
        }
        return result;
    }

    private void cleanupT1() throws AnalyticsException {
        this.analyticsRS.deleteTable("T1");
    }

    @Test
    public void testTableCreateDelete() throws AnalyticsException {
        this.analyticsRS.deleteTable("TABLE1");
        boolean ok;
        try {
            this.analyticsRS.get("TABLE1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
            ok = false;
        } catch (AnalyticsTableNotAvailableException e) {
            ok = true;
        }
        Assert.assertTrue(ok);
        this.analyticsRS.deleteTable("TABLE2");
        this.analyticsRS.deleteTable("TABLEX");
        this.analyticsRS.createTable("TABLE1");
        try {
            this.analyticsRS.get("TABLE1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
            ok = true;
        } catch (AnalyticsTableNotAvailableException e) {
            ok = false;
        }
        Assert.assertTrue(ok);
        /* this should not throw an exception */
        this.analyticsRS.createTable("Table1");
        this.analyticsRS.deleteTable("TABLE2");
        this.analyticsRS.deleteTable("TABLE1");
        this.analyticsRS.deleteTable("TABLEX");
        try {
            this.analyticsRS.get("TABLE1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
            ok = false;
        } catch (AnalyticsTableNotAvailableException e) {
            ok = true;
        }
        Assert.assertTrue(ok);
    }

    @Test
    public void testLongTableNameCreate() throws AnalyticsException {
        String tableName = "OIJFOEIJFW4535363463643ooijowigjweg09XXXX52352_2532oijgwgowij_2352502965u2526515243623632632oijgwergwergw_15251233";
        this.analyticsRS.deleteTable(tableName);
        this.analyticsRS.createTable(tableName);
        Record record = this.createRecord(tableName, "S1", "192.168.1.1", 55, "LOG1");
        List<Record> records = new ArrayList<>();
        records.add(record);
        this.analyticsRS.put(records);
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get(tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 1);
        this.analyticsRS.deleteTable(tableName);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDataRecordAddRetrieve() throws AnalyticsException {
        this.cleanupT1();
        String serverName = "ESB1";
        String ip = "10.0.0.1";
        int tenant = 44;
        String log = "Boom!";
        this.analyticsRS.createTable("T1");
        Record record = this.createRecord("T1", serverName, ip, tenant, log);
        List<Record> records = new ArrayList<>();
        records.add(record);
        this.analyticsRS.put(records);
        String id = record.getId();
        List<String> ids = new ArrayList<>();
        ids.add(id);
        RecordGroup[] rgs = this.analyticsRS.get("T1", 3, null, ids);
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
    public void testMultipleDataRecordAddRetieve() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable("T1");
        List<Record> records = generateRecords("T1", 1, 100, -1, -1);
        this.analyticsRS.put(records);
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
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
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 1, columns, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        columns.remove("ip");
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 4, columns, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertNotEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        this.cleanupT1();
    }

    @Test
    public void testMultipleDataRecordAddRetrieveUpdate() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable("T1");
        List<Record> records = generateRecords("T1", 1, 50, -1, -1);
        this.analyticsRS.put(records);
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        records = this.generateRecordsForUpdate(records);
        this.analyticsRS.put(records);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        this.cleanupT1();
    }

    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange1() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable("T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords("T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, time - 10, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 1, null, time, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(new HashSet<>(recordsIn), new HashSet<>(records));
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 1, null, time, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, time + 1, time + timeOffset * 99 + 1, 0, -1));
        Assert.assertEquals(recordsIn.size(), 99);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 5, null, time + 1, time + timeOffset * 99, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        records.remove(99);
        records.remove(0);
        Assert.assertEquals(new HashSet<>(records), new HashSet<>(recordsIn));
        this.cleanupT1();
    }

    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange2() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable("T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords("T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, time + 22, time + timeOffset * 100, 0, -1));
        Assert.assertEquals(recordsIn.size(), 97);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 3, null, time, time + timeOffset * 96 - 2, 0, -1));
        Assert.assertEquals(recordsIn.size(), 96);
        this.cleanupT1();
    }

    @Test
    public void testMultipleDataRecordAddRetieveWithTimestampRange3() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable("T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords("T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn = AnalyticsCommonUtils
                .listRecords(this.analyticsRS, this.analyticsRS.get("T1", 1, null, time - 100, time - 10, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, time + timeOffset * 103, time + timeOffset * 110, 0, -1));
        Assert.assertEquals(recordsIn.size(), 0);
        this.cleanupT1();
    }

    @Test
    public void testRecordRetrievalWithFixedCount() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable("T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords("T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        List<Record> recordsIn;
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, 500));
        Assert.assertEquals(recordsIn.size(), 100);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, 38));
        Assert.assertEquals(recordsIn.size(), 38);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, 500));
        Assert.assertEquals(recordsIn.size(), 100);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, 50));
        Assert.assertTrue(recordsIn.size() >= 50);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, time + 22, time + timeOffset * 100, 0, 200));
        Assert.assertEquals(recordsIn.size(), 97);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, time + 22, time + timeOffset * 100, 0, 67));
        Assert.assertEquals(recordsIn.size(), 67);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 3, null, time, time + timeOffset * 96 - 2, 0, 1000));
        Assert.assertEquals(recordsIn.size(), 96);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 3, null, time, time + timeOffset * 96 - 2, 0, 41));
        Assert.assertEquals(recordsIn.size(), 41);
        this.cleanupT1();
    }

    @Test
    public void testDataRecordDeleteWithIds() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable("T1");
        List<Record> records = generateRecords("T1", 2, 10, -1, -1);
        this.analyticsRS.put(records);
        Assert.assertEquals(AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 10);
        List<String> ids = new ArrayList<>();
        ids.add(records.get(2).getId());
        ids.add(records.get(5).getId());
        this.analyticsRS.delete("T1", ids);
        Assert.assertEquals(AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 8);
        ids.clear();
        ids.add(records.get(0).getId());
        this.analyticsRS.delete("T1", ids);
        Assert.assertEquals(AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 7);
        this.analyticsRS.delete("T1", ids);
        Assert.assertEquals(AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 7);
        this.analyticsRS.delete("T1", new ArrayList<>());
        Assert.assertEquals(AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 7);
        this.cleanupT1();
    }

    @Test
    public void testDataRecordDeleteWithTimestamps() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable("T1");
        long time = System.currentTimeMillis();
        int timeOffset = 10;
        List<Record> records = generateRecords("T1", 1, 100, time, timeOffset);
        this.analyticsRS.put(records);
        Assert.assertEquals(AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1)).size(), 100);
        this.analyticsRS.delete("T1", time - 100, time + 12);
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 2, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(recordsIn.size(), 98);
        records.remove(0);
        records.remove(0);
        Assert.assertEquals(new HashSet<>(records), new HashSet<>(recordsIn));
        this.analyticsRS.delete("T1", time + timeOffset * 97, time + timeOffset * 101);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        records.remove(97);
        records.remove(96);
        records.remove(95);
        Assert.assertEquals(recordsIn.size(), records.size());
        Assert.assertEquals(new HashSet<>(records), new HashSet<>(recordsIn));
        this.analyticsRS.delete("T1", time + timeOffset * 5 - 2, time + timeOffset * 7 + 4);
        recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
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
     *
     * @throws AnalyticsException
     */
    @Test
    public void testDuplicateRecordSearch() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable("T1");
        List<Record> list = new ArrayList<>();
        Record testRecord = new Record("MyRecord", "T1", new HashMap<>(), 10000);
        list.add(testRecord);
        this.analyticsRS.put(list);
        Assert.assertEquals(
                AnalyticsCommonUtils.listRecords(this.analyticsRS, this.analyticsRS.get("T1", 1, null, 9000, 11000, 0, -1))
                        .size(), 1);
        list.clear();
        Record testRecord2 = new Record("MyRecord", "T1", new HashMap<>(), 20000);
        list.add(testRecord2);
        this.analyticsRS.put(list);
        Assert.assertEquals(
                AnalyticsCommonUtils.listRecords(this.analyticsRS, this.analyticsRS.get("T1", 1, null, 19000, 21000, 0, -1))
                        .size(), 1);
        Assert.assertEquals(
                AnalyticsCommonUtils.listRecords(this.analyticsRS, this.analyticsRS.get("T1", 1, null, 9000, 11000, 0, -1))
                        .size(), 0);
        this.cleanupT1();
    }

    /**
     * This test checks whether only the records which strictly fall within the prescribed time ranges are deleted.
     * This test is required in particular for connectors which depend on timestamp-based secondary indices
     * (i.e. HBase, Cassandra).
     *
     * @throws AnalyticsException
     */
    @Test
    public void testDuplicateRecordDelete() throws AnalyticsException {
        this.cleanupT1();
        this.analyticsRS.createTable("T1");
        List<Record> putList = new ArrayList<>();
        putList.add(new Record("MyRecord", "T1", new HashMap<>(), 10000));
        putList.add(new Record("MyRecord", "T1", new HashMap<>(), 20000));
        this.analyticsRS.put(putList);
        Assert.assertEquals(
                AnalyticsCommonUtils.listRecords(this.analyticsRS, this.analyticsRS.get("T1", 1, null, Long.MIN_VALUE,
                        Long.MAX_VALUE - 1L, 0, -1)).size(), 1);
        this.analyticsRS.delete("T1", 9000, 11000);
        Assert.assertEquals(
                AnalyticsCommonUtils.listRecords(this.analyticsRS, this.analyticsRS.get("T1", 1, null, Long.MIN_VALUE,
                        Long.MAX_VALUE - 1L, 0, -1)).size(), 1);
        this.cleanupT1();
    }

    @Test
    public void testDataRecordAddReadPerformance() throws AnalyticsException {
        System.out.println(
                "\n************** START RECORD PERF TEST [" + this.getImplementationName() + "] **************");
        this.cleanupT1();
        
        /* warm-up */
        this.analyticsRS.createTable("T1");
        List<Record> records;
        for (int i = 0; i < 10; i++) {
            records = generateRecords("T1", i, 100, -1, -1);
            this.analyticsRS.put(records);
        }
        this.cleanupT1();

        this.analyticsRS.createTable("T1");
        long hash1 = 0;
        int n = 50, batch = 200;
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            records = generateRecords("T1", i, batch, -1, -1);
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
        List<Record> recordsIn = AnalyticsCommonUtils.listRecords(this.analyticsRS,
                this.analyticsRS.get("T1", 3, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
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
