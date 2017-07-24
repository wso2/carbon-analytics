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
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TokenRange;
import com.google.common.cache.CacheBuilder;

import com.google.common.collect.Lists;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.ndatasource.common.DataSourceException;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * This class represents the Cassandra implementation of {@link AnalyticsRecordStore}.
 */
@SuppressWarnings("deprecation")
public class CassandraAnalyticsRecordStore implements AnalyticsRecordStore {

    private Session session;
    
    private PreparedStatement tableExistsStmt;
    
    private PreparedStatement timestampRecordDeleteStmt;
    
    private PreparedStatement timestampRecordAddStmt;
    
    private String ksName;
    
    private ConcurrentMap<Object, Object> recordInsertStmtMap = CacheBuilder.newBuilder().maximumSize(
            CassandraConstants.RECORD_INSERT_STATEMENTS_CACHE_SIZE).build().asMap();

    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        String dsName = CassandraUtils.extractDataSourceName(properties);
        this.ksName = CassandraUtils.extractARSKSName(properties);
        String ksCreateQuery = CassandraUtils.generateCreateKeyspaceQuery(this.ksName, properties);
        try {
            Cluster cluster = (Cluster) GenericUtils.loadGlobalDataSource(dsName);
            if (cluster == null) {
                throw new AnalyticsException("Error establishing connection to Cassandra instance: Invalid datasource configuration");
            }
            this.session = cluster.connect();
            if (session == null) {
                throw new AnalyticsException("Error establishing connection to Cassandra instance: Failed to initialize " +
                        "client from Datasource");
            }
            this.session.execute(ksCreateQuery);
            this.session.execute("CREATE TABLE IF NOT EXISTS " + this.ksName + ".TS (tenantId INT, "
                    + "tableName VARCHAR, timestamp VARINT, id VARCHAR, PRIMARY KEY ((tenantId, tableName), timestamp))");
            this.initCommonPreparedStatements();
        } catch (DataSourceException e) {
            throw new AnalyticsException("Error establishing connection to Cassandra instance:" + e.getMessage(), e);
        }
    }
    
    private void initCommonPreparedStatements() {
        this.tableExistsStmt = this.session.prepare("SELECT columnfamily_name FROM system.schema_columnfamilies WHERE "
                + "keyspace_name = ? and columnfamily_name = ?");
        this.timestampRecordDeleteStmt = session.prepare("DELETE FROM " + 
                this.ksName + ".TS WHERE tenantId = ? AND tableName = ? and timestamp = ?");
        this.timestampRecordAddStmt = session.prepare(
                "INSERT INTO " + this.ksName + ".TS (tenantId, tableName, timestamp, id) VALUES (?, ?, ?, ?)");
    }
    
    private PreparedStatement retrieveRecordInsertStmt(String dataTable) {
        PreparedStatement stmt = (PreparedStatement) this.recordInsertStmtMap.get(dataTable);
        if (stmt == null) {
            synchronized (this.recordInsertStmtMap) {
                stmt = (PreparedStatement) this.recordInsertStmtMap.get(dataTable);
                if (stmt == null) {
                    stmt = session.prepare("INSERT INTO " + this.ksName + "." + dataTable + 
                            " (id, timestamp, data) VALUES (?, ?, ?)");
                    this.recordInsertStmtMap.put(dataTable, stmt);
                }
            }            
        }
        return stmt;
    }

    private TokenRangeRecordGroup[] calculateTokenRangeGroups(int tenantId, String tableName, List<String> columns,
                                                              int numPartitionsHint, int count) {
        Metadata md = this.session.getCluster().getMetadata();
        Set<Host> hosts = md.getAllHosts();
        int partitionsPerHost = (int) Math.ceil(numPartitionsHint / (double) hosts.size());
        List<TokenRangeRecordGroup> result = new ArrayList<TokenRangeRecordGroup>(
                partitionsPerHost * hosts.size());
        Map<CassandraTokenRange, CassandraTokenRange> tokens = new HashMap<>();
        Map<Host, Integer> hostTokenCounts = new HashMap<>();
        Map<Host, List<CassandraTokenRange>> hostTokensMap = new HashMap<>(hosts.size());
        for (Host host : hosts) {
            this.populateTokensForHost(tokens, md, host);
            hostTokenCounts.put(host, 0);
            hostTokensMap.put(host, new ArrayList<CassandraTokenRange>());
        }
        int maxTokensPerHost = tokens.size() / hosts.size();
        for (CassandraTokenRange token : tokens.values())  {
            this.assignTokenRangeToHost(token, hostTokensMap, hostTokenCounts, maxTokensPerHost);
        }
        for (Map.Entry<Host, List<CassandraTokenRange>> entry : hostTokensMap.entrySet()) {
            String ip = entry.getKey().getAddress().getHostAddress();
            List<CassandraTokenRange> hostTokens = entry.getValue();
            int partitionSize = (int) Math.ceil(hostTokens.size() / (double) partitionsPerHost);
            for (List<CassandraTokenRange> hostPartitionTokens : Lists.partition(hostTokens, partitionSize)) {
                result.add(new TokenRangeRecordGroup(tenantId, tableName, columns,
                        new ArrayList<CassandraTokenRange>(hostPartitionTokens), ip, count));
            }
        }
        return result.toArray(new TokenRangeRecordGroup[0]);
    }

    private void assignTokenRangeToHost(CassandraTokenRange token, Map<Host, List<CassandraTokenRange>> hostTokensMap,
                                        Map<Host, Integer> hostTokenCounts, int maxTokensPerHost) {
        int count;
        for (Host host : token.getHosts()) {
            count = hostTokenCounts.get(host);
            if (count < maxTokensPerHost) {
                hostTokensMap.get(host).add(token);
                hostTokenCounts.put(host, count + 1);
                return;
            }
        }
        /* all the candidate hosts counts are more than the threshold,
         * but we still need to allocate the token range to someone */
        hostTokensMap.get(token.getHosts().get(0)).add(token);
    }

    private List<CassandraTokenRange> unwrapTRAndConvertToLocalTR(Set<TokenRange> trs, Host host) {
        List<CassandraTokenRange> trsUnwrapped = new ArrayList<CassandraTokenRange>();
        for (TokenRange tr : trs) {
            for (TokenRange utr : tr.unwrap()) {
                trsUnwrapped.add(new CassandraTokenRange(utr.getStart().getValue(), utr.getEnd().getValue(), host));
            }
        }
        return trsUnwrapped;
    }

    private void populateTokensForHost(Map<CassandraTokenRange, CassandraTokenRange> tokens, Metadata md, Host host) {
        List<CassandraTokenRange> trs = this.unwrapTRAndConvertToLocalTR(md.getTokenRanges(this.ksName, host), host);
        CassandraTokenRange currentToken;
        for (CassandraTokenRange tr : trs) {
            currentToken = tokens.get(tr);
            if (currentToken != null) {
                currentToken.addHost(host);
            } else {
                tokens.put(tr, tr);
            }
        }
    }
    
    private String generateTargetDataTableName(int tenantId, String tableName) {
        return GenericUtils.generateTableUUID(tenantId, tableName);
    }

    @Override
    public synchronized void createTable(int tenantId, String tableName) throws AnalyticsException {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.ksName + "." + dataTable +
                " (id VARCHAR, timestamp BIGINT, data MAP<VARCHAR, BLOB>, PRIMARY KEY (id))");
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        ResultSet rs = this.session.execute("SELECT id, timestamp FROM " + this.ksName + 
                ".TS WHERE tenantId = ? AND tableName = ? AND timestamp >= ? AND timestamp < ?",
                tenantId, tableName, BigInteger.valueOf(timeFrom).multiply(BigInteger.valueOf(CassandraConstants.TS_MULTIPLIER)), 
                BigInteger.valueOf(timeTo).multiply(BigInteger.valueOf(CassandraConstants.TS_MULTIPLIER)));
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
        BatchStatement stmt = new BatchStatement();
        Row row;
        for (int i = 0; i < CassandraConstants.STREAMING_BATCH_SIZE && itr.hasNext(); i++) {
            row = itr.next();
            stmt.add(this.timestampRecordDeleteStmt.bind(tenantId, tableName, row.getVarint(1)));
            ids.add(row.getString(0));
        }
        this.session.execute(stmt);
        this.session.execute("DELETE FROM " + this.ksName + "." + dataTable + " WHERE id IN ?", ids);
    }
    
    @Override
    public void delete(int tenantId, String tableName, List<String> ids) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        this.deleteTSEntriesOfRecords(tenantId, tableName, dataTable, ids);
        this.session.execute("DELETE FROM " + this.ksName + "." + dataTable + " WHERE id IN ?", ids);
    }
    
    private void deleteTSEntriesOfRecords(int tenantId, String tableName, String dataTable, List<String> recordIds) {
        ResultSet rs = this.session.execute("SELECT id, timestamp FROM " + this.ksName + "." + dataTable + 
                " WHERE id IN ?", recordIds);
        List<BigInteger> tsTableTimestamps = this.extractTSTableTimestampList(rs);        
        this.deleteTSEntries(tenantId, tableName, tsTableTimestamps);
    }
    
    private void deleteTSEntries(int tenantId, String tableName, Collection<BigInteger> tsTableTimestamps) {
        BatchStatement stmt = new BatchStatement();
        for (BigInteger ts : tsTableTimestamps) {
            stmt.add(this.timestampRecordDeleteStmt.bind(tenantId, tableName, ts));
        }
        this.session.execute(stmt);
    }

    @Override
    public synchronized void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        this.session.execute("DELETE FROM " + this.ksName + ".TS WHERE tenantId = ? AND tableName = ?", tenantId, tableName);
        this.session.execute("DROP TABLE IF EXISTS " + this.ksName + "." + dataTable);
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
        if (recordsFrom > 0) {
            throw new AnalyticsException("The Cassandra connector does not support range queries with an offset: " + recordsFrom);
        }
        if (!this.tableExists(tenantId, tableName)) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        if (numPartitionsHint == 1 || !(timeFrom == Long.MIN_VALUE && timeTo == Long.MAX_VALUE)) {
            return new RecordGroup[] { new GlobalCassandraRecordGroup(tenantId, tableName, columns, timeFrom, timeTo, recordsCount) };
        } else {
            return this.calculateTokenRangeGroups(tenantId, tableName, columns, numPartitionsHint, recordsCount);
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
                recordGroup.getColumns(), recordGroup.getTokenRanges(), recordGroup.getCount());        
    }
    
    private AnalyticsIterator<Record> readRecordsByRange(GlobalCassandraRecordGroup recordGroup) throws AnalyticsException {
        int tenantId = recordGroup.getTenantId();
        String tableName = recordGroup.getTableName();
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        List<String> columns = recordGroup.getColumns();
        ResultSet rs;
        String query;
        int count = recordGroup.getCount();
        if (recordGroup.getTimeFrom() == Long.MIN_VALUE && recordGroup.getTimeTo() == Long.MAX_VALUE) {
            if (count == -1) {
                query = "SELECT id, timestamp, data FROM " + this.ksName + "." + dataTable;
            } else {
                query = "SELECT id, timestamp, data FROM " + this.ksName + "." + dataTable + " LIMIT " + count;
            }
            rs = this.session.execute(query);
            return this.lookupRecordsByDirectRS(tenantId, tableName, rs, columns);
        } else {
            if (count == -1) {
                query = "SELECT id, timestamp FROM " + this.ksName + ".TS WHERE tenantId = ? AND tableName = ? "
                        + "AND timestamp >= ? AND timestamp < ?";
            } else {
                query = "SELECT id, timestamp FROM " + this.ksName + ".TS WHERE tenantId = ? AND tableName = ? "
                        + "AND timestamp >= ? AND timestamp < ? LIMIT " + count;
            }
            ResultSet tsrs = this.session.execute(query, tenantId, tableName,
                    BigInteger.valueOf(recordGroup.getTimeFrom()).multiply(BigInteger.valueOf(CassandraConstants.TS_MULTIPLIER)),
                    BigInteger.valueOf(recordGroup.getTimeTo()).multiply(BigInteger.valueOf(CassandraConstants.TS_MULTIPLIER)));
            return new CassandraRecordTSBasedIDDataIterator(tenantId, tableName, tsrs.iterator(), columns);
        }
    }
    
    private AnalyticsIterator<Record> readRecordsByIds(GlobalCassandraRecordGroup recordGroup) throws AnalyticsException {
        return this.lookupRecordsByIds(recordGroup.getTenantId(), 
                recordGroup.getTableName(), recordGroup.getIds(), recordGroup.getColumns());
    }
    
    private AnalyticsIterator<Record> lookupRecordsByIds(int tenantId, String tableName, List<String> ids, List<String> columns) {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        ResultSet rs = this.session.execute("SELECT id, timestamp, data FROM " + this.ksName + 
                "." + dataTable + " WHERE id IN ?", ids);
        return this.lookupRecordsByDirectRS(tenantId, tableName, rs, columns);
    }
    
    private AnalyticsIterator<Record> lookupRecordsByTokenRanges(int tenantId, String tableName, 
            List<String> columns, List<CassandraTokenRange> tokenRanges, int count) {
        return new CassandraTokenRangeDataIterator(tenantId, tableName, columns, tokenRanges, count);
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
    public boolean isRecordCountSupported() {
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
        return (BigInteger.valueOf(timestamp).multiply(BigInteger.valueOf(CassandraConstants.TS_MULTIPLIER))).add(
                BigInteger.valueOf(Math.abs(id.hashCode() % CassandraConstants.TS_MULTIPLIER)));
    }
    
    private void addBatch(List<Record> batch) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Record firstRecord = batch.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        try {
            //this.deleteTSEntries(tenantId, tableName, dataTable, this.extractRecordIds(batch));
            this.addRawRecordBatch(dataTable, batch);
            this.addTSRecordBatch(tenantId, tableName, batch);
        } catch (Exception e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in adding record batch: " + e.getMessage(), e);
            }
        }
    }
    
    private void addRawRecordBatch(String dataTable, List<Record> batch) {
        PreparedStatement ps = this.retrieveRecordInsertStmt(dataTable);
        BatchStatement stmt = new BatchStatement();
        for (Record record : batch) {
            stmt.add(ps.bind(record.getId(), record.getTimestamp(), this.getDataMapFromValues(record.getValues())));
        }
        this.session.execute(stmt);
    }
        
    private void addTSRecordBatch(int tenantId, String tableName, List<Record> batch) {
        BatchStatement stmt = new BatchStatement();
        for (Record record : batch) {
            stmt.add(this.timestampRecordAddStmt.bind(tenantId, tableName, this.toTSTableTimestamp(record.getTimestamp(), 
                    record.getId()), record.getId()));
        }
        this.session.execute(stmt);
    }

    private boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);  
        Statement stmt = this.tableExistsStmt.bind(this.ksName.toLowerCase(), dataTable.toLowerCase());
        ResultSet rs = this.session.execute(stmt);
        return rs.iterator().hasNext();
    }
    
    /**
     * Cassandra data {@link AnalyticsIterator} implementation for streaming, which reads data
     * using the given token ranges.
     */
    public class CassandraTokenRangeDataIterator implements AnalyticsIterator<Record> {
        
        private int tenantId;
        
        private String tableName;
        
        private List<String> columns;
        
        private Iterator<CassandraTokenRange> tokenRangesItr;
                
        private AnalyticsIterator<Record> dataItr;
        
        private int count;
        
        private int dataPointer;
        
        public CassandraTokenRangeDataIterator(int tenantId, String tableName, List<String> columns,
                List<CassandraTokenRange> tokenRanges, int count) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.tokenRangesItr = tokenRanges.iterator();
            this.count = count;
        }
        
        private void populateNextTokenRangeData() {
            String dataTable = generateTargetDataTableName(tenantId, tableName);
            CassandraTokenRange tokenRange = this.tokenRangesItr.next();
            String query;
            if (this.count == -1) {
                query = "SELECT id, timestamp, data FROM " + ksName + "." + dataTable + 
                        " WHERE token(id) > ? and token(id) <= ?";
            } else {
                query = "SELECT id, timestamp, data FROM " + ksName + "." + dataTable + 
                        " WHERE token(id) > ? and token(id) <= ? LIMIT " + this.count;
            }
            ResultSet rs = session.execute(query, tokenRange.getStart(), tokenRange.getEnd());
            this.dataItr = lookupRecordsByDirectRS(this.tenantId, this.tableName, rs, this.columns);
        }

        @Override
        public boolean hasNext() {
            if (this.count != -1 && this.dataPointer >= this.count) {
                return false;
            }
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
                this.dataPointer++;
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

        protected int tenantId;
        
        protected String tableName;
                
        protected List<String> columns;
        
        protected Iterator<Row> resultSetItr;
        
        protected AnalyticsIterator<Record> dataItr;
        
        public CassandraRecordIDDataIterator(int tenantId, String tableName, Iterator<Row> resultSetItr, List<String> columns) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.resultSetItr = resultSetItr;
        }
        
        protected void populateBatch() {
            List<String> ids = new ArrayList<String>(CassandraConstants.STREAMING_BATCH_SIZE);
            Row row;
            for (int i = 0; i < CassandraConstants.STREAMING_BATCH_SIZE && this.resultSetItr.hasNext(); i++) {
                row = this.resultSetItr.next();
                ids.add(row.getString(0));
            }
            String dataTable = generateTargetDataTableName(this.tenantId, this.tableName);
            ResultSet rs = session.execute("SELECT id, timestamp, data FROM " + ksName + "." + dataTable + " WHERE id IN ?", ids);
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
     * {@link CassandraRecordIDDataIterator} based variant for considering indexed timestamp values
     * also for doing read-repair for duplicated timestamps in the index.
     */
    public class CassandraRecordTSBasedIDDataIterator extends CassandraRecordIDDataIterator {
        
        public CassandraRecordTSBasedIDDataIterator(int tenantId, String tableName, 
                Iterator<Row> resultSetItr, List<String> columns) {
            super(tenantId, tableName, resultSetItr, columns);
        }
        
        @Override
        protected void populateBatch() {
            MultiMap tsIds = new MultiHashMap(CassandraConstants.STREAMING_BATCH_SIZE);
            Row row;
            for (int i = 0; i < CassandraConstants.STREAMING_BATCH_SIZE && this.resultSetItr.hasNext(); i++) {
                row = this.resultSetItr.next();
                tsIds.put(row.getString(0), row.getVarint(1));
            }
            String dataTable = generateTargetDataTableName(this.tenantId, this.tableName);
            ResultSet rs = session.execute("SELECT id, timestamp, data FROM " + ksName + "." + dataTable + " WHERE id IN ?", tsIds.keySet());
            this.dataItr = new CassandraTSValidatingDirectDataIterator(this.tenantId, this.tableName, rs.iterator(), this.columns, tsIds);
        }
        
    }
    
    /**
     * Cassandra data {@link AnalyticsIterator} implementation for streaming, which reads data
     * directly from the data table.
     */
    public class CassandraDirectDataIterator implements AnalyticsIterator<Record> {

        protected int tenantId;
        
        protected String tableName;
        
        private Set<String> columns;
        
        private Iterator<Row> resultSetItr;
        
        public CassandraDirectDataIterator(int tenantId, String tableName, Iterator<Row> resultSetItr, List<String> columns) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            if (columns != null) {
                this.columns = new HashSet<String>(columns);
            }
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
                for (Map.Entry<String, ByteBuffer> binaryValue : binaryValues.entrySet()) {
                    if (this.columns.contains(binaryValue.getKey())) {
                        values.put(binaryValue.getKey(), GenericUtils.deserializeObject(this.extractBytes(binaryValue.getValue())));
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
     * {@link CassandraDirectDataIterator} based iterator which does timestamp based
     * read-repair operations for data records lookups.
     */
    public class CassandraTSValidatingDirectDataIterator extends CassandraDirectDataIterator {
        
        private MultiMap tsIds;
        
        private Record current;
        
        public CassandraTSValidatingDirectDataIterator(int tenantId, String tableName, Iterator<Row> resultSetItr, 
                List<String> columns, MultiMap tsIds) {
            super (tenantId, tableName, resultSetItr, columns);
            this.tsIds = tsIds;
        }
        
        @Override
        public boolean hasNext() {
            if (this.current != null) {
                return true;
            }
            while (super.hasNext()) {
                this.current = this.next();
                if (this.current != null) {
                    break;
                }
            }
            return this.current != null;
        }
        
        @Override
        public Record next() {
            if (this.current != null) {
                Record result = this.current;
                this.current = null;
                return result;
            } else {
                return this.validateRecordAndReturn(super.next());
            }
        }
        
        private Record validateRecordAndReturn(Record record) {
            BigInteger targetTS = toTSTableTimestamp(record.getTimestamp(), record.getId());
            @SuppressWarnings("unchecked")
            Collection<BigInteger> tss = (Collection<BigInteger>) tsIds.get(record.getId());
            boolean removed = tss.remove(targetTS);
            if (!tss.isEmpty()) {
                this.removeTSEntries(tss);
            }
            if (removed) {
                return record;
            } else {
                return null;
            }
        }
        
        private void removeTSEntries(Collection<BigInteger> tss) {
            deleteTSEntries(this.tenantId, this.tableName, tss);
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
        
        private int count;
                
        public GlobalCassandraRecordGroup(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo, int count) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.timeFrom = timeFrom;
            this.timeTo = timeTo;
            this.count = count;
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
        
        public int getCount() {
            return count;
        }
        
    }

}
