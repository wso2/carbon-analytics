/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.stream.processor.core.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.util.snapshot.PersistenceReference;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class that manages the periodic persistence of Siddhi Applications
 */
public class PersistenceManager implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);

    private final ScheduledExecutorService scheduledExecutorService;
    private final long interval;
    private ScheduledFuture<?> scheduledFuture = null;

    public PersistenceManager(ScheduledExecutorService scheduledExecutorService, long interval) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.interval = interval;
    }

    /**
     * Method that creates a scheduler service that would call the run() method every time interval
     */
    public void init() {
        if (interval > 0) {
            scheduledFuture = scheduledExecutorService.
                    scheduleAtFixedRate(this, interval, interval, TimeUnit.MINUTES);
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
        ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap = StreamProcessorDataHolder.
                getSiddhiManager().getSiddhiAppRuntimeMap();
        for (SiddhiAppRuntime siddhiAppRuntime : siddhiAppRuntimeMap.values()) {
            PersistenceReference persistenceReference = siddhiAppRuntime.persist();
            if (log.isDebugEnabled()) {
                log.debug("Revision " + persistenceReference.getRevision() +
                        " of siddhi App " + siddhiAppRuntime.getName() + " persisted successfully");
            }
        }
    }
}
