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
package org.wso2.carbon.event.execution.manager.stream.template.deployer.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.stream.template.deployer.EventStreamDeployerImpl;
import org.wso2.carbon.event.execution.manager.stream.template.deployer.internal.EventStreamDeployerValueHolder;
import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * This class is used to get the Event Stream service.
 *
 * @scr.component name="eventStreamDeployer.component" immediate="true"
 * @scr.reference name="eventStreamService.service"
 * interface="org.wso2.carbon.event.stream.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 */
public class EventStreamDeployerDS {

    private static final Log log = LogFactory.getLog(EventStreamDeployerDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventStreamDeployerImpl streamDeployerService = new EventStreamDeployerImpl();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(),
                    streamDeployerService, null);
            //EventStreamDeployerValueHolder.setEventStreamDeployerService(streamDeployerService);

            if (log.isDebugEnabled()) {
                log.debug("Event Stream deployer service started successfully");
            }
        } catch (RuntimeException e) {
            log.error("Event Stream deployer service cannot be deployed ", e);
        }
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        EventStreamDeployerValueHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        EventStreamDeployerValueHolder.setEventStreamService(null);
    }
}
