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
package org.wso2.carbon.analytics.dataservice.core.indexing;

import com.hazelcast.spi.exception.TargetNotMemberException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.SortedDocValuesField;
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
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumValueSource;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexOptions;
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
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.wso2.carbon.analytics.dataservice.commons.AggregateField;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownResponse;
import org.wso2.carbon.analytics.dataservice.commons.CategorySearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.Constants;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsQueryParser;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterException;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.core.indexing.LocalIndexDataStore.IndexOperation;
import org.wso2.carbon.analytics.dataservice.core.indexing.LocalIndexDataStore.LocalIndexDataQueue;
import org.wso2.carbon.analytics.dataservice.core.indexing.aggregates.AggregateFunction;
import org.wso2.carbon.analytics.dataservice.core.indexing.aggregates.AggregateFunctionFactory;
import org.wso2.carbon.analytics.dataservice.core.indexing.aggregates.RecordContext;
import org.wso2.carbon.analytics.dataservice.core.indexing.sort.RecordSortUtils;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the indexing functionality.
 */
public class AnalyticsDataIndexer {

    private static final Log log = LogFactory.getLog(AnalyticsDataIndexer.class);
    
    private static final int MAX_NON_TOKENIZED_INDEX_STRING_SIZE = 1000;
    
    private static final String ENABLE_INDEXING_STATS_SYS_PROP = "enableIndexingStats";
    
    private static final String INDEX_DATA_FS_BASE_PATH = File.separator + "_data" + 
            File.separator + "index" + File.separator;

    private static final String TAXONOMY_INDEX_DATA_FS_BASE_PATH = File.separator + "_data" + 
            File.separator + "taxonomy" + File.separator;

    public static final String INDEX_ID_INTERNAL_FIELD = "_id";

    public static final String INDEX_INTERNAL_TIMESTAMP_FIELD = "_timestamp";

    private static final String INDEX_INTERNAL_SCORE_FIELD = "_score";

    public static final String NULL_INDEX_VALUE = "";

    private static final String EMPTY_FACET_VALUE = "EMPTY_FACET_VALUE!";

    private static final String DEFAULT_SCORE = "1";
    
    public static final int WORKER_TIMEOUT = 60;

    public static final int REINDEX_THREAD_COUNT = 5;

    public static final int REINDEX_QUEUE_LIMIT = 100;

    private static final String LUCENE_QUERY_FOR_AGGREGATION = "luceneQuery";

    private static final String NO_OF_RECORDS = "noOfRecords";

    private Map<String, IndexWriter> indexWriters = new HashMap<>();

    private Map<String, DirectoryTaxonomyWriter> indexTaxonomyWriters = new HashMap<>();

    private AggregateFunctionFactory aggregateFunctionFactory;
    
    private ExecutorService shardWorkerExecutor, reIndexWorkerExecutor;
    
    private List<IndexWorker> workers;

    private AnalyticsIndexerInfo indexerInfo;
    
    private IndexNodeCoordinator indexNodeCoordinator;
    
    private LocalIndexDataStore localIndexDataStore;
    
    private Set<Integer> localShards = new HashSet<>();
    
    private boolean indexingStatsEnabled;
    
    private AnalyticsDataIndexingStatsCollector statsCollector;
        
    public AnalyticsDataIndexer(AnalyticsIndexerInfo indexerInfo) throws AnalyticsException {
    	this.indexerInfo = indexerInfo;
    }
    
    /**
     * This method initializes the indexer, and must be called before any other operation in this class is called.
     * @throws AnalyticsException
     */
    public void init() throws AnalyticsException {
        if (System.getProperty(ENABLE_INDEXING_STATS_SYS_PROP) != null) {
            this.indexingStatsEnabled = true;
        }
        if (this.indexingStatsEnabled) {
            this.statsCollector = new AnalyticsDataIndexingStatsCollector();
        }
        this.getAnalyticsRecordStore().createTable(org.wso2.carbon.analytics.dataservice.core.Constants.META_INFO_TENANT_ID, 
                org.wso2.carbon.analytics.dataservice.core.Constants.GLOBAL_SHARD_ALLOCATION_CONFIG_TABLE);
        this.localIndexDataStore = new LocalIndexDataStore(this);
        this.indexNodeCoordinator = new IndexNodeCoordinator(this);
        this.indexNodeCoordinator.init();
    }
    
    public boolean isIndexingStatsEnabled() {
        return indexingStatsEnabled;
    }
    
    public int getReplicationFactor() {
        return this.indexerInfo.getIndexReplicationFactor();
    }
    
    public IndexNodeCoordinator getIndexNodeCoordinator() {
        return indexNodeCoordinator;
    }
    
    public AnalyticsIndexerInfo getAnalyticsIndexerInfo() {
        return indexerInfo;
    }
    
    public AnalyticsDataService getAnalyticsDataService() {
        return this.indexerInfo.getAnalyticsDataService();
    }
    
    public AnalyticsRecordStore getAnalyticsRecordStore() {
        return this.indexerInfo.getAnalyticsRecordStore();
    }
    
    public int getShardCount() {
        return this.indexerInfo.getShardCount();
    }
    
    public long getShardIndexRecordBatchSize() {
        return this.indexerInfo.getShardIndexRecordBatchSize();
    }
    
    public int getShardIndexWorkerInterval() {
        return this.indexerInfo.getShardIndexWorkerInterval();
    }
    
    /* processIndexOperations and processIndexOperationsFlushQueue must be synchronized, they are accessed by
     * indexer threads and wait for indexing tasks, if not done property, index corruption will happen */
    private synchronized void processIndexOperations(int shardIndex) throws AnalyticsException {
        long maxBatchSize = this.getShardIndexRecordBatchSize();
        long tmpSize;
        /* process until the queue has sizable amount of records left in it, or else, go back to the
         * indexing thread and wait for more to fill up */
        do {
            tmpSize = this.processLocalShardDataQueue(shardIndex, 
                    this.localIndexDataStore.getIndexDataQueue(shardIndex), maxBatchSize)[1];
        } while (tmpSize >= maxBatchSize);
    }
    
    /* processIndexOperations and processIndexOperationsFlushQueue must be synchronized */
    public synchronized void processIndexOperationsFlushQueue(int shardIndex) throws AnalyticsException {
        long maxBatchCount = this.getShardIndexRecordBatchSize();
        LocalIndexDataQueue queue = this.localIndexDataStore.getIndexDataQueue(shardIndex);
        long queueSizeAtStart = queue.size();
        long processedCount = 0, tmpCount;
        do {
            tmpCount = this.processLocalShardDataQueue(shardIndex, queue, maxBatchCount)[0];
            if (tmpCount == 0) {
                /* nothing left in the queue, time to leave */
                break;
            }
            processedCount += tmpCount;
        } while (processedCount < queueSizeAtStart);
    }
    
    private long[] processLocalShardDataQueue(int shardIndex, LocalIndexDataQueue dataQueue, 
            long maxSize) throws AnalyticsException {
        if (dataQueue == null) {
            return new long[] { 0, 0 };
        }
        long entriesProcessed = 0;
        long bytesProcessed = 0;
        boolean delete = false;
        int deleteTenantId = 0;
        String deleteTableName = null;
        IndexOperation indexOp;
        List<IndexOperation> indexOps = new ArrayList<>();
        try {
            dataQueue.startDequeue();
            while (!dataQueue.isEmpty()) {
                indexOp = dataQueue.peekNext();
                if (log.isDebugEnabled()) {
                    log.debug("Local index entry dequeue [" + shardIndex + "]");
                }
                if (indexOp.isDelete() != delete) {
                    this.processIndexOperationBatch(shardIndex, indexOps);
                    delete = indexOp.isDelete();
                    deleteTenantId = indexOp.getDeleteTenantId();
                    deleteTableName = indexOp.getDeleteTableName();
                } else if (delete) {
                    if (!(indexOp.getDeleteTenantId() == deleteTenantId && indexOp.getDeleteTableName().equals(deleteTableName))) {
                        this.processIndexOperationBatch(shardIndex, indexOps);
                        delete = indexOp.isDelete();
                        deleteTenantId = indexOp.getDeleteTenantId();
                        deleteTableName = indexOp.getDeleteTableName();
                    }
                }
                indexOps.add(indexOp);
                entriesProcessed++;
                bytesProcessed += indexOp.getByteSize();
                if (bytesProcessed >= maxSize) {
                    break;
                }
            }
            this.processIndexOperationBatch(shardIndex, indexOps);
            if (log.isDebugEnabled()) {
                if (bytesProcessed > 0) {
                    log.debug("Processed " + bytesProcessed + " bytes of batched index data");
                }
            }
            return new long[] { entriesProcessed, bytesProcessed };
        } finally {
            /* Even if there is an error, we should dequeue the peeked records, or else,
             * for errors like a target table couldn't be found anymore, the same records
             * in the queue will cycle forever. This setup is specifically done for server
             * crashes, where in the middle of the earlier loop, if it exists, the peeked
             * records will not be lost. */
            dataQueue.endDequeue();
        }
    }
    
    private IndexOperation mergeOps(List<IndexOperation> ops) {
        if (ops.isEmpty()) {
            return null;
        }
        IndexOperation result = ops.get(0);
        for (int i = 1; i < ops.size(); i++) {
            if (result.isDelete()) {
                result.getIds().addAll(ops.get(i).getIds());
            } else {
                result.getRecords().addAll(ops.get(i).getRecords());
            }
        }
        return result;
    }
    
    private void processIndexOperationBatch(int shardIndex, List<IndexOperation> indexOps) throws AnalyticsException {
        IndexOperation indexOp = this.mergeOps(indexOps);
        if (indexOp != null) {
            if (indexOp.isDelete()) {
                this.deleteInIndex(indexOp.getDeleteTenantId(), indexOp.getDeleteTableName(), 
                        shardIndex, indexOp.getIds());
            } else {
                Collection<List<Record>> recordBatches = GenericUtils.generateRecordBatches(indexOp.getRecords());
                int tenantId;
                String tableName;
                for (List<Record> recordBatch : recordBatches) {
                    tenantId = recordBatch.get(0).getTenantId();
                    tableName = recordBatch.get(0).getTableName();
                    this.updateIndex(shardIndex, recordBatch, this.lookupIndices(tenantId, tableName));
                }
            }
        }
        indexOps.clear();
    }
    
    public Set<Integer> getLocalShards() {
        return localShards;
    }
    
    public void refreshLocalIndexShards(Set<Integer> localShards) throws AnalyticsException {
        this.localShards = localShards;
        if (IndexNodeCoordinator.checkIfIndexingNode()) {
            this.localIndexDataStore.refreshLocalIndexShards();
            this.reschuduleWorkers();
        }
    }
    
    private void reschuduleWorkers() throws AnalyticsException {
        this.stopAndCleanupIndexProcessing();
        this.workers = new ArrayList<>(this.localShards.size());
        if (this.localShards.size() == 0) {
            return;
        }
        this.shardWorkerExecutor = Executors.newFixedThreadPool(this.localShards.size());
        for (int shardIndex : this.localShards) {
            IndexWorker worker = new IndexWorker(shardIndex);
            this.workers.add(worker);
            this.shardWorkerExecutor.execute(worker);
        }
    }
    
    public int calculateShardId(String id) {
        return Math.abs(id.hashCode()) % this.getShardCount();
    }
    
    private List<Integer> lookupGloballyExistingShardIds()
            throws AnalyticsIndexException {
        List<Integer> result = new ArrayList<>(this.localShards.size());
        for (int shardIndex : this.localShards) {
            result.add(shardIndex);
        }
        return result;
    }
    
    public List<SearchResultEntry> search(final int tenantId, final String tableName, final String query,
            final int start, final int count, List<SortByField> sortByFields) throws AnalyticsException {
        List<SearchResultEntry> result;
        if (this.isClusteringEnabled()) {
            //get all the records from 0th to the "start + count" th record from all the nodes, then sort, reverse and paginate
            List<List<SearchResultEntry>> entries = this.executeIndexLookup(new SearchCall(tenantId,
                    tableName, query, 0, start + count, sortByFields));
            result = new ArrayList<>();
            for (List<SearchResultEntry> entry : entries) {
                result.addAll(entry);
            }
            Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId, tableName);
            result = RecordSortUtils.getSortedSearchResultEntries(tenantId, tableName, sortByFields,
                                                                  indices, this.getAnalyticsDataService(), result);
            int toIndex = start + count;
            if (toIndex >= result.size()) {
                toIndex = result.size();
            }
            if (start < result.size()) {
                result = new ArrayList<>(result.subList(start, toIndex));
            } else {
                result = new ArrayList<>(0);
            }
        } else {
            result = this.doSearch(this.localShards, tenantId, tableName, query, start, count, sortByFields);
        }
        if (log.isDebugEnabled()) {
            log.debug("Search [" + query + "]: " + result.size());
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private List<SearchResultEntry> doSearch(Set<Integer> shardIndices, int tenantId, String tableName,
                                             String query, int start, int count, List<SortByField> sortByFields)
            throws AnalyticsIndexException {
        List<SearchResultEntry> results = new ArrayList<>();
        IndexReader reader = null;
        ExecutorService searchExecutor = Executors.newCachedThreadPool();
        if (count <= 0) {
            log.error("Record Count/Page size is ZERO!. Please set Record count/Page size.");
        }
        try {
            reader = this.getCombinedIndexReader(shardIndices, tenantId, tableName);
            IndexSearcher searcher = new IndexSearcher(reader, searchExecutor);
            Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId, tableName);
            Query indexQuery = getSearchQueryFromString(query, indices);
            TopDocsCollector collector = getTopDocsCollector(start, count, sortByFields, indices);
            searcher.search(indexQuery, collector);
            ScoreDoc[] hits = collector.topDocs(start).scoreDocs;
            Document indexDoc;
            for (ScoreDoc doc : hits) {
                indexDoc = searcher.doc(doc.doc);
                results.add(new SearchResultEntry(indexDoc.get(INDEX_ID_INTERNAL_FIELD), doc.score));
            }
            if (log.isDebugEnabled()) {
                log.debug("Local Search: " + results.size());
            }
            return results;
        } catch (Exception e) {
            log.error("Error in index search: " + e.getMessage(), e);
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

    private Query getSearchQueryFromString(String query, Map<String, ColumnDefinition> indices)
            throws org.apache.lucene.queryparser.classic.ParseException, AnalyticsIndexException {
        Analyzer analyzer = getPerFieldAnalyzerWrapper(indices);
        String validatedQuery = getValidatedLuceneQuery(query);
        return new AnalyticsQueryParser(analyzer, indices).parse(validatedQuery);
    }

    private String getValidatedLuceneQuery(String query) {
        String validatedQuery;
        if (query == null || query.isEmpty()) {
            validatedQuery = "*:*";
            log.info("Lucene filtering query is not given, So matching all values.");
        } else {
            validatedQuery = query;
        }
        return validatedQuery;
    }

    @SuppressWarnings("rawtypes")
    private TopDocsCollector getTopDocsCollector(int start, int count, List<SortByField> sortByFields,
                                                 Map<String, ColumnDefinition> indices)
            throws AnalyticsException {
        TopDocsCollector collector;
        try {
            if (sortByFields != null && !sortByFields.isEmpty()) {
                SortField[] sortFields = createSortFields(sortByFields, indices);
                collector = TopFieldCollector.create(new Sort(sortFields), start + count, false, true, false);
            } else {
                collector = TopScoreDocCollector.create(start + count);
            }
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error while creating TopFieldCollector: " + e.getMessage(), e);
        }
        return collector;
    }

    private SortField[] createSortFields(List<SortByField> sortByFields, Map<String, ColumnDefinition> indices)
            throws AnalyticsIndexException {
        List<SortField> sortFields = new ArrayList<>();
        for (SortByField sortByField : sortByFields) {
            SortField sortField;
            String fieldName = sortByField.getFieldName();
            ColumnDefinition columnDefinition = indices.get(fieldName);
            switch (sortByField.getSort()) {
                case ASC: {
                    sortField = new SortField(fieldName, getSortFieldType(fieldName, indices));
                    break;
                }
                case DESC: {
                    sortField = new SortField(fieldName, getSortFieldType(fieldName, indices), true);
                    break;
                }
                default:
                    throw new AnalyticsIndexException("Error while processing Sorting fields: " +
                                                 sortByField.getSort().toString() + " unsupported sortType");
            }
            sortFields.add(sortField);
        }
        return sortFields.toArray(new SortField[sortFields.size()]);
    }

    private SortField.Type getSortFieldType(String fieldName,
                                            Map<String, ColumnDefinition> indices)
            throws AnalyticsIndexException {
        ColumnDefinition columnDefinition = indices.get(fieldName);
        SortField.Type type;
        if (columnDefinition == null) {
            if (fieldName != null && fieldName.equals(INDEX_INTERNAL_TIMESTAMP_FIELD)) {
                type = SortField.Type.LONG;
            } else {
                throw new AnalyticsIndexException("Cannot find index information for field: " + fieldName);
            }
        } else {
            switch (columnDefinition.getType()) {
                case STRING:
                    type = SortField.Type.STRING;
                    break;
                case INTEGER:
                    type = SortField.Type.INT;
                    break;
                case LONG:
                    type = SortField.Type.LONG;
                    break;
                case FLOAT:
                    type = SortField.Type.FLOAT;
                    break;
                case DOUBLE:
                    type = SortField.Type.DOUBLE;
                    break;
                default:
                    throw new AnalyticsIndexException("Error while determining the type of the column: " +
                            fieldName + ", " + columnDefinition.getType().toString() + " not supported");
            }
        }
        return type;
    }

    private Analyzer getPerFieldAnalyzerWrapper(Map<String, ColumnDefinition> indices)
            throws AnalyticsIndexException {
        Analyzer perFieldAnalyzerWrapper;
        Map<String, Analyzer> analyzersPerField = new HashMap<>();
        for (Map.Entry<String, ColumnDefinition> index : indices.entrySet()) {
            if (index.getValue().getType() == AnalyticsSchema.ColumnType.STRING) {
                analyzersPerField.put(Constants.NON_TOKENIZED_FIELD_PREFIX + index.getKey(), new KeywordAnalyzer());
            }
        }
        if (analyzersPerField.isEmpty()) {
            perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(this.indexerInfo.getLuceneAnalyzer());
        } else {
            perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(this.indexerInfo.getLuceneAnalyzer(), 
                    analyzersPerField);
        }
        return perFieldAnalyzerWrapper;
    }
    
    private boolean isClusteringEnabled() {
        return AnalyticsServiceHolder.getAnalyticsClusterManager().isClusteringEnabled();
    }
    
    private Map<Object, Set<Integer>> generateMemberShardMappingForIndexLookup() throws AnalyticsIndexException {
        return this.indexNodeCoordinator.generateMemberShardMappingForIndexLookup();
    }
    
    private <T> List<T> executeIndexLookup(IndexLookupOperationCall<T> call) throws AnalyticsIndexException {
        try {
            return this.executeIndexLookupDirect(call);
        } catch (TargetNotMemberException e) {
            log.warn("Target member not available for index lookup, refreshing index shard info...");
            try {
                this.indexNodeCoordinator.refreshIndexShardInfo();
            } catch (AnalyticsException ex) {
                log.warn("Error in refreshing shard info in execute index lookup: " + ex.getMessage(), ex);
            }
            return this.executeIndexLookupDirect(call);
        }
    }
    
    private <T> List<T> executeIndexLookupDirect(IndexLookupOperationCall<T> call) throws AnalyticsIndexException {
        Map<Object, Set<Integer>> target = this.generateMemberShardMappingForIndexLookup();
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        List<T> result = new ArrayList<>();
        List<Future<T>> futures = new ArrayList<>();
        try {
            IndexLookupOperationCall<T> copyCall;
            for (Map.Entry<Object, Set<Integer>> entry : target.entrySet()) {
                copyCall = (IndexLookupOperationCall<T>) call.copy();
                copyCall.setShardIndices(entry.getValue());
                futures.add(acm.executeOneFuture(org.wso2.carbon.analytics.dataservice.core.Constants.
                        ANALYTICS_INDEXING_GROUP, entry.getKey(), copyCall));
            }
            for (Future<T> future : futures) {
                result.add(future.get());
            }
        } catch (AnalyticsClusterException | ExecutionException | InterruptedException e) {
            throw new AnalyticsIndexException("Error in executing cluster index lookup: " + e.getMessage(), e);
        }
        return result;
    }

    public int searchCount(final int tenantId, final String tableName, final String query) 
            throws AnalyticsIndexException {
        int result;
        if (this.isClusteringEnabled()) {
            List<Integer> counts = this.executeIndexLookup(new SearchCountCall(tenantId, tableName, query));
            result = 0;
            for (int count : counts) {
                result += count;
            }
        } else {
            result = doSearchCount(this.localShards, tenantId, tableName, query);
        }
        if (log.isDebugEnabled()) {
            log.debug("Search Count: " + result);
        }
        return result;
    }
    
    private int doSearchCount(Set<Integer> shardIds, int tenantId, String tableName,
            String query) throws AnalyticsIndexException {
        IndexReader reader = null;
        try {
            reader = this.getCombinedIndexReader(shardIds, tenantId, tableName);
            IndexSearcher searcher = new IndexSearcher(reader);
            Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId, tableName);
            Analyzer analyzer = getPerFieldAnalyzerWrapper(indices);
            String validatedQuery;
            if (query == null || query.isEmpty()) {
                log.warn("Lucene filter query is not given. So matching all values.");
                validatedQuery = "*:*";
            } else {
                validatedQuery = query;
            }
            Query indexQuery = new AnalyticsQueryParser(analyzer, indices).parse(validatedQuery);
            TotalHitCountCollector collector = new TotalHitCountCollector();
            searcher.search(indexQuery, collector);
            int result = collector.getTotalHits();
            if (log.isDebugEnabled()) {
                log.debug("Local Search Count: " + result);
            }
            return result;
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



    public List<AnalyticsDrillDownRange> drillDownRangeCount(final int tenantId,
            final AnalyticsDrillDownRequest drillDownRequest) throws AnalyticsIndexException {
        if (drillDownRequest.getRangeField() == null) {
            throw new AnalyticsIndexException("Rangefield is not set");
        }
        if (drillDownRequest.getRanges() == null) {
            throw new AnalyticsIndexException("Ranges are not set");
        }
        List<AnalyticsDrillDownRange> finalResult;
        Map<String, AnalyticsDrillDownRange> entryMap;
        if (this.isClusteringEnabled()) {
            List<List<AnalyticsDrillDownRange>> entries =
                        this.executeIndexLookup(new DrillDownRangeCountCall(tenantId, drillDownRequest));
            finalResult = new ArrayList<>();
            entryMap = new LinkedHashMap<>();
            for (List<AnalyticsDrillDownRange> entry : entries) {
                for (AnalyticsDrillDownRange range : entry) {
                    if (entryMap.get(range.getLabel())== null) {
                        entryMap.put(range.getLabel(), range);
                    } else {
                        AnalyticsDrillDownRange newRange = entryMap.get(range.getLabel());
                        entryMap.put(range.getLabel(), new AnalyticsDrillDownRange(range.getLabel(),
                                                                                   range.getFrom(), range.getTo(), range.getScore() + newRange.getScore()));
                    }
                }
            }
            finalResult.addAll(entryMap.values());
        } else {
            IndexReader indexReader = null;
            try {
                indexReader = getCombinedIndexReader(this.localShards, tenantId, drillDownRequest.getTableName());
                finalResult = getAnalyticsDrillDownRanges(tenantId, drillDownRequest);
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
        return finalResult;
    }

    /*public List<AnalyticsDrillDownRange> getAnalyticsDrillDownRanges(int tenantId,
                                                                      AnalyticsDrillDownRequest drillDownRequest,
                                                                      IndexReader indexReader)
            throws AnalyticsIndexException, org.apache.lucene.queryparser.classic.ParseException,
                   IOException {
        IndexSearcher searcher = new IndexSearcher(indexReader);
        List<AnalyticsDrillDownRange> drillDownRanges = new ArrayList<>();
        drillDownRanges.addAll(drillDownRequest.getRanges());
        Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId, drillDownRequest.getTableName());
        Query indexQuery = new MatchAllDocsQuery();
        FacetsCollector fc = new FacetsCollector();
        if (drillDownRequest.getQuery() != null && !drillDownRequest.getQuery().isEmpty()) {
            Analyzer analyzer = getPerFieldAnalyzerWrapper(indices);
            indexQuery = new AnalyticsQueryParser(analyzer,
                                                  indices).parse(drillDownRequest.getQuery());
        }
        FacetsCollector.search(searcher, indexQuery, Integer.MAX_VALUE, fc);
        DoubleRange[] ranges = this.createRangeBuckets(drillDownRanges);
        ValueSource valueSource = this.getCompiledScoreFunction(drillDownRequest.getScoreFunction(), indices);
        Facets facets = null;
        if (indices.keySet().contains(drillDownRequest.getRangeField())) {
            if (indices.get(drillDownRequest.getRangeField()).isScoreParam()) {
                facets = new DoubleRangeFacetCounts(drillDownRequest.getRangeField(), valueSource, fc, ranges);
            }
        }
        if (facets == null) {
            facets = new DoubleRangeFacetCounts(drillDownRequest.getRangeField(), fc, ranges);
        }
        FacetResult facetResult = facets.getTopChildren(Integer.MAX_VALUE, drillDownRequest.getRangeField());
        for (int i = 0; i < drillDownRanges.size(); i++) {
            AnalyticsDrillDownRange range = drillDownRanges.get(i);
            range.setScore(facetResult.labelValues[i].value.doubleValue());
        }
        return drillDownRanges;
    }*/

    public List<AnalyticsDrillDownRange> getAnalyticsDrillDownRanges(int tenantId,
                                                                     AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException, org.apache.lucene.queryparser.classic.ParseException,
                   IOException {
        Map<String, AnalyticsDrillDownRange> drillDownRanges = new LinkedHashMap<>();
        String rangeField = drillDownRequest.getRangeField();
        for (int shard : this.getLocalShards()) {
            for (AnalyticsDrillDownRange range : drillDownRequest.getRanges()) {
                double score = this.getDrillDownRecordCountPerShard(tenantId, shard, drillDownRequest, rangeField, range);
                if (drillDownRanges.get(range.getLabel()) == null) {
                    drillDownRanges.put(range.getLabel(), new AnalyticsDrillDownRange(range.getLabel(),
                        range.getFrom(), range.getTo(), score));
                } else {
                    AnalyticsDrillDownRange oldRange = drillDownRanges.get(range.getLabel());
                    drillDownRanges.put(range.getLabel(), new AnalyticsDrillDownRange(range.getLabel(),
                        range.getFrom(), range.getTo(), oldRange.getScore() + score));
                }
            }
        }
        return new ArrayList<>(drillDownRanges.values());
    }

    private MultiReader getCombinedIndexReader(Set<Integer> shardIds, int tenantId, String tableName)
            throws IOException, AnalyticsIndexException {
        List<IndexReader> indexReaders = new ArrayList<>();
        for (int shardId : shardIds) {
            String tableId = this.generateTableId(tenantId, tableName);
            try {
                IndexReader reader = DirectoryReader.open(this.lookupIndexWriter(shardId, tableId), true);
                indexReaders.add(reader);
            } catch (IndexNotFoundException ignore) {
                /* this can happen if a user just started to index records in a table,
                 * but it didn't yet do the first commit, so it does not have segment* files.
                 * The execution comes to this place, because the shards are identified, since
                 * there is some other intermediate files written to the index directory. 
                 * So in this situation, if we are in the middle of the initial commit, we ignore
                 * this partially indexed data for now */
            }
        }
        return new MultiReader(indexReaders.toArray(new IndexReader[indexReaders.size()]));
    }

    public SubCategories drilldownCategories(int tenantId, CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        if (drillDownRequest.getStart() < 0 || drillDownRequest.getCount() < 0) {
            throw new AnalyticsIndexException("starting index of the category list and page size of the category list cannot be less than zero");
        }
        List<CategorySearchResultEntry> resultEntries = new ArrayList<>();
        if (this.isClusteringEnabled()) {
            List<CategoryDrillDownResponse> categoriesPerNodes =
                    this.executeIndexLookup(new DrillDownCategoriesCall(tenantId, drillDownRequest));
            for (CategoryDrillDownResponse categoriesPerNode : categoriesPerNodes) {
                resultEntries.addAll(categoriesPerNode.getCategories());
            }
        } else {
            CategoryDrillDownResponse response = this.getDrillDownCategories(tenantId, drillDownRequest);
            resultEntries.addAll(response.getCategories());
        }
        List<CategorySearchResultEntry> mergedResult = this.mergeCategoryResults(resultEntries);
        String[] path = drillDownRequest.getPath();
        if (path == null) {
            path = new String[] {};
        }
        List<CategorySearchResultEntry> paginatedCategories = this.getPaginatedCategoryResultsEntries(mergedResult,
                drillDownRequest.getStart(), drillDownRequest.getCount());
        return new SubCategories(path, paginatedCategories, mergedResult.size());
    }

    /**
     * Different shards/Nodes can contain the same categories, so we need to merge the duplicate categories and sum the scores
     * @param searchResults the List of Category Results which may contain duplicate categories
     * @return De-dupped List ofcategories
     */
    private List<CategorySearchResultEntry> mergeCategoryResults(List<CategorySearchResultEntry>
                                                                         searchResults) {
        Map<String, Double> mergedResults = new LinkedHashMap<>();
        List<CategorySearchResultEntry> finalResult = new ArrayList<>();
        for (CategorySearchResultEntry perShardResults : searchResults) {
                Double score = mergedResults.get(perShardResults.getCategoryValue());
                if (score != null) {
                    score += perShardResults.getScore();
                    mergedResults.put(perShardResults.getCategoryValue(), score);
                } else {
                    mergedResults.put(perShardResults.getCategoryValue(), perShardResults.getScore());
                }
        }
        for (Map.Entry<String, Double> entry : mergedResults.entrySet()) {
            finalResult.add(new CategorySearchResultEntry(entry.getKey(), entry.getValue()));
        }
        Collections.sort(finalResult);
        Collections.reverse(finalResult);
        return finalResult;
    }

    private List<CategorySearchResultEntry> getPaginatedCategoryResultsEntries(List<CategorySearchResultEntry> resultEntries, int start, int count) {
        int categoryCount = start + count;
        if (categoryCount == 0 || categoryCount > resultEntries.size()) {
            categoryCount = resultEntries.size();
        }
        if (start > resultEntries.size()-1) {
            return new ArrayList<>(0);
        }
        List <CategorySearchResultEntry> finalResult = new ArrayList<>(resultEntries.subList(start, categoryCount));
        return finalResult;
    }

    private List<SearchResultEntry> drillDownRecords(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                                     IndexReader indexReader, TaxonomyReader taxonomyReader,
                                                     String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        List<SearchResultEntry> searchResults = new ArrayList<>();
        try {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            FacetsCollector facetsCollector = new FacetsCollector(true);
            Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId, drillDownRequest.getTableName());
            FacetsConfig config = this.getFacetsConfigurations(indices);
            DrillSideways drillSideways = new DrillSideways(indexSearcher, config, taxonomyReader);
            DrillDownQuery drillDownQuery = this.createDrillDownQuery(drillDownRequest,
                    indices, config,rangeField, range);
            drillSideways.search(drillDownQuery, facetsCollector);
            int topResultCount = drillDownRequest.getRecordStartIndex() + drillDownRequest.getRecordCount();
            TopDocs topDocs;
            if (drillDownRequest.getSortByFields() == null || drillDownRequest.getSortByFields().isEmpty()) {
                topDocs = FacetsCollector.search(indexSearcher, drillDownQuery, topResultCount, facetsCollector);
            } else {
                SortField[] sortFields = createSortFields(drillDownRequest.getSortByFields(), indices);
                topDocs = FacetsCollector.search(indexSearcher, drillDownQuery, null, topResultCount, new Sort(sortFields),
                        true, false, facetsCollector);
            }
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc);
                searchResults.add(new SearchResultEntry(document.get(INDEX_ID_INTERNAL_FIELD), scoreDoc.score));
            }
            return searchResults;
        } catch (IndexNotFoundException ignore) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error while performing drilldownRecords: " + e.getMessage(), e);
        } finally {
            this.closeTaxonomyIndexReaders(indexReader, taxonomyReader);
        }
    }

    private CategoryDrillDownResponse drilldowncategories(int tenantId, IndexReader indexReader,
                                                                TaxonomyReader taxonomyReader,
                                                                CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        List<CategorySearchResultEntry> searchResults = new ArrayList<>();
        try {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            FacetsCollector facetsCollector = new FacetsCollector(true);
            Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId, drillDownRequest.getTableName());
            FacetsConfig config = this.getFacetsConfigurations(indices);
            DrillSideways drillSideways = new DrillSideways(indexSearcher, config, taxonomyReader);
            Query queryObj = new MatchAllDocsQuery();
            if (drillDownRequest.getQuery() != null && !drillDownRequest.getQuery().isEmpty()) {
                Analyzer analyzer = getPerFieldAnalyzerWrapper(indices);
                queryObj = (new AnalyticsQueryParser(analyzer, indices)).parse(drillDownRequest.getQuery());
            }
            DrillDownQuery drillDownQuery = new DrillDownQuery(config, queryObj);
            String[] path = drillDownRequest.getPath();
            if (path == null) {
                path = new String[]{};
            }
            drillDownQuery.add(drillDownRequest.getFieldName(), path);
            drillSideways.search(drillDownQuery, facetsCollector);
            ValueSource valueSource = this.getCompiledScoreFunction(drillDownRequest.getScoreFunction(),
                                                                    indices);
            Facets facets = new TaxonomyFacetSumValueSource(taxonomyReader, config, facetsCollector,
                                            valueSource);
            return getCategoryDrillDownResponse(drillDownRequest, searchResults, path, facets);
        } catch (IndexNotFoundException ignore) {
            return new CategoryDrillDownResponse(new ArrayList<CategorySearchResultEntry>(0));
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error while performing drilldownCategories: " + e.getMessage(), e);
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            throw new AnalyticsIndexException("Error while parsing query " + e.getMessage(), e);
        } finally {
            this.closeTaxonomyIndexReaders(indexReader, taxonomyReader);
        }
    }

    private CategoryDrillDownResponse getCategoryDrillDownResponse(
            CategoryDrillDownRequest drillDownRequest,
            List<CategorySearchResultEntry> searchResults, String[] path,
            Facets facets) throws IOException {
        FacetResult facetResult = facets.getTopChildren(Integer.MAX_VALUE, drillDownRequest.getFieldName(),
                                                        path);
        CategoryDrillDownResponse response;
        if (facetResult != null) {
            LabelAndValue[] categories = facetResult.labelValues;
            for (LabelAndValue category : categories) {
                searchResults.add(new CategorySearchResultEntry(category.label, category.value.doubleValue()));
            }
            response = new CategoryDrillDownResponse(searchResults);
        } else {
            response = new CategoryDrillDownResponse(new ArrayList<CategorySearchResultEntry>(0));
        }
        return response;
    }

    private double getDrillDownRecordCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                                     IndexReader indexReader, TaxonomyReader taxonomyReader,
                                                     String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        try {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId,
                                                                drillDownRequest.getTableName());
            FacetsConfig config = this.getFacetsConfigurations(indices);
            DrillDownQuery drillDownQuery = this.createDrillDownQuery(drillDownRequest,
                                                                      indices, config,rangeField, range);
            ValueSource scoreFunction = this.getCompiledScoreFunction(drillDownRequest.getScoreFunction(), indices);
            FacetsCollector facetsCollector = new FacetsCollector(true);
            Map<String, List<String>> categoryPaths = drillDownRequest.getCategoryPaths();
            double count = 0;
            if (!categoryPaths.isEmpty()) {
                Map.Entry<String, List<String>> aCategory = categoryPaths.entrySet().iterator().next();
                String categoryName = aCategory.getKey();
                FacetsCollector.search(indexSearcher, drillDownQuery, Integer.MAX_VALUE, facetsCollector);
                Facets facets = new TaxonomyFacetSumValueSource(taxonomyReader, config, facetsCollector, scoreFunction);
                FacetResult facetResult = facets.getTopChildren(Integer.MAX_VALUE, categoryName, new String[0]);
                if (facetResult != null) {
                    LabelAndValue[] subCategories = facetResult.labelValues;
                    for (LabelAndValue category : subCategories) {
                        count += category.value.doubleValue();
                    }
                }
            } else {
                count = indexSearcher.search(drillDownQuery, Integer.MAX_VALUE).totalHits;
            }
            return count;
        } catch (IndexNotFoundException ignore) {
            return 0;
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
                                                Map<String, ColumnDefinition> indices, FacetsConfig config,
                                                String rangeField,
                                                AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        Query languageQuery = new MatchAllDocsQuery();
        try {
            if (drillDownRequest.getQuery() != null && !drillDownRequest.getQuery().isEmpty()) {
                Analyzer analyzer = getPerFieldAnalyzerWrapper(indices);
                languageQuery = new AnalyticsQueryParser(analyzer,
                         indices).parse(drillDownRequest.getQuery());
            }
            DrillDownQuery drillDownQuery = new DrillDownQuery(config, languageQuery);
            if (range != null && rangeField != null) {
                drillDownQuery.add(rangeField, NumericRangeQuery.newDoubleRange(rangeField,
                                                                                range.getFrom(), range.getTo(), true, false));
            }
            if (drillDownRequest.getCategoryPaths() != null && !drillDownRequest.getCategoryPaths().isEmpty()) {
                for (Map.Entry<String, List<String>> entry : drillDownRequest.getCategoryPaths()
                        .entrySet()) {
                    List<String> path = entry.getValue();
                    String[] pathAsArray;
                    if (path == null || path.isEmpty()) {
                        pathAsArray = new String[]{};
                    } else {
                        pathAsArray = path.toArray(new String[path.size()]);
                    }
                    drillDownQuery.add(entry.getKey(), pathAsArray);
                }
            }
            return drillDownQuery;
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            throw new AnalyticsIndexException("Error while parsing lucene query '" +
                                              languageQuery + "': " + e.getMessage(), e.getCause() );
        }
    }

    private FacetsConfig getFacetsConfigurations(Map<String, ColumnDefinition> indices) {
        FacetsConfig config = new FacetsConfig();
        for (Map.Entry<String, ColumnDefinition> entry : indices.entrySet()) {
            if (entry.getValue().isFacet()) {
                String indexField = entry.getKey();
                config.setHierarchical(indexField, true);
                config.setMultiValued(indexField, true);
            }
        }
        return config;
    }

    public AggregateFunctionFactory getAggregateFunctionFactory() {
        if (this.aggregateFunctionFactory == null) {
            this.aggregateFunctionFactory = new AggregateFunctionFactory();
        }
        return this.aggregateFunctionFactory;
    }

    private ValueSource getCompiledScoreFunction(String scoreFunction, Map<String, ColumnDefinition> scoreParams)
            throws AnalyticsIndexException {
        try {
            Expression funcExpression;
            if (scoreFunction == null || scoreFunction.trim().isEmpty()) {
                funcExpression = JavascriptCompiler.compile(DEFAULT_SCORE);
            } else {
                funcExpression = JavascriptCompiler.compile(scoreFunction);
            }
            return getValueSource(scoreParams, funcExpression);
        } catch (ParseException e) {
            throw new AnalyticsIndexException("Error while evaluating the score function:" +
                                              e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new AnalyticsIndexException("Error while evaluating the score function: "
                                              + e.getMessage(), e);
        }
    }

    private ValueSource getValueSource(Map<String, ColumnDefinition> scoreParams,
                                       Expression funcExpression) throws AnalyticsIndexException {
        SimpleBindings bindings = new SimpleBindings();
        bindings.add(new SortField(INDEX_INTERNAL_SCORE_FIELD, SortField.Type.SCORE));
        bindings.add(new SortField(INDEX_INTERNAL_TIMESTAMP_FIELD, SortField.Type.LONG));
        for (Map.Entry<String, ColumnDefinition> entry : scoreParams.entrySet()) {
            if (entry.getValue().isScoreParam()) {
                switch (entry.getValue().getType()) {
                    case DOUBLE:
                        bindings.add(new SortField(entry.getKey(), SortField.Type.DOUBLE));
                        break;
                    case FLOAT:
                        bindings.add(new SortField(entry.getKey(), SortField.Type.FLOAT));
                        break;
                    case INTEGER:
                        bindings.add(new SortField(entry.getKey(), SortField.Type.INT));
                        break;
                    case LONG:
                        bindings.add(new SortField(entry.getKey(), SortField.Type.LONG));
                        break;
                    default:
                        throw new AnalyticsIndexException("Cannot resolve data type: " +
                            entry.getValue().getType()+ " for scoreParam: " + entry.getKey());
                }

            }
        }
        return funcExpression.getValueSource(bindings);
    }

    public List<SearchResultEntry> getDrillDownRecords(int tenantId,
            AnalyticsDrillDownRequest drillDownRequest, String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        int startIndex = drillDownRequest.getRecordStartIndex();
        if (startIndex < 0 ) throw new AnalyticsIndexException("Start index should be greater than 0");
        int endIndex = startIndex + drillDownRequest.getRecordCount();
        if (endIndex <= startIndex) throw new AnalyticsIndexException("Record Count should be greater than 0");
        Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId, drillDownRequest.getTableName());
        List<SearchResultEntry> resultFacetList;
        if (this.isClusteringEnabled()) {
            List<List<SearchResultEntry>> entries = this.executeIndexLookup(new DrillDownSearchCall(tenantId, drillDownRequest));
            resultFacetList = new ArrayList<>();
            for (List<SearchResultEntry> entry : entries) {
                resultFacetList.addAll(entry);
            }
            if (resultFacetList.size() < startIndex) {
                return new ArrayList<>();
            }
            if (resultFacetList.size() < endIndex) {
                return resultFacetList.subList(startIndex, resultFacetList.size());
            }
            resultFacetList = RecordSortUtils.getSortedSearchResultEntries(tenantId, drillDownRequest.getTableName(),
                    drillDownRequest.getSortByFields(), indices, this.getAnalyticsDataService(), resultFacetList);
            return resultFacetList.subList(startIndex, endIndex);
        } else {
            return doDrillDownPerNode(tenantId, drillDownRequest, rangeField, range);
        }
    }

    public List<SearchResultEntry> doDrillDownPerNode(int tenantId,
                                                      AnalyticsDrillDownRequest drillDownRequest,
                                                      String rangeField,
                                                      AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        int startIndex = drillDownRequest.getRecordStartIndex();
        if (startIndex < 0 ) throw new AnalyticsIndexException("Start index should be greater than 0");
        int endIndex = startIndex + drillDownRequest.getRecordCount();
        if (endIndex <= startIndex) throw new AnalyticsIndexException("Record Count should be greater than 0");
        List<Integer> taxonomyShardIds = this.lookupGloballyExistingShardIds();
        List<SearchResultEntry> resultFacetList = new ArrayList<>();
        for (int shardId : taxonomyShardIds) {
            resultFacetList.addAll(this.drillDownRecordsPerShard(tenantId, shardId, drillDownRequest, rangeField, range));
        }
        Collections.sort(resultFacetList);
        Collections.reverse(resultFacetList);
        if (resultFacetList.size() < startIndex) {
            return new ArrayList<>();
        }
        //Sublists are wrapped with ArrayLists because, Sublist structore is not serialized.
        if (resultFacetList.size() < endIndex) {
            return new ArrayList<>(resultFacetList.subList(startIndex, resultFacetList.size()));
        }
        return new ArrayList<>(resultFacetList.subList(startIndex, endIndex));
    }

    public CategoryDrillDownResponse getDrillDownCategories(int tenantId,
                   CategoryDrillDownRequest drillDownRequest) throws AnalyticsIndexException {
        List<Integer> taxonomyShardIds = this.lookupGloballyExistingShardIds();
        return getCategoryDrillDownResponse(tenantId, drillDownRequest, taxonomyShardIds);
    }

    private CategoryDrillDownResponse getCategoryDrillDownResponse(int tenantId,
                                                                   CategoryDrillDownRequest drillDownRequest,
                                                                   List<Integer> taxonomyShardIds)
            throws AnalyticsIndexException {
        List<CategorySearchResultEntry> perNodeCategoryReslutEntries = new ArrayList<>();
        for (int shardId : taxonomyShardIds) {
            CategoryDrillDownResponse response = this.drillDownCategoriesPerShard(tenantId, shardId, drillDownRequest);
            perNodeCategoryReslutEntries.addAll(response.getCategories());
        }

        return new CategoryDrillDownResponse(perNodeCategoryReslutEntries);
    }

    public double getDrillDownRecordCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                         String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        if (this.isClusteringEnabled()) {
            double totalCount = 0;
            List<Double> countsPerNodes = this.executeIndexLookup(new DrillDownSearchCountCall(tenantId, drillDownRequest));
            for (Double countPerNode : countsPerNodes) {
                totalCount += countPerNode;
            }
            return totalCount;
        } else {
            return doDrillDownCountPerNode(tenantId, drillDownRequest, rangeField, range);
        }
    }

    public double doDrillDownCountPerNode(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                          String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        List<Integer> taxonomyShardIds = this.lookupGloballyExistingShardIds();
        double totalCount = 0;
        for (int shardId : taxonomyShardIds) {
            totalCount += this.getDrillDownRecordCountPerShard(tenantId, shardId, drillDownRequest, rangeField, range);
        }
        return totalCount;
    }

    private List<SearchResultEntry> drillDownRecordsPerShard(
            final int tenantId,
            final int shardId,
            final AnalyticsDrillDownRequest drillDownRequest,
            final String rangeField,
            final AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        try {
            String tableId = this.generateTableId(tenantId, drillDownRequest.getTableName());
            IndexReader indexReader = DirectoryReader.open(this.lookupIndexWriter(shardId, tableId), true);
            TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(this.lookupTaxonomyIndexWriter(shardId, tableId));
            return drillDownRecords(tenantId, drillDownRequest, indexReader, taxonomyReader, rangeField, range);
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in opening index readers: " + e.getMessage(), e);
        }
    }

    private CategoryDrillDownResponse drillDownCategoriesPerShard(final int tenantId, final int shardId,
                                                             final CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        try {
            String tableId = this.generateTableId(tenantId, drillDownRequest.getTableName());
            IndexReader indexReader = DirectoryReader.open(this.lookupIndexWriter(shardId, tableId), true);
            TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(this.lookupTaxonomyIndexWriter(shardId, tableId));
            return drilldowncategories(tenantId, indexReader, taxonomyReader, drillDownRequest);
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in opening index readers: " + e.getMessage(), e);
        }
    }

    private double getDrillDownRecordCountPerShard(final int tenantId,
                                                   final int shardId,
                                                   final AnalyticsDrillDownRequest drillDownRequest,
                                                   final String rangeField,
                                                   final AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        try {
            String tableId = this.generateTableId(tenantId, drillDownRequest.getTableName());
            IndexReader indexReader = DirectoryReader.open(this.lookupIndexWriter(shardId, tableId), true);
            TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(this.lookupTaxonomyIndexWriter(shardId, tableId));
            return getDrillDownRecordCount(tenantId, drillDownRequest, indexReader, taxonomyReader, rangeField, range);
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in opening index readers: " + e.getMessage(), e);
        }
    }

    /**
    * Inserts the given records into the index.
    * @param records The records to be inserted
    * @throws AnalyticsException
    */
    public void put(List<Record> records) throws AnalyticsException {
        this.indexNodeCoordinator.put(records);
    }
    
    public void putLocal(List<Record> records) throws AnalyticsException {
        this.localIndexDataStore.put(records);
    }
    
    /**
    * Deletes the given records in the index.
    * @param tenantId The tenant id
    * @param tableName The table name
    * @param ids The ids of the records to be deleted
    * @throws AnalyticsException
    */
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        this.indexNodeCoordinator.delete(tenantId, tableName, ids);
    }
    
    public void deleteLocal(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        this.localIndexDataStore.delete(tenantId, tableName, ids);
    }
    
    private void deleteInIndex(int tenantId, String tableName, int shardIndex, List<String> ids) throws AnalyticsException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting data in local index [" + shardIndex + "]: " + ids.size());
        }
        String tableId = this.generateTableId(tenantId, tableName);
        IndexWriter indexWriter = this.lookupIndexWriter(shardIndex, tableId);
        List<Term> terms = new ArrayList<Term>(ids.size());
        for (String id : ids) {
            terms.add(new Term(INDEX_ID_INTERNAL_FIELD, id));
        }
        try {
            indexWriter.deleteDocuments(terms.toArray(new Term[terms.size()]));
            indexWriter.commit();
            if (this.isIndexingStatsEnabled()) {
                this.statsCollector.processedRecords(terms.size());
            }
        } catch (IOException e) {
            throw new AnalyticsException("Error in deleting indices: " + e.getMessage(), e);
        }
    }
    
    public Map<Integer, List<Record>> extractShardedRecords(List<Record> records) {
        Map<Integer, List<Record>> result = new HashMap<>();
        int shardIndex;
        List<Record> shardedList;
        for (Record record : records) {
            shardIndex = this.calculateShardId(record.getId());
            shardedList = result.get(shardIndex);
            if (shardedList == null) {
                shardedList = new ArrayList<>();
                result.put(shardIndex, shardedList);
            }
            shardedList.add(record);
        }
        return result;
    }
    
    public Map<Integer, List<String>> extractShardedIds(List<String> ids) {
        Map<Integer, List<String>> result = new HashMap<>();
        int shardIndex;
        List<String> shardedList;
        for (String id : ids) {
            shardIndex = this.calculateShardId(id);
            shardedList = result.get(shardIndex);
            if (shardedList == null) {
                shardedList = new ArrayList<>();
                result.put(shardIndex, shardedList);
            }
            shardedList.add(id);
        }
        return result;
    }
        
    private void updateIndex(int shardIndex, List<Record> recordBatch, 
            Map<String, ColumnDefinition> columns) throws AnalyticsIndexException {
        if (log.isDebugEnabled()) {
            log.debug("Updating data in local index [" + shardIndex + "]: " + recordBatch.size());
        }
        Record firstRecord = recordBatch.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        String tableId = this.generateTableId(tenantId, tableName);
        IndexWriter indexWriter = this.lookupIndexWriter(shardIndex, tableId);
        TaxonomyWriter taxonomyWriter = this.lookupTaxonomyIndexWriter(shardIndex, tableId);
        try {
            for (Record record : recordBatch) {
                indexWriter.updateDocument(new Term(INDEX_ID_INTERNAL_FIELD, record.getId()),
                                           this.generateIndexDoc(record, columns, taxonomyWriter).getFields());
            }
            indexWriter.commit();
            taxonomyWriter.commit();
            if (this.isIndexingStatsEnabled()) {
                this.statsCollector.processedRecords(recordBatch.size());
            }
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in updating index: " + e.getMessage(), e);
        }
    }
    
    private String trimNonTokenizedIndexStringField(String value) {
        if (value.length() > MAX_NON_TOKENIZED_INDEX_STRING_SIZE) {
            return value.substring(0, MAX_NON_TOKENIZED_INDEX_STRING_SIZE);
        } else {
            return value;
        }
    }
    
    private void checkAndAddDocEntry(Document doc, AnalyticsSchema.ColumnType type, String name, Object obj)
            throws AnalyticsIndexException {
        FieldType numericFieldType;
        if (obj == null) {
            doc.add(new StringField(name, NULL_INDEX_VALUE, Store.NO));
            return;
        }
        switch (type) {
        case STRING:
            doc.add(new TextField(name, obj.toString(), Store.NO));
            //SortedDocValuesField is to sort STRINGs and search without tokenizing
            doc.add(new SortedDocValuesField(Constants.NON_TOKENIZED_FIELD_PREFIX + name,
                    new BytesRef(this.trimNonTokenizedIndexStringField(obj.toString()).getBytes())));
            break;
        case INTEGER:
            numericFieldType = getLuceneNumericFieldType(FieldType.NumericType.INT);
            if (obj instanceof Number) {
                doc.add(new IntField(name, ((Number) obj).intValue(), numericFieldType));
            } else {
                doc.add(new StringField(name, obj.toString(), Store.NO));
            }
            break;
        case DOUBLE:
            numericFieldType = getLuceneNumericFieldType(FieldType.NumericType.DOUBLE);
            if (obj instanceof Number) {
                doc.add(new DoubleField(name, ((Number) obj).doubleValue(), numericFieldType));
            } else {
                doc.add(new StringField(name, obj.toString(), Store.NO));
            }
            break;
        case LONG:
            numericFieldType = getLuceneNumericFieldType(FieldType.NumericType.LONG);
            if (obj instanceof Number) {
                doc.add(new LongField(name, ((Number) obj).longValue(), numericFieldType));
            } else {
                doc.add(new StringField(name, obj.toString(), Store.NO));
            }
            break;
        case FLOAT:
            numericFieldType = getLuceneNumericFieldType(FieldType.NumericType.FLOAT);
            if (obj instanceof Number) {
                doc.add(new FloatField(name, ((Number) obj).floatValue(), numericFieldType));
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

    private FieldType getLuceneNumericFieldType(FieldType.NumericType type) {
        FieldType fieldType = new FieldType();
        fieldType.setStored(false);
        fieldType.setDocValuesType(DocValuesType.NUMERIC);
        fieldType.setTokenized(true);
        fieldType.setOmitNorms(true);
        fieldType.setIndexOptions(IndexOptions.DOCS);
        fieldType.setNumericType(type);
        if (type == FieldType.NumericType.FLOAT || type == FieldType.NumericType.INT) {
            fieldType.setNumericPrecisionStep(NumericUtils.PRECISION_STEP_DEFAULT_32);
        }
        fieldType.freeze();
        return fieldType;
    }

    private void checkAndAddTaxonomyDocEntries(Document doc,
                                                   String name, Object obj,
                                                   FacetsConfig facetsConfig)
            throws AnalyticsIndexException {
        if (obj == null) {
            doc.add(new StringField(name, NULL_INDEX_VALUE, Store.NO));
        } else {
            facetsConfig.setMultiValued(name, true);
            facetsConfig.setHierarchical(name, true);
            String values = obj.toString();
            if (values.isEmpty()) {
                values = EMPTY_FACET_VALUE;
            }
            doc.add(new FacetField(name, values.split(",")));
        }
    }

    private Document generateIndexDoc(Record record, Map<String, ColumnDefinition> columns,
                   TaxonomyWriter taxonomyWriter) throws AnalyticsIndexException, IOException {
        Document doc = new Document();
        FacetsConfig config = new FacetsConfig();
        FieldType numericFieldType = getLuceneNumericFieldType(FieldType.NumericType.LONG);
        doc.add(new StringField(INDEX_ID_INTERNAL_FIELD, record.getId(), Store.YES));
        doc.add(new SortedDocValuesField(INDEX_ID_INTERNAL_FIELD,
                                         new BytesRef(record.getId().getBytes())));
        doc.add(new LongField(INDEX_INTERNAL_TIMESTAMP_FIELD, record.getTimestamp(), numericFieldType));
        /* make the best effort to store in the given timestamp, or else, 
         * fall back to a compatible format, or else, lastly, string */
        String name;
        for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
            name = entry.getKey();
            this.checkAndAddDocEntry(doc, entry.getValue().getType(), name, record.getValue(name));
            if (entry.getValue().isFacet()) {
                this.checkAndAddTaxonomyDocEntries(doc, name, record.getValue(name), config);
            }
        }
        return config.build(taxonomyWriter, doc);
    }

    public Map<String, ColumnDefinition> lookupIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        Map<String, ColumnDefinition> indices;
        try {
            AnalyticsSchema schema = this.indexerInfo.getAnalyticsDataService().getTableSchema(tenantId, tableName);
            indices = schema.getIndexedColumns();
            if (indices == null) {
                indices = new HashMap<>();
            }
        } catch (AnalyticsException e) {
            log.error("Error while looking up table Schema: " + e.getMessage(), e);
            throw new AnalyticsIndexException("Error while looking up Table Schema: " + e.getMessage(), e);
        }
        return indices;
    }

    private String generateDirPath(int shardId, String basePath, String tableId) {
        String indexStoreLoc = this.indexerInfo.getIndexStoreLocation();
        if (!indexStoreLoc.endsWith(File.separator)) {
            indexStoreLoc += File.separator;
        }
        return indexStoreLoc + shardId + basePath + tableId;
    }

    private Directory createDirectory(int shardId, String tableId) throws AnalyticsIndexException {
        return this.createDirectory(shardId, INDEX_DATA_FS_BASE_PATH, tableId);
    }
    
    private Directory createDirectory(int shardId, String basePath, String tableId) throws AnalyticsIndexException {
        String path = this.generateDirPath(shardId, basePath, tableId);
        try {
            return new NIOFSDirectory(Paths.get(path));
        } catch (Exception e) {
            throw new AnalyticsIndexException("Error in creating directory: " + e.getMessage(), e);
        }
    }
    
    private String generateShardedTableId(int shardId, String tableId) {
        return shardId + "_" + tableId;
    }

    private IndexWriter lookupIndexWriter(int shardId, String tableId) throws AnalyticsIndexException {
        String shardedTableId = this.generateShardedTableId(shardId, tableId);
        IndexWriter indexWriter = this.indexWriters.get(shardedTableId);
        if (indexWriter == null) {
            synchronized (this.indexWriters) {
                indexWriter = this.indexWriters.get(shardedTableId);
                if (indexWriter == null) {
                    IndexWriterConfig conf = new IndexWriterConfig(this.indexerInfo.getLuceneAnalyzer());
                    try {
                        indexWriter = new IndexWriter(this.createDirectory(shardId, tableId), conf);
                        this.indexWriters.put(shardedTableId, indexWriter);
                    } catch (IOException e) {
                        throw new AnalyticsIndexException("Error in creating index writer: " + e.getMessage(), e);
                    }
                }
            }
        }
        return indexWriter;
    }

    private DirectoryTaxonomyWriter lookupTaxonomyIndexWriter(int shardId, String tableId) throws AnalyticsIndexException {
        String shardedTableId = this.generateShardedTableId(shardId, tableId);
        DirectoryTaxonomyWriter taxonomyWriter = this.indexTaxonomyWriters.get(shardedTableId);
        if (taxonomyWriter == null) {
            synchronized (this.indexTaxonomyWriters) {
                taxonomyWriter = this.indexTaxonomyWriters.get(shardedTableId);
                if (taxonomyWriter == null) {
                    try {
                        taxonomyWriter = new DirectoryTaxonomyWriter(this.createDirectory(shardId, 
                                TAXONOMY_INDEX_DATA_FS_BASE_PATH, tableId), 
                                IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                        this.indexTaxonomyWriters.put(shardedTableId, taxonomyWriter);
                    } catch (IOException e) {
                        throw new AnalyticsIndexException("Error in creating index writer: " +
                                                          e.getMessage(), e);
                    }
                }
            }
        }
        return taxonomyWriter;
    }

    public void clearIndexData(int tenantId, String tableName) throws AnalyticsException {
        this.indexNodeCoordinator.clearIndexData(tenantId, tableName);
    }
    
    public void clearIndexDataLocal(int tenantId, String tableName) throws AnalyticsIndexException {
        String tableId = this.generateTableId(tenantId, tableName);
        IndexWriter indexWriter;
        TaxonomyWriter taxonomyWriter;
        for (int shardIndex : this.localShards) {
            try {
                indexWriter = this.lookupIndexWriter(shardIndex, tableId);
                indexWriter.deleteAll();
                indexWriter.commit();
                synchronized (this.indexTaxonomyWriters) {
                    taxonomyWriter = this.lookupTaxonomyIndexWriter(shardIndex, tableId);
                    taxonomyWriter.commit();
                    taxonomyWriter.close();
                    this.indexTaxonomyWriters.remove(this.generateShardedTableId(shardIndex, tableId));
                    FileUtils.deleteDirectory(new File(this.generateDirPath(shardIndex, 
                            TAXONOMY_INDEX_DATA_FS_BASE_PATH, tableId)));
                }
            } catch (IOException e) {
                throw new AnalyticsIndexException("Error in clearing index data: " + e.getMessage(), e);
            }
        }
    }
    
    private String generateTableId(int tenantId, String tableName) {
        /* the table names are not case-sensitive */
        return tenantId + "_" + tableName.toLowerCase();
    }
    
    private void closeAndRemoveIndexWriters() throws AnalyticsIndexException {
        try {
            Iterator<Entry<String, IndexWriter>> itr1 = this.indexWriters.entrySet().iterator();
            while (itr1.hasNext()) {
                itr1.next().getValue().close();
                itr1.remove();
            }
            Iterator<Entry<String, DirectoryTaxonomyWriter>> itr2 = this.indexTaxonomyWriters.entrySet().iterator();
            while (itr1.hasNext()) {
                itr2.next().getValue().close();
                itr2.remove();
            }
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in closing index writers: " + e.getMessage(), e);
        }
    }
    
    public synchronized void stopAndCleanupIndexProcessing() {
        if (this.shardWorkerExecutor != null) {
            for (IndexWorker worker : this.workers) {
                worker.stop();
            }
            this.shardWorkerExecutor.shutdownNow();
            try {
                this.shardWorkerExecutor.awaitTermination(
                        org.wso2.carbon.analytics.dataservice.core.Constants.INDEX_WORKER_STOP_WAIT_TIME,
                        TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
                /* ignore */
            }
            this.workers = null;
            this.shardWorkerExecutor = null;
        }

        if (this.reIndexWorkerExecutor != null) {
            this.reIndexWorkerExecutor.shutdownNow();
            try {
                this.reIndexWorkerExecutor.awaitTermination(
                        org.wso2.carbon.analytics.dataservice.core.Constants.REINDEX_WORKER_STOP_WAIT_TIME,
                        TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
                /* ignore */
            }
            this.reIndexWorkerExecutor = null;
        }
    }

    public void close() throws AnalyticsIndexException {
        this.stopAndCleanupIndexProcessing();
        this.localIndexDataStore.close();
        this.indexNodeCoordinator.close();
        this.closeAndRemoveIndexWriters();
    }
        
    public void waitForIndexing(long maxWait) throws AnalyticsException, AnalyticsTimeoutException {
        this.indexNodeCoordinator.waitForIndexing(maxWait);
    }
    
    public void waitForIndexingLocal(long maxWait) throws AnalyticsException, AnalyticsTimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(this.localShards.size());
        for (int shardIndex : this.localShards) {
            final int si = shardIndex;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        processIndexOperationsFlushQueue(si);
                    } catch (AnalyticsException e) {
                        log.warn("Error in index operation flushing: " + e.getMessage(), e);
                    }
                }
            });
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(maxWait == -1 ? Integer.MAX_VALUE : maxWait, TimeUnit.MILLISECONDS)) {
                throw new AnalyticsTimeoutException("Timed out waiting for local indexing operations: " + maxWait);
            }
        } catch (InterruptedException ignore) { }
    }
    
    public void waitForIndexing(int tenantId, String tableName, long maxWait) 
            throws AnalyticsException {
        this.waitForIndexing(maxWait);
    }
    
    public AnalyticsIterator<Record> searchWithAggregates(final int tenantId, 
            final AggregateRequest aggregateRequest)
            throws AnalyticsException {
        AnalyticsIterator<Record> iterator;
        List<String[]> subCategories;
        Set<List<String>> finalUniqueCategories;
        try {
            if (this.isClusteringEnabled()) {
                finalUniqueCategories = new HashSet<>();
                List<Set<List<String>>> entries = this.executeIndexLookup(
                        new SearchWithAggregateCall(tenantId, aggregateRequest));
                for (Set<List<String>> entry : entries) {
                    finalUniqueCategories.addAll(entry);
                }
            } else {
                finalUniqueCategories = getUniqueGroupings(tenantId, aggregateRequest);
            }
            subCategories =  getUniqueSubCategories(aggregateRequest, finalUniqueCategories);
        //    iterator = new StreamingAggregateRecordIterator(tenantId, subCategories, aggregateRequest, indexer);
            iterator = this.getNonStreamingAggregateRecords(tenantId, aggregateRequest, subCategories);
            return iterator;
        } catch (IOException e) {
            log.error("Error occured while performing aggregation, " + e.getMessage(), e);
            throw new AnalyticsIndexException("Error occured while performing aggregation, " + e.getMessage(), e);
        }
    }

    private AnalyticsIterator<Record> getNonStreamingAggregateRecords(int tenantId, AggregateRequest aggregateRequest,
                                                 List<String[]> subCategories)
            throws AnalyticsException {
        Map<String, String[]> recordIdsPerGroup = new HashMap<>();
        List<String> allMatchingIds = new ArrayList<>();
        for (String[] category : subCategories) {
            List<SearchResultEntry> resultEntries = this.getSearchResultEntries(tenantId, category, aggregateRequest);
            List<String> ids = getRecordIds(resultEntries);
            allMatchingIds.addAll(ids);
            if (category != null && category.length != 0) {
                for (String id : ids) {
                    recordIdsPerGroup.put(id, category);
                }
            }
        }
        return getNonStreamingAggregatesIterator(tenantId, aggregateRequest, recordIdsPerGroup, allMatchingIds);
    }

    private AnalyticsIterator<Record> getNonStreamingAggregatesIterator(int tenantId, AggregateRequest aggregateRequest,
                                                   Map<String, String[]> recordIdsPerGroup,
                                                   List<String> allMatchingIds)
            throws AnalyticsException {
        List<Record> aggregatedRecords = new ArrayList<>();
        AnalyticsDataResponse response = this.getAnalyticsDataService().get(tenantId, aggregateRequest.getTableName(),
                1, null, allMatchingIds);
        Iterator<Record> recordIterator = AnalyticsDataServiceUtils.responseToIterator(this.getAnalyticsDataService(), response);

        Map<String[], List<Record>> recordsPerGroup = new HashMap<>();
        if (aggregateRequest.getGroupByField() != null && !aggregateRequest.getGroupByField().isEmpty()) {
            while (recordIterator.hasNext()) {
                Record record = recordIterator.next();
                String id = record.getId();
                String[] group = recordIdsPerGroup.get(id);
                List<Record> records = recordsPerGroup.get(group);
                if (records == null) {
                    records = new ArrayList<>();
                }
                records.add(record);
                recordsPerGroup.put(group, records);
            }
        } else {
            recordsPerGroup.put(new String[]{}, AnalyticsDataServiceUtils.listRecords(this.getAnalyticsDataService(), response));
        }
        for (Entry<String[], List<Record>> entry : recordsPerGroup.entrySet()) {
            aggregatedRecords.add(this.aggregatePerGrouping(tenantId, entry.getKey(),entry.getValue().iterator(),
                    entry.getValue().size(), aggregateRequest ));
        }
        return new NonStreamingAggregateRecordIterator(aggregatedRecords);
    }

    public Set<List<String>> getUniqueGroupings(int tenantId, AggregateRequest aggregateRequest)
            throws AnalyticsIndexException, IOException {
        if (aggregateRequest.getAggregateLevel() >= 0) {
            List<Integer> taxonomyShardIds = this.lookupGloballyExistingShardIds();
            if (aggregateRequest.getGroupByField() != null && !aggregateRequest.getGroupByField().isEmpty()) {
                ExecutorService pool = Executors.newFixedThreadPool(taxonomyShardIds.size());
                Set<Future<Set<List<String>>>> perShardUniqueCategories = new HashSet<>();
                Set<List<String>> finalUniqueCategories = new HashSet<>();
                for (int i = 0; i < taxonomyShardIds.size(); i++) {
                    String tableId = this.generateTableId(tenantId, aggregateRequest.getTableName());
                    Callable<Set<List<String>>> callable = new TaxonomyWorker(tenantId, AnalyticsDataIndexer.this,
                            taxonomyShardIds.get(i), tableId, aggregateRequest);
                    Future<Set<List<String>>> result = pool.submit(callable);
                    perShardUniqueCategories.add(result);
                }
                try {
                    for (Future<Set<List<String>>> result : perShardUniqueCategories) {
                        finalUniqueCategories.addAll(result.get());
                    }
                    return finalUniqueCategories;
                } catch (Exception e) {
                    log.error("Error while generating Unique categories for aggregation, " + e.getMessage(), e);
                    throw new AnalyticsIndexException("Error while generating Unique categories for aggregation, " +
                                                      e.getMessage(), e);
                } finally {
                    shutdownWorkerThreadPool(pool);
                }
            } else {
                return new HashSet<>();
            }
        } else {
            throw new AnalyticsIndexException("Aggregate level cannot be less than zero");
        }
    }

    private void shutdownWorkerThreadPool(ExecutorService pool)
            throws AnalyticsIndexException {
        if (pool != null) {
            pool.shutdown();
        }
        try {
            if(!pool.awaitTermination(WORKER_TIMEOUT, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error while shutting down the threadpool , " + e.getMessage(), e);
            throw new AnalyticsIndexException("Error while shutting down the threadpool , " +
                                              e.getMessage(), e);
        } finally {
            pool = null;
        }
    }

    private List<String[]> getUniqueSubCategories(AggregateRequest aggregateRequest,
                                                  Set<List<String>> uniqueCategories)
            throws AnalyticsIndexException {
        List<String[]> groupings = new ArrayList<>();
        if (aggregateRequest.getGroupByField() == null || aggregateRequest.getGroupByField().isEmpty()) {
            // for sure, uniqueCategories is empty if groupByField is not present
            // and groupings is empty too. adding a dummy String array to aggregate all the records without facet field.
            groupings.add(new String[]{});
        } else {
            for (List<String> category : uniqueCategories) {
                groupings.add(category.toArray(new String[category.size()]));
            }
        }
        return groupings;
    }

    private Record aggregatePerGrouping(int tenantId, String[] path, Iterator<Record> iterator, int actualNoOfRecords,
                                        AggregateRequest aggregateRequest)
            throws AnalyticsException {
        Map<String, AggregateFunction> perAliasAggregateFunction = initPerAliasAggregateFunctions(aggregateRequest);
        Record aggregatedRecord = null;
        while (iterator.hasNext()) {
            Record record = iterator.next();
            for (AggregateField field : aggregateRequest.getFields()) {
                AggregateFunction function = perAliasAggregateFunction.get(field.getAlias());
                RecordContext recordValues = RecordContext.create(record.getValues());
                function.process(recordValues, field.getAggregateVariables());
            }
        }
        Map<String, Object> aggregatedValues = generateAggregateRecordValues(path, actualNoOfRecords, aggregateRequest,
                                                                             perAliasAggregateFunction);
        aggregatedRecord = new Record(tenantId, aggregateRequest.getTableName(), aggregatedValues);
        return aggregatedRecord;
    }

    private List<SearchResultEntry> getSearchResultEntries(int tenantId, String[] path,
                                                           AggregateRequest aggregateRequest)
            throws AnalyticsException {
        List<SearchResultEntry> searchResultEntries = null;
        if (aggregateRequest.getGroupByField() != null && !aggregateRequest.getGroupByField().isEmpty()) {
            int recordCount = aggregateRequest.getNoOfRecords() > 0 ? aggregateRequest.getNoOfRecords() : Integer.MAX_VALUE;
            AnalyticsDrillDownRequest analyticsDrillDownRequest = new AnalyticsDrillDownRequest();
            analyticsDrillDownRequest.setTableName(aggregateRequest.getTableName());
            analyticsDrillDownRequest.setQuery(aggregateRequest.getQuery());
            analyticsDrillDownRequest.setRecordStartIndex(0);
            analyticsDrillDownRequest.setRecordCount(recordCount);
            Map<String, List<String>> groupByCategory = new HashMap<>();
            List<String> groupByValue = new ArrayList<>();
            groupByValue.addAll(Arrays.asList(path));
            groupByCategory.put(aggregateRequest.getGroupByField(), groupByValue);
            analyticsDrillDownRequest.setCategoryPaths(groupByCategory);
            searchResultEntries = this.getDrillDownRecords(tenantId, analyticsDrillDownRequest, null, null);
        } else {
            if (aggregateRequest.getNoOfRecords() > 0) {
                searchResultEntries = this.search(tenantId, aggregateRequest.getTableName(), aggregateRequest.getQuery(),
                                                  0, aggregateRequest.getNoOfRecords(), null);
            } else {
                throw new AnalyticsException("No of records to be iterated is missing.. ( Parameter : NoOfRecords is zero..)");
            }
        }
        return searchResultEntries;
    }

    private Map<String, Object> generateAggregateRecordValues(String[] path, int actualNoOfRecords,
                                                              AggregateRequest aggregateRequest,
                                                              Map<String, AggregateFunction> perAliasAggregateFunction)
            throws AnalyticsException {
        Map<String, Object> aggregatedValues = new HashMap<>();
        String luceneQuery = "*:*";
        if (aggregateRequest.getQuery() != null && !aggregateRequest.getQuery().isEmpty()) {
            luceneQuery = aggregateRequest.getQuery();
        }
        for (AggregateField field : aggregateRequest.getFields()) {
            String alias = field.getAlias();
            Object result = perAliasAggregateFunction.get(alias).finish();
            aggregatedValues.put(alias, result);
        }
        if (aggregateRequest.getGroupByField() != null && !aggregateRequest.getGroupByField().isEmpty()) {
            aggregatedValues.put(aggregateRequest.getGroupByField(),
                                 path);
        }
        aggregatedValues.put(LUCENE_QUERY_FOR_AGGREGATION, luceneQuery);
        aggregatedValues.put(NO_OF_RECORDS, actualNoOfRecords);
        return aggregatedValues;
    }

    private Map<String, AggregateFunction> initPerAliasAggregateFunctions(
            AggregateRequest aggregateRequest)
            throws AnalyticsException {
        Map<String, AggregateFunction> perAliasAggregateFunction = new HashMap<>();
        for (AggregateField field : aggregateRequest.getFields()) {
            AggregateFunction function = getAggregateFunctionFactory().create(field.getAggregateFunction());
            if (function == null) {
                throw new AnalyticsException("Unknown aggregate function!");
            } else if (field.getAlias() == null || field.getAlias().isEmpty()) {
                throw new AnalyticsException("One of the aggregating field alias is not provided");
            }
            perAliasAggregateFunction.put(field.getAlias(), function);
        }
        return perAliasAggregateFunction;
    }

    private static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<>();
        for (SearchResultEntry searchResult : searchResults) {
            ids.add(searchResult.getId());
        }
        return ids;
    }

    public void reIndex(int tenantId, String table, long startTime, long endTime)
            throws AnalyticsException {
        if (this.reIndexWorkerExecutor == null) {
            this.reIndexWorkerExecutor = new ThreadPoolExecutor(0, REINDEX_THREAD_COUNT,
                                                                Long.MAX_VALUE, TimeUnit.SECONDS,
                                                                new ArrayBlockingQueue<Runnable>(REINDEX_QUEUE_LIMIT));
        }
        try {
            this.reIndexWorkerExecutor.submit(new ReIndexWorker(tenantId, this, table, startTime, endTime));
        } catch (RejectedExecutionException e) {
            String msg = "Reindex operation limit has reached: " + REINDEX_QUEUE_LIMIT;
            throw new AnalyticsException(msg);
        }
    }

    /*private static class StreamingAggregateRecordIterator implements AnalyticsIterator<Record> {

        private static Log logger = LogFactory.getLog(StreamingAggregateRecordIterator.class);
        private AggregateRequest request;
        private List<String[]> groupings;
        private int tenantId;
        private String[] currentGrouping;
        private AnalyticsDataIndexer indexer;
        private Record currentRecord;
        public StreamingAggregateRecordIterator(int tenantId, List<String[]> uniqueGroupings,
                                                AggregateRequest request,
                                                AnalyticsDataIndexer indexer) {
            this.request = request;
            this.tenantId = tenantId;
            this.groupings = uniqueGroupings;
            this.indexer = indexer;
        }

        @Override
        public void close() throws IOException {
            this.request = null;
            this.currentGrouping = null;
            this.groupings = null;
            this.currentRecord = null;
        }

        @Override
        public synchronized boolean hasNext() {
            if (groupings != null && !groupings.isEmpty()) {
                currentGrouping = groupings.get(0);
                try {
                    if (currentRecord != null) {
                        return true;
                    } else {
                        currentRecord = getAggregatedRecord();
                    }
                } catch (AnalyticsException e) {
                    logger.error("Failed to create aggregated record: " + e.getMessage(), e);
                    throw new RuntimeException("Error while iterating aggregate records: " +
                                               e.getMessage(), e);
                }
                if (currentRecord == null) {
                    groupings.remove(currentGrouping);
                    return this.hasNext();
                } else {
                    return true;
                }
            } else {
                    return false;
            }
        }

        private Record getAggregatedRecord() throws AnalyticsException {
            List<SearchResultEntry> searchResultEntries = indexer.getSearchResultEntries(tenantId,
                currentGrouping, request);
            if (!searchResultEntries.isEmpty()) {
                List<String> recordIds = getRecordIds(searchResultEntries);
                int noOfRecords = recordIds.size();
                AnalyticsDataResponse analyticsDataResponse =
                        indexer.getAnalyticsIndexerInfo().getAnalyticsDataService().get(tenantId, request.getTableName(),
                                                                                        1, null, recordIds);
                Iterator<Record> iterator = AnalyticsDataServiceUtils.responseToIterator(indexer.getAnalyticsIndexerInfo()
                        .getAnalyticsDataService(), analyticsDataResponse);
                return indexer.aggregatePerGrouping(tenantId, currentGrouping, iterator, noOfRecords, request);
            } else {
                return null;
            }
        }

        @Override
        public synchronized Record next() {
            if (hasNext()) {
                groupings.remove(currentGrouping);
                Record tempRecord = currentRecord;
                currentRecord = null;
                return tempRecord;
            }
            return null;
        }

        @Override
        public void remove() {
            //This will not work in this iterator
        }
    }*/

    private class NonStreamingAggregateRecordIterator implements AnalyticsIterator<Record> {

        private List<Record> records;
        private Iterator<Record> iterator;

        public NonStreamingAggregateRecordIterator(List<Record> records) {
            this.records = records;
        }

        @Override
        public void close() throws IOException {
            // ignored
        }

        @Override
        public boolean hasNext() {
            if (records == null || records.isEmpty()) {
                return false;
            } else {
                if (iterator == null) {
                    iterator = records.iterator();
                }
                return iterator.hasNext();
            }
        }

        @Override
        public Record next() {
            if (this.hasNext()) {
                return iterator.next();
            } else {
                return null;
            }
        }

        @Override
        public void remove() {
            //ignored
        }
    }

    /**
     * This represents an indexing worker, who does index operations in the background.
     */
    private class IndexWorker implements Runnable {
        
        private boolean stop;
        
        private int shardIndex;
        
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
                } catch (Throwable e) {
                    log.error("Error in processing index batch operations: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(getShardIndexWorkerInterval());
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * This represents a re-indexing worker, who does index operations in the background.
     */
    private class ReIndexWorker implements Runnable {

        private boolean stop;
        private AnalyticsDataIndexer indexer;
        private String tableName;
        private long fromTime;
        private int tenantId;
        private long toTime;

        public ReIndexWorker(int tenantId, AnalyticsDataIndexer indexer, String tableName, long from, long to) {
            this.indexer = indexer;
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.fromTime = from;
            this.toTime = to;
        }

        @Override
        public void run() {
            DateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss.SSS");
            log.info("Re-Indexing called for table: " + tableName + " timestamp between: " +
                     format.format(new Date(fromTime)) + " and " +
                     format.format(new Date(toTime)));
            AnalyticsDataService ads = indexer.getAnalyticsDataService();
            try {
                AnalyticsDataResponse response = ads.get(tenantId, tableName, 1, null, fromTime, toTime, 0, -1);
                List<Record> recordBatch;
                int i;
                Iterator<Record> iterator = AnalyticsDataServiceUtils.responseToIterator(ads, response);
                while (iterator.hasNext() && !this.stop) {
                    i = 0;
                    recordBatch = new ArrayList<>();
                    while (i < org.wso2.carbon.analytics.dataservice.core.Constants.RECORDS_BATCH_SIZE && iterator.hasNext()) {
                        recordBatch.add(iterator.next());
                        i++;
                    }
                    indexer.put(recordBatch);
                }
            } catch (Throwable e) {
                log.error("Error in re-indexing records: " + e.getMessage(), e);
            }
        }
    }

    private class TaxonomyWorker implements Callable<Set<List<String>>> {

        private AggregateRequest aggregateRequest;
        private AnalyticsDataIndexer indexer;
        private int shardId;
        private String tableId;
        private int tenantId;

        public TaxonomyWorker(int tenantId, AnalyticsDataIndexer  indexer,int shardId, String tableId, AggregateRequest request)
                throws AnalyticsIndexException, IOException {
            this.tenantId = tenantId;
            this.indexer = indexer;
            this.tableId = tableId;
            this.shardId = shardId;
            this.aggregateRequest = request;
        }

        @Override
        public Set<List<String>> call() throws Exception {

            Set<List<String>> perShardCategorySet = new HashSet<>();
            int aggregateLevel = aggregateRequest.getAggregateLevel();
            List<String> parentPath = aggregateRequest.getParentPath();
            if (parentPath != null && !parentPath.isEmpty()) {
                this.addAllCategoriesToSet(parentPath.toArray(new String[parentPath.size()]), aggregateLevel, perShardCategorySet);
            } else {
                this.addAllCategoriesToSet(null, aggregateLevel, perShardCategorySet);
            }
            return perShardCategorySet;
        }

        private void addAllCategoriesToSet(String[] parent, int localAggregateLevel, Set<List<String>> uniqueGroups)
                throws IOException, AnalyticsException {
            TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(indexer.lookupTaxonomyIndexWriter(shardId, tableId));
            IndexReader indexReader = DirectoryReader.open(indexer.lookupIndexWriter(shardId, tableId), true);
            CategoryDrillDownRequest request = new CategoryDrillDownRequest();
            request.setFieldName(aggregateRequest.getGroupByField());
            request.setPath(parent);
            request.setTableName(aggregateRequest.getTableName());
            request.setQuery(aggregateRequest.getQuery());
            CategoryDrillDownResponse resultEntries = indexer.drilldowncategories(tenantId, indexReader, taxonomyReader, request);

            for (CategorySearchResultEntry child : resultEntries.getCategories()) {
                List<String> newParent = new ArrayList<>();
                if (parent != null && parent.length != 0) {
                    newParent.addAll(Arrays.asList(parent));
                    newParent.add(child.getCategoryValue());
                } else {
                    newParent.add(child.getCategoryValue());
                }
                if (localAggregateLevel > 0) {
                    addAllCategoriesToSet(newParent.toArray(new String[newParent.size()]), localAggregateLevel-1, uniqueGroups);
                } else if (localAggregateLevel == 0) {
                    uniqueGroups.add(newParent);
                }
            }
        }
    }
    /**
     * Base class for all index operation lookup calls;
     */
    public abstract static class IndexLookupOperationCall<T> implements Callable<T>, Serializable {

        private static final long serialVersionUID = -3795911382229854410L;
        
        protected Set<Integer> shardIndices;
        
        public void setShardIndices(Set<Integer> shardIndices) {
            this.shardIndices = shardIndices;
        }
        
        public abstract IndexLookupOperationCall<T> copy();
        
    }
    
    public static class SearchCountCall extends IndexLookupOperationCall<Integer> {

        private static final long serialVersionUID = -6551068087138398124L;

        private int tenantId;
        
        private String tableName;
        
        private String query;
        
        public SearchCountCall(int tenantId, String tableName, String query) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.query = query;
        }
        
        @Override
        public Integer call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                return adsImpl.getIndexer().doSearchCount(this.shardIndices, this.tenantId, this.tableName,
                                                          this.query);
            }
            return 0;
        }

        @Override
        public IndexLookupOperationCall<Integer> copy() {
            return new SearchCountCall(this.tenantId, this.tableName, this.query);
        }
        
    }
    
    public static class SearchCall extends IndexLookupOperationCall<List<SearchResultEntry>> {

        private static final long serialVersionUID = -6551068087138398124L;

        private int tenantId;
        
        private String tableName;
        
        private String query;
        
        private int start;
        
        private int count;

        private List<SortByField> sortByFields;
        
        public SearchCall(int tenantId, String tableName, String query, int start, int count, List<SortByField> sortByFields) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.query = query;
            this.start = start;
            this.count = count;
            this.sortByFields = sortByFields;
        }
        
        @Override
        public List<SearchResultEntry> call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                return adsImpl.getIndexer().doSearch(this.shardIndices, this.tenantId, this.tableName,
                                                     this.query, this.start, this.count, sortByFields);
            }
            return new ArrayList<>();
        }

        @Override
        public IndexLookupOperationCall<List<SearchResultEntry>> copy() {
            return new SearchCall(this.tenantId, this.tableName, this.query, this.start, this.count, sortByFields);
        }
        
    }

    public static class DrillDownSearchCall extends IndexLookupOperationCall<List<SearchResultEntry>> {


        private static final long serialVersionUID = 8317130568980809116L;
        private int tenantId;
        private AnalyticsDrillDownRequest request;

        public DrillDownSearchCall(int tenantId, AnalyticsDrillDownRequest request) {
            this.tenantId = tenantId;
            this.request = request;
        }

        @Override
        public List<SearchResultEntry> call()  throws Exception{
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The Analtyics data service implementation is not registered");
            }
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                return adsImpl.getIndexer().doDrillDownPerNode(tenantId, request, null, null);
            }
            return new ArrayList<>();
        }


        @Override
        public IndexLookupOperationCall<List<SearchResultEntry>> copy() {
            return new DrillDownSearchCall(tenantId, request);
        }
    }

    public static class DrillDownSearchCountCall extends IndexLookupOperationCall<Double> {


        private static final long serialVersionUID = -2319119330228041861L;
        private int tenantId;
        private AnalyticsDrillDownRequest request;

        public DrillDownSearchCountCall(int tenantId,
                                        AnalyticsDrillDownRequest request) {
            this.tenantId = tenantId;
            this.request = request;
        }

        @Override
        public IndexLookupOperationCall<Double> copy() {
            return new DrillDownSearchCountCall(tenantId, request);
        }

        @Override
        public Double call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The Analytics data service implementation is not registered");
            }
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                return adsImpl.getIndexer().doDrillDownCountPerNode(tenantId, request, null, null);
            }
            return 0.0;
        }
    }

    public static class DrillDownCategoriesCall extends IndexLookupOperationCall<CategoryDrillDownResponse> {


        private static final long serialVersionUID = -2277888731238692285L;
        private int tenantId;
        private CategoryDrillDownRequest request;

        public DrillDownCategoriesCall(int tenantId,
                                       CategoryDrillDownRequest request) {
            this.tenantId = tenantId;
            this.request = request;
        }

        @Override
        public IndexLookupOperationCall<CategoryDrillDownResponse> copy() {
            return new DrillDownCategoriesCall(tenantId, request);
        }

        @Override
        public CategoryDrillDownResponse call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The Analytics data service implementation is not registered");
            }
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                return adsImpl.getIndexer().getDrillDownCategories(tenantId, request);
            }
            return new CategoryDrillDownResponse(new ArrayList<CategorySearchResultEntry>(0));
        }
    }

    public static class DrillDownRangeCountCall extends IndexLookupOperationCall<List<AnalyticsDrillDownRange>> {

        private static final long serialVersionUID = 4949911704640332561L;
        private int tenantId;
        private AnalyticsDrillDownRequest request;

        public DrillDownRangeCountCall(int tenantId,
                                       AnalyticsDrillDownRequest request) {
            this.tenantId = tenantId;
            this.request = request;
        }

        @Override
        public IndexLookupOperationCall<List<AnalyticsDrillDownRange>> copy() {
            return new DrillDownRangeCountCall(tenantId, request);
        }

        @Override
        public List<AnalyticsDrillDownRange> call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The Analytics data service implementation is not registered");
            }
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                return adsImpl.getIndexer().getAnalyticsDrillDownRanges(tenantId, request);
            }
            return new ArrayList<>();
        }
    }

    public static class SearchWithAggregateCall extends IndexLookupOperationCall<Set<List<String>>> {

        private static final long serialVersionUID = -5074344695392737981L;
        private int tenantId;
        private AggregateRequest request;

        public SearchWithAggregateCall(int tenantId,
                                       AggregateRequest request) {
            this.tenantId = tenantId;
            this.request = request;
        }

        @Override
        public IndexLookupOperationCall<Set<List<String>>> copy() {
            return new SearchWithAggregateCall(tenantId, request);
        }

        @Override
        public Set<List<String>> call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The Analytics data service implementation is not registered");
            }
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                return adsImpl.getIndexer().getUniqueGroupings(tenantId, request);
            }
            return new HashSet<>();
        }
    }
}
