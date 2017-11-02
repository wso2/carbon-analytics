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
import org.wso2.carbon.status.dashboard.core.bean.WorkerConfigurationDetails;
import org.wso2.carbon.status.dashboard.core.bean.WorkerGeneralDetails;
import org.wso2.carbon.status.dashboard.core.dbhandler.exceptions.RDBMSTableException;
import org.wso2.carbon.status.dashboard.core.dbhandler.exceptions.StatusDashboardValidationException;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.DBTableUtils;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.QueryManager;
import org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants;
import org.wso2.carbon.status.dashboard.core.internal.DashboardDataHolder;
import org.wso2.carbon.status.dashboard.core.services.DefaultQueryLoaderService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_COLUMN;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_COLUMNS;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_CONDITION;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_TABLE_NAME;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.SQL_WHERE;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.WHITESPACE;

// TODO: 11/1/17 Constants

/**
 * This class represents key database operations related to worker data.
 */
public class StatusDashboardWorkerDBHandler {
    private static final Logger logger = LoggerFactory.getLogger(StatusDashboardWorkerDBHandler.class);
    private static final String DATASOURCE_ID = DashboardDataHolder.getDashboardDataSourceName();
    private String selectQuery;
    private String deleteQuery;
    private String insertQuery;
    private HikariDataSource dataSource;
    private Connection conn;
    private Map<String, Map<String, String>> workerAttributeTypeMap;
    private static final String WORKERID_PLACEHOLDER = "{{WORKER_ID}}";
    private static final String WORKERID_EXPRESSION = "WORKERID='{{WORKER_ID}}'";
    private static final String WORKER_DETAILS_TABLE = "WORKERS_DETAILS";
    private static final String WORKER_CONFIG_TABLE = "WORKERS_CONFIGURATION";
    private String tableCreateQuery;
    private String tableCheckQuery;

    public StatusDashboardWorkerDBHandler() {
        dataSource = DashboardDataHolder.getInstance().getDashboardDataSource();
        if (dataSource != null) {
            this.conn = DBHandler.getInstance().getConnection(dataSource);
            String dbType = DBTableUtils.getInstance().getDBType(this.conn);
            QueryManager.getInstance().readConfigs(dbType);
            workerAttributeTypeMap = DBTableUtils.getInstance().loadWorkerAttributeTypeMap();
            selectQuery = QueryManager.getInstance().getQuery(SQLConstants.SELECT_QUERY);
            selectQuery = loadQuery(selectQuery, SQLConstants.SELECT_QUERY, dbType);

            deleteQuery = QueryManager.getInstance().getQuery(SQLConstants.DELETE_QUERY);
            deleteQuery = loadQuery(deleteQuery, SQLConstants.DELETE_QUERY, dbType);

            insertQuery = QueryManager.getInstance().getQuery(SQLConstants.INSERT_QUERY);
            insertQuery = loadQuery(insertQuery, SQLConstants.INSERT_QUERY, dbType);

            // TODO: 11/2/17 proper fix
            tableCheckQuery = QueryManager.getInstance().getQuery(SQLConstants.ISTABLE_EXISTS_QUERY);
            tableCheckQuery = loadQuery(tableCheckQuery, SQLConstants.ISTABLE_EXISTS_QUERY, dbType);

            tableCreateQuery = QueryManager.getInstance().getQuery(SQLConstants.CREATE_TABLE);
            tableCreateQuery = loadQuery(tableCreateQuery, SQLConstants.CREATE_TABLE, dbType);

            creteConfigurationDB();
            creteDetailsDB();
        } else {
            throw new RDBMSTableException(DATASOURCE_ID + " Could not find. Hence cannot initialize the status " +
                    "dashboard.");
        }
    }

    // TODO: 11/2/17 improve for all databases
    private void creteConfigurationDB() {
        String resolvedTableCreateQuery = "CREATE TABLE IF NOT EXISTS WORKERS_CONFIGURATION (\n" +
                "WORKERID VARCHAR(255) PRIMARY KEY,\n" +
                "HOST VARCHAR(500),\n" +
                "PORT INT\n" +
                ");";
        Connection conn = this.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(resolvedTableCreateQuery);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error creating table." + WORKER_CONFIG_TABLE,e);
        }
    }

    private void creteDetailsDB() {
        String resolvedTableCreateQuery = "CREATE TABLE IF NOT EXISTS WORKERS_DETAILS (\n" +
                " CARBONID VARCHAR(255) PRIMARY KEY ,\n" +
                " WORKERID VARCHAR(255),\n" +
                " JAVARUNTIMENAME VARCHAR(255),\n" +
                " JAVAVMVERSION VARCHAR(255),\n" +
                " JAVAVMVENDOR VARCHAR(255),\n" +
                " JAVAHOME VARCHAR(255),\n" +
                " JAVAVERSION VARCHAR(255),\n" +
                " OSNAME VARCHAR(255),\n" +
                " OSVERSION VARCHAR(255),\n" +
                " USERHOME VARCHAR(255),\n" +
                " USERTIMEZONE VARCHAR(255),\n" +
                " USERNAME VARCHAR(255),\n" +
                " USERCOUNTRY VARCHAR(255),\n" +
                " REPOLOCATION VARCHAR(255),\n" +
                " SERVERSTARTTIME BIGINT,\n" +
                " LASTSNAPSHOTTIME BIGINT,\n" +
                " FOREIGN KEY (WORKERID) REFERENCES WORKERS_CONFIGURATION(WORKERID)\n" +
                ");";
        Connection conn = this.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(resolvedTableCreateQuery);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error creating table." + WORKER_DETAILS_TABLE);
        }
    }

    /**
     * This will load the database general queries which is in deployment YAML or default queries.
     *
     * @param query  DB query from YAML.
     * @param key    requested query name.
     * @param dbType Database type
     * @return
     */
    private String loadQuery(String query, String key, String dbType) {
        if (query != null) {
            return query;
        } else {
            return DefaultQueryLoaderService.getInstance()
                    .getDashboardDefaultConfigurations().getQueries().get(dbType).get(key);
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
        try {
            if (conn != null && !conn.isClosed()) {
                return conn;
            } else {
                return DBHandler.getInstance().getConnection(dataSource);
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Error occurred in while checking the connection in closed.  " +
                    DATASOURCE_ID + e.getMessage(), e);
        }
    }

    /**
     * Method which is used to insert the worker configuration details to database.
     *
     * @param workerConfigurationDetails workerConfiguration object
     * @return isSuccess
     */
    public boolean insertWorkerConfiguration(WorkerConfigurationDetails workerConfigurationDetails) throws RDBMSTableException {
        String columnNames = WorkerConfigurationDetails.getColumnLabeles();
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
            throw new RDBMSTableException(e.getMessage(), e);
        }
    }

    /**
     * Insert worker data worker db.
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
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt = DBTableUtils.getInstance().populateInsertStatement(records, stmt, attributesTypes);
            DBHandler.getInstance().insert(stmt);
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException("Attempted execution of query [" + query + "] produced an exceptions" +
                    " in " + DATASOURCE_ID, e);
        }
    }

    /**
     * Delete workers data to worker db.
     *
     * @param workerId condition of the selection.
     * @return isSuccess.
     */
    public boolean deleteWorkerGeneralDetails(String workerId) {
        return this.delete(generateConditionWorkerID(workerId), WORKER_DETAILS_TABLE);
    }

    /**
     * Delete workers data to worker db.
     *
     * @param workerId condition of the selection.
     * @return isSuccess.
     */
    public boolean deleteWorkerConfiguration(String workerId) {
        return this.delete(generateConditionWorkerID(workerId), WORKER_CONFIG_TABLE);
    }

    /**
     * Delete workers data to worker db.
     *
     * @param tableName condition of the selection.
     * @param workerId
     * @return isSuccess.
     */
    private boolean delete(String workerId, String tableName) {
        String resolvedDeleteQuery = resolveTableName(deleteQuery, tableName);
        Connection conn = this.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(resolvedDeleteQuery.replace(PLACEHOLDER_CONDITION,
                    SQL_WHERE + WHITESPACE + workerId));
            DBHandler.getInstance().delete(stmt);
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException("Error occurred while deleting the worker:" + workerId + " in a table " +
                    tableName + DATASOURCE_ID, e);
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
        List<Object> row = this.select(generateConditionWorkerID(workerId), columnNames, WORKER_DETAILS_TABLE);
        if (!row.isEmpty()) {
            WorkerGeneralDetails details = new WorkerGeneralDetails();
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
     * @param workerId condition of the selection.
     * @return list of object.
     */
    public String selectWorkerCarbonID(String workerId) {
        String columnNames = "CARBONID";
        List<Object> row = this.select(generateConditionWorkerID(workerId), columnNames, WORKER_DETAILS_TABLE);
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
    public WorkerConfigurationDetails selectWorkerConfigurationDetails(String workerId) {
        String columnNames = WorkerConfigurationDetails.getColumnLabeles();
        List<Object> row = this.select(generateConditionWorkerID(workerId), columnNames, WORKER_CONFIG_TABLE);
        if (!row.isEmpty()) {
            WorkerConfigurationDetails details = new WorkerConfigurationDetails();
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
    private List<Object> select(String condition, String columns, String tableName) {
        String resolvedSelectQuery = resolveTableName(this.selectQuery, tableName);
        Map<String, String> attributesTypes = workerAttributeTypeMap.get(tableName);
        Connection conn = this.getConnection();
        List<Object> row = new ArrayList<>();
        String[] columnLabels = columns.split(",");
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(DBTableUtils.getInstance().formatQueryWithCondition
                    (resolvedSelectQuery.replace(PLACEHOLDER_COLUMNS, WHITESPACE + columns),
                            condition));
            rs = DBHandler.getInstance().select(stmt);
            while (rs.next()) {
                for (String columnLabel : columnLabels) {
                    row.add(DBTableUtils.getInstance().fetchData(rs, columnLabel, attributesTypes.get
                            (columnLabel.trim())));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table '" + "': "
                    + e.getMessage(), e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                //ignore
            }
        }
        return row;
    }

    /**
     * Select worker from the worker DB.
     *
     * @return list of object.
     */
    public List<WorkerConfigurationDetails> selectAllWorkers() {
        String resolvedSelectQuery = resolveTableName(this.selectQuery, WORKER_CONFIG_TABLE);
        Map<String, String> attributesTypes = workerAttributeTypeMap.get(WORKER_CONFIG_TABLE);
        Connection conn = this.getConnection();
        WorkerConfigurationDetails row;
        List<WorkerConfigurationDetails> workerConfigurationDetails = new ArrayList<>();

        try {
            PreparedStatement stmt = conn.prepareStatement(DBTableUtils.getInstance().formatQueryWithCondition
                    (resolvedSelectQuery.replace(PLACEHOLDER_COLUMNS, WHITESPACE +
                            WorkerConfigurationDetails.getColumnLabeles()), "true"));
            ResultSet rs = DBHandler.getInstance().select(stmt);
            while (rs.next()) {
                row = new WorkerConfigurationDetails();
                row.setPort((Integer) DBTableUtils.getInstance().fetchData(rs, "PORT", attributesTypes.get
                        ("PORT")));
                row.setHost((String) DBTableUtils.getInstance().fetchData(rs, "HOST", attributesTypes.get
                        ("HOST")));
                row.setWorkerId((String) DBTableUtils.getInstance().fetchData(rs, "WORKERID",
                        attributesTypes.get("WORKERID")));
                workerConfigurationDetails.add(row);

            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table '" + "WORKER CONFIGURATION" + "': "
                    + e.getMessage(), e);
        }
        return workerConfigurationDetails;
    }

    /**
     * Used to update worker configuration details.
     *
     * @param workerId the ID host:port of the worker
     * @returnboolean update is success or not.
     */
    public boolean updateWorkerConfigDetails(String workerId, String username, String password) {
        try {
            Connection conn = this.getConnection();
            String query = "update WORKERS_CONFIGURATION set " + ", USERNAME='" + username +
                    "', PASSWORD='" + password + "'" + " where " + generateConditionWorkerID(workerId);
            PreparedStatement stmt = conn.prepareStatement(query);
            DBHandler.getInstance().update(stmt);
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException(e.getMessage() + " in " + DATASOURCE_ID, e);
        }
    }

    /**
     * Used to update worker general details.
     *
     * @param workerId             the ID host:port of the worker
     * @param workerGeneralDetails worker general details object.
     * @return update is success or not.
     */
    public boolean updateWorkerGerneralDetails(String workerId, WorkerGeneralDetails workerGeneralDetails) {
        try {
            Map<String, String> attributesTypes = workerAttributeTypeMap.get(WORKER_DETAILS_TABLE);
            Object[] newRecords = workerGeneralDetails.toArray();
            Connection conn = this.getConnection();
            String query = "update WORKERS_DETAILS set " + WorkerGeneralDetails.getColumnLabeles().replace
                    (",", "=? ,") + " where " + generateConditionWorkerID(workerId);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt = DBTableUtils.getInstance().populateUpdateStatement(newRecords, stmt, attributesTypes);
            DBHandler.getInstance().update(stmt);
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException(e.getMessage() + " in " + DATASOURCE_ID, e);
        }
    }

    /**
     * Closed db connection.
     */
    public void cleanupConnection() {
        Connection conn = this.getConnection();
        DBHandler.getInstance().cleanupConnection(conn);
    }

    /**
     * Generated thw worker ID condition.
     *
     * @param workerId sp-workerID
     * @return generated condition of workerID
     */
    private String generateConditionWorkerID(String workerId) {
        return WORKERID_EXPRESSION.replace(WORKERID_PLACEHOLDER, workerId);
    }
}
