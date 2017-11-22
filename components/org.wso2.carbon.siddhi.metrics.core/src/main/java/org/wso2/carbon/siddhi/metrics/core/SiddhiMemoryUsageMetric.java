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

import org.wso2.carbon.metrics.core.Gauge;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.siddhi.metrics.core.internal.SiddhiMetricsDataHolder;
import org.wso2.carbon.siddhi.metrics.core.internal.SiddhiMetricsManagement;
import org.wso2.siddhi.core.util.statistics.MemoryUsageTracker;
import org.wso2.siddhi.core.util.statistics.memory.ObjectSizeCalculator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.wso2.carbon.metrics.core.Level.INFO;
import static org.wso2.carbon.metrics.core.Level.OFF;

/**
 * Siddhi Memory usage MMetrics Tracker.
 */
public class SiddhiMemoryUsageMetric implements MemoryUsageTracker {
    private static final String METRIC_SUFFIX_MEMORY = "";
    private ConcurrentMap<Object, ObjectMetric> registeredObjects = new ConcurrentHashMap<Object, ObjectMetric>();
    private MetricService metricService;
    private String siddhiAppName;
    private boolean statisticEnabled;

    public SiddhiMemoryUsageMetric(MetricService metricService, String siddhiAppName,boolean isStatisticEnabled) {
        this.metricService = metricService;
        this.siddhiAppName = siddhiAppName;
        this.statisticEnabled = isStatisticEnabled;
    }

    /**
     * Register the object that needs to be measured the memory usage.
     *
     * @param object          Object.
     * @param memoryTrackerId An unique value to identify the object.
     */
    @Override
    public void registerObject(Object object, String memoryTrackerId) {
        if (registeredObjects.get(object) == null) {
            memoryTrackerId = MetricService.name(memoryTrackerId, METRIC_SUFFIX_MEMORY);
            registeredObjects.put(object, new ObjectMetric(object, memoryTrackerId));
            SiddhiMetricsManagement.getInstance().addComponent(siddhiAppName, memoryTrackerId);
        }
    }

    /**
     * @return Name of the memory usage tracker.
     */
    @Override
    public String getName(Object object) {
        if (registeredObjects.get(object) != null) {
            return registeredObjects.get(object).getName();
        } else {
            return null;
        }
    }

    class ObjectMetric {
        private final Object object;
        private String name;

        public ObjectMetric(final Object object, String name) {
            this.object = object;
            this.name = name;
            initMetric();
        }

        public String getName() {
            return name;
        }

        private void initMetric() {
                metricService.gauge(
                        name,
                        Level.OFF,
                        new Gauge<Long>() {
                            @Override
                            public Long getValue() {
                                try {
                                    return ObjectSizeCalculator.getObjectSize(object);
                                } catch (UnsupportedOperationException e) {
                                    return 0L;
                                }
                            }
                        });
            if(statisticEnabled) {
                SiddhiMetricsDataHolder.getInstance().getMetricManagementService().setMetricLevel(name, INFO);
            } else {
                SiddhiMetricsDataHolder.getInstance().getMetricManagementService().setMetricLevel(name, OFF);
            }
        }
    }
}
