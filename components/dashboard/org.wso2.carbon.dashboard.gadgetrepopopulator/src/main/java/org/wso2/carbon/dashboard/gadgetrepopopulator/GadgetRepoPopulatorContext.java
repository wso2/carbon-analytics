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

package org.wso2.carbon.dashboard.gadgetrepopopulator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;

public class GadgetRepoPopulatorContext {
    private static Log log = LogFactory.getLog(GadgetRepoPopulatorContext.class);

    private static RegistryService registryService = null;

    private static UserRealm userRealm = null;

    public static UserRealm getUserRealm() throws GadgetRepoPopulatorException {
        if (userRealm == null) {
            throw new GadgetRepoPopulatorException("UserRealm is null");
        } else {
            return userRealm;
        }
    }

    public static void setUserRealm(UserRealm userRealm) {
        GadgetRepoPopulatorContext.userRealm = userRealm;
    }

    public static void setRegistryService(RegistryService registryService) {
        GadgetRepoPopulatorContext.registryService = registryService;
    }

    public static Registry getRegistry(int tenantId) throws GadgetRepoPopulatorException {
        if (registryService == null) {
            throw new GadgetRepoPopulatorException("Registry is null");
        }
        try {
            return registryService.getConfigSystemRegistry(tenantId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new GadgetRepoPopulatorException(e);
        }
    }
}
