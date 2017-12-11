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
 * Model for siddhi app specific metrics.
 */
public class WorkerMetricsHistory {
    private MetricsLineCharts processCPU;
    private MetricsLineCharts systemCPU;
    private MetricsLineCharts usedMemory;
    private MetricsLineCharts committedMemory;
    private MetricsLineCharts initMemory;
    private MetricsLineCharts totalMemory;
    private MetricsLineCharts loadAverage;
    private MetricsLineCharts throughput;

    public WorkerMetricsHistory() {
        processCPU = new MetricsLineCharts();
        systemCPU = new MetricsLineCharts();
        usedMemory = new MetricsLineCharts();
        totalMemory = new MetricsLineCharts();
        initMemory = new MetricsLineCharts();
        committedMemory = new MetricsLineCharts();
        loadAverage = new MetricsLineCharts();
        throughput = new MetricsLineCharts();
    }

    public MetricsLineCharts getProcessCPU() {
        return processCPU;
    }

    public void setProcessCPUData(List<List<Object>> processCPUData) {
        this.processCPU.setData(processCPUData);
    }

    public MetricsLineCharts getSystemCPU() {
        return systemCPU;
    }

    public void setSystemCPU(List<List<Object>> systemCPUData) {
        this.systemCPU.setData(systemCPUData);
    }

    public MetricsLineCharts getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(List<List<Object>> heapMemoryData) {
        this.usedMemory.setData(heapMemoryData);
    }

    public MetricsLineCharts getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(List<List<Object>> physicalMemoryData) {
        this.totalMemory.setData(physicalMemoryData);
    }

    public MetricsLineCharts getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(List<List<Object>> loadAverageData) {
        this.loadAverage.setData(loadAverageData);
    }

    public MetricsLineCharts getThroughput() {
        return throughput;
    }

    public void setThroughput(List<List<Object>> throughputData) {
        this.throughput.setData(throughputData);
    }

    public MetricsLineCharts getCommittedMemory() {
        return committedMemory;
    }

    public void setCommittedMemory(List<List<Object>> committedMemory) {
        this.committedMemory.setData(committedMemory);
    }

    public MetricsLineCharts getInitMemory() {
        return initMemory;
    }

    public void setInitMemory(List<List<Object>> initMemory) {
        this.initMemory.setData(initMemory);
    }
}
