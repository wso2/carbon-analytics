/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.dataservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.commons.*;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataPurgingConfiguration;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataPurgingIncludeTable;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataServiceConfigProperty;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataServiceConfiguration;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsRecordStoreConfiguration;
import org.wso2.carbon.analytics.dataservice.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.dataservice.tasks.AnalyticsGlobalDataPurgingTask;
import org.wso2.carbon.analytics.datasource.commons.*;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceConstants;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * The implementation of {@link AnalyticsDataService}.
 */
public class AnalyticsDataServiceImpl implements AnalyticsDataService {

    private static final Log logger = LogFactory.getLog(AnalyticsDataServiceImpl.class);

    private static final String ANALYTICS_DATASERVICE_GROUP = "__ANALYTICS_DATASERVICE_GROUP__";
    
    private static final String ANALYTICS_DS_CONFIG_FILE = "analytics-dataservice-config.xml";

    public static final String INDEX_ID_INTERNAL_FIELD = "_id";

    public static final String INDEX_INTERNAL_TIMESTAMP_FIELD = "_timestamp";

    private static final String INDEX_INTERNAL_SCORE_FIELD = "_score";

    private static final String INDEX_INTERNAL_WEIGHT_FIELD = "_weight";

    private static final String ANALYTICS_DATA_PURGING_GLOBAL = "ANALYTICS_DATA_PURGING_GLOBAL";

    private static final String GLOBAL_DATA_PURGING = "GLOBAL_DATA_PURGING";

    private static final int DELETE_BATCH_SIZE = 1000;
    
    private static final int TABLE_INFO_TENANT_ID = -1000;
    
    private static final String TABLE_INFO_TABLE_NAME = "__TABLE_INFO__";
    
    private static final String TENANT_TABLE_MAPPING_TABLE_PREFIX = "__TENANT_MAPPING";
    
    private static final String TABLE_INFO_DATA_COLUMN = "TABLE_INFO_DATA";

    private Map<String, AnalyticsRecordStore> analyticsRecordStores;
    
    private AnalyticsFileSystem analyticsFileSystem;
        
    private AnalyticsDataIndexer indexer;
    
    private Map<String, AnalyticsTableInfo> tableInfoMap = new HashMap<String, AnalyticsTableInfo>();
    
    private String primaryARSName;
    
    public AnalyticsDataServiceImpl() throws AnalyticsException {
        AnalyticsDataServiceConfiguration config = this.loadAnalyticsDataServiceConfig();
        Analyzer luceneAnalyzer;
        this.initARS(config);
        try {
            String afsClass = config.getAnalyticsFileSystemConfiguration().getImplementation();
            String analyzerClass = config.getLuceneAnalyzerConfiguration().getImplementation();
            this.analyticsFileSystem = (AnalyticsFileSystem) Class.forName(afsClass).newInstance();
            this.analyticsFileSystem.init(this.convertToMap(config.getAnalyticsFileSystemConfiguration().getProperties()));
            luceneAnalyzer = (Analyzer) Class.forName(analyzerClass).newInstance();
        } catch (Exception e) {
            throw new AnalyticsException("Error in creating analytics data service from configuration: " + 
                    e.getMessage(), e);
        }
        this.indexer = new AnalyticsDataIndexer(this.getPrimaryAnalyticsRecordStore(), this.analyticsFileSystem, this,
                                                config.getShardCount(), luceneAnalyzer);
        AnalyticsServiceHolder.setAnalyticsDataService(this);
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            acm.joinGroup(ANALYTICS_DATASERVICE_GROUP, null);
        } 
        this.indexer.init();
        if (config.getAnalyticsDataPurgingConfiguration() != null) {
            final AnalyticsDataPurgingConfiguration analyticsDataPurgingConfiguration = config.getAnalyticsDataPurgingConfiguration();
            TaskService taskService = AnalyticsServiceHolder.getTaskService();
            if (analyticsDataPurgingConfiguration.isEnable()) {
                try {
                    if (analyticsDataPurgingConfiguration.isPurgeNode()) {
                        taskService.registerTaskType(ANALYTICS_DATA_PURGING_GLOBAL);
                    }
                    TaskManager dataPurgingTaskManager = taskService.getTaskManager(ANALYTICS_DATA_PURGING_GLOBAL);
                    if (dataPurgingTaskManager.isTaskScheduled(GLOBAL_DATA_PURGING)) {
                        dataPurgingTaskManager.deleteTask(GLOBAL_DATA_PURGING);
                    }
                    dataPurgingTaskManager.registerTask(createDataPurgingTask(analyticsDataPurgingConfiguration));
                    dataPurgingTaskManager.scheduleTask(GLOBAL_DATA_PURGING);

                } catch (TaskException e) {
                    logger.error("Unable to schedule global data purging task: " + e.getMessage(), e);
                }
            } else {
                Set<String> registeredTaskTypes = taskService.getRegisteredTaskTypes();
                if (registeredTaskTypes != null && registeredTaskTypes.contains(ANALYTICS_DATA_PURGING_GLOBAL)) {
                    try {
                        TaskManager dataPurgingTaskManager = taskService.getTaskManager(ANALYTICS_DATA_PURGING_GLOBAL);
                        if (dataPurgingTaskManager.isTaskScheduled(GLOBAL_DATA_PURGING)) {
                            dataPurgingTaskManager.deleteTask(GLOBAL_DATA_PURGING);
                            logger.info("Global data purging task removed.");
                        }
                    } catch (TaskException e) {
                        logger.error("Unable to get purging task related information: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
    
    private void initARS(AnalyticsDataServiceConfiguration config) throws AnalyticsException {
        this.primaryARSName = config.getPrimaryRecordStore().trim();
        if (this.primaryARSName.length() == 0) {
            throw new AnalyticsException("Primary record store name cannot be empty.");
        }
        this.analyticsRecordStores = new HashMap<String, AnalyticsRecordStore>();
        for (AnalyticsRecordStoreConfiguration arsConfig : config.getAnalyticsRecordStoreConfigurations()) {
            String name = arsConfig.getName().trim();
            String arsClass = arsConfig.getImplementation();
            AnalyticsRecordStore ars;
            try {
                ars = (AnalyticsRecordStore) Class.forName(arsClass).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new AnalyticsException("Error in creating analytics record store with name '" + name + 
                        "': " + e.getMessage(), e);
            }
            ars.init(this.convertToMap(arsConfig.getProperties()));
            this.analyticsRecordStores.put(name, ars);
        }
        if (!this.analyticsRecordStores.containsKey(this.primaryARSName)) {
            throw new AnalyticsException("The primary record store with name '" + this.primaryARSName + "' cannot be found.");
        }
    }
    
    private AnalyticsRecordStore getPrimaryAnalyticsRecordStore() {
        return this.analyticsRecordStores.get(this.primaryARSName);
    }

    private TaskInfo createDataPurgingTask(AnalyticsDataPurgingConfiguration analyticsDataPurgingConfiguration) {
        String taskName = GLOBAL_DATA_PURGING;
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo(analyticsDataPurgingConfiguration.getCronExpression());
        triggerInfo.setDisallowConcurrentExecution(true);
        Map<String, String> taskProperties = new HashMap<>(2);
        taskProperties.put(Constants.RETENTION_PERIOD, String.valueOf(analyticsDataPurgingConfiguration.getRetentionDays()));
        taskProperties.put(Constants.INCLUDE_TABLES, getIncludeTables(analyticsDataPurgingConfiguration
                                                                              .getPurgingIncludeTables()));
        return new TaskInfo(taskName, AnalyticsGlobalDataPurgingTask.class.getName(), taskProperties, triggerInfo);
    }

    private String getIncludeTables(AnalyticsDataPurgingIncludeTable[] purgingIncludeTables) {
        StringBuilder stringBuffer = new StringBuilder();
        if (purgingIncludeTables != null) {
            for (AnalyticsDataPurgingIncludeTable purgingIncludeTable : purgingIncludeTables) {
                if (purgingIncludeTable != null && purgingIncludeTable.getValue() != null &&
                    !purgingIncludeTable.getValue().isEmpty()) {
                    stringBuffer.append(purgingIncludeTable.getValue());
                    stringBuffer.append(Constants.INCLUDE_CLASS_SPLITTER);
                }
            }
        }
        return stringBuffer.toString();
    }

    private AnalyticsDataServiceConfiguration loadAnalyticsDataServiceConfig() throws AnalyticsException {
        try {
            File confFile = new File(GenericUtils.getAnalyticsConfDirectory() +
                    File.separator + AnalyticsDataSourceConstants.ANALYTICS_CONF_DIR +
                    File.separator + ANALYTICS_DS_CONFIG_FILE);
            if (!confFile.exists()) {
                throw new AnalyticsException("Cannot initalize analytics data service, " +
                        "the analytics data service configuration file cannot be found at: " +
                        confFile.getPath());
            }
            JAXBContext ctx = JAXBContext.newInstance(AnalyticsDataServiceConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (AnalyticsDataServiceConfiguration) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new AnalyticsException(
                    "Error in processing analytics data service configuration: " + e.getMessage(), e);
        }
    }
    
    private Map<String, String> convertToMap(AnalyticsDataServiceConfigProperty[] props) {
        Map<String, String> result = new HashMap<String, String>();
        for (AnalyticsDataServiceConfigProperty prop : props) {
            result.put(prop.getName(), prop.getValue());
        }
        return result;
    }
    
    public AnalyticsDataIndexer getIndexer() {
        return indexer;
    }
    
    public AnalyticsRecordStore getAnalyticsRecordStore(String name) throws AnalyticsException {
        AnalyticsRecordStore ars = this.analyticsRecordStores.get(name);
        if (ars == null) {
            throw new AnalyticsException("Analytics record store with the name '" + name + "' cannot be found.");
        }
        return ars;
    }
    
    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        this.createTable(tenantId, this.primaryARSName, tableName);
    }

    @Override
    public void createTable(int tenantId, String recordStoreName, String tableName)
            throws AnalyticsException {
        tableName = GenericUtils.normalizeTableName(tableName);
        recordStoreName = recordStoreName.trim();
        this.getAnalyticsRecordStore(recordStoreName).createTable(tenantId, tableName);
        AnalyticsTableInfo tableInfo = null;
        try {
            tableInfo = this.lookupTableInfo(tenantId, tableName);
        } catch (AnalyticsTableNotAvailableException ignore) {
            /* ignore */
        }
        if (tableInfo == null || !tableInfo.getRecordStoreName().equals(recordStoreName)) {
            tableInfo = new AnalyticsTableInfo(recordStoreName, new AnalyticsSchema());
        }
        this.writeTableInfo(tenantId, tableName, tableInfo);
        this.writeToTenantTableMapping(tenantId, tableName);
        this.invalidateAnalyticsTableInfo(tenantId, tableName);
    }
    
    private String generateTenantTableMappingTableName(int tenantId) {
        if (tenantId < 0) {
            return TENANT_TABLE_MAPPING_TABLE_PREFIX + "_X" + Math.abs(tenantId);
        } else {
            return TENANT_TABLE_MAPPING_TABLE_PREFIX + "_" + tenantId;
        }
    }
    
    private void writeToTenantTableMapping(int tenantId, String tableName) throws AnalyticsException {
        String targetTableName = this.generateTenantTableMappingTableName(tenantId);
        Record record = new Record(tableName, TABLE_INFO_TENANT_ID, targetTableName, new HashMap<String, Object>(0));
        List<Record> records = new ArrayList<Record>(1);
        records.add(record);
        try {
            this.getPrimaryAnalyticsRecordStore().put(records);
        } catch (AnalyticsTableNotAvailableException e) {
            this.getPrimaryAnalyticsRecordStore().createTable(TABLE_INFO_TENANT_ID, targetTableName);
            this.getPrimaryAnalyticsRecordStore().put(records);
        }
    }
    
    private void deleteTenantTableMapping(int tenantId, String tableName) throws AnalyticsException {
        String targetTableName = this.generateTenantTableMappingTableName(tenantId);
        List<String> ids = new ArrayList<String>(1);
        ids.add(tableName);
        try {
            this.getPrimaryAnalyticsRecordStore().delete(TABLE_INFO_TENANT_ID, targetTableName, ids);
        } catch (AnalyticsTableNotAvailableException ignore) {
            /* ignore */
        }
    }

    @Override
    public List<String> listRecordStoreNames() {
        return new ArrayList<String>(this.analyticsRecordStores.keySet());
    }

    @Override
    public String getRecordStoreNameByTable(int tenantId, String tableName) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        tableName = GenericUtils.normalizeTableName(tableName);
        return this.lookupTableInfo(tenantId, tableName).getRecordStoreName();
    }

    @Override
    public void clearIndexData(int tenantId, String tableName) throws AnalyticsIndexException {
        tableName = GenericUtils.normalizeTableName(tableName);
        this.getIndexer().clearIndexData(tenantId, tableName);
    }
    
    @Override
    public void setTableSchema(int tenantId, String tableName, AnalyticsSchema schema)
            throws AnalyticsTableNotAvailableException, AnalyticsException {
        tableName = GenericUtils.normalizeTableName(tableName);
        this.checkInvalidIndexNames(schema.getColumns());
        this.checkInvalidScoreParams(schema.getColumns());
        AnalyticsTableInfo tableInfo = this.lookupTableInfo(tenantId, tableName);
        tableInfo.setSchema(schema);
        this.writeTableInfo(tenantId, tableName, tableInfo);
        this.checkAndInvalidateTableInfo(tenantId, tableName);
    }
    
    private void checkAndInvalidateTableInfo(int tenantId, String tableName) throws AnalyticsException {
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            /* send cluster message to invalidate */
            acm.executeAll(ANALYTICS_DATASERVICE_GROUP, new AnalyticsTableInfoChangeMessage(tenantId, tableName));
        } else {
            this.invalidateAnalyticsTableInfo(tenantId, tableName);
        }
    }
    
    private void invalidateAnalyticsTableInfo(int tenantId, String tableName) {
        this.tableInfoMap.remove(GenericUtils.calculateTableIdentity(tenantId, tableName));
    }

    private void checkInvalidIndexNames(Map<String, ColumnDefinition> columns) throws AnalyticsIndexException {
        if (columns == null) {
            return;
        }
        Set<String> columnNames = columns.keySet();
        for (String column : columnNames) {
            if (column.contains(" ")) {
                throw new AnalyticsIndexException("Index columns cannot have a space in the name: '" + column + "'");
            }
        }
        if (columnNames.contains(INDEX_ID_INTERNAL_FIELD)) {
            throw new AnalyticsIndexException("The column index '" + INDEX_ID_INTERNAL_FIELD +
                                              "' is a reserved name");
        }
        if (columnNames.contains(INDEX_INTERNAL_TIMESTAMP_FIELD)) {
            throw new AnalyticsIndexException("The column index '" + INDEX_INTERNAL_TIMESTAMP_FIELD +
                                              "' is a reserved name");
        }
        if (columnNames.contains(INDEX_INTERNAL_SCORE_FIELD)) {
            throw new AnalyticsIndexException("The column index '" + INDEX_INTERNAL_SCORE_FIELD +
                                              "' is a reserved name");
        }
        if (columnNames.contains(INDEX_INTERNAL_WEIGHT_FIELD)) {
            throw new AnalyticsIndexException("The column index '" + INDEX_INTERNAL_WEIGHT_FIELD +
                                              "' is a reserved name");
        }
    }

    private void checkInvalidScoreParams(Map<String, ColumnDefinition> columns)
            throws AnalyticsIndexException {
        if (columns == null) {
            return;
        }
        for (Map.Entry<String,ColumnDefinition> entry : columns.entrySet()) {
            AnalyticsSchema.ColumnType type =  entry.getValue().getType();
            if (type != AnalyticsSchema.ColumnType.DOUBLE && type != AnalyticsSchema.ColumnType.FLOAT &&
                type != AnalyticsSchema.ColumnType.INTEGER && type != AnalyticsSchema.ColumnType.LONG &&
                    entry.getValue().isScoreParam()) {
                throw new AnalyticsIndexException("'" + entry.getKey() +
                                                  "' is not indexed as a numeric column");
            }
        }
    }
    
    @Override
    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        tableName = GenericUtils.normalizeTableName(tableName);
        return this.lookupTableInfo(tenantId, tableName).getSchema();
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        try {
            tableName = GenericUtils.normalizeTableName(tableName);
            return this.getRecordStoreNameByTable(tenantId, tableName) != null;
        } catch (AnalyticsTableNotAvailableException e) {
            return false;
        }
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        tableName = GenericUtils.normalizeTableName(tableName);
        String arsName;
        try {
            arsName = this.getRecordStoreNameByTable(tenantId, tableName);
        } catch (AnalyticsTableNotAvailableException e) {
            return;
        }
        if (arsName == null) {
            return;
        }
        this.deleteTenantTableMapping(tenantId, tableName);
        this.deleteTableInfo(tenantId, tableName);
        this.checkAndInvalidateTableInfo(tenantId, tableName);
        this.getAnalyticsRecordStore(arsName).deleteTable(tenantId, tableName);
        this.clearIndices(tenantId, tableName);
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        String targetTableName = this.generateTenantTableMappingTableName(tenantId);
        try {
            List<Record> records = GenericUtils.listRecords(this.getPrimaryAnalyticsRecordStore(), 
                    this.getPrimaryAnalyticsRecordStore().get(TABLE_INFO_TENANT_ID, 
                            targetTableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
            List<String> result = new ArrayList<String>();
            for (Record record : records) {
                result.add(record.getId());
            }
            return result;
        } catch (AnalyticsTableNotAvailableException e) {
            return new ArrayList<String>(0);
        }
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        tableName = GenericUtils.normalizeTableName(tableName);
        String arsName = this.getRecordStoreNameByTable(tenantId, tableName);
        if (arsName == null) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        return this.getAnalyticsRecordStore(arsName).getRecordCount(tenantId, tableName, timeFrom, timeTo);
    }
    
    /**
     * This method preprocesses the records before adding to the record store,
     * e.g. update the record ids if its not already set by using the table
     * schema's primary keys.
     * @param records
     */
    private void preprocessRecords(Collection<List<Record>> recordBatches) throws AnalyticsException {
        for (List<Record> recordBatch : recordBatches) {
            this.preprocessRecordBatch(recordBatch);
        }
    }
    
    private AnalyticsTableInfo readTableInfo(int tenantId, 
            String tableName) throws AnalyticsTableNotAvailableException, AnalyticsException {
        AnalyticsRecordStore ars = this.getPrimaryAnalyticsRecordStore();
        List<String> ids = new ArrayList<String>();
        ids.add(GenericUtils.calculateTableIdentity(tenantId, tableName));
        List<Record> records;
        try {
            records = GenericUtils.listRecords(ars, ars.get(TABLE_INFO_TENANT_ID, TABLE_INFO_TABLE_NAME, 1, null, ids));            
        } catch (AnalyticsTableNotAvailableException e) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        if (records.size() == 0) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        } else {
            byte[] data = (byte[]) records.get(0).getValue(TABLE_INFO_DATA_COLUMN);
            if (data == null) {
                throw new AnalyticsException("Corrupted table info for tenant id: " + tenantId + " table: " + tableName);
            }
            return (AnalyticsTableInfo) GenericUtils.deserializeObject(data);
        }
    }
    
    private void deleteTableInfo(int tenantId, String tableName) throws AnalyticsException {
        AnalyticsRecordStore ars = this.getPrimaryAnalyticsRecordStore();
        List<String> ids = new ArrayList<String>(1);
        ids.add(GenericUtils.calculateTableIdentity(tenantId, tableName));
        try {
            ars.delete(TABLE_INFO_TENANT_ID, TABLE_INFO_TABLE_NAME, ids);
        } catch (AnalyticsTableNotAvailableException ignore) {
            /* ignore */
        }
    }
    
    private void writeTableInfo(int tenantId, 
            String tableName, AnalyticsTableInfo tableInfo) throws AnalyticsException {
        AnalyticsRecordStore ars = this.getPrimaryAnalyticsRecordStore();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(TABLE_INFO_DATA_COLUMN, GenericUtils.serializeObject(tableInfo));
        Record record = new Record(GenericUtils.calculateTableIdentity(tenantId, tableName), 
                TABLE_INFO_TENANT_ID, TABLE_INFO_TABLE_NAME, values);
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        try {
            ars.put(records);
        } catch (AnalyticsTableNotAvailableException e) {
            ars.createTable(TABLE_INFO_TENANT_ID, TABLE_INFO_TABLE_NAME);
            ars.put(records);
        }
    }
    
    private AnalyticsTableInfo lookupTableInfo(int tenantId, 
            String tableName) throws AnalyticsException, AnalyticsTableNotAvailableException {
        String tableIdentity = GenericUtils.calculateTableIdentity(tenantId, tableName);
        AnalyticsTableInfo tableInfo = this.tableInfoMap.get(tableIdentity);
        if (tableInfo == null) {
            tableInfo = this.readTableInfo(tenantId, tableName);
            this.tableInfoMap.put(tableIdentity, tableInfo);
        }
        return tableInfo;
    }
    
    private void populateWithGenerateIds(List<Record> records) {
        for (Record record : records) {
            if (record.getId() == null) {
                record.setId(GenericUtils.generateRecordID());
            }
        }
    }
    
    private String generateRecordIdFromPrimaryKeyValues(Map<String, Object> values, List<String> primaryKeys) {
        StringBuilder builder = new StringBuilder();
        Object obj;
        for (String key : primaryKeys) {
            obj = values.get(key);
            if (obj != null) {
                builder.append(obj.toString());
            }
        }
        /* to make sure, we don't have an empty string */
        builder.append("");
        try {
            byte[] data = builder.toString().getBytes(Constants.DEFAULT_CHARSET);
            return UUID.nameUUIDFromBytes(data).toString();
        } catch (UnsupportedEncodingException e) {
            /* this wouldn't happen */
            throw new RuntimeException(e);
        }
    }
    
    private void populateRecordWithPrimaryKeyAwareId(Record record, List<String> primaryKeys) {
        record.setId(this.generateRecordIdFromPrimaryKeyValues(record.getValues(), primaryKeys));
    }
    
    private void populateRecordsWithPrimaryKeyAwareIds(List<Record> records, List<String> primaryKeys) {
        for (Record record : records) {
            /* users have the ability to explicitly provide a record id,
             * in-spite of having primary keys defined to auto generate the id */
            if (record.getId() == null) {
                this.populateRecordWithPrimaryKeyAwareId(record, primaryKeys);
            }
        }
    }
    
    private void preprocessRecordBatch(List<Record> recordBatch) throws AnalyticsException {
        Record firstRecord = recordBatch.get(0);
        AnalyticsSchema schema = this.lookupTableInfo(firstRecord.getTenantId(), 
                firstRecord.getTableName()).getSchema();
        List<String> primaryKeys = schema.getPrimaryKeys();
        if (primaryKeys != null && primaryKeys.size() > 0) {
            this.populateRecordsWithPrimaryKeyAwareIds(recordBatch, primaryKeys);
        } else {
            this.populateWithGenerateIds(recordBatch);
        }
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Collection<List<Record>> recordBatches = GenericUtils.generateRecordBatches(records, true);
        this.preprocessRecords(recordBatches);
        for (List<Record> recordsBatch : recordBatches) {
            this.putSimiarRecordBatch(recordsBatch);
        }
    }
    
    private void putSimiarRecordBatch(List<Record> recordsBatch) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        Record firstRecord = recordsBatch.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        String arsName = this.getRecordStoreNameByTable(tenantId, tableName);
        this.getAnalyticsRecordStore(arsName).put(recordsBatch);
        AnalyticsSchema schema = this.lookupTableInfo(tenantId, tableName).getSchema();
        Map<String, ColumnDefinition> indexedColumns = schema.getIndexedColumns();
        if (indexedColumns.size() > 0) {
            this.getIndexer().put(recordsBatch);
        }
    }
    
    @Override
    public AnalyticsDataResponse get(int tenantId, String tableName, int numPartitionsHint, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException {
        tableName = GenericUtils.normalizeTableName(tableName);
        String arsName = this.getRecordStoreNameByTable(tenantId, tableName);
        if (arsName == null) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        RecordGroup[] rgs = this.getAnalyticsRecordStore(arsName).get(tenantId, tableName, numPartitionsHint, columns, timeFrom, 
                timeTo, recordsFrom, recordsCount);
        return new AnalyticsDataResponse(arsName, rgs);
    }
    
    public AnalyticsDataResponse getWithKeyValues(int tenantId, String tableName, int numPartitionsHint, List<String> columns,
            List<Map<String, Object>> valuesBatch) throws AnalyticsException, AnalyticsTableNotAvailableException {
        tableName = GenericUtils.normalizeTableName(tableName);
        List<String> ids = new ArrayList<String>();
        AnalyticsSchema schema = this.lookupTableInfo(tenantId, tableName).getSchema();
        List<String> primaryKeys = schema.getPrimaryKeys();
        if (primaryKeys != null && primaryKeys.size() > 0) {
            for (Map<String, Object> values : valuesBatch) {
                ids.add(this.generateRecordIdFromPrimaryKeyValues(values, primaryKeys));
            }
        }
        return this.get(tenantId, tableName, numPartitionsHint, null, ids);
    }

    @Override
    public AnalyticsDataResponse get(int tenantId, String tableName, int numPartitionsHint,
            List<String> columns, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        tableName = GenericUtils.normalizeTableName(tableName);
        String arsName = this.getRecordStoreNameByTable(tenantId, tableName);
        if (arsName == null) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        RecordGroup[] rgs = this.getAnalyticsRecordStore(arsName).get(tenantId, tableName, numPartitionsHint, columns, ids);
        return new AnalyticsDataResponse(arsName, rgs);
    }
    
    @Override
    public AnalyticsIterator<Record> readRecords(String recordStoreName, RecordGroup recordGroup) throws AnalyticsException {
        return this.getAnalyticsRecordStore(recordStoreName).readRecords(recordGroup);
    }
    
    @Override
    public boolean isPaginationSupported(String recordStoreName) throws AnalyticsException {
        return this.getAnalyticsRecordStore(recordStoreName).isPaginationSupported();
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        tableName = GenericUtils.normalizeTableName(tableName);
        String arsName = this.getRecordStoreNameByTable(tenantId, tableName);
        if (arsName == null) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        AnalyticsRecordStore ars = this.getAnalyticsRecordStore(arsName);
        Iterator<Record> recordIterator = GenericUtils.recordGroupsToIterator(ars, this.get(tenantId, tableName, 
                1, null, timeFrom, timeTo, 0, -1).getRecordGroups());
        while (recordIterator.hasNext()) {
            this.getIndexer().delete(tenantId, tableName, this.getRecordIdsBatch(recordIterator));
        }
        ars.delete(tenantId, tableName, timeFrom, timeTo);
    }
    
    private List<String> getRecordIdsBatch(Iterator<Record> recordIterator) throws AnalyticsException {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < DELETE_BATCH_SIZE & recordIterator.hasNext(); i++) {
            result.add(recordIterator.next().getId());
        }
        return result;
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        tableName = GenericUtils.normalizeTableName(tableName);
        String arsName = this.getRecordStoreNameByTable(tenantId, tableName);
        if (arsName == null) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        this.getIndexer().delete(tenantId, tableName, ids);
        this.getAnalyticsRecordStore(arsName).delete(tenantId, tableName, ids);
    }

    @Override
    public List<SearchResultEntry> search(int tenantId, String tableName, String query,
            int start, int count) throws AnalyticsIndexException, AnalyticsException {
        tableName = GenericUtils.normalizeTableName(tableName);
        return this.getIndexer().search(tenantId, tableName, query, start, count);
    }
    
    @Override
    public int searchCount(int tenantId, String tableName, String query) throws AnalyticsIndexException {
        tableName = GenericUtils.normalizeTableName(tableName);
        return this.getIndexer().searchCount(tenantId, tableName, query);
    }

    @Override
    public List<SearchResultEntry> drillDownSearch(int tenantId, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        return this.getIndexer().getDrillDownRecords(tenantId, drillDownRequest, null, null);
    }

    @Override
    public double drillDownSearchCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        return this.getIndexer().getDrillDownRecordCount(tenantId, drillDownRequest, null, null);
    }

    @Override
    public SubCategories drillDownCategories(int tenantId,
                                             CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        drillDownRequest.setTableName(GenericUtils.normalizeTableName(drillDownRequest.getTableName()));
        return this.getIndexer().drilldownCategories(tenantId, drillDownRequest);
    }

    @Override
    public List<AnalyticsDrillDownRange> drillDownRangeCount(int tenantId,
                                                             AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        drillDownRequest.setTableName(GenericUtils.normalizeTableName(drillDownRequest.getTableName()));
        return this.getIndexer().drillDownRangeCount(tenantId, drillDownRequest);
    }

    private void clearIndices(int tenantId, String tableName) throws AnalyticsIndexException, AnalyticsException {
        tableName = GenericUtils.normalizeTableName(tableName);
        this.getIndexer().clearIndexData(tenantId, tableName);
    }

    @Override
    public void waitForIndexing(long maxWait) throws AnalyticsException,
            AnalyticsTimeoutException {
        this.getIndexer().waitForIndexing(maxWait);
    }
    
    @Override
    public void destroy() throws AnalyticsException {
        if (this.indexer != null) {
            this.indexer.close();
        }
        for (AnalyticsRecordStore ars : this.analyticsRecordStores.values()) {
            ars.destroy();
        }
        try {
            this.analyticsFileSystem.destroy();
        } catch (IOException e) {
            throw new AnalyticsException("Error in analytics data service destroy: " + e.getMessage(), e);
        }
    }
    
    /**
     * This is executed to invalidate the specific analytics table information at the current node.
     */
    public static class AnalyticsTableInfoChangeMessage implements Callable<String>, Serializable {

        private static final long serialVersionUID = 299364639589319379L;

        private int tenantId;
        
        private String tableName;
        
        public AnalyticsTableInfoChangeMessage(int tenantId, String tableName) {
            this.tenantId = tenantId;
            this.tableName = tableName;
        }
        
        @Override
        public String call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            /* these cluster messages are specific to AnalyticsDataServiceImpl */
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                adsImpl.invalidateAnalyticsTableInfo(this.tenantId, this.tableName);
            }
            return "OK";
        }
        
    }
    
    /**
     * This class represents meta information about an analytics table.
     */
    public static class AnalyticsTableInfo implements Serializable {
        
        private static final long serialVersionUID = -9100036429450395707L;

        private String recordStoreName;
        
        private AnalyticsSchema schema;
        
        public AnalyticsTableInfo(String recordStoreName, AnalyticsSchema schema) {
            this.recordStoreName = recordStoreName;
            this.schema = schema;
        }
        
        public String getRecordStoreName() {
            return recordStoreName;
        }
        
        public AnalyticsSchema getSchema() {
            return schema;
        }
        
        public void setSchema(AnalyticsSchema schema) {
            this.schema = schema;
        }
        
    }
    
}
