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
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataPurgingConfiguration;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataPurgingIncludeTable;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataServiceConfigProperty;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataServiceConfiguration;
import org.wso2.carbon.analytics.dataservice.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.dataservice.tasks.AnalyticsGlobalDataPurgingTask;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
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

    private AnalyticsRecordStore analyticsRecordStore;
    
    private AnalyticsFileSystem analyticsFileSystem;
        
    private AnalyticsDataIndexer indexer;
    
    private Map<String, AnalyticsSchema> schemaMap = new HashMap<String, AnalyticsSchema>();
    
    public AnalyticsDataServiceImpl() throws AnalyticsException {
        AnalyticsDataServiceConfiguration config = this.loadAnalyticsDataServiceConfig();
        Analyzer luceneAnalyzer;
        try {
            String arsClass = config.getAnalyticsRecordStoreConfiguration().getImplementation();
            String afsClass = config.getAnalyticsFileSystemConfiguration().getImplementation();
            String analyzerClass = config.getLuceneAnalyzerConfiguration().getImplementation();
            this.analyticsRecordStore = (AnalyticsRecordStore) Class.forName(arsClass).newInstance();
            this.analyticsFileSystem = (AnalyticsFileSystem) Class.forName(afsClass).newInstance();
            this.analyticsRecordStore.init(this.convertToMap(config.getAnalyticsRecordStoreConfiguration().getProperties()));
            this.analyticsFileSystem.init(this.convertToMap(config.getAnalyticsFileSystemConfiguration().getProperties()));
            luceneAnalyzer = (Analyzer) Class.forName(analyzerClass).newInstance();
        } catch (Exception e) {
            throw new AnalyticsException("Error in creating analytics data service from configuration: " + 
                    e.getMessage(), e);
        }
        this.indexer = new AnalyticsDataIndexer(this.analyticsRecordStore, this.analyticsFileSystem, this,
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
                    logger.error("Unable to schedule global data puring task: " + e.getMessage(), e);
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

    private TaskInfo createDataPurgingTask(AnalyticsDataPurgingConfiguration analyticsDataPurgingConfiguration) {
        String taskName = GLOBAL_DATA_PURGING;
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo(analyticsDataPurgingConfiguration.getCronExpression());
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
    
    public AnalyticsRecordStore getAnalyticsRecordStore() {
        return analyticsRecordStore;
    }
    
    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        this.getAnalyticsRecordStore().createTable(tenantId, tableName);
    }

    @Override
    public void clearIndexData(int tenantId, String tableName) throws AnalyticsIndexException {
        this.getIndexer().clearIndexData(tenantId, tableName);
    }
    
    @Override
    public void setTableSchema(int tenantId, String tableName, AnalyticsSchema schema)
            throws AnalyticsTableNotAvailableException, AnalyticsException {
        this.checkInvalidIndexNames(schema.getColumns());
        this.checkInvalidScoreParams(schema.getColumns());
        this.getAnalyticsRecordStore().setTableSchema(tenantId, tableName, schema);
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            /* send cluster message to invalidate */
            acm.executeAll(ANALYTICS_DATASERVICE_GROUP, new AnalyticsSchemaChangeMessage(tenantId, tableName));
        } else {
            this.invalidateAnalyticsSchema(tenantId, tableName);
        }
    }
    
    private void invalidateAnalyticsSchema(int tenantId, String tableName) {
        this.schemaMap.remove(GenericUtils.calculateTableIdentity(tenantId, tableName));
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
        return this.lookupSchema(tenantId, tableName);
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        return this.getAnalyticsRecordStore().tableExists(tenantId, tableName);
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        this.getAnalyticsRecordStore().deleteTable(tenantId, tableName);
        this.setTableSchema(tenantId, tableName, new AnalyticsSchema());
        this.clearIndices(tenantId, tableName);
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        return this.getAnalyticsRecordStore().listTables(tenantId);
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return this.getAnalyticsRecordStore().getRecordCount(tenantId, tableName, timeFrom, timeTo);
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
    
    private AnalyticsSchema lookupSchema(int tenantId, String tableName) throws AnalyticsException {
        String tableIdentity = GenericUtils.calculateTableIdentity(tenantId, tableName);
        AnalyticsSchema schema = this.schemaMap.get(tableIdentity);
        if (schema == null) {
            schema = this.getAnalyticsRecordStore().getTableSchema(tenantId, tableName);
            this.schemaMap.put(tableIdentity, schema);
        }
        return schema;
    }
    
    private void populateWithGenerateIds(List<Record> records) {
        for (Record record : records) {
            if (record.getId() == null) {
                record.setId(GenericUtils.generateRecordID());
            }
        }
    }
    
    private void populateRecordWithPrimaryKeyAwareId(Record record, List<String> primaryKeys) {
        StringBuilder builder = new StringBuilder();
        Object obj;
        for (String key : primaryKeys) {
            obj = record.getValue(key);
            if (obj != null) {
                builder.append(obj.toString());
            }
        }
        /* to make sure, we don't have an empty string */
        builder.append("");
        try {
            byte[] data = builder.toString().getBytes(Constants.DEFAULT_CHARSET);
            record.setId(UUID.nameUUIDFromBytes(data).toString());
        } catch (UnsupportedEncodingException e) {
            /* this wouldn't happen */
            throw new RuntimeException(e);
        }
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
        AnalyticsSchema schema = this.lookupSchema(firstRecord.getTenantId(), firstRecord.getTableName());
        List<String> primaryKeys = schema.getPrimaryKeys();
        if (primaryKeys != null && primaryKeys.size() > 0) {
            this.populateRecordsWithPrimaryKeyAwareIds(recordBatch, primaryKeys);
        } else {
            this.populateWithGenerateIds(recordBatch);
        }
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Collection<List<Record>> recordBatches = GenericUtils.generateRecordBatches(records);
        this.preprocessRecords(recordBatches);
        this.getAnalyticsRecordStore().put(records);
        for (List<Record> recordsBatch : recordBatches) {
            Record record = recordsBatch.get(0);
            AnalyticsSchema schema = this.lookupSchema(record.getTenantId(), record.getTableName());
            Map<String, ColumnDefinition> indexedColumns = schema.getIndexedColumns();
            if (indexedColumns.size() > 0) {
                this.getIndexer().put(records);
            }
        }
    }
    
    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException {
        return this.getAnalyticsRecordStore().get(tenantId, tableName, numPartitionsHint, columns, timeFrom, 
                timeTo, recordsFrom, recordsCount);
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint,
            List<String> columns, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return this.getAnalyticsRecordStore().get(tenantId, tableName, numPartitionsHint, columns, ids);
    }
    
    @Override
    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        return this.getAnalyticsRecordStore().readRecords(recordGroup);
    }
    
    @Override
    public boolean isPaginationSupported() {
        return this.getAnalyticsRecordStore().isPaginationSupported();
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        this.getIndexer().delete(tenantId, tableName, 
                this.getRecordIdsFromTimeRange(tenantId, tableName, timeFrom, timeTo));
        this.getAnalyticsRecordStore().delete(tenantId, tableName, timeFrom, timeTo);
    }
    
    private List<String> getRecordIdsFromTimeRange(int tenantId, String tableName, long timeFrom, 
            long timeTo) throws AnalyticsException {
        List<Record> records = GenericUtils.listRecords(this,
                                                        this.get(tenantId, tableName, 1, null, timeFrom, timeTo, 0, -1));
        List<String> result = new ArrayList<>(records.size());
        for (Record record : records) {
            result.add(record.getId());
        }
        return result;
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        this.getIndexer().delete(tenantId, tableName, ids);
        this.getAnalyticsRecordStore().delete(tenantId, tableName, ids);
    }

    @Override
    public List<SearchResultEntry> search(int tenantId, String tableName, String query,
            int start, int count) throws AnalyticsIndexException, AnalyticsException {
        return this.getIndexer().search(tenantId, tableName, query, start, count);
    }
    
    @Override
    public int searchCount(int tenantId, String tableName, String query) throws AnalyticsIndexException {
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
        return this.getIndexer().drilldownCategories(tenantId, drillDownRequest);
    }

    @Override
    public List<AnalyticsDrillDownRange> drillDownRangeCount(int tenantId,
                                                             AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        return this.getIndexer().drillDownRangeCount(tenantId, drillDownRequest);
    }

    private void clearIndices(int tenantId, String tableName) throws AnalyticsIndexException, AnalyticsException {
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
        this.analyticsRecordStore.destroy();
        try {
            this.analyticsFileSystem.destroy();
        } catch (IOException e) {
            throw new AnalyticsException("Error in ADS destroy: " + e.getMessage(), e);
        }
    }
    
    /**
     * This is executed to invalidate the specific analytics schema information at the current node.
     */
    public static class AnalyticsSchemaChangeMessage implements Callable<String>, Serializable {

        private static final long serialVersionUID = 299364639589319379L;

        private int tenantId;
        
        private String tableName;
        
        public AnalyticsSchemaChangeMessage(int tenantId, String tableName) {
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
                adsImpl.invalidateAnalyticsSchema(this.tenantId, this.tableName);
            }
            return "OK";
        }
        
    }
    
}
