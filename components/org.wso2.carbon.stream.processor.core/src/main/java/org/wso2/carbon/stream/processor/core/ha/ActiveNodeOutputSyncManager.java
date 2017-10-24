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
import org.wso2.carbon.stream.processor.core.internal.beans.ActiveNodeLastPublishedEventTimeStamp;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages Active node output syncing when live state sync is disabled.
 *
 */
public class ActiveNodeOutputSyncManager implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ActiveNodeOutputSyncManager.class);
    private final HACoordinationSinkHandlerManager sinkHandlerManager;
    private final ClusterCoordinator clusterCoordinator;

    public ActiveNodeOutputSyncManager(SinkHandlerManager sinkHandlerManager, ClusterCoordinator clusterCoordinator){
        this.sinkHandlerManager = (HACoordinationSinkHandlerManager) sinkHandlerManager;
        this.clusterCoordinator = clusterCoordinator;
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Active Node: Live sync disabled. Setting Output Last Processed Timestamps");
        }
        Map<String, SinkHandler> registeredSinkHandlers = sinkHandlerManager.getRegisteredSinkHandlers();
        Map<String, Long> propertiesMap = new HashMap<>();
        for (SinkHandler sinkHandler : registeredSinkHandlers.values()) {
            ActiveNodeLastPublishedEventTimeStamp activeNodeLastPublishedTimestamp =
                    ((HACoordinationSinkHandler) sinkHandler).getActiveNodeLastPublishedTimestamp();
            propertiesMap.put(activeNodeLastPublishedTimestamp.getSinkHandlerElementId(),
                    activeNodeLastPublishedTimestamp.getTimestamp());
            if (log.isDebugEnabled()) {
                log.debug("Active Node: Live sync disabled. " + sinkHandler.getElementId() + " sink handler " +
                        "last published events timestamp: " + activeNodeLastPublishedTimestamp.getTimestamp());
            }
        }
        HAManager.activeNodePropertiesMap.put(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMPS, propertiesMap);
        clusterCoordinator.setPropertiesMap(HAManager.activeNodePropertiesMap);
    }
}