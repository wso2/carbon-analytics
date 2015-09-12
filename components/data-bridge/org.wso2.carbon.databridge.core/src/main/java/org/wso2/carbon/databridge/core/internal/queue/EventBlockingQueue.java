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
package org.wso2.carbon.databridge.core.internal.queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.Utils.DataBridgeUtils;
import org.wso2.carbon.databridge.core.Utils.EventComposite;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class provides the blocking implementation based on the size of event composite in the queue. Also it make sure
 * the queue doesn't grow beyond the determined size.
 */
public class EventBlockingQueue extends ArrayBlockingQueue<EventComposite> {
    private static final Log log = LogFactory.getLog(EventBlockingQueue.class);
    private final Object lock = new Object();
    private AtomicInteger currentSize;
    private int currentEventCompositeSize;
    private int maxSize;
    private Semaphore semaphore;

    public EventBlockingQueue(int maxQueueSize, int maxSizeCapacity) {
        super(maxQueueSize);
        this.currentSize = new AtomicInteger(0);
        this.maxSize = maxSizeCapacity;
        this.semaphore = new Semaphore(1);
    }

    public synchronized void put(EventComposite eventComposite) {
        eventComposite.setSize(DataBridgeUtils.getSize(eventComposite));
        currentEventCompositeSize = eventComposite.getSize();
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
        try {
            super.put(eventComposite);
            if (currentSize.addAndGet(eventComposite.getSize()) >= maxSize) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException ignored) {
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("current queue size in bytes : " + currentSize + " , elements : " + size());
            }
        } catch (InterruptedException e) {
            String logMessage = "Failure to insert event into queue";
            log.warn(logMessage);
        }
    }

    public EventComposite poll() {
        EventComposite eventComposite = super.poll();
        currentSize.addAndGet(-eventComposite.getSize());
        if (semaphore.availablePermits() == 0 && ((currentEventCompositeSize + currentSize.get()) < maxSize) || isEmpty()) {
            synchronized (lock) {
                if (semaphore.availablePermits() == 0 && ((currentEventCompositeSize + currentSize.get()) < maxSize) || isEmpty()) {
                    semaphore.release();
                }
            }
        }
        return eventComposite;
    }
}
