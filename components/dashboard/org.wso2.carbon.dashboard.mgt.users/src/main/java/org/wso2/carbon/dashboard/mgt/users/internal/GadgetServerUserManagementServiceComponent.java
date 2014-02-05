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

package org.wso2.carbon.dashboard.mgt.users.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService;
import org.wso2.carbon.dashboard.mgt.users.GadgetServerUserManagementContext;
import org.wso2.carbon.dashboard.mgt.users.GadgetServerUserManagementService;
import org.wso2.carbon.dashboard.mgt.users.TenantAdminDataPopulator;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;


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
 * @scr.reference name="login.subscription.service"
 * interface="org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService"
 * cardinality="0..1"
 * policy="dynamic"
 * bind="setLoginSubscriptionManagerService"
 * unbind="unsetLoginSubscriptionManagerService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class GadgetServerUserManagementServiceComponent {

    private static final Log log =
            LogFactory.getLog(GadgetServerUserManagementServiceComponent.class);

    protected void activate(ComponentContext context) {
        try {

            if (!GadgetServerUserManagementContext.isPortalPermissionsSet(MultitenantConstants.SUPER_TENANT_ID)) {
                // Setting self registration on by default
                GadgetServerUserManagementContext.setSelfRegistration(true, MultitenantConstants.SUPER_TENANT_ID);

                // Seting external Gadget addition to true by default
                GadgetServerUserManagementContext.setExternalGadgetAddition(true, MultitenantConstants.SUPER_TENANT_ID);

                GadgetServerUserManagementContext.setAnonModeState(true, MultitenantConstants.SUPER_TENANT_ID);
            }

            log.debug("Gadget Server User Management Component Backend bundle is activated ");

        } catch (Exception e) {
            log.debug("Gadget Server User Management Component Backend Component bundle ");
        }
    }

    protected void deactivate(ComponentContext context) {
        log.debug("Gadget Server User Management Component Backend bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Registry Service");
        }
        GadgetServerUserManagementContext.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Registry Service");
        }
        GadgetServerUserManagementContext.setRegistryService(null);
    }

    protected void setUserRealm(UserRealm userRealm) {
        GadgetServerUserManagementContext.setUserRealm(userRealm);
    }

    protected void unsetUserRealm(UserRealm usrRealm) {
        GadgetServerUserManagementContext.setUserRealm(null);
    }

    protected void setLoginSubscriptionManagerService(LoginSubscriptionManagerService loginManager) {
        log.debug("LoginSubscriptionManagerService is set");
        loginManager.subscribe(new TenantAdminDataPopulator());
    }

    protected void unsetLoginSubscriptionManagerService(LoginSubscriptionManagerService loginManager) {
        log.debug("LoginSubscriptionManagerService is unset");
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtx) {
        GadgetServerUserManagementContext.setConfigContextService(configCtx);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtx) {
        GadgetServerUserManagementContext.setConfigContextService(null);
    }
}
