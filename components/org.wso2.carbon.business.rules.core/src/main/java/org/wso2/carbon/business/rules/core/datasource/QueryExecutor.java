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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.datasource.util.BusinessRuleDatasourceUtils;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRulesDatasourceException;
import org.wso2.carbon.database.query.manager.QueryManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;


/**
 * Query Executor class
 * **/
public class QueryExecutor {
    private DataSource dataSource;
    private QueryGenerator queryGenerator;
    private QueryManager queryManager;
    private Logger log = LoggerFactory.getLogger(QueryExecutor.class);

    public QueryExecutor() {
        dataSource = DataSourceServiceProvider.getInstance().getDataSource();
        queryGenerator = new QueryGenerator();
        queryManager = DataHolder.getInstance().getQueryManager();
    }

    public boolean executeInsertQuery(String uuid, byte[] businessRule, int deploymentStatus){
        boolean result = false;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(true);
            statement = getInsertQuery(conn, uuid, businessRule, deploymentStatus);
            result = statement.execute();
            return result;
        } catch (SQLException e) {
            log.error("Inserting business rule " + new String(businessRule) + " is failed due to " + e.getMessage());
            return false;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public boolean executeDeleteQuery(String uuid) throws BusinessRulesDatasourceException {
        boolean result = false;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(true);
            statement = getDeleteQuery(conn, uuid);
            result = statement.execute();
            return result;
        } catch (SQLException e) {
            log.error("Deleting business rule with uuid '" + uuid + " is failed due to " + e.getMessage());
            return false;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public boolean executeUpdateBusinessRuleQuery(String uuid, byte[] newBusinessRule, int deploymentStatus) throws
            BusinessRulesDatasourceException {
        boolean result = false;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = getUpdateBusinessRuleQuery(conn, uuid, newBusinessRule, deploymentStatus);
            result = statement.execute();
            return result;
        } catch (SQLException e) {
            log.error("Updating business rule with uuid '" + uuid + " is failed due to " + e.getMessage());
            return false;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public boolean executeUpdateDeploymentStatusQuery(String uuid, int deploymentStatus) throws
            BusinessRulesDatasourceException {
        boolean result = false;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = getUpdateDeploymentStatus(conn, uuid, deploymentStatus);
            result = statement.execute();
            return  result;
        } catch (SQLException e) {
            log.error("Updating deployment status of the business rule to  with uuid '" + uuid + " is failed due to " + e.getMessage());
            return false;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public ResultSet executeRetrieveBusinessRule(String uuid) throws BusinessRulesDatasourceException {
        ResultSet resultSet = null;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = getRetrieveBusinessRule(conn, uuid);
            resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            log.error("Retrieving the business rule with uuid '" + uuid + "' from database is failed due to " + e.getMessage());
            return null;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public ResultSet executeRetrieveAllBusinessRules() throws BusinessRulesDatasourceException{
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            conn = dataSource.getConnection();
            statement = getRetrieveAllBusinessRules(conn);
            resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            log.error("Retrieving all the business rules from database is failed due to " + e.getMessage());
            return null;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    private PreparedStatement getInsertQuery(Connection conn, String businessRuleUUID, byte[] businessRule,
                                             int deploymentStatus) throws SQLException{
        PreparedStatement insertPreparedStatement;
        insertPreparedStatement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants.
                    ADD_BUSINESS_RULE));
        insertPreparedStatement.setString(1, businessRuleUUID);
        insertPreparedStatement.setBytes(2, businessRule);
        insertPreparedStatement.setInt(3, deploymentStatus);

        return insertPreparedStatement;
    }

    private PreparedStatement getDeleteQuery(Connection conn, String businessRuleUUID) throws SQLException {
        PreparedStatement deletePreparedStatement;
        deletePreparedStatement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants.
                DELETE_BUSINESS_RULE));
        deletePreparedStatement.setString(1, businessRuleUUID);
        return deletePreparedStatement;
    }

    private PreparedStatement getUpdateBusinessRuleQuery(Connection conn, String businessRuleUUID,
                                                         byte[] newBusinessRule, int deploymentStatus)
            throws SQLException {
        PreparedStatement updateBRPreparedStatement;
        updateBRPreparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.UPDATE_BUSINESS_RULE));
        updateBRPreparedStatement.setBytes(1, newBusinessRule);
        updateBRPreparedStatement.setInt(2, deploymentStatus);
        updateBRPreparedStatement.setString(3, businessRuleUUID);
        return updateBRPreparedStatement;
    }

    private PreparedStatement getUpdateDeploymentStatus(Connection conn, String businessRuleUUID, int deploymentStatus)
            throws SQLException {
        PreparedStatement updateBRPreparedStatement;
        updateBRPreparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.UPDATE_DEPLOYMENT_STATUS));
        updateBRPreparedStatement.setString(2, businessRuleUUID);
        updateBRPreparedStatement.setInt(1, deploymentStatus);
        return updateBRPreparedStatement;
    }

    private PreparedStatement getRetrieveBusinessRule(Connection conn, String businessRuleUUID)
            throws SQLException {
        PreparedStatement retrieveBRPreparedStatement;
        retrieveBRPreparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.RETRIEVE_BUSINESS_RULE));
        retrieveBRPreparedStatement.setString(1, businessRuleUUID);
        return retrieveBRPreparedStatement;
    }

    private PreparedStatement getRetrieveAllBusinessRules(Connection conn) throws SQLException {
        PreparedStatement getAllBRPreparedStatement;
        getAllBRPreparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.RETRIEVE_ALL));
        return getAllBRPreparedStatement;
    }
}
