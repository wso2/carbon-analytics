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
*
*/
package org.wso2.carbon.event.receiver.core.internal.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingEventQueue {
    private static final Log log = LogFactory.getLog(BlockingEventQueue.class);
    private int maxSizeInBytes;
    private BlockingQueue<WrappedEvent> queue;
    private Semaphore semaphore;
    private AtomicInteger currentSize;
    private final Object lock;
    private int currentEventSize;

    public BlockingEventQueue(int maxSizeInMb, int maxNumOfEvents) {
        this.maxSizeInBytes = maxSizeInMb * 1000000; // to convert to bytes
        this.queue = new LinkedBlockingQueue<>(maxNumOfEvents);
        this.semaphore = new Semaphore(1);
        this.currentSize = new AtomicInteger(0);
        this.lock = new Object();
        this.currentEventSize = 0;
    }

    public synchronized void put(Event event) throws InterruptedException {
        this.currentEventSize = EventReceiverUtil.getSize(event) + 4; //for the int value for size field.
        if (currentSize.get() >= maxSizeInBytes) {
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
        this.queue.put(new WrappedEvent(this.currentEventSize, event));
        if (currentSize.addAndGet(this.currentEventSize) >= maxSizeInBytes) {
            try {
                semaphore.acquire();
            } catch (InterruptedException ignored) {
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Current queue size in bytes : " + currentSize + ", remaining capacity : " +
                    this.queue.remainingCapacity());
        }

    }

    public Event take() throws InterruptedException {
        WrappedEvent wrappedEvent = this.queue.take();
        releaseEvent(wrappedEvent);
        return wrappedEvent.getEvent();
    }

    private void releaseEvent(WrappedEvent wrappedEvent) {
        currentSize.addAndGet(-wrappedEvent.getSize());
        if (semaphore.availablePermits() == 0 && ((currentEventSize + currentSize.get() < maxSizeInBytes) || queue.size() == 0)) {
            synchronized (lock) {
                if (semaphore.availablePermits() == 0 && ((currentEventSize + currentSize.get() < maxSizeInBytes) || queue.size() == 0)) {
                    semaphore.release();
                }
            }
        }
    }

    public Event poll() {
        WrappedEvent wrappedEvent = this.queue.poll();
        if (wrappedEvent != null) {
            releaseEvent(wrappedEvent);
            return wrappedEvent.getEvent();
        } else {
            return null;
        }
    }

    public Event peek() {
        WrappedEvent wrappedEvent = this.queue.peek();
        if (wrappedEvent != null) {
            return wrappedEvent.getEvent();
        } else {
            return null;
        }
    }

    private class WrappedEvent {
        private int size;
        private Event event;

        public WrappedEvent(int size, Event event) {
            this.size = size;
            this.event = event;
        }

        public int getSize() {
            return size;
        }

        public Event getEvent() {
            return event;
        }
    }
}
