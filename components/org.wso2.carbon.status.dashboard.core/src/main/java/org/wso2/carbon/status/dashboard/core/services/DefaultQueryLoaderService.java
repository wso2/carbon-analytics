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
import org.wso2.carbon.status.dashboard.core.bean.StatusDashboardConfiguration;
import org.wso2.carbon.status.dashboard.core.configuration.DefaultConfigurationBuilder;
import org.wso2.carbon.status.dashboard.core.internal.DashboardDataHolder;
import org.wso2.carbon.status.dashboard.core.internal.roles.provider.RolesProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Default query loader from the deployment yaml.
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.core.internal.config.loaderServiceComponent",
        service = DefaultQueryLoaderService.class,
        immediate = true
)
public class DefaultQueryLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultQueryLoaderService.class);

    public DefaultQueryLoaderService() {
    }

    private static DefaultQueryLoaderService instance = new DefaultQueryLoaderService();

    public static DefaultQueryLoaderService getInstance() {
        return instance;
    }

    @Activate
    protected void start(BundleContext bundleContext) {
        logger.info("Status dashboard default query loader component is activated.");
        StatusDashboardConfiguration dashboardDefaultConfiguration = DefaultConfigurationBuilder.getInstance().getConfiguration();
        StatusDashboardConfiguration resolvedConfiguration = resolveQueries(dashboardDefaultConfiguration);
        DashboardDataHolder.getInstance().setStatusDashboardConfiguration(resolvedConfiguration);
        RolesProvider rolesProvider = new RolesProvider(resolvedConfiguration);
        DashboardDataHolder.getInstance().setRolesProvider(rolesProvider);
    }

    @Deactivate
    protected void stop() throws Exception {
        logger.info("Status dashboard default query loader component is deactivated.");

    }

    private StatusDashboardConfiguration resolveQueries(StatusDashboardConfiguration defaultQueries) {
        StatusDashboardConfiguration deploymentQueries = DashboardDataHolder.getInstance()
                .getStatusDashboardConfiguration();
        if (deploymentQueries == null) {
            return defaultQueries;
        } else {
            StatusDashboardConfiguration resolvedConfiguration = new StatusDashboardConfiguration();
            String adminUsername = deploymentQueries.getUsername() == null ? defaultQueries.getUsername()
                    : deploymentQueries.getUsername();
            resolvedConfiguration.setUsername(adminUsername);
            String adminPassword = deploymentQueries.getPassword() == null ? defaultQueries.getPassword()
                    : deploymentQueries.getPassword();
            resolvedConfiguration.setPassword(adminPassword);
            Integer pollingInterval = deploymentQueries.getPollingInterval() == null ? defaultQueries.getPollingInterval()
                    : deploymentQueries.getPollingInterval();
            resolvedConfiguration.setPollingInterval(pollingInterval);

            String metricsDatasourceName = deploymentQueries.getMetricsDatasourceName() == null ?
                    defaultQueries.getMetricsDatasourceName()
                    : deploymentQueries.getMetricsDatasourceName();
            resolvedConfiguration.setMetricsDatasourceName(metricsDatasourceName);

            String dashboardDatasourceName = deploymentQueries.getDashboardDatasourceName() == null ?
                    defaultQueries.getDashboardDatasourceName()
                    : deploymentQueries.getDashboardDatasourceName();
            resolvedConfiguration.setDashboardDatasourceName(dashboardDatasourceName);
            List<String> sysAdminRoles=deploymentQueries.getSysAdminRoles() ;


            List<String> developerRoles=deploymentQueries.getDeveloperRoles();

            List<String> viewerRoles=deploymentQueries.getViewerRoles();
            if (sysAdminRoles== null) {
                resolvedConfiguration.setSysAdminRoles(new ArrayList<>());
            } else {
                resolvedConfiguration.setSysAdminRoles(sysAdminRoles);
            }

            if (developerRoles == null) {
                resolvedConfiguration.setDeveloperRoles(new ArrayList<>());
            }else {
                resolvedConfiguration.setDeveloperRoles(developerRoles);
            }
            if (viewerRoles== null) {
                resolvedConfiguration.setViewerRoles(new ArrayList<>());
            } else {
                resolvedConfiguration.setViewerRoles(viewerRoles);
            }
            if(deploymentQueries.getTypeMapping() != null) {
                defaultQueries.getTypeMapping().putAll(deploymentQueries.getTypeMapping());
                resolvedConfiguration.setTypeMapping(defaultQueries.getTypeMapping());
            }
            resolvedConfiguration.setTypeMapping(defaultQueries.getTypeMapping());
            if(deploymentQueries.getQueries() != null) {
                defaultQueries.getQueries().putAll(deploymentQueries.getQueries());
            }
            resolvedConfiguration.setQueries(defaultQueries.getQueries());
            return resolvedConfiguration;
        }


    }

    @Reference(
            name = "org.wso2.carbon.status.dashboard.core.services.ConfigServiceComponent",
            service = ConfigServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigSourceService"
    )
    protected void registerConfigSourceService(ConfigServiceComponent configServiceComponent) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) ConfigServiceComponent");
        }
    }

    protected void unregisterConfigSourceService(ConfigServiceComponent configServiceComponent) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) ConfigServiceComponent");
        }
    }
}
