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
import org.wso2.carbon.stream.processor.core.model.LastPublishedTimestamp;
import org.wso2.carbon.stream.processor.core.model.LastPublishedTimestampCollection;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerManager;

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

    public PassiveNodeOutputSyncManager(ClusterCoordinator clusterCoordinator, SinkHandlerManager sinkHandlerManager,
                                        String host, String port, boolean liveSyncEnabled) {
        this.activeNodeHost = host;
        this.activeNodePort = port;
        this.liveSyncEnabled = liveSyncEnabled;
        this.clusterCoordinator = clusterCoordinator;
        this.sinkHandlerManager = sinkHandlerManager;
    }

    @Override
    public void run() {

        if (liveSyncEnabled) {
            String url = "http://%s:%d/ha/publishedTimestamp";
            URI baseURI = URI.create(String.format(url, activeNodeHost, Integer.parseInt(activeNodePort)));
            String httpResponseMessage = RequestUtil.sendRequest(baseURI);
            if (log.isDebugEnabled()) {
                log.debug("Passive Node: Accessed active node to retrieve last published timestamps.");
            }

            Map<String, SinkHandler> sinkHandlerMap = sinkHandlerManager.getRegisteredSinkHandlers();
            LastPublishedTimestampCollection timestampCollection = new Gson().fromJson(httpResponseMessage,
                    LastPublishedTimestampCollection.class);

            if (sinkHandlerMap.size() != timestampCollection.size()) {
                //This might happen due to delays in deploying siddhi applications to both nodes
                log.warn("Passive Node: Active node and Passive node do not have same amount of sink handlers.");
            }

            for (LastPublishedTimestamp publisherSyncTimestamp : timestampCollection.getLastPublishedTimestamps()) {
                if (log.isDebugEnabled()) {
                    log.debug("Passive Node: Updating publisher queue for sink " + publisherSyncTimestamp.getId()
                            + " with timestamp " + publisherSyncTimestamp.getTimestamp() + ". Live state sync on");
                }
                HACoordinationSinkHandler sinkHandler = (HACoordinationSinkHandler) sinkHandlerMap.
                        get(publisherSyncTimestamp.getId());
                sinkHandler.trimPassiveNodeEventQueue(publisherSyncTimestamp.getTimestamp());
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
                    sinkHandler.trimPassiveNodeEventQueue(publisherSyncTimestamp.getValue());
                }
            }
        }
    }
}
