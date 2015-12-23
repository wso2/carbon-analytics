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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * This class contains node/shard mapping information for all the nodes.
 */
public class GlobalShardAllocationConfig {
    
    private AnalyticsRecordStore recordStore;
    
    public GlobalShardAllocationConfig(AnalyticsRecordStore recordStore) {
        this.recordStore = recordStore;
    }
    
    public String getNodeIdForShard(int shardIndex) throws AnalyticsException {
        List<Record> records = GenericUtils.listRecords(this.recordStore, this.recordStore.get(Constants.META_INFO_TENANT_ID, 
                Constants.GLOBAL_SHARD_ALLOCATION_CONFIG_TABLE, 1, null, Arrays.asList(String.valueOf(shardIndex))));
        if (records.size() > 0) {
            return records.get(0).getValue(Constants.GLOBAL_SHARD_ALLOCATION_CONFIG_COLUMN).toString();
        } else {
            return null;
        }
    }
    
    public void setNodeIdForShard(int shardIndex, String nodeId) throws AnalyticsException {
        Map<String, Object> values = new HashMap<>(1);
        values.put(Constants.GLOBAL_SHARD_ALLOCATION_CONFIG_COLUMN, nodeId);
        Record record = new Record(String.valueOf(shardIndex), Constants.META_INFO_TENANT_ID, 
                Constants.GLOBAL_SHARD_ALLOCATION_CONFIG_TABLE, values);
        this.recordStore.put(Arrays.asList(record));
    }
    
}
