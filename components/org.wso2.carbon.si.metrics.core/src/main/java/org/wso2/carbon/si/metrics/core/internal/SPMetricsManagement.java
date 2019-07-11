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
package org.wso2.carbon.si.metrics.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SPMetricsManagement {
    private static final Logger log = LoggerFactory.getLogger(SPMetricsManagement.class);

    private Map<String, List<String>> componentMap;
    private MetricManagementService metricManagementService;
    private MetricService metricService;
    private static SPMetricsManagement instance = new SPMetricsManagement();

    private SPMetricsManagement() {
        metricManagementService = SPMetricsDataHolder.getInstance().getMetricManagementService();
        metricService = SPMetricsDataHolder.getInstance().getMetricService();
        componentMap = new HashMap<>();
    }

    public static SPMetricsManagement getInstance() {
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
                try {
                    this.metricManagementService.setMetricLevel(component, INFO);
                } catch (IllegalArgumentException e) {
                    // Error: given metric is not available. Do nothing!
                    log.debug("Invalid metric name: " + component, e);
                }
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
        if (componentMap.containsKey(siddhiAppName)) {
            List<String> registeredComponents = componentMap.get(siddhiAppName);
            for (String component : registeredComponents) {
                metricService.remove(component);
            }
            componentMap.remove(siddhiAppName);
        }
    }
}
