/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.datasource.core.rs;

import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;

import java.util.List;
import java.util.Map;

/**
 * This interface represents all the analytic record related operations.
 */
public interface AnalyticsRecordStore {

    /**
     * This method initializes the AnalyticsRecordStore implementation, and is called once before any other method.
     * @param properties The properties associated with this analytics record store
     * @throws AnalyticsException
     */
    void init(Map<String, String> properties) throws AnalyticsException;
    
    /**
     * Creates a table, if not already there, where the columns are not defined here, but can contain any arbitrary number
     * of columns when data is added. The table names are given already normalized case wise, i.e. the same table will not
     * be referenced with the name just having case mismatches. This allows, record store implementation to ignore case
     * sensitiveness and to normalize the table name itself for storing/retrieving.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to be created
     * @throws AnalyticsException
     */
    void createTable(int tenantId, String tableName) throws AnalyticsException;
    
    /**
     * Deletes the table with the given category and name if a table exists already.
     * This will not throw an error if the table is not there.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to be dropped
     * @throws AnalyticsException
     */
    void deleteTable(int tenantId, String tableName) throws AnalyticsException;
    
    /**
     * Checks whether or not pagination (i.e. jumping to record n and then retrieving k further records)
     * is supported by the underlying record store implementation. Configurable.
     * @return Pagination/row-count support
     */
    boolean isPaginationSupported();
    
    /**
     * Checks whether or not record count operation is supported.
     *
     * @return Record count support
     * @throws AnalyticsException
     */
    boolean isRecordCountSupported() throws AnalyticsException;

    /**
     * Returns the number of records in the table with the given category and name, -1 if the number of records cannot
     * be determined reliably.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to get the count from
     * @param timeFrom The starting time to consider the count from, inclusive, relatively to epoch,
     * Long.MIN_VALUE should signal, this restriction to be disregarded
     * @param timeTo The ending time to consider the count to, non-inclusive, relatively to epoch,
     * Long.MAX_VALUE should signal, this restriction to be disregarded     
     * @return The record count
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Adds a new record to the table. If the record id is mentioned, 
     * it will be used to do the insert, or else, the insert will be done with a randomly generated id.
     * If the record already exists,it updates the record store with the given records, matches by its record id, 
     * this will be a full replace of the record, where the older record is effectively deleted and the new one is
     * added, there will not be a merge of older record's field's with the new one.
     * @param records The list of records to be inserted
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Retrieves data from a table, with a given range.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns The list of columns to required in results, null if all needs to be returned
     * @param timeFrom The starting time to get records from, inclusive, relatively to epoch,
     * Long.MIN_VALUE should signal, this restriction to be disregarded
     * @param timeTo The ending time to get records to, non-inclusive, relatively to epoch,
     * Long.MAX_VALUE should signal, this restriction to be disregarded
     * @param recordsFrom The paginated index from value, zero based, inclusive
     * @param recordsCount The paginated records count to be read, -1 for infinity
     * @return An array of {@link RecordGroup} objects, which represents individual data sets in their local location
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom, 
            long timeTo, int recordsFrom, int recordsCount) 
            throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Retrieves data from a table with given ids.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns The list of columns to required in results, null if all needs to be returned
     * @param ids The list of ids of the records to be read
     * @return An array of {@link RecordGroup} objects, which contains individual data sets in their local location
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Reads in the records from a given record group, the records will be streamed in.
     *
     * @param recordGroup The record group which represents the local data set
     * @return An iterator of type {@link org.wso2.carbon.analytics.datasource.commons.Record} in the local record group
     * @throws org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException
     */
    AnalyticsIterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException;

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
     *  Destroys the AnalyticsRecordStore instance and closes
     *  all connection transients initiated by the AnalyticsRecordStore
     *  implementation
     *  @throws AnalyticsException
     */
    void destroy() throws AnalyticsException;
    
}
