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
package org.wso2.carbon.analytics.spark.core.internal;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.analytics.spark.core.SparkScriptDeployer;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AnalyticsServerStartupObserver implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(AnalyticsServerStartupObserver.class);
    private static AnalyticsServerStartupObserver instance = new AnalyticsServerStartupObserver();
    private AtomicBoolean initialized;
    private List<DeploymentFileData> pausedDeployments;

    private AnalyticsServerStartupObserver() {
        this.initialized = new AtomicBoolean();
        this.pausedDeployments = new ArrayList<>();
    }

    public static AnalyticsServerStartupObserver getInstance() {
        return instance;
    }

    @Override
    public void completingServerStartup() {
        initialized.set(true);
        deployPausedDeployments();
    }

    @Override
    public void completedServerStartup() {
        initialized.set(true);
        deployPausedDeployments();
    }

    private void deployPausedDeployments() {
        try {
            Deployer deployer = CarbonUtils.getDeployer(SparkScriptDeployer.class.getName());
            for (DeploymentFileData deploymentFileData : this.pausedDeployments) {
                try {
                    deployer.deploy(deploymentFileData);
                } catch (DeploymentException e) {
                    log.error("Failed to process the paused deployment of spark script : "
                            + deploymentFileData.getName(), e);
                }
            }
            pausedDeployments.clear();
        } catch (CarbonException e) {
            log.error("Error while deploying the paused spark scripts. ", e);
        }
    }

    public boolean getInitialized() {
        return this.initialized.get();
    }

    public void addPausedDeployment(DeploymentFileData deploymentFileData) {
        this.pausedDeployments.add(deploymentFileData);
    }
}
