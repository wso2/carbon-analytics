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
import org.wso2.carbon.sp.jobmanager.core.exception.ResourceManagerException;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Allocation algorithm based on CPU utilization implementation
 */
public class CPUBasedAllocationAlgorithm implements ResourceAllocationAlgorithm {
    private static final Logger logger = Logger.getLogger(CPUBasedAllocationAlgorithm.class);
    private static final double SYSTEM_CPU_WEIGHT = 1;
    private static final double PROCESS_CPU_WEIGHT = 1;
    private DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();

    @Override
    public ResourceNode getNextResourceNode(Map<String, ResourceNode> resourceNodeMap, int minResourceCount
    , SiddhiQuery siddhiQuery) {
        long initialTimestamp = System.currentTimeMillis();
        if (deploymentConfig != null && !resourceNodeMap.isEmpty()) {
            if (resourceNodeMap.size() >= minResourceCount) {
                Iterator resourceIterator = resourceNodeMap.values().iterator();
                while(true) {
                    try {
                        return getMaximumResourceNode(resourceIterator);
                    } catch (ResourceManagerException e) {
                        if ((System.currentTimeMillis() - initialTimestamp) >= (deploymentConfig.
                                getHeartbeatInterval() * 2))
                            throw e;
                    }
                }
            } else {
                logger.error("Minimum resource requirement did not match, hence not deploying the partial siddhi app ");
            }
        }
        return null;
    }

    private ResourceNode getMaximumResourceNode(Iterator resourceIterator) {
        Map<String, Double> unsortedMap = new HashMap<>();
        while (resourceIterator.hasNext()) {
            ResourceNode resourceNode = (ResourceNode) resourceIterator.next();
            if (resourceNode.isMetricsUpdated()) {
                unsortedMap.put(resourceNode.getId(), calculateWorkerResourceMeasurement(resourceNode));
            } else {
                    throw new ResourceManagerException("Metrics needs to be enabled on Resource node: "
                            + resourceNode.getId() + " to be used with Allocation algorithm class: "
                            + ServiceDataHolder.getAllocationAlgorithm().getClass().getCanonicalName());
            }
        }
        Map.Entry<String, Double> node = Collections.min(unsortedMap.entrySet(),
                (e1, e2) -> e1.getValue().compareTo(e2.getValue()));
        if (logger.isDebugEnabled()) {
            logger.debug("Next node to get allocated is " + node.getKey());
        }
        return ServiceDataHolder.getResourcePool().getResourceNodeMap().get(node.getKey());
    }

    private double calculateWorkerResourceMeasurement(ResourceNode resourceNode) {
        double processCPU = resourceNode.getProcessCPU();
        double systemCPU = resourceNode.getSystemCPU();
        return (SYSTEM_CPU_WEIGHT * (1 - systemCPU)) + (PROCESS_CPU_WEIGHT * (1 - processCPU));
    }
}
