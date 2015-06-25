/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.dashboard.deployment;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dashboard.DashboardConstants;
import org.wso2.carbon.analytics.dashboard.DashboardDeploymentException;
import org.wso2.carbon.analytics.dashboard.util.DeploymentUtil;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.ApplicationConfiguration;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deploys artifacts related to a dashboard.
 * A dashboard can have following artifacts
 * 1. Dashboard definition (This is a JSON formatted file which describes the content of a dashboard)
 * 2. Gadget implementations (A directory with a Google gadget implementation)
 * 2. Layout definition (A directory that defines a layout for a dashboard)
 */
public class DashboardDeployer implements AppDeploymentHandler {
    private static final Log log = LogFactory.getLog(DashboardDeployer.class);

    @Override
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig)
            throws DeploymentException {

        ApplicationConfiguration appConfig = carbonApp.getAppConfig();
        List<Artifact.Dependency> deps = appConfig.getApplicationArtifact().getDependencies();
        List<Artifact> artifacts = new ArrayList<Artifact>();
        for (Artifact.Dependency dep : deps) {
            if (dep.getArtifact() != null) {
                artifacts.add(dep.getArtifact());
            }
        }
        deploy(artifacts);
    }

    private void deploy(List<Artifact> artifacts) throws DashboardDeploymentException {
        for (Artifact artifact : artifacts) {
            List<CappFile> files = artifact.getFiles();
            if (files == null || files.isEmpty()) {
                continue;
            }
            for (CappFile cappFile : files) {
                String fileName = cappFile.getName();
                String path = artifact.getExtractedPath() + File.separator + fileName;
                File file = new File(path);
                if (DashboardConstants.DASHBOARD_ARTIFACT_TYPE.equals(artifact.getType())) {
                    try {
                        if (fileName.endsWith(DashboardConstants.DASHBOARD_EXTENSION)) {
                            String dashboardDefn = DeploymentUtil.readFile(file);
                            String resourceName = fileName.substring(0,
                                    fileName.lastIndexOf(DashboardConstants.DASHBOARD_EXTENSION));
                            DeploymentUtil.createRegistryResource(DashboardConstants.DASHBOARDS_RESOURCE_PATH
                                            + resourceName,
                                    dashboardDefn);
                            log.info("Dashboard definition [" + resourceName + "] has been created.");
                        }
                    } catch (IOException e) {
                        String errorMsg = "Error while reading from the file : " + file.getAbsolutePath();
                        log.error(errorMsg, e);
                        throw new DashboardDeploymentException(errorMsg, e);
                    } catch (RegistryException e) {
                        String errorMsg = "Error while creating registry resource for dashboard";
                        log.error(errorMsg, e);
                        throw new DashboardDeploymentException(errorMsg, e);
                    }
                } else if (DashboardConstants.GADGET_ARTIFACT_TYPE.equals(artifact.getType())) {
                    try {
                        if (file.isDirectory()) {
                            String storePath = getArtifactPath("gadget");
                            File destination = new File(storePath + file.getName());
                            DeploymentUtil.copyFolder(file, destination);
                            log.info("Gadget directory [" + file.getName() + "] has been copied to path "
                                    + destination.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        String errorMsg = "Error while reading from the file : " + file.getAbsolutePath();
                        log.error(errorMsg, e);
                        throw new DashboardDeploymentException(errorMsg, e);
                    }
                } else if (DashboardConstants.LAYOUT_ARTIFACT_TYPE.equals(artifact.getType())) {
                    try {
                        if (file.isDirectory()) {
                            String storePath = getArtifactPath("layout");
                            File destination = new File(storePath + file.getName());
                            DeploymentUtil.copyFolder(file, destination);
                            log.info("Layout directory [" + file.getName() + "] has been copied to path "
                                    + destination.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        String errorMsg = "Error while reading from the file : " + file.getAbsolutePath();
                        log.error(errorMsg, e);
                        throw new DashboardDeploymentException(errorMsg, e);
                    }
                }

            }
        }
    }

    @Override
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {
        List<Artifact.Dependency> deps = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        List<Artifact> artifacts = new ArrayList<Artifact>();
        for (Artifact.Dependency dep : deps) {
            Artifact artifact = dep.getArtifact();
            if (dep.getArtifact() != null) {
                artifacts.add(dep.getArtifact());
            }
        }
        undeploy(artifacts);
    }

    private void undeploy(List<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            List<CappFile> files = artifact.getFiles();
            String fileName = artifact.getFiles().get(0).getName();
            String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
            File file = new File(artifactPath);
            if (DashboardConstants.DASHBOARD_ARTIFACT_TYPE.equals(artifact.getType())) {
                try {
                    if (fileName.endsWith(DashboardConstants.DASHBOARD_EXTENSION)) {
                        String resourcePath = DashboardConstants.DASHBOARDS_RESOURCE_PATH
                                + fileName.substring(0, fileName.lastIndexOf(DashboardConstants.DASHBOARD_EXTENSION));
                        try {
                            DeploymentUtil.removeRegistryResource(resourcePath);
                        } catch (RegistryException e) {
                            String errorMsg = "Error deleting registry resource " + resourcePath;
                            log.error(errorMsg, e);
                            throw new DashboardDeploymentException(errorMsg, e);
                        }
                    }
                } catch (DeploymentException e) {
                    log.error("Error occurred while trying to undeploy : " + artifact.getName());
                }
            } else if (DashboardConstants.GADGET_ARTIFACT_TYPE.equals(artifact.getType())) {
                file.delete();
                log.info("Artifact [" + file.getName() + "] has been deleted from gadgets directory.");
            } else if (DashboardConstants.LAYOUT_ARTIFACT_TYPE.equals(artifact.getType())) {
                file.delete();
                log.info("Artifact [" + file.getName() + "] has been deleted from layouts directory.");
            }
        }
    }

    /**
     * Returns the absolute path for the artifact store location.
     *
     * @param artifactName
     * @return
     */
    protected String getArtifactPath(String artifactName) {
        String carbonRepository = CarbonUtils.getCarbonRepository();
        StringBuilder sb = new StringBuilder(carbonRepository);
        sb.append("jaggeryapps").append(File.separator)
                .append(DashboardConstants.APP_NAME).append(File.separator)
                .append("store").append(File.separator)
                .append("carbon.super").append(File.separator)
                .append(artifactName).append(File.separator);
        return sb.toString();
    }
}
