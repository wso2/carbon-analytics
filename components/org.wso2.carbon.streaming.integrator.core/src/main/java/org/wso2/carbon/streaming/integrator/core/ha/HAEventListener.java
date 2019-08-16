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

package org.wso2.carbon.streaming.integrator.core.ha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.streaming.integrator.core.ha.transport.EventSyncConnectionPoolManager;
import org.wso2.carbon.streaming.integrator.core.ha.util.HAConstants;
import org.wso2.carbon.streaming.integrator.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.streaming.integrator.core.persistence.PersistenceManager;
import io.siddhi.core.stream.input.source.SourceHandler;
import io.siddhi.core.stream.input.source.SourceHandlerManager;
import io.siddhi.core.stream.output.sink.SinkHandler;
import io.siddhi.core.stream.output.sink.SinkHandlerManager;
import io.siddhi.core.table.record.RecordTableHandler;
import io.siddhi.core.table.record.RecordTableHandlerManager;
import io.siddhi.core.util.transport.BackoffRetryCounter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
        log.info("memberAdded event received for node id : " + nodeDetail.getNodeId());
        ClusterCoordinator clusterCoordinator = StreamProcessorDataHolder.getClusterCoordinator();
        HAManager haManager = StreamProcessorDataHolder.getHAManager();

        if (clusterCoordinator.isLeaderNode()) {
            //if this event has come for active node due to member addition of passive node
            if (!haManager.getNodeId().equals(nodeDetail.getNodeId())) {
                SourceHandlerManager sourceHandlerManager = StreamProcessorDataHolder.getSourceHandlerManager();
                Map<String, SourceHandler> registeredSourceHandlers = sourceHandlerManager.
                        getRegsiteredSourceHandlers();

                Map propertiesMap = null;
                long waitTimeout = haManager.getDeploymentConfig().getPassiveNodeDetailsWaitTimeOutMillis();
                long initialWaitingStartedTimestamp = 0L;
                boolean waitTimeOutExceeded = false;
                do {
                    for (NodeDetail node : clusterCoordinator.getAllNodeDetails()) {
                        if (!node.getNodeId().equals(haManager.getNodeId())) {
                            propertiesMap = node.getPropertiesMap();
                            if (null == propertiesMap) {
                                try {
                                    if (initialWaitingStartedTimestamp == 0) {
                                        initialWaitingStartedTimestamp = System.currentTimeMillis();
                                    } else if (System.currentTimeMillis() - initialWaitingStartedTimestamp >
                                            waitTimeout) {
                                        log.error("Wait time out " + waitTimeout + " milliseconds exceeded. " +
                                                "Active node could not retrieve passive node details from database");
                                        waitTimeOutExceeded = true;
                                    } else {
                                        log.warn("Passive node properties Map is null. Waiting " +
                                                haManager.getDeploymentConfig().
                                                        getPassiveNodeDetailsRetrySleepTimeMillis()
                                                + " milliseconds till it available");
                                        Thread.sleep(haManager.getDeploymentConfig().
                                                getPassiveNodeDetailsRetrySleepTimeMillis());
                                    }
                                } catch (InterruptedException e) {
                                    log.error("Error occurred while waiting for passive node property map to " +
                                            "available. " + e.getMessage(), e);
                                }
                            } else {
                                log.info("Active node retrieved node details of passive node");
                            }
                            break;
                        }
                    }
                    if (waitTimeOutExceeded) {
                        break;
                    }
                } while (propertiesMap == null);

                if (null != propertiesMap) {
                    haManager.setPassiveNodeHostPort(getHost(propertiesMap),
                            getPort(propertiesMap));
                    haManager.initializeEventSyncConnectionPool();
                    for (SourceHandler sourceHandler : registeredSourceHandlers.values()) {
                        ((HACoordinationSourceHandler) sourceHandler).setPassiveNodeAdded(true);
                    }
                    haManager.setPassiveNodeAdded(true);
                    new PersistenceManager().run();
                }
            }
        } else {
            // if the event has come for passive node due to entering it self to cluster
            if (haManager.getNodeId().equals(nodeDetail.getNodeId())) {
                Map<String, Object> passiveNodeDetailsPropertiesMap = new HashMap<>();
                passiveNodeDetailsPropertiesMap.put(HAConstants.HOST, haManager.getDeploymentConfig()
                        .eventSyncServerConfigs().getHost());
                passiveNodeDetailsPropertiesMap.put(HAConstants.PORT, haManager.getDeploymentConfig()
                        .eventSyncServerConfigs().getPort());
                passiveNodeDetailsPropertiesMap.put(HAConstants.ADVERTISED_HOST, haManager.getDeploymentConfig()
                        .eventSyncServerConfigs()
                        .getAdvertisedHost());
                passiveNodeDetailsPropertiesMap.put(HAConstants.ADVERTISED_PORT, haManager.getDeploymentConfig()
                        .eventSyncServerConfigs().getAdvertisedPort());
                clusterCoordinator.setPropertiesMap(passiveNodeDetailsPropertiesMap);
            }
        }
    }

    @Override
    public void memberRemoved(NodeDetail nodeDetail) {
        log.info("memberRemoved event received for node id : " + nodeDetail.getNodeId());
        ClusterCoordinator clusterCoordinator = StreamProcessorDataHolder.getClusterCoordinator();
        HAManager haManager = StreamProcessorDataHolder.getHAManager();
        if (clusterCoordinator.isLeaderNode()) {
            if (!haManager.getNodeId().equals(nodeDetail.getNodeId())) {
                SourceHandlerManager sourceHandlerManager = StreamProcessorDataHolder.getSourceHandlerManager();
                Map<String, SourceHandler> registeredSourceHandlers = sourceHandlerManager.
                        getRegsiteredSourceHandlers();
                StreamProcessorDataHolder.getHAManager().setPassiveNodeAdded(false);
                for (SourceHandler sourceHandler : registeredSourceHandlers.values()) {
                    ((HACoordinationSourceHandler) sourceHandler).setPassiveNodeAdded(false);
                }
                try {
                    if (null != EventSyncConnectionPoolManager.getConnectionPool()) {
                        EventSyncConnectionPoolManager.getConnectionPool().close();
                    }
                } catch (Exception e) {
                    log.error("Error closing tcp client connection pool. " + e.getMessage(), e);
                } finally {
                    EventSyncConnectionPoolManager.uninitializeConnectionPool();
                }
            }
        }
    }

    @Override
    public void coordinatorChanged(NodeDetail nodeDetail) {
        log.info("coordinatorChanged event received for node id : " + nodeDetail.getNodeId());
        ClusterCoordinator clusterCoordinator = StreamProcessorDataHolder.getClusterCoordinator();
        if (clusterCoordinator != null) {
            //synchronizing this inorder to prevent quick changes of active passive states in same node if occurred
            synchronized (this) {
                if (clusterCoordinator.isLeaderNode()) {
                    StreamProcessorDataHolder.getHAManager().changeToActive();
                } else {
                    //Allow only to become passive if and only if node was active before - this could happen if both
                    // nodes become unresponsive and already passive node could become passive again
                    if (StreamProcessorDataHolder.getHAManager().isActiveNode()) {
                        StreamProcessorDataHolder.getHAManager().changeToPassive();
                    }
                }
            }
        }
    }

    @Override
    public void becameUnresponsive(String nodeId) {
        log.info("becameUnresponsive event received for node id : " + nodeId);
        if (StreamProcessorDataHolder.getHAManager().isActiveNode()) {
            StreamProcessorDataHolder.getHAManager().changeToPassive();
        }

    }

    private String getHost(Map nodePropertiesMap) {
        Object host = nodePropertiesMap.get(HAConstants.ADVERTISED_HOST);
        if (host == null) {
            host = nodePropertiesMap.get(HAConstants.HOST);
        }
        return (String) host;
    }

    private int getPort(Map nodePropertiesMap) {
        int port = 0;
        try {
            port = (int) nodePropertiesMap.get(HAConstants.ADVERTISED_PORT);
        } catch (Exception e) {
            log.warn("Error in getting the advertisedPort from deployment yaml. Hence using port as the " +
                    "advertisedPort" + e.getMessage());
        }
        if (port == 0) {
            port = (int) nodePropertiesMap.get(HAConstants.PORT);
        }
        return port;
    }
}
