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
package org.wso2.carbon.analytics.eventsink;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreDeploymentException;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.List;

/**
 * This class represents the implementation of Carbon application deployer for analytics event store.
 */
public class AnalyticsEventStoreCAppDeployer implements AppDeploymentHandler {
    private static final Log log = LogFactory.getLog(AnalyticsEventStoreCAppDeployer.class);
    private static final String TYPE = "analytics/eventstore";

    @Override
    public void deployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {
        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                if (files.size() == 1) {
                    String fileName = files.get(0).getName();
                    String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                    try {
                        Deployer deployer = CarbonUtils.getDeployer(AnalyticsEventStoreDeployer.class.getName());
                        deployer.deploy(new DeploymentFileData(new File(artifactPath)));
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                    } catch (DeploymentException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        throw e;
                    } catch (CarbonException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        String msg = "Error while getting the deployer instance " +
                                "for event store artifact. ";
                        log.error(msg, e);
                        throw new AnalyticsEventStoreDeploymentException(msg, e);
                    }
                } else if (files.size() != 0) {
                    log.error("Analytics Indices must have a single XML file to " +
                            "be deployed. But " + files.size() + " files found.");
                }
            }
        }
    }

    @Override
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {
        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                if (files.size() != 1) {
                    log.error("Analytics event store application must have a single analytics event store file. But " +
                            files.size() + " files found.");
                    continue;
                }
                if (AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED.
                        equals(artifact.getDeploymentStatus())) {
                    String fileName = artifact.getFiles().get(0).getName();
                    String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                    try {
                        Deployer deployer = CarbonUtils.getDeployer(AnalyticsEventStoreDeployer.class.getName());
                        deployer.undeploy(artifactPath);
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_PENDING);
                    } catch (DeploymentException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        log.error("Error occurred while trying to undeploy : " + artifact.getName());
                    } catch (CarbonException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        String msg = "Error while getting the deployer instance " +
                                "for event store artifact. ";
                        log.error(msg, e);
                        throw new AnalyticsEventStoreDeploymentException(msg, e);
                    }
                }
            }
        }
    }
}
