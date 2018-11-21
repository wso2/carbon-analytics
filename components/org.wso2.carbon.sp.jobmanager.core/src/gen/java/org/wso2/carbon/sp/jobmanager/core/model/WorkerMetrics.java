/*
 *
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sp.jobmanager.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

/**
 * This is the bean class use to keep the real time metrics details of the worker node.
 */
@ApiModel(description = "Represents a Resource Node Metrics")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-23T12:20:42.963Z")
public class WorkerMetrics {

    @JsonProperty("processCPU")
    private double processCPU;

    @JsonProperty("systemCPU")
    private double systemCPU;

    @JsonProperty("loadAverage")
    private double loadAverage;

    @JsonProperty("memoryUsage")
    private double memoryUsage;

    public double getProcessCPU() {
        return processCPU;
    }

    public WorkerMetrics setProcessCPU(double processCPU) {
        this.processCPU = processCPU;
        return this;
    }

    public double getSystemCPU() {
        return systemCPU;
    }

    public WorkerMetrics setSystemCPU(double systemCPU) {
        this.systemCPU = systemCPU;
        return this;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public WorkerMetrics setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
        return this;
    }

    public double getTotalMemory() {
        return memoryUsage;
    }

    public WorkerMetrics setTotalMemory(double memoryUsage) {
        this.memoryUsage = memoryUsage;
        return this;
    }

}
