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
import org.wso2.carbon.status.dashboard.core.dbhandler.DeploymentConfigs;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.InputStream;

/**
 * Class use for loading the deafault values from YML file.
 */
public class DefaultConfigurationBuilder {

    private static final Logger log = LoggerFactory.getLogger(DefaultConfigurationBuilder.class);

    private static DefaultConfigurationBuilder instance = new DefaultConfigurationBuilder();

    public static DefaultConfigurationBuilder getInstance() {
        return instance;
    }

    private DefaultConfigurationBuilder() {
    }

    /**
     * Get the Environment {@code DashboardsConfiguration}
     * <p>
     * Location of the configuration file should be defined in the environment variable 'dashboard.status.conf'.
     * If environment variable is not specified, return the default configuration
     *
     * @return DashboardsConfiguration defined in the environment
     */
    public DeploymentConfigs getConfiguration() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(SQLConstants.DASHBOARD_CONFIG_FILE);
        DeploymentConfigs dashboardConfiguration;
        if (inputStream != null) {
            Yaml yaml = new Yaml(new CustomClassLoaderConstructor
                    (DeploymentConfigs.class, DeploymentConfigs.class.getClassLoader()));
            yaml.setBeanAccess(BeanAccess.FIELD);
            dashboardConfiguration = yaml.loadAs(inputStream, DeploymentConfigs.class);
        } else {
            throw new RuntimeException("Dashboard configuration file not found in: " +
                    " ,hence using default configuration");
        }
        return dashboardConfiguration;
    }
}
