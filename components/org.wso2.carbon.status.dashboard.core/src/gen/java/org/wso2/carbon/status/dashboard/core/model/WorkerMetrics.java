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

package org.wso2.carbon.status.dashboard.core.model;

/**
 * worker real time metrics bean..
 */
public class WorkerMetrics {
    private double processCPU = 0;
    private double systemCPU = 0;
    private double loadAverage = 0;
    private double memoryUsage = 0;

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

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

}
