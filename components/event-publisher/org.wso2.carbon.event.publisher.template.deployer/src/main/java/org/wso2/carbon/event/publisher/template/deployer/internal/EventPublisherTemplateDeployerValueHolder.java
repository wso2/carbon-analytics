/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.publisher.template.deployer.internal;

import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.registry.core.service.RegistryService;

public class EventPublisherTemplateDeployerValueHolder {

    private static EventReceiverService eventReceiverService;
    private static RegistryService registryService;

    public static void setEventReceiverService(EventReceiverService eventReceiverService) {
        EventPublisherTemplateDeployerValueHolder.eventReceiverService = eventReceiverService;
    }

    public static EventReceiverService getEventReceiverService() {
        return EventPublisherTemplateDeployerValueHolder.eventReceiverService;
    }

    public static void setRegistryService(RegistryService registryService) {
        EventPublisherTemplateDeployerValueHolder.registryService = registryService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }
}
