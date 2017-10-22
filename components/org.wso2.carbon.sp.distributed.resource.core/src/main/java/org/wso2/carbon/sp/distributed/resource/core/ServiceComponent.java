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

package org.wso2.carbon.sp.distributed.resource.core;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.sp.distributed.resource.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.distributed.resource.core.bean.NodeConfig;
import org.wso2.carbon.sp.distributed.resource.core.exception.ResourceNodeException;
import org.wso2.carbon.sp.distributed.resource.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.distributed.resource.core.util.HeartbeatSender;
import org.wso2.carbon.sp.distributed.resource.core.util.ResourceConstants;
import org.wso2.carbon.utils.Utils;

import java.util.Map;
import java.util.Timer;

/**
 * ServiceComponent for the Resource.
 */
@Component(
        name = "sp-resource-component",
        service = ServiceComponent.class,
        immediate = true
)
public class ServiceComponent {
    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    /**
     * Timer to schedule heartbeat sending task.
     */
    private Timer timer;

    /**
     * Activation method of Resource ServiceComponent. This will be called when all of its references are satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception will be thrown if an issue occurs while executing the activate method.
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        if (ResourceConstants.RUNTIME_NAME_WORKER.equalsIgnoreCase(Utils.getRuntimeName())
                && ResourceConstants.MODE_DISTRIBUTED.equalsIgnoreCase(ServiceDataHolder.getDeploymentMode())) {
            log.info("WSO2 Stream Processor starting in distributed mode as a resource node.");
            /* If the node started in worker runtime with distributed mode enabled, Then, join a resource pool and
             * start periodically sending heartbeats.
             */
            timer = new Timer();
            timer.schedule(new HeartbeatSender(timer), 0);
            /* Since the above scheduled job will run on a different thread, explicitly wait until the leader node is
             * discovered. So that, making this a mandatory reference for the Stream Processor Core, makes the
             * deployment Siddhi Apps to wait until there's a leader node. It will help avoiding un necessary processing
             * of Siddhi Apps.
             */
            while (ServiceDataHolder.getLeaderNodeConfig() == null) {
                // Sleep the thread in-between to stop busy-spinning.
                Thread.sleep(200);
            }
        }
    }

    /**
     * This is the deactivation method of ServiceComponent.
     * This will be called when this component is being stopped.
     *
     * @throws Exception will be thrown if an issue occurs while executing the de-activate method.
     */
    @Deactivate
    protected void stop() throws Exception {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    /**
     * Register carbon {@link ConfigProvider} to be used with config reading.
     *
     * @param configProvider {@link ConfigProvider} reference.
     * @throws ResourceNodeException Will be thrown upon failure to read deployment.yaml
     */
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) throws ResourceNodeException {
        ServiceDataHolder.setConfigProvider(configProvider);
        if (ResourceConstants.RUNTIME_NAME_WORKER.equalsIgnoreCase(Utils.getRuntimeName())) {
            // Read deployment config / manager node config
            DeploymentConfig deploymentConfig;
            NodeConfig currentNodeConfig;
            try {
                if (configProvider.getConfigurationObject(ResourceConstants.DEPLOYMENT_CONFIG_NS) != null) {
                    if (ResourceConstants.MODE_DISTRIBUTED.equalsIgnoreCase((String) ((Map) configProvider
                            .getConfigurationObject(ResourceConstants.DEPLOYMENT_CONFIG_NS)).get("type"))) {
                        deploymentConfig = configProvider.getConfigurationObject(DeploymentConfig.class);
                        if (deploymentConfig != null) {
                            // Id will be in a separate namespace (wso2.carbon), therefore manually set it.
                            String id = (String) ((Map) configProvider
                                    .getConfigurationObject("wso2.carbon")).get("id");
                            currentNodeConfig = new NodeConfig().setId(id)
                                    .setHttpInterface(deploymentConfig.getHttpInterface());
                            ServiceDataHolder.setDeploymentConfig(deploymentConfig);
                            ServiceDataHolder.setCurrentNodeConfig(currentNodeConfig);
                            ServiceDataHolder.setDeploymentMode(deploymentConfig.getType());
                            ServiceDataHolder.getResourceManagers().addAll(deploymentConfig.getResourceManagers());
                        } else {
                            throw new ResourceNodeException("Couldn't read " + ResourceConstants.DEPLOYMENT_CONFIG_NS +
                                    " from deployment.yaml");
                        }
                    }
                } else {
                    throw new ResourceNodeException(ResourceConstants.DEPLOYMENT_CONFIG_NS + " is not specified in " +
                            "deployment.yaml");
                }
            } catch (ConfigurationException e) {
                throw new ResourceNodeException("Error while reading " + ResourceConstants.DEPLOYMENT_CONFIG_NS +
                        " from deployment.yaml", e);
            }
        }
    }

    /**
     * Unregister ConfigProvider and unset node config and deployment config.
     *
     * @param configProvider configProvider.
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        ServiceDataHolder.setConfigProvider(null);
        ServiceDataHolder.setDeploymentConfig(null);
        ServiceDataHolder.setCurrentNodeConfig(null);
    }
}
