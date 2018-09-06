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

package org.wso2.carbon.stream.processor.core.persistence;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.core.ha.HAManager;
import org.wso2.carbon.stream.processor.core.ha.transport.EventSyncConnectionPoolManager;
import org.wso2.carbon.stream.processor.core.ha.transport.EventSyncConnection;
import org.wso2.carbon.stream.processor.core.ha.util.HAConstants;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.snapshot.PersistenceReference;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class that manages the periodic persistence of Siddhi Applications
 */
public class PersistenceManager implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);
    private HAManager haManager;
    private DeploymentConfig deploymentConfig;
    private EventSyncConnection eventSyncConnection;
    private AtomicLong sequenceIDGenerator;

    public PersistenceManager() {
    }

    @Override
    public void run() {
        haManager = StreamProcessorDataHolder.getHAManager();
        if (haManager != null) {
            if (haManager.isActiveNode()) {
                eventSyncConnection = getTCPConnection();
                sequenceIDGenerator = EventSyncConnectionPoolManager.getSequenceID();
                persistAndSendControlMessage();
            } //Passive node will not persist the state
        } else {
            persist();
        }
    }

    private void persist() {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap = StreamProcessorDataHolder.
                getSiddhiManager().getSiddhiAppRuntimeMap();
        for (SiddhiAppRuntime siddhiAppRuntime : siddhiAppRuntimeMap.values()) {
            PersistenceReference persistenceReference = siddhiAppRuntime.persist();
            if (log.isDebugEnabled()) {
                log.debug("Revision " + persistenceReference.getRevision() +
                        " of siddhi App " + siddhiAppRuntime.getName() + " persisted successfully");
            }
        }
        if (StreamProcessorDataHolder.getNodeInfo() != null) {
            StreamProcessorDataHolder.getNodeInfo().setLastPersistedTimestamp(System.currentTimeMillis());
        }
    }

    private void persistAndSendControlMessage() {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap = StreamProcessorDataHolder.
                getSiddhiManager().getSiddhiAppRuntimeMap();
        String[] siddhiRevisionArray = new String[siddhiAppRuntimeMap.size()];
        int siddhiAppCount = 0;
        for (SiddhiAppRuntime siddhiAppRuntime : siddhiAppRuntimeMap.values()) {
            PersistenceReference persistenceReference = siddhiAppRuntime.persist();
            Future fullStateFuture = persistenceReference.getFullStateFuture();
            try {
                if (fullStateFuture != null) {
                    fullStateFuture.get(60000, TimeUnit.MILLISECONDS);
                } else {
                    for (Future future : persistenceReference.getIncrementalStateFuture()) {
                        future.get(60000, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (Throwable e) {
                log.error("Active Node: Persisting of Siddhi app is not successful. Check if app deployed properly");
            }
            siddhiRevisionArray[siddhiAppCount] = sequenceIDGenerator.incrementAndGet() + HAConstants
                    .PERSISTED_APP_SPLIT_DELIMITER + persistenceReference.getRevision();
            siddhiAppCount++;
            if (log.isDebugEnabled()) {
                log.debug("Revision " + persistenceReference.getRevision() +
                        " of siddhi App " + siddhiAppRuntime.getName() + " persisted successfully");
            }
        }
        if (haManager != null && haManager.isActiveNode()) {
            sendControlMessageToPassiveNode(siddhiRevisionArray);
        }
        if (StreamProcessorDataHolder.getNodeInfo() != null) {
            StreamProcessorDataHolder.getNodeInfo().setLastPersistedTimestamp(System.currentTimeMillis());
        }
        log.info("siddhi Apps are persisted successfully");
    }


    private EventSyncConnection getTCPConnection() {
        deploymentConfig = StreamProcessorDataHolder.getDeploymentConfig();
        EventSyncConnectionPoolManager.initializeConnectionPool(deploymentConfig);
        GenericKeyedObjectPool tcpConnectionPool = EventSyncConnectionPoolManager.getConnectionPool();
        EventSyncConnection eventSyncConnection = null;
        try {
            eventSyncConnection = (EventSyncConnection) tcpConnectionPool.borrowObject("ActiveNode");
            tcpConnectionPool.returnObject("ActiveNode", eventSyncConnection);
        } catch (Exception e) {
            log.error("Error in getting a connection to the Passive node. { host: '" + deploymentConfig
                    .getPassiveNodeHost() + "', port: '" + deploymentConfig.getPassiveNodePort() + "'");
        }
        return eventSyncConnection;
    }

    private void sendControlMessageToPassiveNode(String[] siddhiRevisionArray) {
        try {
            String siddhiAppRevisions = Arrays.toString(siddhiRevisionArray);
            if (eventSyncConnection != null) {
                eventSyncConnection.send(HAConstants.CHANNEL_ID_CONTROL_MESSAGE,
                        siddhiAppRevisions.getBytes(HAConstants.DEFAULT_CHARSET));
            } else {
                log.error("Error in getting the TCP connection to the passive node. Hence not sending the control " +
                        "message to the passive node");
            }
        } catch (ConnectionUnavailableException e) {
            log.error("Error in connecting to the Passive node. { host: '" + deploymentConfig.getPassiveNodeHost() +
                    "', " + "port: '" + deploymentConfig.getPassiveNodePort() + "'");
        } catch (UnsupportedEncodingException e) {
            log.error("Error when get bytes in encoding '" + HAConstants.DEFAULT_CHARSET + "' " + e.getMessage(), e);
        }
    }
}
