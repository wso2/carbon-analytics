/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stream.processor.core.persistence.util;

import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBPersistenceStoreUtils {
    private static final Logger log = Logger.getLogger(DBPersistenceStoreUtils.class);

    /**
     * Method that would create the persistence table
     */
    public static void createTableIfNotExist(ExecutionInfo executionInfo, DataSource dataSource, String dataSourceName,
                                       String tableName) {
        if (!executionInfo.isTableExist()) {
            Statement stmt = null;
            Connection con = null;
            try {
                try {
                    con = dataSource.getConnection();
                    con.setAutoCommit(false);
                    stmt = con.createStatement();
                } catch (SQLException e) {
                    log.error("Cannot establish connection to datasource " + dataSourceName +
                            " when checking persistence table exists", e);
                    return;
                }
                try (ResultSet ignored = stmt.executeQuery(executionInfo.getPreparedTableExistenceCheckStatement())) {
                    executionInfo.setTableExist(true);
                } catch (SQLException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Table " + tableName + " does not Exist. Table Will be created. ");
                    }
                    cleanupConnections(stmt, con);
                    try {
                        con = dataSource.getConnection();
                        stmt = con.createStatement();
                        con.setAutoCommit(false);
                        stmt.executeUpdate(executionInfo.getPreparedCreateTableStatement());
                        con.commit();
                        executionInfo.setTableExist(true);
                    } catch (SQLException ex) {
                        log.error("Could not create table " + tableName +
                                " using datasource " + dataSourceName, ex);
                    }
                }
            } finally {
                cleanupConnections(stmt, con);
            }
        }
    }

    public static void cleanupConnections(Statement stmt, Connection connection) {
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
