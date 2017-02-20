//TODO:need to decide on a package name
package org.wso2.analytics.api.commons;

import org.wso2.analytics.data.commons.AnalyticsDataService;
import org.wso2.analytics.data.commons.exception.AnalyticsException;
import org.wso2.analytics.data.commons.service.AnalyticsDataResponse;
import org.wso2.analytics.data.commons.service.AnalyticsSchema;
import org.wso2.analytics.data.commons.sources.AnalyticsIterator;
import org.wso2.analytics.data.commons.sources.Record;
import org.wso2.analytics.data.commons.sources.RecordGroup;
import org.wso2.analytics.indexerservice.impl.CarbonIndexerClient;

import java.util.List;
import java.util.Map;

/**
 * This interface represents the interface which is exposed to external parties who wants to use the data service with indexing implementation
 */
public interface AnalyticsDataAPI {

    /**
     * Lists all the record stores available in the system.
     *
     * @return The list of record store names
     */
    List<String> listRecordStoreNames();

    /**
     * Creates a table, if not already there, where the columns are not defined here, but can contain any arbitrary number
     * of columns when data is added. The table names are not case sensitive.
     *
     * @param recordStoreName The name of the target record store to store the table at
     * @param tableName       The name of the table to be created
     * @throws AnalyticsException
     */
    void createTable(String recordStoreName, String tableName) throws AnalyticsException;

    /**
     * @param tableName The name of the table to be created
     * @throws AnalyticsException
     * @see AnalyticsDataService#createTable(String, String). The primary record store is used
     * to create the table.
     */
    void createTable(String tableName) throws AnalyticsException;

    /**
     * Creates the table if it does not already exist in the system.
     *
     * @param recordStoreName The name of the target record store to store the table at
     * @param tableName       The name of the table to be created
     * @throws AnalyticsException
     */
    void createTableIfNotExists(String recordStoreName, String tableName) throws AnalyticsException;


    /**
     * Returns the record store name given the table information.
     *
     * @param tableName The table name
     * @return The record store name
     * @throws AnalyticsException
     */
    String getRecordStoreNameByTable(String tableName) throws AnalyticsException;

    /**
     * Sets the schema for the target analytics table, if there is already one assigned, it will be
     * overwritten.
     *
     * @param tableName The table name
     * @param schema    The schema to be applied to the table
     * @param merge Specify whether to merge the new schema with the older one or replace
     * @throws AnalyticsException
     */
    void setTableSchema(String tableName, CompositeSchema schema, boolean merge) throws AnalyticsException;

    /**
     * Retrieves the table schema for the given table.
     *
     * @param tableName The table name
     * @return The schema of the table
     * @throws AnalyticsException
     */
    CompositeSchema getTableSchema(String tableName) throws AnalyticsException;

    /**
     * Checks if the specified table with the given category and name exists.
     *
     * @param tableName The table name
     * @return true if the table exists, false otherwise
     * @throws AnalyticsException
     */
    boolean tableExists(String tableName) throws AnalyticsException;

    /**
     * Deletes the table with the given category and name if a table exists already.
     * This will not throw an error if the table is not there.
     *
     * @param tableName The name of the table to be dropped
     * @throws AnalyticsException
     */
    void deleteTable(String tableName) throws AnalyticsException;

    /**
     * Lists all the current tables with the given category.
     *
     * @return The list of table names
     * @throws AnalyticsException
     */
    List<String> listTables() throws AnalyticsException;

    /**
     * Adds a new record to the table. If the record id is mentioned,
     * it will be used to do the insert, or else, it will check the table's schema to check for the existence of
     * primary keys, if there are any, the primary keys will be used to derive the id, or else
     * the insert will be done with a randomly generated id.
     * If the record already exists, it updates the record store with the given records, matches by its record id,
     * this will be a full replace of the record, where the older record is effectively deleted and the new one is
     * added, there will not be a merge of older record's field's with the new one.
     *
     * @param records The list of records to be inserted
     * @throws AnalyticsException
     */
    void put(List<Record> records) throws AnalyticsException;

    /**
     * Retrieves data from a table, with a given range.
     *
     * @param tableName         The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns           The list of columns to required in results, null if all needs to be returned
     * @param timeFrom          The starting time to get records from, inclusive, relatively to epoch,
     *                          Long.MIN_VALUE should signal, this restriction to be disregarded
     * @param timeTo            The ending time to get records to, non-inclusive, relatively to epoch,
     *                          Long.MAX_VALUE should signal, this restriction to be disregarded
     * @param recordsFrom       The paginated index from value, zero based, inclusive
     * @param recordsCount      The paginated records count to be read, -1 for infinity
     * @return The analytics data response
     * @throws AnalyticsException
     */
    AnalyticsDataResponse get(String tableName, int numPartitionsHint, List<String> columns, long timeFrom,
                              long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException;

    /**
     * Retrieves data from a table with given ids.
     *
     * @param tableName         The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns           The list of columns to required in results, null if all needs to be returned
     * @param ids               The list of ids of the records to be read
     * @return The analytics data response
     * @throws AnalyticsException
     */
    AnalyticsDataResponse get(String tableName, int numPartitionsHint, List<String> columns,
                              List<String> ids) throws AnalyticsException;

    /**
     * Retrieves data from a table with the values for all the given primary key value composites.
     *
     * @param tableName         The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns           The list of columns to required in results, null if all needs to be returned
     * @param valuesBatch       A batch of key/values which contains the primary keys values to match the data in the table
     * @return The analytics data response
     * @throws AnalyticsException
     */
    AnalyticsDataResponse getWithKeyValues(String tableName, int numPartitionsHint, List<String> columns,
                                           List<Map<String, Object>> valuesBatch) throws AnalyticsException;

    /**
     * Reads in the records from a given record group at a given record store, the records will be streamed in.
     *
     * @param recordStoreName The record store name
     * @param recordGroup     The record group which represents the local data set
     * @return An iterator of type {@link Record} in the local record group
     * @throws AnalyticsException
     */
    AnalyticsIterator<Record> readRecords(String recordStoreName, RecordGroup recordGroup) throws AnalyticsException;

    /**
     * Deletes a set of records in the table.
     *
     * @param tableName The name of the table to search on
     * @param timeFrom  The starting time to get records from for deletion
     * @param timeTo    The ending time to get records to for deletion
     * @throws AnalyticsException
     */
    void delete(String tableName, long timeFrom, long timeTo) throws AnalyticsException;

    /**
     * Delete data in a table with given ids.
     *
     * @param tableName The name of the table to search on
     * @param ids       The list of ids of the records to be deleted
     * @throws AnalyticsException
     */
    void delete(String tableName, List<String> ids) throws AnalyticsException;


    /**
     * Creates a table, if not already there, where the columns are not defined here, but can contain any arbitrary number
     * of columns when data is added. The table names are not case sensitive.
     *
     * @param username The username used to authorize the table creation
     * @param recordStoreName The name of the target record store to store the table at
     * @param tableName       The name of the table to be created
     * @throws org.wso2.analytics.data.commons.exception.AnalyticsException
     */
    void createTable(String username, String recordStoreName, String tableName) throws AnalyticsException;

    /**
     * Creates a table, if not already there, where the columns are not defined here, but can contain any arbitrary number
     * of columns when data is added. The table names are not case sensitive.
     *
     * @param username The username used to authorize the table creation
     * @param tableName The name of the table to be created
     * @throws AnalyticsException
     * @see AnalyticsDataService#createTable(String, String). The primary record store is used
     * to create the table.
     */
    void createTableWithUsername(String username, String tableName) throws AnalyticsException;

    /**
     * Creates the table if it does not already exist in the system.
     *
     * @param username The username used to authorize the table creation
     * @param recordStoreName The name of the target record store to store the table at
     * @param tableName       The name of the table to be created
     * @throws AnalyticsException
     */
    void createTableIfNotExists(String username, String recordStoreName, String tableName) throws AnalyticsException;

    /**
     * Returns the record store name given the table information.
     *
     * @param username The username used to authorize get the recordstore by table
     * @param tableName The table name
     * @return The record store name
     * @throws AnalyticsException
     */
    String getRecordStoreNameByTable(String username, String tableName) throws AnalyticsException;

    /**
     * Sets the schema for the target analytics table, if there is already one assigned, it will be
     * overwritten.
     *
     * @param username The username used to authorize to set tableSchema
     * @param tableName The table name
     * @param schema    The schema to be applied to the table
     *                  @param merge Mark whether to merge or not with the existing schema.
     * @throws AnalyticsException
     */
    void setTableSchema(String username, String tableName, CompositeSchema schema, boolean merge) throws AnalyticsException;

    /**
     * Retrieves the table schema for the given table.
     *
     * @param username The username used to authorize to get the table schema
     * @param tableName The table name
     * @return The schema of the table
     * @throws AnalyticsException
     */
    AnalyticsSchema getTableSchema(String username, String tableName) throws AnalyticsException;

    /**
     * Checks if the specified table with the given category and name exists.
     *
     * @param username The username used to authorize to check  the existence of a table.
     * @param tableName The table name
     * @return true if the table exists, false otherwise
     * @throws AnalyticsException
     */
    boolean tableExists(String username, String tableName) throws AnalyticsException;

    /**
     * Deletes the table with the given category and name if a table exists already.
     * This will not throw an error if the table is not there.
     *
     * @param username The username used to authorize table deletion
     * @param tableName The name of the table to be dropped
     * @throws AnalyticsException
     */
    void deleteTable(String username, String tableName) throws AnalyticsException;

    /**
     * Lists all the current tables with the given category.
     *
     * @param username The username to authorize listing tables
     * @return The list of table names
     * @throws AnalyticsException
     */
    List<String> listTables(String username) throws AnalyticsException;

    /**
     * Adds a new record to the table. If the record id is mentioned,
     * it will be used to do the insert, or else, it will check the table's schema to check for the existence of
     * primary keys, if there are any, the primary keys will be used to derive the id, or else
     * the insert will be done with a randomly generated id.
     * If the record already exists, it updates the record store with the given records, matches by its record id,
     * this will be a full replace of the record, where the older record is effectively deleted and the new one is
     * added, there will not be a merge of older record's field's with the new one.
     *
     * @param username The username used to authorize inserting records to datastore.
     * @param records The list of records to be inserted
     * @throws AnalyticsException
     */
    void put(String username, List<Record> records) throws AnalyticsException;

    /**
     * Retrieves data from a table, with a given range.
     *
     * @param username          The username used to authorize to get records from the table
     * @param tableName         The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns           The list of columns to required in results, null if all needs to be returned
     * @param timeFrom          The starting time to get records from, inclusive, relatively to epoch,
     *                          Long.MIN_VALUE should signal, this restriction to be disregarded
     * @param timeTo            The ending time to get records to, non-inclusive, relatively to epoch,
     *                          Long.MAX_VALUE should signal, this restriction to be disregarded
     * @param recordsFrom       The paginated index from value, zero based, inclusive
     * @param recordsCount      The paginated records count to be read, -1 for infinity
     * @return The analytics data response
     * @throws AnalyticsException
     */
    AnalyticsDataResponse get(String username, String tableName, int numPartitionsHint, List<String> columns, long timeFrom,
                              long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException;

    /**
     * Retrieves data from a table with given ids.
     *
     * @param username          The username used to authorize to get the records
     * @param tableName         The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns           The list of columns to required in results, null if all needs to be returned
     * @param ids               The list of ids of the records to be read
     * @return The analytics data response
     * @throws AnalyticsException
     */
    AnalyticsDataResponse get(String username, String tableName, int numPartitionsHint, List<String> columns,
                              List<String> ids) throws AnalyticsException;

    /**
     * Retrieves data from a table with the values for all the given primary key value composites.
     *
     * @param username          The username used to authorize to get records
     * @param tableName         The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns           The list of columns to required in results, null if all needs to be returned
     * @param valuesBatch       A batch of key/values which contains the primary keys values to match the data in the table
     * @return The analytics data response
     * @throws AnalyticsException
     */
    AnalyticsDataResponse getWithKeyValues(String username, String tableName, int numPartitionsHint, List<String> columns,
                                           List<Map<String, Object>> valuesBatch) throws AnalyticsException;

    /**
     * Reads in the records from a given record group at a given record store, the records will be streamed in.
     *
     * @param username The username used to authorize to read records
     * @param recordStoreName The record store name
     * @param recordGroup     The record group which represents the local data set
     * @return An iterator of type {@link Record} in the local record group
     * @throws AnalyticsException
     */
    AnalyticsIterator<Record> readRecords(String username, String recordStoreName, RecordGroup recordGroup) throws AnalyticsException;

    /**
     * Deletes a set of records in the table.
     *
     * @param username  The username used to authorize to delete the records from a table
     * @param tableName The name of the table to search on
     * @param timeFrom  The starting time to get records from for deletion
     * @param timeTo    The ending time to get records to for deletion
     * @throws AnalyticsException
     */
    void delete(String username, String tableName, long timeFrom, long timeTo) throws AnalyticsException;

    /**
     * Delete data in a table with given ids.
     *
     * @param username  The username used to authorize to delete the records from a table
     * @param tableName The name of the table to search on
     * @param ids       The list of ids of the records to be deleted
     * @throws AnalyticsException
     */
    void delete(String username, String tableName, List<String> ids) throws AnalyticsException;

    /**
     * Destroys and frees any resources taken up by the analytics data service implementation.
     */
    void destroy() throws AnalyticsException;

    /**
     * Invalidates the given table for the current node.
     *
     * @param tableName name of the table
     */
    void invalidateTable(String tableName);

    /**
     * Returns the Solr client.
     *
     * @return A Wrapped solr indexer client to communicate with a solr server
     * @throws AnalyticsException
     */
    CarbonIndexerClient getIndexerClient() throws AnalyticsException;

    /**
     * Returns the solr client authorized with the given username.
     *
     * @param username the username used to authorize the communication between the client and the solr server
     * @return A Wrapped solr indexer client to communicate with a solr server
     * @throws AnalyticsException
     */
    CarbonIndexerClient getIndexerClient(String username) throws AnalyticsException;
}
