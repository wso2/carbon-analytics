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

import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;
import org.wso2.carbon.analytics.datasource.core.lock.LockProvider;

/**
 * This interface represents the common data store implementations used in analytics. 
 */
public interface AnalyticsDataSource {
    
    /**
     * This method initializes the AnalyticsDataSource implementation, and is called once before any other method.
     * @param properties The properties associated with this analytics data source 
     * @throws AnalyticsDataSourceException
     */
    void init(Map<String, String> properties) throws AnalyticsDataSourceException;
    
    /**
     * Creates a table, if not already there, where the columns are not defined here, but can contain any arbitrary number
     * of columns when data is added. The table names are not case sensitive.
     * @param tableCategoryId The category of the table
     * @param tableName The name of the table to be created
     * @throws AnalyticsDataSourceException
     */
    void createTable(long tableCategoryId, String tableName) throws AnalyticsDataSourceException;
    
    /**
     * Checks if the specified table with the given category and name exists.
     * @param tableCategoryId The table category
     * @param tableName The table name
     * @return true if the table exists, false otherwise
     * @throws AnalyticsDataSourceException
     */
    boolean tableExists(long tableCategoryId, String tableName) throws AnalyticsDataSourceException;
    
    /**
     * Deletes the table with the given category and name if a table exists already.
     * This will not throw an error if the table is not there.
     * @param tableCategoryId The category of the table
     * @param tableName The name of the table to be dropped
     * @throws AnalyticsDataSourceException
     */
    void deleteTable(long tableCategoryId, String tableName) throws AnalyticsDataSourceException;
    
    /**
     * Lists all the current tables with the given category.
     * @param tableCategoryId The category of the tables
     * @return The list of table names
     * @throws AnalyticsDataSourceException
     */
    List<String> listTables(long tableCategoryId) throws AnalyticsDataSourceException;

    /**
     * Returns the number of records in the table with the given category and name.
     * @param tableCategoryId The category of the table
     * @param tableName The name of the table to get the count from
     * @return The record count
     * @throws AnalyticsDataSourceException
     * @throws AnalyticsTableNotAvailableException
     */
    long getRecordCount(long tableCategoryId, String tableName) 
            throws AnalyticsDataSourceException, AnalyticsTableNotAvailableException;
    
    /**
     * Adds a new record to the table. If the record id is mentioned, 
     * it will be used to do an insert/update, or else, an insert will be done with a generated id.
     * @param records The list of records to be inserted
     * @throws AnalyticsDataSourceException
     * @throws AnalyticsTableNotAvailableException
     */
    void put(List<Record> records) throws AnalyticsDataSourceException, AnalyticsTableNotAvailableException;
    
    /**
     * Retrieves data from a table.
     * @param tableCategoryId The category of the table
     * @param tableName The name of the table to search on
     * @param columns The list of columns to required in results, null if all needs to be returned
     * @param timeFrom The starting time to get records from, inclusive, -1 for beginning of time
     * @param timeTo The ending time to get records to, non-inclusive, -1 for infinity
     * @param recordsFrom The paginated index from value, zero based, inclusive
     * @param recordsCount The paginated records count to be read, -1 for infinity
     * @return An array of {@link RecordGroup} objects, which contains individual data sets in their local location
     * @throws AnalyticsDataSourceException
     * @throws AnalyticsTableNotAvailableException
     */
    RecordGroup[] get(long tableCategoryId, String tableName, List<String> columns, long timeFrom, 
            long timeTo, int recordsFrom, int recordsCount) 
            throws AnalyticsDataSourceException, AnalyticsTableNotAvailableException;
    
    /**
     * 
     * Retrieves data from a table with given ids.
     * @param tableCategoryId The category of the table 
     * @param tableName The name of the table to search on
     * @param columns The list of columns to required in results, null if all needs to be returned
     * @param ids The list of ids of the records to be read
     * @return An array of {@link RecordGroup} objects, which contains individual data sets in their local location
     * @throws AnalyticsDataSourceException
     * @throws AnalyticsTableNotAvailableException
     */
    RecordGroup[] get(long tableCategoryId, String tableName, List<String> columns, 
            List<String> ids) throws AnalyticsDataSourceException, AnalyticsTableNotAvailableException;

    /**
     * Deletes a set of records in the table.
     * @param tableCategoryId The category of the table 
     * @param tableName The name of the table to search on 
     * @param timeFrom The starting time to get records from for deletion
     * @param timeTo The ending time to get records to for deletion
     * @throws AnalyticsDataSourceException
     * @throws AnalyticsTableNotAvailableException
     */
    void delete(long tableCategoryId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsDataSourceException, AnalyticsTableNotAvailableException;
    
    /**
     * Delete data in a table with given ids.
     * @param tableCategoryId The category of the table 
     * @param tableName The name of the table to search on 
     * @param ids The list of ids of the records to be deleted
     * @throws AnalyticsDataSourceException
     * @throws AnalyticsTableNotAvailableException
     */
    void delete(long tableCategoryId, String tableName, List<String> ids) 
            throws AnalyticsDataSourceException, AnalyticsTableNotAvailableException;
    
    /**
     * Creates and returns a {@link FileSystem} object to do file related operations.
     * @return The {@link FileSystem} object
     * @throws AnalyticsDataSourceException
     */
    FileSystem getFileSystem() throws AnalyticsDataSourceException;
    
    /**
     * Returns a lock provider for this analytics data source. This can be returned null, and the users
     * of this analytics data source should use a default lock provider, which should usually be an in-memory
     * based implementation or a distributed memory based one for clusters. Only if there is a more efficient
     * implementation of a lock provider is available that is bound to this data source, it should be returned. 
     * @return Natively data source specific {@link LockProvider}
     * @throws AnalyticsLockException
     */
    LockProvider getLockProvider() throws AnalyticsLockException;

}
