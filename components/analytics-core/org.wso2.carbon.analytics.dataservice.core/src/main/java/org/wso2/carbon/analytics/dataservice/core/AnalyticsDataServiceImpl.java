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
package org.wso2.carbon.analytics.dataservice.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse.Entry;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsDataPurgingConfiguration;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsDataPurgingIncludeTable;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsDataServiceConfigProperty;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsDataServiceConfiguration;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsRecordStoreConfiguration;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsIndexedTableStore;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsIndexerInfo;
import org.wso2.carbon.analytics.dataservice.core.tasks.AnalyticsGlobalDataPurgingTask;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceConstants;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.base.MultitenantConstants;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
    
    private static final String ANALYTICS_DS_CONFIG_FILE = "analytics-config.xml";

    public static final String INDEX_ID_INTERNAL_FIELD = "_id";

    public static final String INDEX_INTERNAL_TIMESTAMP_FIELD = "_timestamp";

    private static final String INDEX_INTERNAL_SCORE_FIELD = "_score";

    private static final String INDEX_INTERNAL_WEIGHT_FIELD = "_weight";

    private static final String ANALYTICS_DATA_PURGING_GLOBAL = "ANALYTICS_DATA_PURGING_GLOBAL";

    private static final String GLOBAL_DATA_PURGING = "GLOBAL_DATA_PURGING";

    private static final int DELETE_BATCH_SIZE = 1000;
    
    private static final int TABLE_INFO_TENANT_ID = -1000;
    
    private static final String TABLE_INFO_TABLE_NAME = "__TABLE_INFO__";
    
    private static final String TENANT_INFO_TABLE_NAME = "__TENANT_INFO__";
    
    private static final String TENANT_TABLE_MAPPING_TABLE_PREFIX = "__TENANT_MAPPING";
    
    private static final String TABLE_INFO_DATA_COLUMN = "TABLE_INFO_DATA";
    
    private int recordsBatchSize;

    private Map<String, AnalyticsRecordStore> analyticsRecordStores;
            
    private AnalyticsDataIndexer indexer;
    
    private Map<String, AnalyticsTableInfo> tableInfoMap = new HashMap<String, AnalyticsTableInfo>();
    
    private String primaryARSName;
        
    private AnalyticsIndexedTableStore indexedTableStore;
    
    private static ThreadLocal<Boolean> initIndexedTableStore = new ThreadLocal<Boolean>() {
        @Override
        public Boolean initialValue() {
            return true;
        }
    };
    
    public AnalyticsDataServiceImpl() throws AnalyticsException {
        AnalyticsDataServiceConfiguration config = this.loadAnalyticsDataServiceConfig();
        Analyzer luceneAnalyzer;
        this.initARS(config);
        try {
            String analyzerClass = config.getLuceneAnalyzerConfiguration().getImplementation();
            luceneAnalyzer = (Analyzer) Class.forName(analyzerClass).newInstance();
        } catch (Exception e) {
            throw new AnalyticsException("Error in creating analytics data service from configuration: " + 
                    e.getMessage(), e);
        }
        this.initIndexedTableStore();
        AnalyticsIndexerInfo indexerInfo = new AnalyticsIndexerInfo();
        indexerInfo.setAnalyticsRecordStore(this.getPrimaryAnalyticsRecordStore());
        indexerInfo.setAnalyticsDataService(this);
        indexerInfo.setIndexedTableStore(this.indexedTableStore);
        indexerInfo.setShardCount(config.getShardCount());
        indexerInfo.setShardIndexRecordBatchSize(this.extractShardIndexRecordBatchSize(config));
        indexerInfo.setShardIndexWorkerInterval(this.extractShardIndexWorkerInterval(config));
        indexerInfo.setLuceneAnalyzer(luceneAnalyzer);
        indexerInfo.setIndexStoreLocation(GenericUtils.resolveLocation(Constants.DEFAULT_INDEX_STORE_LOCATION));
        indexerInfo.setIndexReplicationFactor(config.getIndexReplicationFactor());
        indexerInfo.setIndexWorkerCount(this.extractIndexWorkerCount(config));
        indexerInfo.setIndexCommunicatorBufferSize(config.getMaxIndexerCommunicatorBufferSize());
        indexerInfo.setIndexQueueCleanupThreshold(config.getQueueCleanupThreshold());
        if (config.getTaxonomyWriterCacheConfiguration() != null) {
            indexerInfo.setTaxonomyWriterCacheType(config.getTaxonomyWriterCacheConfiguration().getCacheType());
            indexerInfo.setTaxonomyWriterLRUCacheType(config.getTaxonomyWriterCacheConfiguration().getLRUType());
            indexerInfo.setTaxonomyWriterLRUCacheSize(config.getTaxonomyWriterCacheConfiguration().getCacheSize());
        }
        this.indexer = new AnalyticsDataIndexer(indexerInfo);
        AnalyticsServiceHolder.setAnalyticsDataService(this);
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            acm.joinGroup(ANALYTICS_DATASERVICE_GROUP, null);
        } 
        this.indexer.init();
        this.initDataPurging(config);
    }
    
    private int extractIndexWorkerCount(AnalyticsDataServiceConfiguration config) throws AnalyticsException {
        int value = config.getIndexWorkerCount();
        if (value <= 0) {
            throw new AnalyticsException("The index worker count must be greater than zero");
        }
        return value;
    }
    
    private long extractShardIndexRecordBatchSize(AnalyticsDataServiceConfiguration config) throws AnalyticsException {
    	long value = config.getShardIndexRecordBatchSize();
    	if (value < Constants.SHARD_INDEX_RECORD_BATCH_SIZE_MIN) {
    		throw new AnalyticsException("The shard index record batch size must to be greater than " + 
    	            Constants.SHARD_INDEX_RECORD_BATCH_SIZE_MIN + ": " + value);
    	}
    	return value;
    }
    
    private int extractShardIndexWorkerInterval(AnalyticsDataServiceConfiguration config) throws AnalyticsException {
        int value = config.getShardIndexWorkerInterval();
        if (value < Constants.SHARD_INDEX_WORKER_INTERVAL_MIN || value > Constants.SHARD_INDEX_WORKER_INTERVAL_MAX) {
            throw new AnalyticsException("The shard index worker interval value must be between " + 
                    Constants.SHARD_INDEX_WORKER_INTERVAL_MIN + " and " + Constants.SHARD_INDEX_WORKER_INTERVAL_MAX);
        }
        return value;
    }
    
    public static void setInitIndexedTableStore(boolean init) {
        initIndexedTableStore.set(init);
    }
    
    private void initIndexedTableStore() throws AnalyticsException {
        if (!initIndexedTableStore.get()) {
            return;
        }
        this.indexedTableStore = new AnalyticsIndexedTableStore();
        try {
            for (int tenantId : this.readTenantIds()) {
                for (String tableName : this.listTables(tenantId)) {                    
                    if (this.isTableIndexed(this.getTableSchema(tenantId, tableName))) {
                        this.indexedTableStore.addIndexedTable(tenantId, tableName);
                    }
                }
            }
        } catch (AnalyticsException e) {
            throw new AnalyticsException("Error in initializing indexed table store: " + e.getMessage(), e);
        }
    }
    
    private boolean isTableIndexed(AnalyticsSchema schema) {
        return schema.getIndexedColumns().size() > 0;
    }

    private void initDataPurging(AnalyticsDataServiceConfiguration config) {
        boolean dataPurgingEnable = !Boolean.getBoolean(Constants.DISABLE_ANALYTICS_DATA_PURGING_JVM_OPTION);
        logger.info("Current Node Data Purging: " + (dataPurgingEnable ? "Yes" : "No"));
        if (dataPurgingEnable) {
            TaskService taskService = AnalyticsServiceHolder.getTaskService();
            if (taskService != null) {
                // Registering task type for CApp based purging operations
                try {
                    taskService.registerTaskType(Constants.ANALYTICS_DATA_PURGING);
                } catch (TaskException e) {
                    logger.error("Unable to registry task type for CApp based purging operations: " + e.getMessage(), e);
                }
                if (config.getAnalyticsDataPurgingConfiguration() != null) {
                    final AnalyticsDataPurgingConfiguration analyticsDataPurgingConfiguration = config.getAnalyticsDataPurgingConfiguration();
                    if (analyticsDataPurgingConfiguration.isEnable()) {
                        try {
                            taskService.registerTaskType(ANALYTICS_DATA_PURGING_GLOBAL);
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
                } else {
                    logger.warn("Ignoring the data purging related operation," +
                                " since the task service is not registered in this context.");
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
        this.recordsBatchSize = config.getRecordsBatchSize();
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
        if (this.isGlobalTenantTableAccess(tenantId)) {
            for (int targetTenantId : this.readTenantIds()) {
                this.createTableFinal(targetTenantId, recordStoreName, tableName);
            }
        } else {
            this.createTableFinal(tenantId, recordStoreName, tableName);
        }
    }
    
    @Override
    public void createTableIfNotExists(int tenantId, String recordStoreName, String tableName) throws AnalyticsException {
        if (this.isGlobalTenantTableAccess(tenantId)) {
            for (int targetTenantId : this.readTenantIds()) {
                if (!this.tableExistsFinal(targetTenantId, tableName)) {
                    this.createTableFinal(targetTenantId, recordStoreName, tableName);
                }
            }
        } else {
            if (!this.tableExistsFinal(tenantId, tableName)) {
                this.createTableFinal(tenantId, recordStoreName, tableName);
            }
        }
    }

    private void createTableFinal(int tenantId, String recordStoreName, String tableName)
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
            tableInfo = new AnalyticsTableInfo(tenantId, tableName, recordStoreName, new AnalyticsSchema());
        }
        this.writeTenantId(tenantId);
        this.writeTableInfo(tenantId, tableName, tableInfo);
        this.invalidateAnalyticsTableInfo(tenantId, tableName);
    }
    
    private String generateTenantTableMappingTableName(int tenantId) {
        if (tenantId < 0) {
            return TENANT_TABLE_MAPPING_TABLE_PREFIX + "_X" + Math.abs(tenantId);
        } else {
            return TENANT_TABLE_MAPPING_TABLE_PREFIX + "_" + tenantId;
        }
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

    private AnalyticsTableInfo readTableInfo(int tenantId, 
            String tableName) throws AnalyticsTableNotAvailableException, AnalyticsException {
        String targetTableName = this.generateTenantTableMappingTableName(tenantId);
        AnalyticsRecordStore ars = this.getPrimaryAnalyticsRecordStore();
        List<String> ids = new ArrayList<String>();
        ids.add(tableName);
        List<Record> records;
        try {
            records = GenericUtils.listRecords(ars, ars.get(TABLE_INFO_TENANT_ID, targetTableName, 1, null, ids));            
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
    
    private List<Integer> readTenantIds() throws AnalyticsException {
        AnalyticsRecordStore ars = this.getPrimaryAnalyticsRecordStore();
        List<Record> records;
        try {
            records = GenericUtils.listRecords(ars, ars.get(TABLE_INFO_TENANT_ID, TENANT_INFO_TABLE_NAME, 1, 
                    null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        } catch (AnalyticsTableNotAvailableException e) {
            return new ArrayList<>(0);
        }
        List<Integer> result = new ArrayList<>();
        int tid;
        for (Record record : records) {
            try {
                tid = Integer.parseInt(record.getId());
                if (tid > 0 || tid == MultitenantConstants.SUPER_TENANT_ID) {
                    /* we only need the real tenants from this */
                    result.add(tid);
                }
            } catch (NumberFormatException e) {
                throw new AnalyticsException("Corrupted data in global tenant id list: " + record.getId(), e);
            }
        }
        return result;
    }
    
    private void writeTenantId(int tenantId) throws AnalyticsException {
        AnalyticsRecordStore ars = this.getPrimaryAnalyticsRecordStore();
        List<Record> records = new ArrayList<Record>(1);
        records.add(new Record(String.valueOf(tenantId), TABLE_INFO_TENANT_ID, TENANT_INFO_TABLE_NAME, 
                new HashMap<String, Object>(0)));
        try {
            ars.put(records);
        } catch (AnalyticsTableNotAvailableException e) {
            ars.createTable(TABLE_INFO_TENANT_ID, TENANT_INFO_TABLE_NAME);
            ars.put(records);
        }
    }

    private void writeTableInfo(int tenantId, 
            String tableName, AnalyticsTableInfo tableInfo) throws AnalyticsException {
        String targetTableName = this.generateTenantTableMappingTableName(tenantId);
        AnalyticsRecordStore ars = this.getPrimaryAnalyticsRecordStore();
        Map<String, Object> values = new HashMap<String, Object>(1);
        values.put(TABLE_INFO_DATA_COLUMN, GenericUtils.serializeObject(tableInfo));
        Record record = new Record(tableName, TABLE_INFO_TENANT_ID, targetTableName, values);
        List<Record> records = new ArrayList<Record>(1);
        records.add(record);
        try {
            ars.put(records);
        } catch (AnalyticsTableNotAvailableException e) {
            ars.createTable(TABLE_INFO_TENANT_ID, targetTableName);
            ars.put(records);
        }
    }
    
    /**
     * This method is used to convert DAS v3.x type table metadata to DAS v3.1+ metadata.
     * This is required in implementing reading tenant data from super-tenant, which requires
     * to read in all the tenant ids and their tables, so for this requirement, the table metadata
     * structure has been modified to cater that.
     */
    public void convertTableInfoFromv30Tov31() throws AnalyticsException {
        AnalyticsRecordStore ars = this.getPrimaryAnalyticsRecordStore();
        List<Record> records;
        try {
            records = GenericUtils.listRecords(ars, ars.get(TABLE_INFO_TENANT_ID, TABLE_INFO_TABLE_NAME, 1, null, 
                    Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));            
        } catch (AnalyticsTableNotAvailableException e) {
            throw new AnalyticsException("Table info data does not exist");
        }
        for (Record record : records) {
            byte[] data = (byte[]) record.getValue(TABLE_INFO_DATA_COLUMN);
            if (data == null) {
                System.out.println("Corrupted analytics table info with table identity: " + 
                        record.getId() + " -> " + record.getValues());
            }
            AnalyticsTableInfo tableInfo;
            try {
                tableInfo = (AnalyticsTableInfo) GenericUtils.deserializeObject(data);
            } catch (Exception e) {
                System.out.println("Corrupted analytics table info with table identity: " + 
                        record.getId() + " -> " + e.getMessage());
                continue;
            }
            this.writeTenantId(tableInfo.getTenantId());
            this.writeTableInfo(tableInfo.getTenantId(), tableInfo.getTableName(), tableInfo);
            System.out.println("Table [" + tableInfo.getTenantId() + "][" + tableInfo.getTableName() + "] converted.");
        }
    }

    @Override
    public List<String> listRecordStoreNames() {
        List<String> result = new ArrayList<String>(this.analyticsRecordStores.keySet());
        /* add the primary record store name as the first one */
        result.remove(this.primaryARSName);
        result.add(0, this.primaryARSName);
        return result;
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
        try {
            this.getIndexer().clearIndexData(tenantId, tableName);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException("Error in clearing index data: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void setTableSchema(int tenantId, String tableName, AnalyticsSchema schema) 
            throws AnalyticsTableNotAvailableException, AnalyticsException {    
        if (this.isGlobalTenantTableAccess(tenantId)) {
            for (int targetTenantId : this.readTenantIds()) {
                this.setTableSchemaFinal(targetTenantId, tableName, schema);
            }
        } else {
            this.setTableSchemaFinal(tenantId, tableName, schema);
        }
    }
    
    public void setTableSchemaFinal(int tenantId, String tableName, AnalyticsSchema schema)
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
    
    private void refreshIndexedTableStoreEntry(int tenantId, String tableName) {
        try {
            AnalyticsTableInfo tableInfo = this.readTableInfo(tenantId, tableName);
            if (this.isTableIndexed(tableInfo.getSchema())) {
                this.indexedTableStore.addIndexedTable(tenantId, tableName);
            }
        } catch (AnalyticsTableNotAvailableException e) {
            this.indexedTableStore.removeIndexedTable(tenantId, tableName);
        } catch (AnalyticsException e) {
            logger.error("Error in refreshing indexed table store entry for tenant: " + 
                    tenantId + " table: " + tableName + " : " + e.getMessage(), e);
        }
    }
    
    public void invalidateAnalyticsTableInfo(int tenantId, String tableName) {
        if (this.isGlobalTenantTableAccess(tenantId)) {
            List<Integer> tids;
            try {
                tids = this.readTenantIds();
            } catch (AnalyticsException e) {
                throw new RuntimeException("Error in reading tenant ids when analytics table invalidation: " + 
                        e.getMessage(), e);
            }
            for (int targetTenantId : tids) {
                this.invalidateAnalyticsTableInfoFinal(targetTenantId, tableName);
            }
        } else {
            this.invalidateAnalyticsTableInfoFinal(tenantId, tableName);
        }
    }
    
    public void invalidateAnalyticsTableInfoFinal(int tenantId, String tableName) {
        tableName = GenericUtils.normalizeTableName(tableName);
        this.tableInfoMap.remove(GenericUtils.calculateTableIdentity(tenantId, tableName));
        this.refreshIndexedTableStoreEntry(tenantId, tableName);
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
    
    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        if (this.isGlobalTenantTableAccess(tenantId)) {
            for (int targetTenantId : this.readTenantIds()) {
                try {
                    return this.getTableSchemaFinal(targetTenantId, tableName);
                } catch (AnalyticsTableNotAvailableException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No table found for tenantId: " + targetTenantId + " tableName: " + tableName);
                    }
                }
            }
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        } else {
            return this.getTableSchemaFinal(tenantId, tableName);
        }
    }
    
    public AnalyticsSchema getTableSchemaFinal(int tenantId, String tableName) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        tableName = GenericUtils.normalizeTableName(tableName);
        return this.lookupTableInfo(tenantId, tableName).getSchema();
    }
    
    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        if (this.isGlobalTenantTableAccess(tenantId)) {
            for (int targetTenantId : this.readTenantIds()) {
                if (!this.tableExistsFinal(targetTenantId, tableName)) {
                    return false;
                }
            }
            /* the table in all the tenants was there, if the tenant itself was there */
            return true;
        } else {
            return this.tableExistsFinal(tenantId, tableName);
        }
    }

    public boolean tableExistsFinal(int tenantId, String tableName) throws AnalyticsException {
        try {
            tableName = GenericUtils.normalizeTableName(tableName);
            return this.getRecordStoreNameByTable(tenantId, tableName) != null;
        } catch (AnalyticsTableNotAvailableException e) {
            return false;
        }
    }
    
    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        if (this.isGlobalTenantTableAccess(tenantId)) {
            for (int targetTenantId : this.readTenantIds()) {
                this.deleteTableFinal(targetTenantId, tableName);
            }
        } else {
            this.deleteTableFinal(tenantId, tableName);
        }
    }

    public void deleteTableFinal(int tenantId, String tableName) throws AnalyticsException {
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
        this.deleteTableInfo(tenantId, tableName);
        this.checkAndInvalidateTableInfo(tenantId, tableName);
        this.getAnalyticsRecordStore(arsName).deleteTable(tenantId, tableName);
        this.clearIndices(tenantId, tableName);
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
     * @param recordBatches
     */
    private void preprocessRecords(Collection<List<Record>> recordBatches) throws AnalyticsException {
        for (List<Record> recordBatch : recordBatches) {
            this.preprocessRecordBatch(recordBatch);
        }
    }
    
    private void deleteTableInfo(int tenantId, String tableName) throws AnalyticsException {
        String targetTableName = this.generateTenantTableMappingTableName(tenantId);
        List<String> ids = new ArrayList<String>(1);
        ids.add(tableName);
        try {
            this.getPrimaryAnalyticsRecordStore().delete(TABLE_INFO_TENANT_ID, targetTableName, ids);
        } catch (AnalyticsTableNotAvailableException ignore) {
            /* ignore */
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

    /*
    the users should ensure that the order of the primary key list is independent of the order:
    check DAS-289
     */
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
            this.putSimilarRecordBatch(recordsBatch);
        }
    }
    
    private void putSimilarRecordBatch(List<Record> recordsBatch) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        Record firstRecord = recordsBatch.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        String arsName = this.getRecordStoreNameByTable(tenantId, tableName);
        this.getAnalyticsRecordStore(arsName).put(recordsBatch);
        if (this.isTableIndexed(this.lookupTableInfo(tenantId, tableName).getSchema())) {
            this.getIndexer().put(recordsBatch);
        }
    }
    
    private boolean isGlobalTenantTableAccess(int tenantId) {
        return tenantId == Constants.GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID;
    }
    
    @Override
    public AnalyticsDataResponse get(int tenantId, String tableName, int numPartitionsHint, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (this.isGlobalTenantTableAccess(tenantId)) {
            if (recordsFrom != 0 && recordsCount != Integer.MAX_VALUE) {
                throw new AnalyticsException("Global analytics data lookup cannot be done on a dataset subset, "
                        + "recordsFrom: " + recordsFrom + " recordsCount: " + recordsCount);
            }
            return this.getByGlobalLookup(tableName, numPartitionsHint, columns, timeFrom, timeTo);
        } else {
            return this.getByRange(tenantId, tableName, numPartitionsHint, columns, timeFrom, timeTo, 
                    recordsFrom, recordsCount);
        }
    }
    
    private AnalyticsDataResponse getByGlobalLookup(String tableName, int numPartitionsHint, List<String> columns,
            long timeFrom, long timeTo) throws AnalyticsException, AnalyticsTableNotAvailableException {
        List<Entry> responseEntries = new ArrayList<>();
        List<Integer> tenants = this.readTenantIds();
        if (logger.isDebugEnabled()) {
            logger.debug("Global Data Lookup, Tenant Count: " + tenants.size());
        }
        if (tenants.isEmpty()) {
            throw new AnalyticsTableNotAvailableException(Constants.GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID, tableName);
        }
        /* here, the assumption is, most of the tenants will have the target table we are looking for,
         * to have a good distribution of partitions */
        numPartitionsHint = (int) Math.ceil(numPartitionsHint / (double) tenants.size());
        for (int tenantId : tenants) {
            try {
                responseEntries.addAll(this.getByRange(tenantId, tableName, 
                        numPartitionsHint, columns, timeFrom, timeTo, 0, Integer.MAX_VALUE).getEntries());
            } catch (AnalyticsTableNotAvailableException ignore) { /* ignore */ }
        }
        if (responseEntries.isEmpty()) {
            throw new AnalyticsTableNotAvailableException(Constants.GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID, tableName);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Global Data Lookup, Response Entries Count: " + responseEntries.size());
        }
        return new AnalyticsDataResponse(responseEntries);
    }
    
    private AnalyticsDataResponse getByRange(int tenantId, String tableName, int numPartitionsHint, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException {
        tableName = GenericUtils.normalizeTableName(tableName);
        String arsName = this.getRecordStoreNameByTable(tenantId, tableName);
        if (arsName == null) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        RecordGroup[] rgs;
        if (this.isTimestampRangePartitionsCompatible(numPartitionsHint, timeFrom, timeTo, recordsFrom, recordsCount)) {
            List<RecordGroup> rgList = new ArrayList<>(numPartitionsHint);
            List<Long[]> tsRanges = this.splitTimestampRangeForPartitions(timeFrom, timeTo, numPartitionsHint);
            for (Long[] tsRange : tsRanges) {
                rgList.addAll(Arrays.asList(this.getAnalyticsRecordStore(arsName).get(tenantId, tableName, 
                        1, columns, tsRange[0], tsRange[1], recordsFrom, recordsCount)));
            }
            rgs = rgList.toArray(new RecordGroup[0]);
        } else {
            rgs = this.getAnalyticsRecordStore(arsName).get(tenantId, tableName, numPartitionsHint, columns, timeFrom, 
                    timeTo, recordsFrom, recordsCount);            
        }
        return new AnalyticsDataResponse(this.createResponseEntriesFromSingleRecordStore(arsName, rgs));
    }
    
    private List<Entry> createResponseEntriesFromSingleRecordStore(String arsName, 
            RecordGroup[] rgs) {
        List<Entry> entries = new ArrayList<>(rgs.length);
        for (RecordGroup rg : rgs) {
            entries.add(new Entry(arsName, rg));
        }
        return entries;
    }
    
    private List<Long[]> splitTimestampRangeForPartitions(long timeFrom, long timeTo, int numPartitionsHint) {
        List<Long[]> result = new ArrayList<>();
        int delta = (int) Math.ceil((timeTo - timeFrom) / (double) numPartitionsHint);
        long val = timeFrom;
        while (true) {
            if (val + delta >= timeTo) {
                result.add(new Long[] { val, timeTo });
                break;
            } else {
                result.add(new Long[] { val, val + delta });
                val += delta;
            }            
        }
        return result;
    }
    
    private boolean isTimestampRangePartitionsCompatible(int numPartitionsHint, long timeFrom, long timeTo, 
            int recordsFrom, int recordsCount) {
        return numPartitionsHint > 1 && timeFrom != Long.MIN_VALUE  && timeTo != Long.MAX_VALUE && recordsFrom == 0 && 
                (recordsCount == -1 || recordsCount == Integer.MAX_VALUE); 
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
        return this.get(tenantId, tableName, numPartitionsHint, columns, ids);
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
        List<List<String>> idsSubLists = getChoppedLists(ids, this.recordsBatchSize);
        List<RecordGroup> recordGroups = new ArrayList<>();

        for (List<String> idSubList : idsSubLists) {
            ArrayList<RecordGroup> recordGroupSubList = new ArrayList<>(Arrays.asList(
                    this.getAnalyticsRecordStore(arsName).get(tenantId, tableName, numPartitionsHint, columns, idSubList)));
            recordGroups.addAll(recordGroupSubList);
        }

        RecordGroup[] rgs = new RecordGroup[recordGroups.size()];
        rgs = recordGroups.toArray(rgs);
        return new AnalyticsDataResponse(this.createResponseEntriesFromSingleRecordStore(arsName, rgs));
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
    public boolean isRecordCountSupported(String recordStoreName) throws AnalyticsException {
        return this.getAnalyticsRecordStore(recordStoreName).isRecordCountSupported();
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        tableName = GenericUtils.normalizeTableName(tableName);
        /* this is done to make sure, raw record data as well as the index data are also deleted,
         * even if the table is not indexed now, it could have been indexed earlier, so delete operation
         * must be done in the indexer as well */
        while (true) {
            List<Record> recordBatch = AnalyticsDataServiceUtils.listRecords(this, this.get(tenantId, tableName, 1,
                    null, timeFrom, timeTo, 0, DELETE_BATCH_SIZE));
            if (recordBatch.size() == 0) {
                break;
            }
            this.delete(tenantId, tableName, this.getRecordIdsBatch(recordBatch));
        }
    }
    
    private List<String> getRecordIdsBatch(List<Record> recordsBatch) throws AnalyticsException {
        List<String> result = new ArrayList<>(recordsBatch.size());
        for (Record record : recordsBatch) {
            result.add(record.getId());
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
        /* the below ordering is important, the raw records should be deleted first */
        this.getAnalyticsRecordStore(arsName).delete(tenantId, tableName, ids);
        this.getIndexer().delete(tenantId, tableName, ids);
    }

    @Override
    public List<SearchResultEntry> search(int tenantId, String tableName, String query, int start,
                                          int count) throws AnalyticsException {
        return this.search(tenantId, tableName, query, start, count, null);
    }

    @Override
    public List<SearchResultEntry> search(int tenantId, String tableName, String query,
            int start, int count, List<SortByField> sortByFields) throws AnalyticsIndexException, AnalyticsException {
        tableName = GenericUtils.normalizeTableName(tableName);
        return this.getIndexer().search(tenantId, tableName, query, start, count, sortByFields);
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

    @Override
    public AnalyticsIterator<Record> searchWithAggregates(int tenantId, AggregateRequest aggregateRequest)
            throws AnalyticsException {
        aggregateRequest.setTableName(GenericUtils.normalizeTableName(aggregateRequest.getTableName()));
        return this.getIndexer().searchWithAggregates(tenantId, aggregateRequest);
    }

    @Override
    public List<AnalyticsIterator<Record>> searchWithAggregates(int tenantId,
                                                          AggregateRequest[] aggregateRequests)
            throws AnalyticsException {
        List<AnalyticsIterator<Record>> iterators = new ArrayList<>();
        for (AggregateRequest request : aggregateRequests) {
            iterators.add(this.searchWithAggregates(tenantId, request));
        }
        return iterators;
    }

    @Override
    public void reIndex(int tenantId, String tableName, long startTime, long endTime)
            throws AnalyticsException {
        String table = GenericUtils.normalizeTableName(tableName);
        if (this.isTableIndexed(this.getTableSchema(tenantId, table))) {
            this.getIndexer().reIndex(tenantId, table, startTime, endTime);
        }
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
    public void waitForIndexing(int tenantId, String tableName, long maxWait) throws AnalyticsException,
            AnalyticsTimeoutException {
        this.getIndexer().waitForIndexing(tenantId, tableName, maxWait);
    }
    
    @Override
    public void destroy() throws AnalyticsException {
        if (this.indexer != null) {
            this.indexer.close();
        }
        for (AnalyticsRecordStore ars : this.analyticsRecordStores.values()) {
            ars.destroy();
        }
    }

    private <T> List<List<T>> getChoppedLists(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<List<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(list.subList(i, Math.min(N, i + L))));
        }
        return parts;
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

        private int tenantId;
        
        private String tableName;
        
        private String recordStoreName;
        
        private AnalyticsSchema schema;
        
        public AnalyticsTableInfo() { }
        
        public AnalyticsTableInfo(int tenantId, String tableName, String recordStoreName, AnalyticsSchema schema) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.recordStoreName = recordStoreName;
            this.schema = schema;
        }
        
        public int getTenantId() {
            return tenantId;
        }
        
        public String getTableName() {
            return tableName;
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

    public static class MultiTableAggregateIterator implements AnalyticsIterator<Record> {

        private AnalyticsIterator<Record> currentItr;
        private List<AnalyticsIterator<Record>> iterators;

        public MultiTableAggregateIterator(List<AnalyticsIterator<Record>> iterators)
                throws AnalyticsException {
            this.iterators = iterators;
        }

        @Override
        public boolean hasNext() {
            if (iterators == null) {
                return false;
            } else {
                if (currentItr == null) {
                    if (!iterators.isEmpty()) {
                        currentItr = iterators.remove(0);
                        return this.hasNext();
                    } else {
                        return false;
                    }
                } else {
                    if (!currentItr.hasNext()) {
                        if (!iterators.isEmpty()) {
                            currentItr = iterators.remove(0);
                            return this.hasNext();
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }

        @Override
        public Record next() {
            if (this.hasNext()) {
                return currentItr.next();
            } else {
                return null;
            }
        }

        @Override
        public void remove() {
            /* ignored */
        }

        @Override
        public void close() throws IOException {
            for (AnalyticsIterator<Record> iterator : iterators) {
                iterator.close();
            }
        }
    }
    
}
