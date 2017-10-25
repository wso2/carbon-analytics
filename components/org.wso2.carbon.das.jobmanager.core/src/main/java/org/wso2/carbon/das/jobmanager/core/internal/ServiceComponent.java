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
import org.wso2.carbon.das.jobmanager.core.ResourceManager;
import org.wso2.carbon.das.jobmanager.core.api.ResourceManagerApi;
import org.wso2.carbon.das.jobmanager.core.appCreator.SPSiddhiAppCreator;
import org.wso2.carbon.das.jobmanager.core.bean.ClusterConfig;
import org.wso2.carbon.das.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.das.jobmanager.core.bean.ManagerNode;
import org.wso2.carbon.das.jobmanager.core.deployment.DeploymentManagerImpl;
import org.wso2.carbon.das.jobmanager.core.exception.ResourceManagerException;
import org.wso2.carbon.das.jobmanager.core.impl.DistributionManagerServiceImpl;
import org.wso2.carbon.das.jobmanager.core.impl.DistributionResourceServiceImpl;
import org.wso2.carbon.das.jobmanager.core.impl.RDBMSServiceImpl;
import org.wso2.carbon.das.jobmanager.core.model.ResourceMapping;
import org.wso2.carbon.stream.processor.core.util.DeploymentMode;
import org.wso2.carbon.das.jobmanager.core.util.DistributedConstants;
import org.wso2.carbon.stream.processor.core.util.RuntimeMode;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.stream.processor.core.distribution.DistributionService;
import org.wso2.carbon.utils.Utils;
import org.wso2.msf4j.Microservice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ServiceComponent for the Resource Manager.
 */
@Component(
        name = "sp-resource-manager-component",
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
    protected void start(BundleContext bundleContext) throws Exception {
        log.info("Resource Manager bundle activated.");
        if (ServiceDataHolder.getRuntimeMode() == RuntimeMode.MANAGER
                && ServiceDataHolder.getDeploymentMode() == DeploymentMode.DISTRIBUTED) {
            ServiceDataHolder.setRdbmsService(new RDBMSServiceImpl());
            initResourceMapping();
            resourceManagerAPIServiceRegistration = bundleContext.registerService(Microservice.class.getName(),
                                                                                  new ResourceManagerApi(), null);
            distributionServiceRegistration = bundleContext.registerService(
                    DistributionService.class.getName(),
                    new DistributionManagerServiceImpl(new SPSiddhiAppCreator(), new DeploymentManagerImpl()),
                    null);
        } else {
            distributionServiceRegistration = bundleContext.registerService(
                    DistributionService.class.getName(), new DistributionResourceServiceImpl(), null);
        }
    }

    /**
     * This is the deactivation method of ServiceComponent.
     * This will be called when this component is being stopped.
     *
     * @throws Exception will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Resource Manager bundle deactivated.");
        if (resourceManagerAPIServiceRegistration != null) {
            resourceManagerAPIServiceRegistration.unregister();
        }
        distributionServiceRegistration.unregister();
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
    protected void registerConfigProvider(ConfigProvider configProvider) throws ResourceManagerException {
        ServiceDataHolder.setConfigProvider(configProvider);
        if (DistributedConstants.RUNTIME_NAME_MANAGER.equalsIgnoreCase(Utils.getRuntimeName())) {
            ServiceDataHolder.setRuntimeMode(RuntimeMode.MANAGER);
            // Read cluster config
            ClusterConfig clusterConfig;
            try {
                if (configProvider.getConfigurationObject(DistributedConstants.CLUSTER_CONFIG_NS) != null) {
                    clusterConfig = configProvider.getConfigurationObject(ClusterConfig.class);
                    if (clusterConfig != null) {
                        ServiceDataHolder.setClusterConfig(clusterConfig);
                    } else {
                        throw new ResourceManagerException("Couldn't read " + DistributedConstants.CLUSTER_CONFIG_NS +
                                " from deployment.yaml");
                    }
                } else {
                    throw new ResourceManagerException(DistributedConstants.CLUSTER_CONFIG_NS + " is not specified " +
                            "in deployment.yaml");
                }
            } catch (ConfigurationException e) {
                throw new ResourceManagerException("Error while reading " + DistributedConstants.CLUSTER_CONFIG_NS +
                        " from deployment.yaml", e);
            }

            // Read deployment config / manager node config
            DeploymentConfig deploymentConfig;
            ManagerNode currentNodeConfig;
            try {
                if (configProvider.getConfigurationObject(DistributedConstants.DEPLOYMENT_CONFIG_NS) != null) {
                    if (DistributedConstants.MODE_DISTRIBUTED.equalsIgnoreCase((String) ((Map) configProvider
                            .getConfigurationObject(DistributedConstants.DEPLOYMENT_CONFIG_NS)).get("type"))) {
                        deploymentConfig = configProvider.getConfigurationObject(DeploymentConfig.class);
                        if (deploymentConfig != null) {
                            String id = (String) ((Map) configProvider.getConfigurationObject("wso2.carbon"))
                                    .get("id");
                            currentNodeConfig = new ManagerNode()
                                    .setId(id)
                                    .setHeartbeatInterval(deploymentConfig.getHeartbeatInterval())
                                    .setHeartbeatMaxRetry(deploymentConfig.getHeartbeatMaxRetry())
                                    .setHttpInterface(deploymentConfig.getHttpInterface());
                            ServiceDataHolder.setDeploymentConfig(deploymentConfig);
                            ServiceDataHolder.setCurrentNode(currentNodeConfig);
                            if (DistributedConstants.MODE_DISTRIBUTED.equalsIgnoreCase(deploymentConfig.getType())) {
                                ServiceDataHolder.setDeploymentMode(DeploymentMode.DISTRIBUTED);
                            } else if (DistributedConstants.MODE_HA.equalsIgnoreCase(deploymentConfig.getType())) {
                                ServiceDataHolder.setDeploymentMode(DeploymentMode.HA);
                            } else {
                                ServiceDataHolder.setDeploymentMode(DeploymentMode.SINGLE_NODE);
                            }
                        } else {
                            throw new ResourceManagerException("Couldn't read " +
                                    DistributedConstants.DEPLOYMENT_CONFIG_NS + " from deployment.yaml");
                        }
                    }
                } else {
                    throw new ResourceManagerException(DistributedConstants.DEPLOYMENT_CONFIG_NS +
                            " is not specified in deployment.yaml");
                }
            } catch (ConfigurationException e) {
                throw new ResourceManagerException("Error while reading " +
                        DistributedConstants.DEPLOYMENT_CONFIG_NS + " from deployment.yaml", e);
            }
        }
    }

    /**
     * Unregister ConfigProvider and unset cluster config and deployment config.
     *
     * @param configProvider
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        ServiceDataHolder.setConfigProvider(null);
        ServiceDataHolder.setClusterConfig(null);
        ServiceDataHolder.setDeploymentConfig(null);
    }

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
        // Join the cluster if this node is a Manager and it's running on Distributed mode.
        ClusterConfig clusterConfig = ServiceDataHolder.getClusterConfig();
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        if (ServiceDataHolder.getClusterCoordinator() != null && clusterConfig != null &&
                ServiceDataHolder.getDeploymentMode() == DeploymentMode.DISTRIBUTED &&
                ServiceDataHolder.getRuntimeMode() == RuntimeMode.MANAGER) {
            if (clusterConfig.isEnabled()) {
                if (DistributedConstants.MODE_DISTRIBUTED.equalsIgnoreCase(deploymentConfig.getType())) {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put(DistributedConstants.KEY_NODE_INFO, ServiceDataHolder.getCurrentNode());
                    clusterCoordinator.setPropertiesMap(properties);
                    clusterCoordinator.registerEventListener(new MemberEventListener() {
                        @Override
                        public void memberAdded(NodeDetail nodeDetail) {
                            // Do nothing.
                        }

                        @Override
                        public void memberRemoved(NodeDetail nodeDetail) {
                            // Do nothing.
                        }

                        @Override
                        public void coordinatorChanged(NodeDetail nodeDetail) {
                            ServiceDataHolder.setIsLeader(clusterCoordinator.isLeaderNode());
                            ServiceDataHolder.setLeaderNode((ManagerNode) nodeDetail.getPropertiesMap()
                                    .get(DistributedConstants.KEY_NODE_INFO));
                            if (ServiceDataHolder.isLeader()) {
                                startLeaderTasks();
                                log.info("Became the Leader node in distributed mode.");
                            } else {
                                log.info("Leader changed to : " + ServiceDataHolder.getLeaderNode());
                            }
                        }
                    });
                    ServiceDataHolder.setIsLeader(clusterCoordinator.isLeaderNode());
                    if (ServiceDataHolder.isLeader()) {
                        ServiceDataHolder.setLeaderNode(ServiceDataHolder.getCurrentNode());
                        startLeaderTasks();
                        log.info("Stream Processor has started as the Leader node in distributed mode.");
                    } else {
                        log.info("Stream Processor has started as a Manager node in distributed mode.");
                    }
                }
            } else {
                log.warn("Clustering disabled in configuration. Hence, Manager runtime activated without clustering.");
            }
        }
    }

    private void startLeaderTasks() {
        ResourceManager resourceManager = new ResourceManager();
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        HeartbeatCheckTask heartbeatCheckTask = new HeartbeatCheckTask();
        heartbeatCheckTask.registerHeartbeatChangeListener(resourceManager);

        initResourceMapping();
        ServiceDataHolder.getExecutorService().scheduleAtFixedRate(
                heartbeatCheckTask, deploymentConfig.getHeartbeatInterval(),
                deploymentConfig.getHeartbeatInterval(), TimeUnit.MILLISECONDS);

        // TODO: 10/19/17 Other leader tasks
        log.info("Starting leader tasks ...........");
    }

    private void initResourceMapping() {
        String groupId = ServiceDataHolder.getClusterConfig().getGroupId();
        ResourceMapping existingResourceMapping = ServiceDataHolder.getRdbmsService().getResourceMapping(groupId);
        ServiceDataHolder.setResourceMapping((existingResourceMapping != null) ? existingResourceMapping
                : new ResourceMapping(groupId));
    }
}
