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
package org.wso2.carbon.analytics.dataservice.core;

import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;
import java.util.Map;

/**
 * The implementation of {@link SecureAnalyticsDataService}.
 */
public class SecureAnalyticsDataServiceImpl implements SecureAnalyticsDataService {

    private AnalyticsDataService analyticsDataService;

    public SecureAnalyticsDataServiceImpl(AnalyticsDataService analyticsDataService) {
        this.analyticsDataService = analyticsDataService;
    }

    @Override
    public void createTable(String username, String tableName) throws AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_CREATE_TABLE)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to create table");
        }
        analyticsDataService.createTable(tenantId, tableName);
    }

    @Override
    public void clearIndexData(String username, String tableName) throws AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_DELETE_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to delete index data");
        }
        analyticsDataService.clearIndexData(tenantId, tableName);
    }

    @Override
    public void setTableSchema(String username, String tableName, AnalyticsSchema schema)
            throws AnalyticsTableNotAvailableException, AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_CREATE_TABLE)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to set table schema");
        }
        analyticsDataService.setTableSchema(tenantId, tableName, schema);
    }

    @Override
    public AnalyticsSchema getTableSchema(String username, String tableName)
            throws AnalyticsTableNotAvailableException, AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_LIST_TABLE)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to get table schema");
        }
        return analyticsDataService.getTableSchema(tenantId, tableName);
    }

    @Override
    public boolean tableExists(String username, String tableName) throws AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_LIST_TABLE)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to check table status");
        }
        return analyticsDataService.tableExists(tenantId, tableName);
    }

    @Override
    public void deleteTable(String username, String tableName) throws AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_DROP_TABLE)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to delete table");
        }
        analyticsDataService.deleteTable(tenantId, tableName);
    }

    @Override
    public List<String> listTables(String username) throws AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_LIST_TABLE)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to list table information");
        }
        return analyticsDataService.listTables(tenantId);
    }

    @Override
    public long getRecordCount(String username, String tableName, long timeFrom, long timeTo)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_LIST_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to get record count");
        }
        return analyticsDataService.getRecordCount(tenantId, tableName, timeFrom, timeTo);
    }

    @Override
    public void put(String username, List<Record> records) throws AnalyticsException,
                                                                  AnalyticsTableNotAvailableException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_PUT_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to put records");
        }
        analyticsDataService.put(records);
    }

    @Override
    public AnalyticsDataResponse get(String username, String tableName, int numPartitionsHint, List<String> columns,
                             long timeFrom, long timeTo, int recordsFrom, int recordsCount)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_GET_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to get records");
        }
        return analyticsDataService.get(tenantId, tableName, numPartitionsHint, columns, timeFrom, timeTo,
                                        recordsFrom, recordsCount);
    }

    @Override
    public AnalyticsDataResponse get(String username, String tableName, int numPartitionsHint, List<String> columns,
                             List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_GET_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to get records");
        }
        return analyticsDataService.get(tenantId, tableName, numPartitionsHint, columns, ids);
    }

    @Override
    public AnalyticsDataResponse getWithKeyValues(String username, String tableName, int numPartitionsHint,
                                          List<String> columns,
                                          List<Map<String, Object>> valuesBatch)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_GET_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to get records");
        }
        return analyticsDataService.getWithKeyValues(tenantId, tableName, numPartitionsHint, columns, valuesBatch);
    }

    @Override
    public AnalyticsIterator<Record> readRecords(String recordStoreName, RecordGroup recordGroup) throws AnalyticsException {
       return analyticsDataService.readRecords(recordStoreName, recordGroup);
    }

    @Override
    public boolean isPaginationSupported(String recordStoreName) throws AnalyticsException {
        return analyticsDataService.isPaginationSupported(recordStoreName);
    }
    
    @Override
    public boolean isRecordCountSupported(String recordStoreName) throws AnalyticsException {
        return analyticsDataService.isRecordCountSupported(recordStoreName);
    }

    @Override
    public void delete(String username, String tableName, long timeFrom, long timeTo)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_DELETE_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to delete records");
        }
        analyticsDataService.delete(tenantId, tableName, timeFrom, timeTo);
    }

    @Override
    public void delete(String username, String tableName, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_DELETE_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to delete records");
        }
        analyticsDataService.delete(tenantId, tableName, ids);
    }

    @Override
    public List<SearchResultEntry> search(String username, String tableName, String query, int start,
                                          int count, List<SortByField> sortByFields) throws AnalyticsIndexException, AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SEARCH_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to search indexed records");
        }
        return analyticsDataService.search(tenantId, tableName, query, start, count, sortByFields);
    }

    @Override
    public List<SearchResultEntry> search(String username, String tableName, String query,
                                          int start, int count) throws AnalyticsException {
        return this.search(username, tableName, query, start, count, null);
    }

    @Override
    public int searchCount(String username, String tableName, String query)
            throws AnalyticsIndexException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_PUT_RECORD)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to get search indexed record count");
            }
            return analyticsDataService.searchCount(tenantId, tableName, query);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public void waitForIndexing(long maxWait) throws AnalyticsTimeoutException, AnalyticsException {
        analyticsDataService.waitForIndexing(maxWait);
    }

    @Override
    public void waitForIndexing(String username, String tableName, long maxWait)
            throws AnalyticsTimeoutException, AnalyticsException {
            int tenantId = getTenantId(username);
            analyticsDataService.waitForIndexing(tenantId, tableName, maxWait);
    }

    @Override
    public List<SearchResultEntry> drillDownSearch(String username,
                                                   AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SEARCH_RECORD)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to perform drilldownSearch");
            }
            return analyticsDataService.drillDownSearch(tenantId, drillDownRequest);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public double drillDownSearchCount(String username, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SEARCH_RECORD)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to perform drilldownSearchCount");
            }
            return analyticsDataService.drillDownSearchCount(tenantId, drillDownRequest);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public SubCategories drillDownCategories(String username,
                                             CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SEARCH_RECORD)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to drilldown categories");
            }
            return analyticsDataService.drillDownCategories(tenantId, drillDownRequest);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public List<AnalyticsDrillDownRange> drillDownRangeCount(String username,
                                                             AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SEARCH_RECORD)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to drilldown ranges");
            }
            return analyticsDataService.drillDownRangeCount(tenantId, drillDownRequest);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public AnalyticsIterator<Record> searchWithAggregates(String username, AggregateRequest aggregateRequest)
            throws AnalyticsException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SEARCH_RECORD)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to search with aggregates");
            }
            return analyticsDataService.searchWithAggregates(tenantId, aggregateRequest);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public List<AnalyticsIterator<Record>> searchWithAggregates(String username,
                                                                AggregateRequest[] aggregateRequests)
            throws AnalyticsException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SEARCH_RECORD)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to search with aggregates");
            }
            return analyticsDataService.searchWithAggregates(tenantId, aggregateRequests);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public void reIndex(String username, String tableName, long startTime, long endTime)
            throws AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_PUT_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to re-index");
        }
        analyticsDataService.reIndex(tenantId, tableName, startTime, endTime);
    }

    @Override
    public void destroy() throws AnalyticsException {
        analyticsDataService.destroy();
    }

    @Override
    public List<String> listRecordStoreNames() {
        return analyticsDataService.listRecordStoreNames();
    }

    @Override
    public void createTable(String username, String recordStoreName, String tableName) throws AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_CREATE_TABLE)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to create table");
        }
        analyticsDataService.createTable(tenantId, recordStoreName, tableName);
    }
    
    @Override
    public void createTableIfNotExists(String username, String recordStoreName, String tableName) throws AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_CREATE_TABLE)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to create table");
        }
        analyticsDataService.createTableIfNotExists(tenantId, recordStoreName, tableName);
    }

    @Override
    public String getRecordStoreNameByTable(String username, String tableName)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_LIST_TABLE)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to list tables");
        }
        return analyticsDataService.getRecordStoreNameByTable(tenantId, tableName);
    }

    private int getTenantId(String username) throws AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            return AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

}
