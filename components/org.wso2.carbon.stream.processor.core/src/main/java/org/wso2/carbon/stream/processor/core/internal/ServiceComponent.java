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
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.common.utils.config.FileConfigManager;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.carbon.stream.processor.core.persistence.FileSystemPersistenceStore;
import org.wso2.carbon.stream.processor.core.persistence.PersistenceManager;
import org.wso2.carbon.stream.processor.core.persistence.exception.PersistenceStoreConfigurationException;
import org.wso2.carbon.stream.processor.core.persistence.util.PersistenceConstants;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.util.SiddhiComponentActivator;
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;
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
        Map configurationMap = configProvider.getConfigurationMap(PersistenceConstants.STATE_PERSISTENCE_NS);

        if (configurationMap != null &&
                (boolean) configurationMap.get(PersistenceConstants.STATE_PERSISTENCE_ENABLED)) {
            String persistenceStoreClassName = (String) configurationMap.
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

            persistenceStore.setProperties(configurationMap);
            siddhiManager.setPersistenceStore(persistenceStore);
            Object persistenceInterval = configurationMap.get(PersistenceConstants.STATE_PERSISTENCE_INTERVAL_IN_MIN);
            if (persistenceInterval == null || !(persistenceInterval instanceof Integer)) {
                persistenceInterval = 1;
                if (log.isDebugEnabled()) {
                    log.warn("Periodic persistence interval not set. Default value of one minute is used");
                }
            }
            scheduledExecutorService = Executors.newScheduledThreadPool(1);

            if ((int) persistenceInterval > 0) {
                scheduledFuture = scheduledExecutorService.
                        scheduleAtFixedRate(new PersistenceManager(), (int) persistenceInterval,
                                (int) persistenceInterval, TimeUnit.MINUTES);
            }
            StreamProcessorDataHolder.setIsPersistenceEnabled(true);
            log.info("Periodic state persistence started with an interval of " + persistenceInterval.toString() +
                    " using " + persistenceStoreClassName);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Periodic persistence is disabled");
            }
        }
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

        scheduledExecutorService.shutdown();
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

}
