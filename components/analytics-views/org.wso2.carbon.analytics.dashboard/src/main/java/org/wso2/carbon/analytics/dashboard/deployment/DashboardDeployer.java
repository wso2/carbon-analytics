/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.dashboard.deployment;

import com.google.gson.Gson;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dashboard.Dashboard;
import org.wso2.carbon.analytics.dashboard.DashboardConstants;
import org.wso2.carbon.analytics.dashboard.DashboardDeploymentException;
import org.wso2.carbon.analytics.dashboard.internal.ServiceHolder;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.ApplicationConfiguration;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class deploys artifacts related to analytics dashboard such as dashboard definitions and gadgets
 */
public class DashboardDeployer implements AppDeploymentHandler {
    private static final Log log = LogFactory.getLog(DashboardDeployer.class);
    private Gson gson = new Gson();

    @Override
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfiguration)
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
            if (DashboardConstants.DASHBOARD_ARTIFACT_TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                if (files == null || files.isEmpty()) {
                    continue;
                }
                for (CappFile cappFile : files) {
                    String fileName = cappFile.getName();
                    String path = artifact.getExtractedPath() + File.separator + fileName;
                    File file = new File(path);
                    try {
                        if(DashboardConstants.DASHBOARD_DEFINITION_FILE.equals(file.getName())) {
                            String dashboardDefn = readFile(file);
                            Dashboard dashboard = gson.fromJson(dashboardDefn,Dashboard.class);
                            createRegistryResource(DashboardConstants.DASHBOARDS_RESOURCE_PATH + dashboard.getId() ,
                                    dashboardDefn)  ;
                            if(log.isDebugEnabled()) {
                                log.debug("Dashboard definition [" + dashboard.getId() + "] has been created.");
                            }
                        }
                        if(file.isDirectory()) {
                            String storePath = buildStorePath();
                            File destination = new File(storePath + file.getName());
                            copyFolder(file,destination);
                            if(log.isDebugEnabled()) {
                                log.debug("Gadget directory [" + file.getName() + "] has been copied to path "
                                        + destination.getAbsolutePath());
                            }
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
                }
            }
        }
    }

    @Override
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {
        log.info("** Undeploying **");
    }

    private String buildStorePath() {
        String carbonRepository = CarbonUtils.getCarbonRepository();
        StringBuilder sb = new StringBuilder(carbonRepository);
        sb.append("jaggeryapps").append(File.separator)
                .append(DashboardConstants.APP_NAME).append(File.separator)
                .append("store").append(File.separator)
                .append("gadget").append(File.separator);
        return sb.toString();
    }

    private void createRegistryResource(String url, Object content) throws RegistryException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Registry registry = ServiceHolder.getRegistryService().getConfigSystemRegistry(tenantId);
        Resource resource = registry.newResource();
        resource.setContent(gson.toJson(content));
        resource.setMediaType("application/json");
        registry.put(url, resource);
    }

    private  void copyFolder(File src, File dest) throws IOException {
        if(src.isDirectory()){
            //if directory not exists, create it
            if(!dest.exists()){
                dest.mkdir();
            }
            String files[] = src.list();
            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile,destFile);
            }
        }else{
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0){
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        }
    }

    private String readFile(File file) throws IOException {
        String content = null;
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            char[] chars = new char[(int) file.length()];
            reader.read(chars);
            content = new String(chars);
        } catch (IOException e) {
            throw e;
        } finally {
            reader.close();
        }
        return content;
    }
}