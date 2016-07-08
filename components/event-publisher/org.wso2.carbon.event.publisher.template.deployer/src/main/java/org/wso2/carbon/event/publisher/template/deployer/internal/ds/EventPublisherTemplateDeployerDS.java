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
package org.wso2.carbon.event.publisher.template.deployer.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.template.deployer.EventPublisherTemplateDeployer;
import org.wso2.carbon.event.publisher.template.deployer.internal.EventPublisherTemplateDeployerValueHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;


/**
 * @scr.component name="TemplateDeployer.eventPublisher.component" immediate="true"
 * @scr.reference name="eventPublisherService.service"
 * interface="org.wso2.carbon.event.publisher.core.EventPublisherService" cardinality="1..1"
 * policy="dynamic" bind="setEventPublisherService" unbind="unsetEventPublisherService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class EventPublisherTemplateDeployerDS {

    private static final Log log = LogFactory.getLog(EventPublisherTemplateDeployerDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventPublisherTemplateDeployer templateDeployer = new EventPublisherTemplateDeployer();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(), templateDeployer, null);
        } catch (RuntimeException e) {
            log.error("Couldn't register EventPublisherTemplateDeployer service", e);
        }
    }

    protected void setEventPublisherService(EventPublisherService eventPublisherService) {
        EventPublisherTemplateDeployerValueHolder.setEventPublisherService(eventPublisherService);
    }

    protected void unsetEventPublisherService(EventPublisherService eventPublisherService) {
        EventPublisherTemplateDeployerValueHolder.setEventPublisherService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws
                                                                       RegistryException {
        EventPublisherTemplateDeployerValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventPublisherTemplateDeployerValueHolder.setRegistryService(null);
    }
}
