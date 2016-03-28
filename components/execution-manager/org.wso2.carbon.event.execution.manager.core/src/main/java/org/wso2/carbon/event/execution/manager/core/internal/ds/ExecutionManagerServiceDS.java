/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.execution.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.execution.manager.core.ExecutionManagerService;
import org.wso2.carbon.event.execution.manager.core.exception.ExecutionManagerException;
import org.wso2.carbon.event.execution.manager.core.internal.CarbonExecutionManagerService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * This class is used to get the EventProcessor service.
 *
 * @scr.component name="org.wso2.carbon.event.execution.manager.core.ExecutionManagerService" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class ExecutionManagerServiceDS {
    private static final Log log = LogFactory.getLog(ExecutionManagerServiceDS.class);

    protected void activate(ComponentContext context) {

        try {
            ExecutionManagerService executionManagerService = new CarbonExecutionManagerService();
            context.getBundleContext().registerService(ExecutionManagerService.class.getName(),
                    executionManagerService, null);
            ExecutionManagerValueHolder.setExecutionManagerService(executionManagerService);

            if (log.isDebugEnabled()) {
                log.debug("Execution manager core service deployed successfully");
            }
        } catch (RuntimeException e) {
            log.error("Execution manager core service cannot be deployed ", e);
        } catch (ExecutionManagerException e) {
            log.error("Execution manager core service cannot be deployed", e);
        }
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        ExecutionManagerValueHolder.setRegistryService(registryService);

    }

    protected void unsetRegistryService(RegistryService registryService) {
        ExecutionManagerValueHolder.setRegistryService(null);
    }

}
