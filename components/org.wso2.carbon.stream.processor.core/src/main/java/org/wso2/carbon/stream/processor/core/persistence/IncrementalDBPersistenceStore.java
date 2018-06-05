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

package org.wso2.carbon.stream.processor.core.persistence;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.log4j.Logger;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.stream.processor.core.ha.util.CompressionUtil;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.persistence.dto.RDBMSQueryConfigurationEntry;
import org.wso2.carbon.stream.processor.core.persistence.exception.DatabaseUnsupportedException;
import org.wso2.carbon.stream.processor.core.persistence.exception.DatasourceConfigurationException;
import org.wso2.carbon.stream.processor.core.persistence.util.DBPersistenceStoreUtils;
import org.wso2.carbon.stream.processor.core.persistence.util.ExecutionInfo;
import org.wso2.carbon.stream.processor.core.persistence.util.PersistenceConstants;
import org.wso2.carbon.stream.processor.core.persistence.util.RDBMSConfiguration;
import org.wso2.siddhi.core.util.persistence.IncrementalPersistenceStore;
import org.wso2.siddhi.core.util.persistence.util.IncrementalSnapshotInfo;
import org.wso2.siddhi.core.util.persistence.util.PersistenceHelper;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncrementalDBPersistenceStore implements IncrementalPersistenceStore {
    private static final Logger log = Logger.getLogger(IncrementalDBPersistenceStore.class);

    private static final String MSSQL_DATABASE_TYPE = "microsoft sql server";
    private static final String POSTGRES_DATABASE_TYPE = "postgresql";

    private ExecutionInfo executionInfo;
    private String datasourceName;
    private DataSource datasource;
    private String databaseType;
    private String databaseVersion;
    private String tableName;

    @Override
    public void save(IncrementalSnapshotInfo incrementalSnapshotInfo, byte[] bytes) {
        byte[] compressedSnapshot;
        try {
            compressedSnapshot = CompressionUtil.compressGZIP(bytes);
        } catch (IOException e) {
            log.error("Error occurred while trying to compress the snapshot. Failed to " +
                    "persist revision: " + incrementalSnapshotInfo.getRevision() +
                    " of Siddhi app: " + incrementalSnapshotInfo.getSiddhiAppId());
            return;
        }
        DBPersistenceStoreUtils.createTableIfNotExist(executionInfo, datasource, datasourceName, tableName);

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            try {
                con = datasource.getConnection();
            } catch (SQLException e) {
                log.error("Cannot establish connection to datasource " + datasourceName +
                        " while saving revision " + incrementalSnapshotInfo.getRevision(), e);
                return;
            }
            con.setAutoCommit(false);
            stmt = con.prepareStatement(executionInfo.getPreparedInsertStatement());
            stmt.setString(1, incrementalSnapshotInfo.getSiddhiAppId());
            stmt.setString(2, incrementalSnapshotInfo.getRevision());
            if (databaseType.equals(POSTGRES_DATABASE_TYPE)) {
                stmt.setBlob(3, new SerialBlob(compressedSnapshot));
            } else {
                Blob blob = con.createBlob();
                blob.setBytes(1, compressedSnapshot);
                stmt.setBlob(3, blob);
            }
            stmt.executeUpdate();
            con.commit();
            if (log.isDebugEnabled()) {
                log.debug("Periodic persistence of " + incrementalSnapshotInfo.getSiddhiAppId() + " persisted successfully.");
            }
        } catch (SQLException e) {
            log.error("Error while saving revision" + incrementalSnapshotInfo.getRevision() + " of the siddhiApp " +
                    incrementalSnapshotInfo.getSiddhiAppId() + " to the database with datasource name " + datasourceName, e);
        } finally {
            DBPersistenceStoreUtils.cleanupConnections(stmt, con);
        }
        cleanOldRevisions(incrementalSnapshotInfo);
    }

    @Override
    public void setProperties(Map map) {
        Map configurationMap = (Map) map.get(PersistenceConstants.STATE_PERSISTENCE_CONFIGS);

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
            try (Connection connection = datasource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                databaseType = metaData.getDatabaseProductName().toLowerCase();
                databaseVersion = metaData.getDatabaseProductVersion();
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connected to database: " + databaseType + " " + databaseVersion);
                }
            }
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
    public byte[] load(IncrementalSnapshotInfo incrementalSnapshotInfo) {
        PreparedStatement stmt = null;
        Connection con = null;
        byte[] blobAsBytes = null;
        byte[] decompressedSnapshot = null;
        try {
            try {
                con = datasource.getConnection();
            } catch (SQLException e) {
                log.error("Cannot establish connection to datasource " + datasourceName +
                        " while loading revision " + incrementalSnapshotInfo.getRevision(), e);
                return null;
            }
            con.setAutoCommit(false);
            stmt = con.prepareStatement(executionInfo.getPreparedSelectStatement());
            stmt.setString(1, incrementalSnapshotInfo.getRevision());
            stmt.setString(2, incrementalSnapshotInfo.getSiddhiAppId());
            try (ResultSet resultSet = stmt.executeQuery()) {
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
                    try {
                        decompressedSnapshot = CompressionUtil.decompressGZIP(blobAsBytes);
                    } catch (IOException e) {
                        throw new RuntimeException("Error occurred while trying to decompress the snapshot. Failed to " +
                                "load revision: " + incrementalSnapshotInfo.getRevision() + " of Siddhi app: " +
                                incrementalSnapshotInfo.getSiddhiAppId(), e);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error while retrieving revision " + incrementalSnapshotInfo.getRevision() + " of siddhiApp: " +
                    incrementalSnapshotInfo.getSiddhiAppId() + " from the database with datasource " + datasourceName, e);
        } finally {
            DBPersistenceStoreUtils.cleanupConnections(stmt, con);
        }
        return decompressedSnapshot;
    }

    @Override
    public List<IncrementalSnapshotInfo> getListOfRevisionsToLoad(long restoreTime, String siddhiAppName) {
        List<IncrementalSnapshotInfo> results = new ArrayList<>();
        List<String> revisions = getListOfRevisionsFromDB(siddhiAppName);
        for (String revision : revisions) {
            IncrementalSnapshotInfo snapshotInfo = PersistenceHelper.convertRevision(revision);
            if (snapshotInfo.getTime() <= restoreTime &&
                    siddhiAppName.equals(snapshotInfo.getSiddhiAppId()) &&
                    snapshotInfo.getElementId() != null &&
                    snapshotInfo.getQueryName() != null) {
                //Note: Here we discard the (items.length == 2) scenario which is handled
                // by the full snapshot handling
                if (log.isDebugEnabled()) {
                    log.debug("List of revisions to load : " + revision);
                }
                results.add(snapshotInfo);
            }
        }
        return results;
    }

    @Override
    public String getLastRevision(String siddhiAppName) {
        createTableIfNotExist();
        List<String> revisions = getListOfRevisionsFromDB(siddhiAppName);
        long restoreTime = -1;
        IncrementalSnapshotInfo snapshotToLoad = null;
        for (String revision : revisions) {
            IncrementalSnapshotInfo snapshotInfo = PersistenceHelper.convertRevision(revision);
            if (snapshotInfo.getTime() > restoreTime &&
                    siddhiAppName.equals(snapshotInfo.getSiddhiAppId()) &&
                    snapshotInfo.getElementId() != null &&
                    snapshotInfo.getQueryName() != null) {
                //Note: Here we discard the (items.length == 2) scenario which is handled
                // by the full snapshot handling
                restoreTime = snapshotInfo.getTime();
                snapshotToLoad = snapshotInfo;
            }
        }
        if (restoreTime != -1) {
            if (log.isDebugEnabled()) {
                log.debug("Latest revision to load: " + restoreTime + PersistenceConstants.REVISION_SEPARATOR +
                        siddhiAppName);
            }
            return restoreTime + PersistenceConstants.REVISION_SEPARATOR + siddhiAppName
                    + PersistenceConstants.REVISION_SEPARATOR + snapshotToLoad.getQueryName()
                    + PersistenceConstants.REVISION_SEPARATOR + snapshotToLoad.getElementId()
                    + PersistenceConstants.REVISION_SEPARATOR + snapshotToLoad.getType();
        }
        return null;
    }

    private List<String> getListOfRevisionsFromDB(String siddhiAppName) {
        List<String> revisions = new ArrayList<>();
        PreparedStatement stmt = null;
        Connection con = null;
        try {
            try {
                con = datasource.getConnection();
            } catch (SQLException e) {
                log.error("Cannot establish connection to datasource " + datasourceName +
                        " . Could not load the list of revisions for Siddhi app: " + siddhiAppName, e);
                return null;
            }
            con.setAutoCommit(false);
            stmt = con.prepareStatement(executionInfo.getPreparedSelectRevisionsStatement());
            stmt.setString(1, siddhiAppName);
            try (ResultSet resultSet = stmt.executeQuery()) {
                con.commit();
                while (resultSet.next()) {
                    revisions.add(String.valueOf(resultSet.getString("revision")));
                }
            }
        } catch (SQLException e) {
            log.error("Could not load the list of revisions, for Siddhi app: " + siddhiAppName +
                    ", from the database with datasource " + datasourceName, e);
        } finally {
            DBPersistenceStoreUtils.cleanupConnections(stmt, con);
        }
        return revisions;
    }

    private void initializeDatabaseExecutionInfo() {
        executionInfo = new ExecutionInfo();
        RDBMSQueryConfigurationEntry databaseQueryEntries =
                RDBMSConfiguration.getInstance().getDatabaseQueryEntries(databaseType, databaseVersion, tableName);

        if (databaseQueryEntries == null) {
            throw new DatabaseUnsupportedException("The configured database type " +
                    "is not supported with periodic persistence.");
        }

        executionInfo.setPreparedInsertStatement(databaseQueryEntries.getInsertTableQuery());
        executionInfo.setPreparedCreateTableStatement(databaseQueryEntries.getCreateTableQuery());
        executionInfo.setPreparedTableExistenceCheckStatement(databaseQueryEntries.getIsTableExistQuery());
        executionInfo.setPreparedSelectStatement(databaseQueryEntries.getSelectTableQuery());
        executionInfo.setPreparedSelectLastStatement(databaseQueryEntries.getSelectLastQuery());
        executionInfo.setPreparedSelectRevisionsStatement(databaseQueryEntries.getSelectRevisionsQuery());
        executionInfo.setPreparedDeleteStatement(databaseQueryEntries.getDeleteQuery());
        executionInfo.setPreparedDeleteOldRevisionsStatement(databaseQueryEntries.getDeleteOldRevisionsQuery());
        executionInfo.setPreparedCountStatement(databaseQueryEntries.getCountQuery());
    }

    private void cleanOldRevisions(IncrementalSnapshotInfo incrementalSnapshotInfo) {
        if (incrementalSnapshotInfo.getType() == IncrementalSnapshotInfo.SnapshotType.BASE) {
            List<String> allRevisions = getListOfRevisionsFromDB(incrementalSnapshotInfo.getSiddhiAppId());
            if (allRevisions != null) {
                String revisionsToClean = getRevisionsToClean(incrementalSnapshotInfo, allRevisions);
                if (revisionsToClean != null) {
                    cleanOldRevisionsFromDB(revisionsToClean, incrementalSnapshotInfo.getSiddhiAppId());
                }
            }
        }
    }

    private void cleanOldRevisionsFromDB(String revisionsToClean, String siddhiAppId) {
        PreparedStatement stmt = null;
        Connection con = null;
        try {
            con = datasource.getConnection();
            con.setAutoCommit(false);
        } catch (SQLException e) {
            log.error("Cannot establish connection to data source " + datasourceName +
                    " to clean old revisions", e);
            return;
        }
        try {
            stmt = con.prepareStatement(executionInfo.getPreparedDeleteOldRevisionsStatement());
            stmt.setString(1, revisionsToClean);
            stmt.setString(2, siddhiAppId);
            stmt.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            log.error("Error in cleaning old revisions of siddhiApp: " +
                    siddhiAppId + "from the database with datasource " + datasourceName, e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("Unable to close statement." + e.getMessage(), e);
                }
            }
            DBPersistenceStoreUtils.cleanupConnections(stmt, con);
        }
    }

    private String getRevisionsToClean(IncrementalSnapshotInfo incrementalSnapshotInfo,
                                       List<String> allRevisions) {
        StringBuilder revisionsToClean = new StringBuilder();
        long baseTimeStamp = (incrementalSnapshotInfo.getTime());
        for (String revision : allRevisions) {
            IncrementalSnapshotInfo snapshotInfo = PersistenceHelper.convertRevision(revision);
            if (snapshotInfo.getTime() < baseTimeStamp &&
                    incrementalSnapshotInfo.getSiddhiAppId().equals(snapshotInfo.getSiddhiAppId()) &&
                    incrementalSnapshotInfo.getQueryName().equals(snapshotInfo.getQueryName()) &&
                    incrementalSnapshotInfo.getElementId().equals(snapshotInfo.getElementId())) {
                revisionsToClean.append(",").append(revision);
            }
        }
        if (revisionsToClean.length() != 0) {
            return revisionsToClean.substring(1); //removing leading comma
        }
        return null;
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
                    con.setAutoCommit(false);
                    stmt = con.createStatement();
                } catch (SQLException e) {
                    log.error("Cannot establish connection to datasource " + datasourceName +
                            " when checking persistence table exists", e);
                    return;
                }
                try (ResultSet ignored = stmt.executeQuery(executionInfo.getPreparedTableExistenceCheckStatement())) {
                    executionInfo.setTableExist(true);
                } catch (SQLException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Table " + tableName + " does not Exist. Table Will be created. ");
                    }
                    DBPersistenceStoreUtils.cleanupConnections(stmt, con);
                    try {
                        con = datasource.getConnection();
                        stmt = con.createStatement();
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
                DBPersistenceStoreUtils.cleanupConnections(stmt, con);
            }
        }
    }
}
