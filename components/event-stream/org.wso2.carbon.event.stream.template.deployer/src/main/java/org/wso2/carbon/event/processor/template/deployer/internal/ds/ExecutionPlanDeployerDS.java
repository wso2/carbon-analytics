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
package org.wso2.carbon.event.processor.template.deployer.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.processor.template.deployer.ExecutionPlanTemplateDeployer;
import org.wso2.carbon.event.processor.template.deployer.internal.ExecutionPlanDeployerValueHolder;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.stream.core.EventStreamService;


/**
 * @scr.component name="TemplateDeployer.realtime.component" immediate="true"
 * @scr.reference name="eventStreamService.service"
 * interface="org.wso2.carbon.event.stream.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="eventProcessorService.service"
 * interface="org.wso2.carbon.event.processor.core.EventProcessorService" cardinality="1..1"
 * policy="dynamic" bind="setEventProcessorService" unbind="unsetEventProcessorService"
 */
public class ExecutionPlanDeployerDS {

    private static final Log log = LogFactory.getLog(ExecutionPlanDeployerDS.class);

    protected void activate(ComponentContext context) {

        try {
            ExecutionPlanTemplateDeployer templateDeployer = new ExecutionPlanTemplateDeployer();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(), templateDeployer, null);

        } catch (RuntimeException e) {
            log.error("Couldn't register ExecutionPlanTemplateDeployer service", e);
        }
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        ExecutionPlanDeployerValueHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        ExecutionPlanDeployerValueHolder.setEventStreamService(null);
    }

    public void setEventProcessorService(EventProcessorService eventProcessorService) {
        ExecutionPlanDeployerValueHolder.setEventProcessorService(eventProcessorService);
    }

    public void unsetEventProcessorService(EventProcessorService eventProcessorService) {
        ExecutionPlanDeployerValueHolder.setEventProcessorService(null);
    }
}
