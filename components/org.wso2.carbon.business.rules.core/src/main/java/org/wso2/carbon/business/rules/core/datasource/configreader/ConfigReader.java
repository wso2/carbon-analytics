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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read the database queries from deployment.yaml and holds them for later use.
 */
public class ConfigReader {
    private static final String DATASOURCE = "datasource";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);
    private static final String USER_NAME = "username";
    private static final String PASSWORD = "password";
    private static final String DEPLOYMENT_CONFIGS = "deployment_configs";
    private static final String COMPONENT_NAMESPACE = "wso2.business.rules.manager";
    private static Map<String, Object> configs = null;

    public ConfigReader() {
        configs = readConfigs();
    }

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
            LOGGER.error("Failed to read deployment.yaml file . ", e);
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

    public static String getDatasourceName() {
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
}
