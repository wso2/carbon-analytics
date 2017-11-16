/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.metrics.core.internal;

import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;

/**
 * Class which holds the OSGI Service references.
 */
public class SiddhiMetricsDataHolder {
    private static SiddhiMetricsDataHolder instance = new SiddhiMetricsDataHolder();

    private static MetricService metricService;
    private static MetricManagementService metricManagementService;

    private SiddhiMetricsDataHolder() {

    }

    /**
     * This returns the StreamProcessorDataHolder instance.
     * @return The StreamProcessorDataHolder instance of this singleton class
     */
    public static SiddhiMetricsDataHolder getInstance() {
        return instance;
    }

    /**
     * Store the wso2 carbon metrics service.
     * @return return metrics service.
     */
    public MetricService getMetricService() {
        return metricService;
    }

    /**
     * Set the metrics service.
     * @param metricService wso2.carbon.metrics service
     */
    public void setMetricService(MetricService metricService) {
        SiddhiMetricsDataHolder.metricService = metricService;
    }

    /**
     * Return the metrics management service.
     * @return the wso2 carbon metrics management service.
     */
    public MetricManagementService getMetricManagementService() {
        return metricManagementService;
    }

    /**
     * Set the metrics managemet service for metrics management purpose such as enable disable metrics
     * @param metricManagementService wso2.carbon.metrics
     */
    public void setMetricManagementService(MetricManagementService metricManagementService) {
        SiddhiMetricsDataHolder.metricManagementService = metricManagementService;
    }
}
