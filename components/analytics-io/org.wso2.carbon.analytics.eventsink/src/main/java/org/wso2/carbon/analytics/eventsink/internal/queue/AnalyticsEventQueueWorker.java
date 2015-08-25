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
import java.util.concurrent.*;

/**
 * This is the queue worker which listens to analytics queue; and once the batch size is reached, it will
 * do an insertion operation on the analytics data service.
 */
public class AnalyticsEventQueueWorker implements EventHandler<WrappedEventFactory.WrappedEvent> {
    private static final Log log = LogFactory.getLog(AnalyticsEventQueueWorker.class);

    private List<Event> events;
    private int tenantId;
    private ExecutorService threadPoolExecutor;

    public AnalyticsEventQueueWorker(int tenantId) {
        this.tenantId = tenantId;
        this.events = new ArrayList<>();
        this.threadPoolExecutor = Executors.newFixedThreadPool(ServiceHolder.getAnalyticsEventSinkConfiguration().
                getWorkerPoolSize());
    }


    @Override
    public void onEvent(WrappedEventFactory.WrappedEvent wrappedEvent, long sequence, boolean endOfBatch) throws Exception {
        this.events.add(wrappedEvent.getEvent());
        if (endOfBatch || this.events.size() == ServiceHolder.getAnalyticsEventSinkConfiguration().getBundleSize()) {
            List<Event> tmpEvents = this.events;
            this.events = new ArrayList<>();
            submitJob(tmpEvents);
        }
    }

    private void submitJob(List<Event> tmpEvents) {
        this.threadPoolExecutor.submit(new AnalyticsEventProcessor(tmpEvents));
    }

    public class AnalyticsEventProcessor extends Thread {

        private List<Event> events;

        private AnalyticsEventProcessor(List<Event> events) {
            this.events = events;
        }

        public void run() {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                ServiceHolder.getAnalyticsDSConnector().insertEvents(tenantId, this.events);
                PrivilegedCarbonContext.endTenantFlow();
            } catch (Exception e) {
                String errorMsg = "Error processing event. ";
                log.error(errorMsg, e);
            }
        }
    }
}
