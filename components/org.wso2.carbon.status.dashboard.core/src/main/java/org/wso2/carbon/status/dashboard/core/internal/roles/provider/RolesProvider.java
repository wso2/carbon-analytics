/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.status.dashboard.core.internal.roles.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.permissions.bean.Role;
import org.wso2.carbon.status.dashboard.core.dbhandler.DeploymentConfigs;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the bean class for default sysAdminRoles in deployment yaml.
 */
public class RolesProvider {
    private static final Logger log = LoggerFactory.getLogger(RolesProvider.class);
    private List<Role> sysAdminRolesList = null;
    private List<Role> developerRolesList = null;
    private List<Role> viewerRolesList = null;
    private DeploymentConfigs dashboardConfigurations;

    public RolesProvider(DeploymentConfigs dashboardConfigurations) {
        this.dashboardConfigurations = dashboardConfigurations;

    }

    /**
     *
     * @param client
     * @return
     */
    public List<Role> getSysAdminRolesList(IdPClient client) {
        if (sysAdminRolesList == null) {
            sysAdminRolesList = new ArrayList<>();
            if (dashboardConfigurations.getSysAdminRoles() != null) {
                readSysAdminConfigs(client);
            }
        }
        return sysAdminRolesList;
    }

    /**
     *
     * @param client
     * @return
     */
    public List<Role> getDeveloperRolesList(IdPClient client) {
        if (developerRolesList == null) {
            developerRolesList = new ArrayList<>();
            if (!dashboardConfigurations.getDeveloperRoles().isEmpty()) {
                nonAdminConfigs(client);
            }
        }
        return developerRolesList;
    }

    /**
     *
     * @param client
     * @return
     */
    public List<Role> getViewerRolesList(IdPClient client) {
        if (viewerRolesList == null) {
            viewerRolesList = new ArrayList<>();
            if (!dashboardConfigurations.getViewerRoles().isEmpty()) {
                nonAdminConfigs(client);
            }
        }
        return viewerRolesList;
    }

    /**
     *
     * @param client
     */
    private void readSysAdminConfigs(IdPClient client) {
        if (!dashboardConfigurations.getSysAdminRoles().isEmpty()) {
            sysAdminRolesList = new ArrayList<>();
            sysAdminRolesList = populateRoles(dashboardConfigurations.getSysAdminRoles(), client);
        } else {
            //by default give permission to admin
            try {
                org.wso2.carbon.analytics.idp.client.core.models.Role role = client.getAdminRole();
                sysAdminRolesList.add(new Role(role.getId(), role.getDisplayName()));
            } catch (IdPClientException e) {
                log.error("Error retrieving roles from idp client  admin roles.", e);
            }
        }
    }

    /**
     *
     * @param client
     */
    private void nonAdminConfigs(IdPClient client) {
        if (!dashboardConfigurations.getDeveloperRoles().isEmpty()) {
            developerRolesList = new ArrayList<>();
            developerRolesList = populateRoles(dashboardConfigurations.getDeveloperRoles(), client);
        }
        if (!dashboardConfigurations.getViewerRoles().isEmpty()) {
            viewerRolesList = new ArrayList<>();
            viewerRolesList = populateRoles(dashboardConfigurations.getViewerRoles(), client);
        }
    }

    private List<Role> populateRoles(List<String> displayNamesList, IdPClient client) {
        List<Role> roleList = new ArrayList<>();
        try {
            List<org.wso2.carbon.analytics.idp.client.core.models.Role> roles = client.getAllRoles();
            roles.forEach(idpRole -> {
                        if (displayNamesList.contains(idpRole.getDisplayName())) {
                            Role role = new Role(idpRole.getId(), idpRole.getDisplayName());
                            roleList.add(role);
                        }
                    }
            );
            if (displayNamesList.size() > roles.size()) {
                log.error("Please define role under 'auth.config' first.");
            }
        } catch (IdPClientException e) {
            log.error("Error retrieving roles from idp client.", e);
        }
        return roleList;
    }


}
