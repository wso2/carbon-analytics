package org.wso2.carbon.event.simulator.core.internal.bean;

/**
 * DatabaseConnectionDetailsDTO class is used to contain data required to validate a database connection
 */
public class DatabaseConnectionDetailsDTO {

    private String driver;
    private String dataSourceLocation;
    private String username;
    private String password;

    public String getDriver() {
        return driver;
    }
    public String getDataSourceLocation() {
        return dataSourceLocation;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
