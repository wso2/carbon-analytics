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

import org.wso2.carbon.analytics.dataservice.core.indexing.IndexNodeCoordinator.LocalShardAddressInfo;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * This class represents the global shard member mapping.
 */
public class GlobalShardMemberMapping {

    private int shardCount;
    
    private GlobalShardAllocationConfig config;
    
    private Map<String, Object> nodeIdMemberMap;
    
    private Map<Integer, Set<String>> shardNodeIdMap;
    
    private Random random = new Random();
    
    public GlobalShardMemberMapping(int shardCount, GlobalShardAllocationConfig config) throws AnalyticsException {
        this.shardCount = shardCount;
        this.config = config;
        this.reset();
    }
    
    public void reset() throws AnalyticsException {
        this.nodeIdMemberMap = new HashMap<>();
        this.shardNodeIdMap = new HashMap<>();
        for (int i = 0; i < this.shardCount; i++) {
            this.shardNodeIdMap.put(i, this.config.getNodeIdsForShard(i));
        }
    }
    
    public Map<Object, Set<Integer>> generateMemberShardMappingForIndexLookup() {
        Map<Object, Set<Integer>> result = new HashMap<>();
        Object member;
        for (int i = 0; i < this.shardCount; i++) {
            member = this.lookupCandidateMemberFromNodeIds(this.shardNodeIdMap.get(i));
            if (member != null) {
                Set<Integer> shardIndices = result.get(member);
                if (shardIndices == null) {
                    shardIndices = new HashSet<>();
                    result.put(member, shardIndices);
                }
                shardIndices.add(i);
            }
        }
        return result;
    }
    
    private Object lookupCandidateMemberFromNodeIds(Set<String> nodeIds) {
        List<Object> members = new ArrayList<>(nodeIds.size());
        Object member;
        for (String nodeId : nodeIds) {
            member = this.nodeIdMemberMap.get(nodeId);
            if (member != null) {
                members.add(member);
            }
        }
        if (members.isEmpty()) {
            return null;
        }
        return members.get(AnalyticsDataIndexer.abs(this.random.nextInt()) % members.size());
    }
    
    public Set<String> getNodeIdsForShard(int shardIndex) throws AnalyticsException {
        return this.shardNodeIdMap.get(shardIndex);
    }
    
    public Object getMemberFromNodeId(String nodeId) {
        return this.nodeIdMemberMap.get(nodeId);
    }
    
    public void updateMemberMapping(LocalShardAddressInfo localShardAddressInfo) {
        this.nodeIdMemberMap.put(localShardAddressInfo.getNodeId(), localShardAddressInfo.getMember());
    }

    public Set<String> removeAndGetNonExistingMemberNodeIds() throws AnalyticsException {
                Set<String> allNodeIds = new HashSet<>();
                Set<String> removedNodeIds = new HashSet<>();
                for (Map.Entry<Integer, Set<String>> entry : this.shardNodeIdMap.entrySet()) {
                        allNodeIds.addAll(entry.getValue());
                    }
                for (String nodeId : allNodeIds) {
                        Object member = nodeIdMemberMap.get(nodeId);
                        if (member == null) {
                                nodeIdMemberMap.remove(nodeId);
                                removedNodeIds.add(nodeId);
                            }
                    }
                if (!removedNodeIds.isEmpty()) {
                        for (Map.Entry<Integer, Set<String>> entry : this.shardNodeIdMap.entrySet()) {
                                Set<String> shardNodeIds = entry.getValue();
                                shardNodeIds.removeAll(removedNodeIds);
                                shardNodeIdMap.put(entry.getKey(), shardNodeIds);
                            }
                    }
                return removedNodeIds;
            }


    @Override
    public String toString() {
        Map<Integer, Map<String, Object>> shardNodeMemberMap = new HashMap<>();
        for (Map.Entry<Integer, Set<String>> entry : this.shardNodeIdMap.entrySet()) {
            Map<String, Object> nodeMemberMap = new HashMap<>();
            for (String nodeId : entry.getValue()) {
                nodeMemberMap.put(nodeId, this.nodeIdMemberMap.get(nodeId));
            }
            shardNodeMemberMap.put(entry.getKey(), nodeMemberMap);
        }
        return shardNodeMemberMap.toString();
    }
    
}
