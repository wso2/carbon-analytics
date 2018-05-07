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
public class WorkerMoreMetricsHistory {
    private MetricsLineCharts jvmClassLoadingLoadedCurrent;
    private MetricsLineCharts jvmClassLoadingLoadedTotal;
    private MetricsLineCharts jvmClassLoadingUnloadedTotal;
    private MetricsLineCharts jvmGcPsMarksweepCount;
    private MetricsLineCharts jvmGcPsMarksweepTime;
    private MetricsLineCharts jvmGcPsScavengeCount;
    private MetricsLineCharts jvmGcPsScavengeTime;
    private MetricsLineCharts jvmMemoryHeapCommitted;
    private MetricsLineCharts jvmMemoryHeapInit;
    private MetricsLineCharts jvmMemoryHeapMax;
    private MetricsLineCharts jvmMemoryHeapUsage;
    private MetricsLineCharts jvmMemoryHeapUsed;
    private MetricsLineCharts jvmMemoryNonHeapCommitted;
    private MetricsLineCharts jvmMemoryNonHeapInit;
    private MetricsLineCharts jvmMemoryNonHeapMax;
    private MetricsLineCharts jvmMemoryNonHeapUsage;
    private MetricsLineCharts jvmMemoryNonHeapUsed;
    private MetricsLineCharts jvmMemoryTotalCommitted;
    private MetricsLineCharts jvmMemoryTotalInit;
    private MetricsLineCharts jvmMemoryTotalMax;
    private MetricsLineCharts jvmMemoryTotalUsed;
    private MetricsLineCharts jvmOsCpuLoadProcess;
    private MetricsLineCharts jvmOsCpuLoadSystem;
    private MetricsLineCharts jvmOsFileDescriptorMaxCount;
    private MetricsLineCharts jvmOsFileDescriptorOpenCount;
    private MetricsLineCharts jvmOsPhysicalMemoryFreeSize;
    private MetricsLineCharts jvmOsPhysicalMemoryTotalSize;
    private MetricsLineCharts jvmOsSwapSpaceFreeSize;
    private MetricsLineCharts jvmOsSwapSpaceTotalSize;
    private MetricsLineCharts jvmOsSystemLoadAverage;
    private MetricsLineCharts jvmOsVirtualMemoryCommittedSize;
    private MetricsLineCharts jvmThreadsCount;
    private MetricsLineCharts jvmThreadsDaemonCount;
    private MetricsLineCharts jvmMemoryPoolsSize;
    private MetricsLineCharts jvmThreadsBlockedCount;
    private MetricsLineCharts jvmThreadsDeadlockCount;
    private MetricsLineCharts jvmThreadsNewCount;
    private MetricsLineCharts jvmThreadsRunnableCount;
    private MetricsLineCharts jvmThreadsTerminatedCount;
    private MetricsLineCharts jvmThreadsTimedWaitingCount;
    private MetricsLineCharts jvmThreadsWaitingCount;
    
    public WorkerMoreMetricsHistory() {
        jvmClassLoadingLoadedCurrent = new MetricsLineCharts();
        jvmClassLoadingLoadedTotal = new MetricsLineCharts();
        jvmClassLoadingUnloadedTotal = new MetricsLineCharts();
        jvmGcPsMarksweepCount = new MetricsLineCharts();
        jvmGcPsMarksweepTime = new MetricsLineCharts();
        jvmGcPsScavengeCount = new MetricsLineCharts();
        jvmGcPsScavengeTime = new MetricsLineCharts();
        jvmMemoryHeapCommitted = new MetricsLineCharts();
        jvmMemoryHeapInit = new MetricsLineCharts();
        jvmMemoryHeapMax = new MetricsLineCharts();
        jvmMemoryHeapUsage = new MetricsLineCharts();
        jvmMemoryHeapUsed = new MetricsLineCharts();
        jvmMemoryNonHeapCommitted = new MetricsLineCharts();
        jvmMemoryNonHeapInit = new MetricsLineCharts();
        jvmMemoryNonHeapMax = new MetricsLineCharts();
        jvmMemoryNonHeapUsage = new MetricsLineCharts();
        jvmMemoryNonHeapUsed = new MetricsLineCharts();
        jvmMemoryTotalCommitted = new MetricsLineCharts();
        jvmMemoryTotalInit = new MetricsLineCharts();
        jvmMemoryTotalMax = new MetricsLineCharts();
        jvmMemoryTotalUsed = new MetricsLineCharts();
        jvmOsCpuLoadProcess = new MetricsLineCharts();
        jvmOsCpuLoadSystem = new MetricsLineCharts();
        jvmOsFileDescriptorMaxCount = new MetricsLineCharts();
        jvmOsFileDescriptorOpenCount = new MetricsLineCharts();
        jvmOsPhysicalMemoryFreeSize = new MetricsLineCharts();
        jvmOsPhysicalMemoryTotalSize = new MetricsLineCharts();
        jvmOsSwapSpaceFreeSize = new MetricsLineCharts();
        jvmOsSwapSpaceTotalSize = new MetricsLineCharts();
        jvmOsSystemLoadAverage = new MetricsLineCharts();
        jvmOsVirtualMemoryCommittedSize = new MetricsLineCharts();
        jvmThreadsCount = new MetricsLineCharts();
        jvmThreadsDaemonCount = new MetricsLineCharts();
        jvmMemoryPoolsSize = new MetricsLineCharts();
        jvmThreadsBlockedCount = new MetricsLineCharts();
        jvmThreadsDeadlockCount = new MetricsLineCharts();
        jvmThreadsNewCount = new MetricsLineCharts();
        jvmThreadsRunnableCount = new MetricsLineCharts();
        jvmThreadsTerminatedCount = new MetricsLineCharts();
        jvmThreadsTimedWaitingCount = new MetricsLineCharts();
        jvmThreadsWaitingCount = new MetricsLineCharts();
    }
    
    public MetricsLineCharts getJvmClassLoadingLoadedCurrent() {
        return jvmClassLoadingLoadedCurrent;
    }
    
    public void setJvmClassLoadingLoadedCurrent(List<List<Object>> jvmClassLoadingLoadedCurrent) {
        this.jvmClassLoadingLoadedCurrent.setData(jvmClassLoadingLoadedCurrent);
    }
    
    public MetricsLineCharts getJvmClassLoadingLoadedTotal() {
        return jvmClassLoadingLoadedTotal;
    }
    
    public void setJvmClassLoadingLoadedTotal(List<List<Object>> jvmClassLoadingLoadedTotal) {
        this.jvmClassLoadingLoadedTotal.setData(jvmClassLoadingLoadedTotal);
    }
    
    public MetricsLineCharts getJvmClassLoadingUnloadedTotal() {
        return jvmClassLoadingUnloadedTotal;
    }
    
    public void setJvmClassLoadingUnloadedTotal(List<List<Object>> jvmClassLoadingUnloadedTotal) {
        this.jvmClassLoadingUnloadedTotal.setData(jvmClassLoadingUnloadedTotal);
    }
    
    public MetricsLineCharts getJvmGcPsMarksweepCount() {
        return jvmGcPsMarksweepCount;
    }
    
    public void setJvmGcPsMarksweepCount(List<List<Object>> jvmGcPsMarksweepCount) {
        this.jvmGcPsMarksweepCount.setData(jvmGcPsMarksweepCount);
    }
    
    public MetricsLineCharts getJvmGcPsMarksweepTime() {
        return jvmGcPsMarksweepTime;
    }
    
    public void setJvmGcPsMarksweepTime(List<List<Object>> jvmGcPsMarksweepTime) {
        this.jvmGcPsMarksweepTime.setData(jvmGcPsMarksweepTime);
    }
    
    public MetricsLineCharts getJvmGcPsScavengeCount() {
        return jvmGcPsScavengeCount;
    }
    
    public void setJvmGcPsScavengeCount(List<List<Object>> jvmGcPsScavengeCount) {
        this.jvmGcPsScavengeCount.setData(jvmGcPsScavengeCount);
    }
    
    public MetricsLineCharts getJvmGcPsScavengeTime() {
        return jvmGcPsScavengeTime;
    }
    
    public void setJvmGcPsScavengeTime(List<List<Object>> jvmGcPsScavengeTime) {
        this.jvmGcPsScavengeTime.setData(jvmGcPsScavengeTime);
    }
    
    public MetricsLineCharts getJvmMemoryHeapCommitted() {
        return jvmMemoryHeapCommitted;
    }
    
    public void setJvmMemoryHeapCommitted(List<List<Object>> jvmMemoryHeapCommitted) {
        this.jvmMemoryHeapCommitted.setData(jvmMemoryHeapCommitted);
    }
    
    public MetricsLineCharts getJvmMemoryHeapInit() {
        return jvmMemoryHeapInit;
    }
    
    public void setJvmMemoryHeapInit(List<List<Object>> jvmMemoryHeapInit) {
        this.jvmMemoryHeapInit.setData(jvmMemoryHeapInit);
    }
    
    public MetricsLineCharts getJvmMemoryHeapMax() {
        return jvmMemoryHeapMax;
    }
    
    public void setJvmMemoryHeapMax(List<List<Object>> jvmMemoryHeapMax) {
        this.jvmMemoryHeapMax.setData(jvmMemoryHeapMax);
    }
    
    public MetricsLineCharts getJvmMemoryHeapUsage() {
        return jvmMemoryHeapUsage;
    }
    
    public void setJvmMemoryHeapUsage(List<List<Object>> jvmMemoryHeapUsage) {
        this.jvmMemoryHeapUsage.setData(jvmMemoryHeapUsage);
    }
    
    public MetricsLineCharts getJvmMemoryHeapUsed() {
        return jvmMemoryHeapUsed;
    }
    
    public void setJvmMemoryHeapUsed(List<List<Object>> jvmMemoryHeapUsed) {
        this.jvmMemoryHeapUsed.setData(jvmMemoryHeapUsed);
    }
    
    public MetricsLineCharts getJvmMemoryNonHeapCommitted() {
        return jvmMemoryNonHeapCommitted;
    }
    
    public void setJvmMemoryNonHeapCommitted(List<List<Object>> jvmMemoryNonHeapCommitted) {
        this.jvmMemoryNonHeapCommitted.setData(jvmMemoryNonHeapCommitted);
    }
    
    public MetricsLineCharts getJvmMemoryNonHeapInit() {
        return jvmMemoryNonHeapInit;
    }
    
    public void setJvmMemoryNonHeapInit(List<List<Object>> jvmMemoryNonHeapInit) {
        this.jvmMemoryNonHeapInit.setData(jvmMemoryNonHeapInit);
    }
    
    public MetricsLineCharts getJvmMemoryNonHeapMax() {
        return jvmMemoryNonHeapMax;
    }
    
    public void setJvmMemoryNonHeapMax(List<List<Object>> jvmMemoryNonHeapMax) {
        this.jvmMemoryNonHeapMax.setData(jvmMemoryNonHeapMax);
    }
    
    public MetricsLineCharts getJvmMemoryNonHeapUsage() {
        return jvmMemoryNonHeapUsage;
    }
    
    public void setJvmMemoryNonHeapUsage(List<List<Object>> jvmMemoryNonHeapUsage) {
        this.jvmMemoryNonHeapUsage.setData(jvmMemoryNonHeapUsage);
    }
    
    public MetricsLineCharts getJvmMemoryNonHeapUsed() {
        return jvmMemoryNonHeapUsed;
    }
    
    public void setJvmMemoryNonHeapUsed(List<List<Object>> jvmMemoryNonHeapUsed) {
        this.jvmMemoryNonHeapUsed.setData(jvmMemoryNonHeapUsed);
    }
    
    public MetricsLineCharts getJvmMemoryTotalCommitted() {
        return jvmMemoryTotalCommitted;
    }
    
    public void setJvmMemoryTotalCommitted(List<List<Object>> jvmMemoryTotalCommitted) {
        this.jvmMemoryTotalCommitted.setData(jvmMemoryTotalCommitted);
    }
    
    public MetricsLineCharts getJvmMemoryTotalInit() {
        return jvmMemoryTotalInit;
    }
    
    public void setJvmMemoryTotalInit(List<List<Object>> jvmMemoryTotalInit) {
        this.jvmMemoryTotalInit.setData(jvmMemoryTotalInit);
    }
    
    public MetricsLineCharts getJvmMemoryTotalMax() {
        return jvmMemoryTotalMax;
    }
    
    public void setJvmMemoryTotalMax(List<List<Object>> jvmMemoryTotalMax) {
        this.jvmMemoryTotalMax.setData(jvmMemoryTotalMax);
    }
    
    public MetricsLineCharts getJvmMemoryTotalUsed() {
        return jvmMemoryTotalUsed;
    }
    
    public void setJvmMemoryTotalUsed(List<List<Object>> jvmMemoryTotalUsed) {
        this.jvmMemoryTotalUsed.setData(jvmMemoryTotalUsed);
    }
    
    public MetricsLineCharts getJvmOsCpuLoadProcess() {
        return jvmOsCpuLoadProcess;
    }
    
    public void setJvmOsCpuLoadProcess(List<List<Object>> jvmOsCpuLoadProcess) {
        this.jvmOsCpuLoadProcess.setData(jvmOsCpuLoadProcess);
    }
    
    public MetricsLineCharts getJvmOsCpuLoadSystem() {
        return jvmOsCpuLoadSystem;
    }
    
    public void setJvmOsCpuLoadSystem(List<List<Object>> jvmOsCpuLoadSystem) {
        this.jvmOsCpuLoadSystem.setData(jvmOsCpuLoadSystem);
    }
    
    public MetricsLineCharts getJvmOsFileDescriptorMaxCount() {
        return jvmOsFileDescriptorMaxCount;
    }
    
    public void setJvmOsFileDescriptorMaxCount(List<List<Object>> jvmOsFileDescriptorMaxCount) {
        this.jvmOsFileDescriptorMaxCount.setData(jvmOsFileDescriptorMaxCount);
    }
    
    public MetricsLineCharts getJvmOsFileDescriptorOpenCount() {
        return jvmOsFileDescriptorOpenCount;
    }
    
    public void setJvmOsFileDescriptorOpenCount(List<List<Object>> jvmOsFileDescriptorOpenCount) {
        this.jvmOsFileDescriptorOpenCount.setData(jvmOsFileDescriptorOpenCount);
    }
    
    public MetricsLineCharts getJvmOsPhysicalMemoryFreeSize() {
        return jvmOsPhysicalMemoryFreeSize;
    }
    
    public void setJvmOsPhysicalMemoryFreeSize(List<List<Object>> jvmOsPhysicalMemoryFreeSize) {
        this.jvmOsPhysicalMemoryFreeSize.setData(jvmOsPhysicalMemoryFreeSize);
    }
    
    public MetricsLineCharts getJvmOsPhysicalMemoryTotalSize() {
        return jvmOsPhysicalMemoryTotalSize;
    }
    
    public void setJvmOsPhysicalMemoryTotalSize(List<List<Object>> jvmOsPhysicalMemoryTotalSize) {
        this.jvmOsPhysicalMemoryTotalSize.setData(jvmOsPhysicalMemoryTotalSize);
    }
    
    public MetricsLineCharts getJvmOsSwapSpaceFreeSize() {
        return jvmOsSwapSpaceFreeSize;
    }
    
    public void setJvmOsSwapSpaceFreeSize(List<List<Object>> jvmOsSwapSpaceFreeSize) {
        this.jvmOsSwapSpaceFreeSize.setData(jvmOsSwapSpaceFreeSize);
    }
    
    public MetricsLineCharts getJvmOsSwapSpaceTotalSize() {
        return jvmOsSwapSpaceTotalSize;
    }
    
    public void setJvmOsSwapSpaceTotalSize(List<List<Object>> jvmOsSwapSpaceTotalSize) {
        this.jvmOsSwapSpaceTotalSize.setData(jvmOsSwapSpaceTotalSize);
    }
    
    public MetricsLineCharts getJvmOsSystemLoadAverage() {
        return jvmOsSystemLoadAverage;
    }
    
    public void setJvmOsSystemLoadAverage(List<List<Object>> jvmOsSystemLoadAverage) {
        this.jvmOsSystemLoadAverage.setData(jvmOsSystemLoadAverage);
    }
    
    public MetricsLineCharts getJvmOsVirtualMemoryCommittedSize() {
        return jvmOsVirtualMemoryCommittedSize;
    }
    
    public void setJvmOsVirtualMemoryCommittedSize(List<List<Object>> jvmOsVirtualMemoryCommittedSize) {
        this.jvmOsVirtualMemoryCommittedSize.setData(jvmOsVirtualMemoryCommittedSize);
    }
    
    public MetricsLineCharts getJvmThreadsCount() {
        return jvmThreadsCount;
    }
    
    public void setJvmThreadsCount(List<List<Object>> jvmThreadsCount) {
        this.jvmThreadsCount.setData(jvmThreadsCount);
    }
    
    public MetricsLineCharts getJvmThreadsDaemonCount() {
        return jvmThreadsDaemonCount;
    }
    
    public void setJvmThreadsDaemonCount(List<List<Object>> jvmThreadsDaemonCount) {
        this.jvmThreadsDaemonCount.setData(jvmThreadsDaemonCount);
    }
    
    public MetricsLineCharts getJvmMemoryPoolsSize() {
        return jvmMemoryPoolsSize;
    }
    
    public void setJvmMemoryPoolsSize(List<List<Object>> data) {
        this.jvmMemoryPoolsSize.setData(data);
    }
    
    public MetricsLineCharts getJvmThreadsBlockedCount() {
        return jvmThreadsBlockedCount;
    }
    
    public void setJvmThreadsBlockedCount(List<List<Object>> data) {
        this.jvmThreadsBlockedCount.setData(data);
    }
    
    public MetricsLineCharts getJvmThreadsDeadlockCount() {
        return jvmThreadsDeadlockCount;
    }
    
    public void setJvmThreadsDeadlockCount(List<List<Object>> data) {
        this.jvmThreadsDeadlockCount.setData(data);
    }
    
    public MetricsLineCharts getJvmThreadsNewCount() {
        return jvmThreadsNewCount;
    }
    
    public void setJvmThreadsNewCount(List<List<Object>> data) {
        this.jvmThreadsNewCount.setData(data);
    }
    
    public MetricsLineCharts getJvmThreadsRunnableCount() {
        return jvmThreadsRunnableCount;
    }
    
    public void setJvmThreadsRunnableCount(List<List<Object>> data) {
        this.jvmThreadsRunnableCount.setData(data);
    }
    
    public MetricsLineCharts getJvmThreadsTerminatedCount() {
        return jvmThreadsTerminatedCount;
    }
    
    public void setJvmThreadsTerminatedCount(List<List<Object>> data) {
        this.jvmThreadsTerminatedCount.setData(data);
    }
    
    public MetricsLineCharts getJvmThreadsTimedWaitingCount() {
        return jvmThreadsTimedWaitingCount;
    }
    
    public void setJvmThreadsTimedWaitingCount(List<List<Object>> data) {
        this.jvmThreadsTimedWaitingCount.setData(data);
    }
    
    public MetricsLineCharts getJvmThreadsWaitingCount() {
        return jvmThreadsWaitingCount;
    }
    
    public void setJvmThreadsWaitingCount(List<List<Object>> data) {
        this.jvmThreadsWaitingCount.setData(data);
    }
}
