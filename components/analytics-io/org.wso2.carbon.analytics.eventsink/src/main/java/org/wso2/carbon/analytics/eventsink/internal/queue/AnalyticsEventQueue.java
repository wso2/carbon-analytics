/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.eventsink.internal.queue;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.eventsink.internal.util.ServiceHolder;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the Queue which is created per tenant basis, and it will buffer all the events received for the tenant,
 * so that the consumers can do batch insertion.
 */

public class AnalyticsEventQueue {
    private static final Log log = LogFactory.getLog(AnalyticsEventQueue.class);
    private RingBuffer<WrappedEventFactory.WrappedEvent> ringBuffer;
    private AtomicInteger currentSize;
    private int currentEventSize;
    private int maxSize;
    private Semaphore semaphore;
    private final Object lock = new Object();

    @SuppressWarnings("unchecked")
    public AnalyticsEventQueue(int tenantId) {
        Disruptor<WrappedEventFactory.WrappedEvent> eventQueue = new Disruptor<>(new WrappedEventFactory(),
                ServiceHolder.getAnalyticsEventSinkConfiguration().getQueueSize(),
                Executors.newCachedThreadPool(new ThreadFactoryBuilder().
                        setNameFormat("Thread pool- component - AnalyticsEventQueue").build()));
        eventQueue.handleEventsWith(new AnalyticsEventQueueWorker(tenantId, this));
        this.currentEventSize = 0;
        this.ringBuffer = eventQueue.start();
        this.currentSize = new AtomicInteger(0);
        this.maxSize = ServiceHolder.getAnalyticsEventSinkConfiguration().getMaxQueueCapacity();
        this.semaphore = new Semaphore(1);
        if (log.isDebugEnabled()) {
            log.debug("Event Queue Size = " + ServiceHolder.getAnalyticsEventSinkConfiguration().getQueueSize());
        }
    }

    public synchronized void put(Event event) {
        this.currentEventSize = DataBridgeCommonsUtils.getSize(event) + 4; //for the int value for size field.
        if (currentSize.get() >= maxSize) {
            try {
                semaphore.acquire();
                if (semaphore.availablePermits() == 0) {
                    synchronized (lock) {
                        if (semaphore.availablePermits() == 0) {
                            semaphore.release();
                        }
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }
        long sequence = this.ringBuffer.next();
        WrappedEventFactory.WrappedEvent bufferedEvent = this.ringBuffer.get(sequence);
        bufferedEvent.setEvent(event);
        bufferedEvent.setSize(this.currentEventSize);
        this.ringBuffer.publish(sequence);
        if (currentSize.addAndGet(this.currentEventSize) >= maxSize) {
            try {
                semaphore.acquire();
            } catch (InterruptedException ignored) {
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("current queue size in bytes : " + currentSize + ", remaining capacity : " +
                    this.ringBuffer.remainingCapacity());
        }
    }

    public void notifyReleasedEvent(WrappedEventFactory.WrappedEvent wrappedEvent, boolean endOfBatch) {
        currentSize.addAndGet(-wrappedEvent.getSize());
        wrappedEvent.setEvent(null);
        if (semaphore.availablePermits() == 0 && ((currentEventSize + currentSize.get()) < maxSize) || endOfBatch) {
            synchronized (lock) {
                if (semaphore.availablePermits() == 0 && ((currentEventSize + currentSize.get()) < maxSize) || endOfBatch) {
                    semaphore.release();
                }
            }
        }
    }

    public int getRemainingQueueSize() {
        return (int) this.ringBuffer.remainingCapacity();
    }

    public long getRemainingBufferCapacity() {
        return this.maxSize - this.currentSize.get();
    }
}
