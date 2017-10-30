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

package org.wso2.carbon.das.jobmanager.core.deployment;

import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.DeploymentManager;
import org.wso2.carbon.das.jobmanager.core.ResourcePoolChangeListener;
import org.wso2.carbon.das.jobmanager.core.SiddhiAppDeployer;
import org.wso2.carbon.das.jobmanager.core.appCreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.das.jobmanager.core.appCreator.DistributedSiddhiQuery;
import org.wso2.carbon.das.jobmanager.core.appCreator.SiddhiQuery;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.das.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.das.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.das.jobmanager.core.model.SiddhiAppHolder;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeploymentManagerImpl implements DeploymentManager, ResourcePoolChangeListener {
    private static final Logger LOG = Logger.getLogger(DeploymentManagerImpl.class);
    private Iterator resourceIterator;

    @Override
    public DeploymentStatus deploy(DistributedSiddhiQuery distributedSiddhiQuery) {
        Map<String, List<SiddhiAppHolder>> siddhiAppHoldersMap = ServiceDataHolder
                .getResourcePool().getSiddhiAppHoldersMap();
        List<SiddhiAppHolder> siddhiAppHolders = new ArrayList<>();
        boolean isDeployed = true;
        if (siddhiAppHoldersMap.containsKey(distributedSiddhiQuery.getAppName())) {
            LOG.warn("Siddhi app " + distributedSiddhiQuery.getAppName() + " already deployed. " +
                    "Hence, un-deploying existing Siddhi app.");
            rollback(siddhiAppHoldersMap.get(distributedSiddhiQuery.getAppName()));
        }
        for (DeployableSiddhiQueryGroup queryGroup : distributedSiddhiQuery.getQueryGroups()) {
            for (SiddhiQuery query : queryGroup.getSiddhiQueries()) {
                ResourceNode node = getNextResourceNode();
                if (node != null) {
                    siddhiAppHolders.add(new SiddhiAppHolder(distributedSiddhiQuery.getAppName(),
                            queryGroup.getGroupName(), query.getAppName(), query.getApp(), node));
                    String appName = SiddhiAppDeployer.deploy(node, query);
                    if (appName == null || appName.isEmpty()) {
                        LOG.error("Couldn't deploy " + distributedSiddhiQuery.getAppName() + ". Hence, rolling back.");
                        isDeployed = false;
                        break;
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Siddhi app %s of %s deployed successfully.", appName,
                                    distributedSiddhiQuery.getAppName()));
                        }
                    }
                } else {
                    LOG.error("Insufficient resources to deploy Siddhi app: " + distributedSiddhiQuery.getAppName());
                    isDeployed = false;
                    break;
                }
            }
        }
        if (isDeployed) {
            siddhiAppHoldersMap.put(distributedSiddhiQuery.getAppName(), siddhiAppHolders);
        } else {
            rollback(siddhiAppHolders);
        }
        return getDeploymentStatus(isDeployed, siddhiAppHolders);
    }

    private DeploymentStatus getDeploymentStatus(boolean isDeployed, List<SiddhiAppHolder> siddhiAppHolders) {
        return new DeploymentStatus(isDeployed, siddhiAppHolders.stream()
                .collect(Collectors.groupingBy(SiddhiAppHolder::getGroupName,
                        Collectors.mapping(o -> o.getDeployedNode().getHttpInterface().getHost(),
                                Collectors.toList()))));
    }

    @Override
    public DeploymentStatus unDeploy(DistributedSiddhiQuery distributedSiddhiQuery) {
        // TODO: 10/30/17 To be implemented
        return null;
    }

    @Override
    public int isDeployed(DistributedSiddhiQuery distributedSiddhiQuery) {
        // TODO: 10/30/17 To be implemented
        return 0;
    }

    /**
     * Get an available {@link ResourceNode} in round robin manner.
     *
     * @return a {@link ResourceNode}
     */
    private ResourceNode getNextResourceNode() {
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        if (resourceIterator == null) {
            resourceIterator = resourcePool.getResourceNodeMap().values().iterator();
        }
        if (resourceIterator.hasNext()) {
            return (ResourceNode) resourceIterator.next();
        } else {
            resourceIterator = resourcePool.getResourceNodeMap().values().iterator();
            if (resourceIterator.hasNext()) {
                return (ResourceNode) resourceIterator.next();
            } else {
                return null;
            }
        }
    }

    @Override
    public void resourceAdded(ResourceNode resourceNode) {
        // Refresh iterator after the pool change (since new node added).
        resourceIterator = ServiceDataHolder.getResourcePool().getResourceNodeMap().values().iterator();
    }

    @Override
    public void resourceRemoved(ResourceNode resourceNode) {
        // Refresh iterator after the pool change (since node get removed).
        resourceIterator = ServiceDataHolder.getResourcePool().getResourceNodeMap().values().iterator();
        // TODO: 10/30/17 Redolence should happen here
        LOG.info("Re-balance ..............................");
    }

    /**
     * Rollback already deployed Siddhi apps.
     *
     * @param siddhiAppHolders list of Siddhi app holders to be un deployed.
     */
    private void rollback(List<SiddhiAppHolder> siddhiAppHolders) {
        // TODO: 10/30/17 To be implemented
    }

    /**
     * This will return ResourceNode -> List of SiddhiAppHolders mapping
     * mapping using the siddhiAppHoldersMap.
     *
     * @return ResourceNode to List of SiddhiAppHolders mapping.
     */
    private Map<ResourceNode, List<SiddhiAppHolder>> getNodeAppMapping() {
        Map<String, List<SiddhiAppHolder>> siddhiAppHoldersMap = ServiceDataHolder
                .getResourcePool().getSiddhiAppHoldersMap();
        return siddhiAppHoldersMap.values().stream().flatMap(List::stream)
                .collect(Collectors.groupingBy(SiddhiAppHolder::getDeployedNode));
    }
}
