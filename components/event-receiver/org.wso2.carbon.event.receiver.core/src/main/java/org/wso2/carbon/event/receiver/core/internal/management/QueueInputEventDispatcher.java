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

package org.wso2.carbon.event.receiver.core.internal.management;

import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.event.processor.manager.core.EventSync;
import org.wso2.carbon.event.processor.manager.core.Manager;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.siddhi.core.util.snapshot.ByteSerializer;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QueueInputEventDispatcher extends AbstractInputEventDispatcher implements EventSync {

    private final StreamDefinition streamDefinition;
    private Logger log = Logger.getLogger(AbstractInputEventDispatcher.class);
    private final BlockingQueue<Object[]> eventQueue = new LinkedBlockingQueue<Object[]>();
    private Lock readLock;
    private String syncId;
    private int tenantId;
    private ReentrantLock threadBarrier = new ReentrantLock();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public QueueInputEventDispatcher(int tenantId, String syncId, Lock readLock, org.wso2.carbon.databridge.commons.StreamDefinition exportedStreamDefinition) {
        this.readLock = readLock;
        this.tenantId = tenantId;
        this.syncId = syncId;

        org.wso2.siddhi.query.api.definition.StreamDefinition streamDefinition = new org.wso2.siddhi.query.api.definition.StreamDefinition();
        streamDefinition.setId(syncId);

        List<Attribute> attributes = new ArrayList<Attribute>();
        if (exportedStreamDefinition.getMetaData() != null) {
            attributes.addAll(exportedStreamDefinition.getMetaData());
        }
        if (exportedStreamDefinition.getCorrelationData() != null) {
            attributes.addAll(exportedStreamDefinition.getCorrelationData());
        }
        if (exportedStreamDefinition.getPayloadData() != null) {
            attributes.addAll(exportedStreamDefinition.getPayloadData());
        }
        for (Attribute attr : attributes) {
            streamDefinition.attribute(attr.getName(), org.wso2.siddhi.query.api.definition.Attribute.Type.valueOf(attr.getType().toString()));
        }
        this.streamDefinition = streamDefinition;

        executorService.submit(new QueueInputEventDispatcherWorker());
    }

    @Override
    public void onEvent(Object[] event) {
        try {
            threadBarrier.lock();
            eventQueue.put(event);
            threadBarrier.unlock();
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting to put the event to queue.", e);
        }
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public byte[] getState() {
        threadBarrier.lock();
        byte[] state = ByteSerializer.OToB(eventQueue);
        threadBarrier.unlock();
        return state;
    }

    @Override
    public void syncState(byte[] bytes) {
        BlockingQueue<Object[]> events = (BlockingQueue<Object[]>) ByteSerializer.BToO(bytes);
        for (Object object : events) {
            if (Arrays.deepEquals((Object[]) object, eventQueue.peek())) {
                eventQueue.poll();
            } else {
                break;
            }
        }
    }

    @Override
    public void process(Object[] data) {
        readLock.lock();
        readLock.unlock();
        callBack.sendEventData(data);
    }

    @Override
    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    class QueueInputEventDispatcherWorker implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
                while (true) {
                    try {
                        readLock.lock();
                        readLock.unlock();
                        Object[] event = eventQueue.take();
                        readLock.lock();
                        readLock.unlock();
                        if (!isDrop()) {
                            callBack.sendEventData(event);
                        }
                        if (isSendToOther()) {
                            EventReceiverServiceValueHolder.getEventManagementService().syncEvent(syncId, Manager.ManagerType.Receiver, event);
                        }
                    } catch (InterruptedException e) {
                        log.error("Interrupted while waiting to get an event from queue.", e);
                    }
                }
            } catch (Exception e) {
                log.error("Error in dispatching events.");
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }
}
