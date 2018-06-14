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

package org.wso2.carbon.sp.jobmanager.core.impl;

import org.apache.log4j.Logger;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.sp.jobmanager.core.api.ApiResponseMessage;
import org.wso2.carbon.sp.jobmanager.core.api.ResourceManagerApiService;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.HeartbeatResponse;
import org.wso2.carbon.sp.jobmanager.core.model.InterfaceConfig;
import org.wso2.carbon.sp.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.sp.jobmanager.core.model.ManagerNodeConfig;
import org.wso2.carbon.sp.jobmanager.core.model.NodeConfig;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.sp.jobmanager.core.util.ResourceManagerConstants;
import org.wso2.carbon.sp.jobmanager.core.util.TypeConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * API implementation for resource manager.
 */
public class ResourceManagerApiServiceImpl extends ResourceManagerApiService {
    private static final Logger LOG = Logger.getLogger(ResourceManagerApiServiceImpl.class);

    @Override
    public Response getDeployment() {
        // TODO: 10/31/17 To be implemented.
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "To be implement.")).build();
    }

    @Override
    public Response updateHeartbeat(NodeConfig nodeConfig) {
        boolean isReceiverNode = nodeConfig.isReceiverNode();
        if (ServiceDataHolder.getCoordinator() == null) { // When clustering is disabled
            ManagerNode leaderNode = ServiceDataHolder.getLeaderNode();
            if (leaderNode == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No leader node is set because clustering is disabled. Setting current node as leader");
                }
                ServiceDataHolder.isLeader(true);
                ServiceDataHolder.setLeaderNode(ServiceDataHolder.getCurrentNode());
                // Get last known state of the resource pool from database and restore it.
                String groupId = ServiceDataHolder.getClusterConfig().getGroupId();
                ResourcePool existingResourcePool = ServiceDataHolder.getRdbmsService().getResourcePool(groupId);
                ServiceDataHolder.setResourcePool((existingResourcePool != null) ? existingResourcePool
                        : new ResourcePool(groupId));
                ServiceDataHolder.getResourcePool().init();
                LOG.info(ServiceDataHolder.getCurrentNode() + " is the leader of the resource pool.");
            }
        } else if (ServiceDataHolder.getLeaderNode() == null) { // Cluster has not already notified who the leader is
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .entity(new HeartbeatResponse()
                            .connectedManagers(null)
                            .joinedState(null)
                            .leader(null))
                    .build();
        }
        if (ServiceDataHolder.isLeader()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Leader node received heartbeat from " + nodeConfig.getId());
            }
            ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
            List<InterfaceConfig> connectedManagers = new ArrayList<>();
            ClusterCoordinator clusterCoordinator = ServiceDataHolder.getCoordinator();
            if (clusterCoordinator != null) {
                for (NodeDetail nodeDetail : clusterCoordinator.getAllNodeDetails()) {
                    if (nodeDetail.getPropertiesMap() != null) {
                        Map<String, Object> propertiesMap = nodeDetail.getPropertiesMap();
                        String httpsInterfaceHost = (String) propertiesMap.get(ResourceManagerConstants.KEY_NODE_HOST);
                        int httpsInterfacePort = (int) propertiesMap.get(ResourceManagerConstants.KEY_NODE_PORT);
                        String httpsInterfaceUsername = (String) propertiesMap.get(
                                ResourceManagerConstants.KEY_NODE_USERNAME);
                        String httpsInterfacePassword = (String) propertiesMap.get(
                                ResourceManagerConstants.KEY_NODE_PASSWORD);
                        InterfaceConfig interfaceConfig = new InterfaceConfig();
                        interfaceConfig.setHost(httpsInterfaceHost);
                        interfaceConfig.setPort(httpsInterfacePort);
                        interfaceConfig.setUsername(httpsInterfaceUsername);
                        interfaceConfig.setPassword(httpsInterfacePassword);
                        connectedManagers.add(interfaceConfig);
                    }
                }
            } else {
                connectedManagers.add(TypeConverter.convert(ServiceDataHolder.getCurrentNode().getHttpsInterface()));
            }
            ResourceNode existingResourceNode;
            if (isReceiverNode) {
                existingResourceNode = resourcePool.getReceiverNodeMap().get(nodeConfig.getId());
            } else {
                existingResourceNode = resourcePool.getResourceNodeMap().get(nodeConfig.getId());
            }
            HeartbeatResponse.JoinedStateEnum joinedState = (existingResourceNode == null)
                    ? HeartbeatResponse.JoinedStateEnum.NEW
                    : HeartbeatResponse.JoinedStateEnum.EXISTS;
            ManagerNodeConfig leader = TypeConverter.convert(resourcePool.getLeaderNode());
            if (existingResourceNode == null) {
                ResourceNode resourceNode = new ResourceNode(nodeConfig.getId());
                resourceNode.setState(HeartbeatResponse.JoinedStateEnum.EXISTS.toString());
                resourceNode.setHttpsInterface(TypeConverter.convert(nodeConfig.getHttpsInterface()));
                if (nodeConfig.getWorkerMetrics() != null) {
                    resourceNode.updateResourceMetrics(nodeConfig.getWorkerMetrics());
                }
                if (isReceiverNode) {
                    resourceNode.setReceiverNode(true);
                    resourcePool.addReceiverNode(resourceNode);
                } else {
                    resourcePool.addResourceNode(resourceNode);
                }
            } else {
                InterfaceConfig existingIFace = TypeConverter.convert(existingResourceNode.getHttpsInterface());
                InterfaceConfig currentIFace = nodeConfig.getHttpsInterface();
                if (currentIFace.equals(existingIFace)) {
                    if (nodeConfig.getWorkerMetrics() != null) {
                        existingResourceNode.updateResourceMetrics(nodeConfig.getWorkerMetrics());
                    }
                    existingResourceNode.updateLastPingTimestamp();
                    boolean redeploy = false;
                    if (ResourceManagerConstants.STATE_NEW.equalsIgnoreCase(existingResourceNode.getState())) {
                        joinedState = HeartbeatResponse.JoinedStateEnum.NEW;
                    } else {
                        // Existing state is STATE_EXISTS. then;
                        if (ResourceManagerConstants.STATE_NEW.equalsIgnoreCase(nodeConfig.getState().toString())) {
                            // This block will hit when resource node goes down and comes up back again within the
                            // heartbeat check time interval of the manager node.
                            joinedState = HeartbeatResponse.JoinedStateEnum.EXISTS;
                            // Here, we need to redeploy apps
                            redeploy = true;
                        } else {
                            joinedState = HeartbeatResponse.JoinedStateEnum.EXISTS;
                        }
                    }
                    resourcePool.notifyResourceNode(nodeConfig.getId(), redeploy, isReceiverNode);
                } else {
                    // If existing node and the current node have the same nodeId, but different interfaces,
                    // Then reject new node from joining the resource pool.
                    joinedState = HeartbeatResponse.JoinedStateEnum.REJECTED;
                }
            }
            return Response
                    .ok()
                    .entity(new HeartbeatResponse()
                            .connectedManagers(connectedManagers)
                            .joinedState(joinedState)
                            .leader(leader))
                    .build();
        } else {
            return Response
                    .status(Response.Status.MOVED_PERMANENTLY)
                    .entity(new HeartbeatResponse()
                            .connectedManagers(null)
                            .joinedState(null)
                            .leader(TypeConverter.convert(ServiceDataHolder.getLeaderNode())))
                    .build();
        }
    }
}
