/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.simulator.core.service;

import org.apache.commons.io.FilenameUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.event.simulator.core.service.bean.ActiveSimulatorData;
import org.wso2.carbon.event.simulator.core.service.bean.ResourceDependencyData;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.common.SimulationDependencyListener;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.carbon.utils.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * SimulationConfigDeployer is responsible for all simulation config deployment tasks
 */
@Component(
        name = "simulation-config-deployer",
        immediate = true,
        service = org.wso2.carbon.deployment.engine.Deployer.class
)
public class SimulationConfigDeployer implements Deployer, SimulationDependencyListener {
    private static final Logger log = LoggerFactory.getLogger(SimulationConfigDeployer.class);
    private ArtifactType artifactType = new ArtifactType<>(EventSimulatorConstants.SIMULATION_FILE_EXTENSION);
    private URL directoryLocation;
    private ServiceRegistration serviceRegistration;


    /**
     * deployConfigFile() is used to deploy a simulation configuration added to directory 'simulation-configs'
     *
     * @param file simulation config file added
     */
    private void deployConfigFile(File file) throws Exception {
        if (!file.getName().startsWith(".")) {
            if (FilenameUtils.isExtension(file.getName(), EventSimulatorConstants.SIMULATION_FILE_EXTENSION)) {
                String simulationName = FilenameUtils.getBaseName(file.getName());
                try {
                    String simulationConfig = SimulationConfigUploader.getConfigUploader().getSimulationConfig
                            (simulationName, (Paths.get(Utils.getRuntimePath().toString(),
                                    EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                    EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
                    if (!simulationConfig.isEmpty()) {
                        EventSimulator eventSimulator = new EventSimulator(simulationName, simulationConfig, true);
                        EventSimulatorMap.getInstance().getActiveSimulatorMap().put(simulationName,
                                new ActiveSimulatorData(eventSimulator, simulationConfig));
                        log.info("Deployed active simulation '" + simulationName + "'.");
                    }
                } catch (ResourceNotFoundException e) {
                    EventSimulatorMap eventSimulatorMap = EventSimulatorMap.getInstance();
                    ResourceDependencyData resourceDependencyData =
                            eventSimulatorMap.getInActiveSimulatorMap().get(simulationName);
                    ResourceDependencyData newResourceDependency =
                            new ResourceDependencyData(e.getResourceType(), e.getResourceName(), e.getMessage());
                    if (resourceDependencyData != null) {
                        if (!resourceDependencyData.equals(newResourceDependency)) {
                            eventSimulatorMap.getInActiveSimulatorMap().put(simulationName, newResourceDependency);
                            log.error(e.getMessage(), e);
                            log.info("Updated inactive simulation '" + simulationName + "'.");
                        }
                    } else {
                        eventSimulatorMap.getInActiveSimulatorMap().put(simulationName, newResourceDependency);
                        log.error(e.getMessage(), e);
                        log.info("Deployed inactive simulation '" + simulationName + "'.");
                    }
                }
            } else {
                log.error("Simulation '" + file.getName() + "' has an invalid content type. File type supported is '."
                                  + EventSimulatorConstants.SIMULATION_FILE_EXTENSION + "'.");
            }
        }
    }

    private void undeployConfigFile(String simulationName) throws Exception {
        EventSimulatorMap eventSimulatorMap = EventSimulatorMap.getInstance();
        ActiveSimulatorData activeSimulatorData = eventSimulatorMap.getActiveSimulatorMap().get(simulationName);
        ResourceDependencyData resourceDependencyData = eventSimulatorMap.getInActiveSimulatorMap().get(simulationName);
        if (activeSimulatorData != null) {
            activeSimulatorData.getEventSimulator().stop();
            eventSimulatorMap.getActiveSimulatorMap().remove(simulationName);
            log.info("Undeployed active simulation '" + simulationName + "'.");
        } else if (resourceDependencyData != null) {
            eventSimulatorMap.getInActiveSimulatorMap().remove(simulationName);
            log.info("Undeployed inactive simulation '" + simulationName + "'.");
        }
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do.
        serviceRegistration = bundleContext.registerService(SimulationDependencyListener.class.getName(),
                this, null);
    }


    /**
     * init() is used to initialize SimulationConfigDeployer
     */
    @Override
    public void init() {
        try {
            directoryLocation = new URL("file:" + EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS);
            log.info("Simulation config deployer initiated.");
        } catch (MalformedURLException e) {
            log.error("Error while initializing simulation config directoryLocation"
                    + EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS, e);
        }
    }


    /**
     * deploy() is used to deploy a csv file added
     *
     * @param artifact simulation config file added
     * @return name of simulation config file
     */
    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        try {
            deployConfigFile(artifact.getFile());
        } catch (Exception e) {
            throw new CarbonDeploymentException(e.getMessage(), e);
        }
        return artifact.getFile().getName();
    }


    /**
     * undeploy() is called when a simulation config file is deleted
     *
     * @param key name of the simulation config file deleted
     */
    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        try {
            if (!((String) key).startsWith(".")) {
                undeployConfigFile(FilenameUtils.getBaseName((String) key));
            }
        } catch (Exception e) {
            throw new CarbonDeploymentException(e.getMessage(), e);
        }
    }

    /**
     * update() is called when a simulation config file is updated
     *
     * @param artifact simulation config file that was updated
     * @return name of artifact
     */
    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        try {
            String simulationName = artifact.getName();
            if (!simulationName.startsWith(".")) {
                undeployConfigFile(simulationName);
                deployConfigFile(artifact.getFile());
            }
        } catch (Exception e) {
            throw new CarbonDeploymentException(e.getMessage(), e);
        }
        return artifact.getName();
    }

    @Override
    public URL getLocation() {
        return directoryLocation;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    @Reference(
            name = "event.stream.service",
            service = EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService"
    )
    protected void setEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.debug("@Reference(bind) EventStreamService");
        }

    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.debug("@Reference(unbind) EventStreamService");
        }
    }

    /* Below is the SimulationDependencyListener logic */

    /**
     * onDeploy() is called when one of the deployers the SimulationConfigDeployer is listening to deploys an
     * artifact
     */
    @Override
    public void onDeploy() {
        EventSimulatorMap.getInstance().retryInActiveSimulatorDeployment(true);
    }

    @Override
    public void onUpdate() {
        EventSimulatorMap.getInstance().checkValidityAfterDependency();
    }

    @Override
    public void onDelete() {
        EventSimulatorMap.getInstance().checkValidityAfterDependency();
    }
}
