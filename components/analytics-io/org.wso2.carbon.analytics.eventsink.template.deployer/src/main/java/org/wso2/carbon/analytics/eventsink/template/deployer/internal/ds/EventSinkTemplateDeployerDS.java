/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.eventsink.template.deployer.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventSinkService;
import org.wso2.carbon.analytics.eventsink.template.deployer.EventSinkTemplateDeployer;
import org.wso2.carbon.analytics.eventsink.template.deployer.internal.EventSinkTemplateDeployerValueHolder;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "TemplateDeployer.eventSink.component", 
         immediate = true)
public class EventSinkTemplateDeployerDS {

    private static final Log log = LogFactory.getLog(EventSinkTemplateDeployerDS.class);

    @Activate
    protected void activate(ComponentContext context) {
        try {
            EventSinkTemplateDeployer templateDeployer = new EventSinkTemplateDeployer();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(), templateDeployer, null);
        } catch (RuntimeException e) {
            log.error("Couldn't register EventSinkTemplateDeployer service", e);
        }
    }

    @Reference(
             name = "analyticsEventSinkService.service", 
             service = org.wso2.carbon.analytics.eventsink.AnalyticsEventSinkService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAnalyticsEventSinkService")
    protected void setAnalyticsEventSinkService(AnalyticsEventSinkService analyticsEventSinkService) {
        EventSinkTemplateDeployerValueHolder.setAnalyticsEventSinkService(analyticsEventSinkService);
    }

    protected void unsetAnalyticsEventSinkService(AnalyticsEventSinkService analyticsEventSinkService) {
        EventSinkTemplateDeployerValueHolder.setAnalyticsEventSinkService(null);
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventSinkTemplateDeployerValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventSinkTemplateDeployerValueHolder.setRegistryService(null);
    }
}

