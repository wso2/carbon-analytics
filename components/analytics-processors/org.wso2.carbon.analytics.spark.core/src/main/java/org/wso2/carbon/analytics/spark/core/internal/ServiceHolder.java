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

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.AnalyticsProcessorService;
import org.wso2.carbon.analytics.spark.core.sources.AnalyticsIncrementalMetaStore;
import org.wso2.carbon.analytics.spark.core.udf.CarbonUDAF;
import org.wso2.carbon.analytics.spark.core.udf.CarbonUDF;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the OSGI services registered with Declarative service component.
 */
public class ServiceHolder {

    private ServiceHolder() {
        /**
         * Avoid instantiation of this class.
         */
    }

    private static TaskService taskService;

    private static AnalyticsDataService analyticsDataService;

    private static AnalyticsProcessorService analyticsProcessorService;

    private static RegistryService registryService;

    private static ServerConfigurationService serverConfigService;

    private static TenantRegistryLoader tenantRegistryLoader;

    private static SparkAnalyticsExecutor analyticskExecutor;

    private static Map<String, CarbonUDF> carbonUDFs = new HashMap<>();

    private static Map<String, Class<? extends UserDefinedAggregateFunction>> carbonUDAFs = new HashMap<>();

    private static boolean analyticsExecutionEnabled = true;

    private static boolean analyticsEngineEnabled = true;

    private static boolean analyticsSparkContextEnabled = true;

    private static boolean analyticsStatsEnabled = false;

    private static AnalyticsIncrementalMetaStore incrementalMetaStore;

    private static JavaSparkContext javaSparkContext;

    public static void setTaskService(TaskService taskService) {
        ServiceHolder.taskService = taskService;
    }

    public static TaskService getTaskService() {
        return taskService;
    }

    public static TaskManager getTaskManager() throws TaskException {
        return taskService.getTaskManager(AnalyticsConstants.SCRIPT_TASK_TYPE);
    }

    public static AnalyticsDataService getAnalyticsDataService() {
        if (analyticsDataService == null) {
            analyticsDataService = AnalyticsServiceHolder.getAnalyticsDataService();
        }
        return analyticsDataService;
    }

    public static void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.analyticsDataService = analyticsDataService;
    }

    public static AnalyticsProcessorService getAnalyticsProcessorService() {
        return analyticsProcessorService;
    }

    public static void setAnalyticsProcessorService(
            AnalyticsProcessorService analyticsProcessorService) {
        ServiceHolder.analyticsProcessorService = analyticsProcessorService;
    }

    public static void setRegistryService(RegistryService registryService) {
        ServiceHolder.registryService = registryService;
    }

    public static ServerConfigurationService getServerConfigService() {
        return serverConfigService;
    }

    public static void setServerConfigService(ServerConfigurationService serverConfigService) {
        ServiceHolder.serverConfigService = serverConfigService;
    }

    public static void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        ServiceHolder.tenantRegistryLoader = tenantRegistryLoader;
    }

    public static TenantRegistryLoader getTenantRegistryLoader() {
        return ServiceHolder.tenantRegistryLoader;
    }

    public static UserRegistry getTenantConfigRegistry(int tenantId) throws RegistryException {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            return ServiceHolder.registryService.getConfigSystemRegistry();
        } else {
            ServiceHolder.tenantRegistryLoader.loadTenantRegistry(tenantId);
            return ServiceHolder.registryService.getConfigSystemRegistry(tenantId);
        }
    }

    public static SparkAnalyticsExecutor getAnalyticskExecutor() {
        return analyticskExecutor;
    }

    public static void setAnalyticskExecutor(SparkAnalyticsExecutor analyticskExecutor) {
        ServiceHolder.analyticskExecutor = analyticskExecutor;
    }

    public static boolean isAnalyticsExecutionEnabled() {
        return analyticsExecutionEnabled;
    }

    public static void setAnalyticsExecutionEnabled(boolean analyticsExecutionEnabled) {
        ServiceHolder.analyticsExecutionEnabled = analyticsExecutionEnabled;
    }

    public static boolean isAnalyticsEngineEnabled() {
        return analyticsEngineEnabled;
    }

    public static void setAnalyticsEngineEnabled(boolean analyticsEngineEnabled) {
        ServiceHolder.analyticsEngineEnabled = analyticsEngineEnabled;
    }

    public static void setAnalyticsSparkContextEnabled(boolean analyticsSparkContextEnabled) {
        ServiceHolder.analyticsSparkContextEnabled = analyticsSparkContextEnabled;
    }

    public static boolean isAnalyticsSparkContextEnabled() {
        return analyticsSparkContextEnabled;
    }

    public static boolean isAnalyticsStatsEnabled() {
        return analyticsStatsEnabled;
    }

    public static void setAnalyticsStatsEnabled(boolean analyticsStatsEnabled) {
        ServiceHolder.analyticsStatsEnabled = analyticsStatsEnabled;
    }

    public static void addCarbonUDFs(CarbonUDF carbonUDF) {
        ServiceHolder.carbonUDFs.put(carbonUDF.getClass().getName(), carbonUDF);
    }

    public static void removeCarbonUDFs(CarbonUDF carbonUDF) {
        ServiceHolder.carbonUDFs.remove(carbonUDF.getClass().getName());
    }

    public static Map<String, CarbonUDF> getCarbonUDFs() {
        return ServiceHolder.carbonUDFs;
    }

    public static void addCarbonUDAFs(CarbonUDAF carbonUDAF) {
        ServiceHolder.carbonUDAFs.put(carbonUDAF.getAlias(), carbonUDAF.getClass());
    }

    public static void removeCarbonUDAF(CarbonUDAF udaf) {
        ServiceHolder.carbonUDAFs.remove(udaf.getAlias());
    }

    public static Map<String, Class<? extends UserDefinedAggregateFunction>> getCarbonUDAFs() {
        return ServiceHolder.carbonUDAFs;
    }

    public static AnalyticsIncrementalMetaStore getIncrementalMetaStore() {
        if (incrementalMetaStore == null) {
            try {
                incrementalMetaStore = new AnalyticsIncrementalMetaStore();
            } catch (AnalyticsException e) {
                throw new RuntimeException("Error in creating analytics incremental metastore: "
                                           + e.getMessage(), e);
            }
        }
        return incrementalMetaStore;
    }

    public static JavaSparkContext getJavaSparkContext() {
        return javaSparkContext;
    }

    public static void setJavaSparkContext(JavaSparkContext javaSparkContext) {
        ServiceHolder.javaSparkContext = javaSparkContext;
    }

}
