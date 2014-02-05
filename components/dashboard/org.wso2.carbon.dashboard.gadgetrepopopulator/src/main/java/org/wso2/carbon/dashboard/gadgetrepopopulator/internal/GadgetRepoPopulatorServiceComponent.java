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

package org.wso2.carbon.dashboard.gadgetrepopopulator.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService;
import org.wso2.carbon.dashboard.gadgetrepopopulator.GadgetRepoPopulatorContext;
import org.wso2.carbon.dashboard.gadgetrepopopulator.GadgetRepoPopulator;
import org.wso2.carbon.dashboard.gadgetrepopopulator.TenantRepoPopulator;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;

/**
 * @scr.component name="org.wso2.carbon.dashboard.gadgetrepopopulator" immediate="true"
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
 */
public class GadgetRepoPopulatorServiceComponent {

    private static final Log log = LogFactory.getLog(GadgetRepoPopulatorServiceComponent.class);


    protected void activate(ComponentContext context) {
        log.debug("Gadget Repository Populator - bundle is activated ");

        // This is the initial server start. Populate for Tenant 0.
        GadgetRepoPopulator.populateRepo(MultitenantConstants.SUPER_TENANT_ID);
    }


    protected void deactivate(ComponentContext context) {
        log.debug("Gadget Repository Populator for Gadget Server bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        log.debug("Setting the Registry Service");
        GadgetRepoPopulatorContext.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        log.debug("Unsetting the Registry Service");
        GadgetRepoPopulatorContext.setRegistryService(null);
    }

    protected void setUserRealm(UserRealm userRealm) {
        GadgetRepoPopulatorContext.setUserRealm(userRealm);
    }

    protected void unsetUserRealm(UserRealm usrRealm) {
        GadgetRepoPopulatorContext.setUserRealm(null);
    }

    protected void setLoginSubscriptionManagerService(LoginSubscriptionManagerService loginManager) {
        log.debug("LoginSubscriptionManagerService is set");
        loginManager.subscribe(new TenantRepoPopulator());
    }

    protected void unsetLoginSubscriptionManagerService(LoginSubscriptionManagerService loginManager) {
        log.debug("LoginSubscriptionManagerService is unset");
    }
}
