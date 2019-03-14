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

import org.apache.log4j.Logger;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.databridge.commons.ServerEventListener;
import org.wso2.carbon.stream.processor.core.DeploymentMode;
import org.wso2.carbon.stream.processor.common.HAStateChangeListener;
import org.wso2.carbon.stream.processor.core.NodeInfo;
import org.wso2.carbon.stream.processor.core.event.queue.EventListMapManager;
import org.wso2.carbon.stream.processor.core.ha.tcp.TCPServer;
import org.wso2.carbon.stream.processor.core.ha.transport.EventSyncConnectionPoolManager;
import org.wso2.carbon.stream.processor.core.ha.util.HAConstants;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.stream.processor.core.internal.beans.EventSyncClientPoolConfig;
import org.wso2.carbon.stream.processor.core.persistence.PersistenceManager;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.CannotRestoreSiddhiAppStateException;
import org.wso2.siddhi.core.stream.input.source.SourceHandler;
import org.wso2.siddhi.core.stream.output.sink.SinkHandler;
import org.wso2.siddhi.core.table.record.RecordTableHandler;
import org.wso2.siddhi.core.util.transport.BackoffRetryCounter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class that manages Active and Passive nodes in a 2 node minimum HA configuration
 */
public class HAManager {

    private ClusterCoordinator clusterCoordinator;
    private boolean isActiveNode;
    private String nodeId;
    private String clusterId;
    private HACoordinationSourceHandlerManager sourceHandlerManager;
    private HACoordinationSinkHandlerManager sinkHandlerManager;
    private HACoordinationRecordTableHandlerManager recordTableHandlerManager;
    private TCPServer tcpServerInstance = TCPServer.getInstance();
    private EventListMapManager eventListMapManager;
    private DeploymentConfig deploymentConfig;
    private EventSyncClientPoolConfig eventSyncClientPoolConfig;
    private BackoffRetryCounter backoffRetryCounter = new BackoffRetryCounter();
    private boolean passiveNodeAdded;
    private String host;
    private int port;

    private final static Map<String, Object> passiveNodeDetailsPropertiesMap = new HashMap<>();
    private static final Logger log = Logger.getLogger(HAManager.class);

    public HAManager(ClusterCoordinator clusterCoordinator, String nodeId, String clusterId,
                     DeploymentConfig deploymentConfig) {
        this.clusterCoordinator = clusterCoordinator;
        this.nodeId = nodeId;
        this.clusterId = clusterId;
        this.eventListMapManager = new EventListMapManager();
        this.deploymentConfig = deploymentConfig;
        this.eventSyncClientPoolConfig = deploymentConfig.getTcpClientPoolConfig();
    }

    public void start() {
        sourceHandlerManager = new HACoordinationSourceHandlerManager();
        sinkHandlerManager = new HACoordinationSinkHandlerManager();
        recordTableHandlerManager = new HACoordinationRecordTableHandlerManager();

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
            //notify the HAStateChangeListener as becameActive
            List<HAStateChangeListener> haStateChangeListeners = StreamProcessorDataHolder.
                    getHaStateChangeListenerList();
            for (HAStateChangeListener listener : haStateChangeListeners) {
                listener.becameActive();
            }
            //if active node restarted before the heart beat become old then node will be still active and then,
            // we need to initialize connections to passive node because we are not getting a passive member added
            // event. This will happen if and only if passive node already exist
            Map passiveNodePropertyMap = null;
            for (NodeDetail node : clusterCoordinator.getAllNodeDetails()) {
                if (!node.getNodeId().equals(nodeId)) {
                    passiveNodePropertyMap = node.getPropertiesMap();
                }
            }

            if (null != passiveNodePropertyMap) {
                setPassiveNodeAdded(true);
                for (SourceHandler sourceHandler : sourceHandlerManager.getRegsiteredSourceHandlers().values()) {
                    ((HACoordinationSourceHandler) sourceHandler).setPassiveNodeAdded(true);
                }
                setPassiveNodeHostPort(getHost(passiveNodePropertyMap),
                        getPort(passiveNodePropertyMap));
                initializeEventSyncConnectionPool();
                new PersistenceManager().run();
            }
        } else {
            log.info("HA Deployment: Starting up as Passive Node");
            //initialize passive queue
            passiveNodeDetailsPropertiesMap.put(HAConstants.HOST, deploymentConfig.eventSyncServerConfigs().getHost());
            passiveNodeDetailsPropertiesMap.put(HAConstants.PORT, deploymentConfig.eventSyncServerConfigs().getPort());
            passiveNodeDetailsPropertiesMap.put(HAConstants.ADVERTISED_HOST, deploymentConfig.eventSyncServerConfigs()
                    .getAdvertisedHost());
            passiveNodeDetailsPropertiesMap.put(HAConstants.ADVERTISED_PORT, deploymentConfig.eventSyncServerConfigs()
                    .getAdvertisedPort());
            clusterCoordinator.setPropertiesMap(passiveNodeDetailsPropertiesMap);
            EventListMapManager.initializeEventListMap();

            //start tcp server
            tcpServerInstance.start(deploymentConfig);

            //notify the HAStateChangeListener as becamePassive
            List<HAStateChangeListener> listeners = StreamProcessorDataHolder.getHaStateChangeListenerList();
            for (HAStateChangeListener listener : listeners) {
                listener.becamePassive();
            }
        }

        NodeInfo nodeInfo = StreamProcessorDataHolder.getNodeInfo();
        nodeInfo.setMode(DeploymentMode.MINIMUM_HA);
        nodeInfo.setNodeId(nodeId);
        nodeInfo.setGroupId(clusterId);
        nodeInfo.setActiveNode(isActiveNode);
    }

    /**
     * Stops TCP server and other resources
     * Sync state from persisted information
     * Playback events
     * Start siddhi app runtimes
     * Start databridge servers
     */
    void changeToActive() {
        if (!isActiveNode) {
            log.info("HA Deployment: This Node is now becoming the Active Node");
            isActiveNode = true;
            changeSiddhiAppState(true);
            NodeInfo nodeInfo = StreamProcessorDataHolder.getNodeInfo();
            nodeInfo.setActiveNode(isActiveNode);
            tcpServerInstance.stop();
            syncState();

            //Give time for byte buffer queue to be empty
            if (null != tcpServerInstance.getEventSyncServer().getEventByteBufferQueue()) {
                while (tcpServerInstance.getEventSyncServer().getEventByteBufferQueue().peek() != null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        log.warn("Error in checking byte buffer queue empty");
                    }
                }
            }

            tcpServerInstance.clearResources();
            //change the system clock to work with event time
            enableEventTimeClock(true);
            startSiddhiAppRuntimeWithoutSources();
            try {
                eventListMapManager.trimAndSendToInputHandler();
            } catch (InterruptedException e) {
                log.warn("Error in sending events to input handler." + e.getMessage());
            }

            //change the system clock to work with current time
            enableEventTimeClock(false);

            //here before starting the sources need to enable sinks and record table handlers
            // so that new events will process accordingly
            for (SinkHandler sinkHandler : sinkHandlerManager.getRegisteredSinkHandlers().values()) {
                try {
                    ((HACoordinationSinkHandler) sinkHandler).setAsActive();
                } catch (Throwable t) {
                    log.error("HA Deployment: Error when connecting to sink " + sinkHandler.getElementId() +
                            " while changing from passive state to active, skipping the sink. ", t);
                    continue;
                }
            }

            for (RecordTableHandler recordTableHandler : recordTableHandlerManager.getRegisteredRecordTableHandlers().
                    values()) {
                try {
                    ((HACoordinationRecordTableHandler) recordTableHandler).setAsActive();
                } catch (Throwable e) {
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

            startSiddhiAppRuntimeSources();

            //start the databridge servers
            List<ServerEventListener> listeners = StreamProcessorDataHolder.getServerListeners();
            for (ServerEventListener listener : listeners) {
                listener.start();
            }

            //notify the HAStateChangeListener as becameActive
            List<HAStateChangeListener> haStateChangeListeners = StreamProcessorDataHolder.
                    getHaStateChangeListenerList();
            for (HAStateChangeListener listener : haStateChangeListeners) {
                listener.becameActive();
            }
            log.info("Successfully Changed to Active Mode ");
        }
    }

    /**
     * Start TCP server
     * Initialize eventEventListMap
     */
    void changeToPassive() {
        log.info("HA Deployment: This Node is now becoming the Passive Node");
        //stop the databridge servers
        List<ServerEventListener> listeners = StreamProcessorDataHolder.getServerListeners();
        for (ServerEventListener listener : listeners) {
            listener.stop();
        }
        stopSiddhiAppRuntimes();
        isActiveNode = false;
        changeSiddhiAppState(false);

        //initialize event list map
        EventListMapManager.initializeEventListMap();

        NodeInfo nodeInfo = StreamProcessorDataHolder.getNodeInfo();
        nodeInfo.setActiveNode(isActiveNode);
        //start tcp server
        tcpServerInstance.start(deploymentConfig);

        //notify the HAStateChangeListener as becamePassive
        List<HAStateChangeListener> haStateChangeListeners = StreamProcessorDataHolder.
                getHaStateChangeListenerList();
        for (HAStateChangeListener listener : haStateChangeListeners) {
            listener.becamePassive();
        }
        log.info("Successfully Changed to Passive Mode ");
    }

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
        if (log.isDebugEnabled()) {
            log.debug("Successfully Synced the state ");
        }
    }

    private void enableEventTimeClock(boolean enablePlayBack) {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap
                = StreamProcessorDataHolder.getSiddhiManager().getSiddhiAppRuntimeMap();

        siddhiAppRuntimeMap.forEach((siddhiAppName, siddhiAppRuntime) -> {
            if (log.isDebugEnabled()) {
                log.debug("Changed Event Play back mode '" + enablePlayBack + "' for Siddhi Application " +
                        siddhiAppRuntime.getName());
            }
            siddhiAppRuntime.enablePlayBack(enablePlayBack, null, null);
        });
    }

    private void startSiddhiAppRuntimeWithoutSources() {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap
                = StreamProcessorDataHolder.getSiddhiManager().getSiddhiAppRuntimeMap();

        siddhiAppRuntimeMap.forEach((siddhiAppName, siddhiAppRuntime) -> {
            if (log.isDebugEnabled()) {
                log.debug("Starting without sources of Siddhi Application " + siddhiAppRuntime.getName());
            }
            siddhiAppRuntime.startWithoutSources();
        });
    }

    private void startSiddhiAppRuntimeSources() {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap
                = StreamProcessorDataHolder.getSiddhiManager().getSiddhiAppRuntimeMap();

        siddhiAppRuntimeMap.forEach((siddhiAppName, siddhiAppRuntime) -> {
            if (log.isDebugEnabled()) {
                log.debug("Starting sources of Siddhi Application " + siddhiAppRuntime.getName());
            }
            siddhiAppRuntime.startSources();
        });
    }

    private void stopSiddhiAppRuntimes() {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap
                = StreamProcessorDataHolder.getSiddhiManager().getSiddhiAppRuntimeMap();

        siddhiAppRuntimeMap.forEach((siddhiAppName, siddhiAppRuntime) -> {
            if (log.isDebugEnabled()) {
                log.debug("Stopping Siddhi Application " +
                        siddhiAppRuntime.getName());
            }
            siddhiAppRuntime.shutdown();
        });
    }

    private void changeSiddhiAppState(boolean state) {
        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().
                getSiddhiAppMap();

        siddhiAppMap.forEach((siddhiAppName, siddhiAppData) -> {
            if (log.isDebugEnabled()) {
                log.debug("Changed Siddhi Application " + siddhiAppName + " state to " +
                        state);
            }
            siddhiAppData.setActive(state);
        });
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

    public void initializeEventSyncConnectionPool() {
        EventSyncConnectionPoolManager.initializeConnectionPool(host, port, deploymentConfig);
    }

    public void setPassiveNodeHostPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean isActiveNode() {
        return isActiveNode;
    }

    public boolean isPassiveNodeAdded() {
        return passiveNodeAdded;
    }

    public void setPassiveNodeAdded(boolean passiveNodeAdded) {
        this.passiveNodeAdded = passiveNodeAdded;
    }

    public String getNodeId () {
        return nodeId;
    }

    public DeploymentConfig getDeploymentConfig() {
        return deploymentConfig;
    }
}
