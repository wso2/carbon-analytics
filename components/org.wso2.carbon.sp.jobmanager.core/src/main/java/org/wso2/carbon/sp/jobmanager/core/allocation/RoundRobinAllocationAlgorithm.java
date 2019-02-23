/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sp.jobmanager.core.allocation;

import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.appcreator.SiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;

import java.util.Iterator;
import java.util.Map;

/**
 * RoundRobin Allocation Algorithm implementation
 */
public class RoundRobinAllocationAlgorithm implements ResourceAllocationAlgorithm {
    private static final Logger logger = Logger.getLogger(RoundRobinAllocationAlgorithm.class);
    private Iterator resourceIterator;

    @Override
    public ResourceNode getNextResourceNode(Map<String, ResourceNode> resourceNodeMap, int minResourceCount ,
                                            SiddhiQuery siddhiQuery) {
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        if (deploymentConfig != null && !resourceNodeMap.isEmpty()) {
            if (resourceNodeMap.size() >= minResourceCount) {
                if (resourceIterator == null) {
                    resourceIterator = resourceNodeMap.values().iterator();
                }
                if (resourceIterator.hasNext()) {
                    return (ResourceNode) resourceIterator.next();
                } else {
                    resourceIterator = resourceNodeMap.values().iterator();
                    if (resourceIterator.hasNext()) {
                        return (ResourceNode) resourceIterator.next();
                    }
                }
            } else {
                logger.error("Minimum resource requirement did not match, hence not deploying the partial siddhi app ");
            }
        }
        return null;
    }
}
