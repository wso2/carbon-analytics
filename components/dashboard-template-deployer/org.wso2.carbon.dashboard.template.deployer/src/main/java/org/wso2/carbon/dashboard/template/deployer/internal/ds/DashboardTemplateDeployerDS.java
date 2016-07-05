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

package org.wso2.carbon.dashboard.template.deployer.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.dashboard.template.deployer.DashboardTemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;

/**
 * @scr.component name="TemplateDeployer.dashboard.component" immediate="true"
 */
public class DashboardTemplateDeployerDS {
    private static final Log log = LogFactory.getLog(DashboardTemplateDeployerDS.class);

    protected void activate(ComponentContext context) {
        try {
            DashboardTemplateDeployer templateDeployer = new DashboardTemplateDeployer();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(), templateDeployer, null);
        } catch (RuntimeException e) {
            log.error("Couldn't register DashboardTemplateDeployer service", e);
        }
    }
}
