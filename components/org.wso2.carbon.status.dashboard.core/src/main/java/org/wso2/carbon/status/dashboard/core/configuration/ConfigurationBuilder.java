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
package org.wso2.carbon.status.dashboard.core.configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.status.dashboard.core.bean.SpDashboardConfiguration;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.InputStream;

/**
 *Class use for loading the deafault values from YML file.
 */
public class ConfigurationBuilder {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationBuilder.class);

    private static ConfigurationBuilder instance = new ConfigurationBuilder();

    public static ConfigurationBuilder getInstance() {
        return instance;
    }

    private ConfigurationBuilder() {
    }

    /**
     * Get the Environment {@code DashboardsConfiguration}
     * <p>
     * Location of the configuration file should be defined in the environment variable 'dashboard.status.conf'.
     * If environment variable is not specified, return the default configuration
     *
     * @return DashboardsConfiguration defined in the environment
     */
    public SpDashboardConfiguration getConfiguration() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(SQLConstants.RDBMS_QUERY_CONFIG_FILE);
        return getConfiguration(inputStream);
    }

    /**
     * Get the {@code DashboardsConfiguration} represented by a particular configuration file.
     *
     * @return DashboardsConfiguration represented by a particular configuration file
     */
    private SpDashboardConfiguration getConfiguration(InputStream inputStream) {
        SpDashboardConfiguration dashboardConfiguration;
        if (inputStream != null) {
            Yaml yaml = new Yaml(new CustomClassLoaderConstructor
                    (SpDashboardConfiguration.class, SpDashboardConfiguration.class.getClassLoader()));
            yaml.setBeanAccess(BeanAccess.FIELD);
            dashboardConfiguration = yaml.loadAs(inputStream, SpDashboardConfiguration.class);
        } else {
            throw new RuntimeException("Dashboard configuration file not found in: " +
                    " ,hence using default configuration");
        }
        return dashboardConfiguration;
    }
}
