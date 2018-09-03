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
import org.wso2.carbon.stream.processor.core.internal.beans.TCPClientPoolConfig;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class is used hold the secure/non-secure connections for an Agent.
 */

public class TCPConnectionPoolManager {
    private static GenericKeyedObjectPool connectionPool;
    private static AtomicLong sequenceID = new AtomicLong();


    public static void initializeConnectionPool(DeploymentConfig deploymentConfig) {
        TCPClientPoolConfig tcpClientPoolConfig = deploymentConfig.getTcpClientPoolConfig();
        TCPConnectionPoolFactory tcpConnectionPoolFactory = new TCPConnectionPoolFactory(deploymentConfig.getPassiveNodeHost(),
                deploymentConfig.getPassiveNodePort());
        initializeConnectionPool(tcpConnectionPoolFactory, tcpClientPoolConfig.getMaxActive(), tcpClientPoolConfig
                .getMaxIdle(), tcpClientPoolConfig.isTestOnBorrow(), tcpClientPoolConfig
                .getTimeBetweenEvictionRunsMillis(), tcpClientPoolConfig.getMinEvictableIdleTimeMillis());
    }

    public static void initializeConnectionPool(TCPConnectionPoolFactory factory,
                                                int maxActive,
                                                int maxIdle,
                                                boolean testOnBorrow,
                                                long timeBetweenEvictionRunsMillis,
                                                long minEvictableIdleTimeMillis) {
        if (connectionPool == null) {
            connectionPool = new GenericKeyedObjectPool();
            connectionPool.setFactory(factory);
            connectionPool.setMaxTotal(maxActive);
            connectionPool.setMaxActive(maxActive);
            connectionPool.setTestOnBorrow(testOnBorrow);
            connectionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
            connectionPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
            connectionPool.setMaxIdle(maxIdle);
            connectionPool.setMaxWait(10000);
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
