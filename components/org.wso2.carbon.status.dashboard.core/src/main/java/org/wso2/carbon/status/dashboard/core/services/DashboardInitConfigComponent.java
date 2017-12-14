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
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.status.dashboard.core.configuration.DefaultConfigurationBuilder;
import org.wso2.carbon.status.dashboard.core.dbhandler.DeploymentConfigs;
import org.wso2.carbon.status.dashboard.core.internal.DashboardDataHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * This component handle the all the initialization tasks.
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.core.services.DashboardInitConfigComponent",
        service = DashboardInitConfigComponent.class,
        immediate = true
)
public class DashboardInitConfigComponent {
    private static final Logger logger = LoggerFactory.getLogger(DashboardInitConfigComponent.class);

    @Activate
    protected void start(BundleContext bundleContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) DashboardInitConfigComponent");
        }
        try {
            DeploymentConfigs deploymentConfigurations = DashboardDataHolder.getInstance()
                    .getConfigProvider().getConfigurationObject(DeploymentConfigs.class);
            DeploymentConfigs dashboardDefaultConfiguration = DefaultConfigurationBuilder.getInstance()
                    .getConfiguration();
            DeploymentConfigs resolvedConfiguration = mergeQueries(dashboardDefaultConfiguration,
                    deploymentConfigurations);
            DashboardDataHolder.getInstance().setStatusDashboardDeploymentConfigs(resolvedConfiguration);
        } catch (ConfigurationException e) {
            logger.error("Error in reading configuration from the deployment.YML",e);
        }

    }

    @Deactivate
    protected void stop() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) DashboardInitConfigComponent");
        }
        DashboardDataHolder.getInstance().setStatusDashboardDeploymentConfigs(null);
    }

    /**
     * Defauld and deployment Query merger.
     * @param defaultQueries
     * @return
     */
    private DeploymentConfigs mergeQueries(DeploymentConfigs defaultQueries,
                                           DeploymentConfigs deploymentQueries) {
        if (deploymentQueries == null) {
            return defaultQueries;
        } else {
            DeploymentConfigs resolvedConfiguration = new DeploymentConfigs();
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

            int connectionTimeout = deploymentQueries.getWorkerConnectionConfigurations().getConnectionTimeOut() ==
                    null ? defaultQueries.getWorkerConnectionConfigurations().getConnectionTimeOut()
                    : deploymentQueries.getWorkerConnectionConfigurations().getConnectionTimeOut();

           int readTimeOut = deploymentQueries.getWorkerConnectionConfigurations().getReadTimeOut() == null ?
                    defaultQueries.getWorkerConnectionConfigurations().getReadTimeOut()
                    : deploymentQueries.getWorkerConnectionConfigurations().getReadTimeOut();

            resolvedConfiguration.setWorkerConnectionConfigurations(connectionTimeout,readTimeOut);

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
            return resolvedConfiguration;
        }


    }

    @Reference(
            name = "org.wso2.carbon.status.dashboard.core.services.DatasourceServiceComponent",
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
