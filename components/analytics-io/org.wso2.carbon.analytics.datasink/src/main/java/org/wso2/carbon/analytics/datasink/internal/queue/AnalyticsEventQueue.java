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
package org.wso2.carbon.analytics.datasink.internal.queue;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasink.internal.AnalyticsDatasinkConfiguration;
import org.wso2.carbon.databridge.commons.Event;

import java.util.concurrent.Executors;

public class AnalyticsEventQueue {
    private static final Log log = LogFactory.getLog(AnalyticsEventQueue.class);
    private RingBuffer<Event> ringBuffer;

    public final EventFactory<Event> EVENT_FACTORY = new EventFactory<Event>() {
        public Event newInstance() {
            return new Event();
        }
    };

    @SuppressWarnings("unchecked")
    public AnalyticsEventQueue(int tenantId) {
        Disruptor<Event> eventQueue = new Disruptor<Event>(EVENT_FACTORY, AnalyticsDatasinkConfiguration.
                getInstance().getQueueSize(), Executors.newCachedThreadPool());
        eventQueue.handleEventsWith(new AnalyticsEventQueueWorker(tenantId));
        this.ringBuffer = eventQueue.start();
        if (log.isDebugEnabled()) {
            log.debug("Event Queue Size = " + AnalyticsDatasinkConfiguration.
                    getInstance().getQueueSize());
        }
    }

    public void put(Event event) {
        long sequence = this.ringBuffer.next();
        Event bufferedEvent = this.ringBuffer.get(sequence);
        updateEvent(bufferedEvent, event);
        this.ringBuffer.publish(sequence);
    }

    private void updateEvent(Event oldEvent, Event newEvent) {
        oldEvent.setArbitraryDataMap(newEvent.getArbitraryDataMap());
        oldEvent.setCorrelationData(newEvent.getCorrelationData());
        oldEvent.setMetaData(newEvent.getMetaData());
        oldEvent.setPayloadData(newEvent.getPayloadData());
        oldEvent.setStreamId(newEvent.getStreamId());
        oldEvent.setTimeStamp(newEvent.getTimeStamp());
    }
}
