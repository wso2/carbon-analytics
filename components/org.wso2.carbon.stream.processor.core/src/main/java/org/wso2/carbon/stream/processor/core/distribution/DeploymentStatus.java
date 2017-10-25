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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container class to send back deployment status
 */
public class DeploymentStatus {
    //// TODO: 10/25/17 Add place holder to propagate failure reason
    private boolean isDeployed;
    private Map<String, List<String>> deploymentDataMap = new HashMap<>();

    public DeploymentStatus(boolean isDeployed,
                            Map<String, List<String>> deploymentDataMap) {
        this.isDeployed = isDeployed;
        this.deploymentDataMap = deploymentDataMap;
    }

    public boolean isDeployed() {
        return isDeployed;
    }

    public Map<String, List<String>> getDeploymentDataMap() {
        return deploymentDataMap;
    }

    /**
     * Method to get deployment data for a given group. List of IP addresses indicating deployment location of
     * queries belonging to given group.
     *
     * @param groupName Name of the group to retrieve deployment data
     * @return List of IP addresses. Will return null if query group name is incorrect.
     */
    public List<String> getDeploymentDataForGroup(String groupName) {
        return deploymentDataMap.get(groupName);
    }
}
