/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.analytics.dataservice.deployment;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.Constants;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * This class represents the implementation of Carbon application deployer for analytics index.
 */
public class AnalyticsIndexCAppDeployer implements AppDeploymentHandler {
    private static final Log log = LogFactory.getLog(AnalyticsIndexCAppDeployer.class);
    private static final String TYPE = "application/xml";
    private Map<String, Boolean> acceptanceList = null;

    @Override
    public void deployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {
        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        // loop through all artifacts
        for (Artifact.Dependency dep : artifacts) {
            Deployer deployer;
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            // check whether the needed features are installed
            if (!isAccepted(artifact.getType())) {
                log.warn("Can't deploy artifact : " + artifact.getName() + " of type : " +
                        artifact.getType() + ". Required features are not installed in the system");
                continue;
            }
            // for each service type, select the deployer
            if (TYPE.equals(artifact.getType())) {
                deployer = AppDeployerUtils.getArtifactDeployer(axisConfiguration,
                        Constants.ANALYTICS_INDICES_DEPLOYMENT_DIR_NAME,
                        Constants.ANALYTICS_INDICES_FILE_EXTENSION);
            } else {
                continue;
            }
            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("Analytics Indices must have a single XML file to " +
                        "be deployed. But " + files.size() + " files found.");
                continue;
            }
            // call the index deployer to deploy
            if (deployer != null) {
                String fileName = artifact.getFiles().get(0).getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                try {
                    deployer.deploy(new DeploymentFileData(new File(artifactPath), deployer));
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                } catch (DeploymentException e) {
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                    throw e;
                }
            }
        }
    }

    @Override
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {

    }

    private boolean isAccepted(String serviceType) {
        if (acceptanceList == null) {
            acceptanceList = AppDeployerUtils.buildAcceptanceList(AnalyticsServiceHolder.getRequiredFeatures());
        }
        Boolean acceptance = acceptanceList.get(serviceType);
        return (acceptance == null || acceptance);
    }
}
