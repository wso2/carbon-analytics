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

/**
 * Implementation class of AnalyticsDataAPI - Implements the DAL level APIs with indexing.
 */
public class AnalyticsDataAPIImpl implements AnalyticsDataAPI {

    private static final String TEMP_INDEX_DATA_STORE_ = "_TEMP_INDEX_DATA_STORE_";
    private static final String TEMP_INDEX_DATA_ = "_TEMP_INDEX_DATA_";
    private static Log log = LogFactory.getLog(AnalyticsDataAPIImpl.class);
    private CarbonIndexerService indexerService;
    private AnalyticsDataService analyticsDataService;
    private static final String TEMP_INDEX_SCHEMA_STORE = "_TEMP_INDEX_SCHEMA_STORE_";
    private static final String TEMP_INDEX_SCHEMA_FIELD = "_TEMP_INDEX_SCHEMA_";

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
            AddIndexSchemaToStagingArea(tableName, indexSchema);
            throw new AnalyticsException("Error while updating the index schema for table : " + tableName, e);
        }
    }

    /**
     * Keep the indexSchema in a backup area if the solr server is not available or the indexer service is not available
     * @param tableName table name of the index schema
     * @param indexSchema The index Schema
     * @throws AnalyticsException If the DAL.put() didn't work
     */
    private void AddIndexSchemaToStagingArea(String tableName, IndexSchema indexSchema)
            throws AnalyticsException {
        if (!analyticsDataService.tableExists(TEMP_INDEX_SCHEMA_STORE)) {
            analyticsDataService.createTable(TEMP_INDEX_SCHEMA_STORE);
        }
        Map<String, Object> values = new HashMap<>(1);
        values.put(TEMP_INDEX_SCHEMA_FIELD, AnalyticsCommonUtils.serializeObject(indexSchema));
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
                if (isIndexedTable(table)) {
                    table = recordBatch.get(0).getTableName();
                    IndexSchema indexSchema = indexerService.getIndexSchema(table);
                    indexDocuments = DataAPIUtils.getIndexDocuments(recordBatch, indexSchema);
                    indexerService.put(table, indexDocuments);
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
        if (!analyticsDataService.tableExists(TEMP_INDEX_DATA_STORE_)) {
            analyticsDataService.createTable(TEMP_INDEX_DATA_STORE_);
        }
        if (indexDocuments != null && !indexDocuments.isEmpty()) {
            List<Record> stagingIndexRecords = new ArrayList<>(1);
            Map<String, Object> values = new HashMap<>(1);
            values.put(TEMP_INDEX_DATA_, AnalyticsCommonUtils.serializeObject(indexDocuments));
            Record record = new Record(table, values);
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
    }

    @Override
    public void delete(String tableName, List<String> ids) throws AnalyticsException {
        analyticsDataService.delete(tableName, ids);
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
    public void setTableSchema(String username, String tableName, AnalyticsSchema schema)
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

    }

    @Override
    public void invalidateTable(String tableName) {

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
     * (If inserting data to solr failed those data goes to the staging area)
     */
    private class RetryIndexWorker implements Runnable {

        private boolean stop;

        public RetryIndexWorker() {
            stop = false;
        }

        @Override
        public void run() {
            while (!stop) {
                try {
                    if (analyticsDataService.tableExists(TEMP_INDEX_DATA_STORE_)) {

                    }
                } catch (Throwable e) {

                }
            }

        }
    }
}
