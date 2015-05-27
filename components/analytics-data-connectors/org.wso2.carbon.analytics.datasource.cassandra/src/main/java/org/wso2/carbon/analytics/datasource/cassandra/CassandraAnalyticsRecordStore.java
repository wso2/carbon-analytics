/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.datasource.cassandra;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * This class represents the Cassandra implementation of {@link AnalyticsRecordStore}.
 */
public class CassandraAnalyticsRecordStore implements AnalyticsRecordStore {

    private static final int TS_MULTIPLIER = 100000;
    private Session session;
    
    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        String servers = properties.get(CassandraUtils.CASSANDRA_SERVERS);
        if (servers == null) {
            throw new AnalyticsException("The Cassandra connector property '" + CassandraUtils.CASSANDRA_SERVERS + "' is mandatory");
        }
        Cluster cluster = Cluster.builder().addContactPoints(servers.split(",")).build();
        this.session = cluster.connect();
        this.session.execute("CREATE KEYSPACE IF NOT EXISTS ARS WITH REPLICATION = "
                + "{'class':'SimpleStrategy', 'replication_factor':3};");
        this.session.execute("CREATE TABLE IF NOT EXISTS ARS.META (tenantId INT, "
                + "tableName VARCHAR, tableSchema BLOB, PRIMARY KEY (tenantId, tableName))");
        this.session.execute("CREATE TABLE IF NOT EXISTS ARS.TS (tenantId INT, "
                + "tableName VARCHAR, timestamp BIGINT, id VARCHAR, PRIMARY KEY ((tenantId, tableName), timestamp))");
    }
    
    private String generateTargetDataTableName(int tenantId, String tableName) {
        return "DATA_" + tenantId + "_" + GenericUtils.normalizeTableName(tableName);
    }

    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        this.session.execute("CREATE TABLE IF NOT EXISTS ARS." + dataTable +
                " (id VARCHAR, timestamp BIGINT, data MAP<VARCHAR, BLOB>, PRIMARY KEY (id))");
        this.session.execute("INSERT INTO ARS.META (tenantId, tableName) VALUES (?, ?)", 
                tenantId, GenericUtils.normalizeTableName(tableName));
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        tableName = GenericUtils.normalizeTableName(tableName);
        this.session.execute("DELETE FROM ARS.META WHERE tenantId = ? AND tableName = ?", tenantId, tableName);
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        this.session.execute("DELETE FROM ARS.TS WHERE tenantId = ? AND tableName = ?", tenantId, tableName);
        this.session.execute("DROP TABLE IF EXISTS ARS." + dataTable);
    }

    @Override
    public void destroy() throws AnalyticsException {
        if (this.session != null) {
            this.session.close();
        }
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom, 
            long timeTo, int recordsFrom, int recordsCount) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return new RecordGroup[] { new CassandraRecordGroup(tenantId, tableName, columns, timeFrom, timeTo) };
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        return new RecordGroup[] { new CassandraRecordGroup(tenantId, tableName, columns, ids) };
    }
    
    @Override
    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        CassandraRecordGroup crg = (CassandraRecordGroup) recordGroup;
        if (crg.isByIds()) {
            return this.readRecordsByIds(crg);
        } else {
            return this.readRecordsByRange(crg);
        }
    }
    
    private Iterator<Record> readRecordsByRange(CassandraRecordGroup recordGroup) throws AnalyticsException {
        int tenantId = recordGroup.getTenantId();
        String tableName = GenericUtils.normalizePath(recordGroup.getTableName());
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        ResultSet rs;
        if (recordGroup.getTimeFrom() == Long.MIN_VALUE && recordGroup.getTimeTo() == Long.MAX_VALUE) {
            rs = this.session.execute("SELECT id, timestamp, data FROM ARS." + dataTable);
            return this.lookupRecordsByRS(tenantId, tableName, rs);
        } else {
            ResultSet tsrs = this.session.execute("SELECT id FROM ARS.TS WHERE tenantId = ? AND tableName = ? AND timestamp >= ? AND timestamp < ?",
                    tenantId, tableName, recordGroup.getTimeFrom() * TS_MULTIPLIER, recordGroup.getTimeTo() * TS_MULTIPLIER);
            List<Row> rows = tsrs.all();
            List<String> ids = new ArrayList<String>(rows.size());
            for (Row row : rows) {
                ids.add(row.getString(0));
            }
            return this.lookupRecordsByIds(tenantId, tableName, ids);
        }
    }
    
    private Iterator<Record> readRecordsByIds(CassandraRecordGroup recordGroup) throws AnalyticsException {
        return this.lookupRecordsByIds(recordGroup.getTenantId(), 
                GenericUtils.normalizeTableName(recordGroup.getTableName()), recordGroup.getIds());
    }
    
    private Iterator<Record> lookupRecordsByIds(int tenantId, String tableName, List<String> ids) {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        ResultSet rs = this.session.execute("SELECT id, timestamp, data FROM ARS." + dataTable + " WHERE id IN ?", ids);
        return this.lookupRecordsByRS(tenantId, tableName, rs);
    }
    
    private Iterator<Record> lookupRecordsByRS(int tenantId, String tableName, ResultSet rs) {
        return null;
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return -1;
    }

    @Override
    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        ResultSet rs = this.session.execute("SELECT tableSchema FROM ARS.META WHERE tenantId = ? AND tableName = ?", 
                tenantId, GenericUtils.normalizeTableName(tableName));
        Row row = rs.one();
        if (row == null) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        ByteBuffer byteBuffer = row.getBytes(0);
        if (byteBuffer == null || byteBuffer.remaining() == 0) {
            return new AnalyticsSchema();
        }
        byte[] data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data);
        return (AnalyticsSchema) GenericUtils.deserializeObject(data);
    }

    @Override
    public boolean isPaginationSupported() {
        return false;
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        ResultSet rs = this.session.execute("SELECT tableName FROM ARS.META WHERE tenantId = ?", tenantId);
        List<String> result = new ArrayList<String>();
        Iterator<Row> itr = rs.iterator();
        Row row;
        while (itr.hasNext()) {
            row = itr.next();
            result.add(row.getString(0));
        }
        return result;
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Collection<List<Record>> batches = GenericUtils.generateRecordBatches(records);
        for (List<Record> batch : batches) {
            this.addBatch(batch);
        }
    }
    
    private Map<String, ByteBuffer> getDataMapFromValues(Map<String, Object> values) {
        Map<String, ByteBuffer> result = new HashMap<String, ByteBuffer>(values.size());
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            result.put(entry.getKey(), ByteBuffer.wrap(GenericUtils.serializeObject(entry.getValue())));
        }
        return result;
    }
    
    private long toTSTableTimestamp(long timestamp) {
        return timestamp * TS_MULTIPLIER + (int) (Math.random() * TS_MULTIPLIER);
    }
    
    private void addBatch(List<Record> batch) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Record firstRecord = batch.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = GenericUtils.normalizeTableName(firstRecord.getTableName());
        try {                
            String dataTable = this.generateTargetDataTableName(tenantId, tableName);
            PreparedStatement ps = session.prepare("INSERT INTO ARS." + dataTable +" (id, timestamp, data) VALUES (?, ?, ?)");
            BatchStatement stmt = new BatchStatement();
            for (Record record : batch) {
                stmt.add(ps.bind(record.getId(), record.getTimestamp(), this.getDataMapFromValues(record.getValues())));
            }
            this.session.execute(stmt);
            ps = session.prepare("INSERT INTO ARS.TS (tenantId, tableName, timestamp, id) VALUES (?, ?, ?, ?)");
            stmt = new BatchStatement();
            for (Record record : batch) {
                stmt.add(ps.bind(tenantId, tableName, this.toTSTableTimestamp(record.getTimestamp()), record.getId()));
            }
            this.session.execute(stmt);
        } catch (Exception e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in adding record batch: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void setTableSchema(int tenantId, String tableName, AnalyticsSchema schema) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        try {
            this.session.execute("UPDATE ARS.META SET tableSchema = ? WHERE tenantId = ? AND tableName = ?", 
                ByteBuffer.wrap(GenericUtils.serializeObject(schema)), tenantId, tableName);
        } catch (Exception e) {
            if (!tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in setting table schema: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        ResultSet rs = this.session.execute("SELECT tableSchema FROM ARS.META WHERE tenantId = ? AND tableName = ?", 
                tenantId, tableName);
        return rs.iterator().hasNext();
    }
    
    public static class CassandraRecordGroup implements RecordGroup {

        private static final long serialVersionUID = 4922546772273816597L;
        
        private boolean byIds;
        
        private int tenantId;
        
        private String tableName;
        
        private List<String> columns;
        
        private long timeFrom;
        
        private long timeTo;
        
        private List<String> ids;
        
        public CassandraRecordGroup(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.timeFrom = timeFrom;
            this.timeTo = timeTo;
            this.byIds = false;
        }
        
        public CassandraRecordGroup(int tenantId, String tableName, List<String> columns, List<String> ids) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.ids = ids;
            this.byIds = true;
        }

        @Override
        public String[] getLocations() throws AnalyticsException {
            return new String[] { "localhost" };
        }
        
        public boolean isByIds() {
            return byIds;
        }
        
        public int getTenantId() {
            return tenantId;
        }
        
        public String getTableName() {
            return tableName;
        }
        
        public List<String> getColumns() {
            return columns;
        }
        
        public long getTimeFrom() {
            return timeFrom;
        }
        
        public long getTimeTo() {
            return timeTo;
        }

        public List<String> getIds() {
            return ids;
        }
        
    }
    
    public static void main(String[] args) throws Exception {
        AnalyticsRecordStore x = new CassandraAnalyticsRecordStore();
        Map<String, String> props = new HashMap<String, String>();
        props.put("servers", "localhost");
        x.init(props);
        System.out.println("Start..");
        
        x.createTable(1, "T1");
        x.createTable(1, "T2");
        x.createTable(1, "T3");
        x.createTable(2, "TX");
        x.setTableSchema(1, "T2", new AnalyticsSchema());
        System.out.println("A:" + x.getTableSchema(1, "T2"));
        x.deleteTable(1, "T3");
        System.out.println("B:" + x.tableExists(1, "T3"));
        System.out.println("X:" + x.listTables(1));
        System.out.println("Y:" + x.listTables(2));
        
        x.createTable(20, "Table1");
        x.createTable(21, "Table2");
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(20, "Table1", 43, 100, System.currentTimeMillis(), 10);
        records.addAll(AnalyticsRecordStoreTest.generateRecords(21, "Table2", 43, 100, System.currentTimeMillis(), 10));
        x.put(records);
        
        System.out.println("End.");
        System.exit(0);
    }

}
