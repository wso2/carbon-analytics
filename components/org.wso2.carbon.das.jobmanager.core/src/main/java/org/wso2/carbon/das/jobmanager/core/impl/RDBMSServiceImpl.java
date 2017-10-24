package org.wso2.carbon.das.jobmanager.core.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cluster.coordinator.commons.configs.CoordinationPropertyNames;
import org.wso2.carbon.cluster.coordinator.commons.exception.ClusterCoordinationException;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.das.jobmanager.core.model.ResourceMapping;
import org.wso2.carbon.das.jobmanager.core.util.DistributedConstants;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

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
import java.util.Map;
import javax.sql.DataSource;

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

        String datasourceName = ServiceDataHolder.getClusterConfig().getStrategyConfig().getDatasource();
        if (datasourceName == null) {
            throw new ClusterCoordinationException("No datasource specified to be used with RDBMS Coordination " +
                    "Strategy. Please check configurations under " + CoordinationPropertyNames.CLUSTER_CONFIG_NS);
        }
        DataSourceService dataSourceService = ServiceDataHolder.getDataSourceService();
        try {
            this.datasource = (HikariDataSource) dataSourceService.getDataSource(datasourceName);
            if (log.isDebugEnabled()) {
                log.debug("Datasource " + datasourceName + " configured correctly");
            }
        } catch (DataSourceException e) {
            throw new ClusterCoordinationException("Error in initializing the datasource " + datasourceName, e);
        }
        createTables();
    }


    /**
     * Create the tables needed for resource mapping info persistence.
     */
    private void createTables() {
        createResourceMappingTable();
    }

    /**
     * Create resource mapping table.
     */
    private void createResourceMappingTable() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(DistributedConstants.CREATE_RESOURCE_MAPPING_TABLE);
            preparedStatement.execute();
            connection.commit();
            if (log.isDebugEnabled()) {
                log.debug("Resource Mapping Table Created Successfully");
            }
        } catch (SQLException e) {
            throw new ClusterCoordinationException("Error in executing create resource mapping table query.", e);
        } finally {
            close(preparedStatement, "Execute query");
            close(connection, "Execute query");
        }
    }

    public void updateResourceMapping(String groupId, ResourceMapping resourceMapping)
            throws ClusterCoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(resourceMapping);
            byte[] resourceMappingAsBytes = byteArrayOutputStream.toByteArray();
            preparedStatement = connection.prepareStatement(DistributedConstants.PS_REPLACE_RESOURCE_MAPPING_ROW);
            preparedStatement.setString(1, groupId);
            preparedStatement.setBinaryStream(2, new ByteArrayInputStream(resourceMappingAsBytes));
            preparedStatement.executeUpdate();
            connection.commit();
            if (log.isDebugEnabled()) {
                log.debug(DistributedConstants.TASK_UPSERT_RESOURCE_MAPPING +
                        " " + groupId + " executed successfully");
            }
        } catch (SQLException e) {
            rollback(connection, DistributedConstants.TASK_UPSERT_RESOURCE_MAPPING);
            throw new ClusterCoordinationException("Error occurred while " +
                    DistributedConstants.TASK_UPSERT_RESOURCE_MAPPING + ". Group ID" + groupId, e);
        } catch (IOException e) {
            throw new ClusterCoordinationException(e);
        } finally {
            close(preparedStatement, DistributedConstants.TASK_UPSERT_RESOURCE_MAPPING);
            close(connection, DistributedConstants.TASK_UPSERT_RESOURCE_MAPPING);
        }
    }

    public ResourceMapping getResourceMapping(String groupId) throws ClusterCoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ResourceMapping resourceMapping = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(DistributedConstants.PS_SELECT_RESOURCE_MAPPING_ROW);
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
                    if (blobObject instanceof Map) {
                        resourceMapping = (ResourceMapping) blobObject;
                    }
                }
            }
            connection.commit();
        } catch (SQLException | ClassNotFoundException | IOException e) {
            throw new ClusterCoordinationException("Error occurred while " +
                    DistributedConstants.TASK_GET_RESOURCE_MAPPING, e);
        } finally {
            close(resultSet, DistributedConstants.TASK_GET_RESOURCE_MAPPING);
            close(preparedStatement, DistributedConstants.TASK_GET_RESOURCE_MAPPING);
            close(connection, DistributedConstants.TASK_GET_RESOURCE_MAPPING);
        }
        return resourceMapping;
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
}
