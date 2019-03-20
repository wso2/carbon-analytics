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
package org.wso2.carbon.sp.metrics.core;

import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.sp.metrics.core.internal.SPMetricsDataHolder;
import org.wso2.carbon.sp.metrics.core.internal.SPMetricsManagement;
import org.wso2.carbon.sp.metrics.core.internal.SPStatisticsManager;
import org.wso2.siddhi.core.util.statistics.BufferedEventsTracker;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.util.statistics.MemoryUsageTracker;
import org.wso2.siddhi.core.util.statistics.StatisticsManager;
import org.wso2.siddhi.core.util.statistics.StatisticsTrackerFactory;
import org.wso2.siddhi.core.util.statistics.ThroughputTracker;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.List;

/**
 * Factory to retrieve required metric tracker.
 */
public class SPMetricsFactory implements StatisticsTrackerFactory {
    private MetricService metricService;
    private SPMetricsManagement metricsManagement;
    
    public SPMetricsFactory() {
        this.metricService = SPMetricsDataHolder.getInstance().getMetricService();
        this.metricsManagement = SPMetricsManagement.getInstance();
    }
    
    public LatencyTracker createLatencyTracker(String name, StatisticsManager statisticsManager) {
        SPStatisticsManager SPStatisticsManager = (SPStatisticsManager) statisticsManager;
        SPLatencyMetric SPLatencyMetric = new SPLatencyMetric(name, this.metricService);
        this.metricsManagement.addComponent(SPStatisticsManager.getComponentName(), SPLatencyMetric.getName());
        return SPLatencyMetric;
    }
    
    public ThroughputTracker createThroughputTracker(String name, StatisticsManager statisticsManager) {
        SPStatisticsManager SPStatisticsManager = (SPStatisticsManager) statisticsManager;
        SPThroughputMetric SPThroughputMetric = new SPThroughputMetric(name, this.metricService);
        this.metricsManagement.addComponent(SPStatisticsManager.getComponentName(),
                SPThroughputMetric.getName());
        return SPThroughputMetric;
    }
    
    public BufferedEventsTracker createBufferSizeTracker(StatisticsManager statisticsManager) {
        SPStatisticsManager SPStatisticsManager = (SPStatisticsManager) statisticsManager;
        return new SPBufferedEventsMetric(this.metricService, SPStatisticsManager.getComponentName());
    }
    
    public MemoryUsageTracker createMemoryUsageTracker(StatisticsManager statisticsManager) {
        SPStatisticsManager SPStatisticsManager = (SPStatisticsManager) statisticsManager;
        return new SPMemoryUsageMetric(this.metricService, SPStatisticsManager.getComponentName());
    }

    @Override
    public StatisticsManager createStatisticsManager(String prefix, String componentName, List<Element> elements) {
        return new SPStatisticsManager(componentName);
    }
}
