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

import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.PLACEHOLDER_COLUMNS;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.PLACEHOLDER_COLUMNS_PRIMARYKEY;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.PLACEHOLDER_CONDITION;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.PLACEHOLDER_TABLE_NAME;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.QUESTION_MARK;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.SQL_WHERE;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.String_TEMPLATE;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.TUPLES_SEPARATOR;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.WHITESPACE;


public class StatusDashboardManagerDBHandler {
    private static final Logger logger = LoggerFactory.getLogger(StatusDashboardManagerDBHandler.class);
    private static final String DATASOURCE_ID = ManagerDataHolder.getInstance().getManagerDeploymentConfig()
            .getDashboardManagerDatasourceName();
    private static final String MANAGERID_PLACEHOLDER = "{{MANAGER_ID}}";
    private static final String MANAGERID_EXPRESSION = "MANAGERID={{MANAGER_ID}}";
    private static final String MANAGER_CONFIG_TABLE = "MANAGER_CONFIGURATION";
    private static boolean isConfigTableCreated = false;
    private String createTableQuery;
    private String checkTableQUery;
    private String selectQuery;
    private String deleteQuery;
    private String insertQuery;
    private HikariDataSource dataSource;
    private Map<String, String> managerAttributeMap;
    private QueryManager managerQueryManager;

    public StatusDashboardManagerDBHandler() {
        dataSource = ManagerDataHolder.getInstance().getDashboardManagerDataSource();
        Connection connection = null;
        logger.info("ID" + DATASOURCE_ID);
        if (dataSource != null) {
            try {
                connection = dataSource.getConnection();
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                managerQueryManager = new QueryManager(databaseMetaData.getDatabaseProductName(), databaseMetaData
                        .getDatabaseProductVersion());
                managerAttributeMap = DBTableUtils.getInstance().loadManagerConfigTableTuples(managerQueryManager);
                selectQuery = managerQueryManager.getQuery(SQLConstants.SELECT_QUERY);
                deleteQuery = managerQueryManager.getQuery(SQLConstants.DELETE_QUERY);
                insertQuery = managerQueryManager.getQuery(SQLConstants.INSERT_QUERY);
                checkTableQUery = managerQueryManager.getQuery(SQLConstants.ISTABLE_EXISTS_QUERY);
                createTableQuery = managerQueryManager.getQuery(SQLConstants.CREATE_TABLE);
            } catch (SQLException | QueryMappingNotAvailableException | ConfigurationException ex) {
                throw new StatusDashboardRuntimeException("Error while initializing the connection", ex);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        logger.warn("Database error. Could not close the database connection", e);
                    }
                }
            }
            createConfiguration();
        } else {
            throw new RDBMSTableException(DATASOURCE_ID + "Could not find.Hence cannot initialize the status dashboard "
                                                  + "Please check whether database is available");
        }
    }

    /**
     * Method which can be used to clear up and ephemeral SQL connectivity artifacts.
     *
     * @param conn {@link Connection} instance (can be null)
     */

    public static void cleanupConnection(Connection conn) {
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
     * Function to craete tables
     */
    private void createConfiguration() {
        Connection conn = this.getConnection();
        PreparedStatement statement = null;
        logger.info("creating the tables");
        String resolved = checkTableQUery.replace(PLACEHOLDER_TABLE_NAME, MANAGER_CONFIG_TABLE);
        String resolvedTableCreateQuery = createTableQuery.replace(PLACEHOLDER_TABLE_NAME, MANAGER_CONFIG_TABLE);
        if (!DBHandler.getInstance().isTableExist(conn, resolved)) {
            if (!isConfigTableCreated) {
                Map<String, String> attributeList = DBTableUtils.getInstance().loadManagerConfigTableTuples
                        (managerQueryManager);
                String resolvedTuples = String.format("MANAGERID " + String_TEMPLATE + "PRIMARY KEY" +
                                                              TUPLES_SEPARATOR + "PORT " + String_TEMPLATE
                                                              + TUPLES_SEPARATOR + "HOST" + String_TEMPLATE,
                                                      attributeList.get("MANAGERID"), attributeList.get("PORT"),
                                                      attributeList.get("HOST"));
                resolvedTableCreateQuery = resolvedTableCreateQuery.replace(PLACEHOLDER_COLUMNS_PRIMARYKEY,
                                                                            resolvedTuples);
                try {
                    statement = conn.prepareStatement(resolvedTableCreateQuery);
                    statement.execute();
                    isConfigTableCreated = true;
                } catch (SQLException e) {
                    logger.error("Error while creating table,please try to create it manuaaly" + MANAGER_CONFIG_TABLE,
                                 e);
                } finally {
                    try {
                        if (statement != null) {
                            statement.close();
                        }
                    } catch (SQLException e) {
                        //ignore this
                    }
                    cleanupConnection(conn);
                }
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
     * Resolve the table names in the queries.
     *
     * @param query db queries
     * @return the tableName
     */
    private String resolveTableName(String query, String tableName) {
        return query.replace(PLACEHOLDER_TABLE_NAME, tableName);
    }


    /**
     * Method which is used to insert the manager configuration details to the database.
     *
     * @param managerConfigurationDetails
     * @return
     */
    public boolean insertManagerConfiguration(ManagerConfigurationDetails managerConfigurationDetails) {
        String columnNames = ManagerConfigurationDetails.getColumnLabels();
        Map<String, Object> records = managerConfigurationDetails.toMap();
        try {
            return this.insert(columnNames, records, MANAGER_CONFIG_TABLE);
        } catch (RDBMSTableException e) {
            throw new RDBMSTableException(e.getMessage(), e);
        }
    }


    /**
     * Insert manager data to manager database
     *
     * @param columnNames colomn label need to get
     * @param records     object need to be added
     * @param tableName
     * @return
     */
    private boolean insert(String columnNames, Map<String, Object> records, String tableName) throws
                                                                                              RDBMSTableException {
        String resolvedInsertQuery = resolveTableName(insertQuery, tableName);
        String query = DBTableUtils.getInstance().composeInsertQuery(resolvedInsertQuery.replace(PLACEHOLDER_COLUMNS,
                                                                                                 "(" + columnNames
                                                                                                         + ")"),
                                                                     managerAttributeMap.size());
        Connection conn = this.getConnection();
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(query);
            statement = DBTableUtils.getInstance().populateInsertStatement(records, statement, managerAttributeMap,
                                                                           managerQueryManager);
            DBHandler.getInstance().insert(statement);
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException(e.getMessage(), e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error("error while closing insert query", e);
                }
            }
            cleanupConnection(conn);
        }
    }

    /**
     * Delete manager config details from managerDB
     *
     * @param managerId
     * @return
     */

    public boolean deleteManagerConfiguration(String managerId) {
        return this.delete(managerId, generateConditionManagerID(QUESTION_MARK), MANAGER_CONFIG_TABLE);
    }

    /**
     * Delete ManagerId in the manager DB
     *
     * @param managerId : Id of the manager node
     * @param condition
     * @param tableName
     * @return
     */
    private boolean delete(String managerId, String condition, String tableName) {
        String resolvedDeleteQuery = resolveTableName(deleteQuery, tableName);
        Connection conn = this.getConnection();
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(resolvedDeleteQuery.replace(PLACEHOLDER_CONDITION,
                                                                          SQL_WHERE + WHITESPACE + condition));
            statement.setString(1, managerId);
            DBHandler.getInstance().delete(statement);
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException("Error occured while deleting the manager:" + managerId + "in a "
                                                  + "table" + tableName + DATASOURCE_ID, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                logger.error("Error occured while closing the statement", e);
            }
            cleanupConnection(conn);
        }
    }

    /**
     * To generate the manager ID condition
     *
     * @param managerIdPlaceHolder
     * @return
     */
    private String generateConditionManagerID(String managerIdPlaceHolder) {
        return MANAGERID_EXPRESSION.replace(MANAGERID_PLACEHOLDER, managerIdPlaceHolder);
    }


}
