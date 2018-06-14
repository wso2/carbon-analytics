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

package org.wso2.carbon.sp.jobmanager.core.api;

import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.sp.jobmanager.core.factories.ResourceManagerApiServiceFactory;
import org.wso2.carbon.sp.jobmanager.core.model.Deployment;
import org.wso2.carbon.sp.jobmanager.core.model.HeartbeatResponse;
import org.wso2.carbon.sp.jobmanager.core.model.NodeConfig;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

@Path("/resourceManager")
@RequestInterceptor(AuthenticationInterceptor.class)
@io.swagger.annotations.Api(description = "the resourceManager API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-21T09:39:46.914Z")
public class ResourceManagerApi implements Microservice {
    private final ResourceManagerApiService delegate = ResourceManagerApiServiceFactory.getResourceManagerApi();

    @GET
    @Path("/deployment")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the satus.",
            notes = "Returns status of manager nodes cluster and the resourcepool.",
            response = Deployment.class, tags = {"ResourceManager",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful operation",
                    response = Deployment.class)})
    public Response getDeployment() throws NotFoundException {
        return delegate.getDeployment();
    }

    @POST
    @Path("/heartbeat")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Register resource node and Updates Heartbeat.",
            notes = "Resource nodes will call this endpoint to get them self registerd in "
                    + "the resource pool. Once registered, consecetive calls for this "
                    + "endpoints will be used as heartbeat from tose resource nodes.",
            response = HeartbeatResponse.class, tags = {"ResourceManager",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Resource successfully added / Heartbeat "
                    + "updated.", response = HeartbeatResponse.class),

            @io.swagger.annotations.ApiResponse(code = 301, message = "Not the current leader (redir. to correct "
                    + "leader).", response = HeartbeatResponse.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Leader not found",
                    response = HeartbeatResponse.class)})
    public Response updateHeartbeat(@ApiParam(value = "Node object that needs to be registered or update heartbeats "
            + "as the resource.", required = true) NodeConfig node) throws NotFoundException {
        return delegate.updateHeartbeat(node);
    }
}
