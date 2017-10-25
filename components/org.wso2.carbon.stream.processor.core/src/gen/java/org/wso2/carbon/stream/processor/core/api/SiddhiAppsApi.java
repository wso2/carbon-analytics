/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.stream.processor.core.api;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.stream.processor.core.factories.SiddhiAppsApiServiceFactory;
import org.wso2.carbon.stream.processor.core.model.InlineResponse200;
import org.wso2.carbon.stream.processor.core.model.InlineResponse400;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Auto generated class from Swagger to MSF4J.
 */

@Component(
        name = "siddhi-core-services",
        service = Microservice.class,
        immediate = true
)
@Path("/siddhi-apps")
@RequestInterceptor(AuthenticationInterceptor.class)
@io.swagger.annotations.Api(description = "The siddhi-apps API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-05-31T15:43:24.557Z")
public class SiddhiAppsApi implements Microservice {
    private final SiddhiAppsApiService delegate = SiddhiAppsApiServiceFactory.getSiddhiAppsApi();

    @POST
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Saves the Siddhi Application", notes = "Saves the Siddhi " +
            "Application. Request **siddhiApp** explains the Siddhi Query. ", response = InlineResponse400.class,
            tags = {"Artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "The Siddhi Application is successfully " +
                    "validated and saved.", response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "A validation error occured.",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 409, message = "A Siddhi Application with the given name " +
                    "already exists.", response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = InlineResponse400.class)})
    public Response siddhiAppsPost(@ApiParam(value = "Siddhi Application", required = true) String body)
            throws NotFoundException {
        return delegate.siddhiAppsPost(body);
    }

    @PUT
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Updates the Siddhi Application.", notes = "Updates the Siddhi " +
            "Application. Request **siddhiApp** explains the Siddhi Query. ", response = InlineResponse400.class,
            tags = {"Artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The Siddhi Application is successfully " +
                    "validated and updated.", response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 201, message = "The Siddhi Application is successfully " +
                    "validated and saved.", response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "A validation error occured.",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = InlineResponse400.class)})
    public Response siddhiAppsPut(@ApiParam(value = "Siddhi Application", required = true) String body)
            throws NotFoundException {
        return delegate.siddhiAppsPut(body);
    }

    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Lists Siddhi Applications", notes = "Provides the name list of " +
            "Siddhi Applications that exist.", response = InlineResponse200.class, tags = {"Artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The Siddhi Applications are successfully " +
                    "retrieved.", response = InlineResponse200.class)})
    public Response siddhiAppsGet(
            @ApiParam(value = "Retrieves only active/inactive Siddhi Applications as specified.", required = false)
            @QueryParam("isActive") String isActive) throws NotFoundException {
        return delegate.siddhiAppsGet(isActive);
    }

    @DELETE
    @Path("/{appName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Deletes the Siddhi Application.", notes = "Removes the currently" +
            " deployed Siddhi Application with the name specified. Path param of **appName** determines name of the " +
            "Siddhi application. ", response = InlineResponse400.class, tags = {"Artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The Siddhi Application is successfully deleted.",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "The Siddhi Application name provided is " +
                    "invalid.", response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "The Siddhi Application is not found.",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = InlineResponse400.class)})
    public Response siddhiAppsAppNameDelete(@ApiParam(value = "The name of the Siddhi Application", required = true)
                                            @PathParam("appName") String appName) throws NotFoundException {
        return delegate.siddhiAppsAppNameDelete(appName);
    }

    @GET
    @Path("/{appName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retrieves the specified Siddhi Application.", notes = "Retrieves " +
            "the Siddhi Application with the specified name.", response = InlineResponse200.class, tags = {"Artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The Siddhi Application is successfully " +
                    "retrieved.", response = InlineResponse200.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "The Siddhi Application specified is not found.",
                    response = InlineResponse200.class)})
    public Response siddhiAppsAppNameGet(@ApiParam(value = "The name of the Siddhi Application", required = true)
                                         @PathParam("appName") String appName) throws NotFoundException {
        return delegate.siddhiAppsAppNameGet(appName);
    }

    @GET
    @Path("/{appName}/status")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Fetches the status of the Siddhi Application.", notes = "Fetches " +
            "the status of the Siddhi Application.", response = InlineResponse200.class, tags = {"Artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The state of the Siddhi Application is " +
                    "successfully retrieved.", response = InlineResponse200.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "The Siddhi Application specified is not found.",
                    response = InlineResponse200.class)})
    public Response siddhiAppsAppNameStatusGet(@ApiParam(value = "The name of the Siddhi Application.", required = true)
                                               @PathParam("appName") String appName) throws NotFoundException {
        return delegate.siddhiAppsAppNameStatusGet(appName);
    }

    @POST
    @Path("/{appName}/backup")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Backs up the current state of a Siddhi Application.",
            notes = "Backs up the current state of the specified Siddhi Application. ",
            response = InlineResponse400.class, tags = {"State",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The state of the Siddhi Application is " +
                    "successfully persisted.", response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "The Siddhi Application specified is not found.",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = InlineResponse400.class)})
    public Response siddhiAppsAppNameSnapshotPost(@ApiParam(value = "The name of the Siddhi Application.",
            required = true)
                                                  @PathParam("appName") String appName) throws NotFoundException {
        return delegate.siddhiAppsAppNameBackupPost(appName);
    }

    @POST
    @Path("/{appName}/restore")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Restores the state of a Siddhi Application.", notes = "Restores " +
            "the state of the specified Siddhi Application.", response = InlineResponse400.class, tags = {"State",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The Siddhi Application is successfully " +
                    "restored.", response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "The Siddhi Application specified is not found.",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = InlineResponse400.class)})
    public Response siddhiAppsAppNameRestorePost(
            @ApiParam(value = "The name of the Siddhi Application.", required = true)
            @PathParam("appName") String appName,
            @ApiParam(value = "The revision number of the backup.", required = false)
            @QueryParam("revision") String revision) throws NotFoundException {
        return delegate.siddhiAppsAppNameRestorePost(appName, revision);
    }

}
