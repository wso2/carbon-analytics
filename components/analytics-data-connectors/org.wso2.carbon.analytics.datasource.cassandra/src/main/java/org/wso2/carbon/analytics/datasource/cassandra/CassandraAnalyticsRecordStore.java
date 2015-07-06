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
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TokenRange;

import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the Cassandra implementation of {@link AnalyticsRecordStore}.
 */
public class CassandraAnalyticsRecordStore implements AnalyticsRecordStore {

    private static final int STREAMING_BATCH_SIZE = 1000;

    private static final int TS_MULTIPLIER = (int) Math.pow(2, 30);

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
                + "tableName VARCHAR, timestamp VARINT, id VARCHAR, PRIMARY KEY ((tenantId, tableName), timestamp))");
    }
    
    private TokenRangeRecordGroup[] calculateTokenRangeGroups(int tenantId, String tableName, List<String> columns, 
            int numPartitionsHint) {
        Metadata md = this.session.getCluster().getMetadata();
        Set<Host> hosts = md.getAllHosts();
        int partitionsPerHost = (int) Math.ceil(numPartitionsHint / (double) hosts.size());
        List<TokenRangeRecordGroup> result = new ArrayList<CassandraAnalyticsRecordStore.TokenRangeRecordGroup>(
                partitionsPerHost * hosts.size());
        for (Host host : hosts) {
            result.addAll(this.calculateTokenRangeGroupForHost(tenantId, tableName, columns, 
                    md, host, partitionsPerHost));
        }
        return result.toArray(new TokenRangeRecordGroup[0]);
    }
    
    private List<TokenRange> unwrapTR(Set<TokenRange> trs) {
        List<TokenRange> trsUnwrapped = new ArrayList<TokenRange>();
        for (TokenRange tr : trs) {
            trsUnwrapped.addAll(tr.unwrap());
        }
        return trsUnwrapped;
    }
    
    private List<TokenRangeRecordGroup> calculateTokenRangeGroupForHost(int tenantId, String tableName, 
            List<String> columns, Metadata md, Host host, int partitionsPerHost) {
        String ip = host.getAddress().getHostAddress();
        List<TokenRange> trs = this.unwrapTR(md.getTokenRanges("ARS", host));
        int delta = (int) Math.ceil(trs.size() / (double) partitionsPerHost);
        List<TokenRangeRecordGroup> result = new ArrayList<CassandraAnalyticsRecordStore.TokenRangeRecordGroup>(
                partitionsPerHost);
        if (delta > 0) {
            for (int i = 0; i < trs.size(); i += delta) {
                result.add(new TokenRangeRecordGroup(tenantId, tableName, columns, 
                        trs.subList(i, i + delta > trs.size() ? trs.size() : i + delta), ip));
            }
        }
        return result;
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
        ResultSet rs = this.session.execute("SELECT id, timestamp FROM ARS.TS WHERE tenantId = ? AND tableName = ? AND timestamp >= ? AND timestamp < ?",
                tenantId, tableName, BigInteger.valueOf(timeFrom).multiply(BigInteger.valueOf(TS_MULTIPLIER)), 
                BigInteger.valueOf(timeTo).multiply(BigInteger.valueOf(TS_MULTIPLIER)));
        Iterator<Row> tsItr = rs.iterator();
        while (tsItr.hasNext()) {
            this.deleteWithTSItrBatch(tenantId, tableName, tsItr);
        }
    }

    private List<BigInteger> extractTSTableTimestampList(ResultSet rs) {
        List<BigInteger> result = new ArrayList<BigInteger>();
        Iterator<Row> itr = rs.iterator();
        Row row;
        while (itr.hasNext()) {
            row = itr.next();
            result.add(this.toTSTableTimestamp(row.getLong(1), row.getString(0)));
        }
        return result;
    }
    
    private void deleteWithTSItrBatch(int tenantId, String tableName, Iterator<Row> itr) {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        List<String> ids = new ArrayList<String>();
        PreparedStatement ps = session.prepare("DELETE FROM ARS.TS WHERE tenantId = ? AND tableName = ? and timestamp = ?");
        BatchStatement stmt = new BatchStatement();
        Row row;
        for (int i = 0; i < STREAMING_BATCH_SIZE && itr.hasNext(); i++) {
            row = itr.next();
            stmt.add(ps.bind(tenantId, tableName, row.getVarint(1)));
            ids.add(row.getString(0));
        }
        this.session.execute(stmt);
        this.session.execute("DELETE FROM ARS." + dataTable + " WHERE id IN ?", ids);
    }
    
    @Override
    public void delete(int tenantId, String tableName, List<String> ids) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        ResultSet rs = this.session.execute("SELECT id, timestamp FROM ARS." + dataTable + " WHERE id IN ?", ids);
        List<BigInteger> tsTableTimestamps = this.extractTSTableTimestampList(rs);        
        PreparedStatement ps = session.prepare("DELETE FROM ARS.TS WHERE tenantId = ? AND tableName = ? and timestamp = ?");
        BatchStatement stmt = new BatchStatement();
        for (BigInteger ts : tsTableTimestamps) {
            stmt.add(ps.bind(tenantId, tableName, ts));
        }
        this.session.execute(stmt);
        this.session.execute("DELETE FROM ARS." + dataTable + " WHERE id IN ?", ids);
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
        if (numPartitionsHint == 1 || !(timeFrom == Long.MIN_VALUE && timeTo == Long.MAX_VALUE)) {
            return new RecordGroup[] { new GlobalCassandraRecordGroup(tenantId, tableName, columns, timeFrom, timeTo) };
        } else {
            return this.calculateTokenRangeGroups(tenantId, tableName, columns, numPartitionsHint);
        }
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (!this.tableExists(tenantId, tableName)) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        return new RecordGroup[] { new GlobalCassandraRecordGroup(tenantId, tableName, columns, ids) };
    }
    
    @Override
    public AnalyticsIterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        if (recordGroup instanceof GlobalCassandraRecordGroup) {
            GlobalCassandraRecordGroup crg = (GlobalCassandraRecordGroup) recordGroup;
            if (crg.isByIds()) {
                return this.readRecordsByIds(crg);
            } else {
                return this.readRecordsByRange(crg);
            }
        } else if (recordGroup instanceof TokenRangeRecordGroup) {
            return this.readPartitionedRecords((TokenRangeRecordGroup) recordGroup);
        } else {
            throw new AnalyticsException("Unknnown Cassandra record group type: " + recordGroup.getClass());
        }
    }
    
    private AnalyticsIterator<Record> readPartitionedRecords(TokenRangeRecordGroup recordGroup) 
            throws AnalyticsException {
        return this.lookupRecordsByTokenRanges(recordGroup.getTenantId(), recordGroup.getTableName(), 
                recordGroup.getColumns(), recordGroup.getTokenRanges());        
    }
    
    private AnalyticsIterator<Record> readRecordsByRange(GlobalCassandraRecordGroup recordGroup) throws AnalyticsException {
        int tenantId = recordGroup.getTenantId();
        String tableName = recordGroup.getTableName();
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        List<String> columns = recordGroup.getColumns();
        ResultSet rs;
        if (recordGroup.getTimeFrom() == Long.MIN_VALUE && recordGroup.getTimeTo() == Long.MAX_VALUE) {
            rs = this.session.execute("SELECT id, timestamp, data FROM ARS." + dataTable);
            return this.lookupRecordsByDirectRS(tenantId, tableName, rs, columns);
        } else {
            ResultSet tsrs = this.session.execute("SELECT id FROM ARS.TS WHERE tenantId = ? AND tableName = ? "
                    + "AND timestamp >= ? AND timestamp < ?", tenantId, tableName,
                    BigInteger.valueOf(recordGroup.getTimeFrom()).multiply(BigInteger.valueOf(TS_MULTIPLIER)),
                    BigInteger.valueOf(recordGroup.getTimeTo()).multiply(BigInteger.valueOf(TS_MULTIPLIER)));
            return new CassandraRecordIDDataIterator(tenantId, tableName, tsrs.iterator(), columns);
        }
    }
    
    private AnalyticsIterator<Record> readRecordsByIds(GlobalCassandraRecordGroup recordGroup) throws AnalyticsException {
        return this.lookupRecordsByIds(recordGroup.getTenantId(), 
                recordGroup.getTableName(), recordGroup.getIds(), recordGroup.getColumns());
    }
    
    private AnalyticsIterator<Record> lookupRecordsByIds(int tenantId, String tableName, List<String> ids, List<String> columns) {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        ResultSet rs = this.session.execute("SELECT id, timestamp, data FROM ARS." + dataTable + " WHERE id IN ?", ids);
        return this.lookupRecordsByDirectRS(tenantId, tableName, rs, columns);
    }
    
    private AnalyticsIterator<Record> lookupRecordsByTokenRanges(int tenantId, String tableName, 
            List<String> columns, List<TokenRange> tokenRanges) {
        return new CassandraTokenRangeDataIterator(tenantId, tableName, columns, tokenRanges);
    }
    
    private AnalyticsIterator<Record> lookupRecordsByDirectRS(int tenantId, String tableName, ResultSet rs, List<String> columns) {
        return new CassandraDirectDataIterator(tenantId, tableName, rs.iterator(), columns);
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
    
    private BigInteger toTSTableTimestamp(long timestamp, String id) {
        return (BigInteger.valueOf(timestamp).multiply(BigInteger.valueOf(TS_MULTIPLIER))).add(
                BigInteger.valueOf(Math.abs(id.hashCode() % TS_MULTIPLIER)));
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
     * Cassandra data {@link AnalyticsIterator} implementation for streaming, which reads data
     * using the given token ranges.
     */
    public class CassandraTokenRangeDataIterator implements AnalyticsIterator<Record> {
        
        private int tenantId;
        
        private String tableName;
        
        private List<String> columns;
        
        private Iterator<TokenRange> tokenRangesItr;
                
        private AnalyticsIterator<Record> dataItr;
        
        public CassandraTokenRangeDataIterator(int tenantId, String tableName, List<String> columns,
                List<TokenRange> tokenRanges) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.tokenRangesItr = tokenRanges.iterator();
        }
        
        private void populateNextTokenRangeData() {
            String dataTable = generateTargetDataTableName(tenantId, tableName);
            TokenRange tokenRange = this.tokenRangesItr.next();
            ResultSet rs = session.execute("SELECT id, timestamp, data FROM ARS." + dataTable + 
                    " WHERE token(id) > ? and token(id) <= ?", tokenRange.getStart().getValue(), 
                    tokenRange.getEnd().getValue());
            this.dataItr = lookupRecordsByDirectRS(this.tenantId, this.tableName, rs, this.columns);
        }

        @Override
        public boolean hasNext() {
            if (this.dataItr == null) {
                if (this.tokenRangesItr.hasNext()) {
                    this.populateNextTokenRangeData();
                } else {
                    return false;
                }
            }
            if (this.dataItr.hasNext()) {
                return true;
            } else {
                this.dataItr = null;
                return this.hasNext();
            }
        }

        @Override
        public Record next() {
            if (this.hasNext()) {
                return this.dataItr.next();
            } else {
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            /** nothing to do **/
        }
        
    }
    
    /**
     * Cassandra data {@link AnalyticsIterator} implementation for streaming, which reads data
     * using the record id based lookups.
     */
    public class CassandraRecordIDDataIterator implements AnalyticsIterator<Record> {

        private int tenantId;
        
        private String tableName;
                
        private List<String> columns;
        
        private Iterator<Row> resultSetItr;
        
        private AnalyticsIterator<Record> dataItr;
        
        public CassandraRecordIDDataIterator(int tenantId, String tableName, Iterator<Row> resultSetItr, List<String> columns) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.resultSetItr = resultSetItr;
        }
        
        private void populateBatch() {
            List<String> ids = new ArrayList<String>(STREAMING_BATCH_SIZE);
            Row row;
            for (int i = 0; i < STREAMING_BATCH_SIZE && this.resultSetItr.hasNext(); i++) {
                row = this.resultSetItr.next();
                ids.add(row.getString(0));
            }
            String dataTable = generateTargetDataTableName(this.tenantId, this.tableName);
            ResultSet rs = session.execute("SELECT id, timestamp, data FROM ARS." + dataTable + " WHERE id IN ?", ids);
            this.dataItr = new CassandraDirectDataIterator(this.tenantId, this.tableName, rs.iterator(), this.columns);
        }
        
        @Override
        public boolean hasNext() {
            if (this.dataItr == null) {
                if (this.resultSetItr.hasNext()) {
                    this.populateBatch();
                } else {
                    return false;
                }
            }
            if (this.dataItr.hasNext()) {
                return true;
            } else {
                this.dataItr = null;
                return this.hasNext();
            }
        }

        @Override
        public Record next() {
            if (this.hasNext()) {
                return this.dataItr.next();
            } else {
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            /** nothing to do **/
        }
        
    }
    
    /**
     * Cassandra data {@link AnalyticsIterator} implementation for streaming, which reads data
     * directly from the data table.
     */
    public static class CassandraDirectDataIterator implements AnalyticsIterator<Record> {

        private int tenantId;
        
        private String tableName;
        
        private List<String> columns;
        
        private Iterator<Row> resultSetItr;
        
        public CassandraDirectDataIterator(int tenantId, String tableName, Iterator<Row> resultSetItr, List<String> columns) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.resultSetItr = resultSetItr;
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
            /** nothing to do **/
        }
        
    }
    
    /**
     * Cassandra {@link RecordGroup} implementation to be used without partitioning.
     */
    public static class GlobalCassandraRecordGroup implements RecordGroup {

        private static final long serialVersionUID = 4922546772273816597L;
        
        private boolean byIds;
        
        private int tenantId;
        
        private String tableName;
        
        private List<String> columns;
        
        private long timeFrom;
        
        private long timeTo;
        
        private List<String> ids;
                
        public GlobalCassandraRecordGroup(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.timeFrom = timeFrom;
            this.timeTo = timeTo;
            this.byIds = false;
        }
        
        public GlobalCassandraRecordGroup(int tenantId, String tableName, List<String> columns, List<String> ids) {
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
    
    /**
     * Cassandra {@link RecordGroup} implementation, which is token range aware,
     * used for partition based record groups.
     */
    public static class TokenRangeRecordGroup implements RecordGroup {
        
        private static final long serialVersionUID = -8748485743904308191L;

        private int tenantId;
        
        private String tableName;
        
        private List<String> columns;
        
        private List<TokenRange> tokenRanges;
        
        private String host;
        
        public TokenRangeRecordGroup(int tenantId, String tableName, List<String> columns,
                List<TokenRange> tokenRanges, String host) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.tokenRanges = tokenRanges;
            this.host = host;
        }
     
        @Override
        public String[] getLocations() throws AnalyticsException {
            return new String[] { this.host };
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
        
        public List<TokenRange> getTokenRanges() {
            return tokenRanges;
        }
        
    }

}
