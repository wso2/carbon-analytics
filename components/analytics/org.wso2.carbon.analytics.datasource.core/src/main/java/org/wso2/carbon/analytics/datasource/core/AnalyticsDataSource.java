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

import java.util.List;
import java.util.Map;

/**
 * This interface represents the common data store implementations used in analytics. 
 */
public interface AnalyticsDataSource {
	
	/**
	 * This method initializes the AnalyticsDataSource implementation, and is called once before any other method.
	 * @param properites The properties associated with this analytics data source 
	 * @throws AnalyticsDataSourceException
	 */
	void init(Map<String, Object> properites) throws AnalyticsDataSourceException;
	
	/**
	 * Adds a table with the given details.
	 * @param tableName The name of the target table
	 * @param columns The columns consisting in the table
	 * the key is the name of the column and the value the data type
	 * @throws AnalyticsDataSourceException
	 */
	void addTable(String tableName, Map<String, DataType> columns) throws AnalyticsDataSourceException;
	
	/**
	 * Drops a given table, and deletes the indices associated with it.
	 * @param tableName The name of the table to be dropped
	 * @throws AnalyticsDataSourceException
	 */
	void dropTable(String tableName) throws AnalyticsDataSourceException;
	
	/**
	 * Adds a new record to the table. If the record id is mentioned, 
	 * it will be used to do an insert/update, or else, an insert will be done with a generated id.
	 * @param records The list of records to be inserted
	 * @throws AnalyticsDataSourceException
	 */
	void put(List<Record> records) throws AnalyticsDataSourceException;
	
	/**
	 * Retrieves data from a table.
	 * @param tableName The name of the table to search on
	 * @param columns The list of columns to required in results
	 * @param timeFrom The starting time to get records from, inclusive, -1 for beginning of time
	 * @param timeTo The ending time to get records to, non-inclusive, -1 for infinity
	 * @param recordsFrom The paginated index from value, zero based, inclusive
	 * @param recordsCount The paginated records count to be read, -1 for infinity
	 * @return An array of {@link RecordGroup} objects, which contains individual data sets in their local location
	 * @throws AnalyticsDataSourceException
	 */
	RecordGroup[] get(String tableName, List<String> columns, long timeFrom, 
			long timeTo, int recordsFrom, int recordsCount) throws AnalyticsDataSourceException;
	
	/**
	 * Retrieves data from a table with given ids.
	 * @param tableName The name of the table to search on
	 * @param columns The list of columns to required in results
	 * @param ids The list of ids of the records to be read
	 * @return An array of {@link RecordGroup} objects, which contains individual data sets in their local location
	 * @throws AnalyticsDataSourceException
	 */
	RecordGroup[] get(String tableName, List<String> columns, 
			List<String> ids) throws AnalyticsDataSourceException;

	/**
	 * Deletes a set of records in the table.
	 * @param tableName The name of the table to search on 
	 * @param timeFrom The starting time to get records from for deletion
	 * @param timeTo The ending time to get records to for deletion
	 * @throws AnalyticsDataSourceException
	 */
	void delete(String tableName, long timeFrom, long timeTo) throws AnalyticsDataSourceException;
	
	/**
	 * Delete data in a table with given ids.
	 * @param tableName The name of the table to search on 
	 * @param ids The list of ids of the records to be deleted
	 * @throws AnalyticsDataSourceException
	 */
	void delete(String tableName, List<String> ids) throws AnalyticsDataSourceException;

}
