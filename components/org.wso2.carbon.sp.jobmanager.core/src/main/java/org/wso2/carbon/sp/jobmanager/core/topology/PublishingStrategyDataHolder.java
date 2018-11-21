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
 * Data Holder to hold required details of Publishing Strategy of an Output Stream. Given Output Stream can have
 * multiple publishing strategies for each consumer.
 */
public class PublishingStrategyDataHolder {
    private TransportStrategy strategy;
    private String groupingField = null;
    private int parallelism;

    public PublishingStrategyDataHolder(TransportStrategy strategy, int parallelism) {
        this.strategy = strategy;
        this.parallelism = parallelism;
    }

    public PublishingStrategyDataHolder(TransportStrategy strategy, String groupingField, int parallelism) {
        this.strategy = strategy;
        this.groupingField = groupingField;
        this.parallelism = parallelism;
    }

    public TransportStrategy getStrategy() {
        return strategy;
    }

    public String getGroupingField() {
        return groupingField;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }
}
