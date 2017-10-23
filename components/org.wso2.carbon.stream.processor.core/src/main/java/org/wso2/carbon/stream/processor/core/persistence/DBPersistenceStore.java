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

package org.wso2.carbon.stream.processor.core.persistence;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.log4j.Logger;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.persistence.exception.DatabaseUnsupportedException;
import org.wso2.carbon.stream.processor.core.persistence.exception.DatasourceConfigurationException;
import org.wso2.carbon.stream.processor.core.persistence.util.ExecutionInfo;
import org.wso2.carbon.stream.processor.core.persistence.util.PersistenceConstants;
import org.wso2.carbon.stream.processor.core.persistence.util.RDBMSConfiguration;
import org.wso2.carbon.stream.processor.core.persistence.dto.RDBMSQueryConfigurationEntry;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;

/**
 * Implementation of Persistence Store that would persist snapshots to an RDBMS instance
 */
public class DBPersistenceStore implements PersistenceStore {

    private static final Logger log = Logger.getLogger(DBPersistenceStore.class);
    private static final String MSSQL_DATABASE_TYPE = "microsoft sql server";
    private static final String POSTGRES_DATABASE_TYPE = "postgresql";

    private ExecutionInfo executionInfo;
    private String datasourceName;
    private DataSource datasource;
    private String tableName;
    private int numberOfRevisionsToKeep;
    private String databaseType;

    @Override
    public void save(String siddhiAppName, String revision, byte[] snapshot) {
        createTableIfNotExist();

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            try {
                con = datasource.getConnection();
            } catch (SQLException e) {
                log.error("Cannot establish connection to datasource " + datasourceName +
                        " while saving revision " + revision, e);
                return;
            }
            con.setAutoCommit(false);
            stmt = con.prepareStatement(executionInfo.getPreparedInsertStatement());
            stmt.setString(1, siddhiAppName);
            stmt.setString(2, revision);
            if (databaseType.equals(POSTGRES_DATABASE_TYPE)) {
                stmt.setBlob(3, new SerialBlob(snapshot));
            } else {
                Blob blob = con.createBlob();
                blob.setBytes(1, snapshot);
                stmt.setBlob(3, blob);
            }
            stmt.executeUpdate();
            con.commit();
            if (log.isDebugEnabled()) {
                log.debug("Periodic persistence of " + siddhiAppName + " persisted successfully.");
            }
        } catch (SQLException e) {
            log.error("Error while saving revision" + revision + " of the siddhiApp " +
                    siddhiAppName + " to the database with datasource name " + datasourceName, e);
        } finally {
            cleanupConnections(stmt, con);
        }
        cleanOldRevisions(siddhiAppName);
    }

    @Override
    public void setProperties(Map properties) {
        Map configurationMap = (Map) properties.get(PersistenceConstants.STATE_PERSISTENCE_CONFIGS);
        Object numberOfRevisionsObject = properties.get(PersistenceConstants.STATE_PERSISTENCE_REVISIONS_TO_KEEP);
        if (numberOfRevisionsObject == null || !(numberOfRevisionsObject instanceof Integer)) {
            numberOfRevisionsToKeep = 3;
            if (log.isDebugEnabled()) {
                log.debug("Number of revisions to keep is not set or invalid. Default value will be used.");
            }
        } else {
            numberOfRevisionsToKeep = (int) numberOfRevisionsObject;
        }

        if (configurationMap != null) {
            Object datasourceObject = configurationMap.get("datasource");
            Object tableObject = configurationMap.get("table");
            if (datasourceObject == null || !(datasourceObject instanceof String)) {
                datasourceName = PersistenceConstants.DEFAULT_DB_PERSISTENCE_DATASOURCE;
                if (log.isDebugEnabled()) {
                    log.debug("datasource for database system persistence not set. Default datasource will be used.");
                }
            } else {
                datasourceName = String.valueOf(datasourceObject);
            }
            if (tableObject == null || !(tableObject instanceof String)) {
                tableName = PersistenceConstants.DEFAULT_DB_PERSISTENCE_TABLE_NAME;
                if (log.isDebugEnabled()) {
                    log.debug("Table name for database system persistence not set. Default table name will be used.");
                }
            } else {
                tableName = String.valueOf(tableObject);
            }

        } else {
            datasourceName = PersistenceConstants.DEFAULT_DB_PERSISTENCE_DATASOURCE;
            tableName = PersistenceConstants.DEFAULT_DB_PERSISTENCE_TABLE_NAME;
            if (log.isDebugEnabled()) {
                log.debug("Database system persistence config not set. Default config values will be used.");
            }
        }

        try {
            datasource = (HikariDataSource) StreamProcessorDataHolder.getDataSourceService().
                    getDataSource(datasourceName);
            databaseType = datasource.getConnection().getMetaData().getDatabaseProductName().toLowerCase();
        } catch (DataSourceException e) {
            throw new DatasourceConfigurationException("Datasource " + datasourceName +
                    " is not defined to use for snapshot persistence.", e);
        } catch (SQLException e) {
            throw new DatasourceConfigurationException("Connection cannot be established for datasource " +
                    datasourceName, e);
        }

        initializeDatabaseExecutionInfo();
    }

    @Override
    public byte[] load(String siddhiAppName, String revision) {
        PreparedStatement stmt = null;
        Connection con = null;
        byte[] blobAsBytes = null;
        try {
            try {
                con = datasource.getConnection();
            } catch (SQLException e) {
                log.error("Cannot establish connection to datasource " + datasourceName +
                        " while loading revision " + revision, e);
                return null;
            }
            con.setAutoCommit(false);
            stmt = con.prepareStatement(executionInfo.getPreparedSelectStatement());
            stmt.setString(1, revision);
            stmt.setString(2, siddhiAppName);
            ResultSet resultSet = stmt.executeQuery();
            con.commit();
            if (resultSet.next()) {
                Blob blobSnapshot;
                if (databaseType.equals(MSSQL_DATABASE_TYPE)) {
                    blobSnapshot = new SerialBlob(resultSet.getBytes("snapshot"));
                } else {
                    blobSnapshot = resultSet.getBlob("snapshot");
                }
                int blobLength = (int) blobSnapshot.length();
                blobAsBytes = blobSnapshot.getBytes(1, blobLength);
            }

        } catch (SQLException e) {
            log.error("Error while retrieving revision " + revision + " of siddhiApp: " +
                    siddhiAppName + " from the database with datasource " + datasourceName, e);
        } finally {
            cleanupConnections(stmt, con);
        }
        return blobAsBytes;
    }

    @Override
    public String getLastRevision(String siddhiAppName) {
        createTableIfNotExist();
        PreparedStatement stmt = null;
        Connection con = null;
        String revision = null;
        try {
            try {
                con = datasource.getConnection();
            } catch (SQLException e) {
                log.error("Cannot establish connection to datasource " + datasourceName +
                        " while trying retrieve last revision of " + siddhiAppName, e);
                return null;
            }
            stmt = con.prepareStatement(executionInfo.getPreparedSelectLastStatement());
            stmt.setString(1, siddhiAppName);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                revision = String.valueOf(resultSet.getString("revision"));
            }
        } catch (SQLException e) {
            log.error("Error while retrieving last revision of siddhiApp: " +
                    siddhiAppName + "from the database with datasource " + datasourceName, e);
        } finally {
            cleanupConnections(stmt, con);
        }

        return revision;
    }

    private void initializeDatabaseExecutionInfo() {
        executionInfo = new ExecutionInfo();
        RDBMSQueryConfigurationEntry databaseQueryEntries =
                RDBMSConfiguration.getInstance().getDatabaseQueryEntries(databaseType, tableName);

        if (databaseQueryEntries == null) {
            throw new DatabaseUnsupportedException("The configured database type " +
                    "is not supported with periodic persistence.");
        }

        executionInfo.setPreparedInsertStatement(databaseQueryEntries.getInsertTableQuery());
        executionInfo.setPreparedCreateTableStatement(databaseQueryEntries.getCreateTableQuery());
        executionInfo.setPreparedTableExistenceCheckStatement(databaseQueryEntries.getIsTableExistQuery());
        executionInfo.setPreparedSelectStatement(databaseQueryEntries.getSelectTableQuery());
        executionInfo.setPreparedSelectLastStatement(databaseQueryEntries.getSelectLastQuery());
        executionInfo.setPreparedDeleteStatement(databaseQueryEntries.getDeleteQuery());
        executionInfo.setPreparedCountStatement(databaseQueryEntries.getCountQuery());

    }

    /**
     * Method that would create the persistence table
     */
    private void createTableIfNotExist() {
        if (!executionInfo.isTableExist()) {
            Statement stmt = null;
            Connection con = null;
            try {
                try {
                    con = datasource.getConnection();
                    stmt = con.createStatement();
                } catch (SQLException e) {
                    log.error("Cannot establish connection to datasource " + datasourceName +
                            " when checking persistence table exists", e);
                    return;
                }
                try {
                    stmt.executeQuery(executionInfo.getPreparedTableExistenceCheckStatement());
                    executionInfo.setTableExist(true);

                } catch (SQLException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Table " + tableName + " does not Exist. Table Will be created. ");
                    }
                    try {
                        con.setAutoCommit(false);
                        stmt.executeUpdate(executionInfo.getPreparedCreateTableStatement());
                        con.commit();
                        executionInfo.setTableExist(true);
                    } catch (SQLException ex) {
                        log.error("Could not create table " + tableName +
                                " using datasource " + datasourceName, ex);
                    }
                }
            } finally {
                cleanupConnections(stmt, con);
            }
        }
    }

    /**
     * Method to remove revisions that are older than the user specified amount
     *
     * @param siddhiAppName is the name of the Siddhi Application whose old revisions to remove
     */
    private void cleanOldRevisions(String siddhiAppName) {
        PreparedStatement stmt = null;
        Connection con = null;
        int count = 0;

        try {
            con = datasource.getConnection();
            con.setAutoCommit(false);
        } catch (SQLException e) {
            log.error("Cannot establish connection to data source " + datasourceName +
                    " to clean old revisions", e);
            return;
        }
        try {
            stmt = con.prepareStatement(executionInfo.getPreparedCountStatement());
            stmt.setString(1, siddhiAppName);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
            int numberOfRevisionsToClean = count - numberOfRevisionsToKeep;
            if (numberOfRevisionsToClean > 0) {
                stmt = con.prepareStatement(executionInfo.getPreparedDeleteStatement());
                if (databaseType.equals(MSSQL_DATABASE_TYPE)) {
                    stmt.setInt(1, numberOfRevisionsToClean);
                    stmt.setString(2, siddhiAppName);
                } else {
                    stmt.setString(1, siddhiAppName);
                    stmt.setInt(2, numberOfRevisionsToClean);
                }
                stmt.executeUpdate();
                con.commit();
            }

        } catch (SQLException e) {
            log.error("Error in cleaning old revisions of siddhiApp: " +
                    siddhiAppName + "from the database with datasource " + datasourceName, e);
        } finally {
            cleanupConnections(stmt, con);
        }
    }


    private void cleanupConnections(Statement stmt, Connection connection) {
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
