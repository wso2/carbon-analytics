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
package org.wso2.carbon.analytics.datasource.core;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a data record in {@link AnalyticsDataSource}.
 */
public class Record {

	private String tableName;
	
	private List<Column> values;
	
	private List<Column> permenantValues;
	
	private List<Column> arbitraryValues;
	
	private long timestamp;
	
	private long identity;
	
	private String id;
	
	public Record(String tableName, List<Column> permenantValues, long timestamp) {
		this(null, tableName, permenantValues, timestamp);
	}
	
	public Record(String tableName, List<Column> permenantValues, List<Column> arbitraryValues, long timestamp) {
		this(null, tableName, permenantValues, arbitraryValues, timestamp);
	}
	
	public Record(String id, String tableName, List<Column> permenantValues, long timestamp) {
		this.id = id;
		if (this.id == null) {
			this.id = this.generateID();
		}
		this.tableName = tableName;
		this.permenantValues = permenantValues;
		this.values = this.permenantValues;
		this.timestamp = timestamp;
		this.identity = this.generateRecordIdentity();
	}
	
	public Record(String id, String tableName, List<Column> permenantValues, List<Column> arbitraryValues, long timestamp) {
		this.id = id;
		if (this.id == null) {
			this.id = this.generateID();
		}
		this.tableName = tableName;
		this.permenantValues = permenantValues;
		this.arbitraryValues = arbitraryValues;
		this.values = new ArrayList<Record.Column>(permenantValues.size() + arbitraryValues.size());
		this.values.addAll(permenantValues);
		this.values.addAll(arbitraryValues);
		this.timestamp = timestamp;
		this.identity = this.generateRecordIdentity();
	}
	
	public String getId() {
		return id;
	}
	
	private String generateID() {
		StringBuilder builder = new StringBuilder();
		builder.append(System.currentTimeMillis());
		builder.append(Math.random());
		return builder.toString();
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public List<Column> getValues() {
		return values;
	}
	
	public List<Column> getPermenantValues() {
		return permenantValues;
	}
	
	public List<Column> getArbitraryValues() {
		return arbitraryValues;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public long getIdentity() {
		return identity;
	}
	
	/**
	 * Generates a hash value for the target location this record represents,
	 * basically to identify the table and the fields and their order. 
	 */
	private long generateRecordIdentity() {
		long identity = this.getTableName().hashCode();
		int x = 1;
		for (Column entry : this.getValues()) {
			identity += entry.getName().hashCode() >> x;
			x++;
		}
		if (this.getArbitraryValues() != null) {
			for (Column entry : this.getArbitraryValues()) {
				identity += entry.getName().hashCode() >> x;
				x++;
			}
		}
		return identity;
	}
	
	public static class Column {
		
		private String name;
				
		private Object value;
		
		public Column(String name, Object value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public Object getValue() {
			return value;
		}
				
	}
	
}
