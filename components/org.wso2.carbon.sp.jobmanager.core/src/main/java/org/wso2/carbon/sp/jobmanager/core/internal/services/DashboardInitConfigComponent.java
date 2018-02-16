/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.jobmanager.core.internal.services;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.sp.jobmanager.core.configuration.DefaultConfigurationBuilder;
import org.wso2.carbon.sp.jobmanager.core.dbhandler.ManagerDeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.internal.ManagerDataHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * This component handle the all the initialization tasks.
 */
@Component(
        service = DashboardInitConfigComponent.class,
        immediate = true
)
public class DashboardInitConfigComponent {
    private static final Logger logger = LoggerFactory.getLogger(DashboardInitConfigComponent.class);

    @Activate
    protected void start(BundleContext bundleContext) {
        if (logger.isDebugEnabled()) {
            logger.info("DashboardInitConfigComponent started successfully");
        }
        try {
            ManagerDeploymentConfig deploymentConfigurations = ManagerDataHolder.getInstance()
                    .getConfigProvider().getConfigurationObject(ManagerDeploymentConfig.class);
            ManagerDeploymentConfig dashboardDefaultConfiguration = DefaultConfigurationBuilder.getInstance()
                    .getConfiguration();
            ManagerDeploymentConfig resolvedConfiguration = mergeQueries(dashboardDefaultConfiguration,
                                                                         deploymentConfigurations);
            ManagerDataHolder.getInstance().setManagerDeploymentConfig(resolvedConfiguration);
        } catch (ConfigurationException e) {
            logger.error("Error in reading configuration from the deployment.YML", e);
        }

    }

    @Deactivate
    protected void stop() {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) DashboardInitConfigComponent");
        }
        ManagerDataHolder.getInstance().setManagerDeploymentConfig(null);
    }

    /**
     * Default and deployment Query merger.
     * @param defaultQueries
     * @return
     */
    private ManagerDeploymentConfig mergeQueries(ManagerDeploymentConfig defaultQueries,
                                                 ManagerDeploymentConfig deploymentQueries) {
        if (deploymentQueries == null) {
            return defaultQueries;
        } else {
            ManagerDeploymentConfig resolvedConfiguration = new ManagerDeploymentConfig();
            String adminUsername = deploymentQueries.getUsername() == null ? defaultQueries.getUsername()
                    : deploymentQueries.getUsername();
            resolvedConfiguration.setUsername(adminUsername);
            String adminPassword = deploymentQueries.getPassword() == null ? defaultQueries.getPassword()
                    : deploymentQueries.getPassword();
            resolvedConfiguration.setPassword(adminPassword);
            Integer pollingInterval =
                    deploymentQueries.getPollingInterval() == null ? defaultQueries.getPollingInterval()
                            : deploymentQueries.getPollingInterval();
            resolvedConfiguration.setPollingInterval(pollingInterval);

            String dashboardManagerDatasourceName = deploymentQueries.getDashboardManagerDatasourceName() == null ?
                    defaultQueries.getDashboardManagerDatasourceName()
                    : deploymentQueries.getDashboardManagerDatasourceName();
            resolvedConfiguration.setDashboardManagerDatasourceName(dashboardManagerDatasourceName);

            int connectionTimeout = deploymentQueries.getManagerConnectionConfigurations().getConnectionTimeOut() ==
                    null ? defaultQueries.getManagerConnectionConfigurations().getConnectionTimeOut()
                    : deploymentQueries.getManagerConnectionConfigurations().getConnectionTimeOut();

            int readTimeOut = deploymentQueries.getManagerConnectionConfigurations().getReadTimeOut() == null ?
                    defaultQueries.getManagerConnectionConfigurations().getReadTimeOut()
                    : deploymentQueries.getManagerConnectionConfigurations().getReadTimeOut();

            resolvedConfiguration.setManagerConnectionConfigurations(connectionTimeout, readTimeOut);

            List<String> sysAdminRoles = deploymentQueries.getSysAdminRoles();

            List<String> developerRoles = deploymentQueries.getDeveloperRoles();

            List<String> viewerRoles = deploymentQueries.getViewerRoles();
            if (sysAdminRoles == null) {
                resolvedConfiguration.setSysAdminRoles(new ArrayList<>());
            } else {
                resolvedConfiguration.setSysAdminRoles(sysAdminRoles);
            }

            if (developerRoles == null) {
                resolvedConfiguration.setDeveloperRoles(new ArrayList<>());
            } else {
                resolvedConfiguration.setDeveloperRoles(developerRoles);
            }
            if (viewerRoles == null) {
                resolvedConfiguration.setViewerRoles(new ArrayList<>());
            } else {
                resolvedConfiguration.setViewerRoles(viewerRoles);
            }

            return resolvedConfiguration;
        }
    }

    @Reference(
            name = "org.wso2.carbon.sp.jobmanager.core.internal.services.DatasourceServiceComponent",
            service = DatasourceServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterServiceDatasource"
    )
    public void regiterServiceDatasource(DatasourceServiceComponent datasourceServiceComponent) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) DatasourceServiceComponent");
        }
    }

    public void unregisterServiceDatasource(DatasourceServiceComponent datasourceServiceComponent) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) DatasourceServiceComponent");
        }
    }
}
