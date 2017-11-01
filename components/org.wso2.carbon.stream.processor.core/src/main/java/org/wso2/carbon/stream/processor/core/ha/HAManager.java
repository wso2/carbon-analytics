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
import org.apache.log4j.Logger;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.stream.processor.core.ha.util.CompressionUtil;
import org.wso2.carbon.stream.processor.core.ha.util.RequestUtil;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.model.HAStateSyncObject;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    private ScheduledExecutorService passiveNodeOutputSchedulerService;
    private ScheduledFuture passiveNodeOutputScheduledFuture;
    private boolean liveSyncEnabled;
    private int outputSyncInterval;
    private String localHost;
    private String localPort;
    private Timer syncAfterGracePeriodTimer;
    private int stateSyncGracePeriod;
    private boolean isActiveNode;
    private String nodeId;
    private String clusterId;
    private int sinkQueueCapacity;
    private int sourceQueueCapacity;
    private String activeNodeHost;
    private String activeNodePort;
    private HACoordinationSourceHandlerManager sourceHandlerManager;
    private List<Timer> retrySiddhiAppSyncTimerList;

    public static Map<String, Object> activeNodePropertiesMap = new HashMap<>();
    private static final Logger log = Logger.getLogger(HAManager.class);

    public HAManager(ClusterCoordinator clusterCoordinator, String nodeId, String clusterId,
                     DeploymentConfig deploymentConfig) {
        this.clusterCoordinator = clusterCoordinator;
        this.nodeId = nodeId;
        this.clusterId = clusterId;
        this.localHost = deploymentConfig.getLiveSync().getAdvertisedHost();
        this.localPort = String.valueOf(deploymentConfig.getLiveSync().getAdvertisedPort());
        this.liveSyncEnabled = deploymentConfig.getLiveSync().isEnabled();
        this.outputSyncInterval = deploymentConfig.getOutputSyncInterval();
        this.stateSyncGracePeriod = deploymentConfig.getStateSyncGracePeriod();
        this.sinkQueueCapacity = deploymentConfig.getSinkQueueCapacity();
        this.sourceQueueCapacity = deploymentConfig.getSourceQueueCapacity();
        this.retrySiddhiAppSyncTimerList = new LinkedList<>();
    }

    public void start() {
        sourceHandlerManager = new HACoordinationSourceHandlerManager(sourceQueueCapacity);
        HACoordinationSinkHandlerManager sinkHandlerManager = new HACoordinationSinkHandlerManager(sinkQueueCapacity);
        HACoordinationRecordTableHandlerManager recordTableHandlerManager =
                new HACoordinationRecordTableHandlerManager(sinkQueueCapacity);

        StreamProcessorDataHolder.setSinkHandlerManager(sinkHandlerManager);
        StreamProcessorDataHolder.setSourceHandlerManager(sourceHandlerManager);
        StreamProcessorDataHolder.setRecordTableHandlerManager(recordTableHandlerManager);
        SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();

        siddhiManager.setSourceHandlerManager(StreamProcessorDataHolder.getSourceHandlerManager());
        siddhiManager.setSinkHandlerManager(StreamProcessorDataHolder.getSinkHandlerManager());
        siddhiManager.setRecordTableHandlerManager(StreamProcessorDataHolder.getRecordTableHandlerManager());

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

        if (isActiveNode) {
            log.info("HA Deployment: Starting up as Active Node");
            clusterCoordinator.setPropertiesMap(activeNodePropertiesMap);
            isActiveNode = true;
            if (!liveSyncEnabled) {
                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.scheduleAtFixedRate(new ActiveNodeOutputSyncManager(
                                sinkHandlerManager, recordTableHandlerManager, clusterCoordinator), 0, outputSyncInterval,
                        TimeUnit.MILLISECONDS);
            }
        } else {
            log.info("HA Deployment: Starting up as Passive Node");
            Map<String, Object> activeNodeHostAndPortMap = clusterCoordinator.getLeaderNode().getPropertiesMap();

            //Not checking for null of Map since LeaderNode check is done. Leader node will have properties
            activeNodeHost = (String) activeNodeHostAndPortMap.get("host");
            activeNodePort = (String) activeNodeHostAndPortMap.get("port");

            if (liveSyncEnabled) {
                log.info("Passive Node: Live Sync enabled. State sync from Active node scheduled after "
                        + stateSyncGracePeriod / 1000 + " seconds");
                syncAfterGracePeriodTimer = liveSyncAfterGracePeriod(stateSyncGracePeriod);
            } else {
                log.info("Passive Node: Live Sync disabled. State sync from Active node scheduled after "
                        + stateSyncGracePeriod / 1000 + " seconds");
                syncAfterGracePeriodTimer = persistenceStoreSyncAfterGracePeriod(stateSyncGracePeriod);
            }

            passiveNodeOutputSchedulerService = Executors.newSingleThreadScheduledExecutor();
            passiveNodeOutputScheduledFuture = passiveNodeOutputSchedulerService.scheduleAtFixedRate(
                    new PassiveNodeOutputSyncManager(clusterCoordinator, sinkHandlerManager, recordTableHandlerManager,
                            activeNodeHost, activeNodePort, liveSyncEnabled), outputSyncInterval, outputSyncInterval,
                    TimeUnit.MILLISECONDS);
        }
        HAInfo haInfo = new HAInfo(nodeId, clusterId, isActiveNode);
        StreamProcessorDataHolder.setHaInfo(haInfo);
        StreamProcessorDataHolder.getInstance().getBundleContext().registerService(HAInfo.class.getName(), haInfo,
                null);
    }

    /**
     * Stops Publisher Syncing of Passive Node
     * Cancels scheduled state sync of Passive Node from Active Node
     * Updates Coordination Properties with Advertised Host and Port
     */
    void changeToActive() {
        activeNodePropertiesMap.put("host", localHost);
        activeNodePropertiesMap.put("port", localPort);
        clusterCoordinator.setPropertiesMap(activeNodePropertiesMap);

        if (passiveNodeOutputScheduledFuture != null) {
            passiveNodeOutputScheduledFuture.cancel(false);
        }
        if (passiveNodeOutputSchedulerService != null) {
            passiveNodeOutputSchedulerService.shutdown();
        }
        if (syncAfterGracePeriodTimer != null) {
            syncAfterGracePeriodTimer.cancel();
            syncAfterGracePeriodTimer.purge();
        }
        if (retrySiddhiAppSyncTimerList.size() > 0) {
            for (Timer timer : retrySiddhiAppSyncTimerList) {
                timer.cancel();
                timer.purge();
            }
        }
    }

    /**
     * Implements a timer task to run after specified time interval to sync with active node
     *
     * @param gracePeriod time given for passive node to connect to all sources before re-syncing with active node
     * @return reference to the timer. Can be used to cancel task if needed
     */
    private Timer liveSyncAfterGracePeriod(int gracePeriod) {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.info("Passive Node: Borrowing state from active node after " + gracePeriod / 1000 + " seconds");
                Map<String, SourceHandler> sourceHandlerMap = sourceHandlerManager.getRegsiteredSourceHandlers();
                for (SourceHandler sourceHandler : sourceHandlerMap.values()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Setting source handler with ID " + sourceHandler.getElementId() +
                                " to collect events in buffer");
                    }
                    ((HACoordinationSourceHandler) sourceHandler).collectEvents(true);
                }
                HAStateSyncObject haStateSyncObject = getActiveNodeSnapshot(activeNodeHost, activeNodePort);
                if (haStateSyncObject.hasState()) {
                    Map<String, byte[]> snapshotMap = haStateSyncObject.getSnapshotMap();
                    Map<String, byte[]> decompressedSnapshotMap;
                    decompressedSnapshotMap = new HashMap<>();
                    for (Map.Entry<String, byte[]> snapshotEntry : snapshotMap.entrySet()) {
                        try {
                            decompressedSnapshotMap.put(snapshotEntry.getKey(), CompressionUtil.decompressGZIP(
                                    snapshotEntry.getValue()));
                        } catch (IOException e) {
                            log.error("Passive Node: Error decompressing bytes of active nodes state. "
                                    + e.getMessage(), e);
                        }
                    }

                    ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap = StreamProcessorDataHolder.
                            getSiddhiManager().getSiddhiAppRuntimeMap();

                    for (SiddhiAppRuntime siddhiAppRuntime : siddhiAppRuntimeMap.values()) {
                        byte[] snapshot = decompressedSnapshotMap.get(siddhiAppRuntime.getName());
                        if (snapshot != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Passive Node: Restoring state of Siddhi Application " + siddhiAppRuntime.
                                        getName() + " of passive node while live syncing after specified" +
                                        " grace period");
                            }
                            siddhiAppRuntime.restore(snapshot);
                        } else {
                            log.warn("Passive Node: No Snapshot found for Siddhi Application " + siddhiAppRuntime.
                                    getName() + " while trying live sync with active node after specified " +
                                    "grace period");
                        }
                    }
                }
            }
        }, gracePeriod);

        return timer;
    }

    /**
     * Implements a timer task to run after specified time interval to get active nodes last persisted state
     *
     * @param gracePeriod time given for passive node to connect to all sources before re-syncing with active node
     * @return reference to the timer. Can be used to cancel task if needed
     */
    private Timer persistenceStoreSyncAfterGracePeriod(int gracePeriod) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                StreamProcessorDataHolder.getSiddhiManager().restoreLastState();
            }
        }, gracePeriod);

        return timer;
    }

    /**
     * Method to send an http request to active node to get all snapshots and last processed events timestamp
     *
     * @param activeNodeHost advertised host of active node
     * @param activeNodePort advertised port of active node
     * @return {@link HAStateSyncObject} containing a map of snapshots
     */
    private HAStateSyncObject getActiveNodeSnapshot(String activeNodeHost, String activeNodePort) {
        String url = "http://%s:%d/ha/state";
        URI baseURI = URI.create(String.format(url, activeNodeHost, Integer.parseInt(activeNodePort)));
        String httpResponseMessage = RequestUtil.sendRequest(baseURI);
        if (httpResponseMessage != null) {
            return new Gson().fromJson(httpResponseMessage, HAStateSyncObject.class);
        } else {
            return new HAStateSyncObject(false);
        }
    }

    /**
     * Method to get Snapshot from Active Node for a specified Siddhi Application
     *
     * @param siddhiAppName the name of the Siddhi Application whose state is required from the Active Node
     * @return the snapshot of specified Siddhi Application. Return null of no snapshot found.
     */
    public HAStateSyncObject getActiveNodeSiddhiAppSnapshot(String siddhiAppName) {
        boolean isActive = clusterCoordinator.isLeaderNode();
        if (!isActive) {
            Map<String, Object> activeNodeHostAndPortMap = clusterCoordinator.getLeaderNode().getPropertiesMap();

            if (activeNodeHostAndPortMap != null) {
                activeNodeHost = (String) activeNodeHostAndPortMap.get("host");
                activeNodePort = (String) activeNodeHostAndPortMap.get("port");

                String url = "http://%s:%d/ha/state/" + siddhiAppName;
                URI baseURI = URI.create(String.format(url, activeNodeHost, Integer.parseInt(activeNodePort)));
                String httpResponseMessage = RequestUtil.sendRequest(baseURI);

                HAStateSyncObject haStateSyncObject = new Gson().fromJson(httpResponseMessage, HAStateSyncObject.class);
                if (haStateSyncObject != null) {
                    return haStateSyncObject;
                } else {
                    return new HAStateSyncObject(false);
                }
            } else {
                log.error("Leader Node Host and Port is Not Set!");
                return new HAStateSyncObject(false);
            }
        } else {
            log.error("Illegal getActiveNodeSiddhiAppSnapshot Called from Active Node");
            return new HAStateSyncObject(false);
        }
    }

    public boolean isActiveNode() {
        return isActiveNode;
    }

    public boolean isLiveStateSyncEnabled() {
        return liveSyncEnabled;
    }

    public void addRetrySiddhiAppSyncTimer(Timer retrySiddhiAppSyncTimer) {
        this.retrySiddhiAppSyncTimerList.add(retrySiddhiAppSyncTimer);
    }
}
