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
package org.wso2.carbon.event.template.manager.core.internal.ds;

import org.wso2.carbon.event.template.manager.core.TemplateManagerService;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class consist of the values holders of RegistryService, EventStreamService, TemplateManagerService
 * and EventProcessorService which are required for the service operations
 */
public class TemplateManagerValueHolder {

    private static RegistryService registryService;
    private static TemplateManagerService templateManagerService;
    private static ConcurrentHashMap<String, TemplateDeployer> templateDeployers = new ConcurrentHashMap();
    private static EventStreamService eventStreamService;


    /**
     * To avoid instantiating
     */
    private TemplateManagerValueHolder() {

    }

    public static void setRegistryService(RegistryService registryService) {
        TemplateManagerValueHolder.registryService = registryService;
    }

    public static RegistryService getRegistryService() {
        return TemplateManagerValueHolder.registryService;
    }

    public static TemplateManagerService getTemplateManagerService() {
        return TemplateManagerValueHolder.templateManagerService;
    }

    public static void setTemplateManagerService(TemplateManagerService templateManagerService) {
        TemplateManagerValueHolder.templateManagerService = templateManagerService;
    }

    public static ConcurrentHashMap<String, TemplateDeployer> getTemplateDeployers() {
        return templateDeployers;
    }

    public static void setEventStreamService(EventStreamService eventStreamService) {
        TemplateManagerValueHolder.eventStreamService = eventStreamService;
    }

    public static EventStreamService getEventStreamService() {
        return TemplateManagerValueHolder.eventStreamService;
    }
}
