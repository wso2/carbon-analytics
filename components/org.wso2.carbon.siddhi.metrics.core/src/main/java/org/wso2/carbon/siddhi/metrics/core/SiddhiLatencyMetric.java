/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.metrics.core;

import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.carbon.siddhi.metrics.core.internal.SiddhiMetricsDataHolder;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;

import static org.wso2.carbon.metrics.core.Level.INFO;
import static org.wso2.carbon.metrics.core.Level.OFF;

/**
 * Siddhi Latency metrics tracker.
 */
public class SiddhiLatencyMetric implements LatencyTracker {
    // Using thread local variables to keep the timer track
    // the time of the same execution path by different threads.
    private static final String METRIC_SUFFIX_LATENCY = "";
    private ThreadLocal<Timer> execLatencyTimer;
    private ThreadLocal<Timer.Context> context;
    private String latencyTrackerId;

    public SiddhiLatencyMetric(String name, MetricService metricService, boolean isStatisticEnabled) {
        this.latencyTrackerId = MetricService.name(name, METRIC_SUFFIX_LATENCY);
        Timer timer = metricService.timer(this.latencyTrackerId, INFO);
        SiddhiMetricsDataHolder.getInstance().getMetricManagementService().setMetricLevel(latencyTrackerId, OFF);
        execLatencyTimer = new ThreadLocal<Timer>() {
            protected Timer initialValue() {
            if (isStatisticEnabled) {
                SiddhiMetricsDataHolder.getInstance().getMetricManagementService().setMetricLevel(latencyTrackerId,
                        INFO);
            } else {
                SiddhiMetricsDataHolder.getInstance().getMetricManagementService().setMetricLevel(latencyTrackerId,
                        OFF);
            }
                return timer;
            }
        };
        context = new ThreadLocal<Timer.Context>() {
            protected Timer.Context initialValue() {
                return null;
            }
        };

    }

    /**
     * This is called when the processing of the event is started. This is called at
     * ProcessStreamReceiver#receive before the event is passed into process chain.
     */
    public void markIn() {
        if (context.get() != null) {
            throw new IllegalStateException("MarkIn consecutively called without calling markOut in " +
                    this.latencyTrackerId);
        }
        context.set(execLatencyTimer.get().start());
    }

    /**
     * This is called to when the processing of an event is finished. This is called at two places,
     * 1. OutputRateLimiter#sendToCallBacks - When the event is processed and by the full chain and emitted out.
     * 2. ProcessStreamReceiver#receive - When event is not processed by full process
     * chain(e.g. Filtered out by a filter).
     */
    @Override
    public void markOut() {
        if (context.get() != null) {
            context.get().stop();
            context.set(null);
        }
    }

    /**
     * @return Name of the latency tracker.
     */
    @Override
    public String getName() {
        return latencyTrackerId;
    }

}
