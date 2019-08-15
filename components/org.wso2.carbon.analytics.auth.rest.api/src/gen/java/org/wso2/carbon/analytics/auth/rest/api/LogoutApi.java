/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.auth.rest.api;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.analytics.auth.rest.api.dto.ErrorDTO;
import org.wso2.carbon.analytics.auth.rest.api.factories.LogoutApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Logout API class.
 */
@Component(
        name = "LogoutApi",
        service = Microservice.class,
        immediate = true
)
@Path("/logout")
@Consumes({"application/json"})
@Produces({"application/json"})
@ApplicationPath("/logout")
@io.swagger.annotations.Api(description = "the logout API")
public class LogoutApi implements Microservice {
    private final LogoutApiService delegate = LogoutApiServiceFactory.getLogoutApi();

    @POST
    @Path("/{appName:(.*)}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Logout Request to Streaming Integrator",
            response = void.class, tags = {})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Authorization Request Successful.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response logoutAppNamePost(@ApiParam(value = "AppName", required = true) @PathParam("appName") String appName
            , @Context Request request)
            throws NotFoundException {
        return delegate.logoutAppNamePost(appName, request);
    }

    @GET
    @Path("/slo/{appName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Login type check Request to Streaming Integrator.",
            response = HashMap.class, tags = {})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "SSO logout Request to Stream processor",
                    response = HashMap.class),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Invalid Authorization Header",
                    response = ErrorDTO.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occurred.",
                    response = ErrorDTO.class)})
    public Response singleLogoutAppNamePost(@ApiParam(value = "AppName", required = true) @PathParam("appName")
                                                String appName, @Context Request request) throws NotFoundException {
        return delegate.ssoLogout(appName, request);
    }
}
