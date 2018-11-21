/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.sp.jobmanager.core.api;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.analytics.msf4j.interceptor.common.util.InterceptorConstants;
import org.wso2.carbon.sp.jobmanager.core.factories.ManagersApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Auto generated class from Swagger to MSF4J.
 */

@Component(
        service = Microservice.class,
        immediate = true
)

@Path("/managers")

@RequestInterceptor(AuthenticationInterceptor.class)
@io.swagger.annotations.Api(description = "the managers API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2018-01-29T08:19:07.148Z")
public class ManagersApi implements Microservice {
    private static final Log logger = LogFactory.getLog(ManagersApi.class);
    private final ManagersApiService managersApi = ManagersApiServiceFactory.getManagersApi();

    private static String getUsername(Request request) {
        return request.getProperty(InterceptorConstants.PROPERTY_USERNAME).toString();
    }

    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "List all the registered manager nodes.",
            notes = "List all the managers that are connected with the give manager node"
                    + " (HA mode).",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getAllManagers(@Context Request request) throws NotFoundException {
        return managersApi.getAllManagers(request);
    }

    @GET
    @Path("/siddhi-apps")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the summary details of all siddhi apps of a given manager.",
            notes = "Retrieves the siddhi app summary details of manager with the "
                    + "specified id.",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Summary successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Summary not found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getSiddhiApps(@Context Request request) throws NotFoundException {
        return managersApi.getSiddhiApps(request);
    }

    @GET
    @Path("/siddhi-apps-execution/{appName}")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the siddhi app exection plan of particular siddhi app",
            notes = "Retrieves the text view of the siddhi application.",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Summary successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Summary not found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getSiddhiAppTextView(
            @Context Request request,
            @ApiParam(value = "name of the parent siddhi app.", required = true)
            @PathParam("appName") String appName) throws NotFoundException {
        return managersApi.getSiddhiAppTextView(appName, request);
    }

    @GET
    @Path("/siddhi-apps/{appName}")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the details of child siddhi apps",
            notes = "Retrieves the details of child siddhi apps with the specified "
                    + "parent siddhi app name.",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Child app details fully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The parent application is not found.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpexted error occured.",
                    response = void.class)})
    public Response getChildSiddhiAppDetails(
            @Context Request request,
            @ApiParam(value = "name of the parent siddhi app.", required = true)
            @PathParam("appName") String appName) throws NotFoundException {
        return managersApi.getChildSiddhiAppDetails(appName, request);
    }

    /**
     * Get user sysAdminRoles by username.
     *
     * @param username
     * @return User sysAdminRoles.
     * @throws NotFoundException
     */
    @GET
    @Path("/roles")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get user roles of a specified user", notes = "Lists sysAdminRoles " +
            "of a given user.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getRolesByUsername(@Context Request request,
                                       @ApiParam(value = "Id of the worker.", required = true)
                                       @QueryParam("permissionSuffix") String permissionSuffix)
            throws NotFoundException {
        return managersApi.getRolesByUsername(request, permissionSuffix);
    }

    @GET
    @Path("/kafkaDetails/{appName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the kafka details of each child siddhi apps",
            notes = "Retrieves kafka details of child siddhi apps with the specified "
                    + "parent siddhi app name.",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Child app details fully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The parent application is not found.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpexted error occured.",
                    response = void.class)})
    public Response getKafkaDetails(
            @Context Request request,
            @ApiParam(value = "name of the parent siddhi app.", required = true)
            @PathParam("appName") String appName) throws NotFoundException {
        return managersApi.getKafkaDetails(appName, request);
    }

    @GET
    @Path("/isActive")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Check whether the current manager node is the active/leader node ",
            notes = "" + ".", response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Current node is not the active node",
                    response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response isActive(@Context Request request)
            throws NotFoundException {
        return managersApi.isActive(request);
    }
}
