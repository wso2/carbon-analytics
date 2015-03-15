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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.AnalyticsDirectory;
import org.wso2.carbon.analytics.dataservice.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.AnalyticsQueryParser;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterException;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.clustering.GroupEventListener;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataCorruptionException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsTimeoutException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * This class represents the indexing functionality.
 */
public class AnalyticsDataIndexer implements GroupEventListener {
    
    private static final int INDEXING_SCHEDULE_PLAN_RETRY_COUNT = 3;

    private static final String DISABLE_INDEXING_ENV_PROP = "disableIndexing";

    private static final int WAIT_INDEX_TIME_INTERVAL = 1000;

    private static final String INDEX_OP_DATA_ATTRIBUTE = "__INDEX_OP_DATA__";

    private static final String SHARD_INDEX_DATA_UPDATE_RECORDS_TABLE_PREFIX = "__SHARD_INDEX_UPDATE_RECORDS__";
    
    private static final String SHARD_INDEX_DATA_DELETE_RECORDS_TABLE_PREFIX = "__SHARD_INDEX_DELETE_RECORDS__";

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
    
    private ExecutorService shardWorkerExecutor;
            
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
            log.info("Analytics Indexing Mode: CLUSTERED");
            acm.joinGroup(ANALYTICS_INDEXING_GROUP, this);
        } else {
            log.info("Analytics Indexing Mode: STANDALONE");
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
    
    private void scheduleWorkers(List<Integer> shardIndices) throws AnalyticsException {
        this.stopAndCleanupIndexProcessing();
        this.shardWorkerExecutor = Executors.newFixedThreadPool(shardIndices.size());
        for (int shardIndex : shardIndices) {
            this.shardWorkerExecutor.execute(new IndexWorker(shardIndex));
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
    
    private void insertIndexOperationRecords(List<Record> indexOpRecords) throws AnalyticsException {
        /* each record will be in its own table */
        List<Record> records = new ArrayList<Record>(1);
        for (Record record : indexOpRecords) {
            records.clear();
            records.add(record);
            try {
                this.getAnalyticsRecordStore().put(records);
            } catch (AnalyticsTableNotAvailableException e) {
                this.getAnalyticsRecordStore().createTable(record.getTenantId(), record.getTableName());
                this.getAnalyticsRecordStore().put(records);
            }
        }
    }
    
    private void scheduleIndexUpdate(List<Record> records) throws AnalyticsException {
        Map<Integer, List<IndexOperation>> shardedIndexOpBatches = this.groupRecordsIdsByShardIndex(records);
        List<Record> indexOpRecords = this.generateIndexOperationRecords(SHARD_INDEX_DATA_RECORD_TENANT_ID,
                SHARD_INDEX_DATA_UPDATE_RECORDS_TABLE_PREFIX, shardedIndexOpBatches);
        this.insertIndexOperationRecords(indexOpRecords);
    }
    
    private void scheduleIndexDelete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        Map<Integer, List<IndexOperation>> shardedRecordIdBatches = this.groupRecordIdsByShardIndex(
                tenantId, tableName, ids);
        List<Record> indexOpRecords = this.generateIndexOperationRecords(SHARD_INDEX_DATA_RECORD_TENANT_ID,
                SHARD_INDEX_DATA_DELETE_RECORDS_TABLE_PREFIX, shardedRecordIdBatches);
        this.insertIndexOperationRecords(indexOpRecords);
    }
    
    private byte[] indexOpsToBinary(List<IndexOperation> indexOps) throws AnalyticsException {
        ByteArrayOutputStream byteOut = null;
        ObjectOutputStream objOut = null;
        try {
            byteOut = new ByteArrayOutputStream();
            objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(indexOps);
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new AnalyticsException("Error in converting index ops to binary: " + e.getMessage(), e);
        } finally {
            try {
                if (objOut != null) {
                    objOut.close();
                }
            } catch (IOException e) {
                log.error(e);
            }
            try {
                if (byteOut != null) {
                    byteOut.close();
                }
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<IndexOperation> binaryToIndexOps(byte[] data) throws AnalyticsDataCorruptionException {
        ByteArrayInputStream byteIn = null;
        ObjectInputStream objIn = null;
        try {
            byteIn = new ByteArrayInputStream(data);
            objIn = new ObjectInputStream(byteIn);
            return (List<IndexOperation>) objIn.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new AnalyticsDataCorruptionException("Error in converting binary data to index ops: " + 
                    e.getMessage(), e);
        } finally {
            try {
                if (objIn != null) {
                    objIn.close();
                }
            } catch (IOException e) {
                log.error(e);
            }
            try {
                if (byteIn != null) {
                    byteIn.close();
                }
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
    
    private Record generateIndexOperationRecord(int tenantId, String tableNamePrefix, int shardIndex, 
            List<IndexOperation> indexOps) throws AnalyticsException {
        Map<String, Object> values = new HashMap<String, Object>(1);
        values.put(INDEX_OP_DATA_ATTRIBUTE, this.indexOpsToBinary(indexOps));
        return new Record(tenantId, this.generateShardedIndexDataTableName(tableNamePrefix, shardIndex), 
                values, System.currentTimeMillis());
    }
    
    private List<Record> generateIndexOperationRecords(int tenantId, String tableNamePrefix,
            Map<Integer, List<IndexOperation>> shardedIndexOpBatches) throws AnalyticsException {
        List<Record> result = new ArrayList<Record>(shardedIndexOpBatches.size());
        for (Map.Entry<Integer, List<IndexOperation>> entry : shardedIndexOpBatches.entrySet()) {
            result.add(this.generateIndexOperationRecord(tenantId, tableNamePrefix, entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private List<IndexOperation> extractIndexOperations(Record indexOpRecord) throws AnalyticsDataCorruptionException {
        byte[] data = (byte[]) indexOpRecord.getValue(INDEX_OP_DATA_ATTRIBUTE);
        return this.binaryToIndexOps(data);
    }
    
    private List<IndexOperation> checkAndExtractIndexOperations(List<Record> indexOpRecords) throws AnalyticsException {
        List<IndexOperation> indexOps = new ArrayList<IndexOperation>();
        for (Record indexOpRecord : indexOpRecords) {
            try {
                indexOps.addAll(this.extractIndexOperations(indexOpRecord));
            } catch (AnalyticsDataCorruptionException e) {
                this.removeIndexOperationRecord(indexOpRecord);
                log.error("Corrupted index operation record deleted, id: " + indexOpRecord.getId() + 
                        " shard index: " + indexOpRecord.getTimestamp());
            }
        }
        return indexOps;
    }
    
    private Collection<List<IndexOperation>> extractIndexOpBatches(List<IndexOperation> indexOps) {
        Map<String, List<IndexOperation>> opBatches = new HashMap<String, List<IndexOperation>>();
        List<IndexOperation> opBatch;
        String identity;
        for (IndexOperation indexOp : indexOps) {
            identity = this.generateTableId(indexOp.getTenantId(), indexOp.getTableName());
            opBatch = opBatches.get(identity);
            if (opBatch == null) {
                opBatch = new ArrayList<IndexOperation>();
                opBatches.put(identity, opBatch);
            }
            opBatch.add(indexOp);
        }
        return opBatches.values();
    }
    
    private List<String> extractIds(List<IndexOperation> indexOpBatch) {
        List<String> ids = new ArrayList<String>(indexOpBatch.size());
        for (IndexOperation indexOp : indexOpBatch) {
            ids.add(indexOp.getId());
        }
        return ids;
    }
    
    private List<Record> lookupRecordBatch(List<IndexOperation> indexOpBatch) 
            throws AnalyticsException {
        IndexOperation firstOp = indexOpBatch.get(0);
        int tenantId = firstOp.getTenantId();
        String tableName = firstOp.getTableName();
        List<String> ids = this.extractIds(indexOpBatch);
        AnalyticsRecordStore ars = this.getAnalyticsRecordStore();
        try {
            return GenericUtils.listRecords(ars, ars.get(tenantId, tableName, 1, null, ids));
        } catch (AnalyticsTableNotAvailableException e) {
            return new ArrayList<Record>(0);
        }
    }
    
    private void processIndexUpdateOpBatches(int shardIndex, 
            Collection<List<IndexOperation>> indexUpdateOpBatches) throws AnalyticsException {
        Map<String, IndexType> indices;
        Record firstRecord;
        List<Record> records;
        for (List<IndexOperation> indexOpBatch : indexUpdateOpBatches) {
            records = this.lookupRecordBatch(indexOpBatch);
            if (records.size() == 0) {
                continue;
            }
            firstRecord = records.get(0);
            indices = this.lookupIndices(firstRecord.getTenantId(), firstRecord.getTableName());
            if (indices.size() > 0) {
                this.updateIndex(shardIndex, records, indices);
            }
        }
    }
    
    private List<IndexOperation> resolveDeleteOperations(List<IndexOperation> indexOpBatch, 
            List<Record> existingRecords) {
        Map<String, IndexOperation> opMap = new HashMap<String, IndexOperation>(indexOpBatch.size());
        for (IndexOperation indexOp : indexOpBatch) {
            opMap.put(indexOp.getId(), indexOp);
        }
        for (Record existingRecord : existingRecords) {
            opMap.remove(existingRecord.getId());
        }
        return new ArrayList<IndexOperation>(opMap.values());
    }
    
    private void processIndexDeleteOpBatches(int shardIndex, 
            Collection<List<IndexOperation>> indexDeleteOpBatches) throws AnalyticsException {
        List<Record> existingRecords;
        List<IndexOperation> finalList;
        for (List<IndexOperation> indexOpBatch : indexDeleteOpBatches) {
            /* here we are checking the records that were told be deleted in the indices are 
             * there in the record store, only if it's not there in the record store now,
             * that means it is deleted, or else, it means, that record has again been added,
             * and those indices would have been updated with insert index operations */
            existingRecords = this.lookupRecordBatch(indexOpBatch);
            finalList = this.resolveDeleteOperations(indexOpBatch, existingRecords);
            if (finalList.size() > 0) {
                this.delete(shardIndex, finalList);
            }
        }
    }
    
    private void processIndexUpdateOperations(int shardIndex) throws AnalyticsException {
        List<Record> indexUpdateOpRecords = this.loadIndexOperationUpdateRecords(shardIndex);
        List<IndexOperation> indexUpdateOps = this.checkAndExtractIndexOperations(indexUpdateOpRecords);
        Collection<List<IndexOperation>> indexUpdateOpBatches = this.extractIndexOpBatches(indexUpdateOps);
        this.processIndexUpdateOpBatches(shardIndex, indexUpdateOpBatches);
        this.removeIndexOperationRecords(indexUpdateOpRecords);
    }
    
    private void processIndexDeleteOperations(int shardIndex) throws AnalyticsException {
        List<Record> indexDeleteOpRecords = this.loadIndexOperationDeleteRecords(shardIndex);
        List<IndexOperation> indexDeleteOps = this.checkAndExtractIndexOperations(indexDeleteOpRecords);
        Collection<List<IndexOperation>> indexDeleteOpBatches = this.extractIndexOpBatches(indexDeleteOps);
        this.processIndexDeleteOpBatches(shardIndex, indexDeleteOpBatches);
        this.removeIndexOperationRecords(indexDeleteOpRecords);
    }
    
    private void processIndexOperations(int shardIndex) throws AnalyticsException {
        this.processIndexUpdateOperations(shardIndex);
        this.processIndexDeleteOperations(shardIndex);
    }
    
    private void removeIndexOperationRecords(List<Record> records) throws AnalyticsException {
        if (records.size() == 0) {
            return;
        }
        Record firstRecord = records.get(0);
        List<String> ids = this.extractRecordIds(records);
        this.getAnalyticsRecordStore().delete(firstRecord.getTenantId(), firstRecord.getTableName(), ids);
    }
    
    private void removeIndexOperationRecord(Record record) throws AnalyticsException {
        List<Record> records = new ArrayList<Record>(1);
        records.add(record);
        this.removeIndexOperationRecords(records);
    }
    
    private String generateShardedIndexDataTableName(String tableNamePrefix, int shardIndex) {
        return tableNamePrefix + shardIndex;
    }
    
    private List<Record> loadIndexOperationUpdateRecords(int shardIndex) throws AnalyticsException {
        return this.loadIndexOperationRecords(SHARD_INDEX_DATA_RECORD_TENANT_ID, 
                this.generateShardedIndexDataTableName(SHARD_INDEX_DATA_UPDATE_RECORDS_TABLE_PREFIX, shardIndex));
    }
        
    private List<Record> loadIndexOperationDeleteRecords(int shardIndex) throws AnalyticsException {
        return this.loadIndexOperationRecords(SHARD_INDEX_DATA_RECORD_TENANT_ID, 
                this.generateShardedIndexDataTableName(SHARD_INDEX_DATA_DELETE_RECORDS_TABLE_PREFIX, shardIndex));
    }
    
    private List<Record> loadAllIndexOperationUpdateRecords() throws AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        for (int i = 0; i < this.getShardCount(); i++) {
            result.addAll(this.loadIndexOperationUpdateRecords(i));
        }
        return result;
    }
    
    private List<Record> loadAllIndexOperationDeleteRecords() throws AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        for (int i = 0; i < this.getShardCount(); i++) {
            result.addAll(this.loadIndexOperationDeleteRecords(i));
        }
        return result;
    }
    
    private List<Record> loadIndexOperationRecords(int tenantId, String tableName) throws AnalyticsException {
        try {
            return GenericUtils.listRecords(this.getAnalyticsRecordStore(), 
                    this.getAnalyticsRecordStore().get(tenantId, tableName, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        } catch (AnalyticsTableNotAvailableException e) {
            /* ignore this scenario, before any indexes, this will happen */
            return new ArrayList<Record>(0);
        }
    }
    
    private Map<Integer, List<IndexOperation>> groupRecordsIdsByShardIndex(List<Record> records) {
        Map<Integer, List<IndexOperation>> result = new HashMap<Integer, List<IndexOperation>>();
        int shardIndex;
        List<IndexOperation> group;
        for (Record record : records) {
            shardIndex = this.calculateShardId(record);
            group = result.get(shardIndex);
            if (group == null) {
                group = new ArrayList<IndexOperation>();
                result.put(shardIndex, group);
            }
            group.add(new IndexOperation(record.getTenantId(), record.getTableName(), record.getId()));
        }
        return result;
    }
    
    private Map<Integer, List<IndexOperation>> groupRecordIdsByShardIndex(int tenantId, String tableName, List<String> ids) {
        Map<Integer, List<IndexOperation>> result = new HashMap<Integer, List<IndexOperation>>();
        int shardIndex;
        List<IndexOperation> group;
        for (String id : ids) {
            shardIndex = this.calculateShardId(id);
            group = result.get(shardIndex);
            if (group == null) {
                group = new ArrayList<IndexOperation>();
                result.put(shardIndex, group);
            }
            group.add(new IndexOperation(tenantId, tableName, id));
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
        String globalPath = this.generateDirPath(this.generateTableId(tenantId, tableName));
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
    
    /**
     * Adds the given records to the index if they are previously scheduled to be indexed.
     * @param records The records to be indexed
     * @throws AnalyticsException
     */
    public void put(List<Record> records) throws AnalyticsException {
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
        int tenantId = firstOp.getTenantId();
        String tableName = firstOp.getTableName();
        String tableId = this.generateShardedTableId(tenantId, tableName, Integer.toString(shardIndex));
        IndexWriter indexWriter = this.createIndexWriter(tableId);
        List<Term> terms = new ArrayList<Term>(deleteOpBatch.size());
        for (IndexOperation op : deleteOpBatch) {
            terms.add(new Term(INDEX_ID_INTERNAL_FIELD, op.getId()));
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
        String tableId = this.generateTableId(tenantId, tableName);
        this.indexDefs.put(tableId, columns);
        this.getRepository().setIndices(tenantId, tableName, columns);
        this.notifyClusterIndexChange(tenantId, tableName);
    }
    
    public Map<String, IndexType> lookupIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        String tableId = this.generateTableId(tenantId, tableName);
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
        String tableId = this.generateTableId(tenantId, tableName);
        this.indexDefs.remove(tableId);
        this.getRepository().clearAllIndices(tenantId, tableName);
        this.closeAndRemoveIndexDirs(tenantId, tableName);
        /* delete all global index data, not only local ones */
        this.deleteIndexData(tenantId, tableName);
        this.notifyClusterIndexChange(tenantId, tableName);
    }
    
    private String generateShardedTableId(int tenantId, String tableName, String shardId) {
        /* the table names are not case-sensitive */
        return this.generateTableId(tenantId, tableName) + "/" + shardId;
    }
    
    private boolean isShardedTableId(int tenantId, String tableName, String shardedTableId) {
        return shardedTableId.startsWith(this.generateTableId(tenantId, tableName) + "/");
    }
    
    private String generateTableId(int tenantId, String tableName) {
        /* the table names are not case-sensitive */
        return tenantId + "_" + tableName.toLowerCase();
    }
    
    private void clusterNoficationReceived(int tenantId, String tableName) throws AnalyticsIndexException {
        /* remove the entry from the cache, this will force the next index operations to load
         * the index definition from the back-end store, this makes sure, we have optimum cache cleanup
         * and improves memory usage for tenant partitioning */
        String tableId = this.generateTableId(tenantId, tableName);
        this.indexDefs.remove(tableId);
        this.closeAndRemoveIndexDirs(tenantId, tableName);
    }
    
    private void notifyClusterIndexChange(int tenantId, 
            String tableName) throws AnalyticsIndexException {
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            try {
                acm.executeAll(ANALYTICS_INDEXING_GROUP, new IndexChangeMessage(tenantId, tableName));
            } catch (AnalyticsClusterException e) {
                throw new AnalyticsIndexException("Error in cluster index notification: " + e.getMessage(), e);
            }
        }
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
        if (this.shardWorkerExecutor != null) {
            this.shardWorkerExecutor.shutdownNow();
            this.shardWorkerExecutor = null;
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
        List<Record> updateRecords = this.loadAllIndexOperationUpdateRecords();
        List<Record> deleteRecords = this.loadAllIndexOperationDeleteRecords();
        long start = System.currentTimeMillis(), end;
        boolean updateDone = false, deleteDone = false;
        while (true) {
            if (!updateDone && this.checkRecordsEmpty(updateRecords)) {
                updateDone = true;
            }
            if (!deleteDone && this.checkRecordsEmpty(deleteRecords)) {
                deleteDone = true;
            }
            if (updateDone && deleteDone) {
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
    
    private Collection<List<Record>> groupRecordsByTable(List<Record> records) {
        Map<String, List<Record>> result = new HashMap<String, List<Record>>();
        String identity;
        List<Record> group;
        for (Record record : records) {
            identity = record.getTenantId() + "_" + record.getTableName();
            group = result.get(identity);
            if (group == null) {
                group = new ArrayList<Record>();
                result.put(identity, group);
            }
            group.add(record);
        }
        return result.values();
    }
    
    private boolean checkRecordsEmpty(List<Record> records) throws AnalyticsException {
        Collection<List<Record>> groups = this.groupRecordsByTable(records);
        Record firstRecord;
        for (List<Record> group : groups) {
            firstRecord = group.get(0);
            try {
                if (GenericUtils.listRecords(this.getAnalyticsRecordStore(), 
                        this.getAnalyticsRecordStore().get(firstRecord.getTenantId(), 
                        firstRecord.getTableName(), 1, null, 
                        this.extractRecordIds(group))).size() > 0) {
                    return false;
                }
            } catch (AnalyticsTableNotAvailableException e) {
                /* ignore */
            }
        }            
        return true;        
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
    public static class IndexingStopMessage implements Callable<String>, Serializable {

        private static final long serialVersionUID = 2146438164013418569L;

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
     * This is executed to stop all indexing operations in the current node.
     */
    public static class IndexChangeMessage implements Callable<String>, Serializable {

        private static final long serialVersionUID = -7722819207554840105L;

        private int tenantId;
        
        private String tableName;
        
        public IndexChangeMessage(int tenantId, String tableName) {
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
                adsImpl.getIndexer().clusterNoficationReceived(this.tenantId, this.tableName);
            }
            return "OK";
        }
        
    }
    
    /**
     * This is executed to start indexing operations in the current node.
     */
    public static class IndexingScheduleMessage implements Callable<String>, Serializable {
        
        private static final long serialVersionUID = 7912933193977147465L;
        
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
     * This class represents an indexing operation for a record.
     */
    public static class IndexOperation implements Serializable {
        
        private static final long serialVersionUID = -5071679492708482851L;

        private int tenantId;
        
        private String tableName;
        
        private String id;
        
        public IndexOperation(int tenantId, String tableName, String id) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.id = id;
        }
        
        public int getTenantId() {
            return tenantId;
        }
        
        public String getTableName() {
            return tableName;
        }

        public String getId() {
            return id;
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
                    processIndexOperations(this.getShardIndex());
                    Thread.sleep(INDEX_WORKER_SLEEP_TIME);
                } catch (AnalyticsException e) {
                    log.error("Error in processing index batch operations: " + e.getMessage(), e);
                } catch (InterruptedException e) { 
                    break;
                }
            }
        }
        
    }
    
}
