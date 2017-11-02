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

import org.wso2.carbon.business.rules.core.bean.BusinessRule;
import org.wso2.carbon.business.rules.core.bean.scratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.bean.scratch.BusinessRuleFromScratchProperty;
import org.wso2.carbon.business.rules.core.bean.template.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.datasource.util.BusinessRuleDatasourceUtils;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRulesDatasourceException;
import org.wso2.carbon.database.query.manager.QueryManager;

import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * QueryExecutor for executing queries on business rules database
 */
public class QueryExecutor {
    private DataSource dataSource;
    private QueryManager queryManager;
    private Gson gson;

    public QueryExecutor() {
        dataSource = DataSourceServiceProvider.getInstance().getDataSource();
        queryManager = DataHolder.getInstance().getQueryManager();
        gson = new Gson();
    }

    public boolean executeInsertQuery(String uuid, byte[] businessRule, int deploymentStatus, int artifactCount)
            throws BusinessRulesDatasourceException {
        boolean result;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = getStatementForInsertingBusinessRule(conn, uuid, businessRule, deploymentStatus, artifactCount);
            if (statement == null) {
                return false;
            }
            result = statement.execute();
            conn.commit();
            return result;
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Inserting business rule with uuid '" + uuid + "' is failed. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public boolean executeDeleteQuery(String uuid) throws BusinessRulesDatasourceException {
        boolean result;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = getStatementForDeletingBusinessRule(conn, uuid);
            if (statement == null) {
                return false;
            }
            result = statement.execute();
            conn.commit();
            return result;
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Deleting business rule with uuid '" + uuid +
                    " is failed. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public boolean executeUpdateBusinessRuleQuery(String uuid, byte[] newBusinessRule, int deploymentStatus) throws
            BusinessRulesDatasourceException {
        boolean result;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = getStatementForUpdatingBusinessRule(conn, uuid, newBusinessRule, deploymentStatus);
            if (statement == null) {
                return false;
            }
            result = statement.execute();
            conn.commit();
            return result;
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Updating business rule with uuid '" + uuid +
                    " is failed. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public boolean executeUpdateDeploymentStatusQuery(String uuid, int deploymentStatus)
            throws BusinessRulesDatasourceException {
        boolean result;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = getStatementForUpdatingDeploymentStatus(conn, uuid, deploymentStatus);
            if (statement == null) {
                return false;
            }
            result = statement.execute();
            conn.commit();
            return result;
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Updating deployment status of the business rule with uuid '" +
                    uuid + " is failed due to. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public BusinessRule retrieveBusinessRule(String uuid) throws BusinessRulesDatasourceException {
        ResultSet resultSet;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = getStatementForRetrievingBusinessRule(conn, uuid);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Blob blob = resultSet.getBlob(2);
                byte[] bdata = blob.getBytes(1, (int) blob.length());

                JsonObject jsonObject = gson.fromJson(new String(bdata, Charset.forName("UTF-8")), JsonObject
                        .class)
                        .getAsJsonObject();

                String name = jsonObject.get("name").getAsString();
                String templateGroupUUID = jsonObject.get("templateGroupUUID").getAsString();
                String type = jsonObject.get("type").getAsString();
                BusinessRule businessRule;
                if ("scratch".equalsIgnoreCase(type)) {
                    String inputRuleTemplateUUID = jsonObject.get("inputRuleTemplateUUID").getAsString();
                    String outputRuleTemplateUUID = jsonObject.get("outputRuleTemplateUUID").getAsString();
                    BusinessRuleFromScratchProperty properties = gson.fromJson(jsonObject.get("properties"),
                            BusinessRuleFromScratchProperty.class);
                    businessRule = new BusinessRuleFromScratch(uuid, name, templateGroupUUID, type,
                            inputRuleTemplateUUID, outputRuleTemplateUUID, properties);
                    return businessRule;
                } else if ("template".equalsIgnoreCase(type)) {
                    String ruleTemplateUUID = jsonObject.get("ruleTemplateUUID").getAsString();
                    Map<String, String> properties = gson.fromJson(jsonObject.get("properties"), HashMap.class);
                    businessRule = new BusinessRuleFromTemplate(uuid, name, templateGroupUUID, type,
                            ruleTemplateUUID, properties);
                    return businessRule;
                }
            }
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Retrieving the business rule with uuid '" + uuid +
                    "' from database is failed due to. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
        return null;
    }

    public Map<String, BusinessRule> executeRetrieveAllBusinessRules() throws BusinessRulesDatasourceException {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet;
        Map<String, BusinessRule> map = new HashMap<>();
        try {
            conn = dataSource.getConnection();
            statement = getStatementForRetrievingAllBusinessRules(conn);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String businessRuleUUID = resultSet.getString(1);
                Blob blob = resultSet.getBlob(2);
                byte[] bdata = blob.getBytes(1, (int) blob.length());

                JsonObject jsonObject = gson.fromJson(new String(bdata, Charset.forName("UTF-8")),
                        JsonObject.class).getAsJsonObject();

                String uuid = jsonObject.get("uuid").getAsString();
                String name = jsonObject.get("name").getAsString();
                String templateGroupUUID = jsonObject.get("templateGroupUUID").getAsString();
                String type = jsonObject.get("type").getAsString();

                if ("scratch".equalsIgnoreCase(type)) {
                    String inputRuleTemplateUUID = jsonObject.get("inputRuleTemplateUUID").getAsString();
                    String outputRuleTemplateUUID = jsonObject.get("outputRuleTemplateUUID").getAsString();
                    BusinessRuleFromScratchProperty properties = gson.fromJson(jsonObject.get("properties"),
                            BusinessRuleFromScratchProperty.class);
                    BusinessRule businessRule = new BusinessRuleFromScratch(uuid, name, templateGroupUUID, type,
                            inputRuleTemplateUUID, outputRuleTemplateUUID, properties);
                    map.put(businessRuleUUID, businessRule);
                } else if ("template".equalsIgnoreCase(type)) {
                    String ruleTemplateUUID = jsonObject.get("ruleTemplateUUID").getAsString();
                    Map<String, String> properties = gson.fromJson(jsonObject.get("properties"), HashMap.class);
                    BusinessRule businessRule = new BusinessRuleFromTemplate(uuid, name, templateGroupUUID, type,
                            ruleTemplateUUID, properties);
                    map.put(businessRuleUUID, businessRule);
                }
            }
            return map;
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Retrieving all the business rules from database is failed " +
                    "due to. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public List<Object[]> executeRetrieveAllBusinessRulesWithStatus() throws BusinessRulesDatasourceException {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet;
        List<Object[]> list = new ArrayList<>();
        try {
            conn = dataSource.getConnection();
            statement = getStatementForRetrievingAllBusinessRules(conn);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Integer deploymentStatus = resultSet.getInt(3);
                Blob blob = resultSet.getBlob(2);
                byte[] bdata = blob.getBytes(1, (int) blob.length());

                JsonObject jsonObject = gson.fromJson(new String(bdata, Charset.forName("UTF-8")),
                        JsonObject.class).getAsJsonObject();

                String uuid = jsonObject.get("uuid").getAsString();
                String name = jsonObject.get("name").getAsString();
                String templateGroupUUID = jsonObject.get("templateGroupUUID").getAsString();
                String type = jsonObject.get("type").getAsString();

                if ("scratch".equalsIgnoreCase(type)) {
                    String inputRuleTemplateUUID = jsonObject.get("inputRuleTemplateUUID").getAsString();
                    String outputRuleTemplateUUID = jsonObject.get("outputRuleTemplateUUID").getAsString();
                    BusinessRuleFromScratchProperty properties = gson.fromJson(jsonObject.get("properties"),
                            BusinessRuleFromScratchProperty.class);
                    BusinessRule businessRule = new BusinessRuleFromScratch(uuid, name, templateGroupUUID, type,
                            inputRuleTemplateUUID, outputRuleTemplateUUID, properties);
                    Object[] objects = {businessRule, deploymentStatus};
                    list.add(objects);
                } else if ("template".equalsIgnoreCase(type)) {
                    String ruleTemplateUUID = jsonObject.get("ruleTemplateUUID").getAsString();
                    Map<String, String> properties = gson.fromJson(jsonObject.get("properties"), HashMap.class);
                    BusinessRule businessRule = new BusinessRuleFromTemplate(uuid, name, templateGroupUUID, type,
                            ruleTemplateUUID, properties);
                    Object[] objects = new Object[2];
                    objects[0] = businessRule;
                    objects[1] = deploymentStatus;
                    list.add(objects);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Retrieving all the business rules from database is failed. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public int executeRetrieveArtifactCountQuery(String uuid) throws BusinessRulesDatasourceException {
        ResultSet resultSet;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = getStatementForRetrievingArtifactCount(conn, uuid);
            resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Retrieving artifact count of the business rule with uuid '" +
                    uuid + "' is failed. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public int executeRetrieveDeploymentStatus(String uuid) throws BusinessRulesDatasourceException {
        ResultSet resultSet;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = getStatementForRetrievingDeploymentStatus(conn, uuid);
            resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Retrieving status of the business rule with uuid '" +
                    uuid + "' is failed. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public boolean executeUpdateArtifactCountQuery(String uuid, int artifactCount)
            throws BusinessRulesDatasourceException {
        boolean result;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = getStatementForUpdatingArtifactCount(conn, uuid, artifactCount);
            if (statement == null) {
                return false;
            }
            result = statement.execute();
            conn.commit();
            return result;
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Updating deployment status of the business rule with uuid '" +
                    uuid +
                    " is failed due to. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public void createTable() throws BusinessRulesDatasourceException {
        Connection conn = null;
        PreparedStatement statement = null;
        boolean result;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants.CREATE_TABLE));
            statement.execute();
            conn.commit();
        } catch (SQLException e) {
            throw new BusinessRulesDatasourceException("Failed to create the table for business rule. ", e);
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    private PreparedStatement getStatementForInsertingBusinessRule(Connection conn, String businessRuleUUID, byte[] businessRule,
                                                                   int deploymentStatus, int artifactCount) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants.
                ADD_BUSINESS_RULE));
        preparedStatement.setString(1, businessRuleUUID);
        preparedStatement.setBytes(2, businessRule);
        preparedStatement.setInt(3, deploymentStatus);
        preparedStatement.setInt(4, artifactCount);
        return preparedStatement;
    }

    private PreparedStatement getStatementForDeletingBusinessRule(Connection conn, String businessRuleUUID) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.DELETE_BUSINESS_RULE));
        preparedStatement.setString(1, businessRuleUUID);
        return preparedStatement;
    }

    private PreparedStatement getStatementForUpdatingBusinessRule(Connection conn, String businessRuleUUID,
                                                                  byte[] newBusinessRule, int deploymentStatus)
            throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.UPDATE_BUSINESS_RULE));
        preparedStatement.setBytes(1, newBusinessRule);
        preparedStatement.setInt(2, deploymentStatus);
        preparedStatement.setString(3, businessRuleUUID);
        return preparedStatement;
    }

    private PreparedStatement getStatementForUpdatingDeploymentStatus(Connection conn, String businessRuleUUID,
                                                                      int deploymentStatus) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.UPDATE_DEPLOYMENT_STATUS));
        preparedStatement.setString(2, businessRuleUUID);
        preparedStatement.setInt(1, deploymentStatus);
        return preparedStatement;
    }

    private PreparedStatement getStatementForRetrievingBusinessRule(Connection conn, String businessRuleUUID)
            throws SQLException {
        PreparedStatement preparedStatement;
        preparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.RETRIEVE_BUSINESS_RULE));
        preparedStatement.setString(1, businessRuleUUID);
        return preparedStatement;
    }

    private PreparedStatement getStatementForRetrievingAllBusinessRules(Connection conn) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.RETRIEVE_ALL));
        return preparedStatement;
    }

    private PreparedStatement getStatementForRetrievingArtifactCount(Connection conn, String businessRuleUUID) throws
            SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants
                .RETRIEVE_ARTIFACT_COUNT));
        preparedStatement.setString(1, businessRuleUUID);
        return preparedStatement;
    }

    private PreparedStatement getStatementForRetrievingDeploymentStatus(Connection conn, String businessRuleUUID)
            throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.RETRIEVE_DEPLOYMENT_STATUS));
        preparedStatement.setString(1, businessRuleUUID);
        return preparedStatement;
    }

    private PreparedStatement getStatementForUpdatingArtifactCount(Connection conn, String businessRuleUUID,
                                                                   int artifactCount) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.UPDATE_ARTIFACT_COUNT));
        preparedStatement.setInt(1, artifactCount);
        preparedStatement.setString(2, businessRuleUUID);
        return preparedStatement;
    }
}
