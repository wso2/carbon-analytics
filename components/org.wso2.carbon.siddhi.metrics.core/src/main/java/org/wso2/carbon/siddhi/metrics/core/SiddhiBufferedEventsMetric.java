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
import org.wso2.siddhi.core.util.statistics.BufferedEventsTracker;
import org.wso2.siddhi.core.util.statistics.EventBufferHolder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.wso2.carbon.metrics.core.Level.INFO;
import static org.wso2.carbon.metrics.core.Level.OFF;

/**
 * Siddhi Memory usage MMetrics Tracker.
 */
public class SiddhiBufferedEventsMetric implements BufferedEventsTracker {
    private ConcurrentMap<Object, ObjectMetric> registeredObjects = new ConcurrentHashMap<Object, ObjectMetric>();
    private MetricService metricService;
    private String siddhiAppName;
    private boolean statisticEnabled;
    
    public SiddhiBufferedEventsMetric(MetricService metricService, String siddhiAppName, boolean isStatisticEnabled) {
        this.metricService = metricService;
        this.siddhiAppName = siddhiAppName;
        this.statisticEnabled = isStatisticEnabled;
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
            SiddhiMetricsManagement.getInstance().addComponent(siddhiAppName, bufferedEventsTrackerId);
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
        private final EventBufferHolder eventBufferHolder;
        private String name;
        
        public ObjectMetric(final EventBufferHolder eventBufferHolder, String name) {
            this.eventBufferHolder = eventBufferHolder;
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
                                if (eventBufferHolder != null) {
                                    return eventBufferHolder.getBufferedEvents();
                                } else {
                                    return 0L;
                                }
                            } catch (Throwable e) {
                                return 0L;
                            }
                        }
                    });
            if (statisticEnabled) {
                SiddhiMetricsDataHolder.getInstance().getMetricManagementService().setMetricLevel(name, INFO);
            } else {
                SiddhiMetricsDataHolder.getInstance().getMetricManagementService().setMetricLevel(name, OFF);
            }
        }
    }
}
