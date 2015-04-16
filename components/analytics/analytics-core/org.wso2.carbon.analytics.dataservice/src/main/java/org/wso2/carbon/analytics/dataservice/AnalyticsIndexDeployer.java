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
package org.wso2.carbon.analytics.dataservice;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsIndexConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class AnalyticsIndexDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(AnalyticsIndexDeployer.class);

    private static final String EXTENSION = "xml";
    private static final String DIR_NAME = "analytics-indices";

    @Override
    public void init(ConfigurationContext configurationContext) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String repository;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            repository = CarbonUtils.getCarbonRepository();
        } else {
            repository = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId;
        }
        repository += File.separator + DIR_NAME;
        File file = new File(repository);
        if (!file.exists()) {
            boolean dirCreated = file.mkdirs();
            if (!dirCreated) {
                log.warn("Cannot successfully create the deployment dir for analytics indices in path: "
                        + file.getAbsolutePath());
            }
        }
    }

    @Override
    public void setDirectory(String s) {

    }

    /**
     * This method will deploy the analytics index deployer. Here the file name should be same as the table name
     * for which the user is willing to set the index.
     *
     * @param deploymentFileData The actual deployment file data of analytics index.
     * @throws AnalyticsIndexDeploymentException
     */
    public void deploy(DeploymentFileData deploymentFileData) throws AnalyticsIndexDeploymentException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (AnalyticsDataServiceServerStartupObserver.getInstance().getInitialized()) {
            try {
                log.info("Deploying analytics indices from file : " + deploymentFileData.getName() + " for tenant id :" + tenantId);
                JAXBContext context = JAXBContext.newInstance(AnalyticsIndexConfiguration.class);
                Unmarshaller un = context.createUnmarshaller();
                AnalyticsIndexConfiguration configuration =
                        (AnalyticsIndexConfiguration) un.unmarshal(deploymentFileData.getFile());
                AnalyticsServiceHolder.getAnalyticsDataService().setIndices(tenantId,
                        getTableNameFromAnalyticsIndexFileName(deploymentFileData.getName()),
                        configuration.getIndexColumnsMap(), configuration.getScoreParams());
            } catch (JAXBException e) {
                String errorMsg = "Error while reading from the file : " + deploymentFileData.getAbsolutePath();
                log.error(errorMsg, e);
                throw new AnalyticsIndexDeploymentException(errorMsg, e);
            } catch (AnalyticsIndexException e) {
                String errorMsg = "Error setting the indices from file : " + deploymentFileData.getAbsolutePath();
                log.error(errorMsg, e);
                throw new AnalyticsIndexDeploymentException(errorMsg, e);
            }
        }else {
            AnalyticsDataServiceServerStartupObserver.getInstance().addPausedDeployment(deploymentFileData);
        }
    }

    public void undeploy(String fileName) throws AnalyticsIndexDeploymentException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Undeploying the analytics indices from file : "+ fileName +" for tenant id :" + tenantId);
        String tableName = getTableNameFromAnalyticsIndexFileName(fileName);
        try {
            AnalyticsServiceHolder.getAnalyticsDataService().setIndices(tenantId,
                    tableName, new HashMap<String, IndexType>(), new ArrayList<String>());
        } catch (AnalyticsIndexException e) {
            String errorMsg = "Error undeploying the analytics index file : " + fileName
                    + " for tenant id : " + tenantId;
            log.error(errorMsg, e);
            throw new AnalyticsIndexDeploymentException(errorMsg, e);
        }
    }

    private String getTableNameFromAnalyticsIndexFileName(String filePath) {
        String fileName = new File(filePath).getName();
        return fileName.substring(0, fileName.length() - EXTENSION.length() - 1);
    }

    @Override
    public void setExtension(String s) {

    }
}
