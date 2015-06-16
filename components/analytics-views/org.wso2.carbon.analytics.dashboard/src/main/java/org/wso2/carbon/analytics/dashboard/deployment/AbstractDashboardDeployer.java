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
import org.wso2.carbon.analytics.dashboard.DashboardConstants;
import org.wso2.carbon.analytics.dashboard.DashboardDeploymentException;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.ApplicationConfiguration;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all dashboard related artifact deployment.
 * A typical dashboard CApp contains following components.
 *  1. Dashboard defintion as a JSON file
 *  2. Layout definition of the dashboard. (This is a seperate folder with layout definition files)
 *  3. Gadget implementations (There can be many gadget implentations, each with a dedicated folder for that)
 */
public abstract class AbstractDashboardDeployer implements AppDeploymentHandler {

    /**
     * Returns the artifact type
     * @return
     */
    protected abstract String getArtifactType();

    /**
     * Deploy local artifacts
     * @param artifacts
     * @throws DashboardDeploymentException
     */
    protected abstract void deploy(List<Artifact> artifacts) throws DashboardDeploymentException;

    /**
     * Undeploy local artifacts
     * @param artifacts
     */
    protected abstract void undeploy(List<Artifact> artifacts);

    /**
     * Returns the absolute path for the artifact store location.
     * @param artifactName
     * @return
     */
    protected String getArtifactPath(String artifactName) {
        String carbonRepository = CarbonUtils.getCarbonRepository();
        StringBuilder sb = new StringBuilder(carbonRepository);
        sb.append("jaggeryapps").append(File.separator)
                .append(DashboardConstants.APP_NAME).append(File.separator)
                .append("store").append(File.separator)
                .append(artifactName).append(File.separator);
        return sb.toString();
    }

    @Override
    public void deployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {
        ApplicationConfiguration appConfig = carbonApplication.getAppConfig();
        List<Artifact.Dependency> deps = appConfig.getApplicationArtifact().getDependencies();
        List<Artifact> artifacts = new ArrayList<Artifact>();
        for (Artifact.Dependency dep : deps) {
            if (dep.getArtifact() != null) {
                artifacts.add(dep.getArtifact());
            }
        }
        deploy(artifacts);
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


}
