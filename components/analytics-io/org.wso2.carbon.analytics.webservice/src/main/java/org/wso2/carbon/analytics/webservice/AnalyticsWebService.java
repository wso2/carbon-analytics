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

package org.wso2.carbon.analytics.webservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.beans.IndexConfigurationBean;
import org.wso2.carbon.analytics.webservice.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.beans.SubCategoriesBean;
import org.wso2.carbon.analytics.webservice.exception.AnalyticsWebServiceException;
import org.wso2.carbon.analytics.webservice.internal.ServiceHolder;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.Arrays;
import java.util.List;

/**
 * This class is the service class for analytics data service web service.
 * This will expose some methods in the AnalyticsDataAPI as a web service methods.
 */
public class AnalyticsWebService extends AbstractAdmin {
    private static final Log logger = LogFactory.getLog(AnalyticsWebService.class);
    private static final int DEFAULT_NUM_PARTITIONS_HINT = 1;
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
            analyticsDataAPI.createTable(getUsername(), tableName);
        } catch (Exception e) {
            logger.error("Unable to create table[" + tableName + "] due to " + e.getMessage(), e);
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
            analyticsDataAPI.setTableSchema(getUsername(), tableName, Utils.createAnalyticsSchema(schema));
        } catch (AnalyticsException e) {
            logger.error("Unable to set table schema[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to set table schema due to " + e.getMessage(), e);
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
            throws AnalyticsWebServiceException {
        try {
            return Utils.createTableSchemaBean(analyticsDataAPI.getTableSchema(getUsername(), tableName));
        } catch (AnalyticsException e) {
            logger.error("Unable to get table schema[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get table schema for table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Checks if the specified table with the given category and name exists.
     *
     * @param tableName The table name
     * @return true if the table exists, false otherwise
     * @throws AnalyticsWebServiceException
     */
    public boolean tableExists(String tableName) throws AnalyticsWebServiceException {
        try {
            return analyticsDataAPI.tableExists(getUsername(), tableName);
        } catch (AnalyticsException e) {
            logger.error("Unable to check table[" + tableName + "] exist due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to check status of the table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Deletes the table with the given category and name if a table exists already.
     * This will not throw an error if the table is not there.
     *
     * @param tableName The name of the table to be dropped
     * @throws AnalyticsWebServiceException
     */
    public void deleteTable(String tableName) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.deleteTable(getUsername(), tableName);
        } catch (AnalyticsException e) {
            logger.error("Unable to delete table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to delete table[" + tableName + "] due to " + e.getMessage(), e);
        }
    }

    /**
     * Lists all the current tables with the given category.
     *
     * @return The list of table names
     * @throws AnalyticsWebServiceException
     */
    public String[] listTables() throws AnalyticsWebServiceException {
        try {
            List<String> tables = analyticsDataAPI.listTables(getUsername());
            if (tables != null) {
                String[] tableNameArray = new String[tables.size()];
                return tables.toArray(tableNameArray);
            } else {
                return new String[0];
            }
        } catch (AnalyticsException e) {
            logger.error("Unable to get table name list due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to list tables due to " + e.getMessage(), e);
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
            return analyticsDataAPI.getRecordCount(getUsername(), tableName, timeFrom, timeTo);
        } catch (AnalyticsException e) {
            logger.error("Unable to get record count for table[" + tableName + "] due to " + e.getMessage(), e);
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
     * @param recordBeans Arrays of RecordBean to be inserted
     * @return Arrays of updated RecordBean with ids
     * @throws AnalyticsWebServiceException
     */
    public RecordBean[] put(RecordBean[] recordBeans) throws AnalyticsWebServiceException {
        try {
            List<RecordBean> recordBeanList = Arrays.asList(recordBeans);
            String username = getUsername();
            List<Record> records = Utils.getRecords(username, recordBeanList);
            analyticsDataAPI.put(username, records);
            recordBeans = Utils.createRecordBeans(records).toArray(recordBeans);
        } catch (AnalyticsException e) {
            logger.error("Unable to put records  due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to add record due to " + e.getMessage(), e);
        }
        return recordBeans;
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
     * @param recordsCount      The paginated records count to be read, -getUsername() for infinity
     * @return An array of {@link RecordBean} objects, which represents individual data sets in their local location
     * @throws AnalyticsWebServiceException
     */
    public RecordBean[] getByRange(String tableName, int numPartitionsHint, String[] columns, long timeFrom,
                                   long timeTo, int recordsFrom, int recordsCount) throws AnalyticsWebServiceException {

        try {
            List<String> columnList = null;
            if (columns != null) {
                columnList = Arrays.asList(columns);
            }
            List<Record> records = GenericUtils.listRecords(analyticsDataAPI,
                                                            analyticsDataAPI.get(getUsername(), tableName, numPartitionsHint, columnList, timeFrom, timeTo, recordsFrom,
                                                                                 recordsCount));
            List<RecordBean> recordBeans = Utils.createRecordBeans(records);
            RecordBean[] resultRecordBeans = new RecordBean[recordBeans.size()];
            return recordBeans.toArray(resultRecordBeans);
        } catch (AnalyticsException e) {
            logger.error("Unable to get records from table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get record from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Retrieves data from a table with given ids.
     *
     * @param tableName         The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns           The list of columns to required in results, null if all needs to be returned
     * @param ids               The list of ids of the records to be read
     * @return An array of {@link RecordBean} objects, which contains individual data sets in their local location
     * @throws AnalyticsWebServiceException
     */
    public RecordBean[] getById(String tableName, int numPartitionsHint, String[] columns, String[] ids)
            throws AnalyticsWebServiceException {
        try {
            List<String> columnList = null;
            if (columns != null) {
                columnList = Arrays.asList(columns);
            }
            List<String> idList = null;
            if (ids != null) {
                idList = Arrays.asList(ids);
            }

            List<Record> records = GenericUtils.listRecords(analyticsDataAPI, analyticsDataAPI.get(getUsername(), tableName,
                                                                                                   numPartitionsHint, columnList, idList));
            List<RecordBean> recordBeans = Utils.createRecordBeans(records);
            RecordBean[] resultRecordBeans = new RecordBean[recordBeans.size()];
            return recordBeans.toArray(resultRecordBeans);
        } catch (AnalyticsException e) {
            logger.error("Unable to get records from table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get record from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Deletes a set of records in the table.
     *
     * @param tableName The name of the table to search on
     * @param timeFrom  The starting time to get records from for deletion
     * @param timeTo    The ending time to get records to for deletion
     * @throws AnalyticsWebServiceException
     */
    public void deleteByRange(String tableName, long timeFrom, long timeTo) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.delete(getUsername(), tableName, timeFrom, timeTo);
        } catch (AnalyticsException e) {
            logger.error("Unable to delete records from table[" + tableName + "] due to " + e.getMessage(), e);
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
    public void deleteByIds(String tableName, String[] ids) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.delete(getUsername(), tableName, Arrays.asList(ids));
        } catch (AnalyticsException e) {
            logger.error("Unable to delete records from table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to delete record from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Sets the indices for a given table under the given tenant. The indices must be
     * saved in a persistent storage under analytics data service, to be able to lookup the
     * indices later, i.e. these indices should be in-effect after a server restart.
     *
     * @param tableName              The table name
     * @param indexConfigurationBean A IndexConfigurationBean that contains a set of columns to create indices for,
     *                               and their data types
     * @throws AnalyticsWebServiceException
     */
    public void setIndices(String tableName, IndexConfigurationBean indexConfigurationBean)
            throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.setIndices(getUsername(), tableName, Utils.getIndices(indexConfigurationBean), Utils.getScoreParam(indexConfigurationBean));
        } catch (AnalyticsIndexException e) {
            logger.error("Unable to set indices for table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to set indices from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Returns the declared indices for a given table under the given tenant.
     *
     * @param tableName The table name
     * @return IndexConfigurationBean that contains arrays of IndexEntryBean and an String array that consist of
     * Score Params
     * @throws AnalyticsWebServiceException
     */
    public IndexConfigurationBean getIndices(String tableName) throws AnalyticsWebServiceException {
        try {
            return Utils.getIndexConfiguration(analyticsDataAPI.getIndices(getUsername(), tableName),
                                               analyticsDataAPI.getScoreParams(getUsername(), tableName));
        } catch (AnalyticsException e) {
            logger.error("Unable to get indices from table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get indices from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Clears all the indices for the given table.
     *
     * @param tableName The table name
     * @throws AnalyticsWebServiceException
     */
    public void clearIndices(String tableName) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.clearIndices(getUsername(), tableName);
        } catch (AnalyticsException e) {
            logger.error("Unable to clear indices from table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to clear indices from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Searches the data with a given search query.
     *
     * @param tableName The table name
     * @param query     The search query
     * @param start     The start location of the result, 0 based
     * @param count     The maximum number of result entries to be returned
     * @return An arrays of {@link RecordBean}s
     * @throws AnalyticsWebServiceException
     */
    public RecordBean[] search(String tableName, String query, int start, int count)
            throws AnalyticsWebServiceException {
        try {
            List<SearchResultEntry> searchResults = analyticsDataAPI.search(getUsername(), tableName, query,
                                                                            start, count);
            List<String> recordIds = Utils.getRecordIds(searchResults);
            List<Record> records = GenericUtils.listRecords(analyticsDataAPI, analyticsDataAPI.get(getUsername(), tableName, DEFAULT_NUM_PARTITIONS_HINT, null, recordIds));
            List<RecordBean> recordBeans = Utils.createRecordBeans(records);
            RecordBean[] resultRecordBeans = new RecordBean[recordBeans.size()];
            return recordBeans.toArray(resultRecordBeans);
        } catch (AnalyticsException e) {
            logger.error("Unable to get search result for table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get search result from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Returns the search count of results of a given search query.
     *
     * @param tableName The table name
     * @param query     The search query
     * @return The count of results
     * @throws AnalyticsWebServiceException
     */
    public int searchCount(String tableName, String query) throws AnalyticsWebServiceException {
        try {
            return analyticsDataAPI.searchCount(getUsername(), tableName, query);
        } catch (AnalyticsIndexException e) {
            logger.error("Unable to get search count for table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get search count from table[" + tableName + "] due to "
                                                   + e.getMessage(), e);
        }
    }

    /**
     * This method waits until the current indexing operations for the system is done.
     *
     * @param maxWait Maximum amount of time in milliseconds, -getUsername() for infinity
     * @throws AnalyticsWebServiceException
     */
    public void waitForIndexing(long maxWait) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.waitForIndexing(maxWait);
        } catch (AnalyticsException e) {
            logger.error("An exception occurred: " + e.getMessage(), e);
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
            logger.error("An exception occurred: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("An exception occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Check whether under laying data layer implementation support for pagination or not.
     *
     * @return boolean true or false based on under laying data layer implementation
     */
    public boolean isPaginationSupported() {
        return analyticsDataAPI.isPaginationSupported();
    }

    /**
     * Returns the subcategories of a facet field, given DrillDownRequestBean
     *
     * @param drillDownRequest The category drilldown object which contains the category drilldown information
     * @return SubCategoriesBean instance that contains the immediate category information
     * @throws AnalyticsWebServiceException
     */
    public SubCategoriesBean drillDownCategories(DrillDownRequestBean drillDownRequest)
            throws AnalyticsWebServiceException {

        CategoryDrillDownRequest categoryDrillDownRequest = new CategoryDrillDownRequest();
        categoryDrillDownRequest.setTableName(drillDownRequest.getTableName());
        categoryDrillDownRequest.setFieldName(drillDownRequest.getFieldName());
        categoryDrillDownRequest.setPath(drillDownRequest.getPath());
        categoryDrillDownRequest.setQuery(drillDownRequest.getQuery());
        categoryDrillDownRequest.setScoreFunction(drillDownRequest.getScoreFunction());

        SubCategoriesBean subCategoriesBean = new SubCategoriesBean();
        try {
            SubCategories subCategories = analyticsDataAPI.drillDownCategories(getUsername(), categoryDrillDownRequest);
            subCategoriesBean.setPath(subCategories.getPath());


            subCategoriesBean.setCategories();
        } catch (AnalyticsIndexException e) {
            logger.error("Unable to get drill down category information for table[" + drillDownRequest.getTableName() + "] and " +
                         "field[" + drillDownRequest.getFieldName() + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get drill down category information for " +
                                                   "table[" + drillDownRequest.getTableName() + "] and " +
                                                   "field[" + drillDownRequest.getFieldName() + "] due to " + e
                                                           .getMessage(), e);
        }
        return subCategoriesBean;
    }
}
