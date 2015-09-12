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

import com.lmax.disruptor.EventHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.eventsink.internal.util.ServiceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the queue worker which listens to analytics queue; and once the batch size is reached, it will
 * do an insertion operation on the analytics data service.
 */
public class AnalyticsEventQueueWorker implements EventHandler<WrappedEventFactory.WrappedEvent> {
    private static final Log log = LogFactory.getLog(AnalyticsEventQueueWorker.class);
    private AnalyticsEventQueue queue;
    private List<Event> events;
    private int tenantId;
    private int totalSize;
    private AnalyticsBlockingExecutor threadPoolExecutor;

    public AnalyticsEventQueueWorker(int tenantId, AnalyticsEventQueue queue) {
        this.tenantId = tenantId;
        this.events = new ArrayList<>();
        this.queue = queue;
        this.threadPoolExecutor = new AnalyticsBlockingExecutor(
                ServiceHolder.getAnalyticsEventSinkConfiguration().getWorkerPoolSize());
        this.totalSize = 0;
    }

    @Override
    public void onEvent(WrappedEventFactory.WrappedEvent wrappedEvent, long sequence, boolean endOfBatch) throws Exception {
        if (totalSize + wrappedEvent.getSize() > ServiceHolder.getAnalyticsEventSinkConfiguration().getBatchSize()) {
            if (!this.events.isEmpty()) {
                pushEvents();
                this.events.add(wrappedEvent.getEvent());
                totalSize += wrappedEvent.getSize();
            } else {
                this.events.add(wrappedEvent.getEvent());
                totalSize += wrappedEvent.getSize();
                pushEvents();
            }
        } else {
            this.events.add(wrappedEvent.getEvent());
            totalSize += wrappedEvent.getSize();
            if (log.isDebugEnabled()){
                log.debug("Collecting events, current totalSize : "+ totalSize);
            }
        }
        if (endOfBatch && !this.events.isEmpty()){
            pushEvents();
        }
        this.queue.notifyReleasedEvent(wrappedEvent, endOfBatch);
    }

    private void pushEvents() {
        List<Event> tmpEvents = this.events;
        this.events = new ArrayList<>();
        submitJob(tmpEvents);
        totalSize = 0;
    }

    private void submitJob(List<Event> tmpEvents) {
        this.threadPoolExecutor.submit(new AnalyticsEventProcessor(tmpEvents, totalSize));
    }

    public class AnalyticsEventProcessor extends Thread {

        private List<Event> events;
        private int size;

        private AnalyticsEventProcessor(List<Event> events, int totalSize) {
            this.events = events;
            this.size = totalSize;
        }

        public void run() {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                if (log.isDebugEnabled()){
                    log.debug("Batch size of : "+ this.size +" is going to be inserted in DAL");
                }
                ServiceHolder.getAnalyticsDSConnector().insertEvents(tenantId, this.events);
            } catch (Exception e) {
                String errorMsg = "Error processing event. ";
                log.error(errorMsg, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }
}
