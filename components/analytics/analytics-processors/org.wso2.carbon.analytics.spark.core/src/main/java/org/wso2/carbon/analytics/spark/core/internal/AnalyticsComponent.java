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

import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterException;
import org.wso2.carbon.analytics.spark.core.AnalyticsProcessorService;
import org.wso2.carbon.analytics.spark.core.CarbonAnalyticsProcessorService;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.utils.NetworkUtils;

/**
 * @scr.component name="analytics.core" immediate="true"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="analytics.dataservice" interface="org.wso2.carbon.analytics.dataservice.AnalyticsDataService"
 * cardinality="1..1" policy="dynamic"  bind="setAnalyticsDataService" unbind="unsetAnalyticsDataService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="tenant.registryloader" interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader" unbind="unsetTenantRegistryLoader"
 */
public class AnalyticsComponent {

    private static final String PORT_OFFSET_SERVER_PROP = "Ports.Offset";

    private static final Log log = LogFactory.getLog(AnalyticsComponent.class);

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Analytics Spark Core");
        }
        try {
            try {
                int portOffset = Integer.parseInt(ServerConfiguration.getInstance().getFirstProperty(PORT_OFFSET_SERVER_PROP));
                ServiceHolder.setAnalyticskExecutor(new SparkAnalyticsExecutor(
                        NetworkUtils.getLocalHostname(), portOffset));
            } catch (Throwable e) {
                String msg = "Error initializing analytics executor: " + e.getMessage();
                log.error(msg, e);
//            throw new RuntimeException(msg, e);
            }
            AnalyticsProcessorService analyticsProcessorService = new CarbonAnalyticsProcessorService();
            ctx.getBundleContext().registerService(AnalyticsProcessorService.class, analyticsProcessorService, null);
            ServiceHolder.setAnalyticsProcessorService(analyticsProcessorService);
            if (log.isDebugEnabled()) {
                log.debug("Finished activating Analytics Spark Core");
            }
        }catch (Throwable throwable){
            log.error("Error in registering the analytics processor service! ", throwable);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        ServiceHolder.getAnalyticskExecutor().stop();
    }

    protected void setTaskService(TaskService taskService) {
        ServiceHolder.setTaskService(taskService);
        try {
            ServiceHolder.getTaskService().registerTaskType(AnalyticsConstants.SCRIPT_TASK_TYPE);
        } catch (TaskException e) {
            log.error("Error while registering the task type : " + AnalyticsConstants.SCRIPT_TASK_TYPE, e);
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
}
