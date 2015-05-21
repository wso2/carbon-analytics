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
package org.wso2.carbon.event.processor.manager.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.siddhi.core.SiddhiManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PersistenceManager implements Runnable {

    private static final Log log = LogFactory.getLog(PersistenceManager.class);
    private final SiddhiManager siddhiManager;
    private final ScheduledExecutorService scheduledExecutorService;
    private final long interval;
    private final int tenantId;
    private ScheduledFuture<?> scheduledFuture = null;

    public PersistenceManager(SiddhiManager siddhiManager, ScheduledExecutorService scheduledExecutorService,
                              long interval, int tenantId) {
        this.siddhiManager = siddhiManager;
        this.scheduledExecutorService = scheduledExecutorService;
        this.interval = interval;
        this.tenantId = tenantId;
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
    }

    @Override
    public void run() {
        persist();
    }

    private void persist() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId,true);
            siddhiManager.persist();
        } catch (Throwable e) {
            log.error("Unable to persist state for tenant :" + tenantId, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
