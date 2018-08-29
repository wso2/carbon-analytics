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

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.log4j.Logger;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.databridge.commons.ServerEventListener;
import org.wso2.carbon.stream.processor.core.DeploymentMode;
import org.wso2.carbon.stream.processor.core.NodeInfo;
import org.wso2.carbon.stream.processor.core.event.queue.EventListMapManager;
import org.wso2.carbon.stream.processor.core.ha.tcp.TCPServer;
import org.wso2.carbon.stream.processor.core.ha.transport.ClientPool;
import org.wso2.carbon.stream.processor.core.ha.transport.ClientPoolFactory;
import org.wso2.carbon.stream.processor.core.ha.util.RequestUtil;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.stream.processor.core.internal.beans.TCPClientPoolConfig;
import org.wso2.carbon.stream.processor.core.internal.beans.TCPServerConfig;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.CannotRestoreSiddhiAppStateException;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class that manages Active and Passive nodes in a 2 node minimum HA configuration
 */
public class HAManager {

    private ClusterCoordinator clusterCoordinator;
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
    private TCPServer tcpServerInstance = TCPServer.getInstance();
    private EventListMapManager eventListMapManager;
    private int passiveQueueCapacity;
    private DeploymentConfig deploymentConfig;
    private BlockingQueue<ByteBuffer> eventByteBufferQueue;
    private TCPServerConfig tcpServerConfig;
    private TCPClientPoolConfig tcpClientPoolConfig;

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
        this.eventListMapManager = new EventListMapManager();
        this.passiveQueueCapacity = deploymentConfig.getPassiveQueueCapacity();
        this.eventByteBufferQueue = new LinkedBlockingQueue<ByteBuffer>(deploymentConfig.
                getEventByteBufferQueueCapacity());
        this.deploymentConfig = deploymentConfig;
        this.tcpServerConfig = deploymentConfig.getTcpServer();
        this.tcpClientPoolConfig = deploymentConfig.getTcpClientPool();
    }

    public void start() {
        sourceHandlerManager = new HACoordinationSourceHandlerManager(sourceQueueCapacity, getTCPClientConnectionPool
                (tcpClientPoolConfig.getHost(), tcpClientPoolConfig.getPort()));
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
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        } else {
            log.info("HA Deployment: Starting up as Passive Node");
            //initialize passive queue
            EventListMapManager.initializeEventTreeMap();

            //start tcp server
            tcpServerInstance.start(deploymentConfig.getTcpServer(), eventByteBufferQueue);
        }

        NodeInfo nodeInfo = StreamProcessorDataHolder.getNodeInfo();
        nodeInfo.setMode(DeploymentMode.MINIMUM_HA);
        nodeInfo.setNodeId(nodeId);
        nodeInfo.setGroupId(clusterId);
        nodeInfo.setActiveNode(isActiveNode);
    }

    /**
     * Stops TCP server
     * Sync state
     * start siddhi app runtimes
     */
    void changeToActive() {
        if (!isActiveNode) {
            tcpServerInstance.stop();
            syncState();

            //Give time for byte buffer queue to be empty
            while (eventByteBufferQueue.peek() != null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.warn("Error in checking byte buffer queue empty");
                }
            }
            tcpServerInstance.shutdownOtherResources();
            //change the system clock to work with event time
            enableEventTimeClock(true);
            try {
                eventListMapManager.trimAndSendToInputHandler();
            } catch (InterruptedException e) {
                e.printStackTrace();//todo
            }
            isActiveNode = true;

            //change the system clock to work with current time
            enableEventTimeClock(false);
            startSiddhiAppRuntimes();
            //start the databridge servers
            List<ServerEventListener> listeners = StreamProcessorDataHolder.getServerListeners();
            for (ServerEventListener listener : listeners) {
                listener.start();
            }
            NodeInfo nodeInfo = StreamProcessorDataHolder.getNodeInfo();
            nodeInfo.setActiveNode(isActiveNode);
        }


//        activeNodePropertiesMap.put("host", localHost);
//        activeNodePropertiesMap.put("port", localPort);
//        clusterCoordinator.setPropertiesMap(activeNodePropertiesMap);
//
//        if (passiveNodeOutputScheduledFuture != null) {
//            passiveNodeOutputScheduledFuture.cancel(false);
//        }
//        if (passiveNodeOutputSchedulerService != null) {
//            passiveNodeOutputSchedulerService.shutdown();
//        }
//        if (syncAfterGracePeriodTimer != null) {
//            syncAfterGracePeriodTimer.cancel();
//            syncAfterGracePeriodTimer.purge();
//        }
//        if (retrySiddhiAppSyncTimerList.size() > 0) {
//            for (Timer timer : retrySiddhiAppSyncTimerList) {
//                timer.cancel();
//                timer.purge();
//            }
//        }
//        if (!liveSyncEnabled && !isActiveNodeOutputSyncManagerStarted) {
//            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
//            scheduledExecutorService.scheduleAtFixedRate(new ActiveNodeOutputSyncManager(
//                            sinkHandlerManager, recordTableHandlerManager, clusterCoordinator), 0, outputSyncInterval,
//                    TimeUnit.MILLISECONDS);
//            isActiveNodeOutputSyncManagerStarted = true;
//        }
//        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
//        scheduledExecutorService.scheduleAtFixedRate(new ActiveNodeEventDispatcher(eventQueue, "localhost", 9892),
//                0, 1000, TimeUnit.MILLISECONDS);
//        NodeInfo nodeInfo = StreamProcessorDataHolder.getNodeInfo();
//        nodeInfo.setActiveNode(isActiveNode);
    }

//    /**
//     * Implements a timer task to run after specified time interval to sync with active node
//     *
//     * @param gracePeriod time given for passive node to connect to all sources before re-syncing with active node
//     * @return reference to the timer. Can be used to cancel task if needed
//     */
//    private Timer liveSyncAfterGracePeriod(int gracePeriod) {
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                log.info("Passive Node: Borrowing state from active node after " + gracePeriod / 1000 + " seconds");
//                Map<String, SourceHandler> sourceHandlerMap = sourceHandlerManager.getRegsiteredSourceHandlers();
//                for (SourceHandler sourceHandler : sourceHandlerMap.values()) {
//                    if (log.isDebugEnabled()) {
//                        log.debug("Setting source handler with ID " + sourceHandler.getElementId() +
//                                " to collect events in buffer");
//                    }
//                    ((HACoordinationSourceHandler) sourceHandler).collectEvents(true);
//                }
//
//                boolean isPersisted = persistActiveNode(activeNodeHost, activeNodePort);
//                if (isPersisted) {
//                    syncState();
//                }
//            }
//        }, gracePeriod);
//        return timer;
//    }

    private void syncState() {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap
                = StreamProcessorDataHolder.getSiddhiManager().getSiddhiAppRuntimeMap();

        siddhiAppRuntimeMap.forEach((siddhiAppName, siddhiAppRuntime) -> {
            if (log.isDebugEnabled()) {
                log.debug("Restoring state of Siddhi Application " +
                        siddhiAppRuntime.getName());
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

    private void enableEventTimeClock(boolean enablePlayBack) {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap
                = StreamProcessorDataHolder.getSiddhiManager().getSiddhiAppRuntimeMap();

        siddhiAppRuntimeMap.forEach((siddhiAppName, siddhiAppRuntime) -> {
            if (log.isDebugEnabled()) {
                log.debug("Changing system clock for Siddhi Application " +
                        siddhiAppRuntime.getName());
            }
            siddhiAppRuntime.enablePlayBack(enablePlayBack, null, null);
        });
    }

    private void startSiddhiAppRuntimes() {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap
                = StreamProcessorDataHolder.getSiddhiManager().getSiddhiAppRuntimeMap();

        siddhiAppRuntimeMap.forEach((siddhiAppName, siddhiAppRuntime) -> {
            if (log.isDebugEnabled()) {
                log.debug("Starting Siddhi Application " +
                        siddhiAppRuntime.getName());
            }
            siddhiAppRuntime.start();
        });
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

    private GenericKeyedObjectPool getTCPClientConnectionPool(String host, int port) {
        ClientPool clientPool = new ClientPool();
        ClientPoolFactory clientPoolFactory = new ClientPoolFactory(host, port);
        return clientPool.getClientPool(clientPoolFactory, tcpClientPoolConfig.getMaxActive(), tcpClientPoolConfig
                .getMaxIdle(), tcpClientPoolConfig.isTestOnBorrow(), tcpClientPoolConfig
                .getTimeBetweenEvictionRunsMillis(), tcpClientPoolConfig.getMinEvictableIdleTimeMillis());
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
