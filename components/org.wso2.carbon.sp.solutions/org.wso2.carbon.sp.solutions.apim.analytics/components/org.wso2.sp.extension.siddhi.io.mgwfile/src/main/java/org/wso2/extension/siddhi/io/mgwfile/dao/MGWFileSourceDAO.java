/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.io.mgwfile.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.extension.siddhi.io.mgwfile.MGWFileSourceConstants;
import org.wso2.extension.siddhi.io.mgwfile.dto.MGWFileInfoDTO;
import org.wso2.extension.siddhi.io.mgwfile.exception.MGWFileSourceException;
import org.wso2.extension.siddhi.io.mgwfile.util.MGWFileSourceDBUtil;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class contains methods DB access for FileEventAdapter
 */
public class MGWFileSourceDAO {

    private static final Log log = LogFactory.getLog(MGWFileSourceDAO.class);

    /**
     * Adds a record into the database with uploaded file's information
     *
     * @param dto                 Uploaded File Information represented by {@link MGWFileInfoDTO}
     * @param uploadedInputStream Input stream with the uploaded file content
     * @throws MGWFileSourceException if there is an error while getting a connection or executing the query
     */
    public static void persistUploadedFile(MGWFileInfoDTO dto, InputStream uploadedInputStream)
            throws MGWFileSourceException {
        Connection connection = null;
        boolean autoCommitStatus = false;
        PreparedStatement statement = null;
        try {
            connection = MGWFileSourceDBUtil.getConnection();
            autoCommitStatus = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(MGWFileSourceConstants.INSERT_UPLOADED_FILE_INFO_QUERY);
            statement.setString(1, dto.getFileName());
            statement.setTimestamp(2, new Timestamp(dto.getTimeStamp()));
            statement.setBinaryStream(3, uploadedInputStream);
            statement.executeUpdate();
            connection.commit();
            if (log.isDebugEnabled()) {
                log.debug("Persisted Uploaded File info : " + dto.toString());
            }
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                log.error("Error occurred while rolling back inserting uploaded information into db transaction,", e1);
            }
            throw new MGWFileSourceException("Error occurred while inserting uploaded information into database", e);
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(autoCommitStatus);
                }
            } catch (SQLException e) {
                log.warn("Failed to reset auto commit state of database connection to the previous state.", e);
            }
            MGWFileSourceDBUtil.closeAllConnections(statement, connection, null);
        }
    }

    /**
     * Returns the next set of files to bre processed by the worker threads.
     *
     * @param limit number of records to be retrieved
     * @return list of {@link MGWFileInfoDTO}
     * @throws MGWFileSourceException if there is an error while getting a connection or executing the query
     */
    public static List<MGWFileInfoDTO> getNextFilesToProcess(int limit) throws MGWFileSourceException {
        Connection connection = null;
        PreparedStatement selectStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet resultSet = null;
        boolean autoCommitStatus = false;
        List<MGWFileInfoDTO> usageFileList = new ArrayList<>();
        try {
            connection = MGWFileSourceDBUtil.getConnection();
            autoCommitStatus = connection.getAutoCommit();
            connection.setAutoCommit(false);
            if ((connection.getMetaData().getDriverName()).contains("Oracle")) {
                selectStatement = connection
                        .prepareStatement(MGWFileSourceConstants.GET_NEXT_FILES_TO_PROCESS_QUERY_ORACLE);
            } else if (connection.getMetaData().getDatabaseProductName().contains("Microsoft")) {
                selectStatement = connection
                        .prepareStatement(MGWFileSourceConstants.GET_NEXT_FILES_TO_PROCESS_QUERY_MSSQL);
            } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                selectStatement = connection
                        .prepareStatement(MGWFileSourceConstants.GET_NEXT_FILES_TO_PROCESS_QUERY_DB2);
            } else {
                selectStatement = connection
                        .prepareStatement(MGWFileSourceConstants.GET_NEXT_FILES_TO_PROCESS_QUERY_DEFAULT);
            }
            selectStatement.setInt(1, limit);
            resultSet = selectStatement.executeQuery();
            while (resultSet.next()) {
                String fileName = resultSet.getString("FILE_NAME");
                long timeStamp = resultSet.getTimestamp("FILE_TIMESTAMP").getTime();
                updateStatement = connection
                        .prepareStatement(MGWFileSourceConstants.UPDATE_FILE_PROCESSING_STARTED_STATUS);
                updateStatement.setString(1, fileName);
                updateStatement.executeUpdate();
                //File content (Blob) is not stored in memory. Will retrieve one by one when processing.
                MGWFileInfoDTO dto = new MGWFileInfoDTO(fileName, timeStamp);
                usageFileList.add(dto);
                if (log.isDebugEnabled()) {
                    log.debug("Added File to list : " + dto.toString());
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                log.error("Error occurred while rolling back getting the next files to process transaction.", e1);
            }
            throw new MGWFileSourceException("Error occurred while getting the next files to process.", e);
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(autoCommitStatus);
                }
            } catch (SQLException e) {
                log.warn("Failed to reset auto commit state of database connection to the previous state.", e);
            }
            MGWFileSourceDBUtil.closeStatement(updateStatement);
            MGWFileSourceDBUtil.closeAllConnections(selectStatement, connection, resultSet);
        }
        return usageFileList;
    }

    /**
     * Updates the completion of processing a uploaded usage file
     *
     * @param dto Processed file represented by {@link MGWFileInfoDTO}
     * @throws MGWFileSourceException if there is an error while getting a connection or executing the query
     */
    public static void updateCompletion(MGWFileInfoDTO dto) throws MGWFileSourceException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = MGWFileSourceDBUtil.getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(MGWFileSourceConstants.UPDATE_COMPETITION_QUERY);
            statement.setString(1, dto.getFileName());
            statement.executeUpdate();
            connection.commit();
            if (log.isDebugEnabled()) {
                log.debug("Updated completion for file : " + dto.toString());
            }
        } catch (SQLException e) {
            throw new MGWFileSourceException("Error occurred while updating the completion state.", e);
        } finally {
            MGWFileSourceDBUtil.closeAllConnections(statement, connection, null);
        }
    }

    /**
     * Get the content of the file based on the file information
     *
     * @param dto Processed file represented by {@link MGWFileInfoDTO}
     * @return InputStream with the content of the file of null if there is no content
     * @throws MGWFileSourceException
     */
    public static InputStream getFileContent(MGWFileInfoDTO dto) throws MGWFileSourceException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        InputStream fileContentInputStream = null;
        try {
            connection = MGWFileSourceDBUtil.getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(MGWFileSourceConstants.GET_UPLOADED_FILE_CONTENT_QUERY);
            statement.setString(1, dto.getFileName());
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                //Postgres bytea data doesn't support getBlob operation
                if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                    fileContentInputStream = resultSet.getBinaryStream(MGWFileSourceConstants.API_USAGE_FILE_CONTENT);
                } else {
                    Blob content = resultSet.getBlob(MGWFileSourceConstants.API_USAGE_FILE_CONTENT);
                    fileContentInputStream = content.getBinaryStream();
                }
                if (log.isDebugEnabled()) {
                    log.debug("Added File to list : " + dto.toString());
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Retrieved content of file : " + dto.toString());
            }
        } catch (SQLException e) {
            throw new MGWFileSourceException(
                    "Error occurred while retrieving the content of the file: " + dto.toString(), e);
        } finally {
            MGWFileSourceDBUtil.closeAllConnections(statement, connection, resultSet);
        }
        return fileContentInputStream;
    }

    /**
     * Delete obsolete usage records in the dbG
     *
     * @param lastKeptDate up to which files should be retained
     * @throws MGWFileSourceException
     */
    public static void deleteProcessedOldFiles(Date lastKeptDate) throws MGWFileSourceException {
        Connection connection = null;
        PreparedStatement delStatement = null;
        boolean autoCommitStatus = false;
        try {
            connection = MGWFileSourceDBUtil.getConnection();
            autoCommitStatus = connection.getAutoCommit();
            connection.setAutoCommit(false);
            delStatement = connection.prepareStatement(MGWFileSourceConstants.DELETE_OLD_UPLOAD_COMPLETED_FILES);
            delStatement.setTimestamp(1, new Timestamp(lastKeptDate.getTime()));
            delStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                log.error("Error occurred while rolling back deleting old uploaded files transaction.", e1);
            }
            throw new MGWFileSourceException("Error occurred while deleting old uploaded files.", e);
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(autoCommitStatus);
                }
            } catch (SQLException e) {
                log.warn("Failed to reset auto commit state of database connection to the previous state.", e);
            }
            MGWFileSourceDBUtil.closeAllConnections(delStatement, connection, null);
        }
    }

}
