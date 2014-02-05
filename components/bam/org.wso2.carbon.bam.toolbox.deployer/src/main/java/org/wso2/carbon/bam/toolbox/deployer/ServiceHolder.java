/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.toolbox.deployer;

import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.dashboard.DashboardDSService;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.GadgetRepoService;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ServiceHolder {
    private ServiceHolder() {

    }

    private static ConfigurationContextService configurationContextService;
    private static RegistryService registryService;
    private static UserRealm userRealm;
    private static RealmService realmService;
    private static DashboardDSService dashboardService;
    private static ServerConfigurationService serverConfiguration;
    private static GadgetRepoService gadgetRepoService;
    private static DataSourceService dataSourceService;
    private static DataBridgeReceiverService dataBridgeReceiverService;

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }


    public static void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        ServiceHolder.configurationContextService = configurationContextService;
    }

    public static Registry getRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    public static void setRegistryService(RegistryService registryService) {
        ServiceHolder.registryService = registryService;
    }

    public static void setUserRealm(UserRealm realmService) {
        ServiceHolder.userRealm = realmService;
    }

    public static UserRealm getUserRealm() {
        return userRealm;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        ServiceHolder.realmService = realmService;
    }

    public static DashboardDSService getDashboardService() {
        return dashboardService;
    }

    public static void setDashboardService(DashboardDSService dashboardService) {
        ServiceHolder.dashboardService = dashboardService;
    }

    public static ServerConfigurationService getServerConfiguration() {
        return serverConfiguration;
    }

    public static void setServerConfiguration(ServerConfigurationService serverConf) {
        serverConfiguration = serverConf;
    }

    public static GadgetRepoService getGadgetRepoService() {
        return gadgetRepoService;
    }

    public static void setGadgetRepoService(GadgetRepoService gadgetRepoService) {
        ServiceHolder.gadgetRepoService = gadgetRepoService;
    }

    public static Registry getGovernanceSystemRegistry(int tenantId) throws RegistryException {
        return ServiceHolder.registryService.getGovernanceSystemRegistry(tenantId);
    }

    public static void setDataSourceService(DataSourceService dataSourceService) {
        ServiceHolder.dataSourceService = dataSourceService;
    }

    public static DataSourceService getDataSourceService() {
        return ServiceHolder.dataSourceService;
    }

    public static DataBridgeReceiverService getDataBridgeReceiverService() {
        return dataBridgeReceiverService;
    }

    public static void setDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.dataBridgeReceiverService = dataBridgeReceiverService;
    }
}


