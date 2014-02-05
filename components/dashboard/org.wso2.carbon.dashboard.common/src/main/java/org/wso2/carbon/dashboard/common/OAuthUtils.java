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
package org.wso2.carbon.dashboard.common;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.common.oauth.GSOAuthModule;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ConfigurationContextService;

public class OAuthUtils {
    private static RegistryService registryService = null;

    private static GSOAuthModule.OAuthStoreProvider oauthStoreProvider = null;

    private static UserRealm userRealm = null;

    private static final Log log = LogFactory.getLog(OAuthUtils.class);
    private static ConfigurationContextService configContextService;

    public static UserRealm getUserRealm() throws Exception {
        if (userRealm == null) {
            throw new Exception("UserRealm is null");
        } else {
            return userRealm;
        }
    }

    public static void setUserRealm(UserRealm userRealm) {
        OAuthUtils.userRealm = userRealm;
    }

    public static void setRegistryService(RegistryService registryService) {
        OAuthUtils.registryService = registryService;
    }

    public static Registry getRegistry(int tenantId) throws Exception {
        if (registryService == null) {
            throw new Exception("Registry is null");
        }
        try {
            return registryService.getConfigSystemRegistry(tenantId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    public static ConfigurationContext getConfigContext() throws Exception {
        if (configContextService == null) {
            throw new Exception("ConfigurationContextService is null");
        }
        try {
            return configContextService.getServerConfigContext();
        } catch (Exception e) {
            log.error(e);
            throw new Exception(e);
        }
    }

    public static void setConfigContextService(ConfigurationContextService configContext) {
        OAuthUtils.configContextService = configContext;
    }

    public static GSOAuthModule.OAuthStoreProvider getOauthStoreProvider() {
        return oauthStoreProvider;
    }

    public static void setOauthStoreProvider(GSOAuthModule.OAuthStoreProvider oauthStoreProvider) {
        OAuthUtils.oauthStoreProvider = oauthStoreProvider;
    }

}
