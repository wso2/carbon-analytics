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

import org.apache.commons.collections.IteratorUtils;
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
import org.apache.lucene.store.NoLockFactory;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.AnalyticsDirectory;
import org.wso2.carbon.analytics.dataservice.AnalyticsQueryParser;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.clustering.GroupEventListener;
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
import org.wso2.carbon.analytics.dataservice.indexing.AnalyticsIndexedTableStore.IndexedTableId;
import org.wso2.carbon.analytics.dataservice.indexing.aggregates.AVGAggregate;
import org.wso2.carbon.analytics.dataservice.indexing.aggregates.Aggregate;
import org.wso2.carbon.analytics.dataservice.indexing.aggregates.COUNTAggregate;
import org.wso2.carbon.analytics.dataservice.indexing.aggregates.MAXAggregate;
import org.wso2.carbon.analytics.dataservice.indexing.aggregates.MINAggregate;
import org.wso2.carbon.analytics.dataservice.indexing.aggregates.SUMAggregate;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

    private static final String ENABLE_INDEXING_STATS_SYS_PROP = "enableIndexingStats";

    private static final Log log = LogFactory.getLog(AnalyticsDataIndexer.class);

    private static final int INDEX_WORKER_STOP_WAIT_TIME = 60;

    private static final int INDEXING_SCHEDULE_PLAN_RETRY_COUNT = 3;

    public static final String DISABLE_INDEXING_ENV_PROP = "disableIndexing";
    
    private static final int WAIT_INDEX_TIME_INTERVAL = 1000;

    private static final String INDEX_OP_DATA_ATTRIBUTE = "__INDEX_OP_DATA__";
    
    public static final int INDEX_DATA_RECORD_TENANT_ID = -1000;
    
    public static final String INDEX_DATA_RECORD_TABLE_NAME = "__INDEX_DATA__";
    
    private static final int SHARD_INDEX_RECORD_BATCH_SIZE = 1000;
    
    private static final String ANALYTICS_INDEXING_GROUP = "__ANALYTICS_INDEXING_GROUP__";

    private static final String INDEX_DATA_FS_BASE_PATH = "/_data/index/";

    private static final String TAXONOMY_INDEX_DATA_FS_BASE_PATH = "/_data/taxonomy/" ;

    public static final String INDEX_ID_INTERNAL_FIELD = "_id";

    public static final String INDEX_INTERNAL_TIMESTAMP_FIELD = "_timestamp";

    private static final String INDEX_INTERNAL_SCORE_FIELD = "_score";

    private static final String NULL_INDEX_VALUE = "";

    private static final String EMPTY_FACET_VALUE = "EMPTY_FACET_VALUE!";

    private static final String DEFAULT_SCORE = "1";

    private Map<String, Directory> indexDirs = new HashMap<>();

    private Map<String, Directory> indexTaxonomyDirs = new HashMap<>();

    private Map<String, Aggregate> aggregates = new HashMap<>();
    
    private Analyzer luceneAnalyzer;
    
    private AnalyticsFileSystem analyticsFileSystem;
    
    private AnalyticsRecordStore analyticsRecordStore;

    private AnalyticsDataService analyticsDataService;
    
    private int shardCount;
    
    private ExecutorService shardWorkerExecutor;
    
    private List<IndexWorker> workers;
    
    private AnalyticsIndexedTableStore indexedTableStore;
    
    private boolean indexingStatsEnabled;
    
    private AnalyticsDataIndexingStatsCollector statsCollector;
    
    public AnalyticsDataIndexer(AnalyticsRecordStore analyticsRecordStore, 
            AnalyticsFileSystem analyticsFileSystem, AnalyticsDataService analyticsDataService,
            AnalyticsIndexedTableStore indexedTableStore, int shardCount, Analyzer analyzer) throws AnalyticsException {
    	this.luceneAnalyzer = analyzer;
        this.analyticsRecordStore = analyticsRecordStore;    	
    	this.analyticsFileSystem = analyticsFileSystem;
        this.analyticsDataService = analyticsDataService;
        this.indexedTableStore = indexedTableStore;
    	this.shardCount = shardCount;        
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
        this.initializeIndexingSchedules();
    }

    private boolean checkIfIndexingNode() {
        String indexDisableProp =  System.getProperty(DISABLE_INDEXING_ENV_PROP);
        return !(indexDisableProp != null && Boolean.parseBoolean(indexDisableProp));
    }
    
    private void initializeIndexingSchedules() throws AnalyticsException {
        if (!this.checkIfIndexingNode()) {
            return;
        }
        this.getAnalyticsRecordStore().createTable(INDEX_DATA_RECORD_TENANT_ID, INDEX_DATA_RECORD_TABLE_NAME);
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            log.info("Analytics Indexing Mode: CLUSTERED");
            acm.joinGroup(ANALYTICS_INDEXING_GROUP, this);
        } else {
            log.info("Analytics Indexing Mode: STANDALONE");
            List<Integer[]> indexingSchedule = this.generateIndexWorkerSchedulePlan(1);
            this.scheduleWorkers(indexingSchedule.get(0));
        }
    }
    
    private List<Integer[]> generateIndexWorkerSchedulePlan(int numWorkers) {
        return GenericUtils.splitNumberRange(this.getShardCount(), numWorkers);
    }
    
    private void scheduleWorkers(Integer[] shardInfo) throws AnalyticsException {
        int shardIndexFrom = shardInfo[0];
        int shardRange = shardInfo[1];
        this.stopAndCleanupIndexProcessing();
        int threadCount = this.getIndexingThreadCount();
        threadCount = Math.min(threadCount, shardRange);
        this.shardWorkerExecutor = Executors.newFixedThreadPool(threadCount);
        this.workers = new ArrayList<IndexWorker>(threadCount);
        IndexWorker worker;
        int range = Math.max(1, shardRange / threadCount);
        int current = shardIndexFrom;
        Map<Integer, Integer> shardDetails = new HashMap<Integer, Integer>();
        int tmpRange;
        for (int i = 0; i < threadCount; i++) {
            if (i + 1 >= threadCount) {
                tmpRange = shardIndexFrom + shardRange - current;
                worker = new IndexWorker(current, tmpRange);                
            } else {
                tmpRange = current + range > shardIndexFrom + shardRange ? shardIndexFrom + shardRange - current : range;
                worker = new IndexWorker(current, tmpRange);
            }
            shardDetails.put(current, tmpRange);
            this.workers.add(worker);
            this.shardWorkerExecutor.execute(worker);
            current += range;
        }
        log.info("Processing Analytics Indexing Shards " + shardDetails);
    }
    
    public boolean isIndexingStatsEnabled() {
        return indexingStatsEnabled;
    }
    
    private int getIndexingThreadCount() {
        return Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
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
    
    private long createIndexProcessId(Record record) {
        return this.createIndexProcessId(record.getTenantId(), record.getTableName(), record.getId());
    }
    
    private long createIndexProcessId(int tenantId, String tableName, String id) {
        int shardId = this.calculateShardId(id);
        return this.createIndexProcessId(tenantId, tableName, shardId);
    }
    
    private long createIndexProcessId(int tenantId, String tableName, int shardId) {
        String tableId = GenericUtils.calculateTableIdentity(tenantId, tableName);
        return Math.abs(tableId.hashCode()) + shardId;
    }
    
    private Record createIndexProcessDataUpdateRecord(long processId, List<Record> records) {
        Map<String, Object> values = new HashMap<String, Object>(1);
        values.put(INDEX_OP_DATA_ATTRIBUTE, records);
        return new Record(GenericUtils.generateRecordID(), INDEX_DATA_RECORD_TENANT_ID, INDEX_DATA_RECORD_TABLE_NAME, 
                values, processId);
    }
    
    private Record createIndexProcessDataDeleteRecord(long processId, DeleteIndexEntry[] ids) {
        Map<String, Object> values = new HashMap<String, Object>(1);
        values.put(INDEX_OP_DATA_ATTRIBUTE, ids);
        return new Record(GenericUtils.generateRecordID(), INDEX_DATA_RECORD_TENANT_ID, INDEX_DATA_RECORD_TABLE_NAME, 
                values, processId);
    }
    
    private List<Record> createIndexUpdateRecords(List<Record> records) {
        Map<Long, List<Record>> data = new HashMap<Long, List<Record>>();
        long processId;
        List<Record> recordList;
        for (Record record : records) {
            processId = this.createIndexProcessId(record);
            recordList = data.get(processId);
            if (recordList == null) {
                recordList = new ArrayList<Record>();
                data.put(processId, recordList);
            }
            recordList.add(record);
        }
        List<Record> result = new ArrayList<Record>(data.size());
        for (Map.Entry<Long, List<Record>> entry : data.entrySet()) {
            result.add(this.createIndexProcessDataUpdateRecord(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private List<Record> createIndexDeleteRecords(int tenantId, String tableName, List<String> ids) {
        Map<Long, List<DeleteIndexEntry>> data = new HashMap<Long, List<DeleteIndexEntry>>();
        long processId;
        List<DeleteIndexEntry> deleteEntryList;
        for (String id : ids) {
            processId = this.createIndexProcessId(tenantId, tableName, id);
            deleteEntryList = data.get(processId);
            if (deleteEntryList == null) {
                deleteEntryList = new ArrayList<DeleteIndexEntry>();
                data.put(processId, deleteEntryList);
            }
            deleteEntryList.add(new DeleteIndexEntry(tenantId, tableName, id));
        }
        List<Record> result = new ArrayList<Record>(data.size());
        for (Map.Entry<Long, List<DeleteIndexEntry>> entry : data.entrySet()) {
            result.add(this.createIndexProcessDataDeleteRecord(entry.getKey(), 
                    entry.getValue().toArray(new DeleteIndexEntry[0])));
        }
        return result;
    }
    
    private void scheduleIndexUpdate(List<Record> records) throws AnalyticsException {
        List<Record> indexRecords = this.createIndexUpdateRecords(records);
        this.getAnalyticsRecordStore().put(indexRecords);
    }
    
    private void scheduleIndexDelete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        List<Record> indexRecords = this.createIndexDeleteRecords(tenantId, tableName, ids);
        this.getAnalyticsRecordStore().put(indexRecords);
    }
    
    private void processIndexOperations(int shardIdFrom, int shardRange) throws AnalyticsException {
        int count = SHARD_INDEX_RECORD_BATCH_SIZE;
        int tmpCount;
        /* continue processing operations in this until there aren't any to be processed */
        while (count >= SHARD_INDEX_RECORD_BATCH_SIZE) {
            count = 0;
            for (IndexedTableId indexTableId : this.indexedTableStore.getAllIndexedTables()) {
                tmpCount = this.processIndexOperations(indexTableId.getTenantId(), 
                        indexTableId.getTableName(), shardIdFrom, shardRange, SHARD_INDEX_RECORD_BATCH_SIZE);
                count = Math.max(count, tmpCount);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private int processIndexOperations(int tenantId, String tableName, int shardIdFrom, int shardRange,
            int count) throws AnalyticsException {
        List<Record> indexRecords = this.loadIndexOperationRecords(tenantId, tableName, shardIdFrom, shardRange, count);
        if (indexRecords.size() == 0) {
            return 0;
        }
        Object[] result = this.checkAndExtractInsertDeleteIndexOperationBatches(tenantId, tableName, 
                shardIdFrom, shardRange, indexRecords);
        List<Object> indexObjs = this.entriesListToIndexObjects((List<Object>) result[0]);
        List<String> processedIds = (List<String>) result[1];
        Map<Integer, List<Record>> shardRecordMap;
        Map<Integer, List<String>> shardIdMap;
        for (Object indexObj : indexObjs) {
            if (indexObj instanceof String[]) {
                shardIdMap = this.groupByShards((String[]) indexObj);
                for (Map.Entry<Integer, List<String>> entry : shardIdMap.entrySet()) {
                    this.deleteInIndex(tenantId, tableName, entry.getKey(), entry.getValue());
                }
            } else if (indexObj instanceof List<?>) {
                shardRecordMap = this.groupByShards((List<Record>) indexObj);
                for (Map.Entry<Integer, List<Record>> entry : shardRecordMap.entrySet()) {
                    this.updateIndex(entry.getKey(), entry.getValue(), this.lookupIndices(tenantId, tableName));
                }                
            }
        }
        this.deleteIndexRecords(processedIds);
        return indexRecords.size();
    }
    
    private Map<Integer, List<Record>> groupByShards(List<Record> records) {
        Map<Integer, List<Record>> result = new HashMap<Integer, List<Record>>();
        int shardId;
        List<Record> recordList;
        for (Record record : records) {
            shardId = this.calculateShardId(record.getId());
            recordList = result.get(shardId);
            if (recordList == null) {
                recordList = new ArrayList<Record>();
                result.put(shardId, recordList);
            }
            recordList.add(record);
        }
        return result;
    }
    
    private Map<Integer, List<String>> groupByShards(String[] ids) {
        Map<Integer, List<String>> result = new HashMap<Integer, List<String>>();
        int shardId;
        List<String> idList;
        for (String id : ids) {
            shardId = this.calculateShardId(id);
            idList = result.get(shardId);
            if (idList == null) {
                idList = new ArrayList<String>();
                result.put(shardId, idList);
            }
            idList.add(id);
        }
        return result;
    }
    
    private List<Object> entriesListToIndexObjects(List<Object> entriesList) {
        List<Record> updateRecords = new ArrayList<Record>();
        List<String> deleteRecords = new ArrayList<String>();
        List<Object> result = new ArrayList<Object>();
        for (Object obj : entriesList) {
            if (obj instanceof Record) {
                if (!deleteRecords.isEmpty()) {
                    result.add(deleteRecords.toArray(new String[deleteRecords.size()]));
                    deleteRecords = new ArrayList<String>();
                }
                updateRecords.add((Record) obj);
            } else if (obj instanceof String) {
                if (!updateRecords.isEmpty()) {
                    result.add(updateRecords);
                    updateRecords = new ArrayList<Record>();
                }
                deleteRecords.add((String) obj);
            }
        }
        if (!deleteRecords.isEmpty()) {
            result.add(deleteRecords.toArray(new String[deleteRecords.size()]));
        }
        if (!updateRecords.isEmpty()) {
            result.add(updateRecords);
        }
        return result;
    }
    
    /* The logic in this method that is used to again group the records by table identity is important,
     * where the earlier query using process ids does not guarantee unique table/tenant-id combination,
     * because there can be collisions when generating the process ids, if collisions are found, the 
     * records that does not belong to this tenant/table/shard is re-inserted to the data store after
     * extracting my records, and only the records that contains my records are returned as processed 
     * records to be deleted later */
    @SuppressWarnings("unchecked")
    private Object[] checkAndExtractInsertDeleteIndexOperationBatches(int tenantId, String tableName,
            int shardIdFrom, int shardRange, List<Record> indexRecords) throws AnalyticsException {
        List<Object> entriesList = new ArrayList<Object>();
        List<String> processedIds = new ArrayList<String>();
        List<Object> foreignEntries = new ArrayList<Object>();
        List<Record> foreignRecords = new ArrayList<Record>(0);
        List<DeleteIndexEntry> foreignDeleteEntries = new ArrayList<DeleteIndexEntry>(0);
        boolean processed;
        for (Record indexRecord : indexRecords) {
            Object value = indexRecord.getValue(INDEX_OP_DATA_ATTRIBUTE);
            if (value instanceof List<?>) {
                processed = false;
                for (Record record : (List<Record>) value) {
                    if (this.checkRecordUpdateProcessEntitlement(record, tenantId, tableName, shardIdFrom, shardRange)) {
                        processed = true;
                        entriesList.add(record);
                    } else {
                        foreignRecords.add(record);
                    }
                }
                if (processed) {
                    processedIds.add(indexRecord.getId());
                    if (!foreignRecords.isEmpty()) {
                        foreignEntries.add(foreignRecords);
                        foreignRecords = new ArrayList<Record>();
                    }
                }
            } else if (value instanceof DeleteIndexEntry[]) {
                processed = false;
                foreignDeleteEntries.clear();
                for (DeleteIndexEntry deleteEntry : (DeleteIndexEntry[]) value) {
                    if (this.checkRecordDeleteProcessEntitlement(deleteEntry, tenantId, tableName, shardIdFrom, shardRange)) {
                        processed = true;
                        entriesList.add(deleteEntry.getId());                    
                    } else {
                        foreignDeleteEntries.add(deleteEntry);
                    }
                }
                if (processed) {
                    processedIds.add(indexRecord.getId());
                    if (!foreignDeleteEntries.isEmpty()) {
                        foreignEntries.add(foreignDeleteEntries.toArray(new DeleteIndexEntry[foreignDeleteEntries.size()]));
                        foreignDeleteEntries = new ArrayList<AnalyticsDataIndexer.DeleteIndexEntry>();
                    }
                }
            } else {
                log.error("Corrupted index operation from index record, deleting index record with table name: " + 
                        indexRecord.getTableName() + " id: " + indexRecord.getId());
                this.deleteIndexRecords(Arrays.asList(indexRecord.getId()));
            }
        }
        this.handleForeignIndexEntries(foreignEntries);
        return new Object[] { entriesList, processedIds };
    }
    
    @SuppressWarnings("unchecked")
    private void handleForeignIndexEntries(List<Object> foreignEntries) throws AnalyticsException {
        for (Object obj : foreignEntries) {
            if (obj instanceof List<?>) {
                this.scheduleIndexUpdate((List<Record>) obj);
            } else if (obj instanceof DeleteIndexEntry[]) {
                DeleteIndexEntry[] entries = (DeleteIndexEntry[]) obj;
                int tenantId = entries[0].getTenantId();
                String tableName = entries[0].getTableName();
                List<String> ids = new ArrayList<String>(entries.length);
                for (DeleteIndexEntry entry : entries) {
                    ids.add(entry.getId());
                }
                this.scheduleIndexDelete(tenantId, tableName, ids);
            }
        }
    }
    
    private boolean checkRecordUpdateProcessEntitlement(Record record, int tenantId, String tableName, 
            int shardIdFrom, int shardRange) {
        for (int i = shardIdFrom; i < shardIdFrom + shardRange; i++) {
            if (tenantId == record.getTenantId() && tableName.equals(record.getTableName()) && 
                    this.calculateShardId(record.getId()) == i) {
                return true;
            }
        }
        return false;
    }
    
    private boolean checkRecordDeleteProcessEntitlement(DeleteIndexEntry deleteEntry, int tenantId, String tableName, 
            int shardIdFrom, int shardRange) {
        for (int i = shardIdFrom; i < shardIdFrom + shardRange; i++) {
            if (tenantId == deleteEntry.getTenantId() && tableName.equals(deleteEntry.getTableName()) && 
                    this.calculateShardId(deleteEntry.getId()) == i) {
                return true;
            }
        }
        return false;
    }
    
    private void deleteIndexRecords(List<String> processedIds) throws AnalyticsException {
        this.getAnalyticsRecordStore().delete(INDEX_DATA_RECORD_TENANT_ID, INDEX_DATA_RECORD_TABLE_NAME, processedIds);
    }
    
    private List<Record> loadIndexOperationRecords(int tenantId, String tableName, int shardIdFrom, int shardRange,
            int count) throws AnalyticsException {
        long processIdStart = this.createIndexProcessId(tenantId, tableName, shardIdFrom);
        long processIdEnd = this.createIndexProcessId(tenantId, tableName, shardIdFrom + shardRange);
        return GenericUtils.listRecords(this.getAnalyticsRecordStore(), this.getAnalyticsRecordStore().get(
                INDEX_DATA_RECORD_TENANT_ID, INDEX_DATA_RECORD_TABLE_NAME, 1, 
                null, processIdStart, processIdEnd, 0, count));
    }
    
    private int calculateShardId(String id) {
        return Math.abs(id.hashCode()) % this.getShardCount();
    }
    
    private String generateGlobalPath(String basepath, int tenantId, String tableName) {
        return this.generateDirPath(basepath, this.generateTableId(tenantId, tableName));
    }
    
    private List<String> lookupGloballyExistingShardIds(String basepath, int tenantId, String tableName)
            throws AnalyticsIndexException {
        String globalPath = this.generateGlobalPath(basepath, tenantId, tableName);
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
            TopScoreDocCollector collector = TopScoreDocCollector.create(count);
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
            perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(this.luceneAnalyzer);
        } else {
            perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(this.luceneAnalyzer, analyzersPerField);
        }
        return perFieldAnalyzerWrapper;
    }

    public int searchCount(int tenantId, String tableName, String query) throws AnalyticsIndexException {
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
        Map<String, ColumnDefinition> indices = this.lookupIndices(tenantId, drillDownRequest.getTableName());
        Query indexQuery = new MatchAllDocsQuery();
        FacetsCollector fc = new FacetsCollector();
        if (drillDownRequest.getQuery() != null) {
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
                                                     String rangeField,AnalyticsDrillDownRange range)
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

    public Map<String, Aggregate> getAggregates() {
        if (this.aggregates.isEmpty()) {
            aggregates.put(Constants.SUM_AGGREGATE, new SUMAggregate());
            aggregates.put(Constants.AVG_AGGREGATE, new AVGAggregate());
            aggregates.put(Constants.COUNT_AGGREGATE, new COUNTAggregate());
            aggregates.put(Constants.MIN_AGGREGATE, new MINAggregate());
            aggregates.put(Constants.MAX_AGGREGATE, new MAXAggregate());
        }
        return this.aggregates;
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
        String tableName = drillDownRequest.getTableName();
        List<String> taxonomyShardIds = this.lookupGloballyExistingShardIds(TAXONOMY_INDEX_DATA_FS_BASE_PATH,
                                                                            tenantId, tableName);
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
                                                                            tenantId, drillDownRequest.getTableName());
        List<CategorySearchResultEntry> categoriesPerShard = new ArrayList<>();
        for (String shardId : taxonomyShardIds) {
            categoriesPerShard.addAll(this.drillDownCategoriesPerShard(tenantId, shardId, drillDownRequest));
        }
        return categoriesPerShard;
    }

    public double getDrillDownRecordCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest,
                                         String rangeField, AnalyticsDrillDownRange range)
            throws AnalyticsIndexException {
        String tableName = drillDownRequest.getTableName();
        List<String> taxonomyShardIds = this.lookupGloballyExistingShardIds(TAXONOMY_INDEX_DATA_FS_BASE_PATH,
                tenantId, tableName);
        double totalCount = 0;
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

    private double getDrillDownRecordCountPerShard(int tenantId,
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
    
    private void deleteInIndex(int tenantId, String tableName, int shardIndex, List<String> ids) throws AnalyticsException {
        String tableId = this.generateShardedTableId(tenantId, tableName, Integer.toString(shardIndex));
        IndexWriter indexWriter = this.createIndexWriter(tableId);
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
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                log.error("Error closing index writer: " + e.getMessage(), e);
            }
        }
    }
    
    private void updateIndex(int shardIndex, List<Record> recordBatch, 
            Map<String, ColumnDefinition> columns) throws AnalyticsIndexException {
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
            if (this.isIndexingStatsEnabled()) {
                this.statsCollector.processedRecords(recordBatch.size());
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
            doc.add(new StringField(Constants.NON_TOKENIZED_FIELD_PREFIX + name, obj.toString(), Store.NO));
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
            AnalyticsSchema schema = this.analyticsDataService.getTableSchema(tenantId, tableName);
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

    private String generateDirPath(String basePath, String tableId) {
        return basePath + tableId;
    }

    private Directory createDirectory(String tableId) throws AnalyticsIndexException {
        return this.createDirectory(INDEX_DATA_FS_BASE_PATH, tableId);
    }
    
    private Directory createDirectory(String basePath, String tableId) throws AnalyticsIndexException {
        String path = this.generateDirPath(basePath, tableId);
        try {
            return new AnalyticsDirectory(this.getFileSystem(), NoLockFactory.INSTANCE, path);
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
        IndexWriterConfig conf = new IndexWriterConfig(this.luceneAnalyzer);
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
    
    public void clearIndexData(int tenantId, String tableName) throws AnalyticsIndexException {
        /* delete all global index data, not only local ones */
        String globalFacetPath = this.generateGlobalPath(TAXONOMY_INDEX_DATA_FS_BASE_PATH, tenantId, tableName);
        String globalIndexPath = this.generateGlobalPath(INDEX_DATA_FS_BASE_PATH, tenantId, tableName);
        try {
            this.getFileSystem().delete(globalFacetPath);
            this.getFileSystem().delete(globalIndexPath);
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in clearing index data for tenant: " + 
                    tenantId + " table: " + tableName + " : " + e.getMessage(), e);
        }
    }
    
    private String generateShardedTableId(int tenantId, String tableName, String shardId) {
        /* the table names are not case-sensitive */
        return this.generateTableId(tenantId, tableName) + "/" + shardId;
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
    }
    
    public void waitForIndexing(long maxWait) throws AnalyticsException, AnalyticsTimeoutException {
        for (IndexedTableId indexedTableId : this.indexedTableStore.getAllIndexedTables()) {
            this.waitForIndexing(indexedTableId.getTenantId(), indexedTableId.getTableName(), maxWait);
        }
    }
    
    public void waitForIndexing(int tenantId, String tableName, long maxWait) 
            throws AnalyticsException, AnalyticsTimeoutException {
        if (maxWait < 0) {
            maxWait = Long.MAX_VALUE;
        }
        long start = System.currentTimeMillis(), end;
        while (true) {
            if (this.loadIndexOperationRecords(tenantId, tableName, 0, this.getShardCount(), 1).isEmpty()) {
                break;
            }
            end = System.currentTimeMillis();
            if (end - start > maxWait) {
                throw new AnalyticsTimeoutException("Timed out at waitForIndexing: " + (end - start));
            }
            try {
                Thread.sleep(WAIT_INDEX_TIME_INTERVAL);
            } catch (InterruptedException e) {
                throw new AnalyticsException("Wait For Indexing Interrupted: " + e.getMessage(), e);
            }
        }
    }
    
    private void planIndexingWorkersInCluster() throws AnalyticsException {
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        int retryCount = 0;
        while (retryCount < INDEXING_SCHEDULE_PLAN_RETRY_COUNT) {
            try {
                acm.executeAll(ANALYTICS_INDEXING_GROUP, new IndexingStopMessage());
                List<Object> members = acm.getMembers(ANALYTICS_INDEXING_GROUP);
                List<Integer[]> schedulePlan = this.generateIndexWorkerSchedulePlan(members.size());
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
    public void onMembersChangeForLeader(boolean removed) {
        try {
            this.planIndexingWorkersInCluster();
        } catch (AnalyticsException e) {
            log.error("Error in planning indexing workers on members change: " + e.getMessage(), e);
        }
    }

    public List<Record> searchWithAggregates(int tenantId, AggregateRequest aggregateRequest)
            throws AnalyticsException {
        List<CategorySearchResultEntry> facetValues = getUniqueGroupings(tenantId, aggregateRequest);
        List<Record> aggregatedRecords = new ArrayList<>();
        for (CategorySearchResultEntry facetValue : facetValues) {
            Record aggregatedRecord = aggregatePerGrouping(tenantId, facetValue, aggregateRequest);
            aggregatedRecords.add(aggregatedRecord);
        }
        return aggregatedRecords;
    }

    private List<CategorySearchResultEntry> getUniqueGroupings(int tenantId,
                                                               AggregateRequest aggregateRequest)
            throws AnalyticsIndexException {
        CategoryDrillDownRequest categoryDrillDownRequest = new CategoryDrillDownRequest();
        categoryDrillDownRequest.setFieldName(aggregateRequest.getGroupByField());
        categoryDrillDownRequest.setPath(new String[]{});
        categoryDrillDownRequest.setQuery(aggregateRequest.getQuery());
        categoryDrillDownRequest.setTableName(aggregateRequest.getTableName());
        return this.drilldownCategories(tenantId, categoryDrillDownRequest).getCategories();
    }

    private Record aggregatePerGrouping(int tenantId, CategorySearchResultEntry facetValue, AggregateRequest aggregateRequest)
            throws AnalyticsException {
        Map<String,  Object> optionalParams = new HashMap<>();
        optionalParams.put(Constants.AggregateOptionalParams.COUNT, facetValue.getScore());
        Map<String, Object> aggregatedValues = new HashMap<>();
        for (AggregateField field : aggregateRequest.getFields()) {
            Iterator<Record> iterator = IteratorUtils.chainedIterator(getRecordIterators(tenantId, facetValue, aggregateRequest));
            Aggregate function = this.getAggregates().get(field.getAggregate());
            Object aggregatedValue = function.aggregate(iterator, field.getFieldName(), optionalParams);
            aggregatedValues.put(field.getAlias(), aggregatedValue);
        }
        aggregatedValues.put(aggregateRequest.getGroupByField(), facetValue.getCategoryValue());
        return new Record(tenantId, aggregateRequest.getTableName(), aggregatedValues);
    }


    private List<Iterator<Record>> getRecordIterators(int tenantId,
                                                      CategorySearchResultEntry facetValue,
                                                      AggregateRequest aggregateRequest)
            throws AnalyticsException {
        List<SearchResultEntry> searchResultEntries = getRecordSearchEntries(tenantId, facetValue, aggregateRequest);
        List<String> recordIds = getRecordIds(searchResultEntries);
        AnalyticsDataResponse analyticsDataResponse = this.analyticsDataService.get(tenantId, aggregateRequest.getTableName(),
                                                                                    1, null, recordIds);
        RecordGroup[] recordGroups = analyticsDataResponse.getRecordGroups();
        List<Iterator<Record>> iterators = new ArrayList<>();
        for (RecordGroup recordGroup : recordGroups) {
            iterators.add(this.analyticsDataService.readRecords(analyticsDataResponse.getRecordStoreName(), recordGroup));
        }
        return iterators;
    }

    private List<SearchResultEntry> getRecordSearchEntries(int tenantId,
                                                           CategorySearchResultEntry facetValue,
                                                           AggregateRequest aggregateRequest)
            throws AnalyticsIndexException {
        AnalyticsDrillDownRequest analyticsDrillDownRequest = new AnalyticsDrillDownRequest();
        analyticsDrillDownRequest.setTableName(aggregateRequest.getTableName());
        analyticsDrillDownRequest.setQuery(aggregateRequest.getQuery());
        analyticsDrillDownRequest.setRecordStartIndex(0);
        analyticsDrillDownRequest.setRecordCount(Integer.MAX_VALUE);
        Map<String, List<String>> groupByCategory = new HashMap<>();
        List<String> groupByValue = new ArrayList<>();
        groupByValue.add(facetValue.getCategoryValue());
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
     * This is executed to start indexing operations in the current node.
     */
    public static class IndexingScheduleMessage implements Callable<String>, Serializable {
        
        private static final long serialVersionUID = 7912933193977147465L;
        
        private Integer[] shardInfo;
        
        public IndexingScheduleMessage(Integer[] shardInfo) {
            this.shardInfo = shardInfo;
        }

        @Override
        public String call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                adsImpl.getIndexer().scheduleWorkers(this.shardInfo);
            }
            return "OK";
        }
    }
    
    /**
     * This class represents a index delete entry.
     */
    public static class DeleteIndexEntry implements Serializable {
        
        private static final long serialVersionUID = -3118546252869493269L;

        private int tenantId;
        
        private String tableName;
        
        private String id;
        
        public DeleteIndexEntry() { }
        
        public DeleteIndexEntry(int tenantId, String tableName, String id) {
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
     * This class represents an indexing operation for a record.
     */
    public static class IndexOperation implements Serializable {
        
        private static final long serialVersionUID = -5071679492708482851L;
        
        private Record record;
        
        private boolean delete;
        
        public IndexOperation() { }
        
        public IndexOperation(Record record, boolean delete) {
            this.record = record;
            this.delete = delete;
        }
        
        public Record getRecord() {
            return record;
        }
        
        public boolean isDelete() {
            return delete;
        }
        
    }
    
    /**
     * This represents an indexing worker, who does index operations in the background.
     */
    private class IndexWorker implements Runnable {

        private static final int INDEX_WORKER_SLEEP_TIME = 1500;

        private int shardIdFrom;
        
        private int shardRange;
        
        private boolean stop;
        
        public IndexWorker(int shardIdFrom, int shardRange) {
            this.shardIdFrom = shardIdFrom;
            this.shardRange = shardRange;
        }
        
        public int getShardIdFrom() {
            return shardIdFrom;
        }
        
        public int getShardRange() {
            return shardRange;
        }
        
        public void stop() {
            this.stop = true;
        }
        
        @Override
        public void run() {
            while (!this.stop) {
                try {
                    processIndexOperations(this.getShardIdFrom(), this.getShardRange());
                } catch (Throwable e) {
                    e.printStackTrace();
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
}
