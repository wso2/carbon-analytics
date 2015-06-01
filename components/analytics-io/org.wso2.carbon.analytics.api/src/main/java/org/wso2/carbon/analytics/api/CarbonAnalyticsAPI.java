/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.api;

import org.wso2.carbon.analytics.api.exception.AnalyticsServiceException;
import org.wso2.carbon.analytics.api.internal.AnalyticsDataConfiguration;
import org.wso2.carbon.analytics.api.internal.ServiceHolder;
import org.wso2.carbon.analytics.api.internal.client.AnalyticsAPIHttpClient;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Iterator;
import java.util.List;

public class CarbonAnalyticsAPI implements AnalyticsDataAPI {

    private AnalyticsDataConfiguration analyticsDataConfiguration;

    public CarbonAnalyticsAPI(String configFilePath) {
        try {
            JAXBContext context = JAXBContext.newInstance(AnalyticsDataConfiguration.class);
            Unmarshaller un = context.createUnmarshaller();
            this.analyticsDataConfiguration = (AnalyticsDataConfiguration) un.unmarshal(new File(configFilePath));
            AnalyticsAPIHttpClient.init(analyticsDataConfiguration);
        } catch (JAXBException ex) {
            throw new AnalyticsServiceException("Error while loading the configuration : " + configFilePath, ex);
        }
    }

    public CarbonAnalyticsAPI() {
        this(CarbonUtils.getCarbonConfigDirPath() + File.separator + AnalyticsDataConstants.ANALYTICS_CONFIG_DIR
                + File.separator + AnalyticsDataConstants.ANALYTICS_DATA_CONFIGURATION_FILE_NAME);
    }

    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().createTable(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().createTable(tenantId, null, tableName, false);
        }
    }

    @Override
    public void clearIndexData(int tenantId, String tableName) throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().clearIndexData(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                         analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().clearIndices(tenantId, null, tableName, false);
        }
    }

    @Override
    public void setTableSchema(int tenantId, String tableName, AnalyticsSchema schema) throws
            AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().setTableSchema(tenantId, tableName, schema);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().setTableSchema(tenantId, null, tableName, schema, false);
        }
    }

    @Override
    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws
            AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().getTableSchema(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getTableSchema(tenantId, null, tableName, false);
        }
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().tableExists(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().isTableExists(tenantId, null, tableName, false);
        }
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().deleteTable(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().deleteTable(tenantId, null, tableName, false);
        }
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().listTables(tenantId);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().listTables(tenantId, null, false);
        }
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) throws
            AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().getRecordCount(tenantId, tableName, timeFrom, timeTo);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getRecordCount(tenantId, null, tableName, timeFrom, timeTo,
                    false);
        }
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().put(records);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().putRecords(null, records, false);
        }
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom,
                             long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().get(tenantId, tableName, numPartitionsHint, columns,
                    timeFrom, timeTo, recordsFrom, recordsCount);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getRecordGroup(tenantId, null, tableName, numPartitionsHint,
                    columns, timeFrom, timeTo, recordsFrom, recordsCount, false);
        }
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, List<String> ids)
            throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().get(tenantId, tableName, numPartitionsHint, columns, ids);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getRecordGroup(tenantId, null, tableName, numPartitionsHint, columns,
                    ids, false);
        }
    }

    @Override
    public void createTable(String username, String tableName) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getSecureAnalyticsDataService().createTable(username, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().createTable(MultitenantConstants.INVALID_TENANT_ID,
                    username, tableName, true);
        }
    }

    @Override
    public void clearIndexData(String username, String tableName) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getSecureAnalyticsDataService().clearIndexData(username, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                         analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().clearIndices(MultitenantConstants.INVALID_TENANT_ID,
                                                              username, tableName, false);
        }
    }

    @Override
    public void setTableSchema(String username, String tableName, AnalyticsSchema schema) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getSecureAnalyticsDataService().setTableSchema(username, tableName, schema);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().setTableSchema(MultitenantConstants.INVALID_TENANT_ID, username,
                    tableName, schema, true);
        }
    }

    @Override
    public AnalyticsSchema getTableSchema(String username, String tableName) throws AnalyticsTableNotAvailableException, AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().getTableSchema(username, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getTableSchema(MultitenantConstants.INVALID_TENANT_ID, username,
                    tableName, true);
        }
    }

    @Override
    public boolean tableExists(String username, String tableName) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().tableExists(username, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().isTableExists(MultitenantConstants.INVALID_TENANT_ID, username,
                    tableName, true);
        }
    }

    @Override
    public void deleteTable(String username, String tableName) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getSecureAnalyticsDataService().deleteTable(username, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().deleteTable(MultitenantConstants.INVALID_TENANT_ID, username,
                    tableName, true);
        }
    }

    @Override
    public List<String> listTables(String username) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().listTables(username);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().listTables(MultitenantConstants.INVALID_TENANT_ID, username,
                    true);
        }
    }

    @Override
    public long getRecordCount(String username, String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().getRecordCount(username, tableName, timeFrom, timeTo);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getRecordCount(MultitenantConstants.INVALID_TENANT_ID, username,
                    tableName, timeFrom, timeTo, true);
        }
    }

    @Override
    public void put(String username, List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getSecureAnalyticsDataService().put(username, records);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().putRecords(username, records, true);
        }
    }

    @Override
    public RecordGroup[] get(String username, String tableName, int numPartitionsHint, List<String> columns,
                             long timeFrom, long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().get(username, tableName, numPartitionsHint, columns,
                    timeFrom, timeTo, recordsFrom, recordsCount);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getRecordGroup(MultitenantConstants.INVALID_TENANT_ID, username,
                    tableName, numPartitionsHint, columns, timeFrom, timeTo, recordsFrom, recordsCount, true);
        }
    }

    @Override
    public RecordGroup[] get(String username, String tableName, int numPartitionsHint, List<String> columns,
                             List<String> ids) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().get(username, tableName, numPartitionsHint, columns, ids);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getRecordGroup(MultitenantConstants.INVALID_TENANT_ID, username,
                    tableName, numPartitionsHint, columns, ids, true);
        }
    }

    @Override
    public boolean isPaginationSupported() {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().isPaginationSupported();
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().isPaginationSupported();
        }
    }

    @Override
    public void delete(String username, String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getSecureAnalyticsDataService().delete(username, tableName, timeFrom, timeTo);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().deleteRecords(MultitenantConstants.INVALID_TENANT_ID, username,
                    tableName, timeFrom, timeTo, true);
        }
    }

    @Override
    public void delete(String username, String tableName, List<String> ids) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getSecureAnalyticsDataService().delete(username, tableName, ids);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().deleteRecords(MultitenantConstants.INVALID_TENANT_ID, username,
                    tableName, ids, true);
        }
    }

    @Override
    public List<SearchResultEntry> search(String username, String tableName, String query, int start, int count) throws AnalyticsIndexException, AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().search(username, tableName, query, start, count);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().search(MultitenantConstants.INVALID_TENANT_ID,
                    username, tableName, query, start, count, true);
        }
    }

    @Override
    public int searchCount(String username, String tableName, String query) throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().searchCount(username, tableName, query);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().searchCount(MultitenantConstants.INVALID_TENANT_ID,
                    username, tableName, query, true);
        }
    }

    @Override
    public List<SearchResultEntry> drillDownSearch(String username, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().drillDownSearch(username, drillDownRequest);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                         analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().drillDownSearch(MultitenantConstants.INVALID_TENANT_ID,
                   username, drillDownRequest, true);
        }
    }
    @Override
    public double drillDownSearchCount(String username, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().drillDownSearchCount(username, drillDownRequest);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                         analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().drillDownSearchCount(MultitenantConstants.INVALID_TENANT_ID,
                                                                             username, drillDownRequest, true);
        }
    }

    @Override
    public SubCategories drillDownCategories(String username,
                                             CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().drillDownCategories(username, drillDownRequest);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                         analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().drillDownCategories(MultitenantConstants.INVALID_TENANT_ID,
                                                                            username, drillDownRequest, true);
        }
    }

    @Override
    public List<AnalyticsDrillDownRange> drillDownRangeCount(String username,
                                                             AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getSecureAnalyticsDataService().drillDownRangeCount(username, drillDownRequest);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                         analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().drillDownRangeCount(MultitenantConstants.INVALID_TENANT_ID,
                                                                            username, drillDownRequest, true);
        }
    }

    @Override
    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().readRecords(recordGroup);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().readRecords(recordGroup);
        }
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().delete(tenantId, tableName, timeFrom, timeTo);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().deleteRecords(tenantId, null, tableName, timeFrom, timeTo, false);
        }
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().delete(tenantId, tableName, ids);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().deleteRecords(tenantId, null, tableName, ids, false);
        }
    }

    @Override
    public List<SearchResultEntry> search(int tenantId, String tableName, String query, int start,
                                          int count) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().search(tenantId, tableName, query, start, count);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().search(tenantId, null, tableName, query, start, count, false);
        }
    }

    @Override
    public int searchCount(int tenantId, String tableName, String query) throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().searchCount(tenantId, tableName, query);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().searchCount(tenantId, null, tableName, query, false);
        }
    }

    @Override
    public List<SearchResultEntry> drillDownSearch(int tenantId, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().drillDownSearch(tenantId, drillDownRequest);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().drillDownSearch(tenantId, null, drillDownRequest, false);
        }
    }
    @Override
    public double drillDownSearchCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().drillDownSearchCount(tenantId, drillDownRequest);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                         analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().drillDownSearchCount(tenantId, null, drillDownRequest, false);
        }
    }

    @Override
    public SubCategories drillDownCategories(int tenantId,
                                             CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().drillDownCategories(tenantId, drillDownRequest);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                         analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().drillDownCategories(tenantId, null, drillDownRequest, false);
        }
    }

    @Override
    public List<AnalyticsDrillDownRange> drillDownRangeCount(int tenantId,
                                                             AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().drillDownRangeCount(tenantId, drillDownRequest);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                         analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().drillDownRangeCount(tenantId, null, drillDownRequest, false);
        }
    }

    @Override
    public void waitForIndexing(long maxWait) throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().waitForIndexing(maxWait);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().waitForIndexing(maxWait);
        }
    }

    @Override
    public void destroy() throws AnalyticsException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().destroy();
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().destroy();
        }
    }
}
