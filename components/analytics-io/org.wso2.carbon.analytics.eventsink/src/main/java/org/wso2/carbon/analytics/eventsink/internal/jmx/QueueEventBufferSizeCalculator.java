/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.eventsink.internal.jmx;

import org.wso2.carbon.analytics.eventsink.internal.queue.AnalyticsEventQueue;
import org.wso2.carbon.analytics.eventsink.internal.queue.AnalyticsEventQueueManager;

/**
 * Implementation of the QueueEventBufferSizeCalculatorMBean interface.
 */
public class QueueEventBufferSizeCalculator implements QueueEventBufferSizeCalculatorMBean {
    @Override
    public long getRemainingBufferCapacityInBytes(int tenantId) {
        long remainingCapacity = -1;
        AnalyticsEventQueueManager eventQueueManager = AnalyticsEventQueueManager.getInstance();
        AnalyticsEventQueue analyticsEventQueue = eventQueueManager.getAnalyticsEventQueue(tenantId);
        if (analyticsEventQueue != null) {
            remainingCapacity = analyticsEventQueue.getRemainingBufferCapacity();
        }
        return remainingCapacity;
    }

    @Override
    public int getRemainingQueueSize(int tenantId) {
        int currentSize = -1;
        AnalyticsEventQueueManager eventQueueManager = AnalyticsEventQueueManager.getInstance();
        AnalyticsEventQueue analyticsEventQueue = eventQueueManager.getAnalyticsEventQueue(tenantId);
        if (analyticsEventQueue != null) {
            currentSize = analyticsEventQueue.getRemainingQueueSize();
        }
        return currentSize;
    }
}
