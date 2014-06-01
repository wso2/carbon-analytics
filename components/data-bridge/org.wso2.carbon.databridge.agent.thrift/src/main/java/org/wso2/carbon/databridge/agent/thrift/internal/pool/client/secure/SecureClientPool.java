/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.agent.thrift.internal.pool.client.secure;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.thrift.internal.pool.client.general.ClientPool;

public class SecureClientPool {

    private static volatile GenericKeyedObjectPool socketPool = null;

    public GenericKeyedObjectPool getClientPool(KeyedPoolableObjectFactory factory,
                                                int maxActive,
                                                int maxIdle,
                                                boolean testOnBorrow,
                                                long timeBetweenEvictionRunsMillis,
                                                long minEvictableIdleTimeMillis) {
        if (socketPool == null) {
            synchronized (ClientPool.class) {
                if (socketPool == null) {
                    socketPool = new GenericKeyedObjectPoolImpl();
                    socketPool.setFactory(factory);
                    socketPool.setMaxActive(maxActive);
                    socketPool.setTestOnBorrow(testOnBorrow);
                    socketPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
                    socketPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
                    socketPool.setMaxIdle(maxIdle);
                    socketPool.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK);
                }
            }
        }
        return socketPool;
    }

    static class GenericKeyedObjectPoolImpl extends GenericKeyedObjectPool {
        @Override
        public void close() throws Exception {
            super.close();
        }
    }

}
