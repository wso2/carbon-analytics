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
import org.wso2.carbon.si.metrics.core.internal.MetricsManagement;
import io.siddhi.core.util.statistics.BufferedEventsTracker;
import io.siddhi.core.util.statistics.EventBufferHolder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Siddhi Memory usage MMetrics Tracker.
 */
public class BufferedEventsMetric implements BufferedEventsTracker {
    private ConcurrentMap<Object, ObjectMetric> registeredObjects = new ConcurrentHashMap<Object, ObjectMetric>();
    private MetricService metricService;
    private String siddhiAppName;

    public BufferedEventsMetric(MetricService metricService, String siddhiAppName) {
        this.metricService = metricService;
        this.siddhiAppName = siddhiAppName;
    }

    /**
     * Register the object that needs to be measured the buffered events count usage.
     *
     * @param eventBufferHolder Buffered object.
     * @parambufferedEventsTrackerId An unique value to identify the  Buffered object.
     */
    @Override
    public void registerEventBufferHolder(EventBufferHolder eventBufferHolder, String bufferedEventsTrackerId) {
        if (registeredObjects.get(eventBufferHolder) == null) {
            registeredObjects.put(eventBufferHolder, new ObjectMetric(eventBufferHolder, bufferedEventsTrackerId));
            MetricsManagement.getInstance().addComponent(siddhiAppName, bufferedEventsTrackerId);
        }
    }

    @Override
    public void enableEventBufferHolderMetrics() {
        for (ConcurrentMap.Entry<Object, ObjectMetric> entry :
                registeredObjects.entrySet()) {
            metricService.gauge(entry.getValue().getName(), Level.INFO,  entry.getValue().getGauge());
        }
    }

    @Override
    public void disableEventBufferHolderMetrics() {
        for (ConcurrentMap.Entry<Object, ObjectMetric> entry :
                registeredObjects.entrySet()) {
            metricService.remove(entry.getValue().getName());
        }
    }

    /**
     * @return Name of the buffered event tracker.
     */
    @Override
    public String getName(EventBufferHolder eventBufferHolder) {
        if (registeredObjects.get(eventBufferHolder) != null) {
            return registeredObjects.get(eventBufferHolder).getName();
        } else {
            return null;
        }
    }

    class ObjectMetric {
        private String name;
        private Gauge<Long> gauge;
        public ObjectMetric(final EventBufferHolder eventBufferHolder, String name) {
            this.name = name;
            this.gauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    try {
                        if (eventBufferHolder != null) {
                            return eventBufferHolder.getBufferedEvents();
                        } else {
                            return 0L;
                        }
                    } catch (Throwable e) {
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
