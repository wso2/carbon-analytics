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
import org.wso2.carbon.siddhi.metrics.core.internal.SiddhiMetricsDataHolder;
import org.wso2.carbon.siddhi.metrics.core.internal.SiddhiMetricsManagement;
import org.wso2.carbon.siddhi.metrics.core.internal.SiddhiStatisticsManager;
import org.wso2.siddhi.core.util.statistics.*;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.List;

/**
 * Factory to retrieve required metric tracker.
 */
public class SiddhiMetricsFactory implements StatisticsTrackerFactory {
    private MetricService metricService;
    private SiddhiMetricsManagement metricsManagement;

    public SiddhiMetricsFactory() {
        this.metricService = SiddhiMetricsDataHolder.getInstance().getMetricService();
        this.metricsManagement = SiddhiMetricsManagement.getInstance();
    }

    public LatencyTracker createLatencyTracker(String name, StatisticsManager statisticsManager) {
        SiddhiStatisticsManager siddhiStatisticsManager = (SiddhiStatisticsManager) statisticsManager;
        SiddhiLatencyMetric siddhiLatencyMetric = new SiddhiLatencyMetric(name, this.metricService,
                siddhiStatisticsManager.isStatisticEnabled());
        this.metricsManagement.addComponent(siddhiStatisticsManager.getSiddhiAppName(), siddhiLatencyMetric.getName());
        return siddhiLatencyMetric;
    }

    public ThroughputTracker createThroughputTracker(String name, StatisticsManager statisticsManager) {
        SiddhiStatisticsManager siddhiStatisticsManager = (SiddhiStatisticsManager) statisticsManager;
        SiddhiThroughputMetric siddhiThroughputMetric = new SiddhiThroughputMetric(name, this.metricService,
                siddhiStatisticsManager.isStatisticEnabled());
        this.metricsManagement.addComponent(siddhiStatisticsManager.getSiddhiAppName(),
                siddhiThroughputMetric.getName());
        return siddhiThroughputMetric;
    }

    @Override
    public BufferedEventsTracker createBufferSizeTracker(StatisticsManager statisticsManager) {
        return null;
    }

    public MemoryUsageTracker createMemoryUsageTracker(StatisticsManager statisticsManager) {
        SiddhiStatisticsManager siddhiStatisticsManager = (SiddhiStatisticsManager) statisticsManager;
        return new SiddhiMemoryUsageMetric(this.metricService, siddhiStatisticsManager.getSiddhiAppName(),
                siddhiStatisticsManager.isStatisticEnabled());
    }

    @Override
    public StatisticsManager createStatisticsManager(String prefix, String siddhiAppName, List<Element> elements) {
        if(elements.size() > 0) {
            return new SiddhiStatisticsManager(siddhiAppName, true);
        } else {
            return new SiddhiStatisticsManager(siddhiAppName, false);
        }
    }
}
