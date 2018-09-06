/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.stream.processor.core.ha.tcp;

import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;

/**
 * Singleton tcp server.
 */
public class TCPServer {
    private static TCPServer instance = new TCPServer();
    private EventSyncServer eventSyncServer = new EventSyncServer();
    private boolean started = false;

    private TCPServer() {
    }

    public static TCPServer getInstance() {
        return instance;
    }

    public void start(DeploymentConfig deploymentConfig) {
        if (!started) {
            eventSyncServer.start(deploymentConfig);
            started = true;
        }
    }

    public void stop() {
        if (started) {
            try {
                eventSyncServer.shutdownGracefully();
            } finally {
                started = false;
            }
        }
    }

    public void clearResources() {
        eventSyncServer.clearResources();
    }

    public EventSyncServer getEventSyncServer() {
        return eventSyncServer;
    }

}
