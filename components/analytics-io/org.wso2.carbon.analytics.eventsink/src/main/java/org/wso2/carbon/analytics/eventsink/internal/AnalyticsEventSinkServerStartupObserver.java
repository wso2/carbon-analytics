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
package org.wso2.carbon.analytics.eventsink.internal;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventStoreDeployer;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is to observe the server startup and do any deployments
 * which got triggered before the server fully startup.
 */

public class AnalyticsEventSinkServerStartupObserver implements ServerStartupObserver {

    private static Log log = LogFactory.getLog(AnalyticsEventSinkServerStartupObserver.class);
    private static AnalyticsEventSinkServerStartupObserver instance = new AnalyticsEventSinkServerStartupObserver();
    private AtomicBoolean started = new AtomicBoolean();

    private AnalyticsEventSinkServerStartupObserver() {
        this.started = new AtomicBoolean();
    }

    public static AnalyticsEventSinkServerStartupObserver getInstance() {
        return instance;
    }

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        PausedDeploymentHandler pausedDeploymentHandler = new PausedDeploymentHandler();
        pausedDeploymentHandler.start();
    }

    public boolean isServerStarted() {
        return started.get();
    }

    private class PausedDeploymentHandler extends Thread {

        public void run() {
            try {
                AnalyticsEventStoreDeployer deployer = (AnalyticsEventStoreDeployer)
                        CarbonUtils.getDeployer(AnalyticsEventStoreDeployer.class.getName());
                if (AnalyticsEventStoreDeployer.getPausedDeployments() != null) {
                    List<DeploymentFileData> pausedDeployment = AnalyticsEventStoreDeployer.getPausedDeployments();
                    started.set(true);
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    for (DeploymentFileData deploymentFileData : pausedDeployment) {
                        try {
                            deployer.deploy(deploymentFileData);
                        } catch (DeploymentException e) {
                            log.error("Error while  deploying analytics event store the file : "
                                    + deploymentFileData.getName(), e);
                        }
                    }
                    AnalyticsEventStoreDeployer.clearPausedDeployments();
                    PrivilegedCarbonContext.endTenantFlow();
                }
            } catch (CarbonException e) {
                log.error("Error when getting the deployer for evn store to proceed the initialization of deployments. ", e);
            }
        }
    }
}
