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

package org.wso2.carbon.dashboard.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.dashboard.DashboardContext;
import org.wso2.carbon.dashboard.DashboardDSService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.dashboard" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="user.realm.delegating"
 * interface="org.wso2.carbon.user.core.UserRealm"
 * cardinality="1..1"
 * policy="dynamic"
 * target="(RealmGenre=Delegating)"
 * bind="setUserRealm"
 * unbind="unsetUserRealm"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class DashboardServiceComponent {

    private static final Log log = LogFactory.getLog(DashboardServiceComponent.class);

    protected void activate(ComponentContext context) {
        try {

            /*// Adding the Dashboard User role
            UserStoreAdmin usAdmin = DashboardContext.getUserRealm().getUserStoreAdmin();
            usAdmin.addRole(DashboardConstants.DASHBOARD_MANAGER_ROLE);

            // Adding login permission to this role
            AccessControlAdmin acAdmin = DashboardContext.getUserRealm().getAccessControlAdmin();
            acAdmin.authorizeRole(DashboardConstants.DASHBOARD_MANAGER_ROLE, "System", "login");

            log.info("Role '" + DashboardConstants.DASHBOARD_MANAGER_ROLE + "' added with login permissions.");*/
             context.getBundleContext().registerService(
                    DashboardDSService.class.getName(),
                    new DashboardDSService(), null);
            log.debug("Dashboard Backend Component bundle is activated ");

        } catch (Exception e) {
            log.debug("Failed to activate Dashboard Backend Component bundle ");
        }
    }

    protected void deactivate(ComponentContext context) {
        log.debug("Dashboard Backend Component bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Registry Service");
        }
        DashboardContext.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Registry Service");
        }
        DashboardContext.setRegistryService(null);
    }

    protected void setUserRealm(UserRealm userRealm) {
        DashboardContext.setUserRealm(userRealm);
    }

    protected void unsetUserRealm(UserRealm usrRealm) {
        DashboardContext.setUserRealm(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtx) {
        DashboardContext.setConfigContextService(configCtx);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtx) {
        DashboardContext.setConfigContextService(null);
    }
}
