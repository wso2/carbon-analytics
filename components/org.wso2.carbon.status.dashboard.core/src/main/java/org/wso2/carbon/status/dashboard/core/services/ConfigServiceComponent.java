/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.status.dashboard.core.services;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.AnalyticsHttpClientBuilderService;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.status.dashboard.core.bean.StatusDashboardConfiguration;
import org.wso2.carbon.status.dashboard.core.internal.DashboardDataHolder;

/**
 * This is OSGi-components to register config provider class.
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.core.services.ConfigServiceComponent",
        service = ConfigServiceComponent.class,
        immediate = true
)
public class ConfigServiceComponent {
    private static final Logger logger = LoggerFactory.getLogger(ConfigServiceComponent.class);

    public ConfigServiceComponent() {
    }

    @Activate
    protected void start(BundleContext bundleContext) {
        logger.info("Status dashboard config service component is activated.");
    }

    @Deactivate
    protected void stop() throws Exception {
        logger.info("Status dashboard config service component is deactivated.");
    }

    /**
     * Get the ConfigProvider service.
     * This is the bind method that gets called for ConfigProvider service registration that satisfy the policy.
     *
     * @param configProvider the ConfigProvider service that is registered as a service.
     */
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) throws ConfigurationException {
        DashboardDataHolder.getInstance().setConfigProvider(configProvider);
        StatusDashboardConfiguration dashboardConfigurations = configProvider
                .getConfigurationObject(StatusDashboardConfiguration.class);
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) ConfigProvider at " + ConfigServiceComponent.class.getName());
        }
        DashboardDataHolder.getInstance().setStatusDashboardConfiguration(dashboardConfigurations);
    }

    /**
     * This is the unbind method for the above reference that gets called for ConfigProvider instance un-registrations.
     *
     * @param configProvider the ConfigProvider service that get unregistered.
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) ConfigProvider at " + ConfigServiceComponent.class.getName());
        }
        DashboardDataHolder.getInstance().setConfigProvider(null);
        DashboardDataHolder.getInstance().setRolesProvider(null);
        DashboardDataHolder.getInstance().setStatusDashboardConfiguration(null);
    }

    @Reference(
            name = "carbon.anaytics.common.clientservice",
            service = AnalyticsHttpClientBuilderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterAnalyticsHttpClient"
    )
    protected void registerAnalyticsHttpClient(AnalyticsHttpClientBuilderService service) {
        DashboardDataHolder.getInstance().setClientBuilderService(service);
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) AnalyticsHttpClientBuilderService at " +
                         AnalyticsHttpClientBuilderService.class.getName());
        }
    }

    protected void unregisterAnalyticsHttpClient(ConfigProvider configProvider) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) AnalyticsHttpClientBuilderService at " +
                         AnalyticsHttpClientBuilderService.class.getName());
        }
        DashboardDataHolder.getInstance().setClientBuilderService(null);
    }

}
