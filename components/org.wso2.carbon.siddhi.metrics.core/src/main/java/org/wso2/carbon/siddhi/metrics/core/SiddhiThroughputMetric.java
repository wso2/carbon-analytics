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

import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.siddhi.metrics.core.internal.SiddhiMetricsDataHolder;
import org.wso2.siddhi.core.util.statistics.ThroughputTracker;

import static org.wso2.carbon.metrics.core.Level.INFO;
import static org.wso2.carbon.metrics.core.Level.OFF;

/**
 * Siddhi throughput metric tracker.
 */
public class SiddhiThroughputMetric implements ThroughputTracker {
    private Meter eventMeter = null;
    private String throughputTrackerId;
    
    public SiddhiThroughputMetric(String throughputTrackerId, MetricService metricService, boolean isStatisticEnabled) {
        this.throughputTrackerId = throughputTrackerId;
        eventMeter = metricService.meter(this.throughputTrackerId, Level.INFO);
        if (isStatisticEnabled) {
            SiddhiMetricsDataHolder.getInstance().getMetricManagementService().setMetricLevel(this
                    .throughputTrackerId, INFO);
        } else {
            SiddhiMetricsDataHolder.getInstance().getMetricManagementService().setMetricLevel(this
                    .throughputTrackerId, OFF);
        }
    }
    
    /**
     * This method is to notify receive of events to calculate the throughput.
     */
    @Override
    public void eventIn() {
        eventMeter.mark();
    }
    
    /**
     * This method is to notify receive of events to calculate the throughput.
     *
     * @param eventCount events passed through.
     */
    @Override
    public void eventsIn(int eventCount) {
        eventMeter.mark(eventCount);
    }
    
    /**
     * @return Name of the memory usage tracker.
     */
    @Override
    public String getName() {
        return throughputTrackerId;
    }
    
}
