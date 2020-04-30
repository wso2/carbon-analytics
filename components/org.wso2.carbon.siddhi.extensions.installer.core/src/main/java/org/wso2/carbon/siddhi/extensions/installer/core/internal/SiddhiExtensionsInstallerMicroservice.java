/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.extensions.installer.core.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.extensions.installer.core.constants.ExtensionsInstallerConstants;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.ConfigMapper;
import org.wso2.carbon.siddhi.extensions.installer.core.exceptions.ExtensionsInstallerException;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.ExtensionConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyInstaller;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyInstallerImpl;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyRetriever;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyRetrieverImpl;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.SiddhiAppExtensionUsageDetector;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.SiddhiAppExtensionUsageDetectorImpl;
import org.wso2.carbon.siddhi.extensions.installer.core.models.SiddhiAppStore;
import org.wso2.carbon.siddhi.extensions.installer.core.util.MissingExtensionsInstaller;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionInstallationStatus.NOT_INSTALLED;
import static org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionUnInstallationStatus.NOT_UNINSTALLED;
import static org.wso2.carbon.siddhi.extensions.installer.core.util.ResponseEntityCreator.ACTION_STATUS_KEY;
import static org.wso2.carbon.siddhi.extensions.installer.core.util.ResponseEntityCreator.ACTION_TYPE_KEY;

/**
 * Exposes Siddhi Extensions Installer as a micro-service.
 */
@Component(
    name = "org.wso2.carbon.siddhi.extensions.installer.core.internal.SiddhiExtensionsInstallerMicroservice",
    service = {SiddhiExtensionsInstallerMicroservice.class, Microservice.class},
    immediate = true
)
@Path("/siddhi-extensions")
public class SiddhiExtensionsInstallerMicroservice implements Microservice {

    private static final Logger logger = LoggerFactory.getLogger(SiddhiExtensionsInstallerMicroservice.class);

    private Map<String, ExtensionConfig> extensionConfigs;
    private SiddhiAppStore siddhiAppStore;

    public SiddhiExtensionsInstallerMicroservice() {
        // Prevents instantiation.
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllExtensionStatuses() {
        try {
            DependencyRetriever dependencyRetriever = new DependencyRetrieverImpl(extensionConfigs);
            return Response
                .status(Response.Status.OK)
                .entity(dependencyRetriever.getAllExtensionStatuses())
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (ExtensionsInstallerException e) {
            logger.error("Failed to get installation statuses of all the extensions.", e);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/status/{extensionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExtensionStatus(@PathParam("extensionId") String extensionId) {
        try {
            DependencyRetriever dependencyRetriever = new DependencyRetrieverImpl(extensionConfigs);
            return Response
                .status(Response.Status.OK)
                .entity(dependencyRetriever.getExtensionStatusFor(extensionId))
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (ExtensionsInstallerException e) {
            logger.error(String.format("Failed to get installation status of extension: %s.", extensionId), e);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/status/{extensionId}/dependencies")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDependencyStatuses(@PathParam("extensionId") String extensionId) {
        try {
            DependencyRetriever dependencyRetriever = new DependencyRetrieverImpl(extensionConfigs);
            return Response
                .status(Response.Status.OK)
                .entity(dependencyRetriever.getDependencyStatusesFor(extensionId))
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (ExtensionsInstallerException e) {
            logger.error(String.format(
                "Failed to get installation statuses of dependencies, of extension: %s.", extensionId), e);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
        }
    }

    @POST
    @Path("/{extensionId}/install")
    @Produces(MediaType.APPLICATION_JSON)
    public Response installDependencies(@PathParam("extensionId") String extensionId) {
        try {
            DependencyInstaller dependencyInstaller = new DependencyInstallerImpl(extensionConfigs);
            Map<String, Object> responseEntity = dependencyInstaller.installDependenciesFor(extensionId);
            Response.Status responseStatus = isActionFailure(responseEntity) ? Response.Status.INTERNAL_SERVER_ERROR :
                Response.Status.OK;
            return Response
                .status(responseStatus)
                .entity(responseEntity)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (ExtensionsInstallerException e) {
            logger.error(String.format("Failed to install dependencies for extension: %s.", extensionId), e);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
        }
    }

    @POST
    @Path("/install")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response installDependencies(Set<String> extensionIds) {
        try {
            DependencyInstaller dependencyInstaller = new DependencyInstallerImpl(extensionConfigs);
            Map<String, Map<String, Object>> responseEntities =
                dependencyInstaller.installDependenciesFor(extensionIds);
            Response.Status responseStatus =
                responseEntities.entrySet().stream().anyMatch(entity -> isActionFailure(entity.getValue())) ?
                    Response.Status.INTERNAL_SERVER_ERROR : Response.Status.OK;
            return Response
                .status(responseStatus)
                .entity(responseEntities)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (ExtensionsInstallerException e) {
            logger.error("There were failures when installing dependencies for the given extensions.", e);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/{extensionId}/dependency-sharing-extensions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDependencySharingExtensions(@PathParam("extensionId") String extensionId) {
        try {
            DependencyRetriever dependencyRetriever = new DependencyRetrieverImpl(extensionConfigs);
            return Response
                .status(Response.Status.OK)
                .entity(dependencyRetriever.getDependencySharingExtensionsFor(extensionId))
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (ExtensionsInstallerException e) {
            logger.error(
                String.format("Failed to get dependency sharing extensions for extension: %s.", extensionId), e);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
        }
    }

    @POST
    @Path("/{extensionId}/uninstall")
    @Produces(MediaType.APPLICATION_JSON)
    public Response uninstallDependencies(@PathParam("extensionId") String extensionId) {
        try {
            DependencyInstaller dependencyInstaller = new DependencyInstallerImpl(extensionConfigs);
            Map<String, Object> responseEntity = dependencyInstaller.unInstallDependenciesFor(extensionId);
            Response.Status responseStatus = isActionFailure(responseEntity) ? Response.Status.INTERNAL_SERVER_ERROR :
                Response.Status.OK;
            return Response
                .status(responseStatus)
                .entity(responseEntity)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (ExtensionsInstallerException e) {
            logger.error(String.format("Failed to un-install dependencies for extension: %s.", extensionId), e);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
        }
    }

    /**
     * Returns whether the given response entity denotes a failure in installation/un-installation.
     * This information is used to mark such a failure as an internal server error.
     *
     * @param responseEntity Response entity produced by the installation/un-installation.
     * @return Whether the response entity denotes a failure in installation/un-installation.
     */
    private boolean isActionFailure(Map<String, Object> responseEntity) {
        return responseEntity.containsKey(ACTION_TYPE_KEY) && responseEntity.containsKey(ACTION_STATUS_KEY) &&
            (NOT_INSTALLED == responseEntity.get(ACTION_STATUS_KEY) ||
                NOT_UNINSTALLED == responseEntity.get(ACTION_STATUS_KEY));
    }

    @POST
    @Path("/siddhi-app-usages")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsedExtensionStatuses(String siddhiAppStringBase64) {
        try {
            String siddhiAppString =
                new String(Base64.getDecoder().decode(siddhiAppStringBase64), StandardCharsets.UTF_8);
            SiddhiAppExtensionUsageDetector siddhiAppExtensionUsageDetector =
                new SiddhiAppExtensionUsageDetectorImpl(extensionConfigs);
            return Response
                .status(Response.Status.OK)
                .entity(siddhiAppExtensionUsageDetector.getUsedExtensionStatuses(siddhiAppString))
                .build();
        } catch (ExtensionsInstallerException e) {
            logger.error("Failed to get installation statuses of extensions used in the Siddhi app.", e);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
        }
    }

    @POST
    @Path("/missing-extensions/install")
    @Produces(MediaType.APPLICATION_JSON)
    public Response installMissingExtensions() {
        try {
            SiddhiAppExtensionUsageDetector usageDetector = new SiddhiAppExtensionUsageDetectorImpl(extensionConfigs);
            DependencyInstaller dependencyInstaller = new DependencyInstallerImpl(extensionConfigs);
            return Response
                .status(Response.Status.OK)
                .entity(MissingExtensionsInstaller
                    .installMissingExtensions(siddhiAppStore, usageDetector, dependencyInstaller))
                .build();
        } catch (ExtensionsInstallerException e) {
            logger.error("Failed to auto install missing extensions.", e);
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
        }
    }

    /**
     * Updates the Siddhi app store, on deployment of a Siddhi app in the runtime.
     *
     * @param siddhiAppName Name of the Siddhi app that was deployed in the runtime.
     * @param siddhiAppBody Body of the Siddhi app that was deployed in the runtime.
     */
    public void reflectDeployment(String siddhiAppName, String siddhiAppBody) {
        siddhiAppStore.addOrUpdateSiddhiApp(siddhiAppName, siddhiAppBody);
        try {
            SiddhiAppExtensionUsageDetector usageDetector = new SiddhiAppExtensionUsageDetectorImpl(extensionConfigs);
            Set<String> notInstalledExtensionKeys =
                MissingExtensionsInstaller.getNotInstalledExtensionKeys(usageDetector, siddhiAppBody);
            if (!notInstalledExtensionKeys.isEmpty()) {
                String message = String.format("The following extensions are required for Siddhi app '%s': %s. ",
                    siddhiAppName, notInstalledExtensionKeys) + "Please use the Extension Installer to install them.";
                logger.warn(message);
            }
        } catch (ExtensionsInstallerException e) {
            logger.error("Failed to detect not installed extensions from Siddhi app.", e);
        }
    }

    /**
     * Updates the Siddhi app store, on un-deployment of a Siddhi app in the runtime.
     *
     * @param siddhiAppName Name of the Siddhi app that was un-deployed in the runtime.
     */
    public void reflectUnDeployment(String siddhiAppName) {
        siddhiAppStore.removeSiddhiApp(siddhiAppName);
    }

    /**
     * This is the activation method of SiddhiExtensionsInstallerMicroservice.
     * This will be called when its references are satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception Error occurred while executing the activate method.
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        extensionConfigs = ConfigMapper.loadAllExtensionConfigs(ExtensionsInstallerConstants.CONFIG_FILE_LOCATION);
        siddhiAppStore = new SiddhiAppStore();
    }

    /**
     * This is the deactivation method of SiddhiExtensionsInstallerMicroservice.
     * This will be called when this component is being stopped or references are satisfied during runtime.
     *
     * @throws Exception Error occurred while executing the de-activate method.
     */
    @Deactivate
    protected void stop() throws Exception {

    }

}
