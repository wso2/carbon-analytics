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

package org.wso2.carbon.stream.processor.core.ha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.stream.processor.core.ha.util.CoordinationConstants;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerManager;
import org.wso2.siddhi.core.table.record.RecordTableHandler;
import org.wso2.siddhi.core.table.record.RecordTableHandlerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages Active node output syncing when live state sync is disabled.
 */
public class ActiveNodeOutputSyncManager implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ActiveNodeOutputSyncManager.class);
    private final HACoordinationSinkHandlerManager sinkHandlerManager;
    private final HACoordinationRecordTableHandlerManager recordTableHandlerManager;
    private final ClusterCoordinator clusterCoordinator;

    public ActiveNodeOutputSyncManager(SinkHandlerManager sinkHandlerManager,
                                       RecordTableHandlerManager recordTableHandlerManager,
                                       ClusterCoordinator clusterCoordinator) {
        this.sinkHandlerManager = (HACoordinationSinkHandlerManager) sinkHandlerManager;
        this.recordTableHandlerManager = (HACoordinationRecordTableHandlerManager) recordTableHandlerManager;
        this.clusterCoordinator = clusterCoordinator;
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Active Node: Live sync disabled. Setting Output Last Processed Timestamps");
        }
        Map<String, SinkHandler> registeredSinkHandlers = sinkHandlerManager.getRegisteredSinkHandlers();
        Map<String, Long> sinkProperties = new HashMap<>();
        for (SinkHandler sinkHandler : registeredSinkHandlers.values()) {
            long timestamp = ((HACoordinationSinkHandler) sinkHandler).getActiveNodeLastPublishedTimestamp();
            sinkProperties.put(sinkHandler.getElementId(), timestamp);
            if (log.isDebugEnabled()) {
                log.debug("Active Node: Live sync disabled. " + sinkHandler.getElementId() + " sink handler " +
                        "last published events timestamp: " + timestamp);
            }
        }
        HAManager.getActiveNodePropertiesMap().put(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMPS, sinkProperties);

        Map<String, RecordTableHandler> registeredRecordTableHandlers = recordTableHandlerManager.
                getRegisteredRecordTableHandlers();
        Map<String, Long> recordTableProperties = new HashMap<>();
        for (Map.Entry<String, RecordTableHandler> recordTableHandler : registeredRecordTableHandlers.entrySet()) {
            long timestamp = ((HACoordinationRecordTableHandler) recordTableHandler.getValue()).
                    getActiveNodeLastOperationTimestamp();
            recordTableProperties.put(recordTableHandler.getKey(), timestamp);
        }
        HAManager.getActiveNodePropertiesMap().put(CoordinationConstants.ACTIVE_RECORD_TABLE_LAST_UPDATE_TIMESTAMPS,
                recordTableProperties);
        clusterCoordinator.setPropertiesMap(HAManager.getActiveNodePropertiesMap());
    }
}