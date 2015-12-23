/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import com.leansoft.bigqueue.BigQueueImpl;
import com.leansoft.bigqueue.IBigQueue;

/**
 * Manages local indexing data.
 */
public class LocalIndexDataStore {

    private Log log = LogFactory.getLog(LocalIndexDataStore.class);
    
    private AnalyticsDataIndexer indexer;
    
    private Map<Integer, LocalIndexDataQueue> indexDataQueues;
    
    public LocalIndexDataStore(AnalyticsDataIndexer indexer) throws AnalyticsException {
        this.indexer = indexer;
        this.indexDataQueues = new HashMap<Integer, LocalIndexDataQueue>();
        this.refreshLocalIndexShards();
    }
    
    public void refreshLocalIndexShards() throws AnalyticsException {
        this.closeQueues();
        for (int shardIndex : this.indexer.getLocalShards()) {
            this.indexDataQueues.put(shardIndex, new LocalIndexDataQueue(shardIndex));
        }
    }
    
    public void put(List<Record> records) throws AnalyticsException {
        Map<Integer, List<Record>> recordsMap = this.indexer.extractShardedRecords(records);
        LocalIndexDataQueue dataList;
        for (Map.Entry<Integer, List<Record>> entry : recordsMap.entrySet()) {
            dataList = this.indexDataQueues.get(entry.getKey());
            if (dataList == null) {
                continue;
            }
            dataList.enqueue(new IndexOperation(false).setRecords(entry.getValue()));
        }
    }
    
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        Map<Integer, List<String>> recordsMap = this.indexer.extractShardedIds(ids);
        LocalIndexDataQueue dataList;
        for (Map.Entry<Integer, List<String>> entry : recordsMap.entrySet()) {
            dataList = this.indexDataQueues.get(entry.getKey());
            if (dataList == null) {
                continue;
            }
            dataList.enqueue(new IndexOperation(true).setIds(entry.getValue()).
                    setDeleteTenantId(tenantId).setDeleteTableName(tableName));
        }
    }
    
    private void closeQueues() {
        for (LocalIndexDataQueue queue : this.indexDataQueues.values()) {
            try {
                queue.close();
            } catch (IOException e) {
                log.warn("Error in closing queue: " + e.getMessage(), e);
            }
        }
    }
    
    public void close() {
        this.closeQueues();
    }
    
    public LocalIndexDataQueue getIndexDataQueue(int shardIndex) {
        return this.indexDataQueues.get(shardIndex);
    }
    
    public static class IndexOperation implements Serializable {
        
        private static final long serialVersionUID = 7764589621281488353L;

        private boolean delete;
        
        private List<String> ids;
        
        private int deleteTenantId;
        
        private String deleteTableName;
        
        private List<Record> records;
        
        public IndexOperation() { }
        
        public IndexOperation(boolean delete) {
            this.delete = delete;
        }

        public List<String> getIds() {
            return ids;
        }

        public IndexOperation setIds(List<String> ids) {
            this.ids = ids;
            return this;
        }

        public List<Record> getRecords() {
            return records;
        }

        public IndexOperation setRecords(List<Record> records) {
            this.records = records;
            return this;
        }
        
        public boolean isDelete() {
            return delete;
        }

        public int getDeleteTenantId() {
            return deleteTenantId;
        }

        public IndexOperation setDeleteTenantId(int deleteTenantId) {
            this.deleteTenantId = deleteTenantId;
            return this;
        }

        public String getDeleteTableName() {
            return deleteTableName;
        }

        public IndexOperation setDeleteTableName(String deleteTableName) {
            this.deleteTableName = deleteTableName;
            return this;
        }
        
        public byte[] getBytes() {
            return GenericUtils.serializeObject(this);
        }
        
        public static IndexOperation fromBytes(byte[] data) {
            return (IndexOperation) GenericUtils.deserializeObject(data);
        }
        
    }
    
    public static class LocalIndexDataQueue {
        
        private static final int QUEUE_CLEANUP_THRESHOLD = 1000;
        
        private IBigQueue queue;
        
        private int removeCount = 0;
        
        public LocalIndexDataQueue(int shardIndex) throws AnalyticsException {
            this.queue = this.createQueue(shardIndex);
        }
        
        private IBigQueue createQueue(int shardIndex) throws AnalyticsException {
            String path = Constants.DEFAULT_LOCAL_INDEX_STAGING_LOCATION;
            path = GenericUtils.resolveLocation(path);
            try {
                return new BigQueueImpl(path, String.valueOf(shardIndex));
            } catch (IOException e) {
                throw new AnalyticsException("Error in creating queue: " + e.getMessage(), e);
            }
        }
        
        public void enqueue(IndexOperation indexOp) throws AnalyticsException {
            try {
                this.queue.enqueue(indexOp.getBytes());
            } catch (IOException e) {
                throw new AnalyticsException("Error in index data enqueue: " + e.getMessage(), e);
            }
        }
        
        public IndexOperation dequeue() throws AnalyticsException {
            try {
                IndexOperation indexOp = IndexOperation.fromBytes(this.queue.dequeue());
                this.removeCount++;
                if (this.removeCount > QUEUE_CLEANUP_THRESHOLD) {
                    this.queue.gc();
                    this.removeCount = 0;
                }
                return indexOp;
            } catch (IOException e) {
                throw new AnalyticsException("Error in index data dequeue: " + e.getMessage(), e);
            }
        }
        
        public boolean isEmpty() {
            return this.queue.isEmpty();
        }
        
        public long size() {
            return this.queue.size();
        }
        
        public void close() throws IOException {
            this.queue.close();
        }
        
    }
    
}
