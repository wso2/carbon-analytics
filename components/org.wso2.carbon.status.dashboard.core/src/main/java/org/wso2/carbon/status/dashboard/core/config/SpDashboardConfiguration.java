package org.wso2.carbon.status.dashboard.core.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.List;

/**
 * .
 */

@Configuration(namespace = "wso2.status.dashboard", description = "SP Status Dashboard Configuration Parameters")
public class SpDashboardConfiguration {

    public SpDashboardConfiguration(){}

    @Element(description = "polling interval to get real-time statistics of worker")
    private int pollingInterval = 5;

    @Element(description = "Database related configurations")
    private List<DatabaseChildConfiguration> dbConfigurations;


    public int getPollingInterval() {
        return pollingInterval;
    }

    public List<DatabaseChildConfiguration> getDbConfigs() {
        return dbConfigurations;
    }
    public DBQueries getDatabaseConfiguration(String type) {
        for (DatabaseChildConfiguration dbConfig : dbConfigurations) {
            if (dbConfig.getType().equals(type)) {
                return dbConfig.getProperties();
            }
        }
        return null;
    }

}
