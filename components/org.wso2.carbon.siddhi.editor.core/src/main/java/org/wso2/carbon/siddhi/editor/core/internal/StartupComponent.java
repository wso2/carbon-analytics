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

package org.wso2.carbon.siddhi.editor.core.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.editor.core.util.HostAddressFinder;
import org.wso2.msf4j.MicroservicesServer;

import java.net.SocketException;

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
}
