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

import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.ndatasource.common.DataSourceException;

import javax.sql.DataSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract RDBMS database backed implementation of {@link AnalyticsRecordStore}.
 */
public class RDBMSAnalyticsRecordStore implements AnalyticsRecordStore {
    
    private static final String ANALYTICS_USER_TABLE_PREFIX = "ANX";

    private static final String RECORD_IDS_PLACEHOLDER = "{{RECORD_IDS}}";

    private static final String TABLE_NAME_PLACEHOLDER = "{{TABLE_NAME}}";
    
    private DataSource dataSource;
    
    private Map<String, String> properties;
    
    private RDBMSQueryConfigurationEntry rdbmsQueryConfigurationEntry;
    
    public RDBMSAnalyticsRecordStore() throws AnalyticsException {
        this.rdbmsQueryConfigurationEntry = null;
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
            this.dataSource = (DataSource) GenericUtils.loadGlobalDataSource(dsName);
        } catch (DataSourceException e) {
            throw new AnalyticsException("Error in loading data source: " + e.getMessage(), e);
        }
        if (this.rdbmsQueryConfigurationEntry == null) {
            this.rdbmsQueryConfigurationEntry = RDBMSUtils.lookupCurrentQueryConfigurationEntry(this.dataSource);
        }
    }
        
    public RDBMSQueryConfigurationEntry getQueryConfiguration() {
        return rdbmsQueryConfigurationEntry;
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
    
    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {        
        if (records.size() == 0) {
            return;
        }
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            Collection<List<Record>> recordBatches = GenericUtils.generateRecordBatches(records);
            for (List<Record> batch : recordBatches) {
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
        String mergeSQL = this.getRecordMergeSQL(tenantId, tableName);
        if (mergeSQL != null) {
            this.mergeRecordsSimilar(conn, records, tenantId, tableName, mergeSQL);
        } else {
            this.insertAndUpdateRecordsSimilar(conn, records, tenantId, tableName);
        }
    }
    
    private void populateStatementForAdd(PreparedStatement stmt, 
            Record record) throws SQLException, AnalyticsException {        
        stmt.setLong(1, record.getTimestamp());
        stmt.setBinaryStream(2, new ByteArrayInputStream(GenericUtils.encodeRecordValues(record.getValues())));
        stmt.setString(3, record.getId());
    }
    
    private void mergeRecordsSimilar(Connection conn, 
            List<Record> records, int tenantId, String tableName, String query) 
            throws SQLException, AnalyticsException, AnalyticsTableNotAvailableException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            for (Record record : records) {
                this.populateStatementForAdd(stmt, record);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            if (!this.tableExists(conn, tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw e;
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    private void insertAndUpdateRecordsSimilar(Connection conn, 
            List<Record> records, int tenantId, String tableName) throws SQLException, 
            AnalyticsException, AnalyticsTableNotAvailableException {
        try {
            this.insertBatchRecordsSimilar(conn, records, tenantId, tableName);
        } catch (SQLException e) {
            /* batch insert failed, maybe because one of the records were already there,
             * lets try to sequentially insert/update */
            this.insertAndUpdateRecordsSimilarSequentially(conn, records, tenantId, tableName);
        } catch (AnalyticsException e) {
            throw e;
        }
    }
    
    private void insertAndUpdateRecordsSimilarSequentially(Connection conn, 
            List<Record> records, int tenantId, String tableName) throws SQLException, AnalyticsException {
        String insertQuery = this.getRecordInsertSQL(tenantId, tableName);
        String updateQuery = this.getRecordUpdateSQL(tenantId, tableName);
        PreparedStatement stmt = null;
        stmt = conn.prepareStatement(insertQuery);
        for (Record record : records) {            
            this.populateStatementForAdd(stmt, record);
            try {
                stmt.executeUpdate();
            } catch (SQLException e) {
                /* maybe the record is already there, lets try to update */
                stmt.close();
                stmt = conn.prepareStatement(updateQuery);
                this.populateStatementForAdd(stmt, record);
                stmt.executeUpdate();
            }
        }        
    }
    
    private void insertBatchRecordsSimilar(Connection conn, 
            List<Record> records, int tenantId, String tableName) throws SQLException, 
            AnalyticsException, AnalyticsTableNotAvailableException {
        String query = this.getRecordInsertSQL(tenantId, tableName);
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            for (Record record : records) {
                this.populateStatementForAdd(stmt, record);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            if (!this.tableExists(conn, tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw e;
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    private String getRecordMergeSQL(int tenantId, String tableName) {
    	String query = this.getQueryConfiguration().getRecordMergeQuery();
    	return translateQueryWithTableInfo(query, tenantId, tableName);
    }
    
    private String getRecordInsertSQL(int tenantId, String tableName) {
        String query = this.getQueryConfiguration().getRecordInsertQuery();
        return translateQueryWithTableInfo(query, tenantId, tableName);
    }
    
    private String getRecordUpdateSQL(int tenantId, String tableName) {
        String query = this.getQueryConfiguration().getRecordUpdateQuery();
        return translateQueryWithTableInfo(query, tenantId, tableName);
    }
    
    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, 
            List<String> ids) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        return new RDBMSIDsRecordGroup[] { new RDBMSIDsRecordGroup(tenantId, tableName, columns, ids) };
    }
    
    private List<Integer[]> generatePartitionPlan(int tenantId, String tableName, 
            int numPartitionsHint, int recordsFrom, int recordsCount) throws AnalyticsException, 
            AnalyticsTableNotAvailableException {
        List<Integer[]> result = new ArrayList<Integer[]>();
        int recordsCountAll = (int) this.getRecordCount(tenantId, tableName, Long.MIN_VALUE, Long.MAX_VALUE);
        if (recordsCount == -1) {
            recordsCount = recordsCountAll;
        } else if (recordsCount > recordsCountAll) {
            recordsCount = recordsCountAll;
        }
        if (recordsCount == 0 || numPartitionsHint < 1) {
            return new ArrayList<Integer[]>(0);
        }
        int batchSize = (int) Math.ceil(recordsCount / (double) numPartitionsHint);
        int i;
        for (long l = 0; l < recordsCount; l += batchSize) {
            /* this is to avoid integer overflow and getting minus values for counter */
            i = (int) l;
            result.add(new Integer[] { recordsFrom + i, 
                    (i + batchSize) > recordsCount ? recordsCount - i : batchSize });
        }
        return result;
    }
    
    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, int recordsCount)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        List<Integer[]> params = this.generatePartitionPlan(tenantId, tableName, numPartitionsHint, 
                recordsFrom, recordsCount);
        RDBMSRangeRecordGroup[] result = new RDBMSRangeRecordGroup[params.size()];
        Integer[] param;
        for (int i = 0; i < result.length; i++) {
            param = params.get(i);
            result[i] = new RDBMSRangeRecordGroup(tenantId, tableName, columns, timeFrom, timeTo, param[0], param[1]);
        }
        return result;
    }

    @Override
    public AnalyticsIterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        if (recordGroup instanceof RDBMSRangeRecordGroup) {
            RDBMSRangeRecordGroup recordRangeGroup = (RDBMSRangeRecordGroup) recordGroup;
            return this.getRecords(recordRangeGroup.getTenantId(), recordRangeGroup.getTableName(), 
                    recordRangeGroup.getColumns(), recordRangeGroup.getTimeFrom(), 
                    recordRangeGroup.getTimeTo(), recordRangeGroup.getRecordsFrom(), 
                    recordRangeGroup.getRecordsCount());
        } else if (recordGroup instanceof RDBMSIDsRecordGroup) {
            RDBMSIDsRecordGroup recordIdGroup = (RDBMSIDsRecordGroup) recordGroup;
            return this.getRecords(recordIdGroup.getTenantId(), recordIdGroup.getTableName(), 
                    recordIdGroup.getColumns(), recordIdGroup.getIds());
        } else {
            throw new AnalyticsException("Invalid RDBMS RecordGroup implementation: " + recordGroup.getClass());
        }
    }
    
    public AnalyticsIterator<Record> getRecords(int tenantId, String tableName, List<String> columns,
                                                long timeFrom, long timeTo, int recordsFrom,
                                                int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(this.getRecordRetrievalQuery(tenantId, tableName));
            if (recordsCount == -1) {
                recordsCount = Integer.MAX_VALUE;
            }
            stmt.setLong(1, timeFrom);
            stmt.setLong(2, timeTo);
            int[] paginationIndices = this.calculateIndicesForPaginationMode(recordsFrom, recordsCount);
            stmt.setInt(3, paginationIndices[0]);
            stmt.setInt(4, paginationIndices[1]);            
            rs = stmt.executeQuery();
            return new RDBMSResultSetIterator(tenantId, tableName, columns, conn, stmt, rs);
        } catch (SQLException e) {
            if (conn != null && !this.tableExists(conn, tenantId, tableName)) {
                RDBMSUtils.cleanupConnection(rs, stmt, conn);
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                RDBMSUtils.cleanupConnection(rs, stmt, conn);
                throw new AnalyticsException("Error in retrieving records: " + e.getMessage(), e);
            }            
        }
    }
    
    private int[] calculateIndicesForPaginationMode(int recordsFrom, int recordsCount) {
        switch (this.rdbmsQueryConfigurationEntry.getPaginationMode()) {
        case MODE1:
            /* MySQL, H2, MSSQL 2012 like */
            return new int[] { recordsFrom, recordsCount };
        case MODE2:
            /* Oracle, MSSQL ROWNUM like */
            return new int[] { recordsFrom, recordsFrom + recordsCount };
        default:
            throw new IllegalArgumentException("Invalid pagination mode: " + 
                    this.rdbmsQueryConfigurationEntry.getPaginationMode());
        }
    }
    
    public AnalyticsIterator<Record> getRecords(int tenantId, String tableName, List<String> columns,
                                                List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (ids.isEmpty()) {
            return new EmptyResultSetAnalyticsIterator();
        }
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
            if (conn != null && !this.tableExists(conn, tenantId, tableName)) {
                RDBMSUtils.cleanupConnection(rs, stmt, conn);
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                RDBMSUtils.cleanupConnection(rs, stmt, conn);
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
            stmt.setLong(1, timeFrom);
            stmt.setLong(2, timeTo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (conn != null && !this.tableExists(conn, tenantId, tableName)) {
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

    @Override
    public void destroy() throws AnalyticsException {
        /* do nothing */
    }

    private void delete(Connection conn, int tenantId, String tableName, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        String sql = this.generateRecordDeletionRecordsWithIdsQuery(tenantId, tableName, ids.size());
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (!this.tableExists(conn, tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in deleting records: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    /**
     * This method is used to generate an UUID from the target table name, to make sure, it is a compact
     * name that can be fitted in all the supported RDBMSs. For example, Oracle has a table name
     * length of 30. So we must translate source table names to hashed strings, which here will have
     * a very low probability of clashing.
     */
    private String generateTableUUID(int tenantId, String tableName) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(byteOut);
            dout.writeInt(tenantId);
            /* we've to limit it to 64 bits */
            dout.writeInt(tableName.hashCode());
            dout.close();
            byteOut.close();
            String result = Base64.encode(byteOut.toByteArray());
            result = result.replace('=', '_');
            result = result.replace('+', '_');
            result = result.replace('/', '_');
            /* a table name must start with a letter */
            return ANALYTICS_USER_TABLE_PREFIX + result;
        } catch (IOException e) {
            /* this will never happen */
            throw new RuntimeException(e);
        }
    }
    
    private String generateTargetTableName(int tenantId, String tableName) {
        return this.generateTableUUID(tenantId, tableName);
    }
    
    private String translateQueryWithTableInfo(String query, int tenantId, String tableName) {
        if (query == null) {
            return null;
        }
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
            conn = this.getConnection();
            Map<String, Object[]> queries = new HashMap<String, Object[]>();
            String[] tableInitQueries = this.getRecordTableDeleteQueries(tenantId, tableName);
            queries.putAll(RDBMSUtils.generateNoParamQueryMap(tableInitQueries));
            RDBMSUtils.executeAllUpdateQueries(conn, queries);
        } catch (SQLException | AnalyticsException e) {
            if (conn == null || this.tableExists(conn, tenantId, tableName)) {
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
            conn = this.getConnection();
            String[] tableInitQueries = this.getRecordTableInitQueries(tenantId, tableName);
            Map<String, Object[]> queries = RDBMSUtils.generateNoParamQueryMap(tableInitQueries);
            RDBMSUtils.executeAllUpdateQueries(conn, queries);
        } catch (SQLException | AnalyticsException e) {
            if (conn == null || !this.tableExists(conn, tenantId, tableName)) {
                throw new AnalyticsException("Error in creating table: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private String getRecordTableCheckQuery(int tenantId, String tableName) {
        String query = this.getQueryConfiguration().getRecordTableCheckQuery();
        return this.translateQueryWithTableInfo(query, tenantId, tableName);
    }
    
    private boolean tableExists(Connection conn, int tenantId, String tableName) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String query = this.getRecordTableCheckQuery(tenantId, tableName);
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, null);
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
    public boolean isPaginationSupported() {
        /* pagination is supported */
        return true;
    }
    
    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        String recordCountQuery = this.getRecordCountQuery(tenantId, tableName);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(recordCountQuery);
            stmt.setLong(1, timeFrom);
            stmt.setLong(2, timeTo);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new AnalyticsException("Record count not available for " + 
                        printableTableName(tenantId, tableName));
            }
        } catch (SQLException e) {
            if (conn != null && !this.tableExists(conn, tenantId, tableName)) {
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
    private class RDBMSResultSetIterator implements AnalyticsIterator<Record> {

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
                    this.rs = null;
                    this.stmt = null;
                    this.conn = null;
                    return null;
                }
            } catch (Exception e) {
                RDBMSUtils.cleanupConnection(this.rs, this.stmt, this.conn);
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @Override
        public void remove() {
            /* this is a read-only iterator, nothing will be removed */
        }
        
        @Override
        public void finalize() {
            /* in the unlikely case, this iterator does not go to the end,
             * we have to make sure the connection is cleaned up */
            RDBMSUtils.cleanupConnection(this.rs, this.stmt, this.conn);
        }

        @Override
        public void close() throws IOException {
            RDBMSUtils.cleanupConnection(this.rs, this.stmt, this.conn);
            this.rs = null;
            this.stmt = null;
            this.conn = null;
        }
    }

    public class EmptyResultSetAnalyticsIterator implements AnalyticsIterator<Record>{

        @Override
        public void close() throws IOException {
            /* Nothing to do */
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Record next() {
            return null;
        }

        @Override
        public void remove() {
            /* Nothing to do */
        }
    }

}
