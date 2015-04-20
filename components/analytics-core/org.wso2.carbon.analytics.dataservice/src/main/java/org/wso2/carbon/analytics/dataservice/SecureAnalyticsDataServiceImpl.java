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
package org.wso2.carbon.analytics.dataservice;

import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.DrillDownResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Iterator;
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
    public RecordGroup[] get(String username, String tableName, int numPartitionsHint, List<String> columns,
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
    public RecordGroup[] get(String username, String tableName, int numPartitionsHint, List<String> columns,
                             List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_GET_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to get records");
        }
        return analyticsDataService.get(tenantId, tableName, numPartitionsHint, columns, ids);
    }

    @Override
    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        return analyticsDataService.readRecords(recordGroup);
    }

    @Override
    public boolean isPaginationSupported() {
        return analyticsDataService.isPaginationSupported();
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
    public void setIndices(String username, String tableName, Map<String, IndexType> columns)
            throws AnalyticsIndexException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SET_INDEXING)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to set indices");
            }
            analyticsDataService.setIndices(tenantId, tableName, columns);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public void setIndices(String username, String tableName, Map<String, IndexType> columns,
                           List<String> scoreParams) throws AnalyticsIndexException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SET_INDEXING)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to set indices");
            }
            analyticsDataService.setIndices(tenantId, tableName, columns, scoreParams);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, IndexType> getIndices(String username, String tableName)
            throws AnalyticsIndexException, AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_GET_INDEXING)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to get indices");
        }
        return analyticsDataService.getIndices(tenantId, tableName);
    }

    @Override
    public List<String> getScoreParams(String username, String tableName)
            throws AnalyticsException, AnalyticsIndexException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_GET_INDEXING)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to get score parameters");
        }
        return analyticsDataService.getScoreParams(tenantId, tableName);
    }

    @Override
    public void clearIndices(String username, String tableName) throws AnalyticsIndexException, AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_DELETE_INDEXING)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to clear indices");
        }
        analyticsDataService.clearIndices(tenantId, tableName);
    }

    @Override
    public List<SearchResultEntry> search(String username, String tableName, String language, String query, int start,
                                          int count) throws AnalyticsIndexException, AnalyticsException {
        int tenantId = getTenantId(username);
        if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SEARCH_RECORD)) {
            throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                           "permission to search indexed records");
        }
        return analyticsDataService.search(tenantId, tableName, language, query, start, count);
    }

    @Override
    public int searchCount(String username, String tableName, String language, String query)
            throws AnalyticsIndexException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SEARCH_RECORD)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to get search indexed record count");
            }
            return analyticsDataService.searchCount(tenantId, tableName, language, query);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public void waitForIndexing(long maxWait) throws AnalyticsTimeoutException, AnalyticsException {
        analyticsDataService.waitForIndexing(maxWait);
    }

    @Override
    public Map<String, List<DrillDownResultEntry>> drillDown(String username,
                                                             AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        try {
            int tenantId = getTenantId(username);
            if (!AuthorizationUtils.isUserAuthorized(tenantId, username, Constants.PERMISSION_SEARCH_RECORD)) {
                throw new AnalyticsUnauthorizedAccessException("User[" + username + "] does not have required " +
                                                               "permission to perform faceted drilldown");
            }
            return analyticsDataService.drillDown(tenantId, drillDownRequest);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException(e.getMessage(), e);
        }
    }

    @Override
    public void destroy() throws AnalyticsException {
        analyticsDataService.destroy();
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
