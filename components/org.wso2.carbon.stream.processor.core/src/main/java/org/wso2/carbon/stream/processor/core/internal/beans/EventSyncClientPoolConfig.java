/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stream.processor.core.internal.beans;

import org.wso2.carbon.config.annotation.Element;

public class EventSyncClientPoolConfig {
    @Element(description = "Number of active connections in the event sync client pool", required = false)
    private int maxActive = 10;
    @Element(description = "Number of total connections in the event sync client pool", required = false)
    private int maxTotal = 10;
    @Element(description = "Number of idle connections in the event sync client pool", required = false)
    private int maxIdle = 10;
    @Element(description = " the maximum amount of time to wait for an idle object when the pool is exhausted", required = false)
    private long maxWait = 60000;
    @Element(description = "he minimum number of milliseconds an object can sit idle in the pool before it is eligible for eviction", required = false)
    private long minEvictableIdleTimeMillis = 120000;

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
}
