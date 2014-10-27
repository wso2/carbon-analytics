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
package org.wso2.carbon.analytics.datasource.rdbms.common;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.DataType;
import org.wso2.carbon.analytics.datasource.core.DirectAnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.DataType.Type;
import org.wso2.carbon.analytics.datasource.core.Record.Column;

/**
 * Abstract RDBMS database backed implementation of {@link AnalyticsDataSource}.
 */
public abstract class RDBMSAnalyticsDataSource extends DirectAnalyticsDataSource {

    private DataSource dataSource;
        
    private Map<String, Map<String, Integer>> tableIndexMap;
    
    private Map<String, Object> properties;
    
    @Override
    public void init(Map<String, Object> properites)
            throws AnalyticsDataSourceException {
        this.properties = properites;
        Object dsObj = properites.get(RDBMSAnalyticsDSConstants.DATASOURCE);
        if (dsObj instanceof DataSource) {
            this.dataSource = (DataSource) dsObj;
        } else {
            String dsName = (String) dsObj;
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
        }
        this.tableIndexMap = new HashMap<String, Map<String,Integer>>();
        /* RDBMS implementation specific init operations */
        this.init();
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public Map<String, Map<String, Integer>> getTableIndexMap() {
        return tableIndexMap;
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    protected Connection getConnection() throws SQLException {
        return this.getConnection(true);
    }
    
    protected Connection getConnection(boolean autoCommit) throws SQLException {
        Connection conn = this.getDataSource().getConnection();
        conn.setAutoCommit(autoCommit);
        return conn;
    }
    
    public abstract void init();
        
    protected String generateCreateTableSQL(String tableName, Map<String, DataType> columns) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE " + tableName + "(");
        Iterator<Map.Entry<String, DataType>> itr = columns.entrySet().iterator();
        /* add the id column */
        builder.append(generateSQLType(
                RDBMSAnalyticsDSConstants.ID_COLUMN_NAME, 
                new DataType(Type.STRING, 40)));
        builder.append(",");
        /* add the internal timestamp column */
        builder.append(generateSQLType(
                RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME, 
                new DataType(Type.LONG, -1)));
        while (itr.hasNext()) {
            builder.append(",");
            Map.Entry<String, DataType> entry = itr.next();
            builder.append(generateSQLType(entry.getKey(), entry.getValue()));
        }
        builder.append(",");
        builder.append("PRIMARY KEY(" + RDBMSAnalyticsDSConstants.ID_COLUMN_NAME + ")");
        builder.append(")");
        return builder.toString();
    }
    
    @Override
    public void addTable(String tableName, Map<String, DataType> columns)
            throws AnalyticsDataSourceException {
        String sql = this.generateCreateTableSQL(tableName, columns);
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection(false);
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            this.addDBIndexImpl(tableName, RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME, conn);
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in adding table: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    protected String generateDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName;
    }

    @Override
    public void dropTable(String tableName) throws AnalyticsDataSourceException {
        String sql = this.generateDropTableSQL(tableName);
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection(false);
            this.dropDBIndexImpl(tableName, RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME, conn);
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in dropping table: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    @Override
    public boolean tableExists(String tableName) throws AnalyticsDataSourceException {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            DatabaseMetaData dbm = conn.getMetaData();
            rs = dbm.getTables(null, null, "%", null);
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in checking table existence: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, null, conn);
        }
    }
    
    @Override
    public void put(List<Record> records) throws AnalyticsDataSourceException {
        /* if the records have identities (unique table name and fields) as the following
         * "ABABABCCAACBDABCABCDBAC", the job of this method is to make it like the following,
         * {"AAAAAAAA", "BBBBBBB", "CCCCCC", "DD" } and add these with separate batch inserts */
        Map<Long, List<Record>> recordBatches = new HashMap<Long, List<Record>>();
        List<Record> recordBatch;
        for (Record record : records) {
            recordBatch = recordBatches.get(record.getIdentity());
            if (recordBatch == null) {
                recordBatch = new ArrayList<Record>();
                recordBatches.put(record.getIdentity(), recordBatch);
            }
            recordBatch.add(record);
        }
        for (List<Record> batch : recordBatches.values()) {
            this.addRecordsSimilar(batch);
        }
    }
    
    protected String generateInsertSQL(Record record) {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO " + record.getTableName() + " (");
        builder.append(RDBMSAnalyticsDSConstants.ID_COLUMN_NAME);
        builder.append(",");
        builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME);
        for (Column entry : record.getValues()) {
            builder.append(",");
            builder.append(entry.getName());
        }
        builder.append(") VALUES (");
        builder.append("?,?");
        int count = record.getValues().size();
        for (int i = 0; i < count; i++) {
            builder.append(",?");
        }
        builder.append(")");
        return builder.toString();
    }
    
    private void addRecordsSimilar(List<Record> records) throws AnalyticsDataSourceException {
        Record firstRecord = records.get(0);
        String sql = this.generateInsertSQL(firstRecord);
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection(false);
            this.checkAndProcessArbitraryFields(conn, firstRecord);
            stmt = conn.prepareStatement(sql);
            if (records.size() == 1) {
                this.populateStatementWithRecord(stmt, firstRecord);
                stmt.executeUpdate();
            } else {
                /* batch insert */
                for (Record record : records) {
                    this.populateStatementWithRecord(stmt, record);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in adding records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    private void checkAndProcessArbitraryFields(Connection conn, Record record) throws SQLException {
        if (record.getArbitraryValues() == null || record.getArbitraryValues().size() == 0) {
            return;
        }
        Set<String> existingColumns = this.getTableColumns(conn, record.getTableName());
        Set<String> requiredColumns = new HashSet<String>(record.getArbitraryValues().size());
        for (Column column : record.getArbitraryValues()) {
            requiredColumns.add(column.getName());
        }
        Iterator<String> requiredItr = requiredColumns.iterator();
        Iterator<String> existingItr;
        String requiredColumn, existingColumn;
        while (requiredItr.hasNext()) {
            requiredColumn = requiredItr.next();
            existingItr = existingColumns.iterator();
            while (existingItr.hasNext()) {
                existingColumn = existingItr.next();
                if (requiredColumn.equalsIgnoreCase(existingColumn)) {
                    requiredItr.remove();
                    break;
                }
            }
        }
        requiredColumns.removeAll(existingColumns);
        if (requiredColumns.size() > 0) {
            for (String column : requiredColumns) {
                this.addTableStringColumn(conn, record.getTableName(), column);
            }
        }
    }
    
    protected Set<String> getTableColumns(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData dmd = conn.getMetaData();
        ResultSet rs = dmd.getColumns(null, null, tableName, "%");
        Set<String> result = new HashSet<String>();
        while (rs.next()) {
            result.add(rs.getString("COLUMN_NAME").toUpperCase());
        }
        return result;
    }
    
    public abstract String getSQLType(DataType dataType);
    
    protected String generateSQLType(String name, DataType dataType) {
        return name + " " + this.getSQLType(dataType);
    }
    
    protected String generateAddStringColumnSQL(String tableName, String column) {
        return "ALTER TABLE " + tableName + " ADD " + column + " " + this.getSQLType(new DataType(Type.STRING, -1));
    }
        
    private void addTableStringColumn(Connection conn, String tableName, 
            String column) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(this.generateAddStringColumnSQL(tableName, column));
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }
    
    protected void populateStatementWithRecord(PreparedStatement stmt, 
            Record record) throws SQLException {
        int index = 1;
        stmt.setString(index++, record.getId());
        stmt.setLong(index++, record.getTimestamp());
        for (Column entry : record.getValues()) {
            if (entry.getValue() != null) {
                stmt.setObject(index++, entry.getValue());
            } else {
                stmt.setNull(index++, Types.OTHER);
            }
        }
    }
    
    protected String generateAddIndexSQL(String tableName, String column) {
        return "CREATE INDEX " + "index_" + tableName + "_" + column + " ON " + tableName + "(" + column + ")";
    }
    
    protected void addDBIndexImpl(String tableName, String columnName, Connection conn)
            throws SQLException {
        String sql = this.generateAddIndexSQL(tableName, columnName);
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    protected String generateDropIndexSQL(String tableName, String column) {
        return "DROP INDEX " + "index_" + tableName + "_" + column;
    }
    
    protected void dropDBIndexImpl(String tableName, String columnName, Connection conn)
            throws SQLException {
        String sql = this.generateDropIndexSQL(tableName, columnName);
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    protected Record createRecordFromResultSetEntry(String tableName, 
            ResultSet rs, List<String> columns) throws SQLException {
        List<Column> values = new ArrayList<Record.Column>();
        String id = rs.getString(RDBMSAnalyticsDSConstants.ID_COLUMN_NAME);
        long timestamp = rs.getLong(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME);
        for (String column : columns) {
            if (!RDBMSAnalyticsDSConstants.ID_COLUMN_NAME.equalsIgnoreCase(column) && 
                    !RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME.equalsIgnoreCase(column)) {
                values.add(new Column(column, this.parseJDBCTypeToPrimitiveType(rs.getObject(column))));
            }
        }
        return new Record(id, tableName, values, timestamp);
    }
    
    protected Object parseJDBCTypeToPrimitiveType(Object obj) throws SQLException {
        if (obj instanceof Clob) {
            Clob clob = (Clob) obj;
            return clob.getSubString(1, (int) clob.length());
        } else if (obj instanceof NClob) {
            Clob nclob = (NClob) obj;
            return nclob.getSubString(1, (int) nclob.length());
        } else {
            return obj;
        }
    }

    private List<String> getAllColumnsOfRS(ResultSet rs) throws SQLException {
        List<String> result = new ArrayList<String>();
        ResultSetMetaData rmd = rs.getMetaData();
        int count = rmd.getColumnCount();
        for (int i = 1; i <= count; i++) {
            result.add(rmd.getColumnName(i));
        }
        return result;
    }
        
    @Override
    public List<Record> getRecords(String tableName, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsDataSourceException {
        String sql = this.generateGetRecordsSQL(tableName, columns);
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
            if (recordsFrom == -1) {
                recordsFrom = 0;
            }
            if (recordsCount == -1) {
                recordsCount = Integer.MAX_VALUE;
            }
            stmt.setLong(1, timeFrom);
            stmt.setLong(2, timeTo);
            stmt.setInt(3, recordsFrom);
            stmt.setInt(4, recordsCount);
            ResultSet rs = stmt.executeQuery();
            if (columns == null || columns.size() == 0) {
                columns = this.getAllColumnsOfRS(rs);
            }
            List<Record> result = new ArrayList<Record>();
            while (rs.next()) {
                result.add(this.createRecordFromResultSetEntry(tableName, rs, columns));
            }
            return result;
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in searching records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
        
    @Override
    public List<Record> getRecords(String tableName, List<String> columns,
            List<String> ids) throws AnalyticsDataSourceException {
        String sql = this.generateGetRecordsWithIdsSQL(tableName, columns, ids.size());
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            int index = 1;
            for (String id : ids) {
                stmt.setString(index++, id);
            }
            ResultSet rs = stmt.executeQuery();
            if (columns == null || columns.size() == 0) {
                columns = this.getAllColumnsOfRS(rs);
            }
            List<Record> result = new ArrayList<Record>();
            while (rs.next()) {
                result.add(this.createRecordFromResultSetEntry(tableName, rs, columns));
            }
            return result;
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in searching records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }

    @Override
    public void delete(String tableName, long timeFrom, long timeTo)
            throws AnalyticsDataSourceException {
        String sql = this.generateDeleteRecordsSQL(tableName);
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
        String sql = this.generateDeleteRecordsWithIdsSQL(tableName, ids.size());
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            int index = 1;
            for (String id : ids) {
                stmt.setString(index++, id);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in deleting records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    protected String generateGetRecordsSQL(String tableName, List<String> columns) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        if (columns == null || columns.size() == 0) {
            builder.append("*");
        } else {
            builder.append(RDBMSAnalyticsDSConstants.ID_COLUMN_NAME);
            builder.append(",");
            builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME);
            for (Iterator<String> itr = columns.iterator(); itr.hasNext();) {
                builder.append(",");
                builder.append(itr.next());
            }
        }
        builder.append(" FROM " + tableName);
        builder.append(" WHERE ");
        builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME + " >= ?");
        builder.append(" AND ");
        builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME + " < ?");
        builder.append(" LIMIT ?,?");
        return builder.toString();        
    }
    
    protected String generateGetRecordsWithIdsSQL(String tableName, List<String> columns, 
            int recordCount) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        if (columns == null || columns.size() == 0) {
            builder.append("*");
        } else {
            builder.append(RDBMSAnalyticsDSConstants.ID_COLUMN_NAME);
            builder.append(",");
            builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME);
            for (Iterator<String> itr = columns.iterator(); itr.hasNext();) {
                builder.append(",");
                builder.append(itr.next());
            }
        }
        builder.append(" FROM " + tableName + " WHERE ");
        builder.append(RDBMSAnalyticsDSConstants.ID_COLUMN_NAME + " IN (");
        for (int i = 0; i < recordCount; i++) {
            builder.append("?");
            if (i + 1 < recordCount) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();        
    }
    
    protected String generateDeleteRecordsSQL(String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM " + tableName);
        builder.append(" WHERE ");
        builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME + " >= ?");
        builder.append(" AND ");
        builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME + " < ?");
        return builder.toString();        
    }
    
    protected String generateDeleteRecordsWithIdsSQL(String tableName, int recordCount) {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM " + tableName + " WHERE ");
        builder.append(RDBMSAnalyticsDSConstants.ID_COLUMN_NAME + " IN (");
        for (int i = 0; i < recordCount; i++) {
            builder.append("?");
            if (i + 1 < recordCount) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();        
    }

}
