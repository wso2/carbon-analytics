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

import org.wso2.status.dashboard.api.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.das.jobmanager.core.dto.SiddhiAppDTO;
import org.wso2.carbon.das.jobmanager.core.dto.SiddhiAppListDTO;
import org.wso2.carbon.das.jobmanager.core.dto.WorkerDTO;
import org.wso2.carbon.das.jobmanager.core.dto.WorkerListDTO;
import org.wso2.carbon.das.jobmanager.core.factories.WorkersApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Component(
        name = "WorkersApi",
        service = Microservice.class,
        immediate = true
)
@Path("/api/das/jobmanager/v1/workers")
@Consumes({"application/json"})
@Produces({"application/json"})
@org.wso2.status.dashboard.api.annotations.Api(description = "the workers API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2017-03-14T10:36:41.439+05:30")
public class WorkersApi implements Microservice {
    private final WorkersApiService delegate = WorkersApiServiceFactory.getWorkersApi();

    @GET

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @org.wso2.status.dashboard.api.annotations.ApiOperation(value = "Retrieving Workers ", notes = "Get a list of registered workers. ",
            response = WorkerListDTO.class, tags = {"Retrieve",})
    @org.wso2.status.dashboard.api.annotations.ApiResponses(value = {
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 200, message = "OK. List of registered workers is returned. ",
                    response = WorkerListDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is " +
                    "not supported ", response = WorkerListDTO.class)})
    public Response workersGet(@ApiParam(value = "Media types acceptable for the response. Default is JSON. ",
            defaultValue = "JSON") @HeaderParam("Accept") String accept
    )
            throws NotFoundException {
        return delegate.workersGet(accept);
    }

    @DELETE
    @Path("/{id}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @org.wso2.status.dashboard.api.annotations.ApiOperation(value = "", notes = "Deregister a Worker ", response = void.class, tags =
            {"Delete",})
    @org.wso2.status.dashboard.api.annotations.ApiResponses(value = {
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response
                    = void.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not " +
                    "exist. ", response = void.class)})
    public Response workersIdDelete(@ApiParam(value = "**ID** consisting of the **UUID** of the Worker. ", required =
            true) @PathParam("id") String id
    )
            throws NotFoundException {
        return delegate.workersIdDelete(id);
    }

    @GET
    @Path("/{id}/siddhiApps")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @org.wso2.status.dashboard.api.annotations.ApiOperation(value = "", notes = "Get a list of execution plans deployed in a Worker. ",
            response = SiddhiAppListDTO.class, tags = {"Retrieve Siddhi Apps",})
    @org.wso2.status.dashboard.api.annotations.ApiResponses(value = {
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 200, message = "OK. Siddhi App list is returned. ",
                    response = SiddhiAppListDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 404, message = "Not Found. Requested Worker does not exist. ",
                    response = SiddhiAppListDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is " +
                    "not supported ", response = SiddhiAppListDTO.class)})
    public Response workersIdSiddhiAppsGet(@ApiParam(value = "**ID** consisting of the **UUID** of the Worker. ",
            required = true) @PathParam("id") String id
            , @ApiParam(value = "Media types acceptable for the response. Default is JSON. ", defaultValue = "JSON")
                                                   @HeaderParam("Accept") String accept
    )
            throws NotFoundException {
        return delegate.workersIdSiddhiAppsGet(id, accept);
    }

    @POST
    @Path("/{id}/siddhiApps")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @org.wso2.status.dashboard.api.annotations.ApiOperation(value = "", notes = "Add a new execution plan to a Worker. ", response =
            SiddhiAppDTO.class, tags = {"Register",})
    @org.wso2.status.dashboard.api.annotations.ApiResponses(value = {
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 201, message = "Registered. Successful response with the newly" +
                    " created object as entity in the body. Location header contains URL of newly created entity. ",
                    response = SiddhiAppDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation " +
                    "error ", response = SiddhiAppDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 409, message = "Conflict. Worker already exists. ", response =
                    SiddhiAppDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the " +
                    "request was in a not supported format. ", response = SiddhiAppDTO.class)})
    public Response workersIdSiddhiAppsPost(@ApiParam(value = "**ID** consisting of the **UUID** of the Worker. " +
            "", required = true) @PathParam("id") String id
            , @ApiParam(value = "Siddhi App object that is to be created. ", required = true) SiddhiAppDTO body
            , @ApiParam(value = "Media type of the entity in the body. Default is JSON. ", required = true,
            defaultValue = "JSON") @HeaderParam("Content-Type") String contentType
    )
            throws NotFoundException {
        return delegate.workersIdSiddhiAppsPost(id, body, contentType);
    }

    @GET
    @Path("/{id}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @org.wso2.status.dashboard.api.annotations.ApiOperation(value = "", notes = "Get details of a Worker ", response = WorkerDTO.class,
            tags = {"Retrieve",})
    @org.wso2.status.dashboard.api.annotations.ApiResponses(value = {
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 200, message = "OK. Requested Worker is returned ", response =
                    WorkerDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 404, message = "Not Found. Requested Worker does not exist. ",
                    response = WorkerDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is " +
                    "not supported ", response = WorkerDTO.class)})
    public Response workersIdGet(@ApiParam(value = "**ID** consisting of the **UUID** of the Worker. ", required =
            true) @PathParam("id") String id
            , @ApiParam(value = "Media types acceptable for the response. Default is JSON. ", defaultValue = "JSON")
                                     @HeaderParam("Accept") String accept
    )
            throws NotFoundException {
        return delegate.workersIdGet(id, accept);
    }

    @POST

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @org.wso2.status.dashboard.api.annotations.ApiOperation(value = "", notes = "Register a new Worker. ", response = WorkerDTO.class,
            tags = {"Register",})
    @org.wso2.status.dashboard.api.annotations.ApiResponses(value = {
            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 201, message = "Registered. Successful response with the newly" +
                    " created object as entity in the body. Location header contains URL of newly created entity. ",
                    response = WorkerDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation " +
                    "error ", response = WorkerDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 409, message = "Conflict. Worker already exists. ", response =
                    WorkerDTO.class),

            @org.wso2.status.dashboard.api.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the " +
                    "request was in a not supported format. ", response = WorkerDTO.class)})
    public Response workersPost(@ApiParam(value = "Worker object that is to be created. ", required = true) WorkerDTO
                                            body
            , @ApiParam(value = "Media type of the entity in the body. Default is JSON. ", required = true,
            defaultValue = "JSON") @HeaderParam("Content-Type") String contentType
    )
            throws NotFoundException {
        return delegate.workersPost(body, contentType);
    }
}
