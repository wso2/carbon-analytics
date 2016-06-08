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

package org.wso2.carbon.analytics.jsservice;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.jsservice.beans.AggregateRequestBean;
import org.wso2.carbon.analytics.jsservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.jsservice.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.jsservice.beans.ColumnKeyValueBean;
import org.wso2.carbon.analytics.jsservice.beans.DrillDownRangeBean;
import org.wso2.carbon.analytics.jsservice.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.jsservice.beans.EventBean;
import org.wso2.carbon.analytics.jsservice.beans.IdsWithColumnsBean;
import org.wso2.carbon.analytics.jsservice.beans.QueryBean;
import org.wso2.carbon.analytics.jsservice.beans.RecordBean;
import org.wso2.carbon.analytics.jsservice.beans.ResponseBean;
import org.wso2.carbon.analytics.jsservice.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.jsservice.beans.StreamDefinitionQueryBean;
import org.wso2.carbon.analytics.jsservice.beans.SubCategoriesBean;
import org.wso2.carbon.analytics.jsservice.exception.JSServiceException;
import org.wso2.carbon.analytics.jsservice.internal.ServiceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class will contain all the analytics data service APIs which will be called from Analytics.jag.
 */
public class AnalyticsJSServiceConnector {

    private Log logger = LogFactory.getLog(AnalyticsJSServiceConnector.class);
    private AnalyticsDataAPI analyticsDataAPI;
    private EventStreamService eventStreamService;
    private Gson gson;

    public AnalyticsJSServiceConnector() {
        analyticsDataAPI = ServiceHolder.getAnalyticsDataAPI();
        eventStreamService = ServiceHolder.getEventStreamService();
        gson = new Gson();
    }

    public ResponseBean tableExists(String username, String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking tableExists for table: " + tableName);
        }
        try {
            boolean tableExists = analyticsDataAPI.tableExists(username, tableName);
            if (logger.isDebugEnabled()) {
                logger.debug("Table's Existance : " + tableExists);
            }
            if (!tableExists) {
                return handleResponse(ResponseStatus.NON_EXISTENT,
                                      "Table : " + tableName + " does not exist.");
            }
        } catch (AnalyticsException e) {
            logger.error("Failed to check the existance of the table: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to check the existance of table: " +
                                                         tableName + ": " + e.getMessage());
        }
        return handleResponse(ResponseStatus.SUCCESS,
                              "Table : " + tableName + " exists.");
    }

    /*This is for tenant specific functionalities. Given the tenant user, start the tenant flow and
   get the tenant specific stream definition*/
    public ResponseBean getStreamDefinition(String username, String requestAsString) {
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        // get super tenant context and get realm service which is an osgi service
        RealmService realmService = (RealmService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
        boolean tenantFlowStarted = false;
        try {
            if (realmService != null) {
                int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                tenantFlowStarted = Utils.startTenantFlow(tenantId);
            }
            return this.getStreamDefinition(requestAsString);
        } catch (UserStoreException e) {
            logger.error("Failed to add the stream definition: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to add the stream definition: " +
                                                         ": " + e.getMessage());
        } finally {
            if (tenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public ResponseBean getStreamDefinition(String requestAsString) {
        try {
            if (requestAsString != null && !requestAsString.isEmpty()) {
                StreamDefinitionQueryBean queryBean = gson.fromJson(requestAsString, StreamDefinitionQueryBean.class);
                if (logger.isDebugEnabled()) {
                    logger.debug("invoking getStreamDefinition for name: " + queryBean.getName() + " version: " +
                                 queryBean.getVersion());
                }
                StreamDefinition streamDefinition =
                        validateAndGetStreamDefinition(queryBean.getName(), queryBean.getVersion());
                StreamDefinitionBean streamDefinitionBean = Utils.getStreamDefinitionBean(streamDefinition);
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(streamDefinitionBean));
            } else {
                return handleResponse(ResponseStatus.NON_EXISTENT, "Name of the Stream is not given");
            }
        } catch (Exception e) {
            logger.error("Failed to get the stream definition: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get the stream definition: " +
                                                         ": " + e.getMessage());
        }
    }


    private StreamDefinition validateAndGetStreamDefinition(String name, String version)
            throws JSServiceException {
        StreamDefinition streamDefinition;
        try {
            if (name != null && version != null) {
                streamDefinition = eventStreamService.getStreamDefinition(name, version);
            } else if (name != null) {
                streamDefinition = eventStreamService.getStreamDefinition(name);
            } else {
                throw new JSServiceException("The stream name is not provided");
            }
        } catch (Exception e) {
            logger.error("Unable to get the stream definition: " + e.getMessage(), e);
            throw new JSServiceException("Unable to get the stream definition: " +
                                                   e.getMessage(), e);
        }
        return streamDefinition;
    }

    public ResponseBean getTableList(String username) {
        List<String> tableList;
        try {
            tableList = analyticsDataAPI.listTables(username);
        } catch (Exception e) {
            logger.error("Unable to get table list:" + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Unable to get table list: " + e.getMessage());
        }
        if (tableList == null || tableList.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received an empty table name list!");
            }
            tableList = new ArrayList<>();
        }
        return handleResponse(ResponseStatus.SUCCESS, gson.toJson(tableList));
    }

    public ResponseBean getRecordStoreList() {
        List<String> recordStoreList;
        try {
            recordStoreList = analyticsDataAPI.listRecordStoreNames();
        } catch (Exception e) {
            logger.error("Unable to get recordStore list:" + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Unable to get recordStore list: " + e.getMessage());
        }
        if (recordStoreList == null || recordStoreList.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received an empty recordStore name list!");
            }
            recordStoreList = new ArrayList<>();
        }
        return handleResponse(ResponseStatus.SUCCESS, gson.toJson(recordStoreList));
    }

    public ResponseBean getRecordStoreByTable(String username, String tableName) {
        String recordStore;
        try {
            recordStore = analyticsDataAPI.getRecordStoreNameByTable(username, tableName);
        } catch (Exception e) {
            logger.error("Unable to get recordStore for table '" + tableName + "': " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Unable to get recordStore for table '" + tableName +
                                                         "': " + e.getMessage());
        }
        if (recordStore == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received an empty recordStore name list!");
            }
            recordStore = "";
        }
        return handleResponse(ResponseStatus.SUCCESS, gson.toJson(recordStore));
    }

    public ResponseBean getRecordCount(String username, String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordCount for tableName: " + tableName);
        }
        try {
            long recordCount = analyticsDataAPI.getRecordCount(username, tableName, Long.MIN_VALUE, Long.MAX_VALUE);
            if (logger.isDebugEnabled()) {
                logger.debug("RecordCount for tableName: " + tableName + " is " + recordCount);
            }
            return handleResponse(ResponseStatus.SUCCESS, (new Long(recordCount)).toString());
        } catch (Exception e) {
            logger.error("Failed to get record count for table: " + tableName + ": " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get record count for table: " +
                                                         tableName + ": " + e.getMessage());
        }
    }

    public ResponseBean getRecordsByRange(String username, String tableName, String timeFrom, String timeTo, String recordsFrom,
                                    String count, String columns) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordByRange for tableName: " + tableName);
        }
        try {
            long from = validateNumericValue("timeFrom", timeFrom);
            long to = validateNumericValue("timeTo", timeTo);
            int start = validateNumericValue("start", recordsFrom).intValue();
            int recordCount = validateNumericValue("count", count).intValue();
            AnalyticsDataResponse response;
            Type columnType = new TypeToken<ArrayList<String>>() {
            }.getType();
            List<String> columnList = gson.fromJson(columns, columnType);
            columnList = (columnList == null || columnList.isEmpty()) ? null : columnList;
            response = analyticsDataAPI.get(username, tableName, 1, columnList,
                                                                              from, to, start, recordCount);
            List<Record> records;
            if (!analyticsDataAPI.isPaginationSupported(analyticsDataAPI.getRecordStoreNameByTable(username, tableName))) {
                Iterator<org.wso2.carbon.analytics.datasource.commons.Record> itr =
                        AnalyticsDataServiceUtils.responseToIterator(analyticsDataAPI, response);
                records = new ArrayList<>();
                for (int i = 0; i < start && itr.hasNext(); i++) {
                    itr.next();
                }
                for (int i = 0; i < recordCount && itr.hasNext(); i++) {
                    records.add(itr.next());
                }
            } else {
                records = AnalyticsDataServiceUtils.listRecords(analyticsDataAPI, response);
            }
            return handleResponse(ResponseStatus.SUCCESS, gson.toJson(Utils.getRecordBeans(records)));

        } catch (Exception e) {
            logger.error("failed to get records from table: '" + tableName + "', " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: '" +
                                                         tableName + "', " + e.getMessage());
        }
    }

    public ResponseBean getWithKeyValues(String username, String tableName, String valuesBatch) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordByRange for tableName: " + tableName);
        }
        try {
            AnalyticsDataResponse response;
            if (valuesBatch != null) {
                ColumnKeyValueBean columnKeyValueBean = gson.fromJson(valuesBatch, ColumnKeyValueBean.class);
                List<Map<String, Object>> valueBatchList = columnKeyValueBean.getValueBatches();
                if (valueBatchList != null && !valueBatchList.isEmpty()) {
                    response = analyticsDataAPI.getWithKeyValues(username, tableName, 1, columnKeyValueBean.getColumns(),
                                                                 columnKeyValueBean.getValueBatches());
                    List<Record> records = AnalyticsDataServiceUtils.listRecords(analyticsDataAPI, response);
                    return handleResponse(ResponseStatus.SUCCESS, gson.toJson(Utils.getRecordBeans(records)));
                } else {
                    throw new JSServiceException("Values batch is null or empty");
                }
            } else {
                throw new JSServiceException("Values batch is not provided");
            }
        } catch (Exception e) {
            logger.error("failed to get records from table: '" + tableName + "', " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: '" +
                                                         tableName + "', " + e.getMessage());
        }
    }

    private Long validateNumericValue(String field, String value) throws JSServiceException {
        if (value == null || !NumberUtils.isNumber(value)) {
            throw new JSServiceException("'" + field + "' is not numeric (value is: " + value + ")");
        } else {
            return Long.parseLong(value);
        }
    }

    public ResponseBean getRecordsByIds(String username, String tableName, String idsAsString) {
        if (idsAsString != null && !idsAsString.isEmpty()) {
            IdsWithColumnsBean bean = new IdsWithColumnsBean();
            try {
                try {
                    Type idsType = new TypeToken<List<String>>() {
                    }.getType();
                    List<String> ids = gson.fromJson(idsAsString, idsType);
                    if (ids != null && !ids.isEmpty()) {
                        bean.setIds(new ArrayList<String>(ids));
                    } else {
                        bean.setIds(new ArrayList<String>(0));
                    }
                } catch (JsonSyntaxException e) {
                    bean = gson.fromJson(idsAsString, IdsWithColumnsBean.class);
                    if (bean.getColumns() == null || bean.getColumns().isEmpty()) {
                        bean.setColumns(null);
                    }
                    if (bean.getIds() == null || bean.getIds().isEmpty()) {
                        bean.setIds(new ArrayList<String>(0));
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Invoking getRecordsByIds for tableName: " + tableName);
                }
                AnalyticsDataResponse response = analyticsDataAPI.get(username, tableName, 1, bean.getColumns(), bean.getIds());
                List<Record> records = AnalyticsDataServiceUtils.listRecords(analyticsDataAPI, response);
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(Utils.getRecordBeans(records)));
            } catch (Exception e) {
                logger.error("failed to get records from table: " + tableName + " : " + e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: " +
                                                             tableName + ": " + e.getMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "Id list is empty");
        }
    }

    public ResponseBean clearIndexData(String username, String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking clearIndexData for tableName : " +
                         tableName);
        }
        try {
            analyticsDataAPI.clearIndexData(username, tableName);
            return handleResponse(ResponseStatus.SUCCESS, "Successfully cleared indices in table: " +
                                                          tableName);
        } catch (Exception e) {
            logger.error("Failed to clear indices for table: " + tableName + ": " + e.getMessage());
            return handleResponse(ResponseStatus.FAILED, "Failed to clear indices for table: " +
                                                         tableName + ": " + e.getMessage());
        }
    }

    public ResponseBean search(String username, String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                QueryBean queryBean = gson.fromJson(queryAsString, QueryBean.class);
                List<SearchResultEntry> searchResults = analyticsDataAPI.search(username, tableName, queryBean.getQuery(),
                                                                            queryBean.getStart(),
                                                                            queryBean.getCount(),
                                                                            Utils.getSortedFields(queryBean.getSortBy()));
                List<RecordBean> recordBeans = getRecordBeans(username, tableName, queryBean.getColumns(), searchResults);
                if (logger.isDebugEnabled()) {
                    for (RecordBean record : recordBeans) {
                        logger.debug("Search Result -- Record Id: " + record.getId() + " values :" +
                                     record.toString());
                    }
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(recordBeans));
            } catch (Exception e) {
                logger.error("Failed to perform search on table: " + tableName + " : " +
                             e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform search on table: " + tableName + ": " + e.getMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "Search parameters are not provided");
        }
    }

    public ResponseBean searchWithAggregates(String username, String tableName, String requestAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search with aggregate for tableName : " + tableName);
        }
        if (requestAsString != null && !requestAsString.isEmpty()) {
            try {
                AggregateRequestBean aggregateRequest = gson.fromJson(requestAsString, AggregateRequestBean.class);
                AggregateRequest request = Utils.getAggregateRequest(aggregateRequest, tableName);
                List<Record> records = Utils.createList(analyticsDataAPI.searchWithAggregates(username, request));
                List<RecordBean> recordBeans = Utils.getRecordBeans(records);
                if (logger.isDebugEnabled()) {
                    for (RecordBean record : recordBeans) {
                        logger.debug("Search Result -- Record Id: " + record.getId() + " values :" +
                                     record.toString());
                    }
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(recordBeans));
            } catch (Exception e) {
                logger.error("Failed to perform search with aggregate on table: " + tableName + " : " +
                             e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform search with aggregate on table: " + tableName + ": " +
                                      e.getMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "Search parameters are not provided");
        }
    }

    public ResponseBean searchMultiTablesWithAggregates(String username, String requestsAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search with aggregate for multiple tables");
        }
        if (requestsAsString != null && !requestsAsString.isEmpty()) {
            try {
                Type aggregateRequestsType = new TypeToken<AggregateRequestBean[]>(){}.getType();
                AggregateRequestBean[] aggregateRequests = gson.fromJson(requestsAsString, aggregateRequestsType);
                AggregateRequest[] requests = Utils.getAggregateRequests(aggregateRequests);
                List<AnalyticsIterator<Record>> iterators = analyticsDataAPI.searchWithAggregates(username, requests);
                List<List<RecordBean>> aggregatedRecords = Utils.getAggregatedRecordsForMultipleTables(iterators);
                if (logger.isDebugEnabled()) {
                    for (List<RecordBean> recordsPerTable : aggregatedRecords) {
                        for (RecordBean recordBean : recordsPerTable) {
                            logger.debug("Search Result -- Record Id: " + recordBean.getId() + " values :" +
                                         recordBean.toString());
                        }
                    }
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(aggregatedRecords));
            } catch (Exception e) {
                logger.error("Failed to perform search with aggregate on multiple tables: " + e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform search with aggregate on multiple tables: " + e.getMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "Search parameters are not provided");
        }
    }

    public ResponseBean reIndex(String username, String tableName, String startTime, String endTime) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking re-index for tableName : " + tableName);
        }
        if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
            try {
                long start = Long.parseLong(startTime);
                long end = Long.parseLong(endTime);
                analyticsDataAPI.reIndex(username, tableName, start, end);
                return handleResponse(ResponseStatus.SUCCESS, "Re-Indexing...");
            } catch (Exception e) {
                logger.error("Failed to re-index records for table: " + tableName +
                             " : " + e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      " Failed to re-index records for table: " + tableName + ": " + e.getMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, " Search parameters not provided");
        }
    }

    public ResponseBean searchCount(String username, String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search count for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                QueryBean queryBean = gson.fromJson(queryAsString, QueryBean.class);
                int result = analyticsDataAPI.searchCount(username, tableName, queryBean.getQuery());
                if (logger.isDebugEnabled()) {
                    logger.debug("Search count : " + result);
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(result));
            } catch (Exception e) {
                logger.error("Failed to get the record count for table: " + tableName +
                             " : " + e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      " Failed to get the record count for table: " + tableName + ": " + e.getMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, " Search parameters not provided");
        }
    }

    public ResponseBean waitForIndexing(long seconds) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking waiting for indexing - timeout : " + seconds + " seconds");
        }
        try {
            analyticsDataAPI.waitForIndexing(seconds * Constants.MILLISECONDSPERSECOND);
            return handleResponse(ResponseStatus.SUCCESS, "Indexing Completed successfully");
        } catch (Exception e) {
            logger.error("Failed to wait till indexing finishes: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED,
                                  "Failed to wait till indexing finishes: " + e.getMessage());
        }
    }

    public ResponseBean waitForIndexingForTable(String username, String tableName, long seconds) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking waiting for indexing - timeout : " + seconds + " seconds for table: " + tableName);
        }
        try {
            analyticsDataAPI.waitForIndexing(username, tableName, seconds * Constants.MILLISECONDSPERSECOND);
            return handleResponse(ResponseStatus.SUCCESS, "Indexing Completed successfully");
        } catch (Exception e) {
            logger.error("Failed to wait till indexing finishes: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED,
                                  "Failed to wait till indexing finishes: " + e.getMessage());
        }
    }

    public String setTableSchema(String username, String tableName, String schemaAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking setTableSchema for tableName : " + tableName);
        }
        if (schemaAsString != null) {
            try {
                AnalyticsSchemaBean analyticsSchemaBean = gson.fromJson(schemaAsString, AnalyticsSchemaBean.class);
                AnalyticsSchema
                        analyticsSchema = Utils.getAnalyticsSchema(analyticsSchemaBean);
                analyticsDataAPI.setTableSchema(username, tableName, analyticsSchema);
                return gson.toJson(handleResponse(ResponseStatus.SUCCESS, "Successfully set table schema for table: "
                                                                          + tableName));
            } catch (Exception e) {
                logger.error("Failed to set the table schema for table: " + tableName + " : " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, " Failed to set table schema for table: " +
                                                                         tableName + ": " + e.getMessage()));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Table schema is not provided"));
        }
    }

    public ResponseBean getTableSchema(String username, String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getTableSchema for table : " + tableName);
        }
        try {
            AnalyticsSchema
                    analyticsSchema = analyticsDataAPI.getTableSchema(username, tableName);
            AnalyticsSchemaBean analyticsSchemaBean = Utils.createTableSchemaBean(analyticsSchema);
            return handleResponse(ResponseStatus.SUCCESS, gson.toJson(analyticsSchemaBean));
        } catch (Exception e) {
            logger.error("Failed to get the table schema for table: " + tableName + " : " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get the table schema for table: " +
                                                         tableName + ": " + e.getMessage());
        }
    }

    public ResponseBean isPaginationSupported(String recordStoreName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking isPaginationSupported for recordStore: " + recordStoreName);
        }
        try {
            if (recordStoreName == null) {
                throw new JSServiceException("RecordStoreName is not mentioned");
            }
            return handleResponse(ResponseStatus.SUCCESS, gson.toJson(analyticsDataAPI.isPaginationSupported
                    (recordStoreName)));
        } catch (Exception e) {
            logger.error("Failed to check pagination support: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED,
                                  "Failed to check pagination support: " + e.getMessage());
        }
    }

    public ResponseBean drillDownCategories(String username, String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drillDownCategories for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                CategoryDrillDownRequestBean queryBean =
                         gson.fromJson(queryAsString, CategoryDrillDownRequestBean.class);
                CategoryDrillDownRequest requestBean =
                        Utils.createCategoryDrillDownRequest(tableName, queryBean);
                SubCategories searchResults =
                        analyticsDataAPI.drillDownCategories(username, requestBean);
                SubCategoriesBean subCategories = Utils.getSubCategories(searchResults);
                if (logger.isDebugEnabled()) {
                    logger.debug("DrilldownCategory Result -- path: " + Arrays.toString(subCategories.getCategoryPath()) +
                                 " values :" + subCategories.getCategories());

                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(subCategories));
            } catch (Exception e) {
                logger.error("Failed to perform categoryDrilldown on table: " + tableName + " : " +
                             e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform Category Drilldown on table: " +
                                      tableName + ": " + e.getMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "Category drilldown parameters " +
                                                         "are not provided");
        }
    }

    public ResponseBean drillDownSearch(String username, String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drillDownSearch for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                DrillDownRequestBean queryBean =
                        gson.fromJson(queryAsString, DrillDownRequestBean.class);
                AnalyticsDrillDownRequest request =
                        Utils.createDrillDownSearchRequest(tableName, queryBean);
                List<SearchResultEntry> searchResults =
                        analyticsDataAPI.drillDownSearch(username, request);
                List<RecordBean> recordBeans = getRecordBeans(username, tableName, queryBean.getColumns(), searchResults);
                if (logger.isDebugEnabled()) {
                    for (RecordBean record : recordBeans) {
                        logger.debug("Drilldown Search Result -- Record Id: " + record.getId() + " values :" +
                                     record.toString());
                    }
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(recordBeans));
            } catch (Exception e) {
                logger.error("Failed to perform DrilldownSearch on table: " + tableName + " : " +
                             e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform DrilldownSearch on table: " +
                                      tableName + ": " + e.getMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "drilldownSearch parameters " +
                                                         "are not provided");
        }
    }

    public ResponseBean drillDownRangeCount(String username, String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drillDownRangeCount for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                DrillDownRequestBean queryBean =
                        gson.fromJson(queryAsString, DrillDownRequestBean.class);
                AnalyticsDrillDownRequest request =
                        Utils.createDrillDownSearchRequest(tableName, queryBean);
                List<AnalyticsDrillDownRange> searchResults =
                        analyticsDataAPI.drillDownRangeCount(username, request);
                List<DrillDownRangeBean> ranges = Utils.getDrilldownRangeBean(searchResults);
                if (logger.isDebugEnabled()) {
                    for (DrillDownRangeBean rangeBean : ranges) {
                        logger.debug("Drilldown Range count Result -- Range Label: " + rangeBean.getLabel() + " from :" +
                                     rangeBean.getFrom() + " to : " + rangeBean.getTo() + " score : " + rangeBean.getCount());
                    }
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(ranges));
            } catch (Exception e) {
                logger.error("Failed to perform DrilldownRangeCount on table: " + tableName + " : " +
                             e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform DrilldownRangeCount on table: " +
                                      tableName + ": " + e.getMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "drilldownRangeCount parameters " +
                                                         "are not provided");
        }
    }

    private List<RecordBean> getRecordBeans(String username, String tableName, List<String> columns,
                                            List<SearchResultEntry> searchResults)
            throws AnalyticsException {
        List<String> ids = Utils.getIds(searchResults);
        List<String> requiredColumns = (columns == null || columns.isEmpty()) ? null : columns;
        AnalyticsDataResponse response = analyticsDataAPI.get(username, tableName, 1, requiredColumns, ids);
        List<Record> records = AnalyticsDataServiceUtils.listRecords(analyticsDataAPI, response);
        Map<String, RecordBean> recordBeanMap = Utils.getRecordBeanKeyedWithIds(records);
        return Utils.getSortedRecordBeans(recordBeanMap, searchResults);
    }

    public ResponseBean drillDownSearchCount(String username, String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drillDownCategories for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                DrillDownRequestBean queryBean =
                        gson.fromJson(queryAsString, DrillDownRequestBean.class);
                AnalyticsDrillDownRequest requestBean =
                        Utils.createDrillDownSearchRequest(tableName, queryBean);
                double count =
                        analyticsDataAPI.drillDownSearchCount(username, requestBean);
                if (logger.isDebugEnabled()) {
                    logger.debug("Search count Result -- Record Count: " + count);
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(count));
            } catch (Exception e) {
                logger.error("Failed to perform DrilldownSearch Count on table: " + tableName + " : " +
                             e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform DrilldownSearch Count on table: " +
                                      tableName + ": " + e.getMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "drilldownSearch parameters " +
                                                         "are not provided");
        }
    }

    /*This is for tenant specific functionalities. Given the tenant user, start the tenant flow and
    add the tenant specific stream definition*/
    public ResponseBean addStreamDefinition(String username, String streamDefAsString) {
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        // get super tenant context and get realm service which is an osgi service
        RealmService realmService = (RealmService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
        boolean tenantFlowStarted = false;
        try {
            if (realmService != null) {
                int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                tenantFlowStarted = Utils.startTenantFlow(tenantId);
            }
            return this.addStreamDefinition(streamDefAsString);
        } catch (UserStoreException e) {
            logger.error("Failed to add the stream definition: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to add the stream definition: " +
                                                         ": " + e.getMessage());
        } finally {
            if (tenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public ResponseBean addStreamDefinition(String streamDefAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("invoking addStreamDefinition");
        }
        try {
            if (streamDefAsString != null && !streamDefAsString.isEmpty()) {
                StreamDefinitionBean streamDefinitionBean = gson.fromJson(streamDefAsString, StreamDefinitionBean.class);
                StreamDefinition streamDefinition = Utils.getStreamDefinition(streamDefinitionBean);
                eventStreamService.addEventStreamDefinition(streamDefinition);
                String streamId = streamDefinition.getStreamId();
                return handleResponse(ResponseStatus.CREATED, streamId);
            } else {
                return handleResponse(ResponseStatus.NON_EXISTENT, "StreamDefinition is not given");
            }
        } catch (Exception e) {
            logger.error("Failed to add the stream definition: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to add the stream definition: " +
                                                         ": " + e.getMessage());
        }
    }

    /*This is for tenant specific functionalities. Given the tenant user, start the tenant flow and
   publish event*/
    public ResponseBean publishEvent(String username, String eventAsString) {
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        // get super tenant context and get realm service which is an osgi service
        RealmService realmService = (RealmService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
        boolean tenantFlowStarted = false;
        try {
            if (realmService != null) {
                int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                tenantFlowStarted = Utils.startTenantFlow(tenantId);
            }
            return this.publishEvent(eventAsString);
        } catch (UserStoreException e) {
            logger.error("Failed to add the stream definition: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to add the stream definition: " +
                                                         ": " + e.getMessage());
        } finally {
            if (tenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public ResponseBean publishEvent(String eventAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("invoking publishEvent");
        }
        try {
            if (eventAsString != null && !eventAsString.isEmpty()) {
                EventBean eventBean = gson.fromJson(eventAsString, EventBean.class);
                if (logger.isDebugEnabled()) {
                    logger.debug("publishing event: stream : " + eventBean.getStreamName() + ", version: " +
                                 eventBean.getStreamVersion());
                }
                StreamDefinition streamDefinition = eventStreamService.getStreamDefinition(eventBean.getStreamName(), eventBean.getStreamVersion());
                eventStreamService.publish(Utils.getStreamEvent(streamDefinition, eventBean));
                return handleResponse(ResponseStatus.SUCCESS, "Event published successfully");

            } else {
                return handleResponse(ResponseStatus.NON_EXISTENT, "Stream event is not provided");
            }
        } catch (Exception e) {
            logger.error("Failed to publish event: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to publish event: " +
                                                         ": " + e.getMessage());
        }
    }

    public ResponseBean handleResponse(ResponseStatus responseStatus, String message) {
        ResponseBean response;
        switch (responseStatus) {
            case CONFLICT:
                response = getResponseMessage(Constants.Status.FAILED, 409, message);
                break;
            case CREATED:
                response = getResponseMessage(Constants.Status.CREATED, 201, message);
                break;
            case SUCCESS:
                response = getResponseMessage(Constants.Status.SUCCESS, 200, message);
                break;
            case FAILED:
                response = getResponseMessage(Constants.Status.FAILED, 500, message);
                break;
            case INVALID:
                response = getResponseMessage(Constants.Status.FAILED, 400, message);
                break;
            case FORBIDDEN:
                response = getResponseMessage(Constants.Status.UNAUTHORIZED, 403, message);
                break;
            case UNAUTHENTICATED:
                response = getResponseMessage(Constants.Status.UNAUTHENTICATED, 403, message);
                break;
            case NON_EXISTENT:
                response = getResponseMessage(Constants.Status.NON_EXISTENT, 404, message);
                break;
            default:
                response = getResponseMessage(Constants.Status.FAILED, 500, message);
                break;
        }
        return response;
    }

    /**
     * Gets the response message.
     * @param status the status
     * @param message the message
     * @return the response message
     */
    private ResponseBean getResponseMessage(String status, int statusCode, String message) {
        ResponseBean standardResponse = new ResponseBean(status, statusCode);
        if (message != null) {
            standardResponse.setMessage(message);
        }
        return standardResponse;
    }

    /**
     * The Enum ResponseStatus.
     */
    public enum ResponseStatus {
        /** The "conflict" response */
        CONFLICT,
        /** The "created" response */
        CREATED,
        /** The "success" response. */
        SUCCESS,
        /** The "failed" response. */
        FAILED,
        /** The "invalid" response. */
        INVALID,
        /** The "forbidden" response. */
        FORBIDDEN,
        /** The "forbidden" response. */
        UNAUTHENTICATED,
        /** The "non existent" response. */
        NON_EXISTENT
    }
}
