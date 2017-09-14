package org.wso2.carbon.status.dashboard.core.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Map;

@Configuration(description = "Database configuration")
public class DatabaseChildConfiguration {

    @Element(description = "Database type")
    private String type = "";

    @Element(description = "Database properties")
    private Map<String, String> properties;

    @Element(description = "Database type mappings")
    private Map<String, String> typeMappings;

    public String getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, String> getTypeMappings() {
        return typeMappings;
    }

    @Override
    public String toString() {
        return "type : " + type + ",  properties : " + properties.toString();
    }
}
