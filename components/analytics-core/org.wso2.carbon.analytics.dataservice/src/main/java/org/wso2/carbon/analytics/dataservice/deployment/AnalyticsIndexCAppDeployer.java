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

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.Constants;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsIndexConfiguration;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;

/**
 * This class represents the implementation of Carbon application deployer for analytics index.
 */
public class AnalyticsIndexCAppDeployer implements AppDeploymentHandler {
    private static final Log log = LogFactory.getLog(AnalyticsIndexCAppDeployer.class);
    private static final String TYPE = "analytics/index";

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
                        deploy(artifactPath);
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                    } catch (DeploymentException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        throw e;
                    }
                } else if (files.size() != 0) {
                    log.error("Analytics Indices must have a single XML file to " +
                            "be deployed. But " + files.size() + " files found.");
                }
            }
        }
    }

    private void deploy(String deploymentFilePath) throws AnalyticsIndexDeploymentException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        File deploymentFile  = new File(deploymentFilePath);
        try {
            log.info("Deploying analytics indices from file : " + deploymentFile.getName() + " for tenant id :" + tenantId);
            JAXBContext context = JAXBContext.newInstance(AnalyticsIndexConfiguration.class);
            Unmarshaller un = context.createUnmarshaller();
            AnalyticsIndexConfiguration configuration =
                    (AnalyticsIndexConfiguration) un.unmarshal(deploymentFile);
//            AnalyticsServiceHolder.getAnalyticsDataService().setIndices(tenantId,
//                    getTableNameFromAnalyticsIndexFileName(deploymentFile.getName()),
//                    configuration.getIndexColumnsMap(), configuration.getScoreParams());
        } catch (JAXBException e) {
            String errorMsg = "Error while reading from the file : " + deploymentFile.getAbsolutePath();
            log.error(errorMsg, e);
            throw new AnalyticsIndexDeploymentException(errorMsg, e);
//        } catch (AnalyticsIndexException e) {
//            String errorMsg = "Error setting the indices from file : " + deploymentFile.getAbsolutePath();
//            log.error(errorMsg, e);
//            throw new AnalyticsIndexDeploymentException(errorMsg, e);
        }
    }

    private String getTableNameFromAnalyticsIndexFileName(String filePath) {
        String fileName = new File(filePath).getName();
        return fileName.substring(0, fileName.length() - Constants.ANALYTICS_INDICES_FILE_EXTENSION.length() - 1);
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
                    log.error("Spark script application must have a single spark script file. But " +
                            files.size() + " files found.");
                    continue;
                }
                if (AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED.
                        equals(artifact.getDeploymentStatus())) {
                    String fileName = artifact.getFiles().get(0).getName();
                    String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                    try {
                        undeploy(artifactPath);
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_PENDING);
                    } catch (DeploymentException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        log.error("Error occured while trying to undeploy : " + artifact.getName());
                    }
                }
            }
        }
    }

    private void undeploy(String fileName) throws AnalyticsIndexDeploymentException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Undeploying the analytics indices from file : "+ fileName +" for tenant id :" + tenantId);
        String tableName = getTableNameFromAnalyticsIndexFileName(fileName);
//        try {
//            AnalyticsServiceHolder.getAnalyticsDataService().setIndices(tenantId,
//                    tableName, new HashMap<String, IndexType>(), new ArrayList<String>());
//        } catch (AnalyticsIndexException e) {
//            String errorMsg = "Error undeploying the analytics index file : " + fileName
//                    + " for tenant id : " + tenantId;
//            log.error(errorMsg, e);
//            throw new AnalyticsIndexDeploymentException(errorMsg, e);
//        }
    }
}
