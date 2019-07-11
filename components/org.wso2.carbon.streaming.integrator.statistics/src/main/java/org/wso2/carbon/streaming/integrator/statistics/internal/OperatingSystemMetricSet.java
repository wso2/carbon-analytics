/*
 * Copyright 2017 WSO2 Inc. (http://wso2.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.streaming.integrator.statistics.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.streaming.integrator.core.DeploymentMode;
import org.wso2.carbon.streaming.integrator.core.NodeInfo;
import org.wso2.carbon.streaming.integrator.statistics.bean.WorkerMetrics;
import org.wso2.carbon.streaming.integrator.statistics.bean.WorkerStatistics;
import org.wso2.carbon.streaming.integrator.statistics.internal.exception.MetricsConfigException;
import io.siddhi.core.util.statistics.metrics.Level;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * A set of metrics for Operating System usage, including stats on load average, cpu load,
 * file descriptors etc using org.wso2.carbon.metrics.
 */
public class OperatingSystemMetricSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperatingSystemMetricSet.class);
    private static final String LOAD_AVG_MBEAN_NAME = "org.wso2.carbon.metrics:name=jvm.os.system.load.average";
    private static final String SYSTEM_CPU_MBEAN_NAME = "org.wso2.carbon.metrics:name=jvm.os.cpu.load.system";
    private static final String PROCESS_CPU_MBEAN_NAME = "org.wso2.carbon.metrics:name=jvm.os.cpu.load.process";
    private static final String MEMORY_USAGE_MBEAN_NAME = "org.wso2.carbon.metrics:name=jvm.memory.heap.usage";
    private static final String VALUE_ATTRIBUTE = "Value";
    private static final String OS_WINDOWS = "windows";
    private static final String OS_OTHER = "other";
    private double loadAverage;
    private double systemCPU;
    private double processCPU;
    private double memoryUsage;
    private boolean isJMXEnabled;
    private MBeanServer mBeanServer;
    private MetricManagementService metricManagementService;

    /**
     * Get the MBean name from the deployment yaml and get access to the MBean.
     */
    public OperatingSystemMetricSet() {
    }

    public void initConnection() {
        try {
            metricManagementService = StreamProcessorStatisticDataHolder.getInstance().getMetricsManagementService();
            isJMXEnabled = metricManagementService.isReporterRunning("JMX");
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Worker level jmx reporting has disabled." + e.getMessage(), e);
        }
    }

    /**
     * Read the load , cpu memory from the MBean of the mBeanServer.
     *
     * @return the metrics values
     */
    public WorkerStatistics getMetrics() throws MetricsConfigException {
        WorkerStatistics workerStatistics = new WorkerStatistics();
        if (metricManagementService.isEnabled()) {
            if (isJMXEnabled) {
                try {
                    // windows system does not have load average.
                    String osName = System.getProperty("os.name").toLowerCase();
                    if (osName.contains("win") || (osName.contains("windows"))) {
                        loadAverage = 0;
                        workerStatistics.setOsName(OS_WINDOWS);
                    } else {
                        //tested with linux only
                        loadAverage = (Double) mBeanServer.getAttribute(new ObjectName(LOAD_AVG_MBEAN_NAME),
                                VALUE_ATTRIBUTE);
                        workerStatistics.setOsName(OS_OTHER);
                    }
                } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException |
                        ReflectionException | MalformedObjectNameException e) {
                    LOGGER.warn("Error has been occurred while reading load average using bean name " +
                            LOAD_AVG_MBEAN_NAME + " cause may not enable jmx reporter. Hence use default " +
                            "metrics. ", e);
                }

                try {
                    systemCPU = (Double) mBeanServer.getAttribute(new ObjectName(SYSTEM_CPU_MBEAN_NAME),
                            VALUE_ATTRIBUTE);
                } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException |
                        ReflectionException | MalformedObjectNameException e) {
                    LOGGER.warn("Error has been occurred while reading system cpu  using bean name " +
                            SYSTEM_CPU_MBEAN_NAME + " cause may not enable jmx reporter. Hence use default" +
                            " metrics. ", e);
                }

                try {
                    memoryUsage = (double) mBeanServer.getAttribute(new ObjectName(MEMORY_USAGE_MBEAN_NAME),
                            VALUE_ATTRIBUTE);
                } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException |
                        ReflectionException | MalformedObjectNameException e) {
                    LOGGER.warn("Error has been occurred while reading memory usage  using bean name " +
                            MEMORY_USAGE_MBEAN_NAME + " cause may not enable jmx reporter. Hence use default" +
                            " metrics. ", e);
                }

                try {
                    processCPU = (Double) mBeanServer.getAttribute(new ObjectName(PROCESS_CPU_MBEAN_NAME),
                            VALUE_ATTRIBUTE);
                } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException |
                        ReflectionException | MalformedObjectNameException e) {
                    LOGGER.warn("Error has been occurred while reading process cpu  using bean name " +
                            PROCESS_CPU_MBEAN_NAME + " cause may not enable jmx reporter. Hence use default" +
                            " metrics. ", e);
                }
            } else {
                throw new MetricsConfigException("JMX reporter has been disabled at WSO2 metrics.");
            }
        } else {
            throw new MetricsConfigException("Metrics are disabled.");
        }

        WorkerMetrics workerMetrics = new WorkerMetrics();
        workerMetrics.setLoadAverage(loadAverage);
        workerMetrics.setSystemCPU(systemCPU);
        workerMetrics.setTotalMemory(memoryUsage);
        workerMetrics.setProcessCPU(processCPU);
        workerStatistics.setWorkerMetrics(workerMetrics);
        workerStatistics.setStatsEnabled(metricManagementService.isEnabled());
        addNodeInforToWorkerStatistics(workerStatistics);
        workerStatistics.setRunningStatus("Reachable");
        return workerStatistics;
    }

    private void addNodeInforToWorkerStatistics(WorkerStatistics workerStatistics) {
        NodeInfo nodeInfo = StreamProcessorStatisticDataHolder.getInstance().getNodeInfo();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        if (nodeInfo.getMode().compareTo(DeploymentMode.SINGLE_NODE) == 0) {
            workerStatistics.setClusterID("Single Node Deployments");
            workerStatistics.setLastSyncTime("n/a");
            workerStatistics.setLastSnapshotTime(dateFormatter.format(new Date(nodeInfo.getLastPersistedTimestamp())));
        } else {
            workerStatistics.setHaStatus(getHAStatus(String.valueOf(nodeInfo.isActiveNode())));
            workerStatistics.setClusterID(nodeInfo.getGroupId());
            if (nodeInfo.isActiveNode()) {
                workerStatistics.setLastSnapshotTime(dateFormatter.format(new Date(nodeInfo.getLastPersistedTimestamp())));
            } else {
                workerStatistics.setInSync(nodeInfo.isInSync());
                workerStatistics.setLastSyncTime(dateFormatter.format(new Date(nodeInfo.getLastSyncedTimestamp())));
            }
        }
    }

    /**
     * this method is used when metric is disabled of jmx reporter is not enabled.
     *
     * @return
     */
    public WorkerStatistics getDefault() {
        WorkerStatistics workerStatistics = new WorkerStatistics();
        WorkerMetrics workerMetrics = new WorkerMetrics();
        workerMetrics.setLoadAverage(loadAverage);
        workerMetrics.setSystemCPU(systemCPU);
        workerMetrics.setTotalMemory(memoryUsage);
        workerMetrics.setProcessCPU(processCPU);
        workerStatistics.setWorkerMetrics(workerMetrics);
        workerStatistics.setStatsEnabled(metricManagementService.isEnabled());
        addNodeInforToWorkerStatistics(workerStatistics);
        workerStatistics.setRunningStatus("Reachable");
        workerStatistics.setStatsEnabled(false);
        return workerStatistics;
    }

    /**
     * Util class to get HA Status mapping.
     *
     * @param isActive isActive from NodeInfo
     * @return HA information
     */
    private String getHAStatus(String isActive) {
        switch (isActive) {
            case "true":
                return "Active";
            case "false":
                return "Passive";
            default: {
                LOGGER.error("Invalid type of HA is Active type. can be only Active or Inactive");
                return "n/a";
            }
        }
    }

    /**
     * Method to disable the metrics of a worker.
     */
    public void disableWorkerMetrics() {
        if (metricManagementService.isEnabled()) {
            metricManagementService.disable();
            StreamProcessorStatisticDataHolder.getInstance().getSiddhiAppRuntimeService()
                    .enableSiddhiAppStatistics(Level.OFF);
        } else {
            LOGGER.warn("Wso2 metrics is already disabled.");
        }
    }

    /**
     * Method to check whether the metrics are enabled or not.
     *
     * @return
     */
    public boolean isEnableWorkerMetrics() {
        return metricManagementService.isEnabled();
    }

    /**
     * Method to disable the metrics of a worker.
     */
    public void enableWorkerMetrics(Level level) {
        if (!metricManagementService.isEnabled()) {
            metricManagementService.enable();
        } else {
            LOGGER.warn("Wso2 metrics is already enabled.");
        }
        StreamProcessorStatisticDataHolder.getInstance().getSiddhiAppRuntimeService()
                .enableSiddhiAppStatistics(level);
    }
}
