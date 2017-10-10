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
package org.wso2.carbon.business.rules.core.datasource;

import org.wso2.carbon.business.rules.core.exceptions.BusinessRulesDatasourceException;
import org.wso2.carbon.database.query.manager.QueryManager;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;


/**
 * Query Executor class
 * **/
public class QueryExecutor {
    private DataSource dataSource;
    private QueryGenerator queryGenerator;
    private QueryManager queryManager;

    public QueryExecutor() {
        dataSource = DataSourceServiceProvider.getInstance().getDataSource();
        queryGenerator = new QueryGenerator();
        queryManager = DataHolder.getInstance().getQueryManager();
    }

    public int executeInsertQuery(String uuid, byte[] businessRule, int deploymentStatus) throws
            BusinessRulesDatasourceException, SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(true);
        Blob businessRuleBlob= conn.createBlob();
        businessRuleBlob.setBytes(1,businessRule);
        PreparedStatement statement = getInsertQuery(conn, uuid, businessRuleBlob, deploymentStatus);
        return statement.executeUpdate();
    }

    public boolean executeDeleteQuery(String uuid) throws SQLException, BusinessRulesDatasourceException {
        Connection conn = dataSource.getConnection();
        PreparedStatement statement = getDeleteQuery(conn, uuid);
        return statement.execute();
    }

    public boolean executeUpdateBusinessRuleQuery(String uuid, byte[] newBusinessRule, int deploymentStatus) throws
            SQLException, BusinessRulesDatasourceException {
        Connection conn = dataSource.getConnection();
        Blob newBusinessRuleBlob = conn.createBlob();
        newBusinessRuleBlob.setBytes(1, newBusinessRule);
        PreparedStatement statement = getUpdateBusinessRuleQuery(conn, uuid, newBusinessRuleBlob, deploymentStatus);
        return statement.execute();
    }

    public boolean executeUpdateDeploymentStatusQuery(String uuid, int deploymentStatus) throws SQLException,
            BusinessRulesDatasourceException {
        Connection conn = dataSource.getConnection();
        PreparedStatement statement = getUpdateDeploymentStatus(conn, uuid, deploymentStatus);
        return statement.execute();
    }

    public ResultSet executeRetrieveBusinessRule(String uuid) throws BusinessRulesDatasourceException, SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement statement = getRetrieveBusinessRule(conn, uuid);
        return statement.executeQuery();
    }

    public ResultSet executeRetrieveAllBusinessRules() throws BusinessRulesDatasourceException, SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement statement = getRetrieveAllBusinessRules(conn);
        return statement.executeQuery();
    }

    private PreparedStatement getInsertQuery(Connection conn, String businessRuleUUID, Blob businessRule,
                                             int deploymentStatus) throws BusinessRulesDatasourceException {
        PreparedStatement insertPreparedStatement;
        try {
            insertPreparedStatement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants.
                    ADD_BUSINESS_RULE));
            insertPreparedStatement.setString(1, businessRuleUUID);
            insertPreparedStatement.setString(2, businessRule.toString());
            insertPreparedStatement.setInt(3, deploymentStatus);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return insertPreparedStatement;
    }

    private PreparedStatement getDeleteQuery(Connection conn, String businessRuleUUID)
            throws BusinessRulesDatasourceException {
        PreparedStatement deletePreparedStatement;
        try {
            deletePreparedStatement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants.
                    DELETE_BUSINESS_RULE));
            deletePreparedStatement.setString(1, businessRuleUUID);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return deletePreparedStatement;
    }

    private PreparedStatement getUpdateBusinessRuleQuery(Connection conn, String businessRuleUUID,
                                                         Blob newBusinessRule, int deploymentStatus)
            throws BusinessRulesDatasourceException {
        PreparedStatement updateBRPreparedStatement;
        try {
            updateBRPreparedStatement = conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.UPDATE_BUSINESS_RULE));
            updateBRPreparedStatement.setBlob(1, newBusinessRule);
            updateBRPreparedStatement.setInt(2, deploymentStatus);
            updateBRPreparedStatement.setString(3, businessRuleUUID);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return updateBRPreparedStatement;
    }

    private PreparedStatement getUpdateDeploymentStatus(Connection conn, String businessRuleUUID, int deploymentStatus)
            throws BusinessRulesDatasourceException {
        PreparedStatement updateBRPreparedStatement;
        try {
            updateBRPreparedStatement = conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.UPDATE_DEPLOYMENT_STATUS));
            updateBRPreparedStatement.setString(2, businessRuleUUID);
            updateBRPreparedStatement.setInt(1, deploymentStatus);

        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return updateBRPreparedStatement;
    }

    private PreparedStatement getRetrieveBusinessRule(Connection conn, String businessRuleUUID)
            throws BusinessRulesDatasourceException {
        PreparedStatement retrieveBRPreparedStatement;
        try {
            retrieveBRPreparedStatement = conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.RETRIEVE_BUSINESS_RULE));
            retrieveBRPreparedStatement.setString(1, businessRuleUUID);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return retrieveBRPreparedStatement;
    }

    private PreparedStatement getRetrieveAllBusinessRules(Connection conn) throws BusinessRulesDatasourceException {
        PreparedStatement getAllBRPreparedStatement;
        try {
            getAllBRPreparedStatement = conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.RETRIEVE_ALL));
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return getAllBRPreparedStatement;
    }
}
