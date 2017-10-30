/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.stream.processor.core.ha;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.table.record.RecordTableHandler;
import org.wso2.siddhi.core.util.transport.BackoffRetryCounter;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class that retry's connecting to record table
 */
public class RetryRecordTableConnection implements Runnable {

    private final BackoffRetryCounter backoffRetryCounter;
    private final RecordTableHandler recordTableHandler;
    private final ScheduledExecutorService scheduledExecutorService;
    private int count = 0;

    private static final Logger log = Logger.getLogger(RetryRecordTableConnection.class);

    public RetryRecordTableConnection(BackoffRetryCounter backoffRetryCounter, RecordTableHandler recordTableHandler,
                                      ScheduledExecutorService scheduledExecutorService) {
        this.backoffRetryCounter = backoffRetryCounter;
        this.recordTableHandler = recordTableHandler;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void run() {
        if (count++ < 3) {
            try {
                ((HACoordinationRecordTableHandler) recordTableHandler).setAsActive();
            } catch (ConnectionUnavailableException e) {
                log.error("HA Deployment: Error in connecting to table " + ((HACoordinationRecordTableHandler)
                        recordTableHandler).getTableId() + " while changing from passive" +
                        " state to active, will retry in " + backoffRetryCounter.getTimeInterval(), e);
                backoffRetryCounter.increment();
                scheduledExecutorService.schedule(new RetryRecordTableConnection(backoffRetryCounter,
                                recordTableHandler, scheduledExecutorService),
                        backoffRetryCounter.getTimeIntervalMillis(), TimeUnit.MILLISECONDS);
            }
        } else {
            log.error("Error reconnecting to " + ((HACoordinationRecordTableHandler) recordTableHandler).
                    getTableId() + ". Passive node may not be in sync with active node");
        }
    }
}
