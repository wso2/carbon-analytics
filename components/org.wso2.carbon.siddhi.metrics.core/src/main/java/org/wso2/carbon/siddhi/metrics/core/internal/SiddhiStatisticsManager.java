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

import org.wso2.siddhi.core.util.statistics.StatisticsManager;

/**
 * Functionality of SiddhiStatisticsManager is not required to be implemented,
 * since the reporting will be handled according the Carbon Metrics configuration.
 */
public class SiddhiStatisticsManager implements StatisticsManager {
    private String siddhiAppName;
    private SiddhiMetricsManagement metricsManagement;
    private boolean isStatisticEnabled;
    public SiddhiStatisticsManager(String siddhiAppName,boolean isStatisticEnabled) {
        this.siddhiAppName = siddhiAppName;
        this.metricsManagement = SiddhiMetricsManagement.getInstance();
        this.isStatisticEnabled = isStatisticEnabled;
    }

    @Override
    public void startReporting() {
        this.metricsManagement.startMetrics(siddhiAppName);
    }

    @Override
    public void stopReporting() {
        this.metricsManagement.stopMetrics(siddhiAppName);
    }

    @Override
    public void cleanup() {
        this.metricsManagement.cleanUpMetrics(siddhiAppName);
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    public boolean isStatisticEnabled() {
        return isStatisticEnabled;
    }
}
