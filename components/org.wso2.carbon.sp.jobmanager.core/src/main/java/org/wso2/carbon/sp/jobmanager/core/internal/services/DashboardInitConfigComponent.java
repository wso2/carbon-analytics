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

//import org.wso2.carbon.status.dashboard.core.configuration.DefaultConfigurationBuilder;
//import org.wso2.carbon.status.dashboard.core.dbhandler.DeploymentConfigs;
//import org.wso2.carbon.status.dashboard.core.internal.MonitoringDataHolder;

/**
 * This component handle the all the initialization tasks.
 */
@Component(
        name = "org.wso2.carbon.sp.jobmanager.core.internal.services.DashboardInitConfigComponent",
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
     * Defauld and deployment Query merger.
     *
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
            logger.info("DB NAME" + dashboardManagerDatasourceName);


            int connectionTimeout = deploymentQueries.getManagerConnectionConfigurations().getConnectionTimeOut() ==
                    null ? defaultQueries.getManagerConnectionConfigurations().getConnectionTimeOut()
                    : deploymentQueries.getManagerConnectionConfigurations().getConnectionTimeOut();

            int readTimeOut = deploymentQueries.getManagerConnectionConfigurations().getReadTimeOut() == null ?
                    defaultQueries.getManagerConnectionConfigurations().getReadTimeOut()
                    : deploymentQueries.getManagerConnectionConfigurations().getReadTimeOut();

            resolvedConfiguration.setManagerConnectionConfigurations(connectionTimeout, readTimeOut);

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
