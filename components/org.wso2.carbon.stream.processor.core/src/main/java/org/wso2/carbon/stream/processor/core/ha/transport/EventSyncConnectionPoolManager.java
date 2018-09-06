/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.stream.processor.core.ha.transport;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.stream.processor.core.internal.beans.EventSyncClientPoolConfig;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class is used hold the secure/non-secure connections for an Agent.
 */

public class EventSyncConnectionPoolManager {
    private static GenericKeyedObjectPool connectionPool;
    private static AtomicLong sequenceID = new AtomicLong();


    public static void initializeConnectionPool(DeploymentConfig deploymentConfig) {
        EventSyncClientPoolConfig eventSyncClientPoolConfig = deploymentConfig.getTcpClientPoolConfig();
        EventSyncConnectionPoolFactory eventSyncConnectionPoolFactory = new EventSyncConnectionPoolFactory(deploymentConfig.getPassiveNodeHost(),
                deploymentConfig.getPassiveNodePort());
        initializeConnectionPool(eventSyncConnectionPoolFactory, eventSyncClientPoolConfig.getMaxActive(), eventSyncClientPoolConfig.getMaxTotal(),
                eventSyncClientPoolConfig.getMaxIdle(), eventSyncClientPoolConfig.getMaxWait(),
                eventSyncClientPoolConfig.getMinEvictableIdleTimeMillis());
    }

    public synchronized static void initializeConnectionPool(EventSyncConnectionPoolFactory factory,
                                                int maxActive,
                                                int maxTotal,
                                                int maxIdle,
                                                long maxWait,
                                                long minEvictableIdleTimeMillis) {
        if (connectionPool == null) {
            connectionPool = new GenericKeyedObjectPool();
            connectionPool.setFactory(factory);
            connectionPool.setMaxTotal(maxTotal);
            connectionPool.setMaxActive(maxActive);
            connectionPool.setTestOnBorrow(true);
            connectionPool.setTimeBetweenEvictionRunsMillis(12000);
            connectionPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
            connectionPool.setMaxIdle(maxIdle);
            connectionPool.setMaxWait(maxWait);
            connectionPool.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK);
        }
    }

    public static GenericKeyedObjectPool getConnectionPool() {
        return connectionPool;
    }

    public static AtomicLong getSequenceID() {
        return sequenceID;
    }
}
