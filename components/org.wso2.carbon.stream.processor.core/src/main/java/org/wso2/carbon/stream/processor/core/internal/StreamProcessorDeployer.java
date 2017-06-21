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
import org.wso2.carbon.stream.processor.common.SimulationDependencyListener;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppAlreadyExistException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * {@code StreamProcessorDeployer} is responsible for all Siddhi Appp file deployment tasks
 */

@Component(
        name = "stream-processor-deployer",
        immediate = true,
        service = org.wso2.carbon.deployment.engine.Deployer.class
)
public class StreamProcessorDeployer implements Deployer {


    private static final Logger log = LoggerFactory.getLogger(StreamProcessorDeployer.class);
    private ArtifactType artifactType = new ArtifactType<>("siddhi");
    private SimulationDependencyListener simulationDependencyListener;
    private URL directoryLocation;

    public static void deploySiddhiQLFile(File file) throws Exception {
        InputStream inputStream = null;
        String siddhiAppName;

        try {
            inputStream = new FileInputStream(file);
            String siddhiAppFileName = file.getName();
            if (siddhiAppFileName.endsWith(SiddhiAppProcessorConstants.SIDDHI_APP_FILE_EXTENSION)) {
                String siddhiAppFileNameWithoutExtension = getFileNameWithoutExtenson(siddhiAppFileName);
                String siddhiApp = getStringFromInputStream(inputStream);
                try {
                    siddhiAppName = StreamProcessorDataHolder.getStreamProcessorService().
                            getSiddhiAppName(siddhiApp);
                    if (siddhiAppFileNameWithoutExtension.equals(siddhiAppName)) {
                        StreamProcessorDataHolder.getStreamProcessorService().deploySiddhiApp(siddhiApp,
                                siddhiAppName);
                    } else {
                        throw new SiddhiAppDeploymentException("Siddhi App file name needs be identical with the " +
                                "name defined in the Siddhi App content");
                    }
                } catch (SiddhiAppAlreadyExistException e) {
                    throw e;
                } catch (Exception e) {
                    SiddhiAppData siddhiAppData = new SiddhiAppData(siddhiApp, false);
                    StreamProcessorDataHolder.getStreamProcessorService().
                            addSiddhiAppFile(siddhiAppFileNameWithoutExtension, siddhiAppData);
                    throw new SiddhiAppDeploymentException(e);
                }
            } else {
                throw new SiddhiAppDeploymentException(("Error: File extension not supported for file name "
                        + siddhiAppFileName + ". Support only"
                        + SiddhiAppProcessorConstants.SIDDHI_APP_FILE_EXTENSION + " ."));
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.ERROR);
                    throw new SiddhiAppDeploymentException("Error when closing the Siddhi QL filestream", e);
                }
            }
        }
    }

    private static String getStringFromInputStream(InputStream is) throws SiddhiAppDeploymentException {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            while ((line = br.readLine()) != null) {
                sb.append(System.getProperty("line.separator")).append(line);
            }
        } catch (IOException e) {
            throw new SiddhiAppDeploymentException("Exception when reading the Siddhi QL file", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new SiddhiAppDeploymentException("Exception when closing the Siddhi QL file stream", e);
                }
            }
        }

        return sb.toString();
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do.
    }

    @Override
    public void init() {
        try {
            directoryLocation = new URL("file:" + SiddhiAppProcessorConstants.SIDDHI_APP_FILES_DIRECTORY);
            log.info("Stream Processor Deployer Initiated");
        } catch (MalformedURLException e) {
            log.error("Error while initializing directoryLocation" + SiddhiAppProcessorConstants.
                    SIDDHI_APP_FILES_DIRECTORY, e);
        }
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {

        if (StreamProcessorDataHolder.getInstance().getRuntimeMode().equals(SiddhiAppProcessorConstants.
                RuntimeMode.SERVER)) {
            try {
                deploySiddhiQLFile(artifact.getFile());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                //throw new CarbonDeploymentException(e.getMessage(), e);
            }
        }
        broadcastDeploy();
        return artifact.getFile().getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        if (StreamProcessorDataHolder.getInstance().getRuntimeMode().equals(SiddhiAppProcessorConstants.
                RuntimeMode.SERVER)) {
            StreamProcessorDataHolder.getStreamProcessorService().undeploySiddhiApp((String) key);
        }
        broadcastDelete();
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {

        if (StreamProcessorDataHolder.getInstance().getRuntimeMode().equals(SiddhiAppProcessorConstants.
                RuntimeMode.SERVER)) {
            StreamProcessorDataHolder.getStreamProcessorService().undeploySiddhiApp(artifact.getName());
            try {
                deploySiddhiQLFile(artifact.getFile());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                //throw new CarbonDeploymentException(e.getMessage(), e);
            }
        }
        broadcastUpdate();
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


    /**
     * broadcastDeploy() is used to notify simulationDependencyListeners about a new file deployment
     * */
    private void broadcastDeploy() {
        if (simulationDependencyListener != null) {
            simulationDependencyListener.onDeploy();
        }
    }


    /**
     * broadcastUpdate() is used to notify simulationDependencyListeners about a update on a deployed file
     * */
    private void broadcastUpdate() {
        if (simulationDependencyListener != null) {
            simulationDependencyListener.onUpdate();
        }
    }


    /**
     * broadcastUpdate() is used to notify simulationDependencyListeners about a delete
     * */
    private void broadcastDelete() {
        if (simulationDependencyListener != null) {
            simulationDependencyListener.onDelete();
        }
    }

    /**
     * This bind method will be called when Greeter OSGi service is registered.
     */
    @Reference(
            name = "carbon.event.stream.service",
            service = EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetGreeterService"
    )
    protected void setGreeterService(EventStreamService eventStreamService) {
    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     */
    protected void unsetGreeterService(EventStreamService eventStreamService) {

    }

    /**
     * This bind method will be called when SimulationDependencyListener OSGi service is registered.
     */
    @Reference(
            name = "siddhi.dependency.resolver",
            service = SimulationDependencyListener.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsubscribeFromListener"
    )
    protected void subscribeToListener(SimulationDependencyListener simulationDependencyListener) {
        this.simulationDependencyListener = simulationDependencyListener;
    }

    protected void unsubscribeFromListener(SimulationDependencyListener simulationDependencyListener) {
        this.simulationDependencyListener = null;
    }

    private static String getFileNameWithoutExtenson(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            return fileName.substring(0, pos);
        }

        return fileName;
    }


}
