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

package org.wso2.carbon.dashboard.mgt.users.ui;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.ConfigurationContextService;

public class GadgetServerUserManagementUiServiceContext {
    private static final Log log = LogFactory.getLog(GadgetServerUserManagementUiServiceContext.class);

    private static ServerConfiguration serverConfiguration;
    private static ConfigurationContextService configContextService = null;

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
        GadgetServerUserManagementUiServiceContext.configContextService = configContext;
    }

    public static ServerConfiguration getServerConfiguration() {
        return GadgetServerUserManagementUiServiceContext.serverConfiguration;
    }

    public static void setServerConfiguration(ServerConfiguration serverConfiguration) {
        GadgetServerUserManagementUiServiceContext.serverConfiguration = serverConfiguration;
    }

    public static String getRootContext() throws Exception {
        return GadgetServerUserManagementUiServiceContext.getConfigContext().getContextRoot();
    }
}
