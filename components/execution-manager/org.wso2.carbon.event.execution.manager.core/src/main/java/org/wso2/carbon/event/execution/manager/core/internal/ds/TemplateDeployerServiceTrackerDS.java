/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.execution.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;

/**
 * @scr.component name="template.deployer.service.tracker.component" immediate="true"
 * @scr.reference name="template.deployer.tracker.service"
 * interface="org.wso2.carbon.event.execution.manager.core.TemplateDeployer" cardinality="0..n"
 * policy="dynamic" bind="setTemplateDeployer" unbind="unSetTemplateDeployer"
 */

public class TemplateDeployerServiceTrackerDS {

    private static final Log log = LogFactory.getLog(TemplateDeployerServiceTrackerDS.class);

    /**
     * initialize the Template deployer core service here.
     *
     * @param context bundle context
     */
    protected void activate(ComponentContext context) {
        try {
            log.info("Successfully deployed the execution manager tracker service");
        } catch (RuntimeException e) {
            log.error("Can not create the execution manager tracker service ", e);
        }
    }

    protected void setTemplateDeployer(
            TemplateDeployer templateDeployer) {
        try {
            ExecutionManagerValueHolder.getTemplateDeployers().put(templateDeployer.getType(), templateDeployer);
        } catch (Throwable t) {
            log.error(t);
        }
    }

    protected void unSetTemplateDeployer(
            TemplateDeployer templateDeployer) {
        ExecutionManagerValueHolder.getTemplateDeployers().remove(templateDeployer.getType());
    }
}

