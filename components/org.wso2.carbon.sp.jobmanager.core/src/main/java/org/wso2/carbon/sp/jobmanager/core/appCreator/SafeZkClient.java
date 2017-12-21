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

package org.wso2.carbon.sp.jobmanager.core.appCreator;

import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;

/**
 * Helper class to use Zookeeper client.
 * <p>
 * Workaround to delay loading of {@link ZKStringSerializer$} class at server startup.
 */
class SafeZkClient {

    /**
     * Creates a new Zookeeper client for the specified server URL.
     *
     * @param zooKeeperServerUrl server URL
     * @return a new Zookeeper client
     */
    public ZkClient createZkClient(String zooKeeperServerUrl) {
        return new ZkClient(zooKeeperServerUrl, 10000, 8000, ZKStringSerializer$.MODULE$);
    }
}
