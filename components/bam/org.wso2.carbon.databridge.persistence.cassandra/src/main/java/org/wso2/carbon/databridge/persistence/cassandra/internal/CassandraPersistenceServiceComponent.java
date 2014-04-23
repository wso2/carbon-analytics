/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.databridge.persistence.cassandra.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.persistence.cassandra.Utils.StreamDefinitionUtils;
import org.wso2.carbon.databridge.persistence.cassandra.datastore.CassandraConnector;
import org.wso2.carbon.databridge.persistence.cassandra.internal.util.ServiceHolder;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="databridge.cassandra.persistence.comp" immediate="true"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="dataaccess.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="databridge.core"
 * interface="org.wso2.carbon.databridge.core.DataBridgeReceiverService"
 * cardinality="1..1" policy="dynamic" bind="setDataBridgeReceiverService"  unbind="unsetDataBridgeReceiverService"
 *
 */
public class CassandraPersistenceServiceComponent {
    private static Log log = LogFactory.getLog(CassandraPersistenceServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext componentContext) {

        ServiceHolder.setCassandraConnector(new CassandraConnector());
        StreamDefinitionUtils.readConfigFile();
        serviceRegistration = componentContext.getBundleContext()
                .registerService(CassandraConnector.class.getName(), ServiceHolder.getCassandraConnector(), null);
        if (log.isDebugEnabled()) {
            log.debug("Started the Data Bridge Cassandra persistence component");
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        if (log.isDebugEnabled()) {
            log.debug("Stopped the Data Bridge Cassandra persistence component");
        }
    }

    protected void setRealmService(RealmService realmService) {
        ServiceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceHolder.setRealmService(null);
    }

    protected void setAuthenticationService(AuthenticationService authenticationService) {
        ServiceHolder.setAuthenticationService(authenticationService);
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        ServiceHolder.setAuthenticationService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws
            RegistryException {
        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(null);
    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        ServiceHolder.setDataAccessService(dataAccessService);
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        ServiceHolder.setDataAccessService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ServiceHolder.setConfigurationContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ServiceHolder.setConfigurationContextService(null);
    }

    protected void setDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.setDataBridgeReceiverService(dataBridgeReceiverService);
    }

    protected void unsetDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.setDataBridgeReceiverService(null);
    }


}
