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
                stmt.setString(2, record.getTableName());
                stmt.setLong(3, record.getTimestamp());
                stmt.setBlob(4, new ByteArrayInputStream(GenericUtils.encodeRecordValues(record.getValues())));
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
    public List<Record> getRecords(String tableName, List<String> columns,
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
            stmt.setString(1, tableName);
            stmt.setLong(2, timeFrom);
            stmt.setLong(3, timeTo);
            stmt.setInt(4, this.adjustRecordsFromForProvider(recordsFrom));
            stmt.setInt(5, this.adjustRecordsCountForProvider(recordsFrom, recordsCount));
            rs = stmt.executeQuery();
            List<Record> result = this.processRecordResultSet(tableName, rs, columns);
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
    
    private List<Record> processRecordResultSet(String tableName, ResultSet rs, 
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
            record = new Record(rs.getString(1), tableName, values, rs.getLong(2));
            result.add(record);
        }
        return result;
    }
            
    @Override
    public List<Record> getRecords(String tableName, List<String> columns,
            List<String> ids) throws AnalyticsDataSourceException {
        String recordGetSQL = this.generateGetRecordRetrievalWithIdQuery(ids.size());
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection(false);
            stmt = conn.prepareStatement(recordGetSQL);
            stmt.setString(1, tableName);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 2, ids.get(i));
            }
            rs = stmt.executeQuery();
            List<Record> result = this.processRecordResultSet(tableName, rs, columns);
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
    public void delete(String tableName, long timeFrom, long timeTo)
            throws AnalyticsDataSourceException {
        String sql = this.getRecordDeletionQuery();
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
            stmt.setString(1, tableName);
            stmt.setLong(2, timeFrom);
            stmt.setLong(3, timeTo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in deleting records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
        
    @Override
    public void delete(String tableName, List<String> ids) throws AnalyticsDataSourceException {
        if (ids.size() == 0) {
            return;
        }
        String sql = this.generateRecordDeletionRecordsWithIdsQuery(ids.size());
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, tableName);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 2, ids.get(i));
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
    public void purgeTable(String tableName) throws AnalyticsDataSourceException {
        this.delete(tableName, -1, -1);
    }

    @Override
    public FileSystem getFileSystem() throws AnalyticsDataSourceException {
        return null;
    }

    @Override
    public LockProvider getLockProvider() throws AnalyticsLockException {
        return null;
    }

}
