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

import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.spark.core.AnalyticsProcessorService;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

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

    private static TenantRegistryLoader tenantRegistryLoader;
    
    private static SparkAnalyticsExecutor analyticskExecutor;

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

    public static void setAnalyticsProcessorService(AnalyticsProcessorService analyticsProcessorService) {
        ServiceHolder.analyticsProcessorService = analyticsProcessorService;
    }

    public static void setRegistryService(RegistryService registryService) {
        ServiceHolder.registryService = registryService;
    }

    public static void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        ServiceHolder.tenantRegistryLoader = tenantRegistryLoader;
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
}
