package org.wso2.carbon.das.jobmanager.core.internal;

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
import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.das.jobmanager.core.beans.ClusterConfig;
import org.wso2.carbon.das.jobmanager.core.exception.JobManagerException;
import org.wso2.carbon.das.jobmanager.core.util.DeploymentMode;
import org.wso2.carbon.das.jobmanager.core.util.DistributedConstants;
import org.wso2.carbon.das.jobmanager.core.util.RuntimeMode;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.utils.Utils;

import java.util.HashMap;

/**
 * ServiceComponent for the Distributed Manager.
 */
@Component(
        name = "DistributedManagerServiceComponent",
        service = ServiceComponent.class,
        immediate = true
)
public class ServiceComponent {
    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    /**
     * This is the activation method of ServiceComponent.
     * This will be called when its references are satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        log.info("ServiceComponent bundle activated.");
    }

    /**
     * This is the deactivation method of ServiceComponent.
     * This will be called when this component is being stopped.
     *
     * @throws Exception will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("ServiceComponent is deactivated");
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    /**
     * Register carbon {@link ConfigProvider} to be used with config reading.
     * @param configProvider {@link ConfigProvider} reference.
     * @throws JobManagerException Will be thrown upon failure to read deployment.yaml
     */
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) throws JobManagerException {
        ServiceDataHolder.setConfigProvider(configProvider);
        if (DistributedConstants.RUNTIME_NAME_MANAGER.equalsIgnoreCase(Utils.getRuntimeName())) {
            ServiceDataHolder.setRuntimeMode(RuntimeMode.MANAGER);
            ClusterConfig clusterConfig;
            try {
                if (ServiceDataHolder.getConfigProvider()
                        .getConfigurationObject(DistributedConstants.CLUSTER_CONFIG_NS) != null) {
                    clusterConfig = ServiceDataHolder.getConfigProvider().getConfigurationObject(ClusterConfig.class);
                    if (clusterConfig != null) {
                        ServiceDataHolder.setClusterConfig(clusterConfig);
                        if (DistributedConstants.MODE_DISTRIBUTED.equalsIgnoreCase(
                                clusterConfig.getModeConfig().getType())) {
                            ServiceDataHolder.setDeploymentMode(DeploymentMode.DISTRIBUTED);
                        }
                    } else {
                        throw new JobManagerException("Configuration doesn't specify any cluster configurations");
                    }
                }
            } catch (ConfigurationException e) {
                throw new JobManagerException("Error while reading cluster configuration from deployment.yaml", e);
            }
        }
    }

    /**
     * Unregister ConfigProvider
     * @param configProvider
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        ServiceDataHolder.setConfigProvider(null);
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void registerDataSourceService(DataSourceService dataSourceService) {
        ServiceDataHolder.setDataSourceService(dataSourceService);
    }

    protected void unregisterDataSourceService(DataSourceService dataSourceService) {
        ServiceDataHolder.setDataSourceService(null);
    }

    @Reference(
            name = "org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator",
            service = ClusterCoordinator.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterClusterCoordinator"
    )
    protected void registerClusterCoordinator(ClusterCoordinator clusterCoordinator) {
        ServiceDataHolder.setClusterCoordinator(clusterCoordinator);
        configureCluster(clusterCoordinator);
    }

    protected void unregisterClusterCoordinator(ClusterCoordinator coordinationStrategy) {
        ServiceDataHolder.setClusterCoordinator(null);
    }

    private void configureCluster(ClusterCoordinator clusterCoordinator) {
        // Only have to join the cluster if this node is a Manager and it's running on Distributed mode.
        ClusterConfig clusterConfig = ServiceDataHolder.getClusterConfig();
        if (ServiceDataHolder.getClusterCoordinator() != null && clusterConfig != null &&
                ServiceDataHolder.getDeploymentMode() == DeploymentMode.DISTRIBUTED &&
                ServiceDataHolder.getRuntimeMode() == RuntimeMode.MANAGER) {
            if (clusterConfig.isEnabled()) {
                if (DistributedConstants.MODE_DISTRIBUTED.equalsIgnoreCase(clusterConfig.getModeConfig().getType())) {
                    clusterCoordinator.setPropertiesMap(new HashMap<String, Object>() {{
                        put("host", ServiceDataHolder.getClusterConfig().getModeConfig().getAdvertisedHost());
                        put("port", ServiceDataHolder.getClusterConfig().getModeConfig().getAdvertisedHost());
                    }});
                    clusterCoordinator.registerEventListener(new MemberEventListener() {
                        @Override
                        public void memberAdded(NodeDetail nodeDetail) {
                            // do nothing
                        }

                        @Override
                        public void memberRemoved(NodeDetail nodeDetail) {
                            // do nothing
                        }

                        @Override
                        public void coordinatorChanged(NodeDetail nodeDetail) {
                            ServiceDataHolder.setIsLeader(clusterCoordinator.isLeaderNode());
                            ServiceDataHolder.setLeaderNodeDetail(clusterCoordinator.getLeaderNode());
                        }
                    });
                    ServiceDataHolder.setIsLeader(clusterCoordinator.isLeaderNode());
                    log.info("Stream Processor started as a Manager node in Distributed mode.");
                }
            } else {
                log.warn("Clustering disabled in configuration. Hence, Manager runtime activated without clustering.");
            }
        }
    }
}
