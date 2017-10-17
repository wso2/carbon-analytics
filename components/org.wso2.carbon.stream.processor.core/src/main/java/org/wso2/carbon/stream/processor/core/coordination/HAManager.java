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

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.stream.processor.core.coordination.util.CompressionUtil;
import org.wso2.carbon.stream.processor.core.coordination.util.RequestUtil;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.model.HAStateSyncObject;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;
import org.wso2.siddhi.core.stream.input.source.SourceHandlerManager;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class that manages Active and Passive nodes in a 2 node minimum HA configuration
 */
public class HAManager {

    private ClusterCoordinator clusterCoordinator;
    private ScheduledExecutorService haPassiveNodeSchedulerService;
    private ScheduledFuture passiveNodeScheduledFuture;
    private boolean liveStateSync;
    private int publisherSyncInterval;
    private String localHost;
    private String localPort;
    private Timer timer;
    private int gracePeriod;
    private boolean isActiveNode;
    private String nodeId;
    private String clusterId;
    private int sinkQueueCapacity;
    private int sourceQueueCapacity;
    private String activeNodeHost;
    private String activeNodePort;

    private static final Logger log = Logger.getLogger(HAManager.class);

    public HAManager(ClusterCoordinator clusterCoordinator, boolean liveStateSync, int publisherSyncInterval,
                     String host, String port, int gracePeriod, String nodeId, String clusterId,
                     int sinkQueueCapacity, int sourceQueueCapacity) {
        this.clusterCoordinator = clusterCoordinator;
        this.liveStateSync = liveStateSync;
        this.publisherSyncInterval = publisherSyncInterval;
        this.localHost = host;
        this.localPort = port;
        this.gracePeriod = gracePeriod;
        this.nodeId = nodeId;
        this.clusterId = clusterId;
        this.sinkQueueCapacity = sinkQueueCapacity;
        this.sourceQueueCapacity = sourceQueueCapacity;
    }

    public void start() {

        HACoordinationSinkHandlerManager coordinationSinkHandlerManger = new HACoordinationSinkHandlerManager(
                sinkQueueCapacity);
        HACoordinationSourceHandlerManager sourceHandlerManager = new HACoordinationSourceHandlerManager(
                sourceQueueCapacity);

        StreamProcessorDataHolder.setSinkHandlerManager(coordinationSinkHandlerManger);
        StreamProcessorDataHolder.setSourceHandlerManager(sourceHandlerManager);
        SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
        if (siddhiManager != null) {
            siddhiManager.setSourceHandlerManager(StreamProcessorDataHolder.getSourceHandlerManager());
            siddhiManager.setSinkHandlerManager(StreamProcessorDataHolder.getSinkHandlerManager());
        }

        clusterCoordinator.registerEventListener(new HAEventListener());

        //Give time for the cluster to normalize
        while (clusterCoordinator.getLeaderNode() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.warn("Error in waiting for leader node");
            }
        }

        isActiveNode = clusterCoordinator.isLeaderNode();

        if (!isActiveNode) {
            log.info("Node starting up as Passive node.");
            Map<String, Object> activeNodeHostAndPortMap = clusterCoordinator.getLeaderNode().getPropertiesMap();

            if (activeNodeHostAndPortMap != null) {
                activeNodeHost = (String) activeNodeHostAndPortMap.get("host");
                activeNodePort = (String) activeNodeHostAndPortMap.get("port");
            } else {
                log.error("Leader Node Host and Port is Not Set!");
            }

            haPassiveNodeSchedulerService = Executors.newSingleThreadScheduledExecutor();
            passiveNodeScheduledFuture = haPassiveNodeSchedulerService.scheduleAtFixedRate(
                    new PassivePublisherSyncManager(activeNodeHost, activeNodePort), publisherSyncInterval,
                    publisherSyncInterval, TimeUnit.MILLISECONDS);

            if (liveStateSync) {
                timer = syncWithActiveNodeAfterGracePeriod(gracePeriod, sourceHandlerManager);
            }
        }
    }

    /**
     * Method to get Snapshot from Active Node for a specified Siddhi Application
     *
     * @param siddhiAppName the name of the Siddhi Application whose state is required from the Active Node
     * @return the snapshot of specified siddhi application. Return null of no snapshot found.
     */
    public byte[] getActiveNodeSnapshot(String siddhiAppName) {
        boolean isActive = clusterCoordinator.isLeaderNode();
        if (!isActive) {
            Map<String, Object> activeNodeHostAndPortMap = clusterCoordinator.getLeaderNode().getPropertiesMap();

            if (activeNodeHostAndPortMap != null) {
                activeNodeHost = (String) activeNodeHostAndPortMap.get("host");
                activeNodePort = (String) activeNodeHostAndPortMap.get("port");
            } else {
                log.error("Leader Node Host and Port is Not Set!");
            }
        } else {
            log.error("Illegal getActiveNodeSnapshot Called from Active Node");
            return null;
        }
        HAStateSyncObject haStateSyncObject = getActiveSnapshotAndLastProcessedTimestamp(activeNodeHost, activeNodePort,
                siddhiAppName);
        if (haStateSyncObject.hasState()) {
            return haStateSyncObject.getSnapshotMap().get(siddhiAppName);
        } else {
            return null;
        }
    }

    /**
     * Stops Publisher Syncing of Passive Node
     * Cancels scheduled state sync of Passive Node from Active Node
     * Updates Coordination with Advertised Host and Port
     */
    void changeToActive() {
        if (passiveNodeScheduledFuture != null) {
            passiveNodeScheduledFuture.cancel(false);
        }
        if (haPassiveNodeSchedulerService != null) {
            haPassiveNodeSchedulerService.shutdown();
        }
        if (timer != null) {
            timer.cancel();
        }
        Map<String, Object> hostAndPortMap = new HashMap<>();
        hostAndPortMap.put("host", localHost);
        hostAndPortMap.put("port", localPort);
        clusterCoordinator.setPropertiesMap(hostAndPortMap);
        isActiveNode = true;
    }

    /**
     * Method to send an http request to active node to get all snapshots and last processed events timestamp
     *
     * @param activeNodeHost advertised host of active node
     * @param activeNodePort advertised port of active node
     * @return First element includes the snapshot of each siddhi app against the siddhi app name
     * Second element includes the last processed timestamp against the source element id
     */
    private HAStateSyncObject getActiveSnapshotAndLastProcessedTimestamp(String activeNodeHost, String activeNodePort) {
        String url = "http://%s:%d/ha/state";
        URI baseURI = URI.create(String.format(url, activeNodeHost, Integer.parseInt(activeNodePort)));
        String httpResponseMessage = RequestUtil.sendRequest(baseURI);

        return new Gson().fromJson(httpResponseMessage, HAStateSyncObject.class);

    }

    /**
     * Method to send an http request to active node to get a siddhiapps snapshot and last processed events timestamp
     *
     * @param activeNodeHost advertised host of active node
     * @param activeNodePort advertised port of active node
     * @param siddhiAppName  name of siddhiapp whose snapshot is required
     * @return First element includes the snapshot of each siddhi app against the siddhi app name
     * Second element includes the last processed timestamp against the source element id
     */
    private HAStateSyncObject getActiveSnapshotAndLastProcessedTimestamp(String activeNodeHost, String activeNodePort,
                                                                         String siddhiAppName) {
        String url = "http://%s:%d/ha/state/" + siddhiAppName;
        URI baseURI = URI.create(String.format(url, activeNodeHost, Integer.parseInt(activeNodePort)));
        String httpResponseMessage = RequestUtil.sendRequest(baseURI);

        return new Gson().fromJson(httpResponseMessage, HAStateSyncObject.class);

    }

    /**
     * Implements a timer task to run after specified time interval to sync with active node
     *
     * @param gracePeriod time given for passive node to connect to all sources before re-syncing with active node
     * @param sourceHandlerManager implementation of source handler manager
     */
    private Timer syncWithActiveNodeAfterGracePeriod(int gracePeriod, SourceHandlerManager sourceHandlerManager) {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.info("Borrowing state from active node after " + gracePeriod + " minute(s)");
                Map<String, SourceHandler> sourceHandlerMap = sourceHandlerManager.getRegsiteredSourceHandlers();
                for (SourceHandler sourceHandler : sourceHandlerMap.values()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Setting source handler with ID " + ((HACoordinationSourceHandler)sourceHandler)
                                .getSourceElementId() + " to collect events in buffer");
                    }
                    ((HACoordinationSourceHandler) sourceHandler).collectEvents(true);
                }
                HAStateSyncObject haStateSyncObject = getActiveSnapshotAndLastProcessedTimestamp(activeNodeHost,
                        activeNodePort);

                Map<String, byte[]> snapshotMap = haStateSyncObject.getSnapshotMap();
                Map<String, byte[]> decompressedSnapshotMap = new HashMap<>();
                for (Map.Entry<String, byte[]> snapshotEntry: snapshotMap.entrySet()) {
                    try {
                        decompressedSnapshotMap.put(snapshotEntry.getKey(), CompressionUtil.decompressGZIP(
                                snapshotEntry.getValue()));
                    } catch (IOException e) {
                        log.error("Error Decompressing Bytes " + e.getMessage(), e);
                    }
                }

                ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap = StreamProcessorDataHolder.
                        getSiddhiManager().getSiddhiAppRuntimeMap();

                for (SiddhiAppRuntime siddhiAppRuntime : siddhiAppRuntimeMap.values()) {
                    byte[] snapshot = decompressedSnapshotMap.get(siddhiAppRuntime.getName());
                    if (snapshot != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Restoring state of siddhi application " + siddhiAppRuntime.getName() +
                                    " of passive node while syncing state after specified grace period");
                        }
                        siddhiAppRuntime.restore(snapshot);
                    } else {
                        log.warn("No Snapshot found for siddhi application " + siddhiAppRuntime.getName() +
                                " while trying sync state with active node after specified grace period");
                        // TODO: 10/16/17 Do anything?
                    }
                }

                Map<String, Long> lastProcessedTimestamp = haStateSyncObject.getSourceTimestamps();
                for (Map.Entry<String, Long> lastProcessedTimestampEntry: lastProcessedTimestamp.entrySet()) {
                    HACoordinationSourceHandler sourceHandler = (HACoordinationSourceHandler) sourceHandlerMap.
                            get(lastProcessedTimestampEntry.getKey());
                    if (log.isDebugEnabled()) {
                        log.debug("Notifying source handler with ID " + sourceHandler.getSourceElementId() + " to " +
                                "process events that were buffered during state sync.");
                    }
                    sourceHandler.processBufferedEvents(lastProcessedTimestampEntry.getValue());
                }
            }
        }, gracePeriod * 60 * 1000L);

        return timer;
    }

    public boolean isActiveNode() {
        return isActiveNode;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public boolean isLiveStateSyncEnabled() {
        return liveStateSync;
    }
}
