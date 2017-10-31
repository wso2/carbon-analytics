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
import org.wso2.carbon.das.jobmanager.core.api.ApiResponseMessage;
import org.wso2.carbon.das.jobmanager.core.api.NotFoundException;
import org.wso2.carbon.das.jobmanager.core.api.ResourceManagerApiService;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.das.jobmanager.core.model.Heartbeat;
import org.wso2.carbon.das.jobmanager.core.model.HeartbeatResponse;
import org.wso2.carbon.das.jobmanager.core.model.InterfaceConfig;
import org.wso2.carbon.das.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.das.jobmanager.core.model.ManagerNodeConfig;
import org.wso2.carbon.das.jobmanager.core.model.NodeConfig;
import org.wso2.carbon.das.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.das.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.das.jobmanager.core.util.ResourceManagerConstants;
import org.wso2.carbon.das.jobmanager.core.util.TypeConverter;

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

public class ResourceManagerApiServiceImpl extends ResourceManagerApiService {
    private static final Logger LOG = Logger.getLogger(ResourceManagerApiServiceImpl.class);

    @Override
    public Response getDeployment() throws NotFoundException {
        // TODO: 10/31/17 To be implemented.
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "To be implement.")).build();
    }

    @Override
    public Response updateHeartbeat(NodeConfig node) throws NotFoundException {
        if (ServiceDataHolder.isLeader()) {
            ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
            List<InterfaceConfig> connectedManagers = ServiceDataHolder.getCoordinator().getAllNodeDetails()
                    .stream().map(nodeDetail -> {
                        ManagerNode member = (ManagerNode) nodeDetail.getPropertiesMap()
                                .get(ResourceManagerConstants.KEY_NODE_INFO);
                        return TypeConverter.convert(member.getHttpInterface());
                    }).collect(Collectors.toList());
            Heartbeat prevHeartbeat = resourcePool.getHeartbeatMonitor().updateHeartbeat(new Heartbeat(node.getId()));
            HeartbeatResponse.JoinedStateEnum joinedState = (prevHeartbeat == null)
                    ? HeartbeatResponse.JoinedStateEnum.NEW
                    : HeartbeatResponse.JoinedStateEnum.EXISTS;
            ManagerNodeConfig leader = TypeConverter.convert(resourcePool.getLeaderNode());
            if (prevHeartbeat == null) {
                ResourceNode resourceNode = new ResourceNode();
                resourceNode.setId(node.getId());
                resourceNode.setState(HeartbeatResponse.JoinedStateEnum.EXISTS.toString());
                resourceNode.setHttpInterface(TypeConverter.convert(node.getHttpInterface()));
                resourcePool.addResourceNode(resourceNode);
            } else {
                ResourceNode existingNode = resourcePool.getResourceNodeMap().get(node.getId());
                if (existingNode != null) {
                    InterfaceConfig existingIFace = TypeConverter.convert(resourcePool.getResourceNodeMap()
                            .get(node.getId()).getHttpInterface());
                    InterfaceConfig currentIFace = node.getHttpInterface();
                    if (!currentIFace.equals(existingIFace)) {
                        // If existing node and the current node have the same nodeId, but different interfaces,
                        // Then reject new node from joining the resource pool.
                        joinedState = HeartbeatResponse.JoinedStateEnum.REJECTED;
                    } else if (ResourceManagerConstants.STATE_NEW.equalsIgnoreCase(existingNode.getState())) {
                        joinedState = HeartbeatResponse.JoinedStateEnum.NEW;
                    } else if (ResourceManagerConstants.STATE_EXISTS.equalsIgnoreCase(existingNode.getState())
                            && ResourceManagerConstants.STATE_NEW.equalsIgnoreCase(node.getState().toString())) {
                        // TODO: 10/25/17 Handle it and  send priviously deployed artefacts if there's any
                    }
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
