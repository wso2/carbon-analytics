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

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QueryGenerator{
    private static QueryManager queryManager = DataHolder.getInstance().getQueryManager();

    public PreparedStatement getInsertQuery(Connection conn, String businessRuleUUID, Blob businessRule,
                                            int deploymentStatus) throws BusinessRulesDatasourceException {
        PreparedStatement insertPreparedStatement;
        try {
            insertPreparedStatement =  conn.prepareStatement(queryManager.getQuery(DatasourceConstants.
                    ADD_BUSINESS_RULE));
            insertPreparedStatement.setString(1, businessRuleUUID);
            insertPreparedStatement.setBlob(2, businessRule);
            insertPreparedStatement.setInt(3, deploymentStatus);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return insertPreparedStatement;
    }

    public PreparedStatement getDeleteQuery(Connection conn, String businessRuleUUID)
            throws BusinessRulesDatasourceException {
        PreparedStatement deletePreparedStatement;
        try {
            deletePreparedStatement =  conn.prepareStatement(queryManager.getQuery(DatasourceConstants.DELETE_BUSINESS_RULE));
            deletePreparedStatement.setString(1, businessRuleUUID);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return deletePreparedStatement;
    }

    public PreparedStatement getUpdateBusinessRuleQuery(Connection conn, String businessRuleUUID,
                                                        Blob newBusinessRule, int deploymentStatus)
            throws BusinessRulesDatasourceException {
        PreparedStatement updateBRPreparedStatement;
        try {
            updateBRPreparedStatement =  conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.UPDATE_BUSINESS_RULE));
            updateBRPreparedStatement.setString(1, businessRuleUUID);
            updateBRPreparedStatement.setBlob(2, newBusinessRule);
            updateBRPreparedStatement.setInt(3, deploymentStatus);

        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return updateBRPreparedStatement;
    }

    public PreparedStatement getUpdateDeploymentStatus(Connection conn, String businessRuleUUID, int deploymentStatus)
            throws BusinessRulesDatasourceException {
        PreparedStatement updateBRPreparedStatement;
        try {
            updateBRPreparedStatement =  conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.UPDATE_DEPLOYMENT_STATUS));
            updateBRPreparedStatement.setString(1, businessRuleUUID);
            updateBRPreparedStatement.setInt(2, deploymentStatus);

        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return updateBRPreparedStatement;
    }

    public PreparedStatement getRetrieveBusinessRule(Connection conn, String businessRuleUUID)
            throws BusinessRulesDatasourceException {
        PreparedStatement updateBRPreparedStatement;
        try {
            updateBRPreparedStatement =  conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.RETRIEVE_BUSINESS_RULE));
            updateBRPreparedStatement.setString(1, businessRuleUUID);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return updateBRPreparedStatement;
    }
}
