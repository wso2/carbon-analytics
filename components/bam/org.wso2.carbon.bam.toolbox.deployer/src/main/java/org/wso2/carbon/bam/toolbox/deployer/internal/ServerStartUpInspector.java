package org.wso2.carbon.bam.toolbox.deployer.internal;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.core.BAMToolBoxDeployer;
import org.wso2.carbon.core.ServerStartupHandler;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class ServerStartUpInspector implements ServerStartupHandler {
    private static final Log log = LogFactory.getLog(ServerStartUpInspector.class);
    private static boolean serverStarted = false;


    @Override
    public void invoke() {
        serverStarted = true;
        log.info("BAM Toolbox is ready to do deployments");
        try {
            doPausedDeployments();
        } catch (DeploymentException e) {
            log.error("Failed to deploy in the server start up", e);
        }
    }

    public static boolean isServerStarted() {
        return serverStarted;
    }

    private static void doPausedDeployments() throws DeploymentException {
        BAMToolBoxDeployer deployer = BAMToolBoxDeployer.getPausedDeployments();
        if (null != deployer) {
            deployer.doInitialUnDeployments();
            for (DeploymentFileData fileData : deployer.getPausedDeploymentFileDatas()) {
                fileData.deploy();
            }
        }

    }
}
