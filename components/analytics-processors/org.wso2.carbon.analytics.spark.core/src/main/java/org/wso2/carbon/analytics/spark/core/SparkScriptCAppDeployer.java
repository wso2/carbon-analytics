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
package org.wso2.carbon.analytics.spark.core;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.core.exception.SparkScriptDeploymentException;
import org.wso2.carbon.analytics.spark.core.internal.AnalyticsPersistenceManager;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
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
 * This class represents the implementation of Carbon application deployer for spark script.
 */
public class SparkScriptCAppDeployer implements AppDeploymentHandler {
    private static final Log log = LogFactory.getLog(SparkScriptCAppDeployer.class);
    private static final String TYPE = "analytics/spark";

    @Override
    public void deployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {
        // disabling spark scripts deploying if analytics execution is disabled DAS-133
        if (!ServiceHolder.isAnalyticsExecutionEnabled()) {
            if(log.isDebugEnabled()){
                log.debug("Spark Script deployment is omitted because Analytics Execution is disabled");
            }
            return;
        }
        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        // loop through all artifacts
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                if (files.size() == 1) {
                    String fileName = files.get(0).getName();
                    fileName = GenericUtils.checkAndReturnPath(fileName);
                    String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                    try {
                        deploy(artifactPath, carbonApplication.getAppName());
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                    } catch (DeploymentException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        throw e;
                    }
                } else if (files.size() != 0) {
                    log.warn("Spark script must have a single XML file to be deployed. But "
                            + files.size() + " files found");
                }
            }
        }
    }

    private void deploy(String scriptFilePath, String carbonAppName) throws SparkScriptDeploymentException {
        File deploymentFileData = new File(scriptFilePath);
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            log.info("Deploying spark script: " + deploymentFileData.getName() + " for tenant : " + tenantId);
            JAXBContext context = JAXBContext.newInstance(AnalyticsScript.class);
            Unmarshaller un = context.createUnmarshaller();
            AnalyticsScript script = (AnalyticsScript) un.unmarshal(deploymentFileData);
            script.setName(getScriptName(deploymentFileData.getName()));
            AnalyticsPersistenceManager.getInstance().putScript(tenantId, script.getName(), script.getScriptContent(),
                    script.getCronExpression(), carbonAppName, false);
        } catch (JAXBException e) {
            String errorMsg = "Error while reading the analytics script : "
                    + deploymentFileData.getAbsolutePath();
            log.error(errorMsg, e);
            throw new SparkScriptDeploymentException(errorMsg, e);
        } catch (AnalyticsPersistenceException e) {
            String errorMsg = "Error while storing the script : "
                    + deploymentFileData.getAbsolutePath();
            log.error(errorMsg);
            throw new SparkScriptDeploymentException(errorMsg, e);
        }
    }

    private String getScriptName(String filePath) throws AnalyticsPersistenceException {
        String fileName = new File(filePath).getName();
        if (fileName.endsWith(AnalyticsConstants.SCRIPT_EXTENSION)) {
            return fileName.substring(0, fileName.length() - (AnalyticsConstants.SCRIPT_EXTENSION.length() +
                    AnalyticsConstants.SCRIPT_EXTENSION_SEPARATOR.length()));
        }
        return fileName;
    }

    @Override
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {
        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        // loop through all artifacts
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
                    fileName = GenericUtils.checkAndReturnPath(fileName);
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

    private void undeploy(String scriptFileName) throws SparkScriptDeploymentException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            log.info("Undeploying spark script : " + scriptFileName + " for tenant id : " + tenantId);
            AnalyticsPersistenceManager.getInstance().deleteScript(tenantId, getScriptName(scriptFileName));
        } catch (AnalyticsPersistenceException e) {
            String errorMsg = "Error while deleting the script : " + scriptFileName;
            log.error(errorMsg, e);
            throw new SparkScriptDeploymentException(errorMsg, e);
        }
    }
}
