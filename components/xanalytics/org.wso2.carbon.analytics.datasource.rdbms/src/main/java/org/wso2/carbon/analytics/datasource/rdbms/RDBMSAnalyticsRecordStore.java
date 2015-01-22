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
package org.wso2.carbon.analytics.datasource.rdbms;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.DirectAnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * Abstract RDBMS database backed implementation of {@link AnalyticsRecordStore}.
 */
public class RDBMSAnalyticsRecordStore extends DirectAnalyticsRecordStore {
    
    private static final String ANALYTICS_USER_TABLE_PREFIX = "ANX";

    private static final String RECORD_IDS_PLACEHOLDER = "{{RECORD_IDS}}";

    private static final String TABLE_NAME_PLACEHOLDER = "{{TABLE_NAME}}";

    private DataSource dataSource;
    
    private Map<String, String> properties;
    
    private RDBMSQueryConfigurationEntry rDBMSQueryConfigurationEntry;
    
    public RDBMSAnalyticsRecordStore() throws AnalyticsException {
        this.rDBMSQueryConfigurationEntry = null;
    }
    
    public RDBMSAnalyticsRecordStore(RDBMSQueryConfigurationEntry rDBMSQueryConfigurationEntry) {
        this.rDBMSQueryConfigurationEntry = rDBMSQueryConfigurationEntry;
    }
    
    @Override
    public void init(Map<String, String> properties)
            throws AnalyticsException {
        this.properties = properties;
        String dsName = properties.get(RDBMSAnalyticsDSConstants.DATASOURCE);
        if (dsName == null) {
            throw new AnalyticsException("The property '" + 
                    RDBMSAnalyticsDSConstants.DATASOURCE + "' is required");
        }
        try {
            this.dataSource = (DataSource) InitialContext.doLookup(dsName);
        } catch (NamingException e) {
            throw new AnalyticsException("Error in looking up data source: " + 
                    e.getMessage(), e);
        }
        if (this.rDBMSQueryConfigurationEntry == null) {
            this.rDBMSQueryConfigurationEntry = RDBMSUtils.lookupCurrentQueryConfigurationEntry(this.dataSource);
        }
    }
    
    public RDBMSQueryConfigurationEntry getQueryConfiguration() {
        return rDBMSQueryConfigurationEntry;
    }
    
    private String[] getRecordTableInitQueries(int tenantId, String tableName) {
        String[] queries = this.getQueryConfiguration().getRecordTableInitQueries();
        String[] result = new String[queries.length];
        for (int i = 0; i < queries.length; i++) {
            result[i] = this.translateQueryWithTableInfo(queries[i], tenantId, tableName);
        }
        return result;
    }
    
    private String[] getRecordTableDeleteQueries(int tenantId, String tableName) {
        String[] queries = this.getQueryConfiguration().getRecordTableDeleteQueries();
        String[] result = new String[queries.length];
        for (int i = 0; i < queries.length; i++) {
            result[i] = this.translateQueryWithTableInfo(queries[i], tenantId, tableName);
        }
        return result;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    private Connection getConnection() throws SQLException {
        return this.getConnection(true);
    }
    
    private Connection getConnection(boolean autoCommit) throws SQLException {
        Connection conn = this.getDataSource().getConnection();
        conn.setAutoCommit(autoCommit);
        return conn;
    }
    
    private String calculateRecordIdentity(Record record) {
        return this.generateTargetTableName(record.getTenantId(), record.getTableName());
    }

    @Override
    public void update(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            Map<String, List<Record>> recordBatches = this.generateRecordBatches(records);
            for (List<Record> batch : recordBatches.values()) {
                this.deleteRecordsSimilar(conn, batch);
            }
            for (List<Record> batch : recordBatches.values()) {
                this.addRecordsSimilar(conn, batch);
            }
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsException("Error in updating records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private Map<String, List<Record>> generateRecordBatches(List<Record> records) {
        /* if the records have identities (unique table category and name) as the following
         * "ABABABCCAACBDABCABCDBAC", the job of this method is to make it like the following,
         * {"AAAAAAAA", "BBBBBBB", "CCCCCC", "DD" } */
        Map<String, List<Record>> recordBatches = new HashMap<String, List<Record>>();
        List<Record> recordBatch;
        for (Record record : records) {
            recordBatch = recordBatches.get(this.calculateRecordIdentity(record));
            if (recordBatch == null) {
                recordBatch = new ArrayList<Record>();
                recordBatches.put(this.calculateRecordIdentity(record), recordBatch);
            }
            recordBatch.add(record);
        }
        return recordBatches;
    }
    
    @Override
    public void insert(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {        
        if (records.size() == 0) {
            return;
        }
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            Map<String, List<Record>> recordBatches = this.generateRecordBatches(records);
            for (List<Record> batch : recordBatches.values()) {
                this.addRecordsSimilar(conn, batch);
            }
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsException("Error in adding records: " + e.getMessage(), e);
        } catch (AnalyticsException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw e;
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private void addRecordsSimilar(Connection conn, 
            List<Record> records) throws SQLException, 
            AnalyticsException, AnalyticsTableNotAvailableException {
        Record firstRecord = records.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        String query = this.getRecordInsertSQL(tenantId, tableName);
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            for (Record record : records) {
                stmt.setString(1, record.getId());
                stmt.setLong(2, record.getTimestamp());
                stmt.setBlob(3, new ByteArrayInputStream(GenericUtils.encodeRecordValues(record.getValues())));
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw e;
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    private String getRecordInsertSQL(int tenantId, String tableName) {
    	String query = this.getQueryConfiguration().getRecordInsertQuery();
    	return translateQueryWithTableInfo(query, tenantId, tableName);
    }

    @Override
    public Iterator<Record> getRecords(int tenantId, String tableName, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(this.getRecordRetrievalQuery(tenantId, tableName));
            if (timeFrom == -1) {
                timeFrom = Long.MIN_VALUE;
            }
            if (timeTo == -1) {
                timeTo = Long.MAX_VALUE;
            }
            if (recordsFrom == -1) {
                recordsFrom = 0;
            }
            if (recordsCount == -1) {
                recordsCount = Integer.MAX_VALUE;
            }
            stmt.setLong(1, timeFrom);
            stmt.setLong(2, timeTo);
            stmt.setInt(3, this.adjustRecordsFromForProvider(recordsFrom));
            stmt.setInt(4, this.adjustRecordsCountForProvider(recordsFrom, recordsCount));            
            rs = stmt.executeQuery();
            return new RDBMSResultSetIterator(tenantId, tableName, columns, conn, stmt, rs);
        } catch (SQLException e) {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in retrieving records: " + e.getMessage(), e);
            }            
        }
    }
    
    private int adjustRecordsFromForProvider(int recordsFrom) {
        if (!this.getQueryConfiguration().isPaginationFirstZeroIndexed()) {
            recordsFrom++;
        }
        if (!this.getQueryConfiguration().isPaginationFirstInclusive()) {
            recordsFrom++;
        }
        return recordsFrom;
    }
    
    private int adjustRecordsCountForProvider(int recordsFrom, int recordsCount) {
        if (!this.getQueryConfiguration().isPaginationSecondLength() && recordsCount != Integer.MAX_VALUE) {
            if (!this.getQueryConfiguration().isPaginationSecondZeroIndexed()) {
                recordsCount++;
            }
            if (!this.getQueryConfiguration().isPaginationSecondInclusive()) {
                recordsCount++;
            }
        }
        return recordsCount;
    }

    @Override
    public Iterator<Record> getRecords(int tenantId, String tableName, List<String> columns,
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        String recordGetSQL = this.generateGetRecordRetrievalWithIdQuery(tenantId, tableName, ids.size());
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(recordGetSQL);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i));
            }
            rs = stmt.executeQuery();
            return new RDBMSResultSetIterator(tenantId, tableName, columns, conn, stmt, rs);
        } catch (SQLException e) {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in retrieving records: " + e.getMessage(), e);
            }
        }
    }
    
    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        String sql = this.getRecordDeletionQuery(tenantId, tableName);
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            if (timeFrom == -1) {
                timeFrom = Long.MIN_VALUE;
            }
            if (timeTo == -1) {
                timeTo = Long.MAX_VALUE;
            }
            stmt.setLong(1, timeFrom);
            stmt.setLong(2, timeTo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in deleting records: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
        
    @Override
    public void delete(int tenantId, String tableName, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (ids.size() == 0) {
            return;
        }
        Connection conn = null;
        try {
            conn = this.getConnection();
            this.delete(conn, tenantId, tableName, ids);
        } catch (SQLException e) {
            throw new AnalyticsException("Error in deleting records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private void deleteRecordsSimilar(Connection conn, List<Record> records) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (records.size() == 0) {
            return;
        }
        Record firstRecord = records.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        List<String> ids = new ArrayList<String>(records.size());
        for (Record record : records) {
            ids.add(record.getId());
        }
        this.delete(conn, tenantId, tableName, ids);
    }
    
    private void delete(Connection conn, int tenantId, String tableName, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        String sql = this.generateRecordDeletionRecordsWithIdsQuery(tenantId, tableName, ids.size());
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in deleting records: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    private String generateTablePrefix(int tenantId) {
        if (tenantId < 0) {
            return RDBMSAnalyticsRecordStore.ANALYTICS_USER_TABLE_PREFIX + "_X" + Math.abs(tenantId) + "_";
        } else {
            return RDBMSAnalyticsRecordStore.ANALYTICS_USER_TABLE_PREFIX + "_" + tenantId + "_";
        }
    }
    
    private String generateTargetTableName(int tenantId, String tableName) {
        return this.normalizeTableName(this.generateTablePrefix(tenantId) + tableName);
    }
    
    private String translateQueryWithTableInfo(String query, int tenantId, String tableName) {
        return query.replace(TABLE_NAME_PLACEHOLDER, this.generateTargetTableName(tenantId, tableName));
    }
    
    private String translateQueryWithRecordIdsInfo(String query, int recordCount) {
        return query.replace(RECORD_IDS_PLACEHOLDER, this.getDynamicSQLParams(recordCount));
    }
    
    private String getRecordRetrievalQuery(int tenantId, String tableName) {
        String query = this.getQueryConfiguration().getRecordRetrievalQuery();
        return this.translateQueryWithTableInfo(query, tenantId, tableName);
    }
    
    private String generateGetRecordRetrievalWithIdQuery(int tenantId, String tableName, int recordCount) {
        String query = this.getQueryConfiguration().getRecordRetrievalWithIdsQuery();
        query = this.translateQueryWithTableInfo(query, tenantId, tableName);
        query = this.translateQueryWithRecordIdsInfo(query, recordCount);
        return query;
    }
    
    private String generateRecordDeletionRecordsWithIdsQuery(int tenantId, String tableName, int recordCount) {
        String query = this.getQueryConfiguration().getRecordDeletionWithIdsQuery();
        query = this.translateQueryWithTableInfo(query, tenantId, tableName);
        query = this.translateQueryWithRecordIdsInfo(query, recordCount);
        return query;
    }
    
    private String getDynamicSQLParams(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                builder.append("?");
            } else {
                builder.append(",?");
            }
        }
        return builder.toString();
    }
    
    private String getRecordDeletionQuery(int tenantId, String tableName) {
        String query = this.getQueryConfiguration().getRecordDeletionQuery();
        return this.translateQueryWithTableInfo(query, tenantId, tableName);
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            String[] tableInitQueries = this.getRecordTableDeleteQueries(tenantId, tableName);
            for (String query : tableInitQueries) {
                query = this.translateQueryWithTableInfo(query, tenantId, tableName);
                this.executeUpdate(conn, query);
            }
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            if (this.tableExists(tenantId, tableName)) {
                throw new AnalyticsException("Error in deleting table: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            String[] tableInitQueries = this.getRecordTableInitQueries(tenantId, tableName);
            for (String query : tableInitQueries) {
                this.executeUpdate(conn, query);
            }
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsException("Error in creating table: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private void executeUpdate(Connection conn, String query) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    @SuppressWarnings("resource")
    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        tableName = this.normalizeTableName(tableName);
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            DatabaseMetaData dbm = conn.getMetaData();
            String prefix = this.normalizeTableName(this.generateTablePrefix(tenantId));
            String srcTable;
            rs = dbm.getTables(null, null, "%", null);
            while (rs.next()) {
                srcTable = rs.getString("TABLE_NAME");
                if (srcTable.startsWith(prefix)) {
                    srcTable = srcTable.substring(prefix.length());
                    srcTable = this.normalizeTableName(srcTable);
                }
                if (tableName.equals(srcTable)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new AnalyticsException("Error in checking table existence: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, null, conn);
        }
    }

    private String normalizeTableName(String tableName) {
        return tableName.toUpperCase();
    }
    
    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        List<String> result = new ArrayList<String>();
        Connection conn = null;
        ResultSet rs = null;
        String tableName;
        String prefix = this.normalizeTableName(this.generateTablePrefix(tenantId));
        try {
            conn = this.getConnection();
            DatabaseMetaData dbm = conn.getMetaData();
            rs = dbm.getTables(null, null, "%", null);
            while (rs.next()) {
                tableName = rs.getString("TABLE_NAME");
                if (tableName.startsWith(prefix)) {
                    tableName = tableName.substring(prefix.length());
                    tableName = this.normalizeTableName(tableName);
                    result.add(tableName);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new AnalyticsException("Error in listing tables: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, null, conn);
        }
    }

    private String getRecordCountQuery(int tenantId, String tableName) {
        String query = this.getQueryConfiguration().getRecordCountQuery();
        return this.translateQueryWithTableInfo(query, tenantId, tableName);
    }
    
    private String printableTableName(int tenantId, String tableName) {
        return "[" + tenantId + ":" + tableName + "]";
    }
    
    @Override
    public long getRecordCount(int tenantId, String tableName) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        String recordCountQuery = this.getRecordCountQuery(tenantId, tableName);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(recordCountQuery);
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new AnalyticsException("Record count not available for " + 
                        printableTableName(tenantId, tableName));
            }
        } catch (SQLException e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            }
            throw new AnalyticsException("Error in retrieving record count: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }
    
    /**
     * This class represents the RDBMS result set iterator, which will stream the result records out.
     */
    private class RDBMSResultSetIterator implements Iterator<Record> {

        private int tenantId;
        
        private String tableName;
        
        private List<String> columns;
        
        private Connection conn;
        
        private Statement stmt;
        
        private ResultSet rs;
        
        private Record nextValue;
        
        private boolean prefetched;
        
        public RDBMSResultSetIterator(int tenantId, String tableName, List<String> columns, 
                Connection conn, Statement stmt, ResultSet rs) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.columns = columns;
            this.conn = conn;
            this.stmt = stmt;
            this.rs = rs;
        }
        
        @Override
        public boolean hasNext() {
            if (!this.prefetched) {
                this.nextValue = this.next();
                this.prefetched = true;
            }
            return nextValue != null;
        }

        @Override
        public Record next() {
            if (this.prefetched) {
                this.prefetched = false;
                Record result = this.nextValue;
                this.nextValue = null;
                return result;
            }
            Set<String> colSet = null;
            if (this.columns != null && this.columns.size() > 0) {
                colSet = new HashSet<String>(this.columns);
            }
            try {
                if (this.rs.next()) {
                    Blob blob = this.rs.getBlob(3);
                    Map<String, Object> values = GenericUtils.decodeRecordValues(blob.getBytes(1, (int) blob.length()), colSet);
                    return new Record(this.rs.getString(1), this.tenantId, this.tableName, values, this.rs.getLong(2));                
                } else {
                    /* end of the result set, time to clean up.. */
                    RDBMSUtils.cleanupConnection(this.rs, this.stmt, this.conn);
                    return null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @Override
        public void remove() {
            /* this is a read-only iterator, nothing will be removed */
        }
        
    }

}
