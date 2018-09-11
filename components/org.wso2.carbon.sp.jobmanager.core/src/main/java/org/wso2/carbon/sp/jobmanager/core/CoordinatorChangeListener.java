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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.sp.jobmanager.core.bean.InterfaceConfig;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.sp.jobmanager.core.util.ResourceManagerConstants;

import java.util.Map;

/**
 * This class contains the implementation related coordinator changes.
 */
public class CoordinatorChangeListener extends MemberEventListener {
    private static final Logger log = LoggerFactory.getLogger(CoordinatorChangeListener.class);

    @Override
    public void memberAdded(NodeDetail nodeDetail) {
        if (ServiceDataHolder.isLeader() && ServiceDataHolder.getResourcePool() != null
                && nodeDetail.getPropertiesMap() != null){
            Map<String, Object> propertiesMap = nodeDetail.getPropertiesMap();
            String nodeId = (String) propertiesMap.get(ResourceManagerConstants.KEY_NODE_ID);
            String httpsInterfaceHost = (String) propertiesMap.get(ResourceManagerConstants.KEY_NODE_HOST);
            Integer httpsInterfacePort = (Integer) propertiesMap.get(ResourceManagerConstants.KEY_NODE_PORT);
            log.info(String.format("ManagerNode { id: %s, host: %s, port: %s } added to the manager cluster" +
                    " of the resource pool.", nodeId, httpsInterfaceHost, httpsInterfacePort));
        }
    }

    @Override
    public void memberRemoved(NodeDetail nodeDetail) {
        if (ServiceDataHolder.isLeader() && ServiceDataHolder.getResourcePool() != null) {
            Map<String, Object> propertiesMap = nodeDetail.getPropertiesMap();
            String nodeId = (String) propertiesMap.get(ResourceManagerConstants.KEY_NODE_ID);
            String httpsInterfaceHost = (String) propertiesMap.get(ResourceManagerConstants.KEY_NODE_HOST);
            Integer httpsInterfacePort = (Integer) propertiesMap.get(ResourceManagerConstants.KEY_NODE_PORT);
            log.info(String.format("ManagerNode { id: %s, host: %s, port: %s } removed from the manager cluster " +
                    "of the resource pool.", nodeId, httpsInterfaceHost, httpsInterfacePort));
        }
    }

    @Override
    public void coordinatorChanged(NodeDetail nodeDetail) {
        Map<String, Object> propertiesMap = nodeDetail.getPropertiesMap();
        String nodeId = (String) propertiesMap.get(ResourceManagerConstants.KEY_NODE_ID);
        Integer heartbeatInterval = (Integer) propertiesMap.get(ResourceManagerConstants.KEY_NODE_INTERVAL);
        Integer heartbeatMaxRetry = (Integer) propertiesMap.get(ResourceManagerConstants.KEY_NODE_MAX_RETRY);
        String httpsInterfaceHost = (String) propertiesMap.get(ResourceManagerConstants.KEY_NODE_HOST);
        Integer httpsInterfacePort = (Integer) propertiesMap.get(ResourceManagerConstants.KEY_NODE_PORT);
        String httpsInterfaceUsername = (String) propertiesMap.get(ResourceManagerConstants.KEY_NODE_USERNAME);
        String httpsInterfacePassword = (String) propertiesMap.get(ResourceManagerConstants.KEY_NODE_PASSWORD);
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setHost(httpsInterfaceHost);
        interfaceConfig.setPort(httpsInterfacePort);
        interfaceConfig.setUsername(httpsInterfaceUsername);
        interfaceConfig.setPassword(httpsInterfacePassword);
        ManagerNode leader = new ManagerNode().setId(nodeId)
                .setHeartbeatInterval(heartbeatInterval)
                .setHeartbeatMaxRetry(heartbeatMaxRetry)
                .setHttpsInterface(interfaceConfig);

        ServiceDataHolder.isLeader(ServiceDataHolder.getCoordinator().isLeaderNode());
        ServiceDataHolder.setLeaderNode(leader);
        if (ServiceDataHolder.isLeader()) {
            // Get last known state of the resource pool from database and restore it.
            String groupId = ServiceDataHolder.getClusterConfig().getGroupId();
            ResourcePool existingResourcePool = ServiceDataHolder.getRdbmsService().getResourcePool(groupId);
            ServiceDataHolder.setResourcePool((existingResourcePool != null) ? existingResourcePool
                    : new ResourcePool(groupId));
            ServiceDataHolder.getResourcePool().init();
            log.info(leader + " became the leader of the resource pool.");
            // if clustering is disabled leader node and resource pool is set when worker heart beat is
            // processed at updateHeartbeat in ResourceManagerApiServiceImpl
        } else {
            log.info(ServiceDataHolder.getLeaderNode() + " became the leader of the resource pool.");
        }
    }
}
