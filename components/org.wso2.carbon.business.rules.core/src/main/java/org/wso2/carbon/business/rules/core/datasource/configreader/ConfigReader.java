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
package org.wso2.carbon.business.rules.core.datasource.configreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.analytics.permissions.bean.Role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read the database queries from deployment.yaml and holds them for later use.
 */
public class ConfigReader {
    private static final String DATASOURCE = "datasource";
    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);
    private static final String USER_NAME = "username";
    private static final String PASSWORD = "password";
    private static final String DEPLOYMENT_CONFIGS = "deployment_configs";
    private static final String COMPONENT_NAMESPACE = "wso2.business.rules.manager";
    private static final String ROLES = "roles";
    private static final String MANAGER = "manager";
    private static final String VIEWER = "viewer";
    private static final String ID = "id";
    private static final String NAME = "name";

    private static final Permission managerPermission = new Permission("BRM", "businessrules.manager");
    private static final Permission viewerPermission = new Permission("BRM", "businessrules.viewer");

    private static Map<String, Object> configs = readConfigs();

    /**
     * Read all the configs under given namespace
     * from deployment.yaml
     * of related runtime
     */
    private static Map<String, Object> readConfigs() {
        try {
            return (Map<String, Object>) DataHolder.getInstance()
                    .getConfigProvider().getConfigurationObject(COMPONENT_NAMESPACE);
        } catch (Exception e) {
            log.error("Failed to read deployment.yaml file . ", e);
        }
        return new HashMap<>();
    }

    public String getUserName() {
        Object username = configs.get(USER_NAME);
        return username != null ? username.toString() : "admin";
    }

    public String getPassword() {
        Object password = configs.get(PASSWORD);
        return password != null ? password.toString() : "admin";
    }

    public String getDatasourceName() {
        return configs.get(DATASOURCE).toString();
    }

    /**
     * Get configurations for each node
     * defined in deployment.yaml
     *
     * @return Map of lists
     */
    public Map getNodes() {
        if (configs != null && configs.get(DEPLOYMENT_CONFIGS) != null) {
            return (Map) ((List) configs.get(DEPLOYMENT_CONFIGS)).get(0);
        }
        return null;
    }

    /**
     * Add roles to the database and grant permissions to roles
     * defined in deployment.yaml
     */
    public void registerRoles() {
        if (configs == null) {
            log.error("Failed to find permission configs for wso2.business.rules.manager in " +
                    "dashboard deployment.yaml");
        }
        Map roles = (Map) configs.get(ROLES);
        List<Map<String, List>> managers = (List<Map<String, List>>) roles.get(MANAGER);
        List<Map<String, List>> viewers = (List<Map<String, List>>) roles.get(VIEWER);

        PermissionProvider permissionProvider = DataHolder.getInstance().getPermissionProvider();

        for (Map manager : managers ) {
            permissionProvider.addPermission(managerPermission);
            Role role = new Role(manager.get(NAME).toString(), manager.get(ID).toString());
            permissionProvider.grantPermission(managerPermission, role);
        }
        for (Map viewer : viewers) {
            permissionProvider.addPermission(viewerPermission);
            Role role = new Role(viewer.get(NAME).toString(), viewer.get(ID).toString());
            permissionProvider.grantPermission(viewerPermission, role);
        }
    }
}
