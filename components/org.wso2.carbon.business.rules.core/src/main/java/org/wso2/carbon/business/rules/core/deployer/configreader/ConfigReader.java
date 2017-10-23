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
package org.wso2.carbon.business.rules.core.deployer.configreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the database queries.
 */
public class ConfigReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);
    private static final String USER_NAME = "username";
    private static final String PASSWORD = "password";
    private static final String NODES = "nodes";
    public static final String DATASOURCE = "datasource";

    private Map<String, Object> configs = null;

    public ConfigReader(String componentNamespace) {
        this.configs = readConfigs(componentNamespace);
    }

    /**
     * Read all the configs under given namespace
     * from deployment.yaml
     * of related runtime
     * */
    private Map<String, Object> readConfigs(String componentNamespace) {
        try {
            return (Map<String, Object>) DataHolder.getInstance()
                    .getConfigProvider().getConfigurationObject(componentNamespace);
        } catch (Exception e) {
            LOGGER.error("Failed to read deployment.yaml file due to " + e.getMessage(), e);
        }
        return new HashMap<>();
    }

    public String getUserName() {
        return configs.get(USER_NAME).toString();
    }

    public String getPassword() {
        return configs.get(PASSWORD).toString();
    }

    public String getDatasourceName() {
        return configs.get(DATASOURCE).toString();
    }
    /**
     * Get configurations for each node
     * defined in deployment.yaml
     * @return Map of lists
     * */
    public Map getNodes() {
        if (configs != null && configs.get(NODES) != null) {
            return (Map) ((List) configs.get(NODES)).get(0);
        }
        return null;
    }
}
