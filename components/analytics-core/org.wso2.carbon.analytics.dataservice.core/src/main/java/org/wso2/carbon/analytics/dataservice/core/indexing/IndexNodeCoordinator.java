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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsInterruptException;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.core.clustering.GroupEventListener;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsIndexedTableStore.IndexedTableId;
import org.wso2.carbon.analytics.dataservice.core.indexing.LocalShardAllocationConfig.ShardStatus;
import org.wso2.carbon.analytics.dataservice.core.indexing.StagingIndexDataStore.StagingIndexDataEntry;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Analytics index operations node coordinator.
 */
public class IndexNodeCoordinator implements GroupEventListener {

    private static final int FAIL_INDEX_OPERATION_REFRESH_THRESHOLD = 100;

    private static final String GSA_LOCK = "__GLOBAL_SHARD_ALLOCATION_LOCK__";

    private static Log log = LogFactory.getLog(IndexNodeCoordinator.class);

    private AnalyticsDataIndexer indexer;

    private GlobalShardAllocationConfig globalShardAllocationConfig;

    private LocalShardAllocationConfig localShardAllocationConfig;

    private String myNodeId;

    private GlobalShardMemberMapping shardMemberMap;

    private Set<String> suppressWarnMessagesInactiveMembers = new HashSet<>();

    private StagingIndexDataStore stagingIndexDataStore;

    private ExecutorService stagingWorkerExecutor;
    private ExecutorService oldStagingWorkerExecutor;

    private List<StagingDataIndexWorker> stagingIndexWorkers;
    private List<OldStagingDataIndexWorker> oldStagingDataIndexWorkers;

    private int failedIndexOperationCount;

    private RemoteMemberIndexCommunicator remoteCommunicator;

    private boolean indexingNode = false;

    /* this executor is specifically used, rather than a single thread executor, so there won't be a thread always live, mostly unused */
    private ExecutorService localShardProcessExecutor = new ThreadPoolExecutor(0, 1,
            0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),new ThreadFactoryBuilder().
            setNameFormat("Thread pool- component - IndexNodeCoordinator.localShardProcessExecutor").build());

    public IndexNodeCoordinator(AnalyticsDataIndexer indexer) throws AnalyticsException {
        this.indexer = indexer;
        this.localShardAllocationConfig = new LocalShardAllocationConfig();
        // this.globalShardAllocationConfig = new GlobalShardAllocationConfig(this.indexer.getAnalyticsRecordStore());
        this.globalShardAllocationConfig = new GlobalShardAllocationConfig();

        this.shardMemberMap = new GlobalShardMemberMapping(this.indexer.getShardCount(),
                                                           this.globalShardAllocationConfig);
        this.stagingIndexDataStore = new StagingIndexDataStore(this.indexer, this.localShardAllocationConfig);
        this.remoteCommunicator = new RemoteMemberIndexCommunicator(indexer.getAnalyticsIndexerInfo()
                .getIndexCommunicatorBufferSize(), this.stagingIndexDataStore);
        this.indexingNode = checkIfIndexingNode();
    }

    public static boolean checkIfIndexingNode() {
        String indexDisableProp = System.getProperty(Constants.DISABLE_INDEXING_ENV_PROP);
        return !(indexDisableProp != null && Boolean.parseBoolean(indexDisableProp));
    }

    private boolean isClusteringEnabled() {
        return AnalyticsServiceHolder.getAnalyticsClusterManager().isClusteringEnabled();
    }

    private void initClustering() throws AnalyticsException {
        if (this.isClusteringEnabled()) {
            AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
            acm.joinGroup(Constants.ANALYTICS_INDEXING_GROUP, this);
        }
    }

    private List<Integer> calculateGlobalLocalShardDiff() throws AnalyticsException {
        List<Integer> result = new ArrayList<>();
        int shardCount = this.indexer.getShardCount();
        for (int i = 0; i < shardCount; i++) {
            if (this.globalShardAllocationConfig.getNodeIdsForShard(i).contains(this.myNodeId) &&
                this.localShardAllocationConfig.getShardStatus(i) == null) {
                result.add(i);
            }
        }
        return result;
    }

    private void removeLocalIndexData(int shardIndex) {
        String directory = Constants.DEFAULT_INDEX_STORE_LOCATION + Constants.INDEX_STORE_DIR_PREFIX + shardIndex;
        try {
            FileUtils.deleteDirectory(new File(GenericUtils.resolveLocation(directory)));
        } catch (Exception e) {
            log.warn("Unable to delete index data directory '" + directory + "': " + e.getMessage(), e);
        }
    }

    private void handleInitAndRestoreShards() throws AnalyticsException {
        ShardStatus status;
        int replicaIndex;
        for (int shardIndex : this.localShardAllocationConfig.getShardIndices()) {
            status = this.localShardAllocationConfig.getShardStatus(shardIndex);
            replicaIndex = this.localShardAllocationConfig.getShardReplica(shardIndex);
                        if (status.equals(ShardStatus.NORMAL)) {
                                this.globalShardAllocationConfig.addNodeIdForShard(shardIndex, replicaIndex, this.myNodeId);
                            } else if (status.equals(ShardStatus.RESTORE)) {
                                this.globalShardAllocationConfig.addNodeIdForShard(shardIndex, replicaIndex, this.myNodeId);
                                this.localShardAllocationConfig.setShardStatus(shardIndex, replicaIndex, ShardStatus.NORMAL);
            } else if (status.equals(ShardStatus.INIT)) {
                            this.globalShardAllocationConfig.addNodeIdForShard(shardIndex, replicaIndex, this.myNodeId);
                        }
        }
    }

    private void syncGlobalWithLocal() throws AnalyticsException {
        this.handleInitAndRestoreShards();
        List<Integer> globalShardDiff = this.calculateGlobalLocalShardDiff();
        for (int shardIndex : globalShardDiff) {
            this.globalShardAllocationConfig.removeNodeIdFromShard(shardIndex, this.myNodeId);
        }
    }

    private void syncLocalWithGlobal() throws AnalyticsException {
        /* update the local shard allocation config with the details from the 
         * global config, and considering the INIT entries of the local config, 
         * where other types will be marked as NORMAL after this */
        List<Integer> initShards = new ArrayList<>();
        for (int shardIndex : this.localShardAllocationConfig.getShardIndices()) {
            if (ShardStatus.INIT.equals(this.localShardAllocationConfig.getShardStatus(shardIndex))) {
                initShards.add(shardIndex);
            }
        }
        for (int shardIndex : this.localShardAllocationConfig.getShardIndices()) {
            this.localShardAllocationConfig.removeShardIndex(shardIndex);
        }
        int shardCount = this.indexer.getShardCount();
        for (int i = 0; i < shardCount; i++) {
            if (this.globalShardAllocationConfig.getNodeIdsForShard(i).contains(this.myNodeId)) {
                int replica = globalShardAllocationConfig.getShardReplica(i, this.myNodeId);
                this.localShardAllocationConfig.setShardStatus(i, replica, ShardStatus.NORMAL);
            }
        }
        for (int shardIndex : initShards) {
            int replica = globalShardAllocationConfig.getShardReplica(shardIndex, this.myNodeId);
            this.localShardAllocationConfig.setShardStatus(shardIndex, replica, ShardStatus.INIT);
        }
        this.localShardAllocationConfig.save();
    }

    private void initShardAllocation() throws AnalyticsException {
        Lock globalAllocationLock = null;
        try {
            boolean initialAllocation = false;
            if (!this.localShardAllocationConfig.isInit()) {
                if (this.isClusteringEnabled()) {
                    globalAllocationLock = AnalyticsServiceHolder.getHazelcastInstance().getLock(GSA_LOCK);
                    globalAllocationLock.lock();
                }
                initialAllocation = true;
            }
            this.syncGlobalWithLocal();
            this.allocateLocalShardsFromGlobal(initialAllocation);
            this.syncLocalWithGlobal();
        } finally {
            if (globalAllocationLock != null) {
                globalAllocationLock.unlock();
            }
        }
    }

    private boolean currentNodeAllocatedShardsGlobally() throws AnalyticsException {
        return !this.extractExistingLocalShardsFromGlobal().isEmpty();
    }

    private void removeMyNodeFromIndexingConfigurations() throws AnalyticsException {
        Set<Integer> shards = this.extractExistingLocalShardsFromGlobal();
        for (int shardIndex : shards) {
            this.globalShardAllocationConfig.removeNodeIdFromShard(shardIndex, this.myNodeId);
        }
        this.syncLocalWithGlobal();
    }
    
    public void init() throws AnalyticsException {
        this.indexingNode = checkIfIndexingNode();
        this.populateMyNodeId();
        boolean indexingNodeDisabling = !this.indexingNode && this.currentNodeAllocatedShardsGlobally();
        if (indexingNodeDisabling && AnalyticsDataServiceUtils.isCarbonServer()) {
            this.removeMyNodeFromIndexingConfigurations();
        }
        this.initClustering();
        if (this.indexingNode) {
            this.initShardAllocation();
            this.stagingIndexDataStore.initStagingTables(this.myNodeId);
        }
        if (this.isClusteringEnabled()) {
            AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
            if (indexingNodeDisabling) {
                acm.executeAll(Constants.ANALYTICS_INDEXING_GROUP, new RefreshIndexShardAllocationCall());
            }
            acm.executeAll(Constants.ANALYTICS_INDEXING_GROUP, new IndexRefreshShardInfoCall());
        } else {
            this.refreshIndexShardInfo();
            this.processOldStagingData();
        }
        if (this.indexingNode) {
            this.processLocalShards();
        }
    }

    public void refreshIndexShardAllocation() throws AnalyticsException {
        if (!this.indexingNode) {
            return;
        }
        Lock globalAllocationLock = AnalyticsServiceHolder.getHazelcastInstance().getLock(GSA_LOCK);
        try {
            globalAllocationLock.lock();
            this.allocateLocalShardsFromGlobal(false);
        } finally {
            globalAllocationLock.unlock();
        }
        this.processLocalShards();
    }

    public GlobalShardMemberMapping getShardMemberMap() {
        return shardMemberMap;
    }

    private void processLocalShards() throws AnalyticsException {
        this.localShardProcessExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Integer> initShards = new ArrayList<>();
                for (int shardIndex : localShardAllocationConfig.getShardIndices()) {
                    switch (localShardAllocationConfig.getShardStatus(shardIndex)) {
                        case INIT:
                            initShards.add(shardIndex);
                            break;
                        case NORMAL:
                            break;
                        case RESTORE:
                            break;
                        default:
                            break;
                    }
                }
                if (!initShards.isEmpty()) {
                    log.info("Initializing indexing shards: " + initShards);
                    /* first remove all existing local index data in init shards */
                    for (int shardIndex : initShards) {
                        removeLocalIndexData(shardIndex);
                    }
                    try {
                        processLocalInitShards(initShards);
                        for (int shardIndex : initShards) {
                            int replica = globalShardAllocationConfig.getShardReplica(shardIndex,
                                    IndexNodeCoordinator.this.myNodeId);
                            localShardAllocationConfig.setShardStatus(shardIndex, replica, ShardStatus.NORMAL);
                        }
                        localShardAllocationConfig.save();
                    } catch (AnalyticsException e) {
                        log.error("Error in processing local init shards: " + e.getMessage(), e);
                    }
                }
            }
        });
    }

    private Object[] convertToObjectShardArray(List<Integer> initShards) {
        Object[] result = new Object[this.indexer.getShardCount()];
        for (int index : initShards) {
            result[index] = new Object();
        }
        return result;
    }

    private void processLocalInitShards(List<Integer> initShards) throws AnalyticsException {
        if (log.isDebugEnabled()) {
            log.debug("Starting processing local init shards: " + initShards);
        }
        AnalyticsIndexedTableStore store = this.indexer.getAnalyticsIndexerInfo().getIndexedTableStore();
        Object[] initShardObjs = this.convertToObjectShardArray(initShards);
        for (IndexedTableId tableId : store.getAllIndexedTables()) {
            this.readAndIndexTable(tableId, initShardObjs);
        }
        if (log.isDebugEnabled()) {
            log.debug("Finished processing local init shards: " + initShards);
        }
    }

    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        Map<Integer, List<String>> shardedIds = this.indexer.extractShardedIds(ids);
        List<String> localIds = new ArrayList<>();
        Map<String, List<String>> remoteIdsMap = new HashMap<>();
        for (Map.Entry<Integer, List<String>> entry : shardedIds.entrySet()) {
            Set<String> nodeIds = this.shardMemberMap.getNodeIdsForShard(entry.getKey());
            for (String nodeId : nodeIds) {
                if (this.myNodeId.equals(nodeId) && this.indexingNode ) {
                    localIds.addAll(entry.getValue());
                } else {
                    Object memberNode = this.shardMemberMap.getMemberFromNodeId(nodeId);
                    if (memberNode == null) {
                        this.addToStaging(nodeId, tenantId, tableName, entry.getValue());
                    } else {
                        List<String> remoteIds = remoteIdsMap.get(nodeId);
                        if (remoteIds == null) {
                            remoteIds = new ArrayList<>();
                            remoteIdsMap.put(nodeId, remoteIds);
                        }
                        remoteIds.addAll(entry.getValue());
                    }
                }
            }
        }
        this.indexer.deleteLocal(tenantId, tableName, localIds);
        for (Map.Entry<String, List<String>> entry : remoteIdsMap.entrySet()) {
            this.processRemoteRecordDelete(entry.getKey(), tenantId, tableName, entry.getValue());
        }
    }

    public void put(List<Record> records) throws AnalyticsException {
        Map<Integer, List<Record>> shardedRecords = this.indexer.extractShardedRecords(records);
        int shardCopyCount = this.indexer.getReplicationFactor() + 1;
        List<Record> localRecords = new ArrayList<>();
        Map<String, List<Record>> remoteRecordsMap = new HashMap<>();
        for (Map.Entry<Integer, List<Record>> entry : shardedRecords.entrySet()) {
            Set<String> nodeIds = this.shardMemberMap.getNodeIdsForShard(entry.getKey());
            for (String nodeId : nodeIds) {
                if (nodeId.equals(this.myNodeId) && this.indexingNode) {
                    localRecords.addAll(entry.getValue());
                } else {
                    Object memberNode = this.shardMemberMap.getMemberFromNodeId(nodeId);
                    if (memberNode == null) {
                        this.addToStaging(nodeId, entry.getValue());
                    } else {
                        List<Record> remoteRecords = remoteRecordsMap.get(nodeId);
                        if (remoteRecords == null) {
                            remoteRecords = new ArrayList<>();
                            remoteRecordsMap.put(nodeId, remoteRecords);
                        }
                        remoteRecords.addAll(entry.getValue());
                    }
                }
            }
            //if this.put() is called from within spark executor, the jvm is not the CarbonJVM,
            //So we need a way to identify that, and specifically insert records to staging area.
                                if (!AnalyticsDataServiceUtils.isCarbonServer() && nodeIds.isEmpty()) {
                                for (int replica = 1; replica <= shardCopyCount; replica++) {
                                        this.addToStaging(entry.getKey(), replica, entry.getValue());
                                    }
                            }
        }
        this.indexer.putLocal(localRecords);
        for (Map.Entry<String, List<Record>> entry : remoteRecordsMap.entrySet()) {
            this.processRemoteRecordPut(entry.getKey(), entry.getValue());
        }
    }

    private void processRemoteRecordPut(String nodeId, List<Record> records) throws AnalyticsException {
        Object member = null;
        try {
            member = this.shardMemberMap.getMemberFromNodeId(nodeId);
            if (member == null) {
                this.addToStaging(nodeId, records);
            } else {
                this.remoteCommunicator.put(member, records, nodeId);
            }
        } catch (Exception e) {
            String msg = "Error in sending remote record batch put to member: " + member + 
                    " with node id: " + nodeId + ": " + e.getMessage() + " -> adding to staging area for later pickup..";
            if (!this.suppressWarnMessagesInactiveMembers.contains(nodeId)) {
                log.warn(msg);
            } else {
                log.debug(msg);
            }
            this.suppressWarnMessagesInactiveMembers.add(nodeId);
            this.checkFailedOperationCountRefresh();
            this.addToStaging(nodeId, records);
        }
    }

    private void checkFailedOperationCountRefresh() throws AnalyticsException {
        this.failedIndexOperationCount++;
        if (this.failedIndexOperationCount > FAIL_INDEX_OPERATION_REFRESH_THRESHOLD) {
            this.failedIndexOperationCount = 0;
            this.refreshIndexShardInfo();
        }
    }

    private void processRemoteRecordDelete(String nodeId, int tenantId, String tableName, List<String> ids)
            throws AnalyticsException {
        Object member = null;
        try {
            member = this.shardMemberMap.getMemberFromNodeId(nodeId);
            if (member == null) {
                this.addToStaging(nodeId, tenantId, tableName, ids);
            } else {
                this.remoteCommunicator.delete(member, tenantId, tableName, ids);
            }
        } catch (Exception e) {
            String msg = "Error in sending remote record batch delete to member: " + member + 
                    "with node id: " + nodeId + ": " + e.getMessage() + " -> adding to staging area for later pickup..";
            if (!this.suppressWarnMessagesInactiveMembers.contains(nodeId)) {
                log.warn(msg);
            } else {
                log.debug(msg);
            }
            this.suppressWarnMessagesInactiveMembers.add(nodeId);
            this.checkFailedOperationCountRefresh();
            this.addToStaging(nodeId, tenantId, tableName, ids);
        }
    }

    private void addToStaging(String nodeId, List<Record> records) throws AnalyticsException {
        this.stagingIndexDataStore.put(nodeId, records);
    }

    private void addToStaging(int shardIndex, int replica, List<Record> records) throws AnalyticsException {
        this.stagingIndexDataStore.put(shardIndex, replica, records);
        }

    private void addToStaging(String nodeId, int tenantId, String tableName, List<String> ids)
            throws AnalyticsException {
        this.stagingIndexDataStore.delete(nodeId, tenantId, tableName, ids);
    }

    private void readAndIndexTable(IndexedTableId tableId, Object[] initShardObjs) throws AnalyticsException {
        if (log.isDebugEnabled()) {
            log.debug("Starting init indexing table: " + tableId);
        }
        AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
        AnalyticsDataResponse resp = ads.get(tableId.getTenantId(), tableId.getTableName(), 1, null,
                                             Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
        Iterator<Record> itr = AnalyticsDataServiceUtils.responseToIterator(ads, resp);
        List<Record> records = new ArrayList<>(Constants.RECORDS_BATCH_SIZE);
        Record record;
        int shardIndex;
        while (itr.hasNext()) {
            record = itr.next();
            shardIndex = this.indexer.calculateShardId(record.getId());
            if (initShardObjs[shardIndex] == null) {
                continue;
            }
            records.add(record);
            if (records.size() >= Constants.RECORDS_BATCH_SIZE) {
                this.indexer.putLocal(records);
                records.clear();
            }
        }
        this.indexer.putLocal(records);
        if (log.isDebugEnabled()) {
            log.debug("Finished init indexing table: " + tableId);
        }
    }

    private Map<String, Map<Integer, Integer>> loadGlobalShards() throws AnalyticsException {
        int shardCount = this.indexer.getShardCount();
        Map<String, Map<Integer, Integer>> result = new HashMap<>();
               Set<String> nodeIdsWithShardReplica;
                Map<Integer, Integer> shards;
        for (int i = 0; i < shardCount; i++) {
            nodeIdsWithShardReplica = this.globalShardAllocationConfig.getNodeIdsWithReplica(i);
            for (String nodeIdWithReplica : nodeIdsWithShardReplica) {
                String nodeId = nodeIdWithReplica.split(GlobalShardAllocationConfig.GSAC_NODEID_REPLICA_SEPERATOR)[0];
                Integer replica = Integer.valueOf(nodeIdWithReplica.split(GlobalShardAllocationConfig
                        .GSAC_NODEID_REPLICA_SEPERATOR)[1]);
                shards = result.get(nodeId);
                if (shards == null) {
                    shards = new HashMap<>();
                    result.put(nodeId, shards);
                }
                shards.put(i, replica);
            }
        }
        return result;
    }

    private Map<Integer, Integer> allocateNewLocalShards(boolean initialAllocation) throws AnalyticsException {
                Map<Integer, Integer> result = new HashMap<>();
        int shardCopyCount = this.indexer.getReplicationFactor() + 1;
        if (log.isDebugEnabled()) {
            log.debug("Replication Factor: " + this.indexer.getReplicationFactor());
        }
        /* the current node will always allocate shards, if the shard copy count is not met by others */
        Set<String> nodeIds;
        for (int i = 0; i < this.indexer.getShardCount(); i++) {
            nodeIds = this.globalShardAllocationConfig.getNodeIdsForShard(i);
            if (!nodeIds.contains(this.myNodeId) && nodeIds.size() < shardCopyCount) {
                //(nodeIds.size() +1) is taken as the replication index of that particular shard
                result.put(i, nodeIds.size() + 1);
            }
        }
        if (initialAllocation) {
            /* if initial, try to snatch shards from other nodes who has more than me */
            Map<String, Map<Integer, Integer>> globalShards = this.loadGlobalShards();
            Map<Integer, Integer> existingShards;
            if (globalShards.get(this.myNodeId) != null) {
                existingShards = new HashMap<>(globalShards.get(this.myNodeId));
            } else {
                existingShards = new HashMap<>(0);
            }
            boolean resume = true;
            while (resume) {
                resume = false;
                for (Map.Entry<String, Map<Integer, Integer>> entry : globalShards.entrySet()) {
                    if (entry.getValue().size() > (result.size() + existingShards.size())) {
                        Iterator<Map.Entry<Integer, Integer>> itr = entry.getValue().entrySet().iterator();
                        Map.Entry<Integer, Integer> shardReplica;
                        while (itr.hasNext()) {
                            shardReplica = itr.next();
                            Integer shardIndex = shardReplica.getKey();
                            if (!result.keySet().contains(shardIndex) &&
                                    !existingShards.keySet().contains(shardIndex)) {
                                itr.remove();
                                this.globalShardAllocationConfig.removeNodeIdFromShard(shardReplica.getKey(),
                                        entry.getKey());
                                result.put(shardReplica.getKey(), shardReplica.getValue());
                                resume = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private void allocateLocalShardsFromGlobal(boolean initialAllocation) throws AnalyticsException {
        Map<Integer, Integer> newShards = this.allocateNewLocalShards(initialAllocation);
        if (initialAllocation) {
            for (Map.Entry<Integer, Integer> entry : newShards.entrySet()) {
                this.localShardAllocationConfig.setShardStatus(entry.getKey(), entry.getValue(), ShardStatus.INIT);
                this.globalShardAllocationConfig.addNodeIdForShard(entry.getKey(), entry.getValue(), this.myNodeId);
                }
        }
    }

    private Set<Integer> extractExistingLocalShardsFromGlobal() throws AnalyticsException {
        int shardCount = this.indexer.getShardCount();
        Set<Integer> myShards = new HashSet<>();
        for (int i = 0; i < shardCount; i++) {
            if (this.globalShardAllocationConfig.getNodeIdsForShard(i).contains(this.myNodeId)) {
                myShards.add(i);
            }
        }
        return myShards;
    }

        private boolean populateMyNodeId() throws AnalyticsException {
                   boolean shouldCreateNodeId = false;
        if (this.myNodeId == null) {
            try {
                File oldNodeIdFile = new File(GenericUtils.resolveLocation(Constants.DEPRECATED_MY_NODEID_LOCATION));
                File newNodeIdFile = new File(GenericUtils.resolveLocation(Constants.MY_NODEID_LOCATION));
                loadNodeIdIfExists(oldNodeIdFile, newNodeIdFile);
                if (this.myNodeId.isEmpty()) {
                    shouldCreateNodeId = true;
                }
            } catch (FileNotFoundException e) {
                if (log.isDebugEnabled()) {
                    log.debug("My node id file not found: " + e.getMessage(), e);
                }
                shouldCreateNodeId = true;
            } catch (Exception e) {
                throw new AnalyticsException("Error in reading my node id: " + e.getMessage(), e);
            }
            if (shouldCreateNodeId) {
                createNewNodeIDFile();
            }
        }
        log.info("My Analytics Node ID: " + this.myNodeId);
            return shouldCreateNodeId;
            }

            private void loadNodeIdIfExists(File oldNodeIdFile, File newNodeIdFile) throws IOException {
                if (oldNodeIdFile.exists() && !newNodeIdFile.exists()) {
                        this.myNodeId = FileUtil.readFileToString(GenericUtils.resolveLocation(
                                        Constants.DEPRECATED_MY_NODEID_LOCATION)).trim();
                        FileUtils.copyFile(oldNodeIdFile, newNodeIdFile);
                        oldNodeIdFile.delete();
                    } else {
                        this.myNodeId = FileUtil.readFileToString(GenericUtils.resolveLocation(
                                        Constants.MY_NODEID_LOCATION)).trim();
                    }
            }

            private void createNewNodeIDFile() throws AnalyticsException {
                this.myNodeId = UUID.randomUUID().toString();
                try {
                        FileUtils.writeStringToFile(new File(GenericUtils.resolveLocation(
                                        Constants.MY_NODEID_LOCATION)), this.myNodeId);
                    } catch (IOException e) {
                        throw new AnalyticsException("Error in writing my node id: " + e.getMessage(), e);
                    }
    }

    @Override
    public void onBecomingLeader() {
        /* nothing to do */
    }

    @Override
    public void onLeaderUpdate() {
        /* nothing to do */
    }

    @Override
    public void onMembersChangeForLeader(boolean removed) {
        /* nothing to do */
    }

    @Override
    public void onMemberRemoved() {
        try {
            this.queryAndRefreshClusterShardOwnerAddresses();
            log.info("Indexing node left, current shard mapping: " + this.shardMemberMap);
        } catch (AnalyticsException e) {
            log.error("Error in querying cluster shard owner addresses: " + e.getMessage(), e);
        }
    }

    public LocalShardAddressInfo generateLocalShardMemberInfo() {
        Object localMember = null;
        if (this.isClusteringEnabled()) {
            AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
            localMember = acm.getLocalMember();
        }
        return new LocalShardAddressInfo(this.myNodeId, localMember);
    }

    private void queryAndRefreshClusterShardOwnerAddresses() throws AnalyticsException {
        this.shardMemberMap.reset();
        this.suppressWarnMessagesInactiveMembers.clear();
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        List<LocalShardAddressInfo> result = acm.executeAll(Constants.ANALYTICS_INDEXING_GROUP,
                                                            new QueryLocalShardsAndAddressCall());
        for (LocalShardAddressInfo entry : result) {
            this.shardMemberMap.updateMemberMapping(entry);
        }
        Set<String> nonExistingNodeIds = this.shardMemberMap.removeAndGetNonExistingMemberNodeIds();
        globalShardAllocationConfig.removeNodeIdsFromShards(nonExistingNodeIds);
    }

    public Map<Object, Set<Integer>> generateMemberShardMappingForIndexLookup() throws AnalyticsIndexException {
        return this.shardMemberMap.generateMemberShardMappingForIndexLookup();
    }

    private void stopAndCleanupStagingWorkers() {
        if (this.stagingIndexWorkers != null) {
            for (StagingDataIndexWorker worker : this.stagingIndexWorkers) {
                worker.stop();
            }
        }
        if (this.stagingWorkerExecutor != null) {
            this.stagingWorkerExecutor.shutdownNow();
            try {
                this.stagingWorkerExecutor.awaitTermination(
                        org.wso2.carbon.analytics.dataservice.core.Constants.INDEX_WORKER_STOP_WAIT_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
                /* ignore */
            }
            this.stagingIndexWorkers = null;
            this.stagingWorkerExecutor = null;
        }
    }

    private void refreshStagingWorkers() {
        if (!this.indexingNode) {
            return;
        }
        this.stopAndCleanupStagingWorkers();
        Integer[] localShardIndices = this.localShardAllocationConfig.getShardIndices();
        if (localShardIndices.length == 0) {
            return;
        }
        this.stagingWorkerExecutor = Executors.newFixedThreadPool(localShardIndices.length,
                new ThreadFactoryBuilder().
                        setNameFormat("Thread pool- component - IndexNodeCoordinator.stagingWorkerExecutor").build());
        this.stagingIndexWorkers = new ArrayList<>(localShardIndices.length);
        for (int shardIndex : localShardIndices) {
            StagingDataIndexWorker worker = new StagingDataIndexWorker(shardIndex);
            this.stagingIndexWorkers.add(worker);
            this.stagingWorkerExecutor.execute(worker);
        }
        if (log.isDebugEnabled()) {
            log.debug("Created " + this.stagingIndexWorkers.size() + " staging worker threads.");
        }
    }

    private void processOldStagingData() {
                if (!this.indexingNode) {
                        return;
                    }
                int localShardIndices = this.indexer.getShardCount();
                if (localShardIndices == 0) {
                        return;
                    }
                this.oldStagingWorkerExecutor = Executors.newFixedThreadPool(localShardIndices,
                                new ThreadFactoryBuilder().setNameFormat("Thread pool- component - IndexNodeCoordinator.OldStagingWorkerExecutor").build());
                this.oldStagingDataIndexWorkers = new ArrayList<>(this.indexer.getShardCount());
                for (int i = 0; i < localShardIndices; i++) {
                        OldStagingDataIndexWorker worker = new OldStagingDataIndexWorker(i);
                        this.oldStagingDataIndexWorkers.add(worker);
                        this.oldStagingWorkerExecutor.execute(worker);
                    }
                if (log.isDebugEnabled()) {
                        log.debug("Created " + this.oldStagingDataIndexWorkers.size() + " staging worker threads for older " +
                                "staging tables");
                    }
            }

    public void close() {
        this.remoteCommunicator.close();
        this.stopAndCleanupStagingWorkers();
        this.localShardProcessExecutor.shutdownNow();
    }

    public void refreshIndexShardInfo() throws AnalyticsException {
        if (this.isClusteringEnabled()) {
            this.queryAndRefreshClusterShardOwnerAddresses();
        } else {
            this.shardMemberMap.reset();
            this.shardMemberMap.updateMemberMapping(this.generateLocalShardMemberInfo());
        }
        this.indexer.refreshLocalIndexShards(new HashSet<>(Arrays.asList(
                this.localShardAllocationConfig.getShardIndices())));
        this.refreshStagingWorkers();
        if (this.indexingNode) {
            this.syncLocalWithGlobal();
        }
        log.info("Indexing Initialized: " + (this.isClusteringEnabled() ?
                                             "CLUSTERED " + this.shardMemberMap : "STANDALONE") + " | Current Node Indexing: " +
                 (this.indexingNode ? "Yes" : "No"));
    }

    public void waitForIndexing(long maxWait) throws AnalyticsException {
        if (this.isClusteringEnabled()) {
            AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
            acm.executeAll(Constants.ANALYTICS_INDEXING_GROUP, new WaitForIndexingCall(maxWait));
        } else {
            this.indexer.waitForIndexingLocal(maxWait);
        }
    }

    public void clearIndexData(int tenantId, String tableName) throws AnalyticsException {
        if (this.isClusteringEnabled()) {
            AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
            acm.executeAll(Constants.ANALYTICS_INDEXING_GROUP, new ClearIndexDataCall(tenantId, tableName));
        } else {
            this.indexer.clearIndexDataLocal(tenantId, tableName);
        }
    }

    public static class IndexRefreshShardInfoCall implements Callable<String>, Serializable {

        private static final long serialVersionUID = 9184535660460958764L;

        @Override
        public String call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            /* these cluster messages are specific to AnalyticsDataServiceImpl */
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                adsImpl.getIndexer().getIndexNodeCoordinator().refreshIndexShardInfo();
                adsImpl.getIndexer().getIndexNodeCoordinator().processOldStagingData();
            }
            return "OK";
        }
    }

    public static class RefreshIndexShardAllocationCall implements Callable<String>, Serializable {

        private static final long serialVersionUID = 9184535660460958764L;

        @Override
        public String call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            /* these cluster messages are specific to AnalyticsDataServiceImpl */
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                adsImpl.getIndexer().getIndexNodeCoordinator().refreshIndexShardAllocation();
            }
            return "OK";
        }
    }

    public static class IndexDataPutCall implements Callable<String>, Serializable {

        private static final long serialVersionUID = 6223557009276101317L;

        private List<Record> records;

        public IndexDataPutCall(List<Record> records) {
            this.records = records;
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
                adsImpl.getIndexer().putLocal(this.records);
                if (log.isDebugEnabled()) {
                    log.debug("Remote put messages received: " + this.records.size());
                }
            }
            return "OK";
        }
    }

    public static class IndexDataDeleteCall implements Callable<String>, Serializable {

        private static final long serialVersionUID = 6223557009276101317L;

        private int tenantId;

        private String tableName;

        private List<String> ids;

        public IndexDataDeleteCall(int tenantId, String tableName, List<String> ids) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.ids = ids;
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
                adsImpl.getIndexer().deleteLocal(this.tenantId, this.tableName, this.ids);
                if (log.isDebugEnabled()) {
                    log.debug("Remote delete messages received: " + this.ids.size());
                }
            }
            return "OK";
        }
    }

    public static class QueryLocalShardsAndAddressCall implements Callable<LocalShardAddressInfo>, Serializable {

        private static final long serialVersionUID = -3795137566620416535L;

        @Override
        public LocalShardAddressInfo call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            /* these cluster messages are specific to AnalyticsDataServiceImpl */
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                return adsImpl.getIndexer().getIndexNodeCoordinator().generateLocalShardMemberInfo();
            }
            return null;
        }

    }

    public static class LocalShardAddressInfo implements DataSerializable {

        private String nodeId;

        private Object member;

        public LocalShardAddressInfo() {
        }

        public LocalShardAddressInfo(String nodeId, Object member) {
            this.nodeId = nodeId;
            this.member = member;
        }

        public String getNodeId() {
            return nodeId;
        }

        public Object getMember() {
            return member;
        }

        @Override
        public void readData(ObjectDataInput input) throws IOException {
            this.nodeId = input.readObject();
            this.member = input.readObject();
        }

        @Override
        public void writeData(ObjectDataOutput output) throws IOException {
            output.writeObject(this.nodeId);
            output.writeObject(this.member);
        }

    }

    public static class WaitForIndexingCall implements Callable<String>, Serializable {

        private static final long serialVersionUID = -5251608432054860585L;

        private long maxWait;

        public WaitForIndexingCall(long maxWait) {
            this.maxWait = maxWait;
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
                adsImpl.getIndexer().waitForIndexingLocal(this.maxWait);
            }
            return "OK";
        }
    }

    public static class ClearIndexDataCall implements Callable<String>, Serializable {

        private static final long serialVersionUID = -5251608432054860585L;

        private int tenantId;

        private String tableName;

        public ClearIndexDataCall(int tenantId, String tableName) {
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
                adsImpl.getIndexer().clearIndexDataLocal(this.tenantId, this.tableName);
            }
            return "OK";
        }
    }

    private void processStagingEntry(int shardIndex, StagingIndexDataEntry entry) throws AnalyticsException {
        try {
            AnalyticsDataService ads = this.indexer.getAnalyticsDataService();
            List<Record> records = AnalyticsDataServiceUtils.listRecords(ads,
                    ads.get(entry.getTenantId(), entry.getTableName(), 1, null, entry.getIds()));
            this.indexer.putLocal(records);
            Set<String> deleteIds = new HashSet<>(entry.getIds());
            deleteIds.removeAll(this.extractIds(records));
            this.indexer.deleteLocal(entry.getTenantId(), entry.getTableName(), new ArrayList<>(deleteIds));
            if (log.isDebugEnabled()) {
                log.debug("Processing staged operation [" + shardIndex + "] PUT: " +
                        +                          records.size() + " DELETE: " + deleteIds.size());
            }
        } catch (AnalyticsInterruptException e) {
            throw e;
        } catch (Exception e) {
            throw new AnalyticsException("Error in processing index staging entry: " + e.getMessage(), e);
        }

    }

    private Set<String> extractIds(List<Record> records) {
        Set<String> ids = new HashSet<>(records.size());
        for (Record record : records) {
            ids.add(record.getId());
        }
        return ids;
    }

    /**
     * This class consumes the index staging data that is been put by data publishers like the Spark analytics tables,
     * which does not have direct visibility to indexing nodes.
     */
    private class StagingDataIndexWorker implements Runnable {

        private static final int STAGING_INDEXER_WORKER_SLEEP = 5000;

        private int shardIndex;

        private boolean stop;

        public StagingDataIndexWorker(int shardIndex) {
            this.shardIndex = shardIndex;
        }

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    List<StagingIndexDataEntry> entries = stagingIndexDataStore.loadEntries(myNodeId, this.shardIndex);
                    if (!entries.isEmpty()) {
                        for (StagingIndexDataEntry entry : entries) {
                            processStagingEntry(this.shardIndex, entry);
                            stagingIndexDataStore.removeEntries(myNodeId, shardIndex,
                                    Arrays.asList(entry.getRecordId()));
                        }
                    } else {
                        Thread.sleep(STAGING_INDEXER_WORKER_SLEEP);
                    }
                } catch (AnalyticsInterruptException | InterruptedException e) {
                    // This exception can be thrown from data queues, if the shutdown hook is triggered
                    log.debug("Staging Data Index Worker Interuppted [" + this.shardIndex + "]: " + e.getMessage(), e);
                    return;
                } catch (Exception e) {
                    log.error("Error in processing staging index data: " + e.getMessage(), e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Staging Data Index Worker Exiting [" + this.shardIndex + "]");
            }
        }

        public void stop() {
            this.stop = true;
        }

    }

    /**
         * This class consumes the index staging data that is been put by data publishers like the Spark analytics tables,
          * which does not have direct visibility to indexing nodes.
     */
    private class OldStagingDataIndexWorker implements Runnable {
        private static final int STAGING_INDEXER_WORKER_SLEEP = 5000;
        private int shardIndex;
        private boolean stop;
        public OldStagingDataIndexWorker(int shardIndex) {
            this.shardIndex = shardIndex;
            }

                    @Override
                    public void run() {
                       while (!this.stop) {
                                try {
                                        List<StagingIndexDataEntry> entries =
                                                stagingIndexDataStore.loadEntriesInOldStagingTables(myNodeId, this.shardIndex);
                                        if (!entries.isEmpty()) {
                                                for (StagingIndexDataEntry entry : entries) {
                                                        processStagingEntry(this.shardIndex, entry);
                                                        stagingIndexDataStore.removeEntriesFromOldStagingTables(myNodeId, shardIndex,
                                                                Arrays.asList(entry.getRecordId()));
                                                    }
                                            } else {
                                                Thread.sleep(STAGING_INDEXER_WORKER_SLEEP);
                                                if (log.isDebugEnabled()) {
                                                        log.debug("No data available in Old staging table for shard: " + shardIndex + ", hence " +
                                                                          "stopping the old staging index worker for shard: " + shardIndex + ".");
                                                    }
                                                stop();
                                                stagingIndexDataStore.deleteStagingEntryLocation(myNodeId, this.shardIndex);
                                            }
                                    } catch (AnalyticsInterruptException | InterruptedException e) {
                                        // This exception can be thrown from data queues, if the shutdown hook is triggered
                                                log.debug("Old staging Data Index Worker Interuppted [" + this.shardIndex + "]: " + e.getMessage(),
                                                                  e);
                                        return;
                                    } catch (Exception e) {
                                        log.error("Error in processing Old staging index data: " + e.getMessage(), e);
                                    }
                            }
                        if (log.isDebugEnabled()) {
                                log.debug("Old Staging Data Index Worker Exiting [" + this.shardIndex + "]");
                            }
        }

                public void stop() {
                        this.stop = true;
                    }
    }


}