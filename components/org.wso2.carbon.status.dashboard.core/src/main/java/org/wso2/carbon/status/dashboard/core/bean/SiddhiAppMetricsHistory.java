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

import java.text.NumberFormat;
import java.util.List;

/**
 * Model for siddhi app overview data.
 */
public class SiddhiAppMetricsHistory {
    private MetricsLineCharts latency;
    private MetricsLineCharts memory;
    private MetricsLineCharts throughput;
    private String latencyRecent;
    private String memoryRecent;
    private String throughputRecent;

    public SiddhiAppMetricsHistory() {
    }

    public SiddhiAppMetricsHistory(String appName) {
        latency = new MetricsLineCharts();
        memory = new MetricsLineCharts();
        throughput = new MetricsLineCharts();
    }

    public MetricsLineCharts getLatency() {
        return latency;
    }

    public void setLatency(List<List<Object>> latencyData) {
        this.latency.setData(latencyData);
        setLatencyRecent(latencyData);
    }

    public MetricsLineCharts getMemory() {
        return memory;
    }

    public void setMemory(List<List<Object>> memoryData) {
        this.memory.setData(memoryData);
        setMemoryRecent(memoryData);
    }

    public MetricsLineCharts getThroughput() {
        return throughput;
    }

    public void setThroughput(List<List<Object>> throughputData) {
        this.throughput.setData(throughputData);
        setThroughputRecent(throughputData);
    }

    public String getLatencyRecent() {
        return latencyRecent;
    }

    public void setLatencyRecent(List<List<Object>> latency) {
        if ((latency != null) && (!latency.isEmpty())) {
            this.latencyRecent = NumberFormat.getIntegerInstance().format((latency.get(latency.size() - 1)).get(1));
        } else {
            latencyRecent = "0";
        }
    }

    public String getMemoryRecent() {
        return memoryRecent;
    }

    public void setMemoryRecent(List<List<Object>> memory) {
        if ((memory != null) && (!memory.isEmpty())) {
            this.memoryRecent = humanReadableByteCount(((double) (memory.get(memory.size() - 1))
                    .get(1)), true);
        } else {
            memoryRecent = "0";
        }
    }

    public String getThroughputRecent() {
        return throughputRecent;
    }

    public void setThroughputRecent(List<List<Object>> throughput) {
        if ((throughput != null) && (!throughput.isEmpty())) {
            this.throughputRecent = NumberFormat.getIntegerInstance().format((throughput.get(throughput.size
                    () - 1)).get(1));
        } else {
            throughputRecent = "0";
        }
    }

    public static String humanReadableByteCount(double bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
