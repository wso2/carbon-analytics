package org.wso2.analytics.api.commons.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.analytics.api.commons.AnalyticsDataAPI;
import org.wso2.analytics.api.commons.CompositeSchema;
import org.wso2.analytics.api.commons.utils.DataAPIUtils;
import org.wso2.analytics.data.commons.AnalyticsDataService;
import org.wso2.analytics.data.commons.exception.AnalyticsException;
import org.wso2.analytics.data.commons.service.AnalyticsDataResponse;
import org.wso2.analytics.data.commons.service.AnalyticsSchema;
import org.wso2.analytics.data.commons.sources.AnalyticsIterator;
import org.wso2.analytics.data.commons.sources.Record;
import org.wso2.analytics.data.commons.sources.RecordGroup;
import org.wso2.analytics.data.commons.utils.AnalyticsCommonUtils;
import org.wso2.analytics.indexerservice.CarbonIndexDocument;
import org.wso2.analytics.indexerservice.CarbonIndexerService;
import org.wso2.analytics.indexerservice.IndexSchema;
import org.wso2.analytics.indexerservice.exceptions.IndexSchemaNotFoundException;
import org.wso2.analytics.indexerservice.exceptions.IndexerException;
import org.wso2.analytics.indexerservice.impl.CarbonIndexerClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implementation class of AnalyticsDataAPI - Implements the DAL level APIs with indexing.
 */
public class AnalyticsDataAPIImpl implements AnalyticsDataAPI {

    private static Log log = LogFactory.getLog(AnalyticsDataAPIImpl.class);

    private static final String TEMP_INDEX_DATA_STORE = "_TEMP_INDEX_DATA_STORE_";
    private static final String FIELD_INDEX_DATA = "_FIELD_INDEX_DATA_";
    private static final String FIELD_INDEX_DATA_TABLE_NAME = "_FIELD_INDEX_DATA_TABLE_NAME_";
    private static final String TEMP_INDEX_SCHEMA_STORE = "_TEMP_INDEX_SCHEMA_STORE_";
    private static final String FIELD_INDEX_SCHEMA = "_FIELD_INDEX_SCHEMA_";
    private static final String FIELD_INDEX_SCHEMA_MERGE = "_FIELD_INDEX_SCHEMA_MERGE_";

    private static final int NO_STAGING_INDEX_WORKERS = 2;
    private static final int STAGING_INDEX_WORKER_EXECUTOR_TIMEOUT_MILLI_SEC = 60000;
    private static final int RETRY_INDEX_DOC_BATCH_SIZE = 100;

    private CarbonIndexerService indexerService;
    private AnalyticsDataService analyticsDataService;
    private ExecutorService stagingIndexWorkerExecutor;
    private StagingIndexWorker stagingIndexWorker;
    private StagingIndexSchemaWorker stagingIndexSchemaWorker;

    public AnalyticsDataAPIImpl() throws AnalyticsException {
        ServiceLoader<CarbonIndexerService> indexerServiceServiceLoader = ServiceLoader.load(CarbonIndexerService.class);
        ServiceLoader<AnalyticsDataService> analyticsDataServiceServiceLoader = ServiceLoader.load(AnalyticsDataService.class);
        while(indexerService == null && indexerServiceServiceLoader.iterator().hasNext()) {
            indexerService = indexerServiceServiceLoader.iterator().next();
        }
        while(analyticsDataService == null && analyticsDataServiceServiceLoader.iterator().hasNext()) {
            analyticsDataService = analyticsDataServiceServiceLoader.iterator().next();
        }
        if (analyticsDataService == null) {
            throw new AnalyticsException("Cannot load AnalyticsDataService..");
        }
        if (indexerService == null) {
            throw new AnalyticsException("Cannot load CarbonIndexerService..");
        }
        scheduleStagingIndexWorkers();
    }

    private void scheduleStagingIndexWorkers() {
        stagingIndexWorkerExecutor = Executors.newFixedThreadPool(NO_STAGING_INDEX_WORKERS);
        stagingIndexWorker = new StagingIndexWorker();
        stagingIndexSchemaWorker = new StagingIndexSchemaWorker();
        stagingIndexWorkerExecutor.execute(stagingIndexWorker);
        stagingIndexWorkerExecutor.execute(stagingIndexSchemaWorker);
    }

    @Override
    public List<String> listRecordStoreNames() {
        return analyticsDataService.listRecordStoreNames();
    }

    @Override
    public void createTable(String recordStoreName, String tableName) throws AnalyticsException {
        analyticsDataService.createTable(recordStoreName, tableName);
    }

    @Override
    public void createTable(String tableName) throws AnalyticsException {
        analyticsDataService.createTable(tableName);
    }

    @Override
    public void createTableIfNotExists(String recordStoreName, String tableName)
            throws AnalyticsException {
        analyticsDataService.createTableIfNotExists(recordStoreName, tableName);
    }

    @Override
    public String getRecordStoreNameByTable(String tableName) throws AnalyticsException {
        return analyticsDataService.getRecordStoreNameByTable(tableName);
    }

    @Override
    public void setTableSchema(String tableName, CompositeSchema schema, boolean merge) throws AnalyticsException {
        analyticsDataService.setTableSchema(tableName, schema.getAnalyticsSchema());
        IndexSchema indexSchema = schema.getIndexSchema();
        try {
            if (indexSchema != null && !indexSchema.getFields().isEmpty()) {
                indexerService.updateIndexSchema(tableName, schema.getIndexSchema(), merge);
                indexerService.createIndexForTable(tableName);
            }
        } catch (IndexerException e) {
            log.error("Error while updating the index schema for table : " + tableName, e);
            log.info("Inserting index Schema details to staging area, as the index schema update failed..");
            AddIndexSchemaToStagingArea(tableName, indexSchema, merge);
            throw new AnalyticsException("Error while updating the index schema for table : " + tableName, e);
        }
    }

    /**
     * Keep the indexSchema in a backup area if the solr server is not available or the indexer service is not available
     * @param tableName table name of the index schema
     * @param indexSchema The index Schema
     * @throws AnalyticsException If the DAL.put() didn't work
     */
    private void AddIndexSchemaToStagingArea(String tableName, IndexSchema indexSchema, boolean merge)
            throws AnalyticsException {
        if (!analyticsDataService.tableExists(TEMP_INDEX_SCHEMA_STORE)) {
            analyticsDataService.createTable(TEMP_INDEX_SCHEMA_STORE);
        }
        Map<String, Object> values = new HashMap<>(2);
        values.put(FIELD_INDEX_SCHEMA, AnalyticsCommonUtils.serializeObject(indexSchema));
        values.put(FIELD_INDEX_SCHEMA_MERGE, merge);
        Record record = new Record(tableName, TEMP_INDEX_SCHEMA_STORE, values);
        List<Record> records = new ArrayList<>(1);
        records.add(record);
        analyticsDataService.put(records);
    }

    @Override
    public CompositeSchema getTableSchema(String tableName)
            throws AnalyticsException {
        CompositeSchema compositeSchema = new CompositeSchema();
        try {
            AnalyticsSchema analyticsSchema = analyticsDataService.getTableSchema(tableName);
            compositeSchema.setAnalyticsSchema(analyticsSchema);
            IndexSchema indexSchema = indexerService.getIndexSchema(tableName);
            compositeSchema.setIndexSchema(indexSchema);
        } catch (IndexSchemaNotFoundException e) {
            //table does not have a corresponding index schema. don't do anything
        } catch (IndexerException e) {
            log.error("Error while retrieving index Schema for table " + tableName +": " + e.getMessage(), e);
            throw new AnalyticsException("Error while retrieving index Schema for table " + tableName +": " + e.getMessage(), e);
        }
        return compositeSchema;
    }

    @Override
    public boolean tableExists(String tableName) throws AnalyticsException {
        return analyticsDataService.tableExists(tableName);
    }

    @Override
    public void deleteTable(String tableName) throws AnalyticsException {
        analyticsDataService.deleteTable(tableName);
        try {
            indexerService.deleteIndexForTable(tableName);
        } catch (IndexerException e) {
            log.error("error while deleting the index for table: " + tableName, e);
            throw new AnalyticsException("Error while deleting the index for table: " + tableName, e);
        }
    }

    @Override
    public List<String> listTables() throws AnalyticsException {
        return analyticsDataService.listTables();
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException {
        analyticsDataService.put(records);
        Collection<List<Record>> recordBatches = AnalyticsCommonUtils.generateRecordBatches(records, true);
        AnalyticsCommonUtils.preProcessRecords(recordBatches, analyticsDataService);
        List<CarbonIndexDocument> indexDocuments = null;
        String table = null;
        try {
            for (List<Record> recordBatch : recordBatches) {
                table = recordBatch.get(0).getTableName();
                if (isIndexedTable(table)) {
                    IndexSchema indexSchema = indexerService.getIndexSchema(table);
                    indexDocuments = DataAPIUtils.getIndexDocuments(recordBatch, indexSchema);
                    indexerService.indexDocuments(table, indexDocuments);
                }
            }
        } catch (IndexSchemaNotFoundException e) {
            log.error("Error while inserting records: " + e.getMessage(), e);
            throw new AnalyticsException("Error while inserting records: " + e.getMessage(), e);
        } catch (IndexerException e) {
            log.error("Error while inserting records: " + e.getMessage(), e);
            log.info("Inserting Index data of records to staging area as indexing failed..");
            AddIndexDocumentsToStagingArea(indexDocuments, table);
            throw new AnalyticsException("Error while inserting records: " + e.getMessage(), e);
        }
    }

    private void AddIndexDocumentsToStagingArea(List<CarbonIndexDocument> indexDocuments,
                                                String table) throws AnalyticsException {
        if (!analyticsDataService.tableExists(TEMP_INDEX_DATA_STORE)) {
            analyticsDataService.createTable(TEMP_INDEX_DATA_STORE);
        }
        if (indexDocuments != null && !indexDocuments.isEmpty()) {
            List<Record> stagingIndexRecords = new ArrayList<>(1);
            Map<String, Object> values = new HashMap<>(2);
            values.put(FIELD_INDEX_DATA, AnalyticsCommonUtils.serializeObject(indexDocuments));
            values.put(FIELD_INDEX_DATA_TABLE_NAME, table);
            Record record = new Record(TEMP_INDEX_DATA_STORE, values);
            stagingIndexRecords.add(record);
            analyticsDataService.put(stagingIndexRecords);
        }
    }

    private boolean isIndexedTable(String tableName) throws IndexerException {
        try {
            IndexSchema indexSchema = indexerService.getIndexSchema(tableName);
            return indexSchema.getFields() != null && !indexSchema.getFields().isEmpty();
        } catch (IndexSchemaNotFoundException e) {
            return false;
        }
    }

    @Override
    public AnalyticsDataResponse get(String tableName, int numPartitionsHint, List<String> columns,
                                     long timeFrom, long timeTo, int recordsFrom, int recordsCount)
            throws AnalyticsException {
        return analyticsDataService.get(tableName, numPartitionsHint, columns, timeFrom, timeTo, recordsFrom, recordsCount);
    }

    @Override
    public AnalyticsDataResponse get(String tableName, int numPartitionsHint, List<String> columns,
                                     List<String> ids) throws AnalyticsException {
        return analyticsDataService.get(tableName, numPartitionsHint, columns, ids);
    }

    @Override
    public AnalyticsDataResponse getWithKeyValues(String tableName, int numPartitionsHint,
                                                  List<String> columns,
                                                  List<Map<String, Object>> valuesBatch)
            throws AnalyticsException {
        return analyticsDataService.getWithKeyValues(tableName, numPartitionsHint, columns, valuesBatch);
    }

    @Override
    public AnalyticsIterator<Record> readRecords(String recordStoreName, RecordGroup recordGroup)
            throws AnalyticsException {
        return analyticsDataService.readRecords(recordStoreName, recordGroup);
    }

    @Override
    public void delete(String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        analyticsDataService.delete(tableName, timeFrom, timeTo);
        try {
            if (isIndexedTable(tableName)) {
                indexerService.deleteDocuments(tableName, DataAPIUtils.createTimeRangeQuery(timeFrom, timeTo));
            }
        } catch (IndexerException e) {
            log.error("Error while deleting records, " + e.getMessage(), e);
            throw new AnalyticsException("Error while deleting records, " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String tableName, List<String> ids) throws AnalyticsException {
        analyticsDataService.delete(tableName, ids);
        try {
            if (isIndexedTable(tableName)) {
                indexerService.deleteDocuments(tableName, ids);
            }
        } catch (IndexerException e) {
            log.error("Error while deleting indexed records, " + e.getMessage(), e);
            throw new AnalyticsException("Error while deleting indexed records, " + e.getMessage(), e);
        }
    }

    @Override
    public void createTable(String username, String recordStoreName, String tableName)
            throws AnalyticsException {

    }

    @Override
    public void createTableWithUsername(String username, String tableName)
            throws AnalyticsException {

    }

    @Override
    public void createTableIfNotExists(String username, String recordStoreName, String tableName)
            throws AnalyticsException {

    }

    @Override
    public String getRecordStoreNameByTable(String username, String tableName)
            throws AnalyticsException {
        return null;
    }

    @Override
    public void setTableSchema(String username, String tableName, CompositeSchema schema, boolean merge)
            throws AnalyticsException {

    }

    @Override
    public AnalyticsSchema getTableSchema(String username, String tableName)
            throws AnalyticsException {
        return null;
    }

    @Override
    public boolean tableExists(String username, String tableName) throws AnalyticsException {
        return false;
    }

    @Override
    public void deleteTable(String username, String tableName) throws AnalyticsException {

    }

    @Override
    public List<String> listTables(String username) throws AnalyticsException {
        return null;
    }

    @Override
    public void put(String username, List<Record> records) throws AnalyticsException {

    }

    @Override
    public AnalyticsDataResponse get(String username, String tableName, int numPartitionsHint,
                                     List<String> columns, long timeFrom, long timeTo,
                                     int recordsFrom, int recordsCount) throws AnalyticsException {
        return null;
    }

    @Override
    public AnalyticsDataResponse get(String username, String tableName, int numPartitionsHint,
                                     List<String> columns, List<String> ids)
            throws AnalyticsException {
        return null;
    }

    @Override
    public AnalyticsDataResponse getWithKeyValues(String username, String tableName,
                                                  int numPartitionsHint, List<String> columns,
                                                  List<Map<String, Object>> valuesBatch)
            throws AnalyticsException {
        return null;
    }

    @Override
    public AnalyticsIterator<Record> readRecords(String username, String recordStoreName,
                                                 RecordGroup recordGroup)
            throws AnalyticsException {
        return null;
    }

    @Override
    public void delete(String username, String tableName, long timeFrom, long timeTo)
            throws AnalyticsException {

    }

    @Override
    public void delete(String username, String tableName, List<String> ids)
            throws AnalyticsException {

    }

    @Override
    public void destroy() throws AnalyticsException {
        stopRetryIndexerWorkers();
        if (analyticsDataService != null) {
            analyticsDataService.destroy();
        }
        try {
            if (indexerService != null) {
                indexerService.destroy();
            }
        } catch (IndexerException e) {
            log.error("Error while destroying the Analytics Data API, " + e.getMessage(), e);
            throw new AnalyticsException("Error while destroying the Analytics Data API, " + e.getMessage(), e);
        }
    }

    private void stopRetryIndexerWorkers() {
        if (stagingIndexWorkerExecutor != null && !stagingIndexWorkerExecutor.isShutdown()) {
            stagingIndexWorker.stop();
            stagingIndexSchemaWorker.stop();
            stagingIndexWorkerExecutor.shutdownNow();
            try {
                stagingIndexWorkerExecutor.awaitTermination(STAGING_INDEX_WORKER_EXECUTOR_TIMEOUT_MILLI_SEC,
                        TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.warn("Failed to stop staging Index workers, Interrupted.");
            } finally {
                stagingIndexWorkerExecutor = null;
                stagingIndexSchemaWorker = null;
                stagingIndexWorker = null;
            }
        }
    }

    @Override
    public void invalidateTable(String tableName) {
        analyticsDataService.invalidateTable(tableName);
    }

    @Override
    public CarbonIndexerClient getIndexerClient() throws AnalyticsException {
        return null;
    }

    @Override
    public CarbonIndexerClient getIndexerClient(String username) throws AnalyticsException {
        return null;
    }

    /**
     * Worker class which tries to reindex data from the staging area.
     * (If inserting data to Solr failed those data goes to the staging area)
     */
    private class StagingIndexWorker implements Runnable {

        private static final int RETRY_INTERVAL = 1000;
        private boolean stop;

        public StagingIndexWorker() {
            stop = false;
        }

        @Override
        public void run() {
            while (!stop) {
                try {
                    if (analyticsDataService.tableExists(TEMP_INDEX_DATA_STORE)) {
                        List<Record> records = getRecordsListFromTable(TEMP_INDEX_DATA_STORE, 0, RETRY_INDEX_DOC_BATCH_SIZE);
                        List<String> ids = new ArrayList<>();
                        for (Record record : records) {
                            try {
                                String table = (String) record.getValue(FIELD_INDEX_DATA_TABLE_NAME);
                                byte[] indexData = (byte[]) record.getValue(FIELD_INDEX_DATA);
                                List<CarbonIndexDocument> documents =
                                        (List<CarbonIndexDocument>) AnalyticsCommonUtils.deserializeObject(indexData);
                                indexerService.indexDocuments(table, documents);
                                ids.add(record.getId());
                            } catch (IndexerException e) {
                                log.error("Staging data Indexing failed, " + e.getMessage(), e);
                            }
                        }
                        analyticsDataService.delete(TEMP_INDEX_DATA_STORE, ids);
                        Thread.sleep(RETRY_INTERVAL);
                    }
                } catch (Throwable e) {
                    log.error("Staging data Indexing failed, " + e.getMessage(), e);
                }
            }
        }

        public void stop() {
            stop = true;
        }
    }

    private class StagingIndexSchemaWorker implements Runnable {

        private static final int RETRY_INTERVAL = 1000;
        private boolean stop;

        public StagingIndexSchemaWorker() {
            stop = false;
        }

        @Override
        public void run() {
            while (!stop) {
                try {
                    if (analyticsDataService.tableExists(FIELD_INDEX_SCHEMA)) {
                        List<Record> records = getRecordsListFromTable(TEMP_INDEX_DATA_STORE, 0, RETRY_INDEX_DOC_BATCH_SIZE);
                        List<String> ids = new ArrayList<>();
                        for (Record record : records) {
                            try {
                                //id is the schema table name
                                String table = record.getId();
                                byte[] indexSchemaData = (byte[]) record.getValue(FIELD_INDEX_SCHEMA);
                                boolean merge = (Boolean) record.getValue(FIELD_INDEX_SCHEMA_MERGE);
                                IndexSchema indexSchema =
                                        (IndexSchema) AnalyticsCommonUtils.deserializeObject(indexSchemaData);
                                indexerService.updateIndexSchema(table, indexSchema, merge);
                                ids.add(record.getId());

                            } catch (IndexerException e) {
                                log.error("Inserting staged index schema failed, " + e.getMessage(), e);
                            }
                        }
                        analyticsDataService.delete(TEMP_INDEX_SCHEMA_STORE, ids);
                        Thread.sleep(RETRY_INTERVAL);
                    }
                } catch (Throwable e) {
                    log.error("Inserting staged index schema failed, " + e.getMessage(), e);
                }
            }
        }

        public void stop() {
            stop = true;
        }
    }

    private List<Record> getRecordsListFromTable(String table, int start, int count) throws AnalyticsException {
        AnalyticsDataResponse response = analyticsDataService.get(table, 0, null,
                                                                  Long.MIN_VALUE, Long.MAX_VALUE, start, count);
        return AnalyticsCommonUtils.listRecords(analyticsDataService, response);
    }
}
