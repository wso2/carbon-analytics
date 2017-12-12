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

package org.wso2.carbon.data.provider.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class RDBMSTableTestUtils {

    private static final Logger log = Logger.getLogger(RDBMSTableTestUtils.class);
    private static String connectionUrlMysql = "jdbc:mysql://{{container.ip}}:{{container.port}}/dasdb";
    private static String connectionUrlPostgres = "jdbc:postgresql://{{container.ip}}:{{container.port}}/dasdb";
    private static String connectionUrlOracle = "jdbc:oracle:thin:@{{container.ip}}:{{container.port}}/XE";
    private static String connectionUrlMsSQL = "jdbc:sqlserver://{{container.ip}}:{{container.port}};" +
            "databaseName=tempdb";
    private static String connectionUrlDB2 = "jdbc:db2://{{container.ip}}:{{container.port}}/SAMPLE";
    private static final String CONNECTION_URL_H2 = "jdbc:h2:./target/dasdb";
    private static final String JDBC_DRIVER_CLASS_NAME_H2 = "org.h2.Driver";
    private static final String JDBC_DRIVER_CLASS_NAME_MYSQL = "com.mysql.jdbc.Driver";
    private static final String JDBC_DRIVER_CLASS_NAME_ORACLE = "oracle.jdbc.driver.OracleDriver";
    private static final String JDBC_DRIVER_CLASS_POSTGRES = "org.postgresql.Driver";
    private static final String JDBC_DRIVER_CLASS_MSSQL = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String JDBC_DRIVER_CLASS_DB2 = "com.ibm.db2.jcc.DB2Driver";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    public static String url = CONNECTION_URL_H2;
    public static String driverClassName = JDBC_DRIVER_CLASS_NAME_H2;
    public static String user = USERNAME;
    public static String password = PASSWORD;
    private static DataSource testDataSource;
    private static String tabelCreateQuery;
    private static String recordInsertQuery;

    private RDBMSTableTestUtils() {

    }

    public static DataSource getTestDataSource() {
        return getDataSource();
    }

    public static DataSource getDataSource() {
        if (testDataSource == null) {
            testDataSource = initDataSource();
        }
        return testDataSource;
    }

    private static DataSource initDataSource() {
        String databaseType = System.getenv("DATABASE_TYPE");
        if (databaseType == null) {
            databaseType = TestType.H2.toString();
        }
        TestType type = RDBMSTableTestUtils.TestType.valueOf(databaseType);
        user = System.getenv("DATABASE_USER");
        password = System.getenv("DATABASE_PASSWORD");
        String port = System.getenv("PORT");
        Properties connectionProperties = new Properties();
        switch (type) {
            case MySQL:
                url = connectionUrlMysql.replace("{{container.ip}}", getIpAddressOfContainer()).
                        replace("{{container.port}}", port);
                driverClassName = JDBC_DRIVER_CLASS_NAME_MYSQL;
                break;
            case H2:
                url = CONNECTION_URL_H2;
                driverClassName = JDBC_DRIVER_CLASS_NAME_H2;
                user = USERNAME;
                password = PASSWORD;
                tabelCreateQuery = "DROP TABLE IF EXISTS Foo_Table;" +
                        "CREATE TABLE IF NOT EXISTS Foo_Table (recipe_id INT NOT NULL," +
                        "recipe_name VARCHAR(30) NOT NULL,PRIMARY KEY (recipe_id), UNIQUE (recipe_name));" +
                        "INSERT INTO Foo_Table (recipe_id, recipe_name) VALUES (1,'Tacos'), (2,'Tomato Soup'), (3, " +
                        "'Grilled Cheese');";
                recordInsertQuery = "INSERT INTO Foo_Table (recipe_id, recipe_name) VALUES (?, ?)";
                break;
            case POSTGRES:
                url = connectionUrlPostgres.replace("{{container.ip}}", getIpAddressOfContainer()).
                        replace("{{container.port}}", port);
                driverClassName = JDBC_DRIVER_CLASS_POSTGRES;
                break;
            case ORACLE:
                url = connectionUrlOracle.replace("{{container.ip}}", getIpAddressOfContainer()).
                        replace("{{container.port}}", port);
                driverClassName = JDBC_DRIVER_CLASS_NAME_ORACLE;
                break;
            case MSSQL:
                url = connectionUrlMsSQL.replace("{{container.ip}}", getIpAddressOfContainer()).
                        replace("{{container.port}}", port);
                driverClassName = JDBC_DRIVER_CLASS_MSSQL;
                break;
            case DB2:
                url = connectionUrlDB2.replace("{{container.ip}}", getIpAddressOfContainer()).
                        replace("{{container.port}}", port);
                driverClassName = JDBC_DRIVER_CLASS_DB2;
                break;
        }
        log.info("URL of docker instance: " + url);
        connectionProperties.setProperty("jdbcUrl", url);
        connectionProperties.setProperty("driverClassName", driverClassName);
        connectionProperties.setProperty("dataSource.user", user);
        connectionProperties.setProperty("dataSource.password", password);
        connectionProperties.setProperty("poolName", "Test_Pool");
        HikariConfig config = new HikariConfig(connectionProperties);
        return new HikariDataSource(config);
    }

    public static void initDatabaseTable() throws SQLException {
        PreparedStatement stmt = null;
        Connection con = null;
        try {
            con = getTestDataSource().getConnection();
            stmt = con.prepareStatement(tabelCreateQuery);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.debug("Clearing DB table failed due to " + e.getMessage(), e);
        } finally {
            cleanupConnection(null, stmt, con);
        }
    }

    public static void insertRecords(int recipeId, String recipeName) throws SQLException {
        PreparedStatement stmt = null;
        Connection con = null;
        try {
            con = getTestDataSource().getConnection();
            stmt = con.prepareStatement(recordInsertQuery);
            stmt.setInt(1, recipeId);
            stmt.setString(2, recipeName);
            stmt.execute();
        } catch (SQLException e) {
            log.error("Getting rows in DB table failed due to " + e.getMessage(), e);
            throw e;
        } finally {
            cleanupConnection(null, stmt, con);
        }
    }

    public enum TestType {
        MySQL, H2, ORACLE, MSSQL, DB2, POSTGRES
    }

    /**
     * Utility for get Docker running host
     *
     * @return docker host
     * @throws URISyntaxException if docker Host url is malformed this will throw
     */
    public static String getIpAddressOfContainer() {
        String ip = System.getenv("DOCKER_HOST_IP");
        String dockerHost = System.getenv("DOCKER_HOST");
        if (!StringUtils.isEmpty(dockerHost)) {
            try {
                URI uri = new URI(dockerHost);
                ip = uri.getHost();
            } catch (URISyntaxException e) {
                log.error("Error while getting the docker Host url." + e.getMessage(), e);
            }
        }
        return ip;
    }

    /**
     * Method which can be used to clear up and ephemeral SQL connectivity artifacts.
     *
     * @param rs   {@link ResultSet} instance (can be null)
     * @param stmt {@link PreparedStatement} instance (can be null)
     * @param conn {@link Connection} instance (can be null)
     */
    public static void cleanupConnection(ResultSet rs, Statement stmt, Connection conn) {
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
}
