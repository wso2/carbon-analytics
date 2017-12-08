/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.permissions.api;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.analytics.permissions.api.model.Permission;
import org.wso2.carbon.analytics.permissions.factories.PermissionsApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;
@Component(
        name = "PermissionsAPI",
        service = Microservice.class,
        immediate = true
)
@Path("/permissions")
@Consumes({"application/json", "application/xml"})
@Produces({"application/xml", "application/json"})
@io.swagger.annotations.Api(description = "the permissions API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-12-07T14:05:52.168Z")
public class PermissionsApi implements Microservice {
    private final PermissionsApiService delegate = PermissionsApiServiceFactory.getPermissionsApi();

    @POST

    @Consumes({"application/json"})
    @Produces({"application/xml", "application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add a new permission and returns its PermissionID",
            response = String.class, tags = {"permission",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Permission was added successfully",
                    response = String.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Adding Permission failed",
                    response = String.class)})
    public Response addPermission(@ApiParam(value = "Permission json object", required = true) Permission body
    ) throws NotFoundException {
        return delegate.addPermission(body);
    }

    @DELETE
    @Path("/{permissionID}")
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/xml", "application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Deleting an existing permission", response = void.class,
            tags = {"permission",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Permission deleted successfully",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Deleting permission unsuccessful",
                    response = void.class)})
    public Response deletePermission(@ApiParam(value = "", required = true)
                                         @PathParam("permissionID") String permissionID) throws NotFoundException {
        return delegate.deletePermission(permissionID);
    }

    @GET
    @Path("/{permissionID}/roles")
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/xml", "application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get granted roles for PermissionID.",
            response = void.class, tags = {"permission",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Roles listing successful",
                    response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Role listing unsuccessful",
                    response = void.class)})
    public Response getGrantedRoles(@ApiParam(value = "", required = true)
                                        @PathParam("permissionID") String permissionID) throws NotFoundException {
        return delegate.getGrantedRoles(permissionID);
    }

    @GET
    @Path("/app/{appName}")
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get app permissions", response = void.class,
            tags = {"permission",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Permission deleted successfully",
                    response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Deleting permission unsuccessful",
                    response = void.class)})
    public Response getPermissionStrings(@ApiParam(value = "", required = true) @PathParam("appName") String appName)
            throws NotFoundException {
        return delegate.getPermissionStrings(appName);
    }

    @POST
    @Path("/{permissionID}}/{roleName}")
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/xml", "application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Check permission for a specific role.",
            response = void.class, tags = {"permission",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Check permission for a specific role successful",
                    response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Check permission for a specific role "
                    + "unsuccessful", response = void.class)})
    public Response hasPermission(@ApiParam(value = "", required = true) @PathParam("permissionID") String permissionID
            , @ApiParam(value = "", required = true) @PathParam("roleName") String roleName
    )
            throws NotFoundException {
        return delegate.hasPermission(permissionID, roleName);
    }

    @POST
    @Path("/roles/{roleName}")
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/xml", "application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Grant/Revoke permission from a specific role.",
            response = void.class, tags = {"permission",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Permission revoked successfully",
                    response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Revoking permission unsuccessful",
                    response = void.class)})
    public Response manipulateRolePermission(
            @ApiParam(value = "Permission json object", required = true) Permission body
            , @ApiParam(value = "", required = true) @PathParam("roleName") String roleName
            , @ApiParam(value = "", required = true) @QueryParam("action") String action
    )
            throws NotFoundException {
        return delegate.manipulateRolePermission(body, roleName, action);
    }

    @POST
    @Path("/{permissionID}/revoke")
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/xml", "application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Revoke permission for all the roles.",
            response = void.class, tags = {"permission",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Permission for all roles revoke successful",
                    response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Deleting permission unsuccessful",
                    response = void.class)})
    public Response revokePermission(@ApiParam(value = "", required = true)
                                         @PathParam("permissionID") String permissionID
    )
            throws NotFoundException {
        return delegate.revokePermission(permissionID);
    }
}
