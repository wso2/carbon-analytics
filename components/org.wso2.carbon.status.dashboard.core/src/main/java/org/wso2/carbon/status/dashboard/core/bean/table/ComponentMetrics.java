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

package org.wso2.carbon.status.dashboard.core.bean.table;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Siddhi app Component Bean Class.
 */
public class ComponentMetrics {
    private String name;
    private String totalEvents;
    private List<MetricElement> metrics = new ArrayList<>();
    
    public ComponentMetrics() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<MetricElement> getMetrics() {
        return metrics;
    }
    
    public void addMetrics(MetricElement metricsEle) {
        boolean isNew = true;
        for (MetricElement metricElement : metrics) {
            if (metricElement.getType().equalsIgnoreCase(metricsEle.getType())) {
                isNew = false;
                metricElement.setAttributes(metricsEle.getAttribute());
            }
        }
        if (isNew) {
            this.metrics.add(metricsEle);
        }
    }
    
    public String getTotalEvents() {
        return totalEvents;
    }
    
    public void setTotalEvents(long totalEvents) {
        this.totalEvents = NumberFormat.getNumberInstance().format(totalEvents);
    }
}
