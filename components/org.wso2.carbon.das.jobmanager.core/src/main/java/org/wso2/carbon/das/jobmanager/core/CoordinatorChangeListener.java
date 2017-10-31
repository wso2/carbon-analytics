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

package org.wso2.carbon.das.jobmanager.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.das.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.das.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.das.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.das.jobmanager.core.util.ResourceManagerConstants;

import java.util.concurrent.TimeUnit;

public class CoordinatorChangeListener extends MemberEventListener {
    private static final Logger log = LoggerFactory.getLogger(CoordinatorChangeListener.class);

    @Override
    public void memberAdded(NodeDetail nodeDetail) {
        if (ServiceDataHolder.isLeader() && ServiceDataHolder.getResourcePool() != null) {
            ManagerNode member = (ManagerNode) nodeDetail.getPropertiesMap()
                    .get(ResourceManagerConstants.KEY_NODE_INFO);
            if (log.isDebugEnabled()) {
                log.debug(member + " added to the cluster.");
            }
        }
    }

    @Override
    public void memberRemoved(NodeDetail nodeDetail) {
        if (ServiceDataHolder.isLeader() && ServiceDataHolder.getResourcePool() != null) {
            ManagerNode member = (ManagerNode) nodeDetail.getPropertiesMap()
                    .get(ResourceManagerConstants.KEY_NODE_INFO);
            if (log.isDebugEnabled()) {
                log.debug(member + " removed from the cluster.");
            }
        }
    }

    @Override
    public void coordinatorChanged(NodeDetail nodeDetail) {
        ManagerNode leader = (ManagerNode) nodeDetail.getPropertiesMap()
                .get(ResourceManagerConstants.KEY_NODE_INFO);
        ServiceDataHolder.setLeader(ServiceDataHolder.getCoordinator().isLeaderNode());
        ServiceDataHolder.setLeaderNode(leader);
        if (ServiceDataHolder.isLeader()) {
            // Get last known state of the resource pool from database and restore it.
            String groupId = ServiceDataHolder.getClusterConfig().getGroupId();
            ResourcePool existingResourcePool = ServiceDataHolder.getRdbmsService().getResourcePool(groupId);
            ServiceDataHolder.setResourcePool((existingResourcePool != null) ? existingResourcePool
                    : new ResourcePool(groupId));
            ServiceDataHolder.getResourcePool().setLeaderNode(leader);
            ServiceDataHolder.getResourcePool().init();
            ServiceDataHolder.getResourcePool()
                    .registerResourcePoolChangeListener(ServiceDataHolder.getDeploymentManager());
            DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
            ServiceDataHolder.getExecutorService().scheduleAtFixedRate(
                    ServiceDataHolder.getResourcePool().getHeartbeatMonitor(), deploymentConfig.getHeartbeatInterval(),
                    deploymentConfig.getHeartbeatInterval(), TimeUnit.MILLISECONDS);
            log.info("Became the leader node in distributed mode.");
        } else {
            log.info("Leader changed to : " + ServiceDataHolder.getLeaderNode());
        }
    }
}
