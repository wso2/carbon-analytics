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
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.event.simulator.core.exception.SimulationConfigDeploymentException;
import org.wso2.carbon.event.simulator.core.internal.util.CommonOperations;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * {@link SimulationConfigDeployer} is responsible for all simulation config deployment tasks
 *
 * @since 1.0.0
 */
@Component(
        name = "simulation-config-deployer",
        immediate = true,
        service = org.wso2.carbon.deployment.engine.Deployer.class
)
public class SimulationConfigDeployer implements Deployer {
    private static final Logger log = LoggerFactory.getLogger(SimulationConfigDeployer.class);
    private ArtifactType artifactType = new ArtifactType<>(EventSimulatorConstants.SIMULATION_FILE_EXTENSION);
    private URL directoryLocation;

    private void deployConfigFile(File file) throws Exception {
        if (FilenameUtils.isExtension(file.getName(), EventSimulatorConstants.SIMULATION_FILE_EXTENSION)) {
            String simulationName = FilenameUtils.getBaseName(file.getName());
            String simulationConfig = SimulationConfigUploader.getConfigUploader().getSimulationConfig
                    (simulationName, (Paths.get(Utils.getCarbonHome().toString(),
                            EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                            EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
            if (!simulationConfig.isEmpty()) {
                EventSimulator simulator = new EventSimulator(simulationName, simulationConfig);
                EventSimulationMap.getSimulatorMap().put(simulationName, simulator);
            }
        } else {
            throw new SimulationConfigDeploymentException("Error: File extension not supported for file name "
                    + file.getName() + ". Support only '." + EventSimulatorConstants.SIMULATION_FILE_EXTENSION + "'" +
                    " .");
        }
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do.
    }

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

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        try {
            deployConfigFile(artifact.getFile());
        } catch (Exception e) {
            throw new CarbonDeploymentException(e.getMessage(), e);
        }
        return artifact.getFile().getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        try {
            String simulationName = FilenameUtils.getBaseName((String) key);
            if (EventSimulationMap.getSimulatorMap().containsKey(simulationName)) {
                EventSimulationMap.getSimulatorMap().get(simulationName).stop();
                EventSimulationMap.getSimulatorMap().remove(simulationName);
            }
        } catch (Exception e) {
            throw new CarbonDeploymentException(e.getMessage(), e);
        }
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        try {
            String simulationName = artifact.getName();
            if (EventSimulationMap.getSimulatorMap().containsKey(simulationName)) {
                EventSimulator simulator = EventSimulationMap.getSimulatorMap().get(simulationName);
                simulator.stop();
                EventSimulationMap.getSimulatorMap().remove(simulationName);
            }
            deployConfigFile(artifact.getFile());
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
            log.info("@Reference(bind) EventStreamService");
        }

    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.info("@Reference(unbind) EventStreamService");
        }

    }
}
