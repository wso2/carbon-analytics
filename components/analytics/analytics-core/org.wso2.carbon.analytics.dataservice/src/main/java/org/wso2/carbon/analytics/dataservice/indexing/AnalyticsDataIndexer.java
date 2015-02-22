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
package org.wso2.carbon.analytics.dataservice.indexing;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.apache.lucene.util.Version;
import org.wso2.carbon.analytics.dataservice.AnalyticsDSUtils;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.AnalyticsDirectory;
import org.wso2.carbon.analytics.dataservice.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.AnalyticsQueryParser;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.clustering.GroupEventListener;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataCorruptionException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsTimeoutException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * This class represents the indexing functionality.
 */
public class AnalyticsDataIndexer implements GroupEventListener {
    
    private static final int INDEXING_SCHEDULE_PLAN_RETRY_COUNT = 3;

    private static final String DISABLE_INDEXING_ENV_PROP = "disableIndexing";

    private static final int WAIT_INDEX_TIME_INTERVAL = 1000;

    private static final String INDEX_BATCH_OP_ATTRIBUTE = "INDEX_BATCH_OP";

    private static final String SHARD_INDEX_DATA_RECORD_TABLE_NAME = "__SHARD_INDEX_RECORDS__";

    private static final int SHARD_INDEX_DATA_RECORD_TENANT_ID = -1000;
    
    private static final String ANALYTICS_INDEXING_GROUP = "__ANALYTICS_INDEXING_GROUP__";

    private static final Log log = LogFactory.getLog(AnalyticsDataIndexer.class);
        
    private static final String INDEX_DATA_FS_BASE_PATH = "/_data/index/";

    public static final String INDEX_ID_INTERNAL_FIELD = "_id";

    public static final String INDEX_INTERNAL_TIMESTAMP_FIELD = "_timestamp";
    
    private static final String NULL_INDEX_VALUE = "";

    private AnalyticsIndexDefinitionRepository repository;
    
    private Map<String, Map<String, IndexType>> indexDefs = new HashMap<String, Map<String, IndexType>>();
    
    private Map<String, Directory> indexDirs = new HashMap<String, Directory>();
    
    private Analyzer luceneAnalyzer;
    
    private AnalyticsFileSystem analyticsFileSystem;
    
    private AnalyticsRecordStore analyticsRecordStore;
    
    private int shardCount;
    
    private ExecutorService executor;
            
    public AnalyticsDataIndexer(AnalyticsRecordStore analyticsRecordStore, 
            AnalyticsFileSystem analyticsFileSystem, int shardCount) throws AnalyticsException {
    	this(analyticsRecordStore, analyticsFileSystem, shardCount, new StandardAnalyzer());
    }
    
    public AnalyticsDataIndexer(AnalyticsRecordStore analyticsRecordStore, 
            AnalyticsFileSystem analyticsFileSystem, int shardCount, 
            Analyzer analyzer) throws AnalyticsException {
    	this.luceneAnalyzer = analyzer;
        this.analyticsRecordStore = analyticsRecordStore;    	
    	this.analyticsFileSystem = analyticsFileSystem;
    	this.shardCount = shardCount;
        this.repository = new AnalyticsIndexDefinitionRepository(this.getFileSystem());
    }
    
    /**
     * This method initializes the indexer, and must be called before any other operation in this class is called.
     * @throws AnalyticsException
     */
    public void init() throws AnalyticsException {
        this.initializeIndexingSchedules();
    }
    
    private boolean checkIfIndexingNode() {
        return System.getProperty(DISABLE_INDEXING_ENV_PROP) == null;
    }
    
    private void initializeIndexingSchedules() throws AnalyticsException {
        if (!this.checkIfIndexingNode()) {
            return;
        }
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            acm.joinGroup(ANALYTICS_INDEXING_GROUP, this);
        } else {
            List<List<Integer>> indexingSchedule = this.generateIndexWorkerSchedulePlan(1);
            this.scheduleWorkers(indexingSchedule.get(0));
        }
    }
    
    private List<List<Integer>> generateIndexWorkerSchedulePlan(int numWorkers) {
        List<List<Integer>> result = new ArrayList<List<Integer>>(numWorkers);
        for (int i = 0; i < numWorkers; i++) {
            result.add(new ArrayList<Integer>());
        }
        for (int i = 0; i < this.getShardCount(); i++) {
            result.get(i % numWorkers).add(i);
        }
        return result;
    }
    
    public void scheduleWorkers(List<Integer> shardIndices) throws AnalyticsException {
        this.stopAndCleanupIndexProcessing();
        this.executor = Executors.newFixedThreadPool(shardIndices.size());
        for (int shardIndex : shardIndices) {
            this.executor.execute(new IndexWorker(shardIndex));
        }
        log.info("Scheduled Analytics Indexing Shards " + shardIndices);
    }
    
    public AnalyticsFileSystem getFileSystem() {
        return analyticsFileSystem;
    }
    
    public AnalyticsRecordStore getAnalyticsRecordStore() {
        return analyticsRecordStore;
    }
    
    public int getShardCount() {
        return shardCount;
    }
    
    public AnalyticsIndexDefinitionRepository getRepository() {
        return repository;
    }
    
    private Map<Integer, IndexBatchOperation> createShardedUpdateIndexBatchOps(
            Map<Integer, List<Record>> shardedRecordBatches) {
        Map<Integer, IndexBatchOperation> shardedIndexBatchOperations = new HashMap<Integer, IndexBatchOperation>();
        IndexBatchOperation indexBatchOperation;
        for (Entry<Integer, List<Record>> entry : shardedRecordBatches.entrySet()) {
            indexBatchOperation = new IndexBatchOperation();
            indexBatchOperation.setOperationType(IndexOperationType.UPDATE);
            indexBatchOperation.setRecords(entry.getValue());
            shardedIndexBatchOperations.put(entry.getKey(), indexBatchOperation);
        }
        return shardedIndexBatchOperations;
    }
    
    private Map<Integer, IndexBatchOperation> createShardedDeleteIndexBatchOps(
            int tenantId, String tableName, Map<Integer, List<String>> shardedRecordIdBatches, long timestamp) {
        Map<Integer, IndexBatchOperation> shardedIndexBatchOperations = new HashMap<Integer, IndexBatchOperation>();
        IndexBatchOperation indexBatchOperation;
        for (Entry<Integer, List<String>> entry : shardedRecordIdBatches.entrySet()) {
            indexBatchOperation = new IndexBatchOperation();
            indexBatchOperation.setOperationType(IndexOperationType.DELETE);
            indexBatchOperation.setDeleteTenantId(tenantId);
            indexBatchOperation.setDeleteTableName(tableName);
            indexBatchOperation.setDeleteTimestamp(timestamp);
            indexBatchOperation.setRecordIds(entry.getValue());
            shardedIndexBatchOperations.put(entry.getKey(), indexBatchOperation);
        }
        return shardedIndexBatchOperations;
    }
    
    public void scheduleIndexUpdate(List<Record> records) throws AnalyticsException {
        Map<Integer, List<Record>> shardedRecordBatches = this.groupRecordsByShardIndex(records);
        Map<Integer, IndexBatchOperation> shardedIndexBatchOperations = this.createShardedUpdateIndexBatchOps(
                shardedRecordBatches);
        List<Record> indexOperationRecords = this.createShardedIndexBatchOperationRecords(
                shardedIndexBatchOperations);
        this.addToScheduledIndexOpStore(indexOperationRecords);
    }
    
    public void scheduleIndexDelete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        Map<Integer, List<String>> shardedRecordIdBatches = this.groupRecordIdsByShardIndex(ids);
        Map<Integer, IndexBatchOperation> shardedIndexBatchOperations = this.createShardedDeleteIndexBatchOps(
                tenantId, tableName, shardedRecordIdBatches, System.currentTimeMillis());
        List<Record> indexOperationRecords = this.createShardedIndexBatchOperationRecords(
                shardedIndexBatchOperations);
        this.addToScheduledIndexOpStore(indexOperationRecords);
    }
    
    private void addToScheduledIndexOpStore(List<Record> indexOperationRecords) throws AnalyticsException {
        try {
            this.getAnalyticsRecordStore().insert(indexOperationRecords);
        } catch (AnalyticsTableNotAvailableException e) {
            this.getAnalyticsRecordStore().createTable(SHARD_INDEX_DATA_RECORD_TENANT_ID, SHARD_INDEX_DATA_RECORD_TABLE_NAME);
            this.getAnalyticsRecordStore().insert(indexOperationRecords);
        }
    }
    
    private void processIndexBatchOperations(int shardIndex) throws AnalyticsException {
        List<Record> indexBatchOpRecords = this.loadIndexBatchOperationRecords(shardIndex);
        List<IndexOperation> indexOperations = new ArrayList<IndexOperation>();
        IndexBatchOperation indexBatchOp;
        List<String> indexBatchOpRecordIds = new ArrayList<String>(indexBatchOpRecords.size());
        for (Record indexBatchOpRecord : indexBatchOpRecords) {
            try {
                indexBatchOp = this.createIndexBatchOpFromRecord(indexBatchOpRecord);
                indexOperations.addAll(indexBatchOp.extractIndexOperations());
                indexBatchOpRecordIds.add(indexBatchOpRecord.getId());
            } catch (AnalyticsDataCorruptionException e) {
                /* corrupted data must be deleted, or else, we will continue to
                 * process these indefinitely */
                this.deleteIndexBatchOperationRecord(indexBatchOpRecord.getId());
                log.error("Corrupted index batch operation record deleted, id: " + indexBatchOpRecord.getId() + 
                        " shard index: " + indexBatchOpRecord.getTimestamp());
            }
        }
        Collections.sort(indexOperations);
        this.processIndexOperations(shardIndex, indexOperations);            
        /* delete the processed records */
        this.deleteIndexBatchOperationRecords(indexBatchOpRecordIds);
    }
    
    private void processDeleteIndexOperations(int shardIndex, 
            List<IndexOperation> deleteOps) throws AnalyticsException {
        Map<String, IndexType> indices;
        Map<String, List<IndexOperation>> batches = this.generateDeleteIndexOpBatches(deleteOps);
        IndexOperation firstOp;
        for (List<IndexOperation> opBatch : batches.values()) {
            firstOp = opBatch.get(0);
            indices = this.lookupIndices(firstOp.getDeleteTenantId(), firstOp.getDeleteTableName());
            if (indices.size() > 0) {
                this.delete(shardIndex, opBatch);
            }
        }
    }
    
    private Map<String, List<IndexOperation>> generateDeleteIndexOpBatches(
            List<IndexOperation> indexOps) throws AnalyticsException {
        Map<String, List<IndexOperation>> opBatches = new HashMap<String, List<IndexOperation>>();
        List<IndexOperation> opBatch;
        String identity;
        for (IndexOperation indexOp : indexOps) {
            identity = this.generateGlobalTableId(indexOp.getDeleteTenantId(), indexOp.getDeleteTableName());
            opBatch = opBatches.get(identity);
            if (opBatch == null) {
                opBatch = new ArrayList<IndexOperation>();
                opBatches.put(identity, opBatch);
            }
            opBatch.add(indexOp);
        }
        return opBatches;
    }
    
    private void processUpdateIndexOperations(int shardIndex, 
            List<IndexOperation> updateOps) throws AnalyticsException {
        Map<String, IndexType> indices;
        Map<String, List<Record>> batches = this.generateRecordBatchesFromUpdateIndexOps(updateOps);
        Record firstRecord;
        for (List<Record> recordBatch : batches.values()) {
            firstRecord = recordBatch.get(0);
            indices = this.lookupIndices(firstRecord.getTenantId(), firstRecord.getTableName());
            if (indices.size() > 0) {
                this.updateIndex(shardIndex, recordBatch, indices);
            }
        }
    }
    
    private void processIndexOperations(int shardIndex, 
            List<IndexOperation> indexOperations) throws AnalyticsException {
        List<IndexOperation> activeOps = new ArrayList<IndexOperation>();
        boolean updating = false, deleting = false;
        for (IndexOperation indexOp : indexOperations) {
            if (IndexOperationType.UPDATE == indexOp.getOperationType()) {
                if (deleting) {
                    this.processDeleteIndexOperations(shardIndex, activeOps);
                    activeOps.clear();
                    deleting = false;
                }
                activeOps.add(indexOp);
                updating = true;
            } else if (IndexOperationType.DELETE == indexOp.getOperationType()) {
                if (updating) {
                    this.processUpdateIndexOperations(shardIndex, activeOps);
                    activeOps.clear();
                    updating = false;
                }
                activeOps.add(indexOp);
                deleting = true;
            }
        }
        if (updating) {
            this.processUpdateIndexOperations(shardIndex, activeOps);
        } else if (deleting) {
            this.processDeleteIndexOperations(shardIndex, activeOps);
        }
    }
    
    private void deleteIndexBatchOperationRecords(List<String> ids) throws AnalyticsException {
        this.getAnalyticsRecordStore().delete(SHARD_INDEX_DATA_RECORD_TENANT_ID, SHARD_INDEX_DATA_RECORD_TABLE_NAME, ids);
    }
    
    private void deleteIndexBatchOperationRecord(String id) throws AnalyticsException {
        List<String> ids = new ArrayList<String>(1);
        ids.add(id);
        this.deleteIndexBatchOperationRecords(ids);
    }
    
    private List<Record> createShardedIndexBatchOperationRecords(
            Map<Integer, IndexBatchOperation> shardedIndexBatchOperations) throws AnalyticsException {
        List<Record> indexOperationRecords = new ArrayList<Record>(shardedIndexBatchOperations.size());
        for (Entry<Integer, IndexBatchOperation> indexOp : shardedIndexBatchOperations.entrySet()) {
            indexOperationRecords.add(this.createShardedIndexBatchOperationRecord(
                    indexOp.getKey(), indexOp.getValue()));
        }
        return indexOperationRecords;
    }
    
    private IndexBatchOperation createIndexBatchOpFromRecord(Record indexBatchOpRecord) 
            throws AnalyticsDataCorruptionException {
        try {
            return AnalyticsDSUtils.binaryToIndexBatchOp((byte[]) indexBatchOpRecord.getValue(INDEX_BATCH_OP_ATTRIBUTE));
        } catch (AnalyticsDataCorruptionException e) {
            throw e;
        } catch (Exception e) {
            throw new AnalyticsDataCorruptionException("Invalid index batch operation data in record: " + e.getMessage(), e);
        }
    }
    
    private List<Record> loadIndexBatchOperationRecords(int shardIndex) throws AnalyticsException {
        return this.loadIndexBatchOperationRecords(shardIndex, shardIndex + 1);
    }
    
    private List<Record> loadIndexBatchOperationRecords(int start, int end) throws AnalyticsException {
        try {
            return GenericUtils.listRecords(this.getAnalyticsRecordStore(), 
                    this.getAnalyticsRecordStore().get(SHARD_INDEX_DATA_RECORD_TENANT_ID, SHARD_INDEX_DATA_RECORD_TABLE_NAME, 
                            null, start, end, 0, -1));
        } catch (AnalyticsTableNotAvailableException e) {
            /* ignore this scenario, before any indexes, this will happen */
            return new ArrayList<Record>();
        }
    }
    
    private Record createShardedIndexBatchOperationRecord(int shardIndex, 
            IndexBatchOperation indexBatchOp) throws AnalyticsException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(INDEX_BATCH_OP_ATTRIBUTE, AnalyticsDSUtils.indexBatchOpToBinary(indexBatchOp));
        return new Record(SHARD_INDEX_DATA_RECORD_TENANT_ID, SHARD_INDEX_DATA_RECORD_TABLE_NAME, values, shardIndex);
    }
    
    private Map<Integer, List<Record>> groupRecordsByShardIndex(List<Record> records) {
        Map<Integer, List<Record>> result = new HashMap<Integer, List<Record>>();
        int shardIndex;
        List<Record> group;
        for (Record record : records) {
            shardIndex = this.calculateShardId(record);
            group = result.get(shardIndex);
            if (group == null) {
                group = new ArrayList<Record>();
                result.put(shardIndex, group);
            }
            group.add(record);
        }
        return result;
    }
    
    private Map<Integer, List<String>> groupRecordIdsByShardIndex(List<String> ids) {
        Map<Integer, List<String>> result = new HashMap<Integer, List<String>>();
        int shardIndex;
        List<String> group;
        for (String id : ids) {
            shardIndex = this.calculateShardId(id);
            group = result.get(shardIndex);
            if (group == null) {
                group = new ArrayList<String>();
                result.put(shardIndex, group);
            }
            group.add(id);
        }
        return result;
    }
    
    private int calculateShardId(Record record) {
        return this.calculateShardId(record.getId());
    }
    
    private int calculateShardId(String id) {
        return Math.abs(id.hashCode()) % this.getShardCount();
    }
    
    private List<String> lookupGloballyExistingShardIds(int tenantId, String tableName) throws AnalyticsIndexException {
        String globalPath = this.generateDirPath(this.generateGlobalTableId(tenantId, tableName));
        try {
            List<String> names = this.getFileSystem().list(globalPath);
            List<String> result = new ArrayList<String>();
            for (String name : names) {
                result.add(name);
            }
            return result;
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in looking up index shard directories for tenant: " + 
                    tenantId + " table: " + tableName);
        }
    }
    
    public List<SearchResultEntry> search(int tenantId, String tableName, String language, String query, 
            int start, int count) throws AnalyticsIndexException {
        List<String> shardIds = this.lookupGloballyExistingShardIds(tenantId, tableName);
        List<SearchResultEntry> result = new ArrayList<SearchResultEntry>();
        for (String shardId : shardIds) {
            result.addAll(this.search(tenantId, tableName, language, query, 0, count + start, shardId));
        }
        Collections.sort(result);
        if (result.size() < start) {
            return new ArrayList<SearchResultEntry>();
        }
        if (result.size() >= count + start) {
            result = result.subList(start, start + count);
        } else {
            result = result.subList(start, result.size());
        }
        return result;
    }
    
    public int searchCount(int tenantId, String tableName, String language, 
            String query) throws AnalyticsIndexException {
        List<String> shardIds = this.lookupGloballyExistingShardIds(tenantId, tableName);
        int result = 0;
        for (String shardId : shardIds) {
            result += this.searchCount(tenantId, tableName, language, query, shardId);
        }
        return result;
    }
    
    private List<SearchResultEntry> search(int tenantId, String tableName, String language, String query, 
            int start, int count, String shardId) throws AnalyticsIndexException {
        List<SearchResultEntry> result = new ArrayList<SearchResultEntry>();
        String shardedTableId = this.generateShardedTableId(tenantId, tableName, shardId);
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(this.lookupIndexDir(shardedTableId));
            IndexSearcher searcher = new IndexSearcher(reader);
            Map<String, IndexType> indices = this.lookupIndices(tenantId, tableName);
            Query indexQuery = new AnalyticsQueryParser(this.luceneAnalyzer, indices).parse(query);
            TopScoreDocCollector collector = TopScoreDocCollector.create(count, true);
            searcher.search(indexQuery, collector);
            ScoreDoc[] hits = collector.topDocs(start).scoreDocs;
            Document indexDoc;
            for (ScoreDoc doc : hits) {
                indexDoc = searcher.doc(doc.doc);
                result.add(new SearchResultEntry(indexDoc.get(INDEX_ID_INTERNAL_FIELD), doc.score));
            }            
            return result;
        } catch (Exception e) {
            throw new AnalyticsIndexException("Error in index search, shard table id: '" + 
                    shardedTableId + "': " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error in closing the reader: " + e.getMessage(), e);;
                }
            }
        }
    }
    
    private int searchCount(int tenantId, String tableName, String language, String query,
            String shardId) throws AnalyticsIndexException {
        String shardedTableId = this.generateShardedTableId(tenantId, tableName, shardId);
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(this.lookupIndexDir(shardedTableId));
            IndexSearcher searcher = new IndexSearcher(reader);
            Map<String, IndexType> indices = this.lookupIndices(tenantId, tableName);
            Query indexQuery = new AnalyticsQueryParser(this.luceneAnalyzer, indices).parse(query);
            TotalHitCountCollector collector = new TotalHitCountCollector();
            searcher.search(indexQuery, collector);
            return collector.getTotalHits();
        } catch (Exception e) {
            throw new AnalyticsIndexException("Error in index search count, shard table id: '" + 
                    shardedTableId + "': " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error in closing the reader: " + e.getMessage(), e);;
                }
            }
        }
    }
    
    private Map<String, List<Record>> generateRecordBatchesFromUpdateIndexOps(
            List<IndexOperation> indexOps) throws AnalyticsException {
        Map<String, List<Record>> recordBatches = new HashMap<String, List<Record>>();
        List<Record> recordBatch;
        String identity;
        Record record;
        for (IndexOperation indexOp : indexOps) {
            record = indexOp.getUpdateRecord();
            identity = this.generateGlobalTableId(record.getTenantId(), record.getTableName());
            recordBatch = recordBatches.get(identity);
            if (recordBatch == null) {
                recordBatch = new ArrayList<Record>();
                recordBatches.put(identity, recordBatch);
            }
            recordBatch.add(record);
        }
        return recordBatches;
    }
    
    /**
     * Adds the given records to the index if they are previously scheduled to be indexed.
     * @param records The records to be indexed
     * @throws AnalyticsException
     */
    public void insert(List<Record> records) throws AnalyticsException {
        this.scheduleIndexUpdate(records);
    }
    
    /**
    * Deletes the given records in the index.
    * @param tenantId The tenant id
    * @param tableName The table name
    * @param The ids of the records to be deleted
    * @throws AnalyticsException
    */
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
       this.scheduleIndexDelete(tenantId, tableName, ids);
    }
    
    private void delete(int shardIndex, List<IndexOperation> deleteOpBatch) throws AnalyticsException {
        IndexOperation firstOp = deleteOpBatch.get(0);
        int tenantId = firstOp.getDeleteTenantId();
        String tableName = firstOp.getDeleteTableName();
        String tableId = this.generateShardedTableId(tenantId, tableName, Integer.toString(shardIndex));
        IndexWriter indexWriter = this.createIndexWriter(tableId);
        List<Term> terms = new ArrayList<Term>(deleteOpBatch.size());
        for (IndexOperation op : deleteOpBatch) {
            terms.add(new Term(INDEX_ID_INTERNAL_FIELD, op.getDeleteId()));
        }
        try {
            indexWriter.deleteDocuments(terms.toArray(new Term[terms.size()]));
            indexWriter.commit();
        } catch (IOException e) {
            throw new AnalyticsException("Error in deleting indices: " + e.getMessage(), e);
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                log.error("Error closing index writer: " + e.getMessage(), e);
            }
        }
    }
    
    private void updateIndex(int shardIndex, List<Record> recordBatch, 
            Map<String, IndexType> columns) throws AnalyticsIndexException {
        Record firstRecord = recordBatch.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        String shardedTableId = this.generateShardedTableId(tenantId, tableName, Integer.toString(shardIndex));
        IndexWriter indexWriter = this.createIndexWriter(shardedTableId);
        try {
            for (Record record : recordBatch) {
                indexWriter.updateDocument(new Term(INDEX_ID_INTERNAL_FIELD, record.getId()), 
                        this.generateIndexDoc(record, columns).getFields());
            }
            indexWriter.commit();
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in updating index: " + e.getMessage(), e);
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                log.error("Error closing index writer: " + e.getMessage(), e);
            }
        }
    }
    
    private void checkAndAddDocEntry(Document doc, IndexType type, String name, Object obj) {
        if (obj == null) {
            doc.add(new StringField(name, NULL_INDEX_VALUE, Store.NO));
            return;
        }
        switch (type) {
        case STRING:
            doc.add(new TextField(name, obj.toString(), Store.NO));
            break;
        case INTEGER:
            if (obj instanceof Integer) {
                doc.add(new IntField(name, (Integer) obj, Store.NO));
            } else if (obj instanceof Long) {
                doc.add(new IntField(name, ((Long) obj).intValue(), Store.NO));
            } else if (obj instanceof Double) {
                doc.add(new IntField(name, ((Double) obj).intValue(), Store.NO));
            } else if (obj instanceof Float) {
                doc.add(new IntField(name, ((Float) obj).intValue(), Store.NO));
            } else {
                doc.add(new StringField(name, obj.toString(), Store.NO));
            }
            break;
        case DOUBLE:
            if (obj instanceof Double) {
                doc.add(new DoubleField(name, (Double) obj, Store.NO));
            } else if (obj instanceof Integer) {
                doc.add(new DoubleField(name, ((Integer) obj).doubleValue(), Store.NO));
            } else if (obj instanceof Long) {
                doc.add(new DoubleField(name, ((Long) obj).doubleValue(), Store.NO));
            } else if (obj instanceof Float) {
                doc.add(new DoubleField(name, ((Float) obj).doubleValue(), Store.NO));
            } else {
                doc.add(new StringField(name, obj.toString(), Store.NO));
            }
            break;
        case LONG:
            if (obj instanceof Long) {
                doc.add(new LongField(name, ((Long) obj).longValue(), Store.NO));
            } else if (obj instanceof Integer) {
                doc.add(new LongField(name, ((Integer) obj).longValue(), Store.NO));
            } else if (obj instanceof Double) {
                doc.add(new LongField(name, ((Double) obj).longValue(), Store.NO));
            } else if (obj instanceof Float) {
                doc.add(new LongField(name, ((Float) obj).longValue(), Store.NO));
            } else {
                doc.add(new StringField(name, obj.toString(), Store.NO));
            }
            break;
        case FLOAT:
            if (obj instanceof Float) {
                doc.add(new FloatField(name, ((Float) obj).floatValue(), Store.NO));
            } else if (obj instanceof Integer) {
                doc.add(new FloatField(name, ((Integer) obj).floatValue(), Store.NO));
            } else if (obj instanceof Long) {
                doc.add(new FloatField(name, ((Long) obj).floatValue(), Store.NO));
            } else if (obj instanceof Double) {
                doc.add(new FloatField(name, ((Double) obj).floatValue(), Store.NO));
            } else {
                doc.add(new StringField(name, obj.toString(), Store.NO));
            }
            break;
        case BOOLEAN:
            doc.add(new StringField(name, obj.toString(), Store.NO));
            break;
        }
    }
    
    private Document generateIndexDoc(Record record, Map<String, IndexType> columns) 
            throws IOException, AnalyticsIndexException {
        Document doc = new Document();
        doc.add(new StringField(INDEX_ID_INTERNAL_FIELD, record.getId(), Store.YES));
        doc.add(new LongField(INDEX_INTERNAL_TIMESTAMP_FIELD, record.getTimestamp(), Store.NO));
        /* make the best effort to store in the given timestamp, or else, 
         * fall back to a compatible format, or else, lastly, string */
        String name;
        for (Map.Entry<String, IndexType> entry : columns.entrySet()) {
            name = entry.getKey();
            this.checkAndAddDocEntry(doc, entry.getValue(), name, record.getValue(name));
        }
        return doc;
    }
    
    private void checkInvalidIndexNames(Set<String> columns) throws AnalyticsIndexException {
        for (String column : columns) {
            if (column.contains(" ")) {
                throw new AnalyticsIndexException("Index columns cannot have a space in the name: '" + column + "'");
            }
        }
        if (columns.contains(INDEX_ID_INTERNAL_FIELD)) {
            throw new AnalyticsIndexException("The column index '" + INDEX_ID_INTERNAL_FIELD + 
                    "' is a reserved name");
        }
        if (columns.contains(INDEX_INTERNAL_TIMESTAMP_FIELD)) {
            throw new AnalyticsIndexException("The column index '" + INDEX_INTERNAL_TIMESTAMP_FIELD + 
                    "' is a reserved name");
        }
    }
    
    public void setIndices(int tenantId, String tableName, Map<String, IndexType> columns) 
            throws AnalyticsIndexException {
        this.checkInvalidIndexNames(columns.keySet());
        String tableId = this.generateGlobalTableId(tenantId, tableName);
        this.indexDefs.put(tableId, columns);
        this.getRepository().setIndices(tenantId, tableName, columns);
        this.notifyClusterIndexChange(tenantId, tableName);
    }
    
    public Map<String, IndexType> lookupIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        String tableId = this.generateGlobalTableId(tenantId, tableName);
        Map<String, IndexType> cols = this.indexDefs.get(tableId);
        if (cols == null) {
            cols = this.getRepository().getIndices(tenantId, tableName);
            this.indexDefs.put(tableId, cols);
        }
        return cols; 
    }
    
    private String generateDirPath(String tableId) {
        return INDEX_DATA_FS_BASE_PATH + tableId;
    }
    
    private Directory createDirectory(String tableId) throws AnalyticsIndexException {
        String path = this.generateDirPath(tableId);
        try {
            return new AnalyticsDirectory(this.getFileSystem(), new SingleInstanceLockFactory(), path);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException("Error in creating directory: " + e.getMessage(), e);
        }
    }
    
    private Directory lookupIndexDir(String tableId) throws AnalyticsIndexException {
        Directory indexDir = this.indexDirs.get(tableId);
        if (indexDir == null) {
            synchronized (this.indexDirs) {
                indexDir = this.indexDirs.get(tableId);
                if (indexDir == null) {
                    indexDir = this.createDirectory(tableId);
                    this.indexDirs.put(tableId, indexDir);
                }
            }
        }
        return indexDir;
    }
    
    private IndexWriter createIndexWriter(String tableId) throws AnalyticsIndexException {
        Directory indexDir = this.lookupIndexDir(tableId);
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_4_10_3, this.luceneAnalyzer);
        try {
            return new IndexWriter(indexDir, conf);
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in creating index writer: " + e.getMessage(), e);
        }
    }
    
    private void deleteIndexData(int tenantId, String tableName) throws AnalyticsIndexException {
        List<String> shardIds = this.lookupGloballyExistingShardIds(tenantId, tableName);
        for (String shardId : shardIds) {
            this.deleteIndexData(tenantId, tableName, shardId);
        }
    }
    
    private void deleteIndexData(int tenantId, String tableName, String shardId) throws AnalyticsIndexException {
        String shardedTableId = this.generateShardedTableId(tenantId, tableName, shardId);
        IndexWriter writer = this.createIndexWriter(shardedTableId);
        try {
            writer.deleteAll();
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in deleting index data: " + e.getMessage(), e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                log.error("Error in closing index writer: " + e.getMessage(), e);
            }
        }
    }
    
    public void clearIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        String tableId = this.generateGlobalTableId(tenantId, tableName);
        this.indexDefs.remove(tableId);
        this.getRepository().clearAllIndices(tenantId, tableName);
        this.closeAndRemoveIndexDirs(tenantId, tableName);
        /* delete all global index data, not only local ones */
        this.deleteIndexData(tenantId, tableName);
        this.notifyClusterIndexChange(tenantId, tableName);
    }
    
    private String generateShardedTableId(int tenantId, String tableName, String shardId) {
        /* the table names are not case-sensitive */
        return this.generateGlobalTableId(tenantId, tableName) + "/" + shardId;
    }
    
    private boolean isShardedTableId(int tenantId, String tableName, String shardedTableId) {
        return shardedTableId.startsWith(this.generateGlobalTableId(tenantId, tableName) + "/");
    }
    
    private String generateGlobalTableId(int tenantId, String tableName) {
        /* the table names are not case-sensitive */
        return tenantId + "_" + tableName.toLowerCase();
    }
    
    public void clusterNoficationReceived(int tenantId, String tableName) throws AnalyticsIndexException {
        /* remove the entry from the cache, this will force the next index operations to load
         * the index definition from the back-end store, this makes sure, we have optimum cache cleanup
         * and improves memory usage for tenant partitioning */
        String tableId = this.generateGlobalTableId(tenantId, tableName);
        this.indexDefs.remove(tableId);
        this.closeAndRemoveIndexDirs(tenantId, tableName);
    }
    
    private void notifyClusterIndexChange(int tenantId, String tableName) throws AnalyticsIndexException {
        
    }
    
    private void closeAndRemoveIndexDirs(int tenantId, String tableName) throws AnalyticsIndexException {
        Set<String> ids = new HashSet<String>();
        for (String id : this.indexDirs.keySet()) {
            if (this.isShardedTableId(tenantId, tableName, id)) {
                ids.add(id);
            }
        }
        this.closeAndRemoveIndexDirs(ids);
    }
    
    private void closeAndRemoveIndexDir(String tableId) throws AnalyticsIndexException {
        Directory indexDir = this.indexDirs.remove(tableId);
        try {
            if (indexDir != null) {
                indexDir.close();
            }
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in closing index directory: " + e.getMessage(), e);
        }
    }
    
    private void closeAndRemoveIndexDirs(Set<String> tableIds) throws AnalyticsIndexException {
        for (String tableId : tableIds) {
            this.closeAndRemoveIndexDir(tableId);
        }
    }
    
    public void stopAndCleanupIndexProcessing() {
        if (this.executor != null) {
            this.executor.shutdownNow();
            this.executor = null;
        }
    }
    
    public void close() throws AnalyticsIndexException {
        this.indexDefs.clear();
        this.closeAndRemoveIndexDirs(new HashSet<String>(this.indexDirs.keySet()));
        this.stopAndCleanupIndexProcessing();
    }
    
    private List<String> extractRecordIds(List<Record> records) {
        List<String> ids = new ArrayList<String>(records.size());
        for (Record record : records) {
            ids.add(record.getId());
        }
        return ids;
    }
    
    public void waitForIndexing(long maxWait) throws AnalyticsException, AnalyticsTimeoutException {
        if (maxWait < 0) {
            maxWait = Long.MAX_VALUE;
        }
        List<Record> records = this.loadIndexBatchOperationRecords(Integer.MIN_VALUE, Integer.MAX_VALUE);
        List<String> ids = this.extractRecordIds(records);
        long start = System.currentTimeMillis(), end;
        while (true) {
            if (!this.checkIndexRecordsExists(ids)) {
                break;
            }
            end = System.currentTimeMillis();
            if (end - start > maxWait) {
                throw new AnalyticsTimeoutException("Timed out at waitForIndexing: " + (end - start));
            }
            try {
                Thread.sleep(WAIT_INDEX_TIME_INTERVAL);
            } catch (InterruptedException ignore) {
                /* ignore */
            }
        }
    }
    
    private boolean checkIndexRecordsExists(List<String> ids) throws AnalyticsException {
        List<Record> records = GenericUtils.listRecords(this.getAnalyticsRecordStore(), 
                this.getAnalyticsRecordStore().get(SHARD_INDEX_DATA_RECORD_TENANT_ID, 
                        SHARD_INDEX_DATA_RECORD_TABLE_NAME, null, ids));
        return records.size() > 0;
    }
    
    private void planIndexingWorkersInCluster() throws AnalyticsException {
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        int retryCount = 0;
        while (retryCount < INDEXING_SCHEDULE_PLAN_RETRY_COUNT) {
            try {
                acm.executeAll(ANALYTICS_INDEXING_GROUP, new IndexingStopMessage());
                List<Object> members = acm.getMembers(ANALYTICS_INDEXING_GROUP);
                List<List<Integer>> schedulePlan = this.generateIndexWorkerSchedulePlan(members.size());
                for (int i = 0; i < members.size(); i++) {
                    acm.executeOne(ANALYTICS_INDEXING_GROUP, members.get(i), 
                            new IndexingScheduleMessage(schedulePlan.get(i)));
                }
                break;
            } catch (AnalyticsException e) {
                e.printStackTrace();
                retryCount++;
                if (retryCount < INDEXING_SCHEDULE_PLAN_RETRY_COUNT) {
                    log.warn("Retrying index schedule planning: " + 
                            e.getMessage() + ": attempt " + (retryCount + 1) + "...", e);
                } else {
                    log.error("Giving up index schedule planning: " + e.getMessage(), e);
                    throw new AnalyticsException("Error in index schedule planning: " + e.getMessage(), e);
                }
            }
        }
    }
    
    @Override
    public void onBecomingLeader() {
        try {
            this.planIndexingWorkersInCluster();
        } catch (AnalyticsException e) {
            log.error("Error in planning indexing workers on becoming leader: " + e.getMessage(), e);
        }
    }

    @Override
    public void onLeaderUpdate() {
        /* nothing to do */
    }

    @Override
    public void onMembersChangeForLeader() {
        try {
            this.planIndexingWorkersInCluster();
        } catch (AnalyticsException e) {
            log.error("Error in planning indexing workers on members change: " + e.getMessage(), e);
        }
    }
    
    /**
     * This is executed to stop all indexing operations in the current node.
     */
    public static class IndexingStopMessage implements Callable<String> {

        @Override
        public String call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            /* these cluster messages are specific to AnalyticsDataServiceImpl */
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                adsImpl.getIndexer().stopAndCleanupIndexProcessing();
            }
            return "OK";
        }
        
    }
    
    /**
     * This is executed to start indexing operations in the current node.
     */
    public static class IndexingScheduleMessage implements Callable<String> {
        
        private List<Integer> shardIndices;
        
        public IndexingScheduleMessage(List<Integer> shardIndices) {
            this.shardIndices = shardIndices;
        }

        @Override
        public String call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                adsImpl.getIndexer().scheduleWorkers(this.shardIndices);
            }
            return "OK";
        }
        
    }
    
    /**
     * This represents an indexing worker, who does index operations in the background.
     */
    private class IndexWorker implements Runnable {

        private static final int INDEX_WORKER_SLEEP_TIME = 1000;
        
        private int shardIndex;
        
        public IndexWorker(int shardIndex) {
            this.shardIndex = shardIndex;
        }
        
        public int getShardIndex() {
            return shardIndex;
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    processIndexBatchOperations(this.getShardIndex());
                    Thread.sleep(INDEX_WORKER_SLEEP_TIME);
                } catch (AnalyticsException e) { 
                    log.error("Error in processing index batch operations: " + e.getMessage(), e);
                } catch (InterruptedException e) { 
                    break;
                }
            }
        }
        
    }
    
    /**
     * Index operation type.
     */
    public static enum IndexOperationType {
        UPDATE,
        DELETE
    }
    
    /**
     * This represents an index batch operation, which is a set of index operations done by the user earlier.
     */
    public static class IndexBatchOperation implements Serializable {

        private static final long serialVersionUID = -1299823657881276408L;

        private IndexOperationType operationType;
        
        private int deleteTenantId;
        
        private String deleteTableName;
        
        private long deleteTimestamp;
        
        private List<String> recordIds;
        
        private List<Record> records;
        
        public IndexOperationType getOperationType() {
            return operationType;
        }
        
        public void setOperationType(IndexOperationType operationType) {
            this.operationType = operationType;
        }

        public List<String> getRecordIds() {
            return recordIds;
        }
        
        public void setRecordIds(List<String> recordIds) {
            this.recordIds = recordIds;
        }

        public List<Record> getRecords() {
            return records;
        }
        
        public void setRecords(List<Record> records) {
            this.records = records;
        }
        
        public int getDeleteTenantId() {
            return deleteTenantId;
        }
        
        public void setDeleteTenantId(int deleteTenantId) {
            this.deleteTenantId = deleteTenantId;
        }
        
        public String getDeleteTableName() {
            return deleteTableName;
        }
        
        public long getDeleteTimestamp() {
            return deleteTimestamp;
        }
        
        public void setDeleteTimestamp(long deleteTimestamp) {
            this.deleteTimestamp = deleteTimestamp;
        }

        public void setDeleteTableName(String deleteTableName) {
            this.deleteTableName = deleteTableName;
        }

        public List<IndexOperation> extractIndexOperations() {
            List<IndexOperation> indexOps = null;
            IndexOperation indexOp;
            if (IndexOperationType.UPDATE == this.getOperationType()) {
                indexOps = new ArrayList<IndexOperation>(this.getRecords().size());
                for (Record record : this.getRecords()) {
                    indexOp = new IndexOperation();
                    indexOp.setOperationType(IndexOperationType.UPDATE);
                    indexOp.setUpdateRecord(record);
                    indexOps.add(indexOp);
                }
            } else if (IndexOperationType.DELETE == this.getOperationType()) {
                indexOps = new ArrayList<IndexOperation>(this.getRecordIds().size());
                for (String id : this.getRecordIds()) {
                    indexOp = new IndexOperation();
                    indexOp.setOperationType(IndexOperationType.DELETE);
                    indexOp.setDeleteTenantId(this.getDeleteTenantId());
                    indexOp.setDeleteTableId(this.getDeleteTableName());
                    indexOp.setDeleteId(id);
                    indexOp.setDeleteTimestamp(this.getDeleteTimestamp());
                    indexOps.add(indexOp);
                }
            }
            return indexOps;
        }
        
    }
    
    /**
     * This represents an index operation.
     */
    public static class IndexOperation implements Comparable<IndexOperation> {
        
        private IndexOperationType operationType;
        
        private int deleteTenantId;
        
        private String deleteTableId;
        
        private String deleteId;
        
        private long deleteTimestamp;
        
        private Record updateRecord;
        
        public long getTimestamp() {
            if (IndexOperationType.UPDATE == this.operationType) {
                return this.getUpdateRecord().getTimestamp();
            } else if (IndexOperationType.DELETE == this.operationType) {
                return this.getDeleteTimestamp();
            } else {
                return 0;
            }
        }
                
        public IndexOperationType getOperationType() {
            return operationType;
        }
        
        public void setOperationType(IndexOperationType operationType) {
            this.operationType = operationType;
        }
        
        public int getDeleteTenantId() {
            return deleteTenantId;
        }
        
        public void setDeleteTenantId(int deleteTenantId) {
            this.deleteTenantId = deleteTenantId;
        }
        
        public String getDeleteTableName() {
            return deleteTableId;
        }
        
        public void setDeleteTableId(String deleteTableId) {
            this.deleteTableId = deleteTableId;
        }

        public String getDeleteId() {
            return deleteId;
        }
        
        public void setDeleteId(String deleteId) {
            this.deleteId = deleteId;
        }
        
        public long getDeleteTimestamp() {
            return deleteTimestamp;
        }
        
        public void setDeleteTimestamp(long deleteTimestamp) {
            this.deleteTimestamp = deleteTimestamp;
        }
        
        public Record getUpdateRecord() {
            return updateRecord;
        }
        
        public void setUpdateRecord(Record updateRecord) {
            this.updateRecord = updateRecord;
        }

        @Override
        public int compareTo(IndexOperation rhs) {
            if (this.getTimestamp() > rhs.getTimestamp()) {
                return 1;
            } else if (this.getTimestamp() < rhs.getTimestamp()) {
                return -1;
            } else {
                return 0;
            }
        }
        
    }
    
}
