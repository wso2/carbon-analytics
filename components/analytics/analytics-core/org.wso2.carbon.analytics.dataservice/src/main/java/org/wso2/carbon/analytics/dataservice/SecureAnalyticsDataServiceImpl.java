package org.wso2.carbon.analytics.dataservice;

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

public class SecureAnalyticsDataServiceImpl implements SecureAnalyticsDataService {

    private AnalyticsDataService analyticsDataService;

    public SecureAnalyticsDataServiceImpl(AnalyticsDataService analyticsDataService) {
        this.analyticsDataService = analyticsDataService;
    }

    @Override
    public void createTable(String username, String tableName) throws AnalyticsException {

        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            analyticsDataService.createTable(tenantId, tableName);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public void setTableSchema(String username, String tableName, AnalyticsSchema schema)
            throws AnalyticsTableNotAvailableException, AnalyticsException {

        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            analyticsDataService.setTableSchema(tenantId, tableName, schema);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }

    }

    @Override
    public AnalyticsSchema getTableSchema(String username, String tableName)
            throws AnalyticsTableNotAvailableException, AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            return analyticsDataService.getTableSchema(tenantId, tableName);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public boolean tableExists(String username, String tableName) throws AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            return analyticsDataService.tableExists(tenantId, tableName);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public void deleteTable(String username, String tableName) throws AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            analyticsDataService.deleteTable(tenantId, tableName);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public List<String> listTables(String username) throws AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            return analyticsDataService.listTables(tenantId);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public long getRecordCount(String username, String tableName, long timeFrom, long timeTo)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            return analyticsDataService.getRecordCount(tenantId, tableName, timeFrom, timeTo);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        analyticsDataService.put(records);
    }

    @Override
    public RecordGroup[] get(String username, String tableName, int numPartitionsHint, List<String> columns,
                             long timeFrom, long timeTo, int recordsFrom, int recordsCount)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            return analyticsDataService.get(tenantId, tableName, numPartitionsHint, columns, timeFrom, timeTo,
                                            recordsFrom, recordsCount);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public RecordGroup[] get(String username, String tableName, int numPartitionsHint, List<String> columns,
                             List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            return analyticsDataService.get(tenantId, tableName, numPartitionsHint, columns, ids);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
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
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            analyticsDataService.delete(tenantId, tableName, timeFrom, timeTo);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public void delete(String username, String tableName, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            analyticsDataService.delete(tenantId, tableName, ids);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public void setIndices(String username, String tableName, Map<String, IndexType> columns)
            throws AnalyticsIndexException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            analyticsDataService.setIndices(tenantId, tableName, columns);
        } catch (UserStoreException e) {
            throw new AnalyticsIndexException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public Map<String, IndexType> getIndices(String username, String tableName)
            throws AnalyticsIndexException, AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            return analyticsDataService.getIndices(tenantId, tableName);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public void clearIndices(String username, String tableName) throws AnalyticsIndexException, AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            analyticsDataService.clearIndices(tenantId, tableName);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public List<SearchResultEntry> search(String username, String tableName, String language, String query, int start,
                                          int count) throws AnalyticsIndexException, AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            return analyticsDataService.search(tenantId, tableName, language, query, start, count);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public int searchCount(String username, String tableName, String language, String query)
            throws AnalyticsIndexException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            return analyticsDataService.searchCount(tenantId, tableName, language, query);
        } catch (UserStoreException e) {
            throw new AnalyticsIndexException("Unable to get tenantId for user: " + username, e);
        }
    }

    @Override
    public void waitForIndexing(long maxWait) throws AnalyticsTimeoutException, AnalyticsException {
        analyticsDataService.waitForIndexing(maxWait);
    }

    @Override
    public void destroy() throws AnalyticsException {
        analyticsDataService.destroy();
    }
}
