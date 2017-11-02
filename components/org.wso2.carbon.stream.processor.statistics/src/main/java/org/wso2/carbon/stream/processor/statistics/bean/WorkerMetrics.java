/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.stream.processor.statistics.bean;

/**
 * This is the bean class use to keep the real time metrics details of the worker node.
 */
public class WorkerMetrics {
    private double processCPU;
    private double systemCPU;
    private double loadAverage;
    private double memoryUsage;

    public WorkerMetrics() {
    }

    public double getProcessCPU() {
        return processCPU;
    }

    public void setProcessCPU(double processCPU) {
        this.processCPU = processCPU;
    }

    public double getSystemCPU() {
        return systemCPU;
    }

    public void setSystemCPU(double systemCPU) {
        this.systemCPU = systemCPU;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
    }

    public double getTotalMemory() {
        return memoryUsage;
    }

    public void setTotalMemory(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

}
