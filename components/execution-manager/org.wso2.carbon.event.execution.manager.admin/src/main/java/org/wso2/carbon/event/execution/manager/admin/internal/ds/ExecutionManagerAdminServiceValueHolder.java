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

import org.wso2.carbon.event.execution.manager.core.ExecutionManagerService;
import org.wso2.carbon.event.stream.deployer.EventStreamDeployer;

/**
 * Consist of ExecutionManagerService which requires by ExecutionAdminService
 */
public class ExecutionManagerAdminServiceValueHolder {

    private static ExecutionManagerService executorManagerService;
    private static EventStreamDeployer eventStreamDeployerService;

    /**
     * To avoid instantiating
     */
    private ExecutionManagerAdminServiceValueHolder() {
    }

    public static ExecutionManagerService getCarbonExecutorManagerService() {
        return executorManagerService;
    }

    public static EventStreamDeployer getEventStreamDeployerService() {
        return eventStreamDeployerService;
    }

    public static void setExecutorManagerService(ExecutionManagerService executorManagerService) {
        ExecutionManagerAdminServiceValueHolder.executorManagerService = executorManagerService;
    }

    public static void setEventStreamDeployerService(EventStreamDeployer eventStreamDeployerService) {
        ExecutionManagerAdminServiceValueHolder.eventStreamDeployerService = eventStreamDeployerService;
    }
}
