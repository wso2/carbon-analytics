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
package org.wso2.carbon.event.receiver.template.deployer.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.template.deployer.EventReceiverTemplateDeployer;
import org.wso2.carbon.event.receiver.template.deployer.internal.EventReceiverTemplateDeployerValueHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;


/**
 * @scr.component name="TemplateDeployer.eventReceiver.component" immediate="true"
 * @scr.reference name="eventReceiverService.service"
 * interface="org.wso2.carbon.event.receiver.core.EventReceiverService" cardinality="1..1"
 * policy="dynamic" bind="setEventReceiverService" unbind="unsetEventReceiverService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class EventReceiverTemplateDeployerDS {

    private static final Log log = LogFactory.getLog(EventReceiverTemplateDeployerDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventReceiverTemplateDeployer templateDeployer = new EventReceiverTemplateDeployer();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(), templateDeployer, null);
        } catch (RuntimeException e) {
            log.error("Couldn't register EventReceiverTemplateDeployer service", e);
        }
    }

    protected void setEventReceiverService(EventReceiverService eventReceiverService) {
        EventReceiverTemplateDeployerValueHolder.setEventReceiverService(eventReceiverService);
    }

    protected void unsetEventReceiverService(EventReceiverService eventReceiverService) {
        EventReceiverTemplateDeployerValueHolder.setEventReceiverService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws
                                                                       RegistryException {
        EventReceiverTemplateDeployerValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventReceiverTemplateDeployerValueHolder.setRegistryService(null);
    }
}
