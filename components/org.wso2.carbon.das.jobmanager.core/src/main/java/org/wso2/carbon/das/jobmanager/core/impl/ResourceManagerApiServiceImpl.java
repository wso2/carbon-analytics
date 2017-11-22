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

package org.wso2.carbon.das.jobmanager.core.impl;

import org.apache.log4j.Logger;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.das.jobmanager.core.api.ApiResponseMessage;
import org.wso2.carbon.das.jobmanager.core.api.NotFoundException;
import org.wso2.carbon.das.jobmanager.core.api.ResourceManagerApiService;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.das.jobmanager.core.model.HeartbeatResponse;
import org.wso2.carbon.das.jobmanager.core.model.InterfaceConfig;
import org.wso2.carbon.das.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.das.jobmanager.core.model.ManagerNodeConfig;
import org.wso2.carbon.das.jobmanager.core.model.NodeConfig;
import org.wso2.carbon.das.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.das.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.das.jobmanager.core.util.ResourceManagerConstants;
import org.wso2.carbon.das.jobmanager.core.util.TypeConverter;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

public class ResourceManagerApiServiceImpl extends ResourceManagerApiService {
    private static final Logger LOG = Logger.getLogger(ResourceManagerApiServiceImpl.class);

    @Override
    public Response getDeployment() throws NotFoundException {
        // TODO: 10/31/17 To be implemented.
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "To be implement.")).build();
    }

    @Override
    public Response updateHeartbeat(NodeConfig nodeConfig) throws NotFoundException {
        if (ServiceDataHolder.isLeader()) {
            ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
            List<InterfaceConfig> connectedManagers = new ArrayList<>();
            for (NodeDetail nodeDetail : ServiceDataHolder.getCoordinator().getAllNodeDetails()) {
                if (nodeDetail.getPropertiesMap() != null) {
                    ManagerNode member = (ManagerNode) nodeDetail.getPropertiesMap()
                            .get(ResourceManagerConstants.KEY_NODE_INFO);
                    connectedManagers.add(TypeConverter.convert(member.getHttpInterface()));
                }
            }
            ResourceNode existingResourceNode = resourcePool.getResourceNodeMap().get(nodeConfig.getId());
            HeartbeatResponse.JoinedStateEnum joinedState = (existingResourceNode == null)
                    ? HeartbeatResponse.JoinedStateEnum.NEW
                    : HeartbeatResponse.JoinedStateEnum.EXISTS;
            ManagerNodeConfig leader = TypeConverter.convert(resourcePool.getLeaderNode());
            if (existingResourceNode == null) {
                ResourceNode resourceNode = new ResourceNode(nodeConfig.getId());
                resourceNode.setState(HeartbeatResponse.JoinedStateEnum.EXISTS.toString());
                resourceNode.setHttpInterface(TypeConverter.convert(nodeConfig.getHttpInterface()));
                resourcePool.addResourceNode(resourceNode);
            } else {
                InterfaceConfig existingIFace = TypeConverter.convert(existingResourceNode.getHttpInterface());
                InterfaceConfig currentIFace = nodeConfig.getHttpInterface();
                if (currentIFace.equals(existingIFace)) {
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
                    resourcePool.notifyResourceNode(nodeConfig.getId(), redeploy);
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
