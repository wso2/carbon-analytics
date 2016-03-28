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
package org.wso2.carbon.event.execution.manager.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.execution.manager.core.ExecutionManagerService;

/**
 * This class is used to get the EventProcessor service.
 *
 * @scr.component name="org.wso2.carbon.event.execution.manager.admin.ExecutionManagerAdminService" immediate="true"
 * @scr.reference name="executionManagerService.service"
 * interface="org.wso2.carbon.event.execution.manager.core.ExecutionManagerService" cardinality="1..1"
 * policy="dynamic" bind="setExecutionManagerService" unbind="unsetExecutionManagerService"
 */

public class ExecutionManagerAdminServiceDS {

    /**
     * Will be invoked when activating the service
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

    }

    protected void setExecutionManagerService(ExecutionManagerService executionManagerService) {
        ExecutionManagerAdminServiceValueHolder.setExecutorManagerService(executionManagerService);
    }

    protected void unsetExecutionManagerService(ExecutionManagerService executionManagerService) {
        ExecutionManagerAdminServiceValueHolder.setExecutorManagerService(null);
    }

}
