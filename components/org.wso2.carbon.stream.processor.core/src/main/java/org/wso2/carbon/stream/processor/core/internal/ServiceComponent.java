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
package org.wso2.carbon.stream.processor.core.internal;

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
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.siddhi.metrics.core.SiddhiMetricsFactory;
import org.wso2.carbon.siddhi.metrics.core.service.MetricsServiceComponent;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.common.utils.config.FileConfigManager;
import org.wso2.carbon.stream.processor.core.DeploymentMode;
import org.wso2.carbon.stream.processor.core.NodeInfo;
import org.wso2.carbon.stream.processor.core.distribution.DistributionService;
import org.wso2.carbon.stream.processor.core.ha.HAManager;
import org.wso2.carbon.stream.processor.core.ha.exception.HAModeException;
import org.wso2.carbon.stream.processor.core.ha.util.CoordinationConstants;
import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.carbon.stream.processor.core.persistence.FileSystemPersistenceStore;
import org.wso2.carbon.stream.processor.core.persistence.PersistenceManager;
import org.wso2.carbon.stream.processor.core.persistence.exception.PersistenceStoreConfigurationException;
import org.wso2.carbon.stream.processor.core.persistence.util.PersistenceConstants;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.StatisticsConfiguration;
import org.wso2.siddhi.core.util.SiddhiComponentActivator;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service component to consume CarbonRuntime instance which has been registered as an OSGi service
 * by Carbon Kernel.
 */
@Component(
        name = "stream-processor-core-service",
        immediate = true
)
public class ServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private ServiceRegistration serviceRegistration;
    private ScheduledFuture<?> scheduledFuture = null;
    private ScheduledExecutorService scheduledExecutorService = null;
    private boolean clusterComponentActivated;
    private boolean serviceComponentActivated;


    /**
     * This is the activation method of ServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        log.info("Service Component is activated");

        String runningFileName = System.getProperty(SiddhiAppProcessorConstants.SYSTEM_PROP_RUN_FILE);
        ConfigProvider configProvider = StreamProcessorDataHolder.getInstance().getConfigProvider();
        // Create Stream Processor Service
        StreamProcessorDataHolder.setStreamProcessorService(new StreamProcessorService());
        SiddhiManager siddhiManager = new SiddhiManager();
        FileConfigManager fileConfigManager = new FileConfigManager(configProvider);
        siddhiManager.setConfigManager(fileConfigManager);
        PersistenceStore persistenceStore;
        Map persistenceConfig = (Map) configProvider.getConfigurationObject(PersistenceConstants.STATE_PERSISTENCE_NS);

        if (persistenceConfig != null &&
                (boolean) persistenceConfig.get(PersistenceConstants.STATE_PERSISTENCE_ENABLED)) {
            String persistenceStoreClassName = (String) persistenceConfig.
                    get(PersistenceConstants.STATE_PERSISTENCE_CLASS);

            if (persistenceStoreClassName != null) {
                try {
                    persistenceStore = (PersistenceStore) Class.forName(persistenceStoreClassName).newInstance();
                    if (log.isDebugEnabled()) {
                        log.debug(persistenceStoreClassName + " chosen as persistence store");
                    }
                } catch (ClassNotFoundException e) {
                    throw new PersistenceStoreConfigurationException("Persistence Store class with name "
                            + persistenceStoreClassName + " is invalid. ", e);
                }
            } else {
                persistenceStoreClassName = "org.wso2.carbon.stream.processor.core." +
                        "persistence.FileSystemPersistenceStore";
                persistenceStore = new FileSystemPersistenceStore();
                log.warn("No persistence store class set. FileSystemPersistenceStore used as default store");
            }

            persistenceStore.setProperties(persistenceConfig);
            siddhiManager.setPersistenceStore(persistenceStore);
            Object persistenceInterval = persistenceConfig.get(PersistenceConstants.STATE_PERSISTENCE_INTERVAL_IN_MIN);
            if (persistenceInterval == null || !(persistenceInterval instanceof Integer)) {
                persistenceInterval = 1;
                if (log.isDebugEnabled()) {
                    log.warn("Periodic persistence interval not set. Default value of one minute is used");
                }
            }
            scheduledExecutorService = Executors.newScheduledThreadPool(1);

            if ((int) persistenceInterval > 0) {
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new PersistenceManager(),
                        (int) persistenceInterval, (int) persistenceInterval, TimeUnit.MINUTES);
            }
            StreamProcessorDataHolder.setIsPersistenceEnabled(true);
            log.info("Periodic state persistence started with an interval of " + persistenceInterval.toString() +
                    " using " + persistenceStoreClassName);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Periodic persistence is disabled");
            }
        }

        StatisticsConfiguration statisticsConfiguration = new StatisticsConfiguration(new SiddhiMetricsFactory());
        siddhiManager.setStatisticsConfiguration(statisticsConfiguration);
        StreamProcessorDataHolder.setSiddhiManager(siddhiManager);

        File runningFile;

        if (runningFileName != null) {
            StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.RUN_FILE);
            if (runningFileName.trim().equals("")) {
                // Can't Continue. We shouldn't be here. that means there is a bug in the startup script.
                log.error("Error: Can't get target file to run. System property {} is not set.",
                        SiddhiAppProcessorConstants.SYSTEM_PROP_RUN_FILE);
                StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.ERROR);
                return;
            }
            runningFile = new File(runningFileName);
            if (!runningFile.exists()) {
                log.error("Error: File " + runningFile.getName() + " not found in the given location.");
                StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.ERROR);
                return;
            }
            try {
                StreamProcessorDeployer.deploySiddhiQLFile(runningFile);
            } catch (Exception e) {
                StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.ERROR);
                log.error(e.getMessage(), e);
                return;
            }
        } else {
            StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.SERVER);
        }

        if (log.isDebugEnabled()) {
            log.debug("Runtime mode is set to : " + StreamProcessorDataHolder.getInstance().getRuntimeMode());
        }

        if (log.isDebugEnabled()) {
            log.debug("WSO2 Data Analytics Server runtime started...!");
        }

        serviceRegistration = bundleContext.registerService(EventStreamService.class.getName(),
                new CarbonEventStreamService(), null);

        NodeInfo nodeInfo = new NodeInfo(DeploymentMode.SINGLE_NODE, configProvider.getConfigurationObject(
                CarbonConfiguration.class).getId());
        bundleContext.registerService(NodeInfo.class.getName(), nodeInfo,null);
        StreamProcessorDataHolder.setNodeInfo(nodeInfo);
        StreamProcessorDataHolder.getInstance().setBundleContext(bundleContext);

        serviceComponentActivated = true;

        if (clusterComponentActivated) {
            setUpClustering(StreamProcessorDataHolder.getClusterCoordinator());
        }
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Service Component is deactivated");

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

        serviceRegistration.unregister();
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
    protected void registerClusterCoordinator(ClusterCoordinator clusterCoordinator) throws ConfigurationException {
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

                if (deploymentConfig.getLiveSync().isEnabled()) {
                    String advertisedHost = deploymentConfig.getLiveSync().getAdvertisedHost();
                    int advertisedPort = deploymentConfig.getLiveSync().getAdvertisedPort();

                    if (("").equals(advertisedHost) || advertisedPort == 0) {
                        throw new ConfigurationException("Two Node Minimum HA live sync has been enabled but " +
                                CoordinationConstants.ADVERTISED_HOST + " or " + CoordinationConstants.ADVERTISED_PORT
                                + " has not been set in deployment.yaml");
                    }
                }
                log.info("WSO2 Stream Processor Starting in Two Node Minimum HA Deployment");

                String nodeId = configProvider.getConfigurationObject(CarbonConfiguration.class).getId();
                String groupId = (String) ((Map) configProvider.getConfigurationObject(
                        CoordinationConstants.CLUSTER_CONFIG_NS)).get(CoordinationConstants.GROUP_ID);

                HAManager haManager = new HAManager(clusterCoordinator, nodeId, groupId, deploymentConfig);
                StreamProcessorDataHolder.setHaManager(haManager);
                haManager.start();
            }
        }
    }

    @Reference(
            name = "org.wso2.carbon.stream.processor.core.distribution.DistributionService",
            service = DistributionService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDistributionService"
    )
    protected void registerDistributionService(DistributionService distributionService) {
        StreamProcessorDataHolder.setDistributionService(distributionService);

    }

    protected void unregisterDistributionService(DistributionService distributionService) {
        StreamProcessorDataHolder.setDistributionService(null);
    }

    @Reference(
            name = "org.wso2.carbon.siddhi.metrics.core.service.MetricsServiceComponent",
            service = MetricsServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterMetricsManager"
    )
    protected void registerMetricsManager(MetricsServiceComponent serviceComponent) {
        //do nothing
    }

    protected void unregisterMetricsManager(MetricsServiceComponent serviceComponent) {
        //do nothing
    }

}
