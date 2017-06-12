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

    @DELETE
    @Path("/{appName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Undeploys the Siddhi App as given by `appName`. " +
            "Path param of **appName** determines name of the Siddhi app ", response = InlineResponse400.class,
            tags = {"artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful response",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid appName supplied",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "appName not found",
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
    @io.swagger.annotations.ApiOperation(value = "Provides the Siddhi App", notes = "Provides the Siddhi App.",
            response = InlineResponse200.class, tags = {"artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation",
                    response = InlineResponse200.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "appName not found",
                    response = InlineResponse200.class)})
    public Response siddhiAppsAppNameGet(@ApiParam(value = "Siddhi App Name", required = true)
                                         @PathParam("appName") String appName) throws NotFoundException {
        return delegate.siddhiAppsAppNameGet(appName);
    }


    @POST
    @Path("/{appName}/restore")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Restore the State for the provided Siddhi App ",
            response = InlineResponse400.class, tags = {"state",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful response",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid appName supplied",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "appName not found",
                    response = InlineResponse400.class)})
    public Response siddhiAppsAppNameRestorePost(
            @ApiParam(value = "Siddhi App Name", required = true) @PathParam("appName") String appName,
            @ApiParam(value = "revison to restore", required = true) @QueryParam("revison") String revision)
            throws NotFoundException {
        return delegate.siddhiAppsAppNameRestorePost(appName, revision);
    }

    @POST
    @Path("/{appName}/snapshot")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Persist the State for the provided Siddhi App ",
            response = InlineResponse400.class, tags = {"state",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful response",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid appName supplied",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "appName not found",
                    response = InlineResponse400.class)})
    public Response siddhiAppsAppNameSnapshotPost(@ApiParam(value = "Siddhi App", required = true)
                                                      @PathParam("appName") String appName) throws NotFoundException {
        return delegate.siddhiAppsAppNameSnapshotPost(appName);
    }

    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Lists Siddhi Apps", notes = "Provides list of " +
            "Siddhi Apps that active.", response = InlineResponse200.class, tags = {"artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation",
                    response = InlineResponse200.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error",
                    response = InlineResponse200.class)})
    public Response siddhiAppsGet() throws NotFoundException {
        return delegate.siddhiAppsGet();
    }

    @POST
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Deploys the Siddhi App. Request " +
            "**siddhiApp** explains the Siddhi Query ", response = InlineResponse400.class, tags = {"artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful response",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error",
                    response = InlineResponse400.class),
            @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict, Siddhi App already exists",
                    response = InlineResponse400.class)})
    public Response siddhiAppsPost(@ApiParam(value = "Siddhi App", required = true) String body)
            throws NotFoundException {
        return delegate.siddhiAppsPost(body);
    }
}
