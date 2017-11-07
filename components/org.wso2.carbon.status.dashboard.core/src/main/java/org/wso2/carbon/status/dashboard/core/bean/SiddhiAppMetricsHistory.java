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
package org.wso2.carbon.status.dashboard.core.bean;

import java.util.List;

/**
 * Model for siddhi app overview data.
 */
public class SiddhiAppMetricsHistory {
    private MetricsLineCharts latency;
    private MetricsLineCharts memory;
    private MetricsLineCharts throughput;


    public SiddhiAppMetricsHistory() {

    }
    public SiddhiAppMetricsHistory(String appName) {
        latency = new MetricsLineCharts();
        latency.setDataLabels(new String[]{"Timestamp", "Latency"});
        memory = new MetricsLineCharts();
        memory.setDataLabels(new String[]{"Timestamp", "Memory"});
        throughput = new MetricsLineCharts();
        throughput.setDataLabels(new String[]{"Timestamp", "Throughput"});
    }

    public MetricsLineCharts getLatency() {
        return latency;
    }

    public void setLatency(List<List<Object>> latencyData) {
        this.latency.setData(latencyData);
    }

    public MetricsLineCharts getMemory() {
        return memory;
    }

    public void setMemory(List<List<Object>> memoryData) {
        this.memory.setData(memoryData);
    }

    public MetricsLineCharts getThroughput() {
        return throughput;
    }

    public void setThroughput(List<List<Object>>  throughputData) {
        this.throughput.setData(throughputData);
    }

}
