package org.wso2.carbon.event.output.adaptor.mysql.internal;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class PooledDataSource {

    private DataSource dataSource;

    public PooledDataSource(String driverClassName, String url, String username, String password) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class.forName(driverClassName).newInstance();
        GenericObjectPool connectionPool = new GenericObjectPool(null);
        connectionPool.setMaxActive(10);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, username, password);
        new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
        dataSource = new org.apache.commons.dbcp.PoolingDataSource(connectionPool);
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        return dataSource.getConnection();
    }
}
