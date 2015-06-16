/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.analytics.dashboard.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dashboard.DashboardConstants;
import org.wso2.carbon.analytics.dashboard.DashboardDeploymentException;
import org.wso2.carbon.analytics.dashboard.util.DeploymentUtil;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Deploys gadget definitions into /store/gadget folder.
 */
public class GadgetDeployer extends AbstractDashboardDeployer {
    private static final Log log = LogFactory.getLog(GadgetDeployer.class);

    @Override
    protected String getArtifactType() {
        return DashboardConstants.GADGET_ARTIFACT_TYPE;
    }

    /**
     * Simply copy layout defintions artifacts in to /store/layout directory
     * @param artifacts
     * @throws DashboardDeploymentException
     */
    @Override
    protected void deploy(List<Artifact> artifacts) throws DashboardDeploymentException {
        for (Artifact artifact : artifacts) {
            if (DashboardConstants.GADGET_ARTIFACT_TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                if (files == null || files.isEmpty()) {
                    continue;
                }
                for (CappFile cappFile : files) {
                    String fileName = cappFile.getName();
                    String path = artifact.getExtractedPath() + File.separator + fileName;
                    File file = new File(path);
                    try {
                        if (file.isDirectory()) {
                            String storePath = getArtifactPath("gadget");
                            File destination = new File(storePath + file.getName());
                            DeploymentUtil.copyFolder(file, destination);
                            if (log.isDebugEnabled()) {
                                log.debug("Gadget directory [" + file.getName() + "] has been copied to path "
                                        + destination.getAbsolutePath());
                            }
                        }
                    } catch (IOException e) {
                        String errorMsg = "Error while reading from the file : " + file.getAbsolutePath();
                        log.error(errorMsg, e);
                        throw new DashboardDeploymentException(errorMsg, e);
                    }
                }
            }
        }

    }

    @Override
    protected void undeploy(List<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            if (DashboardConstants.GADGET_ARTIFACT_TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                String fileName = artifact.getFiles().get(0).getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                File file = new File(artifactPath);
                file.delete();
                if (log.isDebugEnabled()) {
                    log.debug("Artifact [" + file.getName() + "] has been deleted from gadgets directory.");
                }
            }
        }
    }
}
