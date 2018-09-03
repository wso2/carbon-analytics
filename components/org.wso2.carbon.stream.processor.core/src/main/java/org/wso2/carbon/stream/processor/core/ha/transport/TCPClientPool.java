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

/**
 * This class is used hold the secure/non-secure connections for an Agent.
 */

public class TCPClientPool {
    private GenericKeyedObjectPool socketPool;

    public GenericKeyedObjectPool getClientPool(TCPClientPoolFactory factory,
                                                int maxActive,
                                                int maxIdle,
                                                boolean testOnBorrow,
                                                long timeBetweenEvictionRunsMillis,
                                                long minEvictableIdleTimeMillis) {
        if (socketPool == null) {
            synchronized (this) {
                if (socketPool == null) {
                    socketPool = new GenericKeyedObjectPool();
                    socketPool.setFactory(factory);
                    socketPool.setMaxTotal(maxActive);
                    socketPool.setMaxActive(maxActive);
                    socketPool.setTestOnBorrow(testOnBorrow);
                    socketPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
                    socketPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
                    socketPool.setMaxIdle(maxIdle);
                    socketPool.setMaxWait(10000);
                    socketPool.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK);
                }
            }
        }
        return socketPool;
    }
}
