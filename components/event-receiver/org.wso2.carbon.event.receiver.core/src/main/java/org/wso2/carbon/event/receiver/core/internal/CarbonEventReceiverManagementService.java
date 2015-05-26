/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.receiver.core.internal;


import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.manager.commons.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServerConfig;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.core.EventReceiverManagementService;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.siddhi.core.util.snapshot.ByteSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CarbonEventReceiverManagementService extends EventReceiverManagementService {

    private Logger log = Logger.getLogger(CarbonEventReceiverManagementService.class);
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Override
    public byte[] getState() {
        Map<Integer, Map<String, EventReceiver>> tenantSpecificEventAdapters = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getTenantSpecificEventReceiverMap();
        Map<Integer, HashMap<String, byte[]>> data = new HashMap<Integer, HashMap<String, byte[]>>();
        for (Map.Entry<Integer, Map<String, EventReceiver>> pair : tenantSpecificEventAdapters.entrySet()) {
            Map<String, EventReceiver> map = pair.getValue();
            int tenantId = pair.getKey();
            HashMap<String, byte[]> tenantData = new HashMap<String, byte[]>();

            for (Map.Entry<String, EventReceiver> receiverEntry : map.entrySet()) {
                if (receiverEntry.getValue().getInputAdapterRuntime().isEventDuplicatedInCluster()) {
                    byte[] state = receiverEntry.getValue().getInputEventDispatcher().getState();
                    tenantData.put(receiverEntry.getKey(), state);
                }
            }
            data.put(tenantId, tenantData);
        }
        return ByteSerializer.OToB(data);
    }

    @Override
    public void syncState(byte[] bytes) {
        Map<Integer, Map<String, EventReceiver>> tenantSpecificEventAdapters = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getTenantSpecificEventReceiverMap();
        Map<Integer, HashMap<String, byte[]>> data = new HashMap<Integer, HashMap<String, byte[]>>();
        for (Map.Entry<Integer, HashMap<String, byte[]>> pair : data.entrySet()) {
            Map<String, byte[]> map = pair.getValue();
            int tenantId = pair.getKey();
            for (Map.Entry<String, byte[]> receiverEntry : map.entrySet()) {
                tenantSpecificEventAdapters.get(tenantId).get(receiverEntry.getKey())
                        .getInputEventDispatcher().syncState(receiverEntry.getValue());
            }
        }
    }

    @Override
    public void pause() {
        readWriteLock.writeLock().lock();
    }

    @Override
    public void resume() {
        readWriteLock.writeLock().unlock();
    }

    @Override
    public void start() {
        EventReceiverServiceValueHolder.getCarbonEventReceiverService().startInputAdapterRuntimes();
    }

    @Override
    public void startPolling() {
        EventReceiverServiceValueHolder.getCarbonEventReceiverService().startPollingInputAdapterRuntimes();
    }

    public Lock getReadLock() {
        return readWriteLock.readLock();
    }

}
