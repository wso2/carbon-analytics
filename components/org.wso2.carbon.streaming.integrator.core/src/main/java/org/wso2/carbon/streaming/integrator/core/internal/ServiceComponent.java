/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.streaming.integrator.core.internal;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.config.StatisticsConfiguration;
import io.siddhi.core.util.SiddhiComponentActivator;
import io.siddhi.core.util.persistence.IncrementalPersistenceStore;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.core.util.statistics.StatisticsManager;
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
import org.wso2.carbon.analytics.permissions.PermissionManager;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.databridge.commons.ServerEventListener;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.si.metrics.core.MetricsFactory;
import org.wso2.carbon.si.metrics.core.internal.MetricsDataHolder;
import org.wso2.carbon.siddhi.extensions.installer.core.internal.SiddhiExtensionsInstallerMicroservice;
import org.wso2.carbon.streaming.integrator.common.EventStreamService;
import org.wso2.carbon.streaming.integrator.common.HAStateChangeListener;
import org.wso2.carbon.streaming.integrator.common.SiddhiAppRuntimeService;
import org.wso2.carbon.streaming.integrator.common.utils.config.FileConfigManager;
import org.wso2.carbon.streaming.integrator.core.DeploymentMode;
import org.wso2.carbon.streaming.integrator.core.NodeInfo;
import org.wso2.carbon.streaming.integrator.core.ha.HAManager;
import org.wso2.carbon.streaming.integrator.core.ha.exception.HAModeException;
import org.wso2.carbon.streaming.integrator.core.ha.util.CoordinationConstants;
import org.wso2.carbon.streaming.integrator.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.streaming.integrator.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.carbon.streaming.integrator.core.persistence.PersistenceManager;
import org.wso2.carbon.streaming.integrator.core.persistence.beans.PersistenceConfigurations;
import org.wso2.carbon.streaming.integrator.core.persistence.exception.PersistenceStoreConfigurationException;
import org.wso2.carbon.streaming.integrator.core.persistence.util.PersistenceConstants;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.streaming.integrator.core.impl.utils.Constants.HA;
import static org.wso2.carbon.streaming.integrator.core.impl.utils.Constants.PERIOD;

/**
 * Service component to consume CarbonRuntime instance which has been registered as an OSGi service by Carbon Kernel.
 */
@Component(
        name = "stream-processor-core-service",
        immediate = true
)
public class ServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);

    private ServiceRegistration streamServiceRegistration;
    private ServiceRegistration siddhiAppRuntimeServiceRegistration;
    private ScheduledFuture<?> scheduledFuture = null;
    private ScheduledExecutorService scheduledExecutorService = null;
    private boolean clusterComponentActivated;
    private boolean serviceComponentActivated;


    /**
     * This is the activation method of ServiceComponent. This will be called when its references are satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        log.debug("Service Component is activated");

        String siddhiAppsReference = System.getProperty(SiddhiAppProcessorConstants.SYSTEM_PROP_RUN_SIDDHI_APPS);
        ConfigProvider configProvider = StreamProcessorDataHolder.getInstance().getConfigProvider();
        // Create Streaming Integrator Service
        StreamProcessorDataHolder.setStreamProcessorService(new StreamProcessorService());
        SiddhiManager siddhiManager = new SiddhiManager();
        FileConfigManager fileConfigManager = new FileConfigManager(configProvider);
        siddhiManager.setConfigManager(fileConfigManager);
        PersistenceConfigurations persistenceConfigurations = configProvider.getConfigurationObject
                (PersistenceConfigurations.class);

        if (persistenceConfigurations != null && persistenceConfigurations.isEnabled()) {
            String persistenceStoreClassName = persistenceConfigurations.getPersistenceStore();
            try {
                if (Class.forName(persistenceStoreClassName).newInstance() instanceof PersistenceStore) {
                    PersistenceStore persistenceStore =
                            (PersistenceStore) Class.forName(persistenceStoreClassName).newInstance();
                    persistenceStore.setProperties((Map) configProvider.getConfigurationObject(PersistenceConstants.
                            STATE_PERSISTENCE_NS));
                    siddhiManager.setPersistenceStore(persistenceStore);
                } else if (Class.forName(persistenceStoreClassName).newInstance()
                        instanceof IncrementalPersistenceStore) {
                    IncrementalPersistenceStore incrementalPersistenceStore =
                            (IncrementalPersistenceStore) Class.forName(persistenceStoreClassName).newInstance();
                    incrementalPersistenceStore.setProperties(
                            (Map) configProvider.getConfigurationObject(PersistenceConstants.STATE_PERSISTENCE_NS));
                    siddhiManager.setIncrementalPersistenceStore(incrementalPersistenceStore);
                } else {
                    throw new PersistenceStoreConfigurationException("Persistence Store class with name "
                            + persistenceStoreClassName + " is invalid. The given class has to implement either " +
                            "io.siddhi.core.util.persistence.PersistenceStore or " +
                            "io.siddhi.core.util.persistence.IncrementalPersistenceStore.");
                }
                if (log.isDebugEnabled()) {
                    log.debug(persistenceStoreClassName + " chosen as persistence store");
                }

            } catch (ClassNotFoundException e) {
                throw new PersistenceStoreConfigurationException("Persistence Store class with name "
                        + persistenceStoreClassName + " is invalid. ", e);
            }
            int persistenceInterval = persistenceConfigurations.getIntervalInMin();
            scheduledExecutorService = Executors.newScheduledThreadPool(1,
                    new ThreadFactoryBuilder().setPriority(7).setNameFormat("SchedulePersistence-%d").build());
            if (persistenceInterval > 0) {
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new PersistenceManager(),
                        persistenceInterval, persistenceInterval, TimeUnit.MINUTES);
            }
            StreamProcessorDataHolder.setIsPersistenceEnabled(true);
            log.info("Periodic state persistence started with an interval of " + String.valueOf(persistenceInterval) +
                    " using " + persistenceStoreClassName);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Periodic persistence is disabled");
            }
        }

        StatisticsConfiguration statisticsConfiguration = new StatisticsConfiguration(new MetricsFactory());
        siddhiManager.setStatisticsConfiguration(statisticsConfiguration);
        StreamProcessorDataHolder.setSiddhiManager(siddhiManager);
        StreamProcessorDataHolder.setStatisticsConfiguration(statisticsConfiguration);

        File siddhiAppFileReference;

        if (siddhiAppsReference != null) {
            if (siddhiAppsReference.trim().equals("")) {
                // Can't Continue. We shouldn't be here. that means there is a bug in the startup script.
                log.error("Error: Can't get target file to run. System property {} is not set.",
                        SiddhiAppProcessorConstants.SYSTEM_PROP_RUN_SIDDHI_APPS);
            } else {
                siddhiAppFileReference = new File(siddhiAppsReference);

                if (!siddhiAppFileReference.exists()) {
                    log.error("Error: File " + siddhiAppFileReference.getName() + " not found in the given location " +
                            "\"" + siddhiAppFileReference.getPath() + "\" ");
                }

                if (siddhiAppFileReference.isDirectory()) {
                    File[] siddhiAppFileArray = siddhiAppFileReference.listFiles();
                    if (siddhiAppFileArray != null) {
                        for (File siddhiApp : siddhiAppFileArray) {
                            try {
                                StreamProcessorDeployer.deploySiddhiQLFile(siddhiApp);
                            } catch (Exception e) {
                                log.error("Exception occurred when deploying the Siddhi App: " + siddhiApp.getName(), e);
                            }
                        }
                    }
                } else {
                    try {
                        StreamProcessorDeployer.deploySiddhiQLFile(siddhiAppFileReference);
                    } catch (Exception e) {
                        log.error("Exception occurred when deploying the Siddhi App: " +
                                siddhiAppFileReference.getName(), e);
                    }
                }
            }
        }

        streamServiceRegistration = bundleContext.registerService(EventStreamService.class.getName(),
                new CarbonEventStreamService(), null);
        siddhiAppRuntimeServiceRegistration = bundleContext.registerService(SiddhiAppRuntimeService.class
                .getCanonicalName(), new CarbonSiddhiAppRuntimeService(), null);

        NodeInfo nodeInfo = new NodeInfo(DeploymentMode.SINGLE_NODE, configProvider.getConfigurationObject(
                CarbonConfiguration.class).getId());
        bundleContext.registerService(NodeInfo.class.getName(), nodeInfo, null);
        StreamProcessorDataHolder.setNodeInfo(nodeInfo);
        StreamProcessorDataHolder.getInstance().setBundleContext(bundleContext);

        serviceComponentActivated = true;

        if (clusterComponentActivated) {
            setUpClustering(StreamProcessorDataHolder.getClusterCoordinator());
        }
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component is being stopped or
     * references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.debug("Service Component is deactivated");

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppMap();
        for (SiddhiAppData siddhiAppData : siddhiAppMap.values()) {
            if (siddhiAppData.getSiddhiAppRuntime() != null) {
                siddhiAppData.getSiddhiAppRuntime().shutdown();
            }
        }

        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }

        streamServiceRegistration.unregister();
        siddhiAppRuntimeServiceRegistration.unregister();
    }

    /**
     * This bind method will be called when CarbonRuntime OSGi service is registered.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime"
    )
    protected void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        StreamProcessorDataHolder.getInstance().setCarbonRuntime(carbonRuntime);
    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        StreamProcessorDataHolder.getInstance().setCarbonRuntime(null);
    }


    /**
     * This bind method will be called when Siddhi ComponentActivator OSGi service is registered.
     *
     * @param siddhiComponentActivator The SiddhiComponentActivator instance registered by Siddhi OSGi service
     */
    @Reference(
            name = "siddhi.component.activator.service",
            service = SiddhiComponentActivator.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiComponentActivator"
    )
    protected void setSiddhiComponentActivator(SiddhiComponentActivator siddhiComponentActivator) {

    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param siddhiComponentActivator The SiddhiComponentActivator instance registered by Siddhi OSGi service
     */
    protected void unsetSiddhiComponentActivator(SiddhiComponentActivator siddhiComponentActivator) {

    }

    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        StreamProcessorDataHolder.getInstance().setConfigProvider(configProvider);
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        StreamProcessorDataHolder.getInstance().setConfigProvider(null);
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceListener"
    )
    protected void registerDataSourceListener(DataSourceService dataSourceService) {
        StreamProcessorDataHolder.setDataSourceService(dataSourceService);

    }

    protected void unregisterDataSourceListener(DataSourceService dataSourceService) {
        StreamProcessorDataHolder.setDataSourceService(null);
    }

    @Reference(
            name = "org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator",
            service = ClusterCoordinator.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterClusterCoordinator"
    )
    protected void registerClusterCoordinator(ClusterCoordinator clusterCoordinator) throws
            ConfigurationException {
        if (clusterCoordinator != null) {
            clusterComponentActivated = true;
            StreamProcessorDataHolder.setClusterCoordinator(clusterCoordinator);
            if (serviceComponentActivated) {
                setUpClustering(clusterCoordinator);
            }
        }
    }

    protected void unregisterClusterCoordinator(ClusterCoordinator clusterCoordinator) {
        StreamProcessorDataHolder.setClusterCoordinator(null);
    }

    private void setUpClustering(ClusterCoordinator clusterCoordinator) throws ConfigurationException {
        ConfigProvider configProvider = StreamProcessorDataHolder.getInstance().getConfigProvider();
        if (configProvider.getConfigurationObject(CoordinationConstants.DEPLOYMENT_CONFIG_NS) != null) {
            if (CoordinationConstants.MODE_HA.equalsIgnoreCase((String) ((Map) configProvider
                    .getConfigurationObject(CoordinationConstants.DEPLOYMENT_CONFIG_NS)).get("type"))) {
                DeploymentConfig deploymentConfig = configProvider.getConfigurationObject(DeploymentConfig.class);
                StreamProcessorDataHolder.setDeploymentConfig(deploymentConfig);

                if (clusterCoordinator.getAllNodeDetails().size() > 2) {
                    throw new HAModeException("More than two nodes can not be used in the minimum HA mode. " +
                            "Use another clustering mode, change the groupId or disable clustering.");
                }
                log.info("WSO2 Streaming Integrator Starting in Two Node Minimum HA Deployment");
                StreamProcessorDataHolder.setIsStatisticsEnabled(
                        MetricsDataHolder.getInstance().getMetricManagementService().isEnabled());
                StatisticsManager statisticsManager = StreamProcessorDataHolder.getStatisticsConfiguration().
                        getFactory().createStatisticsManager(
                        null, HA + PERIOD + StreamProcessorDataHolder.getNodeInfo().getNodeId(), null);
                StreamProcessorDataHolder.setStatisticsManager(statisticsManager);

                String nodeId = configProvider.getConfigurationObject(CarbonConfiguration.class).getId();
                String groupId = (String) ((Map) configProvider.getConfigurationObject(
                        CoordinationConstants.CLUSTER_CONFIG_NS)).get(CoordinationConstants.GROUP_ID);

                HAManager haManager = new HAManager(clusterCoordinator, nodeId, groupId,
                        StreamProcessorDataHolder.getDeploymentConfig());
                StreamProcessorDataHolder.setHaManager(haManager);
                haManager.start();
            }
        }
    }

    @Reference(
            name = "permission-manager",
            service = PermissionManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPermissionManager"
    )
    protected void setPermissionManager(PermissionManager permissionManager) {
        StreamProcessorDataHolder.setPermissionProvider(permissionManager.getProvider());
    }

    protected void unsetPermissionManager(PermissionManager permissionManager) {
        StreamProcessorDataHolder.setPermissionProvider(null);
    }

    /**
     * Get the ServerEventListener service. This is the bind method that gets called for ServerEventListener service
     * registration that satisfy the policy.
     *
     * @param serverEventListener the server listeners that is registered as a service.
     */
    @Reference(
            name = "org.wso2.carbon.databridge.commons.ServerEventListener",
            service = ServerEventListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterServerListener"
    )
    protected void registerServerListener(ServerEventListener serverEventListener) {
        StreamProcessorDataHolder.setServerListener(serverEventListener);
        if (StreamProcessorDataHolder.getHAManager() != null) {
            if (StreamProcessorDataHolder.getHAManager().isActiveNode()) {
                serverEventListener.start();
            }
        } else {
            serverEventListener.start();
        }

    }

    protected void unregisterServerListener(ServerEventListener serverEventListener) {
        StreamProcessorDataHolder.removeServerListener(serverEventListener);
    }

    /**
     * Get the HAStateChangeListener service. This is the bind method that gets called for HAStateChangeListener service
     * registration that satisfy the policy.
     *
     * @param haStateChangeListener the ha state change server listeners that is registered as a service.
     */
    @Reference(
            name = "org.wso2.carbon.streaming.integrator.common.HAStateChangeListener",
            service = HAStateChangeListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterHAStateChangeListener"
    )
    protected void registerHAStateChangeListener(HAStateChangeListener haStateChangeListener) {
        StreamProcessorDataHolder.setHAStateChangeListener(haStateChangeListener);
    }

    protected void unregisterHAStateChangeListener(HAStateChangeListener haStateChangeListener) {
        StreamProcessorDataHolder.removeHAStateChangeListener(haStateChangeListener);
    }

}
