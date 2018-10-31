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
import org.wso2.carbon.database.query.manager.QueryProvider;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.exception.ResourceManagerException;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;
import org.wso2.carbon.sp.jobmanager.core.util.ResourceManagerConstants;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private Map<String, String> queries;

    public RDBMSServiceImpl() {
        List<Queries> deploymentQueries = ServiceDataHolder.getDeploymentConfig().getQueries();
        List<Queries> componentQueries = new ArrayList<Queries>();
        URL url = this.getClass().getClassLoader().getResource(ResourceManagerConstants.QUERY_YAML_FILE_NAME);
        if (url != null) {
            DeploymentConfig componentConfigurations = null;
            try {
                componentConfigurations = readYamlContent(url.openStream());
            } catch (IOException e) {
                throw new ResourceManagerException("Unable to read " + ResourceManagerConstants.QUERY_YAML_FILE_NAME +
                        " file.");
            }
            if (componentConfigurations != null) {
                componentQueries = componentConfigurations.getQueries();
            }
        } else {
            throw new ResourceManagerException("Unable to load queries.yaml file from resources.");
        }
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
        Connection conn = null;
        try {
            conn = this.datasource.getConnection();
            queries = QueryProvider.mergeMapping(conn.getMetaData().getDatabaseProductName(),
                    conn.getMetaData().getDatabaseProductVersion(), componentQueries, deploymentQueries);
        } catch (QueryMappingNotAvailableException e) {
            throw new ResourceManagerException("Error in getting the mapping query. Please check queries under " +
                    "deployment.config in deployment.yaml", e);
        } catch (SQLException e) {
            throw new ResourceManagerException("Error when getting connection for SP_MGT_DB datasource", e);
        } finally {
            close(conn, "Closing connection used to get database information.");
        }
        createResourcePoolTable();
    }

    /**
     * Create resource pool persistence table.
     */
    private void createResourcePoolTable() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            try {
                preparedStatement = connection.prepareStatement(
                        queries.get(ResourceManagerConstants.CHECK_FOR_RESOURCE_MAPPING_TABLE));
                preparedStatement.execute();
            } catch (SQLException e) {
                try {
                    // this is due to clean up the connection because postgreSQL will not terminate the execution
                    // by itself so you need to rollback manually. Or it will execute the same query again
                    connection.rollback();
                    preparedStatement = connection.prepareStatement(
                            queries.get(ResourceManagerConstants.CREATE_RESOURCE_MAPPING_TABLE));
                    preparedStatement.execute();
                    if (log.isDebugEnabled()) {
                        log.debug("Resource Mapping Table Created Successfully");
                    }
                } catch (SQLException ex) {
                    throw new ResourceManagerException("Error in executing create resource mapping table query.", ex);
                }
            } finally {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new ResourceManagerException("Error when getting the connection for to create resource mapping " +
                    "table.", e);
        } finally {
            close(preparedStatement, "Execute query when creating resource mapping table");
            close(connection, "Execute query when creating resource mapping table");
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
                preparedStatement = connection.prepareStatement(
                        queries.get(ResourceManagerConstants.PS_DELETE_RESOURCE_MAPPING_ROW));
                preparedStatement.setString(1, resourcePool.getGroupId());
                preparedStatement.executeUpdate();
                close(preparedStatement, "Execute delete mapping row query");
                preparedStatement = connection.prepareStatement(
                        queries.get(ResourceManagerConstants.PS_INSERT_RESOURCE_MAPPING_ROW));
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
            preparedStatement = connection.prepareStatement(
                    queries.get(ResourceManagerConstants.PS_SELECT_RESOURCE_MAPPING_ROW));
            preparedStatement.setString(1, groupId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                byte[] bytes = resultSet.getBytes(2);
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bis);
                Object blobObject = ois.readObject();
                if (blobObject instanceof ResourcePool) {
                    resourcePool = (ResourcePool) blobObject;
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
    private Connection getConnection() throws SQLException {
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

    private DeploymentConfig readYamlContent(InputStream yamlContent) {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(DeploymentConfig.class,
                DeploymentConfig.class.getClassLoader()));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(yamlContent, DeploymentConfig.class);
    }
}
