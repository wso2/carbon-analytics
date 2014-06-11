/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.bam.jmx.agent.tasks.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.jmx.agent.JmxConstant;
import org.wso2.carbon.bam.jmx.agent.TenantPublisherConfigData;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;

import java.util.Map;

/**
 * @scr.component name="jmxservices.task" immediate="true"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="registry.loader.default"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryLoader"
 * unbind="unsetRegistryLoader"
 */
public class JmxTaskServiceComponent {

    private static final Log log = LogFactory.getLog(JmxTaskServiceComponent.class);

    private static TaskService taskService;
    private static RegistryService registryService;
    private static TenantRegistryLoader tenantRegistryLoader;

    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Activating the tasks");
        }
        BundleContext bundleContext = ctxt.getBundleContext();
        try {
            getTaskService().registerTaskType(JmxConstant.JMX_SERVICE_TASK_TYPE);
        } catch (TaskException e) {
            log.error("JmxTaskServiceComponent activation error.", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        Map<String, DataPublisher> publisherMap = TenantPublisherConfigData.getDataPublisherMap();
        if (!publisherMap.isEmpty()) {
            for (String tenantId : publisherMap.keySet()) {
                DataPublisher dataPublisher = publisherMap.get(tenantId);
                if (dataPublisher != null) {
                    dataPublisher.stop();
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Jmx Services task bundle is deactivated ");
        }
    }

    protected void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service");
        }
        JmxTaskServiceComponent.taskService = taskService;
    }

    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Task Service");
        }
        JmxTaskServiceComponent.taskService = null;
    }


    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService acquired");
        }
        JmxTaskServiceComponent.registryService = registryService;

    }

    protected void unsetRegistryService(RegistryService registryService) {
        JmxTaskServiceComponent.registryService = null;
    }

    protected void setRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Tenant registry loader acquired");
        }
        JmxTaskServiceComponent.tenantRegistryLoader = tenantRegistryLoader;
    }

    protected void unsetRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        JmxTaskServiceComponent.tenantRegistryLoader = null;
    }

    public static TaskService getTaskService() {
        return taskService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static TenantRegistryLoader getTenantRegistryLoader() {
        return tenantRegistryLoader;
    }
}
