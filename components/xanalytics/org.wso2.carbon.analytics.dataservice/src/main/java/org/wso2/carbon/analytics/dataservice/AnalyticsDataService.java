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
package org.wso2.carbon.analytics.dataservice;

import java.util.List;
import java.util.Map;

import org.wso2.carbon.analytics.dataservice.indexing.IndexType;
import org.wso2.carbon.analytics.dataservice.indexing.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;

/**
 * This interface represents the analytics data service operations.
 */
public interface AnalyticsDataService {

    /**
     * Creates a table, if not already there, where the columns are not defined here, but can contain any arbitrary number
     * of columns when data is added. The table names are not case sensitive.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to be created
     * @throws AnalyticsException
     */
    void createTable(int tenantId, String tableName) throws AnalyticsException;
    
    /**
     * Checks if the specified table with the given category and name exists.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The table name
     * @return true if the table exists, false otherwise
     * @throws AnalyticsException
     */
    boolean tableExists(int tenantId, String tableName) throws AnalyticsException;
    
    /**
     * Deletes the table with the given category and name if a table exists already.
     * This will not throw an error if the table is not there.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to be dropped
     * @throws AnalyticsException
     */
    void deleteTable(int tenantId, String tableName) throws AnalyticsException;
    
    /**
     * Lists all the current tables with the given category.
     * @param tenantId The tenant which this table belongs to
     * @return The list of table names
     * @throws AnalyticsException
     */
    List<String> listTables(int tenantId) throws AnalyticsException;

    /**
     * Returns the number of records in the table with the given category and name.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to get the count from
     * @return The record count
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    long getRecordCount(int tenantId, String tableName) 
            throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Adds a new record to the table. If the record id is mentioned, 
     * it will be used to do the insert, or else, the insert will be done with a randomly generated id.
     * @param records The list of records to be inserted
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    void insert(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Updates the record store with the given records, matches by its record ids, this will be a
     * full replace of the record, where the older record is effectively deleted and the new one is
     * added, there will not be a merge of older record's field's with the new one.
     * @param records The records to be updated
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    void update(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Retrieves data from a table.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on
     * @param columns The list of columns to required in results, null if all needs to be returned
     * @param timeFrom The starting time to get records from, inclusive, -1 for beginning of time
     * @param timeTo The ending time to get records to, non-inclusive, -1 for infinity
     * @param recordsFrom The paginated index from value, zero based, inclusive
     * @param recordsCount The paginated records count to be read, -1 for infinity
     * @return An array of {@link RecordGroup} objects, which contains individual data sets in their local location
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    RecordGroup[] get(int tenantId, String tableName, List<String> columns, long timeFrom, 
            long timeTo, int recordsFrom, int recordsCount) 
            throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Retrieves data from a table with given ids.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on
     * @param columns The list of columns to required in results, null if all needs to be returned
     * @param ids The list of ids of the records to be read
     * @return An array of {@link RecordGroup} objects, which contains individual data sets in their local location
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    RecordGroup[] get(int tenantId, String tableName, List<String> columns, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException;

    /**
     * Deletes a set of records in the table.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on 
     * @param timeFrom The starting time to get records from for deletion
     * @param timeTo The ending time to get records to for deletion
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    void delete(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Delete data in a table with given ids.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on 
     * @param ids The list of ids of the records to be deleted
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    void delete(int tenantId, String tableName, List<String> ids) 
            throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Sets the indices for a given table under the given tenant. The indices must be
     * saved in a persistent storage under analytics data service, to be able to lookup the 
     * indices later, i.e. these indices should be in-effect after a server restart.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @param columns The set of columns to create indices for, and their data types
     * @throws AnalyticsIndexException
     */
    void setIndices(int tenantId, String tableName, Map<String, IndexType> columns) throws AnalyticsIndexException;
    
    /**
     * Returns the declared indices for a given table under the given tenant.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @return List of indices of the table
     * @throws AnalyticsIndexException
     */
    Map<String, IndexType> getIndices(int tenantId, String tableName) throws AnalyticsIndexException;
    
    /**
     * Clears all the indices for the given table.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @throws AnalyticsIndexException
     */
    void clearIndices(int tenantId, String tableName) throws AnalyticsIndexException;
    
    /**
     * Searches the data with a given search query.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @param language The language used to give the search query
     * @param query The search query
     * @param start The start location of the result, 0 based
     * @param count The maximum number of result entries to be returned
     * @return A list of {@link SearchResultEntry}s
     * @throws AnalyticsIndexException
     */
    List<SearchResultEntry> search(int tenantId, String tableName, String language, 
            String query, int start, int count) throws AnalyticsIndexException;
    
    /**
     * Destroys and frees any resources taken up by the analytics data service implementation.
     */
    void destroy() throws AnalyticsException;
    
}
