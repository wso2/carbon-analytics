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
            for (String component:registeredComponent) {
                this.metricManagementService.setMetricLevel(component, INFO);
            }
        }
    }

    public void stopMetrics(String siddhiAppName) {
        List<String> registeredComponents = componentMap.get(siddhiAppName);
        for (String component:registeredComponents) {
            this.metricManagementService.setMetricLevel(component, OFF);
        }

    }

    public void cleanUpMetrics(String siddhiAppName) {
        List<String> registeredComponents = componentMap.get(siddhiAppName);
        for (String component:registeredComponents) {
            metricService.remove(component);
        }
        componentMap.remove(siddhiAppName);
    }
}
