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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.wso2.carbon.analytics.datasource.core.Record.Column;

/**
 * Abstract RDBMS database backed implementation of {@link AnalyticsDataSource}.
 */
public abstract class AbstractRDBMSAnalyticsDataSource extends DirectAnalyticsDataSource {

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
	
	private Connection getConnection() throws SQLException {
		return this.getConnection(true);
	}
	
	private Connection getConnection(boolean autoCommit) throws SQLException {
		Connection conn = this.getDataSource().getConnection();
		conn.setAutoCommit(autoCommit);
		return conn;
	}
	
	public abstract void init();
	
	public abstract String generateCreateTableSQL(String tableName, Map<String, DataType> columns);
	
	@Override
	public void addTable(String tableName, Map<String, DataType> columns)
			throws AnalyticsDataSourceException {
		String sql = this.generateCreateTableSQL(tableName, columns);
		System.out.println(sql);
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
	
	public abstract String generateDropTableSQL(String tableName);

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
		} catch (SQLException e) {
			throw new AnalyticsDataSourceException("Error in dropping table: " + e.getMessage(), e);
		} finally {
			RDBMSUtils.cleanupConnection(null, stmt, conn);
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
	
	public abstract String generateInsertSQL(Record record);
	
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
		requiredColumns.removeAll(existingColumns);
		if (requiredColumns.size() > 0) {
			for (String column : requiredColumns) {
				this.addTableStringColumn(conn, record.getTableName(), column);
			}
		}
	}
	
	private Set<String> getTableColumns(Connection conn, String tableName) throws SQLException {
		DatabaseMetaData dmd = conn.getMetaData();
		ResultSet rs = dmd.getColumns(null, null, tableName, "%");
		Set<String> result = new HashSet<String>();
		while (rs.next()) {
			result.add(rs.getString("COLUMN_NAME"));
		}
		return result;
	}
	
	public abstract String generateAddTableSQL(String tableName, String column);
	
	private void addTableStringColumn(Connection conn, String tableName, 
			String column) throws SQLException {
		Statement stmt = null;
		try {
		    stmt = conn.createStatement();
		    stmt.executeQuery(this.generateAddTableSQL(tableName, column));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	private void populateStatementWithRecord(PreparedStatement stmt, 
			Record record) throws SQLException {
		int index = 1;
		stmt.setString(index++, record.getId());
		stmt.setLong(index++, record.getTimestamp());
		for (Column entry : record.getValues()) {
			stmt.setObject(index++, entry.getValue());
		}
	}
	
	public abstract String generateAddIndexSQL(String tableName, String columnName);
	
	private void addDBIndexImpl(String tableName, String columnName, Connection conn)
			throws SQLException {
		String sql = this.generateAddIndexSQL(tableName, columnName);
		System.out.println(sql);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		    stmt.executeUpdate(sql);
		} finally {
			RDBMSUtils.cleanupConnection(null, stmt, null);
		}
	}
	
	public abstract String generateDropIndexSQL(String tableName, String columnName); 
	
	private void dropDBIndexImpl(String tableName, String columnName, Connection conn)
			throws SQLException {
		String sql = this.generateDropIndexSQL(tableName, columnName);
		System.out.println(sql);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		    stmt.executeUpdate(sql);
		} finally {
			RDBMSUtils.cleanupConnection(null, stmt, null);
		}
	}
	
	private Record createRecordFromResultSetEntry(String tableName, 
			ResultSet rs, List<String> columns) throws SQLException {
		List<Column> values = new ArrayList<Record.Column>();
		String id = rs.getString(RDBMSAnalyticsDSConstants.ID_COLUMN_NAME);
		long timestamp = rs.getLong(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME);
		for (String column : columns) {
			values.add(new Column(column, rs.getObject(column)));
		}
		return new Record(id, tableName, values, timestamp);
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
	
	public abstract String generateGetRecordsSQL(String tableName, List<String> columns);
	
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
	
	public abstract String generateGetRecordsWithIdsSQL(String tableName, List<String> columns, int recordCount);
	
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

	public abstract String generateDeleteRecordsSQL(String tableName);

	@Override
	public void delete(String tableName, long timeFrom, long timeTo)
			throws AnalyticsDataSourceException {
		String sql = this.generateDeleteRecordsSQL(tableName);
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setTimestamp(1, new Timestamp(timeFrom));
			stmt.setTimestamp(2, new Timestamp(timeTo));
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new AnalyticsDataSourceException("Error in deleting records: " + e.getMessage(), e);
		} finally {
			RDBMSUtils.cleanupConnection(null, stmt, conn);
		}
	}
	
	public abstract String generateDeleteRecordsWithIdsSQL(String tableName, int recordCount);
	
	@Override
	public void delete(String tableName, List<String> ids) throws AnalyticsDataSourceException {
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
			throw new AnalyticsDataSourceException("Error in searching records: " + e.getMessage(), e);
		} finally {
			RDBMSUtils.cleanupConnection(null, stmt, conn);
		}
	}

}
