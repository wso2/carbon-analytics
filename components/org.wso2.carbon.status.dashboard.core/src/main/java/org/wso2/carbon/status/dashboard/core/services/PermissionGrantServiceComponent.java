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
package org.wso2.carbon.status.dashboard.core.services;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.permissions.PermissionManager;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.analytics.permissions.bean.Role;
import org.wso2.carbon.status.dashboard.core.exception.UnauthorizedException;
import org.wso2.carbon.status.dashboard.core.impl.utils.Constants;
import org.wso2.carbon.status.dashboard.core.internal.DashboardDataHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * This is OSGi-components to register config provider class.
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.core.services.PermissionGrantServiceComponent",
        service = PermissionGrantServiceComponent.class,
        immediate = true
)
public class PermissionGrantServiceComponent {
    private static final Logger logger = LoggerFactory.getLogger(PermissionGrantServiceComponent.class);
    private PermissionProvider permissionProvider;
    private IdPClient identityClient;

    public PermissionGrantServiceComponent() {
    }

    @Activate
    protected void start(BundleContext bundleContext) {
        logger.info("Status dashboard permission grant service component is activated.");
        try {
            initPermission();
        } catch (UnauthorizedException e) {
            logger.error("Authorization error.", e);
        } catch (IdPClientException e) {
            logger.error("error in getting admin.", e);
        }
    }

    @Deactivate
    protected void stop() throws Exception {
        logger.info("Status dashboard permission grant service component is deactivated.");
    }

    private void initPermission() throws UnauthorizedException, IdPClientException {
        for (Permission permission : getAllPermission()) {
            if(!permissionProvider.isPermissionExists(permission)) {
                permissionProvider.addPermission(permission);
            }
        }

        List<Role> sysAdminRoles = DashboardDataHolder.getInstance()
                .getRolesProvider().getSysAdminRolesList(identityClient);
        if (!sysAdminRoles.isEmpty()) {
            for (Permission permission : buildDashboardAdminPermissions(Constants.PERMISSION_APP_NAME)) {
                for (org.wso2.carbon.analytics.permissions.bean.Role role : sysAdminRoles) {
                    permissionProvider.grantPermission(permission, role);
                }
            }
        } else {
            for (Permission permission : buildDashboardAdminPermissions(Constants.PERMISSION_APP_NAME)) {
                org.wso2.carbon.analytics.permissions.bean.Role role = new org.wso2.carbon.analytics.permissions.bean
                        .Role(identityClient.getAdminRole().getId(), identityClient.getAdminRole().getDisplayName());
                permissionProvider.grantPermission(permission, role);
            }
        }

        List<org.wso2.carbon.analytics.permissions.bean.Role> devRoles = DashboardDataHolder
                .getInstance().getRolesProvider().getDeveloperRolesList(identityClient);
        if (!devRoles.isEmpty()) {
            for (Permission permission : buildDashboardDevPermissions(Constants.PERMISSION_APP_NAME)) {
                for (org.wso2.carbon.analytics.permissions.bean.Role role : devRoles) {
                    permissionProvider.grantPermission(permission, role);
                }
            }
        }

        List<org.wso2.carbon.analytics.permissions.bean.Role> viwerRoles = DashboardDataHolder
                .getInstance().getRolesProvider().getViewerRolesList(identityClient);
        if (!viwerRoles.isEmpty()) {
            for (Permission permission : buildDashboardViewPermissions(Constants.PERMISSION_APP_NAME)) {
                for (org.wso2.carbon.analytics.permissions.bean.Role role : viwerRoles) {
                    permissionProvider.grantPermission(permission, role);
                }
            }
        }
    }

    /**
     * Build basic dashboard permission string.
     *
     * @return
     */
    private List<Permission> getAllPermission() {
        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(Constants.PERMISSION_APP_NAME, Constants.PERMISSION_APP_NAME +
                Constants.PERMISSION_SUFFIX_METRICS_MANAGER));
        permissions.add(new Permission(Constants.PERMISSION_APP_NAME, Constants.PERMISSION_APP_NAME +
                Constants.PERMISSION_SUFFIX_MANAGER));
        permissions.add(new Permission(Constants.PERMISSION_APP_NAME, Constants.PERMISSION_APP_NAME +
                Constants.PERMISSION_SUFFIX_VIEWER));
        return permissions;
    }

    /**
     * Build basic dashboard permission string.
     *
     * @param permisstionString
     * @return
     */
    private List<Permission> buildDashboardAdminPermissions(String permisstionString) {
        return getAllPermission();
    }

    /**
     * Build basic dashboard permission string.
     *
     * @param permisstionString
     * @return
     */
    private List<Permission> buildDashboardDevPermissions(String permisstionString) {
        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(Constants.PERMISSION_APP_NAME, permisstionString + Constants.PERMISSION_SUFFIX_MANAGER));
        permissions.add(new Permission(Constants.PERMISSION_APP_NAME, permisstionString + Constants.PERMISSION_SUFFIX_VIEWER));
        return permissions;
    }
    /**
     * Build basic dashboard permission string.
     *
     * @param permisstionString
     * @return
     */
    private List<Permission> buildDashboardViewPermissions(String permisstionString) {
        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(Constants.PERMISSION_APP_NAME, permisstionString + Constants.PERMISSION_SUFFIX_VIEWER));
        return permissions;
    }

    @Reference(
            name = "org.wso2.carbon.analytics.idp.client.core.api.IdPClient",
            service = IdPClient.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdP"
    )
    protected void setIdP(IdPClient client) {
        this.identityClient = client;
    }

    protected void unsetIdP(IdPClient client) {
        this.identityClient = null;
    }

    @Reference(
            name = "permission-manager",
            service = PermissionManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPermissionManager"
    )
    protected void setPermissionManager(PermissionManager permissionManager) {
        this.permissionProvider = permissionManager.getProvider();
        DashboardDataHolder.getInstance().setPermissionProvider(this.permissionProvider);
    }

    protected void unsetPermissionManager(PermissionManager permissionManager) {
        this.permissionProvider = null;
        DashboardDataHolder.getInstance().setPermissionProvider(null);
    }

    @Reference(
            name = "org.wso2.carbon.status.dashboard.core.internal.config.loaderServiceComponent",
            service = DefaultQueryLoaderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDefaultQueryLoaderService"
    )
    protected void registerDefaultQueryLoaderService(DefaultQueryLoaderService configServiceComponent) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) DefaultQueryLoaderService");
        }
    }

    protected void unregisterDefaultQueryLoaderService(DefaultQueryLoaderService configServiceComponent) {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) DefaultQueryLoaderService");
        }
    }
}
