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
import org.wso2.carbon.das.jobmanager.core.model.NodeConfig;
import org.wso2.carbon.das.jobmanager.core.util.TypeConverter;

import java.util.ArrayList;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-21T09:39:46.914Z")
public class ResourceManagerApiServiceImpl extends ResourceManagerApiService {
    private static final Logger LOG = Logger.getLogger(ResourceManagerApiServiceImpl.class);

    @Override
    public Response getDeployment() throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response updateHeartbeat(NodeConfig node) throws NotFoundException {
        // TODO: 10/19/17 Update heartbeat logic should go here
        // TODO: 10/23/17 How to get Managers
        // TODO: 10/23/17 How to get Node state
        // TODO: 10/23/17 How to get Leader
        // TODO: 10/23/17 Update DB once new node is connected
        if (ServiceDataHolder.isIsLeader()) {
            String groupId = ServiceDataHolder.getClusterConfig().getGroupId();

            ServiceDataHolder.getResourceMapping().getHeartbeatMap().put(node.getId(), new Heartbeat(node.getId()));
            // ServiceDataHolder.getRdbmsService().updateResourceMapping(groupId, resourcePool);
            LOG.info("Resource joined: " + node);
            return Response
                    .ok()
                    .entity(new HeartbeatResponse()
                            .connectedManagers(new ArrayList<>())
                            .joinedState(HeartbeatResponse.JoinedStateEnum.NEW)
                            .leader(TypeConverter.convert(ServiceDataHolder.getLeaderNode())))
                    .build();
        } else {
            return Response
                    .status(Response.Status.MOVED_PERMANENTLY)
                    .entity(ServiceDataHolder.getLeaderNode())
                    .build();
        }
    }
}
