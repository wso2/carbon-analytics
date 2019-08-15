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

import org.wso2.carbon.metrics.core.Gauge;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.si.metrics.core.internal.SPMetricsDataHolder;
import org.wso2.carbon.si.metrics.core.internal.SPMetricsManagement;
import io.siddhi.core.util.statistics.MemoryUsageTracker;
import io.siddhi.core.util.statistics.memory.ObjectSizeCalculator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.wso2.carbon.metrics.core.Level.OFF;

/**
 * Siddhi Memory usage MMetrics Tracker.
 */
public class SPMemoryUsageMetric implements MemoryUsageTracker {
    private ConcurrentMap<Object, ObjectMetric> registeredObjects = new ConcurrentHashMap<Object, ObjectMetric>();
    private MetricService metricService;
    private String siddhiAppName;

    public SPMemoryUsageMetric(MetricService metricService, String siddhiAppName) {
        this.metricService = metricService;
        this.siddhiAppName = siddhiAppName;
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
            registeredObjects.put(object, new ObjectMetric(object, memoryTrackerId));
            SPMetricsManagement.getInstance().addComponent(siddhiAppName, memoryTrackerId);
        }
    }

    @Override
    public void enableMemoryUsageMetrics() {
        for (ConcurrentMap.Entry<Object, ObjectMetric> entry :
                registeredObjects.entrySet()) {
            metricService.gauge(entry.getValue().getName(), Level.INFO,  entry.getValue().getGauge());
        }
    }

    @Override
    public void disableMemoryUsageMetrics() {
        for (ConcurrentMap.Entry<Object, ObjectMetric> entry :
                registeredObjects.entrySet()) {
            metricService.remove(entry.getValue().getName());
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
        private String name;
        private Gauge<Long> gauge;

        public ObjectMetric(final Object object, String name) {
            this.name = name;
            this.gauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    try {
                        return ObjectSizeCalculator.getObjectSize(object);
                    } catch (UnsupportedOperationException e) {
                        return 0L;
                    }
                }
            };
            metricService.gauge(name, Level.INFO, gauge);
        }

        public String getName() {
            return name;
        }

        public Gauge<Long> getGauge() {
            return gauge;
        }
    }
}
