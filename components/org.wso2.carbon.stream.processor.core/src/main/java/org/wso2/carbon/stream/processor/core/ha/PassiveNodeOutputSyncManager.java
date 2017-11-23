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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.stream.processor.core.ha.util.CoordinationConstants;
import org.wso2.carbon.stream.processor.core.ha.util.RequestUtil;
import org.wso2.carbon.stream.processor.core.model.OutputSyncTimestamps;
import org.wso2.carbon.stream.processor.core.model.OutputSyncTimestampCollection;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerManager;
import org.wso2.siddhi.core.table.record.RecordTableHandler;
import org.wso2.siddhi.core.table.record.RecordTableHandlerManager;

import java.net.URI;
import java.util.Map;

/**
 * Class that manages the periodic calls the passive node makes to the active node to sync the publishers
 */
public class PassiveNodeOutputSyncManager implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PassiveNodeOutputSyncManager.class);

    private final boolean liveSyncEnabled;
    private final String activeNodeHost;
    private final String activeNodePort;
    private final ClusterCoordinator clusterCoordinator;
    private final SinkHandlerManager sinkHandlerManager;
    private final RecordTableHandlerManager recordTableHandlerManager;
    private final String username;
    private final String password;

    public PassiveNodeOutputSyncManager(ClusterCoordinator clusterCoordinator, SinkHandlerManager sinkHandlerManager,
                                        RecordTableHandlerManager recordTableHandlerManager, String host, String port,
                                        boolean liveSyncEnabled, String username, String password) {
        this.activeNodeHost = host;
        this.activeNodePort = port;
        this.liveSyncEnabled = liveSyncEnabled;
        this.clusterCoordinator = clusterCoordinator;
        this.sinkHandlerManager = sinkHandlerManager;
        this.recordTableHandlerManager = recordTableHandlerManager;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {

        if (liveSyncEnabled) {

            String url = "http://%s:%d/ha/outputSyncTimestamps";
            URI baseURI = URI.create(String.format(url, activeNodeHost, Integer.parseInt(activeNodePort)));
            String httpResponseMessage = RequestUtil.sendRequest(baseURI, username, password);
            if (log.isDebugEnabled()) {
                log.debug("Passive Node: Accessed active node to retrieve last published timestamps.");
            }

            OutputSyncTimestampCollection outputSyncCollection = new Gson().fromJson(httpResponseMessage,
                    OutputSyncTimestampCollection.class);

            Map<String, SinkHandler> sinkHandlerMap = sinkHandlerManager.getRegisteredSinkHandlers();

            if (sinkHandlerMap.size() != outputSyncCollection.sizeOfSinkTimestamps()) {
                //This might happen due to delays in deploying siddhi applications to both nodes
                log.warn("Passive Node: Active node and Passive node do not have same amount of sink handlers.");
            }

            for (OutputSyncTimestamps publisherSyncTimestamp : outputSyncCollection.getLastPublishedTimestamps()) {
                if (log.isDebugEnabled()) {
                    log.debug("Passive Node: Updating publisher queue for sink " + publisherSyncTimestamp.getId()
                            + " with timestamp " + publisherSyncTimestamp.getTimestamp() + ". Live state sync on");
                }
                HACoordinationSinkHandler sinkHandler = (HACoordinationSinkHandler) sinkHandlerMap.
                        get(publisherSyncTimestamp.getId());
                if (sinkHandler != null) {
                    sinkHandler.trimPassiveNodeEventQueue(publisherSyncTimestamp.getTimestamp());
                }
            }

            // Updating the record table queues
            Map<String, RecordTableHandler> recordTableHandlerMap = recordTableHandlerManager.
                    getRegisteredRecordTableHandlers();

            if (recordTableHandlerMap.size() != outputSyncCollection.sizeOfRecordTableTimestamps()) {
                //This might happen due to delays in deploying siddhi applications to both nodes
                log.warn("Passive Node: Active node and Passive node do not have same amount of sink handlers.");
            }

            for (OutputSyncTimestamps recordTableSyncTimestamp : outputSyncCollection.
                    getRecordTableLastUpdatedTimestamps()) {
                if (log.isDebugEnabled()) {
                    log.debug("Passive Node: Updating record table queue for record table " +
                            recordTableSyncTimestamp.getId() + " with timestamp " + recordTableSyncTimestamp.
                            getTimestamp() + ". Live state sync on");
                }
                HACoordinationRecordTableHandler recordTableHandler = (HACoordinationRecordTableHandler)
                        recordTableHandlerMap.get(recordTableSyncTimestamp.getId());
                if (recordTableHandler != null) {
                    recordTableHandler.trimRecordTableEventQueue(recordTableSyncTimestamp.getTimestamp());
                }
            }

        } else {

            Map<String, Object> propertiesMap = clusterCoordinator.getLeaderNode().getPropertiesMap();
            if (propertiesMap != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Passive Node: Accessed active node properties to retrieve last published timestamps.");
                }
                Map<String, Long> activeProcessedLastTimestampsMap = (Map<String, Long>) propertiesMap.
                        get(CoordinationConstants.ACTIVE_PROCESSED_LAST_TIMESTAMPS);

                Map<String, SinkHandler> sinkHandlerMap = sinkHandlerManager.getRegisteredSinkHandlers();

                if (sinkHandlerMap.size() != activeProcessedLastTimestampsMap.size()) {
                    //This might happen due to delays in deploying siddhi applications to both nodes
                    log.warn("Passive Node: Active node and Passive node do not have same amount of Sinks." +
                            " Make sure both nodes have deployed same amount of Siddhi Applications.");
                }

                for (Map.Entry<String, Long> publisherSyncTimestamp : activeProcessedLastTimestampsMap.entrySet()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Passive Node: Updating publisher queue for sink " + publisherSyncTimestamp.getKey()
                                + " with timestamp " + publisherSyncTimestamp.getValue() + ". Live state sync off");
                    }
                    HACoordinationSinkHandler sinkHandler = (HACoordinationSinkHandler) sinkHandlerMap.
                            get(publisherSyncTimestamp.getKey());
                    if (sinkHandler != null) {
                        sinkHandler.trimPassiveNodeEventQueue(publisherSyncTimestamp.getValue());
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("Passive Node: Accessed active node properties to retrieve last timestamp " +
                            "of update on record table.");
                }
                Map<String, Long> activeRecordTableLastUpdateTimestampsMap = (Map<String, Long>) propertiesMap.
                        get(CoordinationConstants.ACTIVE_RECORD_TABLE_LAST_UPDATE_TIMESTAMPS);

                Map<String, RecordTableHandler> recordTableHandlerMap = recordTableHandlerManager.
                        getRegisteredRecordTableHandlers();

                if (recordTableHandlerMap.size() != activeRecordTableLastUpdateTimestampsMap.size()) {
                    //This might happen due to delays in deploying siddhi applications to both nodes
                    log.warn("Passive Node: Active node and Passive node do not have same amount of Record Tables." +
                            " Make sure both nodes have deployed same amount of Siddhi Applications.");
                }

                for (Map.Entry<String, Long> recordTableSyncTimestamp : activeRecordTableLastUpdateTimestampsMap.
                        entrySet()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Passive Node: Updating record table " + recordTableSyncTimestamp.getKey()
                                + " with timestamp " + recordTableSyncTimestamp.getValue() + ". Live state sync off");
                    }
                    HACoordinationRecordTableHandler recordTableHandler = (HACoordinationRecordTableHandler)
                            recordTableHandlerMap.get(recordTableSyncTimestamp.getKey());
                    if (recordTableHandler != null) {
                        recordTableHandler.trimRecordTableEventQueue(recordTableSyncTimestamp.getValue());
                    }
                }
            }
        }
    }
}
