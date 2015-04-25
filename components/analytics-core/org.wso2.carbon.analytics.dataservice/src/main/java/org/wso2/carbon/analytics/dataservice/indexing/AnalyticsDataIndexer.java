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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.range.DoubleRange;
import org.apache.lucene.facet.range.DoubleRangeFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumValueSource;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.apache.lucene.util.Version;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.AnalyticsDirectory;
import org.wso2.carbon.analytics.dataservice.AnalyticsQueryParser;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterException;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.clustering.GroupEventListener;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategorySearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsCategoryPath;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataCorruptionException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the indexing functionality.
 */
public class AnalyticsDataIndexer implements GroupEventListener {

    private static final Log log = LogFactory.getLog(AnalyticsDataIndexer.class);

    private static final int INDEX_WORKER_STOP_WAIT_TIME = 60;

    private static final int INDEXING_SCHEDULE_PLAN_RETRY_COUNT = 3;

    public static final String DISABLE_INDEXING_ENV_PROP = "disableIndexing";
    
    private static final int WAIT_INDEX_TIME_INTERVAL = 1000;

    private static final String INDEX_OP_DATA_ATTRIBUTE = "__INDEX_OP_DATA__";

    private static final String SHARD_INDEX_DATA_UPDATE_RECORDS_TABLE_PREFIX = "__SHARD_INDEX_UPDATE_RECORDS__";
    
    private static final String SHARD_INDEX_DATA_DELETE_RECORDS_TABLE_PREFIX = "__SHARD_INDEX_DELETE_RECORDS__";

    private static final int SHARD_INDEX_DATA_RECORD_TENANT_ID = -1000;
    
    private static final String ANALYTICS_INDEXING_GROUP = "__ANALYTICS_INDEXING_GROUP__";

    private static final String INDEX_DATA_FS_BASE_PATH = "/_data/index/";

    private static final String TAXONOMY_INDEX_DATA_FS_BASE_PATH = "/_data/taxonomy/" ;

    public static final String INDEX_ID_INTERNAL_FIELD = "_id";

    public static final String INDEX_INTERNAL_TIMESTAMP_FIELD = "_timestamp";

    private static final String INDEX_INTERNAL_SCORE_FIELD = "_score";

    private static final String INDEX_INTERNAL_WEIGHT_FIELD = "_weight";

    private static final String NULL_INDEX_VALUE = "";

    private static final java.lang.String DEFAULT_SCORE = "1";

    private AnalyticsIndexDefinitionRepository repository;
    
    private Map<String, Map<String, IndexType>> indexDefs = new HashMap<>();

    private Map<String, List<String>> scoreParams = new HashMap<>();
    
    private Map<String, Directory> indexDirs = new HashMap<>();

    private Map<String, Directory> indexTaxonomyDirs = new HashMap<>();
    
    private Analyzer luceneAnalyzer;
    
    private AnalyticsFileSystem analyticsFileSystem;
    
    private AnalyticsRecordStore analyticsRecordStore;
    
    private int shardCount;
    
    private ExecutorService shardWorkerExecutor;
    
    private List<IndexWorker> workers;
    
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
        this.workers = new ArrayList<IndexWorker>(shardIndices.size());
        IndexWorker worker;
        for (int shardIndex : shardIndices) {
            worker = new IndexWorker(shardIndex);
            this.workers.add(worker);
            this.shardWorkerExecutor.execute(worker);
        }
        log.info("Processing Analytics Indexing Shards " + shardIndices);
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
        List<Record> records = new ArrayList<>(1);
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
    
    private Record generateIndexOperationRecord(int tenantId, String tableNamePrefix, int shardIndex, 
            List<IndexOperation> indexOps) throws AnalyticsException {
        Map<String, Object> values = new HashMap<>(1);
        values.put(INDEX_OP_DATA_ATTRIBUTE, GenericUtils.serializeObject(indexOps));
        return new Record(GenericUtils.generateRecordID(), tenantId, 
                this.generateShardedIndexDataTableName(tableNamePrefix, shardIndex), values);
    }
    
    private List<Record> generateIndexOperationRecords(int tenantId, String tableNamePrefix,
            Map<Integer, List<IndexOperation>> shardedIndexOpBatches) throws AnalyticsException {
        List<Record> result = new ArrayList<Record>(shardedIndexOpBatches.size());
        for (Map.Entry<Integer, List<IndexOperation>> entry : shardedIndexOpBatches.entrySet()) {
            result.add(this.generateIndexOperationRecord(tenantId, tableNamePrefix, entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private List<IndexOperation> extractIndexOperations(Record indexOpRecord) throws AnalyticsDataCorruptionException {
        byte[] data = (byte[]) indexOpRecord.getValue(INDEX_OP_DATA_ATTRIBUTE);
        return (List<IndexOperation>) GenericUtils.deserializeObject(data);
    }
    
    private List<IndexOperation> checkAndExtractIndexOperations(List<Record> indexOpRecords) throws AnalyticsException {
        List<IndexOperation> indexOps = new ArrayList<>();
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
        Map<String, List<IndexOperation>> opBatches = new HashMap<>();
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
        List<String> ids = new ArrayList<>(indexOpBatch.size());
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
        Map<String, IndexOperation> opMap = new HashMap<>(indexOpBatch.size());
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
        List<Record> records = new ArrayList<>(1);
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
        List<Record> result = new ArrayList<>();
        for (int i = 0; i < this.getShardCount(); i++) {
            result.addAll(this.loadIndexOperationUpdateRecords(i));
        }
        return result;
    }
    
    private List<Record> loadAllIndexOperationDeleteRecords() throws AnalyticsException {
        List<Record> result = new ArrayList<>();
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
        Map<Integer, List<IndexOperation>> result = new HashMap<>();
        int shardIndex;
        List<IndexOperation> group;
        for (Record record : records) {
            shardIndex = this.calculateShardId(record);
            group = result.get(shardIndex);
            if (group == null) {
                group = new ArrayList<>();
                result.put(shardIndex, group);
            }
            group.add(new IndexOperation(record.getTenantId(), record.getTableName(), record.getId()));
        }
        return result;
    }
    
    private Map<Integer, List<IndexOperation>> groupRecordIdsByShardIndex(int tenantId, String tableName, List<String> ids) {
        Map<Integer, List<IndexOperation>> result = new HashMap<>();
        int shardIndex;
        List<IndexOperation> group;
        for (String id : ids) {
            shardIndex = this.calculateShardId(id);
            group = result.get(shardIndex);
            if (group == null) {
                group = new ArrayList<>();
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
    
    private List<String> lookupGloballyExistingShardIds(String basepath, int tenantId, String tableName)
            throws AnalyticsIndexException {
        String globalPath = this.generateDirPath(basepath, this.generateTableId(tenantId, tableName));
        try {
            List<String> names = this.getFileSystem().list(globalPath);
            List<String> result = new ArrayList<>();
            for (String name : names) {
                result.add(name);
            }
            return result;
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in looking up index shard directories for tenant: " + 
                    tenantId + " table: " + tableName);
        }
    }
    
    public List<SearchResultEntry> search(int tenantId, String tableName, String query,
            int start, int count) throws AnalyticsIndexException {

        List<SearchResultEntry> result = new ArrayList<>();
        result.addAll(this.doSearch(tenantId, tableName, query, start, count));
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

    private List<SearchResultEntry> doSearch(int tenantId, String tableName, String query, int start, int count)
            throws AnalyticsIndexException {
        List<SearchResultEntry> result = new ArrayList<>();
        IndexReader reader = null;
        ExecutorService searchExecutor = Executors.newCachedThreadPool();
        try {
            reader = this.getCombinedIndexReader(tenantId, tableName);
            IndexSearcher searcher = new IndexSearcher(reader, searchExecutor);
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
            throw new AnalyticsIndexException("Error in index search: " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error in closing the reader: " + e.getMessage(), e);;
                }
            }
            searchExecutor.shutdown();
        }
    }

    public int searchCount(int tenantId, String tableName, String query) throws AnalyticsIndexException {
        IndexReader reader = null;
        try {
            reader = this.getCombinedIndexReader(tenantId, tableName);
            IndexSearcher searcher = new IndexSearcher(reader);
            Map<String, IndexType> indices = this.lookupIndices(tenantId, tableName);
            Query indexQuery = new AnalyticsQueryParser(this.luceneAnalyzer, indices).parse(query);
            TotalHitCountCollector collector = new TotalHitCountCollector();
            searcher.search(indexQuery, collector);
            return collector.getTotalHits();
        } catch (Exception e) {
            throw new AnalyticsIndexException("Error in index search count: " +
                                              e.getMessage(), e);
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

    public List<AnalyticsDrillDownRange> drillDownRangeCount(int tenantId,
            AnalyticsDrillDownRequest drillDownRequest) throws AnalyticsIndexException {
        if (drillDownRequest.getRangeField() == null) {
            throw new AnalyticsIndexException("Rangefield is not set");
        }
        if (drillDownRequest.getRanges() == null) {
            throw new AnalyticsIndexException("Ranges are not set");
        }
        IndexReader indexReader= null;
        try {
            indexReader = this.getCombinedIndexReader(tenantId, drillDownRequest.getTableName());
            return getAnalyticsDrillDownRanges(tenantId, drillDownRequest, indexReader);
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            throw new AnalyticsIndexException("Error while parsing the lucene query: " +
                                              e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error while reading sharded indices: " +
                                              e.getMessage(), e);
        } finally {
            if (indexReader != null) {
                try {
                    indexReader.close();
                } catch (IOException e) {
                    log.error("Error in closing the index reader: " +
                                                      e.getMessage(), e);
                }
            }
        }
    }

    private List<AnalyticsDrillDownRange> getAnalyticsDrillDownRanges(int tenantId,
                                                                      AnalyticsDrillDownRequest drillDownRequest,
                                                                      IndexReader indexReader)
            throws AnalyticsIndexException, org.apache.lucene.queryparser.classic.ParseException,
                   IOException {
        IndexSearcher searcher = new IndexSearcher(indexReader);
        List<AnalyticsDrillDownRange> drillDownRanges = new ArrayList<>();
        drillDownRanges.addAll(drillDownRequest.getRanges());
        Map<String, IndexType> indices = this.lookupIndices(tenantId, drillDownRequest.getTableName());
        Query indexQuery = new MatchAllDocsQuery();
        FacetsCollector fc = new FacetsCollector();
        if (drillDownRequest.getQuery() != null) {
            indexQuery = new AnalyticsQueryParser(this.luceneAnalyzer,
                                                  indices).parse(drillDownRequest.getQuery());
        }
        FacetsCollector.search(searcher, indexQuery, Integer.MAX_VALUE, fc);
        DoubleRange[] ranges = this.createRangeBuckets(drillDownRanges);
        List<String> scoreParams = this.lookupScoreParams(tenantId, drillDownRequest.getTableName());
        ValueSource valueSource = this.getCompiledScoreFunction(drillDownRequest.getScoreFunction(), scoreParams);
        Facets facets;
        if (scoreParams.contains(drillDownRequest.getRangeField())) {
            facets = new DoubleRangeFacetCounts(drillDownRequest.getRangeField(), valueSource, fc, ranges);
        } else {
            facets = new DoubleRangeFacetCounts(drillDownRequest.getRangeField(), fc, ranges);
        }
        FacetResult facetResult = facets.getTopChildren(Integer.MAX_VALUE, drillDownRequest.getRangeField());
        for (int i = 0; i < drillDownRanges.size(); i++) {
            AnalyticsDrillDownRange range = drillDownRanges.get(i);
            range.setScore(facetResult.labelValues[i].value.doubleValue());
        }
        return drillDownRanges;
    }

    private DoubleRange[] createRangeBuckets(List<AnalyticsDrillDownRange> ranges) {
        List<DoubleRange> buckets = new ArrayList<>();
        for (AnalyticsDrillDownRange range : ranges) {
            DoubleRange doubleRange = new DoubleRange(range.getLabel(), range.getFrom(),true, range.getTo(), false);
            buckets.add(doubleRange);
        }
        return buckets.toArray(new DoubleRange[buckets.size()]);
    }

    private MultiReader getCombinedIndexReader(int tenantId, String tableName)
            throws IOException, AnalyticsIndexException {
        List<String> shardIds = this.lookupGloballyExistingShardIds(INDEX_DATA_FS_BASE_PATH,
                                                                    tenantId, tableName);
        List<IndexReader> indexReaders = new ArrayList<>();

        for (String shardId : shardIds) {
            String shardedTableId = this.generateShardedTableId(tenantId, tableName, shardId);
            IndexReader reader = DirectoryReader.open(this.lookupIndexDir(shardedTableId));
            indexReaders.add(reader);
        }
        return new MultiReader(indexReaders.toArray(new IndexReader[indexReaders.size()]));

    }

    public SubCategories drilldownCategories(int tenantId, CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        List<CategorySearchResultEntry> searchResults = this.getDrillDownCategories(tenantId, drillDownRequest);
        List<CategorySearchResultEntry> mergedResult = this.mergePerShardCategoryResults(searchResults);
        String[] path = drillDownRequest.getPath();
        if (path == null) {
            path = new String[] {};
        }
        return new SubCategories(path, mergedResult);
    }

    private List<CategorySearchResultEntry> mergePerShardCategoryResults(List<CategorySearchResultEntry>
                                                                                 searchResults) {
        Map<String, Float> mergedResults = new LinkedHashMap<>();
        List<CategorySearchResultEntry> finalResult = new ArrayList<>();
        for (CategorySearchResultEntry perShardResults : searchResults) {
                Float score = mergedResults.get(perShardResults.getCategoryName());
                if (score != null) {
                    score += perShardResults.getScore();
                } else {
                    mergedResults.put(perShardResults.getCategoryName(), perShardResults.getScore());
                }
        }
        for (Map.Entry<String, Float> entry : mergedResults.entrySet()) {
            finalResult.add(new CategorySearchResultEntry(entry.getKey(), entry.getValue()));
        }
        return finalResult;
    }

    private List<SearchResultEntry> drillDownRecords(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                                     Directory indexDir, Directory taxonomyIndexDir,
                                                     String rangeField,AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        IndexReader indexReader = null;
        TaxonomyReader taxonomyReader = null;
        List<SearchResultEntry> searchResults =new ArrayList<>();
        try {
            indexReader = DirectoryReader.open(indexDir);
            taxonomyReader = new DirectoryTaxonomyReader(taxonomyIndexDir);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            FacetsCollector facetsCollector = new FacetsCollector(true);
            Map<String, IndexType> indices = this.lookupIndices(tenantId,
                                                                drillDownRequest.getTableName());
            FacetsConfig config = this.getFacetsConfigurations(indices);
            DrillSideways drillSideways = new DrillSideways(indexSearcher, config, taxonomyReader);
            DrillDownQuery drillDownQuery = this.createDrillDownQuery(drillDownRequest,
                                                                      indices, config,rangeField, range);
            drillSideways.search(drillDownQuery, facetsCollector);
            int topResultCount = drillDownRequest.getRecordStartIndex() + drillDownRequest.getRecordCount();
            TopDocs topDocs = FacetsCollector.search(indexSearcher, drillDownQuery, topResultCount, facetsCollector);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc);
                searchResults.add(new SearchResultEntry(document.get(INDEX_ID_INTERNAL_FIELD), scoreDoc.score));
            }
            return searchResults;
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error while performing drilldownRecords: " + e.getMessage(), e);
        } finally {
            this.closeTaxonomyIndexReaders(indexReader, taxonomyReader);
        }
    }

    private List<CategorySearchResultEntry> drillDownCategories(int tenantId, Directory indexDir,
             Directory taxonomyIndexDir, CategoryDrillDownRequest drillDownRequest) throws AnalyticsIndexException {
        IndexReader indexReader = null;
        TaxonomyReader taxonomyReader = null;
        List<CategorySearchResultEntry> searchResults =new ArrayList<>();
        try {
            indexReader = DirectoryReader.open(indexDir);
            taxonomyReader = new DirectoryTaxonomyReader(taxonomyIndexDir);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            FacetsCollector facetsCollector = new FacetsCollector(true);
            Map<String, IndexType> indices = this.lookupIndices(tenantId, drillDownRequest.getTableName());
            FacetsConfig config = this.getFacetsConfigurations(indices);
            DrillSideways drillSideways = new DrillSideways(indexSearcher, config, taxonomyReader);
            Query queryObj = new MatchAllDocsQuery();
            if (drillDownRequest.getQuery() != null && !drillDownRequest.getQuery().isEmpty()) {
                queryObj = (new AnalyticsQueryParser(this.luceneAnalyzer, indices)).parse(drillDownRequest.getQuery());
            }
            DrillDownQuery drillDownQuery = new DrillDownQuery(config, queryObj);
            String[] path = drillDownRequest.getPath();
            if (path == null) {
                path = new String[]{};
            }
            drillDownQuery.add(drillDownRequest.getFieldName(), path);
            drillSideways.search(drillDownQuery, facetsCollector);
            List<String> scoreParams = this.lookupScoreParams(tenantId, drillDownRequest.getTableName());
            ValueSource valueSource = this.getCompiledScoreFunction(drillDownRequest.getScoreFunction(),
                                                                    scoreParams);
            Facets facets = new TaxonomyFacetSumValueSource(taxonomyReader, config, facetsCollector,
                                            valueSource);
            FacetResult facetResult = facets.getTopChildren(Integer.MAX_VALUE, drillDownRequest.getFieldName(),
                                                            path);
            if (facetResult != null) {
                LabelAndValue[] categories = facetResult.labelValues;
                for (LabelAndValue category : categories) {
                    searchResults.add(new CategorySearchResultEntry(category.label, category.value.floatValue()));
                }
            }
            return searchResults;
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error while performing drilldownCategories: " + e.getMessage(), e);
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            throw new AnalyticsIndexException("Error while parsing query " + e.getMessage(), e);
        } finally {
            this.closeTaxonomyIndexReaders(indexReader, taxonomyReader);
        }
    }

    private int getDrillDownRecordCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                                     Directory indexDir, Directory taxonomyIndexDir,
                                                     String rangeField,AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {

        IndexReader indexReader = null;
        TaxonomyReader taxonomyReader = null;
        try {
            indexReader = DirectoryReader.open(indexDir);
            taxonomyReader = new DirectoryTaxonomyReader(taxonomyIndexDir);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            Map<String, IndexType> indices = this.lookupIndices(tenantId,
                                                                drillDownRequest.getTableName());
            FacetsConfig config = this.getFacetsConfigurations(indices);
            DrillSideways drillSideways = new DrillSideways(indexSearcher, config, taxonomyReader);
            DrillDownQuery drillDownQuery = this.createDrillDownQuery(drillDownRequest,
                                                                      indices, config,rangeField, range);
            DrillSideways.DrillSidewaysResult result = drillSideways.search(drillDownQuery, Integer.MAX_VALUE);
            return result.hits.totalHits;
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error while getting drilldownCount: " + e.getMessage(), e);
        } finally {
            this.closeTaxonomyIndexReaders(indexReader, taxonomyReader);
        }
    }

    private void closeTaxonomyIndexReaders(IndexReader indexReader, TaxonomyReader taxonomyReader)
            throws AnalyticsIndexException {
        if (indexReader != null) {
            try {
                indexReader.close();
            } catch (IOException e) {
                log.error("Error while closing index reader in drilldown: "+
                                                  e.getMessage(), e);
            }
        }
        if (taxonomyReader != null) {
            try {
                taxonomyReader.close();
            } catch (IOException e) {
                log.error("Error while closing taxonomy reader in drilldown: "+
                                                  e.getMessage(), e);
            }
        }
    }

    private DrillDownQuery createDrillDownQuery(AnalyticsDrillDownRequest drillDownRequest,
                                                Map<String, IndexType> indices, FacetsConfig config,
                                                String rangeField,
                                                AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        Query languageQuery = new MatchAllDocsQuery();
        try {
            if (drillDownRequest.getQuery() != null) {
                languageQuery = new AnalyticsQueryParser(this.luceneAnalyzer,
                         indices).parse(drillDownRequest.getQuery());
            }
            DrillDownQuery drillDownQuery = new DrillDownQuery(config, languageQuery);
            if (range != null && rangeField != null) {
                drillDownQuery.add(rangeField, NumericRangeQuery.newDoubleRange(rangeField,
                                                                                range.getFrom(), range.getTo(), true, false));
            }
            if (drillDownRequest.getCategoryPaths() != null && !drillDownRequest.getCategoryPaths().isEmpty()) {
                for (Map.Entry<String, AnalyticsCategoryPath> entry : drillDownRequest.getCategoryPaths()
                        .entrySet()) {
                    drillDownQuery.add(entry.getKey(), entry.getValue().getPath());
                }
            }
            return drillDownQuery;
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            throw new AnalyticsIndexException("Error while parsing lucene query '" +
                                              languageQuery + "': " + e.getMessage(), e.getCause() );
        }
    }

    private FacetsConfig getFacetsConfigurations(Map<String, IndexType> indices) {
        FacetsConfig config = new FacetsConfig();
        for (Map.Entry<String, IndexType> entry : indices.entrySet()) {
            if (entry.getValue().equals(IndexType.FACET)) {
                String indexField = entry.getKey();
                config.setHierarchical(indexField, true);
                config.setMultiValued(indexField, true);
            }
        }
        return config;
    }

    private ValueSource getCompiledScoreFunction(String scoreFunction, List<String> scoreParams)
            throws AnalyticsIndexException {
        try {
            Expression funcExpression;
            if (scoreFunction == null || scoreFunction.trim().isEmpty()) {
                funcExpression = JavascriptCompiler.compile(DEFAULT_SCORE);
            } else {
                funcExpression = JavascriptCompiler.compile(scoreFunction);
            }
            SimpleBindings bindings = new SimpleBindings();
            bindings.add(new SortField(INDEX_INTERNAL_SCORE_FIELD, SortField.Type.SCORE));
            for (String scoreParam : scoreParams) {
                bindings.add(new SortField(scoreParam, SortField.Type.DOUBLE));
            }
            return funcExpression.getValueSource(bindings);
        } catch (ParseException e) {
            throw new AnalyticsIndexException("Error while evaluating the score function:" +
                                              e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new AnalyticsIndexException("Error while evaluating the score function: "
                                              + e.getMessage(), e);
        }
    }

    public List<SearchResultEntry> getDrillDownRecords(int tenantId,
            AnalyticsDrillDownRequest drillDownRequest, String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        int startIndex = drillDownRequest.getRecordStartIndex();
        if (startIndex < 0 ) throw new AnalyticsIndexException("Start index should be greater than 0");
        int endIndex = startIndex + drillDownRequest.getRecordCount();
        if (endIndex <= startIndex) throw new AnalyticsIndexException("Record Count should be greater than 0");
        String tableName = drillDownRequest.getTableName();
        List<String> taxonomyShardIds = this.lookupGloballyExistingShardIds(TAXONOMY_INDEX_DATA_FS_BASE_PATH,
                                                                            tenantId,tableName);
        List<SearchResultEntry> resultFacetList = new ArrayList<>();
        for (String shardId : taxonomyShardIds) {
            resultFacetList.addAll(this.drillDownRecordsPerShard(tenantId, shardId, drillDownRequest, rangeField, range));
        }
        Collections.sort(resultFacetList);
        if (resultFacetList.size() < startIndex) {
            return new ArrayList<>();
        }
        if (resultFacetList.size() < endIndex) {
            return resultFacetList.subList(startIndex, resultFacetList.size());
        }
        return resultFacetList.subList(startIndex, endIndex);
    }

    private List<CategorySearchResultEntry> getDrillDownCategories(int tenantId,
                   CategoryDrillDownRequest drillDownRequest) throws AnalyticsIndexException {
        List<String> taxonomyShardIds = this.lookupGloballyExistingShardIds(TAXONOMY_INDEX_DATA_FS_BASE_PATH,
                                                                            tenantId,drillDownRequest.getTableName());
        List<CategorySearchResultEntry> categoriesPerShard = new ArrayList<>();
        for (String shardId : taxonomyShardIds) {
            categoriesPerShard.addAll(this.drillDownCategoriesPerShard(tenantId, shardId, drillDownRequest));
        }
        return categoriesPerShard;
    }

    public int getDrillDownRecordCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                         String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        String tableName = drillDownRequest.getTableName();
        List<String> taxonomyShardIds = this.lookupGloballyExistingShardIds(TAXONOMY_INDEX_DATA_FS_BASE_PATH,
                                                                            tenantId,tableName);
        int totalCount = 0;
        for (String shardId : taxonomyShardIds) {
            totalCount += this.getDrillDownRecordCountPerShard(tenantId, shardId, drillDownRequest, rangeField, range);
        }
        return totalCount;
    }

    private List<SearchResultEntry> drillDownRecordsPerShard(int tenantId,
                                                             String shardId,
                                                             AnalyticsDrillDownRequest drillDownRequest,
                                                             String rangeField,
                                                             AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        String tableName = drillDownRequest.getTableName();
        String shardedTableId = this.generateShardedTableId(tenantId, tableName, shardId);
        Directory indexDir = this.lookupIndexDir(shardedTableId);
        Directory taxonomyDir = this.lookupTaxonomyIndexDir(shardedTableId);
        return this.drillDownRecords(tenantId, drillDownRequest, indexDir, taxonomyDir, rangeField, range);
    }

    private List<CategorySearchResultEntry> drillDownCategoriesPerShard(int tenantId,String shardId,
                                                             CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        String shardedTableId = this.generateShardedTableId(tenantId, drillDownRequest.getTableName(), shardId);
        Directory indexDir = this.lookupIndexDir(shardedTableId);
        Directory taxonomyDir = this.lookupTaxonomyIndexDir(shardedTableId);
        return this.drillDownCategories(tenantId, indexDir, taxonomyDir, drillDownRequest);
    }

    private int getDrillDownRecordCountPerShard(int tenantId,
                                                             String shardId,
                                                             AnalyticsDrillDownRequest drillDownRequest,
                                                             String rangeField,
                                                             AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {

        String tableName = drillDownRequest.getTableName();
        String shardedTableId = this.generateShardedTableId(tenantId, tableName, shardId);
        Directory indexDir = this.lookupIndexDir(shardedTableId);
        Directory taxonomyDir = this.lookupTaxonomyIndexDir(shardedTableId);
        return this.getDrillDownRecordCount(tenantId, drillDownRequest, indexDir, taxonomyDir, rangeField, range);
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
    * @param ids The ids of the records to be deleted
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
        TaxonomyWriter taxonomyWriter = this.createTaxonomyIndexWriter(shardedTableId);
        try {
            for (Record record : recordBatch) {
                indexWriter.updateDocument(new Term(INDEX_ID_INTERNAL_FIELD, record.getId()),
                                           this.generateIndexDoc(record, columns, taxonomyWriter).getFields());
            }
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in updating index: " + e.getMessage(), e);
        } finally {
            try {
                indexWriter.close();
                taxonomyWriter.close();
            } catch (IOException e) {
                log.error("Error closing index writer: " + e.getMessage(), e);
            }
        }
    }
    
    private void checkAndAddDocEntry(Document doc, IndexType type, String name, Object obj)
            throws AnalyticsIndexException {
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
        default:
            break;
        }
    }

    private void checkAndAddTaxonomyDocEntries(Document doc, IndexType type,
                                                   String name, Object obj,
                                                   FacetsConfig facetsConfig)
            throws AnalyticsIndexException {
        if (obj == null) {
            doc.add(new StringField(name, NULL_INDEX_VALUE, Store.NO));
        }
        if (obj instanceof AnalyticsCategoryPath && type == IndexType.FACET) {
            facetsConfig.setMultiValued(name, true);
            facetsConfig.setHierarchical(name, true);
            AnalyticsCategoryPath analyticsCategoryPath = (AnalyticsCategoryPath) obj;
            //the field name for dimensions will be "$ + {name}"
            //      //facetsConfig.setIndexFieldName(name, new StringBuilder("$").append(name).toString());
            doc.add(new FacetField(name, analyticsCategoryPath.getPath()));
        }
    }

    private Document generateIndexDoc(Record record, Map<String, IndexType> columns,
                   TaxonomyWriter taxonomyWriter) throws AnalyticsIndexException, IOException {
        Document doc = new Document();
        FacetsConfig config = new FacetsConfig();
        doc.add(new StringField(INDEX_ID_INTERNAL_FIELD, record.getId(), Store.YES));
        doc.add(new LongField(INDEX_INTERNAL_TIMESTAMP_FIELD, record.getTimestamp(), Store.NO));
        /* make the best effort to store in the given timestamp, or else, 
         * fall back to a compatible format, or else, lastly, string */
        String name;
        for (Map.Entry<String, IndexType> entry : columns.entrySet()) {
            name = entry.getKey();
            this.checkAndAddDocEntry(doc, entry.getValue(), name, record.getValue(name));
            this.checkAndAddTaxonomyDocEntries(doc, entry.getValue(), name, record.getValue(name), config);
        }
        return config.build(taxonomyWriter, doc);
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
        if (columns.contains(INDEX_INTERNAL_SCORE_FIELD)) {
            throw new AnalyticsIndexException("The column index '" + INDEX_INTERNAL_SCORE_FIELD +
                    "' is a reserved name");
        }
        if (columns.contains(INDEX_INTERNAL_WEIGHT_FIELD)) {
            throw new AnalyticsIndexException("The column index '" + INDEX_INTERNAL_WEIGHT_FIELD +
                    "' is a reserved name");
        }
    }

    private void checkInvalidScoreParams(List<String> scoreParams, Map<String, IndexType> columns)
            throws AnalyticsIndexException {
        for (String scoreParam : scoreParams) {
            IndexType type = columns.get(scoreParam);
            if (type != IndexType.DOUBLE && type != IndexType.FLOAT && type != IndexType.INTEGER
                && type != IndexType.LONG) {
                throw new AnalyticsIndexException("'" + scoreParam +
                                                  "' is not indexed as a numeric column");
            }
        }
    }
    
    public void setIndices(int tenantId, String tableName, Map<String, IndexType> columns,
                           List<String> scoreParams) throws AnalyticsIndexException {
        this.checkInvalidIndexNames(columns.keySet());
        String tableId = this.generateTableId(tenantId, tableName);
        this.indexDefs.put(tableId, columns);
        this.getRepository().setIndices(tenantId, tableName, columns);
        this.setScoreParams(tenantId, tableName, scoreParams, columns);
        this.notifyClusterIndexChange(tenantId, tableName);
    }

    public void setIndices(int tenantId, String tableName, Map<String, IndexType> columns)
            throws AnalyticsIndexException {
        this.checkInvalidIndexNames(columns.keySet());
        String tableId = this.generateTableId(tenantId, tableName);
        this.indexDefs.put(tableId, columns);
        this.getRepository().setIndices(tenantId, tableName, columns);
        this.setScoreParams(tenantId, tableName, new ArrayList<String>(0), columns);
        this.notifyClusterIndexChange(tenantId, tableName);
    }

    public void setScoreParams(int tenantId, String tableName, List<String> scoreParams,
                               Map<String, IndexType> columns) throws AnalyticsIndexException {
        if (scoreParams != null) {
            this.checkInvalidScoreParams(scoreParams, columns);
            String tableId = this.generateTableId(tenantId, tableName);
            this.scoreParams.put(tableId, scoreParams);
            this.getRepository().setScoreParams(tenantId, tableName, scoreParams);
        }
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

    public List<String> lookupScoreParams(int tenantId, String tableName) throws AnalyticsIndexException {
        String tableId = this.generateTableId(tenantId, tableName);
        List<String> scoreParams = this.scoreParams.get(tableId);
        if (scoreParams == null) {
            scoreParams = this.getRepository().getScoreParams(tenantId, tableName);
            this.scoreParams.put(tableId, scoreParams);
        }
        return scoreParams;
    }
    
    private String generateDirPath(String tableId) {
        return INDEX_DATA_FS_BASE_PATH + tableId;
    }

    private String generateDirPath(String basePath, String tableId) {
        return basePath + tableId;
    }

    private Directory createDirectory(String tableId) throws AnalyticsIndexException {
        String path = this.generateDirPath(tableId);
        try {
            return new AnalyticsDirectory(this.getFileSystem(), new SingleInstanceLockFactory(), path);
        } catch (AnalyticsException e) {
            throw new AnalyticsIndexException("Error in creating directory: " + e.getMessage(), e);
        }
    }
    private Directory createDirectory(String basePath, String tableId) throws
                                                                       AnalyticsIndexException {
        String path = this.generateDirPath(basePath, tableId);
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

    private Directory lookupTaxonomyIndexDir(String tableId) throws AnalyticsIndexException {
        Directory indexTaxonomyDir = this.indexTaxonomyDirs.get(tableId);
        if (indexTaxonomyDir == null) {
            synchronized (this.indexTaxonomyDirs) {
                indexTaxonomyDir = this.indexTaxonomyDirs.get(tableId);
                if (indexTaxonomyDir == null) {
                    indexTaxonomyDir = this.createDirectory(TAXONOMY_INDEX_DATA_FS_BASE_PATH, tableId);
                    this.indexTaxonomyDirs.put(tableId, indexTaxonomyDir);
                }
            }
        }
        return indexTaxonomyDir;
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

    private TaxonomyWriter createTaxonomyIndexWriter(String tableId) throws AnalyticsIndexException {
        Directory indexDir = this.lookupTaxonomyIndexDir(tableId);
        try {
            return new DirectoryTaxonomyWriter(indexDir, IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in creating index writer: " + e.getMessage(), e);
        }
    }
    
    private void deleteIndexData(int tenantId, String tableName) throws AnalyticsIndexException {
        List<String> shardIds = this.lookupGloballyExistingShardIds(INDEX_DATA_FS_BASE_PATH, tenantId, tableName);
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
        this.scoreParams.remove(tableId);
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
        this.scoreParams.remove(tableId);
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
            this.shardWorkerExecutor.shutdown();
            for (IndexWorker worker : this.workers) {
                worker.stop();
            }
            try {
                if (!this.shardWorkerExecutor.awaitTermination(INDEX_WORKER_STOP_WAIT_TIME, TimeUnit.SECONDS)) {
                    this.shardWorkerExecutor.shutdownNow();
                }
            } catch (InterruptedException ignore) {
                /* ignore */
            }
            this.workers = null;
            this.shardWorkerExecutor = null;
        }
    }

    public void close() throws AnalyticsIndexException {
        this.stopAndCleanupIndexProcessing();
        this.closeAndRemoveIndexDirs(new HashSet<String>(this.indexDirs.keySet()));
        this.indexDefs.clear();
        this.scoreParams.clear();
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
        
        public IndexOperation() { }
        
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
        
        private boolean stop;
        
        public IndexWorker(int shardIndex) {
            this.shardIndex = shardIndex;
        }
        
        public int getShardIndex() {
            return shardIndex;
        }
        
        public void stop() {
            this.stop = true;
        }
        
        @Override
        public void run() {
            while (!this.stop) {
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
