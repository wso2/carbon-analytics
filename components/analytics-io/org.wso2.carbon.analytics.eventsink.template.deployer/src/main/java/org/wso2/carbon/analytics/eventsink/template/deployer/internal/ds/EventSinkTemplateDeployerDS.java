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

/**
 * @scr.component name="TemplateDeployer.eventSink.component" immediate="true"
 * @scr.reference name="analyticsEventSinkService.service"
 * interface="org.wso2.carbon.analytics.eventsink.AnalyticsEventSinkService" cardinality="1..1"
 * policy="dynamic" bind="setAnalyticsEventSinkService" unbind="unsetAnalyticsEventSinkService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class EventSinkTemplateDeployerDS {

    private static final Log log = LogFactory.getLog(EventSinkTemplateDeployerDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventSinkTemplateDeployer templateDeployer = new EventSinkTemplateDeployer();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(), templateDeployer, null);
        } catch (RuntimeException e) {
            log.error("Couldn't register EventSinkTemplateDeployer service", e);
        }
    }

    protected void setAnalyticsEventSinkService(AnalyticsEventSinkService analyticsEventSinkService) {
        EventSinkTemplateDeployerValueHolder.setAnalyticsEventSinkService(analyticsEventSinkService);
    }

    protected void unsetAnalyticsEventSinkService(AnalyticsEventSinkService analyticsEventSinkService) {
        EventSinkTemplateDeployerValueHolder.setAnalyticsEventSinkService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws
                                                                       RegistryException {
        EventSinkTemplateDeployerValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventSinkTemplateDeployerValueHolder.setRegistryService(null);
    }
}
