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
import org.wso2.carbon.analytics.api.AnalyticsDataAPIUtil;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategorySearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.webservice.beans.AggregateResponse;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsAggregateRequest;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsDrillDownRangeBean;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsDrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.beans.CategoryPathBean;
import org.wso2.carbon.analytics.webservice.beans.CategorySearchResultEntryBean;
import org.wso2.carbon.analytics.webservice.beans.EventBean;
import org.wso2.carbon.analytics.webservice.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.beans.SortByFieldBean;
import org.wso2.carbon.analytics.webservice.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.webservice.beans.SubCategoriesBean;
import org.wso2.carbon.analytics.webservice.beans.ValuesBatchBean;
import org.wso2.carbon.analytics.webservice.exception.AnalyticsWebServiceException;
import org.wso2.carbon.analytics.webservice.internal.ServiceHolder;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.stream.core.EventStreamService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is the service class for analytics data service web service.
 * This will expose some methods in the AnalyticsDataAPI as a web service methods.
 */
public class AnalyticsWebService extends AbstractAdmin {
    
    private static final Log logger = LogFactory.getLog(AnalyticsWebService.class);
    private AnalyticsDataAPI analyticsDataAPI;
    private EventStreamService eventStreamService;
    private static final String AT_SIGN = "@";

    public AnalyticsWebService() {
        analyticsDataAPI = ServiceHolder.getAnalyticsDataAPI();
        eventStreamService = ServiceHolder.getEventStreamService();
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
     * Add a stream definition using the Event stream publisher.
     * @param streamDefinitionBean The stream definition bean class.
     */
    public String addStreamDefinition(StreamDefinitionBean streamDefinitionBean)
            throws AnalyticsWebServiceException, MalformedStreamDefinitionException {
        StreamDefinition streamDefinition = Utils.getStreamDefinition(streamDefinitionBean);
        try {
            eventStreamService.addEventStreamDefinition(streamDefinition);
            return streamDefinition.getStreamId();
        } catch (Exception e) {
            logger.error("Unable to set the stream definition: [" + streamDefinition.getName() + ":" +
                         streamDefinition.getVersion()+ "]" + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to set the stream definition: [" +
                         streamDefinition.getName() + ":" + streamDefinition.getVersion()+ "], " +
                         e.getMessage(), e);
        }
    }

    /**
     * get the stream definition using the Event stream publisher.
     * @param name  The name of the stream.
     * @param version The version of the stream
     * @return The stream definition bean
     * @throws AnalyticsWebServiceException
     */
    public StreamDefinitionBean getStreamDefinition(String name, String version)
            throws AnalyticsWebServiceException {
        try {
            StreamDefinition streamDefinition = validateAndGetStreamDefinition(name, version);
            if (streamDefinition != null) {
                return Utils.getStreamDefinitionBean(streamDefinition);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("unable to get the stream definition: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("unable to get the stream definition: " + e.getMessage(), e);
        }
    }

    /**
     * This method can use to remove existing stream definition from system using the Event stream publisher.
     *
     * @param name    The name of the stream.
     * @param version The version of the stream
     * @throws AnalyticsWebServiceException
     */
    public void removeStreamDefinition(String name, String version) throws AnalyticsWebServiceException {
        try {
            eventStreamService.removeEventStreamDefinition(name, version);
        } catch (Exception e) {
            logger.error("Unable to remove stream definition: [" + name + ":" + version + "]" + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to remove stream definition: [" + name + ":" + version + "], " +
                    e.getMessage(), e);
        }
    }

    private StreamDefinition validateAndGetStreamDefinition(String name, String version)
            throws AnalyticsWebServiceException {
        StreamDefinition streamDefinition;
        try {
            if (name != null && version != null) {
                streamDefinition = eventStreamService.getStreamDefinition(name, version);
            } else if (name != null) {
                streamDefinition = eventStreamService.getStreamDefinition(name);
            } else {
                throw new AnalyticsWebServiceException("The stream name is not provided");
            }
        } catch (Exception e) {
            logger.error("Unable to get the stream definition: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get the stream definition: " +
                                                   e.getMessage(), e);
        }
        return streamDefinition;
    }

    /**
     * Publishes events to a given stream represented by stream id.
     * @param eventBean The event bean representing the event data.
     * @throws AnalyticsWebServiceException
     */
    public void publishEvent(EventBean eventBean)
            throws AnalyticsWebServiceException {
        try {
            StreamDefinition streamDefinition = validateAndGetStreamDefinition(eventBean.getStreamName(),
                                                                               eventBean.getStreamVersion());
            Event event = Utils.getEvent(eventBean, streamDefinition);

            eventStreamService.publish(event);
        } catch (Exception e) {
            logger.error("unable to publish event: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("unable to publish event: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the table schema for a table
     * @param schemaBean The schema bean representing the schema
     * @throws AnalyticsWebServiceException
     */
    public void setTableSchema(String tableName, AnalyticsSchemaBean schemaBean)
            throws AnalyticsWebServiceException {
        try {

            AnalyticsSchema schema = Utils.getAnalyticsSchema(schemaBean);

            analyticsDataAPI.setTableSchema(getUsername(), tableName, schema);
        } catch (Exception e) {
            logger.error("unable to set the schema for table: " + tableName + ", " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("unable to set the schema: " + tableName + ", " + e.getMessage(), e);
        }
    }

    /**
     * Returns a list of records containing the aggregate values computed over the given fields map
     * , grouped by a predefined FACET field.
     * @param request The inputs required for performing aggregation.
     * groupByField is used to group the records. It should be a facet field created by the grouping fields.
     * fields attribute represents the record fields and the respective aggregate function.
     * aliases represents the output field names for aggregated values over the fields.
     * @return List of records of which the record values will be the aggregate values of the given fields
     */
    public RecordBean[] searchWithAggregates(AnalyticsAggregateRequest request)
            throws AnalyticsWebServiceException {
        try {
            AggregateRequest aggregateRequest = Utils.getAggregateRequest(request);
            AnalyticsIterator<Record> iterator= analyticsDataAPI.searchWithAggregates(getUsername(), aggregateRequest);
            List<RecordBean> recordBeans = Utils.createRecordBeans(Utils.createList(iterator));
            RecordBean[] resultRecordBeans = new RecordBean[recordBeans.size()];
            return recordBeans.toArray(resultRecordBeans);
        } catch (Exception e) {
            logger.error("unable to search with aggregates for table: " + request.getTableName() + ", " +
                         e.getMessage(), e);
            throw new AnalyticsWebServiceException("unable to search with aggregates: " + request.getTableName() +
                                                   ", " + e.getMessage(), e);
        }
    }

    /**
     * Returns a list of records containing the aggregate values computed over the given fields map
     * , grouped by a predefined FACET field for multiple tables.
     * @param requests The Array of AnalyticsAggregateRequests representing multiple tables
     * groupByField is used to group the records. It should be a facet field created by the grouping fields.
     * fields attribute represents the record fields and the respective aggregate function.
     * aliases represents the output field names for aggregated values over the fields.
     * @return Array of AggregatedObjects containing arrays of records of which the record values will be
     * the aggregate values of the given fields
     */
    public AggregateResponse[] searchMultiTablesWithAggregates(AnalyticsAggregateRequest[] requests)
            throws AnalyticsWebServiceException {
        try {
            AggregateRequest[] aggregateRequest = Utils.getAggregateRequests(requests);
            List<AnalyticsIterator<Record>> iterators = analyticsDataAPI.searchWithAggregates(getUsername(), aggregateRequest);
            AggregateResponse[] responses = Utils.createAggregateResponses(iterators);
            return responses;
        } catch (Exception e) {
            logger.error("unable to search with aggregates for  multiple tables: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("unable to search with aggregates for multiple tables: " + e.getMessage(), e);
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            logger.error("Unable to check table[" + tableName + "] exist due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to check status of the table[" + tableName + "] due to " + e
                    .getMessage(), e);
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
        } catch (Exception e) {
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
            long recordCount = analyticsDataAPI.getRecordCount(getUsername(), tableName, timeFrom, timeTo);
            if (recordCount == -1) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Retrieving record count is not supported for table:" + tableName);
                }
            }
            return recordCount;
        } catch (Exception e) {
            logger.error("Unable to get record count for table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get record count for table[" + tableName + "] due to " + e
                    .getMessage(), e);
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
     * @param recordsCount      The paginated records count to be read, -getUsername() for infinity
     * @return An array of {@link RecordBean} objects, which represents individual data sets in their local location
     * @throws AnalyticsWebServiceException
     */
    public RecordBean[] getByRange(String tableName, int numPartitionsHint, String[] columns, long timeFrom,
                                   long timeTo, int recordsFrom, int recordsCount) throws AnalyticsWebServiceException {

        try {
            List<String> columnList = null;
            if (columns != null && columns.length != 0 && columns[0] != null) {
                columnList = new ArrayList<>(Arrays.asList(columns));
            }
            int originalFrom = recordsFrom;
            if (!isPaginationSupported(getRecordStoreNameByTable(tableName))) {
                recordsCount = recordsFrom + recordsCount;
                recordsFrom = 0;
            }
            AnalyticsDataResponse response = analyticsDataAPI.get(getUsername(), tableName, numPartitionsHint,
                                                                  columnList, timeFrom, timeTo, recordsFrom,
                                                                  recordsCount);
            List<Record> records;
            if (!isPaginationSupported(getRecordStoreNameByTable(tableName))) {
                Iterator<Record> itr = AnalyticsDataAPIUtil.responseToIterator(analyticsDataAPI, response);
                records = new ArrayList<>();
                for (int i = 0; i < originalFrom && itr.hasNext(); i++) {
                    itr.next();
                }
                for (int i = 0; i < recordsCount && itr.hasNext(); i++) {
                    records.add(itr.next());
                }
            } else {
                records = AnalyticsDataAPIUtil.listRecords(analyticsDataAPI, response);
            }
            List<RecordBean> recordBeans = Utils.createRecordBeans(records);
            RecordBean[] resultRecordBeans = new RecordBean[recordBeans.size()];
            return recordBeans.toArray(resultRecordBeans);
        } catch (Exception e) {
            logger.error("Unable to get records from table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get record from table[" + tableName + "] due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Retrieves data from a table, which matches the primary key values batch.
     *
     * @param tableName         The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns           The list of columns to required in results, null if all needs to be returned
     * @param valuesBatchBeans  The values batch containing the key values of primary keys to match
     * @return An array of {@link RecordBean} objects, which represents individual data sets in their local location
     * @throws AnalyticsWebServiceException
     */
    public RecordBean[] getWithKeyValues(String tableName, int numPartitionsHint, String[] columns,
                                         ValuesBatchBean[] valuesBatchBeans) throws AnalyticsWebServiceException {

        try {
            List<String> columnList = null;
            if (columns != null && columns.length != 0 && columns[0] != null) {
                columnList = Arrays.asList(columns);
            }
            List<Map<String, Object>> valuesBatch = Utils.getValuesBatch(valuesBatchBeans,
                                                        analyticsDataAPI.getTableSchema(getUsername(), tableName));
            List<Record> records = AnalyticsDataAPIUtil.listRecords(analyticsDataAPI,
                                                        analyticsDataAPI.getWithKeyValues(getUsername(), tableName, numPartitionsHint,
                                                                                                           columnList, valuesBatch));
            List<RecordBean> recordBeans = Utils.createRecordBeans(records);
            RecordBean[] resultRecordBeans = new RecordBean[recordBeans.size()];
            return recordBeans.toArray(resultRecordBeans);
        } catch (Exception e) {
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
            if (columns != null && columns.length != 0 && columns[0] != null) {
                columnList = new ArrayList<>(Arrays.asList(columns));
            }
            List<String> idList = null;
            if (ids != null) {
                idList = Arrays.asList(ids);
            } else {
                throw new AnalyticsException("Ids cannot be empty!");
            }
            List<Record> records = AnalyticsDataAPIUtil.listRecords(analyticsDataAPI, analyticsDataAPI.get(getUsername(),
                                                     tableName, numPartitionsHint, columnList, idList));
            List<RecordBean> recordBeans = Utils.createRecordBeans(records);
            RecordBean[] resultRecordBeans = new RecordBean[recordBeans.size()];
            return recordBeans.toArray(resultRecordBeans);
        } catch (Exception e) {
            logger.error("Unable to get records from table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get record from table[" + tableName + "] due to " + e
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
        } catch (Exception e) {
            logger.error("Unable to delete records from table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to delete record from table[" + tableName + "] due to " + e
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
            analyticsDataAPI.clearIndexData(getUsername(), tableName);
        } catch (Exception e) {
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
     * @param columns   The columns needed from each record
     * @param sortByFields The fields by which the records needs to be sorted
     * @return An arrays of {@link RecordBean}s
     * @throws AnalyticsWebServiceException
     */
    public RecordBean[] searchWithSorting(String tableName, String query, int start, int count, String[] columns,
                                          SortByFieldBean[] sortByFields)
            throws AnalyticsWebServiceException {
        try {
            List<SearchResultEntry> searchResults = analyticsDataAPI.search(getUsername(), tableName, query,
                    start, count, Utils.getSortByFields(sortByFields));
            String[] recordIds = Utils.getRecordIds(searchResults);
            RecordBean[] recordBeans = getById(tableName, 1, columns, recordIds);
            Map<String, RecordBean> recordBeanMap = Utils.createRecordBeansKeyedWithIds(recordBeans);
            List<RecordBean> sortedRecordBeans = Utils.getSortedRecordBeans(recordBeanMap, searchResults);
            RecordBean[] resultRecordBeans = new RecordBean[sortedRecordBeans.size()];
            return sortedRecordBeans.toArray(resultRecordBeans);
        } catch (Exception e) {
            logger.error("Unable to get search result for table[" + tableName + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get search result from table[" + tableName + "] due to " + e
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
        return searchWithSorting(tableName, query, start, count, null, null);
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            logger.error("An exception occurred: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("An exception occurred: " + e.getMessage(), e);
        }
    }

    /**
     * This method waits until the current indexing operations for a given table is done.
     *
     * @param tableName table being checked
     * @param maxWait Maximum amount of time in milliseconds, -getUsername() for infinity
     * @throws AnalyticsWebServiceException
     */
    public void waitForIndexingForTable(String tableName, long maxWait) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.waitForIndexing(getUsername(), tableName, maxWait);
        } catch (Exception e) {
            logger.error("An exception occurred: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("An exception occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Given the start time and end time, this method will re-index the records of a table.
     * @param tableName the table name of which the records are being re-indexed.
     * @param startTime lowerbound of the timestamp range of records
     * @param endTime upperbound of the timestamp range of records
     * @throws AnalyticsWebServiceException
     * **/
    public void reIndex(String tableName, long startTime, long endTime) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.reIndex(getUsername(), tableName, startTime, endTime);
        } catch (Exception e) {
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
        } catch (Exception e) {
            logger.error("An exception occurred: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("An exception occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Check whether under laying data layer implementation support for pagination or not.
     *
     * @return boolean true or false based on under laying data layer implementation
     */
    public boolean isPaginationSupported(String recordStoreName) throws AnalyticsWebServiceException {
        try {
            return analyticsDataAPI.isPaginationSupported(recordStoreName);
        } catch (Exception e) {
            logger.error("An exception occurred: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("An exception occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Check whether under laying data layer implementation support for get total record count operation or not.
     *
     * @param recordStoreName Record store name
     * @return Record store support for record count or not.
     * @throws AnalyticsWebServiceException
     */
    public boolean isRecordCountSupported(String recordStoreName) throws AnalyticsWebServiceException {
        try {
            return analyticsDataAPI.isRecordCountSupported(recordStoreName);
        } catch (Exception e) {
            logger.error("An exception occurred: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("An exception occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the subcategories of a facet field, given CategoryDrillDownRequestBean
     *
     * @param drillDownRequest The category drilldown object which contains the category drilldown information
     * @return SubCategoriesBean instance that contains the immediate category information
     * @throws AnalyticsWebServiceException
     */
    public SubCategoriesBean drillDownCategories(CategoryDrillDownRequestBean drillDownRequest)
            throws AnalyticsWebServiceException {
        SubCategoriesBean subCategoriesBean = new SubCategoriesBean();
        try {
            SubCategories subCategories = analyticsDataAPI.drillDownCategories(getUsername(),
                                                         getCategoryDrillDownRequest(drillDownRequest));
            subCategoriesBean.setPath(subCategories.getPath());
            if (subCategories.getCategories() != null) {
                subCategoriesBean.setCategories(getSearchResultEntryBeans(subCategories));
                subCategoriesBean.setCategoryCount(subCategories.getCategoryCount());
                subCategoriesBean.setPath(subCategories.getPath());
            }
        } catch (Exception e) {
            logger.error("Unable to get drill down category information for table[" + drillDownRequest.getTableName() + "] and " +
                         "field[" + drillDownRequest.getFieldName() + "] due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get drill down category information for " +
                                                   "table[" + drillDownRequest.getTableName() + "] and " +
                                                   "field[" + drillDownRequest.getFieldName() + "] due to " + e
                                                           .getMessage(), e);
        }
        return subCategoriesBean;
    }

    private CategorySearchResultEntryBean[] getSearchResultEntryBeans(SubCategories subCategories) {
        CategorySearchResultEntryBean[] searchResultEntryBeans =
                new CategorySearchResultEntryBean[subCategories.getCategories().size()];
        int i = 0;
        for (CategorySearchResultEntry searchResultEntry : subCategories.getCategories()) {
            CategorySearchResultEntryBean resultEntryBean = new CategorySearchResultEntryBean();
            resultEntryBean.setCategoryName(searchResultEntry.getCategoryValue());
            resultEntryBean.setScore(searchResultEntry.getScore());
            searchResultEntryBeans[i++] = resultEntryBean;
        }
        return searchResultEntryBeans;
    }

    private CategoryDrillDownRequest getCategoryDrillDownRequest(CategoryDrillDownRequestBean drillDownRequest) {
        CategoryDrillDownRequest categoryDrillDownRequest = new CategoryDrillDownRequest();
        categoryDrillDownRequest.setTableName(drillDownRequest.getTableName());
        categoryDrillDownRequest.setFieldName(drillDownRequest.getFieldName());
        categoryDrillDownRequest.setPath(drillDownRequest.getPath());
        categoryDrillDownRequest.setQuery(drillDownRequest.getQuery());
        categoryDrillDownRequest.setScoreFunction(drillDownRequest.getScoreFunction());
        categoryDrillDownRequest.setStart(drillDownRequest.getStart());
        categoryDrillDownRequest.setCount(drillDownRequest.getCount());
        return categoryDrillDownRequest;
    }

    /**
     * Returns the drill down results of a search query, given AnalyticsDrillDownRequestBean
     *
     * @param drillDownRequest The drilldown object which contains the drilldown information
     * @return An arrays of {@link RecordBean}s
     * @throws AnalyticsWebServiceException
     */
    public RecordBean[] drillDownSearch(AnalyticsDrillDownRequestBean drillDownRequest)
            throws AnalyticsWebServiceException {
        try {
            List<SearchResultEntry> searchResults = analyticsDataAPI.drillDownSearch(getUsername(),
                    getAnalyticsDrillDownRequest(drillDownRequest));
            String[] recordIds = Utils.getRecordIds(searchResults);
            RecordBean[] recordBeans = getById(drillDownRequest.getTableName(), 1, drillDownRequest.getColumns(), recordIds);
            Map<String, RecordBean> recordBeanMap = Utils.createRecordBeansKeyedWithIds(recordBeans);
            List<RecordBean> sortedRecordBeans = Utils.getSortedRecordBeans(recordBeanMap, searchResults);
            RecordBean[] resultRecordBeans = new RecordBean[sortedRecordBeans.size()];
            return sortedRecordBeans.toArray(resultRecordBeans);
        } catch (Exception e) {
            logger.error("Unable to get drill down information due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get drill down information due to " + e.getMessage(), e);
        }
    }

    private AnalyticsDrillDownRequest getAnalyticsDrillDownRequest(AnalyticsDrillDownRequestBean drillDownRequest)
            throws AnalyticsException {
        AnalyticsDrillDownRequest analyticsDrillDownRequest = new AnalyticsDrillDownRequest();
        analyticsDrillDownRequest.setTableName(drillDownRequest.getTableName());
        analyticsDrillDownRequest.setQuery(drillDownRequest.getQuery());
        analyticsDrillDownRequest.setRangeField(drillDownRequest.getRangeField());
        analyticsDrillDownRequest.setRecordCount(drillDownRequest.getRecordCount());
        analyticsDrillDownRequest.setRecordStartIndex(drillDownRequest.getRecordStart());
        analyticsDrillDownRequest.setScoreFunction(drillDownRequest.getScoreFunction());
        if (drillDownRequest.getCategoryPaths() != null) {
            Map<String, List<String>> categoryPath = new HashMap<>();
            for (CategoryPathBean categoryPathBean : drillDownRequest.getCategoryPaths()) {
                categoryPath.put(categoryPathBean.getFieldName(), categoryPathBean.getPath() != null ?
                                                                  Arrays.asList(categoryPathBean.getPath()) : null);
            }
            analyticsDrillDownRequest.setCategoryPaths(categoryPath);
        }
        if (drillDownRequest.getRanges() != null) {
            List<AnalyticsDrillDownRange> drillDownRanges = new ArrayList<>();
            for (AnalyticsDrillDownRangeBean analyticsDrillDownRangeBean : drillDownRequest.getRanges()) {
                AnalyticsDrillDownRange drillDownRange = new AnalyticsDrillDownRange();
                drillDownRange.setLabel(analyticsDrillDownRangeBean.getLabel());
                drillDownRange.setFrom(analyticsDrillDownRangeBean.getFrom());
                drillDownRange.setTo(analyticsDrillDownRangeBean.getTo());
                drillDownRange.setScore(analyticsDrillDownRangeBean.getScore());
                drillDownRanges.add(drillDownRange);
            }
            analyticsDrillDownRequest.setRanges(drillDownRanges);
        }
        analyticsDrillDownRequest.setSortByFields(Utils.getSortByFields(drillDownRequest.getSortByFields()));
        return analyticsDrillDownRequest;
    }

    /**
     * Returns the count of results of a search query, given AnalyticsDrillDownRequestBean
     *
     * @param drillDownRequest The drilldown object which contains the drilldown information
     * @return the count of the records which match the drilldown query
     * @throws AnalyticsWebServiceException
     */
    public double drillDownSearchCount(AnalyticsDrillDownRequestBean drillDownRequest)
            throws AnalyticsWebServiceException {
        try {
            return analyticsDataAPI.drillDownSearchCount(getUsername(), getAnalyticsDrillDownRequest(drillDownRequest));
        } catch (Exception e) {
            logger.error("Unable to get drill down search count information due to " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get drill down search count information due to " + e
                    .getMessage(), e);
        }
    }

    /**
     * Lists all the record stores available in the system.
     *
     * @return The list of record store names
     */
    public List<String> listRecordStoreNames() throws AnalyticsWebServiceException {
        try {
            return analyticsDataAPI.listRecordStoreNames();
        } catch (Exception e) {
            logger.error("Unable to get record store names: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get record store names: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a table, if not already there, where the columns are not defined here, but can contain any arbitrary number
     * of columns when data is added. The table names are not case sensitive.
     *
     * @param recordStoreName The name of the target record store to store the table at
     * @param tableName       The name of the table to be created
     * @throws AnalyticsWebServiceException
     */
    public void createTable(String recordStoreName, String tableName) throws AnalyticsWebServiceException {
        try {
            analyticsDataAPI.createTable(getUsername(), recordStoreName, tableName);
        } catch (Exception e) {
            logger.error("Unable to create table[" + tableName + "] in record store[" + recordStoreName +
                         "]: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to create table[" + tableName +
                                                   "] in record store[" + recordStoreName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the record store name given the table information.
     *
     * @param tableName The table name
     * @return The record store name
     * @throws AnalyticsWebServiceException
     */
    public String getRecordStoreNameByTable(String tableName) throws AnalyticsWebServiceException {
        try {
            return analyticsDataAPI.getRecordStoreNameByTable(getUsername(), tableName);
        } catch (Exception e) {
            logger.error("Unable to get record store name for table[" + tableName + "]: " + e.getMessage(), e);
            throw new AnalyticsWebServiceException("Unable to get record store name for table[" + tableName + "]: " + e.getMessage(), e);
        }
    }
}
