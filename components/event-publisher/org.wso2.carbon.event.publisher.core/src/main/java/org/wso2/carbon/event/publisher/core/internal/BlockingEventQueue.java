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
package org.wso2.carbon.event.publisher.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.publisher.core.internal.util.EventPublisherUtil;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingEventQueue extends LinkedBlockingQueue<EventPublisher.EventWrapper>{
    private static final Log log = LogFactory.getLog(BlockingEventQueue.class);
    private int maxSizeInBytes;
    private Semaphore semaphore;
    private AtomicInteger currentSize;
    private final Object lock;
    private int currentEventSize;

    public BlockingEventQueue(int maxSizeInMb, int maxNumOfEvents) {
        super(maxNumOfEvents);
        this.maxSizeInBytes = maxSizeInMb * 1000000;
        this.semaphore = new Semaphore(1);
        this.currentSize = new AtomicInteger(0);
        this.lock = new Object();
        this.currentEventSize = 0;
    }

    public synchronized void put(EventPublisher.EventWrapper event) throws InterruptedException {
        this.currentEventSize = EventPublisherUtil.getSize(event.getEvent()) + 4 + 8; //for the int and long value for size field.
        event.setSize(this.currentEventSize);
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
        super.put(event);
        if (currentSize.addAndGet(this.currentEventSize) >= maxSizeInBytes) {
            try {
                semaphore.acquire();
            } catch (InterruptedException ignored) {
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("current queue size in bytes : " + currentSize + ", remaining capacity : " +
                    remainingCapacity());
        }

    }


    public EventPublisher.EventWrapper take() throws InterruptedException {
        EventPublisher.EventWrapper wrappedEvent = super.take();
        releaseEvent(wrappedEvent);
        return wrappedEvent;
    }


    private void releaseEvent(EventPublisher.EventWrapper wrappedEvent) {
        currentSize.addAndGet(-wrappedEvent.getSize());
        if (semaphore.availablePermits() == 0 && ((currentEventSize + currentSize.get() < maxSizeInBytes) || size() == 0)) {
            synchronized (lock) {
                if (semaphore.availablePermits() == 0 && ((currentEventSize + currentSize.get() < maxSizeInBytes) || size() == 0)) {
                    semaphore.release();
                }
            }
        }
    }

    public EventPublisher.EventWrapper poll() {
        EventPublisher.EventWrapper wrappedEvent = super.poll();
        if (wrappedEvent != null) {
            releaseEvent(wrappedEvent);
            return wrappedEvent;
        } else {
            return null;
        }
    }

    public EventPublisher.EventWrapper peek() {
        EventPublisher.EventWrapper wrappedEvent = super.peek();
        if (wrappedEvent != null) {
            return wrappedEvent;
        } else {
            return null;
        }
    }

    public boolean offer(EventPublisher.EventWrapper event){
        int size = EventPublisherUtil.getSize(event.getEvent()) + 4 + 8; //for the int and long value for size field.
        event.setSize(size);
        boolean acquired = true;
        boolean offered = false;
        if (currentSize.get() >= maxSizeInBytes) {
                acquired = semaphore.tryAcquire();
                if (acquired) {
                    if (semaphore.availablePermits() == 0) {
                        synchronized (lock) {
                            if (semaphore.availablePermits() == 0) {
                                semaphore.release();
                            }
                        }
                    }
                }
        }
        if (acquired) {
            offered = super.offer(event);
        }
        if (currentSize.addAndGet(size) >= maxSizeInBytes) {
                semaphore.tryAcquire();
        }
        if (log.isDebugEnabled()) {
            log.debug("current queue size in bytes : " + currentSize.get() + ", remaining capacity : " +
                    remainingCapacity());
        }
        return offered;
    }

    public EventPublisher.EventWrapper remove(){
        EventPublisher.EventWrapper eventWrapper = super.remove();
        if (eventWrapper != null){
            releaseEvent(eventWrapper);
        }
        return eventWrapper;
    }
}
