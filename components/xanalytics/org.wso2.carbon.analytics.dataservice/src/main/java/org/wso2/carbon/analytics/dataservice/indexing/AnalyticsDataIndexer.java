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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.wso2.carbon.analytics.dataservice.AnalyticsDirectory;
import org.wso2.carbon.analytics.dataservice.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.AnalyticsQueryParser;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;

/**
 * This class represents the indexing functionality.
 */
public class AnalyticsDataIndexer {
    
    private static final String LOCAL_NODE_ID = "LOCAL";

    private static final int LOCAL_SHARD_COUNT = 10;

    private static final Log log = LogFactory.getLog(AnalyticsDataIndexer.class);
        
    private static final String INDEX_DATA_FS_BASE_PATH = "/_data/index/";

    public static final String INDEX_ID_INTERNAL_FIELD = "_id";

    public static final String INDEX_INTERNAL_TIMESTAMP_FIELD = "_timestamp";
    
    private static final String NULL_INDEX_VALUE = "";

    private AnalyticsIndexDefinitionRepository repository;
    
    private Map<String, Map<String, IndexType>> indexDefs = new HashMap<String, Map<String, IndexType>>();
    
    private Map<String, Directory> indexDirs = new HashMap<String, Directory>();
    
    private Map<String, Integer> indexShardPositions = new HashMap<String, Integer>();
    
    private Map<String, List<String>> localIndexShardIdsMap = new HashMap<String, List<String>>();
    
    private Analyzer DEFAULT_ANALYZER = new StandardAnalyzer();
    
    private FileSystem fileSystem;
        
    public AnalyticsDataIndexer(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.repository = new AnalyticsIndexDefinitionRepository(this.getFileSystem());
    }
    
    public FileSystem getFileSystem() {
        return fileSystem;
    }
    
    public AnalyticsIndexDefinitionRepository getRepository() {
        return repository;
    }
    
    private int getNextIndexShardPosition(int tenantId, String tableName) {
        String id = this.generateGlobalTableId(tenantId, tableName);
        Integer pos = this.indexShardPositions.get(id);
        /* no need to synchronize here, it's just creating an integer */
        if (pos == null) {
            pos = 0;
        }
        pos++;
        this.indexShardPositions.put(id, pos);
        return pos;
    }
    
    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }
    
    private String getLocalUniqueId() {
        String id = null;
        Enumeration<NetworkInterface> interfaces;
        NetworkInterface nif;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                nif = interfaces.nextElement();
                if (!nif.isLoopback()) {
                    byte[] addr = nif.getHardwareAddress();
                    if (addr != null) {
                        id = this.byteArrayToHexString(addr);
                    }
                    /* only try first valid one, if we can't get it from here, 
                     * we wont be able to get it */
                    break;
                }
            }
        } catch (SocketException ignore) {
            /* ignore */
        }
        if (id == null) {
            log.warn("CANNOT LOOK UP UNIQUE LOCAL ID USING A VALID NETWORK INTERFACE HARDWARE ADDRESS, "
                    + "REVERTING TO LOCAL SINGLE NODE MODE, THIS WILL NOT WORK PROPERLY IN A CLUSTER, "
                    + "AND MAY CAUSE INDEX CORRUPTION");
            /* a last effort to get a unique number, Java system properties should also take in account of the 
             * server's port offset */
            id = LOCAL_NODE_ID + ":" + (System.getenv().hashCode() + System.getProperties().hashCode());
        }
        return id;
    }
    
    private List<String> getLocalCandidateIndexShardIds(int tenantId, String tableName) {
        String globalTableId = this.generateGlobalTableId(tenantId, tableName);
        List<String> localIndexShardIds = this.localIndexShardIdsMap.get(globalTableId);
        if (localIndexShardIds == null) {
            String localPrefix = this.getLocalUniqueId();
            localIndexShardIds = new ArrayList<String>();
            for (int i = 0; i < LOCAL_SHARD_COUNT; i++) {
                localIndexShardIds.add(localPrefix + "_" + i);
            }
            this.localIndexShardIdsMap.put(globalTableId, localIndexShardIds);
        }
        return localIndexShardIds;
    }
    
    private String getNextLocalCandidateIndexShardId(int tenantId, String tableName) {
        int pos = this.getNextIndexShardPosition(tenantId, tableName);
        /* retrieve all the shards I own for this tenant / table, and select one */
        List<String> shardIds = this.getLocalCandidateIndexShardIds(tenantId, tableName);
        return shardIds.get(pos % shardIds.size());
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
        /* needs to improve this to do this efficiently */
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
    
    private List<SearchResultEntry> search(int tenantId, String tableName, String language, String query, 
            int start, int count, String shardId) throws AnalyticsIndexException {
        List<SearchResultEntry> result = new ArrayList<SearchResultEntry>();
        String shardedTableId = this.generateShardedTableId(tenantId, tableName, shardId);
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(this.lookupIndexDir(shardedTableId));
            IndexSearcher searcher = new IndexSearcher(reader);
            Map<String, IndexType> indices = this.lookupIndices(tenantId, tableName);
            Query indexQuery = new AnalyticsQueryParser(DEFAULT_ANALYZER, indices).parse(query);
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
    
    private Map<String, List<Record>> generateRecordBatches(List<Record> records) throws AnalyticsException {
        Map<String, List<Record>> recordBatches = new HashMap<String, List<Record>>();
        List<Record> recordBatch;
        String identity;
        for (Record record : records) {
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
        Map<String, IndexType> indices;
        Map<String, List<Record>> batches = this.generateRecordBatches(records);
        Record firstRecord;
        for (List<Record> recordBatch : batches.values()) {
            firstRecord = recordBatch.get(0);
            indices = this.lookupIndices(firstRecord.getTenantId(), firstRecord.getTableName());
            if (indices.size() > 0) {
                this.addToIndex(recordBatch, indices);
            }
        }
    }
    
    /**
     * Updates the given records in the index.
     * @param records The records to be indexed
     * @throws AnalyticsException
     */
    public void update(List<Record> records) throws AnalyticsException {
        Map<String, IndexType> indices;
        Map<String, List<Record>> batches = this.generateRecordBatches(records);
        Record firstRecord;
        for (List<Record> recordBatch : batches.values()) {
            firstRecord = recordBatch.get(0);
            indices = this.lookupIndices(firstRecord.getTenantId(), firstRecord.getTableName());
            if (indices.size() > 0) {
                this.updateIndex(recordBatch, indices);
            }
        }
    }
    
    /**
    * Deletes the given records in the index.
    * @param tenantId The tenant id
    * @param tableName The table name
    * @param The ids of the records to be deleted
    * @throws AnalyticsException
    */
   public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
       List<String> shardIds = this.lookupGloballyExistingShardIds(tenantId, tableName);
       for (String shardId : shardIds) {
           this.delete(tenantId, tableName, ids, shardId);
       }
    }
    
    private void delete(int tenantId, String tableName, List<String> ids, String shardId) throws AnalyticsException {
        if (this.lookupIndices(tenantId, tableName).size() == 0) {
            return;
        }
        String tableId = this.generateShardedTableId(tenantId, tableName, shardId);
        IndexWriter indexWriter = this.createIndexWriter(tableId);
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
    
    /**
     * Deletes the records in the index with the given time range.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @param timeFrom The from time of records, inclusive
     * @param timeTo The to time of records, non-inclusive
     * @throws AnalyticsException
     */
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        List<String> shardIds = this.lookupGloballyExistingShardIds(tenantId, tableName);
        for (String shardId : shardIds) {
            this.delete(tenantId, tableName, timeFrom, timeTo, shardId);
        }
    }
    
    private void delete(int tenantId, String tableName, long timeFrom, long timeTo, String shardId) throws AnalyticsException {
        Map<String, IndexType> indices = this.lookupIndices(tenantId, tableName);
        if (indices.size() == 0) {
            return;
        }
        String tableId = this.generateShardedTableId(tenantId, tableName, shardId);
        IndexWriter indexWriter = this.createIndexWriter(tableId);        
        try {
            Query query = new AnalyticsQueryParser(DEFAULT_ANALYZER, indices).parse(
                    INDEX_INTERNAL_TIMESTAMP_FIELD + ":[" + timeFrom + " TO " + timeTo + "}");
            indexWriter.deleteDocuments(query);
            indexWriter.commit();
        } catch (Exception e) {
            throw new AnalyticsException("Error in deleting indices: " + e.getMessage(), e);
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                log.error("Error closing index writer: " + e.getMessage(), e);
            }
        }
    }
    
    private void addToIndex(List<Record> recordBatch, Map<String, IndexType> columns) throws AnalyticsIndexException {
        Record firstRecord = recordBatch.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        String shardId = this.getNextLocalCandidateIndexShardId(tenantId, tableName);
        String shardedTableId = this.generateShardedTableId(tenantId, tableName, shardId);
        IndexWriter indexWriter = this.createIndexWriter(shardedTableId);
        try {
            for (Record record : recordBatch) {
                indexWriter.addDocument(this.generateIndexDoc(record, columns).getFields());
            }
            indexWriter.commit();
        } catch (IOException e) {
            throw new AnalyticsIndexException("Error in updating index: " + e.getMessage(), e);
        } finally {
            try {
                indexWriter.close();
                //System.out.println("CLOSE INDEX WRITER: " + shardedTableId);
            } catch (IOException e) {
                log.error("Error closing index writer: " + e.getMessage(), e);
            }
        }
    }
    
    private void updateIndex(List<Record> recordBatch, Map<String, IndexType> columns) throws AnalyticsIndexException {
        Record firstRecord = recordBatch.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        List<String> shardIds = this.lookupGloballyExistingShardIds(tenantId, tableName);
        for (String shardId : shardIds) {
            this.updateIndex(tenantId, tableName, recordBatch, columns, shardId);
        }
    }
    
    private void updateIndex(int tenantId, String tableName, List<Record> recordBatch, Map<String, IndexType> columns, String shardId) throws AnalyticsIndexException {
        String shardedTableId = this.generateShardedTableId(tenantId, tableName, shardId);
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
            return new AnalyticsDirectory(this.getFileSystem(), new AnalyticsDirectory.InMemoryLockFactory(path), path);
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
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_4_10_2, this.DEFAULT_ANALYZER);
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
        List<String> tableIds = this.getLocalCandidateIndexShardIds(tenantId, tableName);
        this.closeAndRemoveIndexDirs(new HashSet<String>(tableIds));
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
    
    public void close() throws AnalyticsIndexException {
        this.indexDefs.clear();
        this.closeAndRemoveIndexDirs(new HashSet<String>(this.indexDirs.keySet()));
    }
    
}
