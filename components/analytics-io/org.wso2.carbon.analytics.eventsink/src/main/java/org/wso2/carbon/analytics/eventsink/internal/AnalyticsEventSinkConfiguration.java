/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.eventsink.internal;

import org.wso2.carbon.analytics.eventsink.internal.util.AnalyticsEventSinkConstants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the configuration class which specifies about actual queue length and bundle size of the events
 * that needs to be pushed immediately to the data store.
 */
@XmlRootElement(name = "AnalyticsEventSinkConfiguration")
public class AnalyticsEventSinkConfiguration {
    private int queueSize;
    private int batchSize;
    private int workerPoolSize;
    private int maxQueueCapacity;

    public AnalyticsEventSinkConfiguration() {
        this.queueSize = AnalyticsEventSinkConstants.DEFAULT_EVENT_QUEUE_SIZE;
        this.batchSize = AnalyticsEventSinkConstants.DEFAULT_BATCH_SIZE * 1000;
        this.workerPoolSize = AnalyticsEventSinkConstants.DEFAULT_WORKER_POOL_SIZE;
        this.maxQueueCapacity = AnalyticsEventSinkConstants.DEFAULT_MAX_QUEUE_CAPACITY * 1000000;
    }

    @XmlElement(name = "QueueSize")
    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    @XmlElement(name = "maxBatchSize")
    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize * 1000;
    }

    @XmlElement(name = "WorkerPoolSize")
    public int getWorkerPoolSize() {
        return workerPoolSize;
    }

    public void setWorkerPoolSize(int workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
    }

    @XmlElement(name = "maxQueueCapacity")
    public int getMaxQueueCapacity() {
        return maxQueueCapacity;
    }

    public void setMaxQueueCapacity(int maxQueueCapacity) {
        this.maxQueueCapacity = maxQueueCapacity * 1000000;
    }
}
