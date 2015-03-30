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
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CarbonAnalyticsAPI implements AnalyticsDataAPI {

    private AnalyticsDataConfiguration analyticsDataConfiguration;

    public CarbonAnalyticsAPI(String configFilePath) throws AnalyticsServiceException {
        try {
            JAXBContext context = JAXBContext.newInstance(AnalyticsDataConfiguration.class);
            Unmarshaller un = context.createUnmarshaller();
            this.analyticsDataConfiguration = (AnalyticsDataConfiguration) un.unmarshal(new File(configFilePath));
            AnalyticsAPIHttpClient.init(analyticsDataConfiguration);
        } catch (JAXBException ex) {
            throw new AnalyticsServiceException("Error while loading the configuration : " + configFilePath);
        }
    }

    public CarbonAnalyticsAPI() throws AnalyticsServiceException {
        this(CarbonUtils.getCarbonConfigDirPath() + File.separator + AnalyticsDataConstants.ANALYTICS_CONFIG_DIR
                + File.separator + AnalyticsDataConstants.ANALYTICS_DATA_CONFIGURATION_FILE_NAME);
    }

    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().createTable(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().createTable(tenantId, tableName);
        }
    }

    @Override
    public void setTableSchema(int tenantId, String tableName, AnalyticsSchema schema) throws
            AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().setTableSchema(tenantId, tableName, schema);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().setTableSchema(tenantId, tableName, schema);
        }
    }

    @Override
    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws AnalyticsTableNotAvailableException,
            AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().getTableSchema(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getTableSchema(tenantId, tableName);
        }
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().tableExists(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().isTableExists(tenantId, tableName);
        }
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().deleteTable(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().deleteTable(tenantId, tableName);
        }
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().listTables(tenantId);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().listTables(tenantId);
        }
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsTableNotAvailableException,
            AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().getRecordCount(tenantId, tableName, timeFrom, timeTo);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getRecordCount(tenantId, tableName, timeFrom, timeTo);
        }
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().put(records);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().putRecords(records);
        }
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom,
                             long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException,
            AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().get(tenantId, tableName, numPartitionsHint, columns, timeFrom, timeTo, recordsFrom, recordsCount);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getRecordGroup(tenantId, tableName, numPartitionsHint, columns, timeFrom, timeTo, recordsFrom, recordsCount);
        }
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().get(tenantId, tableName, numPartitionsHint, columns, ids);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getRecordGroup(tenantId, tableName, numPartitionsHint, columns,
                    ids);
        }
    }

    @Override
    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().readRecords(recordGroup);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().readRecords(recordGroup);
        }
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException,
            AnalyticsTableNotAvailableException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().delete(tenantId, tableName, timeFrom, timeTo);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().deleteRecords(tenantId, tableName, timeFrom, timeTo);
        }
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException,
            AnalyticsTableNotAvailableException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().delete(tenantId, tableName, ids);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().deleteRecords(tenantId, tableName, ids);
        }
    }

    @Override
    public void setIndices(int tenantId, String tableName, Map<String, IndexType> columns) throws AnalyticsException,
            AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().setIndices(tenantId, tableName, columns);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().setIndices(tenantId, tableName, columns);
        }
    }

    @Override
    public Map<String, IndexType> getIndices(int tenantId, String tableName) throws AnalyticsIndexException,
            AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().getIndices(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().getIndices(tenantId, tableName);
        }
    }

    @Override
    public void clearIndices(int tenantId, String tableName) throws AnalyticsIndexException, AnalyticsException,
            AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().clearIndices(tenantId, tableName);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().clearIndices(tenantId, tableName);
        }
    }

    @Override
    public List<SearchResultEntry> search(int tenantId, String tableName, String language, String query, int start,
                                          int count) throws AnalyticsIndexException, AnalyticsException,
            AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().search(tenantId, tableName, language, query, start, count);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().search(tenantId, tableName, language, query, start, count);
        }
    }

    @Override
    public int searchCount(int tenantId, String tableName, String language, String query) throws AnalyticsException,
            AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            return ServiceHolder.getAnalyticsDataService().searchCount(tenantId, tableName, language, query);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            return AnalyticsAPIHttpClient.getInstance().searchCount(tenantId, tableName, language, query);
        }
    }

    @Override
    public void waitForIndexing(long maxWait) throws AnalyticsTimeoutException, AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().waitForIndexing(maxWait);
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().waitForIndexing(maxWait);
        }
    }

    @Override
    public void destroy() throws AnalyticsException, AnalyticsServiceException {
        if (analyticsDataConfiguration.getOperationMode().equals(AnalyticsDataConfiguration.Mode.LOCAL)) {
            ServiceHolder.getAnalyticsDataService().destroy();
        } else {
            AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                    analyticsDataConfiguration.getPassword());
            AnalyticsAPIHttpClient.getInstance().destroy();
        }
    }
}
