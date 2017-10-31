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

package org.wso2.carbon.das.jobmanager.core.internal;

import org.wso2.carbon.das.jobmanager.core.HeartbeatListener;
import org.wso2.carbon.das.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.das.jobmanager.core.model.Heartbeat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class HeartbeatMonitor implements Runnable {
    private List<HeartbeatListener> listeners;
    private Map<String, Heartbeat> heartbeatMap;

    public HeartbeatMonitor() {
        listeners = new CopyOnWriteArrayList<>();
        heartbeatMap = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        long currentTimestamp = System.currentTimeMillis();
        for (Map.Entry<String, Heartbeat> heartbeatEntry : heartbeatMap.entrySet()) {
            Heartbeat heartbeat = heartbeatEntry.getValue();
            if (currentTimestamp - heartbeat.getLastUpdatedTimestamp() >= deploymentConfig.getHeartbeatInterval()) {
                heartbeat.incrementFailedAttempts();
                if (heartbeat.getFailedAttempts() > deploymentConfig.getHeartbeatMaxRetry()) {
                    heartbeatMap.remove(heartbeat.getNodeId());
                    notifyHeartbeatExpired(heartbeat);
                }
            }
        }
    }

    public Heartbeat updateHeartbeat(Heartbeat heartbeat) {
        Heartbeat prev = heartbeatMap.put(heartbeat.getNodeId(), heartbeat);
        if (prev != null) {
            notifyHeartbeatUpdated(heartbeat);
        } else {
            notifyHeartbeatAdded(heartbeat);
        }
        return prev;
    }

    public void registerHeartbeatChangeListener(HeartbeatListener listener) {
        listeners.add(listener);
    }

    private void notifyHeartbeatAdded(Heartbeat heartbeat) {
        for (HeartbeatListener listener : listeners) {
            listener.heartbeatAdded(heartbeat);
        }
    }

    private void notifyHeartbeatUpdated(Heartbeat heartbeat) {
        for (HeartbeatListener listener : listeners) {
            listener.heartbeatUpdated(heartbeat);
        }
    }

    private void notifyHeartbeatExpired(Heartbeat heartbeat) {
        for (HeartbeatListener listener : listeners) {
            listener.heartbeatExpired(heartbeat);
        }
    }
}
