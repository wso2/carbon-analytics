/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.si.metrics.core;

import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.si.metrics.core.internal.MetricsDataHolder;
import org.wso2.carbon.si.metrics.core.internal.MetricsManagement;
import org.wso2.carbon.si.metrics.core.internal.MetricsManager;
import io.siddhi.core.util.statistics.BufferedEventsTracker;
import io.siddhi.core.util.statistics.LatencyTracker;
import io.siddhi.core.util.statistics.MemoryUsageTracker;
import io.siddhi.core.util.statistics.StatisticsManager;
import io.siddhi.core.util.statistics.StatisticsTrackerFactory;
import io.siddhi.core.util.statistics.ThroughputTracker;
import io.siddhi.query.api.annotation.Element;

import java.util.List;

/**
 * Factory to retrieve required metric tracker.
 */
public class MetricsFactory implements StatisticsTrackerFactory {
    private MetricService metricService;
    private MetricsManagement metricsManagement;

    public MetricsFactory() {
        this.metricService = MetricsDataHolder.getInstance().getMetricService();
        this.metricsManagement = MetricsManagement.getInstance();
    }

    public LatencyTracker createLatencyTracker(String name, StatisticsManager statisticsManager) {
        MetricsManager metricsManager = (MetricsManager) statisticsManager;
        LatencyMetric latencyMetric = new LatencyMetric(name, this.metricService);
        this.metricsManagement.addComponent(metricsManager.getComponentName(), latencyMetric.getName());
        return latencyMetric;
    }

    public ThroughputTracker createThroughputTracker(String name, StatisticsManager statisticsManager) {
        MetricsManager metricsManager = (MetricsManager) statisticsManager;
        ThroughputMetric throughputMetric = new ThroughputMetric(name, this.metricService);
        this.metricsManagement.addComponent(metricsManager.getComponentName(),
                throughputMetric.getName());
        return throughputMetric;
    }

    public BufferedEventsTracker createBufferSizeTracker(StatisticsManager statisticsManager) {
        MetricsManager metricsManager = (MetricsManager) statisticsManager;
        return new BufferedEventsMetric(this.metricService, metricsManager.getComponentName());
    }

    public MemoryUsageTracker createMemoryUsageTracker(StatisticsManager statisticsManager) {
        MetricsManager metricsManager = (MetricsManager) statisticsManager;
        return new MemoryUsageMetric(this.metricService, metricsManager.getComponentName());
    }

    @Override
    public StatisticsManager createStatisticsManager(String prefix, String componentName, List<Element> elements) {
        return new MetricsManager(componentName);
    }
}
