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

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.eventsink.internal.util.ServiceHolder;
import org.wso2.carbon.databridge.commons.Event;

import java.util.concurrent.Executors;

/**
 * This is the Queue which is created per tenant basis, and it will buffer all the events received for the tenant,
 * so that the consumers can do batch insertion.
 */

public class AnalyticsEventQueue {
    private static final Log log = LogFactory.getLog(AnalyticsEventQueue.class);
    private RingBuffer<WrappedEventFactory.WrappedEvent> ringBuffer;

    @SuppressWarnings("unchecked")
    public AnalyticsEventQueue(int tenantId) {
        Disruptor<WrappedEventFactory.WrappedEvent> eventQueue = new Disruptor<>(new WrappedEventFactory(), ServiceHolder.
                getAnalyticsEventSinkConfiguration().getQueueSize(), Executors.newCachedThreadPool());
        eventQueue.handleEventsWith(new AnalyticsEventQueueWorker(tenantId));
        this.ringBuffer = eventQueue.start();
        if (log.isDebugEnabled()) {
            log.debug("Event Queue Size = " + ServiceHolder.getAnalyticsEventSinkConfiguration().getQueueSize());
        }
    }

    public void put(Event event) {
        if (log.isDebugEnabled()) {
            log.debug("Adding an event to the event queue");
        }
        long sequence = this.ringBuffer.next();
        WrappedEventFactory.WrappedEvent bufferedEvent = this.ringBuffer.get(sequence);
        bufferedEvent.setEvent(event);
        this.ringBuffer.publish(sequence);
    }
}
