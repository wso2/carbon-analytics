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

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The abstract class that needs to be implemented when supporting a new non-secure transport
 * to mainly create, validate and terminate  the client to the endpoint.
 */

public class EventSyncConnectionPoolFactory extends BaseKeyedPoolableObjectFactory {
    private String hostname;
    private int port;

    public EventSyncConnectionPoolFactory(String host, int port) {
        this.hostname = host;
        this.port = port;
    }

    @Override
    public Object makeObject(Object key) throws ConnectionUnavailableException {
        EventSyncConnection eventSyncConnection = new EventSyncConnection();
        eventSyncConnection.connect(hostname, port);
        return eventSyncConnection;
    }

    @Override
    public boolean validateObject(Object key, Object obj) {
        return ((EventSyncConnection) obj).isActive();
    }

    public void destroyObject(Object key, Object obj) {
        EventSyncConnection eventSyncConnection = ((EventSyncConnection) obj);
        eventSyncConnection.shutdown();
    }
}
