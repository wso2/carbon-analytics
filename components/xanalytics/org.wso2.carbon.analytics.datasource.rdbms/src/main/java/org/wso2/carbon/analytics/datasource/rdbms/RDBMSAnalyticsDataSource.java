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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    
    @Override
    public void init(Map<String, String> properites)
            throws AnalyticsDataSourceException {
        this.properties = properites;
        String dsName = properites.get(RDBMSAnalyticsDSConstants.DATASOURCE);
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
    
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/bam3", "root", "root");
        conn.setAutoCommit(false);
        String sqlx = "INSERT INTO AN_TABLE_RECORD (record_id, table_name, timestamp) VALUES (?,?,?)";
        //String sql = "INSERT INTO AN_TABLE_RECORD_COLUMN4 (record_id, record_column_name, record_column_data) VALUES (?,?,?)";
        String sql = "INSERT INTO AN_TABLE_RECORD_COLUMN3 (record_id, record_column_name, record_column_data_long) VALUES (?,?,?)";
        String strData = "FOIJFWOIJFOWIFWOIFJW OIFG OIJ FEOFIJE OFIJEOFIJEOFIJOIJOI OIEJF EOIFJ EOFIEOIFJEOIF OIJOIJF EOIJOIJFOEI JWOGIJEWG";
        byte[] data = strData.getBytes();
        long start = System.currentTimeMillis();
        final int n = 5, batch = 1000;
        for (int j = 0; j < n; j++) {
//            PreparedStatement stmt = conn.prepareStatement(sqlx);
//            List<String> ids = new ArrayList<String>();
//            for (int i = 0; i < batch; i++) {
//                String recordId = "" + Math.random();
//                ids.add(recordId);
//                stmt.setString(1, recordId);
//                stmt.setString(2, "T1");
//                stmt.setLong(3, 905425);
//                stmt.addBatch();
//            }
//            stmt.executeBatch();
//            stmt.close();
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (int i = 0; i < batch * 10; i++) {
                //stmt.setString(1, ids.get(i / 10));
                stmt.setString(1, "1");
                stmt.setString(2, "" + Math.random());
                //stmt.setBlob(3, new ByteArrayInputStream(data));
                stmt.setLong(3, 3435);
                stmt.addBatch();
            }
            stmt.executeBatch();
            stmt.close();
            conn.commit();
            conn.rollback();
        }
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start));
        System.out.println("TPS: " + (n * batch) / (double) (end - start) * 1000.0);
        conn.close();
    }
    
    private String[] getInitSQLQueries() {
    	String[] queries = new String[3];
    	queries[0] = "CREATE TABLE AN_TABLE_RECORD (record_id VARCHAR(50), table_name VARCHAR(256), timestamp BIGINT, PRIMARY KEY(record_id))";
    	queries[1] = "CREATE TABLE AN_TABLE_RECORD_COLUMN (record_id VARCHAR(50), record_column_name VARCHAR(50), record_column_data BLOB, PRIMARY KEY (record_id, record_column_name), FOREIGN KEY (record_id) REFERENCES AN_TABLE_RECORD (record_id) ON DELETE CASCADE)";
    	queries[2] = "CREATE INDEX AN_TABLE_RECORD_TABLE_NAME ON AN_TABLE_RECORD(table_name)";
    	queries[3] = "CREATE INDEX AN_TABLE_RECORD_TIMESTAMP ON AN_TABLE_RECORD(timestamp)";
    	return queries;
    }
    
    private String getSystemTableCheckQuery() {
    	return "DESCRIBE AN_TABLE_RECORD";
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
        try {
            conn = this.getConnection(false);
            this.addRecordEntries(conn, records);
            this.addRecordEntryColumns(conn, records);
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in adding records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private String getRecordInsertSQL() {
    	return "INSERT INTO AN_TABLE_RECORD (record_id, table_name, timestamp) VALUES (?, ?, ?)";
    }
    
    private String getRecordColumnInsertSQL() {
    	return "INSERT INTO AN_TABLE_RECORD_COLUMN (record_id, record_column_name, record_column_data) VALUES (?, ?, ?)";
    }
    
    private void addRecordEntries(Connection conn, List<Record> records) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(this.getRecordInsertSQL());
            for (Record record : records) {
                stmt.setString(1, record.getId());
                stmt.setString(2, record.getTableName());
                stmt.setLong(3, record.getTimestamp());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    private void addRecordEntryColumns(Connection conn, 
            List<Record> records) throws AnalyticsDataSourceException, SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(this.getRecordColumnInsertSQL());
            for (Record record : records) {
                for (Column column : record.getValues()) {
                    stmt.setString(1, record.getId());
                    stmt.setString(2, column.getName());
                    stmt.setBlob(3, new ByteArrayInputStream(GenericUtils.encodeColumnObject(column.getValue())));
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    @Override
    public List<Record> getRecords(String tableName, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsDataSourceException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            PreparedStatement stmt = conn.prepareStatement(this.getRecordRetrievalSQL());
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
            stmt.setInt(4, recordsFrom);
            stmt.setInt(5, recordsCount);
            ResultSet rs = stmt.executeQuery();
            List<Record> result = this.processRecordResultSet(tableName, stmt, rs, columns, conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in retrieving records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private List<Record> processRecordResultSet(String tableName, Statement stmt, ResultSet rs, 
            List<String> columns, Connection conn) throws SQLException, AnalyticsDataSourceException {
        List<Record> result = new ArrayList<Record>();
        String columnValuesSQL = this.generateGetRecordColumnsSQL(columns == null ? 0 : columns.size());
        List<Object[]> records = new ArrayList<Object[]>();
        while (rs.next()) {
            records.add(new Object[] { rs.getString(1), rs.getLong(2)});
        }
        rs.close();
        stmt.close();
        String id;
        long timestamp;
        for (Object[] record : records) {
            id = (String) record[0];
            timestamp = (Long) record[1];
            result.add(new Record(id, tableName, this.getRecordValues(columnValuesSQL, id, columns, conn), timestamp));
        }
        return result;
    }
    
    private List<Column> getRecordValues(String sql, String id, List<String> columns, 
            Connection conn) throws SQLException, AnalyticsDataSourceException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            if (columns != null) {
                for (int i = 0; i < columns.size(); i++) {
                    stmt.setString(i + 2, columns.get(i));
                }
            }
            rs = stmt.executeQuery();
            List<Column> result = new ArrayList<Record.Column>();
            String colName;
            Blob blob;
            Object value;
            while (rs.next()) {
                colName = rs.getString(1);
                blob = rs.getBlob(2);
                value = GenericUtils.decodeColumnObject(blob.getBytes(1, (int) blob.length()));
                result.add(new Column(colName, value));
            }
            return result;
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, null);
        }
    }
            
    @Override
    public List<Record> getRecords(String tableName, List<String> columns,
            List<String> ids) throws AnalyticsDataSourceException {
        String recordGetSQL = this.generateGetRecordRetrievalWithIdsSQL(ids.size());
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            PreparedStatement stmt = conn.prepareStatement(recordGetSQL);
            stmt.setString(1, tableName);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 2, ids.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            List<Record> result = this.processRecordResultSet(tableName, stmt, rs, columns, conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in retrieving records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }

    @Override
    public void delete(String tableName, long timeFrom, long timeTo)
            throws AnalyticsDataSourceException {
        String sql = this.getDeleteRecordsSQL();
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
        String sql = this.generateDeleteRecordsWithIdsSQL(ids.size());
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
    
    private String getRecordRetrievalSQL() {
        return "SELECT record_id, timestamp FROM AN_TABLE_RECORD WHERE table_name = ? AND timestamp >= ? AND timestamp < ? LIMIT ?,?";
    }
    
    private String generateGetRecordRetrievalWithIdsSQL(int recordCount) {
        String sql = "SELECT record_id, timestamp FROM AN_TABLE_RECORD WHERE table_name = ? AND record_id IN (:record_ids)";
        return sql.replaceAll(":record_ids", this.getDynamicSQLParams(recordCount));
    }
    
    private String generateDeleteRecordsWithIdsSQL(int recordCount) {
        String sql = "DELETE FROM AN_TABLE_RECORD WHERE table_name = ? AND record_id IN (:record_ids)";
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
    
    private String generateGetRecordColumnsSQL(int columnCount) {
        String sqlWithColumns = "SELECT record_column_name, record_column_data FROM AN_TABLE_RECORD_COLUMN WHERE record_id = ? AND record_column_name IN (:columns)";
        String sqlWithoutColumns = "SELECT record_column_name, record_column_data FROM AN_TABLE_RECORD_COLUMN WHERE record_id = ?";
        if (columnCount == 0) {
            return sqlWithoutColumns;
        } else {
            return sqlWithColumns.replaceAll(":columns", this.getDynamicSQLParams(columnCount));
        }
    }
    
    private String getDeleteRecordsSQL() {
        return "DELETE FROM AN_TABLE_RECORD WHERE table_name = ? AND timestamp >= ? AND timestamp < ?";
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
