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
import org.wso2.carbon.status.dashboard.core.dbhandler.exceptions.RDBMSTableException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * This class represents key database operations related to worker data.
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
            throw new RDBMSTableException("Error initializing connection: " + e.getMessage(), e);
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
            throw new RDBMSTableException("Attempted execution of query [" + stmt.toString() + "] produced an " +
                    "exceptions" +
                    " in " + stmt.toString(), e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
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
            throw new RDBMSTableException(e.getMessage() + " in " + stmt.toString(), e);
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
            throw new RDBMSTableException("Error retrieving records from table '" + stmt.toString() + "': "
                    + e.getMessage(), e);
        }
        return rs;
    }

    /**
     * Update workers in the worker db.
     *
     * @return new object that updated;
     */
    Object update(PreparedStatement stmt) {
        try {
            stmt.execute();
            stmt.close();
            return stmt;
        } catch (SQLException e) {
            throw new RDBMSTableException(e.getMessage() + " in " + stmt.toString(), e);
        }

    }

    /**
     * Check the table is exsists.
     *
     * @return boolean isTableExists.
     */
    public boolean isTableExist(PreparedStatement stmt) {
        try {
            stmt.executeQuery();
            return true;
        } catch (SQLException e) {
            throw new RDBMSTableException("Table '" + "this.tableName" + "' assumed to not exist since its" +
                    " existence check resulted " + "in exceptions " + e.getMessage() + " in " + stmt.toString(), e);
        }
    }

    /**
     * Closed db connection.
     */
    void cleanupConnection(Connection conn) throws RDBMSTableException {
        try {
            if ((conn != null) && (!conn.isClosed())) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error occurred while closing the connection", e);
                }
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Error occurred while connection closing ", e);
        }

    }

}
