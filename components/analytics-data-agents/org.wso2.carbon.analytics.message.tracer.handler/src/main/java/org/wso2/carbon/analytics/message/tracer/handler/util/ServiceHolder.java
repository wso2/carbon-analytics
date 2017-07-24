/*
 * *
 *  * Copyright (c) 2005 - ${YEAR}, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package org.wso2.carbon.analytics.message.tracer.handler.util;

import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ServiceHolder {

    private static ConfigurationContextService configurationContextService;

    private static RegistryService registryService;

    private static EventStreamService eventStreamService;

    private ServiceHolder() {
        /* preventing initialization */
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        ServiceHolder.configurationContextService = configurationContextService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        ServiceHolder.registryService = registryService;
    }

    public static EventStreamService getEventStreamService() {
        return eventStreamService;
    }

    public static void setEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.eventStreamService = eventStreamService;
    }
}
