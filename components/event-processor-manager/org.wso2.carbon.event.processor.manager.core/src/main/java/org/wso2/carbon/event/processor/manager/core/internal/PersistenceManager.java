/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.processor.manager.core.internal;

import org.wso2.carbon.event.processor.manager.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.manager.core.internal.ds.EventManagementServiceValueHolder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PersistenceManager implements Runnable {

    private final ScheduledExecutorService scheduledExecutorService;
    private final long interval;
    private ScheduledFuture<?> scheduledFuture = null;

    public PersistenceManager(ScheduledExecutorService scheduledExecutorService, long interval) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.interval = interval;
    }

    public void init() {
        if (interval > 0) {
            scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this, interval, interval, TimeUnit.MINUTES);
        }
    }

    public void shutdown() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        persist();
        scheduledExecutorService.shutdown();
    }

    @Override
    public void run() {
        persist();
    }

    private void persist() {
        EventProcessorManagementService eventProcessorManagementService = EventManagementServiceValueHolder.getCarbonEventManagementService().getEventProcessorManagementService();
        eventProcessorManagementService.persist();
    }

}
