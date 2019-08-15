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

import io.siddhi.core.util.statistics.StatisticsManager;

/**
 * Functionality of SPStatisticsManager is not required to be implemented,
 * since the reporting will be handled according the Carbon Metrics configuration.
 */
public class SPStatisticsManager implements StatisticsManager {
    private String componentName;
    private SPMetricsManagement metricsManagement;

    public SPStatisticsManager(String componentName) {
        this.componentName = componentName;
        this.metricsManagement = SPMetricsManagement.getInstance();
    }
    
    @Override
    public void startReporting() {
        this.metricsManagement.startMetrics(componentName);
    }
    
    @Override
    public void stopReporting() {
        this.metricsManagement.stopMetrics(componentName);
    }
    
    @Override
    public void cleanup() {
        this.metricsManagement.cleanUpMetrics(componentName);
    }
    
    public String getComponentName() {
        return componentName;
    }

}
