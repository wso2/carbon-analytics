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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsDeploymentException;
import org.wso2.carbon.analytics.spark.core.internal.AnalyticsDeployerManager;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class AnalyticsDeployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(AnalyticsDeployer.class);

    @Override
    public void init(ConfigurationContext configurationContext) {
        String repoPath = configurationContext.getAxisConfiguration().getRepository().getPath();
        File hotDeploymentDir = new File(repoPath + File.separator + AnalyticsConstants.SCRIPT_STORE_DIR_NAME);
        if (!hotDeploymentDir.exists()) {
            hotDeploymentDir.mkdir();
        }
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (ServiceHolder.getTaskService() != null) {
            try {
                JAXBContext context = JAXBContext.newInstance(AnalyticsScript.class);
                Unmarshaller un = context.createUnmarshaller();
                AnalyticsScript analyticsScript = (AnalyticsScript) un.unmarshal(deploymentFileData.getFile());
                AnalyticsDeployerManager.getInstance().deploy(tenantId, analyticsScript);
                log.info("Successfully deployed file : " + deploymentFileData.getName());
            } catch (JAXBException e) {
                log.error("Error while loading the script :" + deploymentFileData.getName()
                        + " for tenantId: " + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), e);
                throw new AnalyticsDeploymentException("Error while loading the script :" + deploymentFileData.getName()
                        + " for tenantId: " + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            } catch (AnalyticsDeploymentException e) {
                log.error("Error while loading the script." + e.getErrorMessage(), e);
            }
        }else {
            AnalyticsDeployerManager.getInstance().addPausedDeployment(deploymentFileData);
        }
    }

    public void undeploy(String fileName) throws DeploymentException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String analyticsName = new File(fileName).getName();
        if (analyticsName.lastIndexOf(AnalyticsConstants.SCRIPT_EXTENSION) ==
                (analyticsName.length() - AnalyticsConstants.SCRIPT_EXTENSION.length())) {
            analyticsName = analyticsName.substring(0, analyticsName.lastIndexOf(AnalyticsConstants.SCRIPT_EXTENSION));
        }
        try {
            AnalyticsDeployerManager.getInstance().unDeploy(tenantId, analyticsName);
            log.info("Successfully undeployed file : "+ fileName);
        }catch (AnalyticsDeploymentException ex){
            log.error("Error while undeploying the file : "+ fileName+". "+ ex.getErrorMessage(), ex);
            throw ex;
        }
    }

    @Override
    public void setDirectory(String directory) {
    }

    @Override
    public void setExtension(String extension) {
    }
}
