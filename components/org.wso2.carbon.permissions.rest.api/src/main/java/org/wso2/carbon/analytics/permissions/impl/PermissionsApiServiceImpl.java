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
package org.wso2.carbon.analytics.permissions.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.permissions.api.ApiResponseMessage;
import org.wso2.carbon.analytics.permissions.api.NotFoundException;
import org.wso2.carbon.analytics.permissions.api.PermissionsApiService;
import org.wso2.carbon.analytics.permissions.api.model.Permission;
import org.wso2.carbon.analytics.permissions.bean.PermissionString;
import org.wso2.carbon.analytics.permissions.bean.Role;
import org.wso2.carbon.analytics.permissions.configreader.DataHolder;
import org.wso2.carbon.analytics.permissions.exceptions.PermissionException;
import org.wso2.carbon.analytics.permissions.util.PermissionUtil;

import java.util.List;
import javax.ws.rs.core.Response;

/**
 * PermissionsApi Services Implementation.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-12-07T14:05:52.168Z")
@Component(name = "PermissionsApiServiceImpl", service = PermissionsApiServiceImpl.class, immediate = true)
public class PermissionsApiServiceImpl extends PermissionsApiService {
    private static final Logger LOG = LoggerFactory.getLogger(PermissionsApiServiceImpl.class);

    @Override
    public Response addPermission(Permission body) throws NotFoundException {
        String permissionID = null;
        try {
            org.wso2.carbon.analytics.permissions.bean.Permission permission =
                    new org.wso2.carbon.analytics.permissions.bean.Permission
                            (body.getAppName(), body.getPermissionString());
            permissionID = DataHolder.getInstance().getPermissionProvider().addPermissionAPI(permission);
            return Response.ok().entity(permissionID).build();
        } catch (PermissionException e) {
            String errorMsg = String.format("Failed to add Permission with uuid %s ", permissionID);
            LOG.error(errorMsg, e);
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errorMsg)).build();
        }
    }

    @Override
    public Response deletePermission(String permissionID) throws NotFoundException {
        try {
            DataHolder.getInstance().getPermissionProvider().deletePermission(permissionID);
            String successMsg = String.format("Deleted permission with %s ", permissionID);
            LOG.info(successMsg);
            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK,
                    "Deleted permission with ID: " + permissionID)).build();
        } catch (PermissionException e) {
            String errorMsg = String.format("Failed to delete with uuid %s ", permissionID);
            LOG.error(errorMsg, e);
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errorMsg)).build();
        }
    }

    @Override
    public Response getGrantedRoles(String permissionID) throws NotFoundException {
        try {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            List<Role> roleList = DataHolder.getInstance().getPermissionProvider().getGrantedRoles(permissionID);
            String successMsg = String.format("Retrieving granted roles for %s successful ", permissionID);
            LOG.info(successMsg);
            return Response.ok().entity(gson.toJson(roleList)).build();
        } catch (PermissionException e) {
            String errorMsg = String.format("Failed to retrieve granted roles for %s", permissionID);
            LOG.error(errorMsg, e);
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errorMsg)).build();
        }
    }

    @Override
    public Response getPermissionStrings(String appName) throws NotFoundException {
        try {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            List<PermissionString> permissionStrings =
                    DataHolder.getInstance().getPermissionProvider().getPermissionStrings(appName);
            String successMsg = String.format("Getting permissions for app, %s successful",
                    PermissionUtil.getEncodedString(appName));
            LOG.info(successMsg);
            return Response.ok().entity(gson.toJson(permissionStrings)).build();
        } catch (PermissionException e) {
            String errorMsg = String.format("Failed to retrieve permissions for app name: %s ",
                    PermissionUtil.getEncodedString(appName));
            LOG.error(errorMsg, e);
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errorMsg)).build();
        }
    }

    @Override
    public Response hasPermission(String permissionID, String roleName) throws NotFoundException {
        try {
            Boolean hasPermission =
                    DataHolder.getInstance().getPermissionProvider().hasPermission(roleName, permissionID);
            String successMsg = String.format("Checking permission for app:%s role: %s successful",
                    permissionID, PermissionUtil.getEncodedString(roleName));
            LOG.info(successMsg);
            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, successMsg)).build();
        } catch (PermissionException e) {
            String errorMsg = String.format("Checking permission for app:%s role: %s failed",
                    permissionID, PermissionUtil.getEncodedString(roleName));
            LOG.error(errorMsg, e);
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errorMsg)).build();
        }
    }

    @Override
    public Response manipulateRolePermission(Permission body, String roleID, String action) throws NotFoundException {
        final String revokeAction = "revoke";
        final String grantAction = "grant";
        org.wso2.carbon.analytics.permissions.bean.Permission permission = PermissionUtil.mapPermissionModel(body);
        try {
            switch (action.toLowerCase()) {
                case (revokeAction):
                    DataHolder.getInstance().getPermissionProvider().revokePermission(permission, roleID);
                    break;
                case (grantAction):
                    Role role = new Role();
                    role.setId(roleID);
                    DataHolder.getInstance().getPermissionProvider().grantPermission(permission, role);
                    break;
                default:
                    String errorMsg = String.format("Invalid input. Action should be "
                            + "grant/revoke. But found %s", PermissionUtil.getEncodedString(action));
                    LOG.error(errorMsg);
                    return Response.serverError().
                            entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errorMsg)).build();
            }
            String successMsg = String.format("Action, %s for permission, %s successful.",
                    PermissionUtil.getEncodedString(action),
                    PermissionUtil.getEncodedString(permission.toString()));
            LOG.info(successMsg);
            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, successMsg)).build();
        } catch (PermissionException e) {
            String errorMsg = String.format("Failed to perform action, %s on permission %s",
                    PermissionUtil.getEncodedString(action),
                    PermissionUtil.getEncodedString(permission.toString()));
            LOG.error(errorMsg, e);
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errorMsg)).build();
        }
    }

    @Override
    public Response revokePermission(String permissionID) throws NotFoundException {
        try {
            DataHolder.getInstance().getPermissionProvider().revokePermission(permissionID);
            String successMsg = String.format("Permission revoke for permissionID %s success.", permissionID);
            LOG.info(successMsg);
            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK,
                    successMsg)).build();
        } catch (PermissionException e) {
            String errorMsg = String.format("Permission revoke for permissionID %s failed.", permissionID);
            LOG.error(errorMsg, e);
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errorMsg)).build();
        }
    }

}
