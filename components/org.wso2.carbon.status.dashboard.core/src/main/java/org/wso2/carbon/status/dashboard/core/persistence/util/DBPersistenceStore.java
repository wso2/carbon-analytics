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

package org.wso2.carbon.status.dashboard.core.persistence.util;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Implementation of Persistence Store that would persist snapshots to an RDBMS instance.
 */
public abstract class DBPersistenceStore {

    private static final Logger log = Logger.getLogger(DBPersistenceStore.class);
    private DBPersistenceStore() {   }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected Connection getConnection(DataSourceService service , String datasourceName) {
        Connection connection = null;
        try {
            HikariDataSource dsObject = (HikariDataSource) service.getDataSource(datasourceName);
            connection = dsObject.getConnection();
        } catch (SQLException e) {
            log.error("Error in query " + e.getMessage() + " in " + datasourceName, e);
        } catch (DataSourceException e) {
            log.error("Error in data" + e.getMessage() + " in " + datasourceName, e);
        }
        return connection;
    }

    public abstract boolean loadData(Connection connection, String query);

    public abstract boolean insertData(Connection connection , Map<String, Object> dataMap , String query);

    public abstract boolean updateData(Connection connection , Map<String, Object> dataMap , String query);

    public void cleanupConnections(Statement stmt, Connection connection) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("Unable to close statement." + e.getMessage(), e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Unable to close connection." + e.getMessage(), e);
            }
        }
    }
}
