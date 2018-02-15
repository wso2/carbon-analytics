/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.jobmanager.core.dbhandler;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.wso2.carbon.sp.jobmanager.core.bean.ManagerConfigurationDetails;
import org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.DBTableUtils;
import org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants;
import org.wso2.carbon.sp.jobmanager.core.exception.RDBMSTableException;
import org.wso2.carbon.sp.jobmanager.core.exception.StatusDashboardRuntimeException;
import org.wso2.carbon.sp.jobmanager.core.internal.ManagerDataHolder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;


/**
 * This class represents key database operations related to manager data.
 */

public class StatusDashboardManagerDBHandler {
    private static final Logger logger = LoggerFactory.getLogger(StatusDashboardManagerDBHandler.class);
    private static final String DATASOURCE_ID = ManagerDataHolder.getInstance().getManagerDeploymentConfig()
            .getDashboardManagerDatasourceName();
    private static final String MANAGER_CONFIG_TABLE = "MANAGER_CONFIGURATION";
    private boolean isConfigTableCreated = false;
    private String createTableQuery;
    private String checkTableQuery;
    private String deleteQuery;
    private String insertQuery;
    private HikariDataSource dataSource;
    private Map<String, String> managerAttributeMap;
    private QueryManager managerQueryManager;

    public StatusDashboardManagerDBHandler() {
        dataSource = ManagerDataHolder.getInstance().getDashboardManagerDataSource();
        Connection connection = null;
        if (dataSource != null) {
            try {
                connection = dataSource.getConnection();
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                managerQueryManager = new QueryManager(databaseMetaData.getDatabaseProductName(), databaseMetaData
                        .getDatabaseProductVersion());
                managerAttributeMap = DBTableUtils.getInstance().loadManagerConfigTableTuples(managerQueryManager);
                deleteQuery = managerQueryManager.getQuery(SQLConstants.DELETE_QUERY);
                insertQuery = managerQueryManager.getQuery(SQLConstants.INSERT_QUERY);
                checkTableQuery = managerQueryManager.getQuery(SQLConstants.ISTABLE_EXISTS_QUERY);
                createTableQuery = managerQueryManager.getQuery(SQLConstants.CREATE_TABLE);
            } catch (SQLException | QueryMappingNotAvailableException | ConfigurationException ex) {
                throw new StatusDashboardRuntimeException("Error while initializing the connection ", ex);
            } finally {
                cleanupConnection(connection);
            }
            createTables();
        } else {
            throw new RDBMSTableException(DATASOURCE_ID + " Could not find.Hence cannot initialize the status "
                                                  + "dashboard "
                                                  + "Please check whether database is available");
        }
    }

    /**
     * Method which can be used to clear up and ephemeral SQL connectivity artifacts.
     *
     * @param conn {@link Connection} instance (can be null)
     */

    private void cleanupConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                if (logger.isDebugEnabled()) {
                    logger.debug("Closed Connection in Manager DB");
                }
            } catch (SQLException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error while closing the connection in manager DB");
                }
            }
        }
    }

    /**
     * Method which can be used to close the prepared statement.
     */

    private void closePreparedStatement(PreparedStatement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            logger.debug("Error occurred while closing the statement " + e.getMessage(), e);
        }
    }

    /**
     * Function to create tables
     */
    private void createTables() {
        Connection conn = this.getConnection();
        PreparedStatement statement = null;
        if (!isConfigTableCreated && (!DBHandler.getInstance().isTableExist(conn, checkTableQuery))) {
            try {
                statement = conn.prepareStatement(createTableQuery);
                statement.execute();
                isConfigTableCreated = true;
            } catch (SQLException e) {
                logger.error("Error while creating table,please try to create it manually " + MANAGER_CONFIG_TABLE, e);
            } finally {
                closePreparedStatement(statement);
                cleanupConnection(conn);
            }
        }
    }

    /**
     * Returns a connection instance.
     *
     * @return a new {@link Connection} instance from the datasource.
     */
    private Connection getConnection() {
        return DBHandler.getInstance().getConnection(dataSource);
    }


    /**
     * Method which is used to insert the manager configuration details to the database.
     *
     * @param managerConfigurationDetails
     * @return
     */
    public boolean insertManagerConfiguration(ManagerConfigurationDetails managerConfigurationDetails) {
        Map<String, Object> records = managerConfigurationDetails.toMap();
        Connection conn = this.getConnection();
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(insertQuery);
            statement = DBTableUtils.getInstance().populateInsertStatement(records, statement, managerAttributeMap,
                                                                           managerQueryManager);
            DBHandler.getInstance().insert(statement);
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException("Error while inserting the data " + e.getMessage(), e);
        } finally {
            closePreparedStatement(statement);
            cleanupConnection(conn);
        }
    }

    /**
     * Delete ManagerId in the manager DB
     *
     * @param managerId : Id of the manager node
     * @return
     */
    public boolean deleteManagerConfiguration(String managerId) {
        Connection conn = this.getConnection();
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(deleteQuery);
            statement.setString(1, managerId);
            DBHandler.getInstance().delete(statement);
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException(
                    "Error occurred while deleting the manager: " + "\n" + managerId + "\n" + " in"
                            + " a "
                            + "table MANAGER_CONFIGURATION " + "\n" + DATASOURCE_ID, e);
        } finally {
            closePreparedStatement(statement);
            cleanupConnection(conn);
        }
    }
}
