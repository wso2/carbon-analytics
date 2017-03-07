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
import org.wso2.carbon.event.processor.manager.core.EventReceiverManagementService;
import org.wso2.carbon.event.processor.manager.core.exception.EventManagementException;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.siddhi.core.util.snapshot.ByteSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CarbonEventReceiverManagementService extends EventReceiverManagementService {

    private Logger log = Logger.getLogger(CarbonEventReceiverManagementService.class);
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private boolean isReceiverCoordinator = false;

    @Override
    public byte[] getState() {
        Map<Integer, Map<String, EventReceiver>> tenantSpecificEventAdapters = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getTenantSpecificEventReceiverMap();
        Map<Integer, HashMap<String, byte[]>> data = new HashMap<Integer, HashMap<String, byte[]>>();
        for (Map.Entry<Integer, Map<String, EventReceiver>> pair : tenantSpecificEventAdapters.entrySet()) {
            Map<String, EventReceiver> map = pair.getValue();
            int tenantId = pair.getKey();
            HashMap<String, byte[]> tenantData = new HashMap<String, byte[]>();

            for (Map.Entry<String, EventReceiver> receiverEntry : map.entrySet()) {
                if (receiverEntry.getValue().isEventDuplicatedInCluster()) {
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

        Map<Integer, HashMap<String, byte[]>> snapshotDataList = (HashMap<Integer, HashMap<String, byte[]>>) ByteSerializer.BToO(bytes);
        Map<Integer, Map<String, EventReceiver>> tenantSpecificEventAdapters = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getTenantSpecificEventReceiverMap();

        for (Map.Entry<Integer, HashMap<String, byte[]>> tenantEntry : snapshotDataList.entrySet()) {
            for (Map.Entry<String, byte[]> eventReceiverData : tenantEntry.getValue().entrySet()) {
                Map<String, EventReceiver> eventReceiverMap = tenantSpecificEventAdapters.get(tenantEntry.getKey());
                if (eventReceiverMap != null) {
                    EventReceiver eventReceiver = eventReceiverMap.get(eventReceiverData.getKey());
                    if (eventReceiver != null) {
                        eventReceiver.getInputEventDispatcher().syncState(eventReceiverData.getValue());
                    } else {
                        throw new EventManagementException("No event receiver with name '" + eventReceiverData.getKey() + "' exist for tenant  " + tenantEntry.getKey());
                    }
                } else {
                    throw new EventManagementException("No event receiver exist for tenant  " + tenantEntry.getKey());
                }
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
        EventReceiverServiceValueHolder.getCarbonEventReceiverService().start();
    }

    @Override
    public void startPolling() {
        EventReceiverServiceValueHolder.getCarbonEventReceiverService().startPolling();
    }

    @Override
    public boolean isReceiverCoordinator() {
        return isReceiverCoordinator;
    }

    public Lock getReadLock() {
        return readWriteLock.readLock();
    }


    public void setReceiverCoordinator(boolean isReceiverCoordinator) {
        this.isReceiverCoordinator = isReceiverCoordinator;
    }
}
