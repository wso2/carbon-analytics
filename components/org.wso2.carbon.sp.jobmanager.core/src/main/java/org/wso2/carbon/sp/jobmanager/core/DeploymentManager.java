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

package org.wso2.carbon.sp.jobmanager.core;

import org.wso2.carbon.sp.jobmanager.core.appcreator.DistributedSiddhiQuery;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;

/**
 * This interface contains abstract method to deploy, undeploy distributed siddhi app.
 */

public interface DeploymentManager {
    /**
     * Deploy a distributed Siddhi app.
     *
     * @param distributedSiddhiQuery distributed Siddhi app
     * @return the deployment status of the Siddhi app
     */
    DeploymentStatus deploy(DistributedSiddhiQuery distributedSiddhiQuery , boolean metricscheduling);

    /**
     * Un-deploy a distributed Siddhi app.
     *
     * @param siddhiAppName distributed Siddhi app name
     * @return boolean representing whether the app is un deployed or not
     */
    boolean unDeploy(String siddhiAppName);

    /**
     * Get the deployment status of a distributed Siddhi app.
     *
     * @param parentSiddhiAppName parent Siddhi app name.
     * @return boolean stating whether the app is deployed or not.
     */
    boolean isDeployed(String parentSiddhiAppName);
}
