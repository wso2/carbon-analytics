package org.wso2.carbon.status.dashboard.core.persistence.store.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.status.dashboard.core.config.DBQueries;
import org.wso2.carbon.status.dashboard.core.config.SpDashboardConfiguration;
import org.wso2.carbon.status.dashboard.core.persistence.datasourceServicers.StatusDashboardMetricsDataHolder;
import org.wso2.carbon.status.dashboard.core.persistence.datasourceServicers.StatusDashboardWorkerDataHolder;
import org.wso2.carbon.status.dashboard.core.persistence.store.impl.exception.RDBMSTableException;
import org.wso2.carbon.status.dashboard.core.persistence.store.impl.util.RDBMSTableUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.bind.ValidationException;

import static org.wso2.carbon.status.dashboard.core.persistence.store.impl.util.RDBMSTableConstants.SQL_WHERE;
import static org.wso2.carbon.status.dashboard.core.persistence.store.impl.util.RDBMSTableConstants.WHITESPACE;

/**
 * .
 */
public class WorkerDetailsStore {
    private static final Logger logger = LoggerFactory.getLogger(WorkerDetailsStore.class);
    private String dataSourceName;
    private String selectQuery;
    private String selectSomeQuery;
    private String containsQuery;
    private String deleteQuery;
    private String insertQuery;
    private String recordUpdateQuery;
    private String tableCheckQuery;

    //Placeholder strings needed for processing the query configuration file
    private static final String PLACEHOLDER_CONDITION = "{{CONDITION}}";
    private static final String PLACEHOLDER_COLUMNS_VALUES = "{{COLUMNS_AND_VALUES}}";
    private static final String PLACEHOLDER_TABLE_NAME = "{{TABLE_NAME}}";
    public static final String PLACEHOLDER_COLUMNS = "{{COLUMNS}}";
    private static final String PLACEHOLDER_Q = "{{Q}}";
    //Miscellaneous SQL constants

    private static final String SEPARATOR = ", ";
    private static final String QUESTION_MARK = "?";
    private HikariDataSource dataSource = null;
    private DBQueries dbQueries;
    private Map<String, String> attributesTypeMap = new HashMap<>();
    private String tableName = "WORKER_DETAIL";
    private List<Attribute> attributes;

    public void initProcessing(String datasourceName, String tableName) throws ValidationException {
        Statement stmt = null;
        boolean isConnected = connect(datasourceName);
        if (isConnected) {
            try {
                Connection connection = dataSource.getConnection();
                stmt = connection.createStatement();
                String query = dbQueries.getTableCheckQuery().replace(PLACEHOLDER_TABLE_NAME, tableName);
                ResultSet rs = stmt.executeQuery(query);
                ResultSetMetaData metaData = rs.getMetaData();
                int count = metaData.getColumnCount(); //number of column
                attributes = new ArrayList<>();
                for (int i = 1; i <= count; i++) {
                    attributesTypeMap.put(metaData.getColumnLabel(i),
                            metaData.getColumnTypeName(i).toUpperCase());
                    Attribute attribute = new Attribute(metaData.getColumnLabel(i),
                            metaData.getColumnTypeName(i).toUpperCase());
                    attributes.add(attribute);
                }
                selectQuery = this.resolveTableName(dbQueries.getRecordSelectQuery());
                selectSomeQuery = this.resolveTableName(dbQueries.getRecordSelectSomeQuery());
                containsQuery = this.resolveTableName(dbQueries.getRecordExistsQuery());
                deleteQuery = this.resolveTableName(dbQueries.getRecordDeleteQuery());
                insertQuery = this.resolveTableName(dbQueries.getRecordInsertQuery());
                recordUpdateQuery = this.resolveTableName(dbQueries.getRecordUpdateQuery());
                tableCheckQuery = this.resolveTableName(dbQueries.getTableCheckQuery());
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
            this.tableName = tableName;
        }
    }

    /**
     * Method for replacing the placeholder for the table name with the Event Table's name.
     *
     * @param statement the SQL statement in string form.
     * @return the formatted SQL statement.
     */
    private String resolveTableName(String statement) {
        if (statement == null) {
            return null;
        }
        return statement.replace(PLACEHOLDER_TABLE_NAME, this.tableName);
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

    public boolean insert(Object[] records) throws SQLException {
        String query = this.composeInsertQuery();
        PreparedStatement stmt = null;
        Connection conn = this.getConnection();
        try {
            stmt = conn.prepareStatement(query);
            this.populateInsertStatement(records, stmt);
            stmt.execute();
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempted execution of query [" + query + "] produced an exception: " + e.getMessage());
            }
        } finally {
            cleanupConnection(null, stmt, conn);
        }
        return false;
    }

    /**
     * Method for populating values to a pre-created SQL prepared statement.
     *
     * @param record the record whose values should be populated.
     * @param stmt   the statement to which the values should be set.
     */
    private void populateInsertStatement(Object[] record, PreparedStatement stmt) {
        Attribute attribute = null;
        try {
            for (int i = 0; i < this.attributes.size(); i++) {
                attribute = this.attributes.get(i);
                Object value = record[i];
                if (value != null || Objects.equals(attribute.getType(), "STRING")) {
                    RDBMSTableUtils.populateStatementWithSingleElement(stmt, i + 1, attribute.getType(), value);
                } else {
                    throw new RDBMSTableException("Cannot Execute Insert/Update: null value detected for " +
                            "attribute '" + attribute.getName() + "'");
                }
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Dropping event since value for attribute name " + attribute.getName() +
                    "cannot be set: " + e.getMessage(), e);
        }
    }

    public boolean delete(String condition) throws SQLException {
        PreparedStatement stmt = null;
        Connection conn = this.getConnection();
        stmt = conn.prepareStatement(deleteQuery.replace(PLACEHOLDER_CONDITION, SQL_WHERE + WHITESPACE + condition));
        stmt.execute();
        stmt.close();
        return false;
    }


    public List select(String condition, String columns) {
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs;
        List<Object> tuple = new ArrayList<>();
        if ("*".equals(columns)) {
            try {
                stmt = conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(selectQuery,
                        condition));
                rs = stmt.executeQuery();
                while (rs.next()) {
                    for (Attribute attribute : attributes) {
                        tuple.add(fletchData(rs, attribute));
                    }
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                cleanupConnection(null, stmt, conn);
                throw new RDBMSTableException("Error retrieving records from table '" + this.tableName + "': "
                        + e.getMessage(), e);
            }
        } else {
            String[] columnLabels = columns.split(",");
            try {
                stmt = conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(selectSomeQuery.replace
                        (PLACEHOLDER_COLUMNS, WHITESPACE + columns), condition));
                rs = stmt.executeQuery();
                while (rs.next()) {
                    for (String columnLabel : columnLabels) {
                        tuple.add(fletchData(rs, new Attribute(columnLabel , attributesTypeMap.get(columnLabel))));
                    }
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                cleanupConnection(null, stmt, conn);
                throw new RDBMSTableException("Error retrieving records from table '" + this.tableName + "': "
                        + e.getMessage(), e);
            }
        }
        return tuple;
    }

    private Object fletchData(ResultSet rs, Attribute attribute) throws SQLException {
        switch (attribute.getType()) {
            case "BOOL":
                return rs.getBoolean(attribute.getName());
            case "DOUBLE":
                return rs.getDouble(attribute.getName());
            case "FLOAT":
                return rs.getFloat(attribute.getName());
            case "INT":
                return rs.getInt(attribute.getName());
            case "LONG":
                return rs.getLong(attribute.getName());
            case "OBJECT":
                return rs.getObject(attribute.getName());
            case "STRING":
                return rs.getString(attribute.getName());
            default:
                logger.error("Invalid Type of Object ");
        }
        return null;
    }

    public Object update(String condition, String columsValues) throws SQLException {
        PreparedStatement stmt = null;
        Connection conn = this.getConnection();
        stmt = conn.prepareStatement(recordUpdateQuery.replace(PLACEHOLDER_COLUMNS_VALUES, columsValues).replace
                (PLACEHOLDER_CONDITION, SQL_WHERE + WHITESPACE + condition));
        stmt.execute();
        stmt.close();
        return condition;
    }


    public boolean isTupleAvailable(String tableName, String condition) {
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(containsQuery,
                    condition));
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error performing a contains check on table '" + this.tableName
                    + "': " + e.getMessage(), e);
        } finally {
            cleanupConnection(rs, stmt, conn);
        }
    }
    /**
     * Returns a connection instance.
     *
     * @return a new {@link Connection} instance from the datasource.
     */
    private Connection getConnection() {
        Connection conn;
        try {
            conn = this.dataSource.getConnection();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RDBMSTableException("Error initializing connection: " + e.getMessage(), e);
        }
        return conn;
    }

    public boolean isTableExist() {
        Connection connection = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement(tableCheckQuery);
            rs = stmt.executeQuery();
            return true;
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Table '" + "this.tableName" + "' assumed to not exist since its existence check resulted "
                        + "in exception " + e.getMessage());
            }
            return false;
        } finally {
            cleanupConnection(rs, stmt, connection);
        }
    }


    private void cleanupConnection(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
    }

    /**
     * Method for composing the SQL query for INSERT operations with proper placeholders.
     *
     * @return the composed SQL query in string form.
     */
    private String composeInsertQuery() {
        StringBuilder params = new StringBuilder();
        int fieldsLeft = attributes.size();
        while (fieldsLeft > 0) {
            params.append(QUESTION_MARK);
            if (fieldsLeft > 1) {
                params.append(SEPARATOR);
            }
            fieldsLeft = fieldsLeft - 1;
        }
        return insertQuery.replace(PLACEHOLDER_Q, params.toString());
    }


}
