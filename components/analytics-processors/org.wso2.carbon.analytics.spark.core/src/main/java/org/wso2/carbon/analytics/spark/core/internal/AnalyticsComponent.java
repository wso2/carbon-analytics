/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.spark.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.spark.core.AnalyticsProcessorService;
import org.wso2.carbon.analytics.spark.core.CarbonAnalyticsProcessorService;
import org.wso2.carbon.analytics.spark.core.SparkContextServiceImpl;
import org.wso2.carbon.analytics.spark.core.SparkScriptCAppDeployer;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsUDFException;
import org.wso2.carbon.analytics.spark.core.interfaces.SparkContextService;
import org.wso2.carbon.analytics.spark.core.internal.jmx.AnalyticsScriptLastExecutionStartTime;
import org.wso2.carbon.analytics.spark.core.internal.jmx.IncrementalLastProcessedTimestamp;
import org.wso2.carbon.analytics.spark.core.udf.CarbonUDAF;
import org.wso2.carbon.analytics.spark.core.udf.CarbonUDF;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.utils.ComputeClasspath;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.utils.CarbonUtils;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.SocketException;

/**
 * Declarative service component for spark analytics.
 *
 * @scr.component name="analytics.core" immediate="true"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="analytics.dataservice" interface="AnalyticsDataService"
 * cardinality="1..1" policy="dynamic"  bind="setAnalyticsDataService" unbind="unsetAnalyticsDataService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="tenant.registryloader" interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader" unbind="unsetTenantRegistryLoader"
 * @scr.reference name="carbon.udf" interface="org.wso2.carbon.analytics.spark.core.udf.CarbonUDF"
 * cardinality="0..n" policy="dynamic" bind="addCarbonUDF" unbind="removeCarbonUDFs"
 * @scr.reference name="carbon.udaf" interface="org.wso2.carbon.analytics.spark.core.udf.CarbonUDAF"
 * cardinality="0..n" policy="dynamic" bind="addCarbonUDAF" unbind="removeCarbonUDAF"
 */
public class AnalyticsComponent {

    private static final String PORT_OFFSET_SERVER_PROP = "Ports.Offset";

    private static final Log log = LogFactory.getLog(AnalyticsComponent.class);

    private static boolean initialized;

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Analytics Spark Core");
        }
        try {
            checkAnalyticsEnabled();
            checkAnalyticsStatsEnabled();
            BundleContext bundleContext = ctx.getBundleContext();
            if (ServiceHolder.isAnalyticsEngineEnabled()) {
                try {
                    int portOffset = CarbonUtils.getPortFromServerConfig(PORT_OFFSET_SERVER_PROP) + 1;
                    ServiceHolder.setAnalyticskExecutor(new SparkAnalyticsExecutor(
                            this.getLocalHostname(), portOffset));
                    ServiceHolder.getAnalyticskExecutor().initializeSparkServer();
                } catch (Throwable e) {
                    String msg = "Error initializing analytics executor: " + e.getMessage();
                    log.error(msg, e);
                }
            }
            AnalyticsProcessorService analyticsProcessorService = new CarbonAnalyticsProcessorService();
            bundleContext.registerService(AnalyticsProcessorService.class, analyticsProcessorService, null);
            ServiceHolder.setAnalyticsProcessorService(analyticsProcessorService);
            // Registering server startup observer
            SparkScriptCAppDeployer sparkScriptCAppDeployer = new SparkScriptCAppDeployer();
            bundleContext.registerService(
                    AppDeploymentHandler.class.getName(), sparkScriptCAppDeployer, null);
            // registering spark context service
            SparkContextService scs = new SparkContextServiceImpl();
            bundleContext.registerService(SparkContextService.class, scs, null);

            if (log.isDebugEnabled()) {
                log.debug("Finished activating Analytics Spark Core");
            }
        } catch (Exception ex) {
            log.error("Error in registering the analytics processor service! ", ex);
        }
        try {
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            String lastProcessedTimestamp = "org.wso2.carbon:00=analytics,01=LAST_PROCESSED_TIMESTAMP";
            ObjectName lastProcessedTimestampMbean = new ObjectName(lastProcessedTimestamp);
            if (!platformMBeanServer.isRegistered(lastProcessedTimestampMbean)) {
                IncrementalLastProcessedTimestamp processedTimestampBean = new IncrementalLastProcessedTimestamp();
                platformMBeanServer.registerMBean(processedTimestampBean, lastProcessedTimestampMbean);
            }
            String lastExecutionStartTime = "org.wso2.carbon:00=analytics,01=ANALYTICS_SCRIPT_LAST_EXECUTION_START_TIME";
            ObjectName lastExecutionStartTimeMbean = new ObjectName(lastExecutionStartTime);
            if (!platformMBeanServer.isRegistered(lastExecutionStartTimeMbean)) {
                AnalyticsScriptLastExecutionStartTime analyticsScriptLastExecutionStartTime = new AnalyticsScriptLastExecutionStartTime();
                platformMBeanServer.registerMBean(analyticsScriptLastExecutionStartTime, lastExecutionStartTimeMbean);
            }
        } catch (Exception e) {
            log.error("Unable to create EventCounter stat MBean: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        ServiceHolder.getAnalyticskExecutor().stop();
    }

    protected void setTaskService(TaskService taskService) {
        checkAnalyticsEnabled();
        ServiceHolder.setTaskService(taskService);
        if (ServiceHolder.isAnalyticsExecutionEnabled()) {
            //Analytics execution is disabled, therefore not joining the task cluster for the execution.
            try {
                ServiceHolder.getTaskService().registerTaskType(AnalyticsConstants.SCRIPT_TASK_TYPE);
            } catch (TaskException e) {
                log.error("Error while registering the task type : " + AnalyticsConstants.SCRIPT_TASK_TYPE, e);
            }
        }
    }

    protected void unsetTaskService(TaskService taskService) {
        ServiceHolder.setTaskService(null);
    }

    protected void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(analyticsDataService);
    }

    protected void unsetAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(null);
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        ServiceHolder.setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        ServiceHolder.setTenantRegistryLoader(null);
    }

    protected void addCarbonUDF(CarbonUDF carbonUDF) {
        try {
            if (ServiceHolder.getAnalyticskExecutor() != null) {
                ServiceHolder.getAnalyticskExecutor().registerUDFFromOSGIComponent(carbonUDF);
            } else {
                ServiceHolder.addCarbonUDFs(carbonUDF);
            }

            addCarbonUDFJarToSparkClasspath(carbonUDF.getClass());
        } catch (AnalyticsUDFException e) {
            log.error("Error while registering UDFs from OSGI components: " + e.getMessage(), e);
        }
    }

    protected void addCarbonUDAF(CarbonUDAF carbonUDAF) {
        try {
            if (ServiceHolder.getAnalyticskExecutor() != null) {
                ServiceHolder.getAnalyticskExecutor().registerUDAFFromOSGIComponent(carbonUDAF);
            } else {
                ServiceHolder.addCarbonUDAFs(carbonUDAF);
            }
            addCarbonUDFJarToSparkClasspath(carbonUDAF.getClass());
        } catch (AnalyticsUDFException e) {
            log.error("Error while registering UDFs from OSGI components: " + e.getMessage(), e);
        }
    }

    protected void removeCarbonUDAF(CarbonUDAF carbonUDAF) {
        ServiceHolder.removeCarbonUDAF(carbonUDAF);
    }

    @SuppressWarnings("rawtypes")
    private void addCarbonUDFJarToSparkClasspath(Class carbonUDFClass) {
        String[] jarPath = carbonUDFClass.getProtectionDomain().getCodeSource().getLocation().getPath()
                .split(File.separatorChar=='\\' ? "\\\\" : File.separator);
        String jarName = jarPath[jarPath.length-1].split("_")[0];
        ComputeClasspath.addAdditionalJarToClasspath(jarName);
    }

    protected void removeCarbonUDFs(CarbonUDF carbonUDF) {
        ServiceHolder.removeCarbonUDFs(carbonUDF);
    }

    private void checkAnalyticsEnabled() {
        if (!initialized) {
            initialized = true;
            if (ServiceHolder.isAnalyticsEngineEnabled()) {
                if (System.getProperty(AnalyticsConstants.DISABLE_ANALYTICS_ENGINE_JVM_OPTION) != null) {
                    if (Boolean.parseBoolean(System.getProperty(AnalyticsConstants.DISABLE_ANALYTICS_ENGINE_JVM_OPTION))) {
                        ServiceHolder.setAnalyticsEngineEnabled(false);
                        ServiceHolder.setAnalyticsExecutionEnabled(false);
                        ServiceHolder.setAnalyticsSparkContextEnabled(false);
                        //if analytics engine is disabled, execution is also disabled by default
                    }
                }
            }

            if (ServiceHolder.isAnalyticsExecutionEnabled()) {
                if (System.getProperty(AnalyticsConstants.DISABLE_ANALYTICS_EXECUTION_JVM_OPTION) != null) {
                    if (Boolean.parseBoolean(System.getProperty(AnalyticsConstants.DISABLE_ANALYTICS_EXECUTION_JVM_OPTION))) {
                        ServiceHolder.setAnalyticsExecutionEnabled(false);
                    }
                }
            }

            if (ServiceHolder.isAnalyticsSparkContextEnabled()){
                if (System.getProperty(AnalyticsConstants.DISABLE_ANALYTICS_SPARK_CTX_JVM_OPTION) != null){
                    if (Boolean.parseBoolean(System.getProperty(AnalyticsConstants.DISABLE_ANALYTICS_SPARK_CTX_JVM_OPTION))) {
                        ServiceHolder.setAnalyticsSparkContextEnabled(false);
                    }
                }
            }
        }
    }

    private void checkAnalyticsStatsEnabled(){
        if (initialized) {
            if (!ServiceHolder.isAnalyticsStatsEnabled()){
                if (System.getProperty(AnalyticsConstants.ENABLE_ANALYTICS_STATS_OPTION) != null){
                    if (Boolean.parseBoolean(System.getProperty(AnalyticsConstants.ENABLE_ANALYTICS_STATS_OPTION))) {
                        ServiceHolder.setAnalyticsStatsEnabled(true);
                    }
                }
            }
        }
    }

    private String getLocalHostname() throws SocketException {
//        this is removed because, NetworkUtils.getLocalHostname() would return the carbon.xml
//        hostname if provided. but in the spark environment, it would need a unique hostname DAS-171
//        return NetworkUtils.getLocalHostname();
        String localIP = System.getenv(AnalyticsConstants.SPARK_LOCAL_IP_PROP);
        if (localIP != null) {
            if (log.isDebugEnabled()) {
                log.debug("Spark host is set from the SPARK_LOCAL_IP property : " + localIP);
            }
            return localIP;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Spark host is set NOT set, hence using the node network interface");
            }
            return org.apache.axis2.util.Utils.getIpAddress();
        }
    }
}
