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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsLockException;
import org.wso2.carbon.analytics.datasource.core.DirectAnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.Record.Column;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;
import org.wso2.carbon.analytics.datasource.core.lock.LockProvider;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * Abstract RDBMS database backed implementation of {@link AnalyticsDataSource}.
 */
public class RDBMSAnalyticsDataSource extends DirectAnalyticsDataSource {

    private static final String TABLE_REPRESENTATIVE_ID = "__1d45406f-9735-49c0-a714-fc1e60375671__";
    
    private static final String NON_TABLE_REPRESENTATIVE_ID = "__eac3d187-6ba6-46f8-bd7a-9a74fe0a5fc7__";

    private DataSource dataSource;
    
    private Map<String, String> properties;
    
    private QueryConfiguration queryConfiguration;
    
    public RDBMSAnalyticsDataSource() {
        this(null);
    }
    
    public RDBMSAnalyticsDataSource(QueryConfiguration queryConfiguration) {
        this.queryConfiguration = queryConfiguration;
    }
    
    @Override
    public void init(Map<String, String> properties)
            throws AnalyticsDataSourceException {
        this.properties = properties;
        String dsName = properties.get(RDBMSAnalyticsDSConstants.DATASOURCE);
        if (dsName == null) {
            throw new AnalyticsDataSourceException("The property '" + 
                    RDBMSAnalyticsDSConstants.DATASOURCE + "' is required");
        }
        try {
            this.dataSource = (DataSource) InitialContext.doLookup(dsName);
        } catch (NamingException e) {
            throw new AnalyticsDataSourceException("Error in looking up data source: " + 
                    e.getMessage(), e);
        }
        /* create the system tables */
        this.checkAndCreateSystemTables();
    }
    
    public QueryConfiguration getQueryConfiguration() {
        return queryConfiguration;
    }

    private void checkAndCreateSystemTables() throws AnalyticsDataSourceException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            Statement stmt;
            if (!this.checkSystemTables(conn)) {
            	for (String query : this.getInitSQLQueries()) {
            		stmt = conn.createStatement();
            		stmt.executeUpdate(query);
            		stmt.close();
            	}
            }
            conn.commit();
        } catch (SQLException e) {
        	RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in creating system tables: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private boolean checkSystemTables(Connection conn) {
    	Statement stmt = null;
    	try {
    		stmt = conn.createStatement();
    		stmt.execute(this.getSystemTableCheckQuery());
    		return true;
    	} catch (SQLException ignore) {
    		RDBMSUtils.cleanupConnection(null, stmt, null);
    		return false;
    	}
    }
    
    private String[] getInitSQLQueries() {
    	return this.getQueryConfiguration().getInitQueries();
    }
    
    private String getSystemTableCheckQuery() {
    	return this.getQueryConfiguration().getSystemTablesCheckQuery();
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
    public void put(List<Record> records) throws AnalyticsDataSourceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection(false);
            stmt = conn.prepareStatement(this.getRecordInsertSQL());
            for (Record record : records) {
                stmt.setString(1, record.getId());
                stmt.setString(2, record.getTableCategory());
                stmt.setString(3, record.getTableName());
                stmt.setLong(4, record.getTimestamp());
                stmt.setBlob(5, new ByteArrayInputStream(GenericUtils.encodeRecordValues(record.getValues())));
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in adding records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    private String getRecordInsertSQL() {
    	return this.getQueryConfiguration().getRecordInsertQuery();
    }

    @Override
    public List<Record> getRecords(String tableCategory, String tableName, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsDataSourceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection(false);
            stmt = conn.prepareStatement(this.getRecordRetrievalQuery());
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
            stmt.setString(1, tableCategory);
            stmt.setString(2, tableName);
            stmt.setLong(3, timeFrom);
            stmt.setLong(4, timeTo);
            stmt.setString(5, TABLE_REPRESENTATIVE_ID);
            stmt.setInt(6, this.adjustRecordsFromForProvider(recordsFrom));
            stmt.setInt(7, this.adjustRecordsCountForProvider(recordsFrom, recordsCount));            
            rs = stmt.executeQuery();
            List<Record> result = this.processRecordResultSet(tableCategory, tableName, rs, columns);
            conn.commit();
            return result;
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in retrieving records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
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
    
    private List<Record> processRecordResultSet(String tableCategory, String tableName, ResultSet rs, 
            List<String> columns) throws SQLException, AnalyticsDataSourceException {
        List<Record> result = new ArrayList<Record>();
        Record record;
        Blob blob;
        List<Column> values;
        Set<String> colSet = null;
        if (columns != null && columns.size() > 0) {
            colSet = new HashSet<String>(columns);
        }
        while (rs.next()) {
            blob = rs.getBlob(3);
            values = GenericUtils.decodeRecordValues(blob.getBytes(1, (int) blob.length()), colSet);
            record = new Record(rs.getString(1), tableCategory, tableName, values, rs.getLong(2));
            result.add(record);            
        }
        return result;
    }
            
    @Override
    public List<Record> getRecords(String tableCategory, String tableName, List<String> columns,
            List<String> ids) throws AnalyticsDataSourceException {
        String recordGetSQL = this.generateGetRecordRetrievalWithIdQuery(ids.size());
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection(false);
            stmt = conn.prepareStatement(recordGetSQL);
            stmt.setString(1, tableCategory);
            stmt.setString(2, tableName);
            stmt.setString(3, TABLE_REPRESENTATIVE_ID);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 4, ids.get(i));
            }
            rs = stmt.executeQuery();
            List<Record> result = this.processRecordResultSet(tableCategory, tableName, rs, columns);
            conn.commit();
            return result;
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in retrieving records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    @Override
    public void delete(String tableCatelog, String tableName, long timeFrom, long timeTo)
            throws AnalyticsDataSourceException {
        this.delete(tableCatelog, tableName, timeFrom, timeTo, false);
    }
    
    public void delete(String tableCategory, String tableName, long timeFrom, long timeTo, boolean dropTable)
            throws AnalyticsDataSourceException {
        String sql = this.getRecordDeletionQuery();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection(false);
            if (!dropTable) {
                /* there is a chance we will delete all the records, so lets put in the
                 * table representation record if its already not there */
                this.createTable(conn, tableCategory, tableName);
            }
            stmt = conn.prepareStatement(sql);
            if (timeFrom == -1) {
                timeFrom = Long.MIN_VALUE;
            }
            if (timeTo == -1) {
                timeTo = Long.MAX_VALUE;
            }
            stmt.setString(1, tableCategory);
            stmt.setString(2, tableName);
            stmt.setLong(3, timeFrom);
            stmt.setLong(4, timeTo);
            if (dropTable) {
                stmt.setString(5, NON_TABLE_REPRESENTATIVE_ID);
            } else {
                stmt.setString(5, TABLE_REPRESENTATIVE_ID);
            }
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in deleting records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
        
    @Override
    public void delete(String tableCatelog, String tableName, List<String> ids) throws AnalyticsDataSourceException {
        if (ids.size() == 0) {
            return;
        }
        String sql = this.generateRecordDeletionRecordsWithIdsQuery(ids.size());
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, tableCatelog);
            stmt.setString(2, tableName);
            stmt.setString(3, TABLE_REPRESENTATIVE_ID);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 4, ids.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in deleting records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    private String getRecordRetrievalQuery() {
        return this.getQueryConfiguration().getRecordRetrievalQuery();
    }
    
    private String generateGetRecordRetrievalWithIdQuery(int recordCount) {
        String sql = this.getQueryConfiguration().getRecordRetrievalWithIdsQuery();
        return sql.replaceAll(":record_ids", this.getDynamicSQLParams(recordCount));
    }
    
    private String getTableListQuery() {
        return this.getQueryConfiguration().getTableListQuery();
    }
    
    private String generateRecordDeletionRecordsWithIdsQuery(int recordCount) {
        String sql = this.getQueryConfiguration().getRecordDeletionWithIdsQuery();
        return sql.replaceAll(":record_ids", this.getDynamicSQLParams(recordCount));
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
    
    private String getRecordDeletionQuery() {
        return this.getQueryConfiguration().getRecordDeletionQuery();
    }

    @Override
    public void deleteTable(String tableCategory, String tableName) throws AnalyticsDataSourceException {
        this.delete(tableCategory, tableName, -1, -1, true);
    }

    @Override
    public FileSystem getFileSystem() throws AnalyticsDataSourceException {
        return new RDBMSFileSystem(this.getQueryConfiguration(), this.getDataSource());
    }

    @Override
    public LockProvider getLockProvider() throws AnalyticsLockException {
        return null;
    }
    
    @Override
    public void createTable(String tableCategory, String tableName) throws AnalyticsDataSourceException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            this.createTable(conn, tableCategory, tableName);
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in creating table: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }

    public void createTable(Connection conn, String tableCategory, String tableName) throws SQLException, AnalyticsDataSourceException {
        PreparedStatement stmt = null;
        try {
            if (this.isTableRepresentativeRecordAvailable(conn, tableCategory, tableName)) {
                return;
            }
            stmt = conn.prepareStatement(this.getRecordInsertSQL());
            stmt.setString(1, TABLE_REPRESENTATIVE_ID);
            stmt.setString(2, tableCategory);
            stmt.setString(3, tableName);
            stmt.setLong(4, 0);
            stmt.setBlob(5, new ByteArrayInputStream(GenericUtils.encodeRecordValues(new ArrayList<Record.Column>())));
            stmt.executeUpdate();
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    private boolean isTableRepresentativeRecordAvailable(Connection conn, String tableCategory, 
            String tableName) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(this.generateGetRecordRetrievalWithIdQuery(1));
            stmt.setString(1, tableCategory);
            stmt.setString(2, tableName);
            stmt.setString(3, NON_TABLE_REPRESENTATIVE_ID);
            stmt.setString(4, TABLE_REPRESENTATIVE_ID);
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, null);
        }
    }

    @Override
    public List<String> listTables(String tableCategory) throws AnalyticsDataSourceException {
        String recordGetSQL = this.getTableListQuery();
        List<String> result = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(recordGetSQL);
            stmt.setString(1, tableCategory);
            rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in retrieving records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }

}
