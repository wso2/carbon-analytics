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
package org.wso2.carbon.database.query.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the database queries.
 */
public class QueryManager {
    private final String DB_QUERIES = "db_queries";
    private final String DB_TYPE = "type";
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryManager.class);
    private String componentNamespace;
    private Map<String,String> queries = null;
    private String databaseType;

    public QueryManager(String componentNamespace) {
        this.queries = readConfigs(componentNamespace);
    }

    private Map<String, String> readConfigs(String componentNamespace) {
        // TODO: Get the relevant type of the database from the config provider.
        try {
            // TODO: 11/9/17 refine the namespace for stream processor
            Map componentConfigs = DataHolder.getInstance().getConfigProvider().getConfigurationMap
                    (componentNamespace);
            if (null != componentConfigs) {
                databaseType = (String) componentConfigs.get("type");
                if (databaseType == null) {
                    throw new RuntimeException("Database type (db_type) has not been specified.");
                }
                if (componentConfigs.containsKey(DB_QUERIES)) {
                    Map dbQueries = (Map<String, String>) componentConfigs.get(DB_QUERIES);
                    return (null != dbQueries) ? dbQueries : new HashMap<>();
                } else {
                    throw new RuntimeException("Unable to find database queries in the deployment.yaml");
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return new HashMap<>();
    }

    public String getQuery(String key) {
        if (!this.queries.containsKey(key)) {
            throw new RuntimeException("Unable to find the configuration entry for the key: " + key);
        }
        return this.queries.get(key);
    }

    public String getDatabaseType() {
        return databaseType;
    }
}

