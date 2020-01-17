/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.siddhi.distribution.editor.core.internal;

import io.siddhi.distribution.common.common.EventStreamService;
import io.siddhi.distribution.editor.core.exception.SiddhiAppDeploymentException;
import io.siddhi.distribution.editor.core.util.Constants;
import io.siddhi.distribution.editor.core.util.HostAddressFinder;
import io.siddhi.distribution.editor.core.util.SecurityUtil;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Startup component of Siddhi Editor.
 */
@Component(
        service = StartupComponent.class,
        immediate = true
)
public class StartupComponent {

    private static final Logger logger = LoggerFactory.getLogger(StartupComponent.class);

    @Activate
    protected void start(BundleContext bundleContext) {
        logger.debug("Editor Core Startup Listener Service Component is Activated");

        //Load siddhi apps in workspace directory
        String location = (Paths.get(Constants.RUNTIME_PATH,
                Constants.DIRECTORY_DEPLOYMENT)).toString();
        Path workspaceDirectoryPath = SecurityUtil.resolvePath(
                Paths.get(location).toAbsolutePath(), Paths.get(Constants.DIRECTORY_WORKSPACE));
        File directoryReference = workspaceDirectoryPath.toFile();
        File[] siddhiAppFileArray = directoryReference.listFiles();
        if (siddhiAppFileArray != null) {
            for (File siddhiAppFile : siddhiAppFileArray) {
                try {
                    InputStream inputStream = new FileInputStream(siddhiAppFile);
                    String siddhiApp = getStringFromInputStream(inputStream);
                    WorkspaceDeployer.deployConfigFile(siddhiAppFile.getName(), siddhiApp);
                } catch (Exception e) {
                    logger.error("Exception occurred when deploying the Siddhi App" + siddhiAppFile.getName(), e);
                }
            }
        }
    }

    @Deactivate
    protected void stop() {

        logger.debug("Editor Core Startup Listener Service Component is Deactivated");
    }

    @Reference(service = MicroservicesServer.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMicroservicesServer")
    protected void setMicroservicesServer(MicroservicesServer microservicesServer) {

        microservicesServer.getListenerConfigurations().entrySet().stream().forEach(entry -> {
            if (("http").equals(entry.getValue().getScheme())) {
                String hostname;
                try {
                    hostname = HostAddressFinder.findAddress(entry.getValue().getHost());
                } catch (SocketException e) {
                    hostname = entry.getValue().getHost();
                    logger.error("Error in finding address for provided hostname " + hostname + "." +
                            e.getMessage(), e);
                }
                String startingURL = entry.getValue().getScheme() + "://" + hostname + ":" + entry.getValue()
                        .getPort() + "/editor";
                logger.info("Editor Started on : " + startingURL);
            }
        });
        logger.debug("Microservices server registered to startup component of editor");
    }

    protected void unsetMicroservicesServer(MicroservicesServer microservicesServer) {

        logger.debug("Microservices server unregistered from startup component of editor");
    }

    @Reference(
            name = "event.stream.service",
            service = EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService"
    )
    protected void setEventStreamService(EventStreamService eventStreamService) {
        logger.debug("@Reference(bind) EventStreamService");
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        logger.debug("@Reference(unbind) EventStreamService");
    }

    private static String getStringFromInputStream(InputStream is) throws SiddhiAppDeploymentException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
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
}
