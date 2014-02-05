package org.wso2.carbon.bam.toolbox.deployer.internal;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.dashboard.DashboardDSService;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.GadgetRepoService;

/**
 * @scr.component name="org.wso2.carbon.bam.toolbox.dashboard.service.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.dashboard.DashboardDSService"
 * interface="org.wso2.carbon.dashboard.DashboardDSService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDashboardService"
 * unbind="unsetDashboardService"
 * @scr.reference name="org.wso2.carbon.dashboard.mgt.gadgetrepo.GadgetRepoService"
 * interface="org.wso2.carbon.dashboard.mgt.gadgetrepo.GadgetRepoService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setGadgetRepoService"
 * unbind="unsetGadgetRepoService"
 */


public class DashboardServiceComponent {
    private static final Log log = LogFactory.getLog(DashboardServiceComponent.class);

    protected void activate(ComponentContext context) {
     log.info("Successfully setted dashboard services");
    }

    protected void setGadgetRepoService(GadgetRepoService gadgetRepoService) {
        ServiceHolder.setGadgetRepoService(gadgetRepoService);
    }

    protected void unsetGadgetRepoService(GadgetRepoService gadgetRepoService) {
        ServiceHolder.setGadgetRepoService(null);
    }

    protected void setDashboardService(DashboardDSService dashboardService) {
        ServiceHolder.setDashboardService(dashboardService);
    }

    protected void unsetDashboardService(DashboardDSService dashboardService) {
        ServiceHolder.setDashboardService(null);
    }
}
