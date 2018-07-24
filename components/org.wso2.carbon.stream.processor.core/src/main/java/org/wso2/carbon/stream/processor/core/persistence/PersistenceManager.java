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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.core.ha.HAManager;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPNettyClient;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.snapshot.PersistenceReference;

import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

/**
 * Class that manages the periodic persistence of Siddhi Applications
 */
public class PersistenceManager implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);
    TCPNettyClient tcpNettyClient = new TCPNettyClient();

    public PersistenceManager() {
    }

    @Override
    public void run() {
        HAManager haManager = StreamProcessorDataHolder.getHAManager();
        if (haManager != null) {
            if (haManager.isActiveNode()) {
                persist();
            } //Passive node will not persist the state
        } else {
            persist();
        }
    }

    private void persist() {
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap = StreamProcessorDataHolder.
                getSiddhiManager().getSiddhiAppRuntimeMap();
        String[] siddhiRevisionArray = new String[siddhiAppRuntimeMap.size()];
        int siddhiAppCount = 0;
        for (SiddhiAppRuntime siddhiAppRuntime : siddhiAppRuntimeMap.values()) {
            PersistenceReference persistenceReference = siddhiAppRuntime.persist();
            siddhiRevisionArray[siddhiAppCount] = persistenceReference.getRevision();
            siddhiAppCount++;
            if (log.isDebugEnabled()) {
                log.debug("Revision " + persistenceReference.getRevision() +
                        " of siddhi App " + siddhiAppRuntime.getName() + " persisted successfully");
            }
        }
        sendControlMessageToPassiveNode(siddhiRevisionArray);
        if (StreamProcessorDataHolder.getNodeInfo() != null) {
            StreamProcessorDataHolder.getNodeInfo().setLastPersistedTimestamp(System.currentTimeMillis());
        }
    }

    private void sendControlMessageToPassiveNode(String[] siddhiRevisionArray) {
        try {
            if (!tcpNettyClient.isActive()) {
                tcpNettyClient.connect("localhost", 9892);
            }
            String siddhiAppRevisions = Arrays.toString(siddhiRevisionArray);
            tcpNettyClient.send("controlMessage", siddhiAppRevisions.getBytes());
        } catch (ConnectionUnavailableException e) {
            log.error("Error in connecting to 'localhost' and '9892' of the Passive node");
        }
    }
}
