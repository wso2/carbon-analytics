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

import org.apache.commons.io.FileUtils;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The local node shard configuration.
 */
public class LocalShardAllocationConfig implements Serializable {

    private static final long serialVersionUID = -5632823561738758193L;

    private Map<Integer, ShardStatus> shardStatusMap = new HashMap<>();
    private Map<Integer, Integer> shardReplicaMap = new HashMap<>();

    private boolean init;

    public LocalShardAllocationConfig() throws AnalyticsException {

        try {
            initShardStatusMap();
            initShardReplicaMap();
        } catch (FileNotFoundException e) {
            this.init = false;
            return;
        } catch (Exception e) {
            throw new AnalyticsException("Error in loading local shard allocation configuration: " + e.getMessage(), e);
        }
    }
            private void initShardReplicaMap() throws AnalyticsException {
                String[] entries;
                String[] entryStrArray;
                int shardIndex;
                String shardReplicaConfig;
                try {
                        shardReplicaConfig = FileUtil.readFileToString(GenericUtils.resolveLocation(Constants
                                       .LOCAL_SHARD_REPLICA_CONFIG_LOCATION));
                    } catch (FileNotFoundException e) {
                        shardReplicaConfig = "";
                    }  catch (IOException e) {
                        throw new AnalyticsException("Error in loading local shard allocation configuration: " + e.getMessage(), e);
                    }
                entries = shardReplicaConfig.split("\n");
                int replica;
                for (String entry : entries) {
                        entry = entry.trim();
                        if (!entry.isEmpty()) {
                            entryStrArray = entry.split(",");
                                shardIndex = Integer.parseInt(entryStrArray[0].trim());
                                replica = Integer.valueOf(entryStrArray[1].trim());
                                this.shardReplicaMap.put(shardIndex, replica);
                            }
                    }
        }

            private void initShardStatusMap() throws IOException {
                String shardStatusConfig;
                        File oldShardAllocationFile = new File(
                                GenericUtils.resolveLocation(Constants.DEPRECATED_LOCAL_SHARD_ALLOCATION_CONFIG_LOCATION));
                File newShardAllocationFile = new File(
                                GenericUtils.resolveLocation(Constants.LOCAL_SHARD_ALLOCATION_CONFIG_LOCATION));
                File indexStagingLocation = new File(GenericUtils.resolveLocation(
                                Constants.DEFAULT_LOCAL_INDEX_STAGING_LOCATION));
                File indexStoreLocation = new File(GenericUtils.resolveLocation(
                                Constants.DEFAULT_LOCAL_INDEX_STAGING_LOCATION));
                if (oldShardAllocationFile.exists() && !newShardAllocationFile.exists()) {
                        if (indexStagingLocation.exists()) {
                                FileUtils.deleteDirectory(indexStagingLocation);
                            }
                        if (indexStoreLocation.exists()) {
                                FileUtils.deleteDirectory(indexStoreLocation);
                            }
                        oldShardAllocationFile.delete();
                    }
                shardStatusConfig = FileUtil.readFileToString(GenericUtils.resolveLocation(Constants
                                .LOCAL_SHARD_ALLOCATION_CONFIG_LOCATION));
                String[] entries = shardStatusConfig.split("\n");
        int shardIndex;
        ShardStatus status;
        String[] entryStrArray;
        for (String entry : entries) {
            entry = entry.trim();
            if (!entry.isEmpty()) {
                entryStrArray = entry.split(",");
                shardIndex = Integer.parseInt(entryStrArray[0].trim());
                status = ShardStatus.valueOf(entryStrArray[1].trim());
                this.shardStatusMap.put(shardIndex, status);
            }
        }
        this.init = this.shardStatusMap.size() > 0;
    }

    public boolean isInit() {
        return init;
    }

    public int getShardReplica(int shardIndex) {
        if (this.shardReplicaMap.containsKey(shardIndex)) {
                        return this.shardReplicaMap.get(shardIndex);
                    }
                return 0;
        }

    public ShardStatus getShardStatus(int shardIndex) {
        return this.shardStatusMap.get(shardIndex);
    }

    public Integer[] getShardIndices() {
        return this.shardStatusMap.keySet().toArray(new Integer[0]);
    }

    public void save() throws AnalyticsException {
        try {
            FileUtils.writeStringToFile(new File(GenericUtils.resolveLocation(
                    Constants.LOCAL_SHARD_ALLOCATION_CONFIG_LOCATION)), this.toString());
            FileUtils.writeStringToFile(new File(GenericUtils.resolveLocation(Constants
                                        .LOCAL_SHARD_REPLICA_CONFIG_LOCATION)), this.shardReplicaToString());
        } catch (IOException e) {
            throw new AnalyticsException("Error in saving local shard allocation configuration: " + e.getMessage(), e);
        }
    }

    public void removeShardIndex(int shardIndex) throws AnalyticsException {
        this.shardStatusMap.remove(shardIndex);
        this.shardReplicaMap.remove(shardIndex);
    }

    public void setShardStatus(int shardIndex, ShardStatus status) throws AnalyticsException {
        this.shardStatusMap.put(shardIndex, status);
    }

    void setShardStatus(int shardIndex, int replica, ShardStatus status) throws AnalyticsException {
                this.shardStatusMap.put(shardIndex, status);
                this.shardReplicaMap.put(shardIndex, replica);
            }

            private String shardReplicaToString() {
                StringBuilder builder = new StringBuilder();
                for (Map.Entry<Integer, Integer> entry : this.shardReplicaMap.entrySet()) {
                        builder.append(entry.getKey() + "," + entry.getValue().toString() + "\n");
                    }
                return builder.toString();
            }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, ShardStatus> entry : this.shardStatusMap.entrySet()) {
            builder.append(entry.getKey() + "," + entry.getValue().toString() + "\n");
        }
        return builder.toString();
    }

    public static enum ShardStatus {
        INIT,
        RESTORE,
        NORMAL
    }

}