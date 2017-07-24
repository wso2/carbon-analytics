/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.spark.core.sources;

import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSource;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSourceReader;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper class for data sources.
 */
public class AnalyticsDatasourceWrapper implements Serializable {

    private static final long serialVersionUID = 8861289441809536781L;

    private static transient Map<String, DataSource> dataSources;
    private int tenantId;
    private String dsName;
    private String dsDefinition;

    private AnalyticsDatasourceWrapper() {

    }

    public AnalyticsDatasourceWrapper(int tenantId, String dsName, String dsDefinition) {
        this.tenantId = tenantId;
        this.dsName = dsName;
        this.dsDefinition = dsDefinition;
    }

    /**
     * Produces a connection instance from the stipulated data source definition.
     * If not initialised already, the data source instance is also resolved and maintained for future reference.
     *
     * @return The generated Connection
     * @throws DataSourceException
     * @throws SQLException
     */
    public synchronized Connection getConnection() throws DataSourceException, SQLException {
        if (dataSources == null) {
            dataSources = new ConcurrentHashMap<>();
        }
        String key;
        if (tenantId < 0) {
            key = "X" + Math.abs(tenantId) + "_" + dsName;
        } else {
            key = tenantId + "_" + dsName;
        }
        if (dataSources.containsKey(key)) {
            return dataSources.get(key).getConnection();
        } else {
            DataSource dataSource = this.initializeDataSource();
            dataSources.put(key, dataSource);
            return dataSource.getConnection();
        }
    }

    private DataSource initializeDataSource() throws DataSourceException {
        RDBMSDataSource rdbmsDataSource = new RDBMSDataSource(RDBMSDataSourceReader.loadConfig(this.dsDefinition));
        return rdbmsDataSource.getDataSource();
    }
}
