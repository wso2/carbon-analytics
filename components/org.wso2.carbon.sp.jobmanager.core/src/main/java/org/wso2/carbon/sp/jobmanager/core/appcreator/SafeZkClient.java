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

package org.wso2.carbon.sp.jobmanager.core.appcreator;

import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.exception.ResourceManagerException;

/**
 * Helper class to use Zookeeper client.
 * <p>
 * Workaround to delay loading of {@link ZKStringSerializer$} class at server startup.
 */
class SafeZkClient {

    private static final Logger log = Logger.getLogger(SafeZkClient.class);
    private ZkClient zkClient;
    private ZkUtils zkUtils;

    /**
     * Creates a new Zookeeper client for the specified server URL.
     *
     * @param zooKeeperServerUrls server URL
     * @return a new Zookeeper client
     */
    public ZkUtils createZkClient(String[] zooKeeperServerUrls, boolean isSecureKafkaCluster, int sessionTimeout,
                                  int connectionTimeout) {
        for (String zooKeeperServerUrl : zooKeeperServerUrls) {
            try {
                zkClient = new ZkClient(
                        zooKeeperServerUrl, sessionTimeout, connectionTimeout, ZKStringSerializer$.MODULE$);
                zkUtils = new ZkUtils(zkClient, new ZkConnection(zooKeeperServerUrl), isSecureKafkaCluster);
                break;
            } catch (ZkTimeoutException e) {
                log.error("Zookeeper server at " + zooKeeperServerUrl + " can not be reached.", e);
            }
        }
        if (zkUtils != null) {
            return zkUtils;
        } else {
            throw new ResourceManagerException("All listed Zookeeper servers can not be reached.");
        }
    }

    public void closeClient() {
        if (zkUtils != null) {
            zkUtils.close();
            if (log.isDebugEnabled()) {
                log.debug("zkUtils connection closed.");
            }
        }
        if (zkClient != null) {
            zkClient.close();
            if (log.isDebugEnabled()) {
                log.debug("zkClient connection closed.");
            }
        }
    }
}
