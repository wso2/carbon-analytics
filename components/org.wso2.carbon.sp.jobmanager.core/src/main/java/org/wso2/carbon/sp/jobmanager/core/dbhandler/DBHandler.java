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
import org.wso2.carbon.sp.jobmanager.core.exception.RDBMSTableException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * This class represents key database operations related to manager data.
 */

public class DBHandler {
    private static final Logger logger = LoggerFactory.getLogger(DBHandler.class);
    private static DBHandler instance = new DBHandler();

    private DBHandler() {

    }

    public static DBHandler getInstance() {
        return instance;
    }

    /**
     * Initialize the db connection.
     *
     * @throws RDBMSTableException data source cannot be found
     */
    Connection getConnection(HikariDataSource dataSource) {
        try {
            if (dataSource != null) {
                Connection conn = dataSource.getConnection();
                conn.setAutoCommit(true);
                return conn;
            } else {
                throw new RDBMSTableException("Datasource Could not be found. " +
                                                      "Hence cannot initialize the status dashboard.");
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Error initializing connection.", e);
        }
    }


    /**
     * Insert worker data worker db.
     *
     * @return isSuccess
     */
    boolean insert(PreparedStatement stmt) {
        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException("Error while inserting manager.", e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                //ignore
            }
        }
    }

    /**
     * Delete workers data to worker db.
     *
     * @return isSuccess.
     */
    public boolean delete(PreparedStatement stmt) {
        try {
            stmt.execute();
            stmt.close();
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException(" Error while processing the dDELETE operation.", e);
        }
    }

    /**
     * Select worker from the worker DB.
     *
     * @return list of object.
     */

    ResultSet select(PreparedStatement stmt) {
        ResultSet rs;
        try {
            rs = stmt.executeQuery();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table.'", e);
        }
        return rs;
    }

    /**
     * Create table query.
     *
     * @param conn
     * @param ps
     * @throws RDBMSTableException
     */
    public void createTable(Connection conn, PreparedStatement ps) throws RDBMSTableException {
        try {
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new RDBMSTableException("Unable to create table.", e);
        }
    }

    /**
     * Check the table is exsists.
     *
     * @return boolean isTableExists.
     */
    /**
     * Method for checking whether or not the given table (which reflects the current event table instance) exists.
     *
     * @return true/false based on the table existence.
     */
    public boolean isTableExist(Connection conn, String query) {
        try {
            try (PreparedStatement tableCheckstmt = conn.prepareStatement(query)) {
                try (ResultSet rs = tableCheckstmt.executeQuery()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Table  assumed to not exist since its existence check resulted "
                                     + "in exception ");
            }
            return false;
        }
    }
}
