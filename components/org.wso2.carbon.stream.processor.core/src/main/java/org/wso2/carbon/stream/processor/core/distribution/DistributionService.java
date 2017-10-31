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

package org.wso2.carbon.stream.processor.core.distribution;


import org.wso2.carbon.stream.processor.core.util.DeploymentMode;
import org.wso2.carbon.stream.processor.core.util.RuntimeMode;

/**
 * Parent interface of Distribution Service. This extension point can be used to implement various distribution
 * providers.
 */
public interface DistributionService {
    /**
     * Distribute the given Siddhi App into available resources as governed by the underlying
     * implementation.
     *
     * @param userDefinedSiddhiApp String representation of user-defined distributed Siddhi App
     * @return Status of the deployment including node connection details to edge nodes with which user will
     * collaborate.
     */
    DeploymentStatus distribute(String userDefinedSiddhiApp);

    /**
     * Method to provide current node's Runtime Mode. Value can either be MANAGER or RESOURCE. Value will be
     * based on the profile user use to start the node. If user start the node using manager.sh/bat then Runtime Mode
     * will be MANAGER. Service consumer will use this information to make decisions regarding Siddhi App deployment.
     *
     * @return Runtime Mode of current node
     */
    RuntimeMode getRuntimeMode();

    /**
     * Method to provide current node's Deployment Mode. Possible options are DISTRIBUTED, HA and SINGLE_NODE. Value
     * will be based on user's input in deployment.yaml(deployment.config:type). Service consumer will use this
     * information to make decisions regarding Siddhi App deployment.
     *
     * @return Deployment Mode for current node
     */
    DeploymentMode getDeploymentMode();

    /**
     * Method to check whether given Siddhi App is already deployed in the distributed environment. In case of
     * manager restart manager should check the status using this method and refrain from re-deploying. In case of a
     * resource node this method won't be called and should return <tt>false</tt> if called.
     *
     * @param parentSiddhiAppName Name of user defined Siddhi App to check status
     * @return <tt>true</tt> if parent siddhi app is already deployed in distributed environment.
     */
    boolean isDistributed(String parentSiddhiAppName);

    /**
     * Method to undeploy a given Siddhi App. When the user undeploys a Siddhi App it should be communicated to
     * distributed service using this method. In case of a resource node this won'r be called and resource node
     * should do nothing.
     *
     * @param parentSiddhiAppName Name of the parent Siddhi App which needs to be undeployed.
     */
    void undeploy(String parentSiddhiAppName);
}
