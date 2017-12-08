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

import org.wso2.carbon.permissions.rest.api.model.Permission;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-12-07T14:05:52.168Z")
public abstract class PermissionsApiService {
    public abstract Response addPermission(Permission body) throws NotFoundException;

    public abstract Response deletePermission(String permissionID) throws NotFoundException;

    public abstract Response getGrantedRoles(String permissionID) throws NotFoundException;

    public abstract Response getPermissionStrings(String appName) throws NotFoundException;

    public abstract Response hasPermission(String permissionID, String roleName) throws NotFoundException;

    public abstract Response manipulateRolePermission(Permission body, String roleName, String action)
            throws NotFoundException;

    public abstract Response revokePermission(String permissionID) throws NotFoundException;
}
