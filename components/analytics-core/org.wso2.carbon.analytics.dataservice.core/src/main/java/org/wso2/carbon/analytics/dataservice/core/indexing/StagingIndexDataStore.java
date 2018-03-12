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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * Manages the staging indexing data.
 */
public class StagingIndexDataStore {

    private Log log = LogFactory.getLog(StagingIndexDataStore.class);

    private AnalyticsDataIndexer indexer;
    private LocalShardAllocationConfig localShardAllocationConfig;

    public StagingIndexDataStore(AnalyticsDataIndexer indexer) {
        this.indexer = indexer;
    }

    public StagingIndexDataStore(AnalyticsDataIndexer indexer, LocalShardAllocationConfig config) {
        this.indexer = indexer;
        this.localShardAllocationConfig = config;
    }

    public void initStagingTables(String nodeId) throws AnalyticsException {
        AnalyticsRecordStore rs = this.indexer.getAnalyticsRecordStore();
        int shardCount = this.indexer.getShardCount();
        for (int i = 0; i < shardCount; i++) {
            String tableName = this.generateTableName(nodeId, i);
            if (tableName != null) {
                rs.createTable(Constants.META_INFO_TENANT_ID, tableName);
            }
        }
    }

    public void put(String nodeId, List<Record> records) throws AnalyticsException {
        Map<Integer, List<Record>> shardedRecords = this.indexer.extractShardedRecords(records);
        for (Map.Entry<Integer, List<Record>> entry : shardedRecords.entrySet()) {
            Collection<List<Record>> recordBatches = GenericUtils.generateRecordBatches(entry.getValue());
            for (List<Record> recordBatch : recordBatches) {
                int tenantId = recordBatch.get(0).getTenantId();
                String tableName = recordBatch.get(0).getTableName();
                List<String> ids = new ArrayList<>();
                for (Record record : recordBatch) {
                    ids.add(record.getId());
                }
                StagingIndexDataEntry indexEntry = new StagingIndexDataEntry(tenantId, tableName, ids);
                this.addEntryToShard(nodeId, entry.getKey(), indexEntry);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Staging index data put: " + records.size());
        }
    }

    public void put(int shardIndex, int replica, List<Record> records) throws AnalyticsException {
        Collection<List<Record>> recordBatches = GenericUtils.generateRecordBatches(records);
        for (List<Record> recordBatch : recordBatches) {
            int tenantId = recordBatch.get(0).getTenantId();
            String tableName = recordBatch.get(0).getTableName();
            List<String> ids = new ArrayList<>();
            for (Record record : recordBatch) {
                ids.add(record.getId());
            }
            StagingIndexDataEntry indexEntry = new StagingIndexDataEntry(tenantId, tableName, ids);
            this.addEntryToShard(shardIndex, replica, indexEntry);
        }
    }

    public void delete(String nodeId, int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        Map<Integer, List<String>> shardedIds = this.indexer.extractShardedIds(ids);
        for (Map.Entry<Integer, List<String>> entry : shardedIds.entrySet()) {
            StagingIndexDataEntry indexEntry = new StagingIndexDataEntry(tenantId, tableName, ids);
            this.addEntryToShard(nodeId, entry.getKey(), indexEntry);
        }
        if (log.isDebugEnabled()) {
            log.debug("Staging index data delete: " + ids.size());
        }
    }

    private void addEntryToShard(String nodeId, int shardIndex, StagingIndexDataEntry entry) throws AnalyticsException {
        String tableName = this.generateTableName(nodeId, shardIndex);
        int tenantId = Constants.META_INFO_TENANT_ID;
        Map<String, Object> values = new HashMap<>(1);
        values.put(Constants.INDEX_STAGING_DATA_COLUMN, entry);
        AnalyticsRecordStore rs = this.indexer.getAnalyticsRecordStore();
        Record record = new Record(GenericUtils.generateRecordID(), tenantId, tableName, values);
        entry.setRecordId(record.getId());
        try {
            rs.put(Arrays.asList(record));
        } catch (AnalyticsTableNotAvailableException e) {
            rs.createTable(tenantId, tableName);
            rs.put(Arrays.asList(record));
        }
    }

    private void addEntryToShard(int shardIndex, int replica, StagingIndexDataEntry entry) throws AnalyticsException {
        String tableName = this.generateTableName(shardIndex, replica);
        int tenantId = Constants.META_INFO_TENANT_ID;
        Map<String, Object> values = new HashMap<>(1);
        values.put(Constants.INDEX_STAGING_DATA_COLUMN, entry);
        AnalyticsRecordStore rs = this.indexer.getAnalyticsRecordStore();
        Record record = new Record(GenericUtils.generateRecordID(), tenantId, tableName, values);
        entry.setRecordId(record.getId());
        try {
            rs.put(Arrays.asList(record));
        } catch (AnalyticsTableNotAvailableException e) {
            rs.createTable(tenantId, tableName);
            rs.put(Arrays.asList(record));
        }
    }

    private String generateTableNameForOlderStagingTable(String nodeId, int shardIndex) {
        return Constants.INDEX_STAGING_DATA_TABLE + shardIndex + "_" + nodeId;
    }

    private String generateTableName(String nodeId, int shardIndex) {
        int shardReplica = localShardAllocationConfig.getShardReplica(shardIndex);
        if (shardReplica != 0) {
            return Constants.INDEX_STAGING_DATA_TABLE + shardIndex + "_" + shardReplica;
        }
        return null;
    }

    private String generateTableName(int shardIndex, int replica) {
        return Constants.INDEX_STAGING_DATA_TABLE + shardIndex + "_" + replica;
    }

    public List<StagingIndexDataEntry> loadEntries(String nodeId, int shardIndex) throws AnalyticsException {
        return getStagingIndexDataEntries(this.generateTableName(nodeId, shardIndex));
    }

    public List<StagingIndexDataEntry> loadEntriesInOldStagingTables(String nodeId, int shardIndex) throws
            AnalyticsException {
        return getStagingIndexDataEntries(this.generateTableNameForOlderStagingTable(nodeId, shardIndex));
    }

    private List<StagingIndexDataEntry> getStagingIndexDataEntries(String stagingTable)
            throws AnalyticsException {
        if (stagingTable == null) {
            return new ArrayList<>(0);
        }
        AnalyticsRecordStore rs = this.indexer.getAnalyticsRecordStore();
        try {
            List<Record> records = GenericUtils.listRecords(rs, rs.get(Constants.META_INFO_TENANT_ID,
                    stagingTable, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0,
                    Constants.RECORDS_BATCH_SIZE));
            List<StagingIndexDataEntry> result = new ArrayList<>(records.size());
            for (Record record : records) {
                result.add((StagingIndexDataEntry) record.getValue(Constants.INDEX_STAGING_DATA_COLUMN));
            }
            return result;
        } catch (AnalyticsTableNotAvailableException e) {
            return new ArrayList<>(0);
        }
    }

    public void removeEntries(String nodeId, int shardIndex, List<String> ids) throws AnalyticsException {
        removeStagingEntries(this.generateTableName(nodeId, shardIndex), ids);
    }

    public void removeEntriesFromOldStagingTables(String nodeId, int shardIndex, List<String> ids) throws
            AnalyticsException {
        removeStagingEntries(this.generateTableNameForOlderStagingTable(nodeId, shardIndex), ids);
    }

    public void deleteStagingEntryLocation(String nodeId, int shardIndex) throws AnalyticsException {
        AnalyticsRecordStore rs = this.indexer.getAnalyticsRecordStore();
        rs.deleteTable(Constants.META_INFO_TENANT_ID, generateTableNameForOlderStagingTable(nodeId, shardIndex));
    }

    private void removeStagingEntries(String stagingTable, List<String> ids) throws AnalyticsException {
        AnalyticsRecordStore rs = this.indexer.getAnalyticsRecordStore();
        rs.delete(Constants.META_INFO_TENANT_ID, stagingTable, ids);
    }

    public static class StagingIndexDataEntry implements Serializable {

        private static final long serialVersionUID = 2811642328079107132L;

        private int tenantId;

        private String tableName;

        private List<String> ids;

        private String recordId;

        public StagingIndexDataEntry() {
        }

        public StagingIndexDataEntry(int tenantId, String tableName, List<String> ids) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.ids = ids;
        }

        public int getTenantId() {
            return tenantId;
        }

        public void setTenantId(int tenantId) {
            this.tenantId = tenantId;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public List<String> getIds() {
            return ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }

        public String getRecordId() {
            return recordId;
        }

        public void setRecordId(String recordId) {
            this.recordId = recordId;
        }

    }

}
