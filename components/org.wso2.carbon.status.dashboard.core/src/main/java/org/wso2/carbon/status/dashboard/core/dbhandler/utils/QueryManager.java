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
package org.wso2.carbon.status.dashboard.core.dbhandler.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.status.dashboard.core.bean.SpDashboardConfiguration;
import org.wso2.carbon.status.dashboard.core.internal.DashboardDataHolder;

import java.util.Map;

/**
 * Holds the database queries.
 */
public class QueryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryManager.class);
    private static QueryManager instance = new QueryManager();
    private Map<String, String> typeMapping;
    private Map<String, String> queries;

    private QueryManager() {
    }

    public static QueryManager getInstance() {
        return instance;
    }

    public void readConfigs(String dbType) {
        ConfigProvider configProvider = DashboardDataHolder.getInstance().getConfigProvider();
        SpDashboardConfiguration dashboardConfigurations = null;
        try {
            dashboardConfigurations = configProvider
                    .getConfigurationObject(SpDashboardConfiguration.class);
        } catch (ConfigurationException e) {
            LOGGER.error("Error reading configurations ", e);
            //todo add proper exception handling
        }
        // TODO: 11/3/17 handle properly
        if (dashboardConfigurations != null && dashboardConfigurations.getQueries() != null
                && !dashboardConfigurations.getQueries().containsKey(dbType)) {
            //todo: improve exception message
            LOGGER.warn("Unable to find the database type: " + dbType + " hence proceed with default queries");
        }
        this.queries = dashboardConfigurations.getQueries().get(dbType);
        this.typeMapping = dashboardConfigurations.getTypeMapping();
    }

    public String getQuery(String key) {
        if (!this.queries.containsKey(key)) {
            LOGGER.warn("Unable to find the configuration entry for the key: " + key + "Hence proceed with default " +
                    "values.");
        }
        return this.queries.get(key);
    }

    public String getTypeMap(String key) {
        if (!this.typeMapping.containsKey(key)) {
            LOGGER.warn("Unable to find the configuration entry for the key: " + key + "Hence proceed with default " +
                    "values.");
        }
        return this.typeMapping.get(key);
    }
}

