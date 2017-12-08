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
package org.wso2.carbon.permissions.rest.api;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.permissions.rest.api.factories.PermissionsApiServiceFactory;
import org.wso2.carbon.permissions.rest.api.model.Permission;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

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
    private static final Logger log = LoggerFactory.getLogger(PermissionsApi.class);
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
    ) throws org.wso2.carbon.permissions.rest.api.NotFoundException {
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
                                     @PathParam("permissionID") String permissionID) throws org.wso2.carbon.permissions.rest.api.NotFoundException {
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
                                    @PathParam("permissionID") String permissionID) throws org.wso2.carbon.permissions.rest.api.NotFoundException {
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
            throws org.wso2.carbon.permissions.rest.api.NotFoundException {
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
            throws org.wso2.carbon.permissions.rest.api.NotFoundException {
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
            throws org.wso2.carbon.permissions.rest.api.NotFoundException {
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
            throws org.wso2.carbon.permissions.rest.api.NotFoundException {
        return delegate.revokePermission(permissionID);
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Permissions API service component is activated");
        }
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Permissions API service component is deactivated");
    }
}
