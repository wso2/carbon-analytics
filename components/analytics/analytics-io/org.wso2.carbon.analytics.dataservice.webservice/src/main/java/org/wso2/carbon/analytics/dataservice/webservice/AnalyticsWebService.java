/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.dataservice.webservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceException;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.RecordBean;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.RecordGroupBean;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.SearchResultEntryBean;
import org.wso2.carbon.analytics.dataservice.webservice.exception.AnalyticsWebServiceException;
import org.wso2.carbon.analytics.dataservice.webservice.internal.ServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class is the service class for analytics data service web service.
 * This will expose some methods in the AnalyticsDataAPI as a web service methods.
 */
public class AnalyticsWebService extends AbstractAdmin {
    private static final Log logger = LogFactory.getLog(AnalyticsWebService.class);
    private AnalyticsDataAPI analyticsDataAPI;
    private static final String AT_SIGN = "@";

    public AnalyticsWebService() {
        analyticsDataAPI = ServiceHolder.getAnalyticsDataAPI();
    }

    /**
     * This method is use to get logged in username with tenant domain
     *
     * @return Username with tenant domain
     */
    @Override
    protected String getUsername() {
        return super.getUsername() + AT_SIGN + super.getTenantDomain();
    }

    /**
     * Creates a table, if not already there, where the columns are not defined here, but can contain any arbitrary number
     * of columns when data is added. The table names are not case sensitive.
     *
     * @param tableName The name of the table to be created
     * @throws AnalyticsWebServiceException
     */
    public void createTable(String tableName) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.createTable(1, tableName);
        } catch (Exception e) {
            throw new AnalyticsWebServiceException("Unable to create table due to " + e.getMessage(), e);
        }
    }

    /**
     * Sets the schema for the target analytics table, if there is already one assigned, it will be
     * overwritten.
     *
     * @param tableName The table name
     * @param schema    The schema to be applied to the table
     * @throws AnalyticsWebServiceException
     */
    public void setTableSchema(String tableName, AnalyticsSchemaBean schema) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.setTableSchema(1, tableName, null);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to set table schema due to " + e.getMessage(), e);
        } catch (AnalyticsServiceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the table schema for the given table.
     *
     * @param tableName The table name
     * @return The schema of the table
     * @throws AnalyticsWebServiceException
     */
    public AnalyticsSchemaBean getTableSchema(String tableName)
            throws AnalyticsWebServiceException, AnalyticsServiceException {
        try {
            analyticsDataAPI.getTableSchema(1, tableName);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to get table schema for table[" + tableName + "] due to " + e
                    .getMessage(), e);
        } catch (AnalyticsServiceException e) {
            throw e;
        }

        return null;
    }

    /**
     * Checks if the specified table with the given category and name exists.
     *
     * @param tableName The table name
     * @return true if the table exists, false otherwise
     * @throws AnalyticsWebServiceException
     */
    public boolean tableExists(String tableName) throws AnalyticsWebServiceException, AnalyticsServiceException {
        try {
            return analyticsDataAPI.tableExists(1, tableName);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to check status of the table[" + tableName + "] due to " + e
                    .getMessage(), e);
        } catch (AnalyticsServiceException e) {
            throw e;
        }
    }

    /**
     * Deletes the table with the given category and name if a table exists already.
     * This will not throw an error if the table is not there.
     *
     * @param tableName The name of the table to be dropped
     * @throws AnalyticsWebServiceException
     */
    public void deleteTable(String tableName) throws AnalyticsServiceException, AnalyticsWebServiceException {
        try {
            analyticsDataAPI.deleteTable(1, tableName);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to delete table[" + tableName + "] due to " + e.getMessage(), e);
        } catch (AnalyticsServiceException e) {
            throw e;
        }
    }

    /**
     * Lists all the current tables with the given category.
     *
     * @return The list of table names
     * @throws AnalyticsWebServiceException
     */
    public String[] listTables() throws AnalyticsWebServiceException, AnalyticsServiceException {
        try {
            List<String> tables = analyticsDataAPI.listTables(1);
            if (tables != null) {
                String[] tableNameArray = new String[tables.size()];
                return tables.toArray(tableNameArray);
            } else {
                return new String[0];
            }
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to list tables due to " + e.getMessage(), e);
        } catch (AnalyticsServiceException e) {
            throw e;
        }
    }

    /**
     * Returns the number of records in the table with the given category and name.
     *
     * @param tableName The name of the table to get the count from
     * @param timeFrom  The starting time to consider the count from, inclusive, relatively to epoch,
     *                  Long.MIN_VALUE should signal, this restriction to be disregarded
     * @param timeTo    The ending time to consider the count to, non-inclusive, relatively to epoch,
     *                  Long.MAX_VALUE should signal, this restriction to be disregarded
     * @return The record count
     * @throws AnalyticsWebServiceException
     */
    public long getRecordCount(String tableName, long timeFrom, long timeTo)
            throws AnalyticsWebServiceException {
        try {
            return analyticsDataAPI.getRecordCount(1, tableName, timeFrom, timeTo);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to get record count for table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

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
     * @throws AnalyticsWebServiceException
     */
    public void put(List<RecordBean> records) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.put(null);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to add record due to " + e.getMessage(), e);
        }
    }

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
     * @return An array of {@link RecordGroupBean} objects, which represents individual data sets in their local location
     * @throws AnalyticsWebServiceException
     */
    public RecordGroupBean[] get(String tableName, int numPartitionsHint, List<String> columns, long timeFrom,
                                 long timeTo, int recordsFrom, int recordsCount) throws AnalyticsWebServiceException {

        try {
            analyticsDataAPI.get(1, tableName, numPartitionsHint, columns, timeFrom, timeTo, recordsFrom, recordsCount);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to get record from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
        return null;

    }

    /**
     * Retrieves data from a table with given ids.
     *
     * @param tableName         The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns           The list of columns to required in results, null if all needs to be returned
     * @param ids               The list of ids of the records to be read
     * @return An array of {@link RecordGroupBean} objects, which contains individual data sets in their local location
     * @throws AnalyticsWebServiceException
     */
    public RecordGroupBean[] get(String tableName, int numPartitionsHint, List<String> columns, List<String> ids)
            throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.get(1, tableName, numPartitionsHint, columns, ids);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to get record from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
        return null;
    }

    /**
     * Deletes a set of records in the table.
     *
     * @param tableName The name of the table to search on
     * @param timeFrom  The starting time to get records from for deletion
     * @param timeTo    The ending time to get records to for deletion
     * @throws AnalyticsWebServiceException
     */
    public void delete(String tableName, long timeFrom, long timeTo) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.delete(1, tableName, timeFrom, timeTo);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to delete record from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Delete data in a table with given ids.
     *
     * @param tableName The name of the table to search on
     * @param ids       The list of ids of the records to be deleted
     * @throws AnalyticsWebServiceException
     */
    public void delete(String tableName, String[] ids) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.delete(1, tableName, Arrays.asList(ids));
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to delete record from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Sets the indices for a given table under the given tenant. The indices must be
     * saved in a persistent storage under analytics data service, to be able to lookup the
     * indices later, i.e. these indices should be in-effect after a server restart.
     *
     * @param tableName The table name
     * @param columns   The set of columns to create indices for, and their data types
     * @throws AnalyticsWebServiceException
     */
    public void setIndices(String tableName, Map<String, IndexType> columns) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.setIndices(1, tableName, columns);
        } catch (AnalyticsIndexException e) {
            throw new AnalyticsWebServiceException("Unable to set indices from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Returns the declared indices for a given table under the given tenant.
     *
     * @param tableName The table name
     * @return List of indices of the table
     * @throws AnalyticsWebServiceException
     */
    public Map<String, IndexType> getIndices(String tableName) throws AnalyticsWebServiceException {
        try {
            return analyticsDataAPI.getIndices(1, tableName);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to get indices from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Clears all the indices for the given table.
     *
     * @param tenantId  The tenant id
     * @param tableName The table name
     * @throws AnalyticsWebServiceException
     */
    public void clearIndices(int tenantId, String tableName) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.clearIndices(1, tableName);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to clear indices from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Searches the data with a given search query.
     *
     * @param tableName The table name
     * @param language  The language used to give the search query
     * @param query     The search query
     * @param start     The start location of the result, 0 based
     * @param count     The maximum number of result entries to be returned
     * @return A list of {@link SearchResultEntryBean}s
     * @throws AnalyticsWebServiceException
     */
    public SearchResultEntryBean[] search(String tableName, String language, String query, int start, int count)
            throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.search(1, tableName, language, query, start, count);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("Unable to get search result from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
        return null;
    }

    /**
     * Returns the search count of results of a given search query.
     *
     * @param tableName The table name
     * @param language  The language used to give the search query
     * @param query     The search query
     * @return The count of results
     * @throws AnalyticsWebServiceException
     */
    public int searchCount(String tableName, String language, String query) throws AnalyticsWebServiceException {
        try {
            return analyticsDataAPI.searchCount(1, tableName, language, query);
        } catch (AnalyticsIndexException e) {
            throw new AnalyticsWebServiceException("Unable to get search count from table[" + tableName + "] due to "
                                                   + e.getMessage(), e);
        }
    }

    /**
     * This method waits until the current indexing operations for the system is done.
     *
     * @param maxWait Maximum amount of time in milliseconds, -1 for infinity
     * @throws AnalyticsWebServiceException
     */
    public void waitForIndexing(long maxWait) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.waitForIndexing(maxWait);
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("An exception occurred: " + e.getMessage(), e);
        }
    }


    /**
     * Destroys and frees any resources taken up by the analytics data service implementation.
     *
     * @throws AnalyticsWebServiceException
     */
    public void destroy() throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.destroy();
        } catch (AnalyticsException e) {
            throw new AnalyticsWebServiceException("An exception occurred: " + e.getMessage(), e);
        }
    }
}
