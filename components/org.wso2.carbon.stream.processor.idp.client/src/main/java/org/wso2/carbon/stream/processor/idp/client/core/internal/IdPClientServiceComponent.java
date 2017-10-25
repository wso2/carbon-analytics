/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.stream.processor.idp.client.core.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.startupresolver.StartupServiceUtils;
import org.wso2.carbon.stream.processor.idp.client.core.IdPClientManager;
import org.wso2.carbon.stream.processor.idp.client.core.api.IdPClient;
import org.wso2.carbon.stream.processor.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.stream.processor.idp.client.core.spi.IdPClientInitializer;

import java.util.HashMap;
import java.util.Map;

@Component(
        name = "IdPClientServiceComponent",
        immediate = true,
        property = {
                "componentName=" + IdPClientServiceComponent.COMPONENT_NAME
        }
)
public class IdPClientServiceComponent implements RequiredCapabilityListener {
    public static final String COMPONENT_NAME = "sp-idp-service";
    private static final Logger LOG = LoggerFactory.getLogger(IdPClientServiceComponent.class);

    private ServiceRegistration serviceRegistration;
    private BundleContext bundleContext;
    private ConfigProvider configProvider;
    private Map<String, IdPClientInitializer> initializers = new HashMap<>();

    @Activate
    protected void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Deactivate
    protected void stop() {
        LOG.info("IdPClient Component is deactivated...");
        if (this.serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        this.configProvider = null;
    }


    @Reference(
            name = "sp-idp-reader",
            service = IdPClientInitializer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterReader"
    )
    protected void registerReader(IdPClientInitializer initializer) {
        initializers.put(initializer.getType(), initializer);
        StartupServiceUtils.updateServiceCache(COMPONENT_NAME, IdPClientInitializer.class);
    }

    protected void unregisterReader(IdPClientInitializer initializer) {
        initializers.remove(initializer.getType());
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        LOG.info("IdPClientServiceComponent is activated...");
        try {
            IdPClientManager idPClientManager = new IdPClientManager();
            idPClientManager.initIdPClient(configProvider, initializers);

            IdPClient idPClient = idPClientManager.getIdPClient();
            this.serviceRegistration = bundleContext.registerService(IdPClient.class.getName(), idPClient, null);
        } catch (IdPClientException e) {
            LOG.error("Error occurred while initializing IdP Client", e);
        }
    }
}
