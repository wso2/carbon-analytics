/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.streaming.integrator.core.siddhi.error.handler;

import io.siddhi.core.util.error.handler.exception.ErrorStoreException;
import io.siddhi.core.util.error.handler.model.ErrorEntry;
import io.siddhi.core.util.error.handler.store.ErrorStore;
import io.siddhi.core.util.error.handler.util.ErroneousEventType;
import io.siddhi.core.util.error.handler.util.ErrorOccurrence;
import io.siddhi.core.util.error.handler.util.ErrorType;
import org.apache.log4j.Logger;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.streaming.integrator.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.dto.RDBMSQueryConfigurationEntry;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.exception.DatabaseUnsupportedException;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.exception.DatasourceConfigurationException;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.util.DBErrorStoreUtils;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.util.ExecutionInfo;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.util.RDBMSConfiguration;
import org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.util.SiddhiErrorHandlerConstants;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Denotes an Error Store which stores erroneous events collected from Siddhi in a configured database.
 */
public class DBErrorStore extends ErrorStore {

    private static final Logger log = Logger.getLogger(DBErrorStore.class);
    private static final String POSTGRES_DATABASE_TYPE = "postgresql";
    private static final String MSSQL_DATABASE_TYPE = "microsoft sql server";
    private static final String ORACLE_DATABASE_TYPE = "oracle";

    private ExecutionInfo executionInfo;
    private DataSource datasource;
    private String datasourceName;
    private String tableName;
    private String databaseType;
    private String databaseVersion;

    @Override
    public void setProperties(Map properties) {
        Map configurationMap = (Map) properties.get(SiddhiErrorHandlerConstants.ERROR_STORE_CONFIGS);

        Object bufferSize = properties.get(SiddhiErrorHandlerConstants.BUFFER_SIZE);
        if (bufferSize instanceof Integer) {
            setBufferSize((Integer) bufferSize);
        }
        Object dropWhenBufferFull = properties.get(SiddhiErrorHandlerConstants.DROP_WHEN_BUFFER_FULL);
        if (dropWhenBufferFull instanceof Boolean) {
            setDropWhenBufferFull((Boolean) dropWhenBufferFull);
        }

        if (configurationMap != null) {
            Object datasourceObject = configurationMap.get("datasource");
            if (!(datasourceObject instanceof String)) {
                datasourceName = SiddhiErrorHandlerConstants.DEFAULT_DB_ERROR_STORE_DATASOURCE;
                if (log.isDebugEnabled()) {
                    log.debug("datasource for error storing is not set. Default datasource will be used.");
                }
            } else {
                datasourceName = String.valueOf(datasourceObject);
            }
            Object tableObject = configurationMap.get("table");
            if (!(tableObject instanceof String)) {
                tableName = SiddhiErrorHandlerConstants.DEFAULT_DB_ERROR_STORE_TABLE_NAME;
                if (log.isDebugEnabled()) {
                    log.debug("Table name for database error store is not set. Default table name will be used.");
                }
            } else {
                tableName = String.valueOf(tableObject);
            }
        } else {
            datasourceName = SiddhiErrorHandlerConstants.DEFAULT_DB_ERROR_STORE_DATASOURCE;
            tableName = SiddhiErrorHandlerConstants.DEFAULT_DB_ERROR_STORE_TABLE_NAME;
            if (log.isDebugEnabled()) {
                log.debug("Error store config is not set. Default config values will be used.");
            }
        }

        try {
            datasource = (DataSource) StreamProcessorDataHolder.getDataSourceService().
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
                " is not defined to use for error storing.", e);
        } catch (SQLException e) {
            throw new DatasourceConfigurationException("Connection cannot be established for datasource " +
                datasourceName, e);
        }

        initializeDatabaseExecutionInfo();
    }

    private void initializeDatabaseExecutionInfo() {
        executionInfo = new ExecutionInfo();
        RDBMSQueryConfigurationEntry databaseQueryEntries =
            RDBMSConfiguration.getInstance().getDatabaseQueryEntries(databaseType, databaseVersion, tableName);

        if (databaseQueryEntries == null) {
            throw new DatabaseUnsupportedException("The configured database type " +
                "is not supported with periodic persistence.");
        }
        executionInfo.setPreparedCreateTableStatement(databaseQueryEntries.getCreateTableQuery());
        executionInfo.setPreparedInsertStatement(databaseQueryEntries.getInsertTableQuery());
        executionInfo.setPreparedTableExistenceCheckStatement(databaseQueryEntries.getIsTableExistQuery());
        executionInfo.setPreparedSelectStatement(databaseQueryEntries.getSelectTableQuery());
        executionInfo.setPreparedMinimalSelectStatement(databaseQueryEntries.getMinimalSelectTableQuery());
        executionInfo.setPreparedSingleSelectStatement(databaseQueryEntries.getSingleSelectTableQuery());
        executionInfo.setPreparedSelectWithLimitOffsetStatement(databaseQueryEntries.getSelectWithLimitOffsetQuery());
        executionInfo.setPreparedMinimalSelectWithLimitOffsetStatement(
            databaseQueryEntries.getMinimalSelectWithLimitOffsetQuery());
        executionInfo.setPreparedSelectCountFromTableStatement(databaseQueryEntries.getSelectCountQuery());
        executionInfo.setPreparedDeleteStatement(databaseQueryEntries.getDeleteQuery());
    }

    @Override
    protected void saveEntry(long timestamp, String siddhiAppName, String streamName, byte[] eventAsBytes, String cause,
                             byte[] stackTraceAsBytes, byte[] originalPayloadAsBytes, String errorOccurrence,
                             String eventType, String errorType) throws ErrorStoreException {
        DBErrorStoreUtils.createTableIfNotExists(executionInfo, datasource, datasourceName, tableName);
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = datasource.getConnection();
            con.setAutoCommit(false);

            stmt = con.prepareStatement(executionInfo.getPreparedInsertStatement());
            stmt.setLong(1, timestamp);
            stmt.setString(2,siddhiAppName);
            stmt.setString(3,streamName);
            stmt.setString(5,cause);
            stmt.setString(8,errorOccurrence);
            stmt.setString(9,eventType);
            stmt.setString(10,errorType);

            if (databaseType.equals(POSTGRES_DATABASE_TYPE)) {
                stmt.setBlob(4, new SerialBlob(eventAsBytes));
                stmt.setBlob(6, new SerialBlob(stackTraceAsBytes));
                stmt.setBlob(7, new SerialBlob(originalPayloadAsBytes));
            } else if (con.getMetaData().getDriverName().contains("Oracle")) {
                InputStream eventInputStream = new ByteArrayInputStream(eventAsBytes);
                stmt.setBinaryStream(4, eventInputStream, eventAsBytes.length);

                InputStream stackTraceInputStream = new ByteArrayInputStream(stackTraceAsBytes);
                stmt.setBinaryStream(6, stackTraceInputStream, stackTraceAsBytes.length);

                InputStream originalPayloadInputStream = new ByteArrayInputStream(originalPayloadAsBytes);
                stmt.setBinaryStream(7, originalPayloadInputStream, originalPayloadAsBytes.length);
            } else {
                Blob eventBlob = con.createBlob();
                eventBlob.setBytes(1, eventAsBytes);
                stmt.setBlob(4, eventBlob);

                Blob stackTraceBlob = con.createBlob();
                stackTraceBlob.setBytes(1, stackTraceAsBytes);
                stmt.setBlob(6, stackTraceBlob);

                Blob originalPayloadBlob = con.createBlob();
                originalPayloadBlob.setBytes(1, originalPayloadAsBytes);
                stmt.setBlob(7, originalPayloadBlob);
            }

            stmt.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            log.error("Error while saving to " + datasourceName, e);
        } finally {
            DBErrorStoreUtils.cleanupConnections(stmt, con);
        }
    }

    @Override
    public List<ErrorEntry> loadErrorEntries(String siddhiAppName, Map<String, String> queryParams) {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = datasource.getConnection();
            con.setAutoCommit(false);
            boolean isDescriptive = false;
            if (queryParams.containsKey(SiddhiErrorHandlerConstants.DESCRIPTIVE) &&
                queryParams.get(SiddhiErrorHandlerConstants.DESCRIPTIVE) != null) {
                isDescriptive = Boolean.parseBoolean(queryParams.get(SiddhiErrorHandlerConstants.DESCRIPTIVE));
            }

            if (queryParams.containsKey(SiddhiErrorHandlerConstants.LIMIT) &&
                queryParams.containsKey(SiddhiErrorHandlerConstants.OFFSET) &&
                queryParams.get(SiddhiErrorHandlerConstants.LIMIT) != null &&
                queryParams.get(SiddhiErrorHandlerConstants.OFFSET) != null) {

                if (isDescriptive) {
                    stmt = con.prepareStatement(executionInfo.getPreparedSelectWithLimitOffsetStatement());
                } else {
                    stmt = con.prepareStatement(executionInfo.getPreparedMinimalSelectWithLimitOffsetStatement());
                }

                if (databaseType.equals(MSSQL_DATABASE_TYPE) || databaseType.equals(ORACLE_DATABASE_TYPE)) {
                    stmt.setInt(2, Integer.parseInt(queryParams.get(SiddhiErrorHandlerConstants.OFFSET)));
                    stmt.setInt(3, Integer.parseInt(queryParams.get(SiddhiErrorHandlerConstants.LIMIT)));
                } else {
                    stmt.setInt(2, Integer.parseInt(queryParams.get(SiddhiErrorHandlerConstants.LIMIT)));
                    stmt.setInt(3, Integer.parseInt(queryParams.get(SiddhiErrorHandlerConstants.OFFSET)));
                }
            } else if (isDescriptive) {
                stmt = con.prepareStatement(executionInfo.getPreparedSelectStatement());
            } else {
                stmt = con.prepareStatement(executionInfo.getPreparedMinimalSelectStatement());
            }
            stmt.setString(1, siddhiAppName);
            return getErrorEntries(isDescriptive, con, stmt);
        } catch (SQLException e) {
            log.error(String.format("Error while retrieving erroneous events of Siddhi app: %s from the datasource: %s",
                siddhiAppName, datasourceName), e);
            return Collections.emptyList();
        } finally {
            DBErrorStoreUtils.cleanupConnections(stmt, con);
        }
    }

    @Override
    public ErrorEntry loadErrorEntry(int id) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = datasource.getConnection();
            con.setAutoCommit(false);
            stmt = con.prepareStatement(executionInfo.getPreparedSingleSelectStatement());
            stmt.setInt(1, id);
            // Uses the same method without re-inventing the wheel.
            List<ErrorEntry> errorEntry = getErrorEntries(true, con, stmt);
            if (!errorEntry.isEmpty()) {
                return errorEntry.get(0);
            }
            return null;
        } catch (SQLException e) {
            log.error(String.format("Error while retrieving erroneous events with id: %s from the datasource: %s",
                id, datasourceName), e);
            return null;
        } finally {
            DBErrorStoreUtils.cleanupConnections(stmt, con);
        }
    }

    private List<ErrorEntry> getErrorEntries(boolean isDescriptive, Connection con, PreparedStatement stmt)
        throws SQLException {
        List<ErrorEntry> errorEntries = new ArrayList<>();
        try (ResultSet resultSet = stmt.executeQuery()) {
            con.commit();
            while (resultSet.next()) {
                Blob blobEvent;
                Blob blobStackTrace;
                Blob blobOriginalPayload;
                byte[] blobEventAsBytes = null;
                byte[] blobStackTraceAsBytes = null;
                byte[] blobOriginalPayloadAsBytes = null;

                if (isDescriptive) {
                    if (databaseType.equals(MSSQL_DATABASE_TYPE)) {
                        blobEvent = new SerialBlob(resultSet.getBytes(SiddhiErrorHandlerConstants.EVENT));
                        blobStackTrace = new SerialBlob(resultSet.getBytes(SiddhiErrorHandlerConstants.STACK_TRACE));
                        blobOriginalPayload = new SerialBlob(
                            resultSet.getBytes(SiddhiErrorHandlerConstants.ORIGINAL_PAYLOAD));
                    } else {
                        blobEvent = resultSet.getBlob(SiddhiErrorHandlerConstants.EVENT);
                        blobStackTrace = resultSet.getBlob(SiddhiErrorHandlerConstants.STACK_TRACE);
                        blobOriginalPayload = resultSet.getBlob(SiddhiErrorHandlerConstants.ORIGINAL_PAYLOAD);
                    }
                    int blobEventLength = (int) blobEvent.length();
                    blobEventAsBytes = blobEvent.getBytes(1, blobEventLength);

                    int blobStackTraceLength = (int) blobStackTrace.length();
                    blobStackTraceAsBytes = blobStackTrace.getBytes(1, blobStackTraceLength);

                    int blobOriginalPayloadLength = (int) blobOriginalPayload.length();
                    blobOriginalPayloadAsBytes = blobOriginalPayload.getBytes(1, blobOriginalPayloadLength);
                }

                try {
                    ErrorEntry errorEntry = constructErrorEntry(
                        resultSet.getInt(SiddhiErrorHandlerConstants.ID),
                        resultSet.getLong(SiddhiErrorHandlerConstants.TIMESTAMP),
                        resultSet.getString(SiddhiErrorHandlerConstants.SIDDHI_APP_NAME),
                        resultSet.getString(SiddhiErrorHandlerConstants.STREAM_NAME),
                        blobEventAsBytes,
                        resultSet.getString(SiddhiErrorHandlerConstants.CAUSE),
                        blobStackTraceAsBytes,
                        blobOriginalPayloadAsBytes,
                        ErrorOccurrence.valueOf(resultSet.getString(SiddhiErrorHandlerConstants.ERROR_OCCURRENCE)),
                        ErroneousEventType.valueOf(resultSet.getString(SiddhiErrorHandlerConstants.EVENT_TYPE)),
                        ErrorType.valueOf(resultSet.getString(SiddhiErrorHandlerConstants.ERROR_TYPE)));
                    errorEntries.add(errorEntry);
                } catch (IOException | ClassNotFoundException e) {
                    log.error("Failed to convert error entry. Hence, skipping the entry.", e);
                }
            }
        }
        return errorEntries;
    }

    @Override
    public void discardErroneousEvent(int id) {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = datasource.getConnection();
            con.setAutoCommit(false);
            stmt = con.prepareStatement(executionInfo.getPreparedDeleteStatement());
            stmt.setInt(1, id);
            stmt.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            log.error(String.format("Failed to delete event entry with id: %s.", id), e);
        } finally {
            DBErrorStoreUtils.cleanupConnections(stmt, con);
        }
    }

    @Override
    public Map<String, String> getStatus() {
        Map<String, String> status = new HashMap<>();
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = datasource.getConnection();
            con.setAutoCommit(false);
            stmt = con.prepareStatement(executionInfo.getPreparedSelectCountFromTableStatement());
            try (ResultSet resultSet = stmt.executeQuery()) {
                con.commit();
                while (resultSet.next()) {
                    status.put(SiddhiErrorHandlerConstants.ENTRIES_COUNT,
                        String.valueOf(resultSet.getInt(SiddhiErrorHandlerConstants.ENTRIES_COUNT)));
                }
            }
        } catch (SQLException e) {
            log.error(String.format("Error while retrieving status of the datasource: %s", datasourceName), e);
        } finally {
            DBErrorStoreUtils.cleanupConnections(stmt, con);
        }
        return status;
    }

}
