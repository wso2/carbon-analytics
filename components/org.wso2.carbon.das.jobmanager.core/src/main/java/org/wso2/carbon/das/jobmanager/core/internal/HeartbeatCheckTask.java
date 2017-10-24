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

import org.wso2.carbon.das.jobmanager.core.ResourceExpireListener;
import org.wso2.carbon.das.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.das.jobmanager.core.model.Heartbeat;
import org.wso2.carbon.das.jobmanager.core.model.ResourceMapping;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class HeartbeatCheckTask implements Runnable {
    private List<ResourceExpireListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void run() {
        ResourceMapping resourceMapping = ServiceDataHolder.getResourceMapping();
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        long currentTimestamp = System.currentTimeMillis();
        if (resourceMapping != null) {
            for (Map.Entry<String, Heartbeat> heartbeatEntry : resourceMapping.getHeartbeatMap().entrySet()) {
                Heartbeat heartbeat = heartbeatEntry.getValue();
                if (currentTimestamp - heartbeat.getLastUpdatedTimestamp() >= deploymentConfig.getHeartbeatInterval()) {
                    heartbeat.incrementFailedAttempts();
                    if (heartbeat.getFailedAttempts() > deploymentConfig.getHeartbeatMaxRetry()) {
                        heartbeat.setExpired(true);
                        notifyHeartbeatExpired(heartbeat);
                    }
                }
            }
        }
    }

    public void registerHeartbeatChangeListener(ResourceExpireListener listener) {
        listeners.add(listener);
    }

    private void notifyHeartbeatExpired(Heartbeat heartbeat) {
        for (ResourceExpireListener listener : listeners) {
            listener.resourceExpired(heartbeat);
        }
    }
}
