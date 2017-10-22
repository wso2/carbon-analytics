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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.bean.BusinessRule;
import org.wso2.carbon.business.rules.core.bean.scratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.bean.scratch.BusinessRuleFromScratchProperty;
import org.wso2.carbon.business.rules.core.bean.template.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.datasource.util.BusinessRuleDatasourceUtils;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRulesDatasourceException;
import org.wso2.carbon.database.query.manager.QueryManager;

import javax.sql.DataSource;
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


/**
 * Query Executor class
 **/

public class QueryExecutor {
    private DataSource dataSource;
    private QueryManager queryManager;
    private Logger log = LoggerFactory.getLogger(QueryExecutor.class);

    public QueryExecutor() {
        dataSource = DataSourceServiceProvider.getInstance().getDataSource();
        queryManager = DataHolder.getInstance().getQueryManager();
    }

    public boolean executeInsertQuery(String uuid, byte[] businessRule, int deploymentStatus, int artifactCount) {
        boolean result;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = getInsertQuery(conn, uuid, businessRule, deploymentStatus, artifactCount);
            if (statement == null) {
                return false;
            }
            result = statement.execute();
            conn.commit();
            return result;
        } catch (SQLException e) {
            log.error("Inserting business rule: " + uuid + " is failed " +
                    "due to " + e.getMessage
                    (), e);
            return false;
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
            statement = getDeleteQuery(conn, uuid);
            if (statement == null) {
                return false;
            }
            result = statement.execute();
            conn.commit();
            return result;
        } catch (SQLException e) {
            log.error("Deleting business rule with uuid '" + uuid + " is failed due to " + e.getMessage());
            return false;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }
    // TODO: 10/19/17 throw exceptions properly

    public boolean executeUpdateBusinessRuleQuery(String uuid, byte[] newBusinessRule, int deploymentStatus) throws
            BusinessRulesDatasourceException {
        boolean result;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = getUpdateBusinessRuleQuery(conn, uuid, newBusinessRule, deploymentStatus);
            if (statement == null) {
                return false;
            }
            result = statement.execute();
            conn.commit();
            return result;
        } catch (SQLException e) {
            log.error("Updating business rule with uuid '" + uuid + " is failed due to " + e.getMessage());
            return false;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public boolean executeUpdateDeploymentStatusQuery(String uuid, int deploymentStatus) {
        boolean result;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = getUpdateDeploymentStatus(conn, uuid, deploymentStatus);
            if (statement == null) {
                return false;
            }
            result = statement.execute();
            conn.commit();
            return result;
        } catch (SQLException e) {
            log.error("Updating deployment status of the business rule to  with uuid '" + uuid +
                    " is failed due to " + e.getMessage(),e);
            return false;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public BusinessRule retrieveBusinessRule(String uuid) {
        ResultSet resultSet;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = getRetrieveBusinessRule(conn, uuid);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Blob blob = resultSet.getBlob(2);
                byte[] bdata = blob.getBytes(1, (int) blob.length());

                JsonObject jsonObject = new Gson().fromJson(new String(bdata, Charset.forName("UTF-8")), JsonObject
                        .class)
                        .getAsJsonObject();

                String name = jsonObject.get("name").getAsString();
                String templateGroupUUID = jsonObject.get("templateGroupUUID").getAsString();
                String type = jsonObject.get("type").getAsString();
                BusinessRule businessRule;
                if ("scratch".equalsIgnoreCase(type)) {
                    String inputRuleTemplateUUID = jsonObject.get("inputRuleTemplateUUID").getAsString();
                    String outputRuleTemplateUUID = jsonObject.get("outputRuleTemplateUUID").getAsString();
                    BusinessRuleFromScratchProperty properties = new Gson().fromJson(jsonObject.get("properties"),
                            BusinessRuleFromScratchProperty.class);
                    businessRule = new BusinessRuleFromScratch(uuid, name, templateGroupUUID, type,
                            inputRuleTemplateUUID, outputRuleTemplateUUID, properties);
                    return businessRule;
                } else if ("template".equalsIgnoreCase(type)) {
                    String ruleTemplateUUID = jsonObject.get("ruleTemplateUUID").getAsString();
                    Map<String, String> properties = new Gson().fromJson(jsonObject.get("properties"), HashMap.class);
                    businessRule = new BusinessRuleFromTemplate(uuid, name, templateGroupUUID, type,
                            ruleTemplateUUID, properties);
                    return businessRule;
                }
            }
        } catch (SQLException e) {
            log.error("Retrieving the business rule with uuid '" + uuid + "' from database is failed due to " +
                    e.getMessage());
            return null;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
        return null;
    }

    public Map<String, BusinessRule> executeRetrieveAllBusinessRules() {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet;
        Map<String, BusinessRule> map = new HashMap<>();
        try {
            conn = dataSource.getConnection();
            statement = getRetrieveAllBusinessRules(conn);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String businessRuleUUID = resultSet.getString(1);
                Blob blob = resultSet.getBlob(2);
                byte[] bdata = blob.getBytes(1, (int) blob.length());

                JsonObject jsonObject = new Gson().fromJson(new String(bdata, Charset.forName("UTF-8")),
                        JsonObject.class).getAsJsonObject();

                String uuid = jsonObject.get("uuid").getAsString();
                String name = jsonObject.get("name").getAsString();
                String templateGroupUUID = jsonObject.get("templateGroupUUID").getAsString();
                String type = jsonObject.get("type").getAsString();

                if ("scratch".equalsIgnoreCase(type)) {
                    String inputRuleTemplateUUID = jsonObject.get("inputRuleTemplateUUID").getAsString();
                    String outputRuleTemplateUUID = jsonObject.get("outputRuleTemplateUUID").getAsString();
                    BusinessRuleFromScratchProperty properties = new Gson().fromJson(jsonObject.get("properties"),
                            BusinessRuleFromScratchProperty.class);
                    BusinessRule businessRule = new BusinessRuleFromScratch(uuid, name, templateGroupUUID, type,
                            inputRuleTemplateUUID, outputRuleTemplateUUID, properties);
                    map.put(businessRuleUUID, businessRule);
                } else if ("template".equalsIgnoreCase(type)) {
                    String ruleTemplateUUID = jsonObject.get("ruleTemplateUUID").getAsString();
                    Map<String, String> properties = new Gson().fromJson(jsonObject.get("properties"), HashMap.class);
                    BusinessRule businessRule = new BusinessRuleFromTemplate(uuid, name, templateGroupUUID, type,
                            ruleTemplateUUID, properties);
                    map.put(businessRuleUUID, businessRule);
                }
            }
            return map;
        } catch (SQLException e) {
            log.error("Retrieving all the business rules from database is failed due to " + e.getMessage());
            return null;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public List<Object[]> executeRetrieveAllBusinessRulesWithStatus() {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet;
        List<Object[]> list = new ArrayList<>();
        try {
            conn = dataSource.getConnection();
            statement = getRetrieveAllBusinessRules(conn);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Integer deploymentStatus = resultSet.getInt(3);
                Blob blob = resultSet.getBlob(2);
                byte[] bdata = blob.getBytes(1, (int) blob.length());

                JsonObject jsonObject = new Gson().fromJson(new String(bdata, Charset.forName("UTF-8")),
                        JsonObject.class).getAsJsonObject();

                String uuid = jsonObject.get("uuid").getAsString();
                String name = jsonObject.get("name").getAsString();
                String templateGroupUUID = jsonObject.get("templateGroupUUID").getAsString();
                String type = jsonObject.get("type").getAsString();

                if ("scratch".equalsIgnoreCase(type)) {
                    String inputRuleTemplateUUID = jsonObject.get("inputRuleTemplateUUID").getAsString();
                    String outputRuleTemplateUUID = jsonObject.get("outputRuleTemplateUUID").getAsString();
                    BusinessRuleFromScratchProperty properties = new Gson().fromJson(jsonObject.get("properties"),
                            BusinessRuleFromScratchProperty.class);
                    BusinessRule businessRule = new BusinessRuleFromScratch(uuid, name, templateGroupUUID, type,
                            inputRuleTemplateUUID, outputRuleTemplateUUID, properties);
                    Object[] objects = new Object[2];
                    objects[0] = businessRule;
                    objects[1] = deploymentStatus;
                    list.add(objects);
                } else if ("template".equalsIgnoreCase(type)) {
                    String ruleTemplateUUID = jsonObject.get("ruleTemplateUUID").getAsString();
                    Map<String, String> properties = new Gson().fromJson(jsonObject.get("properties"), HashMap.class);
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
            log.error("Retrieving all the business rules from database is failed due to " + e.getMessage());
            return null;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    public int executeRetrieveArtifactCountQuery(String uuid) {
        ResultSet resultSet;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = getArtifactCountPreparedStatement(conn, uuid);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Retrieving the business rule with uuid '" + uuid + "' from database is failed due to " +
                    e.getMessage());
            return -1;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
        return -1;
    }

    public boolean createTable() throws SQLException {
        Connection conn = null;
        PreparedStatement statement = null;
        boolean result;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants.CREATE_TABLE));
            result = statement.execute();
            conn.commit();
            return result;
        } finally {
            BusinessRuleDatasourceUtils.cleanupConnection(null, statement, conn);
        }
    }

    private PreparedStatement getInsertQuery(Connection conn, String businessRuleUUID, byte[] businessRule,
                                             int deploymentStatus, int artifactCount) {
        PreparedStatement insertPreparedStatement = null;
        try {
            insertPreparedStatement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants.
                    ADD_BUSINESS_RULE));
            insertPreparedStatement.setString(1, businessRuleUUID);
            insertPreparedStatement.setBytes(2, businessRule);
            insertPreparedStatement.setInt(3, deploymentStatus);
            insertPreparedStatement.setInt(4, artifactCount);
            return insertPreparedStatement;
        } catch (SQLException e) {
            log.error("Failed to create prepared statement due to " + e.getMessage(), e);
            return null;
        }
    }

    private PreparedStatement getDeleteQuery(Connection conn, String businessRuleUUID) {
        PreparedStatement deletePreparedStatement = null;
        try {
            deletePreparedStatement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants.
                    DELETE_BUSINESS_RULE));
            deletePreparedStatement.setString(1, businessRuleUUID);
            return deletePreparedStatement;
        } catch (SQLException e) {
            log.error("Failed to create prepared statement due to " + e.getMessage(), e);
            return null;
        }
    }

    private PreparedStatement getUpdateBusinessRuleQuery(Connection conn, String businessRuleUUID,
                                                         byte[] newBusinessRule, int deploymentStatus) {
        PreparedStatement updateBRPreparedStatement = null;
        try {
            updateBRPreparedStatement = conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.UPDATE_BUSINESS_RULE));
            updateBRPreparedStatement.setBytes(1, newBusinessRule);
            updateBRPreparedStatement.setInt(2, deploymentStatus);
            updateBRPreparedStatement.setString(3, businessRuleUUID);
            return updateBRPreparedStatement;
        } catch (SQLException e) {
            log.error("Failed to create prepared statement due to " + e.getMessage(), e);
            return null;
        }

    }

    private PreparedStatement getUpdateDeploymentStatus(Connection conn, String businessRuleUUID,
                                                        int deploymentStatus) {
        PreparedStatement updateBRPreparedStatement = null;
        try {
            updateBRPreparedStatement = conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.UPDATE_DEPLOYMENT_STATUS));
            updateBRPreparedStatement.setString(2, businessRuleUUID);
            updateBRPreparedStatement.setInt(1, deploymentStatus);
            return updateBRPreparedStatement;
        } catch (SQLException e) {
            log.error("Failed to create prepared statement due to " + e.getMessage(), e);
            return null;
        }
    }

    private PreparedStatement getRetrieveBusinessRule(Connection conn, String businessRuleUUID) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(queryManager
                    .getQuery(DatasourceConstants.RETRIEVE_BUSINESS_RULE));
            preparedStatement.setString(1, businessRuleUUID);
            return preparedStatement;
        } catch (SQLException e) {
            BusinessRuleDatasourceUtils.cleanupConnection(null, preparedStatement, conn);
            return null;
        }
    }

    private PreparedStatement getRetrieveAllBusinessRules(Connection conn) throws SQLException {
        PreparedStatement preparedStatement;
        preparedStatement = conn.prepareStatement(queryManager
                .getQuery(DatasourceConstants.RETRIEVE_ALL));
        return preparedStatement;
    }

    private PreparedStatement getArtifactCountPreparedStatement(Connection conn, String businessRuleUUID) throws
            SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(queryManager.getQuery(DatasourceConstants
                .RETRIEVE_ARTIFACT_COUNT));
        preparedStatement.setString(1, businessRuleUUID);
        return preparedStatement;
    }
}
