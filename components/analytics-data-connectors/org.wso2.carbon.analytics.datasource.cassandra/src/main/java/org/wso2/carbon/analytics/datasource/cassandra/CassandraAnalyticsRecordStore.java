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

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        this.session.execute("CREATE TABLE IF NOT EXISTS ARS.TS (tenantId INT, "
                + "tableName VARCHAR, timestamp BIGINT, id VARCHAR, PRIMARY KEY ((tenantId, tableName), timestamp))");
    }
    
    private String generateTargetDataTableName(int tenantId, String tableName) {
        if (tenantId < 0) {
            return "DATA_X" + Math.abs(tenantId) + "_" + tableName;
        } else {
            return "DATA_" + tenantId + "_" + tableName;
        }
    }

    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        this.session.execute("CREATE TABLE IF NOT EXISTS ARS." + dataTable +
                " (id VARCHAR, timestamp BIGINT, data MAP<VARCHAR, BLOB>, PRIMARY KEY (id))");
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        ResultSet rs = this.session.execute("SELECT id FROM ARS.TS WHERE tenantId = ? AND tableName = ? AND timestamp >= ? AND timestamp < ?",
                tenantId, tableName, timeFrom == Long.MIN_VALUE ? timeFrom : timeFrom * TS_MULTIPLIER, 
                        timeTo == Long.MAX_VALUE ? timeTo : timeTo * TS_MULTIPLIER);
        List<String> ids = this.resultSetToIds(rs);
        this.delete(tenantId, tableName, ids);
//        this.session.execute("DELETE FROM ARS.TS WHERE tenantId = ? AND tableName = ? AND timestamp >= ? AND timestamp < ?",
//                tenantId, tableName, timeFrom == Long.MAX_VALUE ? timeFrom : timeFrom * TS_MULTIPLIER, 
//                        timeTo == Long.MIN_VALUE ? timeTo : timeTo * TS_MULTIPLIER); 
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        this.session.execute("DELETE FROM ARS." + dataTable + " WHERE id IN ?", ids);
        /* should delete the TS ids also */
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
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
        if (!this.tableExists(tenantId, tableName)) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        return new RecordGroup[] { new CassandraRecordGroup(tenantId, tableName, columns, timeFrom, timeTo) };
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (!this.tableExists(tenantId, tableName)) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        return new RecordGroup[] { new CassandraRecordGroup(tenantId, tableName, columns, ids) };
    }
    
    @Override
    public AnalyticsIterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        CassandraRecordGroup crg = (CassandraRecordGroup) recordGroup;
        if (crg.isByIds()) {
            return this.readRecordsByIds(crg);
        } else {
            return this.readRecordsByRange(crg);
        }
    }
    
    private AnalyticsIterator<Record> readRecordsByRange(CassandraRecordGroup recordGroup) throws AnalyticsException {
        int tenantId = recordGroup.getTenantId();
        String tableName = recordGroup.getTableName();
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        List<String> columns = recordGroup.getColumns();
        ResultSet rs;
        if (recordGroup.getTimeFrom() == Long.MIN_VALUE && recordGroup.getTimeTo() == Long.MAX_VALUE) {
            rs = this.session.execute("SELECT id, timestamp, data FROM ARS." + dataTable);
            return this.lookupRecordsByRS(tenantId, tableName, rs, columns);
        } else {
            ResultSet tsrs = this.session.execute("SELECT id FROM ARS.TS WHERE tenantId = ? AND tableName = ? AND timestamp >= ? AND timestamp < ?",
                    tenantId, tableName, 
                    recordGroup.getTimeFrom() == Long.MIN_VALUE ? recordGroup.getTimeFrom() : recordGroup.getTimeFrom() * TS_MULTIPLIER, 
                    recordGroup.getTimeTo() == Long.MAX_VALUE ? recordGroup.getTimeTo() : recordGroup.getTimeTo() * TS_MULTIPLIER);
            List<String> ids = this.resultSetToIds(tsrs);
            return this.lookupRecordsByIds(tenantId, tableName, ids, columns);
        }
    }
    
    private List<String> resultSetToIds(ResultSet rs) {
        List<Row> rows = rs.all();
        List<String> ids = new ArrayList<String>(rows.size());
        for (Row row : rows) {
            ids.add(row.getString(0));
        }
        return ids;
    }
    
    private AnalyticsIterator<Record> readRecordsByIds(CassandraRecordGroup recordGroup) throws AnalyticsException {
        return this.lookupRecordsByIds(recordGroup.getTenantId(), 
                recordGroup.getTableName(), recordGroup.getIds(), recordGroup.getColumns());
    }
    
    private AnalyticsIterator<Record> lookupRecordsByIds(int tenantId, String tableName, List<String> ids, List<String> columns) {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        ResultSet rs = this.session.execute("SELECT id, timestamp, data FROM ARS." + dataTable + " WHERE id IN ?", ids);
        return this.lookupRecordsByRS(tenantId, tableName, rs, columns);
    }
    
    private AnalyticsIterator<Record> lookupRecordsByRS(int tenantId, String tableName, ResultSet rs, List<String> columns) {
        return new CassandraDataIterator(tenantId, tableName, rs, columns);
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return -1;
    }

    @Override
    public boolean isPaginationSupported() {
        return false;
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
    
    private long toTSTableTimestamp(long timestamp, String id) {
        //System.out.println("** X: " + (timestamp * TS_MULTIPLIER + id.hashCode() % TS_MULTIPLIER));
        //return timestamp * TS_MULTIPLIER + id.hashCode() % TS_MULTIPLIER;
        return timestamp * TS_MULTIPLIER + (long) (Math.random() * TS_MULTIPLIER);
    }
    
    private void addBatch(List<Record> batch) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Record firstRecord = batch.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        try {                
            String dataTable = this.generateTargetDataTableName(tenantId, tableName);
            PreparedStatement ps = session.prepare("INSERT INTO ARS." + dataTable + " (id, timestamp, data) VALUES (?, ?, ?)");
            BatchStatement stmt = new BatchStatement();
            for (Record record : batch) {
                stmt.add(ps.bind(record.getId(), record.getTimestamp(), this.getDataMapFromValues(record.getValues())));
            }
            this.session.execute(stmt);
            ps = session.prepare("INSERT INTO ARS.TS (tenantId, tableName, timestamp, id) VALUES (?, ?, ?, ?)");
            stmt = new BatchStatement();
            for (Record record : batch) {
                stmt.add(ps.bind(tenantId, tableName, this.toTSTableTimestamp(record.getTimestamp(), 
                        record.getId()), record.getId()));
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

    private boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        try {
            ResultSet rs = this.session.execute("SELECT COUNT(*) FROM ARS." + dataTable);
            return rs.iterator().hasNext();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Cassandra data {@link Iterator} implementation for streaming.
     */
    public static class CassandraDataIterator implements AnalyticsIterator<Record> {

        private int tenantId;
        
        private String tableName;
        
        private List<String> columns;
        
        private Iterator<Row> resultSetItr;
        
        public CassandraDataIterator(int tenantId, String tableName, ResultSet rs, List<String> columns) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.resultSetItr = rs.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return this.resultSetItr.hasNext();
        }
        
        private byte[] extractBytes(ByteBuffer byteBuffer) {
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);
            return data;
        }

        @Override
        public Record next() {
            Row row = this.resultSetItr.next();
            Map<String, Object> values;
            Map<String, ByteBuffer> binaryValues = row.getMap(2, String.class, ByteBuffer.class);
            if (this.columns == null) {
                values = new HashMap<String, Object>(binaryValues.size());
                for (Map.Entry<String, ByteBuffer> binaryValue : binaryValues.entrySet()) {
                    values.put(binaryValue.getKey(), GenericUtils.deserializeObject(this.extractBytes(binaryValue.getValue())));
                }
            } else {
                values = new HashMap<String, Object>(this.columns.size());
                for (String column : this.columns) {
                    if (binaryValues.containsKey(column)) {
                        values.put(column, GenericUtils.deserializeObject(this.extractBytes(binaryValues.get(column))));
                    }
                }
            }
            return new Record(row.getString(0), this.tenantId, this.tableName, values, row.getLong(1));
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            /** Nothing to do **/
        }
    }
    
    /**
     * Cassandra {@link RecordGroup} implementation.
     */
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

}
