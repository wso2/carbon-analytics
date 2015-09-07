/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.dataservice.indexing;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsReceiverIndexingFlowControlConfiguration;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * This class contains the state of the current receiver / indexing performance,
 * and does the required flow controlling operations.
 */
public class AnalyticsReceiverIndexingFlowController extends TimerTask {

    private static final int SYNC_TIME_EDGE_INTERVAL = 1000;

    private static final int LOCAL_GLOBAL_SYNC_TIME_INTERVAL = 5000;

    private static final int LOCAL_GLOBAL_UPDATE_THRESHOLD = 1000;
    
    private static final int THROTTLING_ACTION_STABILIZE_INTERVAL = 3000;

    private static final String ANALYICS_FLOW_CONTROLLER_GLOBAL_COUNT = "ANALYICS_FLOW_CONTROLLER_GLOBAL_COUNT";

    private Log log = LogFactory.getLog(AnalyticsReceiverIndexingFlowController.class);
    
    private AnalyticsReceiverIndexingFlowControlConfiguration config;
    
    private AtomicInteger localCount = new AtomicInteger();
    
    private GenericUtils.GenericAtomicLong globalCount;
    
    private Timer localGlobalSyncTimer;
    
    private long localGlobalCountLastSyncTime;
    
    private long lastThrottlingActionTimestamp;
    
    private int throttlePosition;

    public AnalyticsReceiverIndexingFlowController(AnalyticsReceiverIndexingFlowControlConfiguration config) {
        this.config = config;
        if (log.isDebugEnabled()) {
            log.debug("Analytics Receiver Indexing Flow Control Enabled: " + this.getConfig().isEnabled());
        }
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            this.globalCount = new GenericUtils.GenericAtomicLong(
                    AnalyticsServiceHolder.getHazelcastInstance().getAtomicLong(ANALYICS_FLOW_CONTROLLER_GLOBAL_COUNT));
            
        } else {
            this.globalCount = new GenericUtils.GenericAtomicLong();
        }
        if (this.getConfig().isEnabled()) {
            this.localGlobalSyncTimer = new Timer();
            this.localGlobalSyncTimer.scheduleAtFixedRate(this, LOCAL_GLOBAL_SYNC_TIME_INTERVAL, 
                    LOCAL_GLOBAL_SYNC_TIME_INTERVAL);
        }
    }
    
    public AnalyticsReceiverIndexingFlowControlConfiguration getConfig() {
        return config;
    }
    
    public void receive(int recordCount) {
        if (!this.getConfig().isEnabled()) {
            return;
        }
        this.checkLocalCount(this.localCount.addAndGet(recordCount));
        if (this.isThrottled()) {
            this.throttleWait();
        }
    }
    
    private void throttleWait() {
        int localVal = this.throttlePosition - 1;
        if (localVal >= 0) {
            try {
                Thread.sleep((int) Math.pow(2, localVal));
                System.out.println("** SLEEP: " + (int) Math.pow(2, localVal));
            } catch (InterruptedException ignore) { /* ignore */  }
        }
    }
    
    private void checkLocalCount(int count) {
        if (count > LOCAL_GLOBAL_UPDATE_THRESHOLD || count < -LOCAL_GLOBAL_UPDATE_THRESHOLD) {
            this.syncLocalWithGlobalCount(count);
        }
    }
    
    private void syncLocalWithGlobalCount(int count) {
        long updatedGlobalCount = this.globalCount.addAndGet(count);
        if (updatedGlobalCount < 0) {
            this.globalCount.set(0);
        }
        this.localCount.addAndGet(-count);
        this.localGlobalCountLastSyncTime = System.currentTimeMillis();
        this.checkGlobalCountForThrottlingAdjustment();
    }
    
    private boolean lastThrottlingActionStabilizationIntervalExpired() {
        return (System.currentTimeMillis() - this.lastThrottlingActionTimestamp) > THROTTLING_ACTION_STABILIZE_INTERVAL;
    }
    
    private void throttleUp() {
        this.throttlePosition++;
        this.lastThrottlingActionTimestamp = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Analytics Receiver Indexing Flow Control Throttle Up: " + this.throttlePosition);
        }
        System.out.println("\n\n******* Analytics Receiver Indexing Flow Control Throttle Up: " + this.throttlePosition + "\n\n");
    }
    
    private void throttleDown() {
        this.throttlePosition--;
        this.lastThrottlingActionTimestamp = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Analytics Receiver Indexing Flow Control Throttle Down: " + this.throttlePosition);
        }
        System.out.println("\n\n******* Analytics Receiver Indexing Flow Control Throttle Down: " + this.throttlePosition + "\n\n");
    }
    
    private boolean isThrottled() {
        return this.throttlePosition > 0;
    }
    
    private synchronized void checkGlobalCountForThrottlingAdjustment() {
        long count = this.globalCount.get();
        if (count > this.getConfig().getRecordReceivingHighThreshold()) {
            if (this.lastThrottlingActionStabilizationIntervalExpired()) {
                this.throttleUp();
            }
        } else if (this.isThrottled() && count < this.getConfig().getRecordReceivingLowThreshold()) {
            if (this.lastThrottlingActionStabilizationIntervalExpired()) {
                this.throttleDown();
            }
        }
    }
    
    public void processed(int recordCount) {
        if (!this.getConfig().isEnabled()) {
            return;
        }
        this.checkLocalCount(this.localCount.addAndGet(-recordCount));
    }

    @Override
    public void run() {
        if ((System.currentTimeMillis() - this.localGlobalCountLastSyncTime) < 
                (LOCAL_GLOBAL_SYNC_TIME_INTERVAL - SYNC_TIME_EDGE_INTERVAL)) {
            return;
        }
        this.syncLocalWithGlobalCount(this.localCount.get());
    }
    
    public void stop() {
        if (this.localGlobalSyncTimer != null) {
            this.localGlobalSyncTimer.cancel();
        }
    }
    
}
