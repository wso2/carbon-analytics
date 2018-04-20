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
package org.wso2.carbon.status.dashboard.core.dbhandler;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.wso2.carbon.status.dashboard.core.bean.NodeConfigurationDetails;
import org.wso2.carbon.status.dashboard.core.bean.WorkerGeneralDetails;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.DBTableUtils;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants;
import org.wso2.carbon.status.dashboard.core.exception.RDBMSTableException;
import org.wso2.carbon.status.dashboard.core.exception.StatusDashboardRuntimeException;
import org.wso2.carbon.status.dashboard.core.exception.StatusDashboardValidationException;
import org.wso2.carbon.status.dashboard.core.impl.utils.Constants;
import org.wso2.carbon.status.dashboard.core.internal.MonitoringDataHolder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_COLUMNS;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_COLUMNS_PRIMARYKEY;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_CONDITION;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_TABLE_NAME;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.QUESTION_MARK;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.SQL_WHERE;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.STRING_TEMPLATE;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.TUPLES_SEPARATOR;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.WHITESPACE;

/**
 * This class represents key database operations related to node data.
 */

public class StatusDashboardDBHandler {
    private static final Logger logger = LoggerFactory.getLogger(StatusDashboardDBHandler.class);
    private static final String DATASOURCE_ID = MonitoringDataHolder.getInstance().getStatusDashboardDeploymentConfigs()
            .getDashboardDatasourceName();
    private String createTableQuery;
    private String tableCheckQuery;
    private String selectQuery;
    private String deleteQuery;
    private String insertQuery;
    private HikariDataSource dataSource;
    private Map<String, Map<String, String>> workerAttributeTypeMap;
    private static final String WORKERID_PLACEHOLDER = "{{WORKER_ID}}";
    private static final String MANAGERID_PLACEHOLDER = "{{MANAGER_ID}}";
    private static final String MANAGERID_EXPRESSION = "MANAGERID={{MANAGER_ID}}";
    private static final String WORKERID_EXPRESSION = "WORKERID={{WORKER_ID}}";
    private static final String WORKER_DETAILS_TABLE = "WORKERS_DETAILS";
    private static final String WORKER_CONFIG_TABLE = "WORKERS_CONFIGURATION";
    private static final String MANAGER_CONFIG_TABLE = "MANAGER_CONFIGURATION";
    private static boolean isConfigTableCreated = false;
    private static boolean isGeneralTableCreated = false;
    private QueryManager statusDashboardQueryManager;

    public StatusDashboardDBHandler() {
        dataSource = MonitoringDataHolder.getInstance().getDashboardDataSource();
        Connection conn = null;
        if (dataSource != null) {
            conn = DBHandler.getInstance().getConnection(dataSource);
            try {
                conn = MonitoringDataHolder.getInstance().getDashboardDataSource().getConnection();
                DatabaseMetaData databaseMetaData = conn.getMetaData();
                statusDashboardQueryManager = new QueryManager(databaseMetaData.getDatabaseProductName(),
                        databaseMetaData.getDatabaseProductVersion());
                workerAttributeTypeMap = DBTableUtils.getInstance().
                        loadWorkerAttributeTypeMap(statusDashboardQueryManager);
                selectQuery = statusDashboardQueryManager.getQuery(SQLConstants.SELECT_QUERY);
                deleteQuery = statusDashboardQueryManager.getQuery(SQLConstants.DELETE_QUERY);
                insertQuery = statusDashboardQueryManager.getQuery(SQLConstants.INSERT_QUERY);
                tableCheckQuery = statusDashboardQueryManager.getQuery(SQLConstants.ISTABLE_EXISTS_QUERY);
                createTableQuery = statusDashboardQueryManager.getQuery(SQLConstants.CREATE_TABLE);
            } catch (SQLException | ConfigurationException | IOException | QueryMappingNotAvailableException e) {
                throw new StatusDashboardRuntimeException("Error initializing connection. ", e);
            } finally {
                cleanupConnection(conn);
            }
            createWorkerConfigurationDB();
            createManagerConfigurationDB();
            creteDetailsDB();
        } else {
            throw new RDBMSTableException(DATASOURCE_ID + " Could not find. Hence cannot initialize the status " +
                    "dashboard. Please check database is available");
        }
    }

    private void createWorkerConfigurationDB() {
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        String resolved = tableCheckQuery.replace(PLACEHOLDER_TABLE_NAME, WORKER_CONFIG_TABLE);
        String resolvedTableCreateQuery = createTableQuery.replace(PLACEHOLDER_TABLE_NAME, WORKER_CONFIG_TABLE);
        if (!DBHandler.getInstance().isTableExist(conn, resolved)) {
            if (!isConfigTableCreated) {
                Map<String, String> attributesList = DBTableUtils.getInstance().loadWorkerConfigTableTuples
                        (statusDashboardQueryManager);
                String resolvedTuples = String.format(
                        "WORKERID " + STRING_TEMPLATE + " PRIMARY KEY" + TUPLES_SEPARATOR +
                                "HOST " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                "PORT " + STRING_TEMPLATE, attributesList.get("WORKERID"), attributesList.get("HOST"),
                        attributesList.get("PORT"));
                resolvedTableCreateQuery = resolvedTableCreateQuery.replace(PLACEHOLDER_COLUMNS_PRIMARYKEY,
                        resolvedTuples);
                try {
                    stmt = conn.prepareStatement(resolvedTableCreateQuery);
                    stmt.execute();
                    isConfigTableCreated = true;
                } catch (SQLException e) {
                    logger.error("Error creating table please create manually ." + WORKER_CONFIG_TABLE, e);
                } finally {
                    closePreparedStatement(stmt);
                    cleanupConnection(conn);
                }
            }
        }
    }

    private void createManagerConfigurationDB() {
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        String resolved = tableCheckQuery.replace(PLACEHOLDER_TABLE_NAME, MANAGER_CONFIG_TABLE);
        String resolvedTableCreateQuery = createTableQuery.replace(PLACEHOLDER_TABLE_NAME, MANAGER_CONFIG_TABLE);
        if (!isConfigTableCreated && (!DBHandler.getInstance().isTableExist(conn, resolved))) {
            Map<String, String> attributesList = DBTableUtils.getInstance().loadManagerConfigTableTuples
                    (statusDashboardQueryManager);
            String resolvedTuples = String.format(
                    "MANAGERID " + STRING_TEMPLATE + " PRIMARY KEY" + TUPLES_SEPARATOR +
                            "HOST " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                            "PORT " + STRING_TEMPLATE, attributesList.get(Constants.MANAGERID), attributesList
                            .get(Constants.HOST), attributesList.get(Constants.PORT));
            resolvedTableCreateQuery = resolvedTableCreateQuery.replace(PLACEHOLDER_COLUMNS_PRIMARYKEY,
                    resolvedTuples);
            try {
                stmt = conn.prepareStatement(resolvedTableCreateQuery);
                stmt.execute();
                isConfigTableCreated = true;
            } catch (SQLException e) {
                logger.error("Error occurred while creating table, please create manually ." + MANAGER_CONFIG_TABLE, e);
            } finally {
                closePreparedStatement(stmt);
                cleanupConnection(conn);
            }
        }
    }

    private void creteDetailsDB() {
        Connection conn = this.getConnection();
        String resolved = tableCheckQuery.replace(PLACEHOLDER_TABLE_NAME, WORKER_DETAILS_TABLE);
        String resolvedCreatedTable = createTableQuery.replace(PLACEHOLDER_TABLE_NAME, WORKER_DETAILS_TABLE);
        if (!DBHandler.getInstance().isTableExist(conn, resolved)) {
            if (!isGeneralTableCreated) {
                Map<String, String> attributesList = DBTableUtils.getInstance().loadWorkerGeneralTableTuples
                        (statusDashboardQueryManager);
                String resolvedTuples = String.format(
                        " CARBONID " + STRING_TEMPLATE + " PRIMARY KEY " + TUPLES_SEPARATOR +
                                " WORKERID " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " JAVARUNTIMENAME " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " JAVAVMVERSION " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " JAVAVMVENDOR " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " JAVAHOME " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " JAVAVERSION " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " OSNAME " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " OSVERSION " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " USERHOME " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " USERTIMEZONE " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " USERNAME " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " USERCOUNTRY " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " REPOLOCATION " + STRING_TEMPLATE + TUPLES_SEPARATOR +
                                " SERVERSTARTTIME " + STRING_TEMPLATE + TUPLES_SEPARATOR + " " + STRING_TEMPLATE
                        , attributesList.get("CARBONID"), attributesList.get("WORKERID"),
                        attributesList.get("JAVARUNTIMENAME"), attributesList.get("JAVAVMVERSION"),
                        attributesList.get("JAVAVMVENDOR"), attributesList.get("JAVAHOME"),
                        attributesList.get("JAVAVERSION"), attributesList.get("OSNAME"),
                        attributesList.get("OSVERSION"), attributesList.get("USERHOME"),
                        attributesList.get("USERTIMEZONE"), attributesList.get("USERNAME"),
                        attributesList.get("USERCOUNTRY"), attributesList.get("REPOLOCATION"),
                        attributesList.get("SERVERSTARTTIME"),
                        statusDashboardQueryManager.getQuery("foreignKeyQuery"));
                resolvedCreatedTable = resolvedCreatedTable.replace(PLACEHOLDER_COLUMNS_PRIMARYKEY, resolvedTuples);
                PreparedStatement stmt = null;
                try {
                    stmt = conn.prepareStatement(resolvedCreatedTable);
                    stmt.execute();
                    isGeneralTableCreated = true;
                    stmt.close();
                } catch (SQLException e) {
                    throw new RDBMSTableException("Error creating table there may have already existing database ." +
                            WORKER_DETAILS_TABLE);
                } finally {
                    closePreparedStatement(stmt);
                    cleanupConnection(conn);
                }
            }
        }
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
     * Returns a connection instance.
     *
     * @return a new {@link Connection} instance from the datasource.
     */
    private Connection getConnection() {
        return DBHandler.getInstance().getConnection(dataSource);
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
                    logger.debug("Closed Connection in Worker DB");
                }
            } catch (SQLException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error closing Connection in worker DB.", e);
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
            logger.debug("Error occurred while closing the statement.", e);
        }
    }

    /**
     * Method which is used to insert the worker configuration details to database.
     *
     * @param workerConfigurationDetails
     * @return isSuccess
     * @throws RDBMSTableException
     */
    public boolean insertWorkerConfiguration(NodeConfigurationDetails workerConfigurationDetails)
            throws RDBMSTableException {
        String columnNames = NodeConfigurationDetails.getColumnLabeles();
        Object[] records = workerConfigurationDetails.toArray();
        try {
            return this.insert(columnNames, records, WORKER_CONFIG_TABLE);
        } catch (RDBMSTableException e) {
            throw new RDBMSTableException(e.getMessage(), e);
        }

    }

    /**
     * Method which is used to insert the worker configuration details to database.
     *
     * @param workerGeneralDetails workerConfiguration object
     * @return isSuccess
     */
    public boolean insertWorkerGeneralDetails(WorkerGeneralDetails workerGeneralDetails) throws RDBMSTableException {
        String columnNames = WorkerGeneralDetails.getColumnLabeles();
        Object[] records = workerGeneralDetails.toArray();
        try {
            return this.insert(columnNames, records, WORKER_DETAILS_TABLE);
        } catch (RDBMSTableException e) {
            throw new RDBMSTableException("Error inserting worker general details.", e);
        }
    }

    /**
     * Method which is used to insert the manager configuration details to database.
     *
     * @param managerConfigurationDetails managerConfiguration object
     * @return isSuccess
     */
    public boolean insertManagerConfiguration(NodeConfigurationDetails managerConfigurationDetails) throws
            RDBMSTableException {
        String columnNames = NodeConfigurationDetails.getManagerColumnLabeles();
        Object[] records = managerConfigurationDetails.toArray();
        try {
            return this.insert(columnNames, records, MANAGER_CONFIG_TABLE);
        } catch (RDBMSTableException e) {
            throw new RDBMSTableException("Error while inserting the data. " + e.getMessage(), e);
        }

    }


    /**
     * Insert manager data manager db.
     *
     * @param columnNames column labels needed to get
     * @param records     objects needed to insert.
     * @return
     */
    private boolean insert(String columnNames, Object[] records, String tableName) throws RDBMSTableException {
        String resolvedInsertQuery = resolveTableName(insertQuery, tableName);
        Map<String, String> attributesTypes = workerAttributeTypeMap.get(tableName);
        String query = DBTableUtils.getInstance().composeInsertQuery(resolvedInsertQuery.replace(PLACEHOLDER_COLUMNS,
                "(" + columnNames + ")"), attributesTypes.size());
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt = DBTableUtils.getInstance().populateInsertStatement(records, stmt, attributesTypes,
                    statusDashboardQueryManager);
            DBHandler.getInstance().insert(stmt);
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException(e.getMessage(), e);
        } finally {
            closePreparedStatement(stmt);
            cleanupConnection(conn);
        }
    }

    /**
     * Delete workers data to worker db.
     *
     * @param workerId condition of the selection.
     * @return isSuccess.
     */
    public boolean deleteWorkerGeneralDetails(String workerId) {
        return this.delete(workerId, generateConditionWorkerID(QUESTION_MARK), WORKER_DETAILS_TABLE);
    }

    /**
     * Delete workers data to worker db.
     *
     * @param workerId condition of the selection.
     * @return isSuccess.
     */
    public boolean deleteWorkerConfiguration(String workerId) {
        return delete(workerId, generateConditionWorkerID(QUESTION_MARK), WORKER_CONFIG_TABLE);
    }

    /**
     * Delete manager data to manager db.
     *
     * @param managerId condition of the selection.
     * @return isSuccess.
     */
    public boolean deleteManagerConfiguration(String managerId) {
        return delete(managerId, generateConditionManagerID(QUESTION_MARK), MANAGER_CONFIG_TABLE);
    }

    /**
     * Delete workers data to worker db.
     *
     * @param tableName condition of the selection.
     * @param nodeId
     * @return isSuccess.
     */
    private boolean delete(String nodeId, String condition, String tableName) {
        String resolvedDeleteQuery = resolveTableName(deleteQuery, tableName);
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(resolvedDeleteQuery.replace(PLACEHOLDER_CONDITION,
                    SQL_WHERE + WHITESPACE + condition));
            stmt.setString(1, nodeId);
            DBHandler.getInstance().delete(stmt);
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException("Error occurred while deleting the node:" + nodeId + " in a table " +
                    tableName + DATASOURCE_ID, e);
        } finally {
            closePreparedStatement(stmt);
            cleanupConnection(conn);
        }
    }


    /**
     * Select worker from the worker DB.
     *
     * @param workerId condition of the selection.
     * @return list of object.
     */

    public WorkerGeneralDetails selectWorkerGeneralDetails(String workerId) {
        String columnNames = WorkerGeneralDetails.getColumnLabeles();
        List<Object> row = this.select(generateConditionWorkerID(QUESTION_MARK), columnNames, WORKER_DETAILS_TABLE,
                new String[]{workerId});
        if (!row.isEmpty()) {
            WorkerGeneralDetails details = new WorkerGeneralDetails();
            try {
                details.setArrayList(row);
            } catch (StatusDashboardValidationException e) {
                logger.error("Error mapping the data in row in worker general details.");
            }
            return details;
        } else {
            return null;
        }
    }

    /**
     * Select worker from the worker DB.
     *
     * @param workerId condition of the selection.
     * @return list of object.
     */
    public String selectWorkerCarbonID(String workerId) {
        String columnNames = "CARBONID";
        List<Object> row = this.select(generateConditionWorkerID(QUESTION_MARK), columnNames, WORKER_DETAILS_TABLE,
                new String[]{workerId});
        if (row.size() > 0) {
            return (String) row.get(0);
        } else {
            return null;
        }
    }

    /**
     * Select worker from the worker DB.
     *
     * @param workerId condition of the selection.
     * @return list of object.
     */
    public NodeConfigurationDetails selectWorkerConfigurationDetails(String workerId) {
        String columnNames = NodeConfigurationDetails.getColumnLabeles();
        List<Object> row = this.select(generateConditionWorkerID(QUESTION_MARK), columnNames, WORKER_CONFIG_TABLE,
                new String[]{workerId});
        if (!row.isEmpty()) {
            NodeConfigurationDetails details = new NodeConfigurationDetails();
            try {
                details.setArrayList(row);
            } catch (StatusDashboardValidationException e) {
                logger.error("Error mapping the data in row : " + row.toString());
            }
            return details;
        } else {
            return null;
        }
    }

    /**
     * Select worker from the worker DB.
     *
     * @param condition condition of the selection.
     * @param columns   column labels needed to get
     * @return list of object.
     */
    private List<Object> select(String condition, String columns, String tableName, String[] parameters) {
        String resolvedSelectQuery = resolveTableName(this.selectQuery, tableName);
        Map<String, String> attributesTypes = workerAttributeTypeMap.get(tableName);
        Connection conn = this.getConnection();
        List<Object> row = new ArrayList<>();
        String[] columnLabels = columns.split(",");
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(DBTableUtils.getInstance().formatQueryWithCondition
                    (resolvedSelectQuery.replace(PLACEHOLDER_COLUMNS, String.format(" %s ", columns)), condition));
            for (int i = 1; i <= parameters.length; i++) {
                stmt.setString(i, parameters[i - 1]);
            }
            rs = DBHandler.getInstance().select(stmt);
            while (rs.next()) {
                for (String columnLabel : columnLabels) {
                    row.add(DBTableUtils.getInstance().fetchData(rs, columnLabel, attributesTypes.get
                            (columnLabel.trim()), statusDashboardQueryManager));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table.", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                //ignore
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                //ignore
            }
            cleanupConnection(conn);
        }
        return row;
    }

    /**
     * Select worker from the worker DB.
     *
     * @return list of object.
     */
    public List<NodeConfigurationDetails> selectAllWorkers() {
        String resolvedSelectQuery = resolveTableName(this.selectQuery, WORKER_CONFIG_TABLE);
        Map<String, String> attributesTypes = workerAttributeTypeMap.get(WORKER_CONFIG_TABLE);
        Connection conn = this.getConnection();
        NodeConfigurationDetails row;
        List<NodeConfigurationDetails> workerConfigurationDetails = new ArrayList<>();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(resolvedSelectQuery.replace(PLACEHOLDER_COLUMNS, WHITESPACE +
                    NodeConfigurationDetails.getColumnLabeles()).replace(PLACEHOLDER_CONDITION, ""));
            ResultSet rs = DBHandler.getInstance().select(stmt);
            while (rs.next()) {
                row = new NodeConfigurationDetails();
                row.setPort((Integer) DBTableUtils.getInstance().fetchData(rs, "PORT", attributesTypes.get
                        ("PORT"), statusDashboardQueryManager));
                row.setHost((String) DBTableUtils.getInstance().fetchData(rs, "HOST", attributesTypes.get
                        ("HOST"), statusDashboardQueryManager));
                row.setWorkerId((String) DBTableUtils.getInstance().fetchData(rs, "WORKERID",
                        attributesTypes.get("WORKERID"), statusDashboardQueryManager));
                workerConfigurationDetails.add(row);

            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table '" + "WORKER CONFIGURATION", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    //ignore
                }
            }
            cleanupConnection(conn);
        }
        return workerConfigurationDetails;
    }

    /**
     * select managers in the database.
     */
    public List<NodeConfigurationDetails> getAllManagerConfigDetails() {
        String resolvedSelectQuery = resolveTableName(this.selectQuery, MANAGER_CONFIG_TABLE);
        Map<String, String> attributesTypes = workerAttributeTypeMap.get(MANAGER_CONFIG_TABLE);
        Connection conn = this.getConnection();
        NodeConfigurationDetails row;
        List<NodeConfigurationDetails> workerConfigurationDetails = new ArrayList<>();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(resolvedSelectQuery.replace(PLACEHOLDER_COLUMNS, WHITESPACE +
                    NodeConfigurationDetails.getManagerColumnLabeles()).replace(PLACEHOLDER_CONDITION, ""));
            ResultSet rs = DBHandler.getInstance().select(stmt);
            while (rs.next()) {
                row = new NodeConfigurationDetails();
                row.setPort((Integer) DBTableUtils.getInstance().fetchData(rs, "PORT", attributesTypes.get
                        (Constants.PORT), statusDashboardQueryManager));
                row.setHost((String) DBTableUtils.getInstance().fetchData(rs, "HOST", attributesTypes.get
                        (Constants.HOST), statusDashboardQueryManager));
                row.setWorkerId((String) DBTableUtils.getInstance().fetchData(rs, "MANAGERID",
                        attributesTypes.get(Constants.MANAGERID),
                        statusDashboardQueryManager));
                workerConfigurationDetails.add(row);
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table '" + "MANAGER CONFIGURATION", e);
        } finally {
            closePreparedStatement(stmt);
            cleanupConnection(conn);
        }
        return workerConfigurationDetails;
    }

    /**
     * Generated thw worker ID condition.
     *
     * @param workerIdPlaceHolder sp-workerID
     * @return generated condition of workerID
     */
    private String generateConditionWorkerID(String workerIdPlaceHolder) {
        return WORKERID_EXPRESSION.replace(WORKERID_PLACEHOLDER, workerIdPlaceHolder);
    }

    /**
     * Generated thw worker ID condition.
     *
     * @param managerIdPlaceHolder sp-workerID
     * @return generated condition of workerID
     */
    private String generateConditionManagerID(String managerIdPlaceHolder) {
        return MANAGERID_EXPRESSION.replace(MANAGERID_PLACEHOLDER, managerIdPlaceHolder);
    }
}
