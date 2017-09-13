package org.wso2.carbon.status.dashboard.core.persistence.store.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.status.dashboard.core.config.DBMapping;
import org.wso2.carbon.status.dashboard.core.config.DBQueries;
import org.wso2.carbon.status.dashboard.core.config.SpDashboardConfiguration;
import org.wso2.carbon.status.dashboard.core.persistence.datasourceServicers.StatusDashboardMetricsDataHolder;
import org.wso2.carbon.status.dashboard.core.persistence.datasourceServicers.StatusDashboardWorkerDataHolder;
import org.wso2.carbon.status.dashboard.core.persistence.store.WorkerStore;
import org.wso2.carbon.status.dashboard.core.persistence.store.impl.exception.RDBMSTableException;
import org.wso2.carbon.status.dashboard.core.persistence.store.impl.util.RDBMSTableUtils;

import javax.xml.bind.ValidationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * .
 */
public class WorkerDetailsStore implements WorkerStore {
    //Placeholder strings needed for processing the query configuration file
    public static final String RDBMS_QUERY_CONFIG_FILE = "rdbms-table-config.xml";
    public static final String PLACEHOLDER_COLUMNS = "{{COLUMNS, PRIMARY_KEYS}}";
    public static final String PLACEHOLDER_CONDITION = "{{CONDITION}}";
    public static final String PLACEHOLDER_COLUMNS_VALUES = "{{COLUMNS_AND_VALUES}}";
    public static final String PLACEHOLDER_TABLE_NAME = "{{TABLE_NAME}}";
    public static final String PLACEHOLDER_INDEX = "{{INDEX_COLUMNS}}";
    public static final String PLACEHOLDER_Q = "{{Q}}";
    //Miscellaneous SQL constants

    public static final String SEPARATOR = ", ";
    public static final String EQUALS = "=";
    public static final String QUESTION_MARK = "?";
    public static final String OPEN_PARENTHESIS = "(";
    public static final String CLOSE_PARENTHESIS = ")";
    private static final Logger logger = LoggerFactory.getLogger(WorkerDetailsStore.class);
    private HikariDataSource dataSource = null;
    private DBQueries dbQueries;
    private DBMapping dbMapping;
    private Statement stmt = null;

    public void init(String datasourceName) throws ValidationException {
        boolean isConnected = connect(datasourceName);
        Connection connection = dataSource.getConnection();
        if (isConnected) {
            try {
                stmt = connection.createStatement();
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private boolean connect(String datasourceName) throws ValidationException {

        if ("WSO2_STATUS_DASHBOARD_DB".equals(datasourceName)) {
            dataSource = StatusDashboardWorkerDataHolder.getInstance().getDataSource();
        } else if ("WSO2_METRICS_DB".equals(datasourceName)) {
            dataSource = StatusDashboardMetricsDataHolder.getInstance().getDataSource();
        } else {
            // TODO: 9/13/17 proper exception
            throw new ValidationException("Invalid datasource name");
        }

        SpDashboardConfiguration spDashboardConfiguration = new SpDashboardConfiguration();
        String dbType = getDBType(dataSource.getJdbcUrl());
        dbQueries = spDashboardConfiguration.getDBQueries(dbType);
        dbMapping = spDashboardConfiguration.getDbMapping(dbType);
        return true;
    }

    private String getDBType(String jdbcDriverURL) throws ValidationException {
        if (jdbcDriverURL != null) {
            if (jdbcDriverURL.split(":").length > 1) {
                String jdbcType = jdbcDriverURL.split(":")[1];
                if ("jdbc".equals(jdbcType)) {
                    return "mysql";
                } else if ("h2".equals(jdbcType)) {
                    return "h2";
                } else {
                    // TODO: 9/13/17 proper exception
                    throw new ValidationException("Invalid driver name");
                }
            } else {
                // TODO: 9/13/17 proper exception
                throw new ValidationException("Invalid format");
            }
        } else {
            // TODO: 9/13/17 proper exception
            throw new ValidationException("Please provide driver name");
        }
    }
    @Override
    public boolean insert(String tableName, Map values) {
        String sql = this.composeInsertQuery();
        try {
            this.batchExecuteQueriesWithRecords(sql, records, false);
        } catch (SQLException e) {
            throw new RDBMSTableException("Error in adding events to '" + this.tableName + "' store: "
                    + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean delete(String tableName, String condition) {
        stmt = RDBMSTableUtils.isEmpty(condition) ?
                conn.prepareStatement(deleteQuery.replace(PLACEHOLDER_CONDITION, "")) :
                conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(deleteQuery, condition));
        return false;
    }

    @Override
    public Object select(String tableName, String condition) {
        String condition = ((RDBMSCompiledCondition) compiledCondition).getCompiledQuery();
        //Some databases does not support single condition on where clause.
        //(atomic condition on where clause: SELECT * FROM TABLE WHERE true)
        //If the compile condition is resolved for '?', atomicCondition boolean value
        // will be used for ignore condition resolver.
        boolean atomicCondition = false;
        if (condition.equals(QUESTION_MARK)) {
            atomicCondition = true;
            if (log.isDebugEnabled()) {
                log.debug("Ignore the condition resolver in 'find()' method for compile " +
                        "condition: '" + QUESTION_MARK + "'");
            }
        }
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs;
        try {
            stmt = RDBMSTableUtils.isEmpty(condition) | atomicCondition ?
                    conn.prepareStatement(selectQuery.replace(PLACEHOLDER_CONDITION, "")) :
                    conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(selectQuery, condition));
            if (!atomicCondition) {
                RDBMSTableUtils.resolveCondition(stmt, (RDBMSCompiledCondition) compiledCondition,
                        findConditionParameterMap, 0);
            }
            rs = stmt.executeQuery();
            //Passing all java.sql artifacts to the iterator to ensure everything gets cleaned up at once.
            return new RDBMSIterator(conn, stmt, rs, this.attributes, this.tableName);
        } catch (SQLException e) {
            RDBMSTableUtils.cleanupConnection(null, stmt, conn);
            throw new RDBMSTableException("Error retrieving records from table '" + this.tableName + "': "
                    + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Object update(String tableName, String condition, Map values) {
        String sql = this.composeUpdateQuery(compiledCondition, updateSetExpressions);
        this.batchProcessSQLUpdates(sql, updateConditionParameterMaps, compiledCondition,
                updateSetExpressions, updateValues);
        return null;
    }

    @Override
    public boolean isTupleAvailable(String tableName, String condition) {
        String condition = ((RDBMSCompiledCondition) compiledCondition).getCompiledQuery();
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = RDBMSTableUtils.isEmpty(condition) ?
                    conn.prepareStatement(containsQuery.replace(PLACEHOLDER_CONDITION, "")) :
                    conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(containsQuery, condition));
            RDBMSTableUtils.resolveCondition(stmt, (RDBMSCompiledCondition) compiledCondition,
                    containsConditionParameterMap, 0);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error performing a contains check on table '" + this.tableName
                    + "': " + e.getMessage(), e);
        } finally {
            RDBMSTableUtils.cleanupConnection(rs, stmt, conn);
        }
        return false;
    }
    /**
     * Returns a connection instance.
     *
     * @param autoCommit whether or not transactions to the connections should be committed automatically.
     * @return a new {@link Connection} instance from the datasource.
     */
    private Connection getConnection(boolean autoCommit) {
        Connection conn;
        try {
            conn = this.dataSource.getConnection();
            conn.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new RDBMSTableException("Error initializing connection: " + e.getMessage(), e);
        }
        return conn;
    }
    @Override
    public boolean isTableExist() {
        Connection connection = this.getConnection(true);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement(dbQueries.getTableCheckQuery());
            rs = stmt.executeQuery();
            return true;
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Table '" + "this.tableName" + "' assumed to not exist since its existence check resulted "
                        + "in exception " + e.getMessage());
            }
            return false;
        } finally {
            RDBMSTableUtils.cleanupConnection(rs, stmt, connection);
        }
    }

    @Override
    public void cleanupConnections() {

    }

    /**
     * Method for composing the SQL query for INSERT operations with proper placeholders.
     *
     * @return the composed SQL query in string form.
     */
    private String composeInsertQuery(List<String> attributes) {
        StringBuilder params = new StringBuilder();
        int fieldsLeft = attributes.size();
        while (fieldsLeft > 0) {
            params.append(QUESTION_MARK);
            if (fieldsLeft > 1) {
                params.append(SEPARATOR);
            }
            fieldsLeft = fieldsLeft - 1;
        }
        return dbQueries.getRecordInsertQuery().replace(PLACEHOLDER_Q, params.toString());
    }

    /**
     * Method for composing the SQL query for UPDATE operations with proper placeholders.
     *
     * @return the composed SQL query in string form.
     */
    private String composeUpdateQuery(RDBMSCompiledCondition compiledCondition,
                                      Map<String, RDBMSCompiledCondition> updateSetExpressions) {
        String recordUpdateQuery;
        String condition = (compiledCondition).getCompiledQuery();
        String result = updateSetExpressions.entrySet().stream().map(e -> e.getKey()
                + " = " + (e.getValue()).getCompiledQuery())
                .collect(Collectors.joining(", "));
        recordUpdateQuery = dbQueries.getRecordUpdateQuery().replace(PLACEHOLDER_COLUMNS_VALUES, result);

        recordUpdateQuery = RDBMSTableUtils.isEmpty(condition) ? recordUpdateQuery.replace(PLACEHOLDER_CONDITION, "") :
                RDBMSTableUtils.formatQueryWithCondition(recordUpdateQuery, condition);
        return recordUpdateQuery;
    }
    /**
     * Method for performing data definition queries for the current datasource.
     *
     * @param queries    the list of queries to be executed.
     * @param autocommit whether or not the transactions should automatically be committed.
     * @throws SQLException if the query execution fails.
     */
    private void executeDDQueries(List<String> queries, boolean autocommit) throws SQLException {
        boolean committed = autocommit;
        PreparedStatement stmt;
        try {
            for (String query : queries) {
                stmt = connection.prepareStatement(query);
                stmt.execute();
                RDBMSTableUtils.cleanupConnection(null, stmt, null);
            }
            if (!autocommit) {
                connection.commit();
                committed = true;
            }
        } catch (SQLException e) {
            if (!autocommit) {
                RDBMSTableUtils.rollbackConnection(connection);
            }
            throw e;
        } finally {
            if (!committed) {
                RDBMSTableUtils.rollbackConnection(connection);
            }
            RDBMSTableUtils.cleanupConnection(null, null, connection);
        }
    }
}
