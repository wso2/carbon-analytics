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

package org.wso2.carbon.dashboard.mgt.gadgetrepo.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.GadgetRepoContext;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.GadgetRepoService;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.handlers.GadgetZipUploadHandler;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;

/**
 * @scr.component name="org.wso2.carbon.dashboard.mgt.gadgetrepo"
 *                immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realm.delegating"
 *                interface="org.wso2.carbon.user.core.UserRealm"
 *                cardinality="1..1" policy="dynamic"
 *                target="(RealmGenre=Delegating)" bind="setUserRealm"
 *                unbind="unsetUserRealm"
 */
public class GadgetRepoServiceComponent {

	private static final Log log = LogFactory
			.getLog(GadgetRepoServiceComponent.class);

	protected void activate(ComponentContext context) {
        try {
            log.debug("Gadget Repository Backend Component bundle is activated");
            GadgetRepoContext.getRegistryService().getConfigSystemRegistry().getRegistryContext().getHandlerManager().
                    addHandler(null, new MediaTypeMatcher(DashboardConstants.GADGET_MEDIA_TYPE),
                            new GadgetZipUploadHandler());
            context.getBundleContext().registerService(
                    GadgetRepoService.class.getName(),
                    new GadgetRepoService(), null);

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
		GadgetRepoContext.setRegistryService(registryService);
	}

	protected void unsetRegistryService(RegistryService registryService) {
		if (log.isDebugEnabled()) {
			log.info("Unsetting the Registry Service");
		}
		GadgetRepoContext.setRegistryService(null);
	}

	protected void setUserRealm(UserRealm userRealm) {
		GadgetRepoContext.setUserRealm(userRealm);
	}

	protected void unsetUserRealm(UserRealm usrRealm) {
		GadgetRepoContext.setUserRealm(null);
	}

}
