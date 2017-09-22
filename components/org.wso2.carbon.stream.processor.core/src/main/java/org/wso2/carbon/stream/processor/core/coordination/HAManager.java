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

package org.wso2.carbon.stream.processor.core.coordination;

import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HAManager {

    private ClusterCoordinator clusterCoordinator;
    private ScheduledExecutorService haPassiveNodeSchedulerService;
    private ScheduledFuture passiveNodeScheduledFuture;

    public HAManager(ClusterCoordinator clusterCoordinator, String liveStateSync, String host, String port) {
        this.clusterCoordinator = clusterCoordinator;
    }

    public void start() {

        HACoordinationSinkHandlerManager coordinationSinkHandlerManger = new HACoordinationSinkHandlerManager();
        StreamProcessorDataHolder.getSiddhiManager().setSinkHandlerFactory(coordinationSinkHandlerManger);
        HAEventListener eventListener = new HAEventListener(coordinationSinkHandlerManger);
        clusterCoordinator.registerEventListener(eventListener);
        boolean isActiveNode = clusterCoordinator.isLeaderNode();

        if (!isActiveNode) {
            haPassiveNodeSchedulerService = Executors.newSingleThreadScheduledExecutor();
            passiveNodeScheduledFuture = haPassiveNodeSchedulerService.scheduleAtFixedRate(
                    new PassivePublisherSyncManager(coordinationSinkHandlerManger), 0, 1, TimeUnit.MINUTES);
        }

        StreamProcessorDataHolder.getInstance().setSinkHandlerManager(coordinationSinkHandlerManger);
    }

    public void stop() {
        if (passiveNodeScheduledFuture != null) {
            passiveNodeScheduledFuture.cancel(false);
        }
        haPassiveNodeSchedulerService.shutdown();
    }

}
