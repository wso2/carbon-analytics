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
import org.wso2.carbon.stream.processor.core.factories.SiddhiAppsApiServiceFactory;
import org.wso2.carbon.stream.processor.core.model.InlineResponse200;
import org.wso2.carbon.stream.processor.core.model.InlineResponse400;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
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
@io.swagger.annotations.Api(description = "The siddhi-apps API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-05-31T15:43:24.557Z")
public class SiddhiAppsApi implements Microservice {
    private final SiddhiAppsApiService delegate = SiddhiAppsApiServiceFactory.getSiddhiAppsApi();

    @POST
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Saves the Siddhi App", notes = "Saves the Siddhi App. Request " +
            "**siddhiApp** explains the Siddhi Query. ", response = InlineResponse400.class, tags = {"artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully validated and saved",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Validation error",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 409, message = "Siddhi App already exists with the " +
                    "same name",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "Unexpected error occured",
                    response = InlineResponse400.class)})
    public Response siddhiAppsPost(@ApiParam(value = "Siddhi App", required = true) String body)
            throws NotFoundException {
        return delegate.siddhiAppsPost(body);
    }

    @PUT
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Updates the Siddhi App", notes = "Updates the Siddhi App. Request " +
            "**siddhiApp** explains the Siddhi Query. ", response = InlineResponse400.class, tags = {"artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully updated after validation",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully validated and saved",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "validation error",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "Unexpected error occured",
                    response = InlineResponse400.class)})
    public Response siddhiAppsPut(@ApiParam(value = "Siddhi App", required = true) String body)
            throws NotFoundException {
        return delegate.siddhiAppsPut(body);
    }

    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Lists Siddhi Apps", notes = "Provides the name list of " +
            "Siddhi Apps that exist.", response = InlineResponse200.class, tags = {"artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Siddhi apps are retrieved successfully. ",
                    response = InlineResponse200.class)})
    public Response siddhiAppsGet(
            @ApiParam(value = "isActive", required = false)
            @QueryParam("isActive") String isActive) throws NotFoundException {
        return delegate.siddhiAppsGet(isActive);
    }

    @DELETE
    @Path("/{appName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Deletes the Siddhi App", notes = "Deletes the Siddhi App as given " +
            "by `appName`. Path param of **appName** determines name of the Siddhi app. ",
            response = InlineResponse400.class, tags = {"artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully deleted the Siddhi App",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid Siddhi App name found",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Siddhi App not found",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "Unexpected error occured",
                    response = InlineResponse400.class)})
    public Response siddhiAppsAppNameDelete(@ApiParam(value = "Siddhi App Name", required = true)
                                            @PathParam("appName") String appName) throws NotFoundException {
        return delegate.siddhiAppsAppNameDelete(appName);
    }

    @GET
    @Path("/{appName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retrieves the Siddhi App", notes = "Retrieves the Siddhi App.",
            response = InlineResponse200.class, tags = {"artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully retrieved the Siddhi App",
                    response = InlineResponse200.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Siddhi App not found",
                    response = InlineResponse200.class)})
    public Response siddhiAppsAppNameGet(@ApiParam(value = "Siddhi App Name", required = true)
                                         @PathParam("appName") String appName) throws NotFoundException {
        return delegate.siddhiAppsAppNameGet(appName);
    }

    @GET
    @Path("/{appName}/status")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Provides status of the Siddhi App", notes = "Provides the status " +
            "of the Siddhi App.", response = InlineResponse200.class, tags = {"artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Siddhi App status retrieved successfully",
                    response = InlineResponse200.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Siddhi App not found",
                    response = InlineResponse200.class)})
    public Response siddhiAppsAppNameStatusGet(@ApiParam(value = "Siddhi App Name", required = true)
                                         @PathParam("appName") String appName) throws NotFoundException {
        return delegate.siddhiAppsAppNameStatusGet(appName);
    }

    @POST
    @Path("/{appName}/backup")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Backup the current state of a Siddhi App", notes = "Backup the " +
            "current state of the provided Siddhi App. ", response = InlineResponse400.class, tags = {"state",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Siddhi App state persisted successfully",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Siddhi App not found",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "Unexpected error occured",
                    response = InlineResponse400.class)})
    public Response siddhiAppsAppNameSnapshotPost(@ApiParam(value = "Siddhi App", required = true)
                                                      @PathParam("appName") String appName) throws NotFoundException {
        return delegate.siddhiAppsAppNameBackupPost(appName);
    }

    @POST
    @Path("/{appName}/restore")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Restores the state of a Siddhi App ", notes = "Restores the state " +
            "of the provided Siddhi App ", response = InlineResponse400.class, tags = {"state",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Siddhi App state restored successfully",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Siddhi App not found",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "Unexpected error occured",
                    response = InlineResponse400.class)})
    public Response siddhiAppsAppNameRestorePost(
            @ApiParam(value = "Siddhi App Name", required = true) @PathParam("appName") String appName,
            @ApiParam(value = "revision to restore", required = false) @QueryParam("revision") String revision)
            throws NotFoundException {
        return delegate.siddhiAppsAppNameRestorePost(appName, revision);
    }

}
