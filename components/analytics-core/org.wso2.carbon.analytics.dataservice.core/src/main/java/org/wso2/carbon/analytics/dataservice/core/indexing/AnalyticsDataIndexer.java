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
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.wso2.carbon.analytics.dataservice.commons.AggregateField;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategorySearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.Constants;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsQueryParser;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterException;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.core.indexing.LocalIndexDataStore.IndexOperation;
import org.wso2.carbon.analytics.dataservice.core.indexing.LocalIndexDataStore.LocalIndexDataQueue;
import org.wso2.carbon.analytics.dataservice.core.indexing.aggregates.AggregateFunction;
import org.wso2.carbon.analytics.dataservice.core.indexing.aggregates.AggregateFunctionFactory;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the indexing functionality.
 */
public class AnalyticsDataIndexer {

    private static final Log log = LogFactory.getLog(AnalyticsDataIndexer.class);
    
    private static final int MAX_NON_TOKENIZED_INDEX_STRING_SIZE = 1000;
    
    public static final String DISABLE_INDEX_THROTTLING_ENV_PROP = "disableIndexThrottling";
    
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
    
    public static final String PATH_SEPARATOR = "___####___";
    
    public static final int TAXONOMYWORKER_TIMEOUT = 60;
    
    private Map<String, Directory> indexDirs = new HashMap<>();

    private Map<String, Directory> indexTaxonomyDirs = new HashMap<>();

    private AggregateFunctionFactory aggregateFunctionFactory;
    
    private ExecutorService shardWorkerExecutor;
    
    private List<IndexWorker> workers;
    
    private AnalyticsIndexerInfo indexerInfo;
    
    private IndexNodeCoordinator indexNodeCoordinator;
    
    private LocalIndexDataStore localIndexDataStore;
    
    private Set<Integer> localShards = new HashSet<>();
    
    public AnalyticsDataIndexer(AnalyticsIndexerInfo indexerInfo) throws AnalyticsException {
    	this.indexerInfo = indexerInfo;
    }
    
    /**
     * This method initializes the indexer, and must be called before any other operation in this class is called.
     * @throws AnalyticsException
     */
    public void init() throws AnalyticsException {
        this.getAnalyticsRecordStore().createTable(org.wso2.carbon.analytics.dataservice.core.Constants.META_INFO_TENANT_ID, 
                org.wso2.carbon.analytics.dataservice.core.Constants.GLOBAL_SHARD_ALLOCATION_CONFIG_TABLE);
        this.localIndexDataStore = new LocalIndexDataStore(this);
        this.indexNodeCoordinator = new IndexNodeCoordinator(this);
        this.indexNodeCoordinator.init();
    }
    
    public IndexNodeCoordinator getIndexNodeCoordinator() {
        return indexNodeCoordinator;
    }
    
    public AnalyticsIndexerInfo getAnalyticsIndexerInfo() {
        return indexerInfo;
    }
    
    public AnalyticsFileSystem getFileSystem() {
        return this.indexerInfo.getAnalyticsFileSystem();
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
    
    public int getShardIndexRecordBatchSize() {
        return this.indexerInfo.getShardIndexRecordBatchSize();
    }
    
    public int getIndexWorkerCount() {
        if (this.workers == null) {
            return 0;
        } else {
            return this.workers.size();
        }
    }
    
    /* processIndexOperations and processIndexOperationsFlushQueue must be synchronized, they are accessed by
     * indexer threads and wait for indexing tasks, if not done property, index corruption will happen */
    private synchronized void processIndexOperations(int shardIndex) throws AnalyticsException {
        int maxBatchSize = this.getShardIndexRecordBatchSize();
        int tmpCount;
        /* process until the queue has sizable amount of records left in it, or else, go back to the
         * indexing thread and wait for more to fill up */
        do {
            tmpCount = this.processLocalShardDataQueue(shardIndex, 
                    this.localIndexDataStore.getIndexDataQueue(shardIndex), maxBatchSize);
        } while (tmpCount >= maxBatchSize);
    }
    
    /* processIndexOperations and processIndexOperationsFlushQueue must be synchronized */
    public synchronized void processIndexOperationsFlushQueue(int shardIndex) throws AnalyticsException {
        int maxBatchCount = this.getShardIndexRecordBatchSize();
        LocalIndexDataQueue queue = this.localIndexDataStore.getIndexDataQueue(shardIndex);
        long queueSizeAtStart = queue.size();
        int processedCount = 0, tmpCount;
        do {
            tmpCount = this.processLocalShardDataQueue(shardIndex, queue, maxBatchCount);
            if (tmpCount == 0) {
                /* nothing left in the queue, time to leave */
                break;
            }
            processedCount += tmpCount;
        } while (processedCount < queueSizeAtStart);
    }
    
    private int processLocalShardDataQueue(int shardIndex, LocalIndexDataQueue dataQueue, 
            int maxCount) throws AnalyticsException {
        if (dataQueue == null) {
            return 0;
        }
        int result = 0;
        boolean delete = false;
        int deleteTenantId = 0;
        String deleteTableName = null;
        IndexOperation indexOp;
        List<IndexOperation> indexOps = new ArrayList<>();
        while (!dataQueue.isEmpty()) {
            indexOp = dataQueue.dequeue();
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
            result++;
            if (result >= maxCount) {
                break;
            }
        }
        this.processIndexOperationBatch(shardIndex, indexOps);
        return result;
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
        this.localIndexDataStore.refreshLocalIndexShards();
        this.reschuduleWorkers();
    }
    
    private void reschuduleWorkers() throws AnalyticsException {
        this.stopAndCleanupIndexProcessing();
        this.workers = new ArrayList<>(this.localShards.size());
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
    
    private List<String> lookupGloballyExistingShardIds()
            throws AnalyticsIndexException {
        List<String> result = new ArrayList<>(this.localShards.size());
        for (int shardIndex : this.localShards) {
            result.add(String.valueOf(shardIndex));
        }
        return result;
    }
    
    public List<SearchResultEntry> search(final int tenantId, final String tableName, final String query,
            final int start, final int count) throws AnalyticsException {
        List<SearchResultEntry> result;
        if (this.isClusteringEnabled()) {
            AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
            List<List<SearchResultEntry>> entries = acm.execute(org.wso2.carbon.analytics.dataservice.core.Constants.
                    ANALYTICS_INDEXING_GROUP, new SearchCall(tenantId, tableName, query, start, count),
                    this.getMembersForAllShards());
            result = new ArrayList<>();
            for (List<SearchResultEntry> entry : entries) {
                result.addAll(entry);
            }
            Collections.sort(result);
            Collections.reverse(result);
            int toIndex = start + count;
            if (toIndex >= result.size()) {
                toIndex = result.size();
            }
            if (start < result.size()) {
                result = result.subList(start, toIndex);
            } else {
                result = new ArrayList<>(0);
            }
        } else {
            result = this.doSearch(tenantId, tableName, query, start, count);
        }
        if (log.isDebugEnabled()) {
            log.debug("Search [" + query + "]: " + result.size());
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
            Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId, tableName);
            Analyzer analyzer = getPerFieldAnalyzerWrapper(indices);
            String validatedQuery;
            if (query == null || query.isEmpty()) {
                validatedQuery = "*:*";
                log.warn("Lucene filtering query is not given, So matching all values.");
            } else {
                validatedQuery = query;
            }
            Query indexQuery = new AnalyticsQueryParser(analyzer, indices).parse(validatedQuery);
            if (count <= 0) {
                log.warn("Record Count/Page size is ZERO!. Please set Record count/Page size.");
            }
            TopScoreDocCollector collector = TopScoreDocCollector.create(start + count);
            searcher.search(indexQuery, collector);
            ScoreDoc[] hits = collector.topDocs(start).scoreDocs;
            Document indexDoc;
            for (ScoreDoc doc : hits) {
                indexDoc = searcher.doc(doc.doc);
                result.add(new SearchResultEntry(indexDoc.get(INDEX_ID_INTERNAL_FIELD), doc.score));
            }
            if (log.isDebugEnabled()) {
                log.debug("Local Search: " + result.size());
            }
            return result;
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
    
    private List<Object> getMembersForAllShards() throws AnalyticsIndexException {
        try {
            return AnalyticsServiceHolder.getAnalyticsClusterManager().getMembers(
                    org.wso2.carbon.analytics.dataservice.core.Constants.ANALYTICS_INDEXING_GROUP);
        } catch (AnalyticsClusterException e) {
            throw new AnalyticsIndexException("Error in getting members for all shards: " + e.getMessage(), e);
        }
    }

    public int searchCount(final int tenantId, final String tableName, final String query) 
            throws AnalyticsIndexException {
        int result;
        if (this.isClusteringEnabled()) {
            AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
            List<Integer> counts;
            try {
                counts = acm.execute(org.wso2.carbon.analytics.dataservice.core.Constants.
                        ANALYTICS_INDEXING_GROUP, new SearchCountCall(tenantId, tableName, query),
                        this.getMembersForAllShards());
            } catch (AnalyticsClusterException e) {
                throw new AnalyticsIndexException("Error in doing cluster search count: " + e.getMessage(), e);
            }
            result = 0;
            for (int count : counts) {
                result += count;
            }
        } else {
            result = doSearchCount(tenantId, tableName, query);
        }
        if (log.isDebugEnabled()) {
            log.debug("Search Count: " + result);
        }
        return result;
    }
    
    private int doSearchCount(int tenantId, String tableName, String query) throws AnalyticsIndexException {
        IndexReader reader = null;
        try {
            reader = this.getCombinedIndexReader(tenantId, tableName);
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
        IndexReader indexReader = null;
        try {
            indexReader = getCombinedIndexReader(tenantId, drillDownRequest.getTableName());
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
    }

    private DoubleRange[] createRangeBuckets(List<AnalyticsDrillDownRange> ranges) {
        List<DoubleRange> buckets = new ArrayList<>();
        for (AnalyticsDrillDownRange range : ranges) {
            DoubleRange doubleRange = new DoubleRange(range.getLabel(), range.getFrom(), true, range.getTo(), false);
            buckets.add(doubleRange);
        }
        return buckets.toArray(new DoubleRange[buckets.size()]);
    }

    private MultiReader getCombinedIndexReader(int tenantId, String tableName)
            throws IOException, AnalyticsIndexException {
        List<String> shardIds = this.lookupGloballyExistingShardIds();
        List<IndexReader> indexReaders = new ArrayList<>();
        for (String shardId : shardIds) {
            String tableId = this.generateTableId(tenantId, tableName);
            try {
                IndexReader reader = DirectoryReader.open(this.lookupIndexDir(shardId, tableId));
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
        return finalResult;
    }

    private List<SearchResultEntry> drillDownRecords(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                                     Directory indexDir, Directory taxonomyIndexDir,
                                                     String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        IndexReader indexReader = null;
        TaxonomyReader taxonomyReader = null;
        List<SearchResultEntry> searchResults = new ArrayList<>();
        try {
            indexReader = DirectoryReader.open(indexDir);
            taxonomyReader = new DirectoryTaxonomyReader(taxonomyIndexDir);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            FacetsCollector facetsCollector = new FacetsCollector(true);
            Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId,
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
        } catch (IndexNotFoundException ignore) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error while performing drilldownRecords: " + e.getMessage(), e);
        } finally {
            this.closeTaxonomyIndexReaders(indexReader, taxonomyReader);
        }
    }

    private List<CategorySearchResultEntry> drilldowncategories(int tenantId, Directory indexDir,
                                                                Directory taxonomyIndexDir,
                                                                CategoryDrillDownRequest drillDownRequest) throws AnalyticsIndexException {
        IndexReader indexReader = null;
        TaxonomyReader taxonomyReader = null;
        List<CategorySearchResultEntry> searchResults = new ArrayList<>();
        try {
            indexReader = DirectoryReader.open(indexDir);
            taxonomyReader = new DirectoryTaxonomyReader(taxonomyIndexDir);
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
            FacetResult facetResult = facets.getTopChildren(Integer.MAX_VALUE, drillDownRequest.getFieldName(),
                                                            path);
            if (facetResult != null) {
                LabelAndValue[] categories = facetResult.labelValues;
                for (LabelAndValue category : categories) {
                    searchResults.add(new CategorySearchResultEntry(category.label, category.value.doubleValue()));
                }
            }
            return searchResults;
        } catch (IndexNotFoundException ignore) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error while performing drilldownCategories: " + e.getMessage(), e);
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            throw new AnalyticsIndexException("Error while parsing query " + e.getMessage(), e);
        } finally {
            this.closeTaxonomyIndexReaders(indexReader, taxonomyReader);
        }
    }

    private double getDrillDownRecordCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                                     Directory indexDir, Directory taxonomyIndexDir,
                                                     String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        IndexReader indexReader = null;
        TaxonomyReader taxonomyReader = null;
        try {
            indexReader = DirectoryReader.open(indexDir);
            taxonomyReader = new DirectoryTaxonomyReader(taxonomyIndexDir);
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
            if (entry.getValue().getType().equals(AnalyticsSchema.ColumnType.FACET)) {
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
        List<String> taxonomyShardIds = this.lookupGloballyExistingShardIds();
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
        List<String> taxonomyShardIds = this.lookupGloballyExistingShardIds();
        List<CategorySearchResultEntry> categoriesPerShard = new ArrayList<>();
        for (String shardId : taxonomyShardIds) {
            categoriesPerShard.addAll(this.drillDownCategoriesPerShard(tenantId, shardId, drillDownRequest));
        }
        return categoriesPerShard;
    }

    public double getDrillDownRecordCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                         String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        List<String> taxonomyShardIds = this.lookupGloballyExistingShardIds();
        double totalCount = 0;
        for (String shardId : taxonomyShardIds) {
            totalCount += this.getDrillDownRecordCountPerShard(tenantId, shardId, drillDownRequest, rangeField, range);
        }
        return totalCount;
    }

    private List<SearchResultEntry> drillDownRecordsPerShard(
            final int tenantId,
            final String shardId,
            final AnalyticsDrillDownRequest drillDownRequest,
            final String rangeField,
            final AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        String tableName = drillDownRequest.getTableName();
        String tableId = this.generateTableId(tenantId, tableName);
        Directory indexDir = this.lookupIndexDir(shardId, tableId);
        Directory taxonomyDir = this.lookupTaxonomyIndexDir(shardId, tableId);
        return drillDownRecords(tenantId, drillDownRequest, indexDir, taxonomyDir, rangeField, range);
    }

    private List<CategorySearchResultEntry> drillDownCategoriesPerShard(final int tenantId, final String shardId,
                                                             final CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        String tableId = generateTableId(tenantId, drillDownRequest.getTableName());
        Directory indexDir = lookupIndexDir(shardId, tableId);
        Directory taxonomyDir = lookupTaxonomyIndexDir(shardId, tableId);
        return drilldowncategories(tenantId, indexDir, taxonomyDir, drillDownRequest); 
    }

    private double getDrillDownRecordCountPerShard(final int tenantId,
                                                   final String shardId,
                                                   final AnalyticsDrillDownRequest drillDownRequest,
                                                   final String rangeField,
                                                   final AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        String tableName = drillDownRequest.getTableName();
        String tableId = generateTableId(tenantId, tableName);
        Directory indexDir = lookupIndexDir(shardId, tableId);
        Directory taxonomyDir = lookupTaxonomyIndexDir(shardId, tableId);
        return getDrillDownRecordCount(tenantId, drillDownRequest, indexDir, taxonomyDir, rangeField, range);
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
        IndexWriter indexWriter = this.createIndexWriter(String.valueOf(shardIndex), tableId);
        List<Term> terms = new ArrayList<Term>(ids.size());
        for (String id : ids) {
            terms.add(new Term(INDEX_ID_INTERNAL_FIELD, id));
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
        IndexWriter indexWriter = this.createIndexWriter(String.valueOf(shardIndex), tableId);
        TaxonomyWriter taxonomyWriter = this.createTaxonomyIndexWriter(String.valueOf(shardIndex), tableId);
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
    
    private String trimNonTokenizedIndexStringField(String value) {
        if (value.length() > MAX_NON_TOKENIZED_INDEX_STRING_SIZE) {
            return value.substring(0, MAX_NON_TOKENIZED_INDEX_STRING_SIZE);
        } else {
            return value;
        }
    }
    
    private void checkAndAddDocEntry(Document doc, AnalyticsSchema.ColumnType type, String name, Object obj)
            throws AnalyticsIndexException {
        FieldType fieldType = new FieldType();
        fieldType.setStored(false);
        fieldType.setDocValuesType(DocValuesType.NUMERIC);
        fieldType.setTokenized(true);
        fieldType.setOmitNorms(true);
        fieldType.setIndexOptions(IndexOptions.DOCS);
        if (obj == null) {
            doc.add(new StringField(name, NULL_INDEX_VALUE, Store.NO));
            return;
        }
        switch (type) {
        case STRING:
            doc.add(new TextField(name, obj.toString(), Store.NO));
            doc.add(new StringField(Constants.NON_TOKENIZED_FIELD_PREFIX + name, 
                    this.trimNonTokenizedIndexStringField(obj.toString()), Store.NO));
            break;
        case INTEGER:
            fieldType.setNumericType(FieldType.NumericType.INT);
            fieldType.setNumericPrecisionStep(8);
            if (obj instanceof Number) {
                doc.add(new IntField(name, ((Number) obj).intValue(), fieldType));
            } else {
                doc.add(new StringField(name, obj.toString(), Store.NO));
            }
            break;
        case DOUBLE:
            fieldType.setNumericType(FieldType.NumericType.DOUBLE);
            if (obj instanceof Number) {
                doc.add(new DoubleField(name, ((Number) obj).doubleValue(), fieldType));
            } else {
                doc.add(new StringField(name, obj.toString(), Store.NO));
            }
            break;
        case LONG:
            fieldType.setNumericType(FieldType.NumericType.LONG);
            if (obj instanceof Number) {
                doc.add(new LongField(name, ((Number) obj).longValue(), fieldType));
            } else {
                doc.add(new StringField(name, obj.toString(), Store.NO));
            }
            break;
        case FLOAT:
            fieldType.setNumericType(FieldType.NumericType.FLOAT);
            fieldType.setNumericPrecisionStep(8);
            if (obj instanceof Number) {
                doc.add(new FloatField(name, ((Number) obj).floatValue(), fieldType));
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
        fieldType.freeze();
    }

    private void checkAndAddTaxonomyDocEntries(Document doc, AnalyticsSchema.ColumnType type,
                                                   String name, Object obj,
                                                   FacetsConfig facetsConfig)
            throws AnalyticsIndexException {
        if (obj == null) {
            doc.add(new StringField(name, NULL_INDEX_VALUE, Store.NO));
        }
        if (obj instanceof String && type == AnalyticsSchema.ColumnType.FACET) {
            facetsConfig.setMultiValued(name, true);
            facetsConfig.setHierarchical(name, true);
            String values = (String) obj;
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
        doc.add(new StringField(INDEX_ID_INTERNAL_FIELD, record.getId(), Store.YES));
        doc.add(new LongField(INDEX_INTERNAL_TIMESTAMP_FIELD, record.getTimestamp(), Store.NO));
        /* make the best effort to store in the given timestamp, or else, 
         * fall back to a compatible format, or else, lastly, string */
        String name;
        for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
            name = entry.getKey();
            this.checkAndAddDocEntry(doc, entry.getValue().getType(), name, record.getValue(name));
            this.checkAndAddTaxonomyDocEntries(doc, entry.getValue().getType(), name, record.getValue(name), config);
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

    private String generateDirPath(String shardId, String basePath, String tableId) {
        String indexStoreLoc = this.indexerInfo.getIndexStoreLocation();
        if (!indexStoreLoc.endsWith(File.separator)) {
            indexStoreLoc += File.separator;
        }
        return indexStoreLoc + shardId + basePath + tableId;
    }

    private Directory createDirectory(String shardId, String tableId) throws AnalyticsIndexException {
        return this.createDirectory(shardId, INDEX_DATA_FS_BASE_PATH, tableId);
    }
    
    private Directory createDirectory(String shardId, String basePath, String tableId) throws AnalyticsIndexException {
        String path = this.generateDirPath(shardId, basePath, tableId);
        try {
            return new NIOFSDirectory(Paths.get(path));
        } catch (Exception e) {
            throw new AnalyticsIndexException("Error in creating directory: " + e.getMessage(), e);
        }
    }
    
    private String generateShardedTableId(String shardId, String tableId) {
        return shardId + "_" + tableId;
    }
    
    private Directory lookupIndexDir(String shardId, String tableId) throws AnalyticsIndexException {
        String shardedTableId = this.generateShardedTableId(shardId, tableId);
        Directory indexDir = this.indexDirs.get(shardedTableId);
        if (indexDir == null) {
            synchronized (this.indexDirs) {
                indexDir = this.indexDirs.get(shardedTableId);
                if (indexDir == null) {
                    indexDir = this.createDirectory(shardId, tableId);
                    this.indexDirs.put(shardedTableId, indexDir);
                }
            }
        }
        return indexDir;
    }

    private Directory lookupTaxonomyIndexDir(String shardId, String tableId) throws AnalyticsIndexException {
        String shardedTableId = this.generateShardedTableId(shardId, tableId);
        Directory indexTaxonomyDir = this.indexTaxonomyDirs.get(shardedTableId);
        if (indexTaxonomyDir == null) {
            synchronized (this.indexTaxonomyDirs) {
                indexTaxonomyDir = this.indexTaxonomyDirs.get(shardedTableId);
                if (indexTaxonomyDir == null) {
                    indexTaxonomyDir = this.createDirectory(shardId, TAXONOMY_INDEX_DATA_FS_BASE_PATH, tableId);
                    this.indexTaxonomyDirs.put(shardedTableId, indexTaxonomyDir);
                }
            }
        }
        return indexTaxonomyDir;
    }
    
    private IndexWriter createIndexWriter(String shardId, String tableId) throws AnalyticsIndexException {
        Directory indexDir = this.lookupIndexDir(shardId, tableId);
        IndexWriterConfig conf = new IndexWriterConfig(this.indexerInfo.getLuceneAnalyzer());
        try {
            return new IndexWriter(indexDir, conf);
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in creating index writer: " + e.getMessage(), e);
        }
    }

    private TaxonomyWriter createTaxonomyIndexWriter(String shardId, String tableId) throws AnalyticsIndexException {
        Directory indexDir = this.lookupTaxonomyIndexDir(shardId, tableId);
        try {
            return new DirectoryTaxonomyWriter(indexDir, IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in creating index writer: " + e.getMessage(), e);
        }
    }
    
    public void clearIndexData(int tenantId, String tableName) throws AnalyticsException {
        this.indexNodeCoordinator.clearIndexData(tenantId, tableName);
    }
    
    public void clearIndexDataLocal(int tenantId, String tableName) throws AnalyticsIndexException {
        String tableId = this.generateTableId(tenantId, tableName);
        for (int shardIndex : this.localShards) {
            try {
                Directory dir = this.indexDirs.remove(this.generateShardedTableId(String.valueOf(shardIndex), tableId));
                if (dir != null) {
                    dir.close();
                }
                dir = this.indexTaxonomyDirs.remove(this.generateShardedTableId(String.valueOf(shardIndex), tableId));
                if (dir != null) {
                    dir.close();
                }
                try {
                    FileUtils.deleteDirectory(new File(this.generateDirPath(String.valueOf(shardIndex), 
                            TAXONOMY_INDEX_DATA_FS_BASE_PATH, tableId)));
                } catch (FileNotFoundException ignore) { }
                try {
                    FileUtils.deleteDirectory(new File(this.generateDirPath(String.valueOf(shardIndex), 
                            INDEX_DATA_FS_BASE_PATH, tableId)));
                } catch (FileNotFoundException ignore) { }                
            } catch (IOException e) {
                throw new AnalyticsIndexException("Error in clearing index data: " + e.getMessage(), e);
            }
        }
    }
    
    private String generateTableId(int tenantId, String tableName) {
        /* the table names are not case-sensitive */
        return tenantId + "_" + tableName.toLowerCase();
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
    
    public synchronized void stopAndCleanupIndexProcessing() {
        if (this.shardWorkerExecutor != null) {
            for (IndexWorker worker : this.workers) {
                worker.stop();
            }
            this.shardWorkerExecutor.shutdownNow();
            try {
                this.shardWorkerExecutor.awaitTermination(
                        org.wso2.carbon.analytics.dataservice.core.Constants.INDEX_WORKER_STOP_WAIT_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
                /* ignore */
            }
            this.workers = null;
            this.shardWorkerExecutor = null;
        }
    }

    public void close() throws AnalyticsIndexException {
        this.stopAndCleanupIndexProcessing();
        this.localIndexDataStore.close();
        this.indexNodeCoordinator.close();
        this.closeAndRemoveIndexDirs(new HashSet<String>(this.indexDirs.keySet()));
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
        final AnalyticsDataIndexer indexer = this;
        try {
            List<String[]> subCategories = getUniqueGroupings(tenantId, aggregateRequest);
            AnalyticsIterator<Record> iterator = new AggregateRecordIterator(tenantId, subCategories, aggregateRequest, indexer);
            return iterator;
        } catch (IOException e) {
            log.error("Error occured while performing aggregation, " + e.getMessage(), e);
            throw new AnalyticsIndexException("Error occured while performing aggregation, " + e.getMessage(), e);
        }
    }

	private List<String[]> getUniqueGroupings(int tenantId, AggregateRequest aggregateRequest)
            throws AnalyticsIndexException, IOException {
        if (aggregateRequest.getAggregateLevel() >= 0) {
            List<String> taxonomyShardIds = this.lookupGloballyExistingShardIds();
            if (taxonomyShardIds.size() == 0) {
                return new ArrayList<>();
            }
            ExecutorService pool = Executors.newFixedThreadPool(taxonomyShardIds.size());
            Set<Future<Set<String>>> perShardUniqueCategories = new HashSet<>();
            Set<String> finalUniqueCategories = new HashSet<>();
            for (int i = 0; i < taxonomyShardIds.size(); i++) {
                String shardedTableId = this.generateTableId(tenantId, aggregateRequest.getTableName());
                Directory taxonomyDir = this.lookupTaxonomyIndexDir(taxonomyShardIds.get(i), shardedTableId);
                TaxonomyReader reader = new DirectoryTaxonomyReader(taxonomyDir);
                Callable<Set<String>> callable = new TaxonomyWorker(reader, aggregateRequest);
                Future<Set<String>> result = pool.submit(callable);
                perShardUniqueCategories.add(result);
            }
            try {
                for (Future<Set<String>> result : perShardUniqueCategories) {
                    finalUniqueCategories.addAll(result.get());
                }
                return getUniqueSubCategories(aggregateRequest, finalUniqueCategories);
            } catch (Exception e) {
                log.error("Error while generating Unique categories for aggregation, " + e.getMessage(), e);
                throw new AnalyticsIndexException("Error while generating Unique categories for aggregation, " +
                                                  e.getMessage(), e);
            } finally {
                shutdownTaxonomyWorkerThreadPool(pool);
            }
        } else {
            throw new AnalyticsIndexException("Aggregate level cannot be less than zero");
        }
    }

    private void shutdownTaxonomyWorkerThreadPool(ExecutorService pool)
            throws AnalyticsIndexException {
        if (pool != null) {
            pool.shutdown();
        }
        try {
            if(!pool.awaitTermination(TAXONOMYWORKER_TIMEOUT, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error while shutting down the Taxonomyworker threadpool , " + e.getMessage(), e);
            throw new AnalyticsIndexException("Error while shutting down the Taxonomyworker threadpool , " +
                                              e.getMessage(), e);
        } finally {
            pool = null;
        }
    }

    private List<String[]> getUniqueSubCategories(AggregateRequest aggregateRequest,
                                                  Set<String> uniqueCategories)
            throws AnalyticsIndexException {
        List<String[]> groupings = new ArrayList<>();
        try {
            int totalAggregateLevel = aggregateRequest.getAggregateLevel() + 1;
            for (String category : uniqueCategories) {
                String[] path = category.split(PATH_SEPARATOR);
                if (path.length == totalAggregateLevel) {
                    groupings.add(path);
                }
            }
            return groupings;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new AnalyticsIndexException("The field: " + aggregateRequest.getGroupByField() +
                                              " do not have " + aggregateRequest.getAggregateLevel() +
                                              " layers of sub categories");
        }
    }

    private Record aggregatePerGrouping(int tenantId, String[] path,
                                         AggregateRequest aggregateRequest)
            throws AnalyticsException {
        Map<String, Number> optionalParams = new HashMap<>();
        Map<String, AggregateFunction> perAliasAggregateFunction = initPerAliasAggregateFunctions(aggregateRequest,
                optionalParams);
        AnalyticsDataResponse analyticsDataResponse = null;
        Record aggregatedRecord = null;
        List<SearchResultEntry> searchResultEntries = getRecordSearchEntries(tenantId, path, aggregateRequest);
        if (!searchResultEntries.isEmpty()) {
            List<String> recordIds = getRecordIds(searchResultEntries);
            analyticsDataResponse = this.indexerInfo.getAnalyticsDataService().get(
                    tenantId, aggregateRequest.getTableName(), 1, null, recordIds);
            RecordGroup[] recordGroups = analyticsDataResponse.getRecordGroups();
            if (recordGroups != null) {
                for (RecordGroup recordGroup : recordGroups) {
                    AnalyticsIterator<Record> iterator = this.indexerInfo.getAnalyticsDataService().readRecords(
                            analyticsDataResponse.getRecordStoreName(), recordGroup);
                    while (iterator.hasNext()) {
                        Record record = iterator.next();
                        for (AggregateField field : aggregateRequest.getFields()) {
                            Number value = (Number) record.getValue(field.getFieldName());
                            AggregateFunction function = perAliasAggregateFunction.get(field.getAlias());
                            function.process(value);
                        }
                    }
                    Map<String, Object> aggregatedValues = generateAggregateRecordValues(path, aggregateRequest,
                            perAliasAggregateFunction);
                    aggregatedRecord = new Record(tenantId, aggregateRequest.getTableName(), aggregatedValues);
                }
            }
        }
        return aggregatedRecord;
    }

    private Map<String, Object> generateAggregateRecordValues(String[] path,
                                                              AggregateRequest aggregateRequest,
                                                              Map<String, AggregateFunction> perAliasAggregateFunction)
            throws AnalyticsException {
        Map<String, Object> aggregatedValues = new HashMap<>();
        for (AggregateField field : aggregateRequest.getFields()) {
            String alias = field.getAlias();
            Number result = perAliasAggregateFunction.get(alias).finish();
            aggregatedValues.put(alias, result);
        }
        aggregatedValues.put(aggregateRequest.getGroupByField(),
                             path);
        return aggregatedValues;
    }

    private Map<String, AggregateFunction> initPerAliasAggregateFunctions(
            AggregateRequest aggregateRequest, Map<String, Number> optionalParams)
            throws AnalyticsException {
        Map<String, AggregateFunction> perAliasAggregateFunction = new HashMap<>();
        for (AggregateField field : aggregateRequest.getFields()) {
            AggregateFunction function = getAggregateFunctionFactory().create(field.getAggregateFunction(), optionalParams);
            if (function == null) {
                throw new AnalyticsException("Unknown aggregate function!");
            } else if (field.getFieldName() == null || field.getFieldName().isEmpty()) {
                throw new AnalyticsException("One of the aggregating fields is not provided");
            } else if (field.getAlias() == null || field.getAlias().isEmpty()) {
                throw new AnalyticsException("One of the aggregating field alias is not provided");
            }
            perAliasAggregateFunction.put(field.getAlias(), function);
        }
        return perAliasAggregateFunction;
    }

    private List<SearchResultEntry> getRecordSearchEntries(int tenantId, String[] path,
                                                           AggregateRequest aggregateRequest)
            throws AnalyticsIndexException {
        AnalyticsDrillDownRequest analyticsDrillDownRequest = new AnalyticsDrillDownRequest();
        analyticsDrillDownRequest.setTableName(aggregateRequest.getTableName());
        analyticsDrillDownRequest.setQuery(aggregateRequest.getQuery());
        analyticsDrillDownRequest.setRecordStartIndex(0);
        analyticsDrillDownRequest.setRecordCount(Integer.MAX_VALUE);
        Map<String, List<String>> groupByCategory = new HashMap<>();
        List<String> groupByValue = new ArrayList<>();
        groupByValue.addAll(Arrays.asList(path));
        groupByCategory.put(aggregateRequest.getGroupByField(), groupByValue);
        analyticsDrillDownRequest.setCategoryPaths(groupByCategory);
        return this.getDrillDownRecords(tenantId, analyticsDrillDownRequest, null, null);
    }

    private static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<>();
        for (SearchResultEntry searchResult : searchResults) {
            ids.add(searchResult.getId());
        }
        return ids;
    }

    private static class AggregateRecordIterator implements AnalyticsIterator<Record> {

        private static Log logger = LogFactory.getLog(AggregateRecordIterator.class);
        private AggregateRequest request;
        private List<String[]> groupings;
        private int tenantId;
        private String[] currentGrouping;
        private AnalyticsDataIndexer indexer;
        private Record currentRecord;
        public AggregateRecordIterator(int tenantId, List<String[]> uniqueGroupings,
                                       AggregateRequest request, AnalyticsDataIndexer indexer) {
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
            if (groupings!= null && !groupings.isEmpty()) {
                currentGrouping = groupings.get(0);
                if (currentGrouping != null && currentGrouping.length > 0) {
                    try {
                        if (currentRecord != null) {
                            return true;
                        } else {
                            currentRecord = indexer.aggregatePerGrouping(tenantId, currentGrouping, request);
                        }
                    } catch (AnalyticsException e) {
                        logger.error("Failed to create aggregated record: " + e.getMessage(), e);
                        throw new RuntimeException("Error while iterating aggregate records: " + e.getMessage(), e);
                    }
                    if (currentRecord == null) {
                        groupings.remove(currentGrouping);
                        return  this.hasNext();
                    } else {
                        return true;
                    }
                } else {
                    groupings.remove(currentGrouping);
                    this.hasNext();
                }
            } else {
                    return false;
            }
            return false;
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
    }

    /**
     * This represents an indexing worker, who does index operations in the background.
     */
    private class IndexWorker implements Runnable {

        private static final int INDEX_WORKER_SLEEP_TIME = 1500;
        
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
                    Thread.sleep(INDEX_WORKER_SLEEP_TIME);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private class TaxonomyWorker implements Callable<Set<String>> {

        private TaxonomyReader reader;
        private AggregateRequest request;

        public TaxonomyWorker(TaxonomyReader reader, AggregateRequest request) {
            this.reader = reader;
            this.request = request;
        }

        @Override
        public Set<String> call() throws Exception {
            List<String> parentPath = request.getParentPath();
            if (parentPath == null) {
                parentPath = new ArrayList<>();
            }
            Set<String> perShardCategorySet = new TreeSet<>();
            String[] path = parentPath.toArray(new String[parentPath.size()]);
            int ordinal = reader.getOrdinal(request.getGroupByField(), path);
            this.addAllCategoriesToSet(reader, ordinal, null, 1, perShardCategorySet);
            return perShardCategorySet;
        }

        private void addAllCategoriesToSet(TaxonomyReader r, int ord, String parent, int depth,
                                           Set<String> uniqueGroups) throws IOException {
            TaxonomyReader.ChildrenIterator it = r.getChildren(ord);
            int child;
            while ((child = it.next()) != TaxonomyReader.INVALID_ORDINAL) {
                String newParent;
                if (parent != null) {
                    newParent = parent + PATH_SEPARATOR + r.getPath(child).components[depth];
                } else {
                    newParent = r.getPath(child).components[depth];
                }
                uniqueGroups.add(newParent);
                addAllCategoriesToSet(r, child, newParent, depth + 1, uniqueGroups);
            }
        }
        
    }
    
    public static class SearchCountCall implements Callable<Integer>, Serializable {

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
                return adsImpl.getIndexer().doSearchCount(this.tenantId, this.tableName, this.query);
            }
            return 0;
        }
        
    }
    
    public static class SearchCall implements Callable<List<SearchResultEntry>>, Serializable {

        private static final long serialVersionUID = -6551068087138398124L;

        private int tenantId;
        
        private String tableName;
        
        private String query;
        
        private int start;
        
        private int count;
        
        public SearchCall(int tenantId, String tableName, String query, int start, int count) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.query = query;
            this.start = start;
            this.count = count;
        }
        
        @Override
        public List<SearchResultEntry> call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                return adsImpl.getIndexer().doSearch(this.tenantId, this.tableName, this.query, this.start, this.count);
            }
            return new ArrayList<>();
        }
        
    }
    
}
