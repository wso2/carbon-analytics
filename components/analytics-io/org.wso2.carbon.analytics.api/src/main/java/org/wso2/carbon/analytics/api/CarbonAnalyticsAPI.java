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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceException;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceUnauthorizedException;
import org.wso2.carbon.analytics.api.internal.AnalyticsDataConfiguration;
import org.wso2.carbon.analytics.api.internal.ServiceHolder;
import org.wso2.carbon.analytics.api.internal.client.AnalyticsAPIHttpClient;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
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
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the actual implementation of the AnalyticsDataAPI osgi service exposed.
 */
public class CarbonAnalyticsAPI implements AnalyticsDataAPI {

    private AnalyticsDataConfiguration analyticsDataConfiguration;

    private AnalyticsDataConfiguration.Mode mode;

    public CarbonAnalyticsAPI(String configFilePath) {
        try {
            JAXBContext context = JAXBContext.newInstance(AnalyticsDataConfiguration.class);
            Unmarshaller un = context.createUnmarshaller();
            this.analyticsDataConfiguration = (AnalyticsDataConfiguration) un.unmarshal(new File(configFilePath));
            this.resolveSecureVaultCredentials(configFilePath);
            AnalyticsAPIHttpClient.init(analyticsDataConfiguration);
        } catch (JAXBException ex) {
            throw new AnalyticsServiceException("Error while loading the configuration : " + configFilePath, ex);
        } catch (FileNotFoundException ex) {
            throw new AnalyticsServiceException("Unable to load the configuration file : " + configFilePath, ex);
        } catch (XMLStreamException ex) {
            throw new AnalyticsServiceException("Invalid XML configuration provided at the file : " + configFilePath, ex);
        }
    }

    public CarbonAnalyticsAPI() {
        this(CarbonUtils.getCarbonConfigDirPath() + File.separator + AnalyticsDataConstants.ANALYTICS_CONFIG_DIR
                + File.separator + AnalyticsDataConstants.ANALYTICS_DATA_CONFIGURATION_FILE_NAME);
    }

    private AnalyticsDataConfiguration.Mode getOperationMode() {
        if (mode == null) {
            synchronized (this) {
                if (mode == null) {
                    AnalyticsDataConfiguration.Mode mode = analyticsDataConfiguration.getOperationMode();
                    if (mode == AnalyticsDataConfiguration.Mode.AUTO) {
                        try {
                            if (ServiceHolder.getAnalyticsDataService() != null) {
                                this.mode = AnalyticsDataConfiguration.Mode.LOCAL;
                            } else {
                                this.mode = AnalyticsDataConfiguration.Mode.REMOTE;
                            }
                        } catch (Throwable e){
                            this.mode = AnalyticsDataConfiguration.Mode.REMOTE;
                        }
                    } else {
                        this.mode = mode;
                    }
                }
            }
        }
        return mode;
    }

    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().createTable(tenantId, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().
                        createTable(tenantId, null, null, tableName, false, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().
                        createTable(tenantId, null, null, tableName, false, false);
            }
        }
    }

    @Override
    public void createTable(int tenantId, String recordStoreName, String tableName)
            throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().createTable(tenantId, recordStoreName, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().createTable(tenantId, null, recordStoreName, tableName, false, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().createTable(tenantId, null, recordStoreName, tableName, false, false);
            }
        }
    }
    

    @Override
    public void createTableIfNotExists(int tenantId, String recordStoreName, String tableName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().createTableIfNotExists(tenantId, recordStoreName, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().createTable(tenantId, null, recordStoreName, tableName, false, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().createTable(tenantId, null, recordStoreName, tableName, false, true);
            }
        }
    }

    @Override
    public void clearIndexData(int tenantId, String tableName) throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().clearIndexData(tenantId, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().clearIndices(tenantId, null, tableName, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().clearIndices(tenantId, null, tableName, false);
            }
        }
    }

    @Override
    public void setTableSchema(int tenantId, String tableName, AnalyticsSchema schema) throws
            AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().setTableSchema(tenantId, tableName, schema);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().setTableSchema(tenantId, null, tableName, schema, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().setTableSchema(tenantId, null, tableName, schema, false);
            }

        }
    }

    @Override
    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws
            AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().getTableSchema(tenantId, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getTableSchema(tenantId, null, tableName, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getTableSchema(tenantId, null, tableName, false);
            }
        }
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().tableExists(tenantId, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().isTableExists(tenantId, null, tableName, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().isTableExists(tenantId, null, tableName, false);
            }
        }
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().deleteTable(tenantId, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteTable(tenantId, null, tableName, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteTable(tenantId, null, tableName, false);
            }
        }
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().listTables(tenantId);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().listTables(tenantId, null, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().listTables(tenantId, null, false);
            }
        }
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) throws
            AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().getRecordCount(tenantId, tableName, timeFrom, timeTo);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordCount(tenantId, null, tableName, timeFrom, timeTo,
                        false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordCount(tenantId, null, tableName, timeFrom, timeTo,
                        false);
            }
        }
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().put(records);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().putRecords(null, records, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().putRecords(null, records, false);
            }
        }
    }

    @Override
    public AnalyticsDataResponse get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom,
                                     long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().get(tenantId, tableName, numPartitionsHint, columns,
                    timeFrom, timeTo, recordsFrom, recordsCount);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordGroup(tenantId, null, tableName, numPartitionsHint,
                        columns, timeFrom, timeTo, recordsFrom, recordsCount, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordGroup(tenantId, null, tableName, numPartitionsHint,
                        columns, timeFrom, timeTo, recordsFrom, recordsCount, false);
            }
        }
    }

    @Override
    public AnalyticsDataResponse get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, List<String> ids)
            throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().get(tenantId, tableName, numPartitionsHint, columns, ids);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordGroup(tenantId, null, tableName, numPartitionsHint, columns,
                        ids, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordGroup(tenantId, null, tableName, numPartitionsHint, columns,
                        ids, false);
            }
        }
    }

    @Override
    public void createTable(String username, String tableName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().createTable(username, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().createTable(MultitenantConstants.INVALID_TENANT_ID,
                        username, null, tableName, true, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().createTable(MultitenantConstants.INVALID_TENANT_ID,
                        username, null, tableName, true, false);
            }
        }
    }

    @Override
    public void createTable(String username, String recordStoreName, String tableName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().createTable(username, recordStoreName, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().createTable(MultitenantConstants.INVALID_TENANT_ID,
                        username, recordStoreName, tableName, true, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().createTable(MultitenantConstants.INVALID_TENANT_ID,
                        username, recordStoreName, tableName, true, false);
            }
        }
    }
    

    @Override
    public void createTableIfNotExists(String username, String recordStoreName, String tableName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().createTableIfNotExists(username, recordStoreName, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().createTable(MultitenantConstants.INVALID_TENANT_ID,
                        username, recordStoreName, tableName, true, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().createTable(MultitenantConstants.INVALID_TENANT_ID,
                        username, recordStoreName, tableName, true, true);
            }
        }
    }

    @Override
    public void clearIndexData(String username, String tableName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().clearIndexData(username, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().clearIndices(MultitenantConstants.INVALID_TENANT_ID,
                        username, tableName, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().clearIndices(MultitenantConstants.INVALID_TENANT_ID,
                        username, tableName, true);
            }
        }
    }

    @Override
    public void setTableSchema(String username, String tableName, AnalyticsSchema schema) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().setTableSchema(username, tableName, schema);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().setTableSchema(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, schema, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().setTableSchema(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, schema, true);
            }
        }
    }

    @Override
    public AnalyticsSchema getTableSchema(String username, String tableName) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().getTableSchema(username, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getTableSchema(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getTableSchema(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, true);
            }
        }
    }

    @Override
    public boolean tableExists(String username, String tableName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().tableExists(username, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().isTableExists(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().isTableExists(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, true);
            }

        }
    }

    @Override
    public void deleteTable(String username, String tableName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().deleteTable(username, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteTable(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteTable(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, true);
            }
        }
    }

    @Override
    public List<String> listTables(String username) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().listTables(username);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().listTables(MultitenantConstants.INVALID_TENANT_ID, username,
                        true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().listTables(MultitenantConstants.INVALID_TENANT_ID, username,
                        true);
            }
        }
    }

    @Override
    public long getRecordCount(String username, String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().getRecordCount(username, tableName, timeFrom, timeTo);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordCount(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, timeFrom, timeTo, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordCount(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, timeFrom, timeTo, true);
            }
        }
    }

    @Override
    public void put(String username, List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().put(username, records);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().putRecords(username, records, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().putRecords(username, records, true);
            }
        }
    }

    @Override
    public AnalyticsDataResponse get(String username, String tableName, int numPartitionsHint, List<String> columns,
                                     long timeFrom, long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().get(username, tableName, numPartitionsHint, columns,
                    timeFrom, timeTo, recordsFrom, recordsCount);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordGroup(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, numPartitionsHint, columns, timeFrom, timeTo, recordsFrom, recordsCount, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordGroup(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, numPartitionsHint, columns, timeFrom, timeTo, recordsFrom, recordsCount, true);
            }
        }
    }

    @Override
    public AnalyticsDataResponse get(String username, String tableName, int numPartitionsHint, List<String> columns,
                                     List<String> ids) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().get(username, tableName, numPartitionsHint, columns, ids);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordGroup(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, numPartitionsHint, columns, ids, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordGroup(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, numPartitionsHint, columns, ids, true);
            }
        }
    }

    @Override
    public AnalyticsDataResponse getWithKeyValues(String username, String tableName, int numPartitionsHint,
                                                  List<String> columns,
                                                  List<Map<String, Object>> valuesBatch)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().getWithKeyValues(username, tableName, numPartitionsHint,
                    columns, valuesBatch);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getWithKeyValues(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, numPartitionsHint, columns, valuesBatch, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getWithKeyValues(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, numPartitionsHint, columns, valuesBatch, true);
            }
        }
    }

    @Override
    public boolean isPaginationSupported(String recordStoreName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().isPaginationSupported(recordStoreName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().isPaginationSupported(recordStoreName);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().isPaginationSupported(recordStoreName);
            }
        }
    }

    @Override
    public boolean isRecordCountSupported(String recordStoreName) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().isRecordCountSupported(recordStoreName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().isRecordCountSupported(recordStoreName);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().isRecordCountSupported(recordStoreName);
            }
        }
    }

    @Override
    public void delete(String username, String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().delete(username, tableName, timeFrom, timeTo);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteRecords(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, timeFrom, timeTo, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteRecords(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, timeFrom, timeTo, true);
            }
        }
    }

    @Override
    public void delete(String username, String tableName, List<String> ids) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().delete(username, tableName, ids);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteRecords(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, ids, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteRecords(MultitenantConstants.INVALID_TENANT_ID, username,
                        tableName, ids, true);
            }
        }
    }

    @Override
    public List<SearchResultEntry> search(String username, String tableName, String query, int start, int count, List<SortByField> sortByFields)
            throws AnalyticsIndexException, AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().search(username, tableName, query, start, count, sortByFields);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().search(MultitenantConstants.INVALID_TENANT_ID,
                        username, tableName, query, start, count, sortByFields, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().search(MultitenantConstants.INVALID_TENANT_ID,
                        username, tableName, query, start, count, sortByFields, true);
            }
        }
    }

    @Override
    public List<SearchResultEntry> search(String username, String tableName, String query,
                                          int start, int count) throws AnalyticsException {
        return this.search(username, tableName, query, start, count, new ArrayList<SortByField>(0));
    }

    @Override
    public int searchCount(String username, String tableName, String query) throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().searchCount(username, tableName, query);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchCount(MultitenantConstants.INVALID_TENANT_ID,
                        username, tableName, query, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchCount(MultitenantConstants.INVALID_TENANT_ID,
                        username, tableName, query, true);
            }
        }
    }

    @Override
    public List<SearchResultEntry> drillDownSearch(String username, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().drillDownSearch(username, drillDownRequest);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownSearch(MultitenantConstants.INVALID_TENANT_ID,
                        username, drillDownRequest, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownSearch(MultitenantConstants.INVALID_TENANT_ID,
                        username, drillDownRequest, true);
            }
        }
    }

    @Override
    public double drillDownSearchCount(String username, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().drillDownSearchCount(username, drillDownRequest);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownSearchCount(MultitenantConstants.INVALID_TENANT_ID,
                        username, drillDownRequest, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownSearchCount(MultitenantConstants.INVALID_TENANT_ID,
                        username, drillDownRequest, true);
            }
        }
    }

    @Override
    public SubCategories drillDownCategories(String username,
                                             CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().drillDownCategories(username, drillDownRequest);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownCategories(MultitenantConstants.INVALID_TENANT_ID,
                        username, drillDownRequest, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownCategories(MultitenantConstants.INVALID_TENANT_ID,
                        username, drillDownRequest, true);
            }
        }
    }

    @Override
    public List<AnalyticsDrillDownRange> drillDownRangeCount(String username,
                                                             AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().drillDownRangeCount(username, drillDownRequest);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownRangeCount(MultitenantConstants.INVALID_TENANT_ID,
                        username, drillDownRequest, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownRangeCount(MultitenantConstants.INVALID_TENANT_ID,
                        username, drillDownRequest, true);
            }
        }
    }

    @Override
    public AnalyticsIterator<Record> searchWithAggregates(String username, AggregateRequest aggregateRequest)
            throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().searchWithAggregates(username, aggregateRequest);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchWithAggregates(MultitenantConstants.INVALID_TENANT_ID,
                        username, aggregateRequest, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchWithAggregates(MultitenantConstants.INVALID_TENANT_ID,
                        username, aggregateRequest, true);
            }
        }
    }

    @Override
    public List<AnalyticsIterator<Record>> searchWithAggregates(String username,
                                                                AggregateRequest[] aggregateRequests)
            throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().searchWithAggregates(username, aggregateRequests);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                             analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchWithAggregates(MultitenantConstants.INVALID_TENANT_ID,
                                                                                 username, aggregateRequests, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchWithAggregates(MultitenantConstants.INVALID_TENANT_ID,
                                                                                 username, aggregateRequests, true);
            }
        }
    }

    @Override
    public void reIndex(String username, String tableName, long startTime, long endTime)
            throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().reIndex(username, tableName, startTime, endTime);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                             analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().reIndex(MultitenantConstants.INVALID_TENANT_ID, username, tableName, startTime, endTime, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().reIndex(MultitenantConstants.INVALID_TENANT_ID, username, tableName, startTime, endTime, true);
            }
        }
    }

    @Override
    public AnalyticsIterator<Record> readRecords(String recordStoreName, RecordGroup recordGroup) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().readRecords(recordStoreName, recordGroup);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().readRecords(recordStoreName, recordGroup);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().readRecords(recordStoreName, recordGroup);
            }
        }
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().delete(tenantId, tableName, timeFrom, timeTo);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteRecords(tenantId, null, tableName, timeFrom, timeTo, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteRecords(tenantId, null, tableName, timeFrom, timeTo, false);
            }
        }
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().delete(tenantId, tableName, ids);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteRecords(tenantId, null, tableName, ids, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().deleteRecords(tenantId, null, tableName, ids, false);
            }
        }
    }

    @Override
    public List<SearchResultEntry> search(int tenantId, String tableName, String query, int start,
                                          int count) throws AnalyticsException {
       return this.search(tenantId, tableName, query, start, count, new ArrayList<SortByField>(0));
    }

    @Override
    public List<SearchResultEntry> search(int tenantId, String tableName, String query, int start,
                                          int count, List<SortByField> sortByFields) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().search(tenantId, tableName, query, start, count, sortByFields);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().search(tenantId, null, tableName, query, start, count, sortByFields, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().search(tenantId, null, tableName, query, start, count, sortByFields, false);
            }
        }
    }

    @Override
    public int searchCount(int tenantId, String tableName, String query) throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().searchCount(tenantId, tableName, query);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchCount(tenantId, null, tableName, query, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchCount(tenantId, null, tableName, query, false);
            }
        }
    }

    @Override
    public List<SearchResultEntry> drillDownSearch(int tenantId, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().drillDownSearch(tenantId, drillDownRequest);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownSearch(tenantId, null, drillDownRequest, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownSearch(tenantId, null, drillDownRequest, false);
            }
        }
    }

    @Override
    public double drillDownSearchCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().drillDownSearchCount(tenantId, drillDownRequest);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownSearchCount(tenantId, null, drillDownRequest, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownSearchCount(tenantId, null, drillDownRequest, false);
            }
        }
    }

    @Override
    public SubCategories drillDownCategories(int tenantId,
                                             CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().drillDownCategories(tenantId, drillDownRequest);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownCategories(tenantId, null, drillDownRequest, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownCategories(tenantId, null, drillDownRequest, false);
            }
        }
    }

    @Override
    public List<AnalyticsDrillDownRange> drillDownRangeCount(int tenantId,
                                                             AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().drillDownRangeCount(tenantId, drillDownRequest);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownRangeCount(tenantId, null, drillDownRequest, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().drillDownRangeCount(tenantId, null, drillDownRequest, false);
            }
        }
    }

    @Override
    public AnalyticsIterator<Record> searchWithAggregates(int tenantId, AggregateRequest aggregateRequest)
            throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().searchWithAggregates(tenantId, aggregateRequest);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchWithAggregates(tenantId,
                        null, aggregateRequest, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchWithAggregates(tenantId,
                        null, aggregateRequest, false);
            }
        }
    }

    @Override
    public List<AnalyticsIterator<Record>> searchWithAggregates(int tenantId,
                                                                AggregateRequest[] aggregateRequests)
            throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().searchWithAggregates(tenantId, aggregateRequests);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                             analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchWithAggregates(tenantId,
                                                                                 null, aggregateRequests, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().searchWithAggregates(tenantId,
                                                                                 null, aggregateRequests, false);
            }
        }
    }

    @Override
    public void reIndex(int tenantId, String tableName, long startTime, long endTime)
            throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().reIndex(tenantId, tableName, startTime, endTime);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                                                                             analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().reIndex(tenantId, null, tableName, startTime, endTime, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().reIndex(tenantId, null, tableName, startTime, endTime, false);
            }
        }
    }

    @Override
    public void waitForIndexing(long maxWait) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().waitForIndexing(maxWait);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().waitForIndexing(MultitenantConstants.INVALID_TENANT_ID, null, null,
                        maxWait, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().waitForIndexing(MultitenantConstants.INVALID_TENANT_ID, null, null,
                        maxWait, false);
            }
        }
    }

    @Override
    public void waitForIndexing(String username, String tableName, long maxWait)
            throws AnalyticsTimeoutException, AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getSecureAnalyticsDataService().waitForIndexing(username, tableName, maxWait);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().waitForIndexing(MultitenantConstants.INVALID_TENANT_ID,
                        username, tableName, maxWait, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().waitForIndexing(MultitenantConstants.INVALID_TENANT_ID,
                        username, tableName, maxWait, true);
            }
        }
    }

    @Override
    public void waitForIndexing(int tenantId, String tableName, long maxWait) throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().waitForIndexing(tenantId, tableName, maxWait);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().waitForIndexing(tenantId, null, tableName, maxWait, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().waitForIndexing(tenantId, null, tableName, maxWait, false);
            }
        }
    }

    @Override
    public void destroy() throws AnalyticsException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            ServiceHolder.getAnalyticsDataService().destroy();
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().destroy();
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                AnalyticsAPIHttpClient.getInstance().destroy();
            }
        }
    }

    @Override
    public AnalyticsDataResponse getWithKeyValues(int tenantId, String tableName, int numPartitionsHint, List<String> columns,
                                                  List<Map<String, Object>> valuesBatch)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().getWithKeyValues(tenantId, tableName, numPartitionsHint,
                    columns, valuesBatch);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getWithKeyValues(tenantId, null, tableName, numPartitionsHint,
                        columns, valuesBatch, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getWithKeyValues(tenantId, null, tableName, numPartitionsHint,
                        columns, valuesBatch, false);
            }
        }
    }

    private void resolveSecureVaultCredentials(String configPath) throws FileNotFoundException, XMLStreamException {
        FileInputStream fileInputStream = null;
        File configFile = new File(configPath);
        if (configFile.exists()) {
            try {
                fileInputStream = new FileInputStream(configFile);
                StAXOMBuilder builder = new StAXOMBuilder(fileInputStream);
                OMElement configElement = builder.getDocumentElement();
                SecretResolver secretResolver = SecretResolverFactory.create(configElement, true);
                if (secretResolver != null && secretResolver.isInitialized()) {
                    String resolvedPassword = getResolvedPassword(secretResolver,
                            AnalyticsDataConstants.ANALYTICS_API_CONF_PASSWORD_ALIAS);
                    if (resolvedPassword != null) {
                        this.analyticsDataConfiguration.setPassword(resolvedPassword);
                    }
                    resolvedPassword = getResolvedPassword(secretResolver,
                            AnalyticsDataConstants.ANALYTICS_API_TRUST_STORE_PASSWORD_ALIAS);
                    if (resolvedPassword != null) {
                        this.analyticsDataConfiguration.setTrustStorePassword(resolvedPassword);
                    }
                }
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    private String getResolvedPassword(SecretResolver secretResolver, String alias) {
        if (secretResolver.isTokenProtected(alias)) {
            String resolvedPassword = secretResolver.
                    resolve(alias);
            if (resolvedPassword != null && !resolvedPassword.isEmpty()) {
                return resolvedPassword;
            }
        }
        return null;
    }

    @Override
    public String getRecordStoreNameByTable(int tenantId, String tableName) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().getRecordStoreNameByTable(tenantId, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordStoreNameByTable(tenantId, null, tableName, false);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordStoreNameByTable(tenantId, null, tableName, false);
            }
        }
    }

    @Override
    public List<String> listRecordStoreNames() {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getAnalyticsDataService().listRecordStoreNames();
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().listRecordStoreNames();
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().listRecordStoreNames();
            }
        }
    }

    @Override
    public String getRecordStoreNameByTable(String username, String tableName) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        if (getOperationMode() == AnalyticsDataConfiguration.Mode.LOCAL) {
            return ServiceHolder.getSecureAnalyticsDataService().getRecordStoreNameByTable(username, tableName);
        } else {
            try {
                AnalyticsAPIHttpClient.getInstance().validateAndAuthenticate(analyticsDataConfiguration.getUsername(),
                        analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordStoreNameByTable(MultitenantConstants.INVALID_TENANT_ID,
                        username, tableName, true);
            } catch (AnalyticsServiceUnauthorizedException ex) {
                AnalyticsAPIHttpClient.getInstance().invalidateSessionAndAuthenticate(analyticsDataConfiguration.
                        getUsername(), analyticsDataConfiguration.getPassword());
                return AnalyticsAPIHttpClient.getInstance().getRecordStoreNameByTable(MultitenantConstants.INVALID_TENANT_ID,
                        username, tableName, true);
            }
        }
    }

}
