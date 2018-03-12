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

import com.hazelcast.core.HazelcastInstance;

import java.util.HashMap;
import java.util.HashSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;

/**
 * This class contains node/shard mapping information for all the nodes.
 */
public class GlobalShardAllocationConfig {

    static final String GSAC_NODEID_REPLICA_SEPERATOR = "##";
    private AnalyticsClusterManager acm;
    private HazelcastInstance hzInstance;
    private static final String GSAC_GET_NODE_IDS_FOR_SHARD_LOCK = "__GSA_CONFIG_NODE_IDS_FOR_SHARD_LOCK__";
    private static final String GSAC_WRITE_NODE_IDS_FOR_SHARD_LOCK = "__GSA_CONFIG_WRITE_NODE_IDS_FOR_SHARD_LOCK__";
    private static final String GSAC_NODE_IDS_SET_FOR_SHARDS = "GSA_CONFIG_NODE_IDS_SET_FOR_SHARDS";
    private static final String GSAC_REPLICAS_FOR_SHARDS = "GSA_CONFIG_REPLICAS_FOR_SHARDS";
    private Map<Integer, Set<String>> localNodeIdsForShards = new HashMap<>();
    private Map<Integer, Set<String>> localNodeIdsForShardsReplicas = new HashMap<>();

    public GlobalShardAllocationConfig(AnalyticsRecordStore recordStore) {
        acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        hzInstance = AnalyticsServiceHolder.getHazelcastInstance();
    }

    public GlobalShardAllocationConfig() {
        acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        hzInstance = AnalyticsServiceHolder.getHazelcastInstance();

    }

    public Set<String> getNodeIdsForShard(int shardIndex) throws AnalyticsException {
        return getShardSet(shardIndex, GSAC_NODE_IDS_SET_FOR_SHARDS);
    }

    Set<String> getNodeIdsWithReplica(int shardIndex) throws AnalyticsException {
        return getShardSet(shardIndex, GSAC_REPLICAS_FOR_SHARDS);
    }

    private Set<String> getShardSet(int shardIndex, String mapRef) {
        Lock gsacGetNodeIdLock = null;
        Lock gsacSetNodeIdLock = null;
        Map<Integer, Set<String>> nodeIdsSetsForShards;
        try {
            if (acm.isClusteringEnabled()) {
                gsacGetNodeIdLock = hzInstance.getLock(GSAC_GET_NODE_IDS_FOR_SHARD_LOCK);
                gsacSetNodeIdLock = hzInstance.getLock(GSAC_WRITE_NODE_IDS_FOR_SHARD_LOCK);
                gsacGetNodeIdLock.lock();
                gsacSetNodeIdLock.lock();
                nodeIdsSetsForShards = hzInstance.getMap(mapRef);
            } else {
                nodeIdsSetsForShards = localNodeIdsForShards;
            }
            Set<String> nodeIds = nodeIdsSetsForShards.get(shardIndex);
            if (nodeIds != null) {
                return nodeIds;
            } else {
                return new HashSet<>(0);
            }
        } finally {
            if (gsacGetNodeIdLock != null) {
                gsacGetNodeIdLock.unlock();
            }
            if (gsacSetNodeIdLock != null) {
                gsacSetNodeIdLock.unlock();
            }
        }
    }

    public int getShardReplica(int shardIndex, String nodeId) throws AnalyticsException {
        Lock gsacGetNodeIdLock = null;
        Lock gsacSetNodeIdLock = null;
        Map<Integer, Set<String>> nodeIdsSetsForShards;
        try {
            if (acm.isClusteringEnabled()) {
                gsacGetNodeIdLock = hzInstance.getLock(GSAC_GET_NODE_IDS_FOR_SHARD_LOCK);
                gsacSetNodeIdLock = hzInstance.getLock(GSAC_WRITE_NODE_IDS_FOR_SHARD_LOCK);
                gsacGetNodeIdLock.lock();
                gsacSetNodeIdLock.lock();
                nodeIdsSetsForShards = hzInstance.getMap(GSAC_REPLICAS_FOR_SHARDS);
            } else {
                nodeIdsSetsForShards = localNodeIdsForShardsReplicas;
            }
            Set<String> nodeIds = nodeIdsSetsForShards.get(shardIndex);
            if (nodeIds != null) {
                for (String nodeIdWithReplica : nodeIds) {
                    if (nodeIdWithReplica.startsWith(nodeId)) {
                        String replicaAsString = nodeIdWithReplica.split(GSAC_NODEID_REPLICA_SEPERATOR)[1];
                        return Integer.valueOf(replicaAsString);
                    }
                }
                return 0;
            } else {
                return 0;
            }
        } finally {
            if (gsacGetNodeIdLock != null) {
                gsacGetNodeIdLock.unlock();
            }
            if (gsacSetNodeIdLock != null) {
                gsacSetNodeIdLock.unlock();
            }
        }
    }

    public void removeNodeIdFromShard(int shardIndex, String nodeId) throws AnalyticsException {
        Set<String> nodeIds = this.getNodeIdsForShard(shardIndex);
        Set<String> nodeIdsWithShardReplica = this.getNodeIdsWithReplica(shardIndex);
        Map<String, Object> uniqueNodeIds = new HashMap<>(nodeIds.size());
        Map<String, Object> uniqueNodeIdsWithShardReplica = new HashMap<>(nodeIds.size());
        for (String entry : nodeIds) {
            uniqueNodeIds.put(entry, entry);
        }
        for (String entry : nodeIdsWithShardReplica) {
            uniqueNodeIdsWithShardReplica.put(entry, entry);
        }
        uniqueNodeIds.remove(nodeId);
        for (Map.Entry<String, Object> entry : uniqueNodeIdsWithShardReplica.entrySet()) {
            if (entry.getKey().startsWith(nodeId)) {
                uniqueNodeIdsWithShardReplica.remove(entry.getKey());
                break;
            }
        }
        updateNodeIdsSetForShardsReplicas(shardIndex, uniqueNodeIdsWithShardReplica);
        updateNodeIdsSetForShards(shardIndex, uniqueNodeIds);
    }

    public void removeNodeIdsFromShards(Set<String> nonExistingNodeIds) throws AnalyticsException {
        Map<Integer, Set<String>> nodeIdsSetsForShards;
        Map<Integer, Set<String>> nodeIdsSetsForShardReplica;
        Lock gsacWriteNodeIdLock = null;
        try {
            if (!nonExistingNodeIds.isEmpty()) {
                if (acm.isClusteringEnabled()) {
                    gsacWriteNodeIdLock = hzInstance.getLock(GSAC_WRITE_NODE_IDS_FOR_SHARD_LOCK);
                    gsacWriteNodeIdLock.lock();
                    nodeIdsSetsForShards = hzInstance.getMap(GSAC_NODE_IDS_SET_FOR_SHARDS);
                    nodeIdsSetsForShardReplica = hzInstance.getMap(GSAC_REPLICAS_FOR_SHARDS);
                } else {
                    nodeIdsSetsForShards = localNodeIdsForShards;
                    nodeIdsSetsForShardReplica = localNodeIdsForShardsReplicas;
                }
                for (Map.Entry<Integer, Set<String>> entry : nodeIdsSetsForShards.entrySet()) {
                    Set<String> shardNodeIds = entry.getValue();
                    shardNodeIds.removeAll(nonExistingNodeIds);
                    nodeIdsSetsForShards.put(entry.getKey(), shardNodeIds);
                }
                removeNonExistingNodeIdsFromReplica(nonExistingNodeIds, nodeIdsSetsForShardReplica);
            }
        } finally {
            if (gsacWriteNodeIdLock != null) {
                gsacWriteNodeIdLock.unlock();
            }
        }
    }

    private void removeNonExistingNodeIdsFromReplica(Set<String> nonExistingNodeIds,
                                                     Map<Integer, Set<String>> nodeIdsSetsForShardReplica) {
        for (Map.Entry<Integer, Set<String>> entry : nodeIdsSetsForShardReplica.entrySet()) {
            Set<String> nodeIdsWithReplica = entry.getValue();
            for (String nonExistingNodeId : nonExistingNodeIds) {
                for (String nodeIdWithReplica : nodeIdsWithReplica) {
                    if (nodeIdWithReplica.startsWith(nonExistingNodeId)) {
                        nodeIdsWithReplica.remove(nodeIdWithReplica);
                        break;
                    }
                }
            }
            nodeIdsSetsForShardReplica.put(entry.getKey(), nodeIdsWithReplica);
        }
    }

    private void updateShardIndicesMap(int shardIndex, Map<String, Object> values, String mapRef,
                                       Map<Integer, Set<String>> shardMap) {
        Map<Integer, Set<String>> nodeIdsSetsForShards;
        Lock gsacWriteNodeIdLock = null;
        try {
            if (acm.isClusteringEnabled()) {
                gsacWriteNodeIdLock = hzInstance.getLock(GSAC_WRITE_NODE_IDS_FOR_SHARD_LOCK);
                gsacWriteNodeIdLock.lock();
                nodeIdsSetsForShards = hzInstance.getMap(mapRef);
            } else {
                nodeIdsSetsForShards = shardMap;
            }
            nodeIdsSetsForShards.put(shardIndex, new HashSet<>(values.keySet()));

        } finally {
            if (gsacWriteNodeIdLock != null) {
                gsacWriteNodeIdLock.unlock();
            }
        }
    }

    private void updateNodeIdsSetForShards(int shardIndex, Map<String, Object> values) {
        updateShardIndicesMap(shardIndex, values, GSAC_NODE_IDS_SET_FOR_SHARDS, localNodeIdsForShards);
    }

    private void updateNodeIdsSetForShardsReplicas(int shardIndex, Map<String, Object> values) {
        updateShardIndicesMap(shardIndex, values, GSAC_REPLICAS_FOR_SHARDS, localNodeIdsForShardsReplicas);
    }

    public void addNodeIdForShard(int shardIndex, String nodeId) throws AnalyticsException {
        Set<String> nodeIds = this.getNodeIdsForShard(shardIndex);
        Map<String, Object> values = new HashMap<>(nodeIds.size() + 1);
        for (String entry : nodeIds) {
            values.put(entry, entry);
        }
        values.put(nodeId, nodeId);
        updateNodeIdsSetForShards(shardIndex, values);
    }

    void addNodeIdForShard(int shardIndex, int replicaIndex, String nodeId) throws AnalyticsException {
        Set<String> nodeIds = this.getNodeIdsForShard(shardIndex);
        Set<String> nodeIdsWithShardReplica = this.getNodeIdsWithReplica(shardIndex);
        Map<String, Object> uniqueNodeIds = new HashMap<>(nodeIds.size() + 1);
        Map<String, Object> uniqueNodeIdsWithShardReplica = new HashMap<>(nodeIds.size() + 1);
        for (String entry : nodeIds) {
            uniqueNodeIds.put(entry, entry);
        }
        for (String entry : nodeIdsWithShardReplica) {
            uniqueNodeIdsWithShardReplica.put(entry, entry);
        }
        uniqueNodeIds.put(nodeId, nodeId);
        uniqueNodeIdsWithShardReplica.put(nodeId + GSAC_NODEID_REPLICA_SEPERATOR + replicaIndex,
                nodeId + GSAC_NODEID_REPLICA_SEPERATOR + replicaIndex);
        updateNodeIdsSetForShards(shardIndex, uniqueNodeIds);
        updateNodeIdsSetForShardsReplicas(shardIndex, uniqueNodeIdsWithShardReplica);
    }

}
