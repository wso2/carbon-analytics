/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.dashboard.themepopulator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;


public class ThemePopulatorContext {
    private static Log log = LogFactory.getLog(ThemePopulatorContext.class);

    private static RegistryService registryService = null;

    private static UserRealm userRealm = null;

    public static UserRealm getUserRealm() throws ThemePopulatorException {
        if (userRealm == null) {
            throw new ThemePopulatorException("UserRealm is null");
        } else {
            return userRealm;
        }
    }

    public static void setUserRealm(UserRealm userRealm) {
        ThemePopulatorContext.userRealm = userRealm;
    }

    public static void setRegistryService(RegistryService registryService) {
        ThemePopulatorContext.registryService = registryService;
    }

    public static Registry getRegistry(int tenantId) throws ThemePopulatorException {
        if (registryService == null) {
            throw new ThemePopulatorException("Registry is null");
        }
        try {
            return registryService.getConfigSystemRegistry(tenantId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ThemePopulatorException(e);
        }
    }
}
