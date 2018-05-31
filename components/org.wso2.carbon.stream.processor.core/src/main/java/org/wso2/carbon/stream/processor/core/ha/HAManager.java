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

import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.stream.processor.core.DeploymentMode;
import org.wso2.carbon.stream.processor.core.NodeInfo;
import org.wso2.carbon.stream.processor.core.ha.util.RequestUtil;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.CannotRestoreSiddhiAppStateException;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;

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
    private String username;
    private String password;
    private String activeNodeHost;
    private String activeNodePort;
    private HACoordinationSourceHandlerManager sourceHandlerManager;
    private HACoordinationSinkHandlerManager sinkHandlerManager;
    private HACoordinationRecordTableHandlerManager recordTableHandlerManager;
    private List<Timer> retrySiddhiAppSyncTimerList;
    private boolean isActiveNodeOutputSyncManagerStarted;

    private final static Map<String, Object> activeNodePropertiesMap = new HashMap<>();
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
        this.username = deploymentConfig.getLiveSync().getUsername();
        this.password = deploymentConfig.getLiveSync().getPassword();
        this.retrySiddhiAppSyncTimerList = new LinkedList<>();
    }

    public void start() {
        sourceHandlerManager = new HACoordinationSourceHandlerManager(sourceQueueCapacity);
        sinkHandlerManager = new HACoordinationSinkHandlerManager(sinkQueueCapacity);
        recordTableHandlerManager = new HACoordinationRecordTableHandlerManager(sinkQueueCapacity);

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
            activeNodePropertiesMap.put("host", localHost);
            activeNodePropertiesMap.put("port", localPort);
            clusterCoordinator.setPropertiesMap(activeNodePropertiesMap);
            isActiveNode = true;
            if (!liveSyncEnabled) {
                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.scheduleAtFixedRate(new ActiveNodeOutputSyncManager(
                                sinkHandlerManager, recordTableHandlerManager, clusterCoordinator), 0, outputSyncInterval,
                        TimeUnit.MILLISECONDS);
                isActiveNodeOutputSyncManagerStarted = true;
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
                            activeNodeHost, activeNodePort, liveSyncEnabled, username, password), outputSyncInterval,
                    outputSyncInterval, TimeUnit.MILLISECONDS);
        }

        NodeInfo nodeInfo = StreamProcessorDataHolder.getNodeInfo();
        nodeInfo.setMode(DeploymentMode.MINIMUM_HA);
        nodeInfo.setNodeId(nodeId);
        nodeInfo.setGroupId(clusterId);
        nodeInfo.setActiveNode(isActiveNode);
    }

    /**
     * Stops Publisher Syncing of Passive Node
     * Cancels scheduled state sync of Passive Node from Active Node
     * Updates Coordination Properties with Advertised Host and Port
     */
    void changeToActive() {
        isActiveNode = true;
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
        if (!liveSyncEnabled && !isActiveNodeOutputSyncManagerStarted) {
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(new ActiveNodeOutputSyncManager(
                            sinkHandlerManager, recordTableHandlerManager, clusterCoordinator), 0, outputSyncInterval,
                    TimeUnit.MILLISECONDS);
            isActiveNodeOutputSyncManagerStarted = true;
        }
        NodeInfo nodeInfo = StreamProcessorDataHolder.getNodeInfo();
        nodeInfo.setActiveNode(isActiveNode);
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

                boolean isPersisted = persistActiveNode(activeNodeHost, activeNodePort);
                if (isPersisted) {
                    ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap
                            = StreamProcessorDataHolder.getSiddhiManager().getSiddhiAppRuntimeMap();

                    siddhiAppRuntimeMap.forEach((siddhiAppName, siddhiAppRuntime) -> {
                        if (log.isDebugEnabled()) {
                            log.debug("Passive Node: Restoring state of Siddhi Application " +
                                    siddhiAppRuntime.getName() + " of passive node using live sync after" +
                                    " grace period of " + gracePeriod + " milliseconds");
                        }
                        try {
                            siddhiAppRuntime.restoreLastRevision();
                            StreamProcessorDataHolder.getNodeInfo().setLastSyncedTimestamp(System.currentTimeMillis());
                            StreamProcessorDataHolder.getNodeInfo().setInSync(true);
                        } catch (CannotRestoreSiddhiAppStateException e) {
                            log.error("Error in restoring Siddhi Application: " + siddhiAppRuntime.getName(), e);
                        }
                    });
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
                StreamProcessorDataHolder.getNodeInfo().setLastSyncedTimestamp(System.currentTimeMillis());
            }
        }, gracePeriod);
        return timer;
    }

    /**
     * Method to send an http request to active node to get all snapshots and last processed events timestamp
     *
     * @param activeNodeHost advertised host of active node
     * @param activeNodePort advertised port of active node
     * @return true if active node persisted state successfully
     */
    private boolean persistActiveNode(String activeNodeHost, String activeNodePort) {
        String url = "http://%s:%d/ha/state";
        URI baseURI = URI.create(String.format(url, activeNodeHost, Integer.parseInt(activeNodePort)));
        return RequestUtil.requestAndGetStatusCode(baseURI, username, password) == 200;
    }

    /**
     * Method to get Snapshot from Active Node for a specified Siddhi Application
     *
     * @param siddhiAppName the name of the Siddhi Application whose state is required from the Active Node
     * @return true if active node persisted state successfully
     */
    public boolean persistActiveNode(String siddhiAppName) {
        boolean isActive = clusterCoordinator.isLeaderNode();
        if (!isActive) {
            Map<String, Object> activeNodeHostAndPortMap = clusterCoordinator.getLeaderNode().getPropertiesMap();

            if (activeNodeHostAndPortMap != null) {
                activeNodeHost = (String) activeNodeHostAndPortMap.get("host");
                activeNodePort = (String) activeNodeHostAndPortMap.get("port");

                String url = "http://%s:%d/ha/state/" + siddhiAppName;
                URI baseURI = URI.create(String.format(url, activeNodeHost, Integer.parseInt(activeNodePort)));
                return RequestUtil.requestAndGetStatusCode(baseURI, username, password) == 200;
            } else {
                log.error("Leader Node Host and Port is Not Set!");
                return false;
            }
        } else {
            log.error("Illegal persistActiveNode Called from Active Node");
            return false;
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

    public static Map<String, Object> getActiveNodePropertiesMap() {
        return activeNodePropertiesMap;
    }
}
