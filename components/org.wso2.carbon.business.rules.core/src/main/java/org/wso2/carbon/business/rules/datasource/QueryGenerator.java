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
package org.wso2.carbon.business.rules.datasource;

import org.wso2.carbon.business.rules.core.internal.exceptions.BusinessRulesDatasourceException;
import org.wso2.carbon.business.rules.datasource.api.QueryProvider;
import org.wso2.carbon.database.query.manager.QueryManager;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

public class QueryGenerator implements QueryProvider{
    private static QueryManager queryManager = DataHolder.getInstance().getQueryManager();

    @Override
    public PreparedStatement getInsertQuery(DataSource dataSource,
                                            String businessRuleUUID, Blob businessRule, boolean deploymentStatus) throws BusinessRulesDatasourceException {
        PreparedStatement insertPreparedStatement;
        try {
            Connection conn = dataSource.getConnection();
            insertPreparedStatement =  conn.prepareStatement(queryManager.getQuery(DatasourceConstants.INSERT));
            insertPreparedStatement.setString(1, businessRuleUUID);
            insertPreparedStatement.setBlob(2, businessRule);
            insertPreparedStatement.setBoolean(3, deploymentStatus);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return insertPreparedStatement;
    }

    @Override
    public PreparedStatement getDeleteQuery(DataSource dataSource, String businessRuleUUID)
            throws BusinessRulesDatasourceException {
        PreparedStatement deletePreparedStatement;
        try {
            Connection conn = dataSource.getConnection();
            deletePreparedStatement =  conn.prepareStatement(queryManager.getQuery(DatasourceConstants.DELETE));
            deletePreparedStatement.setString(1, businessRuleUUID);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return deletePreparedStatement;
    }

    @Override
    public PreparedStatement getUpdateBusinessRuleQuery(DataSource dataSource, String businessRuleUUID,
                                                        Blob newBusinessRule, boolean deploymentStatus) throws BusinessRulesDatasourceException {
        PreparedStatement updateBRPreparedStatement;
        try {
            Connection conn = dataSource.getConnection();
            updateBRPreparedStatement =  conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.UPDATE_BUSINESS_RULE));
            updateBRPreparedStatement.setString(1, businessRuleUUID);
            updateBRPreparedStatement.setBlob(2, newBusinessRule);
            updateBRPreparedStatement.setBoolean(3, deploymentStatus);

        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return updateBRPreparedStatement;
    }

    @Override
    public PreparedStatement getUpdateDeploymentStatus(DataSource dataSource,
                                                       String businessRuleUUID, boolean deploymentStatus) throws BusinessRulesDatasourceException {
        PreparedStatement updateBRPreparedStatement;
        try {
            Connection conn = dataSource.getConnection();
            updateBRPreparedStatement =  conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.UPDATE_DEPLOYMENT_STATUS));
            updateBRPreparedStatement.setString(1, businessRuleUUID);
            updateBRPreparedStatement.setBoolean(2, deploymentStatus);

        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Unable to connect to the datasource due to " + e.getMessage(),
                    e);
        }
        return updateBRPreparedStatement;
    }

    @Override
    public PreparedStatement getRetrieveBusinessRule(DataSource dataSource, String businessRuleUUID) throws BusinessRulesDatasourceException {
        PreparedStatement updateBRPreparedStatement;
        try {
            Connection conn = dataSource.getConnection();
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
