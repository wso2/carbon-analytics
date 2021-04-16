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

import io.siddhi.core.exception.ExtensionNotFoundException;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.streaming.integrator.common.EventStreamService;
import org.wso2.carbon.streaming.integrator.common.SiddhiAppDeploymentListener;
import org.wso2.carbon.streaming.integrator.common.SimulationDependencyListener;
import org.wso2.carbon.streaming.integrator.core.internal.asyncapi.AsyncAPIDeployer;
import org.wso2.carbon.streaming.integrator.core.internal.exception.SiddhiAppAlreadyExistException;
import org.wso2.carbon.streaming.integrator.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.streaming.integrator.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.streaming.integrator.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.carbon.streaming.integrator.core.persistence.beans.AsyncAPIServiceCatalogueConfigs;
import org.wso2.msf4j.MicroservicesServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private static ServerType serverType = ServerType.SP; // Default server type
    private static boolean isAPIMAlertsEnabled = false;
    private static boolean isAnalyticsEnabledOnSP = false;
    private static boolean apimAnalyticsEnabledOnSP = false;
    private static boolean eiAnalyticsEnabledOnSP = false;


    public static void deploySiddhiQLFile(File file) throws Exception {
        InputStream inputStream = null;
        String siddhiAppName;

        try {
            if (file.isFile()) {
                inputStream = new FileInputStream(file);
                String siddhiAppFileName = file.getName();
                if (siddhiAppFileName.endsWith(SiddhiAppProcessorConstants.SIDDHI_APP_FILE_EXTENSION)) {
                    String siddhiAppFileNameWithoutExtension = getFileNameWithoutExtenson(siddhiAppFileName);
                    SiddhiAppType siddhiAppType = getArtifactType(siddhiAppFileNameWithoutExtension);
                    if (!isDeploymentAllowed(siddhiAppType)) {
                        return;
                    }
                    String siddhiApp = getStringFromInputStream(inputStream);
                    try {
                        siddhiAppName = StreamProcessorDataHolder.getStreamProcessorService().
                                getSiddhiAppName(siddhiApp);
                        if (siddhiAppFileNameWithoutExtension.equals(siddhiAppName)) {
                            broadcastBeforeSiddhiAppDeployment(siddhiAppName, siddhiApp);
                            StreamProcessorDataHolder.getStreamProcessorService().deploySiddhiApp(siddhiApp,
                                    siddhiAppName);
                            try {
                                ConfigProvider configProvider = StreamProcessorDataHolder.getInstance().getConfigProvider();
                                AsyncAPIServiceCatalogueConfigs asyncAPIServiceCatalogueConfigs =
                                        configProvider.getConfigurationObject(AsyncAPIServiceCatalogueConfigs.class);
                                if (asyncAPIServiceCatalogueConfigs != null && asyncAPIServiceCatalogueConfigs.isEnabled()) {
                                    String asyncAPIValue = StreamProcessorDataHolder.getStreamProcessorService().
                                            getSiddhiAnnotationValue(siddhiApp,
                                                    SiddhiAppProcessorConstants.ANNOTATION_ASYNC_API_NAME, file.getName());
                                    AsyncAPIDeployer asyncAPIDeployer =
                                            new AsyncAPIDeployer(asyncAPIServiceCatalogueConfigs, asyncAPIValue);
                                    asyncAPIDeployer.run();
                                }
                            } catch (SiddhiAppConfigurationException e){
                                if (log.isDebugEnabled()) {
                                    log.debug("AsyncAPI annotation not found in file: " + file.getName());
                                }
                            }
                            broadcastSiddhiAppDeployment(siddhiAppName, siddhiApp);
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
                    log.error(("Error: File extension of file name "
                            + siddhiAppFileName + " is not supported. Siddhi App only supports '"
                            + SiddhiAppProcessorConstants.SIDDHI_APP_FILE_EXTENSION + "' ."));
                }
            } else {
                log.error("Error: '" + file.getName() + "' is a sub-directory; hence, ignored.");
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new SiddhiAppDeploymentException("Error when closing the Siddhi QL file stream", e);
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

    private static String getFileNameWithoutExtenson(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            return fileName.substring(0, pos);
        }

        return fileName;
    }

    private static SiddhiAppType getArtifactType(String fileName) {
        fileName = fileName.toUpperCase();
        if (fileName.startsWith(SiddhiAppProcessorConstants.APIM_ALERT_SIDDHI_APP_PREFIX)) {
            return SiddhiAppType.APIMALERT;
        } else if (fileName.startsWith(SiddhiAppProcessorConstants.APIM_SIDDHI_APP_PREFIX)) {
            return SiddhiAppType.APIM;
        } else if (fileName.startsWith(SiddhiAppProcessorConstants.EI_SIDDHI_APP_PREFIX)) {
            return SiddhiAppType.EI;
        } else if (fileName.startsWith(SiddhiAppProcessorConstants.IS_SIDDHI_APP_PREFIX)) {
            return SiddhiAppType.IS;
        } else {
            return SiddhiAppType.OTHER;
        }
    }

    private static boolean isDeploymentAllowed(SiddhiAppType siddhiAppType) {
        switch (serverType) {
            case SP:
                switch (siddhiAppType) {
                    case OTHER:
                        return true;
                    case APIMALERT:
                        if (apimAnalyticsEnabledOnSP && isAPIMAlertsEnabled) {
                            return true;
                        }
                        break;
                    case APIM:
                        if (apimAnalyticsEnabledOnSP) {
                            return true;
                        }
                        break;
                    case IS:
                        if (isAnalyticsEnabledOnSP) {
                            return true;
                        }
                        break;
                    case EI:
                        if (eiAnalyticsEnabledOnSP) {
                            return true;
                        }
                        break;
                }
                break;
            case APIM:
                if (siddhiAppType.name().equals(SiddhiAppType.APIM.name())) {
                    return true;
                }
                if (siddhiAppType.name().equals(SiddhiAppType.APIMALERT.name())) {
                    return isAPIMAlertsEnabled;
                }
                break;
            case IS:
                if (siddhiAppType.name().equals(SiddhiAppType.IS.name())) {
                    return true;
                }
                break;
            case EI:
                if (siddhiAppType.name().equals(SiddhiAppType.EI.name())) {
                    return true;
                }
                break;
        }
        return false;
    }

    private void setServerType() {
        ConfigProvider configProvider = StreamProcessorDataHolder.getInstance().getConfigProvider();
        if (configProvider != null) {
            try {
                String type = (String) ((Map) configProvider
                        .getConfigurationObject("wso2.carbon")).get(SiddhiAppProcessorConstants.WSO2_SERVER_TYPE);
                if (type != null) {
                    switch (type) {
                        case SiddhiAppProcessorConstants.WSO2_SERVER_TYPE_SP:
                            serverType = ServerType.SP;
                            break;
                        case SiddhiAppProcessorConstants.WSO2_SERVER_TYPE_APIM_ANALYTICS:
                            serverType = ServerType.APIM;
                            break;
                        case SiddhiAppProcessorConstants.WSO2_SERVER_TYPE_IS_ANALYTICS:
                            serverType = ServerType.IS;
                            break;
                        case SiddhiAppProcessorConstants.WSO2_SERVER_TYPE_EI_ANALYTICS:
                            serverType = ServerType.EI;
                            break;
                        default:
                            serverType = ServerType.SP;
                            break;
                    }
                    try {
                        LinkedHashMap analyticsSolutionsMap = (LinkedHashMap) configProvider.
                                getConfigurationObject(SiddhiAppProcessorConstants.ANALYTICS_SOLUTIONS);
                        Object directoryPathObject;
                        if (analyticsSolutionsMap != null) {
                            if (serverType.name().equals(ServerType.SP.name())) {
                                directoryPathObject = analyticsSolutionsMap.get(
                                        SiddhiAppProcessorConstants.IS_ANALYTICS_ENABLED);
                                if (directoryPathObject != null) {
                                    isAnalyticsEnabledOnSP = Boolean.parseBoolean(directoryPathObject.toString());
                                }
                                directoryPathObject = analyticsSolutionsMap.get(
                                        SiddhiAppProcessorConstants.APIM_ALETRS_ENABLED);
                                if (directoryPathObject != null) {
                                    isAPIMAlertsEnabled = Boolean.parseBoolean(directoryPathObject.toString());
                                }
                                directoryPathObject = analyticsSolutionsMap.get(
                                        SiddhiAppProcessorConstants.APIM_ANALYTICS_ENABLED);
                                if (directoryPathObject != null) {
                                    apimAnalyticsEnabledOnSP = Boolean.parseBoolean(directoryPathObject.toString());
                                }
                                directoryPathObject = analyticsSolutionsMap.get(
                                        SiddhiAppProcessorConstants.EI_ANALYTICS_ENABLED);
                                if (directoryPathObject != null) {
                                    eiAnalyticsEnabledOnSP = Boolean.parseBoolean(directoryPathObject.toString());
                                }
                            }
                            if (serverType.name().equals(ServerType.APIM.name())) {
                                directoryPathObject = analyticsSolutionsMap.get(
                                        SiddhiAppProcessorConstants.APIM_ALETRS_ENABLED);
                                if (directoryPathObject != null) {
                                    isAPIMAlertsEnabled = Boolean.parseBoolean(directoryPathObject.toString());
                                }
                            }
                        }
                    } catch (ConfigurationException e) {
                        log.error("Failed to read " + SiddhiAppProcessorConstants.ANALYTICS_SOLUTIONS
                                + "'property from the deployment.yaml file. None of the analytics solutions will " +
                                "be deployed. ", e);
                    }
                }
            } catch (ConfigurationException e) {
                log.error("Failed to read the wso2.carbon server " + SiddhiAppProcessorConstants.WSO2_SERVER_TYPE
                        + "'property from the deployment.yaml file. Default value " + serverType.name() +
                        " will be set.", e);
            }
        }
    }

    public enum SiddhiAppType {
        EI, IS, APIM, APIMALERT, OTHER
    }

    public enum ServerType {
        EI, IS, APIM, SP
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do.
    }

    @Override
    public void init() {
        try {
            directoryLocation = new URL("file:" + SiddhiAppProcessorConstants.SIDDHI_APP_FILES_DIRECTORY);
            setServerType();
            log.debug("Streaming Integrator Deployer initiated.");
        } catch (MalformedURLException e) {
            log.error("Error while initializing directoryLocation" + SiddhiAppProcessorConstants.
                    SIDDHI_APP_FILES_DIRECTORY, e);
        }
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {

        int retryCount = 0;
        boolean extensionUnavailabilityErrorRecieved = false;
        while (retryCount != SiddhiAppProcessorConstants.SIDDHI_APP_REDEPLOY_RETRY_COUNT) {
            try {
                deploySiddhiQLFile(artifact.getFile());
                extensionUnavailabilityErrorRecieved = false;
                break;
            } catch (Throwable e) {
                if (e instanceof SiddhiAppDeploymentException && e.getCause() instanceof
                        ExtensionNotFoundException) {
                        extensionUnavailabilityErrorRecieved = true;
                        try {
                            log.warn("Dependencies are not satisfied to deploy siddhi file " +
                                    artifact.getFile().getName() + " due to " + e.getCause().getMessage() +
                                    ".Please check the required dependencies exist.Redeploy will retry in " +
                                    SiddhiAppProcessorConstants.SIDDHI_APP_REDEPLOY_SLEEP_TIMEOUT +
                                    " milliseconds.");
                            StreamProcessorDataHolder.getStreamProcessorService().
                                    undeploySiddhiApp(getFileNameWithoutExtenson(artifact.getName()));
                            Thread.sleep(SiddhiAppProcessorConstants.SIDDHI_APP_REDEPLOY_SLEEP_TIMEOUT);
                        } catch (InterruptedException e1) {
                            log.error("Error while waiting for dependencies to be statisfied " + e1.getMessage());
                        }
                } else {
                    extensionUnavailabilityErrorRecieved = false;
                    log.error(e.getMessage(), e);
                    break;
                }
            }
            retryCount ++;
        }
        if (extensionUnavailabilityErrorRecieved) {
            log.error("Could not deploy siddhi file " + artifact.getFile().getName() + " after retrying for " +
                    SiddhiAppProcessorConstants.SIDDHI_APP_REDEPLOY_RETRY_COUNT + " times.");
        }
        broadcastDeploy();
        return artifact.getFile().getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        StreamProcessorDataHolder.getStreamProcessorService().
                undeploySiddhiApp(getFileNameWithoutExtenson((String) key));
        broadcastDelete();
        broadcastSiddhiAppDelete(getFileNameWithoutExtenson((String) key));
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {

        StreamProcessorDataHolder.getStreamProcessorService().
                undeploySiddhiApp(getFileNameWithoutExtenson(artifact.getName()));
        try {
            deploySiddhiQLFile(artifact.getFile());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
     */
    private void broadcastDeploy() {
        if (simulationDependencyListener != null) {
            simulationDependencyListener.onDeploy();
        }
    }

    /**
     * broadcastUpdate() is used to notify simulationDependencyListeners about a update on a deployed file
     */
    private void broadcastUpdate() {
        if (simulationDependencyListener != null) {
            simulationDependencyListener.onUpdate();
        }
    }

    /**
     * broadcastUpdate() is used to notify simulationDependencyListeners about a delete
     */
    private void broadcastDelete() {
        if (simulationDependencyListener != null) {
            simulationDependencyListener.onDelete();
        }
    }

    /**
     * Notifies Siddhi app deployment listeners, before a Siddhi app is deployed.
     * This differs from the {@link #broadcastSiddhiAppDeployment(String, String)} method because,
     * in cases where a Siddhi app has missing extensions, this method will be called,
     * but the latter one will not.
     *
     * @param siddhiAppName Name of the Siddhi app which is going to be deployed.
     * @param siddhiAppBody Body of the Siddhi app which is going to be deployed.
     */
    private static void broadcastBeforeSiddhiAppDeployment(String siddhiAppName, String siddhiAppBody) {
        for (SiddhiAppDeploymentListener siddhiAppDeploymentListener :
            StreamProcessorDataHolder.getSiddhiAppDeploymentListeners()) {
            siddhiAppDeploymentListener.beforeDeploy(siddhiAppName, siddhiAppBody);
        }
    }

    /**
     * Notifies Siddhi app deployment listeners about a Siddhi app's deployment.
     *
     * @param siddhiAppName Name of the deployed Siddhi app.
     * @param siddhiAppBody Body of the Siddhi app.
     */
    private static void broadcastSiddhiAppDeployment(String siddhiAppName, String siddhiAppBody) {
        for (SiddhiAppDeploymentListener siddhiAppDeploymentListener :
            StreamProcessorDataHolder.getSiddhiAppDeploymentListeners()) {
            siddhiAppDeploymentListener.onDeploy(siddhiAppName, siddhiAppBody);
        }
    }

    /**
     * Notifies Siddhi app deployment listeners about a Siddhi app's deletion.
     *
     * @param siddhiAppName Name of the deleted Siddhi app.
     */
    private static void broadcastSiddhiAppDelete(String siddhiAppName) {
        for (SiddhiAppDeploymentListener siddhiAppDeploymentListener :
            StreamProcessorDataHolder.getSiddhiAppDeploymentListeners()) {
            siddhiAppDeploymentListener.onDelete(siddhiAppName);
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

    /**
     * The bind method, which gets called for the Extensions Installer listener.
     *
     * @param extensionsInstallerListener Extensions Installer listener.
     */
    @Reference(
        name = "org.wso2.carbon.siddhi.extensions.installer.core.internal.SiddhiExtensionsInstallerMicroservice",
        service = SiddhiAppDeploymentListener.class,
        cardinality = ReferenceCardinality.MANDATORY,
        policy = ReferencePolicy.DYNAMIC,
        unbind = "unsubscribeExtensionsInstallerListener"
    )
    protected void subscribeExtensionsInstallerListener(SiddhiAppDeploymentListener extensionsInstallerListener) {
        StreamProcessorDataHolder.addSiddhiAppDeploymentListener(extensionsInstallerListener);
    }

    protected void unsubscribeExtensionsInstallerListener(SiddhiAppDeploymentListener extensionsInstallerListener) {
        StreamProcessorDataHolder.removeSiddhiAppDeploymentListener(extensionsInstallerListener);
    }


    @Reference(service = MicroservicesServer.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMicroservicesServer")
    protected void setMicroservicesServer(MicroservicesServer microservicesServer) {
        if (log.isDebugEnabled()) {
            log.debug("@(bind) MicroservicesServer ");
        }
    }

    protected void unsetMicroservicesServer(MicroservicesServer microservicesServer) {
        if (log.isDebugEnabled()) {
            log.debug(" @(unbind) MicroservicesServer ");
        }
    }
}

