package org.wso2.carbon.status.dashboard.core.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

@Configuration(description = "Database configuration")
public class DatabaseChildConfiguration {

    @Element(description = "Database type")
    private String type = "";

    @Element(description = "Database properties")
    private DBQueries properties = new DBQueries();

    public String getType() {
        return type;
    }

    public DBQueries getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "type : " + type + ",  properties : " + properties.toString();
    }
}
