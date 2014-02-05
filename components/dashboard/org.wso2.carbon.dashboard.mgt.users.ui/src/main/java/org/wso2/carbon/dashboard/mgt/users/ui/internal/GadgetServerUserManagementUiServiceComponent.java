/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.dashboard.mgt.users.ui.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.dashboard.mgt.users.ui.DashboardServlet;
import org.wso2.carbon.dashboard.mgt.users.ui.GadgetServerUserManagementUiServiceContext;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.base.ServerConfiguration;

/**
 * @scr.component name="org.wso2.carbon.dashboard.mgt.users.ui" immediate="true"
 * @scr.reference name="http.service"
 * interface="org.osgi.service.http.HttpService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setHttpService"
 * unbind="unsetHttpService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class GadgetServerUserManagementUiServiceComponent {
    private static final Log log =
            LogFactory.getLog(GadgetServerUserManagementUiServiceComponent.class);

    private HttpService httpServiceInstance;

    protected void activate(ComponentContext context) {
        try {
            // registering the portal servlet
            DashboardServlet dashboardServlet = new DashboardServlet();
            httpServiceInstance.registerServlet("/portal", dashboardServlet, null, null);

            GadgetServerUserManagementUiServiceContext.setServerConfiguration(ServerConfiguration.getInstance());
            log.debug(
                    "******* Dashboard User Management UI Component bundle is activated ******* ");

        } catch (Exception e) {
            log.debug(
                    "******* Failed to activate Dashboard User Management UI Component bundle ******* ");
        }
    }

    protected void deactivate(ComponentContext context) {

        log.debug("******* Dashboard User Management UI Component bundle is deactivated ******* ");
          GadgetServerUserManagementUiServiceContext.setServerConfiguration(null);
    }

    protected void setHttpService(HttpService httpService) {
        this.httpServiceInstance = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpServiceInstance = null;
    }

        protected void setConfigurationContextService(ConfigurationContextService configCtx) {
        GadgetServerUserManagementUiServiceContext.setConfigContextService(configCtx);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtx) {
        GadgetServerUserManagementUiServiceContext.setConfigContextService(null);
    }
}
