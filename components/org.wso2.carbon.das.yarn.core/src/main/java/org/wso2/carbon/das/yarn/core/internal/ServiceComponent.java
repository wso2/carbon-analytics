package org.wso2.carbon.das.yarn.core.internal;

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
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.das.jobmanager.core.appCreator.SPSiddhiAppCreator;
import org.wso2.carbon.das.yarn.core.bean.DeploymentConfig;
import org.wso2.carbon.das.yarn.core.deployment.YarnDeploymentManagerImpl;
import org.wso2.carbon.das.yarn.core.impl.YarnDistributionServiceImpl;
import org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants;
import org.wso2.carbon.stream.processor.core.distribution.DistributionService;
import org.wso2.carbon.stream.processor.core.util.DeploymentMode;

import java.util.Map;

@Component(
        name = "distributed-service-yarn",
        immediate = true
)

public class ServiceComponent {
    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private ServiceRegistration yarnDistributionServiceRegistration;

    @Activate
    protected void start(BundleContext bundleContext){
        if (ServiceDataHolder.getInstance().getDeploymentMode() == DeploymentMode.YARN) {
            log.info("Starting Yarn Distributed Service.");
            yarnDistributionServiceRegistration = bundleContext.registerService(
                    DistributionService.class.getName(),
                    new YarnDistributionServiceImpl(new SPSiddhiAppCreator(), new YarnDeploymentManagerImpl()),
                    null);
        }
        log.info("After Activate");
    }

    @Deactivate
    protected void stop() {
        if (yarnDistributionServiceRegistration != null) {
            yarnDistributionServiceRegistration.unregister();
        }

    }

   @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider){
        DeploymentConfig yarnDeploymentConfig;

        try {
            if (configProvider.getConfigurationObject(YarnDeploymentConstants.DEPLOYMENT_CONFIG_NS) != null) {
                if (YarnDeploymentConstants.MODE_DISTRIBUTED.equalsIgnoreCase((String) ((Map) configProvider
                        .getConfigurationObject(YarnDeploymentConstants.DEPLOYMENT_CONFIG_NS)).get("type"))) {
                    // TODO: 11/20/17 temporary adding here. as config class is not created
                    yarnDeploymentConfig = configProvider.getConfigurationObject(DeploymentConfig.class);
                    if (yarnDeploymentConfig!=null){
                        ServiceDataHolder.getInstance().setYarnDeploymentConfig(yarnDeploymentConfig);
                    }
                        if (YarnDeploymentConstants.MODE_DISTRIBUTED.equalsIgnoreCase(yarnDeploymentConfig.getType())) {
                            ServiceDataHolder.getInstance().setDeploymentMode(DeploymentMode.YARN);
                        } else {
                            ServiceDataHolder.getInstance().setDeploymentMode(DeploymentMode.OTHER);
                        }
                    } else {
                        log.error("Couldn't read " +
                                          YarnDeploymentConstants.DEPLOYMENT_CONFIG_NS + " from deployment.yaml");
                    }
                }
            else {
                log.error(YarnDeploymentConstants.DEPLOYMENT_CONFIG_NS +
                                  " is not specified in deployment.yaml");
            }
        } catch (ConfigurationException e) {
            log.error("Error while reading " +
                              YarnDeploymentConstants.DEPLOYMENT_CONFIG_NS + " from deployment.yaml");
        }
    }

    /**
     * Unregister ConfigProvider and unset cluster config and deployment config.
     *
     * @param configProvider configProvider.
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        ServiceDataHolder.getInstance().setYarnDeploymentConfig(null);
    }

}
