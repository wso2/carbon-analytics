/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sp.jobmanager.core.topology;

import org.wso2.carbon.sp.jobmanager.core.util.TransportStrategy;

/**
 *
 */
public class SubscriptionStrategyDataHolder {
    private TransportStrategy strategy;
    private int offeredParallelism;
    private String partitionKey;

    public SubscriptionStrategyDataHolder(int offeredParallelism, TransportStrategy strategy, String partitionKey) {
        this.offeredParallelism = offeredParallelism;
        this.strategy = strategy;
        this.partitionKey = partitionKey;
    }

    public TransportStrategy getStrategy() {
        return strategy;
    }

    public int getOfferedParallelism() {
        return offeredParallelism;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setOfferedParallelism(int offeredParallelism) {
        this.offeredParallelism = offeredParallelism;
    }
}
