/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.stream.processor.core.ha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;
import org.wso2.siddhi.core.stream.input.source.SourceHandlerManager;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerManager;
import org.wso2.siddhi.core.table.record.RecordTableHandler;
import org.wso2.siddhi.core.table.record.RecordTableHandlerManager;
import org.wso2.siddhi.core.util.transport.BackoffRetryCounter;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Event listener implementation that listens for changes that happen within the cluster used for 2 node minimum HA
 */
public class HAEventListener extends MemberEventListener {

    private static final Logger log = LoggerFactory.getLogger(HAEventListener.class);
    private BackoffRetryCounter backoffRetryCounter = new BackoffRetryCounter();

    @Override
    public void memberAdded(NodeDetail nodeDetail) {
        // Do Nothing
    }

    @Override
    public void memberRemoved(NodeDetail nodeDetail) {
        // Do Nothing
    }

    @Override
    public void coordinatorChanged(NodeDetail nodeDetail) {
        ClusterCoordinator clusterCoordinator = StreamProcessorDataHolder.getClusterCoordinator();
        if (clusterCoordinator != null && clusterCoordinator.isLeaderNode()) {
            log.info("HA Deployment: This Node is now the Active Node");
            StreamProcessorDataHolder.getHAManager().changeToActive();
            SinkHandlerManager sinkHandlerManager = StreamProcessorDataHolder.getSinkHandlerManager();
            Map<String, SinkHandler> registeredSinkHandlers = sinkHandlerManager.getRegisteredSinkHandlers();
            for (SinkHandler sinkHandler : registeredSinkHandlers.values()) {
                ((HACoordinationSinkHandler) sinkHandler).setAsActive();
            }
            SourceHandlerManager sourceHandlerManager = StreamProcessorDataHolder.getSourceHandlerManager();
            Map<String, SourceHandler> registeredSourceHandlers = sourceHandlerManager.
                    getRegsiteredSourceHandlers();
            for (SourceHandler sourceHandler : registeredSourceHandlers.values()) {
                ((HACoordinationSourceHandler) sourceHandler).setAsActive();
            }
            RecordTableHandlerManager recordTableHandlerManager = StreamProcessorDataHolder.
                    getRecordTableHandlerManager();
            Map<String, RecordTableHandler> registeredRecordTableHandlers = recordTableHandlerManager.
                    getRegisteredRecordTableHandlers();
            for (RecordTableHandler recordTableHandler : registeredRecordTableHandlers.values()) {
                try {
                    ((HACoordinationRecordTableHandler) recordTableHandler).setAsActive();
                } catch (ConnectionUnavailableException e) {
                    backoffRetryCounter.reset();
                    log.error("HA Deployment: Error in connecting to table " + ((HACoordinationRecordTableHandler)
                            recordTableHandler).getTableId() + " while changing from passive" +
                            " state to active, will retry in " + backoffRetryCounter.getTimeInterval(), e);
                    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                    backoffRetryCounter.increment();
                    scheduledExecutorService.schedule(new RetryRecordTableConnection(backoffRetryCounter,
                                    recordTableHandler, scheduledExecutorService),
                            backoffRetryCounter.getTimeIntervalMillis(), TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
