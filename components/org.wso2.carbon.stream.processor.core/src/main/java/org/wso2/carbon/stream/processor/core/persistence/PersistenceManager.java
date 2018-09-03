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
import org.wso2.carbon.stream.processor.core.ha.transport.TCPConnectionPoolManager;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPConnectionPoolFactory;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPConnection;
import org.wso2.carbon.stream.processor.core.ha.util.HAConstants;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.stream.processor.core.internal.beans.TCPClientPoolConfig;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.snapshot.PersistenceReference;

import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class that manages the periodic persistence of Siddhi Applications
 */
public class PersistenceManager implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);
    private HAManager haManager;
    private DeploymentConfig deploymentConfig;
    private TCPConnection tcpConnection;
    private AtomicLong sequenceIDGenerator;

    public PersistenceManager() {
    }

    @Override
    public void run() {
        haManager = StreamProcessorDataHolder.getHAManager();
        if (haManager != null) {
            if (haManager.isActiveNode()) {
                tcpConnection = getTCPConnection();
                sequenceIDGenerator = TCPConnectionPoolManager.getSequenceID();
                persistWithControlMessage();
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
        log.info("siddhi Apps are persisted successfully");
    }


    private void persistWithControlMessage() {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap = StreamProcessorDataHolder.
                getSiddhiManager().getSiddhiAppRuntimeMap();
        String[] siddhiRevisionArray = new String[siddhiAppRuntimeMap.size()];
        int siddhiAppCount = 0;
        for (SiddhiAppRuntime siddhiAppRuntime : siddhiAppRuntimeMap.values()) {
            PersistenceReference persistenceReference = siddhiAppRuntime.persist();
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


    private TCPConnection getTCPConnection() {
        deploymentConfig = StreamProcessorDataHolder.getDeploymentConfig();
        TCPConnectionPoolManager.initializeConnectionPool(deploymentConfig);
        GenericKeyedObjectPool tcpConnectionPool = TCPConnectionPoolManager.getConnectionPool();
        TCPConnection tcpConnection = null;
        try {
            tcpConnection = (TCPConnection) tcpConnectionPool.borrowObject("ActiveNode");
            tcpConnectionPool.returnObject("ActiveNode", tcpConnection);
        } catch (Exception e) {
            log.error("Error in getting a connection to the Passive node. { host: '" + deploymentConfig
                    .getPassiveNodeHost() + "', port: '" + deploymentConfig.getPassiveNodePort() + "'");
        }
        return tcpConnection;
    }

    private void sendControlMessageToPassiveNode(String[] siddhiRevisionArray) {
        try {
            String siddhiAppRevisions = Arrays.toString(siddhiRevisionArray);
            if (tcpConnection != null) {
                tcpConnection.send(HAConstants.CHANNEL_ID_CONTROL_MESSAGE, siddhiAppRevisions.getBytes());
            } else {
                log.error("Error in getting the TCP connection to the passive node. Hence not sending the control " +
                        "message to the passive node");
            }
        } catch (ConnectionUnavailableException e) {
            log.error("Error in connecting to the Passive node. { host: '" + deploymentConfig.getPassiveNodeHost() + "', " +
                    "port: '" + deploymentConfig.getPassiveNodePort() + "'");
        }
    }
}
