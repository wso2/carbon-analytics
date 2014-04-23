package org.wso2.carbon.databridge.persistence.cassandra.internal.util;

import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.persistence.cassandra.datastore.CassandraConnector;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
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
public class ServiceHolder {
    private static RealmService realmService;
    private static AuthenticationService authenticationService;
    private static RegistryService registryService;
    private static DataAccessService dataAccessService;

    private static CassandraConnector cassandraConnector;

    private static ConfigurationContextService configurationContextService;

    private static DataBridgeReceiverService dataBridgeReceiverService;


    public static CassandraConnector getCassandraConnector() {
        return cassandraConnector;
    }

    public static void setCassandraConnector(CassandraConnector cassandraConnector) {
        ServiceHolder.cassandraConnector = cassandraConnector;
    }

    public static DataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public static void setDataAccessService(DataAccessService dataAccessService) {
        ServiceHolder.dataAccessService = dataAccessService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        ServiceHolder.realmService = realmService;
    }

    public static AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public static void setAuthenticationService(AuthenticationService authenticationService) {
        ServiceHolder.authenticationService = authenticationService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        ServiceHolder.registryService = registryService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        ServiceHolder.configurationContextService = configurationContextService;
    }

    public static DataBridgeReceiverService getDataBridgeReceiverService() {
        return dataBridgeReceiverService;
    }

    public static void setDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.dataBridgeReceiverService = dataBridgeReceiverService;
    }

}
