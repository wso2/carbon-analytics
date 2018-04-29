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
package org.wso2.carbon.siddhi.metrics.core.internal;

import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.metrics.core.Level.INFO;
import static org.wso2.carbon.metrics.core.Level.OFF;

/**
 * Manages the statistics management functions.
 */
public class SiddhiMetricsManagement {
    private Map<String, List<String>> componentMap;
    private MetricManagementService metricManagementService;
    private MetricService metricService;
    private static SiddhiMetricsManagement instance = new SiddhiMetricsManagement();
    
    private SiddhiMetricsManagement() {
        metricManagementService = SiddhiMetricsDataHolder.getInstance().getMetricManagementService();
        metricService = SiddhiMetricsDataHolder.getInstance().getMetricService();
        componentMap = new HashMap<>();
    }
    
    public static SiddhiMetricsManagement getInstance() {
        return instance;
    }
    
    public void addComponent(String siddhiAppName, String componentMetricsName) {
        List<String> registeredComponent = componentMap.get(siddhiAppName);
        if (registeredComponent == null) {
            List<String> newComponentAppList = new ArrayList<>();
            newComponentAppList.add(componentMetricsName);
            componentMap.put(siddhiAppName, newComponentAppList);
        } else {
            componentMap.get(siddhiAppName).add(componentMetricsName);
        }
    }
    
    public void startMetrics(String siddhiAppName) {
        List<String> registeredComponent = componentMap.get(siddhiAppName);
        if (registeredComponent != null) {
            for (String component : registeredComponent) {
                this.metricManagementService.setMetricLevel(component, INFO);
            }
        }
    }
    
    public void stopMetrics(String siddhiAppName) {
        List<String> registeredComponents = componentMap.get(siddhiAppName);
        for (String component : registeredComponents) {
            this.metricManagementService.setMetricLevel(component, OFF);
        }
        
    }
    
    public void cleanUpMetrics(String siddhiAppName) {
        List<String> registeredComponents = componentMap.get(siddhiAppName);
        for (String component : registeredComponents) {
            metricService.remove(component);
        }
        componentMap.remove(siddhiAppName);
    }
}
