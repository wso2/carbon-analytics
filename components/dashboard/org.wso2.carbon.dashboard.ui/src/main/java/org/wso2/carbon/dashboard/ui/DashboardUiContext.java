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
package org.wso2.carbon.dashboard.ui;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.common.oauth.RegistryBasedOAuthStore;
import org.wso2.carbon.utils.ConfigurationContextService;

public class DashboardUiContext {

    private static final Log log = LogFactory.getLog(DashboardUiContext.class);

    private static ConfigurationContextService configContextService = null;

    private static RegistryBasedOAuthStore oauthStore = null;

    public static RegistryBasedOAuthStore getOauthStore() {
        return oauthStore;
    }

    public static void setOauthStore(RegistryBasedOAuthStore oauthStore) {
        DashboardUiContext.oauthStore = oauthStore;
    }

    public static ConfigurationContext getConfigContext() throws DashboardUiException {
        if (configContextService == null) {
            throw new DashboardUiException("ConfigurationContextService is null");
        }

        return configContextService.getServerConfigContext();

    }

    public static void setConfigContextService(ConfigurationContextService configContext) {
        DashboardUiContext.configContextService = configContext;
    }
}
