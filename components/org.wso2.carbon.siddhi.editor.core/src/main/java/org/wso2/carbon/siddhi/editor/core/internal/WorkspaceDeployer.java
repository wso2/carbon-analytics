/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.internal;

import org.apache.commons.io.FilenameUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.siddhi.editor.core.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.stream.processor.common.EventStreamService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * WorkspaceDeployer is responsible for all editor workspace related deployment tasks
 */
@Component(
        name = "workspace-artifact-deployer",
        immediate = true,
        service = Deployer.class
)
public class WorkspaceDeployer implements Deployer {
    private static final String WORKSPACE_DIRECTORY = "workspace";
    private static final Logger log = LoggerFactory.getLogger(WorkspaceDeployer.class);
    private static final String FILE_EXTENSION = "siddhi";
    private ArtifactType artifactType = new ArtifactType<>(FILE_EXTENSION);
    private URL directoryLocation;

    private static String getStringFromInputStream(InputStream is) throws SiddhiAppDeploymentException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            while ((line = br.readLine()) != null) {
                sb.append(System.getProperty("line.separator")).append(line);
            }
        } catch (IOException e) {
            throw new SiddhiAppDeploymentException("Exception when reading the Siddhi QL file", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new SiddhiAppDeploymentException("Exception when closing the Siddhi QL file stream", e);
                }
            }
        }
        return sb.toString();
    }

    private void deployConfigFile(File file) throws Exception {
        if (!file.getName().startsWith(".")) {
            InputStream inputStream = null;
            try {
                if (FilenameUtils.isExtension(file.getName(), FILE_EXTENSION)) {
                    String siddhiAppName = FilenameUtils.getBaseName(file.getName());
                    inputStream = new FileInputStream(file);
                    String siddhiApp = getStringFromInputStream(inputStream);
                    EditorDataHolder.getDebugProcessorService().deploy(siddhiAppName, siddhiApp);
                    log.info("Siddhi App " + siddhiAppName + " successfully deployed.");
                } else {
                    throw new SiddhiAppDeploymentException(("Error: File extension not supported for file name "
                            + file.getName() + ". Support only"
                            + FILE_EXTENSION + " ."));
                }
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        throw new SiddhiAppDeploymentException("Error when closing the Siddhi QL filestream", e);
                    }
                }
            }
        }
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do.
    }

    @Override
    public void init() {
        try {
            directoryLocation = new URL("file:" + WORKSPACE_DIRECTORY);
            log.info("Workspace artifact deployer initiated.");
        } catch (MalformedURLException e) {
            log.error("Error while initializing workspace artifact directoryLocation : "
                    + WORKSPACE_DIRECTORY, e);
        }
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        try {
            deployConfigFile(artifact.getFile());
        } catch (Exception e) {
            throw new CarbonDeploymentException(e.getMessage(), e);
        }
        return artifact.getFile().getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        try {
            String siddhiAppName = FilenameUtils.getBaseName((String) key);
            if (EditorDataHolder.getSiddhiAppMap().containsKey(siddhiAppName)) {
                EditorDataHolder.getDebugProcessorService().undeploy(siddhiAppName);
            }
        } catch (Exception e) {
            throw new CarbonDeploymentException(e.getMessage(), e);
        }
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        try {
            deployConfigFile(artifact.getFile());
        } catch (Exception e) {
            throw new CarbonDeploymentException(e.getMessage(), e);
        }
        return artifact.getName();
    }

    @Override
    public URL getLocation() {
        return directoryLocation;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    @Reference(
            name = "event.stream.service",
            service = EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService"
    )
    protected void setEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.info("@Reference(bind) EventStreamService");
        }
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.info("@Reference(unbind) EventStreamService");
        }
    }
}
