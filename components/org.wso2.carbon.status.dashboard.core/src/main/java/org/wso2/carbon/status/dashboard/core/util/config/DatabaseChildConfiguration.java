package org.wso2.carbon.status.dashboard.core.util.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Map;

@Configuration(description = "Database configuration")
public class DatabaseChildConfiguration {

    @Element(description = "Database type")
    private String type = "";

    @Element(description = "Database properties")
    private Map<String, String> properties;

    public String getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "type : " + type + ",  properties : " + properties.toString();
    }
}
