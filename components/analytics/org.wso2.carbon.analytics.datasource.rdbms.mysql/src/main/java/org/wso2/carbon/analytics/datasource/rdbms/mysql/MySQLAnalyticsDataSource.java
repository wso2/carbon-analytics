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
package org.wso2.carbon.analytics.datasource.rdbms.mysql;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.DataType;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.DataType.Type;
import org.wso2.carbon.analytics.datasource.core.Record.Column;
import org.wso2.carbon.analytics.datasource.rdbms.common.AbstractRDBMSAnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.rdbms.common.RDBMSAnalyticsDSConstants;

/**
 * MySQL {@link AnalyticsDataSource} implementation.
 */
public class MySQLAnalyticsDataSource extends AbstractRDBMSAnalyticsDataSource {

	private String storageEngine;

	@Override
	public void init() {
		this.storageEngine = (String) this.getProperties().get(MySQLAnalyticsDSConstants.STORAGE_ENGINE);
	}

	public String getStorageEngine() {
		return storageEngine;
	}
	
	@Override
	public String generateCreateTableSQL(String tableName, Map<String, DataType> columns) {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE IF NOT EXISTS " + tableName + "(");
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
		if (this.storageEngine != null) {
			builder.append(" ENGINE = " + this.storageEngine + ";");
		}
		return builder.toString();
	}
	
	@Override
	public String generateDropTableSQL(String tableName) {
		return "DROP TABLE " + tableName;
	}
	
	@Override
	public String generateInsertSQL(Record record) {
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
	
	private String generateSQLType(String name, DataType dataType) {
		StringBuilder builder = new StringBuilder();
		builder.append(name + " ");
		switch (dataType.type) {
		case STRING:
			if (dataType.size == -1) {
				builder.append(MySQLAnalyticsDSConstants.DATA_TYPE_TEXT);
			} else {
				builder.append(MySQLAnalyticsDSConstants.DATA_TYPE_VARCHAR + " (" + dataType.size + ")");
			}
			break;
		case BOOLEAN:
			builder.append(MySQLAnalyticsDSConstants.DATA_TYPE_BOOLEAN);
			break;
		case DOUBLE:
			builder.append(MySQLAnalyticsDSConstants.DATA_TYPE_DOUBLE);
			break;
		case INTEGER:
			builder.append(MySQLAnalyticsDSConstants.DATA_TYPE_INTEGER);
			break;
		case LONG:
			builder.append(MySQLAnalyticsDSConstants.DATA_TYPE_LONG);
			break;
		case TIMESTAMP:
			builder.append(MySQLAnalyticsDSConstants.DATA_TYPE_TIMESTAMP);
			break;
		}
		return builder.toString();
	}
	
	@Override
	public String generateAddTableSQL(String tableName, String column) {
		return "ALTER TABLE " + tableName + " ADD " + column + " CLOB";
	}
	
	@Override
	public String generateAddIndexSQL(String tableName, String column) {
		return "CREATE INDEX " + "index_" + column + " ON " + tableName + "(" + column + ")";
	}
	
	@Override
	public String generateDropIndexSQL(String tableName, String column) {
		return "DROP INDEX " + "index_" + column + " ON " + tableName;
	}
	
	@Override
	public String generateGetRecordsSQL(String tableName, List<String> columns) {
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
	
	@Override
	public String generateGetRecordsWithIdsSQL(String tableName, List<String> columns, 
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
		builder.append(" FROM " + tableName);
		builder.append(" WHERE ");
		builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME + " >= ?");
		builder.append(" AND ");
		builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME + " < ?");
		builder.append(" AND " + RDBMSAnalyticsDSConstants.ID_COLUMN_NAME + " IN (");
		for (int i = 0; i < recordCount; i++) {
			builder.append("?");
			if (i + 1 < recordCount) {
				builder.append(",");
			}
		}
		builder.append(")");
		builder.append(" LIMIT ?,?");
		return builder.toString();		
	}
	
	@Override
	public String generateDeleteRecordsSQL(String tableName) {
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM " + tableName);
		builder.append(" WHERE ");
		builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME + " >= ?");
		builder.append(" AND ");
		builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME + " < ?");
		return builder.toString();		
	}
	
	@Override
	public String generateDeleteRecordsWithIdsSQL(String tableName, int recordCount) {
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM " + tableName);
		builder.append(" WHERE ");
		builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME + " >= ?");
		builder.append(" AND ");
		builder.append(RDBMSAnalyticsDSConstants.TIMESTAMP_COLUMN_NAME + " < ?");
		builder.append(" AND " + RDBMSAnalyticsDSConstants.ID_COLUMN_NAME + " IN (");
		for (int i = 0; i < recordCount; i++) {
			builder.append("?");
			if (i + 1 < recordCount) {
				builder.append(",");
			}
		}
		return builder.toString();		
	}

}
