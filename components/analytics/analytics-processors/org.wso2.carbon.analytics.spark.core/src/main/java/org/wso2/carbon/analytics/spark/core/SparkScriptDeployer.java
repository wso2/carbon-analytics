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
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsScriptDeploymentException;
import org.wso2.carbon.analytics.spark.core.internal.AnalyticsPersistenceManager;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class SparkScriptDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(SparkScriptDeployer.class);

    @Override
    public void init(ConfigurationContext configurationContext) {
    }

    public void deploy(DeploymentFileData deploymentFileData) throws AnalyticsScriptDeploymentException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            JAXBContext context = JAXBContext.newInstance(AnalyticsScript.class);
            Unmarshaller un = context.createUnmarshaller();
            AnalyticsScript script = (AnalyticsScript) un.unmarshal(deploymentFileData.getFile());
            script.setName(getScriptName(deploymentFileData.getName()));
            AnalyticsPersistenceManager.getInstance().putScript(tenantId, script.getName(), script.getScriptContent(),
                    script.getCronExpression());
        } catch (JAXBException e) {
            String errorMsg = "Error while reading the analytics script : "
                    + deploymentFileData.getAbsolutePath();
            log.error(errorMsg, e);
            throw new AnalyticsScriptDeploymentException(errorMsg, e);
        } catch (AnalyticsPersistenceException e) {
            String errorMsg = "Error while storing the script : "
                    + deploymentFileData.getAbsolutePath();
            log.error(errorMsg);
            throw new AnalyticsScriptDeploymentException(errorMsg, e);
        }
    }

    public void undeploy(String fileName) throws AnalyticsScriptDeploymentException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            AnalyticsPersistenceManager.getInstance().deleteScript(tenantId, getScriptName(fileName));
        } catch (AnalyticsPersistenceException e) {
            String errorMsg = "Error while deleting the script : " + fileName;
            log.error(errorMsg, e);
            throw new AnalyticsScriptDeploymentException(errorMsg, e);
        }
    }

    @Override
    public void setDirectory(String s) {

    }

    @Override
    public void setExtension(String s) {

    }

    private String getScriptName(String fileName) throws AnalyticsPersistenceException {
        if (fileName.endsWith(AnalyticsConstants.SCRIPT_EXTENSION)) {
            return fileName.substring(0, fileName.length() - AnalyticsConstants.SCRIPT_EXTENSION.length());
        }
        return fileName;
    }
}
