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

package org.wso2.carbon.sp.jobmanager.core.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.sp.jobmanager.core.exception.ResourceManagerException;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.sp.jobmanager.core.util.ResourceManagerConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * RDBMS service implementation.
 */
public class RDBMSServiceImpl {
    /**
     * The log class
     */
    private static final Log log = LogFactory.getLog(RDBMSServiceImpl.class);
    /**
     * The datasource which is used to be connected to the database.
     */
    private DataSource datasource;

    public RDBMSServiceImpl() {
        String datasourceName = ServiceDataHolder.getDeploymentConfig().getDatasource();
        if (datasourceName == null) {
            throw new ResourceManagerException("No datasource specified to be used with RDBMS based resource pool " +
                    "management. Please check configurations under " + ResourceManagerConstants.DEPLOYMENT_CONFIG_NS);
        }
        DataSourceService dataSourceService = ServiceDataHolder.getDataSourceService();
        try {
            this.datasource = (HikariDataSource) dataSourceService.getDataSource(datasourceName);
            if (log.isDebugEnabled()) {
                log.debug("Datasource " + datasourceName + " configured correctly");
            }
        } catch (DataSourceException e) {
            throw new ResourceManagerException("Error in initializing the datasource " + datasourceName, e);
        }
        createResourcePoolTable();
    }


    /**
     * Create resource pool persistence table.
     */
    private void createResourcePoolTable() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(ResourceManagerConstants.CREATE_RESOURCE_MAPPING_TABLE);
            preparedStatement.execute();
            preparedStatement2 = connection.prepareStatement(ResourceManagerConstants.CREATE_RESOURCE_METRICS_TABLE);
            preparedStatement2.execute();
            preparedStatement3 = connection.prepareStatement(ResourceManagerConstants.CREATE_RESOURCE_SCHEDULING_TABLE);
            preparedStatement3.execute();
            connection.commit();
            if (log.isDebugEnabled()) {
                log.debug("Tables Created Successfully");
            }
        } catch (SQLException e) {
            throw new ResourceManagerException("Error in executing create table queries.", e);
        } finally {
            close(preparedStatement, "Execute query");
            close(connection, "Execute query");
        }
    }

    public void persistResourcePool(ResourcePool resourcePool) throws ResourceManagerException {
        if (resourcePool != null && resourcePool.getGroupId() != null) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            try {
                connection = getConnection();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(resourcePool);
                byte[] resourceMappingAsBytes = byteArrayOutputStream.toByteArray();
                // TODO: 10/31/17 Instead of REPLACE, use DELETE and INSERT
                preparedStatement = connection.prepareStatement(ResourceManagerConstants
                        .PS_REPLACE_RESOURCE_MAPPING_ROW);
                preparedStatement.setString(1, resourcePool.getGroupId());
                preparedStatement.setBinaryStream(2, new ByteArrayInputStream(resourceMappingAsBytes));
                preparedStatement.executeUpdate();
                connection.commit();
                if (log.isDebugEnabled()) {
                    log.debug(ResourceManagerConstants.TASK_UPSERT_RESOURCE_MAPPING + " " +
                            resourcePool.getGroupId() + " executed successfully");
                }
            } catch (SQLException e) {
                rollback(connection, ResourceManagerConstants.TASK_UPSERT_RESOURCE_MAPPING);
                throw new ResourceManagerException("Error occurred while " +
                        ResourceManagerConstants.TASK_UPSERT_RESOURCE_MAPPING
                        + ". Group ID" +
                        resourcePool.getGroupId(), e);
            } catch (IOException e) {
                throw new ResourceManagerException(e);
            } finally {
                close(preparedStatement, ResourceManagerConstants.TASK_UPSERT_RESOURCE_MAPPING);
                close(connection, ResourceManagerConstants.TASK_UPSERT_RESOURCE_MAPPING);
            }
        }
    }

    public ResourcePool getResourcePool(String groupId) throws ResourceManagerException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ResourcePool resourcePool = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(ResourceManagerConstants.PS_SELECT_RESOURCE_MAPPING_ROW);
            preparedStatement.setString(1, groupId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Blob blob = resultSet.getBlob(2);
                if (blob != null) {
                    int blobLength = (int) blob.length();
                    byte[] bytes = blob.getBytes(1, blobLength);
                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    Object blobObject = ois.readObject();
                    if (blobObject instanceof ResourcePool) {
                        resourcePool = (ResourcePool) blobObject;
                    }
                }
            }
            connection.commit();
        } catch (SQLException | ClassNotFoundException | IOException e) {
            throw new ResourceManagerException("Error occurred while " +
                    ResourceManagerConstants.TASK_GET_RESOURCE_MAPPING, e);
        } finally {
            close(resultSet, ResourceManagerConstants.TASK_GET_RESOURCE_MAPPING);
            close(preparedStatement, ResourceManagerConstants.TASK_GET_RESOURCE_MAPPING);
            close(connection, ResourceManagerConstants.TASK_GET_RESOURCE_MAPPING);
        }
        return resourcePool;
    }


    /**
     * Get the connection to the database.
     */
    public Connection getConnection() throws SQLException {
        Connection connection = datasource.getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    /**
     * Close the database connection.
     *
     * @param connection The connection to be closed
     * @param task       The task which was running
     */
    private void close(Connection connection, String task) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Failed to close connection after " + task, e);
        }
    }

    /**
     * Close the prepared statement.
     *
     * @param preparedStatement The statement to be closed
     * @param task              The task which was running
     */
    private void close(PreparedStatement preparedStatement, String task) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Closing prepared statement failed after " + task, e);
            }
        }
    }

    /**
     * Close the resultSet.
     *
     * @param resultSet The resultSet which should be closed
     * @param task      The task which was running
     */
    private void close(ResultSet resultSet, String task) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("Closing result set failed after " + task, e);
            }
        }
    }

    /**
     * The rollback method.
     *
     * @param connection The connection object which the rollback should be applied to
     * @param task       The task which was running
     */
    private void rollback(Connection connection, String task) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                log.warn("Rollback failed on " + task, e);
            }
        }
    }
}
