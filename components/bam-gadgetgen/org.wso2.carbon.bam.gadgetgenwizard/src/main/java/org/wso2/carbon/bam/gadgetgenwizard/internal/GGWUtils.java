package org.wso2.carbon.bam.gadgetgenwizard.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class GGWUtils {

    private static final Log log = LogFactory.getLog(GGWUtils.class);

    private static ConfigurationContextService configurationContextService;

    private static ServerConfigurationService serverConfiguration;

    private static DataSourceService dataSourceService;

    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public static void setDataSourceService(DataSourceService dataSourceService) {
        GGWUtils.dataSourceService = dataSourceService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        GGWUtils.configurationContextService = configurationContextService;
    }

    public static ServerConfigurationService getServerConfiguration() {
        return serverConfiguration;
    }

    public static void setServerConfiguration(ServerConfigurationService serverConfigurationService) {
        GGWUtils.serverConfiguration = serverConfigurationService;
    }
}
