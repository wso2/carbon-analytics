/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sp.jobmanager.core.internal;

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
import org.wso2.carbon.analytics.idp.client.core.api.AnalyticsHttpClientBuilderService;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.sp.jobmanager.core.CoordinatorChangeListener;
import org.wso2.carbon.sp.jobmanager.core.allocation.ResourceAllocationAlgorithm;
import org.wso2.carbon.sp.jobmanager.core.api.ResourceManagerApi;
import org.wso2.carbon.sp.jobmanager.core.appcreator.SPSiddhiAppCreator;
import org.wso2.carbon.sp.jobmanager.core.bean.ClusterConfig;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.deployment.DeploymentManagerImpl;
import org.wso2.carbon.sp.jobmanager.core.exception.ResourceManagerException;
import org.wso2.carbon.sp.jobmanager.core.impl.DistributionManagerServiceImpl;
import org.wso2.carbon.sp.jobmanager.core.impl.RDBMSServiceImpl;
import org.wso2.carbon.sp.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.sp.jobmanager.core.util.ResourceManagerConstants;
import org.wso2.carbon.stream.processor.core.distribution.DistributionService;
import org.wso2.carbon.stream.processor.core.util.DeploymentMode;
import org.wso2.msf4j.Microservice;

import java.util.HashMap;
import java.util.Map;

/**
 * ServiceComponent for the distributed resource manager.
 */
@Component(
        name = "sp.distributed.manager",
        service = ServiceComponent.class,
        immediate = true
)
public class ServiceComponent {
    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private ServiceRegistration resourceManagerAPIServiceRegistration;
    private ServiceRegistration distributionServiceRegistration;

    /**
     * This is the activation method of ServiceComponent.
     * This will be called when all of its references are satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) {
        if (ServiceDataHolder.getDeploymentMode() == DeploymentMode.DISTRIBUTED) {
            log.info("Starting Manager node in distributed mode.");
            ServiceDataHolder.setRdbmsService(new RDBMSServiceImpl());
            ServiceDataHolder.setDeploymentManager(new DeploymentManagerImpl());
            resourceManagerAPIServiceRegistration = bundleContext.registerService(Microservice.class.getName(),
                    new ResourceManagerApi(), null);
            distributionServiceRegistration = bundleContext.registerService(
                    DistributionService.class.getName(),
                    new DistributionManagerServiceImpl(new SPSiddhiAppCreator(),
                            ServiceDataHolder.getDeploymentManager()),
                    null);
        }
    }

    /**
     * This is the deactivation method of ServiceComponent.
     * This will be called when this component is being stopped.
     *
     * @throws Exception will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() {
        if (resourceManagerAPIServiceRegistration != null) {
            resourceManagerAPIServiceRegistration.unregister();
        }
        if (distributionServiceRegistration != null) {
            distributionServiceRegistration.unregister();
        }
    }

    /**
     * Register carbon {@link ConfigProvider} to be used with config reading.
     *
     * @param configProvider {@link ConfigProvider} reference.
     * @throws ResourceManagerException Will be thrown upon failure to read deployment.yaml
     */
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) throws ResourceManagerException,
            IllegalAccessException, InstantiationException {
        DeploymentConfig deploymentConfig;
        ManagerNode currentNodeConfig;
        ClusterConfig clusterConfig;
        try {
            if (configProvider.getConfigurationObject(ResourceManagerConstants.CLUSTER_CONFIG_NS) != null) {
                clusterConfig = configProvider.getConfigurationObject(ClusterConfig.class);
                if (clusterConfig != null) {
                    ServiceDataHolder.setClusterConfig(clusterConfig);
                    if (!clusterConfig.isEnabled()) {
                        log.error("Clustering of manager node is disabled which is not recommended to be used " +
                                "in production environment. Please configure clustering with a minimum of 2 manager " +
                                "nodes before using distributed deployment in production environment.");
                    }
                } else {
                    log.error("Couldn't read " + ResourceManagerConstants.CLUSTER_CONFIG_NS +
                            " from deployment.yaml");
                }
            } else {
                log.error(ResourceManagerConstants.CLUSTER_CONFIG_NS + " is not specified " +
                        "in deployment.yaml");
            }
        } catch (ConfigurationException e) {
            log.error("Error while reading " + ResourceManagerConstants.CLUSTER_CONFIG_NS +
                    " from deployment.yaml", e);
        }
        try {
            if (configProvider.getConfigurationObject(ResourceManagerConstants.DEPLOYMENT_CONFIG_NS) != null) {
                if (ResourceManagerConstants.MODE_DISTRIBUTED.equalsIgnoreCase((String) ((Map) configProvider
                        .getConfigurationObject(ResourceManagerConstants.DEPLOYMENT_CONFIG_NS)).get("type"))) {
                    // Only read the config if the deployment config type is set to "distributed"
                    deploymentConfig = configProvider.getConfigurationObject(DeploymentConfig.class);
                    if (deploymentConfig != null) {
                        ServiceDataHolder.setDeploymentConfig(deploymentConfig);
                        ResourceAllocationAlgorithm resourceAllocationAlgorithm;
                        String allocationAlgoClassName = deploymentConfig.getAllocationAlgorithm();
                        try {
                            resourceAllocationAlgorithm = (ResourceAllocationAlgorithm)
                                    Class.forName(allocationAlgoClassName).newInstance();
                            log.info("Set " + resourceAllocationAlgorithm);
                            if (log.isDebugEnabled()) {
                                log.debug(allocationAlgoClassName + " chosen as Allocation Algorithm");
                            }
                            ServiceDataHolder.setAllocationAlgorithm(resourceAllocationAlgorithm);
                        } catch (ClassNotFoundException e) {
                            throw new ResourceManagerException("Allocation Algorithm class with name "
                                    + allocationAlgoClassName + " is invalid. ", e);
                        }
                        if(deploymentConfig.getHttpsInterface() == null){
                            log.error(ResourceManagerConstants.KEY_NODE_PROTOCOL + " is not specified " +
                                    "in deployment.yaml");
                        }
                        String id = (String) ((Map) configProvider.getConfigurationObject("wso2.carbon"))
                                .get("id");
                        currentNodeConfig = new ManagerNode().setId(id)
                                .setHeartbeatInterval(deploymentConfig.getHeartbeatInterval())
                                .setHeartbeatMaxRetry(deploymentConfig.getHeartbeatMaxRetry())
                                .setHttpsInterface(deploymentConfig.getHttpsInterface());
                        ServiceDataHolder.setCurrentNode(currentNodeConfig);
                        if (ResourceManagerConstants.MODE_DISTRIBUTED.equalsIgnoreCase(deploymentConfig.getType())) {
                            ServiceDataHolder.setDeploymentMode(DeploymentMode.DISTRIBUTED);
                        } else {
                            ServiceDataHolder.setDeploymentMode(DeploymentMode.OTHER);
                        }
                    } else {
                        log.error("Couldn't read " +
                                ResourceManagerConstants.DEPLOYMENT_CONFIG_NS + " from deployment.yaml");
                    }
                }
            } else {
                log.error(ResourceManagerConstants.DEPLOYMENT_CONFIG_NS +
                        " is not specified in deployment.yaml");
            }
        } catch (ConfigurationException e) {
            log.error("Error while reading " +
                    ResourceManagerConstants.DEPLOYMENT_CONFIG_NS + " from deployment.yaml");
        }
    }

    /**
     * Unregister ConfigProvider and unset cluster config and deployment config.
     *
     * @param configProvider configProvider.
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        ServiceDataHolder.setClusterConfig(null);
        ServiceDataHolder.setDeploymentConfig(null);
    }

    /**
     * Register carbon {@link DataSourceService} to resolve datasource.
     *
     * @param dataSourceService {@link DataSourceService} reference.
     */
    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void registerDataSourceService(DataSourceService dataSourceService) {
        ServiceDataHolder.setDataSourceService(dataSourceService);
    }

    /**
     * Unregister dataSourceService and unset shared instance.
     *
     * @param dataSourceService dataSourceService.
     */
    protected void unregisterDataSourceService(DataSourceService dataSourceService) {
        ServiceDataHolder.setDataSourceService(null);
    }

    /**
     * Register carbon {@link ClusterCoordinator} to be used with manager node coordination.
     *
     * @param clusterCoordinator {@link ClusterCoordinator} reference.
     */
    @Reference(
            name = "org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator",
            service = ClusterCoordinator.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterClusterCoordinator"
    )
    protected void registerClusterCoordinator(ClusterCoordinator clusterCoordinator) {
        // Join the cluster if this node is a Manager and it's running on Distributed mode.
        ClusterConfig clusterConfig = ServiceDataHolder.getClusterConfig();
        if (ServiceDataHolder.getDeploymentMode() == DeploymentMode.DISTRIBUTED) {
            if (clusterCoordinator == null) {
                log.warn("Cluster coordinator not present. Hence, Manager runtime activated without clustering.");
            }
            if (clusterCoordinator != null && clusterConfig == null) {
                log.warn("Clustering configuration not present. Hence, Manager runtime activated without clustering.");
            }
            if (clusterCoordinator != null && clusterConfig != null && !clusterConfig.isEnabled()) {
                log.warn("Clustering disabled in configuration. Hence, Manager runtime activated without clustering.");
            }
            if (clusterCoordinator != null && clusterConfig != null && clusterConfig.isEnabled()) {
                ServiceDataHolder.setCoordinator(clusterCoordinator);
                Map<String, Object> properties = new HashMap<>();
                ManagerNode currentNode = ServiceDataHolder.getCurrentNode();
                properties.put(ResourceManagerConstants.KEY_NODE_ID, currentNode.getId());
                properties.put(ResourceManagerConstants.KEY_NODE_INTERVAL, currentNode.getHeartbeatInterval());
                properties.put(ResourceManagerConstants.KEY_NODE_MAX_RETRY, currentNode.getHeartbeatMaxRetry());
                properties.put(ResourceManagerConstants.KEY_NODE_HOST, currentNode.getHttpsInterface().getHost());
                properties.put(ResourceManagerConstants.KEY_NODE_PORT, currentNode.getHttpsInterface().getPort());
                properties.put(ResourceManagerConstants.KEY_NODE_USERNAME,
                        currentNode.getHttpsInterface().getUsername());
                properties.put(ResourceManagerConstants.KEY_NODE_PASSWORD,
                        currentNode.getHttpsInterface().getPassword());
                clusterCoordinator.setPropertiesMap(properties);
                clusterCoordinator.registerEventListener(new CoordinatorChangeListener());
            }
        }
    }

    /**
     * Unregister coordinationStrategy.
     *
     * @param coordinationStrategy coordinationStrategy.
     */
    protected void unregisterClusterCoordinator(ClusterCoordinator coordinationStrategy) {
        // Do nothing.
    }

    @Reference(
            name = "carbon.anaytics.common.clientservice",
            service = AnalyticsHttpClientBuilderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterAnalyticsHttpClient"
    )
    protected void registerAnalyticsHttpClient(AnalyticsHttpClientBuilderService service) {
        ServiceDataHolder.setClientBuilderService(service);
        if (log.isDebugEnabled()) {
            log.debug("@Reference(bind) AnalyticsHttpClientBuilderService at " +
                    AnalyticsHttpClientBuilderService.class.getName());
        }
    }

    protected void unregisterAnalyticsHttpClient(AnalyticsHttpClientBuilderService service) {
        if (log.isDebugEnabled()) {
            log.debug("@Reference(unbind) AnalyticsHttpClientBuilderService at " +
                    AnalyticsHttpClientBuilderService.class.getName());
        }
        ServiceDataHolder.setClientBuilderService(null);
    }
}
