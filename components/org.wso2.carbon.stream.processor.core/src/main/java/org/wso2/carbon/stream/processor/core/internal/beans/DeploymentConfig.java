/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.stream.processor.core.internal.beans;

import org.wso2.carbon.config.annotation.Configuration;

/**
 * Bean class for the deployment configurations.
 */
@Configuration(namespace = "deployment.config", description = "Cluster Coordination Mode Configuration")
public class DeploymentConfig {

    private String type;
    private LiveSyncConfig liveSync;
    private int outputSyncInterval = 60000;
    private int stateSyncGracePeriod = 120000;
    private int sinkQueueCapacity = 20000;
    private int sourceQueueCapacity = 20000;
    private int retryAppSyncPeriod = 60000;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LiveSyncConfig getLiveSync() {
        return liveSync;
    }

    public void setLiveSync(LiveSyncConfig liveSync) {
        this.liveSync = liveSync;
    }

    public int getOutputSyncInterval() {
        return outputSyncInterval;
    }

    public void setOutputSyncInterval(int outputSyncInterval) {
        this.outputSyncInterval = outputSyncInterval;
    }

    public int getStateSyncGracePeriod() {
        return stateSyncGracePeriod;
    }

    public void setStateSyncGracePeriod(int stateSyncGracePeriod) {
        this.stateSyncGracePeriod = stateSyncGracePeriod;
    }

    public int getSinkQueueCapacity() {
        return sinkQueueCapacity;
    }

    public void setSinkQueueCapacity(int sinkQueueCapacity) {
        this.sinkQueueCapacity = sinkQueueCapacity;
    }

    public int getSourceQueueCapacity() {
        return sourceQueueCapacity;
    }

    public void setSourceQueueCapacity(int sourceQueueCapacity) {
        this.sourceQueueCapacity = sourceQueueCapacity;
    }

    public int getRetryAppSyncPeriod() {
        return retryAppSyncPeriod;
    }

    public void setRetryAppSyncPeriod(int retryAppSyncPeriod) {
        this.retryAppSyncPeriod = retryAppSyncPeriod;
    }
}
