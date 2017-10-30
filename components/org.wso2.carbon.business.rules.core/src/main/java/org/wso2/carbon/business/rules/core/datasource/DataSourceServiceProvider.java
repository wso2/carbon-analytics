/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.business.rules.core.datasource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.deployer.configreader.ConfigReader;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRulesDatasourceException;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRulesDatasourceInitializationException;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Data Source Service Provider class
 */
public class DataSourceServiceProvider {
    private static DataSourceServiceProvider dataSourceServiceProvider = new DataSourceServiceProvider();
    private HikariDataSource dataSource;
    private Connection conn;
    private Logger log = LoggerFactory.getLogger(DataSourceServiceProvider.class);

    private DataSourceServiceProvider() {
        initDataSource();
        conn = initConnection();
    }

    static DataSourceServiceProvider getInstance() {
        return dataSourceServiceProvider;
    }

    private void initDataSource() {
        BundleContext bundleContext = FrameworkUtil.getBundle(DataSourceService.class).getBundleContext();
        ServiceReference serviceRef = bundleContext.getServiceReference(DataSourceService.class.getName());

        ConfigReader configReader = new ConfigReader("business.rules");
        String datasourceName = configReader.getDatasourceName();
        if (serviceRef == null) {
            throw new BusinessRulesDatasourceInitializationException("Datasource '" + datasourceName +
                    "' service cannot be found.");
        }
        DataSourceService dataSourceService = (DataSourceService) bundleContext.getService(serviceRef);

        try {
            dataSource = (HikariDataSource) dataSourceService.getDataSource(datasourceName);
        } catch (DataSourceException e) {
            throw new BusinessRulesDatasourceInitializationException("Datasource '" + datasourceName +
                    "' cannot be connected.", e);
        }
    }

    private Connection initConnection() {
        Connection conn;
        try {
            conn = this.dataSource.getConnection();
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceInitializationException("Error initializing connection. ", e);
        }
        return conn;
    }

    DataSource getDataSource() {
        return this.dataSource;
    }

    public Connection getConnection() {
        return this.conn;
    }
}
